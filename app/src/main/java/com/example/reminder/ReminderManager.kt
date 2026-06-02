package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.reminder.data.Period
import com.example.reminder.data.ReminderPolicy
import com.example.reminder.data.ScheduledReminder
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderManager(private val context: Context) {

    /**
     * Checks for scheduling conflicts and shifts the time forward by 2-minute increments
     * until a free slot is found. This prevents multiple reminders from firing simultaneously.
     */
    suspend fun resolveConflicts(db: com.example.reminder.data.ReminderDatabase, requestedTime: LocalDateTime): LocalDateTime {
        var freeTime = requestedTime
        while (db.reminderDao().hasReminderAtTime(freeTime)) {
            freeTime = freeTime.plusMinutes(2)
        }
        return freeTime
    }

    /**
     * Calculates when the next instance of a recurring reminder should happen.
     * Uses [lastTime] as the base to ensure that daily/weekly/etc. patterns
     * remain consistent even if the user interacts with the reminder late.
     */
    fun scheduleNextOccurrence(policy: ReminderPolicy, lastTime: LocalDateTime): ScheduledReminder {
        val nextTime = when (policy.period) {
            Period.ONE_TIME -> lastTime
            Period.DAILY -> lastTime.plusDays(1)
            Period.WEEKLY -> lastTime.plusWeeks(1)
            Period.MONTHLY -> lastTime.plusMonths(1)
            Period.ANNUALLY -> lastTime.plusYears(1)
        }
        return ScheduledReminder(policyId = policy.id, scheduledTime = nextTime)
    }

    /**
     * Registers a precise system alarm for a specific reminder.
     * Uses AlarmClockInfo to ensure the alarm fires even during device sleep/Doze mode.
     */
    fun setAlarm(reminder: ScheduledReminder, policy: ReminderPolicy) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("policy_id", policy.id)
            putExtra("title", policy.title)
            putExtra("message", policy.message)
            putExtra("type", policy.type.name)
            // Carrying the scheduled time allows the receiver to accurately 
            // calculate the NEXT day/week starting from the intended time.
            putExtra("scheduled_time", reminder.scheduledTime.toString())
            // Unique action strings prevent the OS from merging different reminders.
            action = "com.example.reminder.ALARM_ACTION_${reminder.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = reminder.scheduledTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Fallback to high-priority non-exact alarm.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                // setAlarmClock is the most reliable method for user-facing alerts.
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                alarmManager.setAlarmClock(info, pendingIntent)
            }
        } catch (e: SecurityException) {
            // Permission was revoked at runtime.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (e: Exception) {
            // General safety catch.
        }
    }

    /**
     * Sets a 5-minute timer that will automatically trigger the nag feature
     * if the user doesn't interact with the notification.
     */
    fun setWatchdogTimer(reminderId: Int, policyId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WatchdogReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("policy_id", policyId)
            action = "com.example.reminder.WATCHDOG_ACTION_$reminderId"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId + 10000, // Offset to avoid conflict with main alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + (5 * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    /**
     * Cancels the 5-minute response window timer.
     */
    fun cancelWatchdogTimer(reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WatchdogReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAlarm(reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.reminder.ALARM_ACTION_$reminderId"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
