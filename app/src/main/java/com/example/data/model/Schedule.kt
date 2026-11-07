package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val daysOfWeekMask: Int = 127, // 7-bit mask for Monday..Sunday (or Sun..Sat)
    val isOneTime: Boolean = false,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isActive: Boolean = true
) {
    val isAM: Boolean get() = hour < 12
    val displayHour: Int get() = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPmString: String get() = if (hour < 12) "AM" else "PM"
    val formattedTime: String get() = String.format("%d:%02d %s", displayHour, minute, amPmString)
}
