package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminder.data.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Automates the Nag feature if the user hasn't responded to a notification within 5 minutes.
 */
class WatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val policyId = intent.getIntExtra("policy_id", -1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val db = ReminderDatabase.getDatabase(context)
        val manager = ReminderManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch current state of this specific reminder.
                val reminder = db.reminderDao().getPendingReminders().first().find { it.id == reminderId }
                
                // If it's still 'Activated' (unanswered) and hasn't been completed yet.
                if (reminder != null && reminder.isActivated && !reminder.isCompleted) {
                    val policy = db.reminderDao().getPolicyById(policyId)
                    if (policy != null) {
                        // 1. Clear current notification.
                        notificationManager.cancel(reminderId)
                        
                        // 2. Log that watchdog is taking over.
                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "PASSED",
                            policyTitle = policy.title,
                            details = "Watchdog: No response for 5 mins. Engaging auto-nag."
                        ))

                        // 3. Reschedule as a Nag.
                        val nagMinutes = policy.nagIntervalMinutes ?: 15
                        val nextNagTime = manager.resolveConflicts(db, LocalDateTime.now().plusMinutes(nagMinutes.toLong()))
                        
                        val updatedReminder = reminder.copy(
                            scheduledTime = nextNagTime,
                            isActivated = false,
                            activationTime = null
                        )
                        db.reminderDao().updateScheduledReminder(updatedReminder)
                        manager.setAlarm(updatedReminder, policy)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Watchdog", "Error in timeout logic", e)
            }
        }
    }
}
