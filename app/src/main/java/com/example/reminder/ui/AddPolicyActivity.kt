package com.example.reminder.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.reminder.ReminderManager
import com.example.reminder.data.Period
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.data.ReminderPolicy
import com.example.reminder.data.ReminderType
import com.example.reminder.data.TodoImporter
import com.example.reminder.databinding.ActivityAddPolicyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AddPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPolicyBinding
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private var selectedRingtoneUri: String? = null
    private var editingPolicy: ReminderPolicy? = null

    private val nagIntervals = listOf(15, 30, 45, 60, 90, 120)
    private val nagIntervalLabels = listOf("15 minutes", "30 minutes", "45 minutes", "1 hour", "1.5 hours", "2 hours")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val policyId = intent.getIntExtra("policy_id", -1)
        if (policyId != -1) {
            loadPolicy(policyId)
        }

        setupSpinners()

        binding.spinnerType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val type = ReminderType.values()[position]
                binding.layoutNagInterval.visibility = if (type == ReminderType.QUESTION) android.view.View.VISIBLE else android.view.View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.buttonSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSelectTime.setOnClickListener {
            showTimePicker()
        }

        binding.buttonSelectRingtone.setOnClickListener {
            selectRingtone()
        }

        binding.buttonImportTodo.setOnClickListener {
            importFromTodo()
        }

        binding.buttonSave.setOnClickListener {
            savePolicy()
        }

        binding.buttonDeletePolicy.setOnClickListener {
            deletePolicy()
        }
    }

    private fun setupSpinners() {
        binding.spinnerPeriod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Period.values())
        binding.spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ReminderType.values())
        binding.spinnerNagInterval.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nagIntervalLabels)
    }

    private fun loadPolicy(id: Int) {
        val db = ReminderDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val policy = db.reminderDao().getPolicyById(id)
            // Fetch the actual next scheduled instance to show the user when it will next trigger.
            val nextReminder = db.reminderDao().getNextScheduledReminder(id)
            
            if (policy != null) {
                editingPolicy = policy
                launch(Dispatchers.Main) {
                    binding.edittextTitle.setText(policy.title)
                    binding.edittextMessage.setText(policy.message)
                    binding.spinnerPeriod.setSelection(Period.values().indexOf(policy.period))
                    binding.spinnerType.setSelection(ReminderType.values().indexOf(policy.type))
                    
                    if (policy.type == ReminderType.QUESTION && policy.nagIntervalMinutes != null) {
                        val index = nagIntervals.indexOf(policy.nagIntervalMinutes)
                        if (index != -1) {
                            binding.spinnerNagInterval.setSelection(index)
                        }
                    }

                    // Use the next trigger time for the UI if available, otherwise the policy's start date.
                    val displayTime = nextReminder?.scheduledTime ?: policy.startDate
                    selectedDate = displayTime.toLocalDate()
                    binding.textviewSelectedDate.text = "Date: ${selectedDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
                    selectedTime = displayTime.toLocalTime()
                    binding.textviewSelectedTime.text = "Time: ${selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    
                    selectedRingtoneUri = policy.customRingtoneUri
                    if (selectedRingtoneUri != null) {
                        try {
                            val ringtone = android.media.RingtoneManager.getRingtone(this@AddPolicyActivity, android.net.Uri.parse(selectedRingtoneUri))
                            binding.textviewSelectedRingtone.text = "Sound: ${ringtone.getTitle(this@AddPolicyActivity)}"
                        } catch (e: Exception) {
                            binding.textviewSelectedRingtone.text = "Sound: Default"
                        }
                    }

                    binding.buttonSave.text = "Update Policy"
                    binding.buttonDeletePolicy.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    private fun showDatePicker() {
        val now = LocalDate.now()
        val initialDate = selectedDate ?: now
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            binding.textviewSelectedDate.text = "Date: ${selectedDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
        }, initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth).show()
    }

    private fun deletePolicy() {
        val policyId = editingPolicy?.id ?: return
        val db = ReminderDatabase.getDatabase(this)
        
        AlertDialog.Builder(this)
            .setTitle("Delete Policy")
            .setMessage("Are you sure you want to delete this policy and all its upcoming reminders?")
            .setPositiveButton("Delete") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.reminderDao().deletePolicy(policyId)
                    db.reminderDao().deleteScheduledRemindersByPolicy(policyId)
                    launch(Dispatchers.Main) {
                        finish()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTimePicker() {
        val now = LocalTime.now()
        TimePickerDialog(this, { _, hour, minute ->
            selectedTime = LocalTime.of(hour, minute)
            binding.textviewSelectedTime.text = "Time: ${selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }, now.hour, now.minute, true).show()
    }

    private fun selectRingtone() {
        val intent = Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_ALL)
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Reminder Sound")
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri?.let { android.net.Uri.parse(it) })
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        }
        startActivityForResult(intent, 999)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == RESULT_OK) {
            val uri = data?.getParcelableExtra<android.net.Uri>(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                selectedRingtoneUri = uri.toString()
                val ringtone = android.media.RingtoneManager.getRingtone(this, uri)
                binding.textviewSelectedRingtone.text = "Sound: ${ringtone.getTitle(this)}"
            }
        }
    }

    private fun importFromTodo() {
        val importer = TodoImporter(this)
        val suggestions = try {
            importer.getSuggestedTodos()
        } catch (e: Exception) {
            Toast.makeText(this, "Error accessing To Do List. Make sure it is installed.", Toast.LENGTH_LONG).show()
            emptyList()
        }

        if (suggestions.isEmpty()) {
            Toast.makeText(this, "No uncompleted tasks found in To Do List.", Toast.LENGTH_SHORT).show()
            return
        }

        val titles = suggestions.map { it.task }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Import Suggestion")
            .setItems(titles) { _, which ->
                binding.edittextTitle.setText(suggestions[which].task)
                binding.edittextMessage.setText("Reminder for task from To Do List")
            }
            .show()
    }

    /**
     * Logic to save or update a reminder policy.
     * Includes permission checks and time-normalization logic.
     */
    private fun savePolicy() {
        val title = binding.edittextTitle.text.toString()
        val message = binding.edittextMessage.text.toString()
        
        // Android 12+ requires explicit user permission for exact alarms.
        val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    // Try to open the specific permission page for THIS app.
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to the general list of apps if the specific page fails.
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    }
                    startActivity(intent)
                }
                Toast.makeText(this, "Please grant alarm permission to use this app", Toast.LENGTH_LONG).show()
                return
            }
        }

        val period = binding.spinnerPeriod.selectedItem as Period
        val type = binding.spinnerType.selectedItem as ReminderType
        val date = selectedDate
        val time = selectedTime
        
        val nagInterval = if (type == ReminderType.QUESTION) {
            nagIntervals[binding.spinnerNagInterval.selectedItemPosition]
        } else {
            null
        }

        if (title.isBlank()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        if (date == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (time == null) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        // Combine Date and Time into a single LocalDateTime object.
        var startDateTime = LocalDateTime.of(date, time).withSecond(0).withNano(0)
        val now = LocalDateTime.now()
        
        // If the user chooses a time in the current minute that has already passed,
        // we schedule it for 1 second in the future so it triggers immediately.
        if (startDateTime.isBefore(now)) {
            if (startDateTime.year == now.year && startDateTime.dayOfYear == now.dayOfYear && 
                startDateTime.hour == now.hour && startDateTime.minute == now.minute) {
                startDateTime = now.plusSeconds(1)
            }
            // Note: If they picked an earlier time today or a past date, it remains in the past
            // and will be handled by the catch-up logic in MainActivity.
        }

        val db = ReminderDatabase.getDatabase(this)
        val manager = ReminderManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val currentPolicy = editingPolicy
            if (currentPolicy != null) {
                // UPDATE existing policy.
                val updatedPolicy = currentPolicy.copy(
                    title = title,
                    message = message,
                    startDate = startDateTime,
                    period = period,
                    type = type,
                    nagIntervalMinutes = nagInterval,
                    customRingtoneUri = selectedRingtoneUri
                )
                db.reminderDao().updatePolicy(updatedPolicy)

                // Reschedule the next reminder to match the new settings.
                val next = db.reminderDao().getNextScheduledReminder(updatedPolicy.id)
                if (next != null) {
                    // Cancel existing alarm and update the database entry.
                    manager.cancelAlarm(next.id)
                    val updatedReminder = next.copy(scheduledTime = startDateTime)
                    db.reminderDao().updateScheduledReminder(updatedReminder)
                    manager.setAlarm(updatedReminder, updatedPolicy)
                }
            } else {
                // CREATE new policy.
                val policy = ReminderPolicy(
                    title = title,
                    message = message,
                    startDate = startDateTime,
                    period = period,
                    type = type,
                    nagIntervalMinutes = nagInterval,
                    customRingtoneUri = selectedRingtoneUri
                )
                val id = db.reminderDao().insertPolicy(policy)
                val savedPolicy = policy.copy(id = id.toInt())

                // Create the very first execution instance for this policy.
                val firstReminder = com.example.reminder.data.ScheduledReminder(
                    policyId = savedPolicy.id,
                    scheduledTime = startDateTime
                )
                val reminderId = db.reminderDao().insertScheduledReminder(firstReminder)
                manager.setAlarm(firstReminder.copy(id = reminderId.toInt()), savedPolicy)

                // Log the creation event
                db.logDao().insertLog(com.example.reminder.data.LogEntry(
                    eventType = "CREATED",
                    policyTitle = savedPolicy.title,
                    details = "Scheduled for: ${savedPolicy.startDate}"
                ))
            }
            finish()
        }
    }
}
