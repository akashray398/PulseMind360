package com.dev.myapplication.domain.copilot

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CopilotIntentEngine @Inject constructor() {

    fun classifyIntent(query: String): CopilotIntent {
        val q = query.lowercase().trim()

        return when {
            // Follow-up history intent
            q.contains("happen before") || q.contains("happened before") || q.contains("occurred before") ||
            q.contains("previous time") || q.contains("last time") || q.contains("yesterday") -> {
                CopilotIntent.FOLLOW_UP_HISTORY
            }

            // Battery drain
            q.contains("battery") || q.contains("drain") || q.contains("power drop") || q.contains("discharging") -> {
                CopilotIntent.BATTERY_DRAIN_WHY
            }

            // Thermal heating
            q.contains("heat") || q.contains("hot") || q.contains("thermal") || q.contains("warm") || q.contains("temperature") -> {
                CopilotIntent.THERMAL_HEATING_WHY
            }

            // Current state / happening now
            q.contains("right now") || q.contains("happening with my device") || q.contains("current state") || q.contains("vitals") -> {
                CopilotIntent.DEVICE_CURRENT_STATE
            }

            // Normal behavior check
            q.contains("behaving normally") || q.contains("is my device normal") || q.contains("operating normally") -> {
                CopilotIntent.DEVICE_NORMAL_CHECK
            }

            // Latest anomalies
            q.contains("anomalies") || q.contains("anomaly") || q.contains("spike") || q.contains("alerts") -> {
                CopilotIntent.LATEST_ANOMALIES
            }

            // Changes today
            q.contains("changed today") || q.contains("device changed") || q.contains("recent changes") || q.contains("change log") -> {
                CopilotIntent.TODAY_CHANGES
            }

            // Network stability
            q.contains("network") || q.contains("wifi") || q.contains("cellular") || q.contains("bandwidth") || q.contains("ping") || q.contains("internet") -> {
                CopilotIntent.NETWORK_STABILITY
            }

            // Investigation priority
            q.contains("investigate first") || q.contains("priority") || q.contains("what to check") || q.contains("troubleshoot") -> {
                CopilotIntent.INVESTIGATION_PRIORITY
            }

            // Incident explanation
            q.contains("incident") || q.contains("log detail") || q.contains("explain alert") || q.contains("critical error") -> {
                CopilotIntent.EXPLAIN_INCIDENT
            }

            // Overall health summary
            q.contains("health") || q.contains("summary") || q.contains("status") || q.contains("overview") || q.contains("diagnostic") -> {
                CopilotIntent.HEALTH_SUMMARY
            }

            else -> CopilotIntent.UNSUPPORTED
        }
    }
}
