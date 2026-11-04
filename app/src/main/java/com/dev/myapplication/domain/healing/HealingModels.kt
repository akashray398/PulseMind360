package com.dev.myapplication.domain.healing

import com.dev.myapplication.domain.core.SelfHealingAction

data class SelfHealingOverview(
    val availableActions: List<SelfHealingAction>,
    val systemReadinessText: String,
    val timestamp: Long = System.currentTimeMillis()
)
