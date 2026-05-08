package com.financetracker.ui.screen.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financetracker.data.repository.StatisticsRepository.CategorySummary
import com.financetracker.di.AppModule
import com.financetracker.ui.component.CategoryLogo
import com.financetracker.ui.component.PieChart
import com.financetracker.ui.component.PieSlice
import com.financetracker.ui.theme.Green500
import com.financetracker.ui.theme.PieChartColors
import com.financetracker.ui.theme.Red500
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    val factory = remember { StatisticsViewModel.Factory(AppModule.statisticsRepository, AppModule.transactionRepository) }
    val viewModel: StatisticsViewModel = viewModel(factory = factory)

    val period by viewModel.period.collectAsState()
    val offset by viewModel.offset.collectAsState()
    val headerText by viewModel.headerText.collectAsState()
    val expenseTotal by viewModel.expenseTotal.collectAsState()
    val incomeTotal by viewModel.incomeTotal.collectAsState()
    val categorySummaries by viewModel.categorySummaries.collectAsState()
    val prevExpenseTotal by viewModel.prevExpenseTotal.collectAsState()
    val prevIncomeTotal by viewModel.prevIncomeTotal.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val yearlyTotalExpense by viewModel.yearlyTotalExpense.collectAsState()
    val canGoNext by viewModel.canGoNext.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green500, titleContentColor = Color.White),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Period selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(StatPeriod.WEEK to "周", StatPeriod.MONTH to "月", StatPeriod.YEAR to "年", StatPeriod.CUSTOM to "自定义").forEach { (p, label) ->
                        FilterChip(
                            selected = period == p,
                            onClick = { viewModel.setPeriod(p) },
                            label = { Text(label, fontSize = 12.sp) },
                        )
                    }
                }
            }

            // Date navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { viewModel.previous() }) {
                        Text("◀", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).clickable { if (period == StatPeriod.CUSTOM) showDatePicker = true },
                    )
                    IconButton(
                        onClick = { viewModel.next() },
                        enabled = canGoNext,
                    ) {
                        Text("▶", style = MaterialTheme.typography.titleMedium, color = if (canGoNext) MaterialTheme.colorScheme.onSurface else Color.LightGray)
                    }
                }
            }

            // Summary cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("支出", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("¥${String.format("%.2f", expenseTotal)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Red500)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("收入", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("¥${String.format("%.2f", incomeTotal)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Green500)
                        }
                    }
                }
            }

            // Comparison
            if (prevExpenseTotal > 0) {
                item {
                    val diff = expenseTotal - prevExpenseTotal
                    val pct = if (prevExpenseTotal > 0) (diff / prevExpenseTotal * 100) else 0.0
                    val isUp = diff > 0
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = (if (isUp) Red500 else Green500).copy(alpha = 0.08f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("环比对比", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("上期支出 ¥${String.format("%.2f", prevExpenseTotal)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("本期支出 ¥${String.format("%.2f", expenseTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Simple bar comparison
                            val maxVal = maxOf(expenseTotal, prevExpenseTotal).toFloat().coerceAtLeast(1f)
                            val prevW = (prevExpenseTotal / maxVal).toFloat().coerceAtLeast(0.05f)
                            val currW = (expenseTotal / maxVal).toFloat().coerceAtLeast(0.05f)
                            Row(modifier = Modifier.fillMaxWidth().height(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(prevW).fillMaxHeight().padding(end = 2.dp)) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawRoundRect(Color.Gray.copy(alpha = 0.4f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
                                    }
                                }
                                Box(modifier = Modifier.weight(currW).fillMaxHeight().padding(start = 2.dp)) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawRoundRect(if (isUp) Red500 else Green500, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                if (isUp) "比上期多花 ${String.format("%.1f", pct)}%" else "比上期少花 ${String.format("%.1f", kotlin.math.abs(pct))}%",
                                fontSize = 12.sp, color = if (isUp) Red500 else Green500, fontWeight = FontWeight.Medium,
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
                                label = "${s.category.name}",
                                value = s.amount.toFloat(),
                                color = PieChartColors[i % PieChartColors.size],
                            )
                        }
                        PieChart(slices = slices)
                    }
                }
            }

            // Yearly report (monthly bars)
            if (period == StatPeriod.YEAR) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("月度趋势", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("年度总支出 ¥${String.format("%.2f", yearlyTotalExpense)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (monthlyExpenses.isNotEmpty()) {
                                val maxM = monthlyExpenses.max().toFloat().coerceAtLeast(1f)
                                val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
                                Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    monthlyExpenses.forEachIndexed { i, v ->
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                                                val h = (v / maxM).toFloat().coerceAtLeast(0.03f)
                                                Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight(h)) {
                                                    drawRoundRect(if (v > 0) Red500 else Color.LightGray, cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(months[i], fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category list with logos
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("分类明细", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        categorySummaries.forEach { cs ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CategoryLogo(categoryName = cs.category.name, categoryIcon = cs.category.icon, size = 24.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cs.category.name, modifier = Modifier.weight(1f))
                                Text("¥${String.format("%.2f", cs.amount)}", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${String.format("%.1f", cs.percentage * 100)}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Custom range date picker
    if (showDatePicker && period == StatPeriod.CUSTOM) {
        val now = System.currentTimeMillis()
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        var pickStart by remember { mutableStateOf(startDate) }
        var pickEnd by remember { mutableStateOf(endDate) }

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("选择日期范围") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val sdfHeader = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                    Text("开始: ${sdfHeader.format(Date(pickStart))}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        YearMonthDayPicker(pickStart) { pickStart = it }
                    }
                    HorizontalDivider()
                    Text("结束: ${sdfHeader.format(Date(pickEnd))}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        YearMonthDayPicker(pickEnd, maxDate = now) { pickEnd = it }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setCustomStartEnd(pickStart, minOf(pickEnd, now))
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun YearMonthDayPicker(currentMillis: Long, maxDate: Long = System.currentTimeMillis(), onDate: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentMillis }
    val nowCal = Calendar.getInstance().apply { timeInMillis = maxDate }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val maxYear = nowCal.get(Calendar.YEAR)

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        // Year
        var showYear by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { showYear = true }) { Text("${year}年") }
            DropdownMenu(expanded = showYear, onDismissRequest = { showYear = false }) {
                for (y in 2020..maxYear) {
                    DropdownMenuItem(text = { Text("$y") }, onClick = { cal.set(Calendar.YEAR, y); onDate(cal.timeInMillis); showYear = false })
                }
            }
        }
        // Month
        var showMonth by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { showMonth = true }) { Text("${month + 1}月") }
            DropdownMenu(expanded = showMonth, onDismissRequest = { showMonth = false }) {
                for (m in 1..12) {
                    DropdownMenuItem(text = { Text("$m") }, onClick = { cal.set(Calendar.MONTH, m - 1); cal.set(Calendar.DAY_OF_MONTH, minOf(day, cal.getActualMaximum(Calendar.DAY_OF_MONTH))); onDate(cal.timeInMillis); showMonth = false })
                }
            }
        }
        // Day
        var showDay by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { showDay = true }) { Text("${day}日") }
            DropdownMenu(expanded = showDay, onDismissRequest = { showDay = false }) {
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (d in 1..maxDay) {
                    DropdownMenuItem(text = { Text("$d") }, onClick = { cal.set(Calendar.DAY_OF_MONTH, d); onDate(cal.timeInMillis); showDay = false })
                }
            }
        }
    }
}
