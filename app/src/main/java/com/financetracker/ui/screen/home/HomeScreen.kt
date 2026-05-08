package com.financetracker.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financetracker.di.AppModule
import com.financetracker.ui.component.DateHeader
import com.financetracker.ui.component.TransactionItem
import com.financetracker.ui.component.groupTransactionsByDay
import com.financetracker.ui.theme.Green500
import com.financetracker.ui.theme.Red500
import com.financetracker.ui.theme.AccountIconDisplay
import com.financetracker.ui.theme.accountColor
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onTransactionClick: (Long) -> Unit = {},
) {
    val factory = remember {
        HomeViewModel.Factory(
            AppModule.transactionRepository,
            AppModule.categoryRepository,
            AppModule.paymentAccountRepository,
        )
    }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val accountMap = remember(accounts) { accounts.associateBy { it.id } }
    val dailyGroups = remember(recentTransactions) { groupTransactionsByDay(recentTransactions) }
    val flatItems = remember(dailyGroups) {
        dailyGroups.flatMap { group -> listOf<Any>(group) + group.transactions }
    }

    val totalExpense = remember(monthlyTransactions) {
        monthlyTransactions
            .filter { it.type == com.financetracker.domain.model.TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
    val totalIncome = remember(monthlyTransactions) {
        monthlyTransactions
            .filter { it.type == com.financetracker.domain.model.TransactionType.INCOME }
            .sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账助手", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green500, titleContentColor = Color.White),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = Green500,
                contentColor = Color.White,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "记账")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.1f)),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(monthLabel(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            val balance = totalIncome - totalExpense
                            val balanceColor = if (balance >= 0) Green500 else Red500
                            SummaryItem("支出", totalExpense, Red500)
                            SummaryItem("收入", totalIncome, Green500)
                            SummaryItem("结余", balance, balanceColor)
                        }
                    }
                }
            }

            // Assets overview
            item {
                val totalAssets = accounts.sumOf { it.balance }
                val assetsColor = if (totalAssets >= 0) Green500 else Red500
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("总资产", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "¥${String.format("%.2f", totalAssets)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = assetsColor,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        accounts.forEach { acc ->
                            val balColor = if (acc.balance >= 0) Green500 else Red500
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AccountIconDisplay(type = acc.type, accountName = acc.name, size = 18.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        acc.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    "¥${String.format("%.2f", acc.balance)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = balColor,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("最近交易", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("还没有交易记录\n点击右下角按钮开始记账", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(flatItems.size, key = { index ->
                val item = flatItems[index]
                when (item) {
                    is com.financetracker.ui.component.DailyGroup -> "h_${item.dateLabel}"
                    is com.financetracker.domain.model.Transaction -> "tx_${item.id}"
                    else -> "unknown"
                }
            }) { index ->
                when (val item = flatItems[index]) {
                    is com.financetracker.ui.component.DailyGroup -> {
                        DateHeader(item.dateLabel, item.totalExpense, item.totalIncome)
                    }
                    is com.financetracker.domain.model.Transaction -> {
                        TransactionItem(
                            transaction = item,
                            category = categoryMap[item.categoryId],
                            account = accountMap[item.accountId],
                            toAccount = item.transferToAccountId?.let { accountMap[it] },
                            onClick = { onTransactionClick(item.id) },
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SummaryItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "¥${String.format("%.2f", amount)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

private fun monthLabel(): String {
    val cal = Calendar.getInstance()
    return "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月"
}
