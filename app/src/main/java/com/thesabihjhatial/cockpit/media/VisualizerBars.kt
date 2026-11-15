package com.thesabihjhatial.cockpit.media

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

private const val Center = 10
private const val FallSeconds = 0.14f
private const val MinScale = 0.22f
private const val NanosPerSecond = 1_000_000_000f
private const val RiseSeconds = 0.05f

private fun opacity(index: Int): Float = 0.55f + 0.45f * (1f - abs(index - Center) / Center.toFloat())

private fun peak(index: Int): Float {
    val distance = abs(index - Center) / Center.toFloat()
    return min(100f, 34f + 62f * cos(distance * PI.toFloat() / 2f) + (index * 37) % 11 - 5f) / 100f
}

private fun wave(index: Int, time: Float): Float {
    val low = 0.22f + (index * 53 % 30) / 100f
    val duration = 0.7f + (index * 29 % 9) / 10f
    val offset = (index * 41 % 13) / 10f
    val phase = ((time + offset) / duration) % 1f
    return low + (1f - low) * (1f - cos(2f * PI.toFloat() * phase)) / 2f
}

@Composable
fun VisualizerBars(playing: Boolean, spectrum: State<FloatArray?>, modifier: Modifier = Modifier) {
    val accent = LocalCockpitColors.current.accent
    val levels = remember { FloatArray(BarCount) }
    val frame = remember { mutableLongStateOf(0L) }
    val clock = remember { floatArrayOf(0f) }
    val path = remember { Path() }

    LaunchedEffect(playing) {
        if (!playing) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val delta = (now - last) / NanosPerSecond
            last = now
            val live = spectrum.value
            if (live == null) clock[0] += delta
            for (index in 0 until BarCount) {
                val target = if (live != null) MinScale + (1f - MinScale) * live[index] else wave(index, clock[0])
                val seconds = if (target > levels[index]) RiseSeconds else FallSeconds
                levels[index] += (target - levels[index]) * (1f - exp(-delta / seconds))
            }
            frame.longValue++
        }
    }

    Canvas(modifier = modifier) {
        frame.longValue
        val gap = 12.dp.toPx()
        val barWidth = (size.width - gap * (BarCount - 1)) / BarCount
        val top = CornerRadius(10.dp.toPx())
        val bottom = CornerRadius(4.dp.toPx())
        val floor = 8.dp.toPx()
        repeat(BarCount) { index ->
            val height = max(floor, size.height * peak(index) * levels[index])
            val left = index * (barWidth + gap)
            path.reset()
            path.addRoundRect(RoundRect(left, size.height - height, left + barWidth, size.height, top, top, bottom, bottom))
            drawPath(path, accent.copy(alpha = opacity(index)))
        }
    }
}
