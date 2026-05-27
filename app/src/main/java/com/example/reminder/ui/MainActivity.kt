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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PolicyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

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
                    val next = db.reminderDao().getNextScheduledReminder(policy.id)
                    if (next != null) {
                        db.reminderDao().deleteScheduledReminder(next.id)

                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "SKIPPED",
                            policyTitle = policy.title,
                            details = "Skipped occurrence scheduled for ${next.scheduledTime}"
                        ))
                        
                        if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                            val nextOccurrence = manager.scheduleNextOccurrence(policy, next.scheduledTime)
                            val newId = db.reminderDao().insertScheduledReminder(nextOccurrence)
                            manager.setAlarm(nextOccurrence.copy(id = newId.toInt()), policy)
                        } else {
                            // ONE_TIME skipped, delete policy
                            db.reminderDao().deletePolicy(policy.id)
                        }
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
                // Fetch reminders that were supposed to trigger while the app was closed.
                val oldReminders = db.reminderDao().getOldUncompletedReminders(now)
                for (old in oldReminders) {
                    db.reminderDao().deleteScheduledReminder(old.id)
                    val policy = db.reminderDao().getPolicyById(old.policyId)

                    if (policy != null) {
                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "MISSED",
                            policyTitle = policy.title,
                            details = "Scheduled for ${old.scheduledTime}, cleaned up at $now"
                        ))
                    }

                    // If recurring, automatically catch up by scheduling the next instance.
                    if (policy != null && policy.period != com.example.reminder.data.Period.ONE_TIME) {
                        val next = manager.scheduleNextOccurrence(policy, old.scheduledTime)
                        val newId = db.reminderDao().insertScheduledReminder(next)
                        manager.setAlarm(next.copy(id = newId.toInt()), policy)
                    } else if (policy != null) {
                        // If one-time and missed, clean up the obsolete policy.
                        db.reminderDao().deletePolicy(policy.id)
                        db.reminderDao().deleteScheduledRemindersByPolicy(policy.id)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error in cleanup", e)
            }

            // Real-time listener for database changes to update the UI list.
            db.reminderDao().getAllPoliciesWithNextReminder().collectLatest { policies ->
                try {
                    val activePolicies = policies.filter { item ->
                        val next = item.nextReminder
                        // Only show policies that have a pending future reminder.
                        next != null || item.scheduledReminders.isEmpty()
                    }.sortedBy { it.nextReminder?.scheduledTime ?: LocalDateTime.MAX } // Keep in chronological order.

                    adapter.submitList(activePolicies)
                    binding.textviewEmpty.visibility = if (activePolicies.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    
                    // Background cleanup for policies that have finished their schedule.
                    val toDelete = policies.filter { it.nextReminder == null && it.scheduledReminders.isNotEmpty() }
                    if (toDelete.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            toDelete.forEach { 
                                db.reminderDao().deletePolicy(it.policy.id)
                                db.reminderDao().deleteScheduledRemindersByPolicy(it.policy.id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error updating UI", e)
                }
            }
        }
    }

    private fun checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Check if app can bypass battery optimization for more reliable alarms.
        val powerManager = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = android.net.Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Settings page not found on some devices.
            }
        }
    }
}
