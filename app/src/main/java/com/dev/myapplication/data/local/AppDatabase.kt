package com.dev.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.dao.FocusSessionDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.data.local.entity.FocusSessionEntity

@Database(
    entities = [
        ActivityLogEntity::class,
        AutomationRuleEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun focusSessionDao(): FocusSessionDao
}
