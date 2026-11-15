package com.thesabihjhatial.cockpit.apps

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

private const val DarkenStep = 0.06f
private const val FallbackColor = 0xFF4B4B52.toInt()
private const val MinContrast = 3.0

fun tileColor(icon: Bitmap): Int {
    var color = Palette.from(icon).generate().getDominantColor(FallbackColor)
    while (ColorUtils.calculateContrast(Color.WHITE, ColorUtils.setAlphaComponent(color, 255)) < MinContrast) {
        color = ColorUtils.blendARGB(color, Color.BLACK, DarkenStep)
    }
    return ColorUtils.setAlphaComponent(color, 255)
}
