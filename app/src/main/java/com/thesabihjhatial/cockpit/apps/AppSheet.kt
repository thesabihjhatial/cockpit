package com.thesabihjhatial.cockpit.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import kotlin.math.roundToInt

private val Gap = 16.dp
private val GridPadding = 32.dp
private val TileHeight = 152.dp

private class SheetScrollConnection(private val sheet: SheetState) : NestedScrollConnection {
    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (!sheet.isMoving) return Velocity.Zero
        sheet.settle(available.y)
        return available
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        if (source == NestedScrollSource.UserInput && available.y > 0f) Offset(0f, sheet.dragBy(available.y)) else Offset.Zero

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (!sheet.isMoving) return Velocity.Zero
        sheet.settle(available.y)
        return available
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
        if (source == NestedScrollSource.UserInput && sheet.fraction < 1f) Offset(0f, sheet.dragBy(available.y)) else Offset.Zero
}

@Composable
fun AppSheet(
    apps: List<AppInfo>,
    docked: Set<String>,
    dockFull: Boolean,
    launching: String?,
    onDock: (AppInfo) -> Unit,
    onSelect: (AppInfo) -> Unit,
    onUndock: (AppInfo) -> Unit,
    sheet: SheetState,
    modifier: Modifier = Modifier,
) {
    val colors = LocalCockpitColors.current
    val connection = remember(sheet) { SheetScrollConnection(sheet) }
    val gridState = rememberLazyGridState()
    val addLabel = stringResource(R.string.dock_add_to)
    val badge = stringResource(R.string.dock_badge)
    val fullLabel = stringResource(R.string.dock_full)
    val removeLabel = stringResource(R.string.dock_remove)

    LaunchedEffect(sheet.isVisible) { if (!sheet.isVisible) gridState.scrollToItem(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, ((1f - sheet.fraction) * sheet.travel).roundToInt()) }
            .drawBehind { drawRect(color = colors.background, alpha = sheet.fraction) }
            .then(if (sheet.isVisible) Modifier.pointerInput(Unit) {} else Modifier)
            .nestedScroll(connection),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(GridPadding),
            horizontalArrangement = Arrangement.spacedBy(Gap),
            verticalArrangement = Arrangement.spacedBy(Gap),
        ) {
            items(apps, key = { it.id }) { app ->
                val isDocked = app.id in docked
                AppTile(
                    app = app,
                    onClick = { if (sheet.isOpen) onSelect(app) else sheet.open() },
                    modifier = Modifier.fillMaxWidth().height(TileHeight),
                    actions = when {
                        !sheet.isOpen -> emptyList()
                        isDocked -> listOf(TileAction(removeLabel, onSelect = { onUndock(app) }))
                        else -> listOf(TileAction(if (dockFull) fullLabel else addLabel, onSelect = { onDock(app) }, enabled = !dockFull))
                    },
                    badge = if (isDocked) badge else null,
                    enabled = (sheet.isOpen || !sheet.isVisible) && launching == null,
                    loading = app.id == launching,
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(GridPadding)
                .graphicsLayer { alpha = sheet.fraction }
                .background(colors.background)
                .clickable(enabled = sheet.isOpen, onClick = sheet::close)
                .padding(horizontal = GridPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.apps_title),
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = apps.size.toString(), color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
