package com.dev.myapplication.ui.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.domain.AutomationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    fun toggleRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            automationRuleDao.updateRuleStatus(rule.id, !rule.isEnabled)
        }
    }

    fun addRule(
        name: String,
        conditionType: String,
        conditionValue: Int,
        actionType: String,
        actionTarget: String
    ) {
        viewModelScope.launch {
            automationRuleDao.insertRule(
                AutomationRuleEntity(
                    name = name,
                    conditionType = conditionType,
                    conditionValue = conditionValue,
                    actionType = actionType,
                    actionTarget = actionTarget,
                    isEnabled = true
                )
            )
        }
    }

    fun deleteRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            automationRuleDao.deleteRule(rule)
        }
    }

    fun testRunRules() {
        viewModelScope.launch {
            automationEngine.evaluateRules(
                batteryState = batteryMonitor.batteryState.value,
                networkState = networkMonitor.networkState.value
            )
        }
    }
}
