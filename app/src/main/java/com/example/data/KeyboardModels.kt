package com.example.data

import androidx.compose.ui.graphics.Color

enum class HybridThemeStyle(
    val displayName: String,
    val subtitle: String,
    val backgroundColor: Color,
    val keyColor: Color,
    val specialKeyColor: Color,
    val actionKeyColor: Color,
    val keyTextColor: Color,
    val hintTextColor: Color,
    val accentColor: Color,
    val utilityBarColor: Color,
    val keyBorderColor: Color
) {
    OLED_OBSIDIAN(
        displayName = "Hybrid Obsidian",
        subtitle = "Sleek dark Samsung × Gboard fusion",
        backgroundColor = Color(0xFF0B0F17),
        keyColor = Color(0xFF1A2233),
        specialKeyColor = Color(0xFF131A28),
        actionKeyColor = Color(0xFF00E5FF),
        keyTextColor = Color(0xFFF1F5FB),
        hintTextColor = Color(0xFF8393AE),
        accentColor = Color(0xFF00E5FF),
        utilityBarColor = Color(0xFF101624),
        keyBorderColor = Color(0xFF28344B)
    ),
    SAMSUNG_ONE_LIGHT(
        displayName = "One UI Crisp",
        subtitle = "Clean daylight Samsung-inspired keys",
        backgroundColor = Color(0xFFE7EBF2),
        keyColor = Color(0xFFFFFFFF),
        specialKeyColor = Color(0xFFD5DCE8),
        actionKeyColor = Color(0xFF2962FF),
        keyTextColor = Color(0xFF111827),
        hintTextColor = Color(0xFF64748B),
        accentColor = Color(0xFF2962FF),
        utilityBarColor = Color(0xFFF2F5FA),
        keyBorderColor = Color(0xFFCBD5E1)
    ),
    GBOARD_MATERIAL_BLUE(
        displayName = "Gboard Sapphire",
        subtitle = "Material You rounded pill aesthetic",
        backgroundColor = Color(0xFF171E2E),
        keyColor = Color(0xFF253047),
        specialKeyColor = Color(0xFF1D263A),
        actionKeyColor = Color(0xFF8AB4F8),
        keyTextColor = Color(0xFFE8F0FE),
        hintTextColor = Color(0xFF9AA7C0),
        accentColor = Color(0xFF8AB4F8),
        utilityBarColor = Color(0xFF1A2234),
        keyBorderColor = Color(0xFF33415C)
    ),
    MIDNIGHT_EMERALD(
        displayName = "Midnight Mint",
        subtitle = "Deep forest slate with mint sparks",
        backgroundColor = Color(0xFF0B1412),
        keyColor = Color(0xFF152622),
        specialKeyColor = Color(0xFF101E1B),
        actionKeyColor = Color(0xFF00E676),
        keyTextColor = Color(0xFFE8F5F0),
        hintTextColor = Color(0xFF7FA398),
        accentColor = Color(0xFF00E676),
        utilityBarColor = Color(0xFF0F1B18),
        keyBorderColor = Color(0xFF233D37)
    ),
    AMOLED_ECO(
        displayName = "AMOLED Eco Saver",
        subtitle = "True #000000 pixels for max battery life",
        backgroundColor = Color(0xFF000000),
        keyColor = Color(0xFF0B0B0B),
        specialKeyColor = Color(0xFF060606),
        actionKeyColor = Color(0xFF00B8D4),
        keyTextColor = Color(0xFFD6E0EB),
        hintTextColor = Color(0xFF6B7280),
        accentColor = Color(0xFF00B8D4),
        utilityBarColor = Color(0xFF000000),
        keyBorderColor = Color(0xFF1F242D)
    )
}

enum class OneHandedMode(val label: String) {
    OFF("Full Width"),
    LEFT("Left Hand"),
    RIGHT("Right Hand")
}

enum class KeyboardLanguage(val code: String, val label: String, val speechLocale: String) {
    ENGLISH_US("EN", "English (US)", "en-US"),
    HINGLISH("HI-EN", "Hinglish", "en-IN"),
    HINDI_DEVANAGARI("हिं", "हिन्दी", "hi-IN")
}

enum class KeyboardPanel {
    ALPHA,
    SYMBOLS_1,
    SYMBOLS_2,
    EMOJI,
    CLIPBOARD
}

enum class ShiftState {
    OFF,
    SINGLE,
    CAPS_LOCK
}

data class KeyboardSettings(
    val themeStyle: HybridThemeStyle = HybridThemeStyle.OLED_OBSIDIAN,
    val showNumberRow: Boolean = true,
    val glideTypingEnabled: Boolean = true,
    val spacebarCursorEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val soundOnKeypress: Boolean = false,
    val softMicAlwaysVisible: Boolean = true,
    val autoCapitalize: Boolean = true,
    val oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    val language: KeyboardLanguage = KeyboardLanguage.ENGLISH_US,
    val heightScale: Float = 1.0f,
    val ecoBatteryMode: Boolean = false,
    val autoEcoOnLowBattery: Boolean = true
) {
    val effectiveTheme: HybridThemeStyle
        get() = if (ecoBatteryMode) HybridThemeStyle.AMOLED_ECO else themeStyle
}

data class BatteryTelemetry(
    val levelPercent: Int = 85,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false
)
