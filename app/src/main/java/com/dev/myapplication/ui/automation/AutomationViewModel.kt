package com.dev.myapplication.ui.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.domain.AutomationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutomationViewModel @Inject constructor(
    private val automationRuleDao: AutomationRuleDao,
    private val automationEngine: AutomationEngine,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val rulesState: StateFlow<List<AutomationRuleEntity>> = automationRuleDao.getAllRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _evaluationFeedback = MutableStateFlow<String?>(null)
    val evaluationFeedback: StateFlow<String?> = _evaluationFeedback.asStateFlow()

    fun toggleRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            automationRuleDao.updateRuleStatus(rule.id, !rule.isEnabled)
        }
    }

    fun addRule(
        name: String,
        conditionType: String,
        conditionValue: Int,
        secondaryConditionType: String = "",
        secondaryConditionValue: Int = 0,
        logicalOperator: String = "NONE",
        actionType: String,
        actionTarget: String,
        cooldownMinutes: Int = 5,
        priority: Int = 2
    ) {
        viewModelScope.launch {
            automationRuleDao.insertRule(
                AutomationRuleEntity(
                    name = name,
                    conditionType = conditionType,
                    conditionValue = conditionValue,
                    secondaryConditionType = secondaryConditionType,
                    secondaryConditionValue = secondaryConditionValue,
                    logicalOperator = logicalOperator,
                    actionType = actionType,
                    actionTarget = actionTarget,
                    cooldownMinutes = cooldownMinutes,
                    priority = priority,
                    isEnabled = true
                )
            )
        }
    }

    fun addPresetRecipe(recipeType: String) {
        viewModelScope.launch {
            val rule = when (recipeType) {
                "LOW_BATTERY" -> AutomationRuleEntity(
                    name = "Low Battery Saver",
                    conditionType = "BATTERY_BELOW",
                    conditionValue = 15,
                    actionType = "SHOW_NOTIFICATION",
                    actionTarget = "Battery <= 15%. Connect power source to maintain system tasks.",
                    cooldownMinutes = 10,
                    priority = 1
                )
                "WIFI_SYNC" -> AutomationRuleEntity(
                    name = "Wi-Fi Sync Guard",
                    conditionType = "NETWORK_CONNECTED",
                    conditionValue = 0,
                    actionType = "TRIGGER_SYNC_WORKER",
                    actionTarget = "Background Sync",
                    cooldownMinutes = 15,
                    priority = 2
                )
                "CHARGING_ALERT" -> AutomationRuleEntity(
                    name = "Charging Started Alert",
                    conditionType = "CHARGING_STATE_CHANGED",
                    conditionValue = 0,
                    actionType = "SHOW_NOTIFICATION",
                    actionTarget = "Device plugged in and charging.",
                    cooldownMinutes = 5,
                    priority = 2
                )
                "OFFLINE_LOG" -> AutomationRuleEntity(
                    name = "Offline Event Log",
                    conditionType = "NETWORK_DISCONNECTED",
                    conditionValue = 0,
                    actionType = "LOG_EVENT",
                    actionTarget = "Offline State",
                    cooldownMinutes = 5,
                    priority = 3
                )
                else -> return@launch
            }
            automationRuleDao.insertRule(rule)
            _evaluationFeedback.value = "Recipe '${rule.name}' added with 1-click!"
        }
    }

    fun deleteRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            automationRuleDao.deleteRule(rule)
        }
    }

    fun testRunRules() {
        viewModelScope.launch {
            val bState = batteryMonitor.batteryState.value
            val nState = networkMonitor.networkState.value
            val summary = automationEngine.evaluateRules(
                batteryState = bState,
                networkState = nState
            )
            _evaluationFeedback.value = "Evaluated ${summary.activeRulesCount} active rules against live battery (${bState.level}%) & network (${if (nState.isConnected) nState.transportType else "Offline"}). ${summary.triggeredRulesCount} rule(s) satisfied."
        }
    }

    fun clearFeedback() {
        _evaluationFeedback.value = null
    }
}
