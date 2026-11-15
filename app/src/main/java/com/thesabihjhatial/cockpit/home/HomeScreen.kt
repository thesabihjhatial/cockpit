package com.thesabihjhatial.cockpit.home

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thesabihjhatial.cockpit.apps.AppSheet
import com.thesabihjhatial.cockpit.apps.SheetState
import com.thesabihjhatial.cockpit.apps.launchApp
import com.thesabihjhatial.cockpit.bluetooth.rememberBluetoothStatus
import com.thesabihjhatial.cockpit.header.Header
import com.thesabihjhatial.cockpit.media.NowPlayingPanel
import com.thesabihjhatial.cockpit.media.rememberMediaState
import com.thesabihjhatial.cockpit.network.rememberWifiStatus
import com.thesabihjhatial.cockpit.permission.rememberPermissions
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors

private val FlingVelocity = 500.dp
private val RowGap = 16.dp
private val ScreenPadding = 32.dp
private val SheetPeek = 48.dp

@Composable
fun HomeScreen(ready: Boolean, viewModel: HomeViewModel) {
    val context = LocalContext.current
    val colors = LocalCockpitColors.current
    val permissions = rememberPermissions(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    val bluetoothAccess = permissions.access(Manifest.permission.BLUETOOTH_CONNECT)
    val locationGranted = permissions.access(Manifest.permission.ACCESS_FINE_LOCATION).granted
    val bluetoothStatus by rememberBluetoothStatus(bluetoothAccess.granted)
    val wifi by rememberWifiStatus(locationGranted)
    val media = rememberMediaState()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val slots by viewModel.slots.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var picking by remember { mutableStateOf<Int?>(null) }
    val sheet = remember { SheetState(scope) { picking = null } }
    val dragState = rememberDraggableState { sheet.dragBy(it) }

    BackHandler(enabled = sheet.isVisible) { sheet.close() }

    LaunchedEffect(Unit) { viewModel.homeRequests.collect { sheet.close() } }

    LaunchedEffect(ready) { if (ready) permissions.request() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background).safeDrawingPadding()) {
        val density = LocalDensity.current
        val rowHeight = (maxHeight - ScreenPadding * 2 - RowGap * 3) / 4

        SideEffect {
            sheet.flingThreshold = with(density) { FlingVelocity.toPx() }
            sheet.travel = with(density) { (maxHeight - SheetPeek).toPx() }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { sheet.settle(it) },
                )
                .padding(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(RowGap),
        ) {
            Header(
                bluetoothAccess = bluetoothAccess,
                bluetoothStatus = bluetoothStatus,
                wifi = wifi,
                modifier = Modifier.fillMaxWidth().height(rowHeight),
            )
            NowPlayingPanel(media = media, modifier = Modifier.fillMaxWidth().height(rowHeight * 2 + RowGap))
            PinnedRow(
                pinned = pinned,
                onAdd = {
                    picking = it
                    sheet.open()
                },
                onLaunch = { context.launchApp(it.packageName) },
                onUnpin = viewModel::unpin,
                modifier = Modifier.fillMaxWidth().height(rowHeight),
            )
        }
        AppSheet(
            apps = apps,
            docked = slots.filterNotNull().toSet(),
            dockFull = slots.all { it != null },
            onDock = { viewModel.dock(it.packageName) },
            onSelect = { app ->
                picking?.let { viewModel.pin(app.packageName, it) } ?: context.launchApp(app.packageName)
                sheet.close()
            },
            onUndock = { viewModel.undock(it.packageName) },
            sheet = sheet,
        )
    }
}
