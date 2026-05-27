package com.example.reminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class Period {
    ONE_TIME, DAILY, WEEKLY, MONTHLY, ANNUALLY
}

enum class ReminderType {
    NOTIFICATION, QUESTION
}

@Entity(tableName = "reminder_policies")
data class ReminderPolicy(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val startDate: LocalDateTime,
    val period: Period,
    val type: ReminderType,
    val nagIntervalMinutes: Int? = null,
    val customRingtoneUri: String? = null
)
