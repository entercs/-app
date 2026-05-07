package com.financetracker.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val type: TransactionType,
)
