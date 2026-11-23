package com.thesabihjhatial.cockpit.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

private val TimeActions = listOf(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)

fun timeChangeFlow(context: Context): Flow<Unit> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            trySend(Unit)
        }
    }
    val filter = IntentFilter().apply { TimeActions.forEach(::addAction) }
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    awaitClose { context.unregisterReceiver(receiver) }
}
