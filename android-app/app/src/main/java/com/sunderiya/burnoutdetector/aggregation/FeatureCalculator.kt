
package com.sunderiya.burnoutdetector.aggregation

object FeatureCalculator {

    const val WINDOW_DURATION_MS = 300_000L

    const val MIN_TYPED_CHARACTERS = 20

    fun calculate(
        startTime: Long,
        endTime: Long,
        utcOffsetMinutes: Int,
        monitoredMs: Long,
        snapshot: AccumulatorSnapshot
    ): WindowSummary {

        require(startTime >= 0L)

        require(startTime % WINDOW_DURATION_MS == 0L)

        require(endTime > startTime)

        require(endTime - startTime == WINDOW_DURATION_MS)

        require(monitoredMs in 0L..WINDOW_DURATION_MS)

        require(utcOffsetMinutes in -1080..1080)

        val typing = snapshot.typing

        val scrolling = snapshot.scrolling

        val appSwitching = snapshot.appSwitching

        // Privacy suppression for low typing activity.
        val typingAllowed =
            typing.typedCharCount >= MIN_TYPED_CHARACTERS

        val typingSpeed = if (
            typingAllowed && typing.activeTypingMs > 0L
        ) {
            typing.typedCharCount.toDouble() *
                    60_000.0 / typing.activeTypingMs
        } else {
            null
        }

        val totalTypingActions =
            typing.typedCharCount.toLong() +
                    typing.deletionCount.toLong()

        val correctionRatio = if (
            typingAllowed && totalTypingActions > 0L
        ) {
            typing.deletionCount.toDouble() /
                    totalTypingActions
        } else {
            null
        }

        return WindowSummary(

            // Window metadata
            startTime = startTime,

            endTime = endTime,

            utcOffsetMinutes = utcOffsetMinutes,

            monitoredMs = monitoredMs,

            isComplete =
                monitoredMs == WINDOW_DURATION_MS,

            // Non-typing event quality measurements
            eventCount = snapshot.eventCount,

            invalidEventCount = snapshot.invalidEventCount,

            // Privacy-filtered typing measurements
            typedCharCount =
                if (typingAllowed)
                    typing.typedCharCount
                else null,

            deletionCount =
                if (typingAllowed)
                    typing.deletionCount
                else null,

            replacementCount =
                if (typingAllowed)
                    typing.replacementCount
                else null,

            activeTypingMs =
                if (typingAllowed)
                    typing.activeTypingMs
                else null,

            typingSpeed = typingSpeed,

            correctionRatio = correctionRatio,

            // Scrolling measurements
            scrollGestureCount =
                scrolling.scrollGestureCount,

            scrollActiveMs =
                scrolling.scrollActiveMs,

            scrollVariability =
                scrolling.scrollVariability,

            scrollDistancePx =
                scrolling.scrollDistancePx,

            // Application-switching measurements
            appSwitchCount =
                appSwitching.appSwitchCount
        )
    }
}
