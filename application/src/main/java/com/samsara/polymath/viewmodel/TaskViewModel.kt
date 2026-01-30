package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.Task
import com.samsara.polymath.repository.TaskRepository
import com.samsara.polymath.util.RecurringTaskUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
    }
    
    fun getTasksByPersona(personaId: Long): LiveData<List<Task>> = repository.getTasksByPersona(personaId).asLiveData()
    
    fun insertTask(personaId: Long, title: String, description: String = "", personaBackgroundColor: String = "#007AFF", isRecurring: Boolean = false, recurringFrequency: String? = null, recurringDays: String? = null) {
        viewModelScope.launch {
            val tasks = repository.getTasksByPersona(personaId)
            val taskList = tasks.first()
            val maxOrder = taskList.maxOfOrNull { it.order } ?: 0

            // Calculate variant color based on task order (increment hex value slightly)
            val variantColor = calculateVariantColor(personaBackgroundColor, maxOrder + 1)

            repository.insertTask(
                Task(
                    personaId = personaId,
                    title = title,
                    description = description,
                    order = maxOrder + 1,
                    backgroundColor = variantColor,
                    isRecurring = isRecurring,
                    recurringFrequency = recurringFrequency,
                    recurringDays = recurringDays
                )
            )
        }
    }
    
    /**
     * Calculates a variant color by incrementing the hex value slightly based on task order.
     * Each task gets a slightly different shade of the parent persona color.
     */
    private fun calculateVariantColor(parentColor: String, taskOrder: Int): String {
        try {
            // Remove # and parse hex
            val cleanHex = parentColor.removePrefix("#")
            val r = cleanHex.substring(0, 2).toInt(16)
            val g = cleanHex.substring(2, 4).toInt(16)
            val b = cleanHex.substring(4, 6).toInt(16)
            
            // Increment each component by a small amount based on task order
            // Use a smaller increment (3 per task) to create subtle variations
            // This keeps text color consistent across more tasks
            val increment = taskOrder * 3
            
            // Calculate new RGB values (clamp to 0-255)
            val newR = (r + increment).coerceIn(0, 255)
            val newG = (g + increment).coerceIn(0, 255)
            val newB = (b + increment).coerceIn(0, 255)
            
            // Convert back to hex
            return String.format("#%02X%02X%02X", newR, newG, newB)
        } catch (e: Exception) {
            // If parsing fails, return parent color
            return parentColor
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
    
    fun updateTaskOrder(taskId: Long, newOrder: Int) {
        viewModelScope.launch {
            repository.updateTaskOrder(taskId, newOrder)
        }
    }
    
    fun updateTaskOrderWithRank(taskId: Long, newOrder: Int, previousOrder: Int, rankStatus: com.samsara.polymath.data.RankStatus) {
        viewModelScope.launch {
            repository.updateTaskOrderWithRank(taskId, newOrder, previousOrder, rankStatus)
        }
    }
    
    fun markTaskAsComplete(task: Task) {
        viewModelScope.launch {
            // Mark the current task as complete
            repository.updateTaskCompletion(
                task.id,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            
            // If it's a recurring task, create a new one with today's date
            if (task.isRecurring) {
                val tasks = repository.getTasksByPersona(task.personaId)
                val taskList = tasks.first()
                val maxOrder = taskList.maxOfOrNull { it.order } ?: 0
                
                repository.insertTask(
                    Task(
                        personaId = task.personaId,
                        title = task.title,
                        description = task.description,
                        order = maxOrder + 1,
                        backgroundColor = task.backgroundColor,
                        isRecurring = true,
                        recurringFrequency = task.recurringFrequency,
                        recurringDays = task.recurringDays,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
    
    fun reorderTasks(tasks: List<Task>) {
        viewModelScope.launch {
            tasks.forEachIndexed { index, task ->
                repository.updateTaskOrder(task.id, index)
            }
        }
    }
    
    suspend fun getAllTasksSync(): List<Task> {
        // Get all tasks by getting all personas first, then their tasks
        return emptyList() // Will be handled in MainActivity
    }
    
    suspend fun getTasksByPersonaSync(personaId: Long): List<Task> {
        return repository.getTasksByPersona(personaId).first()
    }
    
    suspend fun insertTaskSync(
        personaId: Long,
        title: String,
        description: String,
        order: Int = 0,
        isCompleted: Boolean = false,
        completedAt: Long? = null,
        backgroundColor: String = "#FFFFFF",
        createdAt: Long = System.currentTimeMillis(),
        isRecurring: Boolean = false,
        recurringFrequency: String? = null,
        recurringDays: String? = null
    ): Long {
        return repository.insertTask(
            Task(
                personaId = personaId,
                title = title,
                description = description,
                order = order,
                isCompleted = isCompleted,
                completedAt = completedAt,
                backgroundColor = backgroundColor,
                createdAt = createdAt,
                isRecurring = isRecurring,
                recurringFrequency = recurringFrequency,
                recurringDays = recurringDays
            )
        )
    }
    
    fun getDueTodayTasks(): LiveData<List<Task>> {
        return repository.getAllOpenRecurringTasks()
            .map { tasks -> tasks.filter { RecurringTaskUtil.isDueToday(it) } }
            .asLiveData()
    }

    suspend fun deleteAllTasks() {
        // Will be handled per persona in MainActivity
    }
}

