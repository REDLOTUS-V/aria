package com.event.chats.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage

@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    content: String,
    user: Boolean,
    imagePath: String? = null,
    onCopy: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    timestamp: Long? = null
){
    var isExpanded by remember { mutableStateOf(false) }
    var longClick by remember { mutableStateOf(false) }

        Column(
          /*modifier = Modifier.combinedClickable(
              onClick = {isExpanded = !isExpanded},
              //onLongClick = {longClick = !longClick}
          ).animateContentSize()*/
        ){
            Box(
                modifier = modifier.wrapContentSize()
                    .widthIn(min = 200.dp).padding(horizontal = 10.dp, vertical = 5.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 15.dp,
                            topEnd = 15.dp,
                            bottomStart = if (user) 15.dp else 2.dp,
                            bottomEnd = if (user) 2.dp else 15.dp
                        )
                    )
                    .background(
                        if (user) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                    ).padding(10.dp)
                    .pointerInput(Unit){
                        detectTapGestures(onTap = {isExpanded = !isExpanded},)
                    }
            ) {
                Column{
                    imagePath?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "show image",
                            modifier = Modifier.align(Alignment.Start)
                                .height(260.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    SelectionContainer {
                        if (content.isNotBlank()) {
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (user) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            if (isExpanded) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, end = 10.dp),
                    //horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { onCopy?.invoke() }, modifier = Modifier.size(16.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "copy",
                            //modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier.width(4.dp))
                    IconButton(onClick = {onDelete?.invoke()}, modifier = Modifier.size(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete",
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    timestamp?.let {
                        Text(
                            text = timestamp.formatTimestamp(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
}