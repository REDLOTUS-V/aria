package com.event.chats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Message::class, Conversation::class],
    version = 3,
    exportSchema = true
)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun dao(): ChatDao
}