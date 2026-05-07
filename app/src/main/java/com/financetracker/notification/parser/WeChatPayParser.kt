package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class WeChatPayParser : NotificationParser {
    override val supportedPackages = listOf("com.tencent.mm")

    // 匹配 "¥12.50" 或 "￥12.50" 格式（有/无前缀文字）
    private val amountSymbolPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    // 匹配 "12.50元" 格式（无货币符号时）
    private val amountYuanPattern = Regex("(\\d+\\.?\\d*)\\s*元")
    // 匹配 "收款方: xxx"
    private val merchantPattern = Regex("收款方[:：]\\s*(.+)")
    // 匹配 "向(.+?)付款"
    private val merchantPayPattern = Regex("(?:向|给)(.+?)(?:付款|收款)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        if (packageName != "com.tencent.mm") return null
        val combined = "$title $text"

        var amount = amountSymbolPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = amountYuanPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        }
        if (amount == null) return null

        val merchant = merchantPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: merchantPayPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: "微信支付"

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "wechat",
            payTime = System.currentTimeMillis(),
        )
    }
}
