package com.dev.myapplication.domain

import com.dev.myapplication.data.local.dao.FocusSessionDao
import com.dev.myapplication.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FakeFocusSessionDao : FocusSessionDao {
    val sessions = mutableListOf<FocusSessionEntity>()

    override fun getAllSessions(): Flow<List<FocusSessionEntity>> = MutableStateFlow(sessions)

    override suspend fun insertSession(session: FocusSessionEntity) {
        sessions.add(session)
    }

    override suspend fun clearSessions() {
        sessions.clear()
    }
}

class FocusTimerManagerTest {

    @Test
    fun selectPreset_updatesTimerState() {
        val timerState = FocusTimerState(
            totalSeconds = 900,
            remainingSeconds = 900,
            isRunning = false,
            currentPresetLabel = "Short Focus (15m)"
        )

        assertEquals(900, timerState.totalSeconds)
        assertEquals(900, timerState.remainingSeconds)
        assertFalse(timerState.isRunning)
        assertEquals("Short Focus (15m)", timerState.currentPresetLabel)
    }
}
