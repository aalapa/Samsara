package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.DecayLevel
import com.samsara.polymath.data.Persona
import com.samsara.polymath.data.PersonaWithTaskCount
import com.samsara.polymath.data.RankStatus
import com.samsara.polymath.repository.PersonaRepository
import com.samsara.polymath.repository.TaskRepository
import java.util.concurrent.TimeUnit
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
            val currentTime = System.currentTimeMillis()

            // Calculate task counts and scores for each persona
            val personasWithStats = personas.map { persona ->
                val personaTasks = allTasks.filter { it.personaId == persona.id }
                val completedCount = personaTasks.count { it.isCompleted }
                val openCount = personaTasks.count { !it.isCompleted }

                // Calculate decay level based on days since last opened
                val decayLevel = calculateDecayLevel(persona.lastOpenedAt, currentTime)
                val decayMultiplier = getDecayMultiplier(decayLevel)

                // Calculate progress score with decay penalty
                // Base Score = (1 + completedTasks / totalTasks) * openCount
                // Final Score = Base Score * decayMultiplier
                val totalTasks = completedCount + openCount
                val baseScore = if (totalTasks > 0) {
                    (1 + (completedCount.toDouble() / totalTasks)) * persona.openCount
                } else {
                    persona.openCount.toDouble() // If no tasks, score = openCount
                }
                val score = baseScore * decayMultiplier

                PersonaWithTaskCount(
                    persona = persona,
                    completedTaskCount = completedCount,
                    openTaskCount = openCount,
                    emoji = "", // Will be assigned after sorting
                    score = score,
                    decayLevel = decayLevel
                )
            }

            // Sort by score (descending), then by openCount (descending) for tie-breaking
            val sortedPersonas = personasWithStats.sortedWith(
                compareByDescending<PersonaWithTaskCount> { it.score }
                    .thenByDescending { it.persona.openCount }
            )

            // Include rank status from persona
            sortedPersonas.map { personaWithStats ->
                personaWithStats.copy(
                    rankStatus = personaWithStats.persona.rankStatus
                )
            }
        }.asLiveData()
    }

    /**
     * Calculate decay level based on days since last opened.
     * - 0-6 days: NONE
     * - 7-13 days: SLIGHT
     * - 14-20 days: MEDIUM
     * - 21+ days: SERIOUS
     */
    private fun calculateDecayLevel(lastOpenedAt: Long, currentTime: Long): DecayLevel {
        val daysSinceOpened = TimeUnit.MILLISECONDS.toDays(currentTime - lastOpenedAt)
        return when {
            daysSinceOpened < 7 -> DecayLevel.NONE
            daysSinceOpened < 14 -> DecayLevel.SLIGHT
            daysSinceOpened < 21 -> DecayLevel.MEDIUM
            else -> DecayLevel.SERIOUS
        }
    }

    /**
     * Get score multiplier for decay level.
     * - NONE: 100%
     * - SLIGHT: 85%
     * - MEDIUM: 65%
     * - SERIOUS: 40%
     */
    private fun getDecayMultiplier(decayLevel: DecayLevel): Double {
        return when (decayLevel) {
            DecayLevel.NONE -> 1.0
            DecayLevel.SLIGHT -> 0.85
            DecayLevel.MEDIUM -> 0.65
            DecayLevel.SERIOUS -> 0.40
        }
    }
    
    fun insertPersona(name: String) {
        viewModelScope.launch {
            val personas = repository.getAllPersonas()
            val personaList = personas.first()

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
                    backgroundColor = selectedColor.first,
                    textColor = selectedColor.second
                )
            )
        }
    }
    
    /**
     * Finds the color from available colors that is maximally different from existing colors.
     * Uses Euclidean distance in RGB space to calculate color difference.
     * When all colors are used, picks the least-used color to ensure variety.
     */
    private fun findMostDifferentColor(
        availableColors: List<Pair<String, String>>,
        existingColors: List<String>
    ): Pair<String, String> {
        if (existingColors.isEmpty()) {
            // If no existing colors, return first available color
            return availableColors.first()
        }

        // First, check if there are any unused colors
        val unusedColors = availableColors.filter { colorPair ->
            colorPair.first !in existingColors
        }

        // If there are unused colors, pick the most different one from existing
        val colorsToChooseFrom = if (unusedColors.isNotEmpty()) {
            unusedColors
        } else {
            // All colors used - pick the least-used color for variety
            val colorUsageCounts = availableColors.associateWith { colorPair ->
                existingColors.count { it == colorPair.first }
            }
            val minUsageCount = colorUsageCounts.values.minOrNull() ?: 0
            availableColors.filter { colorUsageCounts[it] == minUsageCount }
        }

        // If only one option, return it
        if (colorsToChooseFrom.size == 1) {
            return colorsToChooseFrom.first()
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
        var bestColor = colorsToChooseFrom.first()

        for (colorPair in colorsToChooseFrom) {
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
            val currentTime = System.currentTimeMillis()
            val allTasks = taskRepository.getAllTasks().first()

            // Helper function to calculate score for a persona
            fun calculatePersonaScore(persona: Persona): Double {
                val personaTasks = allTasks.filter { it.personaId == persona.id }
                val completedCount = personaTasks.count { it.isCompleted }
                val openCount = personaTasks.count { !it.isCompleted }
                val totalTasks = completedCount + openCount

                val decayLevel = calculateDecayLevel(persona.lastOpenedAt, currentTime)
                val decayMultiplier = getDecayMultiplier(decayLevel)

                val baseScore = if (totalTasks > 0) {
                    (1 + (completedCount.toDouble() / totalTasks)) * persona.openCount
                } else {
                    persona.openCount.toDouble() // If no tasks, score = openCount
                }
                return baseScore * decayMultiplier
            }

            // Get current personas and calculate positions based on score
            val personasBefore = repository.getAllPersonas().first()
            val sortedBefore = personasBefore.sortedWith(
                compareByDescending<Persona> { calculatePersonaScore(it) }
                    .thenByDescending { it.openCount }
            )
            val positionsBefore = sortedBefore.mapIndexed { index, persona ->
                persona.id to index
            }.toMap()

            // Increment the open count (this also updates lastOpenedAt)
            repository.incrementOpenCount(personaId)

            // Get personas after increment and recalculate positions
            val personasAfter = repository.getAllPersonas().first()
            val sortedAfter = personasAfter.sortedWith(
                compareByDescending<Persona> { calculatePersonaScore(it) }
                    .thenByDescending { it.openCount }
            )
            val positionsAfter = sortedAfter.mapIndexed { index, persona ->
                persona.id to index
            }.toMap()

            // Update rank status for all personas based on position change
            for (persona in personasAfter) {
                val posBefore = positionsBefore[persona.id] ?: Int.MAX_VALUE
                val posAfter = positionsAfter[persona.id] ?: Int.MAX_VALUE

                val newRankStatus = when {
                    posAfter < posBefore -> RankStatus.UP     // Lower index = higher rank
                    posAfter > posBefore -> RankStatus.DOWN
                    else -> RankStatus.STABLE
                }

                // Only update if status changed
                if (persona.rankStatus != newRankStatus) {
                    repository.updateRankStatus(persona.id, newRankStatus)
                }
            }
        }
    }
    
    suspend fun getPersonaByIdSync(id: Long): Persona? {
        return repository.getPersonaById(id)
    }
}

