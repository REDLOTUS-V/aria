package com.event.data

import com.event.data.model.ChatMessage
import com.event.data.model.Chat
import com.event.database.entity.Conversation
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getMessages(convId: String): Flow<List<ChatMessage>>
    suspend fun saveMsg(msg: ChatMessage)
    suspend fun deleteMsg(id: Int)
    fun getConversations(): Flow<List<Chat>>
    suspend fun getConvId(convId: String): Chat?
    suspend fun saveConv(conv: Chat)

    suspend fun updateSystemPrompt(convId: String, system: String?)
    suspend fun deleteConv(convId: String)
    fun responseStream(convId: String): Flow<String>
}