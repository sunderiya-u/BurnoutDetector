
package com.sunderiya.burnoutdetector.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sunderiya.burnoutdetector.aggregation.WindowSummary
import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport
import com.sunderiya.burnoutdetector.data.repository.BurnoutRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DatabasePersistenceTest {

    private lateinit var context: Context

    private lateinit var databaseName: String

    private var database: BurnoutDatabase? = null

    @Before
    fun setup() {

        context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        databaseName =
            "burnout_test_${UUID.randomUUID()}.db"
    }

    @After
    fun tearDown() {

        closeDatabase()

        context.deleteDatabase(databaseName)
    }

    private fun openDatabase(): BurnoutRepository {

        check(database == null)

        val openedDatabase = Room.databaseBuilder(
            context,
            BurnoutDatabase::class.java,
            databaseName
        ).build()

        database = openedDatabase

        return BurnoutRepository(
            windowDao =
                openedDatabase.interactionWindowDao(),
            fatigueDao =
                openedDatabase.fatigueReportDao()
        )
    }

    private fun closeDatabase() {

        database?.close()

        database = null
    }

    private fun sampleSummary() = WindowSummary(

        startTime = 300_000L,
        endTime = 600_000L,
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

    private fun sampleReport() = FatigueReport(

        promptedAt = 600_000L,
        respondedAt = 660_000L,
        utcOffsetMinutes = 60,
        kssScore = 7
    )

    // -----------------------------------------
    // DB29: Physical database file creation
    // -----------------------------------------

    @Test
    fun createsDatabaseFileInPrivateStorage() =
        runBlocking {

            val repository = openDatabase()

            repository.saveWindow(sampleSummary())

            val databaseFile =
                context.getDatabasePath(databaseName)

            assertTrue(databaseFile.exists())

            assertTrue(databaseFile.isFile)

            assertTrue(databaseFile.length() > 0L)

            val privateDirectory =
                File(context.applicationInfo.dataDir)
                    .canonicalFile

            assertTrue(
                databaseFile.canonicalPath.startsWith(
                    privateDirectory.path + File.separator
                )
            )
        }

    // -----------------------------------------
    // DB30: Persistence after reopening
    // -----------------------------------------

    @Test
    fun preservesRecordsAfterClosingAndReopening() =
        runBlocking {

            val repository = openDatabase()

            val windowId =
                repository.saveWindow(sampleSummary())

            val reportId =
                repository.saveFatigueReport(sampleReport())

            val originalWindow =
                repository.getWindowByStartTime(300_000L)

            val originalReport =
                repository.getFatigueReportById(reportId)

            assertNotNull(originalWindow)

            assertNotNull(originalReport)

            closeDatabase()

            val reopenedRepository = openDatabase()

            assertEquals(
                1,
                reopenedRepository.countWindows()
            )

            assertEquals(
                1,
                reopenedRepository.countFatigueReports()
            )

            val storedWindow =
                reopenedRepository.getWindowByStartTime(
                    300_000L
                )

            val storedReport =
                reopenedRepository.getFatigueReportById(
                    reportId
                )

            assertEquals(originalWindow, storedWindow)

            assertEquals(originalReport, storedReport)

            assertEquals(
                windowId,
                requireNotNull(storedWindow).id
            )
        }

    // -----------------------------------------
    // DB31: Persistent unique constraint
    // -----------------------------------------

    @Test
    fun preventsDuplicatesAfterReopening() =
        runBlocking {

            val repository = openDatabase()

            val originalId =
                repository.saveWindow(sampleSummary())

            closeDatabase()

            val reopenedRepository = openDatabase()

            val duplicateId =
                reopenedRepository.saveWindow(
                    sampleSummary().copy(
                        appSwitchCount = 99
                    )
                )

            assertEquals(-1L, duplicateId)

            assertEquals(
                1,
                reopenedRepository.countWindows()
            )

            val stored = requireNotNull(
                reopenedRepository.getWindowByStartTime(
                    300_000L
                )
            )

            assertEquals(originalId, stored.id)

            assertEquals(3, stored.appSwitchCount)
        }

    // -----------------------------------------
    // DB32: Database file deletion
    // -----------------------------------------

    @Test
    fun deletesDatabaseAndAssociatedFiles() =
        runBlocking {

            val repository = openDatabase()

            repository.saveWindow(sampleSummary())

            repository.saveFatigueReport(sampleReport())

            val databaseFile =
                context.getDatabasePath(databaseName)

            assertTrue(databaseFile.exists())

            closeDatabase()

            val deleted =
                context.deleteDatabase(databaseName)

            assertTrue(deleted)

            assertFalse(databaseFile.exists())

            assertFalse(
                File(databaseFile.path + "-wal").exists()
            )

            assertFalse(
                File(databaseFile.path + "-shm").exists()
            )

            assertFalse(
                File(databaseFile.path + "-journal").exists()
            )
        }

    // -----------------------------------------
    // DB33: Clean database after deletion
    // -----------------------------------------

    @Test
    fun createsEmptyDatabaseAfterDeletion() =
        runBlocking {

            val repository = openDatabase()

            repository.saveWindow(sampleSummary())

            repository.saveFatigueReport(sampleReport())

            assertEquals(1, repository.countWindows())

            assertEquals(
                1,
                repository.countFatigueReports()
            )

            closeDatabase()

            assertTrue(
                context.deleteDatabase(databaseName)
            )

            val freshRepository = openDatabase()

            assertEquals(
                0,
                freshRepository.countWindows()
            )

            assertEquals(
                0,
                freshRepository.countFatigueReports()
            )

            assertTrue(
                freshRepository.getAllWindows().isEmpty()
            )

            assertTrue(
                freshRepository.getAllFatigueReports()
                    .isEmpty()
            )
        }
}
