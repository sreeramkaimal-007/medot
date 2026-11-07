package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId AND isActive = 1")
    fun getSchedulesForMedication(medicationId: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId AND isActive = 1")
    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<Schedule>

    @Query("SELECT * FROM schedules WHERE isActive = 1 ORDER BY hour ASC, minute ASC")
    fun getAllActiveSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE isActive = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getAllActiveSchedulesSync(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<Schedule>): List<Long>

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("DELETE FROM schedules WHERE medicationId NOT IN (SELECT id FROM medications)")
    suspend fun deleteOrphanSchedules()
}
