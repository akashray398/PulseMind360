package com.dev.myapplication.domain.predictive

data class MetricPrediction(
    val predictedMetric: String,
    val estimatedValueOrRange: String,
    val predictionHorizon: String, // "+15m", "+30m", "+1h"
    val confidenceScore: Int, // 0 - 100%
    val historicalEvidence: String,
    val algorithmUsed: String,
    val limitations: String = "Assumes brightness, active apps, and network conditions remain constant.",
    val generationTimestamp: Long = System.currentTimeMillis()
)

data class BaselineStatistics(
    val mean: Float = 0f,
    val median: Float = 0f,
    val variance: Float = 0f,
    val stdDev: Float = 0f,
    val sampleCount: Int = 0,
    val insufficientData: Boolean = true
)

data class BatteryForecast(
    val dischargeRatePctPerHour: Float = 7.5f,
    val timeTo20PctMins: Int = 180,
    val timeTo10PctMins: Int = 240,
    val timeToEmptyMins: Int = 300,
    val isUnusualDrain: Boolean = false,
    val prediction: MetricPrediction
)

data class ThermalForecast(
    val rateOfChangePerMin: Float = 0.05f,
    val predictedTemp15m: Float = 32.0f,
    val predictedTemp30m: Float = 33.0f,
    val isThermalEscalationRisk: Boolean = false,
    val prediction: MetricPrediction
)

data class MemoryTrend(
    val ramUsageSlopePctPerMin: Float = 0.1f,
    val predictedRam30m: Int = 68,
    val isMemoryPressureRisk: Boolean = false,
    val prediction: MetricPrediction
)

data class NetworkReliabilityTrend(
    val dropCount: Int = 0,
    val stabilityScorePct: Int = 98,
    val prediction: MetricPrediction
)

data class PredictiveOverview(
    val batteryForecast: BatteryForecast,
    val thermalForecast: ThermalForecast,
    val memoryTrend: MemoryTrend,
    val networkTrend: NetworkReliabilityTrend,
    val overallConfidence: Int = 92,
    val generationTimestamp: Long = System.currentTimeMillis()
)
