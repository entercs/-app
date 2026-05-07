package com.financetracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parsing_rules")
data class ParsingRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appPackage: String,
    val accountType: String,
    val amountRegex: String,
    val amountGroupIndex: Int = 1,
    val merchantRegex: String = "",
    val merchantGroupIndex: Int = 1,
)
