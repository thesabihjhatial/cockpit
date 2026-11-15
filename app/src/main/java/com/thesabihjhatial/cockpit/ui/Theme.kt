package com.thesabihjhatial.cockpit.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private const val NightEndHour = 6
private const val NightStartHour = 18

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

val DayColors = CockpitColors(
    accent = Color(0xFFD92D20),
    accentInk = Color(0xFFB42318),
    accentSoft = Color(0x2BD92D20),
    background = Color(0xFFF5F5F6),
    line = Color(0xFFD4D4D8),
    muted = Color(0xFF5E5E66),
    raised = Color(0xFFEFEFF1),
    ring = Color(0xFFE4E4E7),
    surface = Color(0xFFFFFFFF),
    text = Color(0xFF18181B),
    text2 = Color(0xFF3F3F46),
    track = Color(0xFFE2E2E6),
)

val LocalCockpitColors = compositionLocalOf { DayColors }

val NightColors = CockpitColors(
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

fun cockpitColors(night: Boolean): CockpitColors = if (night) NightColors else DayColors

fun isDark(hour: Int, headlights: Boolean): Boolean = headlights || isNight(hour)

fun isNight(hour: Int): Boolean = hour >= NightStartHour || hour < NightEndHour

@Composable
fun CockpitTheme(night: Boolean, content: @Composable () -> Unit) {
    val colors = cockpitColors(night)
    val scheme = (if (night) darkColorScheme() else lightColorScheme()).copy(
        background = colors.background,
        onBackground = colors.text,
        onSurface = colors.text,
        surface = colors.surface,
        surfaceContainer = colors.surface,
    )

    CompositionLocalProvider(LocalCockpitColors provides colors) {
        MaterialTheme(colorScheme = scheme) {
            ProvideTextStyle(
                value = TextStyle(color = colors.text, fontFamily = InterFamily, fontFeatureSettings = "tnum, cv11"),
                content = content,
            )
        }
    }
}
