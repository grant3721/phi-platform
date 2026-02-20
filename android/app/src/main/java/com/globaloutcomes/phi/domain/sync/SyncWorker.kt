package com.globaloutcomes.phi.domain.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background worker for periodic data synchronization
 * Runs every 15 minutes when network is available
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Perform sync
            val result = syncManager.performSync()

            if (result.isSuccess) {
                // Sync successful - reset retry count
                Result.success()
            } else {
                // Sync failed - retry with exponential backoff
                val runAttemptCount = runAttemptCount
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    // Max retries exceeded
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            // Exception during sync
            val runAttemptCount = runAttemptCount
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "sync_work"
        private const val MAX_RETRIES = 3
        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val INITIAL_BACKOFF_SECONDS = 30L

        /**
         * Schedule periodic sync work
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = SYNC_INTERVAL_MINUTES,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    backoffDelay = INITIAL_BACKOFF_SECONDS,
                    timeUnit = TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        }

        /**
         * Cancel periodic sync work
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Trigger immediate one-time sync
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }

        /**
         * Get sync work info as Flow
         */
        fun getWorkInfoFlow(context: Context) =
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(WORK_NAME)
    }
}
