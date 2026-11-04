package com.dev.myapplication.domain.warning

import com.dev.myapplication.domain.core.EarlyWarningRecord

data class EarlyWarningAnalysisResult(
    val warnings: List<EarlyWarningRecord>,
    val baselineStatusText: String,
    val timestamp: Long = System.currentTimeMillis()
)
