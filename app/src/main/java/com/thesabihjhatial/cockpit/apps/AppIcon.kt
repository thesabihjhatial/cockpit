package com.thesabihjhatial.cockpit.apps

import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt

private const val AdaptiveCanvasDp = 108f
private const val AdaptiveVisibleDp = 72f

internal fun iconBitmap(icon: Drawable, sizePx: Int): Bitmap {
    if (icon !is AdaptiveIconDrawable) return icon.toBitmap(sizePx, sizePx)
    val canvasPx = (sizePx * AdaptiveCanvasDp / AdaptiveVisibleDp).roundToInt()
    val inset = (canvasPx - sizePx) / 2
    return Bitmap.createBitmap(icon.toBitmap(canvasPx, canvasPx), inset, inset, sizePx, sizePx)
}
