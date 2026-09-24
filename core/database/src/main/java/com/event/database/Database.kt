package com.event.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.event.database.entity.Conversation
import com.event.database.entity.Message

@Database(entities = [Message::class, Conversation::class], version = 2)
abstract class Database: RoomDatabase() {
    abstract fun dao(): Dao
}

