package com.thesabihjhatial.cockpit.header

import com.thesabihjhatial.cockpit.ui.CockpitPalette
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeTextTest {

    @Test
    fun dateShowsWeekdayThenDayAndMonthWithoutPadding() {
        assertEquals("Thursday, 5 November", LocalDateTime.of(2026, 11, 5, 9, 0).dateText(CockpitPalette).text)
    }

    @Test
    fun midnightShowsTwelve() {
        assertEquals("12:05", LocalDateTime.of(2026, 11, 15, 0, 5).timeText())
    }

    @Test
    fun noonShowsTwelve() {
        assertEquals("12:00", LocalDateTime.of(2026, 11, 15, 12, 0).timeText())
    }

    @Test
    fun singleDigitHourHasNoPadding() {
        assertEquals("9:07", LocalDateTime.of(2026, 11, 15, 9, 7).timeText())
    }

    @Test
    fun afternoonUsesTwelveHourClock() {
        assertEquals("6:38", LocalDateTime.of(2026, 11, 15, 18, 38).timeText())
    }
}
