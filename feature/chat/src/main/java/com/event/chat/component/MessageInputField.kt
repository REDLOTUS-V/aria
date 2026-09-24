package com.event.chat.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.event.chat.SendState

@Composable
fun MessageInputField(
    modifier: Modifier = Modifier,
    msg: String,
    onMsgChange: (String) -> Unit,
    sendState: SendState,
    onSend: ( String, Uri?) -> Unit,
    onFailed: () -> Unit

){

    var selectedImage by rememberSaveable {mutableStateOf<Uri?>(null)}
    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {uri: Uri? ->
            uri?.let { selectedImage = uri }
        }
    )

    Surface(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
        //tonalElevation = 4.dp,
        //shadowElevation = 4.dp
    ) {
        Column {
            selectedImage?.let { uri ->
                Box(
                    modifier = Modifier.padding(10.dp).size(100.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "image preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    IconButton(
                        onClick = { selectedImage = null },
                        modifier = Modifier.size(26.dp),

                        ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
            if (sendState is SendState.Error) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sendState.message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onFailed() }) {
                        Text("Retry")
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) { Icon(Icons.Default.Photo, "photo") }
                Spacer(modifier = Modifier.width(5.dp))
                TextField(
                    value = msg,
                    onValueChange = { onMsgChange(it)},
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type anything...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))

                if (sendState is SendState.Sending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = {
                            onSend( msg, selectedImage)
                            selectedImage = null
                        },
                        enabled = msg.isNotBlank() || selectedImage != null
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send, "Send"
                        )
                    }
                }
            }
        }
    }
}