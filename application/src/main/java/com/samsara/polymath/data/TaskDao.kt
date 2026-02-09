package com.samsara.polymath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class DailyCompletionCount(
    val dayMillis: Long,
    val completionCount: Long
)

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
    
    @Query("UPDATE tasks SET `order` = :order, previousOrder = :previousOrder, rankStatus = :rankStatus WHERE id = :id")
    suspend fun updateTaskOrderWithRank(id: Long, order: Int, previousOrder: Int, rankStatus: String)
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean, completedAt: Long?)
    
    @Query("SELECT COUNT(*) FROM tasks WHERE personaId = :personaId AND isCompleted = 1")
    suspend fun getCompletedTaskCount(personaId: Long): Int

    @Query("SELECT * FROM tasks WHERE personaId = :personaId ORDER BY `order` ASC, createdAt ASC")
    suspend fun getTasksByPersonaList(personaId: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE isRecurring = 1 AND isCompleted = 0 ORDER BY personaId ASC, `order` ASC")
    fun getAllOpenRecurringTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE endDate IS NOT NULL AND isCompleted = 0 ORDER BY endDate ASC")
    fun getAllTasksWithEndDate(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE personaId = :personaId AND title = :title AND isRecurring = 1 AND isCompleted = 1 ORDER BY completedAt ASC")
    suspend fun getCompletedRecurringInstances(personaId: Long, title: String): List<Task>

    @Query("UPDATE tasks SET recurringGroupId = :recurringGroupId WHERE id = :id")
    suspend fun updateRecurringGroupId(id: Long, recurringGroupId: Long)

    @Query("""
        SELECT (completedAt / 86400000) * 86400000 AS dayMillis,
               COUNT(*) AS completionCount
        FROM tasks
        WHERE personaId = :personaId AND isCompleted = 1 AND completedAt IS NOT NULL AND completedAt >= :sinceMillis
        GROUP BY completedAt / 86400000
        ORDER BY dayMillis ASC
    """)
    suspend fun getDailyCompletionsByPersona(personaId: Long, sinceMillis: Long): List<DailyCompletionCount>

    @Query("""
        SELECT (completedAt / 86400000) * 86400000 AS dayMillis,
               COUNT(*) AS completionCount
        FROM tasks
        WHERE isCompleted = 1 AND completedAt IS NOT NULL AND completedAt >= :sinceMillis
        GROUP BY completedAt / 86400000
        ORDER BY dayMillis ASC
    """)
    suspend fun getDailyCompletionsGlobal(sinceMillis: Long): List<DailyCompletionCount>
}

