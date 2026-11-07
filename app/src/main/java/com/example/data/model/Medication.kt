package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val type: MedicationType = MedicationType.TABLET,
    val foodInstruction: FoodInstruction = FoodInstruction.NO_RESTRICTION,
    val instructions: String = "",
    val photoUri: String? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
