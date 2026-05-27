package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.data.ScheduledReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val policyId = intent.getIntExtra("policy_id", -1)
        // scheduled_time is passed from the alarm to ensure next-day/week calculations
        // are based on the intended time, not the time the user clicked the button.
        val scheduledTimeStr = intent.getStringExtra("scheduled_time")

        android.util.Log.d("ActionReceiver", "Action received: $action for reminder: $reminderId")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Remove the notification once a button is pressed.
        notificationManager.cancel(reminderId)

        val db = ReminderDatabase.getDatabase(context)
        val manager = ReminderManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            val policy = db.reminderDao().getPolicyById(policyId)
            // Once processed, this specific scheduled instance is no longer needed.
            db.reminderDao().deleteScheduledReminder(reminderId)
            
            if (policy != null) {
                when (action) {
                    "ACTION_YES", "ACTION_OK" -> {
                        // User confirmed the task. If it's recurring, schedule the NEXT occurrence.
                        
                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "COMPLETED",
                            policyTitle = policy.title,
                            details = "Action: $action"
                        ))

                        if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                            val baseTime = if (scheduledTimeStr != null) {
                                try {
                                    java.time.LocalDateTime.parse(scheduledTimeStr)
                                } catch (e: Exception) {
                                    java.time.LocalDateTime.now()
                                }
                            } else {
                                java.time.LocalDateTime.now()
                            }
                            
                            val nextReminder = manager.scheduleNextOccurrence(policy, baseTime)
                            val nextId = db.reminderDao().insertScheduledReminder(nextReminder)
                            manager.setAlarm(nextReminder.copy(id = nextId.toInt()), policy)
                        } else {
                            // If it was a one-time reminder and it's done, delete the entire policy.
                            db.reminderDao().deletePolicy(policy.id)
                            db.reminderDao().deleteScheduledRemindersByPolicy(policy.id)
                        }
                    }
                    "ACTION_NO", "ACTION_PASS" -> {
                        // User postponed the task. Schedule a "Nag" reminder based on policy interval.
                        val nagMinutes = policy.nagIntervalMinutes ?: 15
                        
                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "PASSED",
                            policyTitle = policy.title,
                            details = "Action: $action, Nagging in $nagMinutes mins"
                        ))

                        val nagReminder = com.example.reminder.data.ScheduledReminder(
                            policyId = policyId,
                            scheduledTime = java.time.LocalDateTime.now().plusMinutes(nagMinutes.toLong())
                        )
                        val nagId = db.reminderDao().insertScheduledReminder(nagReminder)
                        manager.setAlarm(nagReminder.copy(id = nagId.toInt()), policy)
                    }
                }
            }
        }
    }
}
