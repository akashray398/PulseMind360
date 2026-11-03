package com.dev.myapplication.domain.blackbox

import com.dev.myapplication.data.local.dao.BlackBoxDao
import com.dev.myapplication.data.local.entity.DiagnosticEventEntity
import com.dev.myapplication.data.local.entity.DiagnosticSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceBlackBoxEngine @Inject constructor(
    private val blackBoxDao: BlackBoxDao
) {

    suspend fun recordDiagnosticEvent(
        eventType: String,
        evidenceJson: String,
        severity: String = "INFO",
        sessionId: String = "default_session"
    ): Long {
        val now = System.currentTimeMillis()
        val entity = DiagnosticEventEntity(
            sessionId = sessionId,
            eventType = eventType,
            timestamp = now,
            evidenceJson = evidenceJson,
            severity = severity
        )
        val id = blackBoxDao.insertEvent(entity)

        // Retention Policy: Prune events older than 48 hours (now - 172_800_000L)
        blackBoxDao.deleteOldEvents(now - 172_800_000L)

        return id
    }

    suspend fun exportDiagnosticSession(reason: String = "MANUAL_EXPORT"): BlackBoxSessionReport {
        val now = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()
        val events = blackBoxDao.getRecentDiagnosticEventsList(100)

        val sessionEntity = DiagnosticSessionEntity(
            id = sessionId,
            startTime = if (events.isNotEmpty()) events.last().timestamp else now,
            endTime = now,
            triggerReason = reason,
            eventCount = events.size
        )
        blackBoxDao.insertSession(sessionEntity)

        val domainEvents = events.map { e ->
            BlackBoxEvent(
                id = e.id,
                sessionId = e.sessionId,
                eventType = e.eventType,
                timestamp = e.timestamp,
                evidenceJson = e.evidenceJson,
                severity = e.severity
            )
        }

        return BlackBoxSessionReport(
            sessionId = sessionId,
            startTime = sessionEntity.startTime,
            endTime = sessionEntity.endTime,
            triggerReason = reason,
            eventCount = events.size,
            eventsList = domainEvents
        )
    }

    fun getRecentEventsFlow(limit: Int = 100): Flow<List<DiagnosticEventEntity>> {
        return blackBoxDao.getRecentDiagnosticEvents(limit)
    }
}
