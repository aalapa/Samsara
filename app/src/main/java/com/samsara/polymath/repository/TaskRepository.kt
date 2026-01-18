package com.samsara.polymath.repository

import com.samsara.polymath.data.Task
import com.samsara.polymath.data.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun getTasksByPersona(personaId: Long): Flow<List<Task>> = taskDao.getTasksByPersona(personaId)
    
    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)
    
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    
    suspend fun updateTaskOrder(id: Long, order: Int) = taskDao.updateTaskOrder(id, order)
    
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean, completedAt: Long?) =
        taskDao.updateTaskCompletion(id, isCompleted, completedAt)
}

