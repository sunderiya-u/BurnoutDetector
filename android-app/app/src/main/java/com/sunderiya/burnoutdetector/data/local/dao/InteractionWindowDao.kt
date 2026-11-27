
package com.sunderiya.burnoutdetector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sunderiya.burnoutdetector.data.local.entity.InteractionWindow

@Dao
interface InteractionWindowDao {

    /**
     * Inserts a completed or partial monitoring window.
     *
     * Duplicate start times are ignored.
     * Returns the inserted row ID, or -1 if ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(window: InteractionWindow): Long

    /**
     * Returns all stored windows in chronological order.
     * Used for future research data export.
     */
    @Query("""
        SELECT * FROM interaction_windows
        ORDER BY startTime ASC
    """)
    suspend fun getAll(): List<InteractionWindow>

    /**
     * Returns a window with the specified start time.
     */
    @Query("""
        SELECT * FROM interaction_windows
        WHERE startTime = :startTime
        LIMIT 1
    """)
    suspend fun getByStartTime(
        startTime: Long
    ): InteractionWindow?

    /**
     * Returns windows ending within the specified
     * observation period.
     *
     * The lower boundary is exclusive.
     * The upper boundary is inclusive.
     */
    @Query("""
        SELECT * FROM interaction_windows
        WHERE endTime > :fromExclusive
        AND endTime <= :toInclusive
        ORDER BY startTime ASC
    """)
    suspend fun getWindowsByEndTime(
        fromExclusive: Long,
        toInclusive: Long
    ): List<InteractionWindow>

    /**
     * Returns complete windows ending within the
     * specified observation period.
     */
    @Query("""
        SELECT * FROM interaction_windows
        WHERE endTime > :fromExclusive
        AND endTime <= :toInclusive
        AND isComplete = 1
        ORDER BY startTime ASC
    """)
    suspend fun getCompleteWindowsByEndTime(
        fromExclusive: Long,
        toInclusive: Long
    ): List<InteractionWindow>

    /**
     * Returns the most recently stored window
     * according to its start time.
     */
    @Query("""
        SELECT * FROM interaction_windows
        ORDER BY startTime DESC
        LIMIT 1
    """)
    suspend fun getLatest(): InteractionWindow?

    /**
     * Returns the number of stored windows.
     */
    @Query("SELECT COUNT(*) FROM interaction_windows")
    suspend fun count(): Int

    /**
     * Returns the number of complete windows.
     */
    @Query("""
        SELECT COUNT(*) FROM interaction_windows
        WHERE isComplete = 1
    """)
    suspend fun countComplete(): Int

    /**
     * Deletes all interaction window records.
     */
    @Query("DELETE FROM interaction_windows")
    suspend fun deleteAll(): Int
}
