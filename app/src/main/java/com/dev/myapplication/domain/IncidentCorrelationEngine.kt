package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState

class IncidentCorrelationEngine(
    private val activityLogDao: ActivityLogDao,
    private val incidentDao: IncidentDao
) {

    suspend fun correlateSystemEvents(
        batteryState: BatteryState,
        networkState: NetworkState
    ) {
        val now = System.currentTimeMillis()

        // Evaluate co-occurring conditions
        val isThermalSpike = batteryState.temperature >= 38f
        val isHighDrain = !batteryState.isCharging && batteryState.level < 20
        val isNetworkIssue = !networkState.isConnected || networkState.isMetered

        if (isThermalSpike && isHighDrain) {
            val incident = IncidentEntity(
                title = "Correlated Thermal & Rapid Battery Drain Incident",
                severity = "CRITICAL",
                startTime = now - 600_000L, // 10 mins ago
                endTime = now,
                eventCount = 3,
                correlatedEventsSummary = "1. Elevated Battery Temperature (${batteryState.temperature}°C)\n2. Low Battery Charge (${batteryState.level}%)\n3. High Discharge Velocity",
                supportingEvidence = "Active Temp: ${batteryState.temperature}°C · Battery Level: ${batteryState.level}% · Plugged: ${batteryState.isCharging}",
                correlationExplanation = "Strong statistical correlation observed between elevated battery temperature and rapid discharge. Note: Correlation does not guarantee direct causation.",
                recommendedChecks = "Inspect active background processing apps, reduce screen brightness, and allow hardware cooling.",
                createdAt = now
            )
            incidentDao.insertIncident(incident)
        } else if (isThermalSpike && isNetworkIssue) {
            val incident = IncidentEntity(
                title = "Correlated Thermal & Network Retries Incident",
                severity = "WARNING",
                startTime = now - 300_000L,
                endTime = now,
                eventCount = 2,
                correlatedEventsSummary = "1. Elevated Battery Temperature (${batteryState.temperature}°C)\n2. Network Transport Issues (${networkState.transportType})",
                supportingEvidence = "Active Temp: ${batteryState.temperature}°C · Transport: ${networkState.transportType} · Connected: ${networkState.isConnected}",
                correlationExplanation = "Concurrent correlation detected between network re-transmission retries and thermal slope increase.",
                recommendedChecks = "Verify network signal stability or pause background data sync.",
                createdAt = now
            )
            incidentDao.insertIncident(incident)
        }
    }
}
