package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.util.MemoryState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeCorrelationIncidentDao : IncidentDao {
    val incidents = mutableListOf<IncidentEntity>()

    override fun getAllIncidents(): Flow<List<IncidentEntity>> = MutableStateFlow(incidents)

    override suspend fun getIncidentById(id: Long): IncidentEntity? {
        return incidents.find { it.id == id }
    }

    override suspend fun getRecentIncidentByTitle(title: String, minStartTime: Long): IncidentEntity? {
        return incidents.find { it.title == title && it.startTime >= minStartTime }
    }

    override suspend fun insertIncident(incident: IncidentEntity): Long {
        val id = (incidents.size + 1).toLong()
        incidents.add(incident.copy(id = id))
        return id
    }

    override suspend fun updateIncidentStatus(id: Long, status: String, notes: String) {
        val idx = incidents.indexOfFirst { it.id == id }
        if (idx != -1) {
            incidents[idx] = incidents[idx].copy(status = status, resolutionNotes = notes)
        }
    }

    override suspend fun clearIncidents() {
        incidents.clear()
    }

    override suspend fun deleteIncident(incident: IncidentEntity) {
        incidents.removeAll { it.id == incident.id }
    }
}

class IncidentCorrelationEngineTest {

    @Test
    fun correlateSystemEvents_createsIncidentWhenThermalAndBatteryDrainCoOccur() = runTest {
        val logDao = FakeActivityLogDao()
        val incidentDao = FakeCorrelationIncidentDao()
        val engine = IncidentCorrelationEngine(logDao, incidentDao)

        val battery = BatteryState(level = 10, isCharging = false, temperature = 39.5f)
        val network = NetworkState(isConnected = true)

        engine.correlateSystemEvents(battery, network)

        assertEquals(1, incidentDao.incidents.size)
        val inc = incidentDao.incidents.first()
        assertEquals("CRITICAL", inc.severity)
        assertEquals("DETECTED", inc.status)
        assertTrue(inc.title.contains("Thermal & Rapid Battery Drain"))
        assertTrue(inc.rankedExplanations.contains("Rank 1"))
    }

    @Test
    fun correlateSystemEvents_suppressesDuplicateIncidentWithin15MinuteWindow() = runTest {
        val logDao = FakeActivityLogDao()
        val incidentDao = FakeCorrelationIncidentDao()
        val engine = IncidentCorrelationEngine(logDao, incidentDao)

        val battery = BatteryState(level = 10, isCharging = false, temperature = 39.5f)
        val network = NetworkState(isConnected = true)

        // First correlation pass -> creates incident
        engine.correlateSystemEvents(battery, network)
        assertEquals(1, incidentDao.incidents.size)

        // Second correlation pass within 15 mins -> suppresses duplicate
        engine.correlateSystemEvents(battery, network)
        assertEquals(1, incidentDao.incidents.size)
    }

    @Test
    fun updateIncidentStatus_updatesLifecycleStateAndResolutionNotes() = runTest {
        val dao = FakeCorrelationIncidentDao()
        val id = dao.insertIncident(
            IncidentEntity(
                title = "Test Thermal Spike Incident",
                severity = "WARNING",
                status = "DETECTED",
                correlatedEventsSummary = "Summary",
                supportingEvidence = "Evidence",
                correlationExplanation = "Explanation",
                recommendedChecks = "Checks"
            )
        )

        dao.updateIncidentStatus(id, "RESOLVED", "Cleared background app rendering loop.")

        val updated = dao.getIncidentById(id)
        assertNotNull(updated)
        assertEquals("RESOLVED", updated?.status)
        assertEquals("Cleared background app rendering loop.", updated?.resolutionNotes)
    }
}
