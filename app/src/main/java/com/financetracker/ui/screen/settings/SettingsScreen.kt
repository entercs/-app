package com.financetracker.ui.screen.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financetracker.di.AppModule
import com.financetracker.ui.theme.Green500

@OptIn(ExperimentalMaterial3Api::class)
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
    val exportState by viewModel.exportState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green500,
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            // Notification monitoring
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

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Data management
            item {
                Text("数据管理", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsRow("初始化分类数据") { viewModel.seedCategories() }
                        HorizontalDivider()
                        SettingsRow("初始化账户数据") { viewModel.seedAccounts() }
                        HorizontalDivider()
                        SettingsRow("导出 CSV") { viewModel.exportCsv(context) }
                    }
                }
            }

            // Export state
            item {
                when (val state = exportState) {
                    is SettingsViewModel.ExportState.Exporting -> {
                        Text("正在导出...", modifier = Modifier.padding(8.dp))
                    }
                    is SettingsViewModel.ExportState.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("导出成功", color = Green500)
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.clearExportState() }) {
                                Text("关闭")
                            }
                            TextButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, state.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "分享 CSV"))
                            }) {
                                Text("分享")
                            }
                        }
                    }
                    is SettingsViewModel.ExportState.Error -> {
                        Text("导出失败: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // About
            item {
                Text("关于", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("记账助手 v1.0.0", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "自动监听支付通知，轻松管理个人财务",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
