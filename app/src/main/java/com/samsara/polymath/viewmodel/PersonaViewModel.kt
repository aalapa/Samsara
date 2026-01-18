package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.Persona
import com.samsara.polymath.data.PersonaWithTaskCount
import com.samsara.polymath.repository.PersonaRepository
import com.samsara.polymath.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PersonaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PersonaRepository
    private val taskRepository: TaskRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = PersonaRepository(database.personaDao())
        taskRepository = TaskRepository(database.taskDao())
    }
    
    fun getAllPersonas(): LiveData<List<Persona>> = repository.getAllPersonas().asLiveData()
    
    fun getAllPersonasWithTaskCount(): LiveData<List<PersonaWithTaskCount>> {
        // Combine personas flow with all tasks flow so we update when either changes
        return combine(
            repository.getAllPersonas(),
            taskRepository.getAllTasks()
        ) { personas, allTasks ->
            personas.map { persona ->
                val completedCount = allTasks.count { it.personaId == persona.id && it.isCompleted }
                PersonaWithTaskCount(persona, completedCount)
            }
        }.asLiveData()
    }
    
    fun insertPersona(name: String) {
        viewModelScope.launch {
            val personas = repository.getAllPersonas()
            val personaList = personas.first()
            val maxOrder = personaList.maxOfOrNull { it.order } ?: 0
            
            // Available colors with contrasting text
            val availableColors = listOf(
                Pair("#007AFF", "#FFFFFF"), // Apple Blue - White text
                Pair("#34C759", "#FFFFFF"), // Apple Green - White text
                Pair("#FF3B30", "#FFFFFF"), // Apple Red - White text
                Pair("#FF9500", "#FFFFFF"), // Apple Orange - White text
                Pair("#AF52DE", "#FFFFFF"), // Apple Purple - White text
                Pair("#FF2D55", "#FFFFFF"), // Apple Pink - White text
                Pair("#5AC8FA", "#000000"), // Apple Teal - Black text
                Pair("#5856D6", "#FFFFFF"), // Apple Indigo - White text
                Pair("#FFCC00", "#000000"), // Yellow - Black text
                Pair("#32D74B", "#FFFFFF"), // Bright Green - White text
                Pair("#00C7BE", "#FFFFFF"), // Turquoise - White text
                Pair("#FF6B6B", "#FFFFFF"), // Coral - White text
                Pair("#4ECDC4", "#000000"), // Mint - Black text
                Pair("#95E1D3", "#000000"), // Light Mint - Black text
                Pair("#F38181", "#FFFFFF"), // Light Coral - White text
                Pair("#AA96DA", "#FFFFFF"), // Lavender - White text
                Pair("#FCBAD3", "#000000"), // Light Pink - Black text
                Pair("#A8E6CF", "#000000"), // Light Green - Black text
                Pair("#FFD93D", "#000000"), // Light Yellow - Black text
                Pair("#6BCB77", "#FFFFFF")  // Fresh Green - White text
            )
            
            // Get existing persona colors
            val existingColors = personaList.map { it.backgroundColor }
            
            // Find the color that is maximally different from existing colors
            val selectedColor = findMostDifferentColor(availableColors, existingColors)
            
            repository.insertPersona(
                Persona(
                    name = name,
                    order = maxOrder + 1,
                    backgroundColor = selectedColor.first,
                    textColor = selectedColor.second
                )
            )
        }
    }
    
    /**
     * Finds the color from available colors that is maximally different from existing colors.
     * Uses Euclidean distance in RGB space to calculate color difference.
     */
    private fun findMostDifferentColor(
        availableColors: List<Pair<String, String>>,
        existingColors: List<String>
    ): Pair<String, String> {
        if (existingColors.isEmpty()) {
            // If no existing colors, return first available color
            return availableColors.first()
        }
        
        // Convert hex colors to RGB
        fun hexToRgb(hex: String): Triple<Int, Int, Int> {
            val cleanHex = hex.removePrefix("#")
            val r = cleanHex.substring(0, 2).toInt(16)
            val g = cleanHex.substring(2, 4).toInt(16)
            val b = cleanHex.substring(4, 6).toInt(16)
            return Triple(r, g, b)
        }
        
        // Calculate Euclidean distance between two colors in RGB space
        fun colorDistance(color1: Triple<Int, Int, Int>, color2: Triple<Int, Int, Int>): Double {
            val dr = color1.first - color2.first
            val dg = color1.second - color2.second
            val db = color1.third - color2.third
            return Math.sqrt((dr * dr + dg * dg + db * db).toDouble())
        }
        
        // Convert existing colors to RGB
        val existingRgb = existingColors.map { hexToRgb(it) }
        
        // Find the color with maximum minimum distance to all existing colors
        var maxMinDistance = -1.0
        var bestColor = availableColors.first()
        
        for (colorPair in availableColors) {
            val candidateRgb = hexToRgb(colorPair.first)
            
            // Calculate minimum distance to any existing color
            val minDistance = existingRgb.minOfOrNull { existingRgb ->
                colorDistance(candidateRgb, existingRgb)
            } ?: Double.MAX_VALUE
            
            // If this color is further from all existing colors, select it
            if (minDistance > maxMinDistance) {
                maxMinDistance = minDistance
                bestColor = colorPair
            }
        }
        
        return bestColor
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

