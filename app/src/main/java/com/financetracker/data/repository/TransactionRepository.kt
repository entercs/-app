package com.financetracker.data.repository

import com.financetracker.data.db.dao.PaymentAccountDao
import com.financetracker.data.db.dao.TransactionDao
import com.financetracker.data.db.entity.TransactionEntity
import com.financetracker.domain.model.Transaction
import com.financetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val dao: TransactionDao,
    private val accountDao: PaymentAccountDao,
) {

    fun getRecent(limit: Int = 10): Flow<List<Transaction>> =
        dao.getRecent(limit).mapToDomain()

    fun getByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>> =
        dao.getByMonth(startOfMonth, endOfMonth).mapToDomain()

    fun search(query: String): Flow<List<Transaction>> =
        dao.search(query).mapToDomain()

    suspend fun getById(id: Long): Transaction? =
        dao.getById(id)?.toDomain()

    suspend fun add(transaction: Transaction): Long {
        val id = dao.insert(transaction.toEntity())
        updateAccountBalance(transaction.accountId, transaction.amount, transaction.type)
        return id
    }

    suspend fun update(transaction: Transaction) {
        val old = dao.getById(transaction.id) ?: return
        dao.update(transaction.toEntity())
        // Reverse old effect
        updateAccountBalance(old.accountId, -old.amount, TransactionType.valueOf(old.type))
        // Apply new effect
        updateAccountBalance(transaction.accountId, transaction.amount, transaction.type)
    }

    suspend fun delete(transaction: Transaction) {
        dao.delete(transaction.toEntity())
        // Reverse the transaction effect
        val reversedType = if (transaction.type == TransactionType.EXPENSE) TransactionType.INCOME else TransactionType.EXPENSE
        updateAccountBalance(transaction.accountId, transaction.amount, reversedType)
    }

    private suspend fun updateAccountBalance(accountId: Long, amount: Double, type: TransactionType) {
        val account = accountDao.getById(accountId) ?: return
        val newBalance = if (type == TransactionType.EXPENSE)
            account.balance - amount
        else
            account.balance + amount
        accountDao.updateBalance(accountId, newBalance)
    }

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
