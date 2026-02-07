package com.samsara.polymath.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "persona_open_events",
    foreignKeys = [
        ForeignKey(
            entity = Persona::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personaId"]), Index(value = ["timestamp"])]
)
data class PersonaOpenEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personaId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
