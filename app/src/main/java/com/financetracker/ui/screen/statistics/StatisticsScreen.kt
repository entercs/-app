package com.financetracker.ui.screen.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.financetracker.data.repository.StatisticsRepository.CategorySummary
import com.financetracker.di.AppModule
import com.financetracker.ui.component.MonthPicker
import com.financetracker.ui.component.PieChart
import com.financetracker.ui.component.PieSlice
import com.financetracker.ui.theme.Green500
import com.financetracker.ui.theme.PieChartColors
import com.financetracker.ui.theme.Red500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    val factory = remember { StatisticsViewModel.Factory(AppModule.statisticsRepository, AppModule.transactionRepository) }
    val viewModel: StatisticsViewModel = viewModel(factory = factory)

    val year by viewModel.year.collectAsState()
    val month by viewModel.month.collectAsState()
    val expenseTotal by viewModel.expenseTotal.collectAsState()
    val incomeTotal by viewModel.incomeTotal.collectAsState()
    val categorySummaries by viewModel.categorySummaries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green500,
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Month picker
            item {
                MonthPicker(
                    year = year,
                    month = month,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                )
            }

            // Summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.1f)),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("支出", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "¥${String.format("%.2f", expenseTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Red500,
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.1f)),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("收入", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "¥${String.format("%.2f", incomeTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Green500,
                            )
                        }
                    }
                }
            }

            // Pie chart
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("支出分类", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        val slices = categorySummaries.mapIndexed { i, s ->
                            PieSlice(
                                label = "${s.category.icon} ${s.category.name}",
                                value = s.amount.toFloat(),
                                color = PieChartColors[i % PieChartColors.size],
                            )
                        }
                        PieChart(slices = slices)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
