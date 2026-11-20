package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HybridViewModel
import com.example.ui.screens.ClipboardDictionaryScreen
import com.example.ui.screens.LiveKeyboardScreen
import com.example.ui.screens.ThemesLayoutScreen
import com.example.ui.theme.MyApplicationTheme

private enum class StudioTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    KEYBOARD(R.string.tab_keyboard, Icons.Rounded.Keyboard, Icons.Outlined.Keyboard, "nav_sandbox"),
    CLIPBOARD(R.string.tab_clipboard, Icons.Rounded.ContentPaste, Icons.Outlined.ContentPaste, "nav_clipboard"),
    SETTINGS(R.string.tab_settings, Icons.Rounded.Tune, Icons.Outlined.Tune, "nav_themes")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HybridKeyboardStudioApp()
            }
        }
    }
}

@Composable
fun HybridKeyboardStudioApp(
    viewModel: HybridViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(StudioTab.KEYBOARD) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshImeStatus()
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceDictation()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.mic_permission_rationale),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun requestMicAndStart() {
        if (viewModel.hasMicPermission()) {
            viewModel.startVoiceDictation()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        StudioTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(id = tab.titleRes)
                                    )
                                },
                                label = { Text(stringResource(id = tab.titleRes), maxLines = 1) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        StudioTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(id = tab.titleRes)
                                    )
                                },
                                label = { Text(stringResource(id = tab.titleRes)) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }

                Crossfade(
                    targetState = currentTab,
                    animationSpec = tween(durationMillis = if (uiState.effectiveEcoActive) 0 else 160),
                    label = "tab_crossfade",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    when (tab) {
                        StudioTab.KEYBOARD -> LiveKeyboardScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onRequestMicPermissionAndListen = ::requestMicAndStart,
                            onNavigateToSettings = { currentTab = StudioTab.SETTINGS }
                        )
                        StudioTab.CLIPBOARD -> ClipboardDictionaryScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        StudioTab.SETTINGS -> ThemesLayoutScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
