package com.financetracker.domain.usecase

import com.financetracker.data.repository.TransactionRepository
import com.financetracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

class SearchTransactionsUseCase(private val repo: TransactionRepository) {
    operator fun invoke(query: String): Flow<List<Transaction>> = repo.search(query)
}
