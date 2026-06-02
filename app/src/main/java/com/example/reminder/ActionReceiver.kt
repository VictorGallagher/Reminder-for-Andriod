package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.data.ScheduledReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Handles user interactions with reminder notifications (buttons like YES, NO, OK, PASS).
 */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val policyId = intent.getIntExtra("policy_id", -1)
        // scheduled_time is passed from the alarm to ensure next-day/week calculations
        // are based on the intended time, not the time the user clicked the button.
        val scheduledTimeStr = intent.getStringExtra("scheduled_time")

        android.util.Log.d("ActionReceiver", "User interacted: $action for reminder ID: $reminderId")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Immediately dismiss the notification once any button is pressed.
        notificationManager.cancel(reminderId)

        val db = ReminderDatabase.getDatabase(context)
        val manager = ReminderManager(context)

        // Cancel the 5-minute watchdog timer since the user has interacted.
        manager.cancelWatchdogTimer(reminderId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val policy = db.reminderDao().getPolicyById(policyId)
                
                if (policy != null) {
                    when {
                        // USER Acknowledged the reminder (YES/OK)
                        action?.contains("ACTION_YES") == true || action?.contains("ACTION_OK") == true -> {
                            val logLabel = if (action.contains("YES")) "YES" else "OK"
                            db.logDao().insertLog(com.example.reminder.data.LogEntry(
                                eventType = "COMPLETED",
                                policyTitle = policy.title,
                                details = "User pressed $logLabel. Resyncing to policy time."
                            ))

                            // Deleting the current instance as it is now finished.
                            db.reminderDao().deleteScheduledReminder(reminderId)

                            // If recurring, schedule the NEXT occurrence.
                            if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                                val baseDate = try {
                                    LocalDateTime.parse(scheduledTimeStr ?: "").toLocalDate()
                                } catch (e: Exception) {
                                    LocalDateTime.now().toLocalDate()
                                }
                                
                                // Construct the intended next time using original policy time components.
                                val nextDay = baseDate.plusDays(1)
                                val syncedTime = LocalDateTime.of(nextDay, policy.startDate.toLocalTime())
                                
                                val nextReminderRaw = manager.scheduleNextOccurrence(policy, syncedTime.minusDays(1)) 
                                val finalTime = manager.resolveConflicts(db, syncedTime)
                                val nextReminder = nextReminderRaw.copy(scheduledTime = finalTime)

                                val nextId = db.reminderDao().insertScheduledReminder(nextReminder)
                                manager.setAlarm(nextReminder.copy(id = nextId.toInt()), policy)
                            } else {
                                // One-time reminders are fully finished now.
                                db.reminderDao().deletePolicy(policy.id)
                                db.reminderDao().deleteScheduledRemindersByPolicy(policy.id)
                            }
                        }
                        
                        // USER Postponed the reminder (NO/PASS - Engaging Nag)
                        action?.contains("ACTION_NO") == true || action?.contains("ACTION_PASS") == true -> {
                            val nagMinutes = policy.nagIntervalMinutes ?: 15
                            val logLabel = if (action.contains("NO")) "NO" else "PASS"
                            
                            // Instead of deleting and creating a new reminder, we UPDATE the present reminder.
                            // This ensures the ID remains the same and fulfills the user's request.
                            val rawNagTime = LocalDateTime.now().plusMinutes(nagMinutes.toLong())
                            val finalNagTime = manager.resolveConflicts(db, rawNagTime)

                            db.logDao().insertLog(com.example.reminder.data.LogEntry(
                                eventType = "PASSED",
                                policyTitle = policy.title,
                                details = "User pressed $logLabel. Updating existing reminder to nag at $finalNagTime."
                            ))

                            // Update the existing reminder record in the database.
                            // We reset 'isActivated' to 0 so it's ready to fire again.
                            val existingReminder = db.reminderDao().getPendingReminders().first().find { it.id == reminderId }
                                ?: ScheduledReminder(id = reminderId, policyId = policyId, scheduledTime = finalNagTime)
                            
                            val updatedReminder = existingReminder.copy(
                                scheduledTime = finalNagTime,
                                isActivated = false
                            )
                            db.reminderDao().updateScheduledReminder(updatedReminder)
                            
                            // Set the new alarm with the updated time.
                            manager.setAlarm(updatedReminder, policy)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ActionReceiver", "Error processing action: $action", e)
            }
        }
    }
}
