package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs WHERE scheduledEpochMillis >= :startEpoch AND scheduledEpochMillis <= :endEpoch ORDER BY scheduledEpochMillis ASC")
    fun getDoseLogsBetween(startEpoch: Long, endEpoch: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE scheduledEpochMillis >= :startEpoch AND scheduledEpochMillis <= :endEpoch ORDER BY scheduledEpochMillis ASC")
    suspend fun getDoseLogsBetweenSync(startEpoch: Long, endEpoch: Long): List<DoseLog>

    @Query("SELECT * FROM dose_logs ORDER BY scheduledEpochMillis DESC")
    fun getAllDoseLogs(): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs ORDER BY scheduledEpochMillis DESC")
    suspend fun getAllDoseLogsSync(): List<DoseLog>

    @Query("SELECT * FROM dose_logs WHERE id = :id LIMIT 1")
    fun getDoseLogById(id: Long): Flow<DoseLog?>

    @Query("SELECT * FROM dose_logs WHERE id = :id LIMIT 1")
    suspend fun getDoseLogByIdSync(id: Long): DoseLog?

    @Query("SELECT * FROM dose_logs WHERE scheduleId = :scheduleId AND scheduledEpochMillis >= :startEpoch AND scheduledEpochMillis <= :endEpoch LIMIT 1")
    suspend fun getDoseLogForScheduleAtTime(scheduleId: Long, startEpoch: Long, endEpoch: Long): DoseLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(doseLog: DoseLog): Long

    @Update
    suspend fun updateDoseLog(doseLog: DoseLog)

    @Query("UPDATE dose_logs SET status = :status, actionEpochMillis = :actionEpoch, notes = :notes WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DoseStatus, actionEpoch: Long?, notes: String)

    @Query("SELECT COUNT(*) FROM dose_logs WHERE scheduledEpochMillis >= :startEpoch AND scheduledEpochMillis <= :endEpoch AND status = 'TAKEN'")
    fun countTakenBetween(startEpoch: Long, endEpoch: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM dose_logs WHERE scheduledEpochMillis >= :startEpoch AND scheduledEpochMillis <= :endEpoch AND (status = 'UPCOMING' OR status = 'DUE_NOW' OR status = 'SNOOZED')")
    fun countUpcomingBetween(startEpoch: Long, endEpoch: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM dose_logs WHERE scheduledEpochMillis >= :startEpoch AND scheduledEpochMillis <= :endEpoch AND (status = 'MISSED' OR status = 'SKIPPED')")
    fun countMissedBetween(startEpoch: Long, endEpoch: Long): Flow<Int>

    @Query("DELETE FROM dose_logs WHERE medicationName IN ('Lisinopril', 'Metformin', 'Atorvastatin')")
    suspend fun deleteSampleDoseLogs()

    @Query("DELETE FROM dose_logs")
    suspend fun deleteAllDoseLogs()
}
