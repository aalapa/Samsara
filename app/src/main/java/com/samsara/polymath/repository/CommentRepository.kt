package com.samsara.polymath.repository

import com.samsara.polymath.data.Comment
import com.samsara.polymath.data.CommentDao
import kotlinx.coroutines.flow.Flow

class CommentRepository(private val commentDao: CommentDao) {
    fun getCommentsByTask(taskId: Long): Flow<List<Comment>> = commentDao.getCommentsByTask(taskId)

    suspend fun insertComment(comment: Comment): Long = commentDao.insertComment(comment)

    suspend fun deleteComment(comment: Comment) = commentDao.deleteComment(comment)
}


