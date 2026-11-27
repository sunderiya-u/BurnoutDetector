
package com.sunderiya.burnoutdetector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport

@Dao
interface FatigueReportDao {

    /**
     * Inserts a scheduled prompt or self-initiated report.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: FatigueReport): Long

    /**
     * Retrieves all reports in chronological order.
     */
    @Query("""
        SELECT * FROM fatigue_reports
        ORDER BY COALESCE(promptedAt, respondedAt) ASC,
        id ASC
    """)
    suspend fun getAll(): List<FatigueReport>

    /**
     * Retrieves a specific report by its ID.
     */
    @Query("""
        SELECT * FROM fatigue_reports
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getById(id: Long): FatigueReport?

    /**
     * Records a response to an unanswered scheduled prompt.
     *
     * Returns the number of updated rows.
     * A previously answered prompt cannot be overwritten.
     */
    @Query("""
        UPDATE fatigue_reports
        SET respondedAt = :respondedAt,
            kssScore = :score,
            utcOffsetMinutes = :offsetMinutes
        WHERE id = :id
        AND promptedAt IS NOT NULL
        AND respondedAt IS NULL
        AND kssScore IS NULL
        AND :respondedAt >= promptedAt
        AND :score BETWEEN 1 AND 9
        AND :offsetMinutes BETWEEN -1080 AND 1080
    """)
    suspend fun recordResponse(
        id: Long,
        respondedAt: Long,
        score: Int,
        offsetMinutes: Int
    ): Int

    /**
     * Retrieves all answered assessments.
     */
    @Query("""
        SELECT * FROM fatigue_reports
        WHERE respondedAt IS NOT NULL
        AND kssScore BETWEEN 1 AND 9
        ORDER BY respondedAt ASC, id ASC
    """)
    suspend fun getAnsweredReports(): List<FatigueReport>

    /**
     * Retrieves answered assessments within a time range.
     */
    @Query("""
        SELECT * FROM fatigue_reports
        WHERE respondedAt >= :fromInclusive
        AND respondedAt < :toExclusive
        AND kssScore BETWEEN 1 AND 9
        ORDER BY respondedAt ASC, id ASC
    """)
    suspend fun getAnsweredReportsInRange(
        fromInclusive: Long,
        toExclusive: Long
    ): List<FatigueReport>

    /**
     * Counts all stored fatigue reports.
     */
    @Query("SELECT COUNT(*) FROM fatigue_reports")
    suspend fun count(): Int

    /**
     * Counts displayed scheduled prompts.
     * Self-initiated assessments are excluded.
     */
    @Query("""
        SELECT COUNT(*) FROM fatigue_reports
        WHERE promptedAt IS NOT NULL
    """)
    suspend fun countScheduledPrompts(): Int

    /**
     * Counts answered scheduled prompts.
     */
    @Query("""
        SELECT COUNT(*) FROM fatigue_reports
        WHERE promptedAt IS NOT NULL
        AND respondedAt IS NOT NULL
        AND kssScore BETWEEN 1 AND 9
    """)
    suspend fun countAnsweredScheduledPrompts(): Int

    /**
     * Deletes all fatigue reports.
     */
    @Query("DELETE FROM fatigue_reports")
    suspend fun deleteAll(): Int
}
