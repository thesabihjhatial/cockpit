package com.thesabihjhatial.cockpit.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TileColorCacheTest {

    private val componentKey = "com.example.app/com.example.app.Main:1700000000000"
    private val shortcutKey = "cn.yunovo.nxos/cn.yunovo.nxos.Music#cn.yunovo.nxos.action.radio:1"

    @Test
    fun coloursSurviveARoundTrip() {
        val original = mapOf(componentKey to 0xFFD92D20.toInt(), shortcutKey to 0xFF1A1A1C.toInt())
        assertEquals(original, decodeColors(encodeColors(original)))
    }

    @Test
    fun keysMayContainColonsSlashesAndHashes() {
        assertEquals(setOf(componentKey, shortcutKey), decodeColors(encodeColors(mapOf(componentKey to 1, shortcutKey to 2))).keys)
    }

    @Test
    fun entriesWithoutASeparatorAreIgnored() {
        assertEquals(emptyMap<String, Int>(), decodeColors(setOf("nonsense")))
    }

    @Test
    fun entriesWithANonNumericColourAreIgnored() {
        assertEquals(emptyMap<String, Int>(), decodeColors(setOf("com.example.app\u0001not-a-colour")))
    }

    @Test
    fun encodingIsEmptyWhenThereIsNothingToRemember() {
        assertTrue(encodeColors(emptyMap()).isEmpty())
    }
}
