package com.thesabihjhatial.cockpit.bluetooth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun rememberBluetoothStatus(enabled: Boolean): State<BluetoothStatus?> {
    val context = LocalContext.current
    val flow: Flow<BluetoothStatus?> = remember(enabled) {
        if (enabled) bluetoothStatusFlow(context) else emptyFlow()
    }
    return flow.collectAsStateWithLifecycle(initialValue = null)
}
