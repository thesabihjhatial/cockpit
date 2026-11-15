package com.thesabihjhatial.cockpit.pins

import org.junit.Assert.assertEquals
import org.junit.Test

class PinnedStoreTest {

    @Test
    fun dockingAnAlreadyDockedAppChangesNothing() {
        val slots = listOf("a", null, null, null)
        assertEquals(slots, slots.withFirstFree("a"))
    }

    @Test
    fun dockingFillsTheFirstFreeSlot() {
        val slots = listOf("a", null, "c", null).withFirstFree("d")
        assertEquals(listOf("a", "d", "c", null), slots)
    }

    @Test
    fun dockingWhenFullChangesNothing() {
        val slots = listOf("a", "b", "c", "d")
        assertEquals(slots, slots.withFirstFree("e"))
    }

    @Test
    fun pinningAnAlreadyPinnedAppMovesIt() {
        val slots = listOf("a", "b", null).withPinned("a", 2)
        assertEquals(listOf(null, "b", "a"), slots)
    }

    @Test
    fun pinningIntoAnOccupiedSlotReplacesIt() {
        val slots = listOf("a", "b", null).withPinned("c", 0)
        assertEquals(listOf("c", "b", null), slots)
    }

    @Test
    fun pinningIntoItsOwnSlotKeepsIt() {
        val slots = listOf("a", null, null).withPinned("a", 0)
        assertEquals(listOf("a", null, null), slots)
    }

    @Test
    fun undockingClearsByPackage() {
        val slots = listOf("a", "b", "c", "d").withoutPackage("c")
        assertEquals(listOf("a", "b", null, "d"), slots)
    }

    @Test
    fun unpinningClearsOnlyThatSlot() {
        val slots = listOf("a", "b", "c").withoutSlot(1)
        assertEquals(listOf("a", null, "c"), slots)
    }
}
