package com.thesabihjhatial.cockpit.splash

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.Gauge
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DurationMs = 3000
private const val EnterDurationMs = 300
private const val EnterScale = 0.96f
private const val ExitDurationMs = 150
private const val NeedleRestAngle = 0f
private const val NeedleStartAngle = -120f

@Composable
private fun Splash() {
    val colors = LocalCockpitColors.current
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val needle = remember { Animatable(if (reduceMotion) NeedleRestAngle else NeedleStartAngle) }
    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) return@LaunchedEffect
        launch { progress.animateTo(1f, tween(DurationMs, easing = LinearEasing)) }
        needle.animateTo(
            NeedleRestAngle,
            keyframes {
                durationMillis = DurationMs
                18f at 1650 using FastOutSlowInEasing
                NeedleRestAngle at 2250 using FastOutSlowInEasing
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(36.dp, Alignment.CenterVertically),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Gauge(
                needleAngle = needle.value,
                arcColor = colors.accent,
                needleColor = colors.text,
                trackColor = colors.ring,
                modifier = Modifier.size(104.dp),
            )
            Text(
                text = stringResource(R.string.logo_description).uppercase(),
                color = colors.text,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.26.em,
                lineHeight = 44.sp,
                modifier = Modifier.offset(x = (44 * 0.13f).dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.ring),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .background(colors.accent),
                )
            }
            Text(
                text = stringResource(R.string.splash_status),
                color = colors.muted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun SplashGate(content: @Composable (ready: Boolean) -> Unit) {
    var showing by rememberSaveable { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (showing) EnterScale else 1f,
        animationSpec = tween(EnterDurationMs),
        label = "scale",
    )

    LaunchedEffect(Unit) {
        delay(DurationMs.toLong())
        showing = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            content(!showing)
        }
        AnimatedVisibility(
            visible = showing,
            enter = EnterTransition.None,
            exit = fadeOut(tween(ExitDurationMs)),
        ) {
            Splash()
        }
    }
}
