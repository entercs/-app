package com.financetracker.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financetracker.data.repository.StatisticsRepository
import com.financetracker.data.repository.StatisticsRepository.CategorySummary
import com.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

enum class StatPeriod { WEEK, MONTH, YEAR, CUSTOM }

class StatisticsViewModel(
    private val statisticsRepo: StatisticsRepository,
    private val transactionRepo: TransactionRepository,
) : ViewModel() {

    // Date range (millis)
    private val _startDate = MutableStateFlow(0L)
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(0L)
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    // Current period
    private val _period = MutableStateFlow(StatPeriod.MONTH)
    val period: StateFlow<StatPeriod> = _period.asStateFlow()

    // Navigation offset (for WEEK/MONTH/YEAR)
    private val _offset = MutableStateFlow(0)
    val offset: StateFlow<Int> = _offset.asStateFlow()

    // Data
    private val _expenseTotal = MutableStateFlow(0.0)
    val expenseTotal: StateFlow<Double> = _expenseTotal.asStateFlow()

    private val _incomeTotal = MutableStateFlow(0.0)
    val incomeTotal: StateFlow<Double> = _incomeTotal.asStateFlow()

    private val _categorySummaries = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categorySummaries: StateFlow<List<CategorySummary>> = _categorySummaries.asStateFlow()

    // Header display
    private val _headerText = MutableStateFlow("")
    val headerText: StateFlow<String> = _headerText.asStateFlow()

    // Can go forward? (future disallowed)
    private val _canGoNext = MutableStateFlow(false)
    val canGoNext: StateFlow<Boolean> = _canGoNext.asStateFlow()

    private var observeJob: Job? = null

    init {
        setPeriod(StatPeriod.MONTH)
    }

    fun setPeriod(newPeriod: StatPeriod) {
        _period.value = newPeriod
        _offset.value = 0
        setCustomRange()
        startObserving()
    }

    fun setCustomStartEnd(start: Long, end: Long) {
        _period.value = StatPeriod.CUSTOM
        _startDate.value = start
        _endDate.value = minOf(end, System.currentTimeMillis())
        updateHeader()
        startObserving()
    }

    fun previous() {
        _offset.value -= 1
        setCustomRange()
        startObserving()
    }

    fun next() {
        if (!_canGoNext.value) return
        _offset.value += 1
        setCustomRange()
        startObserving()
    }

    private fun setCustomRange() {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        when (_period.value) {
            StatPeriod.WEEK -> {
                // Current week: Monday to Sunday
                cal.timeInMillis = now
                cal.add(Calendar.WEEK_OF_YEAR, _offset.value)
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                _startDate.value = cal.timeInMillis
                val weekStart = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                _endDate.value = minOf(cal.timeInMillis, now)
                // Can go next if this week ends before today
                _canGoNext.value = weekStart + 7L * 86400000 <= now
            }
            StatPeriod.MONTH -> {
                cal.timeInMillis = now
                cal.add(Calendar.MONTH, _offset.value)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                _startDate.value = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                cal.add(Calendar.MILLISECOND, -1)
                _endDate.value = minOf(cal.timeInMillis, now)
                // Check if next month exists
                _canGoNext.value = _endDate.value < now || (cal.timeInMillis - 86400000) <= now
            }
            StatPeriod.YEAR -> {
                cal.timeInMillis = now
                cal.add(Calendar.YEAR, _offset.value)
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                _startDate.value = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                cal.add(Calendar.MILLISECOND, -1)
                _endDate.value = minOf(cal.timeInMillis, now)
                _canGoNext.value = (_endDate.value) < now
            }
            StatPeriod.CUSTOM -> { /* Set externally */ }
        }
        updateHeader()
    }

    private fun updateHeader() {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.CHINA)
        _headerText.value = "${sdf.format(java.util.Date(_startDate.value))} - ${sdf.format(java.util.Date(_endDate.value))}"
    }

    private fun startObserving() {
        observeJob?.cancel()
        val start = _startDate.value
        val end = _endDate.value
        observeJob = viewModelScope.launch {
            transactionRepo.getByMonth(start, end + 1).collect {
                loadData(start, end + 1)
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
