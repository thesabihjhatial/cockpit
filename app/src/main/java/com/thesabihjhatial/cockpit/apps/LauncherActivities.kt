package com.thesabihjhatial.cockpit.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo

@Suppress("DEPRECATION")
fun launcherActivities(context: Context): List<ResolveInfo> {
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return context.packageManager.queryIntentActivities(launcherIntent, 0)
        .filter { it.activityInfo.packageName != context.packageName }
}
