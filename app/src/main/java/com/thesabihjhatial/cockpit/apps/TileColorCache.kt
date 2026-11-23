package com.thesabihjhatial.cockpit.apps

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

private const val PreferencesName = "tile-colors"
private const val EntriesKey = "entries"
private const val FieldSeparator = '\u0001'

internal fun decodeColors(entries: Set<String>): Map<String, Int> =
    entries.mapNotNull { entry ->
        val key = entry.substringBefore(FieldSeparator, "")
        val color = entry.substringAfter(FieldSeparator, "").toIntOrNull()
        if (key.isNotEmpty() && color != null) key to color else null
    }.toMap()

internal fun encodeColors(colors: Map<String, Int>): Set<String> =
    colors.map { (key, color) -> "$key$FieldSeparator$color" }.toSet()

/**
 * Remembers each tile colour across restarts.
 *
 * Extracting a palette costs milliseconds per app, and the launcher would
 * otherwise pay that for every installed app on every cold start. A tile colour
 * only changes when the app itself changes, so entries are keyed by id and by the
 * apk's modification time, and pruned when either goes away.
 */
internal class TileColorCache(context: Context) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val colors = ConcurrentHashMap(decodeColors(preferences.getStringSet(EntriesKey, null).orEmpty()))

    fun get(key: String): Int? = colors[key]

    fun put(key: String, color: Int) {
        colors[key] = color
    }

    /** Forgets apps and app versions that are no longer installed, and persists what is left. */
    fun retainOnly(keys: Set<String>) {
        colors.keys.retainAll(keys)
        preferences.edit().putStringSet(EntriesKey, encodeColors(colors)).apply()
    }
}
