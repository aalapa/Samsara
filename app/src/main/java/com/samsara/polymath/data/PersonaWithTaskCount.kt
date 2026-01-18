package com.samsara.polymath.data

/**
 * Wraps a Persona with its task statistics, progress score, and emoji indicator.
 * 
 * Progress Score Calculation (Option 1 with safety checks):
 * Score = (completedTasks / totalTasks) * openCount
 * 
 * Where:
 * - totalTasks = completedTasks + openTasks
 * - If totalTasks = 0, score = 0 (no progress)
 * 
 * This formula rewards both:
 * 1. Completion rate (completedTasks / totalTasks) - how much progress you've made
 * 2. Engagement (openCount) - how often you work on this persona
 * 
 * Emoji Assignment:
 * - 😊 (smiling): Top 3 personas by score (if they have completed tasks)
 * - 😢 (sad): Bottom 3 personas by score, OR any persona with no completed tasks
 * - Empty: All other personas
 * 
 * Tie-breaking: When scores are equal, personas with higher openCount rank higher.
 * 
 * @param persona The persona entity
 * @param completedTaskCount Number of completed tasks for this persona
 * @param openTaskCount Number of open (incomplete) tasks for this persona
 * @param emoji Emoji indicator (😊 for top 3, 😢 for bottom 3, empty for others)
 * @param score Progress score calculated using the formula above
 */
data class PersonaWithTaskCount(
    val persona: Persona,
    val completedTaskCount: Int,
    val openTaskCount: Int = 0,
    val emoji: String = "",
    val score: Double = 0.0
)

