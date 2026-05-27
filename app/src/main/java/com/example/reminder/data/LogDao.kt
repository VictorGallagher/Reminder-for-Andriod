package com.example.reminder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insertLog(log: LogEntry)

    @Query("SELECT * FROM event_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllLogs(): Flow<List<LogEntry>>

    @Query("DELETE FROM event_logs")
    suspend fun clearLogs()
}
