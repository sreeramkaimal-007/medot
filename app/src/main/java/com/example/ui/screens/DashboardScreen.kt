package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldLight
import com.example.alarm.AlarmActivity
import com.example.alarm.AlarmScheduler
import com.example.data.model.ClockStyle
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.Medication
import com.example.ui.components.HelpBottomSheet
import com.example.ui.components.HelpIconButton
import com.example.ui.components.MedicationDetailBottomSheet
import com.example.ui.components.RomanClockDial
import com.example.ui.components.getMarkerColor
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.ClockStyles
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SageGreen
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusTaken
import com.example.ui.theme.StatusUpcoming
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    clockStyle: ClockStyle,
    todayDoseLogs: List<DoseLog>,
    medications: List<Medication>,
    takenCount: Int,
    upcomingCount: Int,
    missedCount: Int,
    onAddMedicationClick: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit,
    onClockStyleChanged: (ClockStyle) -> Unit,
    onTakeDose: (DoseLog) -> Unit,
    onSnoozeDose: (DoseLog, Int) -> Unit,
    onSkipDose: (DoseLog, String) -> Unit,
    modifier: Modifier = Modifier,
    seniorMode: Boolean = false
) {
    val context = LocalContext.current
    var selectedDoseLog by remember { mutableStateOf<DoseLog?>(null) }
    var showHelp by remember { mutableStateOf(false) }
    var showClockStyleSheet by remember { mutableStateOf(false) }

    // Find next upcoming or due dose
    val nextDose = remember(todayDoseLogs) {
        val now = System.currentTimeMillis()
        todayDoseLogs
            .filter { it.status == DoseStatus.DUE_NOW || it.status == DoseStatus.UPCOMING || it.status == DoseStatus.SNOOZED }
            .sortedBy { it.snoozeUntilEpochMillis ?: it.scheduledEpochMillis }
            .firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.medot_icon),
                            contentDescription = "medot logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "me",
                                fontSize = if (seniorMode) 28.sp else 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = MedotNavy
                            )
                            Text(
                                text = "dot",
                                fontSize = if (seniorMode) 28.sp else 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = MedotTeal
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClockStyleSheet = true },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("clock_style_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Change Clock Style",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = onNavigateHistory,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("history_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Medication History",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("settings_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Application Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    HelpIconButton(onClick = { showHelp = true })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Button(
                onClick = onAddMedicationClick,
                modifier = Modifier
                    .height(if (seniorMode) 64.dp else 56.dp)
                    .testTag("add_medicine_fab"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MedotTeal,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MedotNavy.copy(alpha = 0.25f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADD MEDICINE",
                    fontSize = if (seniorMode) 19.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Central Roman Clock Dial
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RomanClockDial(
                        clockStyle = clockStyle,
                        doseLogs = todayDoseLogs,
                        onMarkerClick = { selectedDoseLog = it },
                        seniorMode = seniorMode,
                        modifier = Modifier
                            .size(if (seniorMode) 330.dp else 300.dp)
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Track legend explainer in a clean, high-contrast chip badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "AM",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (seniorMode) 13.sp else 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Outer (Morning)",
                                fontSize = if (seniorMode) 14.sp else 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                            Text("•", color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "PM",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (seniorMode) 13.sp else 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Inner (Evening)",
                                fontSize = if (seniorMode) 14.sp else 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // 2. Summary Counters (Taken, Upcoming, Missed)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CounterCard(
                        count = takenCount,
                        label = "Taken",
                        color = StatusTaken,
                        modifier = Modifier.weight(1f),
                        seniorMode = seniorMode
                    )
                    CounterCard(
                        count = upcomingCount,
                        label = "Upcoming",
                        color = StatusUpcoming,
                        modifier = Modifier.weight(1f),
                        seniorMode = seniorMode
                    )
                    CounterCard(
                        count = missedCount,
                        label = "Missed",
                        color = StatusMissed,
                        modifier = Modifier.weight(1f),
                        seniorMode = seniorMode
                    )
                }
            }

            // 3. "Next Medication" Highlight Card
            item {
                if (nextDose != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_medication_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "NEXT MEDICATION",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (seniorMode) 15.sp else 13.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = nextDose.formattedTime,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (seniorMode) 18.sp else 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = nextDose.medicationName,
                                fontSize = if (seniorMode) 26.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = nextDose.dosage,
                                fontSize = if (seniorMode) 19.sp else 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onTakeDose(nextDose) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(if (seniorMode) 58.dp else 50.dp)
                                        .testTag("quick_take_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TAKE DOSE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (seniorMode) 18.sp else 16.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { selectedDoseLog = nextDose },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(if (seniorMode) 58.dp else 50.dp)
                                        .testTag("quick_details_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Details",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = if (seniorMode) 17.sp else 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Section Title: Today's Medication Schedule
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S SCHEDULE",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (seniorMode) 18.sp else 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${todayDoseLogs.size} scheduled",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // List of today's doses
            if (todayDoseLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AntiqueGold.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AntiqueGold.copy(alpha = 0.15f))
                                    .border(2.dp, AntiqueGold.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AntiqueGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Your medot Dial is Ready",
                                fontSize = if (seniorMode) 22.sp else 19.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "No medications scheduled for today.\nAdd your prescriptions to see them placed as interactive markers around the Roman clock dial.",
                                fontSize = if (seniorMode) 16.sp else 14.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = if (seniorMode) 22.sp else 19.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = onAddMedicationClick,
                                modifier = Modifier
                                    .height(if (seniorMode) 58.dp else 50.dp)
                                    .testTag("empty_state_add_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntiqueGold)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Schedule First Medicine",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (seniorMode) 17.sp else 15.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(todayDoseLogs, key = { it.id }) { dose ->
                    ScheduleItemCard(
                        doseLog = dose,
                        onClick = { selectedDoseLog = dose },
                        seniorMode = seniorMode
                    )
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Medication Details Bottom Sheet
    selectedDoseLog?.let { dose ->
        val associatedMed = medications.find { it.id == dose.medicationId }
        MedicationDetailBottomSheet(
            doseLog = dose,
            medication = associatedMed,
            onDismiss = { selectedDoseLog = null },
            onTakeDose = onTakeDose,
            onSnoozeDose = onSnoozeDose,
            onSkipDose = onSkipDose,
            onTestAlarm = { testDose ->
                // Launch AlarmActivity for testing
                val intent = Intent(context, AlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(AlarmScheduler.EXTRA_DOSE_LOG_ID, testDose.id)
                    putExtra(AlarmScheduler.EXTRA_MED_ID, testDose.medicationId)
                    putExtra(AlarmScheduler.EXTRA_MED_NAME, testDose.medicationName)
                    putExtra(AlarmScheduler.EXTRA_DOSAGE, testDose.dosage)
                    putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, testDose.formattedTime)
                }
                context.startActivity(intent)
            },
            seniorMode = seniorMode
        )
    }

    // Clock Style Selection Bottom Sheet
    if (showClockStyleSheet) {
        ClockStyleBottomSheet(
            currentStyle = clockStyle,
            onSelectStyle = {
                onClockStyleChanged(it)
                showClockStyleSheet = false
            },
            onDismiss = { showClockStyleSheet = false },
            seniorMode = seniorMode
        )
    }

    // Help Dialog
    if (showHelp) {
        HelpBottomSheet(
            title = "How to Use medot",
            contentItems = listOf(
                "Reading the Clock" to "The clock shows your medications at their scheduled hours around the dial. Morning doses appear on the OUTER circle, and evening doses appear on the INNER circle.",
                "Colors on the Dial" to "• Blue: Upcoming dose\n• Orange/Gold: Due right now\n• Green: Already taken ✓\n• Red: Missed dose",
                "Taking a Dose" to "Tap any medication marker on the clock or on the list below, then tap the large green 'TAKE DOSE NOW' button.",
                "Twist to Confirm" to "When an alarm sounds, rotate the clock's minute hand to 12 o'clock to confirm you took your pill!"
            ),
            onDismiss = { showHelp = false },
            seniorMode = seniorMode
        )
    }
}

@Composable
private fun CounterCard(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    seniorMode: Boolean = false
) {
    Surface(
        modifier = modifier.height(if (seniorMode) 88.dp else 76.dp),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = if (seniorMode) 26.sp else 22.sp,
                color = color
            )
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (seniorMode) 15.sp else 13.sp,
                color = color
            )
        }
    }
}

@Composable
private fun ScheduleItemCard(
    doseLog: DoseLog,
    onClick: () -> Unit,
    seniorMode: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("schedule_item_${doseLog.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Marker circle indicator
            Box(
                modifier = Modifier
                    .size(if (seniorMode) 38.dp else 32.dp)
                    .clip(CircleShape)
                    .background(getMarkerColor(doseLog.status)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (doseLog.status == DoseStatus.TAKEN) "✓" else if (doseLog.isAM) "AM" else "PM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 14.sp else 11.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doseLog.medicationName,
                    fontSize = if (seniorMode) 20.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${doseLog.formattedTime} • ${doseLog.dosage}",
                    fontSize = if (seniorMode) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getMarkerColor(doseLog.status).copy(alpha = 0.15f)
            ) {
                Text(
                    text = doseLog.status.displayName,
                    color = getMarkerColor(doseLog.status),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 14.sp else 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockStyleBottomSheet(
    currentStyle: ClockStyle,
    onSelectStyle: (ClockStyle) -> Unit,
    onDismiss: () -> Unit,
    seniorMode: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Choose Clock Face Style",
                fontSize = if (seniorMode) 24.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            ClockStyle.values().forEach { style ->
                val isSelected = style == currentStyle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onSelectStyle(style) }
                        .testTag("clock_style_${style.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini preview circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when (style) {
                                        ClockStyle.CLASSIC_IVORY -> ClockStyles.ClassicIvoryFace
                                        ClockStyle.MIDNIGHT_GOLD -> ClockStyles.MidnightGoldFace
                                        ClockStyle.SOFT_BLUE -> ClockStyles.SoftBlueFace
                                        ClockStyle.HIGH_CONTRAST -> ClockStyles.HighContrastFace
                                    }
                                )
                                .border(
                                    2.dp,
                                    when (style) {
                                        ClockStyle.CLASSIC_IVORY -> ClockStyles.ClassicIvoryBorder
                                        ClockStyle.MIDNIGHT_GOLD -> ClockStyles.MidnightGoldBorder
                                        ClockStyle.SOFT_BLUE -> ClockStyles.SoftBlueBorder
                                        ClockStyle.HIGH_CONTRAST -> ClockStyles.HighContrastBorder
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "XII",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when (style) {
                                    ClockStyle.CLASSIC_IVORY -> ClockStyles.ClassicIvoryNumerals
                                    ClockStyle.MIDNIGHT_GOLD -> ClockStyles.MidnightGoldNumerals
                                    ClockStyle.SOFT_BLUE -> ClockStyles.SoftBlueNumerals
                                    ClockStyle.HIGH_CONTRAST -> ClockStyles.HighContrastNumerals
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style.displayName,
                                fontSize = if (seniorMode) 20.sp else 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = style.description,
                                fontSize = if (seniorMode) 15.sp else 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
