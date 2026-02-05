package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.TimeEntry
import com.samsara.polymath.repository.TimeEntryRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TimeEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TimeEntryRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TimeEntryRepository(database.timeEntryDao())
    }

    val runningTaskId: LiveData<Long?> = repository.getRunningTaskIdFlow().asLiveData()

    fun getTaskTimeMap(personaId: Long): LiveData<Map<Long, Long>> {
        return repository.getTotalTimesByPersonaFlow(personaId)
            .map { list -> list.associate { it.taskId to it.totalTime } }
            .asLiveData()
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
