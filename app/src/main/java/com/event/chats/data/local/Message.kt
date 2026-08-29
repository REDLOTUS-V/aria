package com.event.chats.data.local
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Message",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val user: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val conversationId: String
)