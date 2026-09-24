package com.event.chats

import com.event.chats.data.local.Conversation
import com.event.chats.data.local.Message
import com.event.chats.data.repository.Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class FakeRepository: Repository {
    override fun getAllMessages(conversationId: String): Flow<List<Message>> {
        return emptyFlow()
    }

    var savedMessages = mutableListOf<Message>()

    override suspend fun saveMessage(message: Message) {
        savedMessages.add(message)
    }

    override suspend fun deleteMsg(id: Int) {}

    override suspend fun getConvById(convId: String): Conversation? = null

    override fun getConversations(): Flow<List<Conversation>> = emptyFlow()

    override suspend fun saveConversation(conversation: Conversation) {}

    override suspend fun deleteConversation(convId: String) {}

    var isError = false
    override fun responseStream(convId: String): Flow<String> {
        return flow {
            if (isError) {
                throw Exception("network gone!")
            }else {
                emit("hello")
                delay(10.milliseconds)
                emit(" ray")
                delay(10.milliseconds)
                emit("!")
            }

        }
    }
}
