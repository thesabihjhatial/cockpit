package com.thesabihjhatial.cockpit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Modifier

private const val ArcRadius = 18f
private const val ArcStart = 135f
private const val ArcStroke = 4f
private const val ArcSweep = 270f
private const val Center = 24f
private const val DotRadius = 3.6f
private const val FilledSweep = 135f
private const val NeedleLength = 13f
private const val NeedleStroke = 3f
private const val ViewBox = 48f

@Composable
fun Gauge(
    needleAngle: Float,
    arcColor: Color,
    needleColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val unit = size.width / ViewBox
        val arcTopLeft = Offset((Center - ArcRadius) * unit, (Center - ArcRadius) * unit)
        val arcSize = Size(ArcRadius * 2 * unit, ArcRadius * 2 * unit)
        val center = Offset(Center * unit, Center * unit)
        val stroke = Stroke(width = ArcStroke * unit, cap = StrokeCap.Round)

        drawArc(trackColor, ArcStart, ArcSweep, false, arcTopLeft, arcSize, style = stroke)
        drawArc(arcColor, ArcStart, FilledSweep, false, arcTopLeft, arcSize, style = stroke)
        rotate(needleAngle, center) {
            drawLine(
                color = needleColor,
                start = center,
                end = Offset(center.x, center.y - NeedleLength * unit),
                strokeWidth = NeedleStroke * unit,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(arcColor, DotRadius * unit, center)
    }
}
