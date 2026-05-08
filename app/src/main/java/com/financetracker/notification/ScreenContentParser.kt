package com.financetracker.notification

import android.view.accessibility.AccessibilityNodeInfo
import com.financetracker.domain.model.ParsedNotification

class ScreenContentParser {

    private val amountPatterns = listOf(
        Regex("[¥￥](\\d+\\.?\\d*)"),
        Regex("(\\d+\\.?\\d*)\\s*元"),
        Regex("金额[:：]?\\s*[¥￥]?(\\d+\\.?\\d*)"),
    )
    private val merchantPatterns = listOf(
        Regex("收款方[:：]\\s*(.+)"),
        Regex("商户[:：]\\s*(.+)"),
        Regex("你在(.+?)有一笔"),
        Regex("向(.+?)(?:付款|支付|转账)"),
        Regex("(.+?)(?:收款|付款成功|支付成功)"),
    )

    private val payKeywords = listOf("支付", "付款", "收款", "交易", "账单", "消费", "扣款")

    /** Check if the text contains any payment-related keywords */
    fun hasPayKeywords(texts: List<String>): Boolean {
        return texts.any { text -> payKeywords.any { kw -> kw in text } }
    }

    /** Parse payment details from window content */
    fun parse(packageName: String, root: AccessibilityNodeInfo?): ParsedNotification? {
        if (root == null) return null
        val texts = collectTexts(root, maxNodes = 80)
        if (texts.isEmpty()) return null
        if (!hasPayKeywords(texts)) return null

        val combined = texts.joinToString(" ")

        val amount = amountPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        } ?: return null

        val merchant = merchantPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(combined)?.groupValues?.get(1)?.trim()
        } ?: ""

        val accountType = when {
            packageName.contains("tencent.mm") -> "wechat"
            packageName.contains("Alipay") || packageName.contains("alipay") -> "alipay"
            packageName.contains("jingdong") -> "jd"
            else -> "bank"
        }

        return ParsedNotification(
            amount = amount,
            merchant = merchant.take(30).ifBlank { "${appName(packageName)}支付" },
            accountType = accountType,
            payTime = System.currentTimeMillis(),
        )
    }

    private fun collectTexts(node: AccessibilityNodeInfo, maxNodes: Int, depth: Int = 0): List<String> {
        val result = mutableListOf<String>()
        var count = 0
        collectRecursive(node, result, depth, maxNodes, count)
        // Update count isn't easily mutable in recursion. Let's use a simpler approach.
        return result
    }

    private fun collectRecursive(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>,
        depth: Int,
        maxNodes: Int,
        currentCount: Int,
    ): Int {
        if (node == null) return currentCount
        if (result.size >= maxNodes) return currentCount

        var count = currentCount
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank() && text.length > 1) {
            result.add(text)
            count++
        }
        val contentDesc = node.contentDescription?.toString()?.trim()
        if (!contentDesc.isNullOrBlank() && contentDesc.length > 1 && contentDesc != text) {
            result.add(contentDesc)
            count++
        }

        for (i in 0 until node.childCount) {
            if (result.size >= maxNodes) break
            count = collectRecursive(node.getChild(i), result, depth + 1, maxNodes, count)
        }
        return count
    }

    private fun appName(packageName: String) = when (packageName) {
        "com.tencent.mm" -> "微信"
        "com.eg.android.AlipayGphone" -> "支付宝"
        "com.jingdong.app.mall" -> "京东"
        else -> ""
    }
}
