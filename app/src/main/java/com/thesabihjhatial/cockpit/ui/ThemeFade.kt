package com.thesabihjhatial.cockpit.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier

private const val InMs = 62
private const val OutMs = 63

@Composable
fun ThemeFade(night: Boolean, content: @Composable (night: Boolean) -> Unit) {
    var shown by remember { mutableStateOf(night) }
    val veil = remember { Animatable(0f) }

    LaunchedEffect(night) {
        if (night != shown) {
            veil.animateTo(1f, tween(InMs, easing = LinearEasing))
            shown = night
        }
        veil.animateTo(0f, tween(OutMs, easing = LinearEasing))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content(shown)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = veil.value }
                .background(cockpitColors(shown).background),
        )
    }
}
