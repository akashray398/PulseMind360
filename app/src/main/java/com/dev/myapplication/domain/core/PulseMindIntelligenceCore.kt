package com.dev.myapplication.domain.core

import android.content.Context
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.repository.UserPreferences
import com.dev.myapplication.data.repository.UserPreferencesRepository
import com.dev.myapplication.domain.AiCopilotEngine
import com.dev.myapplication.domain.IncidentCorrelationEngine
import com.dev.myapplication.domain.LiveContextEngine
import com.dev.myapplication.domain.LiveDeviceContext
import com.dev.myapplication.domain.blackbox.BlackBoxSessionReport
import com.dev.myapplication.domain.blackbox.DeviceBlackBoxEngine
import com.dev.myapplication.domain.copilot.CopilotResponseDetail
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.domain.privacy.PrivacyRiskEngine
import com.dev.myapplication.domain.scorecard.ReliabilityScorecard
import com.dev.myapplication.domain.scorecard.ReliabilityScorecardEngine
import com.dev.myapplication.domain.twin.DigitalTwinEngine
import com.dev.myapplication.domain.twin.SimulationResult
import com.dev.myapplication.util.MemoryOptimizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PulseMindIntelligenceCore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val liveContextEngine: LiveContextEngine,
    private val predictiveEngine: PredictiveIntelligenceEngine,
    private val copilotEngine: AiCopilotEngine,
    private val incidentCorrelationEngine: IncidentCorrelationEngine,
    private val privacyRiskEngine: PrivacyRiskEngine,
    private val deviceBlackBoxEngine: DeviceBlackBoxEngine,
    private val reliabilityScorecardEngine: ReliabilityScorecardEngine,
    private val digitalTwinEngine: DigitalTwinEngine,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor,
    private val incidentDao: IncidentDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Feature 1: Live Context Stream
     */
    fun getLiveContextState(): StateFlow<LiveDeviceContext> {
        return liveContextEngine.liveContext
    }

    /**
     * Unified Device State Snapshot
     */
    fun getUnifiedDeviceState(): IntelligenceResult<UnifiedDeviceState> {
        return try {
            val liveCtx = liveContextEngine.getCurrentContext()
            val summary = liveContextEngine.getContextSummary()
            IntelligenceResult.Success(
                UnifiedDeviceState(
                    liveContext = liveCtx,
                    summary = summary
                )
            )
        } catch (e: Exception) {
            IntelligenceResult.Error("Failed to aggregate unified device state", e)
        }
    }

    /**
     * Feature 3: Predictive Device Intelligence
     */
    suspend fun getPredictiveSnapshot(): IntelligenceResult<UnifiedPredictionSnapshot> {
        return try {
            val bState = batteryMonitor.batteryState.value
            val nState = networkMonitor.networkState.value
            val mState = MemoryOptimizer.getMemoryState(context)

            val overview = predictiveEngine.generatePredictiveOverview(bState, nState, mState)
            IntelligenceResult.Success(UnifiedPredictionSnapshot(overview))
        } catch (e: Exception) {
            IntelligenceResult.Error("Failed to compute predictive forecast", e)
        }
    }

    /**
     * Feature 4: Incident Investigator
     */
    suspend fun getIncidentReport(): IntelligenceResult<UnifiedIncidentReport> {
        return try {
            val incidents = incidentDao.getAllIncidents().firstOrNull() ?: emptyList()
            val criticalCount = incidents.count { it.severity == "CRITICAL" }
            IntelligenceResult.Success(
                UnifiedIncidentReport(
                    totalIncidentsCount = incidents.size,
                    criticalIncidentsCount = criticalCount,
                    recentIncidents = incidents
                )
            )
        } catch (e: Exception) {
            IntelligenceResult.Error("Failed to fetch incident investigation records", e)
        }
    }

    /**
     * Feature 5: Privacy & Permission Auditor
     */
    suspend fun getPrivacySummary(): IntelligenceResult<UnifiedPrivacySummary> {
        return try {
            val prefs = try {
                userPreferencesRepository.userPreferencesFlow.first()
            } catch (_: Exception) {
                UserPreferences()
            }
            val report = privacyRiskEngine.generateFullReport(
                isBiometricVaultEnabled = prefs.biometricVaultEnabled,
                isPdfRedactionEnabled = prefs.redactExportMetadata
            )
            IntelligenceResult.Success(
                UnifiedPrivacySummary(
                    auditReport = report,
                    privacyScore = report.score
                )
            )
        } catch (e: Exception) {
            IntelligenceResult.Error("Failed to run privacy audit", e)
        }
    }

    /**
     * Feature 2: AI Copilot
     */
    suspend fun askCopilot(query: String, sessionId: String): IntelligenceResult<CopilotResponseDetail> {
        return try {
            val bState = batteryMonitor.batteryState.value
            val nState = networkMonitor.networkState.value
            val response = copilotEngine.answerQueryDetailed(query, sessionId, bState, nState)
            IntelligenceResult.Success(response)
        } catch (e: Exception) {
            IntelligenceResult.Error("Copilot query processing failed", e)
        }
    }

    /**
     * Capability 1: Device Black Box
     */
    suspend fun getBlackBoxSessionReport(reason: String = "MANUAL_EXPORT"): IntelligenceResult<BlackBoxSessionReport> {
        return try {
            val report = deviceBlackBoxEngine.exportDiagnosticSession(reason)
            IntelligenceResult.Success(report)
        } catch (e: Exception) {
            IntelligenceResult.Error("Failed to export Black Box diagnostic session", e)
        }
    }

    /**
     * Capability 2: Reliability Scorecard
     */
    suspend fun getReliabilityScorecard(): IntelligenceResult<ReliabilityScorecard> {
        return try {
            val bState = batteryMonitor.batteryState.value
            val nState = networkMonitor.networkState.value
            val mState = MemoryOptimizer.getMemoryState(context)
            val scorecard = reliabilityScorecardEngine.evaluateReliabilityScorecard(bState, nState, mState)
            IntelligenceResult.Success(scorecard)
        } catch (e: Exception) {
            IntelligenceResult.Error("Failed to compute reliability scorecard", e)
        }
    }

    /**
     * Capability 3: Digital Twin Simulation
     */
    suspend fun runDigitalTwinSimulation(scenarioName: String = "WHAT_IF_LOAD_SPIKE"): IntelligenceResult<SimulationResult> {
        return try {
            val result = digitalTwinEngine.simulateWhatIfScenario(scenarioName)
            IntelligenceResult.Success(result)
        } catch (e: Exception) {
            IntelligenceResult.Error("Failed to execute Digital Twin simulation", e)
        }
    }
}
