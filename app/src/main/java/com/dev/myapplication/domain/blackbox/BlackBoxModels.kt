package com.dev.myapplication.domain.blackbox

data class BlackBoxEvent(
    val id: Long,
    val sessionId: String,
    val eventType: String,
    val timestamp: Long,
    val evidenceJson: String,
    val severity: String
)

data class BlackBoxSessionReport(
    val sessionId: String,
    val startTime: Long,
    val endTime: Long,
    val triggerReason: String,
    val eventCount: Int,
    val eventsList: List<BlackBoxEvent>
)
