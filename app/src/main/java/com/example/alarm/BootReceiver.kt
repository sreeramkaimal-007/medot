package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.DayDialDatabase
import com.example.data.model.DoseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            Log.d("BootReceiver", "Received $action. Rescheduling alarms...")
            val db = DayDialDatabase.getInstance(context)
            val scheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val endOfDay = calendar.timeInMillis

                val logs = db.doseLogDao().getDoseLogsBetweenSync(startOfDay, endOfDay)
                logs.filter {
                    (it.status == DoseStatus.UPCOMING || it.status == DoseStatus.SNOOZED || it.status == DoseStatus.DUE_NOW) &&
                    (it.snoozeUntilEpochMillis ?: it.scheduledEpochMillis) > now
                }.forEach { log ->
                    scheduler.scheduleDoseAlarm(log)
                }
            }
        }
    }
}
