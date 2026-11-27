
package com.sunderiya.burnoutdetector.aggregation

import com.sunderiya.burnoutdetector.processing.AppSwitchSnapshot
import com.sunderiya.burnoutdetector.processing.AppSwitchTracker
import com.sunderiya.burnoutdetector.processing.EventClassifier
import com.sunderiya.burnoutdetector.processing.ScrollGestureTracker
import com.sunderiya.burnoutdetector.processing.ScrollSnapshot
import com.sunderiya.burnoutdetector.processing.TypingSnapshot
import com.sunderiya.burnoutdetector.processing.TypingTracker

data class AccumulatorSnapshot(
    val typing: TypingSnapshot,
    val scrolling: ScrollSnapshot,
    val appSwitching: AppSwitchSnapshot,
    val eventCount: Int,
    val invalidEventCount: Int
)

class WindowAccumulator {

    private val typingTracker = TypingTracker()

    private val scrollTracker = ScrollGestureTracker()

    private val appSwitchTracker = AppSwitchTracker()

    // Count accepted non-typing events only.
    private var eventCount = 0

    // Temporary application context.
    // Never persisted, exported or logged.
    private var currentContextPackage: String? = null

    fun processTextChange(
        addedCount: Int,
        removedCount: Int,
        timestampMs: Long
    ) {

        val type = EventClassifier.classifyTextChange(
            addedCount = addedCount,
            removedCount = removedCount
        )

        typingTracker.process(
            type = type,
            timestampMs = timestampMs
        )
    }

    fun processScroll(
        timestampMs: Long,
        deltaX: Int? = null,
        deltaY: Int? = null
    ) {

        val invalidBefore =
            scrollTracker.snapshot().invalidEventCount

        scrollTracker.process(
            timestampMs = timestampMs,
            deltaX = deltaX,
            deltaY = deltaY
        )

        val invalidAfter =
            scrollTracker.snapshot().invalidEventCount

        if (invalidAfter == invalidBefore) {
            eventCount++
        }
    }

    fun processWindowChange(
        packageName: String?,
        timestampMs: Long
    ) {

        val invalidBefore =
            appSwitchTracker.snapshot().invalidEventCount

        appSwitchTracker.process(
            packageName = packageName,
            timestampMs = timestampMs
        )

        val invalidAfter =
            appSwitchTracker.snapshot().invalidEventCount

        if (invalidAfter != invalidBefore) {
            return
        }

        eventCount++

        if (packageName != currentContextPackage) {

            typingTracker.breakTiming()

            scrollTracker.finishGesture()

            currentContextPackage = packageName
        }
    }

    fun advanceTime(timestampMs: Long) {

        appSwitchTracker.advanceTime(timestampMs)
    }

    // Called when entering an excluded or uncertain context.
    fun interruptContext() {

        typingTracker.breakTiming()

        scrollTracker.finishGesture()

        appSwitchTracker.clearForegroundState()

        currentContextPackage = null
    }

    fun snapshot(): AccumulatorSnapshot {

        val typing = typingTracker.snapshot()

        val scrolling = scrollTracker.snapshot()

        val appSwitching = appSwitchTracker.snapshot()

        return AccumulatorSnapshot(
            typing = typing,
            scrolling = scrolling,
            appSwitching = appSwitching,
            eventCount = eventCount,

            // Exclude typing-related invalid counts
            // from the generic persisted counter.
            invalidEventCount =
                scrolling.invalidEventCount +
                        appSwitching.invalidEventCount
        )
    }

    // Start the next five-minute window while
    // preserving application-switch continuity.
    fun resetWindow() {

        typingTracker.reset()

        scrollTracker.reset()

        appSwitchTracker.resetWindowCounters()

        eventCount = 0
    }

    // Clear all measurements and temporary identifiers.
    fun resetAll() {

        typingTracker.reset()

        scrollTracker.reset()

        appSwitchTracker.reset()

        eventCount = 0

        currentContextPackage = null
    }
}
