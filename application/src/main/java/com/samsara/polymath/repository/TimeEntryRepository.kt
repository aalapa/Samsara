package com.samsara.polymath.repository

import com.samsara.polymath.data.TimeEntry
import com.samsara.polymath.data.TimeEntryDao
import com.samsara.polymath.data.TaskTimeSum
import com.samsara.polymath.data.PersonaTimeSum
import kotlinx.coroutines.flow.Flow

class TimeEntryRepository(private val timeEntryDao: TimeEntryDao) {
    suspend fun insert(entry: TimeEntry): Long = timeEntryDao.insert(entry)
    suspend fun update(entry: TimeEntry) = timeEntryDao.update(entry)
    suspend fun getRunningEntry(taskId: Long): TimeEntry? = timeEntryDao.getRunningEntry(taskId)
    suspend fun getRunningEntryAnyTask(): TimeEntry? = timeEntryDao.getRunningEntryAnyTask()
    fun getEntriesByTask(taskId: Long): Flow<List<TimeEntry>> = timeEntryDao.getEntriesByTask(taskId)
    suspend fun getTotalTimeByTask(taskId: Long): Long = timeEntryDao.getTotalTimeByTask(taskId)
    suspend fun getTaskTimeSumsByPersona(personaId: Long): List<TaskTimeSum> = timeEntryDao.getTaskTimeSumsByPersona(personaId)
    suspend fun getTotalTimeByAllPersonas(): List<PersonaTimeSum> = timeEntryDao.getTotalTimeByAllPersonas()
    fun getRunningTaskIdFlow(): Flow<Long?> = timeEntryDao.getRunningTaskIdFlow()
}
