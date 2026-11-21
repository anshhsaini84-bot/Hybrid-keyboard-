package com.example.data

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class ResolvedKeyboardColors(
    val backgroundColor: Color,
    val keyColor: Color,
    val specialKeyColor: Color,
    val actionKeyColor: Color,
    val keyTextColor: Color,
    val hintTextColor: Color,
    val accentColor: Color,
    val utilityBarColor: Color,
    val keyBorderColor: Color,
    val isLight: Boolean
)

enum class HybridThemeStyle(
    val displayName: String,
    val shortName: String,
    val isLight: Boolean,
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
    SYSTEM_AUTO(
        displayName = "System Default",
        shortName = "System",
        isLight = false,
        backgroundColor = Color(0xFF141820),
        keyColor = Color(0xFF232A38),
        specialKeyColor = Color(0xFF1B212E),
        actionKeyColor = Color(0xFF4F8DFF),
        keyTextColor = Color(0xFFF3F6FC),
        hintTextColor = Color(0xFF94A3B8),
        accentColor = Color(0xFF4F8DFF),
        utilityBarColor = Color(0xFF181E2A),
        keyBorderColor = Color(0xFF313B4F)
    ),
    SYSTEM_LIGHT(
        displayName = "System Light",
        shortName = "Daylight",
        isLight = true,
        backgroundColor = Color(0xFFECEFF4),
        keyColor = Color(0xFFFFFFFF),
        specialKeyColor = Color(0xFFDCE2EC),
        actionKeyColor = Color(0xFF1A73E8),
        keyTextColor = Color(0xFF111827),
        hintTextColor = Color(0xFF64748B),
        accentColor = Color(0xFF1A73E8),
        utilityBarColor = Color(0xFFF4F6FA),
        keyBorderColor = Color(0xFFCBD5E1)
    ),
    SYSTEM_DARK(
        displayName = "System Dark",
        shortName = "Graphite",
        isLight = false,
        backgroundColor = Color(0xFF16171B),
        keyColor = Color(0xFF282A30),
        specialKeyColor = Color(0xFF1F2127),
        actionKeyColor = Color(0xFF8AB4F8),
        keyTextColor = Color(0xFFF1F3F4),
        hintTextColor = Color(0xFF9AA0A6),
        accentColor = Color(0xFF8AB4F8),
        utilityBarColor = Color(0xFF1B1C20),
        keyBorderColor = Color(0xFF363942)
    ),
    OLED_OBSIDIAN(
        displayName = "Hybrid Obsidian",
        shortName = "Obsidian",
        isLight = false,
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
        shortName = "One UI",
        isLight = true,
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
    SAMSUNG_ONE_DARK(
        displayName = "One UI Midnight",
        shortName = "Galaxy",
        isLight = false,
        backgroundColor = Color(0xFF111318),
        keyColor = Color(0xFF242730),
        specialKeyColor = Color(0xFF1A1D24),
        actionKeyColor = Color(0xFF3B82F6),
        keyTextColor = Color(0xFFF3F4F6),
        hintTextColor = Color(0xFF9CA3AF),
        accentColor = Color(0xFF3B82F6),
        utilityBarColor = Color(0xFF161920),
        keyBorderColor = Color(0xFF323746)
    ),
    GBOARD_MATERIAL_BLUE(
        displayName = "Gboard Sapphire",
        shortName = "Sapphire",
        isLight = false,
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
    GBOARD_PASTEL_SKY(
        displayName = "Gboard Sky",
        shortName = "Sky",
        isLight = true,
        backgroundColor = Color(0xFFE3EFFD),
        keyColor = Color(0xFFF8FBFF),
        specialKeyColor = Color(0xFFCDE0FA),
        actionKeyColor = Color(0xFF0B57D0),
        keyTextColor = Color(0xFF0B1F3A),
        hintTextColor = Color(0xFF4D6A92),
        accentColor = Color(0xFF0B57D0),
        utilityBarColor = Color(0xFFECF4FE),
        keyBorderColor = Color(0xFFBFD7F7)
    ),
    MIDNIGHT_EMERALD(
        displayName = "Midnight Mint",
        shortName = "Mint",
        isLight = false,
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
    ROYAL_AMETHYST(
        displayName = "Royal Amethyst",
        shortName = "Amethyst",
        isLight = false,
        backgroundColor = Color(0xFF150F21),
        keyColor = Color(0xFF251B38),
        specialKeyColor = Color(0xFF1C142B),
        actionKeyColor = Color(0xFFC084FC),
        keyTextColor = Color(0xFFF5EEFF),
        hintTextColor = Color(0xFFA28EC0),
        accentColor = Color(0xFFC084FC),
        utilityBarColor = Color(0xFF1A1229),
        keyBorderColor = Color(0xFF382954)
    ),
    SUNSET_AMBER(
        displayName = "Sunset Amber",
        shortName = "Amber",
        isLight = false,
        backgroundColor = Color(0xFF1A120D),
        keyColor = Color(0xFF2C1F17),
        specialKeyColor = Color(0xFF221711),
        actionKeyColor = Color(0xFFFFB300),
        keyTextColor = Color(0xFFFFF6EB),
        hintTextColor = Color(0xFFB8967B),
        accentColor = Color(0xFFFFB300),
        utilityBarColor = Color(0xFF201610),
        keyBorderColor = Color(0xFF453124)
    ),
    CRIMSON_CARBON(
        displayName = "Crimson Carbon",
        shortName = "Crimson",
        isLight = false,
        backgroundColor = Color(0xFF121012),
        keyColor = Color(0xFF231D20),
        specialKeyColor = Color(0xFF1A1517),
        actionKeyColor = Color(0xFFFF4D6D),
        keyTextColor = Color(0xFFFAF0F2),
        hintTextColor = Color(0xFFA68990),
        accentColor = Color(0xFFFF4D6D),
        utilityBarColor = Color(0xFF171315),
        keyBorderColor = Color(0xFF3A2D33)
    ),
    CYBER_NEON(
        displayName = "Cyber Neon",
        shortName = "Cyber",
        isLight = false,
        backgroundColor = Color(0xFF0A081A),
        keyColor = Color(0xFF171236),
        specialKeyColor = Color(0xFF110D28),
        actionKeyColor = Color(0xFFFF2E97),
        keyTextColor = Color(0xFFEAFAFF),
        hintTextColor = Color(0xFF7B88C6),
        accentColor = Color(0xFF00F0FF),
        utilityBarColor = Color(0xFF0F0B24),
        keyBorderColor = Color(0xFF2E2469)
    ),
    OCEAN_TEAL(
        displayName = "Ocean Teal",
        shortName = "Ocean",
        isLight = false,
        backgroundColor = Color(0xFF081A20),
        keyColor = Color(0xFF102E38),
        specialKeyColor = Color(0xFF0C232B),
        actionKeyColor = Color(0xFF2DD4BF),
        keyTextColor = Color(0xFFE6FAFC),
        hintTextColor = Color(0xFF78A9B5),
        accentColor = Color(0xFF2DD4BF),
        utilityBarColor = Color(0xFF0B2027),
        keyBorderColor = Color(0xFF1B4957)
    ),
    NORDIC_FROST(
        displayName = "Nordic Frost",
        shortName = "Frost",
        isLight = true,
        backgroundColor = Color(0xFFDFE6EE),
        keyColor = Color(0xFFF6F9FC),
        specialKeyColor = Color(0xFFCCD7E4),
        actionKeyColor = Color(0xFF0284C7),
        keyTextColor = Color(0xFF1E293B),
        hintTextColor = Color(0xFF64748B),
        accentColor = Color(0xFF0284C7),
        utilityBarColor = Color(0xFFE8EEF5),
        keyBorderColor = Color(0xFFBCCAD9)
    ),
    ROSE_QUARTZ(
        displayName = "Rose Quartz",
        shortName = "Rose",
        isLight = true,
        backgroundColor = Color(0xFFF5E6EB),
        keyColor = Color(0xFFFFFBFD),
        specialKeyColor = Color(0xFFEAD2DB),
        actionKeyColor = Color(0xFFE11D48),
        keyTextColor = Color(0xFF3B1321),
        hintTextColor = Color(0xFF8C5A6C),
        accentColor = Color(0xFFE11D48),
        utilityBarColor = Color(0xFFF9EEF2),
        keyBorderColor = Color(0xFFE2C2CE)
    ),
    MATCHA_CREAM(
        displayName = "Matcha Cream",
        shortName = "Matcha",
        isLight = true,
        backgroundColor = Color(0xFFE6EEE4),
        keyColor = Color(0xFFF9FCF8),
        specialKeyColor = Color(0xFFD2E0CE),
        actionKeyColor = Color(0xFF2E7D32),
        keyTextColor = Color(0xFF1B2E1E),
        hintTextColor = Color(0xFF5C7560),
        accentColor = Color(0xFF2E7D32),
        utilityBarColor = Color(0xFFEDF4EB),
        keyBorderColor = Color(0xFFC2D4BD)
    ),
    AMOLED_ECO(
        displayName = "AMOLED Eco",
        shortName = "AMOLED",
        isLight = false,
        backgroundColor = Color(0xFF000000),
        keyColor = Color(0xFF0E1014),
        specialKeyColor = Color(0xFF07080A),
        actionKeyColor = Color(0xFF00B8D4),
        keyTextColor = Color(0xFFE2E8F0),
        hintTextColor = Color(0xFF717D8E),
        accentColor = Color(0xFF00B8D4),
        utilityBarColor = Color(0xFF000000),
        keyBorderColor = Color(0xFF1E242E)
    );

    fun toResolvedColors(): ResolvedKeyboardColors = ResolvedKeyboardColors(
        backgroundColor = backgroundColor,
        keyColor = keyColor,
        specialKeyColor = specialKeyColor,
        actionKeyColor = actionKeyColor,
        keyTextColor = keyTextColor,
        hintTextColor = hintTextColor,
        accentColor = accentColor,
        utilityBarColor = utilityBarColor,
        keyBorderColor = keyBorderColor,
        isLight = isLight
    )
}

@Composable
fun resolveKeyboardColors(settings: KeyboardSettings): ResolvedKeyboardColors {
    if (settings.ecoBatteryMode) {
        return HybridThemeStyle.AMOLED_ECO.toResolvedColors()
    }
    if (settings.themeStyle == HybridThemeStyle.SYSTEM_AUTO) {
        val isDark = isSystemInDarkTheme()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = LocalContext.current
            val scheme = if (isDark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
            return ResolvedKeyboardColors(
                backgroundColor = scheme.surfaceContainerLow,
                keyColor = if (isDark) scheme.surfaceContainerHigh else scheme.surface,
                specialKeyColor = scheme.surfaceContainerHighest,
                actionKeyColor = scheme.primary,
                keyTextColor = scheme.onSurface,
                hintTextColor = scheme.onSurfaceVariant,
                accentColor = scheme.primary,
                utilityBarColor = scheme.surfaceContainer,
                keyBorderColor = scheme.outlineVariant,
                isLight = !isDark
            )
        }
        return (if (isDark) HybridThemeStyle.SYSTEM_DARK else HybridThemeStyle.SYSTEM_LIGHT).toResolvedColors()
    }
    return settings.themeStyle.toResolvedColors()
}

enum class OneHandedMode(val label: String) {
    OFF("Full Width"),
    LEFT("Left Hand"),
    RIGHT("Right Hand")
}

enum class KeyboardLanguage(val code: String, val label: String, val nativeLabel: String, val speechLocale: String) {
    ENGLISH_US("EN", "English", "English", "en-US"),
    HINGLISH("HI-EN", "Hinglish", "Hinglish", "en-IN"),
    HINDI_DEVANAGARI("हिं", "Hindi", "हिन्दी", "hi-IN"),
    SPANISH("ES", "Spanish", "Español", "es-ES"),
    FRENCH("FR", "French", "Français", "fr-FR"),
    GERMAN("DE", "German", "Deutsch", "de-DE"),
    ARABIC("عر", "Arabic", "العربية", "ar-SA"),
    RUSSIAN("RU", "Russian", "Русский", "ru-RU"),
    BENGALI("বাং", "Bengali", "বাংলা", "bn-IN"),
    MARATHI("मरा", "Marathi", "मराठी", "mr-IN"),
    TAMIL("தமி", "Tamil", "தமிழ்", "ta-IN")
}

enum class ToolbarTool(val label: String) {
    CLIPBOARD("Clipboard"),
    TEXT_EDIT("Text Edit"),
    TRANSLATE("Convert"),
    RESIZE("Resize"),
    EMOJI("Emoji"),
    ONE_HANDED("One-Hand"),
    FLOATING("Floating"),
    LANGUAGE("Language"),
    ECO("Eco Saver"),
    SETTINGS("Settings")
}

enum class KeyboardPanel {
    ALPHA,
    SYMBOLS_1,
    SYMBOLS_2,
    EMOJI,
    CLIPBOARD,
    TEXT_EDIT,
    TRANSLATE,
    MORE_TOOLS,
    RESIZE
}

enum class TextEditCommand {
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    JUMP_START,
    JUMP_END,
    SELECT_ALL,
    CUT,
    COPY,
    PASTE,
    UNDO
}

enum class ShiftState {
    OFF,
    SINGLE,
    CAPS_LOCK
}

val DEFAULT_PINNED_TOOLS = listOf(
    ToolbarTool.CLIPBOARD,
    ToolbarTool.TEXT_EDIT,
    ToolbarTool.TRANSLATE,
    ToolbarTool.RESIZE
)

data class KeyboardSettings(
    val themeStyle: HybridThemeStyle = HybridThemeStyle.SYSTEM_AUTO,
    val showNumberRow: Boolean = true,
    val glideTypingEnabled: Boolean = true,
    val spacebarCursorEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val hapticStrengthMs: Int = 12,
    val soundOnKeypress: Boolean = false,
    val softMicAlwaysVisible: Boolean = true,
    val autoCapitalize: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val swipeBackspaceDeleteWord: Boolean = true,
    val floatingMode: Boolean = false,
    val autoCaptureClipboard: Boolean = true,
    val oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    val language: KeyboardLanguage = KeyboardLanguage.ENGLISH_US,
    val heightScale: Float = 1.0f,
    val pinnedToolbarTools: List<ToolbarTool> = DEFAULT_PINNED_TOOLS,
    val ecoBatteryMode: Boolean = false,
    val autoEcoOnLowBattery: Boolean = true
)

data class BatteryTelemetry(
    val levelPercent: Int = 85,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false
)
