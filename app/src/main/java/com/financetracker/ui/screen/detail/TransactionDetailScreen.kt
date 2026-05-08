package com.financetracker.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financetracker.di.AppModule
import com.financetracker.domain.model.TransactionType
import com.financetracker.ui.theme.Green500
import com.financetracker.ui.theme.Red500
import com.financetracker.ui.component.AccountLogo
import com.financetracker.ui.component.CategoryLogo
import com.financetracker.ui.theme.accountColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onRefund: ((Long) -> Unit)? = null,
) {
    val factory = remember {
        TransactionDetailViewModel.Factory(
            AppModule.transactionRepository,
            AppModule.categoryRepository,
            AppModule.paymentAccountRepository,
        )
    }
    val viewModel: TransactionDetailViewModel = viewModel(factory = factory)
    val transaction by viewModel.transaction.collectAsState()
    val category by viewModel.category.collectAsState()
    val account by viewModel.account.collectAsState()
    val deleted by viewModel.deleted.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) { viewModel.load(transactionId) }
    LaunchedEffect(deleted) { if (deleted) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { transaction?.let { onEdit(it.id) } }) {
                        Text("编辑", color = Color.White)
                    }
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("删除", color = Color(0xFFFFCDD2))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green500, titleContentColor = Color.White),
            )
        },
    ) { padding ->
        val t = transaction
        if (t == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("加载中...")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Amount
            item {
                val isExpense = t.type == TransactionType.EXPENSE
                val color = if (isExpense) Red500 else Green500
                val prefix = if (isExpense) "-" else "+"
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${category?.icon ?: ""} ${category?.name ?: "未知分类"}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "$prefix¥${String.format("%.2f", t.amount)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                        )
                    }
                }
            }

            // Refund button (only for expenses)
            if (t.type == TransactionType.EXPENSE && onRefund != null) {
                item {
                    OutlinedButton(
                        onClick = { onRefund(t.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Green500),
                    ) {
                        Text("记录退款")
                    }
                }
            }

            // Account + category info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("分类", "${category?.icon ?: ""} ${category?.name ?: "未知"}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AccountLogo(type = account?.type ?: "", accountName = account?.name ?: "", size = 22.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(account?.name ?: "未知")
                        }
                    }
                }
            }

            // Merchant
            if (t.merchant.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow("商户", t.merchant)
                        }
                    }
                }
            }

            // Note + time + source
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (t.note.isNotBlank()) {
                            DetailRow("备注", t.note)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        DetailRow("时间", detailDateFormat.format(Date(t.date)))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow("来源", if (t.source == "notification") "自动记账" else "手动记账")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除后将无法恢复，确定要删除这笔记录吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(); showDeleteDialog = false }) {
                    Text("删除", color = Red500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private val detailDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)
