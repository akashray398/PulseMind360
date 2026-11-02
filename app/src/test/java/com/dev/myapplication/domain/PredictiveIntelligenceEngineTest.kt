package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ContextSnapshotDao
import com.dev.myapplication.data.local.entity.ContextSnapshotEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.util.MemoryState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePredictiveContextSnapshotDao : ContextSnapshotDao {
    val snapshots = mutableListOf<ContextSnapshotEntity>()

    override fun getRecentSnapshots(limit: Int): Flow<List<ContextSnapshotEntity>> =
        MutableStateFlow(snapshots.take(limit))

    override suspend fun getRecentSnapshotsList(limit: Int): List<ContextSnapshotEntity> =
        snapshots.take(limit)

    override suspend fun insertSnapshot(snapshot: ContextSnapshotEntity): Long {
        val id = (snapshots.size + 1).toLong()
        snapshots.add(snapshot.copy(id = id))
        return id
    }

    override suspend fun deleteOldSnapshots(beforeTimestamp: Long) {
        snapshots.removeAll { it.timestamp < beforeTimestamp }
    }

    override suspend fun clearSnapshots() {
        snapshots.clear()
    }
}

class PredictiveIntelligenceEngineTest {

    private val dao = FakePredictiveContextSnapshotDao()
    private val engine = PredictiveIntelligenceEngine(dao)

    @Test
    fun computeBaselineStatistics_detectsInsufficientDataWhenLessThan5Samples() {
        val values = listOf(30.0f, 32.0f, 31.0f)
        val stats = engine.computeBaselineStatistics(values)

        assertTrue(stats.insufficientData)
        assertEquals(3, stats.sampleCount)
        assertEquals(31.0f, stats.mean, 0.1f)
    }

    @Test
    fun computeBaselineStatistics_calculatesAccurateMeanAndStdDevWhenSufficientData() {
        val values = listOf(30.0f, 31.0f, 32.0f, 33.0f, 34.0f)
        val stats = engine.computeBaselineStatistics(values)

        assertFalse(stats.insufficientData)
        assertEquals(5, stats.sampleCount)
        assertEquals(32.0f, stats.mean, 0.1f)
        assertEquals(32.0f, stats.median, 0.1f)
        assertTrue(stats.stdDev > 0f)
    }

    @Test
    fun calculateOlsSlope_computesNegativeSlopeForDischargingBattery() {
        val now = System.currentTimeMillis()
        val points = listOf(
            now - 1200000L to 80f, // -20 mins: 80%
            now - 600000L to 75f,  // -10 mins: 75%
            now to 70f            // Now: 70%
        )

        val slopePerMin = engine.calculateOlsSlope(points)
        // Slope = (70 - 80) / 20 = -0.5% per min
        assertEquals(-0.5f, slopePerMin, 0.05f)
    }

    @Test
    fun generatePredictiveOverview_returnsValidHorizonAndConfidence() = runTest {
        val now = System.currentTimeMillis()
        dao.insertSnapshot(ContextSnapshotEntity(timestamp = now - 1800000L, batteryLevel = 80, batteryTemp = 30f))
        dao.insertSnapshot(ContextSnapshotEntity(timestamp = now - 1200000L, batteryLevel = 75, batteryTemp = 32f))
        dao.insertSnapshot(ContextSnapshotEntity(timestamp = now - 600000L, batteryLevel = 70, batteryTemp = 34f))
        dao.insertSnapshot(ContextSnapshotEntity(timestamp = now, batteryLevel = 65, batteryTemp = 36f))
        dao.insertSnapshot(ContextSnapshotEntity(timestamp = now + 100L, batteryLevel = 65, batteryTemp = 36f))

        val batteryState = BatteryState(level = 65, isCharging = false, temperature = 36f)
        val networkState = NetworkState(isConnected = true, transportType = "WIFI")
        val memoryState = MemoryState(availMemMb = 2000, totalMemMb = 6000, usedMemMb = 4000, usagePct = 66)

        val overview = engine.generatePredictiveOverview(batteryState, networkState, memoryState)

        assertTrue(overview.batteryForecast.timeToEmptyMins > 0)
        assertTrue(overview.thermalForecast.predictedTemp30m >= 36f)
        assertTrue(overview.overallConfidence in 50..100)
    }
}
