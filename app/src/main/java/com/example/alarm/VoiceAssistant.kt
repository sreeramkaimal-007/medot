package com.example.alarm

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class VoiceAssistant(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: String? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isInitialized = true
                pendingSpeech?.let { text ->
                    speakText(text, 0.9f)
                    pendingSpeech = null
                }
            } else {
                Log.e(TAG, "TextToSpeech init failed with status: $status")
            }
        }
    }

    fun speakMedicationReminder(
        medicationName: String,
        dosage: String,
        foodInstruction: String? = null,
        speechRate: Float = 0.9f
    ) {
        val greeting = getGreeting()
        val speechBuilder = StringBuilder()
        speechBuilder.append("$greeting. It is time for your $medicationName. ")
        if (dosage.isNotBlank()) {
            speechBuilder.append("Your recorded dosage is $dosage. ")
        }
        if (!foodInstruction.isNullOrBlank() && foodInstruction != "No Food Restriction") {
            speechBuilder.append("Take $foodInstruction. ")
        }

        val speechText = speechBuilder.toString()
        if (isInitialized) {
            speakText(speechText, speechRate)
        } else {
            pendingSpeech = speechText
        }
    }

    fun speakText(text: String, speechRate: Float = 0.9f) {
        tts?.setSpeechRate(speechRate)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_DOSE_ALARM")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    companion object {
        private const val TAG = "VoiceAssistant"
    }
}
