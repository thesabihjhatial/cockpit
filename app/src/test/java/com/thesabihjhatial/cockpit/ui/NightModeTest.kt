package com.thesabihjhatial.cockpit.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NightModeTest {

    @Test
    fun dayRunsFromSixToSix() {
        assertFalse(isNight(6))
        assertFalse(isNight(17))
    }

    @Test
    fun nightRunsFromSixPmToSixAm() {
        assertTrue(isNight(18))
        assertTrue(isNight(0))
        assertTrue(isNight(5))
    }
}
