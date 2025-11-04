package com.heodongun.ugoal.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TossBlue,
    onPrimary = BackgroundWhite,
    primaryContainer = TossBlueLight,
    onPrimaryContainer = TossBlueDark,
    
    secondary = TossGray600,
    onSecondary = BackgroundWhite,
    secondaryContainer = TossGray100,
    onSecondaryContainer = TossGray800,
    
    tertiary = TossGray400,
    onTertiary = BackgroundWhite,
    tertiaryContainer = TossGray50,
    onTertiaryContainer = TossGray700,
    
    error = ErrorRed,
    onError = BackgroundWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = BackgroundWhite,
    onBackground = TossGray900,
    
    surface = SurfaceWhite,
    onSurface = TossGray900,
    surfaceVariant = TossGray100,
    onSurfaceVariant = TossGray700,
    
    outline = TossGray300,
    outlineVariant = TossGray200
)

@Composable
fun UgoalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Force light theme (Toss style)
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UgoalTypography,
        content = content
    )
}
