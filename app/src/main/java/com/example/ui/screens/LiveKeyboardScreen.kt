package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ClipboardEntryEntity
import com.example.data.KeyboardLanguage
import com.example.engine.VoiceDictationState
import com.example.ui.HybridStudioUiState
import com.example.ui.HybridViewModel
import com.example.ui.keyboard.HybridKeyboardActions
import com.example.ui.keyboard.HybridKeyboardView

@Composable
fun LiveKeyboardScreen(
    uiState: HybridStudioUiState,
    viewModel: HybridViewModel,
    onRequestMicPermissionAndListen: () -> Unit,
    onNavigateToThemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrollable top workspace: Hero + System Activation + Interactive Text Editor
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sleek Hero Header Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_hybrid_banner),
                        contentDescription = stringResource(id = R.string.hero_banner_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xE6090D16),
                                        Color(0x99090D16),
                                        Color(0x33090D16)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                modifier = Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    RoundedCornerShape(50)
                                )
                            ) {
                                Text(
                                    text = "SAMSUNG × GBOARD · ANDROID 17 READY",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Hybrid Keyboard",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Glide swipe • Soft Voice Mic • Low-Battery Eco",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFB8C7E0)
                            )
                        }
                    }
                }
            }

            // System IME Activation Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.imeStatus.isSelectedAsDefault) {
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.imeStatus.isSelectedAsDefault) {
                                        Icons.Rounded.CheckCircle
                                    } else {
                                        Icons.Rounded.Keyboard
                                    },
                                    contentDescription = "System IME Status",
                                    tint = if (uiState.imeStatus.isSelectedAsDefault) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            Column {
                                Text(
                                    text = when {
                                        uiState.imeStatus.isSelectedAsDefault -> "Hybrid Keyboard is Active System-Wide"
                                        uiState.imeStatus.isEnabledInSystem -> "Enabled — Select as Default Keyboard"
                                        else -> "Enable System-Wide Keyboard"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Use inside WhatsApp, Chrome, Messages & all apps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("enable_ime_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.imeStatus.isEnabledInSystem) "IME Settings" else "1. Enable",
                                maxLines = 1
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showInputMethodPicker()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("switch_ime_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "2. Switch", maxLines = 1)
                        }
                    }
                }
            }

            // Live Interactive Sandbox Text Area (driven directly by HybridKeyboardView below)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE HYBRID TEST PAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(
                                            ClipData.newPlainText("Hybrid Text", uiState.sandboxText.text)
                                        )
                                        viewModel.saveSandboxToClipboard()
                                        Toast.makeText(context, "Copied & saved to Hybrid Clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("sandbox_copy_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("Copy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.clearSandbox() }
                                    .testTag("sandbox_clear_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteSweep,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 78.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = uiState.sandboxText,
                            onValueChange = viewModel::updateSandboxValue,
                            readOnly = true, // Uses embedded HybridKeyboardView directly without popping duplicate IME
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_sandbox_field")
                        )
                        if (uiState.sandboxText.text.isEmpty()) {
                            Text(
                                text = "Tap or swipe keys below, or tap Soft Mic to speak...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Quick feature action chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickStudioChip(
                            icon = Icons.Rounded.Mic,
                            label = if (uiState.voiceState is VoiceDictationState.Listening) "Stop Voice Mic" else "Soft Voice Mic",
                            active = uiState.voiceState is VoiceDictationState.Listening,
                            onClick = {
                                if (uiState.voiceState is VoiceDictationState.Listening) {
                                    viewModel.stopVoiceDictation()
                                } else {
                                    onRequestMicPermissionAndListen()
                                }
                            }
                        )
                        QuickStudioChip(
                            icon = Icons.Rounded.BatterySaver,
                            label = if (uiState.effectiveEcoActive) "Eco Mode: ON" else "Eco Mode: OFF",
                            active = uiState.effectiveEcoActive,
                            onClick = { viewModel.toggleEcoBatteryMode() }
                        )
                        QuickStudioChip(
                            icon = Icons.Rounded.Tune,
                            label = "Lang: ${uiState.settings.language.label}",
                            active = uiState.settings.language != KeyboardLanguage.ENGLISH_US,
                            onClick = { viewModel.cycleLanguage() }
                        )
                    }
                }
            }
        }

        // Docked Live Interactive Hybrid Keyboard
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            HybridKeyboardView(
                settings = uiState.settings,
                suggestions = uiState.suggestions,
                clipboardEntries = uiState.clipboardEntries,
                voiceState = uiState.voiceState,
                rmsLevel = uiState.rmsLevel,
                shiftState = uiState.shiftState,
                onShiftStateChange = viewModel::setShiftState,
                showSettingsIcon = true,
                actions = object : HybridKeyboardActions {
                    override fun onCommitText(text: String) {
                        viewModel.commitTextToSandbox(text)
                    }

                    override fun onCommitWordReplacement(word: String) {
                        viewModel.replaceWordInSandbox(word)
                    }

                    override fun onBackspace(count: Int) {
                        viewModel.backspaceInSandbox(count)
                    }

                    override fun onEnter() {
                        viewModel.commitTextToSandbox("\n")
                    }

                    override fun onMoveCursor(delta: Int) {
                        viewModel.moveCursorInSandbox(delta)
                    }

                    override fun onMicClick() {
                        if (uiState.voiceState is VoiceDictationState.Listening) {
                            viewModel.stopVoiceDictation()
                        } else {
                            onRequestMicPermissionAndListen()
                        }
                    }

                    override fun onStopMic() {
                        viewModel.stopVoiceDictation()
                    }

                    override fun onDismissMicError() {
                        viewModel.dismissVoiceError()
                    }

                    override fun onCycleLanguage() {
                        viewModel.cycleLanguage()
                    }

                    override fun onToggleOneHanded() {
                        viewModel.cycleOneHandedMode()
                    }

                    override fun onToggleEcoMode() {
                        viewModel.toggleEcoBatteryMode()
                    }

                    override fun onToggleClipboardPin(entry: ClipboardEntryEntity) {
                        viewModel.toggleClipboardPin(entry)
                    }

                    override fun onDeleteClipboardEntry(id: Int) {
                        viewModel.deleteClipboardEntry(id)
                    }

                    override fun onOpenSettings() {
                        onNavigateToThemes()
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickStudioChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
