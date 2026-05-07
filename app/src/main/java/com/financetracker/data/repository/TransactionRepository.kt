package com.financetracker.data.repository

import com.financetracker.data.db.dao.TransactionDao
import com.financetracker.data.db.entity.TransactionEntity
import com.financetracker.domain.model.Transaction
import com.financetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val dao: TransactionDao) {

    fun getRecent(limit: Int = 10): Flow<List<Transaction>> =
        dao.getRecent(limit).mapToDomain()

    fun getByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>> =
        dao.getByMonth(startOfMonth, endOfMonth).mapToDomain()

    fun search(query: String): Flow<List<Transaction>> =
        dao.search(query).mapToDomain()

    suspend fun getById(id: Long): Transaction? =
        dao.getById(id)?.toDomain()

    suspend fun add(transaction: Transaction): Long =
        dao.insert(transaction.toEntity())

    suspend fun update(transaction: Transaction) =
        dao.update(transaction.toEntity())

    suspend fun delete(transaction: Transaction) =
        dao.delete(transaction.toEntity())

    suspend fun getMonthlyTotal(
        type: TransactionType,
        startOfMonth: Long,
        endOfMonth: Long,
    ): Double = dao.getTotalByType(type.name, startOfMonth, endOfMonth) ?: 0.0

    suspend fun getCategoryTotal(categoryId: Long, startOfMonth: Long, endOfMonth: Long): Double =
        dao.getCategoryTotal(categoryId, startOfMonth, endOfMonth)

    private fun Flow<List<TransactionEntity>>.mapToDomain(): Flow<List<Transaction>> =
        map { list -> list.map { it.toDomain() } }
}

private fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    categoryId = categoryId,
    accountId = accountId,
    merchant = merchant,
    note = note,
    date = date,
    source = source,
)

private fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    accountId = accountId,
    merchant = merchant,
    note = note,
    date = date,
    source = source,
)
