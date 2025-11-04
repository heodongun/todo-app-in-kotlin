package com.heodongun.ugoal.ui.theme

import androidx.compose.ui.graphics.Color

// Toss-inspired color palette
val TossBlue = Color(0xFF3182F6)
val TossBlueLight = Color(0xFF5BA3FF)
val TossBlueDark = Color(0xFF1B64DA)

val TossGray50 = Color(0xFFF9FAFB)
val TossGray100 = Color(0xFFF2F4F6)
val TossGray200 = Color(0xFFE5E8EB)
val TossGray300 = Color(0xFFD1D6DB)
val TossGray400 = Color(0xFFB0B8C1)
val TossGray500 = Color(0xFF8B95A1)
val TossGray600 = Color(0xFF6B7684)
val TossGray700 = Color(0xFF4E5968)
val TossGray800 = Color(0xFF333D4B)
val TossGray900 = Color(0xFF191F28)

val BackgroundWhite = Color(0xFFFFFFFF)
val SurfaceWhite = Color(0xFFFAFAFA)

// Status colors
val SuccessGreen = Color(0xFF00C853)
val WarningYellow = Color(0xFFFFAB00)
val WarningOrange = Color(0xFFFFA726)
val ErrorRed = Color(0xFFFF5252)

// Goal colors (for user selection)
val GoalColorBlue = Color(0xFF3182F6)
val GoalColorPurple = Color(0xFF7C4DFF)
val GoalColorPink = Color(0xFFFF4081)
val GoalColorOrange = Color(0xFFFF6E40)
val GoalColorGreen = Color(0xFF00C853)
val GoalColorTeal = Color(0xFF00BFA5)
val Orange500 = Color(0xFFFF9800) // Added Orange500

fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        TossBlue
    }
}
