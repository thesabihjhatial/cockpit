package com.thesabihjhatial.cockpit.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.annotation.StringRes
import com.thesabihjhatial.cockpit.R

internal const val ActionSeparator = '#'

private val Modes = listOf(
    "cn.yunovo.nxos.activity.action.bt.music" to R.string.mode_bt_music,
    "cn.yunovo.nxos.activity.action.radio" to R.string.mode_radio,
)

class ModeShortcut(val action: String, val info: ResolveInfo, @StringRes val label: Int)

fun modeShortcuts(context: Context): List<ModeShortcut> =
    Modes
        .flatMap { (action, label) ->
            context.packageManager.queryIntentActivities(Intent(action), 0).map { ModeShortcut(action, it, label) }
        }
        .distinctBy { shortcutId(it.info.componentName, it.action) }

fun shortcutId(component: ComponentName, action: String): String = component.flattenToString() + ActionSeparator + action
