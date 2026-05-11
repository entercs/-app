package com.financetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = Color.White,
    primaryContainer = Green500.copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFF002110),
    secondary = Blue500,
    onSecondary = Color.White,
    secondaryContainer = Blue500.copy(alpha = 0.12f),
    onSecondaryContainer = Color(0xFF001D36),
    tertiary = Orange500,
    onTertiary = Color.White,
    error = Red500,
    onError = Color.White,
    errorContainer = Red500.copy(alpha = 0.12f),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFDEDEDE),
    outlineVariant = Color(0xFFEBEBEB),
)

@Composable
fun FinanceTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content,
    )
}
