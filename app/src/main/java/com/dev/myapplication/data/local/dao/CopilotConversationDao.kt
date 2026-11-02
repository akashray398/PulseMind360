package com.dev.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.myapplication.data.local.entity.CopilotConversationEntity
import com.dev.myapplication.data.local.entity.CopilotMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CopilotConversationDao {
    @Query("SELECT * FROM copilot_conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<CopilotConversationEntity>>

    @Query("SELECT * FROM copilot_conversations WHERE id = :id")
    suspend fun getConversationById(id: String): CopilotConversationEntity?

    @Query("SELECT * FROM copilot_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<CopilotMessageEntity>>

    @Query("SELECT * FROM copilot_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesListForConversation(conversationId: String): List<CopilotMessageEntity>

    @Query("SELECT * FROM copilot_messages WHERE queryText LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<CopilotMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: CopilotConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CopilotMessageEntity)

    @Query("UPDATE copilot_messages SET userFeedback = :feedback WHERE id = :messageId")
    suspend fun updateMessageFeedback(messageId: String, feedback: String)

    @Query("DELETE FROM copilot_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM copilot_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM copilot_conversations")
    suspend fun clearAllConversations()

    @Query("DELETE FROM copilot_messages")
    suspend fun clearAllMessages()
}
