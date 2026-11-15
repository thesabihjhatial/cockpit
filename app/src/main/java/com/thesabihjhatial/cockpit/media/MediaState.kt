package com.thesabihjhatial.cockpit.media

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class MediaState(val granted: Boolean, val nowPlaying: NowPlaying?)

private fun Context.hasNotificationAccess(): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

@Composable
fun rememberMediaState(): MediaState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasNotificationAccess()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { granted = context.hasNotificationAccess() }

    val flow: Flow<NowPlaying?> = remember(granted) {
        if (granted) nowPlayingFlow(context) else emptyFlow()
    }
    val nowPlaying by flow.collectAsStateWithLifecycle(initialValue = null)
    return MediaState(granted, nowPlaying)
}
