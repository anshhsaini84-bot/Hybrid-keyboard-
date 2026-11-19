package com.example.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import com.example.data.BatteryTelemetry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Observes system battery & power-save changes passively via BroadcastReceiver
 * (zero polling loops, zero wake locks) for ultra-low battery consumption.
 */
object BatteryMonitor {

    fun observe(context: Context): Flow<BatteryTelemetry> = callbackFlow {
        val appContext = context.applicationContext
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

        fun readCurrent(batteryIntent: Intent?): BatteryTelemetry {
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val percent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 85
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            return BatteryTelemetry(
                levelPercent = percent.coerceIn(0, 100),
                isCharging = charging,
                isPowerSaveMode = powerManager.isPowerSaveMode
            )
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val sticky = if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    intent
                } else {
                    appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                }
                trySend(readCurrent(sticky))
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        val initialSticky = appContext.registerReceiver(receiver, filter)
        trySend(readCurrent(initialSticky))

        awaitClose {
            try {
                appContext.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
            }
        }
    }
}
