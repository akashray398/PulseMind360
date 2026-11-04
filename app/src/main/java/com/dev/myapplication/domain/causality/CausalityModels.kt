package com.dev.myapplication.domain.causality

import com.dev.myapplication.domain.core.CausalHypothesis

data class CausalityAnalysisResult(
    val hypotheses: List<CausalHypothesis>,
    val totalEvidenceAnalyzed: Int,
    val timestamp: Long = System.currentTimeMillis()
)
