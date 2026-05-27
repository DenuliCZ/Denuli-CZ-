package com.example.data.repository

import com.example.data.database.AiSettingsTemplate
import com.example.data.database.ChatMsg
import com.example.data.database.CommunityTrack
import com.example.data.database.Project
import com.example.data.database.StudioDao
import com.example.data.database.PurchaseTransaction
import com.example.data.network.GeminiClient
import kotlinx.coroutines.flow.Flow

class StudioRepository(private val studioDao: StudioDao) {

    // Database Flows
    val allProjects: Flow<List<Project>> = studioDao.getAllProjects()
    val trendingTracks: Flow<List<CommunityTrack>> = studioDao.getTrendingCommunityTracks()
    val chatMessages: Flow<List<ChatMsg>> = studioDao.getAllChatMessages()
    val settingsTemplates: Flow<List<AiSettingsTemplate>> = studioDao.getAllSettingsTemplates()
    val allTransactions: Flow<List<PurchaseTransaction>> = studioDao.getAllTransactions()

    // Database Actions with Suspend
    suspend fun getProjectById(id: Int): Project? = studioDao.getProjectById(id)
    suspend fun saveProject(project: Project): Long = studioDao.insertProject(project)
    suspend fun updateProject(project: Project) = studioDao.updateProject(project)
    suspend fun deleteProjectById(id: Int) = studioDao.deleteProjectById(id)

    suspend fun saveCommunityTrack(track: CommunityTrack): Long = studioDao.insertCommunityTrack(track)
    suspend fun updateCommunityTrack(track: CommunityTrack) = studioDao.updateCommunityTrack(track)

    suspend fun saveChatMessage(msg: ChatMsg): Long = studioDao.insertChatMessage(msg)
    suspend fun clearChat() = studioDao.clearChatHistory()

    suspend fun saveTemplate(template: AiSettingsTemplate): Long = studioDao.insertSettingsTemplate(template)
    suspend fun saveTransaction(tx: PurchaseTransaction): Long = studioDao.insertTransaction(tx)
    suspend fun clearTransactions() = studioDao.clearAllTransactions()

    // Gemini API wrappers
    suspend fun getAiLyrics(projectTitle: String, genre: String, stylePrompt: String, excludedPrompt: String): String {
        val systemPrompt = """
            You are 'Denuli Studio AI' - a world-class songwriter and lyricist.
            Generate original, high-quality, creative song lyrics in Czech or English depending on context.
            Style theme requested: $stylePrompt.
            Avoid the following styles/moods: $excludedPrompt.
            Genre: $genre.
            Format with clear verse, chorus division and rhythm indicators. Keep it evocative, lyrical and catchy!

            CRITICAL SONGWRITING & SECTION TAG RULES (MANDATORY):
            1. ALL structural tags, musical section indicators, cue prompts, or lyric labels (such as Verse, Chorus, Bridge, Intro, Outro, Guitar Solo, Instrumental, Drop, Hook, Pre-Chorus, etc.) MUST ALWAYS be in English. Do NOT translate section headers to Czech (e.g., use "[Verse]" and NOT "[Sloka]", use "[Chorus]" and NOT "[Refrén]").
            2. ALL structural tags and section cues MUST be written strictly in SQUARE BRACKETS like [...] (for example: [Verse 1], [Chorus], [Bridge], [Outro]).
            3. NEVER use ROUND BRACKETS / PARENTHESES like (...) for section headers, prompts, or cues. Round brackets are strictly interpreted by vocalists and AI models as backing/secondary lines to be SUNG. Therefore, putting prompts in round brackets (like "(Verse 1)" or "(Chorus)") causes singers or AI to sing the prompt out loud. All structural cues MUST be in English and in SQUARE BRACKETS [...] instead.
            4. At the end of every single song, you MUST ALWAYS append/include the exact following Outro block (with the specified line endings, square brackets, and Czech/English text):
            [Outro]
            [Drums slowly fade out, leaving only warm synth chords]
            Jen pro vás...
            Každý tón, každý tep mého srdce
        """.trimIndent()

        val userPrompt = "Write lyrics for a song titled '$projectTitle' in the genre '$genre'. Incorporate the mood '$stylePrompt'."
        return GeminiClient.generateWithPrompt(systemPrompt, userPrompt)
    }

    suspend fun getAiMasteringAndOptimization(lyrics: String, genre: String, voiceEffect: String, colorGrading: String): String {
        val systemPrompt = """
            You are 'Denuli Studio Master Coach' - a professional recording engineer, color grader, and audio-visual producer.
            Provide brief, highly precise Czech or English professional tips on how to mix, master, and visually grade the current project for optimal Full HD social media posting.
            Address vocals, noise reduction, delay timing, video transition styles, and cinematic filters.
            Keep output formatted with short, extremely helpful bullet points (maximum 5 points).
        """.trimIndent()

        val userPrompt = """
            Optimize my project:
            - Genre: $genre
            - Selected Vocal Effect: $voiceEffect
            - Selected Color Grading: $colorGrading
            - Current Lyrics excerpt: ${lyrics.take(150)}...
        """.trimIndent()
        return GeminiClient.generateWithPrompt(systemPrompt, userPrompt)
    }

    suspend fun askAiAssistant(userMessage: String, chatHistoryCsv: String): String {
        val systemPrompt = """
            You are 'Denuli Studio AI Assistant' (alias Denuli-CZ Support).
            A user of our comprehensive music & video studio app is asking you a question.
            Be extremely encouraging, music-wise, and super helpful.
            Answer in Czech if the user asks in Czech, or English if they ask in English.
            Give tips on vocal recording, multitrack mixing, copyright protection licenses, transitions, fonts, and direct social exports (to Spotify, Instagram, YouTube, TikTok).
            Keep information concise, creative, and professional!
        """.trimIndent()

        val userPrompt = """
            Recent conversation history overview:
            $chatHistoryCsv
            
            User's new question:
            $userMessage
        """.trimIndent()
        return GeminiClient.generateWithPrompt(systemPrompt, userPrompt)
    }

    suspend fun wipeAllUserDataAndRecordings() {
        studioDao.clearAllProjects()
        studioDao.clearAllCommunityTracks()
        studioDao.clearAllTransactions()
        studioDao.clearChatHistory()
        studioDao.clearAllSettingsTemplates()
    }
}
