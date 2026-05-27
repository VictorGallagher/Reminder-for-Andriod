package com.example.reminder.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reminder.data.LogEntry
import com.example.reminder.databinding.ItemLogBinding
import java.time.format.DateTimeFormatter

class LogAdapter : ListAdapter<LogEntry, LogAdapter.LogViewHolder>(DiffCallback) {
    private val formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm:ss")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LogViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(log: LogEntry) {
            binding.textviewLogType.text = log.eventType
            binding.textviewLogTime.text = log.timestamp.format(formatter)
            binding.textviewLogPolicy.text = log.policyTitle
            binding.textviewLogDetails.text = log.details
            
            val color = when (log.eventType) {
                "CREATED" -> "#4CAF50"
                "ACTIVATED" -> "#2196F3"
                "COMPLETED" -> "#00BCD4"
                "PASSED" -> "#FF9800"
                "MISSED" -> "#F44336"
                "SKIPPED" -> "#9E9E9E"
                else -> "#000000"
            }
            binding.textviewLogType.setTextColor(android.graphics.Color.parseColor(color))
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry) = oldItem == newItem
    }
}
