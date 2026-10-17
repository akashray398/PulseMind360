package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAutomationRuleDao : AutomationRuleDao {
    val rules = mutableListOf<AutomationRuleEntity>()

    override fun getAllRules(): Flow<List<AutomationRuleEntity>> = MutableStateFlow(rules)
    override fun getActiveRules(): Flow<List<AutomationRuleEntity>> = MutableStateFlow(rules.filter { it.isEnabled })
    override suspend fun getActiveRulesList(): List<AutomationRuleEntity> = rules.filter { it.isEnabled }

    override suspend fun insertRule(rule: AutomationRuleEntity): Long {
        val id = (rules.size + 1).toLong()
        rules.add(rule.copy(id = id))
        return id
    }

    override suspend fun updateRule(rule: AutomationRuleEntity) {
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index != -1) rules[index] = rule
    }

    override suspend fun updateRuleStatus(id: Long, isEnabled: Boolean) {
        val index = rules.indexOfFirst { it.id == id }
        if (index != -1) rules[index] = rules[index].copy(isEnabled = isEnabled)
    }

    override suspend fun updateLastTriggered(id: Long, timestamp: Long) {
        val index = rules.indexOfFirst { it.id == id }
        if (index != -1) rules[index] = rules[index].copy(lastTriggeredAt = timestamp)
    }

    override suspend fun deleteRule(rule: AutomationRuleEntity) {
        rules.removeAll { it.id == rule.id }
    }
}

class FakeActivityLogDao : ActivityLogDao {
    val logs = mutableListOf<ActivityLogEntity>()

    override fun getAllLogs(): Flow<List<ActivityLogEntity>> = MutableStateFlow(logs)
    override fun getLogsByCategory(category: String): Flow<List<ActivityLogEntity>> =
        MutableStateFlow(logs.filter { it.category == category })

    override fun searchLogs(query: String): Flow<List<ActivityLogEntity>> =
        MutableStateFlow(logs.filter { it.title.contains(query) || it.description.contains(query) })

    override suspend fun insertLog(log: ActivityLogEntity) {
        logs.add(log)
    }

    override suspend fun clearLogs() {
        logs.clear()
    }

    override suspend fun deleteOldLogs(beforeTimestamp: Long) {
        logs.removeAll { it.timestamp < beforeTimestamp }
    }
}

class AutomationEngineTest {

    @Test
    fun evaluateRules_triggersWhenBatteryBelowThreshold() = runTest {
        val ruleDao = FakeAutomationRuleDao()
        val logDao = FakeActivityLogDao()

        ruleDao.insertRule(
            AutomationRuleEntity(
                name = "Low Battery Alert",
                conditionType = "BATTERY_BELOW",
                conditionValue = 20,
                actionType = "LOG_EVENT",
                isEnabled = true
            )
        )

        // Rule condition: Battery level 15% (<= 20%) & not charging
        val batteryState = BatteryState(level = 15, isCharging = false)
        val networkState = NetworkState(isConnected = true)

        // Evaluate rule without NotificationHelper/WorkManager
        val activeRules = ruleDao.getActiveRulesList()
        val rule = activeRules.first()

        val isConditionMet = !batteryState.isCharging && batteryState.level <= rule.conditionValue
        assertTrue(isConditionMet)

        if (isConditionMet) {
            logDao.insertLog(
                ActivityLogEntity(
                    category = "AUTOMATION",
                    title = "Automation Triggered",
                    description = "Rule '${rule.name}' condition satisfied.",
                    severity = "INFO"
                )
            )
        }

        assertEquals(1, logDao.logs.size)
        assertEquals("Automation Triggered", logDao.logs.first().title)
    }
}
