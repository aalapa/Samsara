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
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.samsara.polymath.adapter.DailyTaskAdapter
import com.samsara.polymath.adapter.DailyTaskItem
import com.samsara.polymath.adapter.GtdListItem
import com.samsara.polymath.adapter.PersonaAdapter
import com.samsara.polymath.data.DecayLevel
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
    private var isFocusMode = true // Default to Focus view on launch
    private var isChakraMode = false
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

        // Apply persisted theme before any view inflation
        val appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            if (appPrefs.getBoolean("dark_mode", false)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

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
    private var expandedTagId: Long? = null // Track which tag chip is expanded

    private fun abbreviateTag(name: String): String {
        return if (name.length <= 3) name
        else name.take(3).replaceFirstChar { it.uppercaseChar() }
    }

    /** Update a tag chip's text: show full name when expanded or checked, abbreviation otherwise */
    private fun updateTagChipText(chip: com.google.android.material.chip.Chip, tagId: Long, fullName: String) {
        val isExpanded = expandedTagId == tagId || chip.isChecked
        chip.text = if (isExpanded) fullName else abbreviateTag(fullName)
    }

    /** Refresh all tag chip labels based on current expanded/checked state */
    private fun collapseOtherTagChips(allTags: List<com.samsara.polymath.data.Tag>) {
        val tagChipStartIndex = 0
        for (i in 0 until allTags.size) {
            val chipIndex = tagChipStartIndex + i
            val chip = binding.filterChipGroup.getChildAt(chipIndex) as? com.google.android.material.chip.Chip ?: continue
            val tag = allTags[i]
            updateTagChipText(chip, tag.id, tag.name)
        }
        // Also handle Untagged chip (last chip)
        val untaggedIndex = tagChipStartIndex + allTags.size
        val untaggedChip = binding.filterChipGroup.getChildAt(untaggedIndex) as? com.google.android.material.chip.Chip
        if (untaggedChip != null) {
            val isExpanded = expandedTagId == -1L || untaggedChip.isChecked
            untaggedChip.text = if (isExpanded) getString(R.string.untagged) else abbreviateTag(getString(R.string.untagged))
        }
    }

    private fun setupFilterChips() {
        // Wire segmented control buttons
        updateSegmentVisuals()
        binding.segAll.setOnClickListener {
            isTodayMode = false; isFocusMode = false; isChakraMode = false
            selectedFilterTagIds.clear(); expandedTagId = null
            clearTagChipSelections(); updateSegmentVisuals()
            switchToPersonasMode(); observePersonas()
        }
        binding.segGtd.setOnClickListener {
            isTodayMode = true; isFocusMode = false; isChakraMode = false
            selectedFilterTagIds.clear(); expandedTagId = null
            clearTagChipSelections(); updateSegmentVisuals()
            switchToTodayMode()
        }
        binding.segFocus.setOnClickListener {
            isFocusMode = true; isTodayMode = false; isChakraMode = false
            selectedFilterTagIds.clear(); expandedTagId = null
            clearTagChipSelections(); updateSegmentVisuals()
            switchToPersonasMode(); observePersonas()
        }
        binding.segChakra.setOnClickListener {
            isChakraMode = true; isTodayMode = false; isFocusMode = false
            selectedFilterTagIds.clear(); expandedTagId = null
            clearTagChipSelections(); updateSegmentVisuals()
            switchToPersonasMode(); observePersonas()
        }

        // Tag chips
        tagViewModel.allTags.observe(this) { allTags ->
            binding.filterChipGroup.removeAllViews()
            binding.filterChipsScrollView.visibility = View.VISIBLE

            allTags.forEach { tag ->
                val isSelected = !isTodayMode && !isFocusMode && !isChakraMode && tag.id in selectedFilterTagIds
                val tagColor = try {
                    if (tag.color != null) android.graphics.Color.parseColor(tag.color)
                    else android.graphics.Color.parseColor("#666666")
                } catch (_: Exception) { android.graphics.Color.parseColor("#666666") }

                val chip = com.google.android.material.chip.Chip(this).apply {
                    text = if (isSelected || expandedTagId == tag.id) tag.name else abbreviateTag(tag.name)
                    isCheckable = true
                    isChecked = isSelected
                    chipMinHeight = 28f * resources.displayMetrics.density
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                    chipStrokeWidth = resources.displayMetrics.density
                    chipStrokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.hairline))
                    setTextColor(getColor(R.color.ink))
                    chipIconSize = 6f * resources.displayMetrics.density
                    chipIconTint = android.content.res.ColorStateList.valueOf(tagColor)
                    chipIcon = getDrawable(R.drawable.tag_circle)
                }
                binding.filterChipGroup.addView(chip)
            }

            val isUntaggedSelected = !isTodayMode && !isFocusMode && !isChakraMode && -1L in selectedFilterTagIds
            val untaggedChip = com.google.android.material.chip.Chip(this).apply {
                text = if (isUntaggedSelected || expandedTagId == -1L) getString(R.string.untagged) else abbreviateTag(getString(R.string.untagged))
                isCheckable = true
                isChecked = isUntaggedSelected
                chipMinHeight = 28f * resources.displayMetrics.density
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                chipStrokeWidth = resources.displayMetrics.density
                chipStrokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.hairline))
                setTextColor(getColor(R.color.ink))
            }
            binding.filterChipGroup.addView(untaggedChip)

            // Wire listeners
            for (i in 0 until binding.filterChipGroup.childCount) {
                val chip = binding.filterChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip ?: continue
                if (i < allTags.size) {
                    val tag = allTags[i]
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            isTodayMode = false; isFocusMode = false; isChakraMode = false
                            selectedFilterTagIds.add(tag.id)
                            expandedTagId = tag.id
                            chip.text = tag.name
                            updateSegmentVisuals()
                            switchToPersonasMode()
                        } else {
                            selectedFilterTagIds.remove(tag.id)
                            if (expandedTagId == tag.id) expandedTagId = null
                            chip.text = abbreviateTag(tag.name)
                            if (selectedFilterTagIds.isEmpty()) updateSegmentVisuals()
                        }
                        observePersonas()
                    }
                    chip.setOnLongClickListener {
                        if (!chip.isChecked) {
                            expandedTagId = if (expandedTagId == tag.id) null else tag.id
                            collapseOtherTagChips(allTags)
                        }
                        true
                    }
                } else {
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            isTodayMode = false; isFocusMode = false; isChakraMode = false
                            selectedFilterTagIds.add(-1L)
                            expandedTagId = -1L
                            chip.text = getString(R.string.untagged)
                            updateSegmentVisuals()
                            switchToPersonasMode()
                        } else {
                            selectedFilterTagIds.remove(-1L)
                            if (expandedTagId == -1L) expandedTagId = null
                            chip.text = abbreviateTag(getString(R.string.untagged))
                            if (selectedFilterTagIds.isEmpty()) updateSegmentVisuals()
                        }
                        observePersonas()
                    }
                    chip.setOnLongClickListener {
                        if (!chip.isChecked) {
                            expandedTagId = if (expandedTagId == -1L) null else -1L
                            collapseOtherTagChips(allTags)
                        }
                        true
                    }
                }
            }
        }
    }

    private fun updateSegmentVisuals() {
        val segCorner = 9f * resources.displayMetrics.density
        val isAllActive = !isTodayMode && !isFocusMode && !isChakraMode && selectedFilterTagIds.isEmpty()
        listOf(
            Triple(binding.segAll, isAllActive, null as Int?),
            Triple(binding.segGtd, isTodayMode, R.color.mode_gtd),
            Triple(binding.segFocus, isFocusMode, R.color.mode_focus),
            Triple(binding.segChakra, isChakraMode, R.color.mode_chakra)
        ).forEach { (tv, active, colorRes) ->
            if (active) {
                tv.background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (colorRes != null) getColor(colorRes) else getColor(R.color.ink))
                    cornerRadius = segCorner
                }
                tv.setTextColor(getColor(R.color.surface))
            } else {
                tv.background = null
                tv.setTextColor(getColor(R.color.ink_muted))
            }
        }
    }

    private fun clearTagChipSelections() {
        for (i in 0 until binding.filterChipGroup.childCount) {
            (binding.filterChipGroup.getChildAt(i) as? com.google.android.material.chip.Chip)?.isChecked = false
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
        viewModel.getAllPersonasWithTaskCount().observe(this) { personasWithCount ->
            val personaMap = personasWithCount.associate {
                it.persona.id to Pair(it.persona.name, it.persona.backgroundColor)
            }

            taskViewModel.getOverdueTasks().observe(this) { overdueTasks ->
                if (!isTodayMode) return@observe
                taskViewModel.getDueTodayTasks().observe(this) { dueTasks ->
                    if (!isTodayMode) return@observe
                    taskViewModel.getUpcomingTasks().observe(this) innerObserve@{ upcomingTasks ->
                        if (!isTodayMode) return@innerObserve
                        val gtdItems = mutableListOf<GtdListItem>()
                        val usedIds = mutableSetOf<Long>()

                        // Overdue first
                        if (overdueTasks.isNotEmpty()) {
                            gtdItems.add(GtdListItem.Header(getString(R.string.overdue)))
                            overdueTasks.forEach { task ->
                                usedIds.add(task.id)
                                val (name, color) = personaMap[task.personaId] ?: ("Unknown" to "#007AFF")
                                gtdItems.add(GtdListItem.TaskItem(DailyTaskItem(task, name, color)))
                            }
                        }

                        // Due Today
                        val filteredToday = dueTasks.filter { it.id !in usedIds }
                        if (filteredToday.isNotEmpty()) {
                            gtdItems.add(GtdListItem.Header(getString(R.string.due_today)))
                            filteredToday.forEach { task ->
                                usedIds.add(task.id)
                                val (name, color) = personaMap[task.personaId] ?: ("Unknown" to "#007AFF")
                                gtdItems.add(GtdListItem.TaskItem(DailyTaskItem(task, name, color)))
                            }
                        }

                        // Upcoming
                        val filteredUpcoming = upcomingTasks.filter { it.id !in usedIds }
                        if (filteredUpcoming.isNotEmpty()) {
                            gtdItems.add(GtdListItem.Header(getString(R.string.upcoming)))
                            filteredUpcoming.forEach { task ->
                                val (name, color) = personaMap[task.personaId] ?: ("Unknown" to "#007AFF")
                                gtdItems.add(GtdListItem.TaskItem(DailyTaskItem(task, name, color)))
                            }
                        }

                        dailyTaskAdapter.submitList(gtdItems)
                        binding.emptyDailyTextView.text = getString(R.string.no_gtd_tasks)
                        binding.emptyDailyTextView.visibility = if (gtdItems.isEmpty()) View.VISIBLE else View.GONE
                        binding.dailyTasksRecyclerView.visibility = if (gtdItems.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
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
            },
            onPersonaToggleFocus = { persona ->
                if (!persona.isFocused) {
                    val currentFocusedCount = adapter.countPersonas { it.persona.isFocused }
                    if (currentFocusedCount >= 7) {
                        Toast.makeText(this, getString(R.string.focus_limit_reached), Toast.LENGTH_SHORT).show()
                        return@PersonaAdapter
                    }
                }
                viewModel.toggleFocus(persona.id, !persona.isFocused)
            },
            onPersonaToggleChakra = { persona ->
                if (!persona.isChakra) {
                    val currentChakraCount = adapter.countPersonas { it.persona.isChakra }
                    if (currentChakraCount >= 7) {
                        Toast.makeText(this, getString(R.string.chakra_limit_reached), Toast.LENGTH_SHORT).show()
                        return@PersonaAdapter
                    }
                }
                viewModel.toggleChakra(persona.id, !persona.isChakra)
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
            val filteredList = when {
                isFocusMode -> personasWithCount.filter { it.persona.isFocused }
                isChakraMode -> personasWithCount.filter { it.persona.isChakra }
                selectedFilterTagIds.isEmpty() -> personasWithCount
                -1L in selectedFilterTagIds -> personasWithCount.filter { it.tags.isEmpty() }
                else -> personasWithCount.filter { personaWithCount ->
                    personaWithCount.tags.any { tag -> tag.id in selectedFilterTagIds }
                }
            }

            val active = filteredList.filter { it.decayLevel != DecayLevel.SERIOUS }
            val archived = filteredList.filter { it.decayLevel == DecayLevel.SERIOUS }
            adapter.setPersonas(active, archived)

            if (isFocusMode && filteredList.isEmpty()) {
                binding.emptyDailyTextView.text = getString(R.string.no_focused_personas)
                binding.emptyDailyTextView.visibility = View.VISIBLE
                binding.personasRecyclerView.visibility = View.GONE
            } else if (isChakraMode && filteredList.isEmpty()) {
                binding.emptyDailyTextView.text = getString(R.string.no_chakra_personas)
                binding.emptyDailyTextView.visibility = View.VISIBLE
                binding.personasRecyclerView.visibility = View.GONE
            } else if (!isTodayMode) {
                binding.emptyDailyTextView.visibility = View.GONE
                binding.personasRecyclerView.visibility = View.VISIBLE
            }
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

                    // Get all completed time entries
                    val allTimeEntries = AppDatabase.getDatabase(applicationContext)
                        .timeEntryDao()
                        .getAllCompletedEntries()

                    // Get all persona open events (last 91 days)
                    val sinceMillis = System.currentTimeMillis() - 91L * 86400000L
                    val allOpenEvents = AppDatabase.getDatabase(applicationContext)
                        .personaOpenEventDao()
                        .getAllEventsSince(sinceMillis)

                    val exportData = ExportData(
                        personas = personas,
                        tasks = allTasks,
                        comments = allComments,
                        statistics = allStatistics,
                        tags = allTags,
                        personaTags = allPersonaTags,
                        timeEntries = allTimeEntries,
                        personaOpenEvents = allOpenEvents
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
                    val tagDao = AppDatabase.getDatabase(applicationContext).tagDao()
                    viewModel.deleteAllPersonas()
                    tagDao.deleteAllTags() // Clear tags to avoid duplicates on re-import

                    // Import tags first and create ID mapping
                    val tagIdMap = mutableMapOf<Long, Long>() // oldId -> newId
                    exportData.tags.forEach { oldTag ->
                        val newTagId = tagDao.insertTag(
                            oldTag.copy(id = 0) // Reset ID to auto-generate
                        )
                        tagIdMap[oldTag.id] = newTagId
                    }
                    
                    // Import personas and create ID mapping
                    val personaIdMap = mutableMapOf<Long, Long>()
                    val importTime = System.currentTimeMillis()
                    exportData.personas.sortedBy { it.order }.forEach { oldPersona ->
                        // Preserve openCount; refresh lastOpenedAt so decay doesn't penalise restored data
                        val newId = viewModel.insertPersonaSync(
                            oldPersona.copy(id = 0, lastOpenedAt = importTime)
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
                                recurringDays = oldTask.recurringDays,
                                endDate = oldTask.endDate,
                                nextDueDate = oldTask.nextDueDate
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

                    // Import time entries with new task IDs
                    val timeEntryDao = AppDatabase.getDatabase(applicationContext).timeEntryDao()
                    exportData.timeEntries.forEach { oldEntry ->
                        val newTaskId = taskIdMap[oldEntry.taskId]
                        if (newTaskId != null) {
                            timeEntryDao.insert(
                                com.samsara.polymath.data.TimeEntry(
                                    taskId = newTaskId,
                                    startTime = oldEntry.startTime,
                                    endTime = oldEntry.endTime
                                )
                            )
                        }
                    }

                    // Import persona open events with new persona IDs
                    val personaOpenEventDao = AppDatabase.getDatabase(applicationContext).personaOpenEventDao()
                    exportData.personaOpenEvents.forEach { oldEvent ->
                        val newPersonaId = personaIdMap[oldEvent.personaId]
                        if (newPersonaId != null) {
                            personaOpenEventDao.insert(
                                com.samsara.polymath.data.PersonaOpenEvent(
                                    personaId = newPersonaId,
                                    timestamp = oldEvent.timestamp
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

