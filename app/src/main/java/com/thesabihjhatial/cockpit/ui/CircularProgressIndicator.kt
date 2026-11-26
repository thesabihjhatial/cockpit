package com.thesabihjhatial.cockpit.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

private const val SpinMs = 900
private const val SweepDegrees = 90f

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 4.dp,
    trackColor: Color,
) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(SpinMs, easing = LinearEasing), RepeatMode.Restart),
        label = "angle",
    )

    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)
        val cap = Stroke(width = stroke, cap = StrokeCap.Round)
        drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = cap)
        drawArc(color, angle - 90f, SweepDegrees, false, topLeft, arcSize, style = cap)
    }
}
