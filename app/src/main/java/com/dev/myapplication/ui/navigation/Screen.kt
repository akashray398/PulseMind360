package com.dev.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Automations : Screen("automations", "Automations", Icons.Default.Autorenew)
    object FocusMode : Screen("focus_mode", "Focus", Icons.Default.Timer)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object DeskStand : Screen("desk_stand", "Desk Mode", Icons.Default.Settings)
    object PrivacyDashboard : Screen("privacy_dashboard", "Privacy", Icons.Default.Settings)
}

val navScreens = listOf(
    Screen.Dashboard,
    Screen.Automations,
    Screen.FocusMode,
    Screen.History,
    Screen.Settings
)
