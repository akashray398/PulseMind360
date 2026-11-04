package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.CausalRecordEntity
import com.dev.myapplication.data.local.entity.EarlyWarningEntity
import com.dev.myapplication.data.local.entity.HealingActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdiDao {
    @Query("SELECT * FROM adi_causal_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCausalRecords(limit: Int = 50): Flow<List<CausalRecordEntity>>

    @Query("SELECT * FROM adi_causal_records ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentCausalRecordsList(limit: Int = 50): List<CausalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCausalRecord(record: CausalRecordEntity)

    @Query("SELECT * FROM adi_early_warnings ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEarlyWarnings(limit: Int = 50): Flow<List<EarlyWarningEntity>>

    @Query("SELECT * FROM adi_early_warnings ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEarlyWarningsList(limit: Int = 50): List<EarlyWarningEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarlyWarning(warning: EarlyWarningEntity)

    @Query("SELECT * FROM adi_healing_actions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHealingActions(limit: Int = 50): Flow<List<HealingActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealingAction(action: HealingActionEntity)

    @Query("DELETE FROM adi_early_warnings WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldWarnings(beforeTimestamp: Long)
}
