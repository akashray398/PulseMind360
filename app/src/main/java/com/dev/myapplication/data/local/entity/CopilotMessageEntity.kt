package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "copilot_messages")
data class CopilotMessageEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val conversationId: String,
    val sender: String, // "USER" or "AI"
    val timestamp: Long = System.currentTimeMillis(),
    val queryText: String = "",
    val intent: String = "UNKNOWN",
    val severity: String = "INFO",
    val confidenceScore: Int = 90,
    val summary: String = "",
    val factsJson: String = "[]",
    val evidenceText: String = "",
    val actionsJson: String = "[]",
    val metricsJson: String = "{}",
    val followUpsJson: String = "[]",
    val userFeedback: String = "NONE" // "NONE", "THUMBS_UP", "THUMBS_DOWN"
)
