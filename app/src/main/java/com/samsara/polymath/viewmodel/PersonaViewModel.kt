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
            
            // Assign random color with contrasting text
            val colors = listOf(
                Pair("#007AFF", "#FFFFFF"), // Apple Blue - White text
                Pair("#34C759", "#FFFFFF"), // Apple Green - White text
                Pair("#FF3B30", "#FFFFFF"), // Apple Red - White text
                Pair("#FF9500", "#FFFFFF"), // Apple Orange - White text
                Pair("#AF52DE", "#FFFFFF"), // Apple Purple - White text
                Pair("#FF2D55", "#FFFFFF"), // Apple Pink - White text
                Pair("#5AC8FA", "#000000"), // Apple Teal - Black text
                Pair("#5856D6", "#FFFFFF"), // Apple Indigo - White text
                Pair("#FFCC00", "#000000"), // Yellow - Black text
                Pair("#32D74B", "#FFFFFF")  // Bright Green - White text
            )
            val randomColor = colors.random()
            
            repository.insertPersona(
                Persona(
                    name = name,
                    order = maxOrder + 1,
                    backgroundColor = randomColor.first,
                    textColor = randomColor.second
                )
            )
        }
    }
    
    fun updatePersonaName(personaId: Long, newName: String) {
        viewModelScope.launch {
            repository.updatePersonaName(personaId, newName)
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
    
    suspend fun getPersonaByIdSync(id: Long): Persona? {
        return repository.getPersonaById(id)
    }
}

