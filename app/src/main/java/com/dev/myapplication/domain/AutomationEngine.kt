package com.dev.myapplication.domain

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.data.worker.DeviceSyncWorker
import com.dev.myapplication.util.NotificationHelper

class AutomationEngine(
    private val automationRuleDao: AutomationRuleDao,
    private val activityLogDao: ActivityLogDao,
    private val notificationHelper: NotificationHelper,
    private val workManager: WorkManager
) {

    private val minCooldownMs = 60_000L // Cooldown for custom rules
    private val alarmCooldownMs = 300_000L // 5 min cooldown for system alerts

    private var lastGuardNotifiedAt = 0L
    private var lastOverheatNotifiedAt = 0L

    suspend fun evaluateRules(
        batteryState: BatteryState,
        networkState: NetworkState
    ) {
        val currentTime = System.currentTimeMillis()

        // 1. Evaluate Smart Charge Lifespan Guard (80% Alarm)
        if (batteryState.isCharging && batteryState.level >= 80) {
            if (currentTime - lastGuardNotifiedAt >= alarmCooldownMs) {
                lastGuardNotifiedAt = currentTime
                notificationHelper.showSmartChargeGuardNotification(batteryState.level)
                activityLogDao.insertLog(
                    ActivityLogEntity(
                        category = "BATTERY_GUARD",
                        title = "80% Smart Charge Guard",
                        description = "Battery reached ${batteryState.level}% while plugged in. Unplug to extend battery lifespan.",
                        severity = "INFO"
                    )
                )
            }
        }

        // 2. Evaluate Overheat Thermal Alarm (40°C Alarm)
        if (batteryState.temperature >= 40.0f) {
            if (currentTime - lastOverheatNotifiedAt >= alarmCooldownMs) {
                lastOverheatNotifiedAt = currentTime
                notificationHelper.showThermalOverheatNotification(batteryState.temperature)
                activityLogDao.insertLog(
                    ActivityLogEntity(
                        category = "THERMAL_ALARM",
                        title = "Thermal Overheat Alert",
                        description = "Battery temperature reached ${batteryState.temperature}°C. Heavy background tasks throttled.",
                        severity = "ALERT"
                    )
                )
            }
        }

        // 3. Evaluate Active User Rules
        val activeRules = automationRuleDao.getActiveRulesList()
        for (rule in activeRules) {
            if (currentTime - rule.lastTriggeredAt < minCooldownMs) {
                continue // Skip rules in cooldown
            }

            val isConditionMet = checkCondition(rule, batteryState, networkState)
            if (isConditionMet) {
                executeRule(rule)
                automationRuleDao.updateLastTriggered(rule.id, currentTime)
            }
        }
    }

    private fun checkCondition(
        rule: AutomationRuleEntity,
        batteryState: BatteryState,
        networkState: NetworkState
    ): Boolean {
        return when (rule.conditionType) {
            "BATTERY_BELOW" -> !batteryState.isCharging && batteryState.level <= rule.conditionValue
            "CHARGING_STATE_CHANGED" -> batteryState.isCharging
            "NETWORK_DISCONNECTED" -> !networkState.isConnected
            "NETWORK_CONNECTED" -> networkState.isConnected
            else -> false
        }
    }

    private suspend fun executeRule(rule: AutomationRuleEntity) {
        val logDetails = "Automation '${rule.name}' triggered action: ${rule.actionType}"

        when (rule.actionType) {
            "SHOW_NOTIFICATION" -> {
                val msg = if (rule.actionTarget.isNotBlank()) rule.actionTarget else "Rule '${rule.name}' condition satisfied."
                notificationHelper.showNotification(
                    id = rule.id.toInt(),
                    channelId = NotificationHelper.CHANNEL_ALERTS,
                    title = "Automation: ${rule.name}",
                    message = msg
                )
            }
            "TRIGGER_SYNC_WORKER" -> {
                val request = OneTimeWorkRequestBuilder<DeviceSyncWorker>().build()
                workManager.enqueue(request)
            }
            "LOG_EVENT" -> {
                // Handled in activity log below
            }
        }

        activityLogDao.insertLog(
            ActivityLogEntity(
                category = "AUTOMATION",
                title = "Automation Triggered",
                description = logDetails,
                severity = "INFO"
            )
        )
    }
}
