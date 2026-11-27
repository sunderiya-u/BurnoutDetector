
package com.sunderiya.burnoutdetector.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sunderiya.burnoutdetector.data.local.dao.InteractionWindowDao
import com.sunderiya.burnoutdetector.data.local.entity.InteractionWindow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InteractionWindowDaoTest {

    private lateinit var database: BurnoutDatabase

    private lateinit var dao: InteractionWindowDao

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

        dao = database.interactionWindowDao()
    }

    @After
    fun tearDown() {

        database.close()
    }

    private fun sampleWindow(
        startTime: Long = 300_000L
    ) = InteractionWindow(

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
    // Test 1: Empty database
    // -----------------------------------------

    @Test
    fun databaseInitiallyEmpty() = runBlocking {

        assertEquals(0, dao.count())
        assertEquals(0, dao.countComplete())
        assertTrue(dao.getAll().isEmpty())
        assertNull(dao.getLatest())
    }

    // -----------------------------------------
    // Test 2: Insert and retrieve
    // -----------------------------------------

    @Test
    fun insertsAndRetrievesWindow() = runBlocking {

        val original = sampleWindow()

        val id = dao.insert(original)

        assertTrue(id > 0L)

        val stored = dao.getByStartTime(
            original.startTime
        )

        assertNotNull(stored)

        assertEquals(
            original.copy(id = id),
            stored
        )

        assertEquals(1, dao.count())
    }

    // -----------------------------------------
    // Test 3: Duplicate prevention
    // -----------------------------------------

    @Test
    fun ignoresDuplicateStartTime() = runBlocking {

        val original = sampleWindow()

        val firstId = dao.insert(original)

        val duplicateId = dao.insert(
            original.copy(appSwitchCount = 99)
        )

        assertTrue(firstId > 0L)
        assertEquals(-1L, duplicateId)
        assertEquals(1, dao.count())

        val stored = dao.getByStartTime(
            original.startTime
        )

        assertEquals(
            original.copy(id = firstId),
            stored
        )
    }

    // -----------------------------------------
    // Test 4: Nullable measurements
    // -----------------------------------------

    @Test
    fun preservesSuppressedAndUnavailableFields() =
        runBlocking {

            val window = sampleWindow().copy(

                typedCharCount = null,
                deletionCount = null,
                replacementCount = null,
                activeTypingMs = null,
                typingSpeed = null,
                correctionRatio = null,

                scrollGestureCount = 1,
                scrollVariability = null,
                scrollDistancePx = null
            )

            val id = dao.insert(window)

            val stored = requireNotNull(
                dao.getByStartTime(window.startTime)
            )

            assertEquals(
                window.copy(id = id),
                stored
            )

            assertNull(stored.typedCharCount)
            assertNull(stored.deletionCount)
            assertNull(stored.replacementCount)
            assertNull(stored.activeTypingMs)
            assertNull(stored.typingSpeed)
            assertNull(stored.correctionRatio)
            assertNull(stored.scrollVariability)
            assertNull(stored.scrollDistancePx)
        }

    // -----------------------------------------
    // Test 5: Chronological ordering
    // -----------------------------------------

    @Test
    fun retrievesWindowsInChronologicalOrder() =
        runBlocking {

            dao.insert(sampleWindow(900_000L))
            dao.insert(sampleWindow(300_000L))
            dao.insert(sampleWindow(600_000L))

            val startTimes = dao.getAll().map {
                it.startTime
            }

            assertEquals(
                listOf(
                    300_000L,
                    600_000L,
                    900_000L
                ),
                startTimes
            )
        }

    // -----------------------------------------
    // Test 6: Latest window
    // -----------------------------------------

    @Test
    fun retrievesLatestChronologicalWindow() =
        runBlocking {

            dao.insert(sampleWindow(900_000L))
            dao.insert(sampleWindow(300_000L))
            dao.insert(sampleWindow(600_000L))

            val latest = requireNotNull(
                dao.getLatest()
            )

            assertEquals(
                900_000L,
                latest.startTime
            )
        }

    // -----------------------------------------
    // Test 7: Complete and partial windows
    // -----------------------------------------

    @Test
    fun distinguishesCompleteAndPartialWindows() =
        runBlocking {

            dao.insert(
                sampleWindow(300_000L)
            )

            dao.insert(
                sampleWindow(600_000L).copy(
                    monitoredMs = 120_000L,
                    isComplete = false
                )
            )

            assertEquals(2, dao.count())
            assertEquals(1, dao.countComplete())

            val partial = requireNotNull(
                dao.getByStartTime(600_000L)
            )

            assertFalse(partial.isComplete)

            assertEquals(
                120_000L,
                partial.monitoredMs
            )
        }

    // -----------------------------------------
    // Test 8: Observation period boundaries
    // -----------------------------------------

    @Test
    fun retrievesWindowsUsingEndTimeBoundaries() =
        runBlocking {

            dao.insert(sampleWindow(0L))
            dao.insert(sampleWindow(300_000L))
            dao.insert(sampleWindow(600_000L))
            dao.insert(sampleWindow(900_000L))

            val windows = dao.getWindowsByEndTime(
                fromExclusive = 300_000L,
                toInclusive = 900_000L
            )

            assertEquals(
                listOf(
                    600_000L,
                    900_000L
                ),
                windows.map { it.endTime }
            )
        }

    // -----------------------------------------
    // Test 9: Complete window filtering
    // -----------------------------------------

    @Test
    fun retrievesOnlyCompleteWindowsInRange() =
        runBlocking {

            dao.insert(
                sampleWindow(300_000L)
            )

            dao.insert(
                sampleWindow(600_000L).copy(
                    monitoredMs = 120_000L,
                    isComplete = false
                )
            )

            dao.insert(
                sampleWindow(900_000L)
            )

            val windows =
                dao.getCompleteWindowsByEndTime(
                    fromExclusive = 300_000L,
                    toInclusive = 1_200_000L
                )

            assertEquals(2, windows.size)

            assertTrue(
                windows.all { it.isComplete }
            )

            assertEquals(
                listOf(
                    300_000L,
                    900_000L
                ),
                windows.map { it.startTime }
            )
        }

    // -----------------------------------------
    // Test 10: Row deletion
    // -----------------------------------------

    @Test
    fun deletesAllInteractionWindows() =
        runBlocking {

            dao.insert(sampleWindow(300_000L))
            dao.insert(sampleWindow(600_000L))
            dao.insert(sampleWindow(900_000L))

            assertEquals(3, dao.count())

            val deletedRows = dao.deleteAll()

            assertEquals(3, deletedRows)
            assertEquals(0, dao.count())
            assertTrue(dao.getAll().isEmpty())
            assertNull(dao.getLatest())
            assertNull(dao.getByStartTime(300_000L))
        }
}
