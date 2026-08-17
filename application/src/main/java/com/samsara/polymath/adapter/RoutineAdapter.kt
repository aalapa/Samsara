package com.samsara.polymath.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.data.RoutineTaskStats
import com.samsara.polymath.data.ScheduledDays
import com.samsara.polymath.data.Task
import com.samsara.polymath.databinding.ItemRoutineHeaderBinding
import com.samsara.polymath.databinding.ItemRoutineTaskBinding

private const val TYPE_HEADER = 0
private const val TYPE_TASK = 1

sealed class RoutineListItem {
    data class Header(
        val chunk: String,
        val completed: Int,
        val total: Int,
        val isOverdue: Boolean = false
    ) : RoutineListItem()
    data class TaskItem(val task: Task) : RoutineListItem()
}

class RoutineAdapter(
    private val onTaskTap: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit,
    private val onChunkColorTap: (chunk: String, currentColor: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var sections: List<String> = listOf("MORNING", "AFTERNOON", "EVENING", "NIGHT")
    private var items: List<RoutineListItem> = emptyList()
    private var completedIds: Set<Long> = emptySet()
    private var statsMap: Map<Long, RoutineTaskStats> = emptyMap()
    var chunkColors: Map<String, Int> = emptyMap()  // chunk → base color int

    /** When true (default) completed tasks render as the slim bar. */
    var isBarMode: Boolean = true
    var isManageMode: Boolean = false
    var sectionEndTimes: Map<String, Int> = emptyMap()  // section name → end time (minutes from midnight)
    var currentMinutes: Int = 0

    fun submit(
        tasks: List<Task>,
        completedIds: Set<Long>,
        statsMap: Map<Long, RoutineTaskStats>
    ) {
        this.completedIds = completedIds
        this.statsMap = statsMap

        val newItems = mutableListOf<RoutineListItem>()
        for (chunk in sections) {
            val chunkTasks = tasks.filter { it.timeChunk == chunk }
            if (chunkTasks.isEmpty()) continue
            val done = chunkTasks.count { it.id in completedIds }

            val endTime = sectionEndTimes[chunk]
            val isOverdue = !isManageMode && endTime != null && currentMinutes >= endTime

            // Hide section entirely when its deadline passed and every task is done
            if (isOverdue && done == chunkTasks.size) continue

            newItems += RoutineListItem.Header(chunk, done, chunkTasks.size, isOverdue && done < chunkTasks.size)
            chunkTasks.forEach { newItems += RoutineListItem.TaskItem(it) }
        }
        items = newItems
        notifyDataSetChanged()
    }

    fun getTaskAt(position: Int): Task? =
        (items.getOrNull(position) as? RoutineListItem.TaskItem)?.task

    fun moveItem(from: Int, to: Int) {
        val mutable = items.toMutableList()
        mutable.add(to, mutable.removeAt(from))
        items = mutable
        notifyItemMoved(from, to)
    }

    fun tasksInAdapterOrder(): List<Task> =
        items.filterIsInstance<RoutineListItem.TaskItem>().map { it.task }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is RoutineListItem.Header   -> TYPE_HEADER
        is RoutineListItem.TaskItem -> TYPE_TASK
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER)
            HeaderVH(ItemRoutineHeaderBinding.inflate(inflater, parent, false))
        else
            TaskVH(ItemRoutineTaskBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is RoutineListItem.Header   -> (holder as HeaderVH).bind(item)
            is RoutineListItem.TaskItem -> (holder as TaskVH).bind(item.task)
        }
    }

    // ── Header ──────────────────────────────────────────────────────────────

    inner class HeaderVH(private val b: ItemRoutineHeaderBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(header: RoutineListItem.Header) {
            b.chunkNameTextView.text = header.chunk
            b.chunkProgressTextView.text =
                if (isManageMode) "" else "${header.completed} / ${header.total}"

            val chunkColor = chunkColors[header.chunk]
            if (chunkColor != null) {
                b.chunkNameTextView.setTextColor(chunkColor)
            } else {
                b.chunkNameTextView.setTextColor(
                    b.root.context.getColor(com.samsara.polymath.R.color.ink_muted)
                )
            }

            b.overdueTextView.visibility = if (header.isOverdue) View.VISIBLE else View.GONE

            // Color dot: filled circle showing current chunk color, tap to pick
            val dotColor = chunkColor ?: Color.LTGRAY
            val dotDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(dotColor)
            }
            b.chunkColorDot.background = dotDrawable
            b.chunkColorDot.setOnClickListener {
                onChunkColorTap(header.chunk, dotColor)
            }
        }
    }

    // ── Task ────────────────────────────────────────────────────────────────

    inner class TaskVH(private val b: ItemRoutineTaskBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(task: Task) {
            val isCompleted = !isManageMode && task.id in completedIds
            val stats = statsMap[task.id]

            var bgColor = Color.WHITE
            try { bgColor = Color.parseColor(task.backgroundColor) } catch (_: Exception) {}
            b.root.setCardBackgroundColor(bgColor)

            val isDark = isColorDark(bgColor)
            val contrast = if (isDark) Color.WHITE else Color.BLACK
            val muted = Color.argb(153, Color.red(contrast), Color.green(contrast), Color.blue(contrast))

            // Tap always toggles completion (or opens edit in manage mode)
            b.root.setOnClickListener {
                if (isManageMode) onTaskEdit(task) else onTaskTap(task)
            }
            b.root.setOnLongClickListener { onTaskEdit(task); true }

            when {
                // ── COMPLETED + BAR MODE ─────────────────────────────────────
                isCompleted && isBarMode -> {
                    b.barLayout.visibility  = View.VISIBLE
                    b.contentArea.visibility = View.GONE

                    val rate = stats?.completionRate ?: 0f
                    val fillWeight  = (rate * 100f).coerceIn(0f, 100f)
                    val emptyWeight = 100f - fillWeight

                    (b.barFill.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let {
                        it.weight = fillWeight; b.barFill.layoutParams = it
                    }
                    (b.barEmpty.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let {
                        it.weight = emptyWeight; b.barEmpty.layoutParams = it
                    }
                    b.barFill.setBackgroundColor(bgColor)
                    b.barEmpty.setBackgroundColor(
                        Color.argb(40, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor))
                    )
                }

                // ── COMPLETED + DETAIL MODE ───────────────────────────────────
                isCompleted && !isBarMode -> {
                    b.barLayout.visibility  = View.GONE
                    b.contentArea.visibility = View.VISIBLE
                    b.taskLayout.visibility  = View.GONE
                    b.statsLayout.visibility = View.VISIBLE

                    b.statsTitleTextView.text = task.title
                    b.statsTitleTextView.setTextColor(contrast)
                    b.checkMarkTextView.setTextColor(contrast)

                    val streak = stats?.streak ?: 0
                    val total  = stats?.total ?: 0
                    b.streakTextView.text = if (streak > 0) "🔥 ${if (streak == 1) "1 day" else "$streak days"}" else "—"
                    b.streakTextView.setTextColor(muted)
                    b.totalTextView.text = "$total total"
                    b.totalTextView.setTextColor(muted)
                }

                // ── NOT COMPLETED ─────────────────────────────────────────────
                else -> {
                    b.barLayout.visibility  = View.GONE
                    b.contentArea.visibility = View.VISIBLE
                    b.taskLayout.visibility  = View.VISIBLE
                    b.statsLayout.visibility = View.GONE

                    b.taskTitleTextView.text = task.title
                    b.taskTitleTextView.setTextColor(contrast)

                    if (isManageMode) {
                        b.scheduledDaysTextView.visibility = View.VISIBLE
                        b.scheduledDaysTextView.text = ScheduledDays.formatLabel(task.scheduledDays)
                        b.scheduledDaysTextView.setTextColor(muted)
                        b.streakHintTextView.visibility = View.GONE
                    } else {
                        b.scheduledDaysTextView.visibility = View.GONE
                        val streak = stats?.streak ?: 0
                        if (streak > 0) {
                            b.streakHintTextView.visibility = View.VISIBLE
                            b.streakHintTextView.text = "🔥 $streak"
                            b.streakHintTextView.setTextColor(muted)
                        } else {
                            b.streakHintTextView.visibility = View.GONE
                        }
                    }
                }
            }
        }

        private fun isColorDark(color: Int): Boolean {
            val d = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return d >= 0.5
        }
    }
}
