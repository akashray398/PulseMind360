package com.dev.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFF80DFEA),
    secondary = VioletSecondary,
    onSecondary = Color.White,
    background = TechDarkBackground,
    onBackground = Color(0xFFE0E6ED),
    surface = TechDarkSurface,
    onSurface = Color(0xFFE0E6ED),
    surfaceVariant = TechDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF8A99AD),
    outline = TechDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00838F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF00363A),
    secondary = Color(0xFF8E24AA),
    onSecondary = Color.White,
    background = TechLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = TechLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = TechLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = TechLightBorder
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
