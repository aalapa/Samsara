package com.samsara.polymath.util

import com.samsara.polymath.data.Task
import java.util.Calendar

object RecurringTaskUtil {
    fun isDueToday(task: Task): Boolean {
        if (!task.isRecurring || task.isCompleted) return false
        // If nextDueDate is set, check if today is on or after the due date
        if (task.nextDueDate != null) {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            return task.nextDueDate >= today.timeInMillis && task.nextDueDate < tomorrow.timeInMillis
        }
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

    fun calculateNextDueDate(frequency: String?, days: String?, createdAt: Long): Long? {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return when (frequency) {
            "DAILY" -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            "WEEKLY" -> {
                val targetDay = days?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_WEEK)
                cal.add(Calendar.DAY_OF_YEAR, 1) // at least tomorrow
                while (cal.get(Calendar.DAY_OF_WEEK) != targetDay) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                cal.timeInMillis
            }
            "MONTHLY" -> {
                val parts = days?.split(",") ?: emptyList()
                val targetDay = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
                val interval = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 1
                cal.add(Calendar.MONTH, interval)
                cal.set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(cal.getActualMaximum(Calendar.DAY_OF_MONTH)))
                cal.timeInMillis
            }
            "CUSTOM" -> {
                val targetDays = days?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                if (targetDays.isEmpty()) return null
                cal.add(Calendar.DAY_OF_YEAR, 1) // at least tomorrow
                repeat(7) {
                    if (cal.get(Calendar.DAY_OF_WEEK) in targetDays) return cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                null
            }
            else -> null
        }
    }

    fun isEndDateToday(task: Task): Boolean {
        val endDate = task.endDate ?: return false
        if (task.isCompleted) return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply { timeInMillis = endDate }
        return today.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)
    }

    fun isEndDateUpcoming(task: Task, withinDays: Int = 3): Boolean {
        val endDate = task.endDate ?: return false
        if (task.isCompleted) return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val todayMillis = today.timeInMillis
        val endOfRange = today.apply { add(Calendar.DAY_OF_YEAR, withinDays) }.timeInMillis
        // Upcoming = after today but within range
        return endDate > todayMillis && endDate <= endOfRange
    }

    fun isOverdue(task: Task): Boolean {
        val endDate = task.endDate ?: return false
        if (task.isCompleted) return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return endDate < today.timeInMillis
    }
}
