package com.thesabihjhatial.cockpit.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

private const val GearPath = "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"

private class Layer(val path: String, val filled: Boolean = false, val width: Float = 0f)

private fun glyph(name: String, vararg layers: Layer): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        layers.forEach { layer ->
            addPath(
                pathData = PathParser().parsePathString(layer.path).toNodes(),
                fill = if (layer.filled) SolidColor(Color.Black) else null,
                stroke = if (layer.width > 0f) SolidColor(Color.Black) else null,
                strokeLineWidth = layer.width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object CockpitIcons {
    val Add = glyph("Add", Layer("M12 5v14M5 12h14", width = 2.2f))
    val Bluetooth = glyph("Bluetooth", Layer("M7 7l10 10-5 5V2l5 5L7 17", width = 2.2f))
    val MusicNote = glyph(
        "MusicNote",
        Layer("M9 18V5l12-2v13", width = 1.5f),
        Layer("M3 18a3 3 0 1 0 6 0a3 3 0 1 0-6 0", width = 1.5f),
        Layer("M15 16a3 3 0 1 0 6 0a3 3 0 1 0-6 0", width = 1.5f),
    )
    val Next = glyph("Next", Layer("M5 4l10 8-10 8V4z", filled = true, width = 2f), Layer("M19 5v14", width = 2f))
    val Pause = glyph(
        "Pause",
        Layer("M7.2 4.5h1.6A1.2 1.2 0 0 1 10 5.7v12.6a1.2 1.2 0 0 1-1.2 1.2H7.2A1.2 1.2 0 0 1 6 18.3V5.7A1.2 1.2 0 0 1 7.2 4.5z", filled = true),
        Layer("M15.2 4.5h1.6A1.2 1.2 0 0 1 18 5.7v12.6a1.2 1.2 0 0 1-1.2 1.2h-1.6A1.2 1.2 0 0 1 14 18.3V5.7A1.2 1.2 0 0 1 15.2 4.5z", filled = true),
    )
    val Play = glyph(
        "Play",
        Layer("M8 4.8v14.4c0 .8.9 1.3 1.6.8l11-7.2c.6-.4.6-1.2 0-1.6l-11-7.2C8.9 3.5 8 4 8 4.8z", filled = true),
    )
    val Previous = glyph("Previous", Layer("M19 20L9 12l10-8v16z", filled = true, width = 2f), Layer("M5 19V5", width = 2f))
    val Settings = glyph("Settings", Layer("M9 12a3 3 0 1 0 6 0a3 3 0 1 0-6 0", width = 2f), Layer(GearPath, width = 2f))
    val Wifi = glyph(
        "Wifi",
        Layer("M2 8.8a15 15 0 0 1 20 0M5 12.6a10 10 0 0 1 14 0M8.5 16.4a5 5 0 0 1 7 0", width = 2.2f),
        Layer("M11 20a1 1 0 1 0 2 0a1 1 0 1 0-2 0", filled = true),
    )
}
