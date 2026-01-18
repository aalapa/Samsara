package com.samsara.polymath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {
    // Order by: manually arranged personas (order > 0) first by order, 
    // then unarranged personas (order = 0) by openCount DESC
    // Using a large number for order=0 so they come after manually arranged ones
    @Query("SELECT * FROM personas ORDER BY CASE WHEN `order` = 0 THEN 999999 ELSE `order` END ASC, openCount DESC, createdAt ASC")
    fun getAllPersonas(): Flow<List<Persona>>
    
    @Query("SELECT * FROM personas WHERE id = :id")
    suspend fun getPersonaById(id: Long): Persona?
    
    @Insert
    suspend fun insertPersona(persona: Persona): Long
    
    @Update
    suspend fun updatePersona(persona: Persona)
    
    @Delete
    suspend fun deletePersona(persona: Persona)
    
    @Query("UPDATE personas SET `order` = :order WHERE id = :id")
    suspend fun updatePersonaOrder(id: Long, order: Int)
    
    @Query("UPDATE personas SET openCount = openCount + 1 WHERE id = :id")
    suspend fun incrementOpenCount(id: Long)
    
    @Query("UPDATE personas SET name = :name WHERE id = :id")
    suspend fun updatePersonaName(id: Long, name: String)
}

