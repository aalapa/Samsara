package com.samsara.polymath.data

import java.util.Calendar

object ScheduledDays {
    const val ALL = 0       // special: every day
    const val MON = 1
    const val TUE = 2
    const val WED = 4
    const val THU = 8
    const val FRI = 16
    const val SAT = 32
    const val SUN = 64
    const val ALL_MASK = 127 // MON|TUE|WED|THU|FRI|SAT|SUN

    val dayBits   = listOf(MON, TUE, WED, THU, FRI, SAT, SUN)
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    fun fromCalendarDay(calDay: Int): Int = when (calDay) {
        Calendar.MONDAY    -> MON
        Calendar.TUESDAY   -> TUE
        Calendar.WEDNESDAY -> WED
        Calendar.THURSDAY  -> THU
        Calendar.FRIDAY    -> FRI
        Calendar.SATURDAY  -> SAT
        Calendar.SUNDAY    -> SUN
        else               -> 0
    }

    fun isScheduledToday(scheduledDays: Int): Boolean {
        if (scheduledDays == ALL) return true
        val bit = fromCalendarDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
        return (scheduledDays and bit) != 0
    }

    /** Display mask: 0 (ALL) → ALL_MASK so UI can highlight all 7 circles. */
    fun displayMask(scheduledDays: Int): Int = if (scheduledDays == ALL) ALL_MASK else scheduledDays

    /** Normalize: if all 7 bits are set, store as 0 (ALL). */
    fun normalize(mask: Int): Int = if (mask == ALL_MASK || mask == 0) ALL else mask

    fun formatLabel(scheduledDays: Int): String {
        if (scheduledDays == ALL) return "Every day"
        val mask = scheduledDays
        if (mask == MON or TUE or WED or THU or FRI) return "Weekdays"
        if (mask == SAT or SUN) return "Weekends"
        return dayBits.zip(dayLabels)
            .filter { (bit, _) -> (mask and bit) != 0 }
            .joinToString(" ") { (_, label) -> label }
    }

    fun startOfDay(timeMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
