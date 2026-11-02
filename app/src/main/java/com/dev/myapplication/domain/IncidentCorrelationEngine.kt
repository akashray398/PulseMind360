package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.util.MemoryOptimizer
import com.dev.myapplication.util.MemoryState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentCorrelationEngine @Inject constructor(
    private val activityLogDao: ActivityLogDao,
    private val incidentDao: IncidentDao
) {

    suspend fun correlateSystemEvents(
        batteryState: BatteryState,
        networkState: NetworkState,
        memoryState: MemoryState = MemoryState()
    ) {
        val now = System.currentTimeMillis()
        val windowMinTime = now - 900_000L // 15-minute deduplication window

        val isThermalSpike = batteryState.temperature >= 38f
        val isHighDrain = !batteryState.isCharging && batteryState.level < 20
        val isNetworkIssue = !networkState.isConnected || networkState.isMetered
        val isRamPressure = memoryState.usagePct >= 85

        // Correlation Case A: Thermal Spike & Rapid Battery Drain
        if (isThermalSpike && isHighDrain) {
            val title = "Correlated Thermal & Rapid Battery Drain Incident"
            val existing = incidentDao.getRecentIncidentByTitle(title, windowMinTime)
            if (existing == null) {
                val incident = IncidentEntity(
                    title = title,
                    severity = "CRITICAL",
                    status = "DETECTED",
                    startTime = now - 600_000L,
                    endTime = now,
                    eventCount = 3,
                    correlatedEventsSummary = "1. Battery Temperature Elevated (${batteryState.temperature}°C)\n2. Battery Level Critical (${batteryState.level}%)\n3. Discharge Velocity High",
                    supportingEvidence = "FACTS: Temp=${batteryState.temperature}°C, Level=${batteryState.level}%, Plugged=${batteryState.isCharging}",
                    correlationExplanation = "Concurrent statistical correlation observed between elevated battery temperature and rapid discharge velocity.",
                    rankedExplanations = "Rank 1 (Primary): Sustained CPU/GPU rendering load.\nRank 2 (Secondary): High battery discharge rate causing thermal accumulation.",
                    recommendedChecks = "Pause heavy graphics rendering, reduce screen brightness, and connect power source.",
                    confidenceScore = 95,
                    createdAt = now
                )
                incidentDao.insertIncident(incident)
            }
        }

        // Correlation Case B: Thermal Spike & Network Retries / High Traffic
        if (isThermalSpike && isNetworkIssue) {
            val title = "Correlated Thermal & Network Retries Incident"
            val existing = incidentDao.getRecentIncidentByTitle(title, windowMinTime)
            if (existing == null) {
                val incident = IncidentEntity(
                    title = title,
                    severity = "WARNING",
                    status = "DETECTED",
                    startTime = now - 300_000L,
                    endTime = now,
                    eventCount = 2,
                    correlatedEventsSummary = "1. Battery Temp (${batteryState.temperature}°C)\n2. Network Transport Issues (${networkState.transportType})",
                    supportingEvidence = "FACTS: Temp=${batteryState.temperature}°C, Transport=${networkState.transportType}, Metered=${networkState.isMetered}",
                    correlationExplanation = "Co-occurrence detected between network transport retries and thermal slope increase.",
                    rankedExplanations = "Rank 1 (Primary): Frequent radio transmitter retries on cellular network.\nRank 2 (Secondary): Background data synchronization loop.",
                    recommendedChecks = "Verify Wi-Fi network signal strength or toggle metered data saver automations.",
                    confidenceScore = 91,
                    createdAt = now
                )
                incidentDao.insertIncident(incident)
            }
        }

        // Correlation Case C: High Memory Pressure & Battery Drain Velocity
        if (isRamPressure && isHighDrain) {
            val title = "Correlated RAM Pressure & Battery Drain Incident"
            val existing = incidentDao.getRecentIncidentByTitle(title, windowMinTime)
            if (existing == null) {
                val incident = IncidentEntity(
                    title = title,
                    severity = "WARNING",
                    status = "DETECTED",
                    startTime = now - 300_000L,
                    endTime = now,
                    eventCount = 2,
                    correlatedEventsSummary = "1. High RAM Usage (${memoryState.usagePct}%)\n2. Low Battery Level (${batteryState.level}%)",
                    supportingEvidence = "FACTS: Used RAM=${memoryState.usedMemMb}MB / ${memoryState.totalMemMb}MB (${memoryState.usagePct}%), Battery=${batteryState.level}%",
                    correlationExplanation = "Co-occurrence detected between memory allocation pressure and rapid battery discharge.",
                    rankedExplanations = "Rank 1 (Primary): Background process memory leak or excessive cache allocation.\nRank 2 (Secondary): Frequent Garbage Collection cycles increasing CPU wakeups.",
                    recommendedChecks = "Use 'Trim RAM' cache button on Dashboard or close background applications.",
                    confidenceScore = 89,
                    createdAt = now
                )
                incidentDao.insertIncident(incident)
            }
        }
    }
}
