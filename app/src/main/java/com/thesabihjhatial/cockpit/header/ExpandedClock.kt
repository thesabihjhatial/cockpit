package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.bluetooth.BluetoothStatus
import com.thesabihjhatial.cockpit.network.WifiStatus
import com.thesabihjhatial.cockpit.permission.PermissionAccess
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import com.thesabihjhatial.cockpit.ui.Text
import java.time.LocalDateTime

@Composable
fun ColumnScope.ExpandedClock(
    clock: State<LocalDateTime>,
    bluetoothAccess: PermissionAccess,
    bluetoothStatus: BluetoothStatus?,
    wifi: WifiStatus,
    launching: String?,
    onBluetooth: () -> Unit,
    onCollapse: () -> Unit,
    onSettings: () -> Unit,
    rowGap: Dp,
    rowHeight: Dp,
    timeModifier: Modifier = Modifier,
) {
    val colors = LocalCockpitColors.current
    val now by clock
    val collapseLabel = stringResource(R.string.clock_collapse)

    Row(
        modifier = Modifier.fillMaxWidth().height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(greetingLabel(now.hour)),
            color = colors.muted,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .clickable(onClickLabel = collapseLabel, onClick = onCollapse)
                .heightIn(min = 56.dp)
                .wrapContentHeight(Alignment.CenterVertically),
        )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight * 3 + rowGap * 2)
            .clickable(onClickLabel = collapseLabel, onClick = onCollapse),
        verticalArrangement = Arrangement.spacedBy(rowGap),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(rowHeight * 2 + rowGap), contentAlignment = Alignment.BottomStart) {
            Text(
                text = now.timeText(),
                fontSize = 300.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.055).em,
                lineHeight = 240.sp,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.offset(x = (-12).dp).then(timeModifier).wrapContentWidth(Alignment.Start, unbounded = true),
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(rowHeight), contentAlignment = Alignment.CenterStart) {
            Text(
                text = now.dateText(colors),
                fontSize = 44.sp,
                letterSpacing = (-0.02).em,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}
