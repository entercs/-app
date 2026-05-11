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

    suspend fun getCategorySummaries(startOfMonth: Long, endOfMonth: Long, type: TransactionType = TransactionType.EXPENSE): List<CategorySummary> {
        val total = transactionRepo.getMonthlyTotal(type, startOfMonth, endOfMonth)
        if (total == 0.0) return emptyList()

        val categories = categoryRepo.getByType(type).first()
        val result = mutableListOf<CategorySummary>()
        for (cat in categories) {
            val amount = transactionRepo.getCategoryTotal(type, cat.id, startOfMonth, endOfMonth)
            if (amount > 0) {
                result.add(
                    CategorySummary(cat, amount, (amount / total).toFloat())
                )
            }
        }
        return result.sortedByDescending { it.amount }
    }

    data class DailyTotal(
        val dayStart: Long,
        val total: Double,
    )

    suspend fun getDailyTotals(start: Long, end: Long, type: TransactionType): List<DailyTotal> {
        return transactionRepo.getDailyTotals(type, start, end).map {
            DailyTotal(it.dayStart, it.total)
        }
    }
}
