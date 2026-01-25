package com.samsara.polymath.data

/**
 * Wraps a Persona with its task statistics, progress score, and visual indicators.
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
 * Visual Feedback:
 * - Score displayed next to persona (matches ranking order)
 * - Rank arrows (↑↓) show position changes
 * - Decay visuals (opacity/desaturation) show neglect
 *
 * Tie-breaking: When scores are equal, personas with higher openCount rank higher.
 *
 * @param persona The persona entity
 * @param completedTaskCount Number of completed tasks for this persona
 * @param openTaskCount Number of open (incomplete) tasks for this persona
 * @param emoji Deprecated - no longer used (kept for backward compatibility)
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

