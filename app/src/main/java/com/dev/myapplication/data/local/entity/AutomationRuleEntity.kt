package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val conditionType: String, // BATTERY_BELOW, CHARGING_STATE_CHANGED, NETWORK_DISCONNECTED, NETWORK_CONNECTED
    val conditionValue: Int = 20, // e.g., battery percentage or 1/0 for state
    val actionType: String, // SHOW_NOTIFICATION, LOG_EVENT, TRIGGER_SYNC_WORKER
    val actionTarget: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long = 0L,
    val triggerCount: Int = 0
)
