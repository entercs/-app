package com.financetracker.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financetracker.data.repository.CategoryRepository
import com.financetracker.data.repository.PaymentAccountRepository
import com.financetracker.data.repository.TransactionRepository
import com.financetracker.domain.model.Category
import com.financetracker.domain.model.PaymentAccount
import com.financetracker.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val transactionRepo: TransactionRepository,
    categoryRepo: CategoryRepository,
    accountRepo: PaymentAccountRepository,
) : ViewModel() {

    private val now = System.currentTimeMillis()

    val monthStart: Long
    val monthEnd: Long

    init {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        monthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        monthEnd = cal.timeInMillis
    }

    val recentTransactions: StateFlow<List<Transaction>> =
        transactionRepo.getRecent(20).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTransactions: StateFlow<List<Transaction>> =
        transactionRepo.getByMonth(monthStart, monthEnd).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> =
        categoryRepo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<PaymentAccount>> =
        accountRepo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deletedTransaction = MutableStateFlow<Transaction?>(null)
    val deletedTransaction: StateFlow<Transaction?> = _deletedTransaction.asStateFlow()

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.delete(transaction)
            _deletedTransaction.value = transaction
        }
    }

    fun undoDelete() {
        val transaction = _deletedTransaction.value ?: return
        viewModelScope.launch {
            transactionRepo.add(transaction.copy(id = 0))
            _deletedTransaction.value = null
        }
    }

    fun clearUndo() {
        _deletedTransaction.value = null
    }

    class Factory(
        private val transactionRepo: TransactionRepository,
        private val categoryRepo: CategoryRepository,
        private val accountRepo: PaymentAccountRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(transactionRepo, categoryRepo, accountRepo) as T
        }
    }
}
