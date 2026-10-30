package com.dev.myapplication.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.domain.IncidentCorrelationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val activityLogDao: ActivityLogDao,
    private val incidentDao: IncidentDao,
    private val incidentCorrelationEngine: IncidentCorrelationEngine,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val logsState: StateFlow<List<ActivityLogEntity>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        query to category
    }.flatMapLatest { (query, category) ->
        when {
            query.isNotBlank() -> activityLogDao.searchLogs(query)
            category != "ALL" -> activityLogDao.getLogsByCategory(category)
            else -> activityLogDao.getAllLogs()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val incidentsState: StateFlow<List<IncidentEntity>> = incidentDao.getAllIncidents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val count = activityLogDao.getLogsCount()
            val now = System.currentTimeMillis()
            val bState = batteryMonitor.batteryState.value
            val nState = networkMonitor.networkState.value

            if (count == 0) {
                // Initial Seed Logs so Activity History is always populated & active
                activityLogDao.insertLog(
                    ActivityLogEntity(
                        category = "BATTERY",
                        title = "Battery Telemetry Initialized",
                        description = "System battery level observed at ${bState.level}% (${if (bState.isCharging) "Charging via ${bState.plugType}" else "Discharging"}). Temperature: ${bState.temperature}°C.",
                        severity = "INFO",
                        timestamp = now - 180_000L
                    )
                )
                activityLogDao.insertLog(
                    ActivityLogEntity(
                        category = "NETWORK",
                        title = "Network Diagnostics Active",
                        description = "Interface verified on ${nState.transportType} transport (${if (nState.isMetered) "Metered" else "Unmetered"}). Link downstream speed: ${nState.linkDownstreamKbps / 1000} Mbps.",
                        severity = "INFO",
                        timestamp = now - 120_000L
                    )
                )
                activityLogDao.insertLog(
                    ActivityLogEntity(
                        category = "AUTOMATION",
                        title = "Automation Logic Pipeline Active",
                        description = "Contextual rules, priority router, and cooldown policies evaluated against active hardware state.",
                        severity = "INFO",
                        timestamp = now - 60_000L
                    )
                )
            }

            incidentCorrelationEngine.correlateSystemEvents(bState, nState)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun clearHistory() {
        viewModelScope.launch {
            activityLogDao.clearLogs()
            incidentDao.clearIncidents()
        }
    }
}
