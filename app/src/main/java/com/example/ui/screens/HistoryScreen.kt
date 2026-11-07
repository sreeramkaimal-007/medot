package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.repository.MedicationRepository
import com.example.ui.components.HelpBottomSheet
import com.example.ui.components.HelpIconButton
import com.example.ui.components.getMarkerColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    ALL_TIME("All Records")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    allDoseLogs: List<DoseLog>,
    onNavigateBack: () -> Unit,
    repository: MedicationRepository,
    modifier: Modifier = Modifier,
    seniorMode: Boolean = false
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(HistoryFilter.TODAY) }
    var showHelp by remember { mutableStateOf(false) }

    // Filter dose logs
    val filteredLogs = remember(allDoseLogs, selectedFilter) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        when (selectedFilter) {
            HistoryFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis
                allDoseLogs.filter { it.scheduledEpochMillis >= start }
            }
            HistoryFilter.YESTERDAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val end = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val start = calendar.timeInMillis
                allDoseLogs.filter { it.scheduledEpochMillis in start until end }
            }
            HistoryFilter.LAST_7_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val start = calendar.timeInMillis
                allDoseLogs.filter { it.scheduledEpochMillis >= start }
            }
            HistoryFilter.LAST_30_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val start = calendar.timeInMillis
                allDoseLogs.filter { it.scheduledEpochMillis >= start }
            }
            HistoryFilter.ALL_TIME -> allDoseLogs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medication History",
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
                            .testTag("history_back_button")
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
                tonalElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val csvData = repository.exportHistoryAsCsv(filteredLogs)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "medot Medication History Report")
                                putExtra(Intent.EXTRA_TEXT, csvData)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export Medication Report"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (seniorMode) 64.dp else 56.dp)
                            .testTag("export_csv_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export History Report (CSV)",
                            fontSize = if (seniorMode) 18.sp else 16.sp,
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
                .padding(horizontal = 16.dp)
        ) {
            // Filter Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryFilter.values()) { filter ->
                    val isSelected = filter == selectedFilter
                    Surface(
                        modifier = Modifier
                            .height(if (seniorMode) 52.dp else 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = filter }
                            .testTag("filter_${filter.name}"),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = if (seniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Results count
            Text(
                text = "${filteredLogs.size} record${if (filteredLogs.size != 1) "s" else ""} found",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // History Records List
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No medication records for ${selectedFilter.label.lowercase()}",
                        fontSize = if (seniorMode) 18.sp else 16.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        HistoryCard(doseLog = log, seniorMode = seniorMode)
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    if (showHelp) {
        HelpBottomSheet(
            title = "Medication History",
            contentItems = listOf(
                "Tracking Doses" to "This screen keeps an accurate log of every medication you have taken, skipped, or missed.",
                "Exporting to Doctor" to "Tap 'Export History Report' at the bottom to share your medication log with your doctor, caregiver, or family as a CSV document."
            ),
            onDismiss = { showHelp = false },
            seniorMode = seniorMode
        )
    }
}

@Composable
private fun HistoryCard(doseLog: DoseLog, seniorMode: Boolean) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()) }
    val scheduledStr = remember(doseLog.scheduledEpochMillis) {
        dateFormat.format(Date(doseLog.scheduledEpochMillis))
    }
    val actionStr = remember(doseLog.actionEpochMillis) {
        doseLog.actionEpochMillis?.let {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Status Icon Circle
            Box(
                modifier = Modifier
                    .size(if (seniorMode) 44.dp else 36.dp)
                    .clip(CircleShape)
                    .background(getMarkerColor(doseLog.status)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (doseLog.status) {
                        DoseStatus.TAKEN -> "✓"
                        DoseStatus.SKIPPED -> "✕"
                        DoseStatus.MISSED -> "!"
                        else -> "●"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (seniorMode) 18.sp else 15.sp
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
                    text = "${doseLog.dosage} • Scheduled: $scheduledStr",
                    fontSize = if (seniorMode) 15.sp else 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                if (actionStr != null) {
                    Text(
                        text = "Confirmed: $actionStr",
                        fontSize = if (seniorMode) 14.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = getMarkerColor(doseLog.status)
                    )
                }
            }

            // Status Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
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
