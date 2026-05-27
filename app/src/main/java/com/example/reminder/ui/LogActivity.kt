package com.example.reminder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reminder.data.ReminderDatabase
import com.example.reminder.databinding.ActivityLogsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
            }
        }
    }
}
