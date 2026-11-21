package ph.edu.wvsu.landas.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import ph.edu.wvsu.landas.LandasApplication
import ph.edu.wvsu.landas.data.AlertSeverity

/**
 * Schedules a single campus drill through WorkManager. When the request fires it
 * publishes a drill alert to the local Room database and raises a notification.
 * WorkManager timings are inexact and may be delayed by battery optimizations.
 */
object DrillScheduler {
    const val DEFAULT_TITLE = "Scheduled campus drill — participate now"
    const val DEFAULT_BODY =
        "This is a scheduled earthquake drill. Practice drop, cover, and hold, then evacuate using the LANDAS map to your assigned assembly area. Mark yourself safe to help the campus validate its accountability process."

    private const val PREFS = "landas_drill_schedule"
    private const val KEY_AT = "scheduled_at"
    private const val WORK_NAME = "landas_scheduled_drill"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun schedule(context: Context, triggerAtMillis: Long) {
        val delay = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(1_000L)
        val request = OneTimeWorkRequestBuilder<DrillWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "title" to DEFAULT_TITLE,
                    "body" to DEFAULT_BODY
                )
            )
            .addTag(WORK_NAME)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        prefs(context).edit().putLong(KEY_AT, triggerAtMillis).apply()
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        clear(context)
    }

    fun scheduledAt(context: Context): Long? {
        val value = prefs(context).getLong(KEY_AT, 0L)
        return value.takeIf { it > 0L }
    }

    internal fun clear(context: Context) {
        prefs(context).edit().remove(KEY_AT).apply()
    }
}

class DrillWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? LandasApplication ?: return Result.failure()
        val title = inputData.getString("title") ?: DrillScheduler.DEFAULT_TITLE
        val body = inputData.getString("body") ?: DrillScheduler.DEFAULT_BODY
        return try {
            val alert = app.container.repository.publishAlert(
                type = "DRILL",
                title = title,
                body = body,
                severity = AlertSeverity.INFO,
                activate = true
            )
            app.container.notificationHelper.showAlert(alert)
            DrillScheduler.clear(applicationContext)
            Result.success()
        } catch (error: Exception) {
            Result.retry()
        }
    }
}
