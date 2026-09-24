package com.event.chat

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.event.chat.component.ChatDrawerContent
import com.event.chat.component.ClearFocus
import com.event.chat.component.MessageBubble
import com.event.chat.component.MessageInputField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewmodel: ChatViewmodel = hiltViewModel()){

    val sendState by viewmodel.sendState.collectAsStateWithLifecycle()
    val messages by viewmodel.messages.collectAsStateWithLifecycle()
    val chats by viewmodel.chats.collectAsStateWithLifecycle()
    val responseStream by viewmodel.responseStream.collectAsStateWithLifecycle()
    val activeChatId by viewmodel.activeChatId.collectAsStateWithLifecycle()
    val userMsg by viewmodel.userMsg.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    ClearFocus()

    val currentStreamingText = responseStream?.takeIf { it.first == activeChatId }?.second

    LaunchedEffect(currentStreamingText) {
        currentStreamingText?.let {
            listState.animateScrollToItem(0)
        }
    }
    var showDialog by  remember { mutableStateOf(false) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawerContent(
                conversations = chats,
                activeConversationId = activeChatId,
                onClick = {
                    showDialog = true
                },
                onNewChat = {
                    viewmodel.newChat()
                    scope.launch { drawerState.close() }
                },
                onSelectConversation = {
                    viewmodel.selectChat(it)
                    scope.launch { drawerState.close() }
                },
                onDelete = {viewmodel.deleteChat(it)}
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->

            val clipboard = LocalClipboard.current
            val context = LocalContext.current
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.Bottom,
                    contentPadding = PaddingValues(top = 70.dp, bottom = 70.dp)
                ) {
                    currentStreamingText?.let {
                        item {
                            MessageBubble(content = it, user = false)
                        }
                    }
                    items(items = messages, key = { it.id }) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (it.user) Alignment.End else Alignment.Start
                        ) {
                            val failedUsermsg = it.id == messages.firstOrNull { m -> m.user }?.id
                            MessageBubble(
                                content = it.content,
                                user = it.user,
                                imagePath = it.imagePath,
                                timestamp = it.timeStamp,
                                onCopy = {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText("copy", it.content
                                                )
                                            )
                                        )
                                    }
                                },
                                onDelete = {viewmodel.deleteMsg(it.id)}
                            )
                            if (failedUsermsg && sendState is SendState.Error) {
                                IconButton({ viewmodel.retry() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "retry"
                                    )
                                }
                            }
                        }
                    }
                }
                TopAppBar(
                    modifier = Modifier.clip(RoundedCornerShape(24.dp)).align(Alignment.TopCenter),
                    title = { Text(chats.find{ it.id == activeChatId }?.title ?: "New chat") },
                    windowInsets = WindowInsets(0,0,0,0),
                    /*colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                    ),*/
                    navigationIcon = {
                        IconButton(
                            onClick = {scope.launch { drawerState.open() }}
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "menu")
                        }
                    }
                )
                MessageInputField(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    msg = userMsg,
                    onMsgChange = {viewmodel.updateMsg(it)},
                    sendState = sendState,
                    onSend = {msg, uri ->
                        viewmodel.sendMessage(context = context, msg =  msg, uri =  uri )
                    },
                    onFailed = {viewmodel.retry()}
                )
            }
        }
    }

    val activeChat = chats.find { it.id == activeChatId }
    var systemPrompt by remember(activeChat) {
        mutableStateOf(activeChat?.systemPrompt ?: "") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {showDialog=false},
            title = { Text("System prompt")},
            text = {
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = {systemPrompt= it},
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 10
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewmodel.setSystemPrompt(systemPrompt)
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}