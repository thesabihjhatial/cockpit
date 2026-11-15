package com.thesabihjhatial.cockpit.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

const val DesignHeight = 720

@Composable
fun DesignScale(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = Density(density = constraints.maxHeight / DesignHeight.toFloat(), fontScale = 1f)
        CompositionLocalProvider(LocalDensity provides density, content = content)
    }
}
