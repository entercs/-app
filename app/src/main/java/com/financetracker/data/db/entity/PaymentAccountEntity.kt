package com.financetracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_accounts")
data class PaymentAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "wechat", "alipay", "jd", "bank", or custom
    val isEnabled: Boolean = true,
    val color: String = "#4CAF50", // hex color for display
)
