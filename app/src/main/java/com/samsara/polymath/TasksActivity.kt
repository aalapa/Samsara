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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personaId = intent.getLongExtra(EXTRA_PERSONA_ID, -1)
        personaName = intent.getStringExtra(EXTRA_PERSONA_NAME) ?: "Tasks"

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

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        // Delete
                        showDeleteConfirmation(task)
                        adapter.notifyItemChanged(position) // Restore the view
                    }
                    ItemTouchHelper.RIGHT -> {
                        // Mark as complete
                        if (!task.isCompleted) {
                            showCompleteConfirmation(task)
                        }
                        adapter.notifyItemChanged(position) // Restore the view
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
            adapter.submitList(tasks)
        }
    }

    private fun setupFab() {
        binding.addTaskFab.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_task))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                val title = dialogBinding.taskTitleEditText.text?.toString()?.trim()
                val description = dialogBinding.taskDescriptionEditText.text?.toString()?.trim() ?: ""
                
                if (!title.isNullOrEmpty()) {
                    viewModel.insertTask(personaId, title, description)
                } else {
                    Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmation(task: com.samsara.polymath.data.Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Task")
            .setMessage(getString(R.string.delete_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteTask(task)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showCompleteConfirmation(task: com.samsara.polymath.data.Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Complete Task")
            .setMessage(getString(R.string.mark_complete_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.markTaskAsComplete(task)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    companion object {
        const val EXTRA_PERSONA_ID = "persona_id"
        const val EXTRA_PERSONA_NAME = "persona_name"

        fun start(activity: AppCompatActivity, personaId: Long, personaName: String) {
            val intent = android.content.Intent(activity, TasksActivity::class.java).apply {
                putExtra(EXTRA_PERSONA_ID, personaId)
                putExtra(EXTRA_PERSONA_NAME, personaName)
            }
            activity.startActivity(intent)
        }
    }
}

