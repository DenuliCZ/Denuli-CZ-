package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)

    // Community Tracks
    @Query("SELECT * FROM community_tracks ORDER BY likes DESC, createdAt DESC")
    fun getTrendingCommunityTracks(): Flow<List<CommunityTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityTrack(track: CommunityTrack): Long

    @Update
    suspend fun updateCommunityTrack(track: CommunityTrack)

    // Live Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMsg>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMsg): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // Settings Templates
    @Query("SELECT * FROM ai_settings_templates")
    fun getAllSettingsTemplates(): Flow<List<AiSettingsTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettingsTemplate(template: AiSettingsTemplate): Long

    @Query("DELETE FROM projects")
    suspend fun clearAllProjects()

    @Query("DELETE FROM community_tracks")
    suspend fun clearAllCommunityTracks()

    @Query("DELETE FROM ai_settings_templates")
    suspend fun clearAllSettingsTemplates()

    // Purchase Transactions for Play Billing Accounting Audit Evidence
    @Query("SELECT * FROM purchase_transactions ORDER BY purchaseTime DESC")
    fun getAllTransactions(): kotlinx.coroutines.flow.Flow<List<PurchaseTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PurchaseTransaction): Long

    @Query("DELETE FROM purchase_transactions")
    suspend fun clearAllTransactions()
}
