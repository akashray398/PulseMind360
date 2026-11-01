package com.dev.myapplication.domain

enum class DeviceStateClassification {
    OPTIMAL,
    THERMAL_STRESS,
    HIGH_DRAIN,
    NETWORK_DEGRADED,
    RAM_PRESSURE,
    CRITICAL_MULTIVARIATE
}

enum class ContextFreshness {
    FRESH,
    NOMINAL,
    STALE,
    UNAVAILABLE
}

data class MonitoringModuleStatus(
    val moduleName: String,
    val isAvailable: Boolean = true,
    val lastUpdateTimestamp: Long = System.currentTimeMillis(),
    val statusText: String = "Active & Reporting"
)

data class ContextChangeEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val category: String,
    val description: String,
    val severity: String = "INFO" // INFO, WARNING, ALERT
)

data class ContextSummary(
    val currentClassification: DeviceStateClassification = DeviceStateClassification.OPTIMAL,
    val activeAnomaliesCount: Int = 0,
    val freshness: ContextFreshness = ContextFreshness.FRESH,
    val confidenceScore: Int = 95,
    val keyTakeaway: String = "All monitoring modules reporting optimal status.",
    val disclaimer: String = "Correlations reflect statistical co-occurrence, not established physical causation."
)

data class LiveDeviceContext(
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val plugType: String = "DISCHARGING",
    val batteryTemp: Float = 0f,
    val ramUsagePct: Int = 0,
    val usedMemMb: Long = 0,
    val totalMemMb: Long = 0,
    val networkTransport: String = "NONE",
    val linkDownstreamKbps: Int = 0,
    val linkUpstreamKbps: Int = 0,
    val isMetered: Boolean = false,
    val stateClassification: DeviceStateClassification = DeviceStateClassification.OPTIMAL,
    val freshness: ContextFreshness = ContextFreshness.FRESH,
    val confidenceScore: Int = 95,
    val activeAnomalies: List<String> = emptyList(),
    val recentChanges: List<ContextChangeEvent> = emptyList(),
    val monitoringStatuses: List<MonitoringModuleStatus> = emptyList(),
    val isStale: Boolean = false,
    val dataAgeMs: Long = 0
)
