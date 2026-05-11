package com.financetracker.ui.screen.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financetracker.di.AppModule
import com.financetracker.domain.model.Banks
import com.financetracker.ui.theme.AccountIconDisplay
import com.financetracker.ui.theme.Green500
import com.financetracker.ui.theme.Red500

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen() {
    val factory = remember {
        SettingsViewModel.Factory(
            AppModule.appPreferences,
            AppModule.categoryRepository,
            AppModule.paymentAccountRepository,
            AppModule.transactionRepository,
        )
    }
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback) {
        feedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            // ── Total assets (top) ──
            item {
                val totalAssets = accounts.sumOf { it.balance }
                val assetsColor = if (totalAssets >= 0) Green500 else Red500
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("净资产", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${String.format("%.2f", totalAssets)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = assetsColor,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Account settings ──
            item {
                Text("账户设置", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        accounts.forEachIndexed { i, acc ->
                            val balColor = try {
                                Color(android.graphics.Color.parseColor(acc.color))
                            } catch (_: Exception) { Color.Gray }
                            var showDialog by remember { mutableStateOf(false) }
                            var editText by remember { mutableStateOf(String.format("%.2f", acc.balance)) }
                            var showBankPicker by remember { mutableStateOf(false) }

                            var showDeleteConfirm by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        editText = String.format("%.2f", acc.balance)
                                        showDialog = true
                                    }
                                    .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AccountIconDisplay(type = acc.type, accountName = acc.name, size = 28.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(acc.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${String.format("%.2f", acc.balance)}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (acc.balance >= 0) Green500 else Red500,
                                )
                            }

                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("删除账户") },
                                    text = { Text("确定要删除「${acc.name}」吗？\n\n如有交易记录将无法删除。") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            viewModel.deleteAccount(acc)
                                            showDeleteConfirm = false
                                        }) {
                                            Text("删除", color = Red500)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
                                    },
                                )
                            }

                            if (showDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDialog = false },
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("编辑 ${acc.name}", modifier = Modifier.weight(1f))
                                            TextButton(onClick = {
                                                showDialog = false
                                                showDeleteConfirm = true
                                            }) {
                                                Text("删除账户", color = Red500)
                                            }
                                        }
                                    },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(
                                                value = editText,
                                                onValueChange = { editText = it },
                                                label = { Text("余额（可正可负）") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                            )
                                            if (acc.type == "bank") {
                                                TextButton(onClick = { showBankPicker = true; showDialog = false }) {
                                                    Text("选择银行卡…")
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            editText.toDoubleOrNull()?.let { viewModel.setBalance(acc.id, it) }
                                            showDialog = false
                                        }) { Text("确定") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDialog = false }) { Text("取消") }
                                    },
                                )
                            }

                            if (showBankPicker) {
                                val banks = Banks.all
                                AlertDialog(
                                    onDismissRequest = { showBankPicker = false },
                                    title = { Text("选择银行") },
                                    text = {
                                        LazyColumn {
                                            items(banks) { bank ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        viewModel.setBalance(acc.id, acc.balance) // keep balance
                                                        viewModel.updateAccountName(acc.id, bank.name, bank.colorHex)
                                                        showBankPicker = false
                                                    }.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    com.financetracker.ui.component.AccountLogo(type = "bank", accountName = bank.name, size = 28.dp)
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(bank.name, style = MaterialTheme.typography.bodyLarge)
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {},
                                    dismissButton = {
                                        TextButton(onClick = { showBankPicker = false }) { Text("取消") }
                                    },
                                )
                            }

                            if (i < accounts.size - 1) HorizontalDivider()
                        }
                        // Add account button
                        var showCreate by remember { mutableStateOf(false) }
                        var createType by remember { mutableStateOf("bank") }
                        var createName by remember { mutableStateOf("") }
                        var createColor by remember { mutableStateOf("#F5A623") }
                        var createBalance by remember { mutableStateOf("") }

                        TextButton(
                            onClick = { showCreate = true; createName = ""; createBalance = "" },
                            modifier = Modifier.fillMaxWidth().padding(4.dp),
                        ) {
                            Text("+ 添加账户")
                        }

                        if (showCreate) {
                            AlertDialog(
                                onDismissRequest = { showCreate = false },
                                title = { Text("添加账户") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        // Type selector
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("wechat" to "微信", "alipay" to "支付宝", "jd" to "京东", "bank" to "银行卡").forEach { (t, l) ->
                                                val sel = createType == t
                                                TextButton(onClick = {
                                                    createType = t
                                                    createName = if (t == "bank") "" else l
                                                    createColor = if (t == "wechat") "#07C160" else if (t == "alipay") "#1677FF" else if (t == "jd") "#E3312C" else "#F5A623"
                                                }) {
                                                    Text(l, color = if (sel) Green500 else MaterialTheme.colorScheme.onSurface, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                                }
                                            }
                                        }
                                        // Bank picker or name input
                                        if (createType == "bank") {
                                            var showBankList by remember { mutableStateOf(false) }
                                            TextButton(onClick = { showBankList = true }) {
                                                Text(if (createName.isBlank()) "选择银行…" else createName)
                                            }
                                            if (showBankList) {
                                                AlertDialog(
                                                    onDismissRequest = { showBankList = false },
                                                    title = { Text("选择银行") },
                                                    text = {
                                                        LazyColumn {
                                                            items(Banks.all) { bank ->
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                                        createName = bank.name; createColor = bank.colorHex; showBankList = false
                                                                    }.padding(8.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    com.financetracker.ui.component.AccountLogo(type = "bank", accountName = bank.name, size = 24.dp)
                                                                    Spacer(modifier = Modifier.width(12.dp))
                                                                    Text(bank.name)
                                                                }
                                                            }
                                                        }
                                                    },
                                                    confirmButton = {},
                                                    dismissButton = { TextButton(onClick = { showBankList = false }) { Text("取消") } },
                                                )
                                            }
                                        } else if (createType != "jd") {
                                            OutlinedTextField(value = createName, onValueChange = { createName = it }, label = { Text("名称") }, singleLine = true)
                                        }
                                        OutlinedTextField(
                                            value = createBalance,
                                            onValueChange = { createBalance = it },
                                            label = { Text("初始余额") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val bal = createBalance.toDoubleOrNull() ?: 0.0
                                        val name = createName.ifBlank { if (createType == "bank") "银行卡" else when (createType) { "wechat" -> "微信"; "alipay" -> "支付宝"; "jd" -> "京东"; else -> "其他" } }
                                        viewModel.createAccount(name, createType, createColor, bal)
                                        showCreate = false
                                    }) { Text("创建") }
                                },
                                dismissButton = { TextButton(onClick = { showCreate = false }) { Text("取消") } },
                            )
                        }
                    }
                }
            }

            // Export CSV
            item {
                TextButton(onClick = { viewModel.exportCsv(context) }) {
                    Text("导出 CSV")
                }
                when (val state = exportState) {
                    is SettingsViewModel.ExportState.Exporting -> {
                        Text("正在导出...", modifier = Modifier.padding(horizontal = 8.dp), fontSize = 12.sp)
                    }
                    is SettingsViewModel.ExportState.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("导出成功", color = Green500, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.clearExportState() }) { Text("关闭", fontSize = 12.sp) }
                            TextButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, state.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "分享 CSV"))
                            }) { Text("分享", fontSize = 12.sp) }
                        }
                    }
                    is SettingsViewModel.ExportState.Error -> {
                        Text("导出失败: ${state.message}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    else -> {}
                }
            }

            // ── Notification monitoring (bottom) ──
            item {
                Text("通知监听", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("自动记账", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "开启后监听支付通知自动记账",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.toggleNotification(enabled)
                                if (enabled) {
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text("前往无障碍设置")
                }
            }

            // Notification debug log
            item {
                val logEntries = remember { mutableStateOf(viewModel.getNotificationLog()) }
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = {
                    logEntries.value = viewModel.getNotificationLog()
                    expanded = !expanded
                }) {
                    Text(if (expanded) "▼ 通知日志 (${logEntries.value.size})" else "▶ 通知日志 (${logEntries.value.size})")
                }
                if (expanded && logEntries.value.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            logEntries.value.take(10).forEach { entry ->
                                Text(entry.shortForm, fontSize = 10.sp, modifier = Modifier.padding(vertical = 1.dp))
                            }
                            TextButton(onClick = { viewModel.clearNotificationLog(); logEntries.value = emptyList() }) {
                                Text("清空日志", fontSize = 12.sp)
                            }
                        }
                    }
                } else if (expanded) {
                    Text("暂无通知记录", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Version
            item {
                Text(
                    "记账助手 v${com.financetracker.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
    }
}
