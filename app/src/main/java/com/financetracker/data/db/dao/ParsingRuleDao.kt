package com.financetracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.financetracker.data.db.entity.ParsingRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParsingRuleDao {
    @Insert
    suspend fun insert(rule: ParsingRuleEntity): Long

    @Update
    suspend fun update(rule: ParsingRuleEntity)

    @Query("SELECT * FROM parsing_rules WHERE appPackage = :appPackage")
    suspend fun getByPackage(appPackage: String): List<ParsingRuleEntity>

    @Query("SELECT * FROM parsing_rules ORDER BY id ASC")
    fun getAll(): Flow<List<ParsingRuleEntity>>
}
