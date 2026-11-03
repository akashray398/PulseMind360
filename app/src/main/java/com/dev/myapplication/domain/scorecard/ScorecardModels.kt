package com.dev.myapplication.domain.scorecard

data class ComponentReliability(
    val componentName: String, // BATTERY, THERMAL, MEMORY, NETWORK, SYSTEM
    val score: Int, // 0 - 100
    val coveragePct: Int = 100,
    val statusText: String = "Nominal"
)

data class ReliabilityScorecard(
    val overallScore: Int,
    val components: List<ComponentReliability>,
    val evaluationWindowHours: Int = 24,
    val timestamp: Long = System.currentTimeMillis()
)
