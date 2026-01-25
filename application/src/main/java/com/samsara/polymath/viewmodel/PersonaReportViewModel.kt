package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.*
import com.samsara.polymath.repository.PersonaRepository
import com.samsara.polymath.repository.PersonaStatisticsRepository
import com.samsara.polymath.repository.TaskRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class PersonaReportViewModel(application: Application) : AndroidViewModel(application) {
    
    private val personaRepository: PersonaRepository
    private val taskRepository: TaskRepository
    private val statisticsRepository: PersonaStatisticsRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        personaRepository = PersonaRepository(database.personaDao())
        taskRepository = TaskRepository(database.taskDao())
        statisticsRepository = PersonaStatisticsRepository(database.personaStatisticsDao())
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
        
        // Get current and previous statistics for each persona
        val personaReports = personas.map { persona ->
            generatePersonaReport(persona, startTime, previousPeriodStart, now)
        }.sortedByDescending { it.improvementScore }
        
        // Find highlights
        val mostActive = personaReports.maxByOrNull { it.currentOpenCount }
        val mostImproved = personaReports.maxByOrNull { it.improvementScore }
        val needsAttention = personaReports.filter { 
            it.currentOpenCount == 0 || it.currentCompletionRate < 0.3 
        }.minByOrNull { it.currentOpenCount }
        
        return ReportSummary(
            reportType = reportType,
            startDate = startTime,
            endDate = now,
            personaReports = personaReports,
            mostImproved = mostImproved,
            needsAttention = needsAttention,
            mostActive = mostActive
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
            improvementScore = improvementScore
        )
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
            daysSinceLastOpened == 0L -> 1.0
            daysSinceLastOpened <= 7 -> 0.9
            daysSinceLastOpened <= 14 -> 0.7
            daysSinceLastOpened <= 30 -> 0.5
            else -> 0.3
        }
        
        return (completionRate * 50 + openCount * 0.5) * decayMultiplier
    }
}

