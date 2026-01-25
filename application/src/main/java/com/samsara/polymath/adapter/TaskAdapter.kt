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
        val totalTaskCount = currentList.size
        holder.bind(getItem(position), totalTaskCount)
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task, totalTaskCount: Int) {
            // Adjust dimensions if more than 7 tasks (instead of scaling)
            val isCompact = totalTaskCount > 7
            
            binding.taskTitleTextView.text = task.title
            
            if (task.description.isNotEmpty()) {
                binding.taskDescriptionTextView.text = task.description
                binding.taskDescriptionTextView.visibility = View.VISIBLE
            } else {
                binding.taskDescriptionTextView.visibility = View.GONE
            }
            
            // Adjust card layout for compact mode
            val density = binding.root.context.resources.displayMetrics.density
            
            // Adjust bottom margin (gap between cards)
            val layoutParams = binding.root.layoutParams as? ViewGroup.MarginLayoutParams
            if (layoutParams != null) {
                val marginDp = if (isCompact) 6 else 9
                layoutParams.bottomMargin = (marginDp * density).toInt()
                binding.root.layoutParams = layoutParams
            }
            
            // Adjust inner padding (the LinearLayout inside the card)
            val paddingDp = if (isCompact) 11 else 15
            val paddingPx = (paddingDp * density).toInt()
            if (binding.root.childCount > 0) {
                val innerLayout = binding.root.getChildAt(0)
                if (innerLayout is ViewGroup) {
                    innerLayout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                }
            }
            
            // Adjust circle size
            val circleSizeDp = if (isCompact) 34 else 45
            val circleSizePx = (circleSizeDp * density).toInt()
            val circleLayoutParams = binding.daysTextView.layoutParams as? ViewGroup.MarginLayoutParams
            if (circleLayoutParams != null) {
                circleLayoutParams.width = circleSizePx
                circleLayoutParams.height = circleSizePx
                // Adjust circle right margin
                val circleMarginDp = if (isCompact) 9 else 12
                circleLayoutParams.marginEnd = (circleMarginDp * density).toInt()
                binding.daysTextView.layoutParams = circleLayoutParams
            }
            
            // Adjust rank indicator size and margin
            val rankSizeDp = if (isCompact) 16 else 20
            val rankSizePx = (rankSizeDp * density).toInt()
            val rankLayoutParams = binding.rankIndicatorImageView.layoutParams as? ViewGroup.MarginLayoutParams
            if (rankLayoutParams != null) {
                rankLayoutParams.width = rankSizePx
                rankLayoutParams.height = rankSizePx
                val rankMarginDp = if (isCompact) 6 else 8
                rankLayoutParams.marginStart = (rankMarginDp * density).toInt()
                binding.rankIndicatorImageView.layoutParams = rankLayoutParams
            }
            
            // Adjust font sizes (using sp to maintain accessibility)
            binding.taskTitleTextView.textSize = if (isCompact) 13f else 15f
            binding.taskDescriptionTextView.textSize = if (isCompact) 11f else 13f
            binding.daysTextView.textSize = if (isCompact) 12f else 14f
            binding.completionInfoTextView.textSize = if (isCompact) 9f else 11f
            
            // Adjust margins for description and completion info
            val descLayoutParams = binding.taskDescriptionTextView.layoutParams as? ViewGroup.MarginLayoutParams
            if (descLayoutParams != null) {
                val descMarginDp = if (isCompact) 3 else 4
                descLayoutParams.topMargin = (descMarginDp * density).toInt()
                binding.taskDescriptionTextView.layoutParams = descLayoutParams
            }
            
            val completionLayoutParams = binding.completionInfoTextView.layoutParams as? ViewGroup.MarginLayoutParams
            if (completionLayoutParams != null) {
                val completionMarginDp = if (isCompact) 6 else 8
                completionLayoutParams.topMargin = (completionMarginDp * density).toInt()
                binding.completionInfoTextView.layoutParams = completionLayoutParams
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

