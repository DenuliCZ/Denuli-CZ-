package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val genre: String = "Pop",
    val lyrics: String = "",
    val vocalPrompt: String = "",
    val stylePrompt: String = "",
    val excludedPrompt: String = "",
    val bpm: Int = 120,
    val vocalGain: Float = 0.8f,
    val backgroundMusicMix: Float = 0.5f,
    val vocalDelayMs: Long = 150L,
    val voiceEffect: String = "None", // None, Reverb, Echo, Pitch Up, Robot
    val natureSound: String = "None", // None, Birds, Rain, Ocean, Thunder, Jungle
    val videoTemplate: String = "Retro Sunset", // Cinematic, Retro, Neon, Nature
    val videoTransition: String = "Fade", // Fade, Slide, Zoom, Cut
    val fontName: String = "Default",
    val colorGradingPreset: String = "Neutral", // Warm, Cyberpunk, Film, B&W
    val projectLicense: String = "Denuli-CZ Copyright Standard",
    val isSharedPublicly: Boolean = false,
    val rightsPriceCzk: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_tracks")
data class CommunityTrack(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val genre: String,
    val lyrics: String,
    val likes: Int = 0,
    val commentsRaw: String = "", // Semicolon or newline-separated comments list
    val isForSale: Boolean = false,
    val priceCzk: Double = 0.0,
    val customLicense: String = "Denuli-CZ Certified",
    val videoTemplate: String = "Retro Sunset",
    val soundSampleType: String = "Vocal + Nature",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMsg(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiAssistant: Boolean = false,
    val isProjectShare: Boolean = false,
    val sharedProjectId: Int? = null,
    val commentTimeSeconds: Int? = null
)

@Entity(tableName = "ai_settings_templates")
data class AiSettingsTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val templateName: String,
    val genre: String,
    val stylePrompt: String,
    val excludedPrompt: String,
    val voiceEffect: String,
    val colorGrading: String
)

@Entity(tableName = "purchase_transactions")
data class PurchaseTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: String,          // Google Play Console Order ID Format (e.g. GPA.1234-5678-9012-34567)
    val productId: String,        // e.g. "sub_denuli_monthly_149" or "sub_denuli_yearly_999"
    val purchaseTime: Long,       // Milliseconds from epoch
    val formattedDate: String,    // Format: YYYY-MM-DD HH:MM:ss UTC (Play Console Style)
    val amountCzk: Double,        // Price in CZK (149.0 or 999.0)
    val currency: String = "CZK", // Currency, default CZK
    val paymentStatus: String = "Charged" // Play Console: Charged, Refunded
)
