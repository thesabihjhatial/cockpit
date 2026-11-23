package com.thesabihjhatial.cockpit.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.apps.AppInfo
import com.thesabihjhatial.cockpit.apps.AppTile
import com.thesabihjhatial.cockpit.apps.TileAction
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.CockpitIcons
import com.thesabihjhatial.cockpit.ui.Icon
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import com.thesabihjhatial.cockpit.ui.Text

private val SlotShape = RoundedCornerShape(16.dp)

@Composable
private fun EmptySlot(onClick: () -> Unit) {
    val colors = LocalCockpitColors.current

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(SlotShape)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.raised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = CockpitIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = colors.muted,
            )
        }
        Text(
            text = stringResource(R.string.dock_add),
            color = colors.muted,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun PinnedRow(
    launching: String?,
    pinned: List<AppInfo?>,
    onAdd: (Int) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onUnpin: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val removeLabel = stringResource(R.string.dock_remove)

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        pinned.forEachIndexed { slot, app ->
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (app == null) {
                    EmptySlot(onClick = { onAdd(slot) })
                } else {
                    AppTile(
                        app = app,
                        onClick = { onLaunch(app) },
                        modifier = Modifier.fillMaxSize(),
                        actions = listOf(TileAction(removeLabel, onSelect = { onUnpin(slot) })),
                        enabled = launching == null,
                        loading = app.id == launching,
                    )
                }
            }
        }
    }
}
