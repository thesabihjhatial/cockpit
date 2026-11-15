package com.thesabihjhatial.cockpit.apps

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val color: Color,
    val icon: ImageBitmap,
    val label: String,
    val packageName: String,
    val tinted: Boolean,
)
