package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "copilot_conversations")
data class CopilotConversationEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Device Diagnostics Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)
