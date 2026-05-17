package com.financetracker.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financetracker.ui.theme.Green500

@Composable
fun BalanceKeypad(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Row 1: 1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("1", "2", "3").forEach { key ->
                NumericKeyButton(key = key, onClick = { onKey(key) }, modifier = Modifier.weight(1f))
            }
        }
        // Row 2: 4 5 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("4", "5", "6").forEach { key ->
                NumericKeyButton(key = key, onClick = { onKey(key) }, modifier = Modifier.weight(1f))
            }
        }
        // Row 3: 7 8 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("7", "8", "9").forEach { key ->
                NumericKeyButton(key = key, onClick = { onKey(key) }, modifier = Modifier.weight(1f))
            }
        }
        // Row 4: . 0 ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NumericKeyButton(key = ".", onClick = { onKey(".") }, modifier = Modifier.weight(1f))
            NumericKeyButton(key = "0", onClick = { onKey("0") }, modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = onBackspace,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("⌫", fontSize = 18.sp)
            }
        }
        // Row 5: -/+ toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedButton(
                onClick = { onKey("-") },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("−", fontSize = 20.sp)
            }
            Button(
                onClick = onDone,
                modifier = Modifier.weight(2f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
            ) {
                Text("完成", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun NumericKeyButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = key,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}