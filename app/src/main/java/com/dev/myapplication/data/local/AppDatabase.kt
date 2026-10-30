package com.dev.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.dao.FocusSessionDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.dao.SentinelBaselineDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.data.local.entity.FocusSessionEntity
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.local.entity.SentinelBaselineEntity

@Database(
    entities = [
        ActivityLogEntity::class,
        AutomationRuleEntity::class,
        FocusSessionEntity::class,
        SentinelBaselineEntity::class,
        IncidentEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun sentinelBaselineDao(): SentinelBaselineDao
    abstract fun incidentDao(): IncidentDao
}
