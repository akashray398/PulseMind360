package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // BATTERY, NETWORK, AUTOMATION, WORKER, FOCUS
    val title: String,
    val description: String,
    val severity: String = "INFO" // INFO, WARNING, ALERT
)
