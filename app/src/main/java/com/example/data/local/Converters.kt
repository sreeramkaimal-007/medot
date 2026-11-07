package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ClockStyle
import com.example.data.model.DoseStatus
import com.example.data.model.FoodInstruction
import com.example.data.model.MedicationType

class Converters {
    @TypeConverter
    fun fromDoseStatus(status: DoseStatus): String = status.name

    @TypeConverter
    fun toDoseStatus(value: String): DoseStatus = try {
        DoseStatus.valueOf(value)
    } catch (_: Exception) {
        DoseStatus.UPCOMING
    }

    @TypeConverter
    fun fromClockStyle(style: ClockStyle): String = style.name

    @TypeConverter
    fun toClockStyle(value: String): ClockStyle = try {
        ClockStyle.valueOf(value)
    } catch (_: Exception) {
        ClockStyle.CLASSIC_IVORY
    }

    @TypeConverter
    fun fromMedicationType(type: MedicationType): String = type.name

    @TypeConverter
    fun toMedicationType(value: String): MedicationType = try {
        MedicationType.valueOf(value)
    } catch (_: Exception) {
        MedicationType.TABLET
    }

    @TypeConverter
    fun fromFoodInstruction(food: FoodInstruction): String = food.name

    @TypeConverter
    fun toFoodInstruction(value: String): FoodInstruction = try {
        FoodInstruction.valueOf(value)
    } catch (_: Exception) {
        FoodInstruction.NO_RESTRICTION
    }
}
