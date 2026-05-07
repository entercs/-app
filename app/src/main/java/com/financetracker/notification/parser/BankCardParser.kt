package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class BankCardParser : NotificationParser {
    override val supportedPackages = listOf(
        "com.android.mms",
        "com.icbc",
        "com.chinamworld.bocmbci",
        "com.android.bankabc",
        "com.chinamworld.main",
        "cmb.pb",
        "com.bankcomm.maidanba",
    )

    private val amountYuanPattern = Regex("(\\d+\\.?\\d*)\\s*元")
    private val amountSymbolPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    private val merchantPattern = Regex("(?:商户|收款方)[:：]\\s*(.+)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        val combined = "$title $text"

        var amount = amountYuanPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = combined.lines()
                .firstOrNull { it.contains("元") || it.contains("¥") || it.contains("￥") }
                ?.let { amountSymbolPattern.find(it)?.groupValues?.get(1)?.toDoubleOrNull() }
        }
        if (amount == null) return null

        val merchant = merchantPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: "银行卡消费"

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "bank",
            payTime = System.currentTimeMillis(),
        )
    }
}
