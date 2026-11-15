package com.thesabihjhatial.cockpit.media

import android.media.audiofx.Visualizer
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

const val BarCount = 21

private const val CaptureSize = 256
private const val DecayFactor = 0.82f
private const val FullScaleMagnitude = 60f
private const val SignalFloor = 0.02f
private const val SilenceMs = 1500L

private fun levels(fft: ByteArray, previous: FloatArray): FloatArray {
    val bins = fft.size / 2
    return FloatArray(BarCount) { bar ->
        val from = bins.toFloat().pow(bar / BarCount.toFloat()).toInt().coerceIn(1, bins - 1)
        val to = bins.toFloat().pow((bar + 1) / BarCount.toFloat()).toInt().coerceIn(from + 1, bins)
        var sum = 0f
        for (bin in from until to) sum += hypot(fft[2 * bin].toFloat(), fft[2 * bin + 1].toFloat())
        max(sqrt(sum / (to - from) / FullScaleMagnitude).coerceIn(0f, 1f), previous[bar] * DecayFactor)
    }
}

@Composable
fun rememberSpectrum(enabled: Boolean, playing: Boolean): State<FloatArray?> {
    val spectrum = remember { mutableStateOf<FloatArray?>(null) }
    val isPlaying by rememberUpdatedState(playing)

    DisposableEffect(enabled) {
        val visualizer = if (enabled) {
            runCatching { startVisualizer { if (isPlaying) spectrum.value = it } }.getOrNull()
        } else {
            null
        }
        onDispose {
            visualizer?.let {
                it.enabled = false
                it.release()
            }
            spectrum.value = null
        }
    }
    return spectrum
}

private fun startVisualizer(onLevels: (FloatArray?) -> Unit): Visualizer {
    val visualizer = Visualizer(0)
    val range = Visualizer.getCaptureSizeRange()
    visualizer.captureSize = CaptureSize.coerceIn(range[0], range[1])
    var previous = FloatArray(BarCount)
    var lastSignal = SystemClock.elapsedRealtime()
    visualizer.setDataCaptureListener(
        object : Visualizer.OnDataCaptureListener {
            override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {
                previous = levels(fft, previous)
                val now = SystemClock.elapsedRealtime()
                if (previous.any { it > SignalFloor }) lastSignal = now
                onLevels(if (now - lastSignal > SilenceMs) null else previous)
            }

            override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) = Unit
        },
        Visualizer.getMaxCaptureRate(),
        false,
        true,
    )
    visualizer.enabled = true
    return visualizer
}
