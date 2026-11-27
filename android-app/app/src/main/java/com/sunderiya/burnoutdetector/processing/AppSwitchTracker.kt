
package com.sunderiya.burnoutdetector.processing

data class AppSwitchSnapshot(
    val appSwitchCount: Int,
    val invalidEventCount: Int
)

class AppSwitchTracker(
    private val minDwellMs: Long = 1000L
) {

    init {
        require(minDwellMs > 0)
    }

    private var appSwitchCount = 0
    private var invalidEventCount = 0

    // Temporary identifiers: never persisted or logged.
    private var confirmedPackage: String? = null
    private var pendingPackage: String? = null
    private var pendingSinceMs: Long? = null

    private var lastTimestampMs: Long? = null

    fun process(
        packageName: String?,
        timestampMs: Long
    ) {

        if (packageName.isNullOrBlank()) {
            invalidEventCount++
            return
        }

        if (!acceptTimestamp(timestampMs)) {
            return
        }

        // Confirm an earlier transition if its
        // minimum dwell time has already elapsed.
        confirmPending(timestampMs)

        // The first application establishes a baseline.
        if (confirmedPackage == null) {
            confirmedPackage = packageName
            clearPending()
            return
        }

        // Returning to the confirmed application
        // cancels an unconfirmed transition.
        if (packageName == confirmedPackage) {
            clearPending()
            return
        }

        // Repeated events from the pending application
        // must not restart its dwell timer.
        if (packageName == pendingPackage) {
            return
        }

        // Start a new pending transition.
        pendingPackage = packageName
        pendingSinceMs = timestampMs
    }

    fun advanceTime(timestampMs: Long) {

        if (!acceptTimestamp(timestampMs)) {
            return
        }

        confirmPending(timestampMs)
    }

    private fun confirmPending(timestampMs: Long) {

        val candidate = pendingPackage ?: return
        val startedAt = pendingSinceMs ?: return

        val elapsed = timestampMs - startedAt

        if (elapsed >= minDwellMs) {

            if (candidate != confirmedPackage) {
                appSwitchCount++
                confirmedPackage = candidate
            }

            clearPending()
        }
    }

    private fun acceptTimestamp(
        timestampMs: Long
    ): Boolean {

        val previous = lastTimestampMs

        if (timestampMs < 0 ||
            (previous != null && timestampMs < previous)
        ) {
            invalidEventCount++
            return false
        }

        lastTimestampMs = timestampMs
        return true
    }

    private fun clearPending() {
        pendingPackage = null
        pendingSinceMs = null
    }

    // Used when foreground information becomes uncertain,
    // such as when an excluded window appears.
    fun clearForegroundState() {
        confirmedPackage = null
        clearPending()
    }

    fun snapshot(): AppSwitchSnapshot {

        return AppSwitchSnapshot(
            appSwitchCount = appSwitchCount,
            invalidEventCount = invalidEventCount
        )
    }

    // Reset window measurements while retaining
    // foreground continuity during active monitoring.
    fun resetWindowCounters() {
        appSwitchCount = 0
        invalidEventCount = 0
    }

    // Full reset when monitoring stops or consent is withdrawn.
    fun reset() {

        appSwitchCount = 0
        invalidEventCount = 0

        clearForegroundState()

        lastTimestampMs = null
    }
}
