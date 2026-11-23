package com.thesabihjhatial.cockpit.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class IconGlyphTest {

    @Test
    fun emptyLayerHasNoCoverage() {
        assertEquals(0f, glyphCoverage(IntArray(0)), 0f)
    }

    @Test
    fun fullyOpaqueLayerIsFullyCovered() {
        assertEquals(1f, glyphCoverage(IntArray(4) { 0xFF112233.toInt() }), 0f)
    }

    @Test
    fun fullyTransparentLayerHasNoCoverage() {
        assertEquals(0f, glyphCoverage(IntArray(4)), 0f)
    }

    @Test
    fun halfOpaqueLayerIsHalfCovered() {
        assertEquals(0.5f, glyphCoverage(intArrayOf(0xFF000000.toInt(), 0, 0xFFFFFFFF.toInt(), 0x10FFFFFF)), 0f)
    }
}
