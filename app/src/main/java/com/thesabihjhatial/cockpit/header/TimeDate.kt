package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.ui.CockpitPalette
import com.thesabihjhatial.cockpit.ui.InterFamily
import com.thesabihjhatial.cockpit.ui.Text
import java.time.LocalDateTime

private const val DateWeight = 60f
private const val LineHeightRatio = 0.92f
private const val MaxTimeSp = 84f
private const val MinTimeSp = 44f
private const val TimeWeight = 40f

private fun timeStyle(size: Float, color: Color) = TextStyle(
    color = color,
    fontFamily = InterFamily,
    fontSize = size.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = (-0.045).em,
    lineHeight = (size * LineHeightRatio).sp,
    fontFeatureSettings = "tnum, cv11",
)

@Composable
private fun fittedTimeSize(text: String, maxWidth: Dp): TextUnit {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(text, maxWidth, density) {
        val limit = with(density) { maxWidth.toPx() }
        var size = MaxTimeSp
        while (size > MinTimeSp) {
            val width = measurer.measure(
                text = AnnotatedString(text),
                style = timeStyle(size, Color.Unspecified),
                overflow = TextOverflow.Clip,
                softWrap = false,
                maxLines = 1,
            ).size.width
            if (width <= limit) break
            size -= 2f
        }
        size.sp
    }
}

@Composable
fun TimeDate(now: LocalDateTime, modifier: Modifier = Modifier) {
    val colors = CockpitPalette
    val time = now.timeText()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(modifier = Modifier.weight(TimeWeight)) {
            val size = fittedTimeSize(time, maxWidth)
            Text(
                text = time,
                style = timeStyle(size.value, colors.text),
                maxLines = 1,
                softWrap = false,
            )
        }
        Row(
            modifier = Modifier
                .weight(DateWeight)
                .heightIn(min = 72.dp)
                .drawBehind {
                    drawLine(colors.line, Offset.Zero, Offset(0f, size.height), strokeWidth = 1.dp.toPx())
                }
                .padding(start = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = now.dateText(colors),
                fontSize = 26.sp,
                letterSpacing = (-0.01).em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
