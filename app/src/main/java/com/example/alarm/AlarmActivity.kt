package com.example.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DayDialDatabase
import com.example.data.model.DoseStatus
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.components.TwistToConfirmClock
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DayDialTheme
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SageGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var voiceAssistant: VoiceAssistant? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen on and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val doseLogId = intent.getLongExtra(AlarmScheduler.EXTRA_DOSE_LOG_ID, -1L)
        val medName = intent.getStringExtra(AlarmScheduler.EXTRA_MED_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(AlarmScheduler.EXTRA_DOSAGE) ?: ""
        val scheduledTime = intent.getStringExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME) ?: ""

        val prefsRepo = UserPreferencesRepository(this)
        val prefs = prefsRepo.preferences.value

        // Setup sound and vibration
        startAlertEffects(prefs.soundEnabled, prefs.vibrationEnabled)

        // Setup voice
        if (prefs.voiceEnabled) {
            voiceAssistant = VoiceAssistant(this).apply {
                speakMedicationReminder(medName, dosage, null, prefs.speechRate)
            }
        }

        setContent {
            DayDialTheme(
                clockStyle = prefs.clockStyle,
                seniorMode = prefs.seniorMode
            ) {
                AlarmContent(
                    medName = medName,
                    dosage = dosage,
                    scheduledTime = scheduledTime,
                    doseLogId = doseLogId,
                    onDismiss = {
                        stopAlertEffects()
                        finish()
                    },
                    seniorMode = prefs.seniorMode
                )
            }
        }
    }

    private fun startAlertEffects(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            try {
                val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AlarmActivity, alertUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) {}
        }

        if (vibrationEnabled) {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val pattern = longArrayOf(0, 600, 300, 600, 300, 600)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
    }

    private fun stopAlertEffects() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
        vibrator?.cancel()
        voiceAssistant?.stop()
        voiceAssistant?.shutdown()
        voiceAssistant = null
    }

    override fun onDestroy() {
        stopAlertEffects()
        super.onDestroy()
    }
}

@Composable
fun AlarmContent(
    medName: String,
    dosage: String,
    scheduledTime: String,
    doseLogId: Long,
    onDismiss: () -> Unit,
    seniorMode: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isConfirmed by remember { mutableStateOf(false) }

    fun handleConfirmDose() {
        isConfirmed = true
        coroutineScope.launch {
            if (doseLogId > 0) {
                val db = DayDialDatabase.getInstance(context)
                val log = db.doseLogDao().getDoseLogByIdSync(doseLogId)
                if (log != null) {
                    db.doseLogDao().updateDoseLog(
                        log.copy(
                            status = DoseStatus.TAKEN,
                            actionEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(doseLogId.toInt())
            }
            delay(1200L)
            onDismiss()
        }
    }

    fun handleSnooze() {
        coroutineScope.launch {
            if (doseLogId > 0) {
                val db = DayDialDatabase.getInstance(context)
                val prefsRepo = UserPreferencesRepository(context)
                val snoozeMinutes = prefsRepo.preferences.value.snoozeDurationMinutes
                val snoozeEpoch = System.currentTimeMillis() + (snoozeMinutes * 60_000L)

                val log = db.doseLogDao().getDoseLogByIdSync(doseLogId)
                if (log != null) {
                    val updated = log.copy(
                        status = DoseStatus.SNOOZED,
                        snoozeUntilEpochMillis = snoozeEpoch
                    )
                    db.doseLogDao().updateDoseLog(updated)
                    AlarmScheduler(context).scheduleDoseAlarm(updated)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(doseLogId.toInt())
            }
            onDismiss()
        }
    }

    Scaffold(
        containerColor = IvoryBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Alarm Header Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(AntiqueGold.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = AntiqueGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TIME FOR YOUR MEDICATION",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 16.sp else 14.sp,
                    color = DeepNavy,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Medication Details
            Text(
                text = medName,
                fontSize = if (seniorMode) 38.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                color = DeepNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Dosage: $dosage",
                fontSize = if (seniorMode) 22.sp else 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = AntiqueGold
            )
            if (scheduledTime.isNotBlank()) {
                Text(
                    text = "Scheduled for $scheduledTime",
                    fontSize = if (seniorMode) 17.sp else 15.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Twist to Confirm Roman Clock
            Text(
                text = if (isConfirmed) "DOSE CONFIRMED ✓" else "Twist Minute Hand to XII to Confirm",
                fontWeight = FontWeight.Bold,
                fontSize = if (seniorMode) 19.sp else 16.sp,
                color = if (isConfirmed) SageGreen else DeepNavy
            )
            Spacer(modifier = Modifier.height(12.dp))

            TwistToConfirmClock(
                onConfirmed = { handleConfirmDose() },
                modifier = Modifier.size(if (seniorMode) 250.dp else 220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Direct Confirm Button (Alternative for seniors who prefer tapping)
            Button(
                onClick = { handleConfirmDose() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (seniorMode) 68.dp else 58.dp)
                    .testTag("alarm_confirm_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isConfirmed) "DOSE CONFIRMED ✓" else "CONFIRM DOSE TAKEN",
                    fontSize = if (seniorMode) 20.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Snooze Button
            OutlinedButton(
                onClick = { handleSnooze() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (seniorMode) 60.dp else 52.dp)
                    .testTag("alarm_snooze_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Snooze, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Snooze 10 Minutes",
                    fontSize = if (seniorMode) 18.sp else 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
