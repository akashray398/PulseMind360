package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.ContextSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContextSnapshotDao {
    @Query("SELECT * FROM context_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSnapshots(limit: Int = 50): Flow<List<ContextSnapshotEntity>>

    @Query("SELECT * FROM context_snapshots ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSnapshotsList(limit: Int = 50): List<ContextSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: ContextSnapshotEntity): Long

    @Query("DELETE FROM context_snapshots WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldSnapshots(beforeTimestamp: Long)

    @Query("DELETE FROM context_snapshots")
    suspend fun clearSnapshots()
}
