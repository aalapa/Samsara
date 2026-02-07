package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.*
import com.samsara.polymath.repository.PersonaOpenEventRepository
import com.samsara.polymath.repository.PersonaRepository
import com.samsara.polymath.repository.PersonaStatisticsRepository
import com.samsara.polymath.repository.TaskRepository
import com.samsara.polymath.repository.TimeEntryRepository
import com.samsara.polymath.util.HeatmapUtils
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class PersonaReportViewModel(application: Application) : AndroidViewModel(application) {

    private val personaRepository: PersonaRepository
    private val taskRepository: TaskRepository
    private val statisticsRepository: PersonaStatisticsRepository
    private val tagDao: TagDao
    private val timeEntryRepository: TimeEntryRepository
    private val personaOpenEventRepository: PersonaOpenEventRepository

    // --- LiveData for tabbed report ---
    private val _reportSummary = MutableLiveData<ReportSummary>()
    val reportSummary: LiveData<ReportSummary> = _reportSummary

    private val _heatmapData = MutableLiveData<Map<Long, Long>>()
    val heatmapData: LiveData<Map<Long, Long>> = _heatmapData

    private val _selectedDayBreakdown = MutableLiveData<List<DailyPersonaTimeSum>>()
    val selectedDayBreakdown: LiveData<List<DailyPersonaTimeSum>> = _selectedDayBreakdown

    private val _selectedDay = MutableLiveData<Long?>()
    val selectedDay: LiveData<Long?> = _selectedDay

    // Per-persona heatmap data for the Report Time tab
    private val _perPersonaHeatmaps = MutableLiveData<List<PersonaHeatmapEntry>>()
    val perPersonaHeatmaps: LiveData<List<PersonaHeatmapEntry>> = _perPersonaHeatmaps

    private var _currentReportType = ReportType.WEEKLY

    init {
        val database = AppDatabase.getDatabase(application)
        personaRepository = PersonaRepository(database.personaDao())
        taskRepository = TaskRepository(database.taskDao())
        statisticsRepository = PersonaStatisticsRepository(database.personaStatisticsDao())
        tagDao = database.tagDao()
        timeEntryRepository = TimeEntryRepository(database.timeEntryDao())
        personaOpenEventRepository = PersonaOpenEventRepository(database.personaOpenEventDao())
    }

    /**
     * Set report type and reload data. Called from the Activity when the user
     * toggles Weekly/Monthly.
     */
    fun setReportType(type: ReportType) {
        _currentReportType = type
        loadReport()
    }

    fun getCurrentReportType(): ReportType = _currentReportType

    /**
     * Load report and heatmap data, post results to LiveData for fragments.
     */
    fun loadReport() {
        viewModelScope.launch {
            try {
                val report = generateReport(_currentReportType)
                _reportSummary.postValue(report)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadHeatmapData()
    }

    /**
     * Load combined heatmap data (time + completions + opens) for the global
     * heatmap, and per-persona heatmaps, over the last 91 days.
     */
    fun loadHeatmapData() {
        viewModelScope.launch {
            val sinceMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(91)

            // Global combined heatmap
            val timeSums = timeEntryRepository.getDailyTimeSums(sinceMillis)
            val completions = taskRepository.getDailyCompletionsGlobal(sinceMillis)
            val opens = personaOpenEventRepository.getDailyOpenCountsGlobal(sinceMillis)
            val combined = HeatmapUtils.combineHeatmapData(timeSums, completions, opens)

            // Fallback: if no open/completion data yet, show raw timer data so heatmap isn't empty
            if (combined.isEmpty()) {
                val rawMap = timeSums.associate { it.dayMillis to it.totalTime }
                _heatmapData.postValue(rawMap)
            } else {
                _heatmapData.postValue(combined)
            }

            // Per-persona heatmaps
            val personas = personaRepository.getAllPersonasSync()
            val entries = personas.mapNotNull { persona ->
                val pTime = timeEntryRepository.getDailyTimeSumsByPersona(persona.id, sinceMillis)
                val pComp = taskRepository.getDailyCompletionsByPersona(persona.id, sinceMillis)
                val pOpen = personaOpenEventRepository.getDailyOpenCountsByPersona(persona.id, sinceMillis)
                val pCombined = HeatmapUtils.combineHeatmapData(pTime, pComp, pOpen)

                // Fallback to raw time if no combined data
                val data = if (pCombined.isEmpty()) {
                    pTime.associate { it.dayMillis to it.totalTime }
                } else {
                    pCombined
                }

                if (data.isEmpty()) null
                else PersonaHeatmapEntry(
                    personaId = persona.id,
                    personaName = persona.name,
                    backgroundColor = persona.backgroundColor,
                    data = data
                )
            }
            _perPersonaHeatmaps.postValue(entries)
        }
    }

    /**
     * Called when the user taps a day on the heatmap.
     */
    fun selectDay(dayMillis: Long) {
        _selectedDay.postValue(dayMillis)
        viewModelScope.launch {
            val breakdown = timeEntryRepository.getTimeBreakdownForDay(dayMillis)
            _selectedDayBreakdown.postValue(breakdown)
        }
    }

    fun clearSelectedDay() {
        _selectedDay.postValue(null)
        _selectedDayBreakdown.postValue(emptyList())
    }

    suspend fun generateReport(reportType: ReportType): ReportSummary {
        val now = System.currentTimeMillis()
        val daysToLookBack = when (reportType) {
            ReportType.WEEKLY -> 7
            ReportType.MONTHLY -> 30
        }
        val startTime = now - TimeUnit.DAYS.toMillis(daysToLookBack.toLong())
        val previousPeriodStart = startTime - TimeUnit.DAYS.toMillis(daysToLookBack.toLong())

        // Get all personas
        val personas = personaRepository.getAllPersonasSync()

        // Get time tracking data per persona
        val timeByPersona = timeEntryRepository.getTotalTimeByAllPersonas()
            .associate { it.personaId to it.totalTime }

        // Get current and previous statistics for each persona
        val personaReports = personas.map { persona ->
            generatePersonaReport(persona, startTime, previousPeriodStart, now)
                .copy(totalTimeSpent = timeByPersona[persona.id] ?: 0)
        }.sortedByDescending { it.improvementScore }

        // Find highlights - top 2 each
        val mostActive = personaReports
            .sortedByDescending { it.currentOpenCount }
            .take(2)

        val mostImproved = personaReports
            .filter { it.improvementScore > 0 }
            .sortedByDescending { it.improvementScore }
            .take(2)

        val needsAttention = personaReports
            .filter { it.currentOpenCount == 0 || it.currentCompletionRate < 0.3 }
            .sortedBy { it.currentOpenCount }
            .take(2)

        // Generate tag-level insights by aggregating persona stats per tag
        val tagReports = generateTagReports(personaReports)

        val tagsMostActive = tagReports
            .sortedByDescending { it.avgOpenCount }
            .take(2)

        val tagsMostImproved = tagReports
            .filter { it.avgImprovementScore > 0 }
            .sortedByDescending { it.avgImprovementScore }
            .take(2)

        val tagsNeedAttention = tagReports
            .filter { it.avgOpenCount < 1.0 || it.avgCompletionRate < 0.3 }
            .sortedBy { it.avgOpenCount }
            .take(2)

        val mostTimeSpent = personaReports
            .filter { it.totalTimeSpent > 0 }
            .sortedByDescending { it.totalTimeSpent }

        return ReportSummary(
            reportType = reportType,
            startDate = startTime,
            endDate = now,
            personaReports = personaReports,
            mostImproved = mostImproved,
            needsAttention = needsAttention,
            mostActive = mostActive,
            tagsMostActive = tagsMostActive,
            tagsMostImproved = tagsMostImproved,
            tagsNeedAttention = tagsNeedAttention,
            mostTimeSpent = mostTimeSpent
        )
    }

    private suspend fun generatePersonaReport(
        persona: Persona,
        startTime: Long,
        previousPeriodStart: Long,
        now: Long
    ): PersonaReport {
        // Get current period stats
        val allTasks = taskRepository.getTasksByPersonaSync(persona.id)
        val currentCompletedTasks = allTasks.count { it.isCompleted }
        val currentTotalTasks = allTasks.size
        val currentCompletionRate = if (currentTotalTasks > 0) {
            currentCompletedTasks.toDouble() / currentTotalTasks
        } else {
            0.0
        }

        // For open count, we'll use the current openCount from the persona
        val currentOpenCount = persona.openCount

        // Get tags for this persona
        val tags = tagDao.getTagsForPersonaSync(persona.id)

        // Try to get previous period stats from saved statistics
        val previousStats = statisticsRepository.getStatisticsForPersona(persona.id, previousPeriodStart)
            .filter { it.timestamp < startTime }
            .maxByOrNull { it.timestamp }

        val previousOpenCount = previousStats?.openCount ?: 0
        val previousCompletionRate = if (previousStats != null && previousStats.totalTasks > 0) {
            previousStats.completedTasks.toDouble() / previousStats.totalTasks
        } else {
            currentCompletionRate // If no previous data, assume same as current
        }

        // Determine trends
        val openCountTrend = when {
            currentOpenCount > previousOpenCount -> TrendDirection.UP
            currentOpenCount < previousOpenCount -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }

        val completionRateTrend = when {
            currentCompletionRate > previousCompletionRate + 0.05 -> TrendDirection.UP
            currentCompletionRate < previousCompletionRate - 0.05 -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }

        // Calculate improvement score (higher is better)
        val completionRateImprovement = (currentCompletionRate - previousCompletionRate) * 100
        val openCountImprovement = (currentOpenCount - previousOpenCount).toDouble()
        val improvementScore = completionRateImprovement * 2 + openCountImprovement * 0.5

        return PersonaReport(
            persona = persona,
            currentOpenCount = currentOpenCount,
            currentCompletionRate = currentCompletionRate,
            totalTasks = currentTotalTasks,
            completedTasks = currentCompletedTasks,
            previousOpenCount = previousOpenCount,
            previousCompletionRate = previousCompletionRate,
            openCountTrend = openCountTrend,
            completionRateTrend = completionRateTrend,
            improvementScore = improvementScore,
            tags = tags
        )
    }

    private fun generateTagReports(personaReports: List<PersonaReport>): List<TagReport> {
        // Build a map of tag -> list of persona reports that have that tag
        val tagToReports = mutableMapOf<Long, MutableList<PersonaReport>>()
        val tagMap = mutableMapOf<Long, Tag>()

        for (report in personaReports) {
            for (tag in report.tags) {
                tagMap[tag.id] = tag
                tagToReports.getOrPut(tag.id) { mutableListOf() }.add(report)
            }
        }

        return tagToReports.map { (tagId, reports) ->
            val avgCompletionRate = reports.map { it.currentCompletionRate }.average()
            val avgPreviousCompletionRate = reports.map { it.previousCompletionRate }.average()
            val avgOpenCount = reports.map { it.currentOpenCount.toDouble() }.average()
            val avgImprovementScore = reports.map { it.improvementScore }.average()

            val completionRateTrend = when {
                avgCompletionRate > avgPreviousCompletionRate + 0.05 -> TrendDirection.UP
                avgCompletionRate < avgPreviousCompletionRate - 0.05 -> TrendDirection.DOWN
                else -> TrendDirection.STABLE
            }

            TagReport(
                tag = tagMap[tagId]!!,
                personaCount = reports.size,
                avgCompletionRate = avgCompletionRate,
                avgPreviousCompletionRate = avgPreviousCompletionRate,
                avgOpenCount = avgOpenCount,
                avgImprovementScore = avgImprovementScore,
                completionRateTrend = completionRateTrend
            )
        }
    }

    // Call this periodically to save current statistics for future comparisons
    fun saveCurrentStatistics() {
        viewModelScope.launch {
            val personas = personaRepository.getAllPersonasSync()
            val now = System.currentTimeMillis()

            personas.forEach { persona ->
                val tasks = taskRepository.getTasksByPersonaSync(persona.id)
                val completedTasks = tasks.count { it.isCompleted }
                val totalTasks = tasks.size
                val score = calculatePersonaScore(persona, totalTasks, completedTasks)

                statisticsRepository.insertStatistics(
                    PersonaStatistics(
                        personaId = persona.id,
                        timestamp = now,
                        openCount = persona.openCount,
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        score = score
                    )
                )
            }

            // Clean up old statistics (keep last 90 days)
            val ninetyDaysAgo = now - TimeUnit.DAYS.toMillis(90)
            statisticsRepository.deleteOldStatistics(ninetyDaysAgo)
        }
    }

    private fun calculatePersonaScore(persona: Persona, totalTasks: Int, completedTasks: Int): Double {
        val completionRate = if (totalTasks > 0) completedTasks.toDouble() / totalTasks else 0.0
        val openCount = persona.openCount.toDouble()

        val daysSinceLastOpened = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - persona.lastOpenedAt
        )
        val decayMultiplier = when {
            daysSinceLastOpened <= 6 -> 1.0     // NONE: 0-6 days
            daysSinceLastOpened <= 13 -> 0.85   // SLIGHT: 7-13 days
            daysSinceLastOpened <= 20 -> 0.65   // MEDIUM: 14-20 days
            else -> 0.4                          // SERIOUS: 21+ days
        }

        // Use the same formula as main ranking: (1 + completionRate) * openCount * decay
        val baseScore = if (totalTasks > 0) {
            (1 + completionRate) * openCount
        } else {
            openCount // If no tasks, score = openCount
        }

        return baseScore * decayMultiplier
    }
}
