package com.dev.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.AdiDao
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.dao.BlackBoxDao
import com.dev.myapplication.data.local.dao.ContextSnapshotDao
import com.dev.myapplication.data.local.dao.CopilotConversationDao
import com.dev.myapplication.data.local.dao.DigitalTwinDao
import com.dev.myapplication.data.local.dao.FocusSessionDao
import com.dev.myapplication.data.local.dao.IncidentDao
import com.dev.myapplication.data.local.dao.ReliabilityScorecardDao
import com.dev.myapplication.data.local.dao.SentinelBaselineDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.data.local.entity.AutomationRuleEntity
import com.dev.myapplication.data.local.entity.CausalRecordEntity
import com.dev.myapplication.data.local.entity.ContextSnapshotEntity
import com.dev.myapplication.data.local.entity.CopilotConversationEntity
import com.dev.myapplication.data.local.entity.CopilotMessageEntity
import com.dev.myapplication.data.local.entity.DiagnosticEventEntity
import com.dev.myapplication.data.local.entity.DiagnosticSessionEntity
import com.dev.myapplication.data.local.entity.DigitalTwinStateEntity
import com.dev.myapplication.data.local.entity.EarlyWarningEntity
import com.dev.myapplication.data.local.entity.FocusSessionEntity
import com.dev.myapplication.data.local.entity.HealingActionEntity
import com.dev.myapplication.data.local.entity.IncidentEntity
import com.dev.myapplication.data.local.entity.ReliabilityAssessmentEntity
import com.dev.myapplication.data.local.entity.SentinelBaselineEntity
import com.dev.myapplication.data.local.entity.SimulationSessionEntity

@Database(
    entities = [
        ActivityLogEntity::class,
        AutomationRuleEntity::class,
        FocusSessionEntity::class,
        SentinelBaselineEntity::class,
        IncidentEntity::class,
        ContextSnapshotEntity::class,
        CopilotConversationEntity::class,
        CopilotMessageEntity::class,
        DiagnosticEventEntity::class,
        DiagnosticSessionEntity::class,
        ReliabilityAssessmentEntity::class,
        DigitalTwinStateEntity::class,
        SimulationSessionEntity::class,
        CausalRecordEntity::class,
        EarlyWarningEntity::class,
        HealingActionEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun sentinelBaselineDao(): SentinelBaselineDao
    abstract fun incidentDao(): IncidentDao
    abstract fun contextSnapshotDao(): ContextSnapshotDao
    abstract fun copilotConversationDao(): CopilotConversationDao
    abstract fun blackBoxDao(): BlackBoxDao
    abstract fun reliabilityScorecardDao(): ReliabilityScorecardDao
    abstract fun digitalTwinDao(): DigitalTwinDao
    abstract fun adiDao(): AdiDao
}
