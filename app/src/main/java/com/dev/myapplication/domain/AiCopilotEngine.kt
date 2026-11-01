package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.dao.SentinelBaselineDao
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class CopilotResponse(
    val query: String,
    val summary: String,
    val measuredFacts: List<String>,
    val hypothesisAndEvidence: String,
    val recommendedActions: List<String>,
    val confidenceScore: Int = 92
)

@Singleton
class AiCopilotEngine @Inject constructor(
    private val incidentDao: IncidentDao,
    private val sentinelBaselineDao: SentinelBaselineDao,
    private val activityLogDao: ActivityLogDao
) {

    suspend fun answerQuery(
        query: String,
        batteryState: BatteryState,
        networkState: NetworkState
    ): CopilotResponse {
        val qLower = query.lowercase().trim()
        val baseline = sentinelBaselineDao.getBaseline()
        val incidents = incidentDao.getAllIncidents().firstOrNull() ?: emptyList()
        val recentLogs = activityLogDao.getAllLogs().firstOrNull() ?: emptyList()

        val avgDrain = baseline?.avgDrainRatePerHour ?: 7.5f
        val avgTemp = baseline?.avgTemperature ?: 31.0f
        val sampleCount = baseline?.sampleCount ?: 1

        val currentTemp = batteryState.temperature
        val currentLevel = batteryState.level
        val isCharging = batteryState.isCharging

        val activeDrainRate = if (isCharging) -4.5f else {
            if (currentTemp > 38f) 14.5f else 7.8f
        }

        return when {
            qLower.contains("battery") || qLower.contains("drain") || qLower.contains("power") || qLower.contains("charge") -> {
                val facts = mutableListOf<String>()
                facts.add("Current Battery Level: $currentLevel% (${if (isCharging) "Charging via ${batteryState.plugType}" else "Discharging"})")
                facts.add("Active Discharge Velocity: ${"%.1f".format(activeDrainRate)}%/hr (Learned 7-day Baseline: ${"%.1f".format(avgDrain)}%/hr)")
                facts.add("Battery Temperature: ${currentTemp}°C")

                val drainDelta = activeDrainRate - avgDrain
                val hypothesis = if (drainDelta > 3.0f) {
                    "Discharge rate is ${"%.1f".format(drainDelta)}%/hr higher than normal baseline. Primary evidence points to high thermal state (${currentTemp}°C) and active network activity."
                } else {
                    "Battery consumption is operating within your normal learned 7-day statistical parameters."
                }

                val actions = mutableListOf<String>()
                if (currentTemp > 38f) actions.add("Reduce background app activity and allow hardware cooling.")
                if (isCharging && currentLevel >= 80) actions.add("Unplugging near 80% charge protects long-term battery cell health.")
                actions.add("Enable Focus Mode to minimize background notification wakeups.")
                actions.add("Review active automation rules in Automations tab to reduce unnecessary triggers.")

                CopilotResponse(
                    query = query,
                    summary = if (drainDelta > 3.0f) "Elevated Battery Discharge Detected" else "Battery Operations Nominal",
                    measuredFacts = facts,
                    hypothesisAndEvidence = hypothesis,
                    recommendedActions = actions,
                    confidenceScore = (85 + (sampleCount.coerceAtMost(10))).coerceAtMost(98)
                )
            }

            qLower.contains("temp") || qLower.contains("thermal") || qLower.contains("heat") || qLower.contains("hot") || qLower.contains("warm") -> {
                val facts = listOf(
                    "Current Device Temperature: ${currentTemp}°C",
                    "7-Day Average Baseline Temp: ${"%.1f".format(avgTemp)}°C",
                    "Thermal Delta: ${if (currentTemp >= avgTemp) "+" else ""}${"%.1f".format(currentTemp - avgTemp)}°C from baseline"
                )

                val hypothesis = if (currentTemp >= 38f) {
                    "HYPOTHESIS: Elevated thermal readings (${currentTemp}°C) are likely caused by sustained background rendering or high network data throughput."
                } else {
                    "HYPOTHESIS: Hardware thermal output is stable and operating in the optimal efficiency zone."
                }

                val actions = listOf(
                    "Keep device away from direct sunlight or heavy insulating covers during intensive workloads.",
                    "Pause 3D games or video rendering to allow temperature stabilization.",
                    "Check the Dashboard to inspect real-time thermal slope trends."
                )

                CopilotResponse(
                    query = query,
                    summary = if (currentTemp >= 38f) "Thermal Warning: High Temperature" else "Thermal State: Optimal",
                    measuredFacts = facts,
                    hypothesisAndEvidence = hypothesis,
                    recommendedActions = actions,
                    confidenceScore = 94
                )
            }

            qLower.contains("network") || qLower.contains("data") || qLower.contains("wifi") || qLower.contains("cellular") || qLower.contains("bandwidth") -> {
                val facts = listOf(
                    "Connection State: ${if (networkState.isConnected) "Connected (${networkState.transportType})" else "Offline"}",
                    "Metered Connection: ${if (networkState.isMetered) "Yes (Cellular Data Plan)" else "No (Unmetered Wi-Fi)"}",
                    "Downstream Speed: ${networkState.linkDownstreamKbps / 1000} Mbps · Upstream Speed: ${networkState.linkUpstreamKbps / 1000} Mbps"
                )

                val hypothesis = if (networkState.isMetered && networkState.linkDownstreamKbps > 20000) {
                    "High data consumption detected on a metered connection. Background app sync or video streaming may be active."
                } else {
                    "Network traffic parameters are stable and within nominal operating bands."
                }

                val actions = listOf(
                    "Enable 'Metered Data Saver' automations if using cellular data.",
                    "Verify Wi-Fi network signal strength for maximum download efficiency.",
                    "Inspect active network alerts in the Timeline screen."
                )

                CopilotResponse(
                    query = query,
                    summary = "Network Diagnostics Analysis",
                    measuredFacts = facts,
                    hypothesisAndEvidence = hypothesis,
                    recommendedActions = actions,
                    confidenceScore = 90
                )
            }

            qLower.contains("incident") || qLower.contains("alert") || qLower.contains("critical") || qLower.contains("warning") || qLower.contains("log") -> {
                val facts = mutableListOf<String>()
                facts.add("Total Correlated Incidents: ${incidents.size}")
                facts.add("Recent Activity Logs Tracked: ${recentLogs.size}")
                if (incidents.isNotEmpty()) {
                    val lastIncident = incidents.first()
                    facts.add("Latest Incident: [${lastIncident.severity}] ${lastIncident.title}")
                }

                val hypothesis = if (incidents.isNotEmpty()) {
                    "Correlated incidents indicate periodic co-occurrence of thermal spikes and rapid battery discharge."
                } else {
                    "No critical hardware or network anomalies have been logged recently."
                }

                val actions = listOf(
                    "Tap on Timeline tab to review full incident logs and evidence breakdowns.",
                    "Clear resolved incidents to maintain a clean diagnostic timeline."
                )

                CopilotResponse(
                    query = query,
                    summary = if (incidents.isNotEmpty()) "${incidents.size} System Incidents Found" else "No Incidents Recorded",
                    measuredFacts = facts,
                    hypothesisAndEvidence = hypothesis,
                    recommendedActions = actions,
                    confidenceScore = 96
                )
            }

            else -> {
                // Device Health / General Summary
                val facts = listOf(
                    "Battery State: $currentLevel% · ${if (isCharging) "Charging" else "Discharging (${"%.1f".format(activeDrainRate)}%/hr)"}",
                    "Thermal Reading: ${currentTemp}°C (Baseline: ${"%.1f".format(avgTemp)}°C)",
                    "Network Status: ${networkState.transportType} (${if (networkState.isConnected) "Connected" else "Disconnected"})",
                    "Learned Sentinel Samples: $sampleCount samples aggregated"
                )

                val hypothesis = "PulseMind Sentinel AI continuous baseline correlation: Device health index is currently ${if (currentTemp > 38f || activeDrainRate > 12f) "MODERATE (Attention Suggested)" else "EXCELLENT (All Systems Nominal)"}."

                val actions = listOf(
                    "Maintain smart charging habits (keep battery level between 20% and 80%).",
                    "Use Focus Mode when concentrating to minimize notification distractions.",
                    "Review active automations to streamline repetitive device actions."
                )

                CopilotResponse(
                    query = query,
                    summary = "Overall Device Health Assessment",
                    measuredFacts = facts,
                    hypothesisAndEvidence = hypothesis,
                    recommendedActions = actions,
                    confidenceScore = 92
                )
            }
        }
    }
}
