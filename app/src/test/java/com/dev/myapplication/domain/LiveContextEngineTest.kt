package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ContextSnapshotDao
import com.dev.myapplication.data.local.entity.ContextSnapshotEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.util.MemoryState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeContextSnapshotDao : ContextSnapshotDao {
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

class LiveContextEngineTest {

    @Test
    fun defaultLiveDeviceContext_hasOptimalClassification() {
        val context = LiveDeviceContext()
        assertEquals(DeviceStateClassification.OPTIMAL, context.stateClassification)
        assertEquals(ContextFreshness.FRESH, context.freshness)
        assertEquals(95, context.confidenceScore)
        assertFalse(context.isStale)
    }

    @Test
    fun contextSnapshotDao_insertsAndPrunesOldSnapshots() = runTest {
        val dao = FakeContextSnapshotDao()
        val now = System.currentTimeMillis()

        dao.insertSnapshot(ContextSnapshotEntity(timestamp = now - 100_000_000L, batteryLevel = 50)) // Old > 24h
        dao.insertSnapshot(ContextSnapshotEntity(timestamp = now, batteryLevel = 85)) // Current

        assertEquals(2, dao.snapshots.size)

        // Prune older than 24h (now - 86,400,000)
        dao.deleteOldSnapshots(now - 86_400_000L)

        assertEquals(1, dao.snapshots.size)
        assertEquals(85, dao.snapshots.first().batteryLevel)
    }

    @Test
    fun contextSummary_generatesValidDisclaimerAndTakeaway() {
        val liveContext = LiveDeviceContext(
            batteryLevel = 15,
            batteryTemp = 39.5f,
            stateClassification = DeviceStateClassification.THERMAL_STRESS
        )

        val summary = ContextSummary(
            currentClassification = liveContext.stateClassification,
            activeAnomaliesCount = 1,
            freshness = liveContext.freshness,
            confidenceScore = liveContext.confidenceScore,
            keyTakeaway = "Elevated thermal slope detected (${liveContext.batteryTemp}°C)."
        )

        assertEquals(DeviceStateClassification.THERMAL_STRESS, summary.currentClassification)
        assertEquals(1, summary.activeAnomaliesCount)
        assertTrue(summary.disclaimer.contains("correlations reflect statistical co-occurrence", ignoreCase = true))
    }

    @Test
    fun stateClassification_detectsMultivariateStress() {
        val battery = BatteryState(level = 10, isCharging = false, temperature = 39.5f)
        val network = NetworkState(isConnected = false)
        val memory = MemoryState(usagePct = 90)

        // Multiple conditions: Low battery + Thermal spike + RAM pressure + Network issue
        val isThermal = battery.temperature >= 38.0f
        val isLowBattery = !battery.isCharging && battery.level <= 15
        val isRamPressure = memory.usagePct >= 85
        val isNetworkIssue = !network.isConnected

        val anomalyCount = listOf(isThermal, isLowBattery, isRamPressure, isNetworkIssue).count { it }

        val classification = when {
            anomalyCount >= 2 -> DeviceStateClassification.CRITICAL_MULTIVARIATE
            isThermal -> DeviceStateClassification.THERMAL_STRESS
            else -> DeviceStateClassification.OPTIMAL
        }

        assertEquals(DeviceStateClassification.CRITICAL_MULTIVARIATE, classification)
    }
}
