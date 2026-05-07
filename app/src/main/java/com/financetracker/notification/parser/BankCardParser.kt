package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class BankCardParser : NotificationParser {
    override val supportedPackages = listOf(
        "com.android.mms",         // SMS
        "com.icbc",                // 工商银行
        "com.chinamworld.bocmbci", // 中国银行
        "com.android.bankabc",     // 农业银行
        "com.chinamworld.main",    // 建设银行
        "cmb.pb",                  // 招商银行
        "com.bankcomm.maidanba",   // 交通银行
    )

    private val amountPattern = Regex("(?:支出|消费|交易|扣款).*?[¥￥]?(\\d+\\.?\\d*)\\s*元")
    private val simpleAmountPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    private val merchantPattern = Regex("(?:商户|收款方)[:：]\\s*(.+)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        val combined = "$title $text"

        var amount = amountPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = combined.lines()
                .firstOrNull { it.contains("元") || it.contains("¥") || it.contains("￥") }
                ?.let { simpleAmountPattern.find(it)?.groupValues?.get(1)?.toDoubleOrNull() }
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
