package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HybridCyan,
    onPrimary = Color(0xFF001F26),
    primaryContainer = Color(0xFF003642),
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = HybridIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E2B58),
    onSecondaryContainer = Color(0xFFDCE1FF),
    tertiary = HybridEmerald,
    onTertiary = Color(0xFF00210E),
    background = DeepObsidian,
    onBackground = Color(0xFFE6EDF8),
    surface = SlateSurface,
    onSurface = Color(0xFFE6EDF8),
    surfaceVariant = SlateCard,
    onSurfaceVariant = Color(0xFF9BA8C0),
    outline = SlateBorder,
    error = HybridCoral
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006778),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = HybridIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E5FF),
    onSecondaryContainer = Color(0xFF0B154B),
    tertiary = Color(0xFF006D36),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF10141C),
    surface = LightSurface,
    onSurface = Color(0xFF10141C),
    surfaceVariant = LightCard,
    onSurfaceVariant = Color(0xFF4A5568),
    outline = LightBorder,
    error = Color(0xFFD32F2F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark studio aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
