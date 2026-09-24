package com.event.data.model

data class ChatMessage(
    val id: Int = 0,
    val content: String,
    val user: Boolean,
    val timeStamp: Long = System.currentTimeMillis(),
    val conversationId: String,
    val imagePath: String? = null
)
