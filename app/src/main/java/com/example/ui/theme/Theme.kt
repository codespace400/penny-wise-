package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary = CoralRed,
    onSecondary = SurfaceWhite,
    secondaryContainer = CoralLight,
    onSecondaryContainer = CoralDark,
    tertiary = AccentBlue,
    background = BackgroundLight,
    onBackground = CharcoalText,
    surface = SurfaceWhite,
    onSurface = CharcoalText,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = CharcoalMuted,
    outline = CharcoalBorder,
    error = CoralRed,
    onError = SurfaceWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenMedium,
    onPrimary = CharcoalText,
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenLight,
    secondary = CoralRed,
    onSecondary = SurfaceWhite,
    secondaryContainer = CoralDark,
    onSecondaryContainer = CoralLight,
    tertiary = AccentCyan,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    error = CoralRed,
    onError = SurfaceWhite
)

@Composable
fun PennyWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
