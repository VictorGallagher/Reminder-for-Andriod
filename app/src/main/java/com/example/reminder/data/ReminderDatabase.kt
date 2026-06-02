package com.example.reminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ReminderPolicy::class, ScheduledReminder::class, LogEntry::class], version = 12, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_db_v2"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
