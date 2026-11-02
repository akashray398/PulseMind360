package com.dev.myapplication.domain.copilot

enum class CopilotIntent {
    BATTERY_DRAIN_WHY,
    THERMAL_HEATING_WHY,
    DEVICE_CURRENT_STATE,
    DEVICE_NORMAL_CHECK,
    LATEST_ANOMALIES,
    TODAY_CHANGES,
    NETWORK_STABILITY,
    INVESTIGATION_PRIORITY,
    HEALTH_SUMMARY,
    EXPLAIN_INCIDENT,
    FOLLOW_UP_HISTORY,
    UNSUPPORTED
}

enum class UserFeedback {
    NONE,
    THUMBS_UP,
    THUMBS_DOWN
}

data class CopilotMetricValue(
    val label: String,
    val value: String,
    val unit: String = "",
    val isBaselineDeviated: Boolean = false
)

data class CopilotResponseDetail(
    val query: String,
    val intent: CopilotIntent,
    val severity: String = "INFO", // INFO, WARNING, ALERT
    val summary: String,
    val measuredFacts: List<String>,
    val hypothesisAndEvidence: String,
    val recommendedActions: List<String>,
    val relevantMetrics: Map<String, String> = emptyMap(),
    val confidenceScore: Int = 92,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedFollowUps: List<String> = emptyList(),
    val isUnsupported: Boolean = false
)
