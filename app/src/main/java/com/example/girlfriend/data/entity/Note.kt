package com.example.girlfriend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val content: String,
    val category: String,       // like/dislike
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
