
package com.sunderiya.burnoutdetector.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores a self-reported fatigue assessment.
 *
 * A report can originate from a scheduled prompt
 * or a self-initiated assessment.
 *
 * Unanswered prompts have null response timestamps
 * and null scores.
 */
@Entity(
    tableName = "fatigue_reports",
    indices = [
        Index(value = ["promptedAt"]),
        Index(value = ["respondedAt"])
    ]
)
data class FatigueReport(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // UTC timestamp when a prompt was displayed.
    // Null for self-initiated assessments.
    val promptedAt: Long?,

    // UTC timestamp when the participant responded.
    // Null for unanswered prompts.
    val respondedAt: Long?,

    // Local timezone offset in minutes.
    val utcOffsetMinutes: Int,

    // Karolinska Sleepiness Scale score (1–9).
    // Null for unanswered prompts.
    val kssScore: Int?
)
