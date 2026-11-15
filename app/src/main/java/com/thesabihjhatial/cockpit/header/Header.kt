package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.bluetooth.BluetoothStatus
import com.thesabihjhatial.cockpit.clock.rememberNow
import com.thesabihjhatial.cockpit.network.WifiStatus
import com.thesabihjhatial.cockpit.permission.PermissionAccess
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors

@Composable
fun Header(
    bluetoothAccess: PermissionAccess,
    bluetoothStatus: BluetoothStatus?,
    wifi: WifiStatus,
    modifier: Modifier = Modifier,
) {
    val now by rememberNow()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(greetingLabel(now.hour)),
            color = LocalCockpitColors.current.muted,
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
                modifier = Modifier.weight(1f),
            )
        }
    }
}
