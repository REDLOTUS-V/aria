package com.event.chats.data.repository

import com.event.chats.BuildConfig
import com.event.chats.data.local.ChatDao
import com.event.chats.data.local.Conversation
import com.event.chats.data.local.Message
import com.event.chats.data.network.ApiService
import com.event.chats.data.network.model.Content
import com.event.chats.data.network.model.GeminiRequest
import com.event.chats.data.network.model.GeminiResponse
import com.event.chats.data.network.model.Part
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val dao: ChatDao,
    private val api: ApiService,
    private val json: Json
) : Repository {
    override fun getAllMessages(conversationId: String): Flow<List<Message>> {
        return dao.getMessages(conversationId)
    }

    override suspend fun saveMessage(message: Message) {
        dao.saveMessage(message)
    }

    override suspend fun getConvById(convId: String): Conversation? {
        return dao.getConvById(convId)
    }

    override fun getConversations(): Flow<List<Conversation>> {
        return dao.getConversations()
    }

    override suspend fun saveConversation(conversation: Conversation) {
        dao.saveConversation(conversation)
    }

    override suspend fun deleteConversation(convId: String) {
        dao.deleteConversation(convId)
    }

    override fun responseStream(conversationId: String): Flow<String> = flow {
        val context = dao.getContext(conversationId).map { it.toContent() }.reversed()
        val response = api.streamGenerateContent(BuildConfig.API_KEY, request= GeminiRequest(context))
        if (!response.isSuccessful) throw Exception("Request Failed: ${response.code()}")
        val body = response.body() ?: throw Exception("Empty body")

        body.source().use { source ->
            while (!source.exhausted()){
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data: ")) continue
                val jsonDecoder = try {
                    json.decodeFromString<GeminiResponse>(line.removePrefix("data: "))
                }catch (e: Exception) {continue }

                val text = jsonDecoder.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!text.isNullOrEmpty()) emit(text)
            }
        }
    }.flowOn(Dispatchers.IO)
    fun Message.toContent(): Content {
        return Content(
            role = if (user) "user" else "model",
            parts = listOf(Part(content))
        )
    }
}