package com.thesabihjhatial.cockpit.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

const val FadeInDelayMs = 37
const val FadeInMs = 88
const val FadeOutMs = 37
private const val InitialScale = 0.94f

val FadeThroughEnter: EnterTransition = fadeIn(tween(FadeInMs, FadeInDelayMs))

val FadeThroughExit: ExitTransition = fadeOut(tween(FadeOutMs))

val FadeThrough: ContentTransform =
    (FadeThroughEnter + scaleIn(tween(FadeInMs, FadeInDelayMs), initialScale = InitialScale)).togetherWith(FadeThroughExit)
