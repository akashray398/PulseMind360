package com.dev.myapplication.domain

import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState

data class AnomalyReport(
    val hasAnomaly: Boolean = false,
    val title: String = "All Systems Nominal",
    val description: String = "No abnormal battery drain, thermal spikes, or network anomalies detected.",
    val severity: String = "INFO" // INFO, WARNING, ALERT
)

class AnomalyDetector {

    fun detectAnomalies(
        batteryState: BatteryState,
        networkState: NetworkState
    ): AnomalyReport {
        return when {
            batteryState.temperature >= 38.0f -> AnomalyReport(
                hasAnomaly = true,
                title = "Thermal Anomaly Spike Detected",
                description = "Battery temperature reaches ${batteryState.temperature}°C. Background process throttling recommended.",
                severity = "ALERT"
            )
            !batteryState.isCharging && batteryState.level <= 15 -> AnomalyReport(
                hasAnomaly = true,
                title = "Rapid Battery Depletion Velocity",
                description = "Battery level critically low at ${batteryState.level}%. Active discharge velocity exceeds baseline.",
                severity = "WARNING"
            )
            networkState.isConnected && networkState.isMetered && networkState.linkDownstreamKbps > 50000 -> AnomalyReport(
                hasAnomaly = true,
                title = "High Metered Network Bandwidth Usage",
                description = "Cellular/Metered connection is consuming high downstream bandwidth (${networkState.linkDownstreamKbps / 1000} Mbps).",
                severity = "WARNING"
            )
            else -> AnomalyReport()
        }
    }
}
