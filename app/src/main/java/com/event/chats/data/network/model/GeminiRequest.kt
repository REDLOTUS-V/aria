package com.event.chats.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>
)
@Serializable
data class Content(
    val role: String?,
    val parts: List<Part>
)
@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)
@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)