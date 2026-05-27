package com.example.reminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "scheduled_reminders")
data class ScheduledReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val policyId: Int,
    val scheduledTime: LocalDateTime,
    val isCompleted: Boolean = false
)
