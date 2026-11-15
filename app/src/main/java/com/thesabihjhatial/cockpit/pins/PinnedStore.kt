package com.thesabihjhatial.cockpit.pins

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val PinSlots = 4

private const val PreferencesName = "pins"
private const val Separator = "\n"
private const val SlotsKey = "slots"

internal fun List<String?>.withFirstFree(id: String): List<String?> {
    val slot = indexOf(null)
    return if (slot < 0 || id in this) this else withPinned(id, slot)
}

internal fun List<String?>.withoutId(id: String): List<String?> =
    map { pinned -> pinned.takeIf { it != id } }

internal fun List<String?>.withoutSlot(slot: Int): List<String?> =
    mapIndexed { index, pinned -> if (index == slot) null else pinned }

internal fun List<String?>.withPinned(id: String, slot: Int): List<String?> =
    mapIndexed { index, pinned -> if (index == slot) id else pinned.takeIf { it != id } }

class PinnedStore(context: Context) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val state = MutableStateFlow(read())

    val slots: StateFlow<List<String?>> = state.asStateFlow()

    fun dock(id: String) = write(state.value.withFirstFree(id))

    fun pin(id: String, slot: Int) = write(state.value.withPinned(id, slot))

    private fun read(): List<String?> {
        val saved = preferences.getString(SlotsKey, null)?.split(Separator).orEmpty()
        return List(PinSlots) { saved.getOrNull(it)?.takeIf(String::isNotEmpty) }
    }

    fun undock(id: String) = write(state.value.withoutId(id))

    fun unpin(slot: Int) = write(state.value.withoutSlot(slot))

    private fun write(slots: List<String?>) {
        state.value = slots
        preferences.edit().putString(SlotsKey, slots.joinToString(Separator) { it.orEmpty() }).apply()
    }
}
