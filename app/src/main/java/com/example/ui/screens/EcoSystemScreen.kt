package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.HybridStudioUiState
import com.example.ui.HybridViewModel

@Composable
fun EcoSystemScreen(
    uiState: HybridStudioUiState,
    viewModel: HybridViewModel,
    modifier: Modifier = Modifier
) {
    val battery = uiState.battery
    val ecoActive = uiState.effectiveEcoActive

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Eco Battery & Android 17 Engine",
            style = MaterialTheme.typography.headlineMedium
        )

        // Live Battery & Power Profile Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (ecoActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (ecoActive) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (battery.isCharging) Icons.Rounded.BatteryChargingFull
                                else Icons.Rounded.BatterySaver,
                                contentDescription = "Battery Telemetry",
                                tint = if (ecoActive) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device Battery: ${battery.levelPercent}%",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = buildString {
                                    append(if (battery.isCharging) "Charging" else "On Battery")
                                    if (battery.isPowerSaveMode) append(" • System Power Saver ON")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (ecoActive) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (ecoActive) "ECO ACTIVE" else "BALANCED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ecoActive) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (battery.levelPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (battery.levelPercent <= 20) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.tertiary
                )

                SettingToggleRow(
                    title = "Ultra-Low Battery Eco Mode",
                    subtitle = "Switches keys to true AMOLED #000000 black, trims haptics to 8ms, disables popups & heavy trails",
                    checked = uiState.settings.ecoBatteryMode,
                    onCheckedChange = { viewModel.toggleEcoBatteryMode(it) }
                )

                SettingToggleRow(
                    title = "Auto-Enable Eco Below 20%",
                    subtitle = "Automatically activates Eco Mode on low battery or Android Power Saver",
                    checked = uiState.settings.autoEcoOnLowBattery,
                    onCheckedChange = viewModel::toggleAutoEcoOnLowBattery
                )
            }
        }

        // Low-Battery Engineering Architecture
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zero-Waste Performance Architecture", style = MaterialTheme.typography.titleMedium)
                }

                SpecRow("Zero Background Polling", "Passive BroadcastReceiver only; 0 wake locks")
                SpecRow("Lazy SpeechRecognizer", "Mic engine only allocates while actively dictating and destroys immediately on stop")
                SpecRow("AMOLED True Black", "Turns off OLED subpixels across keyboard canvas in Eco Mode")
                SpecRow("Single-Pass Glide Trail", "Hardware-accelerated Compose Canvas without offscreen buffers")
            }
        }

        // Android 17 Compatibility Checklist
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Android 7 → Android 17 Compatibility", style = MaterialTheme.typography.titleMedium)
                }

                SpecRow("Running Device API", "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                SpecRow("Edge-to-Edge IME Insets", "Respects WindowInsets.navigationBars & gesture bars")
                SpecRow("Inline Autofill Ready", "supportsInlineSuggestions=true in method.xml")
                SpecRow("Scoped Permissions", "Least-privilege RECORD_AUDIO & VIBRATE only")
            }
        }
    }
}

@Composable
private fun SpecRow(title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
