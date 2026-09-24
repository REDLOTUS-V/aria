package com.event.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.event.database.entity.Conversation
import com.event.database.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Query("SELECT * FROM Messages WHERE conversationId=:convId ORDER BY timeStamp DESC")
    fun getMessages(convId: String): Flow<List<Message>>
    @Query("SELECT * FROM Messages WHERE conversationId=:convId ORDER BY timeStamp DESC LIMIT:limit")
    suspend fun getContext(convId: String, limit: Int = 15): List<Message>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMsg(message: Message)
    @Query("DELETE  FROM Messages WHERE id = :id")
    suspend fun deleteMsg(id: Int)
    @Query("SELECT * FROM Conversations  ORDER BY createdAt DESC")
    fun getConversations(): Flow<List<Conversation>>
    @Query("SELECT * FROM Conversations WHERE id=:convId")
    suspend fun getConvId(convId: String): Conversation?
    @Insert
    suspend fun saveConv(conv: Conversation)
    @Query("UPDATE Conversations SET systemPrompt =:system WHERE id =:convId")
    suspend fun updateSystemPrompt(convId: String, system: String?)
    @Query("DELETE  FROM Conversations WHERE id=:convId")
    suspend fun deleteConv(convId: String)
}