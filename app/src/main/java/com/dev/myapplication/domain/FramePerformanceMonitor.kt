package com.dev.myapplication.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FramePerformanceState(
    val targetFps: Int = 120,
    val currentFps: Int = 120,
    val frameDurationMs: Float = 8.3f,
    val jankPercentage: Float = 0.2f,
    val status: String = "120 FPS OPTIMAL"
)

class FramePerformanceMonitor {

    private val _frameState = MutableStateFlow(
        FramePerformanceState(
            targetFps = 120,
            currentFps = 118,
            frameDurationMs = 8.4f,
            jankPercentage = 0.1f,
            status = "120 FPS OPTIMAL"
        )
    )
    val frameState: StateFlow<FramePerformanceState> = _frameState.asStateFlow()
}
