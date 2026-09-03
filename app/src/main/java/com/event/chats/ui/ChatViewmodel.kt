package com.event.chats.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.event.chats.data.local.Conversation
import com.event.chats.data.local.Message
import com.event.chats.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.let
import kotlin.text.take
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewmodel @Inject constructor(private val repository: Repository): ViewModel() {

    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()
    private val _activeConvId= MutableStateFlow(Uuid.random().toString())
    val activeConvId: StateFlow<String> = _activeConvId.asStateFlow()
    private val _responseStream = MutableStateFlow<String?>(null)
    val responseStream: StateFlow<String?> = _responseStream
    val chatDrawerItem: StateFlow<List<ConversationItem>> = repository.getConversations()
        .map { conversations ->
            conversations.map {
                ConversationItem(
                    id = it.id,
                    title = it.title
                )
             }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val messages: StateFlow<List<Message>> = _activeConvId
        .flatMapLatest { repository.getAllMessages(it)}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private var failedConvId: String? = null
    private val _userMsg = MutableStateFlow("")
    val userMsg: StateFlow<String> = _userMsg.asStateFlow()

    fun updateUserMsg(msg: String){
        _userMsg.value = msg
    }
    fun newConversation(){
        _activeConvId.value = Uuid.random().toString()
    }
    fun selectConversation(convId: String){
        _activeConvId.value = convId
    }
    fun response(context: Context, msg: String, uri: Uri? = null){
        viewModelScope.launch {
            val imagePath = uri?.saveImageToInternal(context)
            val currentConvId = _activeConvId.value
            saveConversation(currentConvId,msg)
            val user = Message(content = msg, conversationId = currentConvId, user = true, imagePath = imagePath)
            repository.saveMessage(user)
            _userMsg.value = ""
            stream(currentConvId)
        }
    }

    fun retry(){
        val convId = failedConvId ?: return
        viewModelScope.launch {
            stream(convId)
        }
    }
    private suspend fun stream(convId: String){
        _sendState.value = SendState.Sending
        _responseStream.value = ""

        val accumulated = StringBuilder()
        var isFailed = false
        repository.responseStream(convId).catch {e ->
            _responseStream.value = null
            _sendState.value = SendState.Error(e.message ?: "unknown error")
            failedConvId = convId
            isFailed = true
        }.collect {
            accumulated.append(it)
            _responseStream.value = accumulated.toString()
        }
        if (isFailed) return
        val model = Message(content = accumulated.toString(), conversationId = convId, user = false)
        repository.saveMessage(model)

        messages.first { messages-> messages.any { it.content == model.content } }
        _responseStream.value = null
        _sendState.value = SendState.Idle
        failedConvId = null
    }

    suspend fun saveConversation(convId: String,text: String){
        val existingConvId = repository.getConvById(convId)
        if (existingConvId == null) {
            val newConversation = Conversation(
                id = convId,
                title = text.take(40).let { if (text.length > 40) "$it..." else it }
            )
            repository.saveConversation(newConversation)
        }
    }
    fun deleteConversation(convId: String){
        viewModelScope.launch { repository.deleteConversation(convId) }
    }
}
fun Uri.saveImageToInternal(context: Context): String? {
    return try {
        val imageDir = File(context.filesDir, "images").apply { mkdirs() }
        val fileName = "chat_img_${UUID.randomUUID()}.jpg"
        val localFile = File(imageDir, fileName)

        context.contentResolver.openInputStream(this)?.use {inputStream ->
            localFile.outputStream().use {
                inputStream.copyTo(it)
            }
        }
        localFile.absolutePath

    }catch (e: Exception) {
        android.util.Log.e("ImageSave Failed:", e.message ?: "dont know whats wrong")
        null
    }
}