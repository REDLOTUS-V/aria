package com.event.chats.ui

import android.content.ClipData
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
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

    var selectedImage by remember { mutableStateOf<Uri?>(null) }

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

            val pickPhoto = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = {uri: Uri? ->
                    uri?.let { selectedImage = uri }
                }
            )
            val clipboard = LocalClipboard.current
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()
            ) {
                val context = LocalContext.current

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
                        Column(horizontalAlignment = if (it.user) Alignment.End else Alignment.Start) {
                            val failedUsermsg = it.id == messages.firstOrNull { m -> m.user }?.id
                            MessageBubble(
                                content = it.content,
                                user = it.user,
                                imagePath = it.imagePath,
                                onCopy = {
                                    scope.launch {
                                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("copy",it.content)))
                                    }
                                }
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

                if (sendState is SendState.Error){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = (sendState as SendState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {viewmodel.retry()}) {
                            Text("Retry")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            pickPhoto.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {Icon(Icons.Default.Photo, "photo") }
                    Spacer(modifier = Modifier.width(5.dp))
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
                            onClick = {
                                viewmodel.response(context, userMsg, selectedImage)
                                selectedImage = null
                            },
                            enabled = userMsg.isNotBlank() || selectedImage != null
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

