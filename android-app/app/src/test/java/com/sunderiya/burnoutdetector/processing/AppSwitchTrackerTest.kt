
package com.sunderiya.burnoutdetector.processing

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSwitchTrackerTest {

    private val appA = "test.app.a"
    private val appB = "test.app.b"
    private val appC = "test.app.c"

    @Test
    fun initialSnapshotContainsZeros() {
        val tracker = AppSwitchTracker()

        assertEquals(
            AppSwitchSnapshot(0, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun firstApplicationDoesNotCountAsSwitch() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun repeatedSameApplicationDoesNotCountAsSwitch() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appA, 2000L)
        tracker.process(appA, 3000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun differentApplicationStartsPendingTransition() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun dwellBelowThresholdDoesNotConfirmSwitch() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.advanceTime(2999L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun exactDwellThresholdConfirmsSwitch() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun dwellAboveThresholdConfirmsSwitch() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.advanceTime(3500L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun repeatedTimerUpdatesDoNotDuplicateSwitch() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)

        tracker.advanceTime(3000L)
        tracker.advanceTime(4000L)
        tracker.advanceTime(5000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun briefTransitionIsCancelled() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.process(appA, 2500L)

        tracker.advanceTime(4000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun repeatedPendingApplicationDoesNotRestartTimer() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.process(appB, 2500L)
        tracker.process(appB, 2800L)

        tracker.advanceTime(3000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun newCandidateReplacesPendingApplication() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.process(appC, 2500L)

        tracker.advanceTime(3000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)

        tracker.advanceTime(3500L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun returningAfterThresholdCountsFirstTransition() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.process(appA, 3500L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun returningAtExactThresholdCountsFirstTransition() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.process(appA, 3000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun confirmedReturnCountsAsSecondSwitch() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        tracker.process(appA, 4000L)
        tracker.advanceTime(5000L)

        assertEquals(2, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun multipleConfirmedTransitionsAreCounted() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)

        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        tracker.process(appC, 4000L)
        tracker.advanceTime(5000L)

        tracker.process(appA, 6000L)
        tracker.advanceTime(7000L)

        assertEquals(3, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun applicationEventCanConfirmPendingTransition() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.process(appB, 3000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun advancingTimeWithoutPendingTransitionDoesNothing() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.advanceTime(5000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun nullPackageIsInvalid() {
        val tracker = AppSwitchTracker()

        tracker.process(null, 1000L)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun blankPackageIsInvalid() {
        val tracker = AppSwitchTracker()

        tracker.process("", 1000L)
        tracker.process("   ", 2000L)

        assertEquals(2, tracker.snapshot().invalidEventCount)
    }

    @Test
    fun negativeTimestampIsInvalid() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, -100L)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun outOfOrderApplicationEventIsRejected() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)

        tracker.process(appC, 1500L)

        tracker.advanceTime(3000L)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun outOfOrderTimerUpdateIsRejected() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)

        tracker.advanceTime(1500L)

        assertEquals(1, tracker.snapshot().invalidEventCount)
        assertEquals(0, tracker.snapshot().appSwitchCount)

        tracker.advanceTime(3000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun identicalTimestampsAreAccepted() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 1000L)

        tracker.advanceTime(2000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
        assertEquals(0, tracker.snapshot().invalidEventCount)
    }

    @Test
    fun customDwellThresholdIsRespected() {
        val tracker = AppSwitchTracker(minDwellMs = 500L)

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)

        tracker.advanceTime(2499L)

        assertEquals(0, tracker.snapshot().appSwitchCount)

        tracker.advanceTime(2500L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun clearingForegroundCancelsPendingTransition() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)

        tracker.clearForegroundState()
        tracker.advanceTime(4000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun clearingForegroundPreservesConfirmedSwitchCount() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        tracker.clearForegroundState()

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun firstApplicationAfterForegroundClearIsBaseline() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)

        tracker.clearForegroundState()

        tracker.process(appB, 2000L)
        tracker.advanceTime(4000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun windowResetClearsCounters() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        tracker.process(null, 3500L)

        tracker.resetWindowCounters()

        assertEquals(
            AppSwitchSnapshot(0, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun windowResetPreservesConfirmedApplication() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)

        tracker.resetWindowCounters()

        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun pendingTransitionCanContinueAcrossWindowBoundary() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 298000L)
        tracker.process(appB, 299500L)

        tracker.advanceTime(299999L)

        assertEquals(0, tracker.snapshot().appSwitchCount)

        // New five-minute window.
        tracker.resetWindowCounters()

        tracker.advanceTime(300500L)

        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun fullResetClearsAllMeasurements() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        tracker.process(null, 3500L)

        tracker.reset()

        assertEquals(
            AppSwitchSnapshot(0, 0),
            tracker.snapshot()
        )
    }

    @Test
    fun fullResetCancelsPendingTransition() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)
        tracker.process(appB, 2000L)

        tracker.reset()
        tracker.advanceTime(4000L)

        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun fullResetClearsTimestampHistory() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 5000L)

        tracker.reset()

        tracker.process(appB, 1000L)

        assertEquals(0, tracker.snapshot().invalidEventCount)
        assertEquals(0, tracker.snapshot().appSwitchCount)
    }

    @Test
    fun snapshotRemainsUnchangedAfterFurtherEvents() {
        val tracker = AppSwitchTracker()

        tracker.process(appA, 1000L)

        val previousSnapshot = tracker.snapshot()

        tracker.process(appB, 2000L)
        tracker.advanceTime(3000L)

        assertEquals(0, previousSnapshot.appSwitchCount)
        assertEquals(1, tracker.snapshot().appSwitchCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroDwellThresholdIsRejected() {
        AppSwitchTracker(minDwellMs = 0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeDwellThresholdIsRejected() {
        AppSwitchTracker(minDwellMs = -100L)
    }
}
