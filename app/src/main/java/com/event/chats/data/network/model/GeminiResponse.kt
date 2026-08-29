package com.event.chats.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)
