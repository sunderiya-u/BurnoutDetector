
package com.sunderiya.burnoutdetector.data.validation

import com.sunderiya.burnoutdetector.aggregation.WindowSummary
import org.junit.Test

class WindowValidatorTest {

    private fun validSummary() = WindowSummary(
        startTime = 300_000L,
        endTime = 600_000L,
        utcOffsetMinutes = 60,
        monitoredMs = 300_000L,
        isComplete = true,
        eventCount = 25,
        invalidEventCount = 2,
        typedCharCount = 120,
        deletionCount = 30,
        replacementCount = 4,
        activeTypingMs = 30_000L,
        typingSpeed = 240.0,
        correctionRatio = 0.2,
        scrollGestureCount = 5,
        scrollActiveMs = 4_000L,
        scrollVariability = 120.0,
        scrollDistancePx = 2500L,
        appSwitchCount = 3
    )

    private fun suppressedSummary() = validSummary().copy(
        typedCharCount = null,
        deletionCount = null,
        replacementCount = null,
        activeTypingMs = null,
        typingSpeed = null,
        correctionRatio = null
    )

    // -----------------------------------------
    // Valid summaries
    // -----------------------------------------

    @Test
    fun acceptsValidCompleteWindow() {

        WindowValidator.validate(validSummary())
    }

    @Test
    fun acceptsValidPartialWindow() {

        WindowValidator.validate(
            validSummary().copy(
                monitoredMs = 120_000L,
                isComplete = false
            )
        )
    }

    @Test
    fun acceptsFullySuppressedTyping() {

        WindowValidator.validate(suppressedSummary())
    }

    @Test
    fun acceptsExactlyTwentyTypedCharacters() {

        WindowValidator.validate(
            validSummary().copy(
                typedCharCount = 20,
                deletionCount = 5,
                activeTypingMs = 10_000L,
                typingSpeed = 120.0,
                correctionRatio = 0.2
            )
        )
    }

    @Test
    fun acceptsZeroActiveTypingTimeWithNullSpeed() {

        WindowValidator.validate(
            validSummary().copy(
                activeTypingMs = 0L,
                typingSpeed = null
            )
        )
    }

    @Test
    fun acceptsZeroActivityWindow() {

        WindowValidator.validate(
            suppressedSummary().copy(
                eventCount = 0,
                invalidEventCount = 0,
                scrollGestureCount = 0,
                scrollActiveMs = 0L,
                scrollVariability = null,
                scrollDistancePx = null,
                appSwitchCount = 0
            )
        )
    }

    // -----------------------------------------
    // Window metadata validation
    // -----------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeStartTime() {

        WindowValidator.validate(
            validSummary().copy(startTime = -1L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnalignedStartTime() {

        WindowValidator.validate(
            validSummary().copy(
                startTime = 300_001L,
                endTime = 600_001L
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncorrectWindowDuration() {

        WindowValidator.validate(
            validSummary().copy(endTime = 700_000L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEndTimeBeforeStartTime() {

        WindowValidator.validate(
            validSummary().copy(endTime = 200_000L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroMonitoringDuration() {

        WindowValidator.validate(
            validSummary().copy(
                monitoredMs = 0L,
                isComplete = false
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExcessiveMonitoringDuration() {

        WindowValidator.validate(
            validSummary().copy(monitoredMs = 300_001L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncorrectCompleteFlag() {

        WindowValidator.validate(
            validSummary().copy(
                monitoredMs = 120_000L,
                isComplete = true
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidTimezoneOffset() {

        WindowValidator.validate(
            validSummary().copy(utcOffsetMinutes = 1081)
        )
    }

    // -----------------------------------------
    // Event counters
    // -----------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeEventCount() {

        WindowValidator.validate(
            validSummary().copy(eventCount = -1)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeInvalidEventCount() {

        WindowValidator.validate(
            validSummary().copy(invalidEventCount = -1)
        )
    }

    // -----------------------------------------
    // Typing validation and privacy suppression
    // -----------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsuppressedTypingBelowThreshold() {

        WindowValidator.validate(
            validSummary().copy(typedCharCount = 19)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPartiallySuppressedTyping() {

        WindowValidator.validate(
            suppressedSummary().copy(deletionCount = 3)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingDeletionCount() {

        WindowValidator.validate(
            validSummary().copy(deletionCount = null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeDeletionCount() {

        WindowValidator.validate(
            validSummary().copy(deletionCount = -1)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExcessiveActiveTypingTime() {

        WindowValidator.validate(
            validSummary().copy(activeTypingMs = 300_001L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncorrectTypingSpeed() {

        WindowValidator.validate(
            validSummary().copy(typingSpeed = 100.0)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonFiniteTypingSpeed() {

        WindowValidator.validate(
            validSummary().copy(typingSpeed = Double.NaN)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsCorrectionRatioAboveOne() {

        WindowValidator.validate(
            validSummary().copy(correctionRatio = 1.5)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncorrectCorrectionRatio() {

        WindowValidator.validate(
            validSummary().copy(correctionRatio = 0.5)
        )
    }

    // -----------------------------------------
    // Scrolling and app switching
    // -----------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeScrollGestureCount() {

        WindowValidator.validate(
            validSummary().copy(scrollGestureCount = -1)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeScrollDistance() {

        WindowValidator.validate(
            validSummary().copy(scrollDistancePx = -1L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsVariabilityForSingleGesture() {

        WindowValidator.validate(
            validSummary().copy(
                scrollGestureCount = 1,
                scrollVariability = 100.0
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingVariabilityForMultipleGestures() {

        WindowValidator.validate(
            validSummary().copy(
                scrollGestureCount = 2,
                scrollVariability = null
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeAppSwitchCount() {

        WindowValidator.validate(
            validSummary().copy(appSwitchCount = -1)
        )
    }
}
