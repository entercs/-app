package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class WeChatPayParser : NotificationParser {
    override val supportedPackages = listOf("com.tencent.mm")

    private val amountPattern = Regex("(?:支付|收款|退款)\\s*[¥￥](\\d+\\.?\\d*)")
    private val merchantPattern = Regex("收款方[:：]\\s*(.+)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        if (packageName != "com.tencent.mm") return null
        val combined = "$title $text"

        val amountMatch = amountPattern.find(combined) ?: return null
        val amount = amountMatch.groupValues[1].toDoubleOrNull() ?: return null

        val merchant = merchantPattern.find(combined)?.groupValues?.get(1)?.trim() ?: "微信支付"

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "wechat",
            payTime = System.currentTimeMillis(),
        )
    }
}
