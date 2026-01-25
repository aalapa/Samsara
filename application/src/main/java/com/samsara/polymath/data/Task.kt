package com.samsara.polymath.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Persona::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personaId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personaId: Long,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false, // Track if this task should recur on completion
    val order: Int = 0,
    val backgroundColor: String = "#FFFFFF", // Inherited from persona with variant
    val previousOrder: Int = 0, // Track previous position for rank changes
    val rankStatus: RankStatus = RankStatus.STABLE // Track movement: STABLE, UP, DOWN
)

enum class RankStatus {
    STABLE,  // Task hasn't moved or was always at top (shows square)
    UP,      // Task moved up (shows green up arrow)
    DOWN     // Task moved down (shows red down arrow)
}

