package com.event.chats.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    content: String,
    user: Boolean,
    imagePath: String? = null,
    onCopy: (() -> Unit)? = null
){
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = if (user) Arrangement.End else Arrangement.Start
    ) {
        Column {
            Box(
                modifier = Modifier.widthIn(min = 300.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 15.dp,
                            topEnd = 15.dp,
                            bottomStart = if (user) 15.dp else 4.dp,
                            bottomEnd = if (user) 4.dp else 15.dp
                        )
                    )
                    .background(
                        if (user) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.onSecondaryFixed
                    )
                    .padding(10.dp)
            ) {
                Column {
                    if (content.isNotBlank()) {
                        Text(
                            text = content,
                            fontSize = 15.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    imagePath?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "show image",
                            modifier = Modifier.fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
            IconButton(onClick = { onCopy?.invoke() }, modifier = Modifier.align(if (user) Alignment.End else Alignment.Start).size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.CopyAll,
                    contentDescription = "copy",
                    modifier = Modifier.size(16.dp),

                )
            }
        }
    }
}