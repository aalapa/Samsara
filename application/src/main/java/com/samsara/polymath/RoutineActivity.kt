package com.samsara.polymath

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    // SharedPreferences key per chunk: "chunk_color_<personaId>_<chunk>"
    private val prefs by lazy { getSharedPreferences("routine_prefs", Context.MODE_PRIVATE) }

    private fun chunkColorKey(chunk: String) = "chunk_color_${personaId}_$chunk"

    private fun getChunkColor(chunk: String): Int? {
        val stored = prefs.getInt(chunkColorKey(chunk), Int.MIN_VALUE)
        return if (stored == Int.MIN_VALUE) null else stored
    }

    private fun setChunkColor(chunk: String, color: Int) {
        prefs.edit().putInt(chunkColorKey(chunk), color).apply()
    }

    private fun chunkColorsMap(): Map<String, Int> =
        listOf("MORNING", "AFTERNOON", "EVENING", "NIGHT")
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
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun getMovementFlags(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int {
                if (vh is RoutineAdapter.HeaderVH) return makeMovementFlags(0, 0)
                return super.getMovementFlags(rv, vh)
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

        var selectedChunk = task.timeChunk
        var selectedDaysMask = ScheduledDays.displayMask(task.scheduledDays)
        val density = resources.displayMetrics.density
        val chunkViews = mutableListOf<TextView>()

        listOf("MORNING","AFTERNOON","EVENING","NIGHT")
            .zip(listOf("Morning","Afternoon","Evening","Night"))
            .forEachIndexed { i, (chunk, label) ->
                val tv = makeToggleButton(label, chunk == selectedChunk, density)
                tv.setOnClickListener {
                    selectedChunk = chunk
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
        var selectedChunk = "MORNING"
        var selectedDaysMask = ScheduledDays.ALL_MASK
        val density = resources.displayMetrics.density
        val chunkViews = mutableListOf<TextView>()

        listOf("MORNING","AFTERNOON","EVENING","NIGHT")
            .zip(listOf("Morning","Afternoon","Evening","Night"))
            .forEachIndexed { i, (chunk, label) ->
                val tv = makeToggleButton(label, chunk == selectedChunk, density)
                tv.setOnClickListener {
                    selectedChunk = chunk
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
