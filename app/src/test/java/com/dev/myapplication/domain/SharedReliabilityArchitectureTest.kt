package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.BlackBoxDao
import com.dev.myapplication.data.local.dao.DigitalTwinDao
import com.dev.myapplication.data.local.dao.ReliabilityScorecardDao
import com.dev.myapplication.data.local.entity.DiagnosticEventEntity
import com.dev.myapplication.data.local.entity.DiagnosticSessionEntity
import com.dev.myapplication.data.local.entity.DigitalTwinStateEntity
import com.dev.myapplication.data.local.entity.ReliabilityAssessmentEntity
import com.dev.myapplication.data.local.entity.SimulationSessionEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.data.repository.UserPreferencesRepository
import com.dev.myapplication.domain.blackbox.DeviceBlackBoxEngine
import com.dev.myapplication.domain.copilot.CopilotIntentEngine
import com.dev.myapplication.domain.copilot.CopilotToolDispatcher
import com.dev.myapplication.domain.core.IntelligenceResult
import com.dev.myapplication.domain.core.PulseMindIntelligenceCore
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.domain.privacy.PrivacyRiskEngine
import com.dev.myapplication.domain.scorecard.ReliabilityScorecardEngine
import com.dev.myapplication.domain.twin.DigitalTwinEngine
import com.dev.myapplication.util.MemoryState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeBlackBoxDao : BlackBoxDao {
    val events = mutableListOf<DiagnosticEventEntity>()
    val sessions = mutableListOf<DiagnosticSessionEntity>()

    override fun getRecentDiagnosticEvents(limit: Int) = MutableStateFlow(events.take(limit))
    override suspend fun getRecentDiagnosticEventsList(limit: Int) = events.take(limit)
    override fun getAllDiagnosticSessions() = MutableStateFlow(sessions)

    override suspend fun insertEvent(event: DiagnosticEventEntity): Long {
        val id = (events.size + 1).toLong()
        events.add(event.copy(id = id))
        return id
    }

    override suspend fun insertSession(session: DiagnosticSessionEntity) {
        sessions.add(session)
    }

    override suspend fun deleteOldEvents(beforeTimestamp: Long) {
        events.removeAll { it.timestamp < beforeTimestamp }
    }

    override suspend fun clearEvents() {
        events.clear()
    }
}

class FakeReliabilityScorecardDao : ReliabilityScorecardDao {
    val assessments = mutableListOf<ReliabilityAssessmentEntity>()

    override fun getAllAssessments() = MutableStateFlow(assessments)
    override suspend fun getLatestAssessmentForComponent(component: String) = assessments.findLast { it.componentName == component }
    override suspend fun insertAssessment(assessment: ReliabilityAssessmentEntity): Long {
        val id = (assessments.size + 1).toLong()
        assessments.add(assessment.copy(id = id))
        return id
    }
    override suspend fun deleteOldAssessments(beforeTimestamp: Long) {
        assessments.removeAll { it.timestamp < beforeTimestamp }
    }
}

class FakeDigitalTwinDao : DigitalTwinDao {
    val states = mutableListOf<DigitalTwinStateEntity>()
    val simulations = mutableListOf<SimulationSessionEntity>()

    override fun getRecentStateTransitions(limit: Int) = MutableStateFlow(states.filter { !it.isSimulation }.take(limit))
    override suspend fun getRecentStateTransitionsList(limit: Int) = states.filter { !it.isSimulation }.take(limit)
    override fun getAllSimulationSessions() = MutableStateFlow(simulations)

    override suspend fun insertStateTransition(state: DigitalTwinStateEntity): Long {
        val id = (states.size + 1).toLong()
        states.add(state.copy(id = id))
        return id
    }

    override suspend fun insertSimulation(simulation: SimulationSessionEntity) {
        simulations.add(simulation)
    }

    override suspend fun clearSimulationStates() {
        states.removeAll { it.isSimulation }
    }
}

class SharedReliabilityArchitectureTest {

    private val fakeContext = FakePrivacyContext()
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
        batteryMonitor = batteryMonitor,
        networkMonitor = networkMonitor,
        incidentDao = incidentDao,
        userPreferencesRepository = UserPreferencesRepository(fakeContext)
    )

    @Test
    fun blackBox_recordsEventsAndExportsSession() = runTest {
        val eventId = blackBoxEngine.recordDiagnosticEvent("THERMAL_SPIKE", "{\"temp\":39.5}", "WARNING")
        assertTrue(eventId > 0)

        val sessionReport = blackBoxEngine.exportDiagnosticSession("EXPORT_TEST")
        assertEquals("EXPORT_TEST", sessionReport.triggerReason)
        assertEquals(1, sessionReport.eventCount)
        assertEquals(1, blackBoxDao.sessions.size)
    }

    @Test
    fun reliabilityScorecard_evaluatesComponentScores() = runTest {
        val bState = BatteryState(level = 80, temperature = 32f)
        val nState = NetworkState(isConnected = true)
        val mState = MemoryState(usagePct = 50)

        val scorecard = scorecardEngine.evaluateReliabilityScorecard(bState, nState, mState)

        assertTrue(scorecard.overallScore > 0)
        assertEquals(5, scorecard.components.size)
        assertTrue(scorecardDao.assessments.isNotEmpty())
    }

    @Test
    fun digitalTwin_runsSimulationWithoutMutatingLiveState() = runTest {
        val simResult = digitalTwinEngine.simulateWhatIfScenario("HEAVY_3D_GAME", loadFactor = 2.0f)

        assertNotNull(simResult)
        assertEquals("HEAVY_3D_GAME", simResult.scenarioName)
        assertTrue(simResult.simulatedTemp >= 20f)
        assertEquals(1, digitalTwinDao.simulations.size)
    }

    @Test
    fun core_integratesAllThreeCapabilitiesSuccessfully() = runTest {
        val blackBoxRes = core.getBlackBoxSessionReport("CORE_TEST")
        assertTrue(blackBoxRes is IntelligenceResult.Success)

        val scorecardRes = core.getReliabilityScorecard()
        assertTrue(scorecardRes is IntelligenceResult.Success)

        val twinRes = core.runDigitalTwinSimulation("WHAT_IF_LOAD_SPIKE")
        assertTrue(twinRes is IntelligenceResult.Success)
    }
}
