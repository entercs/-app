package com.financetracker.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financetracker.domain.model.Category
import com.financetracker.domain.model.PaymentAccount
import com.financetracker.domain.model.Transaction
import com.financetracker.domain.model.TransactionType
import com.financetracker.ui.theme.Green500
import com.financetracker.ui.theme.Red500
import com.financetracker.ui.theme.accountIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    account: PaymentAccount?,
    toAccount: PaymentAccount? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(account?.color ?: "#757575"))
    } catch (_: Exception) {
        Color(0xFF757575)
    }

    Card(
        modifier = modifier.fillMaxWidth().then(
            if (onLongClick != null) Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
            else Modifier.clickable(onClick = onClick)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        val isTransfer = transaction.type == TransactionType.TRANSFER

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category icon
            CategoryLogo(
                categoryName = if (isTransfer) "" else (category?.name ?: ""),
                categoryIcon = if (isTransfer) "🔄" else (category?.icon ?: "📂"),
                size = 28.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                // Category name as primary
                val titleText = if (isTransfer) "转账" else (category?.name ?: "未知分类")
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Merchant (if exists)
                if (transaction.merchant.isNotBlank()) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Account badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.financetracker.ui.theme.AccountIconDisplay(
                        type = account?.type ?: "",
                        accountName = account?.name ?: "",
                        size = 18.dp,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = account?.name ?: "未知账户",
                        style = MaterialTheme.typography.bodySmall,
                        color = bgColor,
                        fontWeight = FontWeight.Medium,
                    )
                    if (isTransfer && toAccount != null) {
                        Text(" → ", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                        Text(toAccount.name, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575), fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Amount
            val prefix = when {
                isTransfer -> "↔"
                transaction.type == TransactionType.INCOME -> "+"
                else -> "-"
            }
            val color = if (isTransfer) Color(0xFF757575) else if (transaction.type == TransactionType.INCOME) Green500 else Red500
            Text(
                text = "$prefix${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color,
            )
        }
    }
}

private val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
private fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
