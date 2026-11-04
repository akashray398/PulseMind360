package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adi_causal_records")
data class CausalRecordEntity(
    @PrimaryKey
    val hypothesisId: String = java.util.UUID.randomUUID().toString(),
    val problemSummary: String,
    val primaryCause: String,
    val secondaryFactorsJson: String = "[]",
    val confidenceScore: Int = 85,
    val evidenceChainJson: String = "[]",
    val isConfirmedCausal: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "adi_early_warnings")
data class EarlyWarningEntity(
    @PrimaryKey
    val warningId: String = java.util.UUID.randomUUID().toString(),
    val metricType: String,
    val baselineValue: String,
    val currentTrendValue: String,
    val degradationRiskLevel: String = "MEDIUM",
    val recommendedPrevention: String,
    val confidenceScore: Int = 90,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "adi_healing_actions")
data class HealingActionEntity(
    @PrimaryKey
    val executionId: String = java.util.UUID.randomUUID().toString(),
    val actionId: String,
    val actionType: String,
    val isSuccess: Boolean,
    val preStateSummary: String,
    val postStateSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)
