package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.model.FoodInstruction
import com.example.data.model.Medication
import com.example.data.model.MedicationType
import com.example.ui.components.HelpBottomSheet
import com.example.ui.components.HelpIconButton
import com.example.ui.components.StationaryGridTimePicker
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SageGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    onSaveMedication: (medication: Medication, times: List<Pair<Int, Int>>) -> Unit,
    modifier: Modifier = Modifier,
    seniorMode: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("1 tablet") }
    var selectedType by remember { mutableStateOf(MedicationType.TABLET) }
    var selectedFood by remember { mutableStateOf(FoodInstruction.AFTER_FOOD) }
    var notes by remember { mutableStateOf("") }

    // List of scheduled times (Hour, Minute)
    var scheduledTimes by remember { mutableStateOf(listOf(Pair(8, 0))) }
    var activePickingHour by remember { mutableIntStateOf(8) }
    var activePickingMinute by remember { mutableIntStateOf(0) }
    var showTimePickerForIndex by remember { mutableStateOf<Int?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showHelp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Medication",
                        fontSize = if (seniorMode) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("add_medication_back_button")
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
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Please enter the medication name."
                            } else if (dosage.isBlank()) {
                                errorMessage = "Please enter the dosage."
                            } else if (scheduledTimes.isEmpty()) {
                                errorMessage = "Please add at least one reminder time."
                            } else {
                                errorMessage = null
                                val med = Medication(
                                    name = name.trim(),
                                    dosage = dosage.trim(),
                                    type = selectedType,
                                    foodInstruction = selectedFood,
                                    instructions = notes.trim(),
                                    notes = notes.trim()
                                )
                                onSaveMedication(med, scheduledTimes)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (seniorMode) 68.dp else 58.dp)
                            .testTag("save_medication_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SAVE & SCHEDULE",
                            fontSize = if (seniorMode) 20.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Medication Name
            Column {
                Text(
                    text = "MEDICATION NAME *",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) errorMessage = null
                    },
                    placeholder = { Text("e.g. Lisinopril, Metformin, Aspirin", fontSize = if (seniorMode) 18.sp else 16.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medication_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 2. Dosage Field with quick suggestions
            Column {
                Text(
                    text = "DOSAGE AMOUNT *",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    placeholder = { Text("e.g. 1 tablet, 10 mg, 5 ml", fontSize = if (seniorMode) 18.sp else 16.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medication_dosage_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Quick dosage chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1 tablet", "2 tablets", "1 capsule", "5 ml", "10 ml", "1 puff", "1 drop").forEach { chip ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { dosage = chip },
                            color = if (dosage == chip) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (dosage == chip) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = if (seniorMode) 15.sp else 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. Medication Type Selection
            Column {
                Text(
                    text = "MEDICATION FORM",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MedicationType.values().forEach { type ->
                        val isSelected = type == selectedType
                        Surface(
                            modifier = Modifier
                                .height(if (seniorMode) 52.dp else 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedType = type },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = if (seniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 4. Food Instructions
            Column {
                Text(
                    text = "MEAL INSTRUCTION",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FoodInstruction.values().forEach { food ->
                        val isSelected = food == selectedFood
                        Surface(
                            modifier = Modifier
                                .height(if (seniorMode) 52.dp else 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedFood = food },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = food.label,
                                    fontSize = if (seniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 5. Scheduled Reminder Times (Multi-time support)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REMINDER TIMES",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (seniorMode) 16.sp else 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Stationary 12-Hour Grid",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of times scheduled
                scheduledTimes.forEachIndexed { index, (h, m) ->
                    val isAM = h < 12
                    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
                    val timeString = String.format("%d:%02d %s", displayH, m, if (isAM) "AM" else "PM")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = timeString,
                                    fontSize = if (seniorMode) 22.sp else 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (scheduledTimes.size > 1) {
                                IconButton(onClick = {
                                    scheduledTimes = scheduledTimes.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove time", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stationary Grid Time Picker (always stationary, no radial dial)
                StationaryGridTimePicker(
                    initialHour = activePickingHour,
                    initialMinute = activePickingMinute,
                    onTimeSelected = { hour24, min ->
                        activePickingHour = hour24
                        activePickingMinute = min
                    },
                    seniorMode = seniorMode
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val newTime = Pair(activePickingHour, activePickingMinute)
                        if (!scheduledTimes.contains(newTime)) {
                            scheduledTimes = (scheduledTimes + newTime).sortedBy { it.first * 60 + it.second }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("add_another_time_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add This Time to Schedule",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (seniorMode) 17.sp else 15.sp
                    )
                }
            }

            // 6. Notes or Doctor's Instructions
            Column {
                Text(
                    text = "DOCTOR OR PATIENT NOTES (OPTIONAL)",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("e.g. Take with a full glass of water. Do not chew.", fontSize = if (seniorMode) 17.sp else 15.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("medication_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }

            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showHelp) {
        HelpBottomSheet(
            title = "Adding a Medication",
            contentItems = listOf(
                "Name & Dosage" to "Type the name printed on your medicine label and how much you take (like '1 tablet' or '5 ml').",
                "Choosing Time" to "Tap the AM button for morning or PM button for evening. Then simply tap the hour (1-12) and minute.",
                "Multiple Times a Day" to "If you take this medicine twice or three times a day, choose the first time and tap 'Add This Time to Schedule', then choose the second time!"
            ),
            onDismiss = { showHelp = false },
            seniorMode = seniorMode
        )
    }
}
