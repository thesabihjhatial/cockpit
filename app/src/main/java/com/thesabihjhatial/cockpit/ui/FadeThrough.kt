package com.thesabihjhatial.cockpit.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

private const val EnterDelayMs = 75
private const val EnterDurationMs = 175
private const val ExitDurationMs = 75
private const val InitialScale = 0.94f

val FadeThrough: ContentTransform =
    (
        fadeIn(tween(EnterDurationMs, EnterDelayMs)) +
            scaleIn(tween(EnterDurationMs, EnterDelayMs), initialScale = InitialScale)
        ).togetherWith(fadeOut(tween(ExitDurationMs)))
