package com.financetracker.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financetracker.data.preferences.AppPreferences
import com.financetracker.data.repository.CategoryRepository
import com.financetracker.data.repository.PaymentAccountRepository
import com.financetracker.data.repository.TransactionRepository
import com.financetracker.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class SettingsViewModel(
    private val prefs: AppPreferences,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: PaymentAccountRepository,
    private val transactionRepo: TransactionRepository,
) : ViewModel() {

    val isNotificationEnabled: StateFlow<Boolean> =
        prefs.isNotificationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotificationEnabled(enabled) }
    }

    fun seedCategories() {
        viewModelScope.launch { categoryRepo.seedIfEmpty() }
    }

    fun seedAccounts() {
        viewModelScope.launch { accountRepo.seedIfEmpty() }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val transactions = transactionRepo.getRecent(10000).first()
                val file = File(context.cacheDir, "finance_export_${System.currentTimeMillis()}.csv")
                FileWriter(file).use { writer ->
                    writer.write("日期,类型,金额,分类ID,账户ID,商户,备注,来源\n")
                    transactions.forEach { t ->
                        writer.write("${t.date},${t.type},${t.amount},${t.categoryId},${t.accountId},${t.merchant},${t.note},${t.source}\n")
                    }
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                _exportState.value = ExportState.Success(uri)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "导出失败")
            }
        }
    }

    fun clearExportState() {
        _exportState.value = ExportState.Idle
    }

    sealed class ExportState {
        data object Idle : ExportState()
        data object Exporting : ExportState()
        data class Success(val uri: Uri) : ExportState()
        data class Error(val message: String) : ExportState()
    }

    class Factory(
        private val prefs: AppPreferences,
        private val categoryRepo: CategoryRepository,
        private val accountRepo: PaymentAccountRepository,
        private val transactionRepo: TransactionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(prefs, categoryRepo, accountRepo, transactionRepo) as T
        }
    }
}
