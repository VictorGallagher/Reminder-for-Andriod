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

        // Exact alarms require special permission on Android 12+.
        // We fallback to high-priority non-exact if permission is missing.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                // setAlarmClock is the most reliable method for user-facing alerts.
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                alarmManager.setAlarmClock(info, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
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
