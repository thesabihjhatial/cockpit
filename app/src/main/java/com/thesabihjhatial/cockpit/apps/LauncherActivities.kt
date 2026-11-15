package com.thesabihjhatial.cockpit.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo

private val Categories = listOf(Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_INFO)

val ResolveInfo.componentName: ComponentName
    get() = ComponentName(activityInfo.packageName, activityInfo.name)

@Suppress("DEPRECATION")
fun launcherActivities(context: Context): List<ResolveInfo> =
    Categories
        .flatMap { category ->
            context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)
        }
        .filter { it.activityInfo.packageName != context.packageName }
        .distinctBy { it.componentName }
