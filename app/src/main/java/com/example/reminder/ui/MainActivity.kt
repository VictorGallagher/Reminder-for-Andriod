package com.example.reminder.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reminder.ReminderManager
import com.example.reminder.Scheduler
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Main dashboard showing the list of active reminder policies and their next trigger times.
 * 
 * DESIGN CONSTRAINTS & DOCUMENTATION:
 * 1. RESOLVED RACE CONDITIONS: Implements a 1-minute safety buffer in catch-up logic to 
 *    prevent "stealing" an active alarm from the system.
 * 2. STARTUP SANITY CHECK: Automatically detects and deletes "stacked" duplicate 
 *    reminders on app launch, ensuring only one future instance exists per policy.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PolicyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val db = ReminderDatabase.getDatabase(this)
        val manager = ReminderManager(this)
        val scheduler = Scheduler(this)

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
                            db.logDao().insertLog(com.example.reminder.data.LogEntry(
                                eventType = "SKIPPED",
                                policyTitle = policy.title,
                                details = "Skipped occurrence scheduled for ${next.scheduledTime}"
                            ))
                            
                            if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                                scheduler.scheduleNextStep(policy, next.scheduledTime)
                            } else {
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
                // 1. PERFORM SANITY CHECK (One-time on startup)
                // This removes any "stacked" duplicate reminders that might have been caused by previous bugs.
                val allPolicies = db.reminderDao().getAllPoliciesWithNextReminder().first()
                allPolicies.forEach { item ->
                    val pending = item.scheduledReminders.filter { !it.isCompleted }
                    if (pending.size > 1) {
                        // Keep only the one furthest in the future, delete the rest.
                        val sorted = pending.sortedByDescending { it.scheduledTime }
                        val toDelete = sorted.drop(1)
                        toDelete.forEach { db.reminderDao().deleteScheduledReminder(it.id) }
                        
                        db.logDao().insertLog(com.example.reminder.data.LogEntry(
                            eventType = "SKIPPED",
                            policyTitle = item.policy.title,
                            details = "Startup Sanity Check: Removed ${toDelete.size} duplicate reminders."
                        ))
                    }
                }

                // 2. CATCH-UP LOGIC (Handle missed reminders)
                val now = LocalDateTime.now()
                // 1-MINUTE SAFETY BUFFER: Only catch up on reminders more than 60 seconds old.
                // This prevents the app from "stealing" a reminder that is currently triggering.
                val catchUpThreshold = now.minusSeconds(60)
                // 5-MINUTE EXPIRED LIMIT: For reminders that have been ringing but never answered.
                val expiredLimit = now.minusMinutes(5)
                
                val oldReminders = db.reminderDao().getOldUncompletedReminders(catchUpThreshold, expiredLimit)
                val processedPolicies = mutableSetOf<Int>()

                for (old in oldReminders) {
                    db.reminderDao().deleteScheduledReminder(old.id)
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
                            details = "Catch-up: Handled missed reminder from ${old.scheduledTime}"
                        ))

                        if (policy.period != com.example.reminder.data.Period.ONE_TIME) {
                            // Calculate next step relative to NOW to prevent backlog loops.
                            scheduler.scheduleNextStep(policy, now)
                        } else {
                            db.reminderDao().deletePolicy(policy.id)
                            db.reminderDao().deleteScheduledRemindersByPolicy(policy.id)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error in startup cleanup", e)
            }

            // 3. UI UPDATER (Continuous listener)
            db.reminderDao().getAllPoliciesWithNextReminder().collectLatest { policies ->
                try {
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
