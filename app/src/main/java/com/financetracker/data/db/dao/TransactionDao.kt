package com.financetracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.financetracker.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startOfMonth AND date < :endOfMonth ORDER BY date DESC")
    fun getByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("""
        SELECT * FROM transactions
        WHERE (merchant LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%')
        ORDER BY date DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE type = :type AND date >= :startOfMonth AND date < :endOfMonth
    """)
    suspend fun getTotalByType(type: String, startOfMonth: Long, endOfMonth: Long): Double?

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE' AND categoryId = :categoryId
        AND date >= :startOfMonth AND date < :endOfMonth
    """)
    suspend fun getCategoryTotal(categoryId: Long, startOfMonth: Long, endOfMonth: Long): Double
}
