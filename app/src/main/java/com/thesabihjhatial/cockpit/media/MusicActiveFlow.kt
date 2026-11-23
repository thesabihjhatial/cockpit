package com.thesabihjhatial.cockpit.media

import android.content.Context
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

fun musicActiveFlow(context: Context): Flow<Boolean> = callbackFlow {
    val audio = context.getSystemService(AudioManager::class.java)
    val callback = object : AudioManager.AudioPlaybackCallback() {
        override fun onPlaybackConfigChanged(configs: List<AudioPlaybackConfiguration>) {
            trySend(audio.isMusicActive)
        }
    }
    audio.registerAudioPlaybackCallback(callback, null)
    trySend(audio.isMusicActive)
    awaitClose { audio.unregisterAudioPlaybackCallback(callback) }
}
