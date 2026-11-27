
package com.sunderiya.burnoutdetector.data.mapper

import com.sunderiya.burnoutdetector.aggregation.WindowSummary
import com.sunderiya.burnoutdetector.data.local.entity.InteractionWindow

/**
 * Converts an aggregated WindowSummary into a Room entity.
 *
 * Nullable features are preserved exactly as produced
 * by FeatureCalculator.
 *
 * No individual events or application identifiers
 * are introduced during mapping.
 */
object WindowMapper {

    fun toEntity(summary: WindowSummary): InteractionWindow {

        return InteractionWindow(

            // Room generates the primary key.
            id = 0L,

            // Window metadata
            startTime = summary.startTime,
            endTime = summary.endTime,
            utcOffsetMinutes = summary.utcOffsetMinutes,
            monitoredMs = summary.monitoredMs,
            isComplete = summary.isComplete,

            // Non-typing event quality counters
            eventCount = summary.eventCount,
            invalidEventCount = summary.invalidEventCount,

            // Typing features
            typedCharCount = summary.typedCharCount,
            deletionCount = summary.deletionCount,
            replacementCount = summary.replacementCount,
            activeTypingMs = summary.activeTypingMs,
            typingSpeed = summary.typingSpeed,
            correctionRatio = summary.correctionRatio,

            // Scrolling features
            scrollGestureCount = summary.scrollGestureCount,
            scrollActiveMs = summary.scrollActiveMs,
            scrollVariability = summary.scrollVariability,
            scrollDistancePx = summary.scrollDistancePx,

            // Application switching
            appSwitchCount = summary.appSwitchCount
        )
    }
}
