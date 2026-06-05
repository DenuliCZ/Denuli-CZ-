package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    @Query("SELECT * FROM projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("SELECT * FROM marketplace_items")
    fun getAllMarketplaceItems(): Flow<List<MarketplaceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketplaceItems(items: List<MarketplaceItem>)

    @Update
    suspend fun updateMarketplaceItem(item: MarketplaceItem)

    @Query("SELECT * FROM audio_tracks WHERE projectId = :projectId ORDER BY trackId ASC")
    fun getTracksForProject(projectId: Int): Flow<List<AudioTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: AudioTrack): Long

    @Update
    suspend fun updateTrack(track: AudioTrack)

    @Delete
    suspend fun deleteTrack(track: AudioTrack)

    @Query("DELETE FROM audio_tracks WHERE projectId = :projectId")
    suspend fun deleteTracksForProject(projectId: Int)
}
