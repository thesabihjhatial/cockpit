package com.thesabihjhatial.cockpit.media

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.bluetooth.BluetoothStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val PausedHoldMs = 30_000L

class MediaState(val granted: Boolean, val nowPlaying: NowPlaying?)

private fun Context.hasNotificationAccess(): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

@Composable
fun rememberMediaState(bluetooth: BluetoothStatus?): MediaState {
    val context = LocalContext.current
    val source = stringResource(R.string.media_bluetooth)
    val unknownTitle = stringResource(R.string.media_bluetooth_audio)
    var granted by remember { mutableStateOf(context.hasNotificationAccess()) }
    var recent by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { granted = context.hasNotificationAccess() }

    val sessionFlow: Flow<NowPlaying?> = remember(granted) {
        if (granted) nowPlayingFlow(context) else emptyFlow()
    }
    val session by sessionFlow.collectAsStateWithLifecycle(initialValue = null)
    val legacy by remember(source) { legacyMusicFlow(context, source) }.collectAsStateWithLifecycle(initialValue = null)
    val musicActive by remember { musicActiveFlow(context) }.collectAsStateWithLifecycle(initialValue = false)
    LaunchedEffect(musicActive) {
        if (musicActive) {
            recent = true
        } else {
            delay(PausedHoldMs)
            recent = false
        }
    }

    val bluetoothAudio = if (bluetooth is BluetoothStatus.Connected && (musicActive || recent)) {
        NowPlaying(null, bluetooth.deviceName, null, 0L, true, true, musicActive, 0L, 0L, source, 1f, unknownTitle)
    } else {
        null
    }
    return MediaState(granted, session ?: legacy?.takeIf { it.playing || musicActive || recent } ?: bluetoothAudio)
}
