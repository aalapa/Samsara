package com.samsara.polymath.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "persona_tags",
    primaryKeys = ["personaId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Persona::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE  // Delete links when persona deleted
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE  // Delete links when tag deleted
        )
    ],
    indices = [
        Index(value = ["personaId"]),
        Index(value = ["tagId"])
    ]
)
data class PersonaTag(
    @ColumnInfo(name = "personaId")
    val personaId: Long,
    
    @ColumnInfo(name = "tagId")
    val tagId: Long,
    
    @ColumnInfo(name = "assigned_at")
    val assignedAt: Long = System.currentTimeMillis()
)


