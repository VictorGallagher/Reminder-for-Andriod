package com.example.reminder.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reminder.data.PolicyWithNextReminder
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Main dashboard showing the list of active reminder policies and their next trigger times.
 * Handles background cleanup and catch-up logic for missed reminders.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PolicyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Basic notification permission for Android 13+.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val db = ReminderDatabase.getDatabase(this)
        val manager = com.example.reminder.ReminderManager(this)

        adapter = PolicyAdapter(
            onPolicyClick = { policy ->
                val intent = Intent(this, AddPolicyActivity::class.java).apply {
                    putExtra("policy_id", policy.id)
                }
                startActivity(intent)
            },
            onSkipClick = { policy ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val next = db.reminderDao().getNextScheduledReminder(policy.id)
                        if (next != null) {
                            db.reminderDao().deleteScheduledReminder(next.id)

                            db.logDao().insertLog(com.example.reminder.data.LogEntry(
                                eventType = "SKIPPED",
                                policyTitle = policy.title,
                                details = "Skipped occurrence scheduled for ${next.scheduledTime}"
                            ))
                            
                            if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                                // Calculate next occurrence relative to the one we just skipped.
                                val nextOccurrenceRaw = manager.scheduleNextOccurrence(policy, next.scheduledTime)
                                val finalTime = manager.resolveConflicts(db, nextOccurrenceRaw.scheduledTime)
                                val nextOccurrence = nextOccurrenceRaw.copy(scheduledTime = finalTime)

                                val newId = db.reminderDao().insertScheduledReminder(nextOccurrence)
                                manager.setAlarm(nextOccurrence.copy(id = newId.toInt()), policy)
                            } else {
                                // If it was a one-time reminder, skipping it makes the policy obsolete.
                                db.reminderDao().deletePolicy(policy.id)
                                db.reminderDao().deleteScheduledRemindersByPolicy(policy.id)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error skipping reminder", e)
                    }
                }
            }
        )
        binding.recyclerviewPolicies.layoutManager = LinearLayoutManager(this)
        binding.recyclerviewPolicies.adapter = adapter

        binding.fabAddPolicy.setOnClickListener {
            startActivity(Intent(this, AddPolicyActivity::class.java))
        }

        binding.fabShowLogs.setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        lifecycleScope.launch {
            try {
                val now = LocalDateTime.now()
                // EXPIRED LIMIT: 5-minute response window for active notifications.
                val expiredLimit = now.minusMinutes(5)
                
                // Fetch reminders that were supposed to trigger while the app was closed,
                // OR reminders that have been ringing for more than 5 minutes without response.
                val oldReminders = db.reminderDao().getOldUncompletedReminders(now, expiredLimit)
                
                // Track which policies we've already caught up for to prevent duplication.
                val processedPolicies = mutableSetOf<Int>()

                for (old in oldReminders) {
                    db.reminderDao().deleteScheduledReminder(old.id)
                    // If it was ringing, cancel the stale notification and watchdog timer.
                    if (old.isActivated) {
                        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        notificationManager.cancel(old.id)
                        manager.cancelWatchdogTimer(old.id)
                    }
                    
                    if (old.policyId in processedPolicies) continue
                    processedPolicies.add(old.policyId)

                    val policy = db.reminderDao().getPolicyById(old.policyId)
                    if (policy != null) {
                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "MISSED",
                            policyTitle = policy.title,
                            details = "Catch-up: Handling missed/expired reminder from ${old.scheduledTime}"
                        ))

                        if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                            // Schedule the next occurrence relative to NOW, not the old time,
                            // to prevent a chain reaction of past alarms.
                            val nextRaw = manager.scheduleNextOccurrence(policy, now)
                            val finalTime = manager.resolveConflicts(db, nextRaw.scheduledTime)
                            val next = nextRaw.copy(scheduledTime = finalTime)

                            val newId = db.reminderDao().insertScheduledReminder(next)
                            manager.setAlarm(next.copy(id = newId.toInt()), policy)
                        } else {
                            // One-time missed reminders result in policy cleanup.
                            db.reminderDao().deletePolicy(policy.id)
                            db.reminderDao().deleteScheduledRemindersByPolicy(policy.id)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error in cleanup", e)
            }

            // Real-time listener for database changes to update the UI list.
            db.reminderDao().getAllPoliciesWithNextReminder().collectLatest { policies ->
                try {
                    // Only show policies that are still "active" (have pending future reminders).
                    val activePolicies = policies.filter { item ->
                        item.nextReminder != null
                    }.sortedBy { it.nextReminder?.scheduledTime ?: LocalDateTime.MAX }

                    adapter.submitList(activePolicies)
                    binding.textviewEmpty.visibility = if (activePolicies.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error updating UI", e)
                }
            }
        }
    }
}
