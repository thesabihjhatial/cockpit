package com.thesabihjhatial.cockpit.header

import com.thesabihjhatial.cockpit.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {

    @Test
    fun afternoonRunsFromNoonToFive() {
        assertEquals(R.string.greeting_afternoon, greetingLabel(12))
        assertEquals(R.string.greeting_afternoon, greetingLabel(16))
    }

    @Test
    fun eveningRunsFromFiveToMidnight() {
        assertEquals(R.string.greeting_evening, greetingLabel(17))
        assertEquals(R.string.greeting_evening, greetingLabel(23))
    }

    @Test
    fun morningRunsFromFiveToNoon() {
        assertEquals(R.string.greeting_morning, greetingLabel(5))
        assertEquals(R.string.greeting_morning, greetingLabel(11))
    }

    @Test
    fun nightCoversMidnightToFive() {
        assertEquals(R.string.greeting_night, greetingLabel(0))
        assertEquals(R.string.greeting_night, greetingLabel(4))
    }
}
