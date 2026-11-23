package com.thesabihjhatial.cockpit.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle

/**
 * The launcher's palette. There is exactly one: a head unit lives in a dark cabin, and a
 * light theme cost a second palette, a cross-fade and a brightness heuristic to choose
 * between them, for no benefit.
 */
@Immutable
data class CockpitColors(
    val accent: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val background: Color,
    val line: Color,
    val muted: Color,
    val raised: Color,
    val ring: Color,
    val surface: Color,
    val text: Color,
    val text2: Color,
    val track: Color,
)

val CockpitPalette = CockpitColors(
    accent = Color(0xFFE5484D),
    accentInk = Color(0xFFFF9592),
    accentSoft = Color(0x33E5484D),
    background = Color(0xFF0F0F10),
    line = Color(0xFF3A3A3E),
    muted = Color(0xFF9E9EA6),
    raised = Color(0xFF262628),
    ring = Color(0xFF262628),
    surface = Color(0xFF1A1A1C),
    text = Color(0xFFF2F2F3),
    text2 = Color(0xFFC7C7CC),
    track = Color(0xFF2A2A2D),
)

val LocalCockpitColors = compositionLocalOf { CockpitPalette }

/**
 * Provides the locals the UI reads: palette, default text style and press feedback.
 *
 * No MaterialTheme and no palette switching — the app draws its own widgets and the
 * palette is a constant.
 */
@Composable
fun CockpitTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalCockpitColors provides CockpitPalette,
        LocalIndication provides PressIndication,
        LocalTextStyle provides TextStyle.Default.merge(
            SpanStyle(
                color = CockpitPalette.text,
                fontFamily = InterFamily,
                fontFeatureSettings = "tnum, cv11",
            ),
        ),
        content = content,
    )
}
