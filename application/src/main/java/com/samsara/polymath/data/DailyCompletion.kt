package com.samsara.polymath.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_completions",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["completedDate"]),
        Index(value = ["taskId", "completedDate"], unique = true)
    ]
)
data class DailyCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val completedDate: Long, // start-of-day millis in local timezone
    val completedAt: Long = System.currentTimeMillis()
)
