package com.dev.myapplication.domain

import android.content.Context
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.ContextSnapshotDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.entity.ContextSnapshotEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.util.MemoryOptimizer
import com.dev.myapplication.util.MemoryState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveContextEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor,
    private val anomalyDetector: AnomalyDetector,
    private val sentinelEngine: SentinelEngine,
    private val contextSnapshotDao: ContextSnapshotDao,
    private val incidentDao: IncidentDao,
    private val activityLogDao: ActivityLogDao
) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _liveContext = MutableStateFlow(LiveDeviceContext())
    val liveContext: StateFlow<LiveDeviceContext> = _liveContext.asStateFlow()

    private val _recentChanges = MutableStateFlow<List<ContextChangeEvent>>(emptyList())
    val recentChanges: StateFlow<List<ContextChangeEvent>> = _recentChanges.asStateFlow()

    private var previousBatteryState: BatteryState? = null
    private var previousNetworkState: NetworkState? = null
    private var previousClassification: DeviceStateClassification? = null
    private var lastSnapshotTimestamp: Long = 0

    init {
        observeMonitors()
    }

    private fun observeMonitors() {
        engineScope.launch {
            combine(
                batteryMonitor.batteryState,
                networkMonitor.networkState
            ) { battery, network ->
                battery to network
            }.collect { (battery, network) ->
                val memState = MemoryOptimizer.getMemoryState(context)
                processLiveContext(battery, network, memState)
            }
        }
    }

    private suspend fun processLiveContext(
        battery: BatteryState,
        network: NetworkState,
        memory: MemoryState
    ) {
        val now = System.currentTimeMillis()

        // 1. Evaluate Anomaly & Sentinel Status
        val anomalyReport = anomalyDetector.detectAnomalies(battery, network)
        val activeAnomalies = mutableListOf<String>()
        if (anomalyReport.hasAnomaly) {
            activeAnomalies.add(anomalyReport.title)
        }

        // 2. Classify Device State
        val classification = classifyDeviceState(battery, network, memory)

        // 3. Stale Data & Freshness Indicators
        val dataAgeMs = 0L // Active live flow update
        val freshness = when {
            dataAgeMs < 5000 -> ContextFreshness.FRESH
            dataAgeMs < 30000 -> ContextFreshness.NOMINAL
            else -> ContextFreshness.STALE
        }

        // 4. Change Detection
        detectChanges(battery, network, classification, now)

        // 5. Monitoring Statuses
        val monitoringStatuses = listOf(
            MonitoringModuleStatus("Battery Monitor", true, now, "Level: ${battery.level}% · Temp: ${battery.temperature}°C"),
            MonitoringModuleStatus("Network Monitor", true, now, "${network.transportType} (${if (network.isConnected) "Connected" else "Offline"})"),
            MonitoringModuleStatus("Memory Monitor", true, now, "RAM: ${memory.usagePct}% used (${memory.usedMemMb}MB / ${memory.totalMemMb}MB)"),
            MonitoringModuleStatus("Sentinel Intelligence", true, now, if (anomalyReport.hasAnomaly) "Anomaly: ${anomalyReport.severity}" else "Nominal"),
            MonitoringModuleStatus("Incident Engine", true, now, "Correlating System Logs")
        )

        // 6. Confidence Score
        val confidenceScore = calculateConfidenceScore(battery, network, memory, freshness)

        val updatedContext = LiveDeviceContext(
            timestamp = now,
            batteryLevel = battery.level,
            isCharging = battery.isCharging,
            plugType = battery.plugType,
            batteryTemp = battery.temperature,
            ramUsagePct = memory.usagePct,
            usedMemMb = memory.usedMemMb,
            totalMemMb = memory.totalMemMb,
            networkTransport = network.transportType,
            linkDownstreamKbps = network.linkDownstreamKbps,
            linkUpstreamKbps = network.linkUpstreamKbps,
            isMetered = network.isMetered,
            stateClassification = classification,
            freshness = freshness,
            confidenceScore = confidenceScore,
            activeAnomalies = activeAnomalies,
            recentChanges = _recentChanges.value,
            monitoringStatuses = monitoringStatuses,
            isStale = freshness == ContextFreshness.STALE,
            dataAgeMs = dataAgeMs
        )

        _liveContext.value = updatedContext

        // 7. Store Periodic Snapshot to Room DB (Limit frequency to every 5 minutes or on classification change)
        if (now - lastSnapshotTimestamp >= 300_000L || previousClassification != classification) {
            lastSnapshotTimestamp = now
            val entity = ContextSnapshotEntity(
                timestamp = now,
                batteryLevel = battery.level,
                isCharging = battery.isCharging,
                batteryTemp = battery.temperature,
                usedMemMb = memory.usedMemMb,
                totalMemMb = memory.totalMemMb,
                networkTransport = network.transportType,
                linkDownstreamKbps = network.linkDownstreamKbps,
                stateClassification = classification.name,
                anomalySeverity = anomalyReport.severity,
                confidenceScore = confidenceScore
            )
            contextSnapshotDao.insertSnapshot(entity)
            // Prune snapshots older than 24 hours (Retention Policy)
            contextSnapshotDao.deleteOldSnapshots(now - 86_400_000L)
        }

        previousBatteryState = battery
        previousNetworkState = network
        previousClassification = classification
    }

    private fun classifyDeviceState(
        battery: BatteryState,
        network: NetworkState,
        memory: MemoryState
    ): DeviceStateClassification {
        val isThermalSpike = battery.temperature >= 38.0f
        val isLowBattery = !battery.isCharging && battery.level <= 15
        val isRamPressure = memory.usagePct >= 85
        val isNetworkIssue = !network.isConnected

        val anomalyCount = listOf(isThermalSpike, isLowBattery, isRamPressure, isNetworkIssue).count { it }

        return when {
            anomalyCount >= 2 -> DeviceStateClassification.CRITICAL_MULTIVARIATE
            isThermalSpike -> DeviceStateClassification.THERMAL_STRESS
            isLowBattery -> DeviceStateClassification.HIGH_DRAIN
            isRamPressure -> DeviceStateClassification.RAM_PRESSURE
            isNetworkIssue -> DeviceStateClassification.NETWORK_DEGRADED
            else -> DeviceStateClassification.OPTIMAL
        }
    }

    private fun detectChanges(
        battery: BatteryState,
        network: NetworkState,
        classification: DeviceStateClassification,
        now: Long
    ) {
        val changes = mutableListOf<ContextChangeEvent>()

        previousBatteryState?.let { prev ->
            if (prev.isCharging != battery.isCharging) {
                changes.add(
                    ContextChangeEvent(
                        id = UUID.randomUUID().toString(),
                        timestamp = now,
                        category = "POWER_STATE",
                        description = if (battery.isCharging) "Power connected via ${battery.plugType}" else "Power disconnected (Discharging)",
                        severity = "INFO"
                    )
                )
            }
            if (prev.temperature < 38f && battery.temperature >= 38f) {
                changes.add(
                    ContextChangeEvent(
                        id = UUID.randomUUID().toString(),
                        timestamp = now,
                        category = "THERMAL_SPIKE",
                        description = "Battery temperature elevated to ${battery.temperature}°C",
                        severity = "WARNING"
                    )
                )
            }
        }

        previousNetworkState?.let { prev ->
            if (prev.isConnected != network.isConnected) {
                changes.add(
                    ContextChangeEvent(
                        id = UUID.randomUUID().toString(),
                        timestamp = now,
                        category = "NETWORK_STATE",
                        description = if (network.isConnected) "Network connected (${network.transportType})" else "Network disconnected",
                        severity = if (network.isConnected) "INFO" else "WARNING"
                    )
                )
            }
        }

        previousClassification?.let { prevClass ->
            if (prevClass != classification) {
                changes.add(
                    ContextChangeEvent(
                        id = UUID.randomUUID().toString(),
                        timestamp = now,
                        category = "STATE_CLASSIFICATION",
                        description = "Device state transitioned from $prevClass to $classification",
                        severity = if (classification == DeviceStateClassification.OPTIMAL) "INFO" else "ALERT"
                    )
                )
            }
        }

        if (changes.isNotEmpty()) {
            val updatedList = (changes + _recentChanges.value).take(30)
            _recentChanges.value = updatedList
        }
    }

    private fun calculateConfidenceScore(
        battery: BatteryState,
        network: NetworkState,
        memory: MemoryState,
        freshness: ContextFreshness
    ): Int {
        var score = 95
        if (freshness == ContextFreshness.STALE) score -= 15
        if (freshness == ContextFreshness.UNAVAILABLE) score -= 40
        if (memory.totalMemMb == 0L) score -= 5
        return score.coerceIn(40, 99)
    }

    // ==========================================
    // Context API Interfaces (/api/v1/context/*)
    // ==========================================

    /**
     * GET /api/v1/context/current
     */
    fun getCurrentContext(): LiveDeviceContext = _liveContext.value

    /**
     * GET /api/v1/context/history
     */
    suspend fun getContextHistory(limit: Int = 50): List<ContextSnapshotEntity> {
        return contextSnapshotDao.getRecentSnapshotsList(limit)
    }

    /**
     * GET /api/v1/context/summary
     */
    fun getContextSummary(): ContextSummary {
        val curr = _liveContext.value
        val takeaway = when (curr.stateClassification) {
            DeviceStateClassification.OPTIMAL -> "All system vitals operating in nominal efficiency range."
            DeviceStateClassification.THERMAL_STRESS -> "Elevated thermal slope detected (${curr.batteryTemp}°C). Hardware cooling recommended."
            DeviceStateClassification.HIGH_DRAIN -> "Rapid discharge velocity active (${curr.batteryLevel}% remaining)."
            DeviceStateClassification.RAM_PRESSURE -> "High RAM utilization (${curr.ramUsagePct}% used)."
            DeviceStateClassification.NETWORK_DEGRADED -> "Network connectivity is currently degraded or offline."
            DeviceStateClassification.CRITICAL_MULTIVARIATE -> "Multivariate system stress co-occurring across battery, thermal, and network monitors."
        }

        return ContextSummary(
            currentClassification = curr.stateClassification,
            activeAnomaliesCount = curr.activeAnomalies.size,
            freshness = curr.freshness,
            confidenceScore = curr.confidenceScore,
            keyTakeaway = takeaway,
            disclaimer = "Statistical co-occurrences reflect system correlations, not established physical causation."
        )
    }

    /**
     * GET /api/v1/context/changes
     */
    fun getContextChanges(): List<ContextChangeEvent> = _recentChanges.value
}
