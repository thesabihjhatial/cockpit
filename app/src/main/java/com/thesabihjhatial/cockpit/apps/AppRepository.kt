package com.thesabihjhatial.cockpit.apps

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val packageManager = context.packageManager
    val launchers = launcherActivities(context).map {
        AppInfo(id = it.componentName.flattenToString(), label = it.loadLabel(packageManager).toString())
    }
    val shortcuts = modeShortcuts(context).map {
        AppInfo(id = shortcutId(it.info.componentName, it.action), label = context.getString(it.label))
    }
    (launchers + shortcuts).sortedBy { it.label.lowercase() }
}
