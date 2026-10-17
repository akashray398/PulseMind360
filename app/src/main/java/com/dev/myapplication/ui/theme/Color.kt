package com.dev.myapplication.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Futuristic Cyan & Violet Palette
val CyanPrimary = Color(0xFF00E5FF)
val CyanPrimaryDark = Color(0xFF00B8D4)
val VioletSecondary = Color(0xFFD500F9)
val VioletSecondaryDark = Color(0xAA00FF00)

val TechDarkBackground = Color(0xFF0B0F17)
val TechDarkSurface = Color(0xFF161C26)
val TechDarkSurfaceVariant = Color(0xFF222B38)
val TechDarkBorder = Color(0xFF2E3A4E)

val TechLightBackground = Color(0xFFF4F7FB)
val TechLightSurface = Color(0xFFFFFFFF)
val TechLightSurfaceVariant = Color(0xFFE2E8F0)
val TechLightBorder = Color(0xFFCBD5E1)

val StatusGood = Color(0xFF00E676)
val StatusWarning = Color(0xFFFFAB00)
val StatusError = Color(0xFFFF1744)

val FuturisticGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00E5FF), Color(0xFF8000FF))
)

val BatteryGradientCharging = Brush.verticalGradient(
    colors = listOf(Color(0xFF00E676), Color(0xFF00B0FF))
)

val BatteryGradientNormal = Brush.verticalGradient(
    colors = listOf(Color(0xFF00E5FF), Color(0xFF3D5AFE))
)

val BatteryGradientLow = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF1744), Color(0xFFFF9100))
)
