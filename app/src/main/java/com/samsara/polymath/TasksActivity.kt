package com.samsara.polymath

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samsara.polymath.R
import com.samsara.polymath.adapter.TaskAdapter
import com.samsara.polymath.databinding.ActivityTasksBinding
import com.samsara.polymath.databinding.DialogAddTaskBinding
import com.samsara.polymath.viewmodel.TaskViewModel

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private lateinit var viewModel: TaskViewModel
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

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClick = { task ->
                // Optional: Could show task details
            },
            onTaskDelete = { task ->
                showDeleteConfirmation(task)
            },
            onTaskComplete = { task ->
                showCompleteConfirmation(task)
            }
        )

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = adapter

        // Setup ItemTouchHelper for drag and swipe
        val itemTouchHelper = ItemTouchHelper(createItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView)
    }

    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
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

                // Update order values
                currentList.forEachIndexed { index, task ->
                    if (task.order != index) {
                        viewModel.updateTaskOrder(task.id, index)
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

                // Restore the view immediately before showing dialog
                // This prevents the refresh from happening behind the dialog
                binding.tasksRecyclerView.post {
                    adapter.notifyItemChanged(position)
                }

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

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        
        // Set text colors to white for dark background
        dialogBinding.taskTitleEditText.setTextColor(android.graphics.Color.WHITE)
        dialogBinding.taskDescriptionEditText.setTextColor(android.graphics.Color.WHITE)
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_task))
            .setView(dialogBinding.root)
            .create()
        
        // Set text colors to white for dark background
        dialog.setOnShowListener {
            val titleView = dialog.findViewById<android.widget.TextView>(android.R.id.title)
            titleView?.setTextColor(android.graphics.Color.WHITE)
            
            // Set button text colors to white
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(android.graphics.Color.WHITE)
        }
        
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.done)) { _, _ ->
                val title = dialogBinding.taskTitleEditText.text?.toString()?.trim()
                val description = dialogBinding.taskDescriptionEditText.text?.toString()?.trim() ?: ""
                
                if (!title.isNullOrEmpty()) {
                    // Capitalize first letter of title (English rules: first letter uppercase, rest as typed)
                    val capitalizedTitle = title.replaceFirstChar { 
                        if (it.isLowerCase()) it.uppercaseChar() else it 
                    }
                    viewModel.insertTask(personaId, capitalizedTitle, description, personaBackgroundColor)
                } else {
                    Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
                }
            }
        
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ ->
            // Cancel - do nothing
        }

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
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .setOnDismissListener {
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
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .setOnDismissListener {
                pendingSwipeTask = null
                pendingSwipePosition = -1
            }
            .show()
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

