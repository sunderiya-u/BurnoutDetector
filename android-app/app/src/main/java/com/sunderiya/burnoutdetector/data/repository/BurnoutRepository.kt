
package com.sunderiya.burnoutdetector.data.repository

import com.sunderiya.burnoutdetector.aggregation.WindowSummary
import com.sunderiya.burnoutdetector.data.local.dao.FatigueReportDao
import com.sunderiya.burnoutdetector.data.local.dao.InteractionWindowDao
import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport
import com.sunderiya.burnoutdetector.data.local.entity.InteractionWindow
import com.sunderiya.burnoutdetector.data.mapper.WindowMapper
import com.sunderiya.burnoutdetector.data.validation.FatigueReportValidator
import com.sunderiya.burnoutdetector.data.validation.WindowValidator

/**
 * Provides validated access to local research data.
 *
 * Monitoring permission, write synchronization and
 * database lifecycle must be coordinated by the caller.
 */
class BurnoutRepository(
    private val windowDao: InteractionWindowDao,
    private val fatigueDao: FatigueReportDao
) {

    // -----------------------------------------
    // Interaction windows
    // -----------------------------------------

    /**
     * Validates and stores an aggregated window.
     *
     * Returns the inserted row ID, or -1 if ignored
     * because of an insertion conflict.
     */
    suspend fun saveWindow(summary: WindowSummary): Long {

        WindowValidator.validate(summary)

        val entity = WindowMapper.toEntity(summary)

        return windowDao.insert(entity)
    }

    suspend fun getAllWindows(): List<InteractionWindow> {

        return windowDao.getAll()
    }

    suspend fun getWindowByStartTime(
        startTime: Long
    ): InteractionWindow? {

        require(startTime >= 0L)

        return windowDao.getByStartTime(startTime)
    }

    suspend fun getLatestWindow(): InteractionWindow? {

        return windowDao.getLatest()
    }

    suspend fun getWindowsByEndTime(
        fromExclusive: Long,
        toInclusive: Long,
        completeOnly: Boolean = false
    ): List<InteractionWindow> {

        require(fromExclusive >= 0L)
        require(toInclusive > fromExclusive)

        return if (completeOnly) {

            windowDao.getCompleteWindowsByEndTime(
                fromExclusive,
                toInclusive
            )

        } else {

            windowDao.getWindowsByEndTime(
                fromExclusive,
                toInclusive
            )
        }
    }

    suspend fun countWindows(): Int {

        return windowDao.count()
    }

    suspend fun countCompleteWindows(): Int {

        return windowDao.countComplete()
    }

    // -----------------------------------------
    // Fatigue reports
    // -----------------------------------------

    suspend fun saveFatigueReport(
        report: FatigueReport
    ): Long {

        FatigueReportValidator.validate(report)

        return fatigueDao.insert(report)
    }

    suspend fun recordFatigueResponse(
        id: Long,
        respondedAt: Long,
        score: Int,
        offsetMinutes: Int
    ): Boolean {

        require(id > 0L)
        require(respondedAt >= 0L)
        require(score in 1..9)
        require(offsetMinutes in -1080..1080)

        val updatedRows = fatigueDao.recordResponse(
            id = id,
            respondedAt = respondedAt,
            score = score,
            offsetMinutes = offsetMinutes
        )

        return updatedRows == 1
    }

    suspend fun getAllFatigueReports(): List<FatigueReport> {

        return fatigueDao.getAll()
    }

    suspend fun getFatigueReportById(
        id: Long
    ): FatigueReport? {

        require(id > 0L)

        return fatigueDao.getById(id)
    }

    suspend fun getAnsweredFatigueReports(): List<FatigueReport> {

        return fatigueDao.getAnsweredReports()
    }

    suspend fun getAnsweredFatigueReportsInRange(
        fromInclusive: Long,
        toExclusive: Long
    ): List<FatigueReport> {

        require(fromInclusive >= 0L)
        require(toExclusive > fromInclusive)

        return fatigueDao.getAnsweredReportsInRange(
            fromInclusive,
            toExclusive
        )
    }

    suspend fun countFatigueReports(): Int {

        return fatigueDao.count()
    }

    suspend fun countScheduledPrompts(): Int {

        return fatigueDao.countScheduledPrompts()
    }

    suspend fun countAnsweredScheduledPrompts(): Int {

        return fatigueDao.countAnsweredScheduledPrompts()
    }
}
