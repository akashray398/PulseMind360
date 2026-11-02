package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM correlated_incidents ORDER BY startTime DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM correlated_incidents WHERE id = :id")
    suspend fun getIncidentById(id: Long): IncidentEntity?

    @Query("SELECT * FROM correlated_incidents WHERE title = :title AND startTime >= :minStartTime ORDER BY startTime DESC LIMIT 1")
    suspend fun getRecentIncidentByTitle(title: String, minStartTime: Long): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Query("UPDATE correlated_incidents SET status = :status, resolutionNotes = :notes WHERE id = :id")
    suspend fun updateIncidentStatus(id: Long, status: String, notes: String)

    @Query("DELETE FROM correlated_incidents")
    suspend fun clearIncidents()

    @Delete
    suspend fun deleteIncident(incident: IncidentEntity)
}
