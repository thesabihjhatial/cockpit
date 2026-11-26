package com.thesabihjhatial.cockpit.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

private const val ActionMetaChanged = "com.android.music.metachanged"
private const val ActionPlayStateChanged = "com.android.music.playstatechanged"

@Suppress("DEPRECATION")
private fun Intent.number(key: String): Long = (extras?.get(key) as? Number)?.toLong() ?: 0L

fun legacyMusicFlow(context: Context, source: String): Flow<NowPlaying?> = callbackFlow {
    var current: NowPlaying? = null

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            val base = current ?: NowPlaying(playing = true, source = source)
            val playing = if (intent.hasExtra("playing")) intent.getBooleanExtra("playing", false) else base.playing
            current = if (intent.action == ActionMetaChanged) {
                base.copy(
                    artist = intent.getStringExtra("artist").orEmpty(),
                    duration = intent.number("duration"),
                    playing = playing,
                    position = intent.number("position"),
                    positionUpdatedAt = SystemClock.elapsedRealtime(),
                    title = intent.getStringExtra("track").orEmpty(),
                )
            } else {
                base.copy(playing = playing, position = base.currentPosition(), positionUpdatedAt = SystemClock.elapsedRealtime())
            }
            trySend(current?.takeIf { it.title.isNotEmpty() })
        }
    }
    val filter = IntentFilter().apply {
        addAction(ActionMetaChanged)
        addAction(ActionPlayStateChanged)
    }
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
    trySend(null)
    awaitClose { context.unregisterReceiver(receiver) }
}
