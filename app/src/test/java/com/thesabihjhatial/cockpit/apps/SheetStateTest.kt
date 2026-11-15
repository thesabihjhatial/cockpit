package com.thesabihjhatial.cockpit.apps

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val FrameNanos = 16_000_000L

private class StepClock : MonotonicFrameClock {
    private var now = 0L

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
        now += FrameNanos
        return onFrame(now)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SheetStateTest {
    private var closed = false

    private fun sheet(scope: TestScope): SheetState =
        SheetState(CoroutineScope(scope.coroutineContext + StepClock())) { closed = true }.apply {
            flingThreshold = 500f
            travel = 1000f
        }

    @Test
    fun dragClampsAtFullyOpen() = runTest {
        val sheet = sheet(this)
        val consumed = sheet.dragBy(-3000f)
        assertEquals(1f, sheet.fraction, 0f)
        assertEquals(-1000f, consumed, 0f)
        assertTrue(sheet.isOpen)
    }

    @Test
    fun dragReportsWhatItConsumed() = runTest {
        val sheet = sheet(this)
        val consumed = sheet.dragBy(-250f)
        assertEquals(0.25f, sheet.fraction, 0.0001f)
        assertEquals(-250f, consumed, 0.0001f)
        assertTrue(sheet.isMoving)
    }

    @Test
    fun fastDownwardFlingClosesAndReportsIt() = runTest {
        val sheet = sheet(this)
        sheet.dragBy(-800f)
        sheet.settle(900f)
        advanceUntilIdle()
        assertEquals(0f, sheet.fraction, 0f)
        assertTrue(closed)
    }

    @Test
    fun fastUpwardFlingOpens() = runTest {
        val sheet = sheet(this)
        sheet.dragBy(-100f)
        sheet.settle(-900f)
        advanceUntilIdle()
        assertTrue(sheet.isOpen)
        assertFalse(closed)
    }

    @Test
    fun slowReleaseBeforeHalfCloses() = runTest {
        val sheet = sheet(this)
        sheet.dragBy(-400f)
        sheet.settle(0f)
        advanceUntilIdle()
        assertEquals(0f, sheet.fraction, 0f)
        assertTrue(closed)
    }

    @Test
    fun slowReleaseBeyondHalfOpens() = runTest {
        val sheet = sheet(this)
        sheet.dragBy(-600f)
        sheet.settle(0f)
        advanceUntilIdle()
        assertTrue(sheet.isOpen)
    }
}
