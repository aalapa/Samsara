package com.samsara.polymath.data

data class RoutineTaskStats(
    val streak: Int,
    val total: Int,
    val scheduledTotal: Int
) {
    val completionRate: Float
        get() = if (scheduledTotal > 0) total.toFloat() / scheduledTotal else 0f
}
