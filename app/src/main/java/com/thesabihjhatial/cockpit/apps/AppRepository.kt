package com.thesabihjhatial.cockpit.apps

import android.content.Context
import android.content.pm.ResolveInfo
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import com.thesabihjhatial.cockpit.ui.DesignHeight
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

private const val IconSizeDp = 56

// Rasterising icons is CPU bound, so flooding the IO pool on a low-end SoC costs
// more in contention than it hides in latency.
private const val ParallelDecodes = 4

private class Entry(val id: String, val info: ResolveInfo, val label: String)

suspend fun loadApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val packageManager = context.packageManager
    val metrics = context.resources.displayMetrics
    val iconSizePx = (IconSizeDp * minOf(metrics.widthPixels, metrics.heightPixels) / DesignHeight.toFloat()).roundToInt()
    val colors = TileColorCache(context)
    val keys = ConcurrentHashMap.newKeySet<String>()
    val gate = Semaphore(ParallelDecodes)
    val launchers = launcherActivities(context).map { Entry(it.componentName.flattenToString(), it, it.loadLabel(packageManager).toString()) }
    val shortcuts = modeShortcuts(context).map { Entry(shortcutId(it.info.componentName, it.action), it.info, context.getString(it.label)) }
    val apps = (launchers + shortcuts)
        .map { entry ->
            async {
                gate.withPermit {
                    val drawable = entry.info.loadIcon(packageManager)
                    val version = File(entry.info.activityInfo.applicationInfo.sourceDir).lastModified()
                    val key = entry.id + ":" + version
                    keys += key
                    // Palette extraction is the expensive half of this loop, so it
                    // runs only for an app or app version we have never coloured.
                    val color = colors.get(key) ?: tileColor(drawable.toBitmap(iconSizePx, iconSizePx)).also { colors.put(key, it) }
                    val glyph = glyph(drawable, iconSizePx)
                    AppInfo(
                        color = Color(color),
                        icon = glyph.bitmap.asImageBitmap(),
                        id = entry.id,
                        label = entry.label,
                        tinted = glyph.tinted,
                    )
                }
            }
        }
        .awaitAll()
        .sortedBy { it.label.lowercase() }

    colors.retainOnly(keys)
    apps
}
