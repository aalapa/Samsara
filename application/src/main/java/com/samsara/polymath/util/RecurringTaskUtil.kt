package com.samsara.polymath.util

import com.samsara.polymath.data.Task
import java.util.Calendar

object RecurringTaskUtil {
    fun isDueToday(task: Task): Boolean {
        if (!task.isRecurring || task.isCompleted) return false
        val freq = task.recurringFrequency ?: return false // no frequency = do whenever, not scheduled for today
        val cal = Calendar.getInstance()
        return when (freq) {
            "DAILY" -> true
            "WEEKLY" -> {
                val storedDay = task.recurringDays?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_WEEK)
                cal.get(Calendar.DAY_OF_WEEK) == storedDay
            }
            "MONTHLY" -> {
                val parts = task.recurringDays?.split(",") ?: emptyList()
                val storedDay = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
                val interval = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 1
                if (cal.get(Calendar.DAY_OF_MONTH) != storedDay) return false
                if (interval <= 1) return true
                // Check if current month aligns with interval from task creation
                val createdCal = Calendar.getInstance().apply { timeInMillis = task.createdAt }
                val monthsDiff = (cal.get(Calendar.YEAR) - createdCal.get(Calendar.YEAR)) * 12 +
                        (cal.get(Calendar.MONTH) - createdCal.get(Calendar.MONTH))
                monthsDiff % interval == 0
            }
            "CUSTOM" -> {
                val days = task.recurringDays?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                cal.get(Calendar.DAY_OF_WEEK) in days
            }
            else -> true
        }
    }
}
