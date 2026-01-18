package com.samsara.polymath

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.samsara.polymath.adapter.PersonaAdapter
import com.samsara.polymath.data.ExportData
import com.samsara.polymath.databinding.ActivityMainBinding
import com.samsara.polymath.databinding.DialogAddPersonaBinding
import com.samsara.polymath.viewmodel.PersonaViewModel
import com.samsara.polymath.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PersonaViewModel
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var adapter: PersonaAdapter
    private val gson = Gson()

    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { exportData(it) }
    }

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { importData(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PersonaViewModel::class.java]
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        setupMenuButton()
        setupRecyclerView()
        observePersonas()
        setupFab()
    }
    
    private fun setupMenuButton() {
        binding.menuButton.setOnClickListener { view ->
            val contextWrapper = android.view.ContextThemeWrapper(this, R.style.PopupMenuTheme)
            val popup = androidx.appcompat.widget.PopupMenu(contextWrapper, view)
            popup.menuInflater.inflate(R.menu.main_menu, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
            }
            popup.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Menu is handled by PopupMenu, but keeping this for compatibility
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportData()
                true
            }
            R.id.action_import -> {
                showImportConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = PersonaAdapter(
            onPersonaClick = { persona ->
                // Increment open count when persona is opened
                viewModel.incrementOpenCount(persona.id)
                // Navigate to tasks activity
                TasksActivity.start(this, persona.id, persona.name)
            },
            onPersonaEdit = { persona ->
                showEditPersonaDialog(persona)
            },
            onPersonaDelete = { persona ->
                showDeletePersonaConfirmation(persona)
            }
        )

        binding.personasRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.personasRecyclerView.adapter = adapter
        
        // Setup ItemTouchHelper for drag and drop reordering
        val itemTouchHelper = ItemTouchHelper(createItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(binding.personasRecyclerView)
    }
    
    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0 // No swipe gestures for personas
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

                // Update order values - set order to index+1 to mark as manually arranged
                // (0 means not manually arranged, will use openCount for sorting)
                currentList.forEachIndexed { index, persona ->
                    val newOrder = index + 1
                    if (persona.order != newOrder) {
                        viewModel.updatePersonaOrder(persona.id, newOrder)
                    }
                }

                adapter.submitList(currentList)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swipe for personas
            }
        }
    }

    private fun observePersonas() {
        viewModel.getAllPersonas().observe(this) { personas ->
            adapter.submitList(personas)
        }
    }

    private fun setupFab() {
        binding.addPersonaFab.setOnClickListener {
            showAddPersonaDialog()
        }
    }

    private fun showAddPersonaDialog() {
        val dialogBinding = DialogAddPersonaBinding.inflate(LayoutInflater.from(this))
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_persona))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                val name = dialogBinding.personaNameEditText.text?.toString()?.trim()
                if (!name.isNullOrEmpty()) {
                    viewModel.insertPersona(name)
                } else {
                    Toast.makeText(this, "Please enter a persona name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun showEditPersonaDialog(persona: com.samsara.polymath.data.Persona) {
        val dialogBinding = DialogAddPersonaBinding.inflate(LayoutInflater.from(this))
        dialogBinding.personaNameEditText.setText(persona.name)
        // Select all text for easy editing
        dialogBinding.personaNameEditText.selectAll()
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.edit_persona))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.done)) { dialogInterface, _ ->
                val newName = dialogBinding.personaNameEditText.text?.toString()?.trim()
                if (!newName.isNullOrEmpty()) {
                    if (newName != persona.name) {
                        viewModel.updatePersonaName(persona.id, newName)
                    }
                } else {
                    Toast.makeText(this, "Persona name cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
        
        // Request focus and show keyboard
        dialogBinding.personaNameEditText.requestFocus()
    }

    private fun showDeletePersonaConfirmation(persona: com.samsara.polymath.data.Persona) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_persona))
            .setMessage(getString(R.string.delete_persona_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deletePersona(persona)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun exportData() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "samsara_backup_$timestamp.json"
        createFileLauncher.launch(fileName)
    }

    private fun exportData(uri: Uri) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val personas = viewModel.getAllPersonasSync()
                    val allTasks = mutableListOf<com.samsara.polymath.data.Task>()
                    
                    personas.forEach { persona ->
                        val tasks = taskViewModel.getTasksByPersonaSync(persona.id)
                        allTasks.addAll(tasks)
                    }

                    val exportData = ExportData(
                        personas = personas,
                        tasks = allTasks
                    )

                    val json = gson.toJson(exportData)
                    
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                            writer.write(json)
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, getString(R.string.export_success), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "${getString(R.string.export_error)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showImportConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.import_data))
            .setMessage(getString(R.string.import_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                openFileLauncher.launch(arrayOf("application/json", "text/plain"))
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun importData(uri: Uri) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val json = contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.readText()
                        }
                    } ?: throw IOException("Could not read file")

                    val exportData = gson.fromJson(json, ExportData::class.java)

                    // Delete all existing data
                    viewModel.deleteAllPersonas()
                    
                    // Import personas first and create ID mapping
                    val personaIdMap = mutableMapOf<Long, Long>()
                    exportData.personas.sortedBy { it.order }.forEach { oldPersona ->
                        val newId = viewModel.insertPersonaSync(
                            oldPersona.copy(id = 0) // Reset ID to auto-generate
                        )
                        personaIdMap[oldPersona.id] = newId
                    }
                    
                    // Import tasks with new persona IDs, grouped by persona
                    val tasksByPersona = exportData.tasks.groupBy { it.personaId }
                    tasksByPersona.forEach { (oldPersonaId, tasks) ->
                        val newPersonaId = personaIdMap[oldPersonaId] ?: return@forEach
                        tasks.sortedBy { it.order }.forEachIndexed { index, oldTask ->
                            taskViewModel.insertTaskSync(
                                newPersonaId,
                                oldTask.title,
                                oldTask.description,
                                order = index,
                                isCompleted = oldTask.isCompleted,
                                completedAt = oldTask.completedAt
                            )
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, getString(R.string.import_success), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "${getString(R.string.import_error)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

