package com.thesabihjhatial.cockpit.display

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.Flow

private const val DimThreshold = 0.5f
private const val MaxBrightness = 255f

fun isDim(brightness: Int): Boolean = brightness / MaxBrightness < DimThreshold

fun headlightsFlow(context: Context): Flow<Boolean> = callbackFlow {
    val resolver = context.contentResolver
    val uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)

    fun dim() = isDim(Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, MaxBrightness.toInt()))

    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            trySend(dim())
        }
    }
    resolver.registerContentObserver(uri, false, observer)
    trySend(dim())
    awaitClose { resolver.unregisterContentObserver(observer) }
}.distinctUntilChanged()

@Composable
fun rememberHeadlightsOn(): State<Boolean> {
    val context = LocalContext.current
    val flow = remember { headlightsFlow(context) }
    return flow.collectAsStateWithLifecycle(initialValue = false)
}
