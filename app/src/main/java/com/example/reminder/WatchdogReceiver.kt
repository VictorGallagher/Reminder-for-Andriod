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

/**
 * Automates the Nag feature if the user hasn't responded to a notification within 5 minutes.
 */
class WatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val policyId = intent.getIntExtra("policy_id", -1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val db = ReminderDatabase.getDatabase(context)
        val scheduler = Scheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = db.reminderDao().getPendingReminders().first().find { it.id == reminderId }
                
                if (reminder != null && reminder.isActivated && !reminder.isCompleted) {
                    val policy = db.reminderDao().getPolicyById(policyId)
                    if (policy != null) {
                        notificationManager.cancel(reminderId)
                        
                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "PASSED",
                            policyTitle = policy.title,
                            details = "Watchdog: No response for 5 mins. Engaging auto-nag."
                        ))

                        scheduler.scheduleNag(policy, reminderId)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Watchdog", "Error in timeout logic", e)
            }
        }
    }
}
