package com.event.chats.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM Message WHERE conversationId = :convId ORDER BY timestamp DESC")
    fun getMessages(convId: String): Flow<List<Message>>

    @Query("SELECT * FROM Message WHERE conversationId=:convId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getContext(convId: String,limit: Int = 15): List<Message>
    @Insert
    suspend fun saveMessage(message: Message)

    @Query("SELECT * FROM conversations WHERE id= :convId")
    suspend fun getConvById(convId: String): Conversation?

    @Query("SELECT * From conversations ORDER BY createdAt")
    fun getConversations(): Flow<List<Conversation>>

    @Insert
    suspend fun saveConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id=:convId")
    suspend fun deleteConversation(convId: String)

}