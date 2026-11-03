package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.ReliabilityAssessmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReliabilityScorecardDao {
    @Query("SELECT * FROM reliability_assessments ORDER BY timestamp DESC")
    fun getAllAssessments(): Flow<List<ReliabilityAssessmentEntity>>

    @Query("SELECT * FROM reliability_assessments WHERE componentName = :component ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAssessmentForComponent(component: String): ReliabilityAssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: ReliabilityAssessmentEntity): Long

    @Query("DELETE FROM reliability_assessments WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldAssessments(beforeTimestamp: Long)
}
