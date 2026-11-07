package com.example.data.model

data class UserPreferences(
    val clockStyle: ClockStyle = ClockStyle.CLASSIC_IVORY,
    val seniorMode: Boolean = true, // Default enabled for target audience!
    val voiceEnabled: Boolean = true,
    val speechRate: Float = 0.9f, // Slightly gentle pace for elderly users
    val snoozeDurationMinutes: Int = 10,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val privacyMode: Boolean = false,
    val leadTimeMinutes: Int = 0
)
