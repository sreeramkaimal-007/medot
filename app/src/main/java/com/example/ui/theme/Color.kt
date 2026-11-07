package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// medot Official Brand Palette - Deep Royal Navy, Medical Cyan/Teal, Electric Blue
val MedotNavy = Color(0xFF0A2558)
val MedotNavyDark = Color(0xFF06183B)
val MedotNavyLight = Color(0xFF133E87)
val MedotTeal = Color(0xFF00A8B5)
val MedotTealDark = Color(0xFF007E88)
val MedotTealLight = Color(0xFF26C6DA)
val MedotElectricBlue = Color(0xFF166FE5)
val MedotCyanAccent = Color(0xFF00C2CB)

// Surfaces and Backgrounds
val MedotBackgroundLight = Color(0xFFF6FAFB)
val MedotSurfaceLight = Color(0xFFFFFFFF)
val MedotSurfaceVariantLight = Color(0xFFE9F4F6)
val MedotOutlineLight = Color(0xFF7E9DAF)

val MedotBackgroundDark = Color(0xFF071428)
val MedotSurfaceDark = Color(0xFF0D223F)
val MedotSurfaceVariantDark = Color(0xFF153359)
val MedotOutlineDark = Color(0xFF436B95)

// Aliases for unified app usage
val DeepNavy = MedotNavy
val NavyLight = MedotNavyLight
val AntiqueGold = MedotTeal
val GoldLight = MedotTealLight
val GoldDark = MedotTealDark
val SageGreen = Color(0xFF009688)
val CrimsonAlert = Color(0xFFD32F2F)
val SlateMuted = MedotOutlineLight
val IvoryBackground = MedotBackgroundLight
val IvorySurface = MedotSurfaceLight
val IvorySurfaceVariant = MedotSurfaceVariantLight
val DividerColor = Color(0xFFD5E6EA)

// Status Colors with High Contrast
val StatusUpcoming = Color(0xFF0B63C5)
val StatusDueNow = Color(0xFFE65100)
val StatusTaken = Color(0xFF009688)
val StatusSnoozed = Color(0xFF6B4CA2)
val StatusSkipped = Color(0xFF607D8B)
val StatusMissed = Color(0xFFD32F2F)

// High Contrast Accessibility Palette
val HighContrastBg = Color(0xFF000000)
val HighContrastSurface = Color(0xFF121212)
val HighContrastText = Color(0xFFFFFFFF)
val HighContrastYellow = Color(0xFFFFD700)
val HighContrastGreen = Color(0xFF00E676)
val HighContrastRed = Color(0xFFFF5252)

// Clock Specific Style Themes
object ClockStyles {
    val ClassicIvoryFace = Color(0xFFFFFFFF)
    val ClassicIvoryBorder = Color(0xFF0A2558)
    val ClassicIvoryNumerals = Color(0xFF0A2558)
    val ClassicIvoryHands = Color(0xFF0A2558)

    val MidnightGoldFace = Color(0xFF071428)
    val MidnightGoldBorder = Color(0xFF00A8B5)
    val MidnightGoldNumerals = Color(0xFFE1F5FE)
    val MidnightGoldHands = Color(0xFF00C2CB)

    val SoftBlueFace = Color(0xFFEBF7F9)
    val SoftBlueBorder = Color(0xFF00A8B5)
    val SoftBlueNumerals = Color(0xFF0A2558)
    val SoftBlueHands = Color(0xFF166FE5)

    val HighContrastFace = Color(0xFF000000)
    val HighContrastBorder = Color(0xFFFFFFFF)
    val HighContrastNumerals = Color(0xFFFFFFFF)
    val HighContrastHands = Color(0xFFFFD700)
}
