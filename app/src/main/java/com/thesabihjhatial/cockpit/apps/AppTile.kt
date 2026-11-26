package com.thesabihjhatial.cockpit.apps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.ui.CircularProgressIndicator
import com.thesabihjhatial.cockpit.ui.CockpitPalette
import com.thesabihjhatial.cockpit.ui.DropdownMenu
import com.thesabihjhatial.cockpit.ui.DropdownMenuItem
import com.thesabihjhatial.cockpit.ui.Text

internal val TileShape = RoundedCornerShape(16.dp)
private const val BadgeAlpha = 0.08f
private const val BadgeTextAlpha = 0.72f
private const val TrackAlpha = 0.12f

class TileAction(val label: String, val onSelect: () -> Unit, val enabled: Boolean = true)

@Composable
private fun Badge(text: String, modifier: Modifier = Modifier) {
    val ink = CockpitPalette.tileInk

    Box(
        modifier = modifier
            .height(22.dp)
            .clip(CircleShape)
            .background(ink.copy(alpha = BadgeAlpha))
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = ink.copy(alpha = BadgeTextAlpha), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTile(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<TileAction> = emptyList(),
    badge: String? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = CockpitPalette
    val icon = rememberAppIcon(app)
    var menuOpen by remember { mutableStateOf(false) }
    val accessibilityActions = actions
        .filter { it.enabled }
        .map { action -> CustomAccessibilityAction(action.label) { action.onSelect(); true } }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(TileShape)
                .background(colors.tileBackground)
                .combinedClickable(
                    enabled = enabled && !loading,
                    onClick = onClick,
                    onLongClick = if (actions.isEmpty()) null else ({ menuOpen = true }),
                )
                .semantics { if (accessibilityActions.isNotEmpty()) customActions = accessibilityActions },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    loading -> Box(modifier = Modifier.size(TileIconDp.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = colors.tileInk,
                            strokeWidth = 4.dp,
                            trackColor = colors.tileInk.copy(alpha = TrackAlpha),
                        )
                    }
                    icon == null -> Box(modifier = Modifier.size(TileIconDp.dp))
                    else -> Image(
                        bitmap = icon,
                        contentDescription = null,
                        modifier = Modifier.size(TileIconDp.dp),
                    )
                }
                Text(
                    text = app.label,
                    color = colors.tileInk,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.03).em,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            if (badge != null) Badge(text = badge, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label) },
                    enabled = action.enabled,
                    onClick = {
                        menuOpen = false
                        action.onSelect()
                    },
                )
            }
        }
    }
}
