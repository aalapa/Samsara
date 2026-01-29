package com.samsara.polymath.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Data class for Persona with associated Tags (for UI)
 */
data class PersonaWithTags(
    @Embedded val persona: Persona,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PersonaTag::class,
            parentColumn = "personaId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)


