package com.samsara.polymath.util

import com.samsara.polymath.data.DailyCompletionCount
import com.samsara.polymath.data.DailyOpenCount
import com.samsara.polymath.data.DailyTimeSum

/**
 * Combines three activity data sources (timer time, task completions, persona opens)
 * into a single heatmap intensity map.
 *
 * Each metric is independently normalized to 0–100 against its own 91-day max,
 * then weighted and summed:
 *   - Timer time:    50 %
 *   - Completions:   35 %
 *   - Opens:         15 %
 *
 * The resulting score (0–100) is returned as a Long so it can be fed straight
 * into [com.samsara.polymath.view.HeatmapView.setData].
 */
object HeatmapUtils {

    private const val WEIGHT_TIME = 0.50
    private const val WEIGHT_COMPLETIONS = 0.35
    private const val WEIGHT_OPENS = 0.15

    /**
     * Merge three daily-aggregated lists into a single day → intensity (0-100) map.
     *
     * @param timeSums      Daily timer totals (millis)
     * @param completions   Daily task completion counts
     * @param opens         Daily persona open counts
     * @return Map of dayMillis → combined intensity (0-100)
     */
    fun combineHeatmapData(
        timeSums: List<DailyTimeSum>,
        completions: List<DailyCompletionCount>,
        opens: List<DailyOpenCount>
    ): Map<Long, Long> {

        // Build raw maps keyed by dayMillis
        val timeMap = timeSums.associate { it.dayMillis to it.totalTime }
        val compMap = completions.associate { it.dayMillis to it.completionCount }
        val openMap = opens.associate { it.dayMillis to it.openCount }

        // Collect all unique days
        val allDays = (timeMap.keys + compMap.keys + openMap.keys).toSet()
        if (allDays.isEmpty()) return emptyMap()

        // Find per-metric maximums for normalization
        val maxTime = timeMap.values.maxOrNull()?.toDouble()?.coerceAtLeast(1.0) ?: 1.0
        val maxComp = compMap.values.maxOrNull()?.toDouble()?.coerceAtLeast(1.0) ?: 1.0
        val maxOpen = openMap.values.maxOrNull()?.toDouble()?.coerceAtLeast(1.0) ?: 1.0

        return allDays.associateWith { day ->
            val normTime = (timeMap[day] ?: 0).toDouble() / maxTime * 100.0
            val normComp = (compMap[day] ?: 0).toDouble() / maxComp * 100.0
            val normOpen = (openMap[day] ?: 0).toDouble() / maxOpen * 100.0

            val score = normTime * WEIGHT_TIME +
                    normComp * WEIGHT_COMPLETIONS +
                    normOpen * WEIGHT_OPENS

            score.toLong().coerceIn(0, 100)
        }.filterValues { it > 0 } // Only keep days with actual activity
    }
}
