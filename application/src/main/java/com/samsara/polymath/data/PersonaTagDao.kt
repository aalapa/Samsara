package com.samsara.polymath.data

import androidx.room.*

@Dao
interface PersonaTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPersonaTag(personaTag: PersonaTag)
    
    @Delete
    suspend fun deletePersonaTag(personaTag: PersonaTag)
    
    @Query("DELETE FROM persona_tags WHERE personaId = :personaId AND tagId = :tagId")
    suspend fun removeTagFromPersona(personaId: Long, tagId: Long)
    
    @Query("DELETE FROM persona_tags WHERE personaId = :personaId")
    suspend fun removeAllTagsFromPersona(personaId: Long)
    
    @Query("DELETE FROM persona_tags WHERE tagId = :tagId")
    suspend fun removeTagFromAllPersonas(tagId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM persona_tags WHERE personaId = :personaId AND tagId = :tagId)")
    suspend fun isTagAssignedToPersona(personaId: Long, tagId: Long): Boolean
    
    @Query("SELECT * FROM persona_tags")
    suspend fun getAllSync(): List<PersonaTag>
    
    @Query("SELECT * FROM persona_tags")
    fun getAllPersonaTags(): kotlinx.coroutines.flow.Flow<List<PersonaTag>>
}

