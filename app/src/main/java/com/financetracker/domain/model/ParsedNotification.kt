package com.financetracker.domain.model

data class ParsedNotification(
    val amount: Double,
    val merchant: String,
    val accountType: String,
    val payTime: Long,
)
