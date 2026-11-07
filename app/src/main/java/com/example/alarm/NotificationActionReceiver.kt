package com.example.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.DayDialDatabase
import com.example.data.model.DoseStatus
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val doseLogId = intent.getLongExtra(AlarmScheduler.EXTRA_DOSE_LOG_ID, -1L)
        if (doseLogId <= 0) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(doseLogId.toInt())

        val db = DayDialDatabase.getInstance(context)
        val prefsRepo = UserPreferencesRepository(context)
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            val doseLog = db.doseLogDao().getDoseLogByIdSync(doseLogId) ?: return@launch

            when (intent.action) {
                ACTION_TAKEN -> {
                    val updated = doseLog.copy(
                        status = DoseStatus.TAKEN,
                        actionEpochMillis = System.currentTimeMillis()
                    )
                    db.doseLogDao().updateDoseLog(updated)
                    scheduler.cancelDoseAlarm(doseLogId)
                }
                ACTION_SNOOZE -> {
                    val snoozeMinutes = prefsRepo.preferences.value.snoozeDurationMinutes
                    val snoozeEpoch = System.currentTimeMillis() + (snoozeMinutes * 60_000L)
                    val updated = doseLog.copy(
                        status = DoseStatus.SNOOZED,
                        snoozeUntilEpochMillis = snoozeEpoch
                    )
                    db.doseLogDao().updateDoseLog(updated)
                    scheduler.scheduleDoseAlarm(updated)
                }
            }
        }
    }

    companion object {
        const val ACTION_TAKEN = "com.example.daydial.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.example.daydial.ACTION_SNOOZE"
    }
}
