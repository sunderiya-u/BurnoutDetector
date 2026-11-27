
package com.sunderiya.burnoutdetector.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sunderiya.burnoutdetector.data.local.dao.FatigueReportDao
import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FatigueReportDaoTest {

    private lateinit var database: BurnoutDatabase

    private lateinit var dao: FatigueReportDao

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

        dao = database.fatigueReportDao()
    }

    @After
    fun tearDown() {

        database.close()
    }

    private fun answeredReport() = FatigueReport(
        promptedAt = 300_000L,
        respondedAt = 360_000L,
        utcOffsetMinutes = 60,
        kssScore = 7
    )

    private fun unansweredReport() = FatigueReport(
        promptedAt = 300_000L,
        respondedAt = null,
        utcOffsetMinutes = 60,
        kssScore = null
    )

    private fun selfInitiatedReport() = FatigueReport(
        promptedAt = null,
        respondedAt = 600_000L,
        utcOffsetMinutes = 60,
        kssScore = 5
    )

    // -----------------------------------------
    // Test 1: Empty database
    // -----------------------------------------

    @Test
    fun databaseInitiallyEmpty() = runBlocking {

        assertEquals(0, dao.count())
        assertEquals(0, dao.countScheduledPrompts())
        assertEquals(0, dao.countAnsweredScheduledPrompts())

        assertTrue(dao.getAll().isEmpty())
        assertTrue(dao.getAnsweredReports().isEmpty())
    }

    // -----------------------------------------
    // Test 2: Answered scheduled prompt
    // -----------------------------------------

    @Test
    fun insertsAndRetrievesAnsweredReport() =
        runBlocking {

            val original = answeredReport()

            val id = dao.insert(original)

            assertTrue(id > 0L)

            val stored = dao.getById(id)

            assertEquals(
                original.copy(id = id),
                stored
            )

            assertEquals(1, dao.count())
        }

    // -----------------------------------------
    // Test 3: Unanswered scheduled prompt
    // -----------------------------------------

    @Test
    fun preservesUnansweredPromptFields() =
        runBlocking {

            val original = unansweredReport()

            val id = dao.insert(original)

            val stored = requireNotNull(
                dao.getById(id)
            )

            assertEquals(
                original.copy(id = id),
                stored
            )

            assertNull(stored.respondedAt)
            assertNull(stored.kssScore)

            assertEquals(1, dao.countScheduledPrompts())
            assertEquals(0, dao.countAnsweredScheduledPrompts())
        }

    // -----------------------------------------
    // Test 4: Self-initiated assessment
    // -----------------------------------------

    @Test
    fun insertsSelfInitiatedReport() =
        runBlocking {

            val original = selfInitiatedReport()

            val id = dao.insert(original)

            val stored = requireNotNull(
                dao.getById(id)
            )

            assertEquals(
                original.copy(id = id),
                stored
            )

            assertNull(stored.promptedAt)

            assertEquals(1, dao.count())
            assertEquals(0, dao.countScheduledPrompts())
            assertEquals(1, dao.getAnsweredReports().size)
        }

    // -----------------------------------------
    // Test 5: Record a response
    // -----------------------------------------

    @Test
    fun recordsResponseToUnansweredPrompt() =
        runBlocking {

            val id = dao.insert(unansweredReport())

            val updatedRows = dao.recordResponse(
                id = id,
                respondedAt = 360_000L,
                score = 8,
                offsetMinutes = 120
            )

            assertEquals(1, updatedRows)

            val stored = requireNotNull(
                dao.getById(id)
            )

            assertEquals(
                FatigueReport(
                    id = id,
                    promptedAt = 300_000L,
                    respondedAt = 360_000L,
                    utcOffsetMinutes = 120,
                    kssScore = 8
                ),
                stored
            )

            assertEquals(1, dao.count())
            assertEquals(1, dao.countAnsweredScheduledPrompts())
        }

    // -----------------------------------------
    // Test 6: Prevent response overwriting
    // -----------------------------------------

    @Test
    fun preventsOverwritingAnsweredPrompt() =
        runBlocking {

            val original = answeredReport()

            val id = dao.insert(original)

            val updatedRows = dao.recordResponse(
                id = id,
                respondedAt = 420_000L,
                score = 9,
                offsetMinutes = 60
            )

            assertEquals(0, updatedRows)

            assertEquals(
                original.copy(id = id),
                dao.getById(id)
            )
        }

    // -----------------------------------------
    // Test 7: Reject invalid responses
    // -----------------------------------------

    @Test
    fun rejectsInvalidResponseValues() =
        runBlocking {

            val id = dao.insert(unansweredReport())

            // Response before the prompt
            assertEquals(
                0,
                dao.recordResponse(
                    id = id,
                    respondedAt = 299_999L,
                    score = 7,
                    offsetMinutes = 60
                )
            )

            // Score below the minimum
            assertEquals(
                0,
                dao.recordResponse(
                    id = id,
                    respondedAt = 360_000L,
                    score = 0,
                    offsetMinutes = 60
                )
            )

            // Score above the maximum
            assertEquals(
                0,
                dao.recordResponse(
                    id = id,
                    respondedAt = 360_000L,
                    score = 10,
                    offsetMinutes = 60
                )
            )

            // Invalid timezone offset
            assertEquals(
                0,
                dao.recordResponse(
                    id = id,
                    respondedAt = 360_000L,
                    score = 7,
                    offsetMinutes = 1081
                )
            )

            // Nonexistent report
            assertEquals(
                0,
                dao.recordResponse(
                    id = 9999L,
                    respondedAt = 360_000L,
                    score = 7,
                    offsetMinutes = 60
                )
            )

            val stored = requireNotNull(
                dao.getById(id)
            )

            assertNull(stored.respondedAt)
            assertNull(stored.kssScore)
        }

    // -----------------------------------------
    // Test 8: Answered report filtering
    // -----------------------------------------

    @Test
    fun retrievesOnlyAnsweredReportsInOrder() =
        runBlocking {

            val selfId = dao.insert(
                selfInitiatedReport()
            )

            dao.insert(unansweredReport())

            val answeredId = dao.insert(
                answeredReport()
            )

            val reports = dao.getAnsweredReports()

            assertEquals(2, reports.size)

            assertEquals(
                listOf(answeredId, selfId),
                reports.map { it.id }
            )

            assertTrue(
                reports.all {
                    it.respondedAt != null &&
                            it.kssScore != null
                }
            )

            val allReports = dao.getAll()

            assertEquals(3, allReports.size)

            assertEquals(
                listOf(300_000L, 300_000L, 600_000L),
                allReports.map {
                    it.promptedAt ?: it.respondedAt
                }
            )
        }

    // -----------------------------------------
    // Test 9: Time range and response counts
    // -----------------------------------------

    @Test
    fun retrievesReportsInRangeAndCountsPrompts() =
        runBlocking {

            dao.insert(answeredReport())

            dao.insert(unansweredReport())

            dao.insert(selfInitiatedReport())

            dao.insert(
                answeredReport().copy(
                    promptedAt = 900_000L,
                    respondedAt = 960_000L
                )
            )

            val reports =
                dao.getAnsweredReportsInRange(
                    fromInclusive = 360_000L,
                    toExclusive = 960_000L
                )

            assertEquals(
                listOf(360_000L, 600_000L),
                reports.map { it.respondedAt }
            )

            assertEquals(4, dao.count())

            assertEquals(
                3,
                dao.countScheduledPrompts()
            )

            assertEquals(
                2,
                dao.countAnsweredScheduledPrompts()
            )
        }

    // -----------------------------------------
    // Test 10: Row deletion
    // -----------------------------------------

    @Test
    fun deletesAllFatigueReports() =
        runBlocking {

            val id = dao.insert(answeredReport())

            dao.insert(unansweredReport())

            dao.insert(selfInitiatedReport())

            assertEquals(3, dao.count())

            val deletedRows = dao.deleteAll()

            assertEquals(3, deletedRows)

            assertEquals(0, dao.count())
            assertEquals(0, dao.countScheduledPrompts())
            assertEquals(0, dao.countAnsweredScheduledPrompts())

            assertTrue(dao.getAll().isEmpty())
            assertTrue(dao.getAnsweredReports().isEmpty())

            assertNull(dao.getById(id))
        }
}
