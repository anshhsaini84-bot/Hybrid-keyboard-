package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.FirstPage
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LastPage
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HybridThemeStyle
import com.example.data.TextEditCommand
import com.example.engine.QuickTranslateEngine

/**
 * Samsung & Gboard signature Text Editing Cursor D-Pad Panel:
 * 4-way precision arrows, Home/End, Select All, Cut, Copy, Paste, and Undo.
 */
@Composable
fun TextEditKeyboardPanel(
    theme: HybridThemeStyle,
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
            // Left: 4-way D-Pad + Home/End
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

            // Right: Select All, Copy, Cut, Paste, Undo
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
    theme: HybridThemeStyle,
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
 * Built-in Instant Offline Translator (Gboard & Samsung Translate Strip):
 * Convert English/Hinglish phrases into Hindi Devanagari (or Hinglish -> English) with 1-tap Insert.
 */
@Composable
fun TranslateKeyboardPanel(
    theme: HybridThemeStyle,
    onInsertTranslation: (String) -> Unit,
    onBackToAlpha: () -> Unit
) {
    var direction by remember { mutableStateOf(QuickTranslateEngine.Direction.EN_OR_HINGLISH_TO_HINDI) }
    var inputPhrase by remember { mutableStateOf("namaste kaise ho") }
    val translated = remember(inputPhrase, direction) {
        QuickTranslateEngine.translate(inputPhrase, direction)
    }

    val quickChips = remember(direction) {
        if (direction == QuickTranslateEngine.Direction.EN_OR_HINGLISH_TO_HINDI) {
            listOf("Good morning", "How are you", "Bahut badhiya", "Thank you", "Kya haal hai")
        } else {
            listOf("Namaste", "Kya haal hai", "Main theek hoon", "Kal milte hain", "Bahut badhiya")
        }
    }

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
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.keyColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable {
                        direction = if (direction == QuickTranslateEngine.Direction.EN_OR_HINGLISH_TO_HINDI) {
                            QuickTranslateEngine.Direction.HINGLISH_TO_ENGLISH
                        } else {
                            QuickTranslateEngine.Direction.EN_OR_HINGLISH_TO_HINDI
                        }
                    }
                    .testTag("translate_direction_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = "Swap Direction",
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = direction.label,
                        color = theme.keyTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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

        // Source text box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(theme.specialKeyColor)
                .border(1.dp, theme.keyBorderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
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

        // Quick preset phrase chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickChips.take(3).forEach { phrase ->
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
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Live translation preview + 1-tap Insert button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.keyColor)
                .border(1.dp, theme.accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIVE TRANSLATION",
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
                    color = if (theme == HybridThemeStyle.SAMSUNG_ONE_LIGHT) Color.White else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
