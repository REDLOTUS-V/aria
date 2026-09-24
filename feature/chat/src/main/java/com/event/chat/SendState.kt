package com.event.chat

sealed interface SendState {
    object Idle: SendState
    object Sending: SendState
    data class Error(val message: String): SendState
}