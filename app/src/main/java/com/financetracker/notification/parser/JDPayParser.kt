package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification
import com.financetracker.domain.model.TransactionType

class JDPayParser : NotificationParser {
    override val supportedPackages = listOf("com.jingdong.app.mall")

    // 收入关键词
    private val incomeKeywords = listOf("收款", "入账", "到账", "退款", "返")
    // 支出关键词
    private val expenseKeywords = listOf("支付", "消费", "购买")

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

        val isIncome = incomeKeywords.any { combined.contains(it) }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        return ParsedNotification(
            amount = amount,
            merchant = if (type == TransactionType.INCOME) "京东收入" else "京东",
            accountType = "jd",
            transactionType = type,
            payTime = System.currentTimeMillis(),
        )
    }
}
