package com.event.chats.data.repository

import android.util.Base64
import android.util.Log
import com.event.chats.BuildConfig
import com.event.chats.data.local.ChatDao
import com.event.chats.data.local.Conversation
import com.event.chats.data.local.Message
import com.event.chats.data.network.ApiService
import com.event.chats.data.network.model.Content
import com.event.chats.data.network.model.GeminiRequest
import com.event.chats.data.network.model.GeminiResponse
import com.event.chats.data.network.model.InlineData
import com.event.chats.data.network.model.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
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

    override fun responseStream(convId: String): Flow<String> = flow {
        val context = dao.getContext(convId).mapIndexed { index, msg ->
            msg.toContent(includedImage = index == 0) }.reversed()

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

     suspend fun Message.toContent(includedImage: Boolean): Content {
        val imagePart = if (includedImage && imagePath != null) {
            withContext(Dispatchers.IO){
                imagePath.pathToBase64()?.let {
                    Part(inlineData = InlineData(mimeType = "image/jpeg", it))
                }
            }
        } else null

        return Content(
            role = if (user) "user" else "model",
            parts = listOfNotNull(Part(content), imagePart)
        )
    }

    fun String.pathToBase64(): String? {
       return  try {
            val bytes = File(this).readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }catch (e: Exception){
            Log.e("ImageEncode", "Failed to encode from path", e)
            null
        }
    }
}