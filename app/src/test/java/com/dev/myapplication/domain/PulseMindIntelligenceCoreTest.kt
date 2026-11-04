package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.CopilotConversationDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.dao.SentinelBaselineDao
import com.dev.myapplication.data.local.entity.CopilotConversationEntity
import com.dev.myapplication.data.local.entity.CopilotMessageEntity
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.local.entity.SentinelBaselineEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.NetworkMonitor
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseMindIntelligenceCoreTest {

    private val fakeContext = FakePrivacyContext()
    private val snapshotDao = FakePredictiveContextSnapshotDao()
    private val incidentDao = FakeCoreIncidentDao()
    private val activityLogDao = FakeActivityLogDao()
    private val sentinelBaselineDao = FakeCoreSentinelBaselineDao()
    private val copilotConversationDao = FakeCoreCopilotConversationDao()

    private val blackBoxDao = FakeBlackBoxDao()
    private val scorecardDao = FakeReliabilityScorecardDao()
    private val digitalTwinDao = FakeDigitalTwinDao()
    private val adiDao = FakeAdiDao()

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

    private val smartInsightsEngine = SmartInsightsEngine()
    private val networkDiagnosticsEngine = NetworkDiagnosticsEngine()
    private val privacyRiskEngine = PrivacyRiskEngine(fakeContext)

    private val copilotToolDispatcher = CopilotToolDispatcher(
        context = fakeContext,
        liveContextEngine = liveContextEngine,
        batteryMonitor = batteryMonitor,
        networkMonitor = networkMonitor,
        networkDiagnosticsEngine = networkDiagnosticsEngine,
        smartInsightsEngine = smartInsightsEngine,
        sentinelBaselineDao = sentinelBaselineDao,
        incidentDao = incidentDao,
        activityLogDao = activityLogDao
    )

    private val copilotIntentEngine = CopilotIntentEngine()

    private val copilotEngine = AiCopilotEngine(
        incidentDao = incidentDao,
        sentinelBaselineDao = sentinelBaselineDao,
        activityLogDao = activityLogDao,
        liveContextEngine = liveContextEngine,
        copilotToolDispatcher = copilotToolDispatcher,
        copilotIntentEngine = copilotIntentEngine,
        copilotConversationDao = copilotConversationDao
    )

    private val userPreferencesRepository = UserPreferencesRepository(fakeContext)

    private val core = PulseMindIntelligenceCore(
        context = fakeContext,
        liveContextEngine = liveContextEngine,
        predictiveEngine = predictiveEngine,
        copilotEngine = copilotEngine,
        incidentCorrelationEngine = IncidentCorrelationEngine(activityLogDao, incidentDao),
        privacyRiskEngine = privacyRiskEngine,
        deviceBlackBoxEngine = blackBoxEngine,
        reliabilityScorecardEngine = scorecardEngine,
        digitalTwinEngine = digitalTwinEngine,
        deviceCausalityEngine = causalityEngine,
        earlyWarningSystem = warningSystem,
        selfHealingOrchestrator = healingOrchestrator,
        batteryMonitor = batteryMonitor,
        networkMonitor = networkMonitor,
        incidentDao = incidentDao,
        userPreferencesRepository = userPreferencesRepository
    )

    @Test
    fun getUnifiedDeviceState_returnsSuccessWithContextAndSummary() {
        val result = core.getUnifiedDeviceState()
        assertTrue(result is IntelligenceResult.Success)
        val data = (result as IntelligenceResult.Success).data
        assertNotNull(data.liveContext)
        assertNotNull(data.summary)
    }

    @Test
    fun getPredictiveSnapshot_returnsSuccessWithPredictiveOverview() = runTest {
        val result = core.getPredictiveSnapshot()
        assertTrue(result is IntelligenceResult.Success)
        val data = (result as IntelligenceResult.Success).data
        assertNotNull(data.predictiveOverview)
    }

    @Test
    fun getIncidentReport_returnsSuccessWithIncidentsCount() = runTest {
        val result = core.getIncidentReport()
        assertTrue(result is IntelligenceResult.Success)
        val data = (result as IntelligenceResult.Success).data
        assertEquals(0, data.totalIncidentsCount)
    }

    @Test
    fun getPrivacySummary_returnsSuccessWithPrivacyScore() = runTest {
        val result = core.getPrivacySummary()
        assertTrue(result is IntelligenceResult.Success)
        val data = (result as IntelligenceResult.Success).data
        assertTrue(data.privacyScore > 0)
    }

    @Test
    fun askCopilot_returnsSuccessWithGroundedResponse() = runTest {
        val result = core.askCopilot("Why is my battery draining?", "test_session")
        assertTrue(result is IntelligenceResult.Success)
        val data = (result as IntelligenceResult.Success).data
        assertNotNull(data.summary)
        assertTrue(data.measuredFacts.isNotEmpty())
    }
}

class FakeCoreSentinelBaselineDao : SentinelBaselineDao {
    var baseline = SentinelBaselineEntity()
    override fun getBaselineFlow() = MutableStateFlow(baseline)
    override suspend fun getBaseline() = baseline
    override suspend fun updateBaseline(baseline: SentinelBaselineEntity) { this.baseline = baseline }
}

class FakeCoreIncidentDao : IncidentDao {
    val list = mutableListOf<IncidentEntity>()
    override fun getAllIncidents() = MutableStateFlow(list)
    override suspend fun getIncidentById(id: Long) = list.find { it.id == id }
    override suspend fun getRecentIncidentByTitle(title: String, minStartTime: Long) = list.find { it.title == title && it.startTime >= minStartTime }
    override suspend fun insertIncident(incident: IncidentEntity): Long {
        val id = (list.size + 1).toLong()
        list.add(incident.copy(id = id))
        return id
    }
    override suspend fun updateIncidentStatus(id: Long, status: String, notes: String) {
        val idx = list.indexOfFirst { it.id == id }
        if (idx != -1) list[idx] = list[idx].copy(status = status, resolutionNotes = notes)
    }
    override suspend fun clearIncidents() { list.clear() }
    override suspend fun deleteIncident(incident: IncidentEntity) { list.remove(incident) }
}

class FakeCoreCopilotConversationDao : CopilotConversationDao {
    val conversations = mutableListOf<CopilotConversationEntity>()
    val messages = mutableListOf<CopilotMessageEntity>()

    override fun getAllConversations() = MutableStateFlow(conversations)
    override suspend fun getConversationById(id: String) = conversations.find { it.id == id }
    override fun getMessagesForConversation(conversationId: String) = MutableStateFlow(messages.filter { it.conversationId == conversationId })
    override suspend fun getMessagesListForConversation(conversationId: String) = messages.filter { it.conversationId == conversationId }
    override suspend fun searchMessages(query: String) = messages.filter { it.queryText.contains(query) }
    override suspend fun insertConversation(conversation: CopilotConversationEntity) { conversations.add(conversation) }
    override suspend fun insertMessage(message: CopilotMessageEntity) { messages.add(message) }
    override suspend fun updateMessageFeedback(messageId: String, feedback: String) {}
    override suspend fun deleteConversation(conversationId: String) { conversations.removeAll { it.id == conversationId } }
    override suspend fun deleteMessagesForConversation(conversationId: String) { messages.removeAll { it.conversationId == conversationId } }
    override suspend fun clearAllConversations() { conversations.clear() }
    override suspend fun clearAllMessages() { messages.clear() }
}
