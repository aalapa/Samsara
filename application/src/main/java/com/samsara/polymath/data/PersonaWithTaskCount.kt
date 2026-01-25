package com.samsara.polymath.data

/**
 * Wraps a Persona with its task statistics, progress score, and emoji indicator.
 *
 * Progress Score Calculation (with decay penalty):
 * Base Score = (1 + completedTasks / totalTasks) * openCount
 * Final Score = Base Score * decayMultiplier
 *
 * Decay Multipliers (based on days since last opened):
 * - NONE (0-6 days): 100%
 * - SLIGHT (7-13 days): 85%
 * - MEDIUM (14-20 days): 65%
 * - SERIOUS (21+ days): 40%
 *
 * Where:
 * - totalTasks = completedTasks + openTasks
 * - If totalTasks = 0, score = openCount (rewards engagement even without tasks)
 *
 * This formula rewards:
 * 1. Base engagement (openCount) - everyone gets at least 1× their opens
 * 2. Completion bonus (completedTasks / totalTasks) - adds up to 1× for 100% completion
 * 3. Recency (decay) - penalizes neglected personas
 *
 * Example scores (without decay):
 * - 10 opens, 0% completion = 10 (1.0 × 10)
 * - 10 opens, 50% completion = 15 (1.5 × 10)
 * - 10 opens, 100% completion = 20 (2.0 × 10)
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
 * @param score Progress score calculated using the formula above (with decay applied)
 * @param rankStatus Track movement: UP, DOWN, STABLE
 * @param decayLevel Decay level based on days since last opened
 */
data class PersonaWithTaskCount(
    val persona: Persona,
    val completedTaskCount: Int,
    val openTaskCount: Int = 0,
    val emoji: String = "",
    val score: Double = 0.0,
    val rankStatus: RankStatus = RankStatus.STABLE,
    val decayLevel: DecayLevel = DecayLevel.NONE
)

