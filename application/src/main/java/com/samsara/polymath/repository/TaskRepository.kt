package com.samsara.polymath.repository

import com.samsara.polymath.data.RankStatus
import com.samsara.polymath.data.Task
import com.samsara.polymath.data.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository(private val taskDao: TaskDao) {
    fun getTasksByPersona(personaId: Long): Flow<List<Task>> = taskDao.getTasksByPersona(personaId)
    
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    
    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)
    
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    
    suspend fun updateTaskOrder(id: Long, order: Int) = taskDao.updateTaskOrder(id, order)
    
    suspend fun updateTaskOrderWithRank(id: Long, order: Int, previousOrder: Int, rankStatus: RankStatus) =
        taskDao.updateTaskOrderWithRank(id, order, previousOrder, rankStatus.name)
    
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean, completedAt: Long?) =
        taskDao.updateTaskCompletion(id, isCompleted, completedAt)
    
    suspend fun getCompletedTaskCount(personaId: Long): Int = taskDao.getCompletedTaskCount(personaId)
    
    suspend fun getTasksByPersonaSync(personaId: Long): List<Task> {
        return taskDao.getTasksByPersonaList(personaId)
    }

    fun getAllOpenRecurringTasks(): Flow<List<Task>> = taskDao.getAllOpenRecurringTasks()

    fun getAllTasksWithEndDate(): Flow<List<Task>> = taskDao.getAllTasksWithEndDate()
}

