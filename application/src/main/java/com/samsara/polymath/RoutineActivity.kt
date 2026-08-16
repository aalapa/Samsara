package com.samsara.polymath

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samsara.polymath.adapter.RoutineAdapter
import com.samsara.polymath.data.RoutineTaskStats
import com.samsara.polymath.data.ScheduledDays
import com.samsara.polymath.data.Task
import com.samsara.polymath.databinding.ActivityRoutineBinding
import com.samsara.polymath.databinding.DialogAddRoutineTaskBinding
import com.samsara.polymath.viewmodel.RoutineViewModel
import kotlinx.coroutines.launch

class RoutineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoutineBinding
    private lateinit var viewModel: RoutineViewModel
    private lateinit var adapter: RoutineAdapter

    private var personaId = 0L
    private var personaName = ""
    private var personaColor = "#16171A"

    private var allTasks: List<Task> = emptyList()
    private var currentTasks: List<Task> = emptyList()
    private var currentCompletedIds: Set<Long> = emptySet()
    private val statCache = mutableMapOf<Long, RoutineTaskStats>()

    private var isManageMode = false
    private var isBarMode = true   // true = compact bars, false = detail stats
    private var manageMenuItem: MenuItem? = null
    private var viewToggleMenuItem: MenuItem? = null

    // SharedPreferences
    private val prefs by lazy { getSharedPreferences("routine_prefs", Context.MODE_PRIVATE) }

    // ── Sections ─────────────────────────────────────────────────────────────

    private var sectionsList: MutableList<String> = mutableListOf()

    private fun sectionsKey() = "sections_${personaId}"

    private fun defaultSections() =
        mutableListOf("MORNING", "AFTERNOON", "EVENING", "NIGHT")

    private fun loadSections(): MutableList<String> {
        val json = prefs.getString(sectionsKey(), null) ?: return defaultSections()
        return try {
            val arr = JSONArray(json)
            MutableList(arr.length()) { arr.getString(it) }
        } catch (_: Exception) { defaultSections() }
    }

    private fun saveSections(sections: List<String>) {
        prefs.edit().putString(sectionsKey(), JSONArray(sections).toString()).apply()
        sectionsList = sections.toMutableList()
    }

    // ── Chunk colors ──────────────────────────────────────────────────────────

    private fun chunkColorKey(chunk: String) = "chunk_color_${personaId}_$chunk"

    private fun getChunkColor(chunk: String): Int? {
        val stored = prefs.getInt(chunkColorKey(chunk), Int.MIN_VALUE)
        return if (stored == Int.MIN_VALUE) null else stored
    }

    private fun setChunkColor(chunk: String, color: Int) {
        prefs.edit().putInt(chunkColorKey(chunk), color).apply()
    }

    private fun chunkColorsMap(): Map<String, Int> =
        sectionsList
            .mapNotNull { chunk -> getChunkColor(chunk)?.let { chunk to it } }
            .toMap()

    /**
     * Returns the background color for the nth task (1-based) in a chunk.
     * Uses the chunk's base color lightened progressively by task position.
     */
    private fun taskColorForSlot(chunk: String, slot: Int): String {
        val base = getChunkColor(chunk) ?: try {
            Color.parseColor(personaColor)
        } catch (_: Exception) { Color.parseColor("#16171A") }
        val step = slot * 12
        val r = (Color.red(base) + step).coerceAtMost(255)
        val g = (Color.green(base) + step).coerceAtMost(255)
        val b = (Color.blue(base) + step).coerceAtMost(255)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoutineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personaId    = intent.getLongExtra(EXTRA_PERSONA_ID, 0L)
        personaName  = intent.getStringExtra(EXTRA_PERSONA_NAME) ?: ""
        personaColor = intent.getStringExtra(EXTRA_PERSONA_COLOR) ?: "#16171A"
        sectionsList = loadSections()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = personaName

        viewModel = ViewModelProvider(this)[RoutineViewModel::class.java]

        adapter = RoutineAdapter(
            onTaskTap         = { task -> handleTaskTap(task) },
            onTaskEdit        = { task -> showEditTaskDialog(task) },
            onChunkColorTap   = { chunk, current -> showChunkColorPicker(chunk, current) }
        )
        adapter.isBarMode = isBarMode
        binding.routineRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.routineRecyclerView.adapter = adapter

        createItemTouchHelper().attachToRecyclerView(binding.routineRecyclerView)
        binding.addTaskFab.setOnClickListener { showAddTaskDialog() }

        observeData()
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_routine, menu)
        viewToggleMenuItem = menu.findItem(R.id.action_view_toggle)
        manageMenuItem     = menu.findItem(R.id.action_manage)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.action_view_toggle -> {
            isBarMode = !isBarMode
            viewToggleMenuItem?.title = if (isBarMode) "Detail" else "Compact"
            adapter.isBarMode = isBarMode
            adapter.notifyDataSetChanged()
            true
        }
        R.id.action_manage -> {
            isManageMode = !isManageMode
            manageMenuItem?.title = if (isManageMode) "Today" else "Manage"
            adapter.isManageMode = isManageMode
            applyModeFilter()
            true
        }
        R.id.action_sections -> { showManageSectionsDialog(); true }
        else -> super.onOptionsItemSelected(item)
    }

    // ── Data observation ─────────────────────────────────────────────────────

    private fun observeData() {
        viewModel.getTasksForPersona(personaId).observe(this) { tasks ->
            allTasks = tasks
            applyModeFilter()
        }
        viewModel.getCompletedIdsForToday().observe(this) { ids ->
            currentCompletedIds = ids.toSet()
            refreshAdapter()
        }
    }

    private fun applyModeFilter() {
        currentTasks = if (isManageMode) allTasks
                       else allTasks.filter { ScheduledDays.isScheduledToday(it.scheduledDays) }
        loadMissingStats(currentTasks)
    }

    private fun loadMissingStats(tasks: List<Task>) {
        val missing = tasks.filter { it.id !in statCache }
        if (missing.isEmpty()) { refreshAdapter(); return }
        lifecycleScope.launch {
            missing.forEach { task -> statCache[task.id] = viewModel.loadStats(task) }
            refreshAdapter()
        }
    }

    private fun refreshAdapter() {
        adapter.sections = sectionsList
        adapter.chunkColors = chunkColorsMap()
        adapter.submit(currentTasks, currentCompletedIds, statCache.toMap())
        updateProgress()
    }

    private fun updateProgress() {
        if (isManageMode) {
            binding.progressBar.visibility = android.view.View.GONE
            binding.progressTextView.text = "${allTasks.size} tasks"
            return
        }
        binding.progressBar.visibility = android.view.View.VISIBLE
        val total = currentTasks.size
        val done  = currentTasks.count { it.id in currentCompletedIds }
        val pct   = if (total > 0) done * 100 / total else 0
        binding.progressBar.progress = pct
        binding.progressTextView.text = "$done / $total"
    }

    // ── Task tap (toggle complete) ────────────────────────────────────────────

    private fun handleTaskTap(task: Task) {
        viewModel.toggleCompletion(task) { stats ->
            statCache[task.id] = stats
            refreshAdapter()
        }
    }

    // ── Chunk color picker ────────────────────────────────────────────────────

    private fun showChunkColorPicker(chunk: String, currentColor: Int) {
        val palette = listOf(
            "#16171A", "#1B2B4B", "#1E3A5F", "#14372E", "#2D1B4E",
            "#4A1942", "#5C1A1A", "#2563EB", "#7C3AED", "#DB2777",
            "#059669", "#D97706", "#DC2626", "#0891B2", "#65A30D"
        )

        val density = resources.displayMetrics.density
        val swatchSize = (44 * density).toInt()
        val gap = (8 * density).toInt()
        val pad = (16 * density).toInt()
        var selectedColor = currentColor
        val swatches = mutableListOf<android.view.View>()

        // 3 rows × 5 columns grid using nested LinearLayouts
        val grid = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(pad, pad, pad, gap)
        }

        fun refreshStrokes() {
            swatches.forEach { s ->
                val bg = s.background as? android.graphics.drawable.GradientDrawable ?: return@forEach
                if (s.tag == selectedColor) bg.setStroke((3 * density).toInt(), Color.WHITE)
                else bg.setStroke(0, Color.TRANSPARENT)
            }
        }

        palette.chunked(5).forEach { rowColors ->
            val row = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = gap }
            }
            rowColors.forEach { hex ->
                val color = try { Color.parseColor(hex) } catch (_: Exception) { Color.GRAY }
                val swatch = android.view.View(this).apply {
                    tag = color
                    layoutParams = android.widget.LinearLayout.LayoutParams(swatchSize, swatchSize)
                        .also { it.marginEnd = gap }
                    background = android.graphics.drawable.GradientDrawable().apply {
                        shape = android.graphics.drawable.GradientDrawable.OVAL
                        setColor(color)
                        if (color == selectedColor) setStroke((3 * density).toInt(), Color.WHITE)
                    }
                    setOnClickListener { selectedColor = color; refreshStrokes() }
                }
                swatches.add(swatch)
                row.addView(swatch)
            }
            grid.addView(row)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("${chunk.lowercase().replaceFirstChar { it.uppercase() }} color")
            .setView(grid)
            .setPositiveButton("Apply") { _, _ ->
                setChunkColor(chunk, selectedColor)
                val chunkTasks = allTasks.filter { it.timeChunk == chunk }
                chunkTasks.forEachIndexed { i, task ->
                    val newColor = taskColorForSlot(chunk, i + 1)
                    viewModel.updateTask(task.copy(backgroundColor = newColor))
                    statCache.remove(task.id)
                }
                refreshAdapter()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Swipe to delete ──────────────────────────────────────────────────────

    private fun createItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
        ) {
            override fun getMovementFlags(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int {
                if (vh is RoutineAdapter.HeaderVH) return makeMovementFlags(0, 0)
                val drag = if (isManageMode) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
                return makeMovementFlags(drag, ItemTouchHelper.LEFT)
            }

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                if (target is RoutineAdapter.HeaderVH) return false
                val from = vh.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
                val fromTask = adapter.getTaskAt(from) ?: return false
                val toTask   = adapter.getTaskAt(to)   ?: return false
                if (fromTask.timeChunk != toTask.timeChunk) return false  // block cross-section drags
                adapter.moveItem(from, to)
                return true
            }

            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                if (isManageMode) viewModel.persistOrder(adapter.tasksInAdapterOrder())
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return
                val task = adapter.getTaskAt(pos) ?: run { adapter.notifyItemChanged(pos); return }
                MaterialAlertDialogBuilder(this@RoutineActivity)
                    .setTitle("Delete task")
                    .setMessage("Delete \"${task.title}\"? This removes all completion history.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteTask(task); statCache.remove(task.id) }
                    .setNegativeButton("Cancel") { _, _ -> adapter.notifyItemChanged(pos) }
                    .setOnCancelListener { adapter.notifyItemChanged(pos) }
                    .show()
            }
        })
    }

    // ── Edit task dialog ─────────────────────────────────────────────────────

    private fun showEditTaskDialog(task: Task) {
        val db = DialogAddRoutineTaskBinding.inflate(LayoutInflater.from(this))
        db.taskNameEditText.setText(task.title)

        var selectedChunk = task.timeChunk.let { if (it in sectionsList) it else sectionsList.firstOrNull() ?: it }
        var selectedDaysMask = ScheduledDays.displayMask(task.scheduledDays)
        val density = resources.displayMetrics.density
        val chunkViews = mutableListOf<TextView>()

        sectionsList.forEachIndexed { i, section ->
            val tv = makeToggleButton(section, section == selectedChunk, density)
            tv.setOnClickListener {
                selectedChunk = section
                chunkViews.forEachIndexed { j, v -> updateToggle(v, j == i, density) }
            }
            chunkViews.add(tv); db.timeChunkGroup.addView(tv)
        }

        ScheduledDays.dayLabels.forEachIndexed { i, label ->
            val bit = ScheduledDays.dayBits[i]
            val tv = makeDayButton(label, (selectedDaysMask and bit) != 0, density)
            tv.setOnClickListener {
                val nowActive = (selectedDaysMask and bit) == 0
                selectedDaysMask = if (nowActive) selectedDaysMask or bit else selectedDaysMask and bit.inv()
                updateToggle(tv, nowActive, density)
            }
            db.daysGroup.addView(tv)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit task")
            .setView(db.root)
            .setPositiveButton("Save") { _, _ ->
                val name = db.taskNameEditText.text?.toString()?.trim()
                if (name.isNullOrEmpty()) { Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                val newChunk = selectedChunk
                val slotInChunk = allTasks.filter { it.timeChunk == newChunk && it.id != task.id }.size + 1
                val newColor = taskColorForSlot(newChunk, slotInChunk)
                viewModel.updateTask(task.copy(
                    title = name, timeChunk = newChunk,
                    scheduledDays = ScheduledDays.normalize(selectedDaysMask),
                    backgroundColor = newColor
                ))
                statCache.remove(task.id)
            }
            .setNegativeButton("Cancel", null).show()
    }

    // ── Add task dialog ──────────────────────────────────────────────────────

    private fun showAddTaskDialog() {
        val db = DialogAddRoutineTaskBinding.inflate(LayoutInflater.from(this))
        var selectedChunk = sectionsList.firstOrNull() ?: "MORNING"
        var selectedDaysMask = ScheduledDays.ALL_MASK
        val density = resources.displayMetrics.density
        val chunkViews = mutableListOf<TextView>()

        sectionsList.forEachIndexed { i, section ->
            val tv = makeToggleButton(section, section == selectedChunk, density)
            tv.setOnClickListener {
                selectedChunk = section
                chunkViews.forEachIndexed { j, v -> updateToggle(v, j == i, density) }
            }
            chunkViews.add(tv); db.timeChunkGroup.addView(tv)
        }

        ScheduledDays.dayLabels.forEachIndexed { i, label ->
            val bit = ScheduledDays.dayBits[i]
            val tv = makeDayButton(label, true, density)
            tv.setOnClickListener {
                val nowActive = (selectedDaysMask and bit) == 0
                selectedDaysMask = if (nowActive) selectedDaysMask or bit else selectedDaysMask and bit.inv()
                updateToggle(tv, nowActive, density)
            }
            db.daysGroup.addView(tv)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add task")
            .setView(db.root)
            .setPositiveButton("Add") { _, _ ->
                val name = db.taskNameEditText.text?.toString()?.trim()
                if (name.isNullOrEmpty()) { Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                val chunk = selectedChunk
                val slotInChunk = allTasks.count { it.timeChunk == chunk } + 1
                val color = taskColorForSlot(chunk, slotInChunk)
                viewModel.insertTask(personaId, name, chunk, ScheduledDays.normalize(selectedDaysMask), color)
            }
            .setNegativeButton("Cancel", null).show()
    }

    // ── Toggle button helpers ────────────────────────────────────────────────

    private fun makeToggleButton(label: String, active: Boolean, density: Float) = TextView(this).apply {
        text = label; textSize = 13f; typeface = Typeface.DEFAULT_BOLD
        gravity = android.view.Gravity.CENTER
        setPadding((12*density).toInt(), (6*density).toInt(), (12*density).toInt(), (6*density).toInt())
        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { marginEnd = (8*density).toInt() }
        updateToggle(this, active, density)
    }

    private fun makeDayButton(label: String, active: Boolean, density: Float): TextView {
        val size = (36*density).toInt()
        return TextView(this).apply {
            text = label; textSize = 12f; typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = ViewGroup.MarginLayoutParams(size, size).apply { marginEnd = (4*density).toInt() }
            updateToggle(this, active, density)
        }
    }

    private fun updateToggle(tv: TextView, active: Boolean, density: Float) {
        val inkColor = try { Color.parseColor(personaColor) } catch (_: Exception) { Color.BLACK }
        if (active) {
            tv.setTextColor(if (isColorDark(inkColor)) Color.WHITE else Color.BLACK)
            tv.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 20 * density; setColor(inkColor)
            }
        } else {
            tv.setTextColor(Color.GRAY)
            tv.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 20 * density; setColor(Color.TRANSPARENT)
                setStroke((1*density).toInt(), Color.LTGRAY)
            }
        }
    }

    private fun isColorDark(color: Int): Boolean {
        val d = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return d >= 0.5
    }

    // ── Section management ────────────────────────────────────────────────────

    private fun showManageSectionsDialog() {
        val editing = sectionsList.toMutableList()
        val renames = mutableMapOf<String, String>()   // originalName → currentName
        val deleted = mutableListOf<String>()
        val density = resources.displayMetrics.density
        val pad = (16 * density).toInt()
        val gap = (8 * density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        val scrollView = ScrollView(this).apply { addView(container) }

        var rebuildRows: () -> Unit = {}
        rebuildRows = {
            container.removeAllViews()
            editing.forEachIndexed { i, name ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = gap }
                }
                val nameView = TextView(this).apply {
                    text = name; textSize = 15f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setPadding(0, gap, gap, gap)
                    setOnClickListener {
                        showRenameSectionDialog(name, editing, i, renames, rebuildRows)
                    }
                }
                val upBtn   = makeSectionBtn("▲", density) { if (i > 0) { editing.add(i-1, editing.removeAt(i)); rebuildRows() } }
                val downBtn = makeSectionBtn("▼", density) { if (i < editing.size-1) { editing.add(i+1, editing.removeAt(i)); rebuildRows() } }
                val delBtn  = makeSectionBtn("✕", density) {
                    if (editing.size > 1) { deleted.add(name); editing.removeAt(i); rebuildRows() }
                    else Toast.makeText(this, "Need at least one section", Toast.LENGTH_SHORT).show()
                }
                row.addView(nameView); row.addView(upBtn); row.addView(downBtn); row.addView(delBtn)
                container.addView(row)
            }
            val addBtn = TextView(this).apply {
                text = "+ Add section"; textSize = 14f
                setTextColor(try { Color.parseColor(personaColor) } catch (_: Exception) { Color.GRAY })
                setPadding(0, (12 * density).toInt(), 0, gap)
                setOnClickListener { showAddSectionNameDialog(editing, rebuildRows) }
            }
            container.addView(addBtn)
        }
        rebuildRows()

        MaterialAlertDialogBuilder(this)
            .setTitle("Manage Sections")
            .setView(scrollView)
            .setPositiveButton("Done") { _, _ -> applyManageSectionsChanges(editing, renames, deleted) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameSectionDialog(
        current: String,
        editing: MutableList<String>,
        index: Int,
        renames: MutableMap<String, String>,
        rebuild: () -> Unit
    ) {
        val density = resources.displayMetrics.density
        val pad = (24 * density).toInt(); val vpad = (8 * density).toInt()
        val input = android.widget.EditText(this).apply {
            setText(current); selectAll()
            setPadding(pad, vpad, pad, vpad)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Rename section")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newName = input.text?.toString()?.trim()
                if (newName.isNullOrEmpty() || newName == current) return@setPositiveButton
                // Track original name → new name (chain renames)
                val originalName = renames.entries.firstOrNull { it.value == current }?.key ?: current
                renames[originalName] = newName
                editing[index] = newName
                rebuild()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showAddSectionNameDialog(editing: MutableList<String>, rebuild: () -> Unit) {
        val density = resources.displayMetrics.density
        val pad = (24 * density).toInt(); val vpad = (8 * density).toInt()
        val input = android.widget.EditText(this).apply {
            hint = "Section name"; setPadding(pad, vpad, pad, vpad)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Add section")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text?.toString()?.trim()
                if (!name.isNullOrEmpty() && name !in editing) { editing.add(name); rebuild() }
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun applyManageSectionsChanges(
        newSections: List<String>,
        renames: Map<String, String>,
        deleted: List<String>
    ) {
        val fallback = newSections.firstOrNull() ?: return
        // Apply renames: update tasks + migrate color pref
        renames.forEach { (oldName, newName) ->
            if (oldName == newName) return@forEach
            allTasks.filter { it.timeChunk == oldName }.forEach { viewModel.updateTask(it.copy(timeChunk = newName)) }
            val oldColor = prefs.getInt(chunkColorKey(oldName), Int.MIN_VALUE)
            if (oldColor != Int.MIN_VALUE) {
                prefs.edit().remove(chunkColorKey(oldName)).putInt(chunkColorKey(newName), oldColor).apply()
            }
        }
        // Handle deleted sections: move tasks to first remaining section
        deleted.forEach { delName ->
            allTasks.filter { it.timeChunk == delName }.forEach { viewModel.updateTask(it.copy(timeChunk = fallback)) }
            prefs.edit().remove(chunkColorKey(delName)).apply()
        }
        saveSections(newSections)
        statCache.clear()
    }

    private fun makeSectionBtn(label: String, density: Float, onClick: () -> Unit): TextView {
        val size = (32 * density).toInt()
        return TextView(this).apply {
            text = label; textSize = 13f; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(size, size)
            setOnClickListener { onClick() }
        }
    }

    companion object {
        const val EXTRA_PERSONA_ID    = "persona_id"
        const val EXTRA_PERSONA_NAME  = "persona_name"
        const val EXTRA_PERSONA_COLOR = "persona_color"

        fun start(context: Context, personaId: Long, name: String, color: String) {
            context.startActivity(Intent(context, RoutineActivity::class.java).apply {
                putExtra(EXTRA_PERSONA_ID, personaId)
                putExtra(EXTRA_PERSONA_NAME, name)
                putExtra(EXTRA_PERSONA_COLOR, color)
            })
        }
    }
}
