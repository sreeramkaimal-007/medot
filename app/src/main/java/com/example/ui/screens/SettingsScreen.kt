package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.MedotNavy
import com.example.ui.theme.MedotTeal
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.VoiceAssistant
import com.example.data.local.DayDialDatabase
import com.example.data.model.ClockStyle
import com.example.data.model.UserPreferences
import com.example.data.model.UserProfile
import com.example.data.remote.supabase.SupabaseManager
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.components.HelpBottomSheet
import com.example.ui.components.HelpIconButton
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MedotNavy
import com.example.ui.theme.MedotTeal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    userProfile: UserProfile?,
    prefsRepository: UserPreferencesRepository,
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showHelp by remember { mutableStateOf(false) }
    var showClockPicker by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    val voiceAssistant = remember { VoiceAssistant(context) }
    val supabaseManager = remember { SupabaseManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var showSqlSchemaDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = if (preferences.seniorMode) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    HelpIconButton(onClick = { showHelp = true })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 0. Registered Account Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Initial Circle
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(DeepNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (userProfile?.fullName?.firstOrNull() ?: 'P').uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile?.fullName ?: "Registered Patient",
                                fontSize = if (preferences.seniorMode) 20.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!userProfile?.email.isNullOrBlank()) {
                                Text(
                                    text = userProfile!!.email,
                                    fontSize = if (preferences.seniorMode) 15.sp else 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntiqueGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Verified Account",
                                color = AntiqueGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = { showSignOutDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 1. Senior Mode Highlight Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (preferences.seniorMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (preferences.seniorMode) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Accessibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Senior Mode (Large Text)",
                                fontSize = if (preferences.seniorMode) 20.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Larger letters, buttons, and high visibility",
                                fontSize = if (preferences.seniorMode) 15.sp else 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Switch(
                        checked = preferences.seniorMode,
                        onCheckedChange = { prefsRepository.updateSeniorMode(it) },
                        modifier = Modifier.testTag("toggle_senior_mode")
                    )
                }
            }

            // 2. Clock Face Style Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClockPicker = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Clock Face Style",
                                fontSize = if (preferences.seniorMode) 19.sp else 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preferences.clockStyle.displayName,
                                fontSize = if (preferences.seniorMode) 16.sp else 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "Change",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // 3. Voice Assistant Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Voice Assistant",
                                    fontSize = if (preferences.seniorMode) 19.sp else 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Reads reminders aloud",
                                    fontSize = if (preferences.seniorMode) 15.sp else 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Switch(
                            checked = preferences.voiceEnabled,
                            onCheckedChange = { prefsRepository.updateVoiceEnabled(it) },
                            modifier = Modifier.testTag("toggle_voice_assistant")
                        )
                    }

                    if (preferences.voiceEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                voiceAssistant.speakMedicationReminder(
                                    medicationName = "Blood Pressure Tablet",
                                    dosage = "one tablet",
                                    foodInstruction = "with breakfast",
                                    speechRate = preferences.speechRate
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("test_voice_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test Voice Reminder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Notifications & Alarms Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Vibration Alert",
                                fontSize = if (preferences.seniorMode) 18.sp else 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = preferences.vibrationEnabled,
                            onCheckedChange = { prefsRepository.updateVibration(it) }
                        )
                    }

                    // Sound Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Alarm Sound",
                                fontSize = if (preferences.seniorMode) 18.sp else 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = preferences.soundEnabled,
                            onCheckedChange = { prefsRepository.updateSound(it) }
                        )
                    }

                    // Snooze Duration Selector
                    Column {
                        Text(
                            text = "Snooze Duration: ${preferences.snoozeDurationMinutes} minutes",
                            fontSize = if (preferences.seniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30).forEach { mins ->
                                val isSelected = mins == preferences.snoozeDurationMinutes
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clickable { prefsRepository.updateSnoozeMinutes(mins) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${mins}m",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Lock Screen Privacy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Lock Screen Privacy",
                                    fontSize = if (preferences.seniorMode) 18.sp else 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Hide medication names on lock screen",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Switch(
                            checked = preferences.privacyMode,
                            onCheckedChange = { prefsRepository.updatePrivacyMode(it) }
                        )
                    }
                }
            }

            // 5. Supabase Cloud Sync
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_sync_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedotTeal.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = MedotTeal,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Supabase Cloud Sync",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (preferences.seniorMode) 20.sp else 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (supabaseManager.isConfigured) Color(0xFF00C853).copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (supabaseManager.isConfigured) "Connected" else "Setup Required",
                                color = if (supabaseManager.isConfigured) Color(0xFF007E33) else Color(0xFFE65100),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (supabaseManager.isConfigured) {
                        Text(
                            text = "Cloud Project: ${supabaseManager.supabaseUrl.replace("https://", "").take(22)}...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (supabaseManager.lastSyncEpochMillis > 0) {
                            val syncDateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(supabaseManager.lastSyncEpochMillis))
                            Text(
                                text = "Last Synced: $syncDateStr",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        Text(
                            text = "To synchronize your medication schedule across devices, set SUPABASE_URL and SUPABASE_ANON_KEY in the AI Studio Secrets panel.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    syncMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (msg.contains("Failed") || msg.contains("Error") || msg.contains("Please")) MaterialTheme.colorScheme.error else Color(0xFF007E33)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (supabaseManager.isConfigured) {
                                    isSyncing = true
                                    syncMessage = null
                                    coroutineScope.launch {
                                        val db = DayDialDatabase.getInstance(context)
                                        val result = supabaseManager.syncAll(db)
                                        isSyncing = false
                                        syncMessage = if (result.isSuccess) {
                                            val r = result.getOrNull()!!
                                            "Synced ${r.medicationsCount} meds & ${r.doseLogsCount} logs ✓"
                                        } else {
                                            "Sync Failed: ${result.exceptionOrNull()?.message?.take(50)}"
                                        }
                                    }
                                } else {
                                    syncMessage = "Please add SUPABASE_URL & SUPABASE_ANON_KEY in AI Studio Secrets."
                                }
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(if (preferences.seniorMode) 54.dp else 46.dp)
                                .testTag("supabase_sync_now_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedotTeal),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showConfigDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(if (preferences.seniorMode) 54.dp else 46.dp)
                                .testTag("supabase_config_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Config", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showSqlSchemaDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(if (preferences.seniorMode) 54.dp else 46.dp)
                                .testTag("supabase_view_sql_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SQL", fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Medical Safety Disclaimer (Mandatory)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Medical Disclaimer",
                        tint = AntiqueGold,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "medot helps you manage medication reminders and keep a record of your medication schedule. It does not provide medical advice or recommend medicines or dosages. Always follow the instructions given by your healthcare professional.",
                        fontSize = if (preferences.seniorMode) 15.sp else 13.sp,
                        lineHeight = if (preferences.seniorMode) 22.sp else 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 6. App Version Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.medot_icon),
                    contentDescription = "medot logo",
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "me",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MedotNavy
                    )
                    Text(
                        text = "dot",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MedotTeal
                    )
                    Text(
                        text = " • Version 1.0",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = "Never Miss Your Medication",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClockPicker) {
        ClockStyleBottomSheet(
            currentStyle = preferences.clockStyle,
            onSelectStyle = {
                prefsRepository.updateClockStyle(it)
                showClockPicker = false
            },
            onDismiss = { showClockPicker = false },
            seniorMode = preferences.seniorMode
        )
    }

    if (showHelp) {
        HelpBottomSheet(
            title = "medot Settings",
            contentItems = listOf(
                "Senior Mode" to "Enables extra-large text and buttons to make reading and tapping effortless.",
                "Voice Reminders" to "When enabled, medot speaks out your medication name and dosage aloud at reminder time.",
                "Privacy Mode" to "Hides specific medicine names on your phone's lock screen notification so only you know what you are taking."
            ),
            onDismiss = { showHelp = false },
            seniorMode = preferences.seniorMode
        )
    }

    if (showSignOutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text("Sign Out of medot?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("You will need to sign in or register again to access the medication dial.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSqlSchemaDialog) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        var copiedToast by remember { mutableStateOf(false) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSqlSchemaDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MedotTeal
                    )
                    Text(
                        "Supabase SQL Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Run this SQL script in your Supabase Dashboard (SQL Editor) to create the medications, schedules, and dose_logs tables with Row Level Security (RLS) enabled:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = SupabaseManager.SQL_SCHEMA_SETUP.trimIndent(),
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (copiedToast) {
                        Text(
                            "✓ Copied to clipboard!",
                            color = MedotTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(SupabaseManager.SQL_SCHEMA_SETUP.trimIndent()))
                        copiedToast = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedotTeal)
                ) {
                    Text("Copy SQL Script", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSqlSchemaDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showConfigDialog) {
        var inputUrl by remember { mutableStateOf(supabaseManager.supabaseUrl) }
        var inputKey by remember { mutableStateOf(supabaseManager.supabaseAnonKey) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MedotTeal
                    )
                    Text(
                        "Supabase Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Project URL & API Key for cloud medication synchronization and authentication:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Supabase URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Anon / Publishable API Key") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        supabaseManager.saveCredentials(inputUrl, inputKey)
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedotTeal)
                ) {
                    Text("Save & Apply", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
