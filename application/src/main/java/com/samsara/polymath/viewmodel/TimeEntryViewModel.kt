package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.TimeEntry
import com.samsara.polymath.repository.PersonaOpenEventRepository
import com.samsara.polymath.repository.TaskRepository
import com.samsara.polymath.repository.TimeEntryRepository
import com.samsara.polymath.util.HeatmapUtils
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TimeEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TimeEntryRepository
    private val taskRepository: TaskRepository
    private val personaOpenEventRepository: PersonaOpenEventRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TimeEntryRepository(database.timeEntryDao())
        taskRepository = TaskRepository(database.taskDao())
        personaOpenEventRepository = PersonaOpenEventRepository(database.personaOpenEventDao())
    }

    val runningTaskId: LiveData<Long?> = repository.getRunningTaskIdFlow().asLiveData()

    // Per-persona combined heatmap data (day → intensity 0-100)
    private val _personaHeatmapData = MutableLiveData<Map<Long, Long>>()
    val personaHeatmapData: LiveData<Map<Long, Long>> = _personaHeatmapData

    fun getTaskTimeMap(personaId: Long): LiveData<Map<Long, Long>> {
        return repository.getTotalTimesByPersonaFlow(personaId)
            .map { list -> list.associate { it.taskId to it.totalTime } }
            .asLiveData()
    }

    /**
     * Load combined heatmap data for a specific persona.
     * Combines timer time (50%), task completions (35%), and persona opens (15%)
     * over the last 91 days.
     */
    fun loadPersonaHeatmap(personaId: Long) {
        viewModelScope.launch {
            val sinceMillis = System.currentTimeMillis() - 91L * 86400000L

            val timeSums = repository.getDailyTimeSumsByPersona(personaId, sinceMillis)
            val completions = taskRepository.getDailyCompletionsByPersona(personaId, sinceMillis)
            val opens = personaOpenEventRepository.getDailyOpenCountsByPersona(personaId, sinceMillis)

            _personaHeatmapData.value = HeatmapUtils.combineHeatmapData(timeSums, completions, opens)
        }
    }

    fun toggleTimer(taskId: Long) {
        viewModelScope.launch {
            val running = repository.getRunningEntry(taskId)
            if (running != null) {
                // Stop this task's timer
                repository.update(running.copy(endTime = System.currentTimeMillis()))
            } else {
                // Stop any other running timer first
                val anyRunning = repository.getRunningEntryAnyTask()
                if (anyRunning != null) {
                    repository.update(anyRunning.copy(endTime = System.currentTimeMillis()))
                }
                // Start new timer
                repository.insert(TimeEntry(taskId = taskId))
            }
        }
    }

    fun stopAllTimers() {
        viewModelScope.launch {
            val running = repository.getRunningEntryAnyTask()
            if (running != null) {
                repository.update(running.copy(endTime = System.currentTimeMillis()))
            }
        }
    }
}
