package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val lyrics: String,
    val genre: String,
    val vocalEffect: String,
    val backgroundAmbience: String,
    val timestamp: Long = System.currentTimeMillis(),
    val trackDuration: Int = 15,
    val audioPath: String? = null,
    val videoPath: String? = null,
    val bpm: Int = 120
)

@Entity(tableName = "marketplace_items")
data class MarketplaceItem(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val isPurchased: Boolean = false,
    val type: String, // "Beat" | "Vocal Effect" | "Ambience" | "Community Track"
    val durationSec: Int = 30,
    val audioPath: String? = null,
    val tags: String? = null,
    val isCommunityPublished: Boolean = false
)

@Entity(tableName = "audio_tracks")
data class AudioTrack(
    @PrimaryKey(autoGenerate = true) val trackId: Int = 0,
    val projectId: Int,
    val name: String,
    val filePath: String?,
    val volume: Float = 0.8f,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val isRecordArmed: Boolean = false,
    val trackType: String,
    val midiInstrument: String? = "Sine Lead",
    val midiPattern: String? = null,
    val eqLow: Float = 0.0f,
    val eqMid: Float = 0.0f,
    val eqHigh: Float = 0.0f,
    val compEnabled: Boolean = false,
    val compThreshold: Float = -20.0f,
    val compRatio: Float = 2.0f,
    val reverbEnabled: Boolean = false,
    val reverbWet: Float = 0.3f,
    val reverbFeedback: Float = 0.5f
)
