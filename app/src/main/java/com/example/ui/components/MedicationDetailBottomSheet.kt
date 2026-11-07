package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.Medication
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SageGreen
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusSnoozed
import com.example.ui.theme.StatusTaken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailBottomSheet(
    doseLog: DoseLog,
    medication: Medication?,
    onDismiss: () -> Unit,
    onTakeDose: (DoseLog) -> Unit,
    onSnoozeDose: (DoseLog, Int) -> Unit,
    onSkipDose: (DoseLog, String) -> Unit,
    onTestAlarm: (DoseLog) -> Unit,
    seniorMode: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSnoozeOptions by remember { mutableStateOf(false) }
    var showSkipConfirmation by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("medication_detail_bottom_sheet")
        ) {
            // Header: Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doseLog.medicationName,
                        fontSize = if (seniorMode) 28.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = doseLog.dosage,
                        fontSize = if (seniorMode) 20.sp else 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(getMarkerColor(doseLog.status))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = doseLog.status.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (seniorMode) 16.sp else 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(label = "Scheduled Time", value = "${doseLog.formattedTime} (${if (doseLog.isAM) "Morning" else "Evening"})", seniorMode = seniorMode)
                    medication?.foodInstruction?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "Food Instruction", value = it.label, seniorMode = seniorMode)
                    }
                    if (!medication?.instructions.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "Doctor / Patient Notes", value = medication?.instructions ?: "", seniorMode = seniorMode)
                    }
                    if (doseLog.actionEpochMillis != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val actionTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(doseLog.actionEpochMillis))
                        DetailRow(label = "Action Recorded", value = "${doseLog.status.displayName} at $actionTimeStr", seniorMode = seniorMode)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Three Primary Actions for Seniors (Min 56dp height)
            // 1. TAKEN (Large Green button)
            Button(
                onClick = {
                    onTakeDose(doseLog)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (seniorMode) 68.dp else 58.dp)
                    .testTag("detail_take_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "TAKE DOSE NOW",
                    fontSize = if (seniorMode) 20.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. SNOOZE and SKIP Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showSnoozeOptions = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(if (seniorMode) 62.dp else 54.dp)
                        .testTag("detail_snooze_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Snooze",
                        fontSize = if (seniorMode) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { showSkipConfirmation = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(if (seniorMode) 62.dp else 54.dp)
                        .testTag("detail_skip_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Skip Dose",
                        fontSize = if (seniorMode) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test Alarm Quick Trigger
            TextButton(
                onClick = {
                    onTestAlarm(doseLog)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_test_alarm_button")
            ) {
                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Alarm & Voice for this Medication", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Snooze Duration Dialog
    if (showSnoozeOptions) {
        AlertDialog(
            onDismissRequest = { showSnoozeOptions = false },
            title = { Text("Select Snooze Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 30).forEach { mins ->
                        Button(
                            onClick = {
                                onSnoozeDose(doseLog, mins)
                                showSnoozeOptions = false
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Snooze for $mins minutes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSnoozeOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Skip Confirmation Dialog
    if (showSkipConfirmation) {
        AlertDialog(
            onDismissRequest = { showSkipConfirmation = false },
            title = { Text("Skip this Dose?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to skip taking ${doseLog.medicationName} (${doseLog.formattedTime})? This will be recorded in your history.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSkipDose(doseLog, "Skipped by patient")
                        showSkipConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Skip Dose")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSkipConfirmation = false }) {
                    Text("Keep Scheduled")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, seniorMode: Boolean) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = if (seniorMode) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            fontSize = if (seniorMode) 18.sp else 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
