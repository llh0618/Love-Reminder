package com.example.girlfriend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "gifts")
data class Gift(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val link: String = "",
    val status: String,         // want/bought/given
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
