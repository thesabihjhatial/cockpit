package com.thesabihjhatial.cockpit.media

import android.media.session.MediaController
import android.os.SystemClock
import androidx.compose.ui.graphics.ImageBitmap

data class NowPlaying(
    val art: ImageBitmap?,
    val artist: String,
    val controller: MediaController,
    val duration: Long,
    val playing: Boolean,
    val position: Long,
    val positionUpdatedAt: Long,
    val source: String,
    val speed: Float,
    val title: String,
)

fun NowPlaying.currentPosition(): Long {
    val elapsed = if (playing && positionUpdatedAt > 0) ((SystemClock.elapsedRealtime() - positionUpdatedAt) * speed).toLong() else 0L
    val current = (position + elapsed).coerceAtLeast(0L)
    return if (duration > 0) current.coerceAtMost(duration) else current
}

fun NowPlaying.togglePlayback() {
    val controls = controller.transportControls
    if (playing) controls.pause() else controls.play()
}
