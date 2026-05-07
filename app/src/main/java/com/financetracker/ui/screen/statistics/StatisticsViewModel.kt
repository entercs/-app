package com.financetracker.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financetracker.data.repository.StatisticsRepository
import com.financetracker.data.repository.StatisticsRepository.CategorySummary
import com.financetracker.data.repository.TransactionRepository
import com.financetracker.ui.component.getCurrentYearMonth
import com.financetracker.ui.component.getMonthRange
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val statisticsRepo: StatisticsRepository,
    private val transactionRepo: TransactionRepository,
) : ViewModel() {

    private val _year = MutableStateFlow(0)
    val year: StateFlow<Int> = _year.asStateFlow()

    private val _month = MutableStateFlow(0)
    val month: StateFlow<Int> = _month.asStateFlow()

    private val _expenseTotal = MutableStateFlow(0.0)
    val expenseTotal: StateFlow<Double> = _expenseTotal.asStateFlow()

    private val _incomeTotal = MutableStateFlow(0.0)
    val incomeTotal: StateFlow<Double> = _incomeTotal.asStateFlow()

    private val _categorySummaries = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categorySummaries: StateFlow<List<CategorySummary>> = _categorySummaries.asStateFlow()

    private var observeJob: Job? = null

    init {
        val (y, m) = getCurrentYearMonth()
        _year.value = y
        _month.value = m
        startObserving()
    }

    fun previousMonth() {
        if (_month.value == 0) { _year.value -= 1; _month.value = 11 }
        else _month.value -= 1
        startObserving()
    }

    fun nextMonth() {
        if (_month.value == 11) { _year.value += 1; _month.value = 0 }
        else _month.value += 1
        startObserving()
    }

    private fun startObserving() {
        observeJob?.cancel()
        val (start, end) = getMonthRange(_year.value, _month.value)
        observeJob = viewModelScope.launch {
            transactionRepo.getByMonth(start, end).collect {
                loadData(start, end)
            }
        }
    }

    private suspend fun loadData(start: Long, end: Long) {
        _expenseTotal.value = statisticsRepo.getMonthlyExpenseTotal(start, end)
        _incomeTotal.value = statisticsRepo.getMonthlyIncomeTotal(start, end)
        _categorySummaries.value = statisticsRepo.getCategorySummaries(start, end)
    }

    class Factory(
        private val statisticsRepo: StatisticsRepository,
        private val transactionRepo: TransactionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatisticsViewModel(statisticsRepo, transactionRepo) as T
        }
    }
}
