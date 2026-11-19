package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Themes & Hybrid Layout",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Theme Gallery
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Keyboard Visual Theme",
                    style = MaterialTheme.typography.titleMedium
                )
                HybridThemeStyle.entries.forEach { style ->
                    val selected = settings.themeStyle == style && !settings.ecoBatteryMode ||
                        (settings.ecoBatteryMode && style == HybridThemeStyle.AMOLED_ECO)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setThemeStyle(style) }
                            .padding(12.dp)
                            .testTag("theme_${style.name}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color palette mini preview
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(style.backgroundColor)
                                .border(1.dp, style.keyBorderColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(style.keyColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(style.actionKeyColor)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = style.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected Theme",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Language & One-Handed Controls
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Typing Language & Voice Locale", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeyboardLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = settings.language == lang,
                            onClick = { viewModel.setLanguage(lang) },
                            label = { Text(lang.label) },
                            modifier = Modifier.testTag("lang_chip_${lang.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("One-Handed Reachability", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OneHandedMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.oneHandedMode == mode,
                            onClick = { viewModel.setOneHandedMode(mode) },
                            label = { Text(mode.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Keyboard Height", style = MaterialTheme.typography.titleMedium)
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
        }

        // Samsung + Gboard Feature Toggles
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Samsung & Gboard Precision Features", style = MaterialTheme.typography.titleMedium)
                SettingToggleRow(
                    title = "Dedicated Number Row (Samsung)",
                    subtitle = "Always show 1–0 row above letters",
                    checked = settings.showNumberRow,
                    onCheckedChange = viewModel::toggleNumberRow
                )
                SettingToggleRow(
                    title = "Glide / Swipe Typing (Gboard)",
                    subtitle = "Slide across keys with glowing finger trail",
                    checked = settings.glideTypingEnabled,
                    onCheckedChange = viewModel::toggleGlideTyping
                )
                SettingToggleRow(
                    title = "Spacebar Cursor Trackpad",
                    subtitle = "Slide left/right on Space to move text cursor",
                    checked = settings.spacebarCursorEnabled,
                    onCheckedChange = viewModel::toggleSpacebarCursor
                )
                SettingToggleRow(
                    title = "Soft Microphone Pill",
                    subtitle = "Keep 1-tap Voice Mic button in keyboard top bar",
                    checked = settings.softMicAlwaysVisible,
                    onCheckedChange = viewModel::toggleSoftMicVisible
                )
                SettingToggleRow(
                    title = "Auto-Capitalization",
                    subtitle = "Capitalize first letter of each sentence",
                    checked = settings.autoCapitalize,
                    onCheckedChange = viewModel::toggleAutoCapitalize
                )
                SettingToggleRow(
                    title = "Tactile Haptic Feedback",
                    subtitle = "Crisp low-latency key vibration",
                    checked = settings.hapticFeedbackEnabled,
                    onCheckedChange = viewModel::toggleHaptics
                )
                SettingToggleRow(
                    title = "Keypress Sound",
                    subtitle = "Subtle audio click (auto-muted in Eco mode)",
                    checked = settings.soundOnKeypress,
                    onCheckedChange = viewModel::toggleSound
                )
            }
        }
    }
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
                fontWeight = FontWeight.SemiBold
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
