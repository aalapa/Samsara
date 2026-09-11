package com.samsara.polymath.adapter

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.R
import com.samsara.polymath.data.Task
import com.samsara.polymath.databinding.ItemTaskBinding
import java.util.concurrent.TimeUnit

data class RecurringStats(val originCreatedAt: Long, val completionCount: Int)

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskComplete: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
    private val onCircleClick: (Task) -> Unit = {}
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    var recurringStats: Map<Long, RecurringStats> = emptyMap()
        set(value) { field = value; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position), currentList.size)
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task, totalTaskCount: Int) {
            val isCompact = totalTaskCount > 7
            val density = binding.root.context.resources.displayMetrics.density

            binding.taskTitleTextView.text =
                if (task.isAvoidTask) "🛡️ ${task.title}" else task.title

            if (task.description.isNotEmpty()) {
                binding.taskDescriptionTextView.text = task.description
                binding.taskDescriptionTextView.visibility = View.VISIBLE
            } else {
                binding.taskDescriptionTextView.visibility = View.GONE
            }

            val marginDp = 6
            (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
                it.bottomMargin = (marginDp * density).toInt()
                binding.root.layoutParams = it
            }

            binding.taskTitleTextView.textSize = if (isCompact) 13.5f else 14.5f
            binding.taskDescriptionTextView.textSize = if (isCompact) 11f else 11.5f
            binding.completionInfoTextView.textSize = if (isCompact) 10f else 11f

            val dayTextSize = if (isCompact) 16f else 20f
            binding.daysTextView.textSize = dayTextSize

            var bgColor = Color.WHITE
            try { bgColor = Color.parseColor(task.backgroundColor) } catch (_: Exception) { }
            binding.root.setCardBackgroundColor(bgColor)

            val isDark = isColorDark(bgColor)
            val contrastColor = if (isDark) Color.WHITE else Color.BLACK

            binding.taskTitleTextView.setTextColor(contrastColor)
            binding.taskDescriptionTextView.setTextColor(
                Color.argb(133, Color.red(contrastColor), Color.green(contrastColor), Color.blue(contrastColor))
            )
            binding.dragHandleImageView.setColorFilter(
                Color.argb(66, Color.red(contrastColor), Color.green(contrastColor), Color.blue(contrastColor))
            )

            val currentTime = System.currentTimeMillis()
            if (task.isCompleted && task.completedAt != null) {
                val daysToComplete = calculateDaysDifference(task.createdAt, task.completedAt)
                val daysSinceCompletion = calculateDaysDifference(task.completedAt, currentTime)

                binding.daysTextView.text = buildDaySpan(daysToComplete.toString())
                binding.daysTextView.setTextColor(binding.root.context.getColor(R.color.positive))

                if (task.isAvoidTask) {
                    val brokeText = when {
                        daysSinceCompletion == 0L -> "Broke today"
                        daysSinceCompletion == 1L -> "Broke 1 day ago"
                        else -> "Broke $daysSinceCompletion days ago"
                    }
                    binding.completionInfoTextView.text = "$daysToComplete day streak • $brokeText"
                    binding.completionInfoTextView.setTextColor(binding.root.context.getColor(R.color.negative))
                } else {
                    val completionText = when {
                        daysSinceCompletion == 0L -> binding.root.context.getString(R.string.done_today)
                        daysSinceCompletion == 1L -> binding.root.context.getString(R.string.done_day_ago, daysSinceCompletion)
                        else -> binding.root.context.getString(R.string.done_days_ago, daysSinceCompletion)
                    }
                    binding.completionInfoTextView.text = "$daysToComplete days • $completionText"
                    binding.completionInfoTextView.setTextColor(binding.root.context.getColor(R.color.positive))
                }
                binding.completionInfoTextView.visibility = View.VISIBLE
            } else {
                val daysSinceCreation = calculateDaysDifference(task.createdAt, currentTime)
                binding.daysTextView.text = buildDaySpan(daysSinceCreation.toString())
                binding.daysTextView.setTextColor(contrastColor)

                val rstats = if (task.isRecurring) recurringStats[task.id] else null
                if (rstats != null) {
                    val originDays = calculateDaysDifference(rstats.originCreatedAt, currentTime)
                    val muted = Color.argb(153, Color.red(contrastColor), Color.green(contrastColor), Color.blue(contrastColor))
                    binding.completionInfoTextView.text = "${originDays}d · ${daysSinceCreation}d · ×${rstats.completionCount}"
                    binding.completionInfoTextView.setTextColor(muted)
                    binding.completionInfoTextView.visibility = View.VISIBLE
                } else {
                    binding.completionInfoTextView.visibility = View.GONE
                }
            }

            binding.daysTextView.setOnClickListener { onCircleClick(task) }
            binding.root.setOnClickListener { onTaskClick(task) }
            binding.root.setOnLongClickListener { onTaskLongClick(task); true }
            binding.dragHandleImageView.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) onStartDrag(this)
                false
            }
        }

        private fun buildDaySpan(number: String): SpannableString {
            val full = "${number}d"
            val span = SpannableString(full)
            span.setSpan(RelativeSizeSpan(0.55f), number.length, full.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return span
        }

        private fun calculateDaysDifference(startTime: Long, endTime: Long): Long =
            TimeUnit.MILLISECONDS.toDays(endTime - startTime)

        private fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return darkness >= 0.5
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
