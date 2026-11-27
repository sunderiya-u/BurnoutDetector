
package com.sunderiya.burnoutdetector.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores aggregated behavioral measurements for one
 * clock-aligned five-minute monitoring window.
 *
 * No individual interaction events, typed content,
 * or application identifiers are persisted.
 */
@Entity(
    tableName = "interaction_windows",
    indices = [
        Index(
            value = ["startTime"],
            unique = true
        )
    ]
)
data class InteractionWindow(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Window metadata
    val startTime: Long,
    val endTime: Long,
    val utcOffsetMinutes: Int,
    val monitoredMs: Long,
    val isComplete: Boolean,

    // Non-typing event quality counters
    val eventCount: Int,
    val invalidEventCount: Int,

    // Typing features
    // Null when the minimum typing threshold is not met.
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

    // Application-switching feature
    val appSwitchCount: Int
)
