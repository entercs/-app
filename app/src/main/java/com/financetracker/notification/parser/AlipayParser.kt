package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class AlipayParser : NotificationParser {
    override val supportedPackages = listOf("com.eg.android.AlipayGphone")

    private val amountPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    private val merchantPattern = Regex("收款方[:：]\\s*(.+)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        if (packageName != "com.eg.android.AlipayGphone") return null
        val combined = "$title $text"

        val amountMatch = amountPattern.find(combined) ?: return null
        val amount = amountMatch.groupValues[1].toDoubleOrNull() ?: return null

        val merchant = merchantPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: "支付宝支付"

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "alipay",
            payTime = System.currentTimeMillis(),
        )
    }
}
