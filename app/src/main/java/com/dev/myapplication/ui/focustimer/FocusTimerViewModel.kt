package com.dev.myapplication.ui.focustimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.dao.FocusSessionDao
import com.dev.myapplication.data.local.entity.FocusSessionEntity
import com.dev.myapplication.domain.FocusTimerManager
import com.dev.myapplication.domain.FocusTimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FocusTimerViewModel @Inject constructor(
    private val focusTimerManager: FocusTimerManager,
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {

    val timerState: StateFlow<FocusTimerState> = focusTimerManager.timerState

    val sessionsState: StateFlow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectPreset(durationMinutes: Int, label: String) {
        focusTimerManager.selectPreset(durationMinutes, label)
    }

    fun startTimer() {
        focusTimerManager.startTimer()
    }

    fun pauseTimer() {
        focusTimerManager.pauseTimer()
    }

    fun resumeTimer() {
        focusTimerManager.resumeTimer()
    }

    fun resetTimer() {
        focusTimerManager.resetTimer()
    }
}
