
package com.sunderiya.burnoutdetector.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sunderiya.burnoutdetector.data.local.dao.FatigueReportDao
import com.sunderiya.burnoutdetector.data.local.dao.InteractionWindowDao
import com.sunderiya.burnoutdetector.data.local.entity.FatigueReport
import com.sunderiya.burnoutdetector.data.local.entity.InteractionWindow

/**
 * Local Room database for aggregated behavioral
 * measurements and self-reported assessments.
 *
 * The database is stored in application-private storage.
 * No individual interaction events are persisted.
 */
@Database(
    entities = [
        InteractionWindow::class,
        FatigueReport::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BurnoutDatabase : RoomDatabase() {

    abstract fun interactionWindowDao(): InteractionWindowDao

    abstract fun fatigueReportDao(): FatigueReportDao

    companion object {

        const val DATABASE_NAME = "burnout_detector.db"

        @Volatile
        private var INSTANCE: BurnoutDatabase? = null

        /**
         * Returns the shared database instance.
         */
        fun getInstance(context: Context): BurnoutDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BurnoutDatabase::class.java,
                    DATABASE_NAME
                )
                    .build()
                    .also { database ->
                        INSTANCE = database
                    }
            }
        }

        /**
         * Closes the current database instance.
         *
         * The caller must stop and synchronize all
         * database operations before calling this method.
         */
        @Synchronized
        fun closeInstance() {

            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
