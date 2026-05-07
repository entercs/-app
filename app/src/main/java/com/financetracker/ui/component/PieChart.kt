package com.financetracker.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.financetracker.ui.theme.PieChartColors

data class PieSlice(val label: String, val value: Float, val color: Color)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    size: Float = 180f,
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) {
        Column(
            modifier = modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val canvasModifier = Modifier.size(size.dp)
        Canvas(modifier = canvasModifier) {
            var startAngle = -90f
            val strokeWidth = size / 4f
            val arcSize = Size(size - strokeWidth, size - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            slices.forEach { slice ->
                val sweepAngle = (slice.value / total) * 360f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth),
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        slices.forEach { slice ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = slice.color)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(slice.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(
                    "¥${String.format("%.2f", slice.value)} (${String.format("%.1f", slice.value / total * 100)}%)",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
