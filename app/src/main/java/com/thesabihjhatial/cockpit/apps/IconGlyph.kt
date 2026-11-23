package com.thesabihjhatial.cockpit.apps

import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt

private const val AdaptiveCanvasDp = 108f
private const val AdaptiveVisibleDp = 72f
private const val GlyphCoverage = 0.4f
private const val OpaqueAlpha = 128

internal class Glyph(val bitmap: Bitmap, val tinted: Boolean)

internal fun glyph(icon: Drawable, sizePx: Int): Glyph {
    if (icon !is AdaptiveIconDrawable) return Glyph(icon.toBitmap(sizePx, sizePx), false)
    val monochrome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) icon.monochrome else null
    val canvasPx = (sizePx * AdaptiveCanvasDp / AdaptiveVisibleDp).roundToInt()
    val inset = (canvasPx - sizePx) / 2

    fun visible(drawable: Drawable): Bitmap =
        Bitmap.createBitmap(drawable.toBitmap(canvasPx, canvasPx), inset, inset, sizePx, sizePx)

    val layer = visible(monochrome ?: icon.foreground)
    return if (monochrome != null || isGlyph(layer)) Glyph(layer, true) else Glyph(visible(icon), false)
}

internal fun glyphCoverage(pixels: IntArray): Float =
    if (pixels.isEmpty()) 0f else pixels.count { it ushr 24 >= OpaqueAlpha } / pixels.size.toFloat()

private fun isGlyph(layer: Bitmap): Boolean {
    val pixels = IntArray(layer.width * layer.height)
    layer.getPixels(pixels, 0, layer.width, 0, 0, layer.width, layer.height)
    return glyphCoverage(pixels) < GlyphCoverage
}
