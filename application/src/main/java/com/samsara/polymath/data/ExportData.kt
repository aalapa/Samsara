package com.samsara.polymath.data

data class ExportData(
    val personas: List<Persona>,
    val tasks: List<Task>,
    val comments: List<Comment> = emptyList(), // Added in version 2
    val statistics: List<PersonaStatistics> = emptyList(), // Added in version 3
    val tags: List<Tag> = emptyList(), // Added in version 4
    val personaTags: List<PersonaTagExport> = emptyList(), // Added in version 4
    val personaOpenEvents: List<PersonaOpenEvent> = emptyList(), // Added in version 8
    val exportDate: Long = System.currentTimeMillis(),
    val version: Int = 9 // Bumped to version 9 for isFocused on personas
)

data class PersonaTagExport(
    val personaId: Long,
    val tagId: Long,
    val assignedAt: Long
)

