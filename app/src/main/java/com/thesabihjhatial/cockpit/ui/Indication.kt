// IndicationInstance is deprecated at ERROR level in Compose 1.7 in favour of
// IndicationNodeFactory. The replacement only helps when the tint must be computed per
// press, which is not the case here, and this keeps the class count (and dex) smaller.
@file:Suppress("DEPRECATION_ERROR")

package com.thesabihjhatial.cockpit.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope

private const val PressAlpha = 0.10f

/**
 * A flat press tint.
 *
 * The ripple was the last thing material3 was still providing, so without this every
 * tap in the launcher would be silent. A translucent rectangle is all a car screen
 * needs, and it costs nothing at idle.
 */
object PressIndication : Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        val pressed by interactionSource.collectIsPressedAsState()
        val tint = LocalCockpitColors.current.text
        return remember(pressed, tint) { PressInstance(pressed, tint) }
    }
}

private class PressInstance(private val pressed: Boolean, private val tint: Color) : IndicationInstance {
    override fun ContentDrawScope.drawIndication() {
        drawContent()
        if (pressed) drawRect(tint.copy(alpha = PressAlpha))
    }
}
