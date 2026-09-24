package com.event.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(tableName = "Conversations")
data class Conversation(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val title: String,
    val systemPrompt: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)