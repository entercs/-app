package com.financetracker.domain.model

data class PaymentAccount(
    val id: Long = 0,
    val name: String,
    val type: String,
    val isEnabled: Boolean = true,
    val color: String = "#4CAF50",
)
