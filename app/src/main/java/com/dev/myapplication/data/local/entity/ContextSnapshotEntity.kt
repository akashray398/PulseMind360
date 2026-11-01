package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "context_snapshots")
data class ContextSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val batteryTemp: Float = 0f,
    val usedMemMb: Long = 0,
    val totalMemMb: Long = 0,
    val networkTransport: String = "NONE",
    val linkDownstreamKbps: Int = 0,
    val stateClassification: String = "OPTIMAL",
    val anomalySeverity: String = "NOMINAL",
    val confidenceScore: Int = 92
)
