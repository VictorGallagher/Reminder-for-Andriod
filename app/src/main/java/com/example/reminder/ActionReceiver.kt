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
        val scheduledTimeStr = intent.getStringExtra("scheduled_time")

        android.util.Log.d("ActionReceiver", "User interacted: $action for reminder ID: $reminderId")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Immediately dismiss the notification once any button is pressed.
        notificationManager.cancel(reminderId)

        val db = ReminderDatabase.getDatabase(context)
        val manager = ReminderManager(context)
        val scheduler = Scheduler(context)
        
        // Response received: cancel the automatic 5-minute watchdog timer.
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
                                details = "User pressed $logLabel. Proceeding to rollover."
                            ))

                            // If recurring, move to the next day and RESYNC to the policy time.
                            if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                                val baseTime = try {
                                    LocalDateTime.parse(scheduledTimeStr ?: "")
                                } catch (e: Exception) {
                                    LocalDateTime.now()
                                }
                                // scheduleNextStep handles deleting the current instance and scheduling the next.
                                scheduler.scheduleNextStep(policy, baseTime)
                            } else {
                                // One-time reminders are fully finished now.
                                db.reminderDao().deleteScheduledReminder(reminderId)
                                db.reminderDao().deletePolicy(policy.id)
                                db.reminderDao().deleteScheduledRemindersByPolicy(policy.id)
                            }
                        }
                        
                        // USER Postponed the reminder (NO/PASS - Engaging Nag)
                        action?.contains("ACTION_NO") == true || action?.contains("ACTION_PASS") == true -> {
                            // CRITICAL: We do NOT delete the reminder here.
                            // The scheduler will simply update its time so it can fire again.
                            scheduler.scheduleNag(policy, reminderId)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ActionReceiver", "Error processing action: $action", e)
            }
        }
    }
}
