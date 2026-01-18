package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.Task
import com.samsara.polymath.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
    }
    
    fun getTasksByPersona(personaId: Long): LiveData<List<Task>> = repository.getTasksByPersona(personaId).asLiveData()
    
    fun insertTask(personaId: Long, title: String, description: String = "") {
        viewModelScope.launch {
            val tasks = repository.getTasksByPersona(personaId)
            val taskList = tasks.first()
            val maxOrder = taskList.maxOfOrNull { it.order } ?: 0
            repository.insertTask(
                Task(
                    personaId = personaId,
                    title = title,
                    description = description,
                    order = maxOrder + 1
                )
            )
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
    
    fun markTaskAsComplete(task: Task) {
        viewModelScope.launch {
            repository.updateTaskCompletion(
                task.id,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
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
    
    suspend fun insertTaskSync(personaId: Long, title: String, description: String, order: Int = 0, isCompleted: Boolean = false, completedAt: Long? = null): Long {
        return repository.insertTask(
            Task(
                personaId = personaId,
                title = title,
                description = description,
                order = order,
                isCompleted = isCompleted,
                completedAt = completedAt
            )
        )
    }
    
    suspend fun deleteAllTasks() {
        // Will be handled per persona in MainActivity
    }
}

