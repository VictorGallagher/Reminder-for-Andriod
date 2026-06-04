package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.reminder.data.ReminderPolicy
import com.example.reminder.data.ScheduledReminder
import java.time.ZoneId

/**
 * Handles the low-level Android System interactions for Alarms.
 * Responsible for registering and canceling alarms with the OS.
 */
class ReminderManager(private val context: Context) {

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
            putExtra("scheduled_time", reminder.scheduledTime.toString())
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
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                alarmManager.setAlarmClock(info, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (e: Exception) {
            // General safety catch.
        }
    }

    /**
     * Cancels an existing system alarm.
     */
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
            reminderId + 10000, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + (5 * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

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
}
