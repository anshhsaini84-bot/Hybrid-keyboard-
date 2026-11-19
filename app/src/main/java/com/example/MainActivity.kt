package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Palette
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
import com.example.ui.screens.EcoSystemScreen
import com.example.ui.screens.LiveKeyboardScreen
import com.example.ui.screens.ThemesLayoutScreen
import com.example.ui.theme.MyApplicationTheme

private enum class StudioTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    SANDBOX(R.string.tab_sandbox, Icons.Rounded.Keyboard, Icons.Outlined.Keyboard, "nav_sandbox"),
    THEMES(R.string.tab_themes, Icons.Rounded.Palette, Icons.Outlined.Palette, "nav_themes"),
    CLIPBOARD(R.string.tab_dictionary, Icons.Rounded.ContentPaste, Icons.Outlined.ContentPaste, "nav_clipboard"),
    ECO(R.string.tab_battery, Icons.Rounded.BatterySaver, Icons.Outlined.BatterySaver, "nav_eco")
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
    var currentTab by rememberSaveable { mutableStateOf(StudioTab.SANDBOX) }

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
                Toast.LENGTH_LONG
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
                    NavigationBar {
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
                    NavigationRail {
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

                when (currentTab) {
                    StudioTab.SANDBOX -> LiveKeyboardScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onRequestMicPermissionAndListen = ::requestMicAndStart,
                        onNavigateToThemes = { currentTab = StudioTab.THEMES },
                        modifier = Modifier.weight(1f)
                    )
                    StudioTab.THEMES -> ThemesLayoutScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                    StudioTab.CLIPBOARD -> ClipboardDictionaryScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                    StudioTab.ECO -> EcoSystemScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
