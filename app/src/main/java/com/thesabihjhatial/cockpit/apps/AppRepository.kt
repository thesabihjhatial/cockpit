package com.thesabihjhatial.cockpit.apps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.thesabihjhatial.cockpit.ui.DesignHeight
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

private const val AdaptiveCanvasDp = 108f
private const val AdaptiveVisibleDp = 72f
private const val IconSizeDp = 56

private val TileColors = ConcurrentHashMap<String, Int>()

private fun glyph(icon: Drawable, sizePx: Int): Bitmap {
    if (icon !is AdaptiveIconDrawable) return icon.toBitmap(sizePx, sizePx)
    val layer = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) icon.monochrome else null) ?: icon.foreground
    val canvasPx = (sizePx * AdaptiveCanvasDp / AdaptiveVisibleDp).roundToInt()
    val inset = (canvasPx - sizePx) / 2
    return Bitmap.createBitmap(layer.toBitmap(canvasPx, canvasPx), inset, inset, sizePx, sizePx)
}

suspend fun loadApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val packageManager = context.packageManager
    val metrics = context.resources.displayMetrics
    val iconSizePx = (IconSizeDp * minOf(metrics.widthPixels, metrics.heightPixels) / DesignHeight.toFloat()).roundToInt()
    launcherActivities(context)
                .map {
            async {
                val id = it.componentName.flattenToString()
                val drawable = it.loadIcon(packageManager)
                val icon = drawable.toBitmap(iconSizePx, iconSizePx)
                val version = File(it.activityInfo.applicationInfo.sourceDir).lastModified()
                AppInfo(
                    color = Color(TileColors.getOrPut("$id:$version") { tileColor(icon) }),
                    icon = glyph(drawable, iconSizePx).asImageBitmap(),
                    id = id,
                    label = it.loadLabel(packageManager).toString(),
                    tinted = drawable is AdaptiveIconDrawable,
                )
            }
        }
        .awaitAll()
        .sortedBy { it.label.lowercase() }
}
