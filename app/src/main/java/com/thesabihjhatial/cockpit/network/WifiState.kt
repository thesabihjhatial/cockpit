package com.thesabihjhatial.cockpit.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun rememberWifiStatus(locationGranted: Boolean): State<WifiStatus> {
    val context = LocalContext.current
    val flow = remember(locationGranted) { wifiStatusFlow(context) }
    return flow.collectAsStateWithLifecycle(initialValue = WifiStatus(connected = false, name = null))
}
