package com.financetracker.ui.theme

import androidx.compose.ui.graphics.Color

val Green500 = Color(0xFF4CAF50)
val Green700 = Color(0xFF388E3C)
val Red500 = Color(0xFFF44336)
val Red700 = Color(0xFFD32F2F)
val Blue500 = Color(0xFF2196F3)
val Orange500 = Color(0xFFFF9800)

// Account brand colors
val WeChatGreen = Color(0xFF07C160)
val AlipayBlue = Color(0xFF1677FF)
val JDRed = Color(0xFFE3312C)
val BankOrange = Color(0xFFF5A623)

fun accountColorHex(type: String): String = when (type) {
    "wechat" -> "#07C160"
    "alipay" -> "#1677FF"
    "jd" -> "#E3312C"
    "bank" -> "#F5A623"
    else -> "#757575"
}

fun accountColor(type: String): Color = when (type) {
    "wechat" -> WeChatGreen
    "alipay" -> AlipayBlue
    "jd" -> JDRed
    "bank" -> BankOrange
    else -> Color(0xFF757575)
}

fun accountIcon(type: String): String = when (type) {
    "wechat" -> "💬"
    "alipay" -> "🔵"
    "jd" -> "🛒"
    "bank" -> "🏦"
    else -> "💳"
}

val PieChartColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFFF44336),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFFF5722),
    Color(0xFF607D8B),
)
