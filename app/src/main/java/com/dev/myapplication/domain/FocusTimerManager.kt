package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.FocusSessionDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.data.local.entity.FocusSessionEntity
import com.dev.myapplication.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusTimerState(
    val totalSeconds: Int = 1500,
    val remainingSeconds: Int = 1500,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val currentPresetLabel: String = "Pomodoro (25m)"
)

class FocusTimerManager(
    private val focusSessionDao: FocusSessionDao,
    private val activityLogDao: ActivityLogDao,
    private val notificationHelper: NotificationHelper
) {

    private val _timerState = MutableStateFlow(FocusTimerState())
    val timerState: StateFlow<FocusTimerState> = _timerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null

    fun selectPreset(durationMinutes: Int, label: String) {
        timerJob?.cancel()
        val seconds = durationMinutes * 60
        _timerState.value = FocusTimerState(
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = false,
            isPaused = false,
            isCompleted = false,
            currentPresetLabel = label
        )
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return

        _timerState.value = _timerState.value.copy(
            isRunning = true,
            isPaused = false,
            isCompleted = false
        )

        timerJob?.cancel()
        timerJob = scope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000L)
                if (!_timerState.value.isPaused) {
                    val newRemaining = _timerState.value.remainingSeconds - 1
                    if (newRemaining <= 0) {
                        onTimerComplete()
                    } else {
                        _timerState.value = _timerState.value.copy(remainingSeconds = newRemaining)
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        if (!_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isPaused = true)
    }

    fun resumeTimer() {
        if (!_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isPaused = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val total = _timerState.value.totalSeconds
        _timerState.value = _timerState.value.copy(
            remainingSeconds = total,
            isRunning = false,
            isPaused = false,
            isCompleted = false
        )
    }

    private suspend fun onTimerComplete() {
        val currentState = _timerState.value
        val minutes = currentState.totalSeconds / 60

        _timerState.value = currentState.copy(
            remainingSeconds = 0,
            isRunning = false,
            isPaused = false,
            isCompleted = true
        )

        focusSessionDao.insertSession(
            FocusSessionEntity(
                durationMinutes = minutes,
                completed = true,
                label = currentState.currentPresetLabel
            )
        )

        activityLogDao.insertLog(
            ActivityLogEntity(
                category = "FOCUS",
                title = "Focus Session Completed",
                description = "Completed '${currentState.currentPresetLabel}' focus session (${minutes} minutes).",
                severity = "INFO"
            )
        )

        notificationHelper.showNotification(
            id = 999,
            channelId = NotificationHelper.CHANNEL_FOCUS,
            title = "Focus Timer Complete!",
            message = "Great job! You finished your '${currentState.currentPresetLabel}' focus session."
        )
    }
}
