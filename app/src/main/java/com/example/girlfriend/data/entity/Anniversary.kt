package com.example.girlfriend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "anniversaries")
data class Anniversary(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: String,           // "2023-05-20"
    val type: String,           // birthday/valentine/qixi/520/anniversary/custom
    val repeat: String,         // yearly/once
    val remindBeforeDays: Int = 0,
    val note: String = "",
    val calendarEventIds: String = "",  // 逗号分隔的多个日历事件ID
    val relationshipDays: String = ""   // 确定关系类型的天数配置，如"30,99,180"，逗号分隔，非关系类型为空
)
