package com.example.data.repository

import android.content.Context
import com.example.alarm.AlarmScheduler
import com.example.data.local.DayDialDatabase
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.FoodInstruction
import com.example.data.model.Medication
import com.example.data.model.MedicationType
import com.example.data.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class MedicationRepository(
    private val database: DayDialDatabase,
    private val scheduler: AlarmScheduler
) {
    val activeMedications: Flow<List<Medication>> = database.medicationDao().getActiveMedications()
    val allSchedules: Flow<List<Schedule>> = database.scheduleDao().getAllActiveSchedules()
    val allDoseLogs: Flow<List<DoseLog>> = database.doseLogDao().getAllDoseLogs()

    fun getTodayDoseLogs(): Flow<List<DoseLog>> {
        val (start, end) = getTodayBounds()
        return database.doseLogDao().getDoseLogsBetween(start, end)
    }

    fun getTodayTakenCount(): Flow<Int> {
        val (start, end) = getTodayBounds()
        return database.doseLogDao().countTakenBetween(start, end)
    }

    fun getTodayUpcomingCount(): Flow<Int> {
        val (start, end) = getTodayBounds()
        return database.doseLogDao().countUpcomingBetween(start, end)
    }

    fun getTodayMissedCount(): Flow<Int> {
        val (start, end) = getTodayBounds()
        return database.doseLogDao().countMissedBetween(start, end)
    }

    suspend fun addMedicationWithSchedules(
        medication: Medication,
        times: List<Pair<Int, Int>> // List of (hour, minute)
    ): Long {
        val medId = database.medicationDao().insertMedication(medication)
        val schedules = times.map { (hour, minute) ->
            Schedule(
                medicationId = medId,
                hour = hour,
                minute = minute,
                daysOfWeekMask = 127,
                startDate = System.currentTimeMillis()
            )
        }
        val scheduleIds = database.scheduleDao().insertSchedules(schedules)

        // Generate today's DoseLogs for these schedules
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        schedules.forEachIndexed { index, schedule ->
            val scheduleId = scheduleIds.getOrNull(index) ?: 0L
            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, schedule.hour)
            calendar.set(Calendar.MINUTE, schedule.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val scheduledTime = calendar.timeInMillis
            val initialStatus = if (scheduledTime < now - 30 * 60_000L) {
                DoseStatus.MISSED
            } else if (scheduledTime <= now + 15 * 60_000L && scheduledTime >= now - 15 * 60_000L) {
                DoseStatus.DUE_NOW
            } else {
                DoseStatus.UPCOMING
            }

            val doseLog = DoseLog(
                medicationId = medId,
                scheduleId = scheduleId,
                medicationName = medication.name,
                dosage = medication.dosage,
                scheduledEpochMillis = scheduledTime,
                scheduledHour = schedule.hour,
                scheduledMinute = schedule.minute,
                status = initialStatus
            )
            val logId = database.doseLogDao().insertDoseLog(doseLog)
            if (scheduledTime >= now - 60_000L) {
                scheduler.scheduleDoseAlarm(doseLog.copy(id = logId))
            }
        }

        return medId
    }

    suspend fun markDoseTaken(doseLogId: Long) {
        val log = database.doseLogDao().getDoseLogByIdSync(doseLogId) ?: return
        val updated = log.copy(
            status = DoseStatus.TAKEN,
            actionEpochMillis = System.currentTimeMillis()
        )
        database.doseLogDao().updateDoseLog(updated)
        scheduler.cancelDoseAlarm(doseLogId)
    }

    suspend fun snoozeDose(doseLogId: Long, snoozeMinutes: Int) {
        val log = database.doseLogDao().getDoseLogByIdSync(doseLogId) ?: return
        val snoozeEpoch = System.currentTimeMillis() + (snoozeMinutes * 60_000L)
        val updated = log.copy(
            status = DoseStatus.SNOOZED,
            snoozeUntilEpochMillis = snoozeEpoch
        )
        database.doseLogDao().updateDoseLog(updated)
        scheduler.scheduleDoseAlarm(updated)
    }

    suspend fun skipDose(doseLogId: Long, reason: String = "Skipped by user") {
        val log = database.doseLogDao().getDoseLogByIdSync(doseLogId) ?: return
        val updated = log.copy(
            status = DoseStatus.SKIPPED,
            actionEpochMillis = System.currentTimeMillis(),
            notes = reason
        )
        database.doseLogDao().updateDoseLog(updated)
        scheduler.cancelDoseAlarm(doseLogId)
    }

    suspend fun deleteMedication(medicationId: Long) {
        database.medicationDao().deleteMedicationById(medicationId)
    }

    suspend fun purgeSampleDataIfPresent() {
        database.medicationDao().deleteSampleMedications()
        database.doseLogDao().deleteSampleDoseLogs()
        database.scheduleDao().deleteOrphanSchedules()
        ensureTodayDoseLogs()
    }

    suspend fun ensureTodayDoseLogs() {
        val schedules = database.scheduleDao().getAllActiveSchedulesSync()
        val (startOfDay, endOfDay) = getTodayBounds()
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        for (schedule in schedules) {
            val existing = database.doseLogDao().getDoseLogForScheduleAtTime(schedule.id, startOfDay, endOfDay)
            if (existing == null) {
                val med = database.medicationDao().getMedicationByIdSync(schedule.medicationId) ?: continue
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, schedule.hour)
                calendar.set(Calendar.MINUTE, schedule.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val scheduledTime = calendar.timeInMillis

                val status = if (scheduledTime < now - 45 * 60_000L) {
                    DoseStatus.MISSED
                } else if (scheduledTime <= now + 15 * 60_000L && scheduledTime >= now - 15 * 60_000L) {
                    DoseStatus.DUE_NOW
                } else {
                    DoseStatus.UPCOMING
                }

                val log = DoseLog(
                    medicationId = med.id,
                    scheduleId = schedule.id,
                    medicationName = med.name,
                    dosage = med.dosage,
                    scheduledEpochMillis = scheduledTime,
                    scheduledHour = schedule.hour,
                    scheduledMinute = schedule.minute,
                    status = status
                )
                val logId = database.doseLogDao().insertDoseLog(log)
                if (scheduledTime > now) {
                    scheduler.scheduleDoseAlarm(log.copy(id = logId))
                }
            }
        }
    }

    fun exportHistoryAsCsv(logs: List<DoseLog>): String {
        val builder = java.lang.StringBuilder()
        builder.append("Medication Name,Dosage,Scheduled Time,Action Status,Confirmed Time,Notes\n")
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())

        for (log in logs) {
            val scheduledDateStr = dateFormat.format(java.util.Date(log.scheduledEpochMillis))
            val actionDateStr = log.actionEpochMillis?.let { dateFormat.format(java.util.Date(it)) } ?: "N/A"
            builder.append("\"${log.medicationName}\",")
            builder.append("\"${log.dosage}\",")
            builder.append("\"$scheduledDateStr\",")
            builder.append("\"${log.status.displayName}\",")
            builder.append("\"$actionDateStr\",")
            builder.append("\"${log.notes.replace("\"", "\"\"")}\"\n")
        }
        return builder.toString()
    }

    private fun getTodayBounds(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis - 1
        return Pair(start, end)
    }

    companion object {
        @Volatile
        private var INSTANCE: MedicationRepository? = null

        fun getInstance(context: Context): MedicationRepository {
            return INSTANCE ?: synchronized(this) {
                val db = DayDialDatabase.getInstance(context)
                val scheduler = AlarmScheduler(context)
                val instance = MedicationRepository(db, scheduler)
                INSTANCE = instance
                instance
            }
        }
    }
}
