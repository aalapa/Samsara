package com.samsara.polymath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class TaskTimeSum(
    val taskId: Long,
    val taskTitle: String,
    val totalTime: Long
)

data class PersonaTimeSum(
    val personaId: Long,
    val totalTime: Long
)

data class TaskIdTime(
    val taskId: Long,
    val totalTime: Long
)

@Dao
interface TimeEntryDao {
    @Insert
    suspend fun insert(entry: TimeEntry): Long

    @Update
    suspend fun update(entry: TimeEntry)

    @Query("SELECT * FROM time_entries WHERE taskId = :taskId AND endTime IS NULL LIMIT 1")
    suspend fun getRunningEntry(taskId: Long): TimeEntry?

    @Query("SELECT * FROM time_entries WHERE endTime IS NULL LIMIT 1")
    suspend fun getRunningEntryAnyTask(): TimeEntry?

    @Query("SELECT * FROM time_entries WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getEntriesByTask(taskId: Long): Flow<List<TimeEntry>>

    @Query("SELECT COALESCE(SUM(endTime - startTime), 0) FROM time_entries WHERE taskId = :taskId AND endTime IS NOT NULL")
    suspend fun getTotalTimeByTask(taskId: Long): Long

    @Query("""
        SELECT t.id AS taskId, t.title AS taskTitle, COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime
        FROM tasks t
        LEFT JOIN time_entries te ON t.id = te.taskId AND te.endTime IS NOT NULL
        WHERE t.personaId = :personaId
        GROUP BY t.id
        HAVING totalTime > 0
        ORDER BY totalTime DESC
    """)
    suspend fun getTaskTimeSumsByPersona(personaId: Long): List<TaskTimeSum>

    @Query("""
        SELECT t.personaId AS personaId, COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime
        FROM time_entries te
        INNER JOIN tasks t ON te.taskId = t.id
        WHERE te.endTime IS NOT NULL
        GROUP BY t.personaId
        ORDER BY totalTime DESC
    """)
    suspend fun getTotalTimeByAllPersonas(): List<PersonaTimeSum>

    @Query("SELECT * FROM time_entries WHERE endTime IS NOT NULL ORDER BY startTime ASC")
    suspend fun getAllCompletedEntries(): List<TimeEntry>

    @Query("SELECT taskId FROM time_entries WHERE endTime IS NULL LIMIT 1")
    fun getRunningTaskIdFlow(): Flow<Long?>

    @Query("""
        SELECT te.taskId AS taskId, COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime
        FROM time_entries te
        INNER JOIN tasks t ON te.taskId = t.id
        WHERE t.personaId = :personaId AND te.endTime IS NOT NULL
        GROUP BY te.taskId
    """)
    fun getTotalTimesByPersonaFlow(personaId: Long): Flow<List<TaskIdTime>>
}
