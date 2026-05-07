package com.financetracker.data.repository

import com.financetracker.domain.model.Category
import com.financetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.first

class StatisticsRepository(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
) {
    suspend fun getMonthlyExpenseTotal(startOfMonth: Long, endOfMonth: Long): Double =
        transactionRepo.getMonthlyTotal(TransactionType.EXPENSE, startOfMonth, endOfMonth)

    suspend fun getMonthlyIncomeTotal(startOfMonth: Long, endOfMonth: Long): Double =
        transactionRepo.getMonthlyTotal(TransactionType.INCOME, startOfMonth, endOfMonth)

    data class CategorySummary(
        val category: Category,
        val amount: Double,
        val percentage: Float,
    )

    suspend fun getCategorySummaries(startOfMonth: Long, endOfMonth: Long): List<CategorySummary> {
        val total = transactionRepo.getMonthlyTotal(TransactionType.EXPENSE, startOfMonth, endOfMonth)
        if (total == 0.0) return emptyList()

        val expenseCategories = categoryRepo.getByType(TransactionType.EXPENSE).first()
        val categories = mutableListOf<CategorySummary>()
        for (cat in expenseCategories) {
            val amount = transactionRepo.getCategoryTotal(cat.id, startOfMonth, endOfMonth)
            if (amount > 0) {
                categories.add(
                    CategorySummary(cat, amount, (amount / total).toFloat())
                )
            }
        }
        return categories.sortedByDescending { it.amount }
    }
}
