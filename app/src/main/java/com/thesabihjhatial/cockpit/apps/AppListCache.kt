package com.thesabihjhatial.cockpit.apps

import android.content.Context

private const val PreferencesName = "app-list"
private const val Key = "apps"
private const val RecordSeparator = "\n"
private const val FieldSeparator = "\t"

internal class AppListCache(context: Context) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    fun read(): List<AppInfo> = preferences.getString(Key, null)
        ?.lineSequence()
        ?.mapNotNull { line ->
            val id = line.substringBefore(FieldSeparator, "")
            val label = line.substringAfter(FieldSeparator, "")
            if (id.isEmpty() || label.isEmpty()) null else AppInfo(id = id, label = label)
        }
        ?.toList()
        .orEmpty()

    fun write(apps: List<AppInfo>) {
        val encoded = apps.joinToString(RecordSeparator) { it.id + FieldSeparator + it.label }
        preferences.edit().putString(Key, encoded).apply()
    }
}
