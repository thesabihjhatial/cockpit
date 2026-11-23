package com.thesabihjhatial.cockpit.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent

fun Context.launchApp(id: String): Boolean {
    val component = ComponentName.unflattenFromString(id.substringBefore(ActionSeparator)) ?: return false
    val action = id.substringAfter(ActionSeparator, "")
    val intent = (if (action.isEmpty()) Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER) else Intent(action))
        .setComponent(component)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { startActivity(intent) }.isSuccess
}
