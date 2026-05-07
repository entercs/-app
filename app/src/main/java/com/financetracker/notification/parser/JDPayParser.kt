package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class JDPayParser : NotificationParser {
    override val supportedPackages = listOf("com.jingdong.app.mall")

    private val amountSymbolPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    private val amountYuanPattern = Regex("(\\d+\\.?\\d*)\\s*元")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        if (packageName != "com.jingdong.app.mall") return null
        val combined = "$title $text"

        var amount = amountSymbolPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = amountYuanPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        }
        if (amount == null) return null

        return ParsedNotification(
            amount = amount,
            merchant = "京东商城",
            accountType = "jd",
            payTime = System.currentTimeMillis(),
        )
    }
}
