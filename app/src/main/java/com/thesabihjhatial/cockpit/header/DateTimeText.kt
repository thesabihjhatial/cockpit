package com.thesabihjhatial.cockpit.header

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.thesabihjhatial.cockpit.ui.CockpitColors
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.Locale

private val DateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH)
private val TimeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
private val WeekdayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)

internal fun LocalDateTime.dateText(colors: CockpitColors): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(format(WeekdayFormatter) + ",") }
    withStyle(SpanStyle(color = colors.text2, fontWeight = FontWeight.Medium)) { append(" " + format(DateFormatter)) }
}

internal fun LocalDateTime.timeText(): String = format(TimeFormatter)
