package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleDoseAlarm(doseLog: DoseLog) {
        if (alarmManager == null) return

        val triggerTime = doseLog.snoozeUntilEpochMillis ?: doseLog.scheduledEpochMillis
        // If trigger time is in the past by more than 2 minutes and not snoozed, do not fire an immediate old alarm
        val now = System.currentTimeMillis()
        if (triggerTime < now - 120_000L && doseLog.status != DoseStatus.SNOOZED) {
            Log.d(TAG, "Skipping past dose ${doseLog.id}")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE_DOSE_ALARM
            putExtra(EXTRA_DOSE_LOG_ID, doseLog.id)
            putExtra(EXTRA_MED_ID, doseLog.medicationId)
            putExtra(EXTRA_MED_NAME, doseLog.medicationName)
            putExtra(EXTRA_DOSAGE, doseLog.dosage)
            putExtra(EXTRA_SCHEDULED_TIME, doseLog.formattedTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseLog.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            doseLog.id.toInt() + 100_000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    Log.d(TAG, "Scheduled AlarmClock for dose ${doseLog.id} at $triggerTime")
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    Log.d(TAG, "Scheduled setAndAllowWhileIdle fallback for dose ${doseLog.id}")
                }
            } else {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.d(TAG, "Scheduled AlarmClock for dose ${doseLog.id} at $triggerTime")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm", e)
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed to schedule fallback alarm", fallbackEx)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    fun cancelDoseAlarm(doseLogId: Long) {
        if (alarmManager == null) return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE_DOSE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseLogId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for dose $doseLogId")
        }
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        const val ACTION_FIRE_DOSE_ALARM = "com.example.daydial.ACTION_FIRE_DOSE_ALARM"
        const val EXTRA_DOSE_LOG_ID = "extra_dose_log_id"
        const val EXTRA_MED_ID = "extra_med_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
    }
}
