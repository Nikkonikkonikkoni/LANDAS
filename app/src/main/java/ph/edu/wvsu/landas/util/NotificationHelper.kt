package ph.edu.wvsu.landas.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ph.edu.wvsu.landas.MainActivity
import ph.edu.wvsu.landas.R
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AlertSeverity

class NotificationHelper(private val context: Context) {
    companion object {
        const val EMERGENCY_CHANNEL = "landas_emergency_alerts"
        const val GENERAL_CHANNEL = "landas_general"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val emergency = NotificationChannel(
                EMERGENCY_CHANNEL,
                "Emergency alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical LANDAS campus alerts, drills, and all-clear notices."
                enableVibration(true)
            }
            val general = NotificationChannel(
                GENERAL_CHANNEL,
                "Campus announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Advisories and campus announcements."
            }
            manager.createNotificationChannel(emergency)
            manager.createNotificationChannel(general)
        }
    }

    fun canNotify(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showAlert(alert: AlertEntity) {
        if (!canNotify()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alert_id", alert.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val channel = if (alert.severity == AlertSeverity.CRITICAL || alert.severity == AlertSeverity.WARNING) {
            EMERGENCY_CHANNEL
        } else {
            GENERAL_CHANNEL
        }
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_landas)
            .setContentTitle(alert.title)
            .setContentText(alert.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(alert.id.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission may have been revoked while the app was running.
        }
    }

    fun showLocalMessage(title: String, body: String) {
        if (!canNotify()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            4_001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, GENERAL_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_landas)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(4_001, notification)
        } catch (_: SecurityException) {
        }
    }
}
