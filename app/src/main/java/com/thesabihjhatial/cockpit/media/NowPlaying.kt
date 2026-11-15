package com.thesabihjhatial.cockpit.media

import android.content.Context
import android.media.AudioManager
import android.media.session.MediaController
import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.graphics.ImageBitmap

data class NowPlaying(
    val art: ImageBitmap?,
    val artist: String,
    val controller: MediaController?,
    val duration: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val playing: Boolean,
    val position: Long,
    val positionUpdatedAt: Long,
    val source: String,
    val speed: Float,
    val title: String,
)

private fun Context.sendMediaKey(keyCode: Int) {
    val audio = getSystemService(AudioManager::class.java)
    audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
}

fun NowPlaying.currentPosition(): Long {
    val elapsed = if (playing && positionUpdatedAt > 0) ((SystemClock.elapsedRealtime() - positionUpdatedAt) * speed).toLong() else 0L
    val current = (position + elapsed).coerceAtLeast(0L)
    return if (duration > 0) current.coerceAtMost(duration) else current
}

fun NowPlaying.skipToNext(context: Context) {
    if (controller != null) controller.transportControls.skipToNext() else context.sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
}

fun NowPlaying.skipToPrevious(context: Context) {
    if (controller != null) controller.transportControls.skipToPrevious() else context.sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
}

fun NowPlaying.togglePlayback(context: Context) {
    val controls = controller?.transportControls
    when {
        controls == null -> context.sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        playing -> controls.pause()
        else -> controls.play()
    }
}
