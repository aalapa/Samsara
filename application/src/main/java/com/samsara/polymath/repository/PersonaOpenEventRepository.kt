package com.samsara.polymath.repository

import com.samsara.polymath.data.DailyOpenCount
import com.samsara.polymath.data.PersonaOpenEvent
import com.samsara.polymath.data.PersonaOpenEventDao

class PersonaOpenEventRepository(private val dao: PersonaOpenEventDao) {
    suspend fun insert(event: PersonaOpenEvent): Long = dao.insert(event)
    suspend fun getDailyOpenCountsByPersona(personaId: Long, sinceMillis: Long): List<DailyOpenCount> =
        dao.getDailyOpenCountsByPersona(personaId, sinceMillis)
    suspend fun getDailyOpenCountsGlobal(sinceMillis: Long): List<DailyOpenCount> =
        dao.getDailyOpenCountsGlobal(sinceMillis)
    suspend fun getAllEventsSince(sinceMillis: Long): List<PersonaOpenEvent> =
        dao.getAllEventsSince(sinceMillis)
    suspend fun deleteOldEvents(beforeMillis: Long) = dao.deleteOldEvents(beforeMillis)
}
