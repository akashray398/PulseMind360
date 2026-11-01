package com.dev.myapplication.ui.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.entity.ContextSnapshotEntity
import com.dev.myapplication.domain.ContextChangeEvent
import com.dev.myapplication.domain.ContextSummary
import com.dev.myapplication.domain.LiveContextEngine
import com.dev.myapplication.domain.LiveDeviceContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveContextViewModel @Inject constructor(
    private val liveContextEngine: LiveContextEngine
) : ViewModel() {

    val liveContext: StateFlow<LiveDeviceContext> = liveContextEngine.liveContext.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LiveDeviceContext()
    )

    val recentChanges: StateFlow<List<ContextChangeEvent>> = liveContextEngine.recentChanges.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _snapshotHistory = MutableStateFlow<List<ContextSnapshotEntity>>(emptyList())
    val snapshotHistory: StateFlow<List<ContextSnapshotEntity>> = _snapshotHistory.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _snapshotHistory.value = liveContextEngine.getContextHistory(30)
        }
    }

    fun getContextSummary(): ContextSummary {
        return liveContextEngine.getContextSummary()
    }

    fun getApiPayloadFormatted(): String {
        val summary = getContextSummary()
        val curr = liveContext.value
        return """
        {
          "endpoint": "/api/v1/context/current",
          "timestamp": ${curr.timestamp},
          "classification": "${curr.stateClassification}",
          "freshness": "${curr.freshness}",
          "confidence_score": ${curr.confidenceScore},
          "vitals": {
            "battery_level": ${curr.batteryLevel},
            "is_charging": ${curr.isCharging},
            "battery_temp_c": ${curr.batteryTemp},
            "ram_used_mb": ${curr.usedMemMb},
            "ram_total_mb": ${curr.totalMemMb},
            "ram_pct": ${curr.ramUsagePct},
            "network_transport": "${curr.networkTransport}",
            "downstream_kbps": ${curr.linkDownstreamKbps}
          },
          "key_takeaway": "${summary.keyTakeaway}",
          "disclaimer": "${summary.disclaimer}"
        }
        """.trimIndent()
    }
}
