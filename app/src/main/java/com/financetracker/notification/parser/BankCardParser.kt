package com.financetracker.notification.parser

import com.financetracker.domain.model.ParsedNotification

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

    // 匹配 "¥12.50" 格式
    private val amountSymbolPattern = Regex("[¥￥](\\d+\\.?\\d*)")
    // 匹配 "12.50元" 格式
    private val amountYuanPattern = Regex("(\\d+\\.?\\d*)\\s*元")
    // 匹配商户
    private val merchantPattern = Regex("(?:商户|收款方|交易对方)[:：]\\s*(.+)")
    private val merchantAtPattern = Regex("在(.+?)(?:消费|支付|付款|扣款)")

    override fun parse(packageName: String, title: String, text: String): ParsedNotification? {
        // 排除非银行 App
        if (packageName !in supportedPackages) return null

        val combined = "$title $text"

        // 提取金额
        var amount = amountSymbolPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        if (amount == null) {
            amount = amountYuanPattern.find(combined)?.groupValues?.get(1)?.toDoubleOrNull()
        }
        if (amount == null) return null

        // 提取商户
        val merchant = merchantPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: merchantAtPattern.find(combined)?.groupValues?.get(1)?.trim()
            ?: "银行卡消费"

        return ParsedNotification(
            amount = amount,
            merchant = merchant,
            accountType = "bank",
            payTime = System.currentTimeMillis(),
        )
    }
}
