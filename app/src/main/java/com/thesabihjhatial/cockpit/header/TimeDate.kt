package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.InterFamily
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import com.thesabihjhatial.cockpit.ui.Text
import java.time.LocalDateTime

// The date string is longer but the clock is far larger. 40/60 is the split where a
// five-character time such as "12:59" fits its column at 16:9 without shrinking.
private const val DateWeight = 60f
private const val TimeWeight = 40f

private const val MaxTimeSp = 84f
private const val MinTimeSp = 44f
private const val LineHeightRatio = 0.92f

/**
 * The style the clock is drawn with, and the one its width is measured with.
 *
 * The colour belongs here: `Text` only inherits the theme's `LocalTextStyle` when no
 * style is passed, so an explicit style without a colour renders in
 * `LocalContentColor` (black) rather than the theme's text colour.
 */
private fun timeStyle(size: Float, color: Color) = TextStyle(
    color = color,
    fontFamily = InterFamily,
    fontSize = size.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = (-0.045).em,
    lineHeight = (size * LineHeightRatio).sp,
    fontFeatureSettings = "tnum, cv11",
)

/**
 * Largest size at which the clock still fits its column.
 *
 * The row is width-driven while the design scale is height-driven, so on a panel
 * narrower than 16:9 a fixed 84 sp overruns the column and crosses the divider into
 * the date. Measuring here keeps the intended 84 sp at 16:9 and only shrinks when
 * the panel really is narrower.
 */
@Composable
private fun fittedTimeSize(text: String, maxWidth: Dp, color: Color): TextUnit {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(text, maxWidth, density) {
        val limit = with(density) { maxWidth.toPx() }
        var size = MaxTimeSp
        while (size > MinTimeSp) {
            val width = measurer.measure(
                text = AnnotatedString(text),
                style = timeStyle(size, color),
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
fun TimeDate(now: LocalDateTime, onClick: () -> Unit, modifier: Modifier = Modifier, timeModifier: Modifier = Modifier) {
    val colors = LocalCockpitColors.current
    val time = now.timeText()

    Row(
        modifier = modifier.clickable(onClickLabel = stringResource(R.string.clock_expand), onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(modifier = Modifier.weight(TimeWeight)) {
            val size = fittedTimeSize(time, maxWidth, colors.text)
            Text(
                text = time,
                style = timeStyle(size.value, colors.text),
                maxLines = 1,
                softWrap = false,
                modifier = timeModifier,
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
