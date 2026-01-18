package com.samsara.polymath.repository

import com.samsara.polymath.data.Persona
import com.samsara.polymath.data.PersonaDao
import kotlinx.coroutines.flow.Flow

class PersonaRepository(private val personaDao: PersonaDao) {
    fun getAllPersonas(): Flow<List<Persona>> = personaDao.getAllPersonas()
    
    suspend fun getPersonaById(id: Long): Persona? = personaDao.getPersonaById(id)
    
    suspend fun insertPersona(persona: Persona): Long = personaDao.insertPersona(persona)
    
    suspend fun updatePersona(persona: Persona) = personaDao.updatePersona(persona)
    
    suspend fun deletePersona(persona: Persona) = personaDao.deletePersona(persona)
    
    suspend fun updatePersonaOrder(id: Long, order: Int) = personaDao.updatePersonaOrder(id, order)
    
    suspend fun incrementOpenCount(id: Long) = personaDao.incrementOpenCount(id)
    
    suspend fun updatePersonaName(id: Long, name: String) = personaDao.updatePersonaName(id, name)
}

