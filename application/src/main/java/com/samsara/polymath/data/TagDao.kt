package com.samsara.polymath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY `order` ASC, name ASC")
    fun getAllTags(): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags ORDER BY `order` ASC, name ASC")
    suspend fun getAllTagsSync(): List<Tag>
    
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?
    
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long
    
    @Update
    suspend fun updateTag(tag: Tag)
    
    @Delete
    suspend fun deleteTag(tag: Tag)
    
    @Query("SELECT tags.* FROM tags " +
           "INNER JOIN persona_tags ON tags.id = persona_tags.tagId " +
           "WHERE persona_tags.personaId = :personaId " +
           "ORDER BY tags.`order` ASC, tags.name ASC")
    fun getTagsForPersona(personaId: Long): Flow<List<Tag>>

    @Query("SELECT tags.* FROM tags " +
           "INNER JOIN persona_tags ON tags.id = persona_tags.tagId " +
           "WHERE persona_tags.personaId = :personaId " +
           "ORDER BY tags.`order` ASC, tags.name ASC")
    suspend fun getTagsForPersonaSync(personaId: Long): List<Tag>
    
    @Query("UPDATE tags SET `order` = :order WHERE id = :tagId")
    suspend fun updateTagOrder(tagId: Long, order: Int)

    @Query("SELECT COUNT(*) FROM persona_tags WHERE tagId = :tagId")
    suspend fun getPersonaCountForTag(tagId: Long): Int
    
    @Query("SELECT tags.*, COUNT(persona_tags.personaId) as usage_count " +
           "FROM tags " +
           "LEFT JOIN persona_tags ON tags.id = persona_tags.tagId " +
           "GROUP BY tags.id " +
           "ORDER BY tags.`order` ASC, tags.name ASC")
    fun getTagsWithUsageCount(): Flow<List<TagWithUsageCount>>
}

data class TagWithUsageCount(
    @Embedded val tag: Tag,
    @ColumnInfo(name = "usage_count")
    val usageCount: Int
)


