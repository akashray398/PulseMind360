package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationRuleDao {
    @Query("SELECT * FROM automation_rules ORDER BY priority ASC, createdAt DESC")
    fun getAllRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1 ORDER BY priority ASC, createdAt DESC")
    fun getActiveRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1 ORDER BY priority ASC, createdAt DESC")
    suspend fun getActiveRulesList(): List<AutomationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutomationRuleEntity): Long

    @Update
    suspend fun updateRule(rule: AutomationRuleEntity)

    @Query("UPDATE automation_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateRuleStatus(id: Long, isEnabled: Boolean)

    @Query("UPDATE automation_rules SET lastTriggeredAt = :timestamp, triggerCount = triggerCount + 1, lastExecutionReason = :reason WHERE id = :id")
    suspend fun updateLastTriggered(id: Long, timestamp: Long, reason: String)

    @Query("UPDATE automation_rules SET lastExecutionReason = :reason WHERE id = :id")
    suspend fun updateExecutionReason(id: Long, reason: String)

    @Delete
    suspend fun deleteRule(rule: AutomationRuleEntity)
}
