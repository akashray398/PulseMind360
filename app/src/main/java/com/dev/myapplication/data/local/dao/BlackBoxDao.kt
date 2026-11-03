package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.DiagnosticEventEntity
import com.dev.myapplication.data.local.entity.DiagnosticSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlackBoxDao {
    @Query("SELECT * FROM blackbox_diagnostic_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDiagnosticEvents(limit: Int = 100): Flow<List<DiagnosticEventEntity>>

    @Query("SELECT * FROM blackbox_diagnostic_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentDiagnosticEventsList(limit: Int = 100): List<DiagnosticEventEntity>

    @Query("SELECT * FROM blackbox_sessions ORDER BY startTime DESC")
    fun getAllDiagnosticSessions(): Flow<List<DiagnosticSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: DiagnosticEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DiagnosticSessionEntity)

    @Query("DELETE FROM blackbox_diagnostic_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldEvents(beforeTimestamp: Long)

    @Query("DELETE FROM blackbox_diagnostic_events")
    suspend fun clearEvents()
}
