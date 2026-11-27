
package com.sunderiya.burnoutdetector.data.validation

import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport

/**
 * Validates self-reported assessments before insertion.
 */
object FatigueReportValidator {

    fun validate(report: FatigueReport) {

        require(report.id == 0L)

        require(
            report.utcOffsetMinutes in -1080..1080
        )

        val promptedAt = report.promptedAt
        val respondedAt = report.respondedAt
        val score = report.kssScore

        // At least one timestamp must exist.
        require(
            promptedAt != null || respondedAt != null
        )

        if (promptedAt != null) {
            require(promptedAt >= 0L)
        }

        if (respondedAt != null) {
            require(respondedAt >= 0L)
        }

        // A response timestamp and score must
        // either both exist or both be null.
        require(
            (respondedAt == null) == (score == null)
        )

        if (score != null) {
            require(score in 1..9)
        }

        // Scheduled responses cannot precede prompts.
        if (promptedAt != null && respondedAt != null) {

            require(respondedAt >= promptedAt)
        }
    }
}
