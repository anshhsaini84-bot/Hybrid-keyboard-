package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardEntryEntity
import com.example.data.KeyboardLanguage
import com.example.data.TextEditCommand
import com.example.data.ToolbarTool
import com.example.engine.ClipboardSyncManager
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
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importImageToClipboard(uri) { ok ->
                Toast.makeText(
                    context,
                    if (ok) "Saved to Clipboard" else "Could not import image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hybrid Keyboard",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            val isDefault = uiState.imeStatus.isSelectedAsDefault
            val isEnabled = uiState.imeStatus.isEnabledInSystem
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isDefault) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (!isEnabled) {
                            context.startActivity(
                                Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        } else {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showInputMethodPicker()
                        }
                    }
                    .testTag(if (!isEnabled) "enable_ime_button" else "switch_ime_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isDefault) Icons.Rounded.CheckCircle else Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        tint = if (isDefault) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = when {
                            isDefault -> "Active"
                            isEnabled -> "Switch Keyboard"
                            else -> "Enable Keyboard"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 12.sp,
                        color = if (isDefault) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Try typing here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sandbox_add_screenshot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Add screenshot to clipboard",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.saveSandboxToClipboard()
                            Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sandbox_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearSandbox() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sandbox_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Clear text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            val attached = uiState.attachedSandboxImage
            if (attached != null && attached.imagePath != null) {
                val bmp = remember(attached.imagePath) {
                    ClipboardSyncManager.loadThumbnail(attached.imagePath, 220)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = attached.content,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = attached.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = viewModel::clearAttachedSandboxImage,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BasicTextField(
                    value = uiState.sandboxText,
                    onValueChange = viewModel::updateSandboxValue,
                    readOnly = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("live_sandbox_field")
                )
                if (uiState.sandboxText.text.isEmpty()) {
                    Text(
                        text = "Type, glide swipe, or hold spacebar for languages...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        fontSize = 18.sp
                    )
                }
            }

            if (uiState.imeStatus.isEnabledInSystem && !uiState.imeStatus.isSelectedAsDefault) {
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Set as default keyboard")
                }
            }
        }

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

                override fun onCommitTextEntry(entry: ClipboardEntryEntity) {
                    viewModel.commitClipboardEntryToSandbox(entry) { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCommitWordReplacement(word: String) {
                    viewModel.replaceWordInSandbox(word)
                }

                override fun onCommitImageEntry(entry: ClipboardEntryEntity) {
                    viewModel.attachImageToSandbox(entry)
                    Toast.makeText(context, "Pasted ${entry.content}", Toast.LENGTH_SHORT).show()
                }

                override fun onBackspace(count: Int) {
                    viewModel.backspaceInSandbox(count)
                }

                override fun onDeletePreviousWord() {
                    viewModel.deletePreviousWordInSandbox()
                }

                override fun onDoubleSpacePeriod() {
                    viewModel.doubleSpacePeriodInSandbox()
                }

                override fun onEnter() {
                    viewModel.commitTextToSandbox("\n")
                }

                override fun onMoveCursor(delta: Int) {
                    viewModel.moveCursorInSandbox(delta)
                }

                override fun onTextEditCommand(command: TextEditCommand) {
                    viewModel.performTextEditInSandbox(command)
                }

                override fun onSyncSystemClipboard() {
                    viewModel.syncSystemClipboardNow()
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

                override fun onSelectLanguage(language: KeyboardLanguage) {
                    viewModel.setLanguage(language)
                }

                override fun onToggleOneHanded() {
                    viewModel.cycleOneHandedMode()
                }

                override fun onToggleFloatingMode() {
                    viewModel.toggleFloatingMode()
                }

                override fun onToggleEcoMode() {
                    viewModel.toggleEcoBatteryMode()
                }

                override fun onUpdateHeightScale(scale: Float) {
                    viewModel.setHeightScale(scale)
                }

                override fun onUpdatePinnedTools(tools: List<ToolbarTool>) {
                    viewModel.updatePinnedToolbarTools(tools)
                }

                override fun onToggleClipboardPin(entry: ClipboardEntryEntity) {
                    viewModel.toggleClipboardPin(entry)
                }

                override fun onDeleteClipboardEntry(id: Int) {
                    viewModel.deleteClipboardEntry(id)
                }

                override fun onOpenSettings() {
                    onNavigateToSettings()
                }
            }
        )
    }
}
