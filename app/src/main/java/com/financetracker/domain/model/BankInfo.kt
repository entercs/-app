package com.financetracker.domain.model

data class BankInfo(
    val name: String,
    val icon: String,
    val colorHex: String,
    val appPackage: String = "",
)

object Banks {
    val all = listOf(
        BankInfo("工商银行", "🏦", "#E31E24", "com.icbc"),
        BankInfo("建设银行", "🏗", "#005BAC", "com.chinamworld.main"),
        BankInfo("农业银行", "🌾", "#009B87", "com.android.bankabc"),
        BankInfo("中国银行", "🏛", "#AE1B2D", "com.chinamworld.bocmbci"),
        BankInfo("招商银行", "🐬", "#E60012", "cmb.pb"),
        BankInfo("交通银行", "🚢", "#003893", "com.bankcomm.maidanba"),
        BankInfo("邮储银行", "📮", "#007A3D", "com.psbc.mobilebank"),
        BankInfo("其他银行", "🏦", "#F5A623", ""),
    )

    fun findByPackage(packageName: String): BankInfo? =
        all.firstOrNull { it.appPackage == packageName }
}
