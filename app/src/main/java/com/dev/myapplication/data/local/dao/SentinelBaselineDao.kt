package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.SentinelBaselineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SentinelBaselineDao {
    @Query("SELECT * FROM sentinel_baselines WHERE id = 1")
    fun getBaselineFlow(): Flow<SentinelBaselineEntity?>

    @Query("SELECT * FROM sentinel_baselines WHERE id = 1")
    suspend fun getBaseline(): SentinelBaselineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBaseline(baseline: SentinelBaselineEntity)
}
