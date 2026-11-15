package com.thesabihjhatial.cockpit.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent

fun Context.launchApp(id: String): Boolean {
    val component = ComponentName.unflattenFromString(id) ?: return false
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(component)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { startActivity(intent) }.isSuccess
}
