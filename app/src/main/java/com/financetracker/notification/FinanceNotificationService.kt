package com.financetracker.notification

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.financetracker.MainActivity
import com.financetracker.domain.model.TransactionType
import com.financetracker.notification.classifier.AutoClassifier
import com.financetracker.notification.parser.*
import kotlinx.coroutines.*

class FinanceNotificationService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val parsers: List<NotificationParser> = listOf(
        WeChatPayParser(),
        AlipayParser(),
        JDPayParser(),
        BankCardParser(),
    )
    private val classifier = AutoClassifier()
    private val screenParser = ScreenContentParser()

    private var lastScreenCaptureTime = 0L
    private var lastScreenCaptureHash = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val parser = parsers.firstOrNull { packageName in it.supportedPackages } ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> handleNotification(event, parser, packageName)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> handleWindowContent(event, packageName)
        }
    }

    private fun handleNotification(event: AccessibilityEvent, parser: NotificationParser, packageName: String) {
        val notif = event.parcelableData as? Notification
        val title = notif?.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = notif?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = notif?.extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

        val fullText = listOf(title, text, subText).filter { it.isNotBlank() }.joinToString(" ")
        val finalText = fullText.ifBlank {
            event.text.joinToString(" ") { it.toString() }
        }

        if (finalText.isBlank()) return

        scope.launch {
            val parsed = parser.parse(packageName, title, finalText)
            if (parsed != null) {
                recordTransaction(parsed)
                showNotification("已自动记账", "${parsed.merchant} ¥${String.format("%.2f", parsed.amount)}")
            } else {
                // Parsing failed — check if it's a payment notification that needs manual recording
                val hasPayHint = listOf("支付", "交易", "付款", "账单").any { it in title || it in text }
                if (hasPayHint) {
                    showReminderNotification(packageName)
                }
            }
        }
    }

    private fun handleWindowContent(event: AccessibilityEvent, packageName: String) {
        // Deduplicate: ignore events within 2s with same content hash
        val now = System.currentTimeMillis()
        val source = event.source
        val contentHash = listNotNullTexts(source).hashCode()

        if (now - lastScreenCaptureTime < 2000 && contentHash == lastScreenCaptureHash) return

        scope.launch {
            val parsed = screenParser.parse(packageName, source)
            if (parsed != null) {
                lastScreenCaptureTime = now
                lastScreenCaptureHash = contentHash
                recordTransaction(parsed)
                showNotification("已自动记账(屏幕)", "${parsed.merchant} ¥${String.format("%.2f", parsed.amount)}")
            }
        }
    }

    private fun listNotNullTexts(node: android.view.accessibility.AccessibilityNodeInfo?): List<String> {
        val result = mutableListOf<String>()
        if (node == null) return result
        collectNodeText(node, result, 50)
        return result
    }

    private fun collectNodeText(node: android.view.accessibility.AccessibilityNodeInfo, result: MutableList<String>, max: Int) {
        if (result.size >= max) return
        node.text?.toString()?.trim()?.takeIf { it.length > 1 }?.let { result.add(it) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.length > 1 }?.let { result.add(it) }
        for (i in 0 until node.childCount) {
            if (result.size >= max) break
            node.getChild(i)?.let { collectNodeText(it, result, max) }
        }
    }

    private suspend fun recordTransaction(parsed: com.financetracker.domain.model.ParsedNotification) {
        val db = com.financetracker.FinanceTrackerApp.instance.database
        val account = db.paymentAccountDao().getByType(parsed.accountType) ?: return
        val categoryId = classifier.classify(parsed.merchant)

        val transaction = com.financetracker.domain.model.Transaction(
            amount = parsed.amount,
            type = TransactionType.EXPENSE,
            categoryId = categoryId,
            accountId = account.id,
            merchant = parsed.merchant,
            date = parsed.payTime,
            source = "notification",
        )
        com.financetracker.di.AppModule.transactionRepository.add(transaction)
    }

    private fun showNotification(title: String, content: String) {
        val channelId = "auto_accounting"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "自动记账通知", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }

    private fun showReminderNotification(packageName: String) {
        val appName = when (packageName) {
            "com.tencent.mm" -> "微信"
            "com.eg.android.AlipayGphone" -> "支付宝"
            "com.jingdong.app.mall" -> "京东"
            else -> "支付应用"
        }
        val channelId = "reminder"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "记账提醒", NotificationManager.IMPORTANCE_HIGH))
        }
        val intent = Intent(this, MainActivity::class.java).apply { putExtra("navigate_to_add", true) }
        val pi = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("检测到${appName}支付通知")
            .setContentText("点击手动记账")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt() + 1, notif)
    }

    override fun onInterrupt() {}
}
