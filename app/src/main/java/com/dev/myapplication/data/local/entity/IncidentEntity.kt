package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "correlated_incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val severity: String = "WARNING", // INFO, WARNING, CRITICAL
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val eventCount: Int = 1,
    val correlatedEventsSummary: String,
    val supportingEvidence: String,
    val correlationExplanation: String,
    val recommendedChecks: String,
    val createdAt: Long = System.currentTimeMillis()
)
