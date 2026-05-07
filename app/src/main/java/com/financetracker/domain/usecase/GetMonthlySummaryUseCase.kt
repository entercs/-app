package com.financetracker.domain.usecase

import com.financetracker.data.repository.StatisticsRepository
import com.financetracker.data.repository.StatisticsRepository.CategorySummary

class GetMonthlySummaryUseCase(private val repo: StatisticsRepository) {
    suspend operator fun invoke(
        startOfMonth: Long,
        endOfMonth: Long,
    ): Summary = Summary(
        expenseTotal = repo.getMonthlyExpenseTotal(startOfMonth, endOfMonth),
        incomeTotal = repo.getMonthlyIncomeTotal(startOfMonth, endOfMonth),
        categorySummaries = repo.getCategorySummaries(startOfMonth, endOfMonth),
    )

    data class Summary(
        val expenseTotal: Double,
        val incomeTotal: Double,
        val categorySummaries: List<CategorySummary>,
    )
}
