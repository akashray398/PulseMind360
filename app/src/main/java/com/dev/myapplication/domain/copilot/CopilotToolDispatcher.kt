package com.dev.myapplication.domain.copilot

import android.content.Context
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.dao.SentinelBaselineDao
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.local.entity.SentinelBaselineEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.domain.ContextSummary
import com.dev.myapplication.domain.LiveContextEngine
import com.dev.myapplication.domain.LiveDeviceContext
import com.dev.myapplication.domain.NetworkDiagnosticReport
import com.dev.myapplication.domain.NetworkDiagnosticsEngine
import com.dev.myapplication.domain.SmartInsightsEngine
import com.dev.myapplication.domain.SystemInsights
import com.dev.myapplication.util.MemoryOptimizer
import com.dev.myapplication.util.MemoryState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CopilotToolDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val liveContextEngine: LiveContextEngine,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor,
    private val networkDiagnosticsEngine: NetworkDiagnosticsEngine,
    private val smartInsightsEngine: SmartInsightsEngine,
    private val sentinelBaselineDao: SentinelBaselineDao,
    private val incidentDao: IncidentDao,
    private val activityLogDao: ActivityLogDao
) {

    fun getCurrentDeviceContext(): LiveDeviceContext {
        return liveContextEngine.getCurrentContext()
    }

    fun getBatteryStatus(): BatteryState {
        return batteryMonitor.batteryState.value
    }

    fun getMemoryStatistics(): MemoryState {
        return MemoryOptimizer.getMemoryState(context)
    }

    suspend fun getNetworkDiagnostics(): NetworkDiagnosticReport {
        val netState = networkMonitor.networkState.value
        return networkDiagnosticsEngine.runDiagnostics(netState)
    }

    suspend fun getRecentAnomalies(): List<IncidentEntity> {
        return incidentDao.getAllIncidents().firstOrNull() ?: emptyList()
    }

    suspend fun getDeviceBaseline(): SentinelBaselineEntity? {
        return sentinelBaselineDao.getBaseline()
    }

    fun getPredictions(): SystemInsights {
        val bState = batteryMonitor.batteryState.value
        val nState = networkMonitor.networkState.value
        return smartInsightsEngine.generateInsights(bState, nState)
    }

    fun generateDiagnosticSummary(): ContextSummary {
        return liveContextEngine.getContextSummary()
    }
}
