package com.event.chats.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
