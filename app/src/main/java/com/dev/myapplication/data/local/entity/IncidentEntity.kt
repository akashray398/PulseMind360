package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "correlated_incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val severity: String = "WARNING", // INFO, WARNING, CRITICAL
    val status: String = "DETECTED", // DETECTED, INVESTIGATING, MONITORING, RESOLVED, DISMISSED
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val eventCount: Int = 1,
    val correlatedEventsSummary: String,
    val supportingEvidence: String,
    val correlationExplanation: String,
    val rankedExplanations: String = "",
    val recommendedChecks: String,
    val resolutionNotes: String = "",
    val confidenceScore: Int = 92,
    val createdAt: Long = System.currentTimeMillis()
)
