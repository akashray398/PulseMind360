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
