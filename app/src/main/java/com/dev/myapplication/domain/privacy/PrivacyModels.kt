package com.dev.myapplication.domain.privacy

enum class RiskCategory {
    LOW,
    MEDIUM,
    HIGH
}

data class PermissionAuditItem(
    val permissionName: String,
    val isGranted: Boolean,
    val reasonRequired: String,
    val relatedFeature: String,
    val riskCategory: RiskCategory,
    val recommendedAction: String
)

data class DataInventoryItem(
    val category: String,
    val purpose: String,
    val collectionMethod: String,
    val storageLocation: String,
    val retentionPeriod: String,
    val isTransmittedToBackend: Boolean,
    val canUserDelete: Boolean
)

data class PrivacyRiskFinding(
    val severity: RiskCategory,
    val evidence: String,
    val affectedComponent: String,
    val explanation: String,
    val recommendedRemediation: String
)

data class PrivacyAuditReport(
    val permissionSummary: List<PermissionAuditItem>,
    val dataInventory: List<DataInventoryItem>,
    val privacyRisks: List<PrivacyRiskFinding>,
    val recommendedImprovements: List<String>,
    val score: Int = 95,
    val auditTimestamp: Long = System.currentTimeMillis()
)
