package com.thesabihjhatial.cockpit.pins

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val PinSlots = 4

private const val PreferencesName = "pins"
private const val Separator = "\n"
private const val SlotsKey = "slots"

internal fun List<String?>.withFirstFree(packageName: String): List<String?> {
    val slot = indexOf(null)
    return if (slot < 0 || packageName in this) this else withPinned(packageName, slot)
}

internal fun List<String?>.withoutPackage(packageName: String): List<String?> =
    map { pinned -> pinned.takeIf { it != packageName } }

internal fun List<String?>.withoutSlot(slot: Int): List<String?> =
    mapIndexed { index, pinned -> if (index == slot) null else pinned }

internal fun List<String?>.withPinned(packageName: String, slot: Int): List<String?> =
    mapIndexed { index, pinned -> if (index == slot) packageName else pinned.takeIf { it != packageName } }

class PinnedStore(context: Context) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val state = MutableStateFlow(read())

    val slots: StateFlow<List<String?>> = state.asStateFlow()

    fun dock(packageName: String) = write(state.value.withFirstFree(packageName))

    fun pin(packageName: String, slot: Int) = write(state.value.withPinned(packageName, slot))

    private fun read(): List<String?> {
        val saved = preferences.getString(SlotsKey, null)?.split(Separator).orEmpty()
        return List(PinSlots) { saved.getOrNull(it)?.takeIf(String::isNotEmpty) }
    }

    fun undock(packageName: String) = write(state.value.withoutPackage(packageName))

    fun unpin(slot: Int) = write(state.value.withoutSlot(slot))

    private fun write(slots: List<String?>) {
        state.value = slots
        preferences.edit().putString(SlotsKey, slots.joinToString(Separator) { it.orEmpty() }).apply()
    }
}
