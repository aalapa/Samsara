package com.samsara.polymath.repository

import com.samsara.polymath.data.Persona
import com.samsara.polymath.data.PersonaDao
import com.samsara.polymath.data.RankStatus
import kotlinx.coroutines.flow.Flow

class PersonaRepository(private val personaDao: PersonaDao) {
    fun getAllPersonas(): Flow<List<Persona>> = personaDao.getAllPersonas()

    suspend fun getPersonaById(id: Long): Persona? = personaDao.getPersonaById(id)

    suspend fun insertPersona(persona: Persona): Long = personaDao.insertPersona(persona)

    suspend fun updatePersona(persona: Persona) = personaDao.updatePersona(persona)

    suspend fun deletePersona(persona: Persona) = personaDao.deletePersona(persona)

    suspend fun incrementOpenCount(id: Long) = personaDao.incrementOpenCount(id)

    suspend fun updatePersonaName(id: Long, name: String) = personaDao.updatePersonaName(id, name)

    suspend fun updateRankStatus(id: Long, rankStatus: RankStatus) = personaDao.updateRankStatus(id, rankStatus)

    suspend fun savePreviousOpenCount(id: Long) = personaDao.savePreviousOpenCount(id)

    suspend fun saveAllPreviousOpenCounts() = personaDao.saveAllPreviousOpenCounts()
}

