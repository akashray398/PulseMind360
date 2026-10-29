package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val conditionType: String, // BATTERY_BELOW, CHARGING_STATE_CHANGED, NETWORK_DISCONNECTED, NETWORK_CONNECTED
    val conditionValue: Int = 20,
    val secondaryConditionType: String = "",
    val secondaryConditionValue: Int = 0,
    val logicalOperator: String = "NONE", // NONE, AND, OR
    val actionType: String, // SHOW_NOTIFICATION, LOG_EVENT, TRIGGER_SYNC_WORKER
    val actionTarget: String = "",
    val cooldownMinutes: Int = 5,
    val priority: Int = 2, // 1 = HIGH, 2 = MEDIUM, 3 = LOW
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long = 0L,
    val triggerCount: Int = 0,
    val lastExecutionReason: String = "Rule ready for evaluation"
)
