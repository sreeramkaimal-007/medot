package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.GoldLight
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SageGreen
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TwistToConfirmClock(
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    // Start angle at 6 o'clock (180 degrees)
    var currentAngleDeg by remember { mutableFloatStateOf(180f) }
    var isConfirmed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val sizePx = constraints.maxWidth.toFloat().coerceAtMost(constraints.maxHeight.toFloat())
        val density = LocalDensity.current
        val sizeDp = with(density) { sizePx.toDp() }
        val centerPx = sizePx / 2f
        val radiusPx = sizePx * 0.44f

        Canvas(
            modifier = Modifier
                .size(sizeDp)
                .pointerInput(isConfirmed) {
                    if (isConfirmed) return@pointerInput
                    detectDragGestures { change, _ ->
                        change.consume()
                        val touchX = change.position.x - centerPx
                        val touchY = change.position.y - centerPx
                        var angle = (atan2(touchY.toDouble(), touchX.toDouble()) * (180.0 / PI)).toFloat()
                        // Normalize angle where top (12 o'clock) is 0 / 360 deg
                        angle = (angle + 90f + 360f) % 360f

                        currentAngleDeg = angle

                        // If user rotated within 18 degrees of 12 o'clock (0 or 360)
                        if (angle < 18f || angle > 342f) {
                            if (!isConfirmed) {
                                isConfirmed = true
                                currentAngleDeg = 0f
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(100L)
                                }
                                onConfirmed()
                            }
                        }
                    }
                }
        ) {
            // Clock Face Outer Border & Disc
            drawCircle(
                color = if (isConfirmed) SageGreen else AntiqueGold,
                radius = radiusPx + 6f,
                center = Offset(centerPx, centerPx)
            )
            drawCircle(
                color = IvoryBackground,
                radius = radiusPx,
                center = Offset(centerPx, centerPx)
            )

            // Goal zone at 12 o'clock
            val goalRadius = radiusPx * 0.90f
            drawCircle(
                color = if (isConfirmed) SageGreen.copy(alpha = 0.4f) else AntiqueGold.copy(alpha = 0.25f),
                radius = 32f,
                center = Offset(centerPx, centerPx - goalRadius)
            )

            // Hour marks & XII highlight
            val paint = android.graphics.Paint().apply {
                color = if (isConfirmed) SageGreen.hashCode() else DeepNavy.hashCode()
                textSize = sizePx * 0.08f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("XII", centerPx, centerPx - radiusPx * 0.70f, paint)
            drawContext.canvas.nativeCanvas.drawText("VI", centerPx, centerPx + radiusPx * 0.85f, paint)

            // Dragged Hand Line
            val angleRad = ((currentAngleDeg - 90f) * (PI / 180f)).toFloat()
            val handLength = radiusPx * 0.82f
            drawLine(
                color = if (isConfirmed) SageGreen else DeepNavy,
                start = Offset(centerPx, centerPx),
                end = Offset(centerPx + handLength * cos(angleRad), centerPx + handLength * sin(angleRad)),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )

            // Center Pin
            drawCircle(
                color = if (isConfirmed) SageGreen else AntiqueGold,
                radius = 16f,
                center = Offset(centerPx, centerPx)
            )
        }

        // Draggable Knob at hand tip (min 56dp touch target)
        val knobDistance = radiusPx * 0.82f
        val angleRad = ((currentAngleDeg - 90f) * (PI / 180f)).toFloat()
        val knobX = centerPx + knobDistance * cos(angleRad)
        val knobY = centerPx + knobDistance * sin(angleRad)
        val knobSize = 56.dp

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (knobX - with(density) { (knobSize / 2).toPx() }).roundToInt(),
                        (knobY - with(density) { (knobSize / 2).toPx() }).roundToInt()
                    )
                }
                .size(knobSize)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(if (isConfirmed) SageGreen else AntiqueGold)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isConfirmed) {
                Icon(Icons.Default.Check, contentDescription = "Confirmed", tint = Color.White)
            } else {
                Icon(Icons.Default.PanTool, contentDescription = "Drag hand to XII", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}
