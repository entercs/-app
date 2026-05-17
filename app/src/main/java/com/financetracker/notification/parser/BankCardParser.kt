package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification
import com.financetracker.domain.model.TransactionType

class BankCardParser : NotificationParser {
    override val supportedPackages = listOf(
        "com.icbc",                  // 工商银行
        "com.chinamworld.bocmbci",   // 中国银行
        "com.android.bankabc",       // 农业银行
        "com.chinamworld.main",      // 建设银行
        "cmb.pb",                    // 招商银行
        "com.bankcomm.maidanba",     // 交通银行
        "com.psbc.mobilebank",       // 邮储银行
        "cn.gov.pbc.dcep",           // 数字人民币
    )

    // 收入关键词
    private val incomeKeywords = listOf("收款", "入账", "到账", "转入", "存入", "收入", "奖励", "红包", "退款", "提现到账")
    // 支出关键词
    private val expenseKeywords = listOf("消费", "支付", "扣款", "支出", "转出", "提现", "还款", "刷卡", "快捷支付")

    // 匹配 "¥12.50" 格式
    private val amountSymbolPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    // 匹配 "12.50元" 格式
    private val amountYuanPattern = Regex("(\\d+\\.?\\d*)\\s*元")
    // 匹配尾号
    private val cardNoPattern = Regex("[尾号账户]+[为:：]?\\*?(\\d{4,6})")
    // 商户提取：在...消费/在...支付/在...扣款
    private val merchantAtPattern = Regex("在(.+?)(?:消费|支付|扣款|收款|入账)")
    // 商户提取：商户:xxx / 收款方:xxx
    private val merchantColonPattern = Regex("(?:商户|收款方|交易对方)[:：]\\s*(.+)")
    // 日期提取
    private val datePattern = Regex("(\\d{1,4}[年/]\\d{1,2}[月/]\\d{1,2}[日]?\\s*\\d{1,2}:\\d{1,2})")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        if (packageName !in supportedPackages) return null

        val combined = "$title $text"

        // 提取金额
        var amount = amountSymbolPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = amountYuanPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        }
        if (amount == null || amount == 0.0) return null

        // 判断收入还是支出
        val isIncome = incomeKeywords.any { combined.contains(it) }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // 提取商户
        val merchant = merchantColonPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: merchantAtPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: if (type == TransactionType.INCOME) "银行卡收入" else "银行卡消费"

        // 提取日期（如果解析失败会用当前时间）
        val payTime = datePattern.find(combined)?.groupValues?.get(1)?.trim()?.let {
            try {
                parseChineseDate(it)
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        } ?: System.currentTimeMillis()

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "bank",
            transactionType = type,
            payTime = payTime,
        )
    }

    private fun parseChineseDate(dateStr: String): Long {
        // 支持格式：2024年1月15日 14:30 或 2024/1/15 14:30
        val cleaned = dateStr.replace("年", "/").replace("月", "/").replace("日", "").trim()
        val parts = cleaned.split(" ", "/")
        if (parts.size >= 5) {
            val year = parts[0].toIntOrNull() ?: return System.currentTimeMillis()
            val month = parts[1].toIntOrNull() ?: return System.currentTimeMillis()
            val day = parts[2].toIntOrNull() ?: return System.currentTimeMillis()
            val timeParts = parts[4].split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val cal = java.util.Calendar.getInstance()
            cal.set(year, month - 1, day, hour, minute, 0)
            return cal.timeInMillis
        }
        return System.currentTimeMillis()
    }
}
