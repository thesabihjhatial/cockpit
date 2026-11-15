package com.thesabihjhatial.cockpit.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings

const val BluetoothAccessId = "bluetooth"
const val MediaAccessId = "media"
const val SettingsId = "settings"

fun Context.openNotificationAccessSettings(): Boolean = open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

fun Context.openSystemSettings(): Boolean = open(Settings.ACTION_SETTINGS)

private fun Context.open(action: String): Boolean = runCatching { startActivity(Intent(action)) }.isSuccess
