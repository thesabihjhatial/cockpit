package com.thesabihjhatial.cockpit.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.ui.CircularProgressIndicator
import com.thesabihjhatial.cockpit.ui.DropdownMenu
import com.thesabihjhatial.cockpit.ui.DropdownMenuItem
import com.thesabihjhatial.cockpit.ui.Text

private val BadgeColor = Color.White.copy(alpha = 0.22f)
private val TileShape = RoundedCornerShape(16.dp)

class TileAction(val label: String, val onSelect: () -> Unit, val enabled: Boolean = true)

@Composable
private fun Badge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(22.dp)
            .clip(CircleShape)
            .background(BadgeColor)
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
    var menuOpen by remember { mutableStateOf(false) }
    // Long press is the only way into this menu, which gloves and TalkBack cannot do,
    // so the same actions are also published as accessibility actions.
    val accessibilityActions = actions
        .filter { it.enabled }
        .map { action -> CustomAccessibilityAction(action.label) { action.onSelect(); true } }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(TileShape)
                .background(app.color)
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
                if (loading) {
                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color.White,
                            strokeWidth = 4.dp,
                            trackColor = Color.White.copy(alpha = 0.25f),
                        )
                    }
                } else {
                    Image(
                        bitmap = app.icon,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        colorFilter = if (app.tinted) ColorFilter.tint(Color.White) else null,
                    )
                }
                Text(
                    text = app.label,
                    color = Color.White,
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
