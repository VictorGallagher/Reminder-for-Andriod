package com.example.reminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "event_logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val eventType: String, // CREATED, ACTIVATED, PASSED, COMPLETED, SKIPPED, MISSED
    val policyTitle: String,
    val details: String? = null
)
