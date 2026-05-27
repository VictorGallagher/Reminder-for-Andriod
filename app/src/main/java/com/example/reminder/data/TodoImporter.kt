package com.example.reminder.data

import android.content.Context
import android.net.Uri

data class SuggestedTodo(val task: String, val createdAt: Long)

class TodoImporter(private val context: Context) {
    companion object {
        private const val AUTHORITY = "com.example.todolist.provider"
        private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/todo_items")
    }

    fun getSuggestedTodos(): List<SuggestedTodo> {
        val suggestions = mutableListOf<SuggestedTodo>()
        val cursor = context.contentResolver.query(
            CONTENT_URI,
            null,
            "isCompleted = 0", // Only suggest uncompleted tasks
            null,
            "createdAt DESC"
        )

        cursor?.use {
            val taskIndex = it.getColumnIndex("task")
            val createdAtIndex = it.getColumnIndex("createdAt")
            
            while (it.moveToNext()) {
                if (taskIndex != -1 && createdAtIndex != -1) {
                    suggestions.add(
                        SuggestedTodo(
                            it.getString(taskIndex),
                            it.getLong(createdAtIndex)
                        )
                    )
                }
            }
        }
        return suggestions
    }
}
