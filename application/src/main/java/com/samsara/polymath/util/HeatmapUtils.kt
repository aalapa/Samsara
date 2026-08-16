package com.samsara.polymath.util

import com.samsara.polymath.data.DailyCompletionCount
import com.samsara.polymath.data.DailyOpenCount

object HeatmapUtils {

    private const val WEIGHT_COMPLETIONS = 0.70
    private const val WEIGHT_OPENS = 0.30

    fun combineHeatmapData(
        completions: List<DailyCompletionCount>,
        opens: List<DailyOpenCount>
    ): Map<Long, Long> {
        val compMap = completions.associate { it.dayMillis to it.completionCount }
        val openMap = opens.associate { it.dayMillis to it.openCount }

        val allDays = (compMap.keys + openMap.keys).toSet()
        if (allDays.isEmpty()) return emptyMap()

        val maxComp = compMap.values.maxOrNull()?.toDouble()?.coerceAtLeast(1.0) ?: 1.0
        val maxOpen = openMap.values.maxOrNull()?.toDouble()?.coerceAtLeast(1.0) ?: 1.0

        return allDays.associateWith { day ->
            val normComp = (compMap[day] ?: 0).toDouble() / maxComp * 100.0
            val normOpen = (openMap[day] ?: 0).toDouble() / maxOpen * 100.0
            val score = normComp * WEIGHT_COMPLETIONS + normOpen * WEIGHT_OPENS
            score.toLong().coerceIn(0, 100)
        }.filterValues { it > 0 }
    }
}
