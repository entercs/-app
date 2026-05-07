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
import com.financetracker.data.db.entity.TransactionEntity
import com.financetracker.notification.classifier.AutoClassifier
import com.financetracker.notification.parser.AlipayParser
import com.financetracker.notification.parser.BankCardParser
import com.financetracker.notification.parser.JDPayParser
import com.financetracker.notification.parser.NotificationParser
import com.financetracker.notification.parser.WeChatPayParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FinanceNotificationService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val parsers: List<NotificationParser> = listOf(
        WeChatPayParser(),
        AlipayParser(),
        JDPayParser(),
        BankCardParser(),
    )
    private val classifier = AutoClassifier()

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Extract real notification content via parcelableData (not event.text which is ticker only)
        val notif = event.parcelableData as? Notification
        val title = notif?.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: notif?.extras?.getString(Notification.EXTRA_TITLE) ?: ""
        val text = notif?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: notif?.extras?.getString(Notification.EXTRA_TEXT) ?: ""
        val subText = notif?.extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: notif?.extras?.getString(Notification.EXTRA_SUB_TEXT) ?: ""

        // Fallback to event.text if parcelableData didn't yield anything
        val fullText = listOf(title, text, subText).filter { it.isNotBlank() }.joinToString(" ")
        val finalText = if (fullText.isNotBlank()) fullText else
            event.text.joinToString(" ") { it.toString() }

        if (finalText.isBlank()) return

        val parser = parsers.firstOrNull { packageName in it.supportedPackages } ?: return

        scope.launch {
            val parsed = parser.parse(packageName, title, finalText) ?: return@launch

            val db = com.financetracker.FinanceTrackerApp.instance.database
            val accountDao = db.paymentAccountDao()
            val account = accountDao.getByType(parsed.accountType) ?: return@launch

            val categoryId = classifier.classify(parsed.merchant)

            val transaction = com.financetracker.domain.model.Transaction(
                amount = parsed.amount,
                type = com.financetracker.domain.model.TransactionType.EXPENSE,
                categoryId = categoryId,
                accountId = account.id,
                merchant = parsed.merchant,
                date = parsed.payTime,
                source = "notification",
            )
            com.financetracker.di.AppModule.transactionRepository.add(transaction)

            showNotification(parsed)
        }
    }

    private fun showNotification(parsed: com.financetracker.domain.model.ParsedNotification) {
        val channelId = "auto_accounting"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "自动记账通知",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("已自动记账")
            .setContentText("${parsed.merchant} ¥${String.format("%.2f", parsed.amount)}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onInterrupt() {}
}
