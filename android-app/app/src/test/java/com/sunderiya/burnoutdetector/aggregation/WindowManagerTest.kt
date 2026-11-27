
package com.sunderiya.burnoutdetector.aggregation

import org.junit.Assert.*
import org.junit.Test

class WindowManagerTest {

    private val summaries = mutableListOf<WindowSummary>()

    private val manager = WindowManager { summary ->
        summaries.add(summary)
    }

    private val appA = "test.app.a"
    private val appB = "test.app.b"

    // Simulated clocks with a constant UTC offset.
    private fun start(time: Long = 0L) {
        manager.start(time, time, 60)
    }

    private fun tick(time: Long): Boolean {
        return manager.tick(time, time, 60)
    }

    private fun pause(time: Long) {
        manager.pause(time, time, 60)
    }

    private fun type(time: Long) {
        manager.processTextChange(
            addedCount = 1,
            removedCount = 0,
            wallTimeMs = time,
            elapsedTimeMs = time,
            offsetMinutes = 60
        )
    }

    private fun scroll(time: Long) {
        manager.processScroll(
            wallTimeMs = time,
            elapsedTimeMs = time,
            offsetMinutes = 60,
            deltaY = 10
        )
    }

    private fun windowChange(
        packageName: String?,
        time: Long
    ) {
        manager.processWindowChange(
            packageName = packageName,
            wallTimeMs = time,
            elapsedTimeMs = time,
            offsetMinutes = 60
        )
    }

    // -----------------------------------------
    // 1. Monitoring initialization
    // -----------------------------------------

    @Test
    fun managerIsInitiallyInactive() {
        assertFalse(manager.isMonitoring())
        assertTrue(summaries.isEmpty())
    }

    @Test
    fun startActivatesMonitoring() {
        start()

        assertTrue(manager.isMonitoring())
        assertTrue(summaries.isEmpty())
    }

    @Test
    fun repeatedStartDoesNotResetActiveWindow() {
        start()

        scroll(1000L)

        start(2000L)

        tick(300_000L)

        assertEquals(1, summaries.size)
        assertEquals(1, summaries[0].eventCount)
        assertTrue(summaries[0].isComplete)
    }

    @Test
    fun inactiveManagerIgnoresEvents() {
        type(1000L)
        scroll(2000L)
        windowChange(appA, 3000L)

        assertFalse(tick(300_000L))
        assertTrue(summaries.isEmpty())
    }

    @Test
    fun pauseWhileInactiveDoesNothing() {
        pause(1000L)

        assertFalse(manager.isMonitoring())
        assertTrue(summaries.isEmpty())
    }

    @Test
    fun discardWhileInactiveDoesNothing() {
        manager.discard()

        assertFalse(manager.isMonitoring())
        assertTrue(summaries.isEmpty())
    }

    // -----------------------------------------
    // 2. Window boundaries
    // -----------------------------------------

    @Test
    fun windowIsNotFinalizedBeforeBoundary() {
        start()

        tick(299_999L)

        assertTrue(summaries.isEmpty())
    }

    @Test
    fun exactBoundaryFinalizesWindow() {
        start()

        tick(300_000L)

        assertEquals(1, summaries.size)
        assertEquals(0L, summaries[0].startTime)
        assertEquals(300_000L, summaries[0].endTime)
        assertEquals(300_000L, summaries[0].monitoredMs)
        assertTrue(summaries[0].isComplete)
    }

    @Test
    fun monitoringRemainsActiveAfterBoundary() {
        start()

        tick(300_000L)

        assertTrue(manager.isMonitoring())
    }

    @Test
    fun multipleBoundariesProduceSeparateWindows() {
        start()

        tick(900_000L)

        assertEquals(3, summaries.size)

        summaries.forEachIndexed { index, summary ->

            assertEquals(
                index * 300_000L,
                summary.startTime
            )

            assertEquals(
                (index + 1) * 300_000L,
                summary.endTime
            )

            assertEquals(300_000L, summary.monitoredMs)
            assertTrue(summary.isComplete)
        }
    }

    @Test
    fun repeatedTickAtSameBoundaryDoesNotDuplicateWindow() {
        start()

        tick(300_000L)
        tick(300_000L)
        tick(300_000L)

        assertEquals(1, summaries.size)
    }

    @Test
    fun eventExactlyAtBoundaryBelongsToNextWindow() {
        start()

        scroll(299_999L)
        scroll(300_000L)

        tick(600_000L)

        assertEquals(2, summaries.size)
        assertEquals(1, summaries[0].eventCount)
        assertEquals(1, summaries[1].eventCount)
    }

    @Test
    fun zeroActivityWindowIsStillFinalized() {
        start()

        tick(300_000L)

        val result = summaries.single()

        assertEquals(0, result.eventCount)
        assertEquals(0, result.scrollGestureCount)
        assertEquals(0, result.appSwitchCount)

        assertNull(result.typedCharCount)
        assertNull(result.typingSpeed)
        assertNull(result.correctionRatio)

        assertTrue(result.isComplete)
    }

    // -----------------------------------------
    // 3. Partial windows
    // -----------------------------------------

    @Test
    fun startingMidWindowProducesPartialFirstWindow() {
        start(120_000L)

        tick(300_000L)

        val result = summaries.single()

        assertEquals(0L, result.startTime)
        assertEquals(300_000L, result.endTime)
        assertEquals(180_000L, result.monitoredMs)
        assertFalse(result.isComplete)
    }

    @Test
    fun fullWindowAfterPartialWindowIsComplete() {
        start(120_000L)

        tick(600_000L)

        assertEquals(2, summaries.size)

        assertFalse(summaries[0].isComplete)
        assertEquals(180_000L, summaries[0].monitoredMs)

        assertTrue(summaries[1].isComplete)
        assertEquals(300_000L, summaries[1].monitoredMs)
    }

    @Test
    fun startTimeIsAlignedToFiveMinuteBoundary() {
        start(650_000L)

        tick(900_000L)

        val result = summaries.single()

        assertEquals(600_000L, result.startTime)
        assertEquals(900_000L, result.endTime)
        assertEquals(250_000L, result.monitoredMs)
    }

    @Test
    fun pauseFinalizesPartialWindow() {
        start()

        scroll(1000L)

        pause(120_000L)

        val result = summaries.single()

        assertFalse(manager.isMonitoring())
        assertFalse(result.isComplete)

        assertEquals(120_000L, result.monitoredMs)
        assertEquals(1, result.eventCount)
    }

    @Test
    fun pauseExactlyAtBoundaryDoesNotCreateEmptyPartialWindow() {
        start()

        pause(300_000L)

        assertEquals(1, summaries.size)
        assertTrue(summaries[0].isComplete)
        assertFalse(manager.isMonitoring())
    }

    @Test
    fun immediatePauseDoesNotCreateZeroDurationWindow() {
        start(1000L)

        pause(1000L)

        assertTrue(summaries.isEmpty())
        assertFalse(manager.isMonitoring())
    }

    @Test
    fun pauseAfterBoundaryFinalizesFullAndPartialWindows() {
        start()

        pause(420_000L)

        assertEquals(2, summaries.size)

        assertTrue(summaries[0].isComplete)
        assertFalse(summaries[1].isComplete)

        assertEquals(120_000L, summaries[1].monitoredMs)
    }

    // -----------------------------------------
    // 4. Duplicate prevention
    // -----------------------------------------

    @Test
    fun resumeWithinSameWindowDoesNotProduceDuplicate() {
        start()

        pause(100_000L)

        start(150_000L)

        tick(300_000L)

        assertEquals(1, summaries.size)
        assertEquals(100_000L, summaries[0].monitoredMs)
    }

    @Test
    fun nextWindowCanBeSavedAfterDuplicateIsSkipped() {
        start()

        pause(100_000L)

        start(150_000L)

        tick(600_000L)

        assertEquals(2, summaries.size)

        assertEquals(0L, summaries[0].startTime)
        assertEquals(300_000L, summaries[1].startTime)

        assertTrue(summaries[1].isComplete)
    }

    @Test
    fun repeatedPauseDoesNotDuplicateSummary() {
        start()

        pause(100_000L)
        pause(150_000L)

        assertEquals(1, summaries.size)
    }

    // -----------------------------------------
    // 5. Consent withdrawal and discard
    // -----------------------------------------

    @Test
    fun discardDoesNotFinalizeUnfinishedWindow() {
        start()

        scroll(1000L)
        type(2000L)

        manager.discard()

        assertFalse(manager.isMonitoring())
        assertTrue(summaries.isEmpty())
    }

    @Test
    fun discardPreservesPreviouslyFinalizedSummaries() {
        start()

        tick(300_000L)

        scroll(310_000L)

        manager.discard()

        assertEquals(1, summaries.size)
        assertFalse(manager.isMonitoring())
    }

    @Test
    fun eventsAfterDiscardAreIgnored() {
        start()

        manager.discard()

        scroll(1000L)
        type(2000L)

        tick(300_000L)

        assertTrue(summaries.isEmpty())
    }

    @Test
    fun restartAfterDiscardDoesNotRetainOldMeasurements() {
        start()

        repeat(20) { index ->
            type(1000L + index * 100L)
        }

        scroll(5000L)

        manager.discard()

        start(10_000L)

        tick(300_000L)

        val result = summaries.single()

        assertNull(result.typedCharCount)
        assertEquals(0, result.eventCount)
        assertEquals(0, result.scrollGestureCount)
    }

    // -----------------------------------------
    // 6. Typing and scrolling continuity
    // -----------------------------------------

    @Test
    fun typingCountsDoNotCarryAcrossWindows() {
        start()

        repeat(20) { index ->
            type(1000L + index * 100L)
        }

        tick(300_000L)

        type(301_000L)

        tick(600_000L)

        assertEquals(20, summaries[0].typedCharCount!!)
        assertNull(summaries[1].typedCharCount)
    }

    @Test
    fun typingDurationDoesNotCrossWindowBoundary() {
        start()

        repeat(20) { index ->
            type(298_000L + index * 100L)
        }

        repeat(20) { index ->
            type(300_000L + index * 100L)
        }

        tick(600_000L)

        assertEquals(1900L, summaries[0].activeTypingMs!!)
        assertEquals(1900L, summaries[1].activeTypingMs!!)
    }

    @Test
    fun scrollGestureDoesNotCrossWindowBoundary() {
        start()

        scroll(299_900L)
        scroll(300_100L)

        tick(600_000L)

        assertEquals(1, summaries[0].scrollGestureCount)
        assertEquals(1, summaries[1].scrollGestureCount)

        assertEquals(0L, summaries[0].scrollActiveMs)
        assertEquals(0L, summaries[1].scrollActiveMs)
    }

    @Test
    fun lowActivityTypingIsSuppressedInFinalSummary() {
        start()

        repeat(19) { index ->
            type(1000L + index * 100L)
        }

        tick(300_000L)

        val result = summaries.single()

        assertNull(result.typedCharCount)
        assertNull(result.deletionCount)
        assertNull(result.replacementCount)
        assertNull(result.activeTypingMs)
        assertNull(result.typingSpeed)
        assertNull(result.correctionRatio)

        assertEquals(0, result.eventCount)
        assertEquals(0, result.invalidEventCount)
    }

    // -----------------------------------------
    // 7. Application-switch boundaries
    // -----------------------------------------

    @Test
    fun timerConfirmsPendingApplicationSwitch() {
        start()

        windowChange(appA, 1000L)
        windowChange(appB, 2000L)

        tick(3000L)
        tick(300_000L)

        assertEquals(1, summaries.single().appSwitchCount)
    }

    @Test
    fun switchConfirmedBeforeBoundaryBelongsToOldWindow() {
        start()

        windowChange(appA, 297_000L)
        windowChange(appB, 298_999L)

        tick(600_000L)

        assertEquals(1, summaries[0].appSwitchCount)
        assertEquals(0, summaries[1].appSwitchCount)
    }

    @Test
    fun switchConfirmedExactlyAtBoundaryBelongsToNewWindow() {
        start()

        windowChange(appA, 298_000L)
        windowChange(appB, 299_000L)

        tick(600_000L)

        assertEquals(0, summaries[0].appSwitchCount)
        assertEquals(1, summaries[1].appSwitchCount)
    }

    @Test
    fun pendingSwitchSurvivesWindowBoundary() {
        start()

        windowChange(appA, 298_000L)
        windowChange(appB, 299_500L)

        tick(300_000L)
        tick(300_500L)
        tick(600_000L)

        assertEquals(0, summaries[0].appSwitchCount)
        assertEquals(1, summaries[1].appSwitchCount)
    }

    @Test
    fun excludedContextCancelsPendingSwitch() {
        start()

        windowChange(appA, 1000L)
        windowChange(appB, 2000L)

        manager.interruptContext(
            wallTimeMs = 2500L,
            elapsedTimeMs = 2500L,
            offsetMinutes = 60
        )

        tick(300_000L)

        assertEquals(0, summaries.single().appSwitchCount)
    }

    @Test
    fun contextInterruptionSeparatesScrollGestures() {
        start()

        scroll(1000L)

        manager.interruptContext(
            wallTimeMs = 1100L,
            elapsedTimeMs = 1100L,
            offsetMinutes = 60
        )

        scroll(1200L)

        tick(300_000L)

        assertEquals(2, summaries.single().scrollGestureCount)
        assertEquals(0L, summaries.single().scrollActiveMs)
    }

    // -----------------------------------------
    // 8. Clock adjustments
    // -----------------------------------------

    @Test
    fun smallClockDifferenceDoesNotResetWindow() {
        start()

        scroll(1000L)

        manager.tick(
            wallTimeMs = 101_000L,
            elapsedTimeMs = 100_000L,
            offsetMinutes = 60
        )

        tick(300_000L)

        assertEquals(1, summaries.size)
        assertEquals(1, summaries[0].eventCount)
        assertTrue(summaries[0].isComplete)
    }

    @Test
    fun exactClockToleranceDoesNotResetWindow() {
        start()

        scroll(1000L)

        manager.tick(
            wallTimeMs = 102_000L,
            elapsedTimeMs = 100_000L,
            offsetMinutes = 60
        )

        tick(300_000L)

        assertEquals(1, summaries.single().eventCount)
    }

    @Test
    fun forwardClockJumpDiscardsUncertainWindow() {
        start()

        scroll(1000L)

        manager.tick(
            wallTimeMs = 600_000L,
            elapsedTimeMs = 100_000L,
            offsetMinutes = 60
        )

        assertTrue(summaries.isEmpty())

        manager.tick(
            wallTimeMs = 900_000L,
            elapsedTimeMs = 400_000L,
            offsetMinutes = 60
        )

        val result = summaries.single()

        assertEquals(600_000L, result.startTime)
        assertEquals(0, result.eventCount)
        assertTrue(result.isComplete)
    }

    @Test
    fun backwardClockJumpDiscardsUncertainWindow() {
        start(600_000L)

        scroll(610_000L)

        manager.tick(
            wallTimeMs = 300_000L,
            elapsedTimeMs = 620_000L,
            offsetMinutes = 60
        )

        assertTrue(summaries.isEmpty())

        manager.tick(
            wallTimeMs = 600_000L,
            elapsedTimeMs = 920_000L,
            offsetMinutes = 60
        )

        val result = summaries.single()

        assertEquals(300_000L, result.startTime)
        assertEquals(0, result.eventCount)
        assertTrue(result.isComplete)
    }

    @Test
    fun backwardClockJumpDoesNotDuplicateFinalizedWindow() {
        start()

        tick(300_000L)

        manager.tick(
            wallTimeMs = 0L,
            elapsedTimeMs = 310_000L,
            offsetMinutes = 60
        )

        manager.tick(
            wallTimeMs = 300_000L,
            elapsedTimeMs = 610_000L,
            offsetMinutes = 60
        )

        assertEquals(1, summaries.size)
    }

    @Test
    fun timezoneOffsetChangeDiscardsUnfinishedWindow() {
        start()

        scroll(1000L)

        manager.tick(
            wallTimeMs = 120_000L,
            elapsedTimeMs = 120_000L,
            offsetMinutes = 120
        )

        assertTrue(summaries.isEmpty())

        manager.tick(
            wallTimeMs = 300_000L,
            elapsedTimeMs = 300_000L,
            offsetMinutes = 120
        )

        val result = summaries.single()

        assertEquals(120, result.utcOffsetMinutes)
        assertEquals(180_000L, result.monitoredMs)
        assertEquals(0, result.eventCount)
        assertFalse(result.isComplete)
    }

    // -----------------------------------------
    // 9. Invalid timestamps and parameters
    // -----------------------------------------

    @Test
    fun outOfOrderElapsedTimestampIsRejected() {
        start()

        tick(2000L)

        assertFalse(tick(1000L))
        assertTrue(manager.isMonitoring())
    }

    @Test
    fun outOfOrderEventDoesNotEnterSummary() {
        start()

        scroll(2000L)
        scroll(1000L)

        tick(300_000L)

        assertEquals(1, summaries.single().eventCount)
    }

    @Test
    fun negativeWallTimestampIsRejected() {
        start()

        assertFalse(
            manager.tick(-1L, 1000L, 60)
        )
    }

    @Test
    fun invalidUtcOffsetIsRejectedDuringTick() {
        start()

        assertFalse(
            manager.tick(1000L, 1000L, 1081)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeStartWallTimestampIsRejected() {
        manager.start(-1L, 0L, 60)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeStartElapsedTimestampIsRejected() {
        manager.start(0L, -1L, 60)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidStartUtcOffsetIsRejected() {
        manager.start(0L, 0L, 1081)
    }

    // -----------------------------------------
    // 10. Independent clock references
    // -----------------------------------------

    @Test
    fun differentClockOriginsProduceCorrectDuration() {
        manager.start(
            wallTimeMs = 600_000L,
            elapsedTimeMs = 50_000L,
            offsetMinutes = 60
        )

        manager.tick(
            wallTimeMs = 900_000L,
            elapsedTimeMs = 350_000L,
            offsetMinutes = 60
        )

        val result = summaries.single()

        assertEquals(600_000L, result.startTime)
        assertEquals(900_000L, result.endTime)
        assertEquals(300_000L, result.monitoredMs)
        assertTrue(result.isComplete)
    }
}
