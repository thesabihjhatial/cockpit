package com.thesabihjhatial.cockpit.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.Modifier

/**
 * A tinted vector icon, with material3's parameter list so call sites are unchanged.
 */
@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalCockpitColors.current.text,
) {
    Image(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint),
    )
}
