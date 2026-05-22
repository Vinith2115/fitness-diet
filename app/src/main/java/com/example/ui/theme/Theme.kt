package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPink,
    tertiary = NeonGreen,
    primaryContainer = GlassBg,
    onPrimaryContainer = TextWhite,
    outlineVariant = GlassBorder,
    background = PitchBlack,
    surface = GlassBg,
    onPrimary = PitchBlack,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = GlassBgSelected,
    onSurfaceVariant = TextGrayMuted
)

private val LightColorScheme = lightColorScheme(
    primary = NeonCyan,
    secondary = NeonPink,
    tertiary = NeonGreen,
    primaryContainer = GlassBg,
    onPrimaryContainer = TextWhite,
    outlineVariant = GlassBorder,
    background = PitchBlack,
    surface = GlassBg,
    onPrimary = PitchBlack,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = GlassBgSelected,
    onSurfaceVariant = TextGrayMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Both themes default to dark pitch black for consistent Glassmorphism look
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
