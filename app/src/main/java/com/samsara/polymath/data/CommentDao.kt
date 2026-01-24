package com.samsara.polymath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE taskId = :taskId ORDER BY createdAt ASC")
    fun getCommentsByTask(taskId: Long): Flow<List<Comment>>

    @Query("SELECT * FROM comments ORDER BY taskId ASC, createdAt ASC")
    suspend fun getAllComments(): List<Comment>

    @Insert
    suspend fun insertComment(comment: Comment): Long

    @Delete
    suspend fun deleteComment(comment: Comment)
}



