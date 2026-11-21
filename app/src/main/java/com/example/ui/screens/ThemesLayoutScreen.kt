package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HybridThemeStyle
import com.example.data.KeyboardLanguage
import com.example.data.OneHandedMode
import com.example.data.ToolbarTool
import com.example.ui.HybridStudioUiState
import com.example.ui.HybridViewModel
import com.example.ui.keyboard.iconVector
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemesLayoutScreen(
    uiState: HybridStudioUiState,
    viewModel: HybridViewModel,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val battery = uiState.battery
    val allThemes = HybridThemeStyle.entries
    val firstRowThemes = allThemes.take(9)
    val secondRowThemes = allThemes.drop(9)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // 1. 18 Curated Themes (2 clean horizontal rows, System Default first)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Themes (${allThemes.size})")
                Text(
                    text = settings.themeStyle.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ThemeSwatchRow(
                themes = firstRowThemes,
                selectedStyle = settings.themeStyle,
                ecoActive = settings.ecoBatteryMode,
                onSelect = viewModel::setThemeStyle
            )
            ThemeSwatchRow(
                themes = secondRowThemes,
                selectedStyle = settings.themeStyle,
                ecoActive = settings.ecoBatteryMode,
                onSelect = viewModel::setThemeStyle
            )
        }

        // 2. Multi-Language (11 Languages) & Keyboard Height Resize
        SettingsGroupCard(title = "Languages (${KeyboardLanguage.entries.size}) & Size") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeyboardLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = settings.language == lang,
                        onClick = { viewModel.setLanguage(lang) },
                        label = { Text("${lang.nativeLabel} (${lang.code})", fontSize = 12.sp) },
                        modifier = Modifier.testTag("lang_chip_${lang.name}")
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OneHandedMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.oneHandedMode == mode,
                        onClick = { viewModel.setOneHandedMode(mode) },
                        label = { Text(mode.label, fontSize = 12.sp) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Keyboard Height", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${(settings.heightScale * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = settings.heightScale,
                onValueChange = viewModel::setHeightScale,
                valueRange = 0.72f..1.35f
            )
        }

        // 3. Customize Top Toolbar (Reorder & Pin up to 5 primary tools)
        SettingsGroupCard(title = "Top Toolbar (Pin & Reorder)") {
            val pinned = settings.pinnedToolbarTools
            Text(
                text = "Active top bar tools (others stay hidden inside ⊞ More Tools):",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pinned.forEachIndexed { idx, tool ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (idx > 0) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = "Move Left",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val next = pinned.toMutableList()
                                        next[idx] = next[idx - 1].also { next[idx - 1] = next[idx] }
                                        viewModel.updatePinnedToolbarTools(next)
                                    }
                            )
                        }
                        Icon(
                            imageVector = tool.iconVector(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = tool.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (idx < pinned.lastIndex) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "Move Right",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val next = pinned.toMutableList()
                                        next[idx] = next[idx + 1].also { next[idx + 1] = next[idx] }
                                        viewModel.updatePinnedToolbarTools(next)
                                    }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ToolbarTool.entries.forEach { tool ->
                    val isPinned = tool in pinned
                    FilterChip(
                        selected = isPinned,
                        onClick = {
                            val next = if (isPinned) {
                                if (pinned.size > 1) pinned.filterNot { it == tool } else pinned
                            } else {
                                if (pinned.size < 5) pinned + tool else pinned.dropLast(1) + tool
                            }
                            viewModel.updatePinnedToolbarTools(next)
                        },
                        leadingIcon = {
                            Icon(tool.iconVector(), null, Modifier.size(15.dp))
                        },
                        label = { Text(tool.label, fontSize = 11.sp) }
                    )
                }
            }
        }

        // 4. Typing, Vibration & Feel
        SettingsGroupCard(title = "Typing, Vibration & Feel") {
            SettingToggleRow(
                title = "Vibration Feedback",
                subtitle = "Turn keypress vibration on or off anytime",
                checked = settings.hapticFeedbackEnabled,
                onCheckedChange = viewModel::toggleHaptics
            )
            AnimatedVisibility(visible = settings.hapticFeedbackEnabled) {
                Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vibration Strength",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val strengthLabel = when {
                            settings.hapticStrengthMs <= 10 -> "Light (${settings.hapticStrengthMs}ms)"
                            settings.hapticStrengthMs <= 20 -> "Crisp (${settings.hapticStrengthMs}ms)"
                            else -> "Strong (${settings.hapticStrengthMs}ms)"
                        }
                        Text(
                            text = strengthLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = settings.hapticStrengthMs.toFloat(),
                        onValueChange = { viewModel.setHapticStrength(it.roundToInt()) },
                        valueRange = 5f..30f
                    )
                }
            }
            GroupDivider()
            SettingToggleRow(
                title = "Keypress Sound",
                subtitle = "Subtle audio click",
                checked = settings.soundOnKeypress,
                onCheckedChange = viewModel::toggleSound
            )
            GroupDivider()
            SettingToggleRow(
                title = "Number Row",
                subtitle = "Show 1–0 row above letters",
                checked = settings.showNumberRow,
                onCheckedChange = viewModel::toggleNumberRow
            )
            GroupDivider()
            SettingToggleRow(
                title = "Glide Typing",
                subtitle = "Swipe across keys to type",
                checked = settings.glideTypingEnabled,
                onCheckedChange = viewModel::toggleGlideTyping
            )
            GroupDivider()
            SettingToggleRow(
                title = "Spacebar Cursor Control",
                subtitle = "Slide to move cursor · Hold for languages",
                checked = settings.spacebarCursorEnabled,
                onCheckedChange = viewModel::toggleSpacebarCursor
            )
            GroupDivider()
            SettingToggleRow(
                title = "Swipe Backspace Delete",
                subtitle = "Slide left on backspace to erase words",
                checked = settings.swipeBackspaceDeleteWord,
                onCheckedChange = viewModel::toggleSwipeBackspaceWord
            )
            GroupDivider()
            SettingToggleRow(
                title = "Double-Space Period",
                subtitle = "Double tap space for \". \"",
                checked = settings.doubleSpacePeriod,
                onCheckedChange = viewModel::toggleDoubleSpacePeriod
            )
            GroupDivider()
            SettingToggleRow(
                title = "Auto-Capitalize",
                subtitle = "Capitalize start of sentences",
                checked = settings.autoCapitalize,
                onCheckedChange = viewModel::toggleAutoCapitalize
            )
            GroupDivider()
            SettingToggleRow(
                title = "Floating Keyboard",
                subtitle = "Compact floating deck",
                checked = settings.floatingMode,
                onCheckedChange = { viewModel.toggleFloatingMode(it) }
            )
            GroupDivider()
            SettingToggleRow(
                title = "Soft Voice Mic",
                subtitle = "Keep voice mic button on side of toolbar",
                checked = settings.softMicAlwaysVisible,
                onCheckedChange = viewModel::toggleSoftMicVisible
            )
            GroupDivider()
            SettingToggleRow(
                title = "Auto-Capture Clipboard",
                subtitle = "Save copied text, code & screenshots",
                checked = settings.autoCaptureClipboard,
                onCheckedChange = viewModel::toggleAutoCaptureClipboard
            )
        }

        // 5. Compact Low-Battery Eco Section
        SettingsGroupCard(
            title = "Battery Saver",
            trailingBadge = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (uiState.effectiveEcoActive) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BatterySaver,
                            contentDescription = null,
                            tint = if (uiState.effectiveEcoActive) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${battery.levelPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.effectiveEcoActive) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        ) {
            SettingToggleRow(
                title = "AMOLED Eco Mode",
                subtitle = "True black keys & minimal animation for low battery",
                checked = settings.ecoBatteryMode,
                onCheckedChange = { viewModel.toggleEcoBatteryMode(it) }
            )
            GroupDivider()
            SettingToggleRow(
                title = "Auto Eco Below 20%",
                subtitle = "Turn on automatically on low battery",
                checked = settings.autoEcoOnLowBattery,
                onCheckedChange = viewModel::toggleAutoEcoOnLowBattery
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ThemeSwatchRow(
    themes: List<HybridThemeStyle>,
    selectedStyle: HybridThemeStyle,
    ecoActive: Boolean,
    onSelect: (HybridThemeStyle) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        themes.forEach { style ->
            val selected = (selectedStyle == style && !ecoActive) ||
                (ecoActive && style == HybridThemeStyle.AMOLED_ECO)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(style) }
                    .testTag("theme_${style.name}")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(style.backgroundColor)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else style.keyBorderColor,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (style == HybridThemeStyle.SYSTEM_AUTO) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "System Auto Theme",
                            tint = style.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                Modifier
                                    .size(15.dp, 11.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(style.keyColor)
                            )
                            Box(
                                Modifier
                                    .size(15.dp, 11.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(style.keyColor)
                            )
                            Box(
                                Modifier
                                    .size(15.dp, 11.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(style.actionKeyColor)
                            )
                        }
                    }
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = style.shortName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsGroupCard(
    title: String,
    trailingBadge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(title)
            trailingBadge?.invoke()
        }
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    RoundedCornerShape(18.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
