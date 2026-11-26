package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.bluetooth.BluetoothStatus
import com.thesabihjhatial.cockpit.network.WifiStatus
import com.thesabihjhatial.cockpit.permission.PermissionAccess
import com.thesabihjhatial.cockpit.ui.CockpitPalette
import com.thesabihjhatial.cockpit.ui.Text
import java.time.LocalDateTime

@Composable
fun Header(
    clock: State<LocalDateTime>,
    bluetoothAccess: PermissionAccess,
    bluetoothStatus: BluetoothStatus?,
    wifi: WifiStatus,
    launching: String?,
    onBluetooth: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now by clock

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(greetingLabel(now.hour)),
            color = CockpitPalette.muted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeDate(now = now, modifier = Modifier.weight(1f))
            StatusRow(
                bluetoothAccess = bluetoothAccess,
                bluetoothStatus = bluetoothStatus,
                wifi = wifi,
                launching = launching,
                onBluetooth = onBluetooth,
                onSettings = onSettings,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
