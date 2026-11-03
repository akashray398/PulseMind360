package com.dev.myapplication.domain.scorecard

import com.dev.myapplication.data.local.dao.ReliabilityScorecardDao
import com.dev.myapplication.data.local.entity.ReliabilityAssessmentEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.LiveContextEngine
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.util.MemoryState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReliabilityScorecardEngine @Inject constructor(
    private val scorecardDao: ReliabilityScorecardDao,
    private val liveContextEngine: LiveContextEngine,
    private val predictiveEngine: PredictiveIntelligenceEngine
) {

    suspend fun evaluateReliabilityScorecard(
        batteryState: BatteryState,
        networkState: NetworkState,
        memoryState: MemoryState
    ): ReliabilityScorecard {
        val now = System.currentTimeMillis()
        val overview = predictiveEngine.generatePredictiveOverview(batteryState, networkState, memoryState)

        // 1. Battery Reliability Score
        val bScore = if (overview.batteryForecast.isUnusualDrain) 72 else 98

        // 2. Thermal Reliability Score
        val tScore = if (overview.thermalForecast.isThermalEscalationRisk) 68 else 96

        // 3. Memory Reliability Score
        val mScore = if (overview.memoryTrend.isMemoryPressureRisk) 75 else 95

        // 4. Network Reliability Score
        val nScore = overview.networkTrend.stabilityScorePct

        // 5. System Overall Weighted Score
        val overall = (bScore * 0.30 + tScore * 0.25 + mScore * 0.20 + nScore * 0.25).toInt().coerceIn(40, 100)

        val components = listOf(
            ComponentReliability("BATTERY", bScore, 100, if (bScore < 80) "Unusual Drain Rate" else "Optimal Efficiency"),
            ComponentReliability("THERMAL", tScore, 100, if (tScore < 80) "Thermal Slope Risk" else "Stable Temp"),
            ComponentReliability("MEMORY", mScore, 100, if (mScore < 80) "RAM Allocation Pressure" else "Normal Usage"),
            ComponentReliability("NETWORK", nScore, 98, if (nScore < 80) "Connection Retries" else "Stable Transport"),
            ComponentReliability("SYSTEM", overall, 100, "Weighted Reliability Aggregate")
        )

        // Persist scorecard components to Room DB
        components.forEach { c ->
            scorecardDao.insertAssessment(
                ReliabilityAssessmentEntity(
                    componentName = c.componentName,
                    reliabilityScore = c.score,
                    coveragePct = c.coveragePct,
                    evidenceJson = "{\"status\":\"${c.statusText}\"}",
                    timestamp = now
                )
            )
        }
        // Prune older than 30 days
        scorecardDao.deleteOldAssessments(now - 2_592_000_000L)

        return ReliabilityScorecard(
            overallScore = overall,
            components = components,
            evaluationWindowHours = 24,
            timestamp = now
        )
    }
}
