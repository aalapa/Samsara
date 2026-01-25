package com.samsara.polymath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {
    // Order by createdAt - actual display order is handled by ViewModel (score-based sorting)
    @Query("SELECT * FROM personas ORDER BY createdAt ASC")
    fun getAllPersonas(): Flow<List<Persona>>
    
    @Query("SELECT * FROM personas WHERE id = :id")
    suspend fun getPersonaById(id: Long): Persona?
    
    @Insert
    suspend fun insertPersona(persona: Persona): Long
    
    @Update
    suspend fun updatePersona(persona: Persona)
    
    @Delete
    suspend fun deletePersona(persona: Persona)
    
    @Query("UPDATE personas SET openCount = openCount + 1, lastOpenedAt = :timestamp WHERE id = :id")
    suspend fun incrementOpenCount(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE personas SET name = :name WHERE id = :id")
    suspend fun updatePersonaName(id: Long, name: String)

    @Query("UPDATE personas SET rankStatus = :rankStatus WHERE id = :id")
    suspend fun updateRankStatus(id: Long, rankStatus: RankStatus)

    @Query("UPDATE personas SET previousOpenCount = openCount WHERE id = :id")
    suspend fun savePreviousOpenCount(id: Long)

    @Query("UPDATE personas SET previousOpenCount = openCount")
    suspend fun saveAllPreviousOpenCounts()

    @Query("SELECT * FROM personas ORDER BY createdAt ASC")
    suspend fun getAllPersonasList(): List<Persona>
    
    @Transaction
    @Query("SELECT * FROM personas ORDER BY `order` ASC")
    fun getAllPersonasWithTags(): Flow<List<PersonaWithTags>>
    
    @Transaction
    @Query("SELECT * FROM personas WHERE id = :personaId")
    suspend fun getPersonaWithTags(personaId: Long): PersonaWithTags?
    
    @Query("SELECT DISTINCT personas.* FROM personas " +
           "INNER JOIN persona_tags ON personas.id = persona_tags.personaId " +
           "WHERE persona_tags.tagId IN (:tagIds) " +
           "ORDER BY personas.`order` ASC")
    fun getPersonasByTags(tagIds: List<Long>): Flow<List<Persona>>
    
    @Query("SELECT personas.* FROM personas " +
           "LEFT JOIN persona_tags ON personas.id = persona_tags.personaId " +
           "WHERE persona_tags.personaId IS NULL " +
           "ORDER BY personas.`order` ASC")
    fun getUntaggedPersonas(): Flow<List<Persona>>
}

