package com.samsara.polymath.data

data class PersonaReport(
    val persona: Persona,
    val currentOpenCount: Int,
    val currentCompletionRate: Double,
    val totalTasks: Int,
    val completedTasks: Int,
    val previousOpenCount: Int,
    val previousCompletionRate: Double,
    val openCountTrend: TrendDirection,
    val completionRateTrend: TrendDirection,
    val improvementScore: Double, // Combined score for ranking
    val tags: List<Tag> = emptyList()
)

enum class TrendDirection {
    UP, DOWN, STABLE
}

data class ReportSummary(
    val reportType: ReportType,
    val startDate: Long,
    val endDate: Long,
    val personaReports: List<PersonaReport>,
    val mostImproved: List<PersonaReport>,
    val needsAttention: List<PersonaReport>,
    val mostActive: List<PersonaReport>,
    val tagsMostActive: List<TagReport> = emptyList(),
    val tagsMostImproved: List<TagReport> = emptyList(),
    val tagsNeedAttention: List<TagReport> = emptyList()
)

data class TagReport(
    val tag: Tag,
    val personaCount: Int,
    val avgCompletionRate: Double,
    val avgPreviousCompletionRate: Double,
    val avgOpenCount: Double,
    val avgImprovementScore: Double,
    val completionRateTrend: TrendDirection
)

enum class ReportType {
    WEEKLY, MONTHLY
}
