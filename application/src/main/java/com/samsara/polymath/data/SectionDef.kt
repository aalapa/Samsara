package com.samsara.polymath.data

data class SectionDef(
    val name: String,
    val endMinutes: Int? = null   // minutes from midnight (0-1439); null = no cutoff
) {
    fun endTimeLabel(): String {
        val m = endMinutes ?: return "--"
        val h = m / 60; val min = m % 60
        val amPm = if (h < 12) "am" else "pm"
        val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        return if (min == 0) "$h12$amPm" else "$h12:${min.toString().padStart(2, '0')}$amPm"
    }
}
