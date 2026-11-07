package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockStyle
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.ui.theme.ClockStyles
import com.example.ui.theme.StatusDueNow
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusSkipped
import com.example.ui.theme.StatusSnoozed
import com.example.ui.theme.StatusTaken
import com.example.ui.theme.StatusUpcoming
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val ROMAN_NUMERALS = listOf(
    "XII", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI"
)

@Composable
fun RomanClockDial(
    clockStyle: ClockStyle,
    doseLogs: List<DoseLog>,
    onMarkerClick: (DoseLog) -> Unit,
    modifier: Modifier = Modifier,
    seniorMode: Boolean = false
) {
    // Live ticking clock state
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val calendar = remember(currentTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val currentHour = calendar.get(Calendar.HOUR)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentSecond = calendar.get(Calendar.SECOND)

    // Hand angles
    val secondAngle = (currentSecond * 6f) - 90f
    val minuteAngle = (currentMinute * 6f + currentSecond * 0.1f) - 90f
    val hourAngle = (currentHour * 30f + currentMinute * 0.5f) - 90f

    // Next dose computation
    val nextDose = remember(doseLogs, currentTimeMillis) {
        doseLogs
            .filter { (it.status == DoseStatus.UPCOMING || it.status == DoseStatus.DUE_NOW || it.status == DoseStatus.SNOOZED) }
            .sortedBy { it.snoozeUntilEpochMillis ?: it.scheduledEpochMillis }
            .firstOrNull()
    }

    val nextCountdownText = remember(nextDose, doseLogs.size, currentTimeMillis) {
        if (doseLogs.isEmpty()) {
            "All Clear • No Doses Today"
        } else if (nextDose == null) {
            "All Doses Taken Today ✓"
        } else {
            val target = nextDose.snoozeUntilEpochMillis ?: nextDose.scheduledEpochMillis
            val diffMinutes = ((target - currentTimeMillis) / 60000L).toInt()
            when {
                diffMinutes <= 0 -> "${nextDose.medicationName} is due now!"
                diffMinutes < 60 -> "Next: ${nextDose.medicationName} in ${diffMinutes}m"
                else -> "Next: ${nextDose.medicationName} at ${nextDose.formattedTime}"
            }
        }
    }

    // Color definitions based on ClockStyle
    val (faceColor, borderColor, numeralColor, handsColor, accentColor) = when (clockStyle) {
        ClockStyle.CLASSIC_IVORY -> Tuple5(
            ClockStyles.ClassicIvoryFace,
            ClockStyles.ClassicIvoryBorder,
            ClockStyles.ClassicIvoryNumerals,
            ClockStyles.ClassicIvoryHands,
            Color(0xFF00A8B5)
        )
        ClockStyle.MIDNIGHT_GOLD -> Tuple5(
            ClockStyles.MidnightGoldFace,
            ClockStyles.MidnightGoldBorder,
            ClockStyles.MidnightGoldNumerals,
            ClockStyles.MidnightGoldHands,
            Color(0xFF00C2CB)
        )
        ClockStyle.SOFT_BLUE -> Tuple5(
            ClockStyles.SoftBlueFace,
            ClockStyles.SoftBlueBorder,
            ClockStyles.SoftBlueNumerals,
            ClockStyles.SoftBlueHands,
            Color(0xFF166FE5)
        )
        ClockStyle.HIGH_CONTRAST -> Tuple5(
            ClockStyles.HighContrastFace,
            ClockStyles.HighContrastBorder,
            ClockStyles.HighContrastNumerals,
            ClockStyles.HighContrastHands,
            Color(0xFFFFD700)
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .testTag("roman_clock_dial")
            .semantics {
                contentDescription = "Interactive Roman Numeral Medication Clock. Current time is ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)}. $nextCountdownText"
            },
        contentAlignment = Alignment.Center
    ) {
        val sizePx = constraints.maxWidth.toFloat().coerceAtMost(constraints.maxHeight.toFloat())
        val density = LocalDensity.current
        val sizeDp = with(density) { sizePx.toDp() }
        val centerPx = sizePx / 2f
        val radiusPx = sizePx * 0.46f

        Box(
            modifier = Modifier.size(sizeDp)
        ) {
            // 1. Clock Face Canvas: Bezel, Roman numerals, concentric tracks, hands
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Tier 1: Outer Case Shadow & Beveled Rim
                drawCircle(
                    color = Color.Black.copy(alpha = 0.15f),
                    radius = radiusPx + 14f,
                    center = Offset(centerPx, centerPx + 4f)
                )
                // Tier 2: Outer Metallic Casing Ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            borderColor.copy(alpha = 0.8f),
                            borderColor,
                            Color.White.copy(alpha = 0.6f),
                            borderColor,
                            borderColor.copy(alpha = 0.7f)
                        ),
                        center = Offset(centerPx, centerPx)
                    ),
                    radius = radiusPx + 10f,
                    center = Offset(centerPx, centerPx)
                )
                // Tier 3: Inner Fluted Ring
                drawCircle(
                    color = borderColor,
                    radius = radiusPx + 4f,
                    center = Offset(centerPx, centerPx),
                    style = Stroke(width = 3f)
                )

                // Tier 4: Clock Face with Soft Radial Sheen
                val dialGradient = when (clockStyle) {
                    ClockStyle.CLASSIC_IVORY -> Brush.radialGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFFF7FCFD), Color(0xFFEAF5F7)),
                        center = Offset(centerPx, centerPx),
                        radius = radiusPx
                    )
                    ClockStyle.MIDNIGHT_GOLD -> Brush.radialGradient(
                        listOf(Color(0xFF0E223D), Color(0xFF08152B), Color(0xFF040B17)),
                        center = Offset(centerPx, centerPx),
                        radius = radiusPx
                    )
                    ClockStyle.SOFT_BLUE -> Brush.radialGradient(
                        listOf(Color(0xFFF0F9FB), Color(0xFFE2F4F7), Color(0xFFCCEBF1)),
                        center = Offset(centerPx, centerPx),
                        radius = radiusPx
                    )
                    ClockStyle.HIGH_CONTRAST -> Brush.radialGradient(
                        listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D), Color(0xFF000000)),
                        center = Offset(centerPx, centerPx),
                        radius = radiusPx
                    )
                }
                drawCircle(
                    brush = dialGradient,
                    radius = radiusPx,
                    center = Offset(centerPx, centerPx)
                )

                // Outer and Inner Concentric Track lines (Outer = AM at 0.85, Inner = PM at 0.54)
                drawCircle(
                    color = borderColor.copy(alpha = 0.35f),
                    radius = radiusPx * 0.85f,
                    center = Offset(centerPx, centerPx),
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = borderColor.copy(alpha = 0.28f),
                    radius = radiusPx * 0.54f,
                    center = Offset(centerPx, centerPx),
                    style = Stroke(width = 2f)
                )

                // Railway Minute Track & Hour Tick marks
                for (minute in 0 until 60) {
                    val angleRad = (minute * 6f - 90f) * (PI / 180f).toFloat()
                    val isHour = minute % 5 == 0
                    val tickLength = if (isHour) radiusPx * 0.08f else radiusPx * 0.038f
                    val strokeWidth = if (isHour) 3.5f else 1.8f
                    val strokeColor = if (isHour) numeralColor else numeralColor.copy(alpha = 0.35f)

                    val startX = centerPx + (radiusPx - tickLength) * cos(angleRad)
                    val startY = centerPx + (radiusPx - tickLength) * sin(angleRad)
                    val endX = centerPx + radiusPx * cos(angleRad)
                    val endY = centerPx + radiusPx * sin(angleRad)

                    drawLine(
                        color = strokeColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    // Tiny gold pip at hour marks
                    if (isHour) {
                        val pipRadius = radiusPx * 0.94f
                        val pipX = centerPx + pipRadius * cos(angleRad)
                        val pipY = centerPx + pipRadius * sin(angleRad)
                        drawCircle(
                            color = accentColor,
                            radius = 2.5f,
                            center = Offset(pipX, pipY)
                        )
                    }
                }

                // Draw Roman Numerals I to XII at radius 0.70f (between AM 0.85f and PM 0.54f)
                val paint = android.graphics.Paint().apply {
                    color = numeralColor.hashCode()
                    textSize = if (seniorMode) sizePx * 0.076f else sizePx * 0.066f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
                    isAntiAlias = true
                }

                for (i in 0 until 12) {
                    val angleDeg = i * 30f - 90f
                    val angleRad = angleDeg * (PI / 180f).toFloat()
                    val numeralRadius = radiusPx * 0.70f
                    val x = centerPx + numeralRadius * cos(angleRad)
                    val y = centerPx + numeralRadius * sin(angleRad) + (paint.textSize / 3f)

                    drawContext.canvas.nativeCanvas.drawText(
                        ROMAN_NUMERALS[i],
                        x,
                        y,
                        paint
                    )
                }

                // Hand Drop Shadows for Real Physical Horology Depth
                val shadowOffset = 3f
                val hourRad = hourAngle * (PI / 180f).toFloat()
                val hourHandLength = radiusPx * 0.42f
                drawLine(
                    color = Color.Black.copy(alpha = 0.20f),
                    start = Offset(centerPx + shadowOffset, centerPx + shadowOffset),
                    end = Offset(centerPx + shadowOffset + hourHandLength * cos(hourRad), centerPx + shadowOffset + hourHandLength * sin(hourRad)),
                    strokeWidth = if (seniorMode) 9f else 7f,
                    cap = StrokeCap.Round
                )

                val minuteRad = minuteAngle * (PI / 180f).toFloat()
                val minuteHandLength = radiusPx * 0.60f
                drawLine(
                    color = Color.Black.copy(alpha = 0.20f),
                    start = Offset(centerPx + shadowOffset, centerPx + shadowOffset),
                    end = Offset(centerPx + shadowOffset + minuteHandLength * cos(minuteRad), centerPx + shadowOffset + minuteHandLength * sin(minuteRad)),
                    strokeWidth = if (seniorMode) 6f else 4.5f,
                    cap = StrokeCap.Round
                )

                // Faceted Hour Hand
                drawLine(
                    color = handsColor,
                    start = Offset(centerPx, centerPx),
                    end = Offset(centerPx + hourHandLength * cos(hourRad), centerPx + hourHandLength * sin(hourRad)),
                    strokeWidth = if (seniorMode) 9f else 7f,
                    cap = StrokeCap.Round
                )

                // Faceted Minute Hand
                drawLine(
                    color = handsColor,
                    start = Offset(centerPx, centerPx),
                    end = Offset(centerPx + minuteHandLength * cos(minuteRad), centerPx + minuteHandLength * sin(minuteRad)),
                    strokeWidth = if (seniorMode) 6f else 4.5f,
                    cap = StrokeCap.Round
                )

                // Slender Second Hand with Counterweight
                val secondRad = secondAngle * (PI / 180f).toFloat()
                val secondHandLength = radiusPx * 0.68f
                val counterLength = radiusPx * 0.16f
                // Tail counterweight
                drawLine(
                    color = accentColor,
                    start = Offset(centerPx - counterLength * cos(secondRad), centerPx - counterLength * sin(secondRad)),
                    end = Offset(centerPx + secondHandLength * cos(secondRad), centerPx + secondHandLength * sin(secondRad)),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                // Counterweight circle
                drawCircle(
                    color = accentColor,
                    radius = 4.5f,
                    center = Offset(centerPx - counterLength * 0.75f * cos(secondRad), centerPx - counterLength * 0.75f * sin(secondRad))
                )

                // Multi-tier Center Pin Hub
                drawCircle(
                    color = borderColor,
                    radius = 9f,
                    center = Offset(centerPx, centerPx)
                )
                drawCircle(
                    color = handsColor,
                    radius = 6f,
                    center = Offset(centerPx, centerPx)
                )
                drawCircle(
                    color = accentColor,
                    radius = 3f,
                    center = Offset(centerPx, centerPx)
                )
            }

            // 2. Luxury Complication Window: Date, Time & Status Readout
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (sizeDp * 0.165f))
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = faceColor.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = if (seniorMode) 19.sp else 16.sp,
                        color = numeralColor
                    )
                    Text(
                        text = nextCountdownText,
                        fontSize = if (seniorMode) 13.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            // 3. Medication Markers positioned on AM (outer = 0.85) vs PM (inner = 0.54) tracks
            doseLogs.forEach { doseLog ->
                val isAM = doseLog.isAM
                val markerRadiusRatio = if (isAM) 0.85f else 0.54f

                // Avoid exact collision if multiple medications are at the same time
                val sameTimeDoses = doseLogs.filter {
                    it.scheduledHour == doseLog.scheduledHour &&
                    it.scheduledMinute == doseLog.scheduledMinute &&
                    it.isAM == isAM
                }
                val indexInSameTime = sameTimeDoses.indexOf(doseLog)
                val angleJitter = if (sameTimeDoses.size > 1) {
                    (indexInSameTime - (sameTimeDoses.size - 1) / 2f) * 8f
                } else {
                    0f
                }

                val hourDecimal = (doseLog.scheduledHour % 12) + (doseLog.scheduledMinute / 60f)
                val markerAngleDeg = (hourDecimal * 30f - 90f) + angleJitter
                val markerAngleRad = markerAngleDeg * (PI / 180f).toFloat()

                val markerX = centerPx + (radiusPx * markerRadiusRatio) * cos(markerAngleRad)
                val markerY = centerPx + (radiusPx * markerRadiusRatio) * sin(markerAngleRad)

                val markerSizeDp = if (seniorMode) 38.dp else 34.dp
                val markerSizePx = with(density) { markerSizeDp.toPx() }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (markerX - markerSizePx / 2f).roundToInt(),
                                (markerY - markerSizePx / 2f).roundToInt()
                            )
                        }
                        .size(markerSizeDp)
                        .clip(CircleShape)
                        .background(getMarkerColor(doseLog.status))
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { onMarkerClick(doseLog) }
                        .semantics {
                            contentDescription = "${doseLog.medicationName}, ${doseLog.dosage}, scheduled at ${doseLog.formattedTime} (${if (isAM) "AM track" else "PM track"}). Status: ${doseLog.status.displayName}."
                        },
                    contentAlignment = Alignment.Center
                ) {
                    MarkerIcon(doseLog.status)
                }
            }
        }
    }
}

@Composable
private fun MarkerIcon(status: DoseStatus) {
    when (status) {
        DoseStatus.TAKEN -> Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Taken",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        DoseStatus.DUE_NOW -> Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Due Now",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        DoseStatus.SNOOZED -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Snoozed",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        DoseStatus.SKIPPED -> Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Skipped",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        DoseStatus.MISSED -> Text(
            text = "!",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
        DoseStatus.UPCOMING -> Text(
            text = "●",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

fun getMarkerColor(status: DoseStatus): Color {
    return when (status) {
        DoseStatus.UPCOMING -> StatusUpcoming
        DoseStatus.DUE_NOW -> StatusDueNow
        DoseStatus.TAKEN -> StatusTaken
        DoseStatus.SNOOZED -> StatusSnoozed
        DoseStatus.SKIPPED -> StatusSkipped
        DoseStatus.MISSED -> StatusMissed
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
