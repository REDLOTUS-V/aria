package com.event.chats.ui.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant


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

fun Long.formatTimestamp(): String {
    val localTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localTime.hour
    val amPm = if (hour < 12) "am" else "pm"

    val display = when{
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$display:${localTime.minute} $amPm"
}
