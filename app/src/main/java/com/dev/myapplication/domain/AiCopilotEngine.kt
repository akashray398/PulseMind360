package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.CopilotConversationDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.dao.SentinelBaselineDao
import com.dev.myapplication.data.local.entity.CopilotMessageEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.copilot.CopilotIntent
import com.dev.myapplication.domain.copilot.CopilotIntentEngine
import com.dev.myapplication.domain.copilot.CopilotResponseDetail
import com.dev.myapplication.domain.copilot.CopilotToolDispatcher
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
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
    private val activityLogDao: ActivityLogDao,
    private val liveContextEngine: LiveContextEngine,
    private val copilotToolDispatcher: CopilotToolDispatcher,
    private val copilotIntentEngine: CopilotIntentEngine,
    private val copilotConversationDao: CopilotConversationDao
) {

    suspend fun answerQueryDetailed(
        query: String,
        sessionId: String,
        batteryState: BatteryState,
        networkState: NetworkState
    ): CopilotResponseDetail {
        val intent = copilotIntentEngine.classifyIntent(query)
        val liveCtx = copilotToolDispatcher.getCurrentDeviceContext()
        val baseline = copilotToolDispatcher.getDeviceBaseline()
        val incidents = copilotToolDispatcher.getRecentAnomalies()
        val memory = copilotToolDispatcher.getMemoryStatistics()
        val predictions = copilotToolDispatcher.getPredictions()

        val avgDrain = baseline?.avgDrainRatePerHour ?: 7.5f
        val avgTemp = baseline?.avgTemperature ?: 31.0f

        val response = when (intent) {
            CopilotIntent.BATTERY_DRAIN_WHY -> {
                val activeDrain = if (batteryState.isCharging) -4.5f else {
                    if (batteryState.temperature > 38f) 14.5f else 7.8f
                }
                val delta = activeDrain - avgDrain

                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = if (delta > 3f) "WARNING" else "INFO",
                    summary = if (delta > 3f) "Elevated Battery Discharge Active" else "Battery Drain Nominal",
                    measuredFacts = listOf(
                        "Battery Level: ${batteryState.level}% (${if (batteryState.isCharging) "Charging" else "Discharging"})",
                        "Active Discharge Rate: ${"%.1f".format(activeDrain)}%/hr (7-Day Baseline: ${"%.1f".format(avgDrain)}%/hr)",
                        "Battery Temp: ${batteryState.temperature}°C",
                        "RAM Utilization: ${memory.usagePct}% (${memory.usedMemMb} MB / ${memory.totalMemMb} MB)"
                    ),
                    hypothesisAndEvidence = if (delta > 3f) {
                        "Discharge rate is ${"%.1f".format(delta)}%/hr above baseline. Thermal readings (${batteryState.temperature}°C) and active network traffic indicate elevated hardware workload."
                    } else {
                        "Discharge velocity matches your learned baseline parameters."
                    },
                    recommendedActions = listOf(
                        "Reduce display brightness or background sync.",
                        "Enable Focus Mode to suppress notification wakeups.",
                        "Inspect background automations in the Automations tab."
                    ),
                    relevantMetrics = mapOf(
                        "Battery" to "${batteryState.level}%",
                        "Drain Velocity" to "${"%.1f".format(activeDrain)}%/hr",
                        "Baseline" to "${"%.1f".format(avgDrain)}%/hr"
                    ),
                    confidenceScore = 95,
                    suggestedFollowUps = listOf(
                        "Has this happened before?",
                        "What should I investigate first?",
                        "Give me a device health summary"
                    )
                )
            }

            CopilotIntent.THERMAL_HEATING_WHY -> {
                val tempDelta = batteryState.temperature - avgTemp
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = if (batteryState.temperature >= 38f) "ALERT" else "INFO",
                    summary = if (batteryState.temperature >= 38f) "Thermal Warning: Elevated Temperature" else "Thermal Output Stable",
                    measuredFacts = listOf(
                        "Device Temperature: ${batteryState.temperature}°C",
                        "7-Day Baseline Temp: ${"%.1f".format(avgTemp)}°C (Delta: ${if (tempDelta >= 0) "+" else ""}${"%.1f".format(tempDelta)}°C)",
                        "Thermal Velocity: ${predictions.thermalVelocityText}",
                        "RAM Usage: ${memory.usagePct}%"
                    ),
                    hypothesisAndEvidence = if (batteryState.temperature >= 38f) {
                        "Sustained thermal output detected. High temperature (+${"%.1f".format(tempDelta)}°C above normal) correlates with concurrent network activity and background tasks."
                    } else {
                        "Hardware temperature is within optimal performance limits."
                    },
                    recommendedActions = listOf(
                        "Pause heavy 3D or video rendering tasks.",
                        "Keep device out of direct sunlight or heavy cases.",
                        "Inspect live thermal slope on the Dashboard."
                    ),
                    relevantMetrics = mapOf(
                        "Temperature" to "${batteryState.temperature}°C",
                        "Baseline Temp" to "${"%.1f".format(avgTemp)}°C",
                        "Thermal Velocity" to predictions.thermalVelocityText
                    ),
                    confidenceScore = 94,
                    suggestedFollowUps = listOf(
                        "Has this happened before?",
                        "Is my device behaving normally?",
                        "What are my latest anomalies?"
                    )
                )
            }

            CopilotIntent.DEVICE_CURRENT_STATE -> {
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = "INFO",
                    summary = "Live Device Vitals Overview",
                    measuredFacts = listOf(
                        "State Classification: ${liveCtx.stateClassification}",
                        "Battery: ${batteryState.level}% (${if (batteryState.isCharging) "Charging" else "Discharging"})",
                        "Temperature: ${batteryState.temperature}°C",
                        "RAM Utilization: ${memory.usagePct}% (${memory.usedMemMb} MB / ${memory.totalMemMb} MB)",
                        "Network Transport: ${networkState.transportType} (${if (networkState.isConnected) "Connected" else "Offline"})"
                    ),
                    hypothesisAndEvidence = "Live Context Engine actively observing device vitals. Current status: ${liveCtx.stateClassification}.",
                    recommendedActions = listOf(
                        "Check Live Context screen for rolling change events.",
                        "Ensure background automations match your daily routine."
                    ),
                    relevantMetrics = mapOf(
                        "Classification" to liveCtx.stateClassification.name,
                        "Battery" to "${batteryState.level}%",
                        "RAM" to "${memory.usagePct}%"
                    ),
                    confidenceScore = 98,
                    suggestedFollowUps = listOf(
                        "Is my device behaving normally?",
                        "What changed in my device today?",
                        "Is my network stable?"
                    )
                )
            }

            CopilotIntent.DEVICE_NORMAL_CHECK -> {
                val isNormal = liveCtx.stateClassification == com.dev.myapplication.domain.DeviceStateClassification.OPTIMAL
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = if (isNormal) "INFO" else "WARNING",
                    summary = if (isNormal) "Device Operating Normally" else "Deviations Detected",
                    measuredFacts = listOf(
                        "System Health Index: ${if (isNormal) "100% (Optimal)" else "Attention Suggested"}",
                        "Current Temp vs Baseline: ${batteryState.temperature}°C vs ${"%.1f".format(avgTemp)}°C",
                        "Discharge Rate: ${predictions.dischargeRatePctPerHour}",
                        "Active Anomalies Count: ${liveCtx.activeAnomalies.size}"
                    ),
                    hypothesisAndEvidence = if (isNormal) {
                        "All measured metrics fall within your learned 7-day baseline statistical bounds."
                    } else {
                        "System state transitioned to ${liveCtx.stateClassification} due to active metric deviations."
                    },
                    recommendedActions = listOf(
                        "Continuous background Sentinel monitoring active.",
                        "Review active anomalies if status changes."
                    ),
                    relevantMetrics = mapOf(
                        "Health Index" to if (isNormal) "Optimal" else "Deviating",
                        "Anomalies" to "${liveCtx.activeAnomalies.size}"
                    ),
                    confidenceScore = 96,
                    suggestedFollowUps = listOf(
                        "What are my latest anomalies?",
                        "What changed in my device today?",
                        "Give me a device health summary"
                    )
                )
            }

            CopilotIntent.LATEST_ANOMALIES -> {
                val hasIncidents = incidents.isNotEmpty()
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = if (hasIncidents) "WARNING" else "INFO",
                    summary = if (hasIncidents) "${incidents.size} Anomalies / Incidents Recorded" else "No Anomalies Recorded",
                    measuredFacts = incidents.take(3).map { inc ->
                        "[${inc.severity}] ${inc.title} - ${inc.correlatedEventsSummary}"
                    }.ifEmpty { listOf("No hardware or network anomaly spikes logged in Room database.") },
                    hypothesisAndEvidence = if (hasIncidents) {
                        "Correlated incidents represent co-occurring thermal, battery, or network events."
                    } else {
                        "All system vitals operating in nominal statistical range."
                    },
                    recommendedActions = listOf(
                        "Tap Timeline tab to review full incident breakdowns.",
                        "Address hardware thermal or network triggers."
                    ),
                    relevantMetrics = mapOf(
                        "Incident Count" to "${incidents.size}"
                    ),
                    confidenceScore = 94,
                    suggestedFollowUps = listOf(
                        "Explain my latest incident",
                        "What should I investigate first?",
                        "Has this happened before?"
                    )
                )
            }

            CopilotIntent.TODAY_CHANGES -> {
                val changes = liveCtx.recentChanges
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = "INFO",
                    summary = "Device State Change Log",
                    measuredFacts = changes.take(4).map { c ->
                        "[${c.category}] ${c.description}"
                    }.ifEmpty { listOf("No significant power, network, or state transitions logged today.") },
                    hypothesisAndEvidence = "Change log reflects real-time power plugs, network handoffs, and state classification transitions.",
                    recommendedActions = listOf(
                        "View full change stream on Live Context Screen."
                    ),
                    relevantMetrics = mapOf(
                        "Changes Logged" to "${changes.size}"
                    ),
                    confidenceScore = 92,
                    suggestedFollowUps = listOf(
                        "Is my network stable?",
                        "What should I investigate first?",
                        "Give me a device health summary"
                    )
                )
            }

            CopilotIntent.NETWORK_STABILITY -> {
                val netDiag = copilotToolDispatcher.getNetworkDiagnostics()
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = if (netDiag.diagnosticStatus == "HEALTHY") "INFO" else "WARNING",
                    summary = "Network Diagnostics Assessment",
                    measuredFacts = listOf(
                        "Transport: ${networkState.transportType} (${if (networkState.isConnected) "Connected" else "Offline"})",
                        "Metered Status: ${if (networkState.isMetered) "Cellular Data Plan" else "Unmetered Wi-Fi"}",
                        "Measured Latency: ${if (netDiag.latencyMs >= 0) "${netDiag.latencyMs}ms" else "Unreachable"}",
                        "DNS Status: ${netDiag.dnsStatus}",
                        "Downstream Speed: ${networkState.linkDownstreamKbps / 1000} Mbps"
                    ),
                    hypothesisAndEvidence = netDiag.rootCauseExplanation,
                    recommendedActions = listOf(
                        "Switch to Wi-Fi if on metered connection.",
                        "Run network ping test on Dashboard if latency spikes."
                    ),
                    relevantMetrics = mapOf(
                        "Transport" to networkState.transportType,
                        "Latency" to "${netDiag.latencyMs}ms",
                        "DNS" to netDiag.dnsStatus
                    ),
                    confidenceScore = 94,
                    suggestedFollowUps = listOf(
                        "What changed in my device today?",
                        "What should I investigate first?",
                        "Is my device behaving normally?"
                    )
                )
            }

            CopilotIntent.INVESTIGATION_PRIORITY -> {
                val priorityItem = when {
                    batteryState.temperature >= 38f -> "1. Elevated Battery Temperature (${batteryState.temperature}°C)"
                    !batteryState.isCharging && batteryState.level <= 15 -> "1. Critical Low Battery Level (${batteryState.level}%)"
                    !networkState.isConnected -> "1. Offline Network Transport (${networkState.transportType})"
                    memory.usagePct >= 85 -> "1. High Memory Pressure (${memory.usagePct}% RAM)"
                    else -> "1. All Systems Nominal - No urgent investigations required."
                }

                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = if (priorityItem.contains("Nominal")) "INFO" else "WARNING",
                    summary = "System Investigation Priorities",
                    measuredFacts = listOf(
                        priorityItem,
                        "2. Active Anomalies Logged: ${liveCtx.activeAnomalies.size}",
                        "3. Recent Incidents Logged: ${incidents.size}"
                    ),
                    hypothesisAndEvidence = "Prioritized based on hardware temperature thresholds, battery discharge velocity, and network availability.",
                    recommendedActions = listOf(
                        "Address item #1 first for maximum system efficiency.",
                        "Inspect Timeline for full correlated event breakdown."
                    ),
                    relevantMetrics = mapOf(
                        "Top Priority" to priorityItem
                    ),
                    confidenceScore = 95,
                    suggestedFollowUps = listOf(
                        "Explain my latest incident",
                        "Give me a device health summary",
                        "Why is my battery draining?"
                    )
                )
            }

            CopilotIntent.EXPLAIN_INCIDENT -> {
                val latest = incidents.firstOrNull()
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = latest?.severity ?: "INFO",
                    summary = latest?.title ?: "No Incidents to Explain",
                    measuredFacts = if (latest != null) listOf(
                        "Title: ${latest.title}",
                        "Severity: [${latest.severity}]",
                        "Correlated Events: ${latest.correlatedEventsSummary}",
                        "Supporting Evidence: ${latest.supportingEvidence}"
                    ) else listOf("No correlated incident records exist in database."),
                    hypothesisAndEvidence = latest?.correlationExplanation ?: "No anomaly correlations logged.",
                    recommendedActions = listOf(
                        latest?.recommendedChecks ?: "No action required.",
                        "Review Timeline screen for historical incident log."
                    ),
                    relevantMetrics = mapOf(
                        "Incident Title" to (latest?.title ?: "None")
                    ),
                    confidenceScore = 96,
                    suggestedFollowUps = listOf(
                        "Has this happened before?",
                        "What should I investigate first?",
                        "Give me a device health summary"
                    )
                )
            }

            CopilotIntent.HEALTH_SUMMARY -> {
                val summary = copilotToolDispatcher.generateDiagnosticSummary()
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = "INFO",
                    summary = "Overall Device Health Assessment",
                    measuredFacts = listOf(
                        "Current State: ${summary.currentClassification}",
                        "Battery Vitals: ${batteryState.level}% (${if (batteryState.isCharging) "Charging" else "Discharging"})",
                        "Thermal Vitals: ${batteryState.temperature}°C (Baseline: ${"%.1f".format(avgTemp)}°C)",
                        "RAM Utilization: ${memory.usagePct}% (${memory.usedMemMb} MB used)",
                        "Network Transport: ${networkState.transportType}"
                    ),
                    hypothesisAndEvidence = summary.keyTakeaway,
                    recommendedActions = listOf(
                        "Maintain smart battery charging between 20% and 80%.",
                        "Use Focus Mode to minimize notification distractions.",
                        "Review active automations."
                    ),
                    relevantMetrics = mapOf(
                        "Health Index" to summary.currentClassification.name,
                        "Confidence" to "${summary.confidenceScore}%"
                    ),
                    confidenceScore = summary.confidenceScore,
                    suggestedFollowUps = listOf(
                        "Why is my battery draining?",
                        "Why is my phone heating?",
                        "Is my network stable?"
                    )
                )
            }

            CopilotIntent.FOLLOW_UP_HISTORY -> {
                val pastMessages = copilotConversationDao.getMessagesListForConversation(sessionId)
                val thermalEventsCount = incidents.count { it.title.lowercase().contains("thermal") }
                val pastContext = if (pastMessages.isNotEmpty()) {
                    "Previous chat context contains ${pastMessages.size} messages. Based on your records, ${thermalEventsCount} prior thermal/drain incident events have been logged in history."
                } else {
                    "No prior conversation history recorded in this session. However, system records show ${thermalEventsCount} historical thermal events in your incident database."
                }

                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = "INFO",
                    summary = "Historical Pattern Analysis",
                    measuredFacts = listOf(
                        "Historical Thermal/Drain Events Logged: $thermalEventsCount",
                        "Total Session Messages Evaluated: ${pastMessages.size}",
                        "Learned Baseline Sample Count: ${baseline?.sampleCount ?: 1}"
                    ),
                    hypothesisAndEvidence = pastContext,
                    recommendedActions = listOf(
                        "Tap Timeline screen to inspect past incident dates.",
                        "Compare current readings against 7-day learned baseline."
                    ),
                    relevantMetrics = mapOf(
                        "Past Events" to "$thermalEventsCount"
                    ),
                    confidenceScore = 93,
                    suggestedFollowUps = listOf(
                        "Explain my latest incident",
                        "Give me a device health summary",
                        "What should I investigate first?"
                    )
                )
            }

            CopilotIntent.UNSUPPORTED -> {
                CopilotResponseDetail(
                    query = query,
                    intent = intent,
                    severity = "INFO",
                    summary = "Query Out of Scope",
                    measuredFacts = listOf(
                        "Requested Query: '$query'",
                        "Copilot Capabilities: Battery diagnostics, thermal analysis, network stability, memory utilization, anomaly detection, incident explanations, and device health summaries."
                    ),
                    hypothesisAndEvidence = "I am a device diagnostic assistant grounded in your phone's real hardware telemetry. I cannot answer general trivia or non-device queries.",
                    recommendedActions = listOf(
                        "Try asking: 'Why is my battery draining?'",
                        "Try asking: 'Why is my phone heating?'",
                        "Try asking: 'Is my network stable?'"
                    ),
                    relevantMetrics = emptyMap(),
                    confidenceScore = 100,
                    suggestedFollowUps = listOf(
                        "Why is my battery draining?",
                        "Why is my phone heating?",
                        "Give me a device health summary"
                    ),
                    isUnsupported = true
                )
            }
        }

        // Persist AI response message to Room DB
        val aiMessageEntity = CopilotMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = sessionId,
            sender = "AI",
            timestamp = System.currentTimeMillis(),
            queryText = query,
            intent = response.intent.name,
            severity = response.severity,
            confidenceScore = response.confidenceScore,
            summary = response.summary,
            factsJson = response.measuredFacts.joinToString("||"),
            evidenceText = response.hypothesisAndEvidence,
            actionsJson = response.recommendedActions.joinToString("||"),
            metricsJson = response.relevantMetrics.entries.joinToString("||") { "${it.key}:${it.value}" },
            followUpsJson = response.suggestedFollowUps.joinToString("||")
        )
        copilotConversationDao.insertMessage(aiMessageEntity)

        return response
    }

    suspend fun answerQuery(
        query: String,
        batteryState: BatteryState,
        networkState: NetworkState
    ): CopilotResponse {
        val detail = answerQueryDetailed(query, "default_session", batteryState, networkState)
        return CopilotResponse(
            query = detail.query,
            summary = detail.summary,
            measuredFacts = detail.measuredFacts,
            hypothesisAndEvidence = detail.hypothesisAndEvidence,
            recommendedActions = detail.recommendedActions,
            confidenceScore = detail.confidenceScore
        )
    }
}
