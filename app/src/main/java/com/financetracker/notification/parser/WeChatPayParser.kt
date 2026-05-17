package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification
import com.financetracker.domain.model.TransactionType

class WeChatPayParser : NotificationParser {
    override val supportedPackages = listOf("com.tencent.mm")

    // 收入关键词
    private val incomeKeywords = listOf("收款", "入账", "到账", "转入", "收到", "红包", "退款", "提现到账")
    // 支出关键词
    private val expenseKeywords = listOf("支付", "付款", "消费", "转出", "提现", "转账")

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

        // Require payment context: must NOT be a regular chat message
        val paymentKeywords = listOf("支付", "付款", "收款", "账单", "扣款", "消费", "交易", "提现", "退款")
        val hasPaymentContext = title == "微信支付" || title.contains("支付")
                || title.contains("付款") || title.contains("收款") || title.contains("转账")
                || paymentKeywords.any { it in text }
        if (!hasPaymentContext) return null

        val combined = "$title $text"

        var amount = amountSymbolPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = amountYuanPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        }
        if (amount == null) return null

        // 判断收入还是支出
        val isIncome = incomeKeywords.any { combined.contains(it) }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        val merchant = merchantPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: merchantPayPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: if (type == TransactionType.INCOME) "微信收入" else "微信"

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "wechat",
            transactionType = type,
            payTime = System.currentTimeMillis(),
        )
    }
}
