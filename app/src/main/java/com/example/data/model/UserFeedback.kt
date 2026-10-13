package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_feedback")
data class UserFeedback(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
