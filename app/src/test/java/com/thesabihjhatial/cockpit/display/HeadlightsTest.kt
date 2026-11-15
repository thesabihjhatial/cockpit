package com.thesabihjhatial.cockpit.display

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadlightsTest {

    @Test
    fun dimBrightnessCountsAsHeadlightsOn() {
        assertTrue(isDim(40))
        assertTrue(isDim(127))
    }

    @Test
    fun brightScreenCountsAsHeadlightsOff() {
        assertFalse(isDim(128))
        assertFalse(isDim(255))
    }
}
