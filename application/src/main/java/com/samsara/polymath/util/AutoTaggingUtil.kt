package com.samsara.polymath.util

import com.samsara.polymath.data.PersonaDao
import com.samsara.polymath.viewmodel.TagViewModel

/**
 * Utility for automatically assigning tags to existing personas based on their names.
 * Run this once after migration 11 -> 12 to tag existing personas.
 */
suspend fun autoTagExistingPersonas(personaDao: PersonaDao, tagViewModel: TagViewModel) {
    val allPersonas = personaDao.getAllPersonasList()
    
    val tagMapping = mapOf(
        // Physical & Martial Arts
        "Judoka" to listOf("Physical", "Martial Arts"),
        "Jiu-Jiteiro" to listOf("Physical", "Martial Arts"),
        "Cali artist" to listOf("Physical"),
        "Rower" to listOf("Physical"),
        "Marathoner" to listOf("Physical"),
        "Yogi" to listOf("Physical", "Spiritual"),
        
        // Technical & Programming
        "Pythonista" to listOf("Technical", "Programming"),
        "Haskeller" to listOf("Technical", "Programming"),
        "React-ivist" to listOf("Technical", "Programming"),
        "Hacker" to listOf("Technical", "Programming"),
        "VinciAIan" to listOf("Technical", "Programming"),
        
        // Language Learning
        "POLYGOT - Sanskrit" to listOf("Language", "Spiritual"),
        "POLYGOT - French" to listOf("Language"),
        "POLYGOT - Spanish" to listOf("Language"),
        
        // Financial & Career
        "FUmoney-ist (Debt free)" to listOf("Financial"),
        "Corporate Donkey" to listOf("Career"),
        "A billionaire ($)" to listOf("Financial", "Aspirational"),
        "A million+ followers" to listOf("Aspirational"),
        "Founder +/ Risk Taker" to listOf("Financial", "Aspirational"),
        
        // Family & Relationships
        "Grhasthasrami" to listOf("Family", "Lifestyle"),
        "Husband" to listOf("Family", "Relationships"),
        "Dad" to listOf("Family", "Relationships"),
        "Son" to listOf("Family", "Relationships"),
        "Brother" to listOf("Family", "Relationships"),
        "Friend" to listOf("Relationships"),
        
        // Creative & Music
        "Alchemist - Writer" to listOf("Creative"),
        "Pianist" to listOf("Creative", "Music"),
        "Violinist" to listOf("Creative", "Music"),
        "Dancer" to listOf("Creative"),
        
        // Spiritual & Philosophical
        "Sai, Shiva ,Babbaji Follower" to listOf("Spiritual"),
        "Long Game Player" to listOf("Spiritual"),
        "Philosopher" to listOf("Spiritual"),
        
        // Lifestyle & Aspirational
        "Book worm" to listOf("Lifestyle"),
        "Traveler" to listOf("Lifestyle", "Travel"),
        "Dreamer" to listOf("Aspirational"),
        "Mathematician" to listOf("Aspirational"),
        "First hand human" to listOf("Lifestyle")
    )
    
    allPersonas.forEach { persona ->
        val tagsToAssign = tagMapping[persona.name]
        tagsToAssign?.forEach { tagName ->
            tagViewModel.assignTagToPersona(persona.id, tagName)
        }
    }
}

