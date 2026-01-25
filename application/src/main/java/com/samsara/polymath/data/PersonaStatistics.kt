package com.samsara.polymath.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persona_statistics")
data class PersonaStatistics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personaId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val openCount: Int,
    val totalTasks: Int,
    val completedTasks: Int,
    val score: Double
)

