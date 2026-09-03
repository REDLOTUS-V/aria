package com.event.chats.data.repository

import com.event.chats.data.local.Conversation
import com.event.chats.data.local.Message
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getAllMessages(conversationId: String): Flow<List<Message>>
    suspend fun saveMessage(message: Message)
    suspend fun getConvById(convId: String): Conversation?
    fun getConversations(): Flow<List<Conversation>>
    suspend fun saveConversation(conversation: Conversation)
    suspend fun deleteConversation(convId: String)
    fun responseStream(convId: String): Flow<String>
}