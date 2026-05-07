package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

class JDPayParser : NotificationParser {
    override val supportedPackages = listOf("com.jingdong.app.mall")

    private val amountPattern = Regex("[¥￥](\\d+\\.?\\d*)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        if (packageName != "com.jingdong.app.mall") return null
        val combined = "$title $text"

        val amountMatch = amountPattern.find(combined) ?: return null
        val amount = amountMatch.groupValues[1].toDoubleOrNull() ?: return null

        return ParsedNotification(
            amount = amount,
            merchant = "京东商城",
            accountType = "jd",
            payTime = System.currentTimeMillis(),
        )
    }
}
