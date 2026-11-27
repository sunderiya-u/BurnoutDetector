
package com.sunderiya.burnoutdetector.data.validation

import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport
import org.junit.Test

class FatigueReportValidatorTest {

    private fun answeredReport() = FatigueReport(
        promptedAt = 300_000L,
        respondedAt = 360_000L,
        utcOffsetMinutes = 60,
        kssScore = 7
    )

    // -----------------------------------------
    // Valid reports
    // -----------------------------------------

    @Test
    fun acceptsAnsweredScheduledPrompt() {

        FatigueReportValidator.validate(answeredReport())
    }

    @Test
    fun acceptsUnansweredScheduledPrompt() {

        FatigueReportValidator.validate(
            answeredReport().copy(
                respondedAt = null,
                kssScore = null
            )
        )
    }

    @Test
    fun acceptsSelfInitiatedAssessment() {

        FatigueReportValidator.validate(
            answeredReport().copy(promptedAt = null)
        )
    }

    @Test
    fun acceptsMinimumKssScore() {

        FatigueReportValidator.validate(
            answeredReport().copy(kssScore = 1)
        )
    }

    @Test
    fun acceptsMaximumKssScore() {

        FatigueReportValidator.validate(
            answeredReport().copy(kssScore = 9)
        )
    }

    @Test
    fun acceptsImmediateResponse() {

        FatigueReportValidator.validate(
            answeredReport().copy(
                respondedAt = 300_000L
            )
        )
    }

    // -----------------------------------------
    // Invalid reports
    // -----------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun rejectsManuallyAssignedPrimaryKey() {

        FatigueReportValidator.validate(
            answeredReport().copy(id = 5L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingBothTimestamps() {

        FatigueReportValidator.validate(
            answeredReport().copy(
                promptedAt = null,
                respondedAt = null,
                kssScore = null
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsScoreWithoutResponseTimestamp() {

        FatigueReportValidator.validate(
            answeredReport().copy(respondedAt = null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsResponseWithoutScore() {

        FatigueReportValidator.validate(
            answeredReport().copy(kssScore = null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsScoreBelowOne() {

        FatigueReportValidator.validate(
            answeredReport().copy(kssScore = 0)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsScoreAboveNine() {

        FatigueReportValidator.validate(
            answeredReport().copy(kssScore = 10)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsResponseBeforePrompt() {

        FatigueReportValidator.validate(
            answeredReport().copy(
                respondedAt = 299_999L
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativePromptTimestamp() {

        FatigueReportValidator.validate(
            answeredReport().copy(promptedAt = -1L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidTimezoneOffset() {

        FatigueReportValidator.validate(
            answeredReport().copy(
                utcOffsetMinutes = -1081
            )
        )
    }
}
