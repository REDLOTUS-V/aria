package com.event.data.model

data class Chat(
    val id: String,
    val title: String,
    val systemPrompt: String? = null
)
