package com.event.data

import android.util.Base64
import android.util.Log
import com.event.data.model.ChatMessage
import com.event.data.model.Chat
import com.event.database.entity.Conversation
import com.event.database.Dao
import com.event.database.entity.Message
import com.event.network.ApiService
import com.event.network.model.Content
import com.event.network.model.GeminiRequest
import com.event.network.model.GeminiResponse
import com.event.network.model.InlineData
import com.event.network.model.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val dao: Dao,
    private val api: ApiService,
    private val json: Json
): Repository {
    override fun getMessages(convId: String): Flow<List<ChatMessage>> = dao.getMessages(convId).map {
        messages -> messages.map { it.chatMessage() }
    }
    override suspend fun saveMsg(msg: ChatMessage) = dao.saveMsg(msg.message())
    override suspend fun deleteMsg(id: Int) = dao.deleteMsg(id)

    override fun getConversations(): Flow<List<Chat>> = dao.getConversations().map {
        convs -> convs.map { it.toChat() }
    }
    override suspend fun getConvId(convId: String): Chat? = dao.getConvId(convId)?.toChat()
    override suspend fun saveConv(conv: Chat) = dao.saveConv(conv.toConversation())
    override suspend fun updateSystemPrompt(convId: String, system: String?) = dao.updateSystemPrompt(convId, system)
    override suspend fun deleteConv(convId: String) = dao.deleteConv(convId)

    override fun responseStream(convId: String): Flow<String> = flow {
        val context = dao.getContext(convId)
            .mapIndexed { index, msg -> msg.toContent(index == 0) }
            .reversed()
        val systemInstruction = Content(
            parts = listOf(
                Part(text = dao.getConvId(convId)?.systemPrompt?.ifBlank { null } ?: BuildConfig.SYSTEM_PROMPT))
        )

        val response = api.generateContent(
            apiKey = BuildConfig.API_KEY,
            request = GeminiRequest(context, systemInstruction)
        )
        if (!response.isSuccessful) throw Exception("Request Failed: ${response.message()}")

        val modelResponse = StringBuilder()
        response.body()?.source()?.use { source ->
            while (!source.exhausted()){
                val line = source.readUtf8Line() ?: break
                if(!line.startsWith("data: ")) continue
                try {
                    val text = json.decodeFromString<GeminiResponse>(line.removePrefix("data: "))
                        .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text

                    if (!text.isNullOrBlank()) {
                        modelResponse.append(text)
                        emit(text)
                    }
                }
                catch (e: Exception){
                    Log.e("serialization Error: ", e.message ?: "unknown")
                }
            }
        } ?: throw Exception("Empty response body")

        saveMsg(
            ChatMessage(
                content = modelResponse.toString(),
                user = false,
                conversationId = convId
            )
        )
    }.flowOn(Dispatchers.IO)

    suspend fun Message.toContent(includedImage: Boolean): Content {
        val imagePart = if (includedImage){
            imagePath?.let { path ->
                withContext(Dispatchers.IO){ path.toBase64() }?.let {
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = it))
                }
            }
        }else null

        return Content(
            role = if (user) "user" else "model",
            parts = listOfNotNull(Part(text = content), imagePart)
        )
    }
    fun String.toBase64(): String? {
        return try {
            val bytes = File(this).readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }catch (e: Exception){
            Log.e("imageEncode", "${e.message}")
            null
        }
    }

    fun Message.chatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            user = user,
            timeStamp = timeStamp,
            conversationId = conversationId,
            imagePath = imagePath,
        )
    }
    fun ChatMessage.message(): Message {
        return Message(
            id = id,
            content = content,
            user = user,
            timeStamp = timeStamp,
            conversationId = conversationId,
            imagePath = imagePath
        )
    }

    fun Conversation.toChat(): Chat {
        return Chat(
            id = id,
            title = title,
            systemPrompt = systemPrompt,
        )
    }

    fun Chat.toConversation(): Conversation {
        return Conversation(
            id = id,
            title = title,
            systemPrompt = systemPrompt

        )
    }
}