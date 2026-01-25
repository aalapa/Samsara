package com.samsara.polymath.repository

import com.samsara.polymath.data.Tag
import com.samsara.polymath.data.TagDao
import com.samsara.polymath.data.PersonaTag
import com.samsara.polymath.data.PersonaTagDao
import com.samsara.polymath.data.TagWithUsageCount
import kotlinx.coroutines.flow.Flow

class TagRepository(
    private val tagDao: TagDao,
    private val personaTagDao: PersonaTagDao
) {
    val allTags: Flow<List<Tag>> = tagDao.getAllTags()
    
    fun getTagsWithUsageCount(): Flow<List<TagWithUsageCount>> {
        return tagDao.getTagsWithUsageCount()
    }
    
    fun getTagsForPersona(personaId: Long): Flow<List<Tag>> {
        return tagDao.getTagsForPersona(personaId)
    }
    
    suspend fun getOrCreateTag(tagName: String, color: String? = null): Tag {
        val normalized = normalizeTagName(tagName)
        var tag = tagDao.getTagByName(normalized)
        
        if (tag == null) {
            val newTagId = tagDao.insertTag(
                Tag(
                    name = normalized,
                    color = color,
                    order = Int.MAX_VALUE // New tags go to end
                )
            )
            tag = tagDao.getTagById(newTagId)
        }
        
        return tag ?: throw IllegalStateException("Failed to create tag")
    }
    
    suspend fun assignTagToPersona(personaId: Long, tagId: Long) {
        personaTagDao.insertPersonaTag(
            PersonaTag(personaId = personaId, tagId = tagId)
        )
    }
    
    suspend fun removeTagFromPersona(personaId: Long, tagId: Long) {
        personaTagDao.removeTagFromPersona(personaId, tagId)
    }
    
    suspend fun setTagsForPersona(personaId: Long, tagIds: List<Long>) {
        // Remove all existing tags
        personaTagDao.removeAllTagsFromPersona(personaId)
        
        // Add new tags
        tagIds.forEach { tagId ->
            personaTagDao.insertPersonaTag(
                PersonaTag(personaId = personaId, tagId = tagId)
            )
        }
    }
    
    suspend fun renameTag(tagId: Long, newName: String) {
        val tag = tagDao.getTagById(tagId)
        tag?.let {
            tagDao.updateTag(it.copy(name = normalizeTagName(newName)))
        }
    }
    
    suspend fun deleteTag(tagId: Long) {
        val tag = tagDao.getTagById(tagId) ?: return
        tagDao.deleteTag(tag)
        // Foreign key CASCADE will automatically delete persona_tags entries
    }
    
    suspend fun getPersonaCountForTag(tagId: Long): Int {
        return tagDao.getPersonaCountForTag(tagId)
    }
    
    private fun normalizeTagName(name: String): String {
        return name.trim()
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

