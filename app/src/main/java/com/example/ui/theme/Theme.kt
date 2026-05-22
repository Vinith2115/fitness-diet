package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFADC6FF),
    secondary = BoldBorderLine,
    tertiary = BoldPrimaryContainer,
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD1E4FF),
    outlineVariant = Color(0xFF384357),
    background = CharcoalSlate,
    surface = SteelSlate,
    onPrimary = Color(0xFF002E69),
    onSecondary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF2E3541),
    onSurfaceVariant = Color(0xFFC1C7CE)
)

private val LightColorScheme = lightColorScheme(
    primary = BoldPrimaryBlue,
    secondary = BoldOnPrimaryContainer,
    tertiary = BoldPrimaryContainer,
    primaryContainer = BoldPrimaryContainer,
    onPrimaryContainer = BoldOnPrimaryContainer,
    outlineVariant = BoldBorderLine,
    background = BoldBgSoftLight,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = BoldTextDark,
    onSurface = BoldTextDark,
    surfaceVariant = BoldSurfaceGrey,
    onSurfaceVariant = BoldTextMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
