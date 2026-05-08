package com.financetracker.notification

import android.view.accessibility.AccessibilityNodeInfo
import com.financetracker.domain.model.ParsedNotification

class ScreenContentParser {

    private val amountPatterns = listOf(
        Regex("¥(\\d+\\.?\\d*)"),
        Regex("￥(\\d+\\.?\\d*)"),
        Regex("(\\d+\\.?\\d*)\\s*元"),
    )

    // Must contain at least one of these to be considered a payment screen
    private val paySuccessKeywords = listOf(
        "支付成功", "付款成功", "交易成功", "付款结果", "支付结果",
        "扣款成功", "消费成功", "付款完成", "支付完成", "交易完成",
    )

    // Reject merchant names containing these garbage keywords
    private val garbageKeywords = listOf(
        "刷新", "搜索", "天气", "℃", "车险", "更多操作", "松开",
        "首页", "我的", "扫一扫", "收付款", "余额", "理财",
        "消息", "通知", "设置", "返回", "确认", "取消",
    )

    fun hasPayKeywords(texts: List<String>): Boolean {
        return texts.any { text -> paySuccessKeywords.any { kw -> kw in text } }
    }

    fun parse(packageName: String, root: AccessibilityNodeInfo?): ParsedNotification? {
        if (root == null) return null
        val texts = collectTexts(root, maxNodes = 80)
        if (texts.isEmpty()) return null

        // STRICT: must contain payment SUCCESS keywords (not just "支付")
        if (!hasPayKeywords(texts)) return null

        val combined = texts.joinToString(" ")

        val amount = amountPatterns.firstNotNullOfOrNull { pattern ->
            pattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        } ?: return null

        // STRICT: amount must be reasonable
        if (amount <= 0.01 || amount > 100000) return null

        // Extract merchant — try known patterns
        var merchant = ""
        val merchantPatterns = listOf(
            Regex("收款方[:：]\\s*(.+)"),
            Regex("商户[:：]\\s*(.+)"),
            Regex("你在(.+?)有一笔"),
            Regex("向(.+?)(?:付款|支付|转账)"),
        )
        for (p in merchantPatterns) {
            val m = p.find(combined)?.groupValues?.get(1)?.trim()
            if (m != null && m.length in 2..20) {
                merchant = m
                break
            }
        }

        // Quality check: reject garbage merchants
        if (merchant.isNotBlank() && garbageKeywords.any { it in merchant }) {
            merchant = ""
        }

        // If no good merchant found, find lines with amount nearby
        if (merchant.isBlank()) {
            val amountIdx = combined.indexOf("${String.format("%.2f", amount)}")
            if (amountIdx > 0) {
                val before = combined.substring(0, amountIdx).trim()
                val candidate = before.takeLast(30).trim()
                if (candidate.isNotBlank() && candidate.length in 2..20 && garbageKeywords.none { it in candidate }) {
                    merchant = candidate
                }
            }
        }

        if (merchant.isBlank()) {
            merchant = when {
                packageName.contains("Alipay") -> "支付宝支付"
                packageName.contains("tencent") -> "微信支付"
                else -> "支付"
            }
        }

        val accountType = when {
            packageName.contains("tencent.mm") -> "wechat"
            packageName.contains("Alipay") || packageName.contains("alipay") -> "alipay"
            packageName.contains("jingdong") -> "jd"
            else -> "bank"
        }

        return ParsedNotification(amount, merchant, accountType, System.currentTimeMillis())
    }

    private fun collectTexts(node: AccessibilityNodeInfo, maxNodes: Int): List<String> {
        val result = mutableListOf<String>()
        collectRecursive(node, result, maxNodes)
        return result
    }

    private fun collectRecursive(node: AccessibilityNodeInfo?, result: MutableList<String>, max: Int) {
        if (node == null || result.size >= max) return
        node.text?.toString()?.trim()?.takeIf { it.length in 2..40 }?.let { result.add(it) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.length in 2..40 }?.let { result.add(it) }
        for (i in 0 until node.childCount) {
            if (result.size >= max) break
            collectRecursive(node.getChild(i), result, max)
        }
    }
}
