
package com.sunderiya.burnoutdetector.data.validation

import com.sunderiya.burnoutdetector.aggregation.FeatureCalculator
import com.sunderiya.burnoutdetector.aggregation.WindowSummary
import kotlin.math.abs

/**
 * Validates aggregated behavioral measurements
 * before they are persisted.
 */
object WindowValidator {

    fun validate(summary: WindowSummary) {

        val windowDuration =
            FeatureCalculator.WINDOW_DURATION_MS

        // Window metadata
        require(summary.startTime >= 0L)

        require(summary.startTime % windowDuration == 0L)

        require(summary.endTime > summary.startTime)

        require(
            summary.endTime - summary.startTime ==
                    windowDuration
        )

        require(
            summary.utcOffsetMinutes in -1080..1080
        )

        require(
            summary.monitoredMs in 1L..windowDuration
        )

        require(
            summary.isComplete ==
                    (summary.monitoredMs == windowDuration)
        )

        // Event quality counters
        require(summary.eventCount >= 0)
        require(summary.invalidEventCount >= 0)

        // Behavioral feature validation
        validateTyping(summary)
        validateScrolling(summary)

        require(summary.appSwitchCount >= 0)
    }

    private fun validateTyping(summary: WindowSummary) {

        val typed = summary.typedCharCount

        // Suppressed typing measurements must all be null.
        if (typed == null) {

            require(summary.deletionCount == null)
            require(summary.replacementCount == null)
            require(summary.activeTypingMs == null)
            require(summary.typingSpeed == null)
            require(summary.correctionRatio == null)

            return
        }

        // Unsuppressed typing must meet the threshold.
        require(
            typed >= FeatureCalculator.MIN_TYPED_CHARACTERS
        )

        val deleted = requireNotNull(summary.deletionCount)
        val replaced = requireNotNull(summary.replacementCount)
        val activeMs = requireNotNull(summary.activeTypingMs)

        require(deleted >= 0)
        require(replaced >= 0)

        require(
            activeMs in 0L..summary.monitoredMs
        )

        // Validate typing speed.
        if (activeMs == 0L) {

            require(summary.typingSpeed == null)

        } else {

            val speed = requireNotNull(summary.typingSpeed)

            require(speed.isFinite())
            require(speed >= 0.0)

            val expectedSpeed =
                typed.toDouble() * 60_000.0 / activeMs

            require(
                abs(speed - expectedSpeed) <=
                        maxOf(0.000001, expectedSpeed * 0.000000001)
            )
        }

        // Validate correction ratio.
        val ratio = requireNotNull(summary.correctionRatio)

        require(ratio.isFinite())
        require(ratio in 0.0..1.0)

        val totalActions =
            typed.toLong() + deleted.toLong()

        val expectedRatio =
            deleted.toDouble() / totalActions

        require(
            abs(ratio - expectedRatio) <= 0.000001
        )
    }

    private fun validateScrolling(summary: WindowSummary) {

        require(summary.scrollGestureCount >= 0)

        require(
            summary.scrollActiveMs in 0L..summary.monitoredMs
        )

        val distance = summary.scrollDistancePx

        if (distance != null) {
            require(distance >= 0L)
        }

        val variability = summary.scrollVariability

        if (summary.scrollGestureCount == 0) {

            require(summary.scrollActiveMs == 0L)
            require(distance == null)
        }

        if (summary.scrollGestureCount < 2) {

            require(variability == null)

        } else {

            requireNotNull(variability)

            require(variability.isFinite())
            require(variability >= 0.0)
        }
    }
}
