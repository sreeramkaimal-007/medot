package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.DayDialDatabase
import com.example.data.model.DoseStatus
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val doseLogId = intent.getLongExtra(AlarmScheduler.EXTRA_DOSE_LOG_ID, -1L)
        val medId = intent.getLongExtra(AlarmScheduler.EXTRA_MED_ID, -1L)
        val medName = intent.getStringExtra(AlarmScheduler.EXTRA_MED_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(AlarmScheduler.EXTRA_DOSAGE) ?: ""
        val scheduledTime = intent.getStringExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME) ?: ""

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "medot:AlarmWakeLock"
        )
        wakeLock?.acquire(30_000L)

        // Update status in DB to DUE_NOW if still UPCOMING or SNOOZED
        if (doseLogId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = DayDialDatabase.getInstance(context)
                val log = db.doseLogDao().getDoseLogByIdSync(doseLogId)
                if (log != null && (log.status == DoseStatus.UPCOMING || log.status == DoseStatus.SNOOZED)) {
                    db.doseLogDao().updateDoseLog(log.copy(status = DoseStatus.DUE_NOW))
                }
            }
        }

        showAlarmNotification(context, doseLogId, medId, medName, dosage, scheduledTime)
    }

    private fun showAlarmNotification(
        context: Context,
        doseLogId: Long,
        medId: Long,
        medName: String,
        dosage: String,
        scheduledTime: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager)

        val prefsRepo = UserPreferencesRepository(context)
        val preferences = prefsRepo.preferences.value

        // Full Screen Intent to AlarmActivity
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_DOSE_LOG_ID, doseLogId)
            putExtra(AlarmScheduler.EXTRA_MED_ID, medId)
            putExtra(AlarmScheduler.EXTRA_MED_NAME, medName)
            putExtra(AlarmScheduler.EXTRA_DOSAGE, dosage)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            doseLogId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Taken
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKEN
            putExtra(AlarmScheduler.EXTRA_DOSE_LOG_ID, doseLogId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (doseLogId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_DOSE_LOG_ID, doseLogId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (doseLogId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (preferences.privacyMode) "Time for your scheduled reminder" else "TIME FOR YOUR MEDICATION"
        val contentText = if (preferences.privacyMode) {
            "Tap to view dose details"
        } else {
            "$medName • $dosage (Scheduled: $scheduledTime)"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$contentText\n\nTake your dose now to stay on schedule."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.checkbox_on_background, "TAKEN", takenPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "SNOOZE ${preferences.snoozeDurationMinutes}m", snoozePendingIntent)

        if (preferences.vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
        }

        if (preferences.soundEnabled) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(alarmSound)
        }

        notificationManager.notify(doseLogId.toInt(), builder.build())

        // Also launch AlarmActivity directly if screen is active or unlocked
        try {
            context.startActivity(fullScreenIntent)
        } catch (_: Exception) {
            // Android 10+ background activity start restrictions will fall back to fullScreenIntent notification
        }
    }

    private fun createNotificationChannel(context: Context, manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Medication Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Crucial reminders for daily medications"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    setSound(soundUri, audioAttributes)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "daydial_medication_alarms"
    }
}
