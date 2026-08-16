package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.RoutineTaskStats
import com.samsara.polymath.data.ScheduledDays
import com.samsara.polymath.data.Task
import com.samsara.polymath.repository.DailyCompletionRepository
import com.samsara.polymath.repository.TaskRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository: TaskRepository
    private val completionRepository: DailyCompletionRepository

    val today: Long = ScheduledDays.startOfDay()

    init {
        val db = AppDatabase.getDatabase(application)
        taskRepository = TaskRepository(db.taskDao())
        completionRepository = DailyCompletionRepository(db.dailyCompletionDao())
    }

    fun getTasksForPersona(personaId: Long): LiveData<List<Task>> =
        taskRepository.getTopLevelTasksByPersona(personaId).asLiveData()

    fun getCompletedIdsForToday(): LiveData<List<Long>> =
        completionRepository.getCompletedTaskIdsForDate(today).asLiveData()

    fun toggleCompletion(task: Task, onStatsReady: (RoutineTaskStats) -> Unit) {
        viewModelScope.launch {
            completionRepository.toggle(task.id, today)
            onStatsReady(loadStats(task))
        }
    }

    suspend fun loadStats(task: Task): RoutineTaskStats {
        val streak        = calculateStreak(task.id, task.scheduledDays)
        val total         = completionRepository.getTotalCompletions(task.id)
        val scheduledTotal = countScheduledDays(task.createdAt, task.scheduledDays)
        return RoutineTaskStats(streak, total, scheduledTotal)
    }

    private suspend fun calculateStreak(taskId: Long, scheduledDays: Int): Int {
        val completedSet = completionRepository.getCompletedDates(taskId).toSet()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis

        var streak = 0
        repeat(365) {
            val dayStart = cal.timeInMillis
            val bit = ScheduledDays.fromCalendarDay(cal.get(Calendar.DAY_OF_WEEK))
            val isScheduled = scheduledDays == ScheduledDays.ALL || (scheduledDays and bit) != 0
            if (isScheduled) {
                when {
                    dayStart in completedSet -> streak++
                    dayStart < todayStart   -> return streak
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun countScheduledDays(createdAt: Long, scheduledDays: Int): Int {
        val cal = Calendar.getInstance()
        val today = ScheduledDays.startOfDay()
        cal.timeInMillis = ScheduledDays.startOfDay(createdAt)
        var count = 0
        while (cal.timeInMillis <= today) {
            val bit = ScheduledDays.fromCalendarDay(cal.get(Calendar.DAY_OF_WEEK))
            if (scheduledDays == ScheduledDays.ALL || (scheduledDays and bit) != 0) count++
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return count.coerceAtLeast(1)
    }

    fun insertTask(
        personaId: Long, title: String, timeChunk: String,
        scheduledDays: Int, personaBackgroundColor: String
    ) {
        viewModelScope.launch {
            val siblings = taskRepository.getTopLevelTasksByPersonaList(personaId)
            val maxOrder = siblings.maxOfOrNull { it.order } ?: 0
            taskRepository.insertTask(
                Task(
                    personaId = personaId, title = title,
                    timeChunk = timeChunk, scheduledDays = scheduledDays,
                    backgroundColor = personaBackgroundColor, order = maxOrder + 1
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { taskRepository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task) }
    }

    fun persistOrder(tasks: List<Task>) {
        viewModelScope.launch {
            tasks.forEachIndexed { index, task ->
                taskRepository.updateTaskOrder(task.id, index)
            }
        }
    }
}
