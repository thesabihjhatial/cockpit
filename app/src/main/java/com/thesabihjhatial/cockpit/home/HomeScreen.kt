package com.thesabihjhatial.cockpit.home

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.thesabihjhatial.cockpit.apps.AppInfo
import com.thesabihjhatial.cockpit.apps.AppSheet
import com.thesabihjhatial.cockpit.apps.launchApp
import com.thesabihjhatial.cockpit.apps.SheetState
import com.thesabihjhatial.cockpit.bluetooth.rememberBluetoothStatus
import com.thesabihjhatial.cockpit.clock.rememberNow
import com.thesabihjhatial.cockpit.header.Header
import com.thesabihjhatial.cockpit.media.ExpandedNowPlaying
import com.thesabihjhatial.cockpit.media.NowPlayingPanel
import com.thesabihjhatial.cockpit.media.rememberMediaState
import com.thesabihjhatial.cockpit.media.rememberSpectrumAccess
import com.thesabihjhatial.cockpit.network.rememberWifiStatus
import com.thesabihjhatial.cockpit.permission.rememberPermissions
import com.thesabihjhatial.cockpit.settings.BluetoothAccessId
import com.thesabihjhatial.cockpit.settings.MediaAccessId
import com.thesabihjhatial.cockpit.settings.openNotificationAccessSettings
import com.thesabihjhatial.cockpit.settings.openSystemSettings
import com.thesabihjhatial.cockpit.settings.SettingsId
import com.thesabihjhatial.cockpit.ui.FadeInDelayMs
import com.thesabihjhatial.cockpit.ui.FadeInMs
import com.thesabihjhatial.cockpit.ui.FadeThrough
import com.thesabihjhatial.cockpit.ui.FadeThroughEnter
import com.thesabihjhatial.cockpit.ui.FadeThroughExit
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import kotlinx.coroutines.delay

private const val ArtKey = "art"
private const val LaunchTimeoutMs = 10000L

private enum class Screen { Home, NowPlaying }

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
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    val bluetoothAccess = permissions.access(Manifest.permission.BLUETOOTH_CONNECT)
    val locationGranted = permissions.access(Manifest.permission.ACCESS_FINE_LOCATION).granted
    val bluetoothStatus by rememberBluetoothStatus(bluetoothAccess.granted)
    val clock = rememberNow()
    val wifi by rememberWifiStatus(locationGranted)
    val media = rememberMediaState(bluetoothStatus)
    val nowPlaying = media.nowPlaying
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val slots by viewModel.slots.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var launching by remember { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf<Int?>(null) }
    val sheet = remember { SheetState(scope) { picking = null } }
    val dragState = rememberDraggableState { sheet.dragBy(it) }
    val showExpanded = expanded && nowPlaying != null
    val screen = if (showExpanded) Screen.NowPlaying else Screen.Home
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

    LaunchedEffect(launching) {
        if (launching != null) {
            delay(LaunchTimeoutMs)
            launching = null
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { launching = null }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        launching = null
        sheet.close()
    }

    val open = { id: String, start: () -> Boolean ->
        if (launching == null) {
            launching = id
            if (!start()) launching = null
        }
    }

    val launch = { app: AppInfo -> open(app.id) { context.launchApp(app.id) } }

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
                targetState = screen,
                modifier = Modifier
                    .fillMaxSize()
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        enabled = screen == Screen.Home,
                        onDragStopped = { sheet.settle(it) },
                    )
                    .padding(ScreenPadding),
                transitionSpec = { FadeThrough },
                label = "screens",
            ) { target ->
                val artModifier = Modifier.sharedElement(
                    state = rememberSharedContentState(key = ArtKey),
                    animatedVisibilityScope = this@AnimatedContent,
                    boundsTransform = { _, _ -> tween(FadeInMs, FadeInDelayMs, FastOutSlowInEasing) },
                )
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(RowGap)) {
                    if (target == Screen.NowPlaying && nowPlaying != null) {
                        ExpandedNowPlaying(
                            nowPlaying = nowPlaying,
                            rowGap = RowGap,
                            rowHeight = rowHeight,
                            spectrumAllowed = spectrumAllowed,
                            onCollapse = { expanded = false },
                            artModifier = artModifier,
                        )
                    } else {
                        Header(
                            clock = clock,
                            bluetoothAccess = bluetoothAccess,
                            bluetoothStatus = bluetoothStatus,
                            wifi = wifi,
                            launching = launching,
                            onBluetooth = { open(BluetoothAccessId) { bluetoothAccess.request(); true } },
                            onSettings = { open(SettingsId) { context.openSystemSettings() } },
                            modifier = Modifier.fillMaxWidth().height(rowHeight),
                        )
                        NowPlayingPanel(
                            media = media,
                            launching = launching,
                            onAllow = { open(MediaAccessId) { context.openNotificationAccessSettings() } },
                            onExpand = { expanded = true },
                            modifier = Modifier.fillMaxWidth().height(rowHeight * 2 + RowGap),
                            artModifier = artModifier,
                        )
                        PinnedRow(
                            launching = launching,
                            pinned = pinned,
                            onAdd = {
                                picking = it
                                sheet.open()
                            },
                            onLaunch = launch,
                            onUnpin = viewModel::unpin,
                            modifier = Modifier.fillMaxWidth().height(rowHeight),
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = screen == Screen.Home, enter = FadeThroughEnter, exit = FadeThroughExit) {
            AppSheet(
                apps = apps,
                docked = slots.filterNotNull().toSet(),
                dockFull = slots.all { it != null },
                launching = launching,
                onDock = { viewModel.dock(it.id) },
                onSelect = { app ->
                    val slot = picking
                    if (slot == null) {
                        launch(app)
                    } else {
                        viewModel.pin(app.id, slot)
                        sheet.close()
                    }
                },
                onUndock = { viewModel.undock(it.id) },
                sheet = sheet,
            )
        }
    }
}
