package com.thesabihjhatial.cockpit.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

private fun activeControllerFlow(context: Context): Flow<MediaController?> = callbackFlow {
    val manager = context.getSystemService(MediaSessionManager::class.java)
    val listenerComponent = ComponentName(context, MediaListenerService::class.java)
    val listener = MediaSessionManager.OnActiveSessionsChangedListener { trySend(it?.preferred()) }
    runCatching { manager.addOnActiveSessionsChangedListener(listener, listenerComponent) }
    trySend(runCatching { manager.getActiveSessions(listenerComponent).preferred() }.getOrNull())
    awaitClose { manager.removeOnActiveSessionsChangedListener(listener) }
}

private fun Context.appLabel(packageName: String): String = runCatching {
    packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
}.getOrDefault(packageName)

@OptIn(ExperimentalCoroutinesApi::class)
fun nowPlayingFlow(context: Context): Flow<NowPlaying?> =
    activeControllerFlow(context).flatMapLatest { controller ->
        controller?.let { stateFlow(context, it) } ?: flowOf(null)
    }

private fun List<MediaController>.preferred(): MediaController? =
    firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: firstOrNull()

private fun stateFlow(context: Context, controller: MediaController): Flow<NowPlaying> = callbackFlow {
    val source = context.appLabel(controller.packageName)
    val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            trySend(controller.toNowPlaying(source))
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            trySend(controller.toNowPlaying(source))
        }
    }
    controller.registerCallback(callback)
    trySend(controller.toNowPlaying(source))
    awaitClose { controller.unregisterCallback(callback) }
}

private fun MediaController.toNowPlaying(source: String): NowPlaying {
    val data = metadata
    val state = playbackState
    val art: Bitmap? = data?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        ?: data?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        ?: data?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
    return NowPlaying(
        art = art?.asImageBitmap(),
        artist = data?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
        controller = this,
        duration = data?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
        playing = state?.state == PlaybackState.STATE_PLAYING,
        position = state?.position ?: 0L,
        positionUpdatedAt = state?.lastPositionUpdateTime ?: 0L,
        source = source,
        speed = state?.playbackSpeed ?: 1f,
        title = data?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
    )
}
