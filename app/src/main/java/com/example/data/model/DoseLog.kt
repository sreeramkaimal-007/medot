package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    indices = [Index("scheduledEpochMillis"), Index("medicationId"), Index("status")]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduledEpochMillis: Long,
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val actionEpochMillis: Long? = null,
    val status: DoseStatus = DoseStatus.UPCOMING,
    val snoozeUntilEpochMillis: Long? = null,
    val notes: String = ""
) {
    val isAM: Boolean get() = scheduledHour < 12
    val displayHour: Int get() = when {
        scheduledHour == 0 -> 12
        scheduledHour > 12 -> scheduledHour - 12
        else -> scheduledHour
    }
    val amPmString: String get() = if (scheduledHour < 12) "AM" else "PM"
    val formattedTime: String get() = String.format("%d:%02d %s", displayHour, scheduledMinute, amPmString)
}
