package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.ClockStyle

private val MedotLightScheme = lightColorScheme(
    primary = MedotNavy,
    onPrimary = Color.White,
    primaryContainer = MedotSurfaceVariantLight,
    onPrimaryContainer = MedotNavy,
    secondary = MedotTeal,
    onSecondary = Color.White,
    secondaryContainer = MedotSurfaceVariantLight,
    onSecondaryContainer = MedotNavy,
    tertiary = MedotElectricBlue,
    onTertiary = Color.White,
    background = MedotBackgroundLight,
    onBackground = MedotNavy,
    surface = MedotSurfaceLight,
    onSurface = MedotNavy,
    surfaceVariant = MedotSurfaceVariantLight,
    onSurfaceVariant = MedotNavy,
    outline = MedotOutlineLight,
    error = CrimsonAlert,
    onError = Color.White
)

private val MedotDarkScheme = darkColorScheme(
    primary = MedotTealLight,
    onPrimary = MedotNavyDark,
    primaryContainer = MedotNavyLight,
    onPrimaryContainer = Color.White,
    secondary = MedotElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = MedotSurfaceDark,
    onSecondaryContainer = MedotTealLight,
    tertiary = MedotCyanAccent,
    onTertiary = MedotNavyDark,
    background = MedotBackgroundDark,
    onBackground = Color.White,
    surface = MedotSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = MedotSurfaceVariantDark,
    onSurfaceVariant = Color.White,
    outline = MedotOutlineDark,
    error = Color(0xFFFF6B6B),
    onError = Color.Black
)

private val MedotHighContrastScheme = darkColorScheme(
    primary = HighContrastYellow,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF222222),
    onPrimaryContainer = HighContrastYellow,
    secondary = HighContrastYellow,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color.White,
    tertiary = HighContrastGreen,
    onTertiary = Color.Black,
    background = HighContrastBg,
    onBackground = HighContrastText,
    surface = HighContrastSurface,
    onSurface = HighContrastText,
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = HighContrastText,
    outline = Color.White,
    error = HighContrastRed,
    onError = Color.Black
)

@Composable
fun MedotTheme(
    clockStyle: ClockStyle = ClockStyle.CLASSIC_IVORY,
    seniorMode: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        clockStyle == ClockStyle.HIGH_CONTRAST -> MedotHighContrastScheme
        clockStyle == ClockStyle.MIDNIGHT_GOLD || darkTheme -> MedotDarkScheme
        else -> MedotLightScheme
    }

    val typography = if (seniorMode) SeniorTypography else Typography

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun DayDialTheme(
    clockStyle: ClockStyle = ClockStyle.CLASSIC_IVORY,
    seniorMode: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MedotTheme(
        clockStyle = clockStyle,
        seniorMode = seniorMode,
        darkTheme = darkTheme,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MedotTheme(darkTheme = darkTheme, content = content)
}
