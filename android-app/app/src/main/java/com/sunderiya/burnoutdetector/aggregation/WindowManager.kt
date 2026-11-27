
package com.sunderiya.burnoutdetector.aggregation

class WindowManager(
    private val onWindowCompleted: (WindowSummary) -> Unit
) {

    companion object {

        private const val WINDOW_MS =
            FeatureCalculator.WINDOW_DURATION_MS

        private const val CLOCK_TOLERANCE_MS = 2000L
    }

    private val accumulator = WindowAccumulator()

    private var monitoringActive = false

    private var windowStartMs = 0L

    private var windowEndMs = 0L

    private var monitoredMs = 0L

    private var utcOffsetMinutes = 0

    // Clock reference for the current monitoring session.
    private var anchorWallMs = 0L

    private var anchorElapsedMs = 0L

    private var lastElapsedMs = 0L

    // Prevent duplicate or backward windows within
    // this WindowManager instance.
    private var lastFinalizedStartMs: Long? = null

    fun isMonitoring(): Boolean {
        return monitoringActive
    }

    fun start(
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int
    ) {

        require(wallTimeMs >= 0L)
        require(elapsedTimeMs >= 0L)
        require(offsetMinutes in -1080..1080)

        if (monitoringActive) {
            return
        }

        accumulator.resetAll()

        monitoringActive = true

        anchorWallMs = wallTimeMs

        anchorElapsedMs = elapsedTimeMs

        lastElapsedMs = elapsedTimeMs

        utcOffsetMinutes = offsetMinutes

        initializeWindow(wallTimeMs)
    }

    private fun initializeWindow(
        wallTimeMs: Long
    ) {

        windowStartMs =
            wallTimeMs - wallTimeMs % WINDOW_MS

        windowEndMs =
            windowStartMs + WINDOW_MS

        monitoredMs = 0L
    }

    private fun projectedWallTime(
        elapsedTimeMs: Long
    ): Long {

        return anchorWallMs +
                (elapsedTimeMs - anchorElapsedMs)
    }

    private fun elapsedTimeForWall(
        wallTimeMs: Long
    ): Long {

        return anchorElapsedMs +
                (wallTimeMs - anchorWallMs)
    }

    fun tick(
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int
    ): Boolean {

        if (!monitoringActive) {
            return false
        }

        if (wallTimeMs < 0L ||
            elapsedTimeMs < lastElapsedMs ||
            offsetMinutes !in -1080..1080
        ) {
            return false
        }

        val expectedWallTime =
            projectedWallTime(elapsedTimeMs)

        val clockDifference =
            kotlin.math.abs(
                wallTimeMs - expectedWallTime
            )

        // Do not continue a window across a significant
        // wall-clock adjustment or timezone offset change.
        if (clockDifference > CLOCK_TOLERANCE_MS ||
            offsetMinutes != utcOffsetMinutes
        ) {

            restartAfterClockChange(
                wallTimeMs,
                elapsedTimeMs,
                offsetMinutes
            )

            return true
        }

        // Finalize every elapsed window in order.
        while (expectedWallTime >= windowEndMs) {

            val boundaryElapsedMs =
                elapsedTimeForWall(windowEndMs)

            // Confirm transitions strictly before
            // the boundary in the old window.
            val beforeBoundary =
                boundaryElapsedMs - 1L

            if (beforeBoundary >= lastElapsedMs) {
                accumulator.advanceTime(beforeBoundary)
            }

            monitoredMs +=
                boundaryElapsedMs - lastElapsedMs

            finalizeCurrentWindow()

            accumulator.resetWindow()

            windowStartMs = windowEndMs

            windowEndMs += WINDOW_MS

            monitoredMs = 0L

            lastElapsedMs = boundaryElapsedMs
        }

        // Account for elapsed monitoring time in
        // the currently active window.
        monitoredMs +=
            elapsedTimeMs - lastElapsedMs

        lastElapsedMs = elapsedTimeMs

        // A transition confirmed exactly at a window
        // boundary belongs to the new window.
        accumulator.advanceTime(elapsedTimeMs)

        return true
    }

    fun processTextChange(
        addedCount: Int,
        removedCount: Int,
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int
    ) {

        if (!tick(
                wallTimeMs,
                elapsedTimeMs,
                offsetMinutes
            )
        ) {
            return
        }

        accumulator.processTextChange(
            addedCount = addedCount,
            removedCount = removedCount,
            timestampMs = elapsedTimeMs
        )
    }

    fun processScroll(
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int,
        deltaX: Int? = null,
        deltaY: Int? = null
    ) {

        if (!tick(
                wallTimeMs,
                elapsedTimeMs,
                offsetMinutes
            )
        ) {
            return
        }

        accumulator.processScroll(
            timestampMs = elapsedTimeMs,
            deltaX = deltaX,
            deltaY = deltaY
        )
    }

    fun processWindowChange(
        packageName: String?,
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int
    ) {

        if (!tick(
                wallTimeMs,
                elapsedTimeMs,
                offsetMinutes
            )
        ) {
            return
        }

        accumulator.processWindowChange(
            packageName = packageName,
            timestampMs = elapsedTimeMs
        )
    }

    fun interruptContext(
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int
    ) {

        if (!monitoringActive) {
            return
        }

        // Conservatively cancel uncertain foreground
        // state before confirming further transitions.
        accumulator.interruptContext()

        tick(
            wallTimeMs,
            elapsedTimeMs,
            offsetMinutes
        )
    }

    fun pause(
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int
    ) {

        if (!monitoringActive) {
            return
        }

        tick(
            wallTimeMs,
            elapsedTimeMs,
            offsetMinutes
        )

        val summary = takeCurrentSummary()

        monitoringActive = false

        accumulator.resetAll()

        monitoredMs = 0L

        if (summary != null) {
            onWindowCompleted(summary)
        }
    }

    // Consent withdrawal or explicit data discard.
    // Do not finalize the unfinished window.
    fun discard() {

        monitoringActive = false

        accumulator.resetAll()

        monitoredMs = 0L

        windowStartMs = 0L

        windowEndMs = 0L

        anchorWallMs = 0L

        anchorElapsedMs = 0L

        lastElapsedMs = 0L

        utcOffsetMinutes = 0
    }

    private fun restartAfterClockChange(
        wallTimeMs: Long,
        elapsedTimeMs: Long,
        offsetMinutes: Int
    ) {

        // Discard the uncertain unfinished window.
        accumulator.resetAll()

        anchorWallMs = wallTimeMs

        anchorElapsedMs = elapsedTimeMs

        lastElapsedMs = elapsedTimeMs

        utcOffsetMinutes = offsetMinutes

        initializeWindow(wallTimeMs)
    }

    private fun takeCurrentSummary(): WindowSummary? {

        if (monitoredMs <= 0L) {
            return null
        }

        val previousStart = lastFinalizedStartMs

        // Keep the first finalized record for a
        // given window and reject backward windows.
        if (previousStart != null &&
            windowStartMs <= previousStart
        ) {
            return null
        }

        val summary = FeatureCalculator.calculate(
            startTime = windowStartMs,
            endTime = windowEndMs,
            utcOffsetMinutes = utcOffsetMinutes,
            monitoredMs = monitoredMs,
            snapshot = accumulator.snapshot()
        )

        lastFinalizedStartMs = windowStartMs

        return summary
    }

    private fun finalizeCurrentWindow() {

        val summary = takeCurrentSummary()

        if (summary != null) {
            onWindowCompleted(summary)
        }
    }
}
