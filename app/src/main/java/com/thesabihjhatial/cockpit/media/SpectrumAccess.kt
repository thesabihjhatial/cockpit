package com.thesabihjhatial.cockpit.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private const val PreferencesName = "media"
private const val PromptedKey = "spectrum_prompted"

private fun Context.hasRecordAudio(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

@Composable
fun rememberSpectrumAccess(expanded: Boolean): Boolean {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE) }
    var granted by remember { mutableStateOf(context.hasRecordAudio()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    LaunchedEffect(expanded) {
        if (expanded && !granted && !preferences.getBoolean(PromptedKey, false)) {
            preferences.edit().putBoolean(PromptedKey, true).apply()
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    return granted
}
