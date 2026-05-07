package com.financetracker.domain.usecase

import com.financetracker.data.repository.TransactionRepository
import com.financetracker.domain.model.Transaction

class AddTransactionUseCase(private val repo: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction): Long = repo.add(transaction)
}
