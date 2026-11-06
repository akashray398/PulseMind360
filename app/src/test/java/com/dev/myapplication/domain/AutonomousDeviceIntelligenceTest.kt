package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.AdiDao
import com.dev.myapplication.data.local.entity.CausalRecordEntity
import com.dev.myapplication.data.local.entity.EarlyWarningEntity
import com.dev.myapplication.data.local.entity.HealingActionEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.data.repository.UserPreferencesRepository
import com.dev.myapplication.domain.blackbox.DeviceBlackBoxEngine
import com.dev.myapplication.domain.causality.DeviceCausalityEngine
import com.dev.myapplication.domain.copilot.CopilotIntentEngine
import com.dev.myapplication.domain.copilot.CopilotToolDispatcher
import com.dev.myapplication.domain.core.IntelligenceResult
import com.dev.myapplication.domain.core.PulseMindIntelligenceCore
import com.dev.myapplication.domain.healing.SelfHealingOrchestrator
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.domain.privacy.PrivacyRiskEngine
import com.dev.myapplication.domain.scorecard.ReliabilityScorecardEngine
import com.dev.myapplication.domain.twin.DigitalTwinEngine
import com.dev.myapplication.domain.warning.EarlyWarningSystem
import com.dev.myapplication.util.MemoryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAdiDao : AdiDao {
    val causalRecords = mutableListOf<CausalRecordEntity>()
    val earlyWarnings = mutableListOf<EarlyWarningEntity>()
    val healingActions = mutableListOf<HealingActionEntity>()

    override fun getRecentCausalRecords(limit: Int) = MutableStateFlow(causalRecords.take(limit))
    override suspend fun getRecentCausalRecordsList(limit: Int) = causalRecords.take(limit)
    override suspend fun insertCausalRecord(record: CausalRecordEntity) { causalRecords.add(record) }

    override fun getRecentEarlyWarnings(limit: Int) = MutableStateFlow(earlyWarnings.take(limit))
    override suspend fun getRecentEarlyWarningsList(limit: Int) = earlyWarnings.take(limit)
    override suspend fun insertEarlyWarning(warning: EarlyWarningEntity) { earlyWarnings.add(warning) }

    override fun getRecentHealingActions(limit: Int) = MutableStateFlow(healingActions.take(limit))
    override suspend fun insertHealingAction(action: HealingActionEntity) { healingActions.add(action) }
    override suspend fun deleteOldWarnings(beforeTimestamp: Long) { earlyWarnings.removeAll { it.timestamp < beforeTimestamp } }
}

class AutonomousDeviceIntelligenceTest {

    private val fakeContext = FakePrivacyContext()
    private val adiDao = FakeAdiDao()
    private val blackBoxDao = FakeBlackBoxDao()
    private val scorecardDao = FakeReliabilityScorecardDao()
    private val digitalTwinDao = FakeDigitalTwinDao()
    private val snapshotDao = FakePredictiveContextSnapshotDao()
    private val incidentDao = FakeCoreIncidentDao()
    private val activityLogDao = FakeActivityLogDao()
    private val sentinelBaselineDao = FakeCoreSentinelBaselineDao()
    private val copilotConversationDao = FakeCoreCopilotConversationDao()

    private val batteryMonitor = BatteryMonitor(fakeContext)
    private val networkMonitor = NetworkMonitor(fakeContext)
    private val anomalyDetector = AnomalyDetector()
    private val sentinelEngine = SentinelEngine(sentinelBaselineDao)

    private val liveContextEngine = LiveContextEngine(
        context = fakeContext,
        batteryMonitor = batteryMonitor,
        networkMonitor = networkMonitor,
        anomalyDetector = anomalyDetector,
        sentinelEngine = sentinelEngine,
        contextSnapshotDao = snapshotDao,
        incidentDao = incidentDao,
        activityLogDao = activityLogDao
    )

    private val predictiveEngine = PredictiveIntelligenceEngine(snapshotDao)
    private val blackBoxEngine = DeviceBlackBoxEngine(blackBoxDao)
    private val scorecardEngine = ReliabilityScorecardEngine(scorecardDao, liveContextEngine, predictiveEngine)
    private val digitalTwinEngine = DigitalTwinEngine(digitalTwinDao, liveContextEngine)

    private val causalityEngine = DeviceCausalityEngine(adiDao, incidentDao, blackBoxEngine, digitalTwinEngine)
    private val warningSystem = EarlyWarningSystem(adiDao, sentinelBaselineDao, predictiveEngine)
    private val healingOrchestrator = SelfHealingOrchestrator(fakeContext, adiDao, digitalTwinEngine)

    private val copilotToolDispatcher = CopilotToolDispatcher(
        context = fakeContext,
        liveContextEngine = liveContextEngine,
        batteryMonitor = batteryMonitor,
        networkMonitor = networkMonitor,
        networkDiagnosticsEngine = NetworkDiagnosticsEngine(),
        smartInsightsEngine = SmartInsightsEngine(),
        sentinelBaselineDao = sentinelBaselineDao,
        incidentDao = incidentDao,
        activityLogDao = activityLogDao
    )

    private val copilotEngine = AiCopilotEngine(
        incidentDao = incidentDao,
        sentinelBaselineDao = sentinelBaselineDao,
        activityLogDao = activityLogDao,
        liveContextEngine = liveContextEngine,
        copilotToolDispatcher = copilotToolDispatcher,
        copilotIntentEngine = CopilotIntentEngine(),
        copilotConversationDao = copilotConversationDao
    )

    private val core = PulseMindIntelligenceCore(
        context = fakeContext,
        liveContextEngine = liveContextEngine,
        predictiveEngine = predictiveEngine,
        copilotEngine = copilotEngine,
        incidentCorrelationEngine = IncidentCorrelationEngine(activityLogDao, incidentDao),
        privacyRiskEngine = PrivacyRiskEngine(fakeContext),
        deviceBlackBoxEngine = blackBoxEngine,
        reliabilityScorecardEngine = scorecardEngine,
        digitalTwinEngine = digitalTwinEngine,
        deviceCausalityEngine = causalityEngine,
        earlyWarningSystem = warningSystem,
        selfHealingOrchestrator = healingOrchestrator,
        batteryMonitor = batteryMonitor,
        networkMonitor = networkMonitor,
        incidentDao = incidentDao,
        userPreferencesRepository = UserPreferencesRepository(fakeContext)
    )

    @Test
    fun causalityEngine_evaluatesEvidenceAndGeneratesHypothesis() = runTest {
        val bState = BatteryState(level = 15, temperature = 39.5f, isCharging = false)
        val nState = NetworkState(isConnected = true)
        val mState = MemoryState(usagePct = 85)

        val result = causalityEngine.analyzeDeviceCausality(bState, nState, mState)

        assertNotNull(result)
        assertTrue(result.hypotheses.isNotEmpty())
        assertTrue(result.hypotheses.any { it.problemSummary.contains("Thermal Slope") })
        assertTrue(adiDao.causalRecords.isNotEmpty())
    }

    @Test
    fun earlyWarningSystem_detectsBaselineDeviations() = runTest {
        val bState = BatteryState(level = 50, temperature = 38.0f)
        val nState = NetworkState(isConnected = true)
        val mState = MemoryState(usagePct = 85)

        val result = warningSystem.evaluateEarlyWarnings(bState, nState, mState)

        assertNotNull(result)
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(adiDao.earlyWarnings.isNotEmpty())
    }

    @Test
    fun earlyWarningSystem_enforcesCooldownAndNetworkWarning() = runTest {
        val bState = BatteryState(level = 50, temperature = 38.0f)
        val nState = NetworkState(isConnected = false)
        val mState = MemoryState(usagePct = 50)

        // Initial Evaluation
        val res1 = warningSystem.evaluateEarlyWarnings(bState, nState, mState)
        assertTrue(res1.warnings.any { it.metricType == "NETWORK_INSTABILITY" })

        // Immediate Re-evaluation (Cooldown active)
        val res2 = warningSystem.evaluateEarlyWarnings(bState, nState, mState)
        assertTrue(res2.warnings.isEmpty()) // Cooldown skips duplicate warning
    }

    @Test
    fun selfHealingOrchestrator_executesActionAndVerifiesPostState() = runTest {
        val actions = healingOrchestrator.getAvailableActions()
        assertTrue(actions.size >= 3)

        val trimResult = healingOrchestrator.executeAction("TRIM_RAM_CACHE")
        assertTrue(trimResult.isSuccess)
        assertEquals("VERIFIED", trimResult.verificationStatus)
        assertTrue(adiDao.healingActions.isNotEmpty())

        val unauthResult = healingOrchestrator.executeAction("CLEAR_HISTORICAL_LOGS", isAuthenticated = false)
        assertFalse(unauthResult.isSuccess)
        assertEquals("UNAUTHORIZED", unauthResult.verificationStatus)
    }

    @Test
    fun core_exposesAdiCapabilitiesSuccessfully() = runTest {
        val causalityRes = core.getDeviceCausalityAnalysis()
        assertTrue(causalityRes is IntelligenceResult.Success)

        val warningRes = core.getEarlyWarnings()
        assertTrue(warningRes is IntelligenceResult.Success)

        val healingOverviewRes = core.getSelfHealingOverview()
        assertTrue(healingOverviewRes is IntelligenceResult.Success)

        val execRes = core.executeSelfHealingAction("TRIM_RAM_CACHE")
        assertTrue(execRes is IntelligenceResult.Success)
    }
}
