package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.FirstPage
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LastPage
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPanel
import com.example.data.KeyboardSettings
import com.example.data.ResolvedKeyboardColors
import com.example.data.TextEditCommand
import com.example.data.ToolbarTool
import com.example.engine.QuickTranslateEngine
import kotlin.math.roundToInt

/**
 * Hidden "More Tools" Drawer (opened via the top-left ⊞ button or swiping the top bar).
 * Keeps the main top bar clean while giving 1-tap access to every feature + customizable top bar!
 */
@Composable
fun MoreToolsDrawerPanel(
    theme: ResolvedKeyboardColors,
    settings: KeyboardSettings,
    activePanel: KeyboardPanel,
    onToolClick: (ToolbarTool) -> Unit,
    onUpdatePinnedTools: (List<ToolbarTool>) -> Unit,
    onBackToAlpha: () -> Unit
) {
    var customizeMode by remember { mutableStateOf(false) }
    val pinned = settings.pinnedToolbarTools

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(244.dp)
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (customizeMode) "Pin & Reorder Top Bar (max 5)" else "More Tools",
                color = theme.keyTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (customizeMode) theme.accentColor else theme.keyColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { customizeMode = !customizeMode }
                        .testTag("more_tools_customize_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (customizeMode) Icons.Rounded.Check else Icons.Rounded.Tune,
                            contentDescription = "Customize Top Bar",
                            tint = if (customizeMode) (if (theme.isLight) Color.White else Color.Black) else theme.accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (customizeMode) "Done" else "Customize",
                            color = if (customizeMode) (if (theme.isLight) Color.White else Color.Black) else theme.keyTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = "Keys",
                    color = theme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onBackToAlpha)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Reorderable Pinned Strip when customizing
        if (customizeMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pinned.forEachIndexed { idx, tool ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentColor.copy(alpha = 0.16f))
                            .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (idx > 0) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = "Move Left",
                                tint = theme.accentColor,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val next = pinned.toMutableList()
                                        next[idx] = next[idx - 1].also { next[idx - 1] = next[idx] }
                                        onUpdatePinnedTools(next)
                                    }
                            )
                        }
                        Icon(
                            imageVector = tool.iconVector(),
                            contentDescription = tool.label,
                            tint = theme.keyTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = tool.label,
                            color = theme.keyTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (idx < pinned.lastIndex) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "Move Right",
                                tint = theme.accentColor,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val next = pinned.toMutableList()
                                        next[idx] = next[idx + 1].also { next[idx + 1] = next[idx] }
                                        onUpdatePinnedTools(next)
                                    }
                            )
                        }
                        if (pinned.size > 1) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Unpin",
                                tint = theme.hintTextColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        onUpdatePinnedTools(pinned.filterNot { it == tool })
                                    }
                            )
                        }
                    }
                }
            }
        }

        // All Tools Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(ToolbarTool.entries) { tool ->
                val isPinned = tool in pinned
                val isActive = if (customizeMode) isPinned else tool.isActiveFor(activePanel, settings)
                Box(
                    modifier = Modifier
                        .height(58.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) theme.accentColor.copy(alpha = 0.16f) else theme.keyColor)
                        .border(
                            width = 1.dp,
                            color = if (isActive) theme.accentColor.copy(alpha = 0.65f) else theme.keyBorderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (customizeMode) {
                                val next = if (isPinned) {
                                    if (pinned.size > 1) pinned.filterNot { it == tool } else pinned
                                } else {
                                    if (pinned.size < 5) pinned + tool else pinned.dropLast(1) + tool
                                }
                                onUpdatePinnedTools(next)
                            } else {
                                onToolClick(tool)
                            }
                        }
                        .testTag("drawer_tool_${tool.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (customizeMode) {
                        Icon(
                            imageVector = if (isPinned) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = null,
                            tint = if (isPinned) theme.accentColor else theme.hintTextColor,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(12.dp)
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = tool.iconVector(),
                            contentDescription = tool.label,
                            tint = if (isActive) theme.accentColor else theme.keyTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = tool.label,
                            color = if (isActive) theme.accentColor else theme.hintTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Hand-Draggable Keyboard Resize Panel:
 * Drag the top handle up or down with your finger to resize keyboard height smoothly (72% - 135%),
 * or pick a 1-tap preset (Small, Normal, Tall, XL).
 */
@Composable
fun ResizeKeyboardPanel(
    theme: ResolvedKeyboardColors,
    heightScale: Float,
    onHeightScaleChange: (Float) -> Unit,
    onDone: () -> Unit
) {
    val percent = (heightScale * 100f).roundToInt()
    val previewHeight = (190f * heightScale).coerceIn(145f, 275f).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(previewHeight)
            .background(theme.backgroundColor)
            .border(1.5.dp, theme.accentColor.copy(alpha = 0.65f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Draggable Top Handle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(theme.accentColor.copy(alpha = 0.14f))
                .border(1.dp, theme.accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                .pointerInput(heightScale) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        // Dragging UP (negative dragAmount) increases keyboard height; DOWN shrinks it
                        val deltaScale = -dragAmount / 280f
                        val next = (heightScale + deltaScale).coerceIn(0.72f, 1.35f)
                        onHeightScaleChange(next)
                    }
                }
                .padding(vertical = 8.dp)
                .testTag("resize_drag_handle"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.DragHandle,
                contentDescription = "Drag Up or Down to Resize Keyboard",
                tint = theme.accentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Drag Up or Down to Resize • $percent%",
                color = theme.keyTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Quick 1-Tap Size Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf(
                "Small" to 0.80f,
                "Normal" to 1.00f,
                "Large" to 1.16f,
                "XL" to 1.32f
            )
            presets.forEach { (label, scale) ->
                val selected = kotlin.math.abs(heightScale - scale) < 0.05f
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) theme.accentColor.copy(alpha = 0.22f) else theme.keyColor,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (selected) theme.accentColor else theme.keyBorderColor,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onHeightScaleChange(scale) }
                        .testTag("resize_preset_${label.lowercase()}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = if (selected) theme.accentColor else theme.keyTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom Reset & Done Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.specialKeyColor,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onHeightScaleChange(1.0f) }
                    .testTag("resize_reset_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Reset",
                        color = theme.keyTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.actionKeyColor,
                modifier = Modifier
                    .weight(1.4f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDone)
                    .testTag("resize_done_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Done",
                        color = if (theme.isLight) Color.White else Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Samsung & Gboard signature Text Editing Cursor D-Pad Panel:
 * 4-way precision arrows, Home/End, Select All, Cut, Copy, Paste, and Undo.
 */
@Composable
fun TextEditKeyboardPanel(
    theme: ResolvedKeyboardColors,
    onCommand: (TextEditCommand) -> Unit,
    onBackToAlpha: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Text Editing & Cursor Control",
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.FirstPage,
                        label = "Home",
                        testTag = "edit_home",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.JUMP_START) }
                    )
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.KeyboardArrowUp,
                        label = null,
                        testTag = "edit_up",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.MOVE_UP) }
                    )
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.LastPage,
                        label = "End",
                        testTag = "edit_end",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.JUMP_END) }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.KeyboardArrowLeft,
                        label = null,
                        testTag = "edit_left",
                        isAccent = true,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.MOVE_LEFT) }
                    )
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.KeyboardArrowDown,
                        label = null,
                        testTag = "edit_down",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.MOVE_DOWN) }
                    )
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.KeyboardArrowRight,
                        label = null,
                        testTag = "edit_right",
                        isAccent = true,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.MOVE_RIGHT) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.SelectAll,
                        label = "All",
                        testTag = "edit_select_all",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.SELECT_ALL) }
                    )
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.AutoMirrored.Rounded.Undo,
                        label = "Undo",
                        testTag = "edit_undo",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.UNDO) }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.ContentCopy,
                        label = "Copy",
                        testTag = "edit_copy",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.COPY) }
                    )
                    EditKeyButton(
                        theme = theme,
                        icon = Icons.Rounded.ContentCut,
                        label = "Cut",
                        testTag = "edit_cut",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onCommand(TextEditCommand.CUT) }
                    )
                }
                EditKeyButton(
                    theme = theme,
                    icon = Icons.Rounded.ContentPaste,
                    label = "Paste",
                    testTag = "edit_paste",
                    isAccent = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.95f),
                    onClick = { onCommand(TextEditCommand.PASTE) }
                )
            }
        }
    }
}

@Composable
private fun EditKeyButton(
    theme: ResolvedKeyboardColors,
    icon: ImageVector,
    label: String?,
    testTag: String,
    isAccent: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isAccent) theme.accentColor.copy(alpha = 0.16f) else theme.keyColor)
            .border(
                width = 1.dp,
                color = if (isAccent) theme.accentColor.copy(alpha = 0.6f) else theme.keyBorderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isAccent) theme.accentColor else theme.keyTextColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    color = if (isAccent) theme.accentColor else theme.keyTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = testTag,
                tint = if (isAccent) theme.accentColor else theme.keyTextColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Built-in Instant Offline Multi-Language Converter:
 * Convert English/Hinglish phrases into 10 target languages right inside the keyboard.
 */
@Composable
fun TranslateKeyboardPanel(
    theme: ResolvedKeyboardColors,
    onInsertTranslation: (String) -> Unit,
    onBackToAlpha: () -> Unit
) {
    var target by remember { mutableStateOf(QuickTranslateEngine.TargetLanguage.HINDI) }
    var inputPhrase by remember { mutableStateOf("good morning") }
    val translated = remember(inputPhrase, target) {
        QuickTranslateEngine.translate(inputPhrase, target)
    }

    val quickChips = remember(target) {
        if (target == QuickTranslateEngine.TargetLanguage.ENGLISH) {
            listOf("Kya haal hai", "Main theek hoon", "Bahut badhiya")
        } else {
            listOf("Good morning", "How are you", "Thank you")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(244.dp)
            .background(theme.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Convert to:",
                color = theme.hintTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Keys",
                color = theme.accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onBackToAlpha)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Scrollable 10-language target pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickTranslateEngine.TargetLanguage.entries.forEach { lang ->
                val selected = lang == target
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) theme.accentColor.copy(alpha = 0.22f) else theme.keyColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (selected) theme.accentColor else theme.keyBorderColor,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { target = lang }
                        .testTag("translate_target_${lang.code.lowercase()}")
                ) {
                    Text(
                        text = lang.label,
                        color = if (selected) theme.accentColor else theme.keyTextColor,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(theme.specialKeyColor)
                .border(1.dp, theme.keyBorderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            BasicTextField(
                value = inputPhrase,
                onValueChange = { inputPhrase = it },
                singleLine = true,
                textStyle = TextStyle(color = theme.keyTextColor, fontSize = 13.sp),
                cursorBrush = SolidColor(theme.accentColor),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickChips.forEach { phrase ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.keyColor,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { inputPhrase = phrase }
                ) {
                    Text(
                        text = phrase,
                        color = theme.hintTextColor,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.keyColor)
                .border(1.dp, theme.accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CONVERTED (${target.label.uppercase()})",
                    color = theme.accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = translated.ifEmpty { "Type or tap a phrase..." },
                    color = theme.keyTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.actionKeyColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        if (translated.isNotBlank()) {
                            onInsertTranslation("$translated ")
                        }
                    }
                    .testTag("translate_insert_button")
            ) {
                Text(
                    text = "Insert",
                    color = if (theme.isLight) Color.White else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
