package com.event.chats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.event.chats.ui.component.ChatDrawerContent
import com.event.chats.ui.component.ClearFocus
import com.event.chats.ui.component.MessageBubble
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun MainScreen(viewmodel: ChatViewmodel = hiltViewModel()){
    val sendState by viewmodel.sendState.collectAsStateWithLifecycle()
    val  messages by viewmodel.messages.collectAsStateWithLifecycle()
    val userMsg by viewmodel.userMsg.collectAsStateWithLifecycle()
    val activeConvId by viewmodel.activeConvId.collectAsStateWithLifecycle()
    val conversationItem by viewmodel.chatDrawerItem.collectAsStateWithLifecycle()
    val responseStream by viewmodel.responseStream.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    ClearFocus()

    LaunchedEffect(responseStream) {
        if (responseStream != null){
            listState.animateScrollToItem(0)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawerContent(
                conversations = conversationItem,
                activeConversationId = activeConvId,
                onNewChat = {
                    viewmodel.newConversation()
                    scope.launch { drawerState.close() }
                },
                onSelectConversation = {
                    viewmodel.selectConversation(it)
                    scope.launch { drawerState.close() }
                },
                onDelete = {viewmodel.deleteConversation(it)}
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(conversationItem.find { it.id == activeConvId }?.title ?: "New chat") },
                    navigationIcon = {
                        IconButton(
                            onClick = {scope.launch { drawerState.open() }}
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()
            ) {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    responseStream?.let {
                        item {
                            MessageBubble(content = it, user = false)
                        }
                    }
                    items(items = messages, key = { it.id }) {
                        MessageBubble(content = it.content, user = it.user)
                    }

                }

                if (sendState is SendState.Error){
                    Text(
                        text = (sendState as SendState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = userMsg,
                        onValueChange = {viewmodel.updateUserMsg(it)},
                        modifier = Modifier.weight(1f),
                        placeholder = {Text("Type anything...")},
                        keyboardActions = KeyboardActions(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(5.dp))

                    if (sendState is SendState.Sending){
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    else{
                        IconButton(
                            onClick = {viewmodel.response(userMsg)},
                            enabled = userMsg.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send"
                            )
                        }
                    }
                }
            }
        }
    }

}

