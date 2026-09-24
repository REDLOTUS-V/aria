package com.event.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.event.data.Repository
import com.event.data.model.Chat
import com.event.data.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewmodel @Inject constructor(private val repository: Repository ): ViewModel() {
    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()
    private val _responseStream = MutableStateFlow<Pair<String, String>?>(null)
    val responseStream = _responseStream.asStateFlow()
    private val _activeChatId = MutableStateFlow(Uuid.random().toString())
    val activeChatId = _activeChatId.asStateFlow()
    val messages: StateFlow<List<ChatMessage>> = _activeChatId.flatMapLatest {
        repository.getMessages(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val chats: StateFlow<List<Chat>> = repository.getConversations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    private val _userMsg = MutableStateFlow("")
    val userMsg: StateFlow<String> = _userMsg.asStateFlow()
    private var failedChatId: String? = null

    fun updateMsg(msg: String){
        _userMsg.value = msg
    }
    fun newChat(){
        _activeChatId.value = Uuid.random().toString()
    }

    fun selectChat(chatId: String){
        _activeChatId.value = chatId
    }
    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteConv(chatId)
        }
    }

    fun sendMessage(context: Context, msg: String, uri: Uri?) {
        viewModelScope.launch {
            val currentChatId = _activeChatId.value
            val imagePath = uri?.saveToInternal(context)
            saveChat(currentChatId, msg)
            repository.saveMsg(
                ChatMessage(content = msg, user = true, conversationId = currentChatId, imagePath = imagePath)
            )
            response(currentChatId)
        }
    }

    private suspend fun saveChat(chatId: String, msg: String){
        val existingChatId = repository.getConvId(chatId)
        if (existingChatId == null){
            repository.saveConv(Chat(id = chatId, title = if (msg.length > 45) "${msg}..." else msg))
        }
    }

    private suspend fun response(chatId: String) {
        _sendState.value = SendState.Sending
        _responseStream.value = chatId to ""
        var responseFailed = false
        _userMsg.value = ""
        val accumulator = StringBuilder()
        repository.responseStream(chatId).catch {
            _responseStream.value = null
            _sendState.value = SendState.Error(it.message ?: "unknown")
            responseFailed = true
            failedChatId = chatId
        }.collect {
            accumulator.append(it)
            _responseStream.value = chatId to accumulator.toString()
        }
        if (responseFailed) return
        messages.first { messages -> messages.any { it.content == accumulator.toString() } }
        _responseStream.value = null
        failedChatId = null
        _sendState.value = SendState.Idle
    }

    fun retry() {
        val failedChatId = failedChatId ?: return
        viewModelScope.launch {
            response(failedChatId)
        }
    }

    fun setSystemPrompt(prompt: String) {
        viewModelScope.launch {
            val currentConvId = _activeChatId.value
            repository.updateSystemPrompt(currentConvId, prompt)
        }
    }
    fun deleteMsg(id: Int){
        viewModelScope.launch {
            repository.deleteMsg(id)
        }
    }
}
fun Uri.saveToInternal(context: Context): String? {
    return try {
        val imageDir = File(context.filesDir, "images").apply { mkdirs() }
        val localfile = File(imageDir, "img_${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(this)?.use { inputStream ->
            localfile.outputStream().use {
                inputStream.copyTo(it)
            }
        }
        localfile.absolutePath
    } catch (e: Exception) {
        null
    }
}
