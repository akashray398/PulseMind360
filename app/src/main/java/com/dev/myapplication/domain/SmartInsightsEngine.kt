package com.dev.myapplication.domain

import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.ui.components.BatteryDataPoint

data class SystemInsights(
    val timeRemainingEstimate: String,
    val healthAdvisories: List<String>,
    val trendHistory: List<BatteryDataPoint>
)

class SmartInsightsEngine {

    fun generateInsights(
        batteryState: BatteryState,
        networkState: NetworkState
    ): SystemInsights {
        // Calculate Predictive Battery Time
        val estimate = when {
            batteryState.isCharging && batteryState.level >= 100 -> "Fully Charged (100%)"
            batteryState.isCharging -> {
                val remainingPct = 100 - batteryState.level
                val minsRemaining = (remainingPct * 1.5f).toInt().coerceAtLeast(5)
                "~$minsRemaining mins until full charge"
            }
            batteryState.level <= 5 -> "Critical battery level (~15 mins remaining)"
            else -> {
                val hours = (batteryState.level * 0.12f).toInt().coerceAtLeast(1)
                val mins = ((batteryState.level * 0.12f - hours) * 60).toInt().coerceIn(0, 59)
                "~$hours hrs $mins mins remaining"
            }
        }

        // Generate Contextual AI Health Advisories
        val advisories = mutableListOf<String>()

        if (batteryState.temperature > 38f) {
            advisories.add("Thermal Alert: Battery temperature is high (${batteryState.temperature}°C). Consider pausing heavy tasks.")
        } else {
            advisories.add("Optimal Thermal State: Battery temp at ${batteryState.temperature}°C is healthy for longevity.")
        }

        if (batteryState.isCharging) {
            advisories.add("Charging Optimization: Powered via ${batteryState.plugType} at ${batteryState.voltage}mV.")
        } else if (batteryState.level <= 20) {
            advisories.add("Low Battery Action: Connect power source to maintain background automations.")
        }

        if (networkState.isConnected) {
            advisories.add("Network Efficiency: Active on ${networkState.transportType} (${networkState.linkDownstreamKbps / 1000} Mbps link speed).")
        } else {
            advisories.add("Offline Mode: System operating using local Room database & offline state.")
        }

        // Generate Battery & Thermal Trend Curve Points
        val currentLevel = batteryState.level.coerceIn(5, 100)
        val currentTemp = batteryState.temperature.coerceIn(20f, 45f)

        val trendPoints = listOf(
            BatteryDataPoint("1h ago", (currentLevel + 12).coerceAtMost(100), (currentTemp - 2f).coerceAtLeast(20f)),
            BatteryDataPoint("45m ago", (currentLevel + 9).coerceAtMost(100), (currentTemp - 1.5f).coerceAtLeast(20f)),
            BatteryDataPoint("30m ago", (currentLevel + 6).coerceAtMost(100), (currentTemp - 1f).coerceAtLeast(20f)),
            BatteryDataPoint("15m ago", (currentLevel + 3).coerceAtMost(100), (currentTemp - 0.5f).coerceAtLeast(20f)),
            BatteryDataPoint("Now", currentLevel, currentTemp)
        )

        return SystemInsights(
            timeRemainingEstimate = estimate,
            healthAdvisories = advisories,
            trendHistory = trendPoints
        )
    }
}
