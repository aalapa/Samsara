package com.samsara.polymath.repository

import com.samsara.polymath.data.DailyCompletion
import com.samsara.polymath.data.DailyCompletionDao
import kotlinx.coroutines.flow.Flow

class DailyCompletionRepository(private val dao: DailyCompletionDao) {

    suspend fun toggle(taskId: Long, date: Long) {
        if (dao.getForTaskAndDate(taskId, date) != null) {
            dao.delete(taskId, date)
        } else {
            dao.insert(DailyCompletion(taskId = taskId, completedDate = date))
        }
    }

    suspend fun getTotalCompletions(taskId: Long): Int = dao.getTotalCompletions(taskId)

    suspend fun getCompletedDates(taskId: Long): List<Long> = dao.getCompletedDates(taskId)

    suspend fun isCompletedToday(taskId: Long, today: Long): Boolean =
        dao.getForTaskAndDate(taskId, today) != null

    fun getCompletedTaskIdsForDate(date: Long): Flow<List<Long>> =
        dao.getCompletedTaskIdsForDate(date)
}
