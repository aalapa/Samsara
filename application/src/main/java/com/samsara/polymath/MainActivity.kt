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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.samsara.polymath.adapter.DailyTaskAdapter
import com.samsara.polymath.adapter.DailyTaskItem
import com.samsara.polymath.adapter.PersonaAdapter
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.Comment
import com.samsara.polymath.data.ExportData
import com.samsara.polymath.data.PersonaStatistics
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
    private lateinit var tagViewModel: com.samsara.polymath.viewmodel.TagViewModel
    private lateinit var adapter: PersonaAdapter
    private lateinit var dailyTaskAdapter: DailyTaskAdapter
    private var isTodayMode = false
    private val gson = Gson()
    
    private val prefs by lazy { 
        getSharedPreferences("samsara_prefs", android.content.Context.MODE_PRIVATE) 
    }

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
        
        // Check authentication
        val authManager = com.samsara.polymath.util.AuthManager(this)
        if (authManager.isAuthEnabled() && !authManager.isAuthenticated()) {
            // Redirect to lock screen
            val intent = Intent(this, LockActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PersonaViewModel::class.java]
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        tagViewModel = ViewModelProvider(this)[com.samsara.polymath.viewmodel.TagViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        setupMenuButton()
        setupRecyclerView()
        setupFilterChips()
        observePersonas()
        setupFab()
        
        // Auto-tag existing personas only once after migration
        if (!prefs.getBoolean("tags_auto_assigned", false)) {
            lifecycleScope.launch {
                val personaDao = AppDatabase.getDatabase(applicationContext).personaDao()
                com.samsara.polymath.util.autoTagExistingPersonas(personaDao, tagViewModel)
                prefs.edit().putBoolean("tags_auto_assigned", true).apply()
            }
        }
    }
    
    private var selectedFilterTagIds = mutableSetOf<Long>()
    
    private fun setupFilterChips() {
        tagViewModel.allTags.observe(this) { allTags ->
            binding.filterChipGroup.removeAllViews()

            // Always show chip bar (for Today chip at minimum)
            binding.filterChipsScrollView.visibility = View.VISIBLE

            // Add "Today" chip
            val todayChip = com.google.android.material.chip.Chip(this).apply {
                text = getString(R.string.today)
                isCheckable = true
                isChecked = isTodayMode
                chipBackgroundColor = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(android.graphics.Color.parseColor("#FF9500"), android.graphics.Color.parseColor("#E0E0E0"))
                )
            }
            binding.filterChipGroup.addView(todayChip)

            // Add "Show All" chip
            val showAllChip = com.google.android.material.chip.Chip(this).apply {
                text = getString(R.string.show_all)
                isCheckable = true
                isChecked = !isTodayMode && selectedFilterTagIds.isEmpty()
            }
            binding.filterChipGroup.addView(showAllChip)

            // Add tag filter chips
            allTags.forEach { tag ->
                val chip = com.google.android.material.chip.Chip(this).apply {
                    text = tag.name
                    isCheckable = true
                    isChecked = !isTodayMode && tag.id in selectedFilterTagIds

                    val chipBgColor = try {
                        if (tag.color != null) android.graphics.Color.parseColor(tag.color)
                        else android.graphics.Color.parseColor("#666666")
                    } catch (e: Exception) {
                        android.graphics.Color.parseColor("#666666")
                    }

                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(chipBgColor)
                    val textColor = if (isColorDark(chipBgColor)) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    setTextColor(textColor)
                }
                binding.filterChipGroup.addView(chip)
            }

            // Add "Untagged" chip
            val untaggedChip = com.google.android.material.chip.Chip(this).apply {
                text = getString(R.string.untagged)
                isCheckable = true
                isChecked = false
            }
            binding.filterChipGroup.addView(untaggedChip)

            // Helper to uncheck all chips except one
            fun uncheckAllExcept(except: com.google.android.material.chip.Chip) {
                for (i in 0 until binding.filterChipGroup.childCount) {
                    val c = binding.filterChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip
                    if (c != except) c?.isChecked = false
                }
            }

            // Wire Today chip
            todayChip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    isTodayMode = true
                    selectedFilterTagIds.clear()
                    uncheckAllExcept(todayChip)
                    switchToTodayMode()
                }
            }

            // Wire Show All chip
            showAllChip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    isTodayMode = false
                    selectedFilterTagIds.clear()
                    uncheckAllExcept(showAllChip)
                    switchToPersonasMode()
                    observePersonas()
                }
            }

            // Wire tag chips
            for (i in 2 until binding.filterChipGroup.childCount) {
                val chip = binding.filterChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip ?: continue
                val tagIndex = i - 2
                if (tagIndex < allTags.size) {
                    val tag = allTags[tagIndex]
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            isTodayMode = false
                            todayChip.isChecked = false
                            showAllChip.isChecked = false
                            selectedFilterTagIds.add(tag.id)
                            switchToPersonasMode()
                        } else {
                            selectedFilterTagIds.remove(tag.id)
                            if (selectedFilterTagIds.isEmpty()) showAllChip.isChecked = true
                        }
                        observePersonas()
                    }
                } else {
                    // Untagged chip
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            isTodayMode = false
                            todayChip.isChecked = false
                            showAllChip.isChecked = false
                            selectedFilterTagIds.add(-1L)
                            switchToPersonasMode()
                        } else {
                            selectedFilterTagIds.remove(-1L)
                            if (selectedFilterTagIds.isEmpty()) showAllChip.isChecked = true
                        }
                        observePersonas()
                    }
                }
            }
        }
    }

    private fun switchToTodayMode() {
        binding.personasRecyclerView.visibility = View.GONE
        binding.dailyTasksRecyclerView.visibility = View.VISIBLE
        binding.addPersonaFab.visibility = View.GONE
        observeDueTodayTasks()
    }

    private fun switchToPersonasMode() {
        binding.personasRecyclerView.visibility = View.VISIBLE
        binding.dailyTasksRecyclerView.visibility = View.GONE
        binding.emptyDailyTextView.visibility = View.GONE
        binding.addPersonaFab.visibility = View.VISIBLE
    }

    private fun observeDueTodayTasks() {
        // Build persona map for names/colors
        viewModel.getAllPersonasWithTaskCount().observe(this) { personasWithCount ->
            val personaMap = personasWithCount.associate {
                it.persona.id to Pair(it.persona.name, it.persona.backgroundColor)
            }

            taskViewModel.getDueTodayTasks().observe(this) { dueTasks ->
                if (!isTodayMode) return@observe
                val dailyItems = dueTasks.map { task ->
                    val (name, color) = personaMap[task.personaId] ?: ("Unknown" to "#007AFF")
                    DailyTaskItem(task, name, color)
                }
                dailyTaskAdapter.submitList(dailyItems)
                binding.emptyDailyTextView.visibility = if (dailyItems.isEmpty()) View.VISIBLE else View.GONE
                binding.dailyTasksRecyclerView.visibility = if (dailyItems.isEmpty()) View.GONE else View.VISIBLE
            }
        }
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
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_manage_tags -> {
                startActivity(Intent(this, TagManagementActivity::class.java))
                true
            }
            R.id.action_report -> {
                startActivity(Intent(this, PersonaReportActivity::class.java))
                true
            }
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
                // Navigate to tasks activity with persona background color
                TasksActivity.start(this, persona.id, persona.name, persona.backgroundColor)
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

        dailyTaskAdapter = DailyTaskAdapter(
            onTaskComplete = { task ->
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Complete Task")
                    .setMessage(getString(R.string.mark_complete_confirmation))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        taskViewModel.markTaskAsComplete(task)
                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            }
        )
        binding.dailyTasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.dailyTasksRecyclerView.adapter = dailyTaskAdapter
    }

    private fun observePersonas() {
        viewModel.getAllPersonasWithTaskCount().observe(this) { personasWithCount ->
            val filteredList = if (selectedFilterTagIds.isEmpty()) {
                // Show all personas
                personasWithCount
            } else if (-1L in selectedFilterTagIds) {
                // Show only untagged personas
                personasWithCount.filter { it.tags.isEmpty() }
            } else {
                // Show personas that have at least one of the selected tags
                personasWithCount.filter { personaWithCount ->
                    personaWithCount.tags.any { tag -> tag.id in selectedFilterTagIds }
                }
            }
            adapter.submitList(filteredList)
        }
    }

    private fun setupFab() {
        binding.addPersonaFab.setOnClickListener {
            showAddPersonaDialog()
        }
    }

    private fun showAddPersonaDialog() {
        val dialogBinding = DialogAddPersonaBinding.inflate(LayoutInflater.from(this))
        val selectedTagIds = mutableSetOf<Long>()
        
        // Observe all tags and populate the chip group
        tagViewModel.allTags.observe(this) { allTags ->
            dialogBinding.tagsChipGroup.removeAllViews()
            
            allTags.forEach { tag ->
                val chip = com.google.android.material.chip.Chip(this).apply {
                    text = tag.name
                    isCheckable = true
                    isChecked = false
                    
                    // Parse tag color if available
                    val chipBgColor = try {
                        if (tag.color != null) android.graphics.Color.parseColor(tag.color)
                        else android.graphics.Color.parseColor("#666666")
                    } catch (e: Exception) {
                        android.graphics.Color.parseColor("#666666")
                    }
                    
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(chipBgColor)
                    val textColor = if (isColorDark(chipBgColor)) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    setTextColor(textColor)
                    
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) selectedTagIds.add(tag.id)
                        else selectedTagIds.remove(tag.id)
                    }
                }
                dialogBinding.tagsChipGroup.addView(chip)
            }
        }
        
        // Handle "Add New Tag" button
        dialogBinding.addNewTagButton.setOnClickListener {
            showCreateTagDialog { newTagId ->
                selectedTagIds.add(newTagId)
            }
        }
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_persona))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                val name = dialogBinding.personaNameEditText.text?.toString()?.trim()
                if (!name.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val personaId = viewModel.insertPersonaSync(name)
                        tagViewModel.setTagsForPersona(personaId, selectedTagIds.toList())
                    }
                } else {
                    Toast.makeText(this, "Please enter a persona name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }
    
    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 
                           0.587 * android.graphics.Color.green(color) + 
                           0.114 * android.graphics.Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun showEditPersonaDialog(persona: com.samsara.polymath.data.Persona) {
        val dialogBinding = DialogAddPersonaBinding.inflate(LayoutInflater.from(this))
        dialogBinding.personaNameEditText.setText(persona.name)
        dialogBinding.personaNameEditText.selectAll()
        
        val selectedTagIds = mutableSetOf<Long>()
        
        // Observe all tags and persona's current tags
        tagViewModel.allTags.observe(this) { allTags ->
            tagViewModel.getTagsForPersona(persona.id).observe(this) { personaTags ->
                dialogBinding.tagsChipGroup.removeAllViews()
                
                // Pre-populate selected tags
                selectedTagIds.clear()
                selectedTagIds.addAll(personaTags.map { it.id })
                
                allTags.forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(this).apply {
                        text = tag.name
                        isCheckable = true
                        isChecked = tag.id in selectedTagIds
                        
                        val chipBgColor = try {
                            if (tag.color != null) android.graphics.Color.parseColor(tag.color)
                            else android.graphics.Color.parseColor("#666666")
                        } catch (e: Exception) {
                            android.graphics.Color.parseColor("#666666")
                        }
                        
                        chipBackgroundColor = android.content.res.ColorStateList.valueOf(chipBgColor)
                        val textColor = if (isColorDark(chipBgColor)) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                        setTextColor(textColor)
                        
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) selectedTagIds.add(tag.id)
                            else selectedTagIds.remove(tag.id)
                        }
                    }
                    dialogBinding.tagsChipGroup.addView(chip)
                }
            }
        }
        
        // Handle "Add New Tag" button
        dialogBinding.addNewTagButton.setOnClickListener {
            showCreateTagDialog { newTagId ->
                selectedTagIds.add(newTagId)
            }
        }
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.edit_persona))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.done)) { dialogInterface, _ ->
                val newName = dialogBinding.personaNameEditText.text?.toString()?.trim()
                if (!newName.isNullOrEmpty()) {
                    if (newName != persona.name) {
                        viewModel.updatePersonaName(persona.id, newName)
                    }
                    tagViewModel.setTagsForPersona(persona.id, selectedTagIds.toList())
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
        dialogBinding.personaNameEditText.requestFocus()
    }
    
    private fun showCreateTagDialog(onTagCreated: (Long) -> Unit) {
        val createTagBinding = com.samsara.polymath.databinding.DialogCreateTagBinding.inflate(LayoutInflater.from(this))
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.create_tag))
            .setView(createTagBinding.root)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                val tagName = createTagBinding.tagNameEditText.text?.toString()?.trim()
                val tagColor = createTagBinding.tagColorEditText.text?.toString()?.trim()
                
                if (!tagName.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val newTag = tagViewModel.createTag(tagName, tagColor)
                        newTag?.let { onTagCreated(it.id) }
                    }
                } else {
                    Toast.makeText(this, "Please enter a tag name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        
        dialog.show()
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

                    // Get all comments directly from the database
                    val allComments = AppDatabase.getDatabase(applicationContext)
                        .commentDao()
                        .getAllComments()

                    // Get all statistics for historical reports
                    val allStatistics = AppDatabase.getDatabase(applicationContext)
                        .personaStatisticsDao()
                        .getStatisticsSince(0) // Get all statistics

                    // Get all tags
                    val allTags = AppDatabase.getDatabase(applicationContext)
                        .tagDao()
                        .getAllTagsSync()

                    // Get all persona-tag associations
                    val allPersonaTags = AppDatabase.getDatabase(applicationContext)
                        .personaTagDao()
                        .getAllSync()
                        .map { com.samsara.polymath.data.PersonaTagExport(it.personaId, it.tagId, it.assignedAt) }

                    val exportData = ExportData(
                        personas = personas,
                        tasks = allTasks,
                        comments = allComments,
                        statistics = allStatistics,
                        tags = allTags,
                        personaTags = allPersonaTags
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
                    
                    // Import tags first and create ID mapping
                    val tagIdMap = mutableMapOf<Long, Long>() // oldId -> newId
                    val tagDao = AppDatabase.getDatabase(applicationContext).tagDao()
                    exportData.tags.forEach { oldTag ->
                        val newTagId = tagDao.insertTag(
                            oldTag.copy(id = 0) // Reset ID to auto-generate
                        )
                        tagIdMap[oldTag.id] = newTagId
                    }
                    
                    // Import personas and create ID mapping
                    val personaIdMap = mutableMapOf<Long, Long>()
                    exportData.personas.sortedBy { it.order }.forEach { oldPersona ->
                        val newId = viewModel.insertPersonaSync(
                            oldPersona.copy(id = 0) // Reset ID to auto-generate
                        )
                        personaIdMap[oldPersona.id] = newId
                    }
                    
                    // Import tasks with new persona IDs, grouped by persona
                    val taskIdMap = mutableMapOf<Long, Long>() // Map old task IDs to new task IDs
                    val tasksByPersona = exportData.tasks.groupBy { it.personaId }
                    tasksByPersona.forEach { (oldPersonaId, tasks) ->
                        val newPersonaId = personaIdMap[oldPersonaId] ?: return@forEach
                        tasks.sortedBy { it.order }.forEachIndexed { index, oldTask ->
                            val newTaskId = taskViewModel.insertTaskSync(
                                personaId = newPersonaId,
                                title = oldTask.title,
                                description = oldTask.description,
                                order = index,
                                isCompleted = oldTask.isCompleted,
                                completedAt = oldTask.completedAt,
                                backgroundColor = oldTask.backgroundColor,
                                createdAt = oldTask.createdAt,
                                isRecurring = oldTask.isRecurring,
                                recurringFrequency = oldTask.recurringFrequency,
                                recurringDays = oldTask.recurringDays
                            )
                            taskIdMap[oldTask.id] = newTaskId
                        }
                    }

                    // Import comments with new task IDs
                    val commentDao = AppDatabase.getDatabase(applicationContext).commentDao()
                    exportData.comments.forEach { oldComment ->
                        val newTaskId = taskIdMap[oldComment.taskId]
                        if (newTaskId != null) {
                            commentDao.insertComment(
                                Comment(
                                    taskId = newTaskId,
                                    text = oldComment.text,
                                    createdAt = oldComment.createdAt
                                )
                            )
                        }
                    }

                    // Import statistics with new persona IDs
                    val statisticsDao = AppDatabase.getDatabase(applicationContext).personaStatisticsDao()
                    exportData.statistics.forEach { oldStat ->
                        val newPersonaId = personaIdMap[oldStat.personaId]
                        if (newPersonaId != null) {
                            statisticsDao.insertStatistics(
                                PersonaStatistics(
                                    id = 0, // Auto-generate new ID
                                    personaId = newPersonaId,
                                    timestamp = oldStat.timestamp,
                                    openCount = oldStat.openCount,
                                    totalTasks = oldStat.totalTasks,
                                    completedTasks = oldStat.completedTasks,
                                    score = oldStat.score
                                )
                            )
                        }
                    }

                    // Import persona-tag associations with new IDs
                    val personaTagDao = AppDatabase.getDatabase(applicationContext).personaTagDao()
                    exportData.personaTags.forEach { personaTag ->
                        val newPersonaId = personaIdMap[personaTag.personaId]
                        val newTagId = tagIdMap[personaTag.tagId]
                        
                        if (newPersonaId != null && newTagId != null) {
                            personaTagDao.insertPersonaTag(
                                com.samsara.polymath.data.PersonaTag(
                                    personaId = newPersonaId,
                                    tagId = newTagId,
                                    assignedAt = personaTag.assignedAt
                                )
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
    
    override fun onStop() {
        super.onStop()
        // Lock app when going to background
        val authManager = com.samsara.polymath.util.AuthManager(this)
        if (authManager.isAuthEnabled()) {
            authManager.lockApp()
        }
    }
}

