package com.example.reminder

import android.content.Context
import com.example.reminder.data.Period
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.data.ReminderPolicy
import com.example.reminder.data.ScheduledReminder
import java.time.LocalDateTime

/**
 * Dedicated coordinator for all scheduling logic.
 * Ensures that reminders follow policy rules, resolve conflicts, 
 * and strictly maintain the "Single Active Reminder" constraint.
 * 
 * DESIGN CONSTRAINTS & DOCUMENTATION:
 * 1. SINGLE ACTIVE REMINDER ENFORCEMENT: scheduleNextStep performing a mandatory 
 *    wipe of all other pending alerts for a policy before scheduling a new one.
 * 2. AUTOMATIC POLICY RESYNC: calculateNextFutureTime uses the original policy 
 *    start time to ensure daily/weekly rollovers ignore temporary nag offsets.
 */
class Scheduler(private val context: Context) {
    private val db = ReminderDatabase.getDatabase(context)
    private val manager = ReminderManager(context)

    /**
     * Finds the very next future occurrence for a policy based on its original period.
     * This method RESYNCS the schedule to the original policy time, stripping away
     * any temporary offsets caused by nagging (snoozing).
     */
    fun calculateNextFutureTime(policy: ReminderPolicy, fromTime: LocalDateTime): LocalDateTime {
        val now = LocalDateTime.now()
        // Extract the original intended time component from the policy.
        val intendedTime = policy.startDate.toLocalTime()
        
        // Start calculation from the date of 'fromTime' but at the correct policy time.
        var nextTime = LocalDateTime.of(fromTime.toLocalDate(), intendedTime)

        // Increment based on period until we find the first occurrence in the future.
        // This handles cases where the phone was off for days.
        do {
            nextTime = when (policy.period) {
                Period.ONE_TIME -> nextTime // One-time doesn't roll over.
                Period.DAILY -> nextTime.plusDays(1)
                Period.WEEKLY -> nextTime.plusWeeks(1)
                Period.MONTHLY -> nextTime.plusMonths(1)
                Period.ANNUALLY -> nextTime.plusYears(1)
            }
        } while (nextTime.isBefore(now))
        
        return nextTime
    }

    /**
     * Shifts time in 2-minute increments to avoid overlapping alarms with other policies.
     */
    suspend fun resolveConflicts(requestedTime: LocalDateTime): LocalDateTime {
        var freeTime = requestedTime
        while (db.reminderDao().hasReminderAtTime(freeTime)) {
            freeTime = freeTime.plusMinutes(2)
        }
        return freeTime
    }

    /**
     * The primary entry point for moving a policy to its next scheduled day.
     * Enforces the "Single Active Reminder" constraint by wiping old data first.
     */
    suspend fun scheduleNextStep(policy: ReminderPolicy, baseTime: LocalDateTime) {
        // 1. Wipe all existing pending reminders for this policy to prevent piling up.
        db.reminderDao().deleteAllPendingRemindersForPolicy(policy.id)

        // 2. Calculate the next synced time slot.
        val rawTime = calculateNextFutureTime(policy, baseTime)
        val finalTime = resolveConflicts(rawTime)

        // 3. Persist and Register.
        val newReminder = ScheduledReminder(
            policyId = policy.id,
            scheduledTime = finalTime
        )
        val newId = db.reminderDao().insertScheduledReminder(newReminder)
        manager.setAlarm(newReminder.copy(id = newId.toInt()), policy)
        
        db.logDao().insertLog(com.example.reminder.data.LogEntry(
            eventType = "CREATED",
            policyTitle = policy.title,
            details = "Rollover: Synced to policy time. Next: $finalTime"
        ))
    }

    /**
     * Handles the "Nag" logic by updating the existing active reminder.
     * Does NOT touch the Policy configuration, only the current trigger instance.
     */
    suspend fun scheduleNag(policy: ReminderPolicy, reminderId: Int) {
        val nagMinutes = policy.nagIntervalMinutes ?: 15
        val rawTime = LocalDateTime.now().plusMinutes(nagMinutes.toLong())
        val finalTime = resolveConflicts(rawTime)

        // Reset activation status so it can trigger again.
        val updatedReminder = ScheduledReminder(
            id = reminderId,
            policyId = policy.id,
            scheduledTime = finalTime,
            isActivated = false,
            activationTime = null
        )
        db.reminderDao().updateScheduledReminder(updatedReminder)
        manager.setAlarm(updatedReminder, policy)
        
        db.logDao().insertLog(com.example.reminder.data.LogEntry(
            eventType = "PASSED",
            policyTitle = policy.title,
            details = "Nag engaged: Updated existing reminder to trigger at $finalTime"
        ))
    }
}
