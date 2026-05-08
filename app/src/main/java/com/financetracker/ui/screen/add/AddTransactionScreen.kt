package com.financetracker.ui.screen.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financetracker.data.repository.CategoryRepository
import com.financetracker.data.repository.PaymentAccountRepository
import com.financetracker.data.repository.TransactionRepository
import com.financetracker.di.AppModule
import com.financetracker.domain.model.Category
import com.financetracker.domain.model.PaymentAccount
import com.financetracker.domain.model.TransactionType
import com.financetracker.ui.theme.Green500
import com.financetracker.ui.theme.Red500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(editTransactionId: Long? = null, refundTransactionId: Long? = null, onNavigateBack: () -> Unit) {
    val factory = remember {
        AddTransactionViewModel.Factory(
            AppModule.transactionRepository,
            AppModule.categoryRepository,
            AppModule.paymentAccountRepository,
        )
    }
    val viewModel: AddTransactionViewModel = viewModel(factory = factory)

    LaunchedEffect(editTransactionId) {
        editTransactionId?.let { viewModel.loadTransaction(it) }
    }
    LaunchedEffect(refundTransactionId) {
        refundTransactionId?.let { viewModel.loadRefund(it) }
    }

    val amountFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { amountFocusRequester.requestFocus() }

    val amount by viewModel.amount.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()
    val transferToAccountId by viewModel.transferToAccountId.collectAsState()
    val merchant by viewModel.merchant.collectAsState()
    val note by viewModel.note.collectAsState()
    val date by viewModel.date.collectAsState()
    val saved by viewModel.saved.collectAsState()

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)

    LaunchedEffect(saved) {
        if (saved) onNavigateBack()
    }

    val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editTransactionId != null) "编辑" else "记一笔", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green500, titleContentColor = Color.White),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Amount input
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) viewModel.setAmount(it) },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    singleLine = true,
                    prefix = { Text("¥ ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                )
            }

            // Type toggle
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(TransactionType.EXPENSE to "支出", TransactionType.INCOME to "收入", TransactionType.TRANSFER to "转账").forEach { (type, label) ->
                        val isSelected = selectedType == type
                        val typeColor = when (type) { TransactionType.EXPENSE -> Red500; TransactionType.INCOME -> Green500; else -> Color(0xFF757575) }
                        val bgColor = if (isSelected) typeColor else Color.Transparent
                        val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        Card(
                            modifier = Modifier.weight(1f).clickable { viewModel.setType(type) },
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            // Category selector (hidden for transfers)
            if (selectedType != TransactionType.TRANSFER) {
                item {
                    Text("选择分类", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            CategoryChip(cat, isSelected = selectedCategoryId == cat.id) {
                                viewModel.setCategoryId(cat.id)
                            }
                        }
                    }
                }
            }

            // Account selector
            item {
                Text(if (selectedType == TransactionType.TRANSFER) "转出账户" else "支付账户", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { account ->
                        AccountChip(account, isSelected = selectedAccountId == account.id) {
                            viewModel.setAccountId(account.id)
                        }
                    }
                }
            }

            // Transfer-to account
            if (selectedType == TransactionType.TRANSFER) {
                item {
                    Text("转入账户", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.filter { it.id != selectedAccountId }.forEach { account ->
                            AccountChip(account, isSelected = transferToAccountId == account.id) {
                                viewModel.setTransferToAccountId(account.id)
                            }
                        }
                    }
                }
            }

            // Date picker
            item {
                Text("交易时间", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Text(
                        text = dateFormat.format(Date(date)),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // Merchant
            item {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = viewModel::setMerchant,
                    label = { Text("商户名称（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // Note
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = viewModel::setNote,
                    label = { Text("备注（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // Save button
            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Green500),
                    enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
                            && selectedAccountId > 0
                            && (selectedType == TransactionType.TRANSFER && transferToAccountId > 0 && transferToAccountId != selectedAccountId
                                || selectedType != TransactionType.TRANSFER && selectedCategoryId > 0),
                ) {
                    Text(if (editTransactionId != null) "更新" else "保存", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CategoryChip(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Green500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (isSelected) BorderStroke(1.5.dp, Green500) else null,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            com.financetracker.ui.component.CategoryLogo(
                categoryName = category.name,
                categoryIcon = category.icon,
                size = 20.dp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Green500 else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AccountChip(account: PaymentAccount, isSelected: Boolean, onClick: () -> Unit) {
    val accColor = try { Color(android.graphics.Color.parseColor(account.color)) } catch (_: Exception) { Green500 }
    val bgColor = if (isSelected) accColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) accColor else null
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = borderColor?.let { BorderStroke(1.5.dp, it) },
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            com.financetracker.ui.component.AccountLogo(type = account.type, accountName = account.name, size = 20.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                account.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) accColor else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
