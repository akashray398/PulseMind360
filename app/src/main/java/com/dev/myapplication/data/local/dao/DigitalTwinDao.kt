package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.DigitalTwinStateEntity
import com.dev.myapplication.data.local.entity.SimulationSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DigitalTwinDao {
    @Query("SELECT * FROM digital_twin_states WHERE isSimulation = 0 ORDER BY effectiveTimestamp DESC LIMIT :limit")
    fun getRecentStateTransitions(limit: Int = 50): Flow<List<DigitalTwinStateEntity>>

    @Query("SELECT * FROM digital_twin_states WHERE isSimulation = 0 ORDER BY effectiveTimestamp DESC LIMIT :limit")
    suspend fun getRecentStateTransitionsList(limit: Int = 50): List<DigitalTwinStateEntity>

    @Query("SELECT * FROM digital_twin_simulations ORDER BY createdAt DESC")
    fun getAllSimulationSessions(): Flow<List<SimulationSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStateTransition(state: DigitalTwinStateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulation(simulation: SimulationSessionEntity)

    @Query("DELETE FROM digital_twin_states WHERE isSimulation = 1")
    suspend fun clearSimulationStates()
}
