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
        val baseline = sentinelBaselineDao.getBaseline() ?: SentinelBaselineEntity()
        val overview = predictiveEngine.generatePredictiveOverview(batteryState, networkState, memoryState)
        val warnings = mutableListOf<EarlyWarningRecord>()

        // 1. Check Thermal Degradation Warning
        if (batteryState.temperature > baseline.avgTemperature + 3.0f) {
            warnings.add(
                EarlyWarningRecord(
                    metricType = "THERMAL_DEGRADATION",
                    baselineValue = "Avg ${"%.1f".format(baseline.avgTemperature)}°C",
                    currentTrendValue = "${batteryState.temperature}°C",
                    degradationRiskLevel = if (batteryState.temperature >= 38.0f) "HIGH" else "MEDIUM",
                    evidenceWindowHours = 24,
                    recommendedPrevention = "Pause heavy rendering or background sync to prevent thermal escalation.",
                    confidenceScore = 91
                )
            )
        }

        // 2. Check Rapid Battery Discharge Warning
        if (overview.batteryForecast.isUnusualDrain) {
            warnings.add(
                EarlyWarningRecord(
                    metricType = "BATTERY_DISCHARGE_VELOCITY",
                    baselineValue = "Avg ${"%.1f".format(baseline.avgDrainRatePerHour)}%/hr",
                    currentTrendValue = "Accelerated Discharge Detected",
                    degradationRiskLevel = "MEDIUM",
                    evidenceWindowHours = 24,
                    recommendedPrevention = "Reduce screen brightness or close resource-heavy background processes.",
                    confidenceScore = 89
                )
            )
        }

        // 3. Check Memory Allocation Pressure
        if (memoryState.usagePct >= 80) {
            warnings.add(
                EarlyWarningRecord(
                    metricType = "RAM_PRESSURE",
                    baselineValue = "Avg 60% RAM",
                    currentTrendValue = "${memoryState.usagePct}% RAM Used",
                    degradationRiskLevel = "MEDIUM",
                    evidenceWindowHours = 24,
                    recommendedPrevention = "Use 'Trim RAM' cache cleanup on Dashboard.",
                    confidenceScore = 87
                )
            )
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

        val baselineStatus = if (warnings.isEmpty()) {
            "All metrics operating cleanly within personal baseline bounds."
        } else {
            "${warnings.size} early warning indicator(s) detected relative to baseline."
        }

        return EarlyWarningAnalysisResult(
            warnings = warnings,
            baselineStatusText = baselineStatus,
            timestamp = now
        )
    }
}
