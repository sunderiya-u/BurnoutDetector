
package com.sunderiya.burnoutdetector.processing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScrollGestureTrackerTest {

    @Test
    fun initialSnapshotContainsZeros() {
        val tracker = ScrollGestureTracker()

        assertEquals(
            ScrollSnapshot(0, 0L, null, null, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun singleEventCreatesOneGesture() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)

        assertEquals(1, tracker.snapshot().scrollGestureCount)
        assertEquals(0L, tracker.snapshot().scrollActiveMs)
    }

    @Test
    fun nearbyEventsBelongToSameGesture() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1100L)
        tracker.process(1200L)

        assertEquals(1, tracker.snapshot().scrollGestureCount)
        assertEquals(200L, tracker.snapshot().scrollActiveMs)
    }

    @Test
    fun exactThresholdContinuesGesture() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1300L)

        assertEquals(1, tracker.snapshot().scrollGestureCount)
        assertEquals(300L, tracker.snapshot().scrollActiveMs)
    }

    @Test
    fun gapAboveThresholdStartsNewGesture() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1301L)

        assertEquals(2, tracker.snapshot().scrollGestureCount)
        assertEquals(0L, tracker.snapshot().scrollActiveMs)
    }

    @Test
    fun multipleGesturesAccumulateActiveDuration() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1200L)

        tracker.process(2000L)
        tracker.process(2300L)

        assertEquals(2, tracker.snapshot().scrollGestureCount)
        assertEquals(500L, tracker.snapshot().scrollActiveMs)
    }

    @Test
    fun variabilityIsNullWithoutGestures() {
        val tracker = ScrollGestureTracker()

        assertNull(tracker.snapshot().scrollVariability)
    }

    @Test
    fun variabilityIsNullForOneGesture() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1200L)

        assertNull(tracker.snapshot().scrollVariability)
    }

    @Test
    fun equalGestureDurationsHaveZeroVariability() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1200L)

        tracker.process(2000L)
        tracker.process(2200L)

        assertEquals(
            0.0,
            tracker.snapshot().scrollVariability!!,
            0.0001
        )
    }

    @Test
    fun differentGestureDurationsProduceVariability() {
        val tracker = ScrollGestureTracker()

        // Gesture duration: 100 ms
        tracker.process(1000L)
        tracker.process(1100L)

        // Gesture duration: 300 ms
        tracker.process(2000L)
        tracker.process(2300L)

        assertEquals(
            100.0,
            tracker.snapshot().scrollVariability!!,
            0.0001
        )
    }

    @Test
    fun threeGesturesProduceCorrectPopulationDeviation() {
        val tracker = ScrollGestureTracker()

        // Duration: 100 ms
        tracker.process(1000L)
        tracker.process(1100L)

        // Duration: 200 ms
        tracker.process(2000L)
        tracker.process(2200L)

        // Duration: 300 ms
        tracker.process(3000L)
        tracker.process(3300L)

        assertEquals(3, tracker.snapshot().scrollGestureCount)
        assertEquals(600L, tracker.snapshot().scrollActiveMs)

        assertEquals(
            81.649658,
            tracker.snapshot().scrollVariability!!,
            0.0001
        )
    }

    @Test
    fun positiveDistanceIsAccumulated() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L, deltaY = 50)
        tracker.process(1100L, deltaY = 30)

        assertEquals(
            80L,
            tracker.snapshot().scrollDistancePx!!
        )
    }

    @Test
    fun negativeDistanceIsConvertedToAbsoluteValue() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L, deltaY = -50)
        tracker.process(1100L, deltaY = -30)

        assertEquals(
            80L,
            tracker.snapshot().scrollDistancePx!!
        )
    }

    @Test
    fun horizontalAndVerticalDistancesAreCombined() {
        val tracker = ScrollGestureTracker()

        tracker.process(
            timestampMs = 1000L,
            deltaX = 10,
            deltaY = -20
        )

        assertEquals(
            30L,
            tracker.snapshot().scrollDistancePx!!
        )
    }

    @Test
    fun unavailableDistanceRemainsNull() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1100L)

        assertNull(tracker.snapshot().scrollDistancePx)
    }

    @Test
    fun zeroDistanceIsAValidMeasurement() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L, deltaY = 0)

        assertEquals(
            0L,
            tracker.snapshot().scrollDistancePx!!
        )
    }

    @Test
    fun missingDistanceDoesNotErasePreviousMeasurement() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L, deltaY = 50)
        tracker.process(1100L)

        assertEquals(
            50L,
            tracker.snapshot().scrollDistancePx!!
        )
    }

    @Test
    fun negativeTimestampIsRejected() {
        val tracker = ScrollGestureTracker()

        tracker.process(-100L, deltaY = 50)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(0, tracker.snapshot().scrollGestureCount)
        assertNull(tracker.snapshot().scrollDistancePx)
    }

    @Test
    fun outOfOrderTimestampIsRejected() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L, deltaY = 20)
        tracker.process(1200L, deltaY = 30)

        tracker.process(1100L, deltaY = 100)

        val result = tracker.snapshot()

        assertEquals(1, result.invalidEventCount)
        assertEquals(1, result.scrollGestureCount)
        assertEquals(200L, result.scrollActiveMs)
        assertEquals(50L, result.scrollDistancePx!!)
    }

    @Test
    fun identicalTimestampsRemainInSameGesture() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1000L)

        assertEquals(1, tracker.snapshot().scrollGestureCount)
        assertEquals(0L, tracker.snapshot().scrollActiveMs)
    }

    @Test
    fun repeatedSnapshotsDoNotDuplicateGestures() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1200L)

        val first = tracker.snapshot()
        val second = tracker.snapshot()

        assertEquals(first, second)
        assertEquals(1, second.scrollGestureCount)
    }

    @Test
    fun finishingGesturePreservesMeasurements() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1200L)

        val before = tracker.snapshot()

        tracker.finishGesture()

        assertEquals(before, tracker.snapshot())
    }

    @Test
    fun finishingGestureTwiceDoesNotDuplicateIt() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1200L)

        tracker.finishGesture()
        tracker.finishGesture()

        assertEquals(1, tracker.snapshot().scrollGestureCount)
        assertEquals(200L, tracker.snapshot().scrollActiveMs)
    }

    @Test
    fun explicitFinishSeparatesNearbyGestures() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.finishGesture()

        tracker.process(1100L)

        assertEquals(2, tracker.snapshot().scrollGestureCount)
    }

    @Test
    fun finishingGestureDoesNotAllowOlderTimestamps() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L)
        tracker.process(1200L)

        tracker.finishGesture()
        tracker.process(1100L)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(1, tracker.snapshot().scrollGestureCount)
    }

    @Test
    fun customGestureThresholdIsRespected() {
        val tracker = ScrollGestureTracker(
            gestureGapMs = 100L
        )

        tracker.process(1000L)
        tracker.process(1150L)

        assertEquals(2, tracker.snapshot().scrollGestureCount)
    }

    @Test
    fun resetClearsAllMeasurements() {
        val tracker = ScrollGestureTracker()

        tracker.process(1000L, deltaY = 50)
        tracker.process(1200L, deltaY = -30)
        tracker.process(2000L, deltaY = 20)
        tracker.process(-100L)

        tracker.reset()

        assertEquals(
            ScrollSnapshot(0, 0L, null, null, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun resetClearsPreviousTimestamp() {
        val tracker = ScrollGestureTracker()

        tracker.process(5000L)
        tracker.reset()

        tracker.process(1000L)

        assertEquals(1, tracker.snapshot().scrollGestureCount)
        assertEquals(0, tracker.snapshot().invalidEventCount)
        assertEquals(0L, tracker.snapshot().scrollActiveMs)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroThresholdIsRejected() {
        ScrollGestureTracker(gestureGapMs = 0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeThresholdIsRejected() {
        ScrollGestureTracker(gestureGapMs = -100L)
    }
}
