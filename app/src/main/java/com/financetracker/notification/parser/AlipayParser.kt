package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class AlipayParser : NotificationParser {
    override val supportedPackages = listOf(
        "com.eg.android.AlipayGphone",
        "com.eg.android.AlipayGphoneRC",
        "com.eg.android.AlipayGphoneGlobal",
        "hk.alipay.wallet",
    )

    // 匹配 "12.50元" 或 "12元" 格式
    private val amountYuanPattern = Regex("(\\d+\\.?\\d*)\\s*元")
    // 匹配 "¥12.50" 或 "￥12.50" 格式（兼容旧格式）
    private val amountSymbolPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    // 匹配 "你在某某有一笔" 格式
    private val merchantInlinePattern = Regex("你在(.+?)有一笔")
    // 匹配 "收款方: xxx" 格式（兼容旧格式）
    private val merchantTagPattern = Regex("收款方[:：]\\s*(.+)")
    // 匹配 "向(.+?)付款" 或 "给(.+?)转账" 等
    private val merchantPayPattern = Regex("(?:向|给|转给)(.+?)(?:付款|转账|支付)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        if (packageName != "com.eg.android.AlipayGphone") return null
        val combined = "$title $text"

        // Extract amount - try 元 format first, then ¥ format
        var amount = amountYuanPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = amountSymbolPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        }
        if (amount == null) return null

        // Extract merchant - try multiple patterns
        val merchant = merchantInlinePattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: merchantTagPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: merchantPayPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: "支付宝支付"

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "alipay",
            payTime = System.currentTimeMillis(),
        )
    }
}
