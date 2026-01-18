package com.samsara.polymath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE personaId = :personaId ORDER BY `order` ASC, createdAt ASC")
    fun getTasksByPersona(personaId: Long): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks ORDER BY personaId ASC, `order` ASC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?
    
    @Insert
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("UPDATE tasks SET `order` = :order WHERE id = :id")
    suspend fun updateTaskOrder(id: Long, order: Int)
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean, completedAt: Long?)
    
    @Query("SELECT COUNT(*) FROM tasks WHERE personaId = :personaId AND isCompleted = 1")
    suspend fun getCompletedTaskCount(personaId: Long): Int
}

