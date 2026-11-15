package com.thesabihjhatial.cockpit.home

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import com.thesabihjhatial.cockpit.media.ExpandedNowPlaying
import com.thesabihjhatial.cockpit.media.NowPlayingPanel
import com.thesabihjhatial.cockpit.media.rememberMediaState
import com.thesabihjhatial.cockpit.media.rememberSpectrumAccess
import com.thesabihjhatial.cockpit.network.rememberWifiStatus
import com.thesabihjhatial.cockpit.permission.rememberPermissions
import com.thesabihjhatial.cockpit.ui.FadeThrough
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors

private const val ArtKey = "art"
private const val ArtMs = 300

private val FlingVelocity = 500.dp
private val RowGap = 16.dp
private val ScreenPadding = 32.dp
private val SheetPeek = 48.dp

@OptIn(ExperimentalSharedTransitionApi::class)
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
    val media = rememberMediaState(bluetoothStatus)
    val nowPlaying = media.nowPlaying
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val slots by viewModel.slots.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf<Int?>(null) }
    val sheet = remember { SheetState(scope) { picking = null } }
    val dragState = rememberDraggableState { sheet.dragBy(it) }
    val showExpanded = expanded && nowPlaying != null
    val spectrumAllowed = rememberSpectrumAccess(showExpanded)

    BackHandler(enabled = sheet.isVisible) { sheet.close() }

    BackHandler(enabled = showExpanded) { expanded = false }

    LaunchedEffect(Unit) {
        viewModel.homeRequests.collect {
            sheet.close()
            expanded = false
        }
    }

    LaunchedEffect(nowPlaying == null) { if (nowPlaying == null) expanded = false }

    LaunchedEffect(ready) { if (ready) permissions.request() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background).safeDrawingPadding()) {
        val density = LocalDensity.current
        val rowHeight = (maxHeight - ScreenPadding * 2 - RowGap * 3) / 4

        SideEffect {
            sheet.flingThreshold = with(density) { FlingVelocity.toPx() }
            sheet.travel = with(density) { (maxHeight - SheetPeek).toPx() }
        }

        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = showExpanded,
                modifier = Modifier
                    .fillMaxSize()
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        enabled = !showExpanded,
                        onDragStopped = { sheet.settle(it) },
                    )
                    .padding(ScreenPadding),
                transitionSpec = { FadeThrough },
                label = "nowPlaying",
            ) { isExpanded ->
                val artModifier = Modifier.sharedElement(
                    state = rememberSharedContentState(key = ArtKey),
                    animatedVisibilityScope = this@AnimatedContent,
                    boundsTransform = { _, _ -> tween(ArtMs, easing = FastOutSlowInEasing) },
                )
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(RowGap)) {
                    if (isExpanded && nowPlaying != null) {
                        ExpandedNowPlaying(
                            nowPlaying = nowPlaying,
                            rowGap = RowGap,
                            rowHeight = rowHeight,
                            spectrumAllowed = spectrumAllowed,
                            onCollapse = { expanded = false },
                            artModifier = artModifier,
                        )
                    } else if (!isExpanded) {
                        Header(
                            bluetoothAccess = bluetoothAccess,
                            bluetoothStatus = bluetoothStatus,
                            wifi = wifi,
                            modifier = Modifier.fillMaxWidth().height(rowHeight),
                        )
                        NowPlayingPanel(
                            media = media,
                            onExpand = { expanded = true },
                            modifier = Modifier.fillMaxWidth().height(rowHeight * 2 + RowGap),
                            artModifier = artModifier,
                        )
                        PinnedRow(
                            pinned = pinned,
                            onAdd = {
                                picking = it
                                sheet.open()
                            },
                            onLaunch = { context.launchApp(it.id) },
                            onUnpin = viewModel::unpin,
                            modifier = Modifier.fillMaxWidth().height(rowHeight),
                        )
                    }
                }
            }
        }
        if (!showExpanded) {
            AppSheet(
                apps = apps,
                docked = slots.filterNotNull().toSet(),
                dockFull = slots.all { it != null },
                onDock = { viewModel.dock(it.id) },
                onSelect = { app ->
                    picking?.let { viewModel.pin(app.id, it) } ?: context.launchApp(app.id)
                    sheet.close()
                },
                onUndock = { viewModel.undock(it.id) },
                sheet = sheet,
            )
        }
    }
}
