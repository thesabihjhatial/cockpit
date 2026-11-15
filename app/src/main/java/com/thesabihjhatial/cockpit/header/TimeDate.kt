package com.thesabihjhatial.cockpit.header

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import java.time.LocalDateTime

private const val DateWeight = 66f
private const val TimeWeight = 34f

@Composable
fun TimeDate(now: LocalDateTime, onClick: () -> Unit, modifier: Modifier = Modifier, timeModifier: Modifier = Modifier) {
    val colors = LocalCockpitColors.current

    Row(
        modifier = modifier.clickable(onClickLabel = stringResource(R.string.clock_expand), onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(TimeWeight)) {
            Text(
                text = now.timeText(),
                fontSize = 84.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.045).em,
                lineHeight = 77.sp,
                maxLines = 1,
                softWrap = false,
                modifier = timeModifier.wrapContentWidth(Alignment.Start, unbounded = true),
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
