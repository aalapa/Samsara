package com.samsara.polymath.data

data class ExportData(
    val personas: List<Persona>,
    val tasks: List<Task>,
    val comments: List<Comment> = emptyList(), // Added in version 2
    val exportDate: Long = System.currentTimeMillis(),
    val version: Int = 2 // Bumped to version 2 to include comments
)

