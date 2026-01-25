package com.samsara.polymath.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personas")
data class Persona(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val order: Int = 0,
    val openCount: Int = 0,
    val backgroundColor: String = "#007AFF", // Default to apple blue
    val textColor: String = "#FFFFFF", // Default to white text
    val previousOpenCount: Int = 0, // Track previous open count for rank changes
    val rankStatus: RankStatus = RankStatus.STABLE, // Track movement: STABLE, UP, DOWN
    val lastOpenedAt: Long = System.currentTimeMillis() // Track when persona was last opened for decay
)

/**
 * Decay levels based on days since last opened.
 * Affects both visual appearance and score calculation.
 */
enum class DecayLevel {
    NONE,    // 0-6 days: No decay, 100% score
    SLIGHT,  // 7-13 days: 85% score, 90% opacity
    MEDIUM,  // 14-20 days: 65% score, 75% opacity
    SERIOUS  // 21+ days: 40% score, 60% opacity + desaturated
}

