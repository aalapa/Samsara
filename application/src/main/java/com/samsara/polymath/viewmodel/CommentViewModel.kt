package com.samsara.polymath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.samsara.polymath.data.AppDatabase
import com.samsara.polymath.data.Comment
import com.samsara.polymath.repository.CommentRepository
import kotlinx.coroutines.launch

class CommentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CommentRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CommentRepository(database.commentDao())
    }

    fun getCommentsByTask(taskId: Long): LiveData<List<Comment>> {
        return repository.getCommentsByTask(taskId).asLiveData()
    }

    fun insertComment(taskId: Long, text: String, createdAt: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insertComment(
                Comment(
                    taskId = taskId,
                    text = text,
                    createdAt = createdAt
                )
            )
        }
    }

    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            repository.deleteComment(comment)
        }
    }

    suspend fun getAllCommentsSync(): List<Comment> {
        return repository.getAllComments()
    }
}



