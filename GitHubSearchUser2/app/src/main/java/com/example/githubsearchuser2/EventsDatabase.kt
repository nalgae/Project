package com.example.githubsearchuser2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

@Database(entities = [GCheckListSearchDB::class], version = 3)
@TypeConverters(Converters::class)
abstract class EventsDatabase : RoomDatabase() {

    abstract fun EventTypesDao(): EventTypesDao

    companion object {
        private var db: EventsDatabase? = null

        fun getInstance(context: Context): EventsDatabase {
            if (db == null) {
                synchronized(EventsDatabase::class) {
                    if (db == null) {
                        db = Room.databaseBuilder(context.applicationContext, EventsDatabase::class.java, "events.db")
                                .addCallback(object : Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        //insertRegularEventType(context)
                                    }
                                })
                                //.addMigrations(MIGRATION_1_2)
                                //.addMigrations(MIGRATION_2_3)
                                .build()
                        db!!.openHelper.setWriteAheadLoggingEnabled(true)
                    }
                }
            }
            return db!!
        }

        fun destroyInstance() {
            db = null
        }

        private fun insertRegularEventType(context: Context) {
            Executors.newSingleThreadScheduledExecutor().execute {
                //val regularEvent = context.resources.getString(R.string.regular_event)
//                val checkListSearchDB = GCheckListSearchDB(0L, "","","","","","","","","","","","","","","","")
                //val checkListSearchDB = GCheckListSearchDB(eventType)
//                db!!.EventTypesDao().insertOrUpdate(checkListSearchDB)
                //context.config.addDisplayEventType(REGULAR_EVENT_TYPE_ID.toString())
            }
        }

        /*private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.apply {
                    execSQL("ALTER TABLE events ADD COLUMN reminder_1_type INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE events ADD COLUMN reminder_2_type INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE events ADD COLUMN reminder_3_type INTEGER NOT NULL DEFAULT 0")
                    execSQL("ALTER TABLE events ADD COLUMN attendees TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.apply {
                    execSQL("ALTER TABLE events ADD COLUMN time_zone TEXT NOT NULL DEFAULT ''")
                }
            }
        }*/
    }
}
