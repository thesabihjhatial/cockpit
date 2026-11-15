package com.thesabihjhatial.cockpit.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart

@Composable
fun rememberNow(): State<LocalDateTime> {
    val context = LocalContext.current
    return produceState(LocalDateTime.now()) {
        timeChangeFlow(context).onStart { emit(Unit) }.collectLatest {
            while (true) {
                val current = LocalDateTime.now()
                value = current
                delay(Duration.between(current, current.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)).toMillis())
            }
        }
    }
}
