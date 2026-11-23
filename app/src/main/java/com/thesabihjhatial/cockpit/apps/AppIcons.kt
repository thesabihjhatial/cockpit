package com.thesabihjhatial.cockpit.apps

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal const val TileIconDp = 56

private val cache = ConcurrentHashMap<String, ImageBitmap>()

internal suspend fun loadAppIcon(context: Context, app: AppInfo, sizePx: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        val key = app.id + "@" + sizePx
        cache[key]?.let { return@withContext it }
        val component = ComponentName.unflattenFromString(app.id.substringBefore(ActionSeparator))
            ?: return@withContext null
        val drawable = runCatching { context.packageManager.getActivityIcon(component) }.getOrNull()
            ?: return@withContext null
        iconBitmap(drawable, sizePx).asImageBitmap().also { cache[key] = it }
    }

@Composable
internal fun rememberAppIcon(app: AppInfo): ImageBitmap? {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { TileIconDp.dp.roundToPx() }
    var icon by remember(app.id, sizePx) { mutableStateOf(cache[app.id + "@" + sizePx]) }
    LaunchedEffect(app.id, sizePx) {
        if (icon == null) icon = loadAppIcon(context, app, sizePx)
    }
    return icon
}
