package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardEntryEntity
import com.example.data.HybridThemeStyle
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardPanel
import com.example.data.KeyboardSettings
import com.example.engine.KeyboardLayouts
import com.example.engine.VoiceDictationState

@Composable
fun KeyboardTopStrip(
    theme: HybridThemeStyle,
    settings: KeyboardSettings,
    activePanel: KeyboardPanel,
    suggestions: List<String>,
    showSuggestionsMode: Boolean,
    voiceState: VoiceDictationState,
    onSuggestionClick: (String) -> Unit,
    onPanelToggle: (KeyboardPanel) -> Unit,
    onMicClick: () -> Unit,
    onCycleLanguage: () -> Unit,
    onToggleOneHanded: () -> Unit,
    onToggleEcoMode: () -> Unit,
    onOpenSettings: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.utilityBarColor)
    ) {
        // Samsung-style quick utility ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                UtilityIconButton(
                    icon = if (activePanel == KeyboardPanel.EMOJI) Icons.Rounded.Keyboard else Icons.Rounded.EmojiEmotions,
                    contentDescription = "Emoji Panel",
                    isActive = activePanel == KeyboardPanel.EMOJI,
                    theme = theme,
                    testTag = "kbd_util_emoji",
                    onClick = {
                        onPanelToggle(
                            if (activePanel == KeyboardPanel.EMOJI) KeyboardPanel.ALPHA else KeyboardPanel.EMOJI
                        )
                    }
                )
                UtilityIconButton(
                    icon = Icons.Rounded.ContentPaste,
                    contentDescription = "Clipboard",
                    isActive = activePanel == KeyboardPanel.CLIPBOARD,
                    theme = theme,
                    testTag = "kbd_util_clipboard",
                    onClick = {
                        onPanelToggle(
                            if (activePanel == KeyboardPanel.CLIPBOARD) KeyboardPanel.ALPHA else KeyboardPanel.CLIPBOARD
                        )
                    }
                )
                UtilityIconButton(
                    icon = Icons.Rounded.Language,
                    contentDescription = "Switch Language",
                    isActive = settings.language != KeyboardLanguage.ENGLISH_US,
                    theme = theme,
                    testTag = "kbd_util_language",
                    onClick = onCycleLanguage
                )
                UtilityIconButton(
                    icon = Icons.Rounded.Smartphone,
                    contentDescription = "One-Handed Mode",
                    isActive = settings.oneHandedMode.name != "OFF",
                    theme = theme,
                    testTag = "kbd_util_one_handed",
                    onClick = onToggleOneHanded
                )
                UtilityIconButton(
                    icon = Icons.Rounded.BatterySaver,
                    contentDescription = "Eco Battery Saver",
                    isActive = settings.ecoBatteryMode,
                    theme = theme,
                    testTag = "kbd_util_eco",
                    onClick = onToggleEcoMode
                )
                if (onOpenSettings != null) {
                    UtilityIconButton(
                        icon = Icons.Rounded.Settings,
                        contentDescription = "Keyboard Settings",
                        isActive = false,
                        theme = theme,
                        testTag = "kbd_util_settings",
                        onClick = onOpenSettings
                    )
                }
            }

            // Dedicated Soft Microphone Pill (always easy to reach)
            if (settings.softMicAlwaysVisible) {
                val isListening = voiceState is VoiceDictationState.Listening ||
                    voiceState is VoiceDictationState.Partial
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isListening) theme.accentColor else theme.keyColor,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = 1.dp,
                            color = if (isListening) theme.accentColor else theme.keyBorderColor,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(onClick = onMicClick)
                        .testTag("kbd_soft_mic_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                            contentDescription = "Soft Voice Microphone",
                            tint = if (isListening) Color.Black else theme.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isListening) "Listening" else "Mic",
                            color = if (isListening) Color.Black else theme.keyTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Gboard-style Prediction & Autocorrect Strip
        if (activePanel == KeyboardPanel.ALPHA || showSuggestionsMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.take(3).forEachIndexed { index, word ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (index == 0) theme.keyColor.copy(alpha = 0.85f)
                                else Color.Transparent
                            )
                            .clickable { onSuggestionClick(word) }
                            .testTag("kbd_suggestion_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = word,
                            color = if (index == 0) theme.accentColor else theme.keyTextColor,
                            fontSize = 13.sp,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UtilityIconButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    theme: HybridThemeStyle,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isActive) theme.accentColor.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) theme.accentColor else theme.hintTextColor,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
fun VoiceSoftMicBanner(
    theme: HybridThemeStyle,
    voiceState: VoiceDictationState,
    rmsLevel: Float,
    language: KeyboardLanguage,
    ecoMode: Boolean,
    onStopMic: () -> Unit,
    onDismissError: () -> Unit
) {
    val visible = voiceState !is VoiceDictationState.Idle
    AnimatedVisibility(visible = visible) {
        val animatedLevel by animateFloatAsState(
            targetValue = if (ecoMode) 0.4f else rmsLevel,
            animationSpec = tween(durationMillis = if (ecoMode) 0 else 90),
            label = "rms"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.specialKeyColor)
                .border(1.dp, theme.accentColor.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState is VoiceDictationState.Error) Color(0xFFFF5252)
                            else theme.accentColor
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Voice Active",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val label = when (voiceState) {
                        is VoiceDictationState.Listening -> "Listening (${language.label})... Speak now"
                        is VoiceDictationState.Partial -> voiceState.text
                        is VoiceDictationState.Error -> voiceState.message
                        VoiceDictationState.Idle -> ""
                    }
                    Text(
                        text = label,
                        color = theme.keyTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (voiceState is VoiceDictationState.Listening || voiceState is VoiceDictationState.Partial) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(8) { idx ->
                                val barFraction = ((animatedLevel * (1f - idx * 0.08f)).coerceIn(0.15f, 1f))
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height((4 + 8 * barFraction).dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(theme.accentColor)
                                )
                            }
                        }
                    }
                }
            }
            IconButton(
                onClick = {
                    if (voiceState is VoiceDictationState.Error) onDismissError() else onStopMic()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Voice Mic",
                    tint = theme.hintTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmojiKeyboardPanel(
    theme: HybridThemeStyle,
    onEmojiClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onBackToAlpha: () -> Unit
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = KeyboardLayouts.emojiCategories

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp)
            .background(theme.backgroundColor)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(categories[selectedCategory].emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEmojiClick(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 23.sp)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(theme.utilityBarColor)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = theme.specialKeyColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBackToAlpha)
            ) {
                Text(
                    text = "ABC",
                    color = theme.keyTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                categories.forEachIndexed { idx, cat ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == selectedCategory) theme.accentColor.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable { selectedCategory = idx },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cat.icon, fontSize = 18.sp)
                    }
                }
            }
            IconButton(onClick = onBackspace, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Backspace",
                    tint = theme.keyTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ClipboardKeyboardPanel(
    theme: HybridThemeStyle,
    entries: List<ClipboardEntryEntity>,
    onPasteEntry: (String) -> Unit,
    onTogglePin: (ClipboardEntryEntity) -> Unit,
    onDeleteEntry: (Int) -> Unit,
    onBackToAlpha: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp)
            .background(theme.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Samsung × Hybrid Clipboard",
                color = theme.hintTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Back to Keys",
                color = theme.accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onBackToAlpha)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Clipboard is empty. Copy text to pin & paste fast!",
                    color = theme.hintTextColor,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(entries, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.keyColor)
                            .border(1.dp, theme.keyBorderColor, RoundedCornerShape(10.dp))
                            .clickable { onPasteEntry(item.content) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.content,
                            color = theme.keyTextColor,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onTogglePin(item) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = "Pin",
                                tint = if (item.isPinned) theme.accentColor else theme.hintTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteEntry(item.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint = theme.hintTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
