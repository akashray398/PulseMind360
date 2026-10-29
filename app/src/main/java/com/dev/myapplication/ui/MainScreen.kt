package com.dev.myapplication.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.myapplication.ui.automation.AutomationScreen
import com.dev.myapplication.ui.dashboard.DashboardScreen
import com.dev.myapplication.ui.deskstand.DeskStandScreen
import com.dev.myapplication.ui.focustimer.FocusTimerScreen
import com.dev.myapplication.ui.navigation.Screen
import com.dev.myapplication.ui.navigation.navScreens
import com.dev.myapplication.ui.privacy.PrivacyDashboardScreen
import com.dev.myapplication.ui.settings.SettingsScreen
import com.dev.myapplication.ui.timeline.TimelineScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Screen.DeskStand.route && currentRoute != Screen.PrivacyDashboard.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    navScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(if (showBottomBar) innerPadding else androidx.compose.foundation.layout.PaddingValues())
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(onOpenDeskStand = { navController.navigate(Screen.DeskStand.route) })
            }
            composable(Screen.Automations.route) {
                AutomationScreen()
            }
            composable(Screen.FocusMode.route) {
                FocusTimerScreen()
            }
            composable(Screen.History.route) {
                TimelineScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onOpenPrivacyDashboard = { navController.navigate(Screen.PrivacyDashboard.route) })
            }
            composable(Screen.DeskStand.route) {
                DeskStandScreen(onClose = { navController.popBackStack() })
            }
            composable(Screen.PrivacyDashboard.route) {
                PrivacyDashboardScreen(onClose = { navController.popBackStack() })
            }
        }
    }
}
