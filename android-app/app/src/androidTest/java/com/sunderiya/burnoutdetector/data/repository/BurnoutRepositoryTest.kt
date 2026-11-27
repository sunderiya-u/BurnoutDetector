
package com.sunderiya.burnoutdetector.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sunderiya.burnoutdetector.aggregation.WindowSummary
import com.sunderiya.burnoutdetector.data.local.BurnoutDatabase
import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport
import com.sunderiya.burnoutdetector.data.mapper.WindowMapper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BurnoutRepositoryTest {

    private lateinit var database: BurnoutDatabase

    private lateinit var repository: BurnoutRepository

    @Before
    fun setup() {

        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            BurnoutDatabase::class.java
        ).build()

        repository = BurnoutRepository(
            windowDao = database.interactionWindowDao(),
            fatigueDao = database.fatigueReportDao()
        )
    }

    @After
    fun tearDown() {

        database.close()
    }

    private fun sampleSummary(
        startTime: Long = 300_000L
    ) = WindowSummary(

        startTime = startTime,
        endTime = startTime + 300_000L,
        utcOffsetMinutes = 60,
        monitoredMs = 300_000L,
        isComplete = true,

        eventCount = 25,
        invalidEventCount = 2,

        typedCharCount = 120,
        deletionCount = 30,
        replacementCount = 4,
        activeTypingMs = 30_000L,
        typingSpeed = 240.0,
        correctionRatio = 0.2,

        scrollGestureCount = 5,
        scrollActiveMs = 4_000L,
        scrollVariability = 120.0,
        scrollDistancePx = 2500L,

        appSwitchCount = 3
    )

    // -----------------------------------------
    // Test 1: Complete window persistence
    // -----------------------------------------

    @Test
    fun savesAndRetrievesValidWindow() =
        runBlocking {

            val summary = sampleSummary()

            val id = repository.saveWindow(summary)

            assertTrue(id > 0L)

            val stored = repository.getWindowByStartTime(
                summary.startTime
            )

            val expected = WindowMapper
                .toEntity(summary)
                .copy(id = id)

            assertEquals(expected, stored)

            assertEquals(1, repository.countWindows())

            assertEquals(
                1,
                repository.countCompleteWindows()
            )

            assertEquals(
                listOf(expected),
                repository.getAllWindows()
            )

            assertEquals(
                expected,
                repository.getLatestWindow()
            )
        }

    // -----------------------------------------
    // Test 2: Reject invalid window
    // -----------------------------------------

    @Test
    fun rejectsInvalidWindowBeforeInsertion() =
        runBlocking {

            val invalidSummary = sampleSummary().copy(
                monitoredMs = 400_000L
            )

            try {

                repository.saveWindow(invalidSummary)

                fail("Invalid window should be rejected")

            } catch (e: IllegalArgumentException) {

                // Expected validation failure
            }

            assertEquals(0, repository.countWindows())

            assertTrue(
                repository.getAllWindows().isEmpty()
            )
        }

    // -----------------------------------------
    // Test 3: Privacy suppression
    // -----------------------------------------

    @Test
    fun preservesSuppressedTypingDuringPersistence() =
        runBlocking {

            val summary = sampleSummary().copy(

                typedCharCount = null,
                deletionCount = null,
                replacementCount = null,
                activeTypingMs = null,
                typingSpeed = null,
                correctionRatio = null
            )

            val id = repository.saveWindow(summary)

            assertTrue(id > 0L)

            val stored = requireNotNull(
                repository.getWindowByStartTime(
                    summary.startTime
                )
            )

            assertNull(stored.typedCharCount)
            assertNull(stored.deletionCount)
            assertNull(stored.replacementCount)
            assertNull(stored.activeTypingMs)
            assertNull(stored.typingSpeed)
            assertNull(stored.correctionRatio)

            assertEquals(
                summary.appSwitchCount,
                stored.appSwitchCount
            )

            assertEquals(
                summary.scrollGestureCount,
                stored.scrollGestureCount
            )
        }

    // -----------------------------------------
    // Test 4: Duplicate window prevention
    // -----------------------------------------

    @Test
    fun preventsDuplicateWindowPersistence() =
        runBlocking {

            val summary = sampleSummary()

            val firstId = repository.saveWindow(summary)

            val duplicateId = repository.saveWindow(
                summary.copy(appSwitchCount = 99)
            )

            assertTrue(firstId > 0L)

            assertEquals(-1L, duplicateId)

            assertEquals(1, repository.countWindows())

            val stored = requireNotNull(
                repository.getWindowByStartTime(
                    summary.startTime
                )
            )

            assertEquals(firstId, stored.id)

            assertEquals(
                summary.appSwitchCount,
                stored.appSwitchCount
            )
        }

    // -----------------------------------------
    // Test 5: Complete and partial filtering
    // -----------------------------------------

    @Test
    fun filtersCompleteWindowsForResearch() =
        runBlocking {

            repository.saveWindow(
                sampleSummary(300_000L)
            )

            repository.saveWindow(
                sampleSummary(600_000L).copy(
                    monitoredMs = 120_000L,
                    isComplete = false
                )
            )

            repository.saveWindow(
                sampleSummary(900_000L)
            )

            val allWindows =
                repository.getWindowsByEndTime(
                    fromExclusive = 300_000L,
                    toInclusive = 1_200_000L,
                    completeOnly = false
                )

            val completeWindows =
                repository.getWindowsByEndTime(
                    fromExclusive = 300_000L,
                    toInclusive = 1_200_000L,
                    completeOnly = true
                )

            assertEquals(3, allWindows.size)

            assertEquals(2, completeWindows.size)

            assertTrue(
                completeWindows.all { it.isComplete }
            )

            assertEquals(3, repository.countWindows())

            assertEquals(
                2,
                repository.countCompleteWindows()
            )
        }

    // -----------------------------------------
    // Test 6: Fatigue report lifecycle
    // -----------------------------------------

    @Test
    fun savesPromptAndRecordsResponse() =
        runBlocking {

            val prompt = FatigueReport(
                promptedAt = 300_000L,
                respondedAt = null,
                utcOffsetMinutes = 60,
                kssScore = null
            )

            val id = repository.saveFatigueReport(prompt)

            assertTrue(id > 0L)

            assertEquals(
                prompt.copy(id = id),
                repository.getFatigueReportById(id)
            )

            assertEquals(
                1,
                repository.countScheduledPrompts()
            )

            assertEquals(
                0,
                repository.countAnsweredScheduledPrompts()
            )

            val updated = repository.recordFatigueResponse(
                id = id,
                respondedAt = 360_000L,
                score = 7,
                offsetMinutes = 60
            )

            assertTrue(updated)

            val stored = requireNotNull(
                repository.getFatigueReportById(id)
            )

            assertEquals(
                360_000L,
                stored.respondedAt
            )

            assertEquals(
                7,
                stored.kssScore
            )

            assertEquals(
                1,
                repository.countAnsweredScheduledPrompts()
            )

            assertEquals(
                1,
                repository.countFatigueReports()
            )

            assertEquals(
                listOf(stored),
                repository.getAnsweredFatigueReports()
            )

            val secondResponse =
                repository.recordFatigueResponse(
                    id = id,
                    respondedAt = 420_000L,
                    score = 9,
                    offsetMinutes = 60
                )

            assertFalse(secondResponse)

            assertEquals(
                stored,
                repository.getFatigueReportById(id)
            )
        }

    // -----------------------------------------
    // Test 7: Reject invalid fatigue report
    // -----------------------------------------

    @Test
    fun rejectsInvalidFatigueReportBeforeInsertion() =
        runBlocking {

            val invalidReport = FatigueReport(
                promptedAt = 300_000L,
                respondedAt = 360_000L,
                utcOffsetMinutes = 60,
                kssScore = 10
            )

            try {

                repository.saveFatigueReport(
                    invalidReport
                )

                fail("Invalid report should be rejected")

            } catch (e: IllegalArgumentException) {

                // Expected validation failure
            }

            assertEquals(
                0,
                repository.countFatigueReports()
            )

            assertTrue(
                repository.getAllFatigueReports().isEmpty()
            )
        }

    // -----------------------------------------
    // Test 8: Research observation matching
    // -----------------------------------------

    @Test
    fun retrievesPrecedingThirtyMinuteWindows() =
        runBlocking {

            // Eight consecutive five-minute windows
            for (index in 0 until 8) {

                repository.saveWindow(
                    sampleSummary(
                        index * 300_000L
                    )
                )
            }

            val responseTime = 2_100_000L

            val report = FatigueReport(
                promptedAt = 2_040_000L,
                respondedAt = responseTime,
                utcOffsetMinutes = 60,
                kssScore = 8
            )

            repository.saveFatigueReport(report)

            val thirtyMinutesMs = 30 * 60 * 1000L

            val matchedWindows =
                repository.getWindowsByEndTime(
                    fromExclusive =
                        responseTime - thirtyMinutesMs,
                    toInclusive = responseTime,
                    completeOnly = true
                )

            assertEquals(6, matchedWindows.size)

            assertEquals(
                listOf(
                    600_000L,
                    900_000L,
                    1_200_000L,
                    1_500_000L,
                    1_800_000L,
                    2_100_000L
                ),
                matchedWindows.map { it.endTime }
            )

            val reports =
                repository.getAnsweredFatigueReportsInRange(
                    fromInclusive = 2_100_000L,
                    toExclusive = 2_400_000L
                )

            assertEquals(1, reports.size)

            assertEquals(8, reports.first().kssScore)
        }
}
