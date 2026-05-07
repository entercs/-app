package com.financetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Green500.copy(alpha = 0.15f),
    secondary = Blue500,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    error = Red500,
    onError = androidx.compose.ui.graphics.Color.White,
)

@Composable
fun FinanceTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content,
    )
}
