package com.thesabihjhatial.cockpit.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/** The theme's default text style. Stands in for material3's LocalTextStyle. */
val LocalTextStyle = compositionLocalOf {
    TextStyle.Default.merge(SpanStyle(fontFamily = InterFamily, fontFeatureSettings = "tnum, cv11"))
}

/**
 * Resolves the style the way material3's Text does: an explicit colour wins, then the
 * colour carried by the passed style, then the theme's text colour. That ordering is
 * what keeps the clock — whose style carries its own colour — legible at night.
 */
@Composable
private fun resolvedStyle(
    color: Color,
    fontSize: TextUnit,
    fontStyle: FontStyle?,
    fontWeight: FontWeight?,
    fontFamily: FontFamily?,
    letterSpacing: TextUnit,
    textDecoration: TextDecoration?,
    textAlign: TextAlign?,
    lineHeight: TextUnit,
    style: TextStyle,
): TextStyle {
    val fallback = LocalCockpitColors.current.text
    val resolved = when {
        color != Color.Unspecified -> color
        style.color != Color.Unspecified -> style.color
        else -> fallback
    }
    return style
        .merge(
            SpanStyle(
                color = resolved,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
            ),
        )
        .merge(
            ParagraphStyle(
                textAlign = textAlign ?: TextAlign.Unspecified,
                lineHeight = lineHeight,
            ),
        )
}

/** Text, with material3's parameter list so call sites are unchanged. */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = resolvedStyle(
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            style = style,
        ),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

/** Spanned text, which the date line needs for its two-tone weekday. */
@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = resolvedStyle(
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            style = style,
        ),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}
