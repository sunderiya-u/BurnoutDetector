
package com.sunderiya.burnoutdetector.aggregation

import com.sunderiya.burnoutdetector.processing.AppSwitchSnapshot
import com.sunderiya.burnoutdetector.processing.ScrollSnapshot
import com.sunderiya.burnoutdetector.processing.TypingSnapshot
import org.junit.Assert.*
import org.junit.Test

class FeatureCalculatorTest {

    // Helper function for creating synthetic measurements.
    private fun createSnapshot(
        typed: Int = 120,
        deleted: Int = 15,
        replaced: Int = 3,
        activeMs: Long = 30_000L,
        gestures: Int = 3,
        scrollMs: Long = 600L,
        variability: Double? = 81.649658,
        distance: Long? = 500L,
        switches: Int = 2,
        events: Int = 10,
        invalidEvents: Int = 0
    ): AccumulatorSnapshot {

        return AccumulatorSnapshot(
            typing = TypingSnapshot(
                typedCharCount = typed,
                deletionCount = deleted,
                replacementCount = replaced,
                activeTypingMs = activeMs,
                invalidEventCount = 0
            ),
            scrolling = ScrollSnapshot(
                scrollGestureCount = gestures,
                scrollActiveMs = scrollMs,
                scrollVariability = variability,
                scrollDistancePx = distance,
                invalidEventCount = 0
            ),
            appSwitching = AppSwitchSnapshot(
                appSwitchCount = switches,
                invalidEventCount = 0
            ),
            eventCount = events,
            invalidEventCount = invalidEvents
        )
    }

    // Helper function for calculating a standard window.
    private fun calculate(
        snapshot: AccumulatorSnapshot = createSnapshot(),
        startTime: Long = 0L,
        endTime: Long = 300_000L,
        monitoredMs: Long = 300_000L,
        offset: Int = 60
    ): WindowSummary {

        return FeatureCalculator.calculate(
            startTime = startTime,
            endTime = endTime,
            utcOffsetMinutes = offset,
            monitoredMs = monitoredMs,
            snapshot = snapshot
        )
    }

    @Test
    fun typingSpeedIsCalculatedCorrectly() {

        val result = calculate()

        assertEquals(
            240.0,
            result.typingSpeed!!,
            0.0001
        )
    }

    @Test
    fun correctionRatioIsCalculatedCorrectly() {

        val result = calculate()

        assertEquals(
            15.0 / 135.0,
            result.correctionRatio!!,
            0.0001
        )
    }

    @Test
    fun replacementCountDoesNotAffectCorrectionRatio() {

        val result = calculate(
            createSnapshot(replaced = 100)
        )

        assertEquals(
            15.0 / 135.0,
            result.correctionRatio!!,
            0.0001
        )
    }

    @Test
    fun noDeletionsProducesZeroCorrectionRatio() {

        val result = calculate(
            createSnapshot(deleted = 0)
        )

        assertEquals(
            0.0,
            result.correctionRatio!!,
            0.0001
        )
    }

    @Test
    fun zeroActiveDurationProducesNullTypingSpeed() {

        val result = calculate(
            createSnapshot(activeMs = 0L)
        )

        assertNull(result.typingSpeed)
        assertEquals(120, result.typedCharCount!!)
        assertEquals(0L, result.activeTypingMs!!)
    }

    @Test
    fun sufficientTypingPreservesAllRawTypingMeasurements() {

        val result = calculate()

        assertEquals(120, result.typedCharCount!!)
        assertEquals(15, result.deletionCount!!)
        assertEquals(3, result.replacementCount!!)
        assertEquals(30_000L, result.activeTypingMs!!)
    }

    @Test
    fun nineteenCharactersSuppressAllTypingFields() {

        val result = calculate(
            createSnapshot(typed = 19)
        )

        assertNull(result.typedCharCount)
        assertNull(result.deletionCount)
        assertNull(result.replacementCount)
        assertNull(result.activeTypingMs)
        assertNull(result.typingSpeed)
        assertNull(result.correctionRatio)
    }

    @Test
    fun twentyCharactersMeetPrivacyThreshold() {

        val result = calculate(
            createSnapshot(
                typed = 20,
                deleted = 5,
                activeMs = 10_000L
            )
        )

        assertEquals(20, result.typedCharCount!!)
        assertEquals(5, result.deletionCount!!)

        assertEquals(
            120.0,
            result.typingSpeed!!,
            0.0001
        )

        assertEquals(
            0.2,
            result.correctionRatio!!,
            0.0001
        )
    }

    @Test
    fun zeroTypedCharactersSuppressAllTypingFields() {

        val result = calculate(
            createSnapshot(
                typed = 0,
                deleted = 0,
                replaced = 0,
                activeMs = 0L
            )
        )

        assertNull(result.typedCharCount)
        assertNull(result.deletionCount)
        assertNull(result.replacementCount)
        assertNull(result.activeTypingMs)
        assertNull(result.typingSpeed)
        assertNull(result.correctionRatio)
    }

    @Test
    fun manyDeletionsDoNotBypassPrivacyThreshold() {

        val result = calculate(
            createSnapshot(
                typed = 5,
                deleted = 100
            )
        )

        assertNull(result.typedCharCount)
        assertNull(result.deletionCount)
        assertNull(result.correctionRatio)
    }

    @Test
    fun typingSuppressionDoesNotRemoveScrollingFeatures() {

        val result = calculate(
            createSnapshot(typed = 10)
        )

        assertNull(result.typedCharCount)

        assertEquals(3, result.scrollGestureCount)
        assertEquals(600L, result.scrollActiveMs)
        assertEquals(500L, result.scrollDistancePx!!)

        assertEquals(
            81.649658,
            result.scrollVariability!!,
            0.0001
        )
    }

    @Test
    fun typingSuppressionDoesNotRemoveAppSwitchCount() {

        val result = calculate(
            createSnapshot(
                typed = 10,
                switches = 4
            )
        )

        assertNull(result.typedCharCount)
        assertEquals(4, result.appSwitchCount)
    }

    @Test
    fun scrollingMeasurementsArePreserved() {

        val result = calculate()

        assertEquals(3, result.scrollGestureCount)
        assertEquals(600L, result.scrollActiveMs)
        assertEquals(500L, result.scrollDistancePx!!)

        assertEquals(
            81.649658,
            result.scrollVariability!!,
            0.0001
        )
    }

    @Test
    fun unavailableScrollDistanceRemainsNull() {

        val result = calculate(
            createSnapshot(distance = null)
        )

        assertNull(result.scrollDistancePx)
    }

    @Test
    fun unavailableScrollVariabilityRemainsNull() {

        val result = calculate(
            createSnapshot(
                gestures = 1,
                variability = null
            )
        )

        assertNull(result.scrollVariability)
    }

    @Test
    fun zeroActivityWindowPreservesZerosAndNulls() {

        val result = calculate(
            createSnapshot(
                typed = 0,
                deleted = 0,
                replaced = 0,
                activeMs = 0L,
                gestures = 0,
                scrollMs = 0L,
                variability = null,
                distance = null,
                switches = 0,
                events = 0
            )
        )

        assertNull(result.typingSpeed)
        assertNull(result.correctionRatio)

        assertEquals(0, result.scrollGestureCount)
        assertEquals(0L, result.scrollActiveMs)
        assertNull(result.scrollVariability)
        assertNull(result.scrollDistancePx)

        assertEquals(0, result.appSwitchCount)
        assertEquals(0, result.eventCount)

        assertTrue(result.isComplete)
    }

    @Test
    fun eventQualityCountersArePreserved() {

        val result = calculate(
            createSnapshot(
                events = 25,
                invalidEvents = 3
            )
        )

        assertEquals(25, result.eventCount)
        assertEquals(3, result.invalidEventCount)
    }

    @Test
    fun fullMonitoringDurationProducesCompleteWindow() {

        val result = calculate(
            monitoredMs = 300_000L
        )

        assertTrue(result.isComplete)
        assertEquals(300_000L, result.monitoredMs)
    }

    @Test
    fun partialMonitoringDurationProducesIncompleteWindow() {

        val result = calculate(
            monitoredMs = 180_000L
        )

        assertFalse(result.isComplete)
        assertEquals(180_000L, result.monitoredMs)
    }

    @Test
    fun oneMillisecondMissingProducesIncompleteWindow() {

        val result = calculate(
            monitoredMs = 299_999L
        )

        assertFalse(result.isComplete)
    }

    @Test
    fun zeroMonitoringDurationProducesIncompleteSummary() {

        val result = calculate(
            monitoredMs = 0L
        )

        assertFalse(result.isComplete)
    }

    @Test
    fun windowMetadataIsPreserved() {

        val result = calculate(
            startTime = 600_000L,
            endTime = 900_000L,
            monitoredMs = 240_000L,
            offset = 120
        )

        assertEquals(600_000L, result.startTime)
        assertEquals(900_000L, result.endTime)
        assertEquals(240_000L, result.monitoredMs)
        assertEquals(120, result.utcOffsetMinutes)
    }

    @Test
    fun negativeUtcOffsetIsAccepted() {

        val result = calculate(offset = -300)

        assertEquals(-300, result.utcOffsetMinutes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeWindowStartIsRejected() {

        calculate(
            startTime = -300_000L,
            endTime = 0L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun unalignedWindowStartIsRejected() {

        calculate(
            startTime = 1000L,
            endTime = 301_000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun incorrectWindowLengthIsRejected() {

        calculate(endTime = 200_000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun equalStartAndEndAreRejected() {

        calculate(endTime = 0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun endBeforeStartIsRejected() {

        calculate(
            startTime = 300_000L,
            endTime = 0L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeMonitoringDurationIsRejected() {

        calculate(monitoredMs = -1L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun excessiveMonitoringDurationIsRejected() {

        calculate(monitoredMs = 300_001L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun excessivePositiveUtcOffsetIsRejected() {

        calculate(offset = 1081)
    }

    @Test(expected = IllegalArgumentException::class)
    fun excessiveNegativeUtcOffsetIsRejected() {

        calculate(offset = -1081)
    }
}
