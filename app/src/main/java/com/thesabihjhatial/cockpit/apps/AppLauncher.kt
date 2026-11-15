package com.thesabihjhatial.cockpit.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent

fun Context.launchApp(id: String) {
    val component = ComponentName.unflattenFromString(id) ?: return
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(component)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
