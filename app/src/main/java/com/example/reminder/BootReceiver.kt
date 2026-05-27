package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminder.data.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Reschedules all pending alarms when the device is rebooted.
 * Android OS clears all AlarmManager registrations on shutdown.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = ReminderDatabase.getDatabase(context)
            val manager = ReminderManager(context)
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Fetch only uncompleted future reminders.
                    val pendingReminders = db.reminderDao().getPendingReminders().first()
                    for (reminder in pendingReminders) {
                        val policy = db.reminderDao().getPolicyById(reminder.policyId)
                        if (policy != null) {
                            manager.setAlarm(reminder, policy)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
