
package com.sunderiya.burnoutdetector.processing

import kotlin.math.abs
import kotlin.math.sqrt

data class ScrollSnapshot(
    val scrollGestureCount: Int,
    val scrollActiveMs: Long,
    val scrollVariability: Double?,
    val scrollDistancePx: Long?,
    val invalidEventCount: Int
)

class ScrollGestureTracker(
    private val gestureGapMs: Long = 300L
) {

    init {
        require(gestureGapMs > 0)
    }

    // Aggregated gesture measurements.
    private var gestureCount = 0
    private var totalActiveMs = 0L
    private var totalDistancePx = 0L
    private var hasDistanceMeasurement = false
    private var invalidEventCount = 0

    // Welford's online variance calculation.
    private var meanDuration = 0.0
    private var m2 = 0.0

    // Temporary state for the current gesture.
    private var gestureStartMs: Long? = null
    private var lastScrollTimestamp: Long? = null

    // Timestamp validation across gesture boundaries.
    private var lastAcceptedTimestamp: Long? = null

    fun process(
        timestampMs: Long,
        deltaX: Int? = null,
        deltaY: Int? = null
    ) {

        if (timestampMs < 0) {
            invalidEventCount++
            return
        }

        val lastAccepted = lastAcceptedTimestamp

        if (lastAccepted != null &&
            timestampMs < lastAccepted
        ) {
            invalidEventCount++
            return
        }

        val previousTimestamp = lastScrollTimestamp

        if (previousTimestamp == null) {

            startGesture(timestampMs)

        } else {

            val interval = timestampMs - previousTimestamp

            if (interval > gestureGapMs) {
                finishGesture()
                startGesture(timestampMs)
            } else {
                lastScrollTimestamp = timestampMs
            }
        }

        lastAcceptedTimestamp = timestampMs

        accumulateDistance(deltaX, deltaY)
    }

    private fun startGesture(timestampMs: Long) {
        gestureStartMs = timestampMs
        lastScrollTimestamp = timestampMs
    }

    private fun accumulateDistance(
        deltaX: Int?,
        deltaY: Int?
    ) {

        if (deltaX != null) {
            totalDistancePx += abs(deltaX.toLong())
            hasDistanceMeasurement = true
        }

        if (deltaY != null) {
            totalDistancePx += abs(deltaY.toLong())
            hasDistanceMeasurement = true
        }
    }

    fun finishGesture() {

        val start = gestureStartMs ?: return
        val end = lastScrollTimestamp ?: return

        val duration = end - start

        gestureCount++
        totalActiveMs += duration

        // Update the running mean and variance.
        val delta = duration.toDouble() - meanDuration

        meanDuration += delta / gestureCount

        val delta2 = duration.toDouble() - meanDuration

        m2 += delta * delta2

        // Clear temporary gesture state.
        gestureStartMs = null
        lastScrollTimestamp = null
    }

    fun snapshot(): ScrollSnapshot {

        var count = gestureCount
        var activeMs = totalActiveMs
        var mean = meanDuration
        var varianceSum = m2

        // Include the current unfinished gesture
        // without modifying the tracker.
        val start = gestureStartMs
        val end = lastScrollTimestamp

        if (start != null && end != null) {

            val duration = end - start

            count++
            activeMs += duration

            val delta = duration.toDouble() - mean

            mean += delta / count

            val delta2 = duration.toDouble() - mean

            varianceSum += delta * delta2
        }

        val variability = if (count >= 2) {
            sqrt((varianceSum / count).coerceAtLeast(0.0))
        } else {
            null
        }

        return ScrollSnapshot(
            scrollGestureCount = count,
            scrollActiveMs = activeMs,
            scrollVariability = variability,
            scrollDistancePx = if (hasDistanceMeasurement) {
                totalDistancePx
            } else {
                null
            },
            invalidEventCount = invalidEventCount
        )
    }

    fun reset() {

        gestureCount = 0
        totalActiveMs = 0L
        totalDistancePx = 0L
        hasDistanceMeasurement = false
        invalidEventCount = 0

        meanDuration = 0.0
        m2 = 0.0

        gestureStartMs = null
        lastScrollTimestamp = null
        lastAcceptedTimestamp = null
    }
}
