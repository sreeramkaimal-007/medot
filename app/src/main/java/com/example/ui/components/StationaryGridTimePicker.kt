package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.IvoryBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StationaryGridTimePicker(
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onTimeSelected: (hour24: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    seniorMode: Boolean = false
) {
    var isAm by remember(initialHour) { mutableStateOf(initialHour < 12) }
    var selectedDisplayHour by remember(initialHour) {
        val h = when {
            initialHour == 0 -> 12
            initialHour > 12 -> initialHour - 12
            else -> initialHour
        }
        mutableIntStateOf(h)
    }
    var selectedMinute by remember(initialMinute) { mutableIntStateOf(initialMinute) }

    fun notifyChange() {
        val hour24 = when {
            isAm && selectedDisplayHour == 12 -> 0
            isAm -> selectedDisplayHour
            !isAm && selectedDisplayHour == 12 -> 12
            else -> selectedDisplayHour + 12
        }
        onTimeSelected(hour24, selectedMinute)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stationary_grid_time_picker"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selected Time Large Readout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d:%02d", selectedDisplayHour, selectedMinute),
                    fontSize = if (seniorMode) 38.sp else 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = if (isAm) "AM (Morning)" else "PM (Evening)",
                    fontSize = if (seniorMode) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AM / PM Large Toggle Buttons (Min 56dp height)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(true to "AM (Morning)", false to "PM (Evening)").forEach { (isAmOption, label) ->
                    val isSelected = isAm == isAmOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(role = Role.RadioButton) {
                                isAm = isAmOption
                                notifyChange()
                            }
                            .semantics {
                                contentDescription = "$label option. ${if (isSelected) "Selected" else "Not selected"}"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (seniorMode) 18.sp else 16.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: 12-Hour Grid (Hours 1 to 12)
            Text(
                text = "SELECT HOUR",
                fontWeight = FontWeight.Bold,
                fontSize = if (seniorMode) 16.sp else 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4
            ) {
                (1..12).forEach { hour ->
                    val isSelected = selectedDisplayHour == hour
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedDisplayHour = hour
                                notifyChange()
                            }
                            .semantics {
                                contentDescription = "$hour o'clock. ${if (isSelected) "Selected" else ""}"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hour.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (seniorMode) 22.sp else 18.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Minute Intervals (00, 15, 30, 45, plus common 5-min intervals)
            Text(
                text = "SELECT MINUTES",
                fontWeight = FontWeight.Bold,
                fontSize = if (seniorMode) 16.sp else 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val minuteOptions = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4
            ) {
                minuteOptions.forEach { min ->
                    val isSelected = selectedMinute == min
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedMinute = min
                                notifyChange()
                            }
                            .semantics {
                                contentDescription = "$min minutes. ${if (isSelected) "Selected" else ""}"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(":%02d", min),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (seniorMode) 19.sp else 16.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
