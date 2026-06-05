package com.example.data.repository

import com.example.data.database.Project
import com.example.data.database.MarketplaceItem
import com.example.data.database.AudioTrack
import com.example.data.database.StudioDao
import kotlinx.coroutines.flow.Flow

class StudioRepository(private val studioDao: StudioDao) {
    val allProjects: Flow<List<Project>> = studioDao.getAllProjects()
    val allMarketplaceItems: Flow<List<MarketplaceItem>> = studioDao.getAllMarketplaceItems()

    fun getTracksForProject(projectId: Int): Flow<List<AudioTrack>> {
        return studioDao.getTracksForProject(projectId)
    }

    suspend fun insertTrack(track: AudioTrack): Long {
        return studioDao.insertTrack(track)
    }

    suspend fun updateTrack(track: AudioTrack) {
        studioDao.updateTrack(track)
    }

    suspend fun deleteTrack(track: AudioTrack) {
        studioDao.deleteTrack(track)
    }

    suspend fun deleteTracksForProject(projectId: Int) {
        studioDao.deleteTracksForProject(projectId)
    }

    suspend fun getProjectById(id: Int): Project? {
        return studioDao.getProjectById(id)
    }

    suspend fun insertProject(project: Project): Long {
        return studioDao.insertProject(project)
    }

    suspend fun deleteProject(project: Project) {
        studioDao.deleteProject(project)
    }

    suspend fun prepopulateMarketplace(items: List<MarketplaceItem>) {
        studioDao.insertMarketplaceItems(items)
    }

    suspend fun purchaseItem(item: MarketplaceItem) {
        studioDao.updateMarketplaceItem(item.copy(isPurchased = true))
    }
}
