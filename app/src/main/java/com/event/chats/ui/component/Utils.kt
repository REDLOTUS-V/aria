package com.event.chats.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.event.chats.data.local.Conversation
import com.event.chats.ui.ConversationItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClearFocus(focusManager: FocusManager = LocalFocusManager.current) {
    val keyboardVisible = WindowInsets.isImeVisible
    LaunchedEffect(keyboardVisible) {
        if (!keyboardVisible){
            focusManager.clearFocus()
        }
    }
}

@Composable
fun Drawer(
    conversation: List<ConversationItem>,
    activeId: String,
    onNewChat: () -> Unit,
    onSelectConv: (String) -> Unit,
    onDelete: (String) -> Unit
    ){
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("chat")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onNewChat() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("New Chat")
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items = conversation, key = {it.id}) {
                    val isActive = it.id == activeId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isActive)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                            .clickable{onSelectConv(activeId)}
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = it.title, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {onDelete(activeId)}
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }

                    }
                }
            }
        }
    }
}