package com.samsara.polymath.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

sealed class GtdListItem {
    data class Header(val title: String) : GtdListItem()
    data class TaskItem(val dailyTaskItem: DailyTaskItem) : GtdListItem()
}

class DailyTaskAdapter(
    private val onTaskComplete: (Task) -> Unit
) : ListAdapter<GtdListItem, RecyclerView.ViewHolder>(GtdDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TASK = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GtdListItem.Header -> TYPE_HEADER
            is GtdListItem.TaskItem -> TYPE_TASK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val tv = TextView(parent.context).apply {
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 24
                        bottomMargin = 8
                        marginStart = 4
                    }
                    setTextColor(Color.parseColor("#AAAAAA"))
                    textSize = 13f
                    setTypeface(null, Typeface.BOLD)
                    letterSpacing = 0.05f
                }
                HeaderViewHolder(tv)
            }
            else -> {
                val binding = ItemDailyTaskBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                TaskViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is GtdListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is GtdListItem.TaskItem -> (holder as TaskViewHolder).bind(item.dailyTaskItem)
        }
    }

    inner class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(header: GtdListItem.Header) {
            textView.text = header.title.uppercase()
        }
    }

    inner class TaskViewHolder(
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

    class GtdDiffCallback : DiffUtil.ItemCallback<GtdListItem>() {
        override fun areItemsTheSame(oldItem: GtdListItem, newItem: GtdListItem): Boolean {
            return when {
                oldItem is GtdListItem.Header && newItem is GtdListItem.Header -> oldItem.title == newItem.title
                oldItem is GtdListItem.TaskItem && newItem is GtdListItem.TaskItem -> oldItem.dailyTaskItem.task.id == newItem.dailyTaskItem.task.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: GtdListItem, newItem: GtdListItem): Boolean {
            return oldItem == newItem
        }
    }
}
