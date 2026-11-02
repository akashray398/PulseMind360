package com.dev.myapplication.ui.predictive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.domain.predictive.PredictiveOverview
import com.dev.myapplication.util.MemoryOptimizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PredictiveIntelligenceViewModel @Inject constructor(
    private val predictiveEngine: PredictiveIntelligenceEngine,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    val batteryState: StateFlow<BatteryState> = batteryMonitor.batteryState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BatteryState()
    )

    val networkState: StateFlow<NetworkState> = networkMonitor.networkState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkState()
    )

    private val _overview = MutableStateFlow<PredictiveOverview?>(null)
    val overview: StateFlow<PredictiveOverview?> = _overview.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPredictions()
    }

    fun loadPredictions() {
        viewModelScope.launch {
            _isLoading.value = true
            val bState = batteryState.value
            val nState = networkState.value
            val mState = MemoryOptimizer.getMemoryState(context)

            _overview.value = predictiveEngine.generatePredictiveOverview(bState, nState, mState)
            _isLoading.value = false
        }
    }
}
