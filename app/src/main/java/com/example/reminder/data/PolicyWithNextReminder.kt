package com.example.reminder.data

import androidx.room.Embedded
import androidx.room.Relation
import java.time.LocalDateTime

data class PolicyWithNextReminder(
    @Embedded val policy: ReminderPolicy,
    @Relation(
        parentColumn = "id",
        entityColumn = "policyId"
    )
    val scheduledReminders: List<ScheduledReminder>
) {
    val nextReminder: ScheduledReminder?
        get() = scheduledReminders
            .filter { !it.isCompleted }
            .sortedBy { it.scheduledTime }
            .firstOrNull()
}
