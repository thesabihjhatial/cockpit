package com.thesabihjhatial.cockpit.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings

fun Context.openSystemSettings() {
    runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) }
}
