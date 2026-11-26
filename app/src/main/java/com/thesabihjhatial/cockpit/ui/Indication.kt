@file:Suppress("DEPRECATION_ERROR", "OVERRIDE_DEPRECATION")

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

object PressIndication : Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        val pressed by interactionSource.collectIsPressedAsState()
        val tint = CockpitPalette.text
        return remember(pressed, tint) { PressInstance(pressed, tint) }
    }
}

private class PressInstance(private val pressed: Boolean, private val tint: Color) : IndicationInstance {
    override fun ContentDrawScope.drawIndication() {
        drawContent()
        if (pressed) drawRect(tint.copy(alpha = PressAlpha))
    }
}
