package com.samsara.polymath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCompletionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(completion: DailyCompletion)

    @Query("DELETE FROM daily_completions WHERE taskId = :taskId AND completedDate = :date")
    suspend fun delete(taskId: Long, date: Long)

    @Query("SELECT * FROM daily_completions WHERE taskId = :taskId AND completedDate = :date LIMIT 1")
    suspend fun getForTaskAndDate(taskId: Long, date: Long): DailyCompletion?

    @Query("SELECT COUNT(*) FROM daily_completions WHERE taskId = :taskId")
    suspend fun getTotalCompletions(taskId: Long): Int

    @Query("SELECT completedDate FROM daily_completions WHERE taskId = :taskId ORDER BY completedDate DESC")
    suspend fun getCompletedDates(taskId: Long): List<Long>

    @Query("SELECT taskId FROM daily_completions WHERE completedDate = :date")
    fun getCompletedTaskIdsForDate(date: Long): Flow<List<Long>>
}
