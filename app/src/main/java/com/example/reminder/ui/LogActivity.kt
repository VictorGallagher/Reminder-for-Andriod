package com.example.reminder.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.databinding.ActivityLogsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

/**
 * Activity for viewing and managing event logs.
 * Supports clearing logs and exporting/sharing the log file.
 */
class LogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = LogAdapter()
        binding.recyclerviewLogs.layoutManager = LinearLayoutManager(this)
        binding.recyclerviewLogs.adapter = adapter

        val db = ReminderDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.logDao().getAllLogs().collectLatest { logs ->
                adapter.submitList(logs)
            }
        }

        binding.buttonClearLogs.setOnClickListener {
            lifecycleScope.launch {
                db.logDao().clearLogs()
                Toast.makeText(this@LogActivity, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonExportLogs.setOnClickListener {
            exportAndShareLogs()
        }
    }

    /**
     * Writes all logs to a physical file and opens the system "Share" sheet.
     * This allows the user to email the logs, save them to Drive, or view them in another app.
     */
    private fun exportAndShareLogs() {
        val db = ReminderDatabase.getDatabase(this)
        lifecycleScope.launch {
            try {
                // Fetch the current state of logs from the database.
                val logs = db.logDao().getAllLogs().first()
                if (logs.isEmpty()) {
                    Toast.makeText(this@LogActivity, "No logs to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Create the file in the app's internal storage area.
                val file = File(getExternalFilesDir(null), "reminder_logs.txt")
                
                file.printWriter().use { out ->
                    out.println("REMINDER APP LOG EXPORT - ${LocalDateTime.now()}")
                    out.println("--------------------------------------------------")
                    logs.forEach { log ->
                        out.println("[${log.timestamp}] ${log.eventType} | ${log.policyTitle} | ${log.details ?: ""}")
                    }
                }

                // Generate a content URI using the FileProvider defined in AndroidManifest.xml.
                val contentUri = FileProvider.getUriForFile(
                    this@LogActivity,
                    "${packageName}.fileprovider",
                    file
                )

                // Prepare the system Share Intent.
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    // Grant temporary permission for the receiving app to read our log file.
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // Show the standard Android share sheet.
                startActivity(Intent.createChooser(shareIntent, "Share Reminder Logs"))
                
            } catch (e: Exception) {
                Toast.makeText(this@LogActivity, "Failed to share logs: ${e.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("LogActivity", "Log export error", e)
            }
        }
    }
}
