package com.dev.myapplication.domain.warning

import com.dev.myapplication.data.local.dao.AdiDao
import com.dev.myapplication.data.local.dao.SentinelBaselineDao
import com.dev.myapplication.data.local.entity.EarlyWarningEntity
import com.dev.myapplication.data.local.entity.SentinelBaselineEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.core.EarlyWarningRecord
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.util.MemoryState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarlyWarningSystem @Inject constructor(
    private val adiDao: AdiDao,
    private val sentinelBaselineDao: SentinelBaselineDao,
    private val predictiveEngine: PredictiveIntelligenceEngine
) {

    suspend fun evaluateEarlyWarnings(
        batteryState: BatteryState,
        networkState: NetworkState,
        memoryState: MemoryState
    ): EarlyWarningAnalysisResult {
        val now = System.currentTimeMillis()
        val cooldownWindowMs = 900_000L // 15-minute deduplication cooldown
        val baseline = sentinelBaselineDao.getBaseline() ?: SentinelBaselineEntity()
        val overview = predictiveEngine.generatePredictiveOverview(batteryState, networkState, memoryState)

        val recentDbWarnings = adiDao.getRecentEarlyWarningsList(20)
        val warnings = mutableListOf<EarlyWarningRecord>()

        fun isMetricInCooldown(type: String): Boolean {
            val last = recentDbWarnings.find { it.metricType == type }
            return last != null && (now - last.timestamp) < cooldownWindowMs
        }

        // Baseline Adaptation Guard: On fresh installs (sampleCount < 3), use conservative threshold (+5.0°C)
        val thermalThresholdOffset = if (baseline.sampleCount >= 3) 3.0f else 5.0f

        // 1. Check Thermal Degradation Warning
        if (batteryState.temperature > baseline.avgTemperature + thermalThresholdOffset) {
            if (!isMetricInCooldown("THERMAL_DEGRADATION")) {
                warnings.add(
                    EarlyWarningRecord(
                        metricType = "THERMAL_DEGRADATION",
                        baselineValue = "Avg ${"%.1f".format(baseline.avgTemperature)}°C",
                        currentTrendValue = "${batteryState.temperature}°C",
                        degradationRiskLevel = if (batteryState.temperature >= 38.0f) "HIGH" else "MEDIUM",
                        evidenceWindowHours = 24,
                        recommendedPrevention = "Pause heavy rendering or background sync to prevent thermal escalation.",
                        confidenceScore = 91,
                        lifecycleStatus = "ACTIVE"
                    )
                )
            }
        } else if (batteryState.temperature <= baseline.avgTemperature + 1.0f) {
            val lastThermal = recentDbWarnings.find { it.metricType == "THERMAL_DEGRADATION" }
            if (lastThermal != null && (now - lastThermal.timestamp) < 3_600_000L && lastThermal.degradationRiskLevel != "RECOVERED") {
                warnings.add(
                    EarlyWarningRecord(
                        metricType = "THERMAL_DEGRADATION",
                        baselineValue = "Avg ${"%.1f".format(baseline.avgTemperature)}°C",
                        currentTrendValue = "${batteryState.temperature}°C (Recovered)",
                        degradationRiskLevel = "LOW",
                        evidenceWindowHours = 24,
                        recommendedPrevention = "Thermal levels returned to normal baseline.",
                        confidenceScore = 95,
                        lifecycleStatus = "RECOVERED"
                    )
                )
            }
        }

        // 2. Check Rapid Battery Discharge Warning
        if (overview.batteryForecast.isUnusualDrain) {
            if (!isMetricInCooldown("BATTERY_DISCHARGE_VELOCITY")) {
                warnings.add(
                    EarlyWarningRecord(
                        metricType = "BATTERY_DISCHARGE_VELOCITY",
                        baselineValue = "Avg ${"%.1f".format(baseline.avgDrainRatePerHour)}%/hr",
                        currentTrendValue = "Accelerated Discharge Detected",
                        degradationRiskLevel = "MEDIUM",
                        evidenceWindowHours = 24,
                        recommendedPrevention = "Reduce screen brightness or close resource-heavy background processes.",
                        confidenceScore = 89,
                        lifecycleStatus = "ACTIVE"
                    )
                )
            }
        }

        // 3. Check Memory Allocation Pressure
        if (memoryState.usagePct >= 80) {
            if (!isMetricInCooldown("RAM_PRESSURE")) {
                warnings.add(
                    EarlyWarningRecord(
                        metricType = "RAM_PRESSURE",
                        baselineValue = "Avg 60% RAM",
                        currentTrendValue = "${memoryState.usagePct}% RAM Used",
                        degradationRiskLevel = "MEDIUM",
                        evidenceWindowHours = 24,
                        recommendedPrevention = "Use 'Trim RAM' cache cleanup on Dashboard.",
                        confidenceScore = 87,
                        lifecycleStatus = "ACTIVE"
                    )
                )
            }
        }

        // 4. Check Network Transport Instability Warning
        if (!networkState.isConnected || (networkState.isMetered && networkState.linkDownstreamKbps in 1..499)) {
            if (!isMetricInCooldown("NETWORK_INSTABILITY")) {
                warnings.add(
                    EarlyWarningRecord(
                        metricType = "NETWORK_INSTABILITY",
                        baselineValue = "Stable Wi-Fi/Cellular Transport",
                        currentTrendValue = if (!networkState.isConnected) "Disconnected" else "Metered Throttling (${networkState.linkDownstreamKbps} Kbps)",
                        degradationRiskLevel = "MEDIUM",
                        evidenceWindowHours = 24,
                        recommendedPrevention = "Verify Wi-Fi connection or enable Data Saver automations.",
                        confidenceScore = 86,
                        lifecycleStatus = "ACTIVE"
                    )
                )
            }
        }

        // Persist Early Warnings to Room DB
        warnings.forEach { w ->
            adiDao.insertEarlyWarning(
                EarlyWarningEntity(
                    warningId = w.warningId,
                    metricType = w.metricType,
                    baselineValue = w.baselineValue,
                    currentTrendValue = w.currentTrendValue,
                    degradationRiskLevel = w.degradationRiskLevel,
                    recommendedPrevention = w.recommendedPrevention,
                    confidenceScore = w.confidenceScore,
                    timestamp = now
                )
            )
        }
        // Retention cleanup: Prune warnings older than 14 days
        adiDao.deleteOldWarnings(now - 1_209_600_000L)

        val baselineStatus = when {
            baseline.sampleCount < 3 -> "Initializing personal baseline (${baseline.sampleCount}/3 samples collected)."
            warnings.isEmpty() -> "All metrics operating cleanly within personal baseline bounds."
            else -> "${warnings.size} early warning indicator(s) active relative to baseline."
        }

        return EarlyWarningAnalysisResult(
            warnings = warnings,
            baselineStatusText = baselineStatus,
            timestamp = now
        )
    }
}
