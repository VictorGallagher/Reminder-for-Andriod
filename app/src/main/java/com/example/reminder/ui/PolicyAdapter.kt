package com.example.reminder.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reminder.data.PolicyWithNextReminder
import com.example.reminder.data.ReminderPolicy
import com.example.reminder.databinding.ItemPolicyBinding
import java.time.format.DateTimeFormatter

class PolicyAdapter(
    private val onPolicyClick: (ReminderPolicy) -> Unit,
    private val onSkipClick: (ReminderPolicy) -> Unit
) : ListAdapter<PolicyWithNextReminder, PolicyAdapter.PolicyViewHolder>(DiffCallback) {

    private val formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolicyViewHolder {
        val binding = ItemPolicyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PolicyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PolicyViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { onPolicyClick(item.policy) }
        holder.bind(item)
    }

    inner class PolicyViewHolder(private val binding: ItemPolicyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PolicyWithNextReminder) {
            val policy = item.policy
            binding.textviewPolicyTitle.text = policy.title
            binding.textviewPolicyInfo.text = "${policy.period} - ${policy.type}"
            
            val next = item.nextReminder
            if (next != null) {
                try {
                    val timeStr = next.scheduledTime.format(formatter)
                    binding.textviewNextReminder.text = "NEXT: $timeStr"
                    binding.textviewNextReminder.setTextColor(android.graphics.Color.parseColor("#00BCD4")) // Teal
                } catch (e: Exception) {
                    binding.textviewNextReminder.text = "Next: ERROR"
                }
            } else {
                binding.textviewNextReminder.text = "No upcoming reminders"
            }
            
            binding.buttonSkip.setOnClickListener { onSkipClick(policy) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PolicyWithNextReminder>() {
        override fun areItemsTheSame(oldItem: PolicyWithNextReminder, newItem: PolicyWithNextReminder): Boolean {
            return oldItem.policy.id == newItem.policy.id
        }

        override fun areContentsTheSame(oldItem: PolicyWithNextReminder, newItem: PolicyWithNextReminder): Boolean {
            return oldItem == newItem
        }
    }
}
