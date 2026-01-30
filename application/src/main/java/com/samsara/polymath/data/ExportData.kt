package com.samsara.polymath.data

data class ExportData(
    val personas: List<Persona>,
    val tasks: List<Task>,
    val comments: List<Comment> = emptyList(), // Added in version 2
    val statistics: List<PersonaStatistics> = emptyList(), // Added in version 3
    val tags: List<Tag> = emptyList(), // Added in version 4
    val personaTags: List<PersonaTagExport> = emptyList(), // Added in version 4
    val exportDate: Long = System.currentTimeMillis(),
    val version: Int = 6 // Bumped to version 6 for end date
)

data class PersonaTagExport(
    val personaId: Long,
    val tagId: Long,
    val assignedAt: Long
)

