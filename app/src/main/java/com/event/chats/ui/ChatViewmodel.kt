package com.event.chats.ui

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

    fun response(userMsg: String){
        viewModelScope.launch {
            val currentConvId = _activeConvId.value
            saveConversation(userMsg)

            _sendState.value = SendState.Sending
            val user = Message(
                content = userMsg,
                conversationId = currentConvId,
                user = true
            )
            repository.saveMessage(user)
            _userMsg.value = ""

            _responseStream.value = ""
            val accumulator = StringBuilder()
            repository.responseStream(currentConvId)
                .catch {e ->
                    _responseStream.value = null
                    _sendState.value = SendState.Error(e.message ?: "unknown error")
                }
                .collect {
                    accumulator.append(it)
                    _responseStream.value = accumulator.toString()
                }
            val savedMsg = Message(
                content = accumulator.toString(),
                conversationId = currentConvId,
                user = false
            )
            repository.saveMessage(savedMsg)

            //suspend coroutine until saved mesage emitted by messages
            messages.first {messages -> messages.any { it.content == savedMsg.content && !it.user } }
            _responseStream.value = null
            _sendState.value = SendState.Idle
        }
    }
    suspend fun saveConversation(text: String){
        val currentConvId = _activeConvId.value
        val existingConvId = repository.getConvById(currentConvId)
        if (existingConvId == null) {
            val newConversation = Conversation(
                id = currentConvId,
                title = text.take(40).let { if (text.length > 40) "$it..." else it }
            )
            repository.saveConversation(newConversation)
        }
    }
    suspend fun saveMessage(message: Message){

    }
    fun deleteConversation(convId: String){
        viewModelScope.launch { repository.deleteConversation(convId) }
    }
}