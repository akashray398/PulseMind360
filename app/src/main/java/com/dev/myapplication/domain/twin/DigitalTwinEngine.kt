package com.dev.myapplication.domain.twin

import com.dev.myapplication.data.local.dao.DigitalTwinDao
import com.dev.myapplication.data.local.entity.DigitalTwinStateEntity
import com.dev.myapplication.data.local.entity.SimulationSessionEntity
import com.dev.myapplication.domain.LiveContextEngine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DigitalTwinEngine @Inject constructor(
    private val digitalTwinDao: DigitalTwinDao,
    private val liveContextEngine: LiveContextEngine
) {

    suspend fun recordStateTransition(
        stateType: String,
        previousValue: String,
        currentValue: String
    ): Long {
        val entity = DigitalTwinStateEntity(
            stateType = stateType,
            previousValue = previousValue,
            currentValue = currentValue,
            effectiveTimestamp = System.currentTimeMillis(),
            isSimulation = false
        )
        return digitalTwinDao.insertStateTransition(entity)
    }

    suspend fun simulateWhatIfScenario(
        scenarioName: String = "WHAT_IF_LOAD_SPIKE",
        loadFactor: Float = 1.5f
    ): SimulationResult {
        val liveCtx = liveContextEngine.getCurrentContext()

        val simBattery = (liveCtx.batteryLevel - (10 * loadFactor)).toInt().coerceIn(0, 100)
        val simTemp = (liveCtx.batteryTemp + (3.5f * loadFactor)).coerceIn(20f, 55f)
        val simRam = (liveCtx.ramUsagePct + (15 * loadFactor)).toInt().coerceIn(10, 99)

        val impactText = when {
            simTemp >= 40f -> "CRITICAL: What-if simulation projects severe thermal throttling (+${"%.1f".format(3.5f * loadFactor)}°C rise)."
            simBattery <= 15 -> "WARNING: What-if simulation projects low battery depletion threshold."
            else -> "NOMINAL: System handles simulated $scenarioName load within safe baseline operational bounds."
        }

        val sessionEntity = SimulationSessionEntity(
            scenarioName = scenarioName,
            simulatedBatteryDelta = -10f * loadFactor,
            simulatedThermalDelta = +3.5f * loadFactor,
            createdAt = System.currentTimeMillis()
        )
        digitalTwinDao.insertSimulation(sessionEntity)

        return SimulationResult(
            scenarioName = scenarioName,
            simulatedBatteryLevel = simBattery,
            simulatedTemp = simTemp,
            simulatedRamUsagePct = simRam,
            systemImpactText = impactText
        )
    }

    fun getRecentTransitionsFlow(limit: Int = 50): Flow<List<DigitalTwinStateEntity>> {
        return digitalTwinDao.getRecentStateTransitions(limit)
    }

    suspend fun getRecentTransitionsList(limit: Int = 50): List<DigitalTwinStateEntity> {
        return digitalTwinDao.getRecentStateTransitionsList(limit)
    }
}
