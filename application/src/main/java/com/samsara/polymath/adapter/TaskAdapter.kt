package com.samsara.polymath.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.R
import com.samsara.polymath.data.RankStatus
import com.samsara.polymath.data.Task
import com.samsara.polymath.databinding.ItemTaskBinding
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskComplete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.taskTitleTextView.text = task.title
            
            if (task.description.isNotEmpty()) {
                binding.taskDescriptionTextView.text = task.description
                binding.taskDescriptionTextView.visibility = View.VISIBLE
            } else {
                binding.taskDescriptionTextView.visibility = View.GONE
            }

            // Apply background color from task (inherited from persona with variant)
            try {
                val bgColor = Color.parseColor(task.backgroundColor)
                binding.root.setCardBackgroundColor(bgColor)
                
                // Determine text color based on background brightness
                val isDark = isColorDark(bgColor)
                val textColor = if (isDark) Color.WHITE else Color.BLACK
                binding.taskTitleTextView.setTextColor(textColor)
                binding.taskDescriptionTextView.setTextColor(textColor)
            } catch (e: Exception) {
                // Fallback to default colors if parsing fails
                binding.root.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                binding.taskTitleTextView.setTextColor(Color.parseColor("#000000"))
            }

            val currentTime = System.currentTimeMillis()
            val daysTextView = binding.daysTextView

            if (task.isCompleted && task.completedAt != null) {
                // Task is completed
                val daysToComplete = calculateDaysDifference(task.createdAt, task.completedAt)
                val daysSinceCompletion = calculateDaysDifference(task.completedAt, currentTime)
                
                daysTextView.text = daysToComplete.toString()
                
                // Show completion info
                val completionText = when {
                    daysSinceCompletion == 0L -> binding.root.context.getString(R.string.done_today)
                    daysSinceCompletion == 1L -> binding.root.context.getString(R.string.done_day_ago, daysSinceCompletion)
                    else -> binding.root.context.getString(R.string.done_days_ago, daysSinceCompletion)
                }
                binding.completionInfoTextView.text = "$daysToComplete days • $completionText"
                binding.completionInfoTextView.visibility = View.VISIBLE
                
                // Change circle color to green
                daysTextView.setBackgroundResource(R.drawable.circle_background_completed)
            } else {
                // Task is not completed
                val daysSinceCreation = calculateDaysDifference(task.createdAt, currentTime)
                daysTextView.text = daysSinceCreation.toString()
                binding.completionInfoTextView.visibility = View.GONE
                
                // Keep blue circle
                daysTextView.setBackgroundResource(R.drawable.circle_background)
            }

            // Set rank indicator icon based on rank status
            val rankIcon = when (task.rankStatus) {
                RankStatus.STABLE -> R.drawable.ic_rank_stable
                RankStatus.UP -> R.drawable.ic_rank_up
                RankStatus.DOWN -> R.drawable.ic_rank_down
            }
            binding.rankIndicatorImageView.setImageResource(rankIcon)

            binding.root.setOnClickListener {
                onTaskClick(task)
            }
        }

        private fun calculateDaysDifference(startTime: Long, endTime: Long): Long {
            val diff = endTime - startTime
            return TimeUnit.MILLISECONDS.toDays(diff)
        }
        
        private fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return darkness >= 0.5
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}

