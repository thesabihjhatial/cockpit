package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.bluetooth.BluetoothStatus
import com.thesabihjhatial.cockpit.network.WifiStatus
import com.thesabihjhatial.cockpit.permission.PermissionAccess
import com.thesabihjhatial.cockpit.settings.openSystemSettings
import com.thesabihjhatial.cockpit.ui.CockpitIcons
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors

@Composable
private fun BluetoothChip(access: PermissionAccess, status: BluetoothStatus?) {
    val colors = LocalCockpitColors.current

    when {
        !access.granted -> StatusChip(
            icon = CockpitIcons.Bluetooth,
            label = stringResource(R.string.bluetooth_allow),
            description = stringResource(R.string.bluetooth_allow),
            iconColor = colors.muted,
            labelColor = colors.muted,
            modifier = Modifier.clickable(onClick = access.request),
        )
        status is BluetoothStatus.Connected -> StatusChip(
            icon = CockpitIcons.Bluetooth,
            label = status.deviceName,
            description = stringResource(R.string.bluetooth_connected, status.deviceName),
            iconColor = colors.accentInk,
            labelColor = colors.text2,
        )
        else -> StatusChip(
            icon = CockpitIcons.Bluetooth,
            label = stringResource(R.string.status_not_connected),
            description = stringResource(R.string.bluetooth_disconnected),
            iconColor = colors.muted,
            labelColor = colors.muted,
        )
    }
}

@Composable
private fun SettingsButton() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .offset(x = 14.dp)
            .size(56.dp)
            .clip(CircleShape)
            .clickable { context.openSystemSettings() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CockpitIcons.Settings,
            contentDescription = stringResource(R.string.settings_title),
            modifier = Modifier.size(24.dp),
            tint = LocalCockpitColors.current.text,
        )
    }
}

@Composable
private fun StatusChip(
    icon: ImageVector,
    label: String,
    description: String,
    iconColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(20.dp), tint = iconColor)
        Text(
            text = label,
            color = labelColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun StatusRow(
    bluetoothAccess: PermissionAccess,
    bluetoothStatus: BluetoothStatus?,
    wifi: WifiStatus,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BluetoothChip(bluetoothAccess, bluetoothStatus)
        WifiChip(wifi)
        SettingsButton()
    }
}

@Composable
private fun WifiChip(wifi: WifiStatus) {
    val colors = LocalCockpitColors.current

    if (wifi.connected) {
        StatusChip(
            icon = CockpitIcons.Wifi,
            label = wifi.name ?: stringResource(R.string.status_connected),
            description = stringResource(R.string.wifi_connected),
            iconColor = colors.text2,
            labelColor = colors.text2,
        )
    } else {
        StatusChip(
            icon = CockpitIcons.Wifi,
            label = stringResource(R.string.status_not_connected),
            description = stringResource(R.string.wifi_disconnected),
            iconColor = colors.muted,
            labelColor = colors.muted,
        )
    }
}
