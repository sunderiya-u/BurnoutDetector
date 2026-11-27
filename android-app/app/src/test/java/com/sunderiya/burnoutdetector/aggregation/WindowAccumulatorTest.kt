
package com.sunderiya.burnoutdetector.aggregation

import org.junit.Assert.*
import org.junit.Test

class WindowAccumulatorTest {

    private val appA = "test.app.a"
    private val appB = "test.app.b"

    @Test
    fun initialSnapshotContainsZeroMeasurements() {

        val accumulator = WindowAccumulator()
        val result = accumulator.snapshot()

        assertEquals(0, result.typing.typedCharCount)
        assertEquals(0, result.typing.deletionCount)
        assertEquals(0, result.typing.replacementCount)
        assertEquals(0L, result.typing.activeTypingMs)

        assertEquals(0, result.scrolling.scrollGestureCount)
        assertEquals(0L, result.scrolling.scrollActiveMs)
        assertNull(result.scrolling.scrollVariability)
        assertNull(result.scrolling.scrollDistancePx)

        assertEquals(0, result.appSwitching.appSwitchCount)
        assertEquals(0, result.eventCount)
        assertEquals(0, result.invalidEventCount)
    }

    @Test
    fun typedCharacterIsClassifiedAndCounted() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(1, 0, 1000L)

        assertEquals(
            1,
            accumulator.snapshot().typing.typedCharCount
        )
    }

    @Test
    fun deletionIsClassifiedAndCounted() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(0, 1, 1000L)

        assertEquals(
            1,
            accumulator.snapshot().typing.deletionCount
        )
    }

    @Test
    fun replacementIsClassifiedAndCounted() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(5, 0, 1000L)

        assertEquals(
            1,
            accumulator.snapshot().typing.replacementCount
        )
    }

    @Test
    fun equalAddedAndRemovedCountsAreIgnored() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(2, 2, 1000L)

        val typing = accumulator.snapshot().typing

        assertEquals(0, typing.typedCharCount)
        assertEquals(0, typing.deletionCount)
        assertEquals(0, typing.replacementCount)
    }

    @Test
    fun typingDurationIsAccumulated() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(1, 0, 1000L)
        accumulator.processTextChange(1, 0, 1200L)
        accumulator.processTextChange(1, 0, 1500L)

        val typing = accumulator.snapshot().typing

        assertEquals(3, typing.typedCharCount)
        assertEquals(500L, typing.activeTypingMs)
    }

    @Test
    fun typingEventsAreExcludedFromGenericEventCount() {

        val accumulator = WindowAccumulator()

        repeat(10) { index ->

            accumulator.processTextChange(
                1,
                0,
                1000L + index * 100L
            )
        }

        val result = accumulator.snapshot()

        assertEquals(10, result.typing.typedCharCount)
        assertEquals(0, result.eventCount)
    }

    @Test
    fun allTextChangeCategoriesAreExcludedFromEventCount() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(1, 0, 1000L)
        accumulator.processTextChange(0, 1, 1100L)
        accumulator.processTextChange(5, 0, 1200L)
        accumulator.processTextChange(2, 2, 1300L)
        accumulator.processTextChange(-1, 0, 1400L)

        assertEquals(0, accumulator.snapshot().eventCount)
    }

    @Test
    fun invalidTypingEventsAreExcludedFromGenericInvalidCount() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(-1, 0, 1000L)
        accumulator.processTextChange(0, -1, 1100L)

        val result = accumulator.snapshot()

        assertEquals(2, result.typing.invalidEventCount)
        assertEquals(0, result.invalidEventCount)
    }

    @Test
    fun negativeTypingTimestampDoesNotLeakIntoGenericCounters() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(1, 0, -100L)

        val result = accumulator.snapshot()

        assertEquals(1, result.typing.invalidEventCount)
        assertEquals(0, result.invalidEventCount)
        assertEquals(0, result.eventCount)
    }

    @Test
    fun scrollEventsAreCounted() {

        val accumulator = WindowAccumulator()

        accumulator.processScroll(1000L)
        accumulator.processScroll(1100L)
        accumulator.processScroll(1200L)

        val result = accumulator.snapshot()

        assertEquals(3, result.eventCount)
        assertEquals(1, result.scrolling.scrollGestureCount)
        assertEquals(200L, result.scrolling.scrollActiveMs)
    }

    @Test
    fun scrollDistanceIsPreserved() {

        val accumulator = WindowAccumulator()

        accumulator.processScroll(
            timestampMs = 1000L,
            deltaX = 10,
            deltaY = -30
        )

        accumulator.processScroll(
            timestampMs = 1100L,
            deltaY = 20
        )

        assertEquals(
            60L,
            accumulator.snapshot().scrolling.scrollDistancePx!!
        )
    }

    @Test
    fun invalidScrollEventIsNotCountedAsAcceptedEvent() {

        val accumulator = WindowAccumulator()

        accumulator.processScroll(-100L)

        val result = accumulator.snapshot()

        assertEquals(0, result.eventCount)
        assertEquals(1, result.invalidEventCount)
        assertEquals(1, result.scrolling.invalidEventCount)
    }

    @Test
    fun outOfOrderScrollEventIsRejected() {

        val accumulator = WindowAccumulator()

        accumulator.processScroll(1000L)
        accumulator.processScroll(1200L)
        accumulator.processScroll(1100L)

        val result = accumulator.snapshot()

        assertEquals(2, result.eventCount)
        assertEquals(1, result.invalidEventCount)
        assertEquals(200L, result.scrolling.scrollActiveMs)
    }

    @Test
    fun validWindowEventsAreCounted() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)
        accumulator.processWindowChange(appA, 1100L)
        accumulator.processWindowChange(appB, 1200L)

        assertEquals(3, accumulator.snapshot().eventCount)
    }

    @Test
    fun invalidWindowEventIsRejected() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(null, 1000L)

        val result = accumulator.snapshot()

        assertEquals(0, result.eventCount)
        assertEquals(1, result.invalidEventCount)
    }

    @Test
    fun applicationSwitchIsConfirmedAfterDwellTime() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)
        accumulator.processWindowChange(appB, 2000L)

        accumulator.advanceTime(3000L)

        assertEquals(
            1,
            accumulator.snapshot().appSwitching.appSwitchCount
        )
    }

    @Test
    fun timerUpdatesDoNotIncreaseEventCount() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)
        accumulator.processWindowChange(appB, 2000L)

        accumulator.advanceTime(3000L)
        accumulator.advanceTime(4000L)

        val result = accumulator.snapshot()

        assertEquals(2, result.eventCount)
        assertEquals(1, result.appSwitching.appSwitchCount)
    }

    @Test
    fun applicationChangeBreaksTypingTiming() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.processTextChange(1, 0, 1100L)
        accumulator.processTextChange(1, 0, 1200L)

        accumulator.processWindowChange(appB, 1300L)

        accumulator.processTextChange(1, 0, 1400L)
        accumulator.processTextChange(1, 0, 1500L)

        val typing = accumulator.snapshot().typing

        assertEquals(4, typing.typedCharCount)
        assertEquals(200L, typing.activeTypingMs)
    }

    @Test
    fun sameApplicationWindowEventPreservesTypingTiming() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.processTextChange(1, 0, 1100L)

        accumulator.processWindowChange(appA, 1200L)

        accumulator.processTextChange(1, 0, 1300L)

        assertEquals(
            200L,
            accumulator.snapshot().typing.activeTypingMs
        )
    }

    @Test
    fun applicationChangeSeparatesScrollGestures() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.processScroll(1100L)
        accumulator.processScroll(1200L)

        accumulator.processWindowChange(appB, 1250L)

        accumulator.processScroll(1300L)
        accumulator.processScroll(1400L)

        val scrolling = accumulator.snapshot().scrolling

        assertEquals(2, scrolling.scrollGestureCount)
        assertEquals(200L, scrolling.scrollActiveMs)
    }

    @Test
    fun sameApplicationWindowEventPreservesScrollGesture() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.processScroll(1100L)

        accumulator.processWindowChange(appA, 1150L)

        accumulator.processScroll(1200L)

        val scrolling = accumulator.snapshot().scrolling

        assertEquals(1, scrolling.scrollGestureCount)
        assertEquals(100L, scrolling.scrollActiveMs)
    }

    @Test
    fun interruptContextBreaksTypingTiming() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(1, 0, 1000L)
        accumulator.processTextChange(1, 0, 1100L)

        accumulator.interruptContext()

        accumulator.processTextChange(1, 0, 1200L)
        accumulator.processTextChange(1, 0, 1300L)

        val typing = accumulator.snapshot().typing

        assertEquals(4, typing.typedCharCount)
        assertEquals(200L, typing.activeTypingMs)
    }

    @Test
    fun interruptContextSeparatesScrollGestures() {

        val accumulator = WindowAccumulator()

        accumulator.processScroll(1000L)
        accumulator.processScroll(1100L)

        accumulator.interruptContext()

        accumulator.processScroll(1200L)
        accumulator.processScroll(1300L)

        val scrolling = accumulator.snapshot().scrolling

        assertEquals(2, scrolling.scrollGestureCount)
        assertEquals(200L, scrolling.scrollActiveMs)
    }

    @Test
    fun interruptContextCancelsPendingApplicationSwitch() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)
        accumulator.processWindowChange(appB, 2000L)

        accumulator.interruptContext()

        accumulator.advanceTime(4000L)

        assertEquals(
            0,
            accumulator.snapshot().appSwitching.appSwitchCount
        )
    }

    @Test
    fun interruptContextPreservesExistingMeasurements() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.processTextChange(1, 0, 1100L)
        accumulator.processTextChange(1, 0, 1200L)

        accumulator.processScroll(1300L, deltaY = 50)

        val before = accumulator.snapshot()

        accumulator.interruptContext()

        val after = accumulator.snapshot()

        assertEquals(before, after)
    }

    @Test
    fun genericInvalidCountCombinesNonTypingInvalidEvents() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(-1, 0, 1000L)
        accumulator.processScroll(-100L)
        accumulator.processWindowChange(null, 1100L)

        val result = accumulator.snapshot()

        assertEquals(1, result.typing.invalidEventCount)
        assertEquals(1, result.scrolling.invalidEventCount)
        assertEquals(1, result.appSwitching.invalidEventCount)

        assertEquals(2, result.invalidEventCount)
    }

    @Test
    fun windowResetClearsMeasurements() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.processTextChange(1, 0, 1100L)
        accumulator.processScroll(1200L, deltaY = 50)

        accumulator.processWindowChange(appB, 2000L)
        accumulator.advanceTime(3000L)

        accumulator.resetWindow()

        val result = accumulator.snapshot()

        assertEquals(0, result.typing.typedCharCount)
        assertEquals(0L, result.typing.activeTypingMs)
        assertEquals(0, result.scrolling.scrollGestureCount)
        assertNull(result.scrolling.scrollDistancePx)
        assertEquals(0, result.appSwitching.appSwitchCount)
        assertEquals(0, result.eventCount)
        assertEquals(0, result.invalidEventCount)
    }

    @Test
    fun windowResetPreventsCrossWindowTypingDuration() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(1, 0, 299900L)

        accumulator.resetWindow()

        accumulator.processTextChange(1, 0, 300100L)

        val typing = accumulator.snapshot().typing

        assertEquals(1, typing.typedCharCount)
        assertEquals(0L, typing.activeTypingMs)
    }

    @Test
    fun windowResetPreventsCrossWindowScrollDuration() {

        val accumulator = WindowAccumulator()

        accumulator.processScroll(299900L)

        accumulator.resetWindow()

        accumulator.processScroll(300100L)

        val scrolling = accumulator.snapshot().scrolling

        assertEquals(1, scrolling.scrollGestureCount)
        assertEquals(0L, scrolling.scrollActiveMs)
    }

    @Test
    fun windowResetPreservesConfirmedApplication() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.resetWindow()

        accumulator.processWindowChange(appB, 2000L)
        accumulator.advanceTime(3000L)

        assertEquals(
            1,
            accumulator.snapshot().appSwitching.appSwitchCount
        )
    }

    @Test
    fun pendingSwitchSurvivesWindowReset() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 298000L)
        accumulator.processWindowChange(appB, 299500L)

        accumulator.advanceTime(299999L)

        accumulator.resetWindow()

        accumulator.advanceTime(300500L)

        val result = accumulator.snapshot()

        assertEquals(1, result.appSwitching.appSwitchCount)
        assertEquals(0, result.eventCount)
    }

    @Test
    fun fullResetClearsAllMeasurements() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)
        accumulator.processTextChange(1, 0, 1100L)
        accumulator.processScroll(1200L, deltaY = 50)

        accumulator.processWindowChange(appB, 2000L)
        accumulator.advanceTime(3000L)

        accumulator.resetAll()

        assertEquals(
            WindowAccumulator().snapshot(),
            accumulator.snapshot()
        )
    }

    @Test
    fun fullResetClearsApplicationContinuity() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        accumulator.resetAll()

        accumulator.processWindowChange(appB, 2000L)
        accumulator.advanceTime(4000L)

        assertEquals(
            0,
            accumulator.snapshot().appSwitching.appSwitchCount
        )
    }

    @Test
    fun fullResetCancelsPendingApplicationSwitch() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)
        accumulator.processWindowChange(appB, 2000L)

        accumulator.resetAll()

        accumulator.advanceTime(4000L)

        assertEquals(
            0,
            accumulator.snapshot().appSwitching.appSwitchCount
        )
    }

    @Test
    fun snapshotsAreNotModifiedByLaterEvents() {

        val accumulator = WindowAccumulator()

        accumulator.processTextChange(1, 0, 1000L)

        val first = accumulator.snapshot()

        accumulator.processTextChange(1, 0, 1100L)

        val second = accumulator.snapshot()

        assertEquals(1, first.typing.typedCharCount)
        assertEquals(2, second.typing.typedCharCount)
    }

    @Test
    fun lowActivityTypingDoesNotLeakThroughGenericCounters() {

        val accumulator = WindowAccumulator()

        repeat(8) { index ->

            accumulator.processTextChange(
                1,
                0,
                1000L + index * 100L
            )
        }

        val summary = FeatureCalculator.calculate(
            startTime = 0L,
            endTime = 300_000L,
            utcOffsetMinutes = 60,
            monitoredMs = 300_000L,
            snapshot = accumulator.snapshot()
        )

        assertNull(summary.typedCharCount)
        assertNull(summary.deletionCount)
        assertNull(summary.replacementCount)
        assertNull(summary.activeTypingMs)
        assertNull(summary.typingSpeed)
        assertNull(summary.correctionRatio)

        assertEquals(0, summary.eventCount)
        assertEquals(0, summary.invalidEventCount)
    }

    @Test
    fun combinedMeasurementsProduceCorrectWindowSummary() {

        val accumulator = WindowAccumulator()

        accumulator.processWindowChange(appA, 1000L)

        // 20 typed characters, 1900 ms active duration.
        repeat(20) { index ->

            accumulator.processTextChange(
                1,
                0,
                2000L + index * 100L
            )
        }

        // Two deletion events.
        accumulator.processTextChange(0, 1, 4000L)
        accumulator.processTextChange(0, 1, 4100L)

        // Two scroll gestures.
        accumulator.processScroll(5000L, deltaY = 50)
        accumulator.processScroll(5100L, deltaY = 30)

        accumulator.processScroll(6000L, deltaY = -20)
        accumulator.processScroll(6300L, deltaY = 40)

        // One confirmed application switch.
        accumulator.processWindowChange(appB, 7000L)
        accumulator.advanceTime(8000L)

        val summary = FeatureCalculator.calculate(
            startTime = 0L,
            endTime = 300_000L,
            utcOffsetMinutes = 60,
            monitoredMs = 300_000L,
            snapshot = accumulator.snapshot()
        )

        assertEquals(20, summary.typedCharCount!!)
        assertEquals(2, summary.deletionCount!!)
        assertEquals(2100L, summary.activeTypingMs!!)

        assertEquals(
            20.0 * 60_000.0 / 2100.0,
            summary.typingSpeed!!,
            0.0001
        )

        assertEquals(
            2.0 / 22.0,
            summary.correctionRatio!!,
            0.0001
        )

        assertEquals(2, summary.scrollGestureCount)
        assertEquals(400L, summary.scrollActiveMs)
        assertEquals(140L, summary.scrollDistancePx!!)

        assertEquals(
            100.0,
            summary.scrollVariability!!,
            0.0001
        )

        assertEquals(1, summary.appSwitchCount)

        // Four scroll events + two window events.
        assertEquals(6, summary.eventCount)

        assertEquals(0, summary.invalidEventCount)
        assertTrue(summary.isComplete)
    }
}
