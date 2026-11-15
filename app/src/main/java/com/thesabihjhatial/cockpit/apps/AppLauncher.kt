package com.thesabihjhatial.cockpit.apps

import android.content.Context

fun Context.launchApp(packageName: String) {
    packageManager.getLaunchIntentForPackage(packageName)?.let { runCatching { startActivity(it) } }
}
