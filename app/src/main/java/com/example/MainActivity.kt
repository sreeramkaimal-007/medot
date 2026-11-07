package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.model.ClockStyle
import com.example.data.model.DoseLog
import com.example.data.model.Medication
import com.example.data.model.UserProfile
import com.example.data.repository.MedicationRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.screens.AddMedicationScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DayDialTheme
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object Dashboard : Screen()
    object AddMedication : Screen()
    object History : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var medicationRepository: MedicationRepository
    private lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DayDialApplication
        medicationRepository = app.repository
        preferencesRepository = UserPreferencesRepository(this)

        setContent {
            val preferences by preferencesRepository.preferences.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

            // Permission Request for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Notifications are needed so you don't miss your medication reminders."
                        )
                    }
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            DayDialTheme(
                clockStyle = preferences.clockStyle,
                seniorMode = preferences.seniorMode
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    when (currentScreen) {
                        is Screen.Splash -> {
                            SplashScreen(
                                onSplashFinished = {
                                    val authPrefs = getSharedPreferences("daydial_auth", Context.MODE_PRIVATE)
                                    val isRegistered = authPrefs.getBoolean("is_registered", false)
                                    currentScreen = if (isRegistered) Screen.Dashboard else Screen.Auth
                                }
                            )
                        }

                        is Screen.Auth -> {
                            AuthScreen(
                                onAuthenticated = { name, email ->
                                    val authPrefs = getSharedPreferences("daydial_auth", Context.MODE_PRIVATE)
                                    authPrefs.edit()
                                        .putBoolean("is_registered", true)
                                        .putString("user_name", name)
                                        .putString("user_email", email)
                                        .apply()

                                    coroutineScope.launch {
                                        val db = com.example.data.local.DayDialDatabase.getInstance(this@MainActivity)
                                        db.userProfileDao().insertOrUpdate(
                                            UserProfile(
                                                id = 1L,
                                                fullName = name,
                                                email = email,
                                                isGuest = false
                                            )
                                        )
                                    }
                                    currentScreen = Screen.Dashboard
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Welcome to medot, $name!")
                                    }
                                },
                                seniorMode = preferences.seniorMode
                            )
                        }

                        is Screen.Dashboard -> {
                            val todayLogs by medicationRepository.getTodayDoseLogs().collectAsState(initial = emptyList())
                            val medications by medicationRepository.activeMedications.collectAsState(initial = emptyList())
                            val takenCount by medicationRepository.getTodayTakenCount().collectAsState(initial = 0)
                            val upcomingCount by medicationRepository.getTodayUpcomingCount().collectAsState(initial = 0)
                            val missedCount by medicationRepository.getTodayMissedCount().collectAsState(initial = 0)

                            DashboardScreen(
                                clockStyle = preferences.clockStyle,
                                todayDoseLogs = todayLogs,
                                medications = medications,
                                takenCount = takenCount,
                                upcomingCount = upcomingCount,
                                missedCount = missedCount,
                                onAddMedicationClick = { currentScreen = Screen.AddMedication },
                                onNavigateHistory = { currentScreen = Screen.History },
                                onNavigateSettings = { currentScreen = Screen.Settings },
                                onClockStyleChanged = { preferencesRepository.updateClockStyle(it) },
                                onTakeDose = { dose ->
                                    coroutineScope.launch {
                                        medicationRepository.markDoseTaken(dose.id)
                                        snackbarHostState.showSnackbar("Dose recorded as Taken ✓")
                                    }
                                },
                                onSnoozeDose = { dose, minutes ->
                                    coroutineScope.launch {
                                        medicationRepository.snoozeDose(dose.id, minutes)
                                        snackbarHostState.showSnackbar("Snoozed for $minutes minutes")
                                    }
                                },
                                onSkipDose = { dose, reason ->
                                    coroutineScope.launch {
                                        medicationRepository.skipDose(dose.id, reason)
                                        snackbarHostState.showSnackbar("Dose skipped")
                                    }
                                },
                                seniorMode = preferences.seniorMode
                            )
                        }

                        is Screen.AddMedication -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            AddMedicationScreen(
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onSaveMedication = { med, times ->
                                    coroutineScope.launch {
                                        medicationRepository.addMedicationWithSchedules(med, times)
                                        currentScreen = Screen.Dashboard
                                        snackbarHostState.showSnackbar("Scheduled ${med.name} successfully!")
                                    }
                                },
                                seniorMode = preferences.seniorMode
                            )
                        }

                        is Screen.History -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            val allLogs by medicationRepository.allDoseLogs.collectAsState(initial = emptyList())
                            HistoryScreen(
                                allDoseLogs = allLogs,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                repository = medicationRepository,
                                seniorMode = preferences.seniorMode
                            )
                        }

                        is Screen.Settings -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            val currentProfile by com.example.data.local.DayDialDatabase.getInstance(this@MainActivity)
                                .userProfileDao()
                                .getUserProfile()
                                .collectAsState(initial = null)

                            SettingsScreen(
                                preferences = preferences,
                                userProfile = currentProfile,
                                prefsRepository = preferencesRepository,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onSignOut = {
                                    val authPrefs = getSharedPreferences("daydial_auth", Context.MODE_PRIVATE)
                                    authPrefs.edit().putBoolean("is_registered", false).apply()
                                    coroutineScope.launch {
                                        val db = com.example.data.local.DayDialDatabase.getInstance(this@MainActivity)
                                        db.userProfileDao().deleteUserProfile()
                                        com.example.data.remote.supabase.SupabaseManager.getInstance(this@MainActivity).signOut()
                                    }
                                    currentScreen = Screen.Auth
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
