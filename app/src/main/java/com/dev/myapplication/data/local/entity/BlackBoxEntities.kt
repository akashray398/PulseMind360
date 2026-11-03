package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blackbox_diagnostic_events")
data class DiagnosticEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String = "default_session",
    val eventType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val evidenceJson: String = "{}",
    val severity: String = "INFO"
)

@Entity(tableName = "blackbox_sessions")
data class DiagnosticSessionEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val triggerReason: String = "MANUAL_EXPORT",
    val eventCount: Int = 0
)
