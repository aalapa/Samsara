package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.Tag
import com.samsara.polymath.data.TagWithUsageCount
import com.samsara.polymath.repository.TagRepository
import kotlinx.coroutines.launch

class TagViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TagRepository
    
    val allTags: LiveData<List<Tag>>
    val tagsWithUsage: LiveData<List<TagWithUsageCount>>
    
    init {
        val db = AppDatabase.getDatabase(application)
        repository = TagRepository(db.tagDao(), db.personaTagDao())
        allTags = repository.allTags.asLiveData()
        tagsWithUsage = repository.getTagsWithUsageCount().asLiveData()
    }
    
    fun getTagsForPersona(personaId: Long): LiveData<List<Tag>> {
        return repository.getTagsForPersona(personaId).asLiveData()
    }
    
    fun assignTagToPersona(personaId: Long, tagName: String, color: String? = null) {
        viewModelScope.launch {
            val tag = repository.getOrCreateTag(tagName, color)
            repository.assignTagToPersona(personaId, tag.id)
        }
    }
    
    suspend fun createTag(tagName: String, color: String? = null): com.samsara.polymath.data.Tag? {
        return try {
            repository.getOrCreateTag(tagName, color)
        } catch (e: Exception) {
            null
        }
    }
    
    fun removeTagFromPersona(personaId: Long, tagId: Long) {
        viewModelScope.launch {
            repository.removeTagFromPersona(personaId, tagId)
        }
    }
    
    fun setTagsForPersona(personaId: Long, tagIds: List<Long>) {
        viewModelScope.launch {
            repository.setTagsForPersona(personaId, tagIds)
        }
    }
    
    fun renameTag(tagId: Long, newName: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.renameTag(tagId, newName)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
    
    fun deleteTag(tagId: Long, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteTag(tagId)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
    
    suspend fun getPersonaCountForTag(tagId: Long): Int {
        return repository.getPersonaCountForTag(tagId)
    }
}

