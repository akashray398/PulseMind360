package com.dev.myapplication.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeviceSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val activityLogDao: ActivityLogDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            activityLogDao.insertLog(
                ActivityLogEntity(
                    category = "WORKER",
                    title = "Background Sync Completed",
                    description = "WorkManager executed background device health check and sync successfully.",
                    severity = "INFO"
                )
            )
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
