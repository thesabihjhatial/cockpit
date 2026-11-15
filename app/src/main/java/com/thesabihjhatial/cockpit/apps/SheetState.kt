package com.thesabihjhatial.cockpit.apps

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val DurationMs = 250
private const val SnapPoint = 0.5f

@Stable
class SheetState(private val scope: CoroutineScope, private val onClosed: () -> Unit) {
    private var animation: Job? = null
    private var progress by mutableFloatStateOf(0f)

    var flingThreshold = 0f
    var travel = 1f

    val fraction: Float get() = progress
    val isMoving by derivedStateOf { progress > 0f && progress < 1f }
    val isOpen by derivedStateOf { progress >= 1f }
    val isVisible by derivedStateOf { progress > 0f }

    fun close() = animateTo(0f)

    fun dragBy(delta: Float): Float {
        animation?.cancel()
        val target = (progress - delta / travel).coerceIn(0f, 1f)
        val consumed = (progress - target) * travel
        progress = target
        return consumed
    }

    fun open() = animateTo(1f)

    fun settle(velocity: Float) = animateTo(
        when {
            velocity < -flingThreshold -> 1f
            velocity > flingThreshold -> 0f
            progress >= SnapPoint -> 1f
            else -> 0f
        },
    )

    private fun animateTo(target: Float) {
        animation?.cancel()
        animation = scope.launch {
            animate(progress, target, animationSpec = tween(DurationMs, easing = FastOutSlowInEasing)) { value, _ ->
                progress = value
            }
            if (target == 0f) onClosed()
        }
    }
}
