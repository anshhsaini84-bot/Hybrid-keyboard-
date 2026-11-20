package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HybridThemeStyle
import com.example.data.KeyboardLanguage
import com.example.data.OneHandedMode
import com.example.ui.HybridStudioUiState
import com.example.ui.HybridViewModel
import kotlin.math.roundToInt

@Composable
fun ThemesLayoutScreen(
    uiState: HybridStudioUiState,
    viewModel: HybridViewModel,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val battery = uiState.battery

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

        // 1. Sleek Horizontal Theme Swatch Row
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Theme")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HybridThemeStyle.entries.forEach { style ->
                    val selected = (settings.themeStyle == style && !settings.ecoBatteryMode) ||
                        (settings.ecoBatteryMode && style == HybridThemeStyle.AMOLED_ECO)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(86.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.setThemeStyle(style) }
                            .testTag("theme_${style.name}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(style.backgroundColor)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else style.keyBorderColor,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    Modifier
                                        .size(16.dp, 12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(style.keyColor)
                                )
                                Box(
                                    Modifier
                                        .size(16.dp, 12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(style.keyColor)
                                )
                                Box(
                                    Modifier
                                        .size(16.dp, 12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(style.actionKeyColor)
                                )
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
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = style.displayName.substringAfter(" "),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Language, One-Handed & Height
        SettingsGroupCard(title = "Language & Size") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeyboardLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = settings.language == lang,
                        onClick = { viewModel.setLanguage(lang) },
                        label = { Text(lang.label, fontSize = 12.sp) },
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
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Height", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${(settings.heightScale * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = settings.heightScale,
                onValueChange = viewModel::setHeightScale,
                valueRange = 0.85f..1.15f
            )
        }

        // 3. Typing & Smart Features (Clean One UI grouped list)
        SettingsGroupCard(title = "Typing & Gestures") {
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
                subtitle = "Slide on spacebar to move cursor",
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
                subtitle = "Keep voice mic button in toolbar",
                checked = settings.softMicAlwaysVisible,
                onCheckedChange = viewModel::toggleSoftMicVisible
            )
            GroupDivider()
            SettingToggleRow(
                title = "Auto-Capture Clipboard",
                subtitle = "Save copied text & screenshots",
                checked = settings.autoCaptureClipboard,
                onCheckedChange = viewModel::toggleAutoCaptureClipboard
            )
            GroupDivider()
            SettingToggleRow(
                title = "Vibration Feedback",
                subtitle = "Tactile click on keypress",
                checked = settings.hapticFeedbackEnabled,
                onCheckedChange = viewModel::toggleHaptics
            )
            GroupDivider()
            SettingToggleRow(
                title = "Keypress Sound",
                subtitle = "Subtle audio click",
                checked = settings.soundOnKeypress,
                onCheckedChange = viewModel::toggleSound
            )
        }

        // 4. Compact Low-Battery Eco Section
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
