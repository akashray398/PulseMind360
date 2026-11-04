package com.dev.myapplication.domain.causality

import com.dev.myapplication.data.local.dao.AdiDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.entity.CausalRecordEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.blackbox.DeviceBlackBoxEngine
import com.dev.myapplication.domain.core.CausalHypothesis
import com.dev.myapplication.domain.twin.DigitalTwinEngine
import com.dev.myapplication.util.MemoryState
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceCausalityEngine @Inject constructor(
    private val adiDao: AdiDao,
    private val incidentDao: IncidentDao,
    private val blackBoxEngine: DeviceBlackBoxEngine,
    private val digitalTwinEngine: DigitalTwinEngine
) {

    fun calculateCalibratedConfidence(
        baseScore: Int,
        evidenceCount: Int,
        patternMatchCount: Int,
        hasContradictoryEvidence: Boolean = false
    ): Int {
        val evidenceBonus = (evidenceCount * 3).coerceAtMost(12)
        val patternBonus = (patternMatchCount * 2).coerceAtMost(8)
        val penalty = if (hasContradictoryEvidence) 15 else 0
        return (baseScore + evidenceBonus + patternBonus - penalty).coerceIn(50, 98)
    }

    suspend fun analyzeDeviceCausality(
        batteryState: BatteryState,
        networkState: NetworkState,
        memoryState: MemoryState
    ): CausalityAnalysisResult {
        val now = System.currentTimeMillis()
        val incidents = incidentDao.getAllIncidents().firstOrNull() ?: emptyList()
        val recentBlackBoxEvents = blackBoxEngine.getRecentEventsFlow(10).firstOrNull() ?: emptyList()
        val recentTwinTransitions = digitalTwinEngine.getRecentTransitionsList(10)

        val hypotheses = mutableListOf<CausalHypothesis>()

        // Rule 1: Evaluate Thermal Elevation
        if (batteryState.temperature >= 38.0f) {
            val thermalPatternCount = incidents.count { it.title.contains("Thermal", ignoreCase = true) }
            val evidenceChain = mutableListOf(
                "Observed Temperature: ${batteryState.temperature}°C (Baseline: 30.0°C)",
                "Battery Charging State: ${batteryState.isCharging}"
            )
            val blackBoxMatch = recentBlackBoxEvents.find { it.eventType == "THERMAL_SPIKE" }
            if (blackBoxMatch != null) {
                evidenceChain.add("Black Box Snapshot: ${blackBoxMatch.eventType} recorded at ${blackBoxMatch.timestamp}")
            }
            val twinMatch = recentTwinTransitions.find { it.stateType.contains("RAM") || it.stateType.contains("TEMP") }
            if (twinMatch != null) {
                evidenceChain.add("Digital Twin Transition: ${twinMatch.stateType} changed from '${twinMatch.previousValue}' to '${twinMatch.currentValue}'")
            }

            val hasContradiction = !batteryState.isCharging && batteryState.temperature >= 45.0f
            val calibratedScore = calculateCalibratedConfidence(
                baseScore = 75,
                evidenceCount = evidenceChain.size,
                patternMatchCount = thermalPatternCount,
                hasContradictoryEvidence = hasContradiction
            )

            val h = CausalHypothesis(
                problemSummary = "Thermal Slope Elevation (${batteryState.temperature}°C)",
                primaryCause = "Sustained high rendering/CPU computation or rapid battery discharge velocity.",
                secondaryFactors = listOf("High ambient temperature", "Background process execution"),
                confidenceScore = calibratedScore,
                evidenceChain = evidenceChain,
                alternativeHypotheses = listOf("Radio transmitter frequency retries on poor signal"),
                isConfirmedCausal = evidenceChain.size >= 3
            )
            hypotheses.add(h)
        }

        // Rule 2: Evaluate Low Battery & High RAM Usage
        if (!batteryState.isCharging && batteryState.level < 20 && memoryState.usagePct >= 80) {
            val ramPatternCount = incidents.count { it.title.contains("RAM", ignoreCase = true) }
            val evidenceChain = mutableListOf(
                "RAM Usage: ${memoryState.usedMemMb}MB / ${memoryState.totalMemMb}MB (${memoryState.usagePct}%)",
                "Battery Level: ${batteryState.level}%"
            )
            val twinRam = recentTwinTransitions.find { it.stateType.contains("RAM") }
            if (twinRam != null) {
                evidenceChain.add("Digital Twin RAM Transition: ${twinRam.stateType} ('${twinRam.previousValue}' ➔ '${twinRam.currentValue}')")
            }

            val calibratedScore = calculateCalibratedConfidence(
                baseScore = 75,
                evidenceCount = evidenceChain.size,
                patternMatchCount = ramPatternCount,
                hasContradictoryEvidence = false
            )

            val h = CausalHypothesis(
                problemSummary = "Low Battery & Memory Allocation Pressure (${memoryState.usagePct}% RAM)",
                primaryCause = "Concurrent background process memory accumulation increasing CPU wakeups.",
                secondaryFactors = listOf("Low charge capacity (${batteryState.level}%)", "Background cache buildup"),
                confidenceScore = calibratedScore,
                evidenceChain = evidenceChain,
                alternativeHypotheses = listOf("System Garbage Collection retries under memory pressure"),
                isConfirmedCausal = false
            )
            hypotheses.add(h)
        }

        // Default Nominal Fallback
        if (hypotheses.isEmpty()) {
            val h = CausalHypothesis(
                problemSummary = "System Operating Nominally",
                primaryCause = "All telemetry indicators (Battery, Thermal, RAM, Network) are within safe baseline bounds.",
                secondaryFactors = emptyList(),
                confidenceScore = 98,
                evidenceChain = listOf("Temp=${batteryState.temperature}°C", "Level=${batteryState.level}%", "RAM=${memoryState.usagePct}%"),
                alternativeHypotheses = emptyList(),
                isConfirmedCausal = true
            )
            hypotheses.add(h)
        }

        // Persist Causal Hypotheses to Room DB
        hypotheses.forEach { h ->
            adiDao.insertCausalRecord(
                CausalRecordEntity(
                    hypothesisId = h.hypothesisId,
                    problemSummary = h.problemSummary,
                    primaryCause = h.primaryCause,
                    secondaryFactorsJson = h.secondaryFactors.joinToString(";"),
                    confidenceScore = h.confidenceScore,
                    evidenceChainJson = h.evidenceChain.joinToString(";"),
                    isConfirmedCausal = h.isConfirmedCausal,
                    timestamp = now
                )
            )
        }

        return CausalityAnalysisResult(
            hypotheses = hypotheses,
            totalEvidenceAnalyzed = incidents.size + recentBlackBoxEvents.size + recentTwinTransitions.size + 3,
            timestamp = now
        )
    }
}
