package com.financetracker.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.financetracker.ui.theme.AlipayBlue
import com.financetracker.ui.theme.BankOrange
import com.financetracker.ui.theme.JDRed
import com.financetracker.ui.theme.WeChatGreen

/**
 * Draws a brand logo for a payment account by type.
 * Returns true if a custom logo was drawn, false for fallback to emoji.
 */
@Composable
fun AccountLogo(type: String, accountName: String = "", size: Dp = 22.dp, modifier: Modifier = Modifier) {
    val color = when (type) {
        "wechat" -> WeChatGreen
        "alipay" -> AlipayBlue
        "jd" -> JDRed
        "bank" -> BankOrange
        else -> Color.Gray
    }
    val dpSize = size

    when (type) {
        "wechat" -> WeChatLogo(color, dpSize, modifier)
        "alipay" -> AlipayLogo(color, dpSize, modifier)
        "jd" -> JDLogo(color, dpSize, modifier)
        "bank" -> BankLogo(color, dpSize, modifier)
        else -> {
            // Fallback: colored circle with first character
            val label = accountName.firstOrNull()?.toString() ?: "?"
            GenericLogo(color, label, dpSize, modifier)
        }
    }
}

@Composable
private fun WeChatLogo(color: Color, size: Dp, modifier: Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        val pad = s * 0.1f
        // Green rounded square background
        drawRoundRect(color = color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.2f))
        // White speech bubble
        val bubbleColor = Color.White
        val bw = s * 0.55f
        val bh = s * 0.45f
        val bx = (s - bw) / 2f
        val by = s * 0.22f
        val path = Path().apply {
            moveTo(bx + bw * 0.15f, by)
            lineTo(bx + bw * 0.85f, by)
            lineTo(bx + bw * 0.85f, by + bh * 0.7f)
            lineTo(bx + bw * 0.6f, by + bh * 0.7f)
            lineTo(bx + bw * 0.5f, by + bh)
            lineTo(bx + bw * 0.4f, by + bh * 0.7f)
            lineTo(bx + bw * 0.15f, by + bh * 0.7f)
            close()
        }
        drawPath(path, color = bubbleColor)
        // Eyes
        val eyeR = s * 0.035f
        drawCircle(Color(0xFF333333), eyeR, Offset(bx + bw * 0.38f, by + bh * 0.3f))
        drawCircle(Color(0xFF333333), eyeR, Offset(bx + bw * 0.62f, by + bh * 0.3f))
        // Smile
        drawArc(Color(0xFF333333), startAngle = 0f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(bx + bw * 0.35f, by + bh * 0.35f),
            size = Size(bw * 0.3f, bh * 0.25f),
            style = Stroke(width = s * 0.025f))
    }
}

@Composable
private fun AlipayLogo(color: Color, size: Dp, modifier: Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        // Blue rounded square
        drawRoundRect(color = color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.2f))
        // White "支" character approximation
        val cx = s / 2f; val cy = s / 2f; val r = s * 0.32f
        drawCircle(Color.White, r, Offset(cx, cy - s * 0.05f))
        // Lower field
        drawArc(Color.White, startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - r, cy - s * 0.05f), size = Size(r * 2f, r * 1.5f),
            style = Stroke(width = s * 0.04f))
        // Horizontal bar
        drawLine(Color.White, Offset(cx - r * 0.6f, cy + r * 0.15f), Offset(cx + r * 0.6f, cy + r * 0.15f), strokeWidth = s * 0.04f)
        // Vertical bar
        drawLine(Color.White, Offset(cx, cy - s * 0.15f), Offset(cx, cy + r * 0.15f), strokeWidth = s * 0.04f)
    }
}

@Composable
private fun JDLogo(color: Color, size: Dp, modifier: Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        // Red rounded square
        drawRoundRect(color = color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.2f))
        // White dog paw icon (JD mascot)
        val cx = s / 2f; val cy = s * 0.45f
        val pr = s * 0.15f
        // Main pad
        drawCircle(Color.White, s * 0.12f, Offset(cx, cy + s * 0.15f))
        // Toes
        val toeSpacing = s * 0.18f
        drawCircle(Color.White, pr, Offset(cx - toeSpacing, cy - pr * 0.3f))
        drawCircle(Color.White, pr, Offset(cx + toeSpacing, cy - pr * 0.3f))
        drawCircle(Color.White, pr * 0.85f, Offset(cx, cy - pr * 0.7f))
    }
}

@Composable
private fun BankLogo(color: Color, size: Dp, modifier: Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        // Orange rounded square
        drawRoundRect(color = color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.2f))
        // White building/pillar shape
        val bw = s * 0.5f; val bh = s * 0.55f
        val bx = (s - bw) / 2f; val by = s * 0.3f
        // Building body
        drawRect(Color.White, topLeft = Offset(bx, by), size = Size(bw, bh))
        // Roof triangle
        val path = Path().apply {
            moveTo(bx - s * 0.04f, by)
            lineTo(s / 2f, by - s * 0.18f)
            lineTo(bx + bw + s * 0.04f, by)
            close()
        }
        drawPath(path, Color.White)
        // Columns (3 vertical lines)
        val colW = s * 0.035f
        for (i in 0 until 3) {
            val colX = bx + bw * 0.25f + i * bw * 0.25f
            drawRect(Color(0xFF555555), Offset(colX - colW / 2f, by + bh * 0.45f), Size(colW, bh * 0.55f))
        }
    }
}

@Composable
private fun GenericLogo(color: Color, label: String, size: Dp, modifier: Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        drawRoundRect(color = color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.25f))
        // Use drawContext for text is not straightforward in Canvas, so we just draw the circle
        drawCircle(Color.White.copy(alpha = 0.3f), s * 0.2f, Offset(s / 2f, s / 2f))
    }
}
