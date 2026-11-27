
package com.sunderiya.burnoutdetector.aggregation

data class WindowSummary(
    val startTime: Long,
    val endTime: Long,
    val utcOffsetMinutes: Int,
    val monitoredMs: Long,
    val isComplete: Boolean,

    val eventCount: Int,
    val invalidEventCount: Int,

    // Typing features
    val typedCharCount: Int?,
    val deletionCount: Int?,
    val replacementCount: Int?,
    val activeTypingMs: Long?,
    val typingSpeed: Double?,
    val correctionRatio: Double?,

    // Scrolling features
    val scrollGestureCount: Int,
    val scrollActiveMs: Long,
    val scrollVariability: Double?,
    val scrollDistancePx: Long?,

    // Application-switching features
    val appSwitchCount: Int
)
