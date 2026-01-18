package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.Persona
import com.samsara.polymath.repository.PersonaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PersonaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PersonaRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = PersonaRepository(database.personaDao())
    }
    
    fun getAllPersonas(): LiveData<List<Persona>> = repository.getAllPersonas().asLiveData()
    
    fun insertPersona(name: String) {
        viewModelScope.launch {
            val personas = repository.getAllPersonas()
            val personaList = personas.first()
            val maxOrder = personaList.maxOfOrNull { it.order } ?: 0
            repository.insertPersona(Persona(name = name, order = maxOrder + 1))
        }
    }
    
    fun deletePersona(persona: Persona) {
        viewModelScope.launch {
            repository.deletePersona(persona)
        }
    }
    
    fun updatePersonaOrder(personaId: Long, newOrder: Int) {
        viewModelScope.launch {
            repository.updatePersonaOrder(personaId, newOrder)
        }
    }
    
    fun reorderPersonas(personas: List<Persona>) {
        viewModelScope.launch {
            personas.forEachIndexed { index, persona ->
                repository.updatePersonaOrder(persona.id, index)
            }
        }
    }
    
    suspend fun getAllPersonasSync(): List<Persona> {
        return repository.getAllPersonas().first()
    }
    
    suspend fun insertPersonaSync(persona: Persona): Long {
        return repository.insertPersona(persona)
    }
    
    suspend fun deleteAllPersonas() {
        val personas = repository.getAllPersonas().first()
        personas.forEach { persona ->
            repository.deletePersona(persona)
        }
    }
    
    fun incrementOpenCount(personaId: Long) {
        viewModelScope.launch {
            repository.incrementOpenCount(personaId)
        }
    }
}

