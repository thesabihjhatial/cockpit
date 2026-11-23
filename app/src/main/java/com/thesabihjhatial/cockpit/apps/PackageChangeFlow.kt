package com.thesabihjhatial.cockpit.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

private val PackageActions = listOf(
    Intent.ACTION_PACKAGE_ADDED,
    Intent.ACTION_PACKAGE_REMOVED,
    Intent.ACTION_PACKAGE_REPLACED,
)

fun packageChangeFlow(context: Context): Flow<Unit> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            trySend(Unit)
        }
    }
    val filter = IntentFilter().apply {
        PackageActions.forEach(::addAction)
        addDataScheme("package")
    }
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    awaitClose { context.unregisterReceiver(receiver) }
}
