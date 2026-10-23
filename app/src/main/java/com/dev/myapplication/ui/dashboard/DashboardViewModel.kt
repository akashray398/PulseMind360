package com.dev.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.data.remote.NetworkInfoApi
import com.dev.myapplication.data.remote.NetworkInfoResponse
import com.dev.myapplication.data.remote.Resource
import com.dev.myapplication.data.worker.DeviceSyncWorker
import com.dev.myapplication.domain.AnomalyDetector
import com.dev.myapplication.domain.AnomalyReport
import com.dev.myapplication.domain.AutomationEngine
import com.dev.myapplication.domain.FramePerformanceMonitor
import com.dev.myapplication.domain.FramePerformanceState
import com.dev.myapplication.domain.SmartInsightsEngine
import com.dev.myapplication.domain.SystemInsights
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor,
    private val automationEngine: AutomationEngine,
    private val smartInsightsEngine: SmartInsightsEngine,
    private val anomalyDetector: AnomalyDetector,
    private val framePerformanceMonitor: FramePerformanceMonitor,
    private val networkInfoApi: NetworkInfoApi,
    private val workManager: WorkManager
) : ViewModel() {

    val batteryState: StateFlow<BatteryState> = batteryMonitor.batteryState
    val networkState: StateFlow<NetworkState> = networkMonitor.networkState
    val framePerformanceState: StateFlow<FramePerformanceState> = framePerformanceMonitor.frameState

    val insightsState: StateFlow<SystemInsights> = combine(
        batteryState,
        networkState
    ) { bState, nState ->
        smartInsightsEngine.generateInsights(bState, nState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = smartInsightsEngine.generateInsights(batteryState.value, networkState.value)
    )

    val anomalyState: StateFlow<AnomalyReport> = combine(
        batteryState,
        networkState
    ) { bState, nState ->
        anomalyDetector.detectAnomalies(bState, nState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = anomalyDetector.detectAnomalies(batteryState.value, networkState.value)
    )

    val systemHealthScore: StateFlow<Int> = combine(
        batteryState,
        networkState,
        anomalyState,
        framePerformanceState
    ) { bState, nState, aState, fState ->
        var score = 100
        if (aState.hasAnomaly) {
            score -= if (aState.severity == "ALERT") 25 else 15
        }
        if (!bState.isCharging && bState.level < 20) {
            score -= 20
        }
        if (bState.temperature > 38f) {
            score -= 15
        }
        if (!nState.isConnected) {
            score -= 10
        }
        if (fState.currentFps in 1..44) {
            score -= 10
        }
        score.coerceIn(5, 100)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 100
    )

    private val _networkInfoState = MutableStateFlow<Resource<NetworkInfoResponse>>(Resource.Idle)
    val networkInfoState: StateFlow<Resource<NetworkInfoResponse>> = _networkInfoState.asStateFlow()

    private val _workerState = MutableStateFlow<String>("IDLE")
    val workerState: StateFlow<String> = _workerState.asStateFlow()

    init {
        viewModelScope.launch {
            batteryState.collect { bState ->
                automationEngine.evaluateRules(bState, networkState.value)
            }
        }
        viewModelScope.launch {
            networkState.collect { nState ->
                automationEngine.evaluateRules(batteryState.value, nState)
            }
        }
    }

    fun fetchNetworkInfo() {
        viewModelScope.launch {
            _networkInfoState.value = Resource.Loading
            try {
                val response = networkInfoApi.getNetworkInfo()
                _networkInfoState.value = Resource.Success(response)
            } catch (e: Exception) {
                _networkInfoState.value = Resource.Error(e.localizedMessage ?: "Failed to reach network API")
            }
        }
    }

    fun triggerSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DeviceSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueue(request)

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(request.id).collect { workInfo ->
                if (workInfo != null) {
                    _workerState.value = when (workInfo.state) {
                        WorkInfo.State.ENQUEUED -> "ENQUEUED (Backoff Policy Ready)"
                        WorkInfo.State.RUNNING -> "RUNNING (Flushing Offline Queue...)"
                        WorkInfo.State.SUCCEEDED -> "SYNC SUCCESS"
                        WorkInfo.State.FAILED -> "FAILED (Retrying Exponentially)"
                        WorkInfo.State.BLOCKED -> "BLOCKED (Awaiting Network)"
                        WorkInfo.State.CANCELLED -> "CANCELLED"
                    }
                }
            }
        }
    }
}
