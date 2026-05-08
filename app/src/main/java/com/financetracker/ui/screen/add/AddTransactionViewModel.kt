package com.financetracker.ui.screen.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financetracker.data.repository.CategoryRepository
import com.financetracker.data.repository.PaymentAccountRepository
import com.financetracker.data.repository.TransactionRepository
import com.financetracker.domain.model.Category
import com.financetracker.domain.model.PaymentAccount
import com.financetracker.domain.model.Transaction
import com.financetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: PaymentAccountRepository,
) : ViewModel() {

    val expenseCategories: StateFlow<List<Category>> =
        categoryRepo.getByType(TransactionType.EXPENSE).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> =
        categoryRepo.getByType(TransactionType.INCOME).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<PaymentAccount>> =
        accountRepo.getEnabled().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedType: StateFlow<TransactionType> = _selectedType.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow(-1L)
    val selectedCategoryId: StateFlow<Long> = _selectedCategoryId.asStateFlow()

    private val _selectedAccountId = MutableStateFlow(-1L)
    val selectedAccountId: StateFlow<Long> = _selectedAccountId.asStateFlow()

    private val _transferToAccountId = MutableStateFlow(-1L)
    val transferToAccountId: StateFlow<Long> = _transferToAccountId.asStateFlow()

    private val _merchant = MutableStateFlow("")
    val merchant: StateFlow<String> = _merchant.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _date = MutableStateFlow(System.currentTimeMillis())
    val date: StateFlow<Long> = _date.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private var editId: Long? = null

    fun loadTransaction(id: Long) {
        editId = id
        viewModelScope.launch {
            val t = transactionRepo.getById(id) ?: return@launch
            _amount.value = t.amount.toBigDecimal().stripTrailingZeros().toPlainString()
            _selectedType.value = t.type
            _selectedCategoryId.value = t.categoryId
            _selectedAccountId.value = t.accountId
            _merchant.value = t.merchant
            _note.value = t.note
            _date.value = t.date
        }
    }

    fun loadRefund(transactionId: Long) {
        viewModelScope.launch {
            val t = transactionRepo.getById(transactionId) ?: return@launch
            _amount.value = t.amount.toBigDecimal().stripTrailingZeros().toPlainString()
            _selectedType.value = TransactionType.INCOME
            _selectedCategoryId.value = t.categoryId
            _selectedAccountId.value = t.accountId
            _merchant.value = "退款: ${t.merchant.ifBlank { "消费" }}"
            _note.value = "退款关联 #${t.id}"
            _date.value = System.currentTimeMillis()
        }
    }

    fun setAmount(value: String) { _amount.value = value }
    fun setType(type: TransactionType) {
        _selectedType.value = type
        _selectedCategoryId.value = -1L
    }
    fun setCategoryId(id: Long) { _selectedCategoryId.value = id }
    fun setAccountId(id: Long) { _selectedAccountId.value = id }
    fun setTransferToAccountId(id: Long) { _transferToAccountId.value = id }
    fun setMerchant(value: String) { _merchant.value = value }
    fun setNote(value: String) { _note.value = value }
    fun setDate(value: Long) { _date.value = value }

    fun save() {
        val amt = _amount.value.toDoubleOrNull() ?: return
        if (amt <= 0) return
        if (_selectedAccountId.value <= 0) return
        val isTransfer = _selectedType.value == TransactionType.TRANSFER
        if (!isTransfer && _selectedCategoryId.value <= 0) return
        if (isTransfer && _transferToAccountId.value <= 0) return
        if (isTransfer && _transferToAccountId.value == _selectedAccountId.value) return

        val transaction = Transaction(
            id = editId ?: 0,
            amount = amt,
            type = _selectedType.value,
            categoryId = if (isTransfer) -1L else _selectedCategoryId.value,
            accountId = _selectedAccountId.value,
            transferToAccountId = if (isTransfer) _transferToAccountId.value else null,
            merchant = _merchant.value,
            note = _note.value,
            date = _date.value,
            source = "manual",
        )
        viewModelScope.launch {
            if (editId != null) transactionRepo.update(transaction)
            else transactionRepo.add(transaction)
            _saved.value = true
        }
    }

    class Factory(
        private val transactionRepo: TransactionRepository,
        private val categoryRepo: CategoryRepository,
        private val accountRepo: PaymentAccountRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddTransactionViewModel(transactionRepo, categoryRepo, accountRepo) as T
        }
    }
}
