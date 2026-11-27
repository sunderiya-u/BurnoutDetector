
package com.sunderiya.burnoutdetector.processing

import org.junit.Assert.assertEquals
import org.junit.Test

class TypingTrackerTest {

    @Test
    fun initialSnapshotContainsZeros() {
        val tracker = TypingTracker()

        assertEquals(
            TypingSnapshot(0, 0, 0, 0L, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun typedCharactersAreCounted() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 1500L)

        assertEquals(2, tracker.snapshot().typedCharCount)
    }

    @Test
    fun deletionsAreCounted() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.DELETION, 1000L)
        tracker.process(TextChangeType.DELETION, 1500L)

        assertEquals(2, tracker.snapshot().deletionCount)
    }

    @Test
    fun replacementsAreCountedSeparately() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.REPLACEMENT, 1000L)

        val result = tracker.snapshot()

        assertEquals(1, result.replacementCount)
        assertEquals(0, result.typedCharCount)
        assertEquals(0, result.deletionCount)
        assertEquals(0L, result.activeTypingMs)
    }

    @Test
    fun ignoredEventsDoNotChangeCounters() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.IGNORED, 1000L)

        assertEquals(
            TypingSnapshot(0, 0, 0, 0L, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun invalidEventsAreCounted() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.INVALID, 1000L)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(0, tracker.snapshot().typedCharCount)
    }

    @Test
    fun firstTypingEventHasZeroDuration() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)

        assertEquals(0L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun shortIntervalsAccumulateActiveDuration() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 1500L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 2000L)

        assertEquals(1000L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun exactThresholdIsIncluded() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 3000L)

        assertEquals(2000L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun intervalAboveThresholdIsExcluded() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 3001L)

        assertEquals(0L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun typingDurationContinuesAfterLongPause() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 1500L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 6000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 6500L)

        assertEquals(1000L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun deletionsContributeToActiveTypingDuration() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.DELETION, 1400L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 1800L)

        assertEquals(800L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun replacementDoesNotUpdateTypingTimestamp() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.REPLACEMENT, 2500L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 3500L)

        assertEquals(0L, tracker.snapshot().activeTypingMs)
        assertEquals(1, tracker.snapshot().replacementCount)
    }

    @Test
    fun ignoredAndInvalidEventsDoNotUpdateTypingTimestamp() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.IGNORED, 2000L)
        tracker.process(TextChangeType.INVALID, 2500L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 3500L)

        assertEquals(0L, tracker.snapshot().activeTypingMs)
        assertEquals(1, tracker.snapshot().invalidEventCount)
    }

    @Test
    fun negativeTimestampIsInvalid() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, -100L)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(0, tracker.snapshot().typedCharCount)
        assertEquals(0L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun outOfOrderTimestampDoesNotCorruptDuration() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 1500L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 1200L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 2000L)

        assertEquals(1000L, tracker.snapshot().activeTypingMs)
        assertEquals(4, tracker.snapshot().typedCharCount)
    }

    @Test
    fun identicalTimestampsAddZeroDuration() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)

        assertEquals(2, tracker.snapshot().typedCharCount)
        assertEquals(0L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun customTypingThresholdIsRespected() {
        val tracker = TypingTracker(
            maxTypingIntervalMs = 1000L
        )

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.TYPED_CHARACTER, 2500L)

        assertEquals(0L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun resetClearsAllMeasurements() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)
        tracker.process(TextChangeType.DELETION, 1500L)
        tracker.process(TextChangeType.REPLACEMENT, 1600L)
        tracker.process(TextChangeType.INVALID, 1700L)

        tracker.reset()

        assertEquals(
            TypingSnapshot(0, 0, 0, 0L, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun resetClearsPreviousTypingTimestamp() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)

        tracker.reset()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1500L)

        assertEquals(1, tracker.snapshot().typedCharCount)
        assertEquals(0L, tracker.snapshot().activeTypingMs)
    }

    @Test
    fun snapshotRemainsUnchangedAfterFurtherEvents() {
        val tracker = TypingTracker()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1000L)

        val previousSnapshot = tracker.snapshot()

        tracker.process(TextChangeType.TYPED_CHARACTER, 1500L)

        assertEquals(1, previousSnapshot.typedCharCount)
        assertEquals(2, tracker.snapshot().typedCharCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroThresholdIsRejected() {
        TypingTracker(maxTypingIntervalMs = 0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeThresholdIsRejected() {
        TypingTracker(maxTypingIntervalMs = -100L)
    }
}
