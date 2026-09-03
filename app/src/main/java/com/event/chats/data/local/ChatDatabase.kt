package com.event.chats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Message::class, Conversation::class],
    version = 4,
    exportSchema = true
)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun dao(): ChatDao
}

val migration_3_4 = object : Migration(3, 4){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Message ADD COLUMN imagePath TEXT DEFAULT NULL")
    }
}