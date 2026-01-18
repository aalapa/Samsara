package com.samsara.polymath.data

data class ExportData(
    val personas: List<Persona>,
    val tasks: List<Task>,
    val exportDate: Long = System.currentTimeMillis(),
    val version: Int = 1
)

