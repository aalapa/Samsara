package com.samsara.polymath.data

data class ExportData(
    val personas: List<Persona>,
    val tasks: List<Task>,
    val comments: List<Comment> = emptyList(), // Added in version 2
    val statistics: List<PersonaStatistics> = emptyList(), // Added in version 3
    val exportDate: Long = System.currentTimeMillis(),
    val version: Int = 3 // Bumped to version 3 to include statistics
)

