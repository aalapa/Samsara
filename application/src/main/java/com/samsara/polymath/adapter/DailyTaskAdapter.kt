package com.samsara.polymath.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.data.Task
import com.samsara.polymath.databinding.ItemDailyTaskBinding

data class DailyTaskItem(
    val task: Task,
    val personaName: String,
    val personaColor: String
)

class DailyTaskAdapter(
    private val onTaskComplete: (Task) -> Unit
) : ListAdapter<DailyTaskItem, DailyTaskAdapter.DailyTaskViewHolder>(DailyTaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyTaskViewHolder {
        val binding = ItemDailyTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DailyTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DailyTaskViewHolder(
        private val binding: ItemDailyTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyTaskItem) {
            binding.taskTitleTextView.text = item.task.title
            binding.personaNameTextView.text = item.personaName

            try {
                binding.personaColorBar.setBackgroundColor(Color.parseColor(item.personaColor))
            } catch (_: Exception) {
                binding.personaColorBar.setBackgroundColor(Color.parseColor("#007AFF"))
            }

            binding.completeButton.setOnClickListener {
                onTaskComplete(item.task)
            }
        }
    }

    class DailyTaskDiffCallback : DiffUtil.ItemCallback<DailyTaskItem>() {
        override fun areItemsTheSame(oldItem: DailyTaskItem, newItem: DailyTaskItem): Boolean {
            return oldItem.task.id == newItem.task.id
        }

        override fun areContentsTheSame(oldItem: DailyTaskItem, newItem: DailyTaskItem): Boolean {
            return oldItem == newItem
        }
    }
}
