package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: Long = 1L,
    val fullName: String = "Valued Patient",
    val email: String = "",
    val isGuest: Boolean = true,
    val cloudSyncEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
