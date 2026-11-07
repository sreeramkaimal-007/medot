package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ClockStyle
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("daydial_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val clockStyleName = prefs.getString(KEY_CLOCK_STYLE, ClockStyle.CLASSIC_IVORY.name) ?: ClockStyle.CLASSIC_IVORY.name
        val clockStyle = try {
            ClockStyle.valueOf(clockStyleName)
        } catch (_: Exception) {
            ClockStyle.CLASSIC_IVORY
        }
        val seniorMode = prefs.getBoolean(KEY_SENIOR_MODE, true)
        val voiceEnabled = prefs.getBoolean(KEY_VOICE_ENABLED, true)
        val speechRate = prefs.getFloat(KEY_SPEECH_RATE, 0.9f)
        val snoozeMinutes = prefs.getInt(KEY_SNOOZE_MINUTES, 10)
        val vibration = prefs.getBoolean(KEY_VIBRATION, true)
        val sound = prefs.getBoolean(KEY_SOUND, true)
        val privacy = prefs.getBoolean(KEY_PRIVACY, false)
        val leadTime = prefs.getInt(KEY_LEAD_TIME, 0)

        return UserPreferences(
            clockStyle = clockStyle,
            seniorMode = seniorMode,
            voiceEnabled = voiceEnabled,
            speechRate = speechRate,
            snoozeDurationMinutes = snoozeMinutes,
            vibrationEnabled = vibration,
            soundEnabled = sound,
            privacyMode = privacy,
            leadTimeMinutes = leadTime
        )
    }

    fun updateClockStyle(style: ClockStyle) {
        prefs.edit().putString(KEY_CLOCK_STYLE, style.name).apply()
        _preferences.value = _preferences.value.copy(clockStyle = style)
    }

    fun updateSeniorMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SENIOR_MODE, enabled).apply()
        _preferences.value = _preferences.value.copy(seniorMode = enabled)
    }

    fun updateVoiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
        _preferences.value = _preferences.value.copy(voiceEnabled = enabled)
    }

    fun updateSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _preferences.value = _preferences.value.copy(speechRate = rate)
    }

    fun updateSnoozeMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_SNOOZE_MINUTES, minutes).apply()
        _preferences.value = _preferences.value.copy(snoozeDurationMinutes = minutes)
    }

    fun updateVibration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        _preferences.value = _preferences.value.copy(vibrationEnabled = enabled)
    }

    fun updateSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _preferences.value = _preferences.value.copy(soundEnabled = enabled)
    }

    fun updatePrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY, enabled).apply()
        _preferences.value = _preferences.value.copy(privacyMode = enabled)
    }

    fun updateLeadTimeMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_LEAD_TIME, minutes).apply()
        _preferences.value = _preferences.value.copy(leadTimeMinutes = minutes)
    }

    companion object {
        private const val KEY_CLOCK_STYLE = "key_clock_style"
        private const val KEY_SENIOR_MODE = "key_senior_mode"
        private const val KEY_VOICE_ENABLED = "key_voice_enabled"
        private const val KEY_SPEECH_RATE = "key_speech_rate"
        private const val KEY_SNOOZE_MINUTES = "key_snooze_minutes"
        private const val KEY_VIBRATION = "key_vibration"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_PRIVACY = "key_privacy"
        private const val KEY_LEAD_TIME = "key_lead_time"
    }
}
