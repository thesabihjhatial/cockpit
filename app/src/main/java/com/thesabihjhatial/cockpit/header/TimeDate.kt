package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH)
private val TimeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
private val WeekdayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)

private const val DateWeight = 66f
private const val TimeWeight = 34f

@Composable
fun TimeDate(now: LocalDateTime, modifier: Modifier = Modifier) {
    val colors = LocalCockpitColors.current

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(TimeWeight)) {
            Text(
                text = now.format(TimeFormatter),
                fontSize = 84.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.045).em,
                lineHeight = 77.sp,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.wrapContentWidth(Alignment.Start, unbounded = true),
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
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(now.format(WeekdayFormatter) + ",") }
                    withStyle(SpanStyle(color = colors.text2, fontWeight = FontWeight.Medium)) {
                        append(" " + now.format(DateFormatter))
                    }
                },
                fontSize = 26.sp,
                letterSpacing = (-0.01).em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
