package com.event.chats.ui

import android.os.Message

interface SendState {
    object Idle: SendState
    object Sending: SendState
    data class Error(val message: String): SendState
}