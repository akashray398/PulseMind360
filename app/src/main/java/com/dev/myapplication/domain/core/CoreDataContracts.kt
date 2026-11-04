package com.dev.myapplication.domain.core

import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.domain.ContextSummary
import com.dev.myapplication.domain.LiveDeviceContext
import com.dev.myapplication.domain.predictive.PredictiveOverview
import com.dev.myapplication.domain.privacy.PrivacyAuditReport

sealed class IntelligenceResult<out T> {
    data class Success<out T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) : IntelligenceResult<T>()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : IntelligenceResult<Nothing>()
}

data class UnifiedDeviceState(
    val liveContext: LiveDeviceContext,
    val summary: ContextSummary,
    val timestamp: Long = System.currentTimeMillis()
)

data class UnifiedPredictionSnapshot(
    val predictiveOverview: PredictiveOverview,
    val timestamp: Long = System.currentTimeMillis()
)

data class UnifiedIncidentReport(
    val totalIncidentsCount: Int,
    val criticalIncidentsCount: Int,
    val recentIncidents: List<IncidentEntity>,
    val timestamp: Long = System.currentTimeMillis()
)

data class UnifiedPrivacySummary(
    val auditReport: PrivacyAuditReport,
    val privacyScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// Canonical Data Contracts
data class CanonicalEvidence(
    val id: String = java.util.UUID.randomUUID().toString(),
    val metricType: String,
    val value: String,
    val unit: String = "",
    val source: String,
    val observedTimestamp: Long = System.currentTimeMillis(),
    val collectedTimestamp: Long = System.currentTimeMillis(),
    val qualityScore: Int = 100,
    val origin: String = "ON_DEVICE_TELEMETRY",
    val schemaVersion: Int = 1
)

data class CanonicalEvent(
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val eventType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sequenceNumber: Long = 0L,
    val relatedEvidenceIds: List<String> = emptyList(),
    val source: String,
    val severity: String = "INFO",
    val origin: String = "LIVE_CONTEXT_ENGINE",
    val schemaVersion: Int = 1
)

data class CanonicalDeviceState(
    val stateId: String = java.util.UUID.randomUUID().toString(),
    val stateType: String,
    val currentValue: String,
    val previousValue: String = "",
    val effectiveTimestamp: Long = System.currentTimeMillis(),
    val evidenceReferences: List<String> = emptyList(),
    val qualityScore: Int = 100,
    val confidenceScore: Int = 95
)

data class CanonicalReliabilityAssessment(
    val assessmentId: String = java.util.UUID.randomUUID().toString(),
    val metric: String,
    val evaluationVersion: String = "v1.0",
    val evaluationWindowHours: Int = 24,
    val resultScore: Int = 100,
    val coveragePct: Int = 100,
    val evidenceReferences: List<String> = emptyList(),
    val explanation: String = "All parameters nominal.",
    val unavailableInputs: List<String> = emptyList()
)

// Autonomous Device Intelligence (ADI) Contracts
data class CausalHypothesis(
    val hypothesisId: String = java.util.UUID.randomUUID().toString(),
    val problemSummary: String,
    val primaryCause: String,
    val secondaryFactors: List<String> = emptyList(),
    val confidenceScore: Int = 85,
    val evidenceChain: List<String> = emptyList(),
    val alternativeHypotheses: List<String> = emptyList(),
    val isConfirmedCausal: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class EarlyWarningRecord(
    val warningId: String = java.util.UUID.randomUUID().toString(),
    val metricType: String,
    val baselineValue: String,
    val currentTrendValue: String,
    val degradationRiskLevel: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    val evidenceWindowHours: Int = 24,
    val recommendedPrevention: String,
    val confidenceScore: Int = 90,
    val timestamp: Long = System.currentTimeMillis()
)

data class SelfHealingAction(
    val actionId: String = java.util.UUID.randomUUID().toString(),
    val actionType: String, // INFORMATIONAL, SETTINGS_NAVIGATION, SUPPORTED_APP_ACTION, RESTRICTED_AUTH
    val title: String,
    val requiredPermission: String = "",
    val expectedBenefit: String,
    val riskLevel: String = "LOW",
    val isExecutable: Boolean = true
)

data class ActionExecutionResult(
    val executionId: String = java.util.UUID.randomUUID().toString(),
    val actionId: String,
    val isSuccess: Boolean,
    val preStateSummary: String,
    val postStateSummary: String,
    val verificationStatus: String = "VERIFIED",
    val timestamp: Long = System.currentTimeMillis()
)
