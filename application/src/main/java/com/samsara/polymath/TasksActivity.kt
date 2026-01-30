package com.samsara.polymath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samsara.polymath.R
import com.samsara.polymath.adapter.CommentAdapter
import com.samsara.polymath.adapter.TaskAdapter
import com.samsara.polymath.databinding.ActivityTasksBinding
import com.samsara.polymath.databinding.DialogAddTaskBinding
import com.samsara.polymath.databinding.DialogTaskCommentsBinding
import com.samsara.polymath.viewmodel.CommentViewModel
import com.samsara.polymath.viewmodel.TaskViewModel

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var commentViewModel: CommentViewModel
    private lateinit var adapter: TaskAdapter
    private var personaId: Long = -1
    private var personaName: String = ""
    private var personaBackgroundColor: String = "#007AFF" // Default color
    private var showCompletedTasks: Boolean = false
    private var pendingSwipeTask: com.samsara.polymath.data.Task? = null
    private var pendingSwipePosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personaId = intent.getLongExtra(EXTRA_PERSONA_ID, -1)
        personaName = intent.getStringExtra(EXTRA_PERSONA_NAME) ?: "Tasks"
        personaBackgroundColor = intent.getStringExtra(EXTRA_PERSONA_BACKGROUND_COLOR) ?: "#007AFF"

        if (personaId == -1L) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        commentViewModel = ViewModelProvider(this)[CommentViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        observeTasks()
        setupFab()
    }

    private fun setupToolbar() {
        binding.toolbar.title = personaName
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.toolbar.inflateMenu(R.menu.tasks_menu)
        // Fix overflow icon visibility - ensure it contrasts with toolbar background
        binding.toolbar.overflowIcon?.setTint(resources.getColor(R.color.text_primary, theme))
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_show_completed -> {
                    showCompletedTasks = !showCompletedTasks
                    item.title = if (showCompletedTasks) {
                        getString(R.string.hide_completed_tasks)
                    } else {
                        getString(R.string.show_completed_tasks)
                    }
                    observeTasks() // Refresh the task list
                    true
                }
                else -> false
            }
        }
    }

    private lateinit var itemTouchHelper: ItemTouchHelper
    
    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClick = { task ->
                showTaskCommentsDialog(task)
            },
            onTaskDelete = { task ->
                showDeleteConfirmation(task)
            },
            onTaskComplete = { task ->
                showCompleteConfirmation(task)
            },
            onTaskLongClick = { task ->
                showEditTaskDialog(task)
            },
            onStartDrag = { viewHolder ->
                itemTouchHelper.startDrag(viewHolder)
            }
        )

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = adapter

        // Setup ItemTouchHelper for swipe only (drag initiated manually via handle)
        itemTouchHelper = ItemTouchHelper(createItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView)
    }

    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,  // Drag directions (manual via handle)
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT  // Swipe directions
        ) {
            // Disable long press to drag - only manual drag via handle
            override fun isLongPressDragEnabled(): Boolean = false
            
            // Enable swipe
            override fun isItemViewSwipeEnabled(): Boolean = true
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                    return false
                }

                val currentList = adapter.currentList.toMutableList()
                val item = currentList.removeAt(fromPosition)
                currentList.add(toPosition, item)

                // Update order values and rank status
                currentList.forEachIndexed { index, task ->
                    if (task.order != index) {
                        // Determine rank status based on position change
                        val rankStatus = when {
                            index == 0 && task.order == 0 -> com.samsara.polymath.data.RankStatus.STABLE // Always at top
                            index < task.order -> com.samsara.polymath.data.RankStatus.UP // Moved up
                            index > task.order -> com.samsara.polymath.data.RankStatus.DOWN // Moved down
                            else -> task.rankStatus // No change
                        }
                        
                        viewModel.updateTaskOrderWithRank(task.id, index, task.order, rankStatus)
                    }
                }

                adapter.submitList(currentList)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val task = adapter.currentList[position]

                // Store the task and position for confirmation
                pendingSwipeTask = task
                pendingSwipePosition = position

                // Don't restore immediately - keep card in swiped position until user decides

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        // Delete - show confirmation
                        showDeleteConfirmation(task)
                    }
                    ItemTouchHelper.RIGHT -> {
                        // Mark as complete - show confirmation
                        if (!task.isCompleted) {
                            showCompleteConfirmation(task)
                        }
                    }
                }
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, swipeFlags)
            }
        }
    }

    private fun observeTasks() {
        viewModel.getTasksByPersona(personaId).observe(this) { tasks ->
            // Filter tasks based on showCompletedTasks flag
            val filteredTasks = if (showCompletedTasks) {
                tasks.filter { it.isCompleted }
            } else {
                tasks.filter { !it.isCompleted }
            }
            adapter.submitList(filteredTasks)
        }
    }

    private fun setupFab() {
        binding.addTaskFab.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun setupFrequencyPicker(dialogBinding: DialogAddTaskBinding, existingFrequency: String? = null, existingDays: String? = null) {
        val cal = java.util.Calendar.getInstance()
        val dayNames = arrayOf(
            getString(R.string.sun), getString(R.string.mon), getString(R.string.tue),
            getString(R.string.wed), getString(R.string.thu), getString(R.string.fri), getString(R.string.sat)
        )

        // Toggle frequency container visibility
        dialogBinding.recurringCheckBox.setOnCheckedChangeListener { _, isChecked ->
            dialogBinding.frequencyContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Show frequency container if already recurring
        if (existingFrequency != null) {
            dialogBinding.frequencyContainer.visibility = View.VISIBLE
        }

        // Setup month interval picker
        dialogBinding.monthIntervalPicker.minValue = 1
        dialogBinding.monthIntervalPicker.maxValue = 12
        dialogBinding.monthIntervalPicker.wrapSelectorWheel = false
        // Parse existing interval if monthly
        if (existingFrequency == "MONTHLY" && existingDays != null) {
            val parts = existingDays.split(",")
            if (parts.size >= 2) {
                dialogBinding.monthIntervalPicker.value = parts[1].trim().toIntOrNull() ?: 1
            }
        }

        // Create day-of-week chips
        val selectedDays = mutableSetOf<Int>()
        if (existingFrequency == "CUSTOM") {
            existingDays?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.let { selectedDays.addAll(it) }
        }

        // Calendar.SUNDAY=1, MONDAY=2, ..., SATURDAY=7
        for (dayIndex in 0..6) {
            val dayOfWeek = if (dayIndex == 0) java.util.Calendar.SUNDAY else dayIndex + 1
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = dayNames[dayIndex]
                isCheckable = true
                isChecked = dayOfWeek in selectedDays
                setTextColor(android.graphics.Color.WHITE)
                chipBackgroundColor = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(android.graphics.Color.parseColor("#007AFF"), android.graphics.Color.parseColor("#444444"))
                )
                tag = dayOfWeek
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedDays.add(dayOfWeek) else selectedDays.remove(dayOfWeek)
                }
            }
            dialogBinding.dayChipGroup.addView(chip)
        }

        // Pre-select radio button only if frequency was previously set
        when (existingFrequency) {
            "DAILY" -> dialogBinding.radioDaily.isChecked = true
            "WEEKLY" -> dialogBinding.radioWeekly.isChecked = true
            "MONTHLY" -> dialogBinding.radioMonthly.isChecked = true
            "CUSTOM" -> dialogBinding.radioCustom.isChecked = true
            // null -> no radio selected (recurring without schedule)
        }

        fun updateFrequencyUI(checkedId: Int) {
            dialogBinding.dayChipGroup.visibility = View.GONE
            dialogBinding.frequencyInfoLabel.visibility = View.GONE
            dialogBinding.monthIntervalContainer.visibility = View.GONE
            when (checkedId) {
                R.id.radioWeekly -> {
                    val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                    val dayName = dayNames[if (dayOfWeek == 1) 0 else dayOfWeek - 1]
                    dialogBinding.frequencyInfoLabel.text = getString(R.string.every_week_on, dayName)
                    dialogBinding.frequencyInfoLabel.visibility = View.VISIBLE
                }
                R.id.radioMonthly -> {
                    val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
                    val suffix = when {
                        dayOfMonth in 11..13 -> "th"
                        dayOfMonth % 10 == 1 -> "st"
                        dayOfMonth % 10 == 2 -> "nd"
                        dayOfMonth % 10 == 3 -> "rd"
                        else -> "th"
                    }
                    dialogBinding.frequencyInfoLabel.text = getString(R.string.every_month_on, "$dayOfMonth$suffix")
                    dialogBinding.frequencyInfoLabel.visibility = View.VISIBLE
                    dialogBinding.monthIntervalContainer.visibility = View.VISIBLE
                }
                R.id.radioCustom -> {
                    dialogBinding.dayChipGroup.visibility = View.VISIBLE
                }
            }
        }

        dialogBinding.frequencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            updateFrequencyUI(checkedId)
        }

        // Initial UI state
        updateFrequencyUI(dialogBinding.frequencyRadioGroup.checkedRadioButtonId)
    }

    private fun getFrequencyFromDialog(dialogBinding: DialogAddTaskBinding): Pair<String?, String?> {
        if (!dialogBinding.recurringCheckBox.isChecked) return null to null
        val cal = java.util.Calendar.getInstance()
        return when (dialogBinding.frequencyRadioGroup.checkedRadioButtonId) {
            R.id.radioDaily -> "DAILY" to null
            R.id.radioWeekly -> "WEEKLY" to cal.get(java.util.Calendar.DAY_OF_WEEK).toString()
            R.id.radioMonthly -> {
                val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
                val interval = dialogBinding.monthIntervalPicker.value
                "MONTHLY" to "$dayOfMonth,$interval"
            }
            R.id.radioCustom -> {
                val days = mutableListOf<Int>()
                for (i in 0 until dialogBinding.dayChipGroup.childCount) {
                    val chip = dialogBinding.dayChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip
                    if (chip?.isChecked == true) {
                        days.add(chip.tag as Int)
                    }
                }
                "CUSTOM" to days.joinToString(",")
            }
            else -> null to null // No frequency selected = recurring without schedule
        }
    }

    private var selectedEndDate: Long? = null

    private fun setupEndDatePicker(dialogBinding: DialogAddTaskBinding, existingEndDate: Long? = null) {
        selectedEndDate = existingEndDate
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

        fun updateLabel() {
            if (selectedEndDate != null) {
                dialogBinding.endDateLabel.text = dateFormat.format(java.util.Date(selectedEndDate!!))
                dialogBinding.endDateLabel.setTextColor(android.graphics.Color.WHITE)
                dialogBinding.clearEndDateButton.visibility = View.VISIBLE
            } else {
                dialogBinding.endDateLabel.text = getString(R.string.no_end_date)
                dialogBinding.endDateLabel.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
                dialogBinding.clearEndDateButton.visibility = View.GONE
            }
        }

        updateLabel()

        dialogBinding.endDateLabel.setOnClickListener {
            val cal = java.util.Calendar.getInstance()
            if (selectedEndDate != null) cal.timeInMillis = selectedEndDate!!
            android.app.DatePickerDialog(this, { _, year, month, day ->
                val picked = java.util.Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                selectedEndDate = picked.timeInMillis
                updateLabel()
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        dialogBinding.clearEndDateButton.setOnClickListener {
            selectedEndDate = null
            updateLabel()
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        setupFrequencyPicker(dialogBinding)
        setupEndDatePicker(dialogBinding)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_task))
            .setView(dialogBinding.root)
            .create()

        dialog.setOnShowListener {
            val titleView = dialog.findViewById<android.widget.TextView>(android.R.id.title)
            titleView?.setTextColor(android.graphics.Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(android.graphics.Color.WHITE)
        }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.done)) { _, _ ->
            val title = dialogBinding.taskTitleEditText.text?.toString()?.trim()
            val description = dialogBinding.taskDescriptionEditText.text?.toString()?.trim() ?: ""
            val isRecurring = dialogBinding.recurringCheckBox.isChecked
            val (frequency, days) = getFrequencyFromDialog(dialogBinding)

            if (!title.isNullOrEmpty()) {
                val capitalizedTitle = title.replaceFirstChar {
                    if (it.isLowerCase()) it.uppercaseChar() else it
                }
                viewModel.insertTask(personaId, capitalizedTitle, description, personaBackgroundColor, isRecurring, frequency, days, selectedEndDate)
            } else {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ -> }
        dialog.show()
    }

    private fun showEditTaskDialog(task: com.samsara.polymath.data.Task) {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))

        dialogBinding.taskTitleEditText.setText(task.title)
        dialogBinding.taskDescriptionEditText.setText(task.description)
        dialogBinding.recurringCheckBox.isChecked = task.isRecurring
        setupFrequencyPicker(dialogBinding, task.recurringFrequency, task.recurringDays)
        setupEndDatePicker(dialogBinding, task.endDate)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.edit_task))
            .setView(dialogBinding.root)
            .create()

        dialog.setOnShowListener {
            val titleView = dialog.findViewById<android.widget.TextView>(android.R.id.title)
            titleView?.setTextColor(android.graphics.Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(android.graphics.Color.WHITE)
        }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.done)) { _, _ ->
            val title = dialogBinding.taskTitleEditText.text?.toString()?.trim()
            val description = dialogBinding.taskDescriptionEditText.text?.toString()?.trim() ?: ""
            val isRecurring = dialogBinding.recurringCheckBox.isChecked
            val (frequency, days) = getFrequencyFromDialog(dialogBinding)

            if (!title.isNullOrEmpty()) {
                val capitalizedTitle = title.replaceFirstChar {
                    if (it.isLowerCase()) it.uppercaseChar() else it
                }
                val updatedTask = task.copy(
                    title = capitalizedTitle,
                    description = description,
                    isRecurring = isRecurring,
                    recurringFrequency = frequency,
                    recurringDays = days,
                    endDate = selectedEndDate
                )
                viewModel.updateTask(updatedTask)
            } else {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ -> }
        dialog.show()
    }

    private fun showDeleteConfirmation(task: com.samsara.polymath.data.Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Task")
            .setMessage(getString(R.string.delete_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteTask(task)
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                // User cancelled - restore card to original position
                if (pendingSwipePosition != -1) {
                    adapter.notifyItemChanged(pendingSwipePosition)
                }
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .setOnDismissListener {
                // Dialog dismissed without action - restore card
                if (pendingSwipeTask != null && pendingSwipePosition != -1) {
                    adapter.notifyItemChanged(pendingSwipePosition)
                }
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .show()
    }

    private fun showCompleteConfirmation(task: com.samsara.polymath.data.Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Complete Task")
            .setMessage(getString(R.string.mark_complete_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.markTaskAsComplete(task)
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                // User cancelled - restore card to original position
                if (pendingSwipePosition != -1) {
                    adapter.notifyItemChanged(pendingSwipePosition)
                }
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .setOnDismissListener {
                // Dialog dismissed without action - restore card
                if (pendingSwipeTask != null && pendingSwipePosition != -1) {
                    adapter.notifyItemChanged(pendingSwipePosition)
                }
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .show()
    }

    private fun showTaskCommentsDialog(task: com.samsara.polymath.data.Task) {
        val dialogBinding = DialogTaskCommentsBinding.inflate(LayoutInflater.from(this))
        
        // Set task title
        dialogBinding.taskTitleTextView.text = task.title
        
        // Setup comments RecyclerView
        val commentAdapter = CommentAdapter()
        dialogBinding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        dialogBinding.commentsRecyclerView.adapter = commentAdapter
        
        // Observe comments for this task
        commentViewModel.getCommentsByTask(task.id).observe(this) { comments ->
            commentAdapter.submitList(comments)
            // Scroll to bottom to show newest comment
            if (comments.isNotEmpty()) {
                dialogBinding.commentsRecyclerView.post {
                    dialogBinding.commentsRecyclerView.smoothScrollToPosition(comments.size - 1)
                }
            }
        }
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.comments))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.add_comment), null) // Set to null to prevent auto-dismiss
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        
        dialog.setOnShowListener {
            val titleView = dialog.findViewById<android.widget.TextView>(android.R.id.title)
            titleView?.setTextColor(android.graphics.Color.WHITE)
            
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton?.setTextColor(android.graphics.Color.WHITE)
            negativeButton?.setTextColor(android.graphics.Color.WHITE)
            
            // Override positive button to keep dialog open when adding comment
            positiveButton?.setOnClickListener {
                val commentText = dialogBinding.commentEditText.text?.toString()?.trim()
                if (!commentText.isNullOrEmpty()) {
                    commentViewModel.insertComment(task.id, commentText)
                    dialogBinding.commentEditText.text?.clear()
                    dialogBinding.commentEditText.requestFocus()
                    // Don't dismiss - keep dialog open for more comments
                }
            }
            
            // Focus on comment input
            dialogBinding.commentEditText.requestFocus()
        }
        
        dialog.show()
    }

    companion object {
        const val EXTRA_PERSONA_ID = "persona_id"
        const val EXTRA_PERSONA_NAME = "persona_name"
        const val EXTRA_PERSONA_BACKGROUND_COLOR = "persona_background_color"

        fun start(activity: AppCompatActivity, personaId: Long, personaName: String, personaBackgroundColor: String) {
            val intent = android.content.Intent(activity, TasksActivity::class.java).apply {
                putExtra(EXTRA_PERSONA_ID, personaId)
                putExtra(EXTRA_PERSONA_NAME, personaName)
                putExtra(EXTRA_PERSONA_BACKGROUND_COLOR, personaBackgroundColor)
            }
            activity.startActivity(intent)
        }
    }
}

