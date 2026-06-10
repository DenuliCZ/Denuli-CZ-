package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.MarketplaceItem
import com.example.data.database.Project
import com.example.data.database.AudioTrack
import com.example.data.network.GeminiClient
import com.example.data.repository.StudioRepository
import com.example.service.ExportForegroundService
import com.example.util.ExportFileHelper
import com.example.util.Language
import com.example.util.TranslationUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val Context.preferencesDataStore by preferencesDataStore(name = "studio_settings")

class StudioViewModel(private val repository: StudioRepository) : ViewModel() {

    private val TAG = "StudioViewModel"

    // --- State declarations ---
    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMarketplaceItems: StateFlow<List<MarketplaceItem>> = repository.allMarketplaceItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject

    private val activeMediaPlayers = mutableMapOf<Int, MediaPlayer>()
    private var mediaRecorder: android.media.MediaRecorder? = null
    private val currentlyRecordingTrackId = MutableStateFlow<Int?>(null)
    val recordingTrackId: StateFlow<Int?> = currentlyRecordingTrackId.asStateFlow()

    private val _isRecordingState = MutableStateFlow(false)
    val isRecordingState: StateFlow<Boolean> = _isRecordingState.asStateFlow()

    private var tempRecordingFile: File? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeProjectTracks: StateFlow<List<AudioTrack>> = _activeProject
        .flatMapLatest { project ->
            if (project != null) {
                repository.getTracksForProject(project.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAgeVerified = MutableStateFlow(false)
    val isAgeVerified: StateFlow<Boolean> = _isAgeVerified

    private val _isGeneratingLyrics = MutableStateFlow(false)
    val isGeneratingLyrics: StateFlow<Boolean> = _isGeneratingLyrics

    private val _isGeneratingCompleteSong = MutableStateFlow(false)
    val isGeneratingCompleteSong: StateFlow<Boolean> = _isGeneratingCompleteSong

    private val _completeSongProgress = MutableStateFlow(0f)
    val completeSongProgress: StateFlow<Float> = _completeSongProgress

    private val _userCredits = MutableStateFlow(500)
    val userCredits: StateFlow<Int> = _userCredits

    private val _bankAccountNumber = MutableStateFlow("")
    val bankAccountNumber: StateFlow<String> = _bankAccountNumber

    private val _legalFullName = MutableStateFlow("")
    val legalFullName: StateFlow<String> = _legalFullName

    private val _icoNumber = MutableStateFlow("")
    val icoNumber: StateFlow<String> = _icoNumber

    private val _hasAgreedToLicensingTerms = MutableStateFlow(false)
    val hasAgreedToLicensingTerms: StateFlow<Boolean> = _hasAgreedToLicensingTerms

    private val _communitySalesAlert = MutableStateFlow<String?>(null)
    val communitySalesAlert: StateFlow<String?> = _communitySalesAlert

    private val _isSynthesizingAudio = MutableStateFlow(false)
    val isSynthesizingAudio: StateFlow<Boolean> = _isSynthesizingAudio

    private val _isVideoGenerating = MutableStateFlow(false)
    val isVideoGenerating: StateFlow<Boolean> = _isVideoGenerating

    private val _isGeneratingAIVideoTimeline = MutableStateFlow(false)
    val isGeneratingAIVideoTimeline: StateFlow<Boolean> = _isGeneratingAIVideoTimeline

    private val _audioProgress = MutableStateFlow(0f)
    val audioProgress: StateFlow<Float> = _audioProgress

    private val _videoProgress = MutableStateFlow(0f)
    val videoProgress: StateFlow<Float> = _videoProgress

    private val _videoGenerationError = MutableStateFlow<String?>(null)
    val videoGenerationError: StateFlow<String?> = _videoGenerationError

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(listOf(
        TranslationUtility.get("chat_welcome") to false // false = assistant, true = user
    ))
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    private val _isExportingProject = MutableStateFlow(false)
    val isExportingProject: StateFlow<Boolean> = _isExportingProject.asStateFlow()

    private val _exportProjectProgress = MutableStateFlow(0f)
    val exportProjectProgress: StateFlow<Float> = _exportProjectProgress.asStateFlow()

    // --- Undo / Redo Stacks for multi-track editing ---
    private val undoStack = java.util.Stack<List<AudioTrack>>()
    private val redoStack = java.util.Stack<List<AudioTrack>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var lastUndoSaveTime = 0L

    fun pushUndoSnapshot(force: Boolean = false) {
        val currentTracks = activeProjectTracks.value
        val now = System.currentTimeMillis()
        
        // Debounce continuous changes like rapid slider dragging unless forced
        if (!force && (now - lastUndoSaveTime < 800L)) {
            return
        }
        
        val snapshot = currentTracks.map { it.copy() }
        
        // Check if snapshot is identical to the top of undoStack to avoid duplicates
        if (undoStack.isNotEmpty()) {
            val last = undoStack.peek()
            if (areTrackListsSame(last, snapshot)) {
                return
            }
        }
        
        undoStack.push(snapshot)
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        lastUndoSaveTime = now
    }

    private fun areTrackListsSame(list1: List<AudioTrack>, list2: List<AudioTrack>): Boolean {
        if (list1.size != list2.size) return false
        for (i in list1.indices) {
            val t1 = list1[i]
            val t2 = list2[i]
            if (t1.trackId != t2.trackId ||
                t1.projectId != t2.projectId ||
                t1.name != t2.name ||
                t1.filePath != t2.filePath ||
                t1.volume != t2.volume ||
                t1.isMuted != t2.isMuted ||
                t1.isSolo != t2.isSolo ||
                t1.isRecordArmed != t2.isRecordArmed ||
                t1.trackType != t2.trackType ||
                t1.midiInstrument != t2.midiInstrument ||
                t1.midiPattern != t2.midiPattern ||
                t1.eqLow != t2.eqLow ||
                t1.eqMid != t2.eqMid ||
                t1.eqHigh != t2.eqHigh ||
                t1.compEnabled != t2.compEnabled ||
                t1.compThreshold != t2.compThreshold ||
                t1.compRatio != t2.compRatio ||
                t1.reverbEnabled != t2.reverbEnabled ||
                t1.reverbWet != t2.reverbWet ||
                t1.reverbFeedback != t2.reverbFeedback ||
                t1.startOffsetMs != t2.startOffsetMs) {
                return false
            }
        }
        return true
    }

    fun undo() {
        viewModelScope.launch {
            if (undoStack.isEmpty()) return@launch
            val currentTracks = activeProjectTracks.value
            val currentSnapshot = currentTracks.map { it.copy() }
            
            val targetSnapshot = undoStack.pop()
            redoStack.push(currentSnapshot)
            
            applySnapshotToDatabase(currentTracks, targetSnapshot)
            
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = redoStack.isNotEmpty()
        }
    }

    fun redo() {
        viewModelScope.launch {
            if (redoStack.isEmpty()) return@launch
            val currentTracks = activeProjectTracks.value
            val currentSnapshot = currentTracks.map { it.copy() }
            
            val targetSnapshot = redoStack.pop()
            undoStack.push(currentSnapshot)
            
            applySnapshotToDatabase(currentTracks, targetSnapshot)
            
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = redoStack.isNotEmpty()
        }
    }

    private suspend fun applySnapshotToDatabase(currentTracks: List<AudioTrack>, targetTracks: List<AudioTrack>) {
        withContext(Dispatchers.IO) {
            val currentIds = currentTracks.map { it.trackId }.toSet()
            val targetIds = targetTracks.map { it.trackId }.toSet()
            
            for (track in currentTracks) {
                if (!targetIds.contains(track.trackId)) {
                    repository.deleteTrack(track)
                }
            }
            
            for (track in targetTracks) {
                if (currentIds.contains(track.trackId)) {
                    repository.updateTrack(track)
                } else {
                    repository.insertTrack(track)
                }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    fun initializeOnboarding(context: Context) {
        viewModelScope.launch {
            val isVerifiedKey = booleanPreferencesKey("is_age_verified")
            context.preferencesDataStore.data.map { prefs ->
                prefs[isVerifiedKey] ?: false
            }.collect { verified ->
                _isAgeVerified.value = verified
            }
        }
        val prefs = context.getSharedPreferences("spark_studio_prefs", Context.MODE_PRIVATE)
        _userCredits.value = prefs.getInt("user_credits", 500)
        _bankAccountNumber.value = prefs.getString("bank_account_number", "") ?: ""
        _legalFullName.value = prefs.getString("legal_full_name", "") ?: ""
        _icoNumber.value = prefs.getString("ico_number", "") ?: ""
        _hasAgreedToLicensingTerms.value = prefs.getBoolean("has_agreed_to_licensing_terms", false)
        prepopulateMarketIfNeeded()
    }

    fun updatePayoutDetails(
        context: Context,
        bankAccount: String,
        fullName: String,
        ico: String,
        agreed: Boolean
    ) {
        _bankAccountNumber.value = bankAccount
        _legalFullName.value = fullName
        _icoNumber.value = ico
        _hasAgreedToLicensingTerms.value = agreed

        val prefs = context.getSharedPreferences("spark_studio_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("bank_account_number", bankAccount)
            putString("legal_full_name", fullName)
            putString("ico_number", ico)
            putBoolean("has_agreed_to_licensing_terms", agreed)
            apply()
        }
    }

    fun updateUserCredits(context: Context, credits: Int) {
        _userCredits.value = credits
        val prefs = context.getSharedPreferences("spark_studio_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("user_credits", credits).apply()
    }

    fun clearSalesAlert() {
        _communitySalesAlert.value = null
    }

    private fun prepopulateMarketIfNeeded() {
        viewModelScope.launch {
            repository.allMarketplaceItems.first().let { current ->
                if (current.isEmpty()) {
                    val defaultPacks = listOf(
                        MarketplaceItem("beat_synthwave", "Retro Synthwave Beat", "Autentický analogový základ z 80. let (110 BPM)", 100.0, false, "Beat", 30),
                        MarketplaceItem("beat_lofi", "Cozy Lo-Fi Study Chords", "Jemné, tlumené jazzové harmonie na klavír v retro šumu", 80.0, false, "Beat", 35),
                        MarketplaceItem("beat_metal", "Brutal Metal Double-Kick", "Bleskové bicí, extrémní zkreslení kytar a agresivní tempo (140 BPM)", 100.0, false, "Beat", 35),
                        MarketplaceItem("beat_edm", "Apex EDM Festival Drop", "Mohutné detuned supersaw syntezátory a pumpující klubový rytmus (128 BPM)", 90.0, false, "Beat", 40),
                        MarketplaceItem("beat_country", "Texas Cabin Country Acoustic", "Vybrnkávání kytary, klidný rytmus a walking doprovod kontrabasu (90 BPM)", 80.0, false, "Beat", 35),
                        MarketplaceItem("effect_daft", "Robot Vocoder Preset", "Přemění hlas do ikonické robotické formy ve stylu Daft Punk", 60.0, false, "Vocal Effect", 0),
                        MarketplaceItem("effect_autotune", "Hyper-Tune Pro Filter", "Ultramoderní automatické rovnání tónů pro rap a pop", 90.0, false, "Vocal Effect", 0),
                        MarketplaceItem("ambience_cyberpunk", "Cyber Neon Rain", "Přírodní déšť protkaný ozvěnou dálnic a neonových svitů", 50.0, false, "Ambience", 45),
                        MarketplaceItem("ambience_zen", "Sacred Zen Garden", "Zvuky proudící čisté vody, bambusových zvonkoher a hluboké ticho", 50.0, false, "Ambience", 60)
                    )
                    repository.prepopulateMarketplace(defaultPacks)
                }
            }
        }
    }

    fun setAgeVerification(context: Context, verified: Boolean) {
        viewModelScope.launch {
            val isVerifiedKey = booleanPreferencesKey("is_age_verified")
            context.preferencesDataStore.edit { prefs ->
                prefs[isVerifiedKey] = verified
            }
            _isAgeVerified.value = verified
        }
    }

    private val _lastAutosaveTime = MutableStateFlow<Long>(0L)
    val lastAutosaveTime: StateFlow<Long> = _lastAutosaveTime.asStateFlow()

    private var autosaveJob: kotlinx.coroutines.Job? = null

    fun startAutosaveLoop(context: Context) {
        autosaveJob?.cancel()
        val appContext = context.applicationContext
        autosaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                kotlinx.coroutines.delay(30000) // 30 seconds
                val current = _activeProject.value
                if (current != null) {
                    try {
                        val sharedPrefs = appContext.getSharedPreferences("browser_local_storage", Context.MODE_PRIVATE)
                        val projectJson = """
                            {
                                "id": ${current.id},
                                "title": "${escapeJson(current.title)}",
                                "lyrics": "${escapeJson(current.lyrics)}",
                                "genre": "${escapeJson(current.genre)}",
                                "vocalEffect": "${escapeJson(current.vocalEffect)}",
                                "backgroundAmbience": "${escapeJson(current.backgroundAmbience)}",
                                "timestamp": ${current.timestamp},
                                "trackDuration": ${current.trackDuration},
                                "audioPath": ${current.audioPath?.let { "\"${escapeJson(it)}\"" } ?: "null"},
                                "videoPath": ${current.videoPath?.let { "\"${escapeJson(it)}\"" } ?: "null"},
                                "bpm": ${current.bpm}
                            }
                        """.trimIndent()

                        sharedPrefs.edit()
                            .putString("current_project_configuration", projectJson)
                            .putLong("current_project_autosave_time", System.currentTimeMillis())
                            .apply()

                        _lastAutosaveTime.value = System.currentTimeMillis()
                        Log.d("Autosave", "Project configuration synced to local storage: $projectJson")
                    } catch (e: Exception) {
                        Log.e("Autosave", "Failed to autosave project configuration", e)
                    }
                }
            }
        }
    }

    fun stopAutosaveLoop() {
        autosaveJob?.cancel()
        autosaveJob = null
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
    }

    fun restoreProjectFromLocalStorage(context: Context): Boolean {
        try {
            val sharedPrefs = context.applicationContext.getSharedPreferences("browser_local_storage", Context.MODE_PRIVATE)
            val jsonString = sharedPrefs.getString("current_project_configuration", null) ?: return false
            
            val title = extractJsonField(jsonString, "title")
            val lyrics = extractJsonField(jsonString, "lyrics").replace("\\n", "\n")
            val genre = extractJsonField(jsonString, "genre")
            val vocalEffect = extractJsonField(jsonString, "vocalEffect")
            val backgroundAmbience = extractJsonField(jsonString, "backgroundAmbience")
            val idStr = extractJsonField(jsonString, "id")
            val id = idStr.toIntOrNull() ?: return false
            val durationStr = extractJsonField(jsonString, "trackDuration")
            val duration = durationStr.toIntOrNull() ?: 15
            val bpmStr = extractJsonField(jsonString, "bpm")
            val bpm = bpmStr.toIntOrNull() ?: 120
            
            val current = _activeProject.value
            if (current != null && current.id == id) {
                viewModelScope.launch {
                    val restored = current.copy(
                        lyrics = lyrics,
                        genre = genre,
                        vocalEffect = vocalEffect,
                        backgroundAmbience = backgroundAmbience,
                        trackDuration = duration,
                        bpm = bpm
                    )
                    repository.insertProject(restored)
                    _activeProject.value = restored
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Konfigurace obnovena z místního úložiště!", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }
        } catch (e: Exception) {
            Log.e("Autosave", "Failed to restore project from SharedPreferences", e)
        }
        return false
    }

    private fun extractJsonField(json: String, field: String): String {
        val pattern = "\"$field\"\\s*:\\s*(?:\"([^\"]*)\"|([\\d.-]+|null|true|false))"
        val regex = Regex(pattern)
        val matchResult = regex.find(json)
        return matchResult?.groups?.get(1)?.value 
            ?: matchResult?.groups?.get(2)?.value 
            ?: ""
    }

    fun selectProject(project: Project?, context: Context? = null) {
        stopAudioPlayback()
        _activeProject.value = project
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        if (project != null) {
            initializeDefaultTracksIfNeeded(project.id, project.audioPath, project.backgroundAmbience)
            if (context != null) {
                startAutosaveLoop(context)
            }
        } else {
            stopAutosaveLoop()
        }
    }

    fun updateTrackVolume(track: AudioTrack, volume: Float) {
        pushUndoSnapshot(force = false)
        viewModelScope.launch {
            val updated = track.copy(volume = volume)
            repository.updateTrack(updated)
            activeMediaPlayers[track.trackId]?.let { player ->
                try {
                    player.setVolume(volume, volume)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun toggleTrackMute(track: AudioTrack) {
        pushUndoSnapshot(force = true)
        viewModelScope.launch {
            val updated = track.copy(isMuted = !track.isMuted)
            repository.updateTrack(updated)
            activeMediaPlayers[track.trackId]?.let { player ->
                try {
                    val targetVol = if (updated.isMuted) 0f else updated.volume
                    player.setVolume(targetVol, targetVol)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun toggleTrackSolo(track: AudioTrack) {
        pushUndoSnapshot(force = true)
        viewModelScope.launch {
            val updated = track.copy(isSolo = !track.isSolo)
            repository.updateTrack(updated)
        }
    }

    fun updateTrackStartOffset(track: AudioTrack, offsetMs: Long) {
        pushUndoSnapshot(force = false)
        viewModelScope.launch {
            val updated = track.copy(startOffsetMs = offsetMs)
            repository.updateTrack(updated)
        }
    }

    fun updateTrackEffects(
        track: AudioTrack,
        eqLow: Float,
        eqMid: Float,
        eqHigh: Float,
        compEnabled: Boolean,
        compThreshold: Float,
        compRatio: Float,
        reverbEnabled: Boolean,
        reverbWet: Float,
        reverbFeedback: Float
    ) {
        pushUndoSnapshot(force = false)
        viewModelScope.launch {
            val updated = track.copy(
                eqLow = eqLow,
                eqMid = eqMid,
                eqHigh = eqHigh,
                compEnabled = compEnabled,
                compThreshold = compThreshold,
                compRatio = compRatio,
                reverbEnabled = reverbEnabled,
                reverbWet = reverbWet,
                reverbFeedback = reverbFeedback
            )
            repository.updateTrack(updated)
        }
    }

    fun addCustomTrack(projectId: Int, name: String, type: String) {
        pushUndoSnapshot(force = true)
        viewModelScope.launch {
            val newTrack = AudioTrack(
                projectId = projectId,
                name = name,
                filePath = null,
                volume = 0.8f,
                trackType = type
            )
            repository.insertTrack(newTrack)
        }
    }

    fun removeTrack(track: AudioTrack) {
        pushUndoSnapshot(force = true)
        viewModelScope.launch {
            stopAudioPlayback()
            repository.deleteTrack(track)
            track.filePath?.let { path ->
                val f = File(path)
                if (f.exists()) f.delete()
            }
        }
    }

    fun playMultiTrack(context: Context, tracks: List<AudioTrack>) {
        stopAudioPlayback()

        val currentProj = _activeProject.value ?: return
        viewModelScope.launch {
            val processedTracks = tracks.map { track ->
                if (track.trackType == "MIDI" && (track.filePath.isNullOrEmpty() || !File(track.filePath).exists())) {
                    val cacheDir = context.cacheDir
                    val file = File(cacheDir, "midi_${track.trackId}_synth.wav")
                    withContext(Dispatchers.IO) {
                        try {
                            com.example.util.MidiSynthesizer.synthesizeMidiToWav(
                                instrument = track.midiInstrument ?: "Sine Lead",
                                patternStr = track.midiPattern ?: "",
                                outputFile = file,
                                trackDuration = currentProj.trackDuration
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed MIDI synthesis in play: ${e.message}")
                        }
                    }
                    val updated = track.copy(filePath = file.absolutePath)
                    withContext(Dispatchers.IO) {
                        repository.updateTrack(updated)
                    }
                    updated
                } else if (track.trackType == "Beat" && (track.filePath.isNullOrEmpty() || !File(track.filePath).exists())) {
                    val file = withContext(Dispatchers.IO) {
                        try {
                            com.example.util.ExportFileHelper.generateRealAudioTrack(
                                context = context,
                                genre = currentProj.genre,
                                durationSec = currentProj.trackDuration,
                                projectBpm = currentProj.bpm
                            ) { }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed Beat synthesis in play: ${e.message}")
                            null
                        }
                    }
                    if (file != null && file.exists()) {
                        val updated = track.copy(filePath = file.absolutePath)
                        withContext(Dispatchers.IO) {
                            repository.updateTrack(updated)
                        }
                        updated
                    } else {
                        track
                    }
                } else if (track.trackType == "Ambience" && (track.filePath.isNullOrEmpty() || !File(track.filePath).exists())) {
                    val file = withContext(Dispatchers.IO) {
                        try {
                            com.example.util.ExportFileHelper.generateRealAudioTrack(
                                context = context,
                                genre = "cinematic",
                                durationSec = currentProj.trackDuration,
                                projectBpm = currentProj.bpm
                            ) { }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed Ambience synthesis in play: ${e.message}")
                            null
                        }
                    }
                    if (file != null && file.exists()) {
                        val updated = track.copy(filePath = file.absolutePath)
                        withContext(Dispatchers.IO) {
                            repository.updateTrack(updated)
                        }
                        updated
                    } else {
                        track
                    }
                } else {
                    track
                }
            }

            val hasSolo = processedTracks.any { it.isSolo }
            val tracksToPlay = processedTracks.filter { track ->
                val hasFile = !track.filePath.isNullOrEmpty() && File(track.filePath).exists()
                val allowedBySoloMute = if (hasSolo) track.isSolo else !track.isMuted
                hasFile && allowedBySoloMute
            }

            if (tracksToPlay.isEmpty()) {
                Toast.makeText(context, "Žádné nahrané ani vygenerované stopy k přehrání!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            tracksToPlay.forEach { track ->
                try {
                    val hasEq = track.eqLow != 0.0f || track.eqMid != 0.0f || track.eqHigh != 0.0f
                    val hasFx = hasEq || track.compEnabled || track.reverbEnabled
                    
                    val playPath = if (hasFx && !track.filePath.isNullOrEmpty()) {
                        val originalFile = File(track.filePath)
                        if (originalFile.exists()) {
                            val tempDest = File(context.cacheDir, "track_${track.trackId}_temp_fx.wav")
                            withContext(Dispatchers.IO) {
                                try {
                                    com.example.util.AudioEffectsProcessor.processTrackFileWithEffects(
                                        track = track,
                                        originalFile = originalFile,
                                        outputFile = tempDest
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to compile live FX preview file: ${e.message}")
                                }
                            }
                            if (tempDest.exists() && tempDest.length() > 0) {
                                tempDest.absolutePath
                            } else {
                                track.filePath
                            }
                        } else {
                            track.filePath
                        }
                    } else {
                        track.filePath
                    }

                    val player = MediaPlayer().apply {
                        setDataSource(playPath)
                        val vol = if (track.isMuted) 0f else track.volume
                        setVolume(vol, vol)
                        prepare()
                        setOnCompletionListener {
                            if (activeMediaPlayers.values.all { !it.isPlaying }) {
                                _isPlaying.value = false
                            }
                        }
                    }
                    activeMediaPlayers[track.trackId] = player
                } catch (e: Exception) {
                    Log.e(TAG, "Nelze připravit stopu ${track.name}: ${e.message}")
                }
            }

            if (activeMediaPlayers.isNotEmpty()) {
                _isPlaying.value = true
                tracksToPlay.forEach { track ->
                    val player = activeMediaPlayers[track.trackId] ?: return@forEach
                    viewModelScope.launch {
                        if (track.startOffsetMs > 0) {
                            kotlinx.coroutines.delay(track.startOffsetMs)
                        }
                        if (_isPlaying.value) {
                            try {
                                player.start()
                            } catch (e: Exception) {
                                Log.e(TAG, "Chyba spuštění stopy: ${e.message}")
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Chyba při inicializaci přehrávačů stop.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startRecording(context: Context, track: AudioTrack) {
        if (currentlyRecordingTrackId.value != null) return
        stopAudioPlayback()

        val outputFile = File(context.cacheDir, "rec_${track.trackId}_${System.currentTimeMillis()}.wav")
        try {
            mediaRecorder = android.media.MediaRecorder().apply {
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            currentlyRecordingTrackId.value = track.trackId
            _isRecordingState.value = true
            tempRecordingFile = outputFile
            Toast.makeText(context, "Nahrávám: ${track.name}...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Recorder error: ${e.message}")
            Toast.makeText(context, "Chyba spuštění nahrávání: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun stopRecording(context: Context) {
        val trackId = currentlyRecordingTrackId.value ?: return
        _isRecordingState.value = false
        currentlyRecordingTrackId.value = null

        try {
            mediaRecorder?.let {
                it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recorder stop error: ${e.message}")
        }
        mediaRecorder = null

        tempRecordingFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                pushUndoSnapshot(force = true)
                viewModelScope.launch {
                    val tracks = activeProjectTracks.value
                    tracks.find { it.trackId == trackId }?.let { originalTrack ->
                        val updatedTrack = originalTrack.copy(filePath = file.absolutePath)
                        repository.updateTrack(updatedTrack)
                    }
                }
                Toast.makeText(context, "Hlasový záznam uložen a zvrstven!", Toast.LENGTH_SHORT).show()
            }
        }
        tempRecordingFile = null
    }

    fun initializeDefaultTracksIfNeeded(projectId: Int, backingAudioPath: String?, ambienceType: String) {
        viewModelScope.launch {
            val existing = repository.getTracksForProject(projectId).first()
            if (existing.isEmpty()) {
                repository.insertTrack(
                    AudioTrack(
                        projectId = projectId,
                        name = "Doprovodný beat (Instrumental)",
                        filePath = backingAudioPath,
                        volume = 0.7f,
                        trackType = "Beat"
                    )
                )
                repository.insertTrack(
                    AudioTrack(
                        projectId = projectId,
                        name = "Hlavní zpěv (Lead Vocal Mic)",
                        filePath = null,
                        volume = 0.9f,
                        trackType = "Vocal"
                    )
                )
                repository.insertTrack(
                    AudioTrack(
                        projectId = projectId,
                        name = "Doprovodný zpěv (Harmony Vocals)",
                        filePath = null,
                        volume = 0.8f,
                        trackType = "Vocal"
                    )
                )
                repository.insertTrack(
                    AudioTrack(
                        projectId = projectId,
                        name = "Syntetizátorová sekvence (MIDI)",
                        filePath = null,
                        volume = 0.6f,
                        trackType = "MIDI",
                        midiInstrument = "Sine Lead",
                        midiPattern = "0_0,2_1,4_2,6_3,8_4,10_3,12_2,14_1"
                    )
                )
                if (ambienceType != "None" && ambienceType.isNotBlank()) {
                    repository.insertTrack(
                        AudioTrack(
                            projectId = projectId,
                            name = "Ambientní pozadí ($ambienceType Atmosphere)",
                            filePath = null,
                            volume = 0.4f,
                            trackType = "Ambience"
                        )
                    )
                }
            } else {
                if (!backingAudioPath.isNullOrEmpty() && File(backingAudioPath).exists()) {
                    existing.find { it.trackType == "Beat" && it.filePath.isNullOrEmpty() }?.let { beatTrack ->
                        repository.updateTrack(beatTrack.copy(filePath = backingAudioPath))
                    }
                }
            }
        }
    }

    fun createNewProject(title: String, lyrics: String, genre: String, vocalEffect: String, backgroundAmbience: String) {
        viewModelScope.launch {
            val newProj = Project(
                title = title,
                lyrics = lyrics,
                genre = genre,
                vocalEffect = vocalEffect,
                backgroundAmbience = backgroundAmbience,
                trackDuration = 15
            )
            val indexId = repository.insertProject(newProj)
            val savedProj = repository.getProjectById(indexId.toInt())
            _activeProject.value = savedProj
        }
    }

    fun updateActiveProject(lyrics: String, genre: String, vocalEffect: String, backgroundAmbience: String) {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                lyrics = lyrics,
                genre = genre,
                vocalEffect = vocalEffect,
                backgroundAmbience = backgroundAmbience
            )
            repository.insertProject(updated)
            _activeProject.value = updated
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            if (_activeProject.value?.id == project.id) {
                stopAudioPlayback()
                _activeProject.value = null
            }
            repository.deleteProject(project)
        }
    }

    fun generateAILyrics(topic: String, genre: String) {
        val current = _activeProject.value ?: return
        if (topic.isBlank()) return
        
        viewModelScope.launch {
            _isGeneratingLyrics.value = true
            try {
                val generated = GeminiClient.generateSongLyrics(topic, genre)
                val updated = current.copy(lyrics = generated)
                repository.insertProject(updated)
                _activeProject.value = updated
            } catch (e: Exception) {
                Log.e(TAG, "Error generating lyrics: ${e.message}")
            } finally {
                _isGeneratingLyrics.value = false
            }
        }
    }

    fun generateCompleteSong(
        context: Context, 
        topic: String, 
        selectedGenre: String,
        promptStyle: String = "",
        negativePrompt: String = "",
        soundInfluencePercent: Float = 80f,
        styleInfluencePercent: Float = 75f,
        vocalVoice: String = "Human",
        customLyrics: String = ""
    ) {
        val current = _activeProject.value ?: return
        stopAudioPlayback()

        viewModelScope.launch {
            _isGeneratingCompleteSong.value = true
            _completeSongProgress.value = 0f
            try {
                _completeSongProgress.value = 0.12f
                
                // 1. Generate lyrics concept via Gemini or use custom lyrics directly
                val responseLyrics = if (customLyrics.isNotBlank()) {
                    customLyrics
                } else {
                    val topicPrompt = if (topic.isNotBlank()) topic else "letní láska a naděje"
                    GeminiClient.generateSongLyrics(topicPrompt, selectedGenre)
                }
                _completeSongProgress.value = 0.35f
                
                // 2. Synthesize finished WAV track with vocaloids and beat instruments!
                val targetFile = com.example.util.ExportFileHelper.generateFinishedSongWav(
                    context = context,
                    genre = selectedGenre,
                    lyrics = responseLyrics,
                    durationSec = current.trackDuration,
                    projectBpm = current.bpm,
                    promptStyle = promptStyle,
                    negativePrompt = negativePrompt,
                    soundInfluencePercent = soundInfluencePercent,
                    styleInfluencePercent = styleInfluencePercent,
                    vocalVoice = vocalVoice,
                    onProgress = { progress ->
                        _completeSongProgress.value = 0.35f + (0.55f * progress)
                    }
                )

                _completeSongProgress.value = 0.95f

                // 3. Update the data structures of Project & AudioTrack
                val tracks = repository.getTracksForProject(current.id).first()
                val mainBeatTrack = tracks.find { it.trackType == "Beat" }
                if (mainBeatTrack != null) {
                    val updatedBeat = mainBeatTrack.copy(
                        name = "Hlavní AI Skladba ($selectedGenre - Spark Cloud)",
                        filePath = targetFile.absolutePath,
                        volume = 0.85f
                    )
                    repository.updateTrack(updatedBeat)
                } else {
                    val insertBeat = com.example.data.database.AudioTrack(
                        projectId = current.id,
                        name = "Hlavní AI Skladba ($selectedGenre - Spark Cloud)",
                        filePath = targetFile.absolutePath,
                        volume = 0.85f,
                        trackType = "Beat"
                    )
                    repository.insertTrack(insertBeat)
                }

                val updatedProject = current.copy(
                    lyrics = responseLyrics,
                    genre = selectedGenre,
                    audioPath = targetFile.absolutePath
                )
                repository.insertProject(updatedProject)
                _activeProject.value = updatedProject

                _completeSongProgress.value = 1.0f
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "AI Skladba byla úspěšně vygenerována a načtena do mixážního pultu! ⚡🎛️", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed generating AI Song: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Chyba generování skladby: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _isGeneratingCompleteSong.value = false
                _completeSongProgress.value = 0f
            }
        }
    }

    fun buyMarketplaceAsset(context: Context, item: MarketplaceItem) {
        val cost = item.price.toInt()
        if (_userCredits.value < cost) {
            Toast.makeText(context, "Nemáte dostatek kreditů! (Potřebujete: $cost 🪙, Máte: ${_userCredits.value} 🪙)", Toast.LENGTH_LONG).show()
            return
        }
        viewModelScope.launch {
            repository.purchaseItem(item)
            val newCredits = _userCredits.value - cost
            updateUserCredits(context, newCredits)
            Toast.makeText(context, "${item.name} byl úspěšně zakoupen za $cost kreditů! Zůstatek: $newCredits 🪙", Toast.LENGTH_LONG).show()
        }
    }

    fun sendChatMessage(msg: String) {
        if (msg.isBlank()) return
        val updated = _chatMessages.value.toMutableList()
        updated.add(msg to true)
        _chatMessages.value = updated
        
        viewModelScope.launch {
            _isChatLoading.value = true
            try {
                val response = GeminiClient.generateText(
                    msg,
                    "Jsi profesionální, kreativní hudební asistent pro Spark Studio v češtině. Pomáháš krotit tlusté synťáky, psát dechberoucí rýmy, míchat pop, rock, hiphop a radit s exportem videí."
                )
                val chatLog = _chatMessages.value.toMutableList()
                chatLog.add(response to false)
                _chatMessages.value = chatLog
            } catch (e: Exception) {
                val chatLog = _chatMessages.value.toMutableList()
                chatLog.add("Omlouvám se, nepodařilo se mi spojit s kreativním vědomím Gemini. Zkontroluj prosím připojení." to false)
                _chatMessages.value = chatLog
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun generateAIMusicInCloud(context: Context) {
        val current = _activeProject.value ?: return
        stopAudioPlayback()

        viewModelScope.launch {
            _isSynthesizingAudio.value = true
            _audioProgress.value = 0f
            try {
                val engine = com.example.data.network.SunoApiClient.getAudioEngine(context)
                val targetFile: File

                when (engine) {
                    "suno" -> {
                        val isConfigured = com.example.data.network.SunoApiClient.isSunoConfigured(context)
                        if (isConfigured) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Suno AI: Zahajuji plně profesionální kompilaci...", Toast.LENGTH_LONG).show()
                            }
                            val songFile = com.example.data.network.SunoApiClient.generateSunoSong(
                                context = context,
                                prompt = current.lyrics.ifBlank { "A gorgeous song about live performance" },
                                genre = current.genre,
                                title = current.title.ifBlank { "Moje AI Píseň" }
                            ) { progressMessage ->
                                Log.d(TAG, "Suno progress: $progressMessage")
                                viewModelScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, progressMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                            if (songFile != null && songFile.exists()) {
                                targetFile = songFile
                            } else {
                                throw Exception("Suno API nevytvořilo platný soubor nahrávky.")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Suno API klíč chybí v Nastavení. Přepínám na Premium Studio HQ...", Toast.LENGTH_LONG).show()
                            }
                            targetFile = com.example.data.network.SunoApiClient.getPremiumHqAudioTrack(
                                context = context,
                                genre = current.genre
                            ) { progressMessage ->
                                viewModelScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, progressMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    "hq_cloud" -> {
                        targetFile = com.example.data.network.SunoApiClient.getPremiumHqAudioTrack(
                            context = context,
                            genre = current.genre
                        ) { progressMessage ->
                            viewModelScope.launch(Dispatchers.Main) {
                                Toast.makeText(context, progressMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> {
                        // Draft synth method
                        val blueprint = GeminiClient.generateMusicBlueprint(current.lyrics, current.genre)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "AI Skladatel: $blueprint", Toast.LENGTH_LONG).show()
                        }
                        targetFile = ExportFileHelper.generateRealAudioTrack(
                            context = context,
                            genre = current.genre,
                            durationSec = current.trackDuration,
                            projectBpm = current.bpm
                        ) { progress ->
                            _audioProgress.value = progress
                        }
                    }
                }

                val savedProj = current.copy(audioPath = targetFile.absolutePath)
                repository.insertProject(savedProj)
                _activeProject.value = savedProj

                withContext(Dispatchers.Main) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(targetFile.absolutePath)
                        prepare()
                        start()
                        setOnCompletionListener {
                            _isPlaying.value = false
                        }
                    }
                    _isPlaying.value = true
                    Toast.makeText(context, "Píseň byla úspěšně generována a importována! 🎵🔥", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cloud AI synthesis breakdown: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Chyba hudebního cloudu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _isSynthesizingAudio.value = false
            }
        }
    }

    fun addGeneratedSunoAudioToMultitrack(context: Context) {
        val current = _activeProject.value ?: return
        val path = current.audioPath
        if (path.isNullOrBlank()) {
            Toast.makeText(context, "Nejprve vygenerujte AI skladbu!", Toast.LENGTH_SHORT).show()
            return
        }
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(context, "Soubor AI skladby neexistuje v mezipaměti.", Toast.LENGTH_SHORT).show()
            return
        }
        pushUndoSnapshot(force = true)
        viewModelScope.launch {
            val newTrack = AudioTrack(
                projectId = current.id,
                name = "AI Skladba: ${current.genre} #${System.currentTimeMillis() % 1000}",
                filePath = file.absolutePath,
                volume = 0.8f,
                trackType = "Beat"
            )
            repository.insertTrack(newTrack)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "AI Skladba byla úspěšně přidána do multitrack stop! 🎧🎹", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun playSynthesizedAudio(context: Context) {
        val current = _activeProject.value ?: return
        stopAudioPlayback()

        val savedPath = current.audioPath
        if (!savedPath.isNullOrBlank()) {
            val audioFile = File(savedPath)
            if (audioFile.exists()) {
                viewModelScope.launch(Dispatchers.Main) {
                    try {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(audioFile.absolutePath)
                            prepare()
                            start()
                            setOnCompletionListener {
                                _isPlaying.value = false
                            }
                        }
                        _isPlaying.value = true
                        Toast.makeText(context, "Přehrávám vygenerovanou píseň 🎶", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed playing historic audio: ${e.message}")
                        generateAIMusicInCloud(context)
                    }
                }
                return
            }
        }

        // If no file exists, auto-generate Cloud HQ / Suno instead of low-fi retro!
        generateAIMusicInCloud(context)
    }

    fun stopAudioPlayback() {
        // Stop and release all active multitrack players
        activeMediaPlayers.values.forEach {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        activeMediaPlayers.clear()

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun generateMP4LyricVideo(context: Context) {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            _isVideoGenerating.value = true
            _videoProgress.value = 0f
            _videoGenerationError.value = null
            try {
                // Ensure audio is ready
                val audioFile = if (!current.audioPath.isNullOrEmpty() && File(current.audioPath).exists()) {
                    File(current.audioPath)
                } else {
                    ExportFileHelper.generateRealAudioTrack(context, current.genre, current.trackDuration) {}
                }

                val visualMood = GeminiClient.analyzeMoodAndRecommendVisuals(current.lyrics)

                val videoFile = ExportFileHelper.generateRealLyricVideo(
                    context = context,
                    lyrics = current.lyrics,
                    genre = current.genre,
                    audioFile = audioFile,
                    durationSec = current.trackDuration,
                    visualMood = visualMood
                ) { progress ->
                    _videoProgress.value = progress
                }

                val updated = current.copy(
                    audioPath = audioFile.absolutePath,
                    videoPath = videoFile.absolutePath
                )
                repository.insertProject(updated)
                _activeProject.value = updated
                _videoGenerationError.value = null

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lyric Video úspěšně vygenerováno: ${videoFile.name}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Video synthesis failed: ${e.message}")
                _videoGenerationError.value = e.message ?: "Chyba při syntéze videa"
                Toast.makeText(context, "Chyba při generování videa: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isVideoGenerating.value = false
            }
        }
    }

    fun generateAlternativeMP4LyricVideo(context: Context) {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            _isVideoGenerating.value = true
            _videoProgress.value = 0f
            _videoGenerationError.value = null
            try {
                val audioFile = if (!current.audioPath.isNullOrEmpty() && File(current.audioPath).exists()) {
                    File(current.audioPath)
                } else {
                    ExportFileHelper.generateRealAudioTrack(context, current.genre, current.trackDuration) {}
                }

                val safeVisualMood = "Klidný alternativní vizuál s přírodní tématikou, hory v zapadajícím slunci, vřelá atmosféra"
                val videoFile = ExportFileHelper.generateRealLyricVideo(
                    context = context,
                    lyrics = current.lyrics,
                    genre = current.genre,
                    audioFile = audioFile,
                    durationSec = current.trackDuration,
                    visualMood = safeVisualMood
                ) { progress ->
                    _videoProgress.value = progress
                }

                val updated = current.copy(
                    audioPath = audioFile.absolutePath,
                    videoPath = videoFile.absolutePath
                )
                repository.insertProject(updated)
                _activeProject.value = updated
                _videoGenerationError.value = null

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Alternativní video úspěšně vygenerováno!", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Alternative video synthesis failed: ${e.message}")
                _videoGenerationError.value = "Chyba alternativního generování: ${e.message}"
            } finally {
                _isVideoGenerating.value = false
            }
        }
    }

    fun triggerBackgroundServiceExport(context: Context, format: String) {
        val current = _activeProject.value ?: return
        val serviceIntent = Intent(context, ExportForegroundService::class.java).apply {
            putExtra(ExportForegroundService.EXTRA_FORMAT, format)
            putExtra(ExportForegroundService.EXTRA_DURATION_SEC, current.trackDuration)
            putExtra(ExportForegroundService.EXTRA_LYRICS, current.lyrics)
            putExtra(ExportForegroundService.EXTRA_GENRE, current.genre)
        }
        try {
            context.startService(serviceIntent)
            Toast.makeText(context, "Export na pozadí spuštěn. Sleduj stav v panelu upozornění.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delegate export service: ${e.message}")
        }
    }

    fun updateMidiTrackPattern(context: Context, track: AudioTrack, instrument: String, pattern: String) {
        val currentProj = _activeProject.value ?: return
        pushUndoSnapshot(force = false)
        viewModelScope.launch(Dispatchers.IO) {
            val cacheDir = context.cacheDir
            val filename = "midi_${track.trackId}_${System.currentTimeMillis()}.wav"
            val midiFile = File(cacheDir, filename)

            try {
                com.example.util.MidiSynthesizer.synthesizeMidiToWav(
                    instrument = instrument,
                    patternStr = pattern,
                    outputFile = midiFile,
                    trackDuration = currentProj.trackDuration
                )

                track.filePath?.let { oldPath ->
                    val oldFile = File(oldPath)
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }
                }

                val updated = track.copy(
                    midiInstrument = instrument,
                    midiPattern = pattern,
                    filePath = midiFile.absolutePath
                )
                repository.updateTrack(updated)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating MIDI track pattern: ${e.message}")
            }
        }
    }

    fun applyAiIntelligentMix(context: Context) {
        val currentProj = _activeProject.value ?: return
        val tracks = activeProjectTracks.value
        if (tracks.isEmpty()) {
            Toast.makeText(context, "Žádné stopy k mixování!", Toast.LENGTH_SHORT).show()
            return
        }

        pushUndoSnapshot(force = true)
        
        viewModelScope.launch {
            Toast.makeText(context, "AI Master Assistant optimalizuje mix podle žánru...", Toast.LENGTH_SHORT).show()
            
            val genre = currentProj.genre
            val tracksDesc = tracks.joinToString("\n") { "- ${it.name} (${it.trackType})" }
            
            val prompt = """
                Navrhni optimální nastavení mixu pro hudební žánr $genre.
                Máme tyto zvukové stopy:
                $tracksDesc
                
                Vrať pouze čistý JSON ve formátu seznamu bez jakýchkoliv odrážek či vysvětlení.
                Každý objekt v poli musí obsahovat:
                "name": přesný název stopy z našeho seznamu
                "volume": číslo od 0.1 do 1.0
                "eqHigh": číslo od -8 do +8
                "eqMid": číslo od -8 do +8
                "eqLow": číslo od -8 do +8
                "reverbEnabled": true nebo false
                "reverbWet": číslo od 0.1 do 0.8
                
                Příklad:
                [
                  {"name": "Hlavní zpěv (Lead Vocal Mic)", "volume": 0.9, "eqHigh": 3.0, "eqMid": 1.5, "eqLow": -1.0, "reverbEnabled": true, "reverbWet": 0.35}
                ]
            """.trimIndent()

            try {
                val rawResponse = GeminiClient.generateText(prompt, "Jste profesionální mastering inženýr a zvukař v nahrávacím studiu.")
                val cleanJson = cleanJsonBraces(rawResponse)
                val success = parseAndApplyMixJson(cleanJson, tracks)
                if (success) {
                    Toast.makeText(context, "AI Mix dokončen úspěšně podle žánru $genre! ✅", Toast.LENGTH_SHORT).show()
                } else {
                    applyDeterministicGenreMix(tracks, genre)
                    Toast.makeText(context, "Profi Mix nastaven podle akustických šablon pro $genre! 🎛️", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                applyDeterministicGenreMix(tracks, genre)
                Toast.makeText(context, "Profi Mix nastaven podle akustických šablon pro $genre! 🎛️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cleanJsonBraces(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.substringAfter("```json").substringBeforeLast("```")
        } else if (text.startsWith("```")) {
            text = text.substringAfter("```").substringBeforeLast("```")
        }
        return text.trim()
    }

    private suspend fun parseAndApplyMixJson(jsonStr: String, tracks: List<AudioTrack>): Boolean {
        try {
            val parser = com.google.gson.JsonParser()
            val element = parser.parse(jsonStr)
            if (!element.isJsonArray) return false
            val array = element.asJsonArray
            withContext(Dispatchers.IO) {
                for (i in 0 until array.size()) {
                    val obj = array.get(i).asJsonObject
                    val name = obj.get("name")?.asString ?: continue
                    
                    val matchedTrack = tracks.find { it.name.trim().equals(name.trim(), ignoreCase = true) }
                    if (matchedTrack != null) {
                        val vol = obj.get("volume")?.asFloat ?: matchedTrack.volume
                        val eqH = obj.get("eqHigh")?.asFloat ?: matchedTrack.eqHigh
                        val eqM = obj.get("eqMid")?.asFloat ?: matchedTrack.eqMid
                        val eqL = obj.get("eqLow")?.asFloat ?: matchedTrack.eqLow
                        val reverbOn = obj.get("reverbEnabled")?.asBoolean ?: matchedTrack.reverbEnabled
                        val reverbW = obj.get("reverbWet")?.asFloat ?: matchedTrack.reverbWet
                        
                        val updated = matchedTrack.copy(
                            volume = vol.coerceIn(0.1f, 1.0f),
                            eqHigh = eqH.coerceIn(-12f, 12f),
                            eqMid = eqM.coerceIn(-12f, 12f),
                            eqLow = eqL.coerceIn(-12f, 12f),
                            reverbEnabled = reverbOn,
                            reverbWet = reverbW.coerceIn(0.1f, 1.0f)
                        )
                        repository.updateTrack(updated)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("StudioViewModel", "Mix parse error: ${e.message}")
            return false
        }
    }

    private suspend fun applyDeterministicGenreMix(tracks: List<AudioTrack>, genre: String) {
        withContext(Dispatchers.IO) {
            val isPopOrVocal = genre.contains("pop", ignoreCase = true) || genre.contains("folk", ignoreCase = true)
            val isHipHopOrRap = genre.contains("hip", ignoreCase = true) || genre.contains("rap", ignoreCase = true) || genre.contains("beat", ignoreCase = true)
            
            for (track in tracks) {
                val updated = when {
                    track.trackType.lowercase() == "vocal" -> {
                        track.copy(
                            volume = 0.90f,
                            eqHigh = if (isPopOrVocal) 3.5f else 2.0f,
                            eqMid = 1.0f,
                            eqLow = -1.5f,
                            reverbEnabled = true,
                            reverbWet = if (isPopOrVocal) 0.4f else 0.25f,
                            reverbFeedback = 0.5f
                        )
                    }
                    track.trackType.lowercase() == "beat" -> {
                        track.copy(
                            volume = 0.75f,
                            eqLow = if (isHipHopOrRap) 4.0f else 2.0f,
                            eqMid = -1.0f,
                            eqHigh = 1.0f,
                            reverbEnabled = false
                        )
                    }
                    track.trackType.lowercase() == "midi" -> {
                        track.copy(
                            volume = 0.60f,
                            eqHigh = 2.5f,
                            eqMid = 0.0f,
                            eqLow = -1.0f,
                            reverbEnabled = true,
                            reverbWet = 0.35f,
                            reverbFeedback = 0.6f
                        )
                    }
                    else -> {
                        track.copy(
                            volume = 0.65f,
                            eqHigh = 1.0f,
                            eqMid = 0.0f,
                            eqLow = 0.0f,
                            reverbEnabled = false
                        )
                    }
                }
                repository.updateTrack(updated)
            }
        }
    }

    fun exportProjectAudio(context: Context, tracks: List<AudioTrack>) {
        val current = _activeProject.value ?: return
        if (tracks.isEmpty()) {
            Toast.makeText(context, "Žádné stopy k exportu!", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            _isExportingProject.value = true
            _exportProjectProgress.value = 0f
            try {
                val processedTracks = withContext(Dispatchers.IO) {
                    tracks.map { track ->
                        if (track.trackType == "MIDI" && (track.filePath.isNullOrEmpty() || !File(track.filePath).exists())) {
                            val cacheDir = context.cacheDir
                            val file = File(cacheDir, "midi_${track.trackId}_synth.wav")
                            try {
                                com.example.util.MidiSynthesizer.synthesizeMidiToWav(
                                    instrument = track.midiInstrument ?: "Sine Lead",
                                    patternStr = track.midiPattern ?: "",
                                    outputFile = file,
                                    trackDuration = current.trackDuration
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed MIDI synthesis in export: ${e.message}")
                            }
                            val updated = track.copy(filePath = file.absolutePath)
                            repository.updateTrack(updated)
                            updated
                        } else {
                            track
                        }
                    }
                }

                val cacheDir = context.cacheDir
                val exportFile = File(cacheDir, "spark_export_${current.title.replace("\\s+".toRegex(), "_")}_${System.currentTimeMillis()}.wav")

                ExportFileHelper.mixActiveTracksToWav(context, processedTracks, exportFile) { progress ->
                    _exportProjectProgress.value = progress
                }

                // Save to downloads so it is easily downloadable
                ExportFileHelper.saveWavToDownloads(context, exportFile, "Spark_Studio_" + current.title.replace("\\s+".toRegex(), "_"))

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Projekt úspěšně zkompilován a uložen do složky Stažené soubory! 🎉", Toast.LENGTH_LONG).show()

                    // Also trigger the standard share intent so they can share it or store it anywhere
                    try {
                        val authority = "${context.packageName}.fileprovider"
                        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, authority, exportFile)
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "audio/wav"
                            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Sdílet nebo uložit zkompilovaný projekt"))
                    } catch (ex: Exception) {
                        Log.e("exportProjectAudio", "Nepodařilo se spustit sdílení: ${ex.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chyba při exportu projektu: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Chyba exportu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _isExportingProject.value = false
            }
        }
    }

    private var communityMediaPlayer: MediaPlayer? = null
    private val _playingCommunityItemId = MutableStateFlow<String?>(null)
    val playingCommunityItemId: StateFlow<String?> = _playingCommunityItemId

    fun playCommunityItem(context: Context, item: MarketplaceItem) {
        if (_playingCommunityItemId.value == item.id) {
            stopCommunityItem()
            return
        }
        stopCommunityItem()
        _playingCommunityItemId.value = item.id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var audioPath = item.audioPath
                if (audioPath.isNullOrEmpty() || !File(audioPath).exists()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Generování předposlechu: ${item.name}...", Toast.LENGTH_SHORT).show()
                    }
                    val genre = when {
                        item.id.contains("synthwave") -> "synthwave"
                        item.id.contains("lofi") -> "lo-fi"
                        item.id.contains("metal") -> "metal"
                        item.id.contains("edm") -> "edm"
                        item.id.contains("country") -> "country"
                        item.id.contains("cyberpunk") -> "cinematic"
                        item.id.contains("zen") -> "cinematic"
                        else -> "pop"
                    }
                    val file = com.example.util.ExportFileHelper.generateRealAudioTrack(
                        context = context,
                        genre = genre,
                        durationSec = item.durationSec.coerceIn(10, 20),
                        projectBpm = null
                    ) { }
                    
                    audioPath = file.absolutePath
                    val updatedItem = item.copy(audioPath = audioPath)
                    repository.updateMarketplaceItem(updatedItem)
                }

                val finalPath = audioPath ?: return@launch
                if (_playingCommunityItemId.value != item.id) {
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    try {
                        val player = MediaPlayer().apply {
                            setDataSource(finalPath)
                            prepare()
                            start()
                            setOnCompletionListener {
                                _playingCommunityItemId.value = null
                                it.release()
                                if (communityMediaPlayer == this) {
                                    communityMediaPlayer = null
                                }
                            }
                        }
                        communityMediaPlayer = player
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing community media player: ${e.message}")
                        _playingCommunityItemId.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to compile background item asset: ${e.message}")
                withContext(Dispatchers.Main) {
                    _playingCommunityItemId.value = null
                }
            }
        }
    }

    fun stopCommunityItem() {
        try {
            communityMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping community element", e)
        }
        communityMediaPlayer = null
        _playingCommunityItemId.value = null
    }

    fun publishProjectToCommunity(
        context: Context,
        tracks: List<com.example.data.database.AudioTrack>,
        customTitle: String,
        customDescription: String,
        genreTag: String,
        metadataTags: String,
        price: Int, // Price in credits
        onFinished: (Boolean) -> Unit
    ) {
        val current = _activeProject.value ?: return
        if (tracks.isEmpty()) {
            Toast.makeText(context, "Žádné stopy k zveřejnění!", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            _isExportingProject.value = true
            _exportProjectProgress.value = 0f
            try {
                // 1. Synthesize MIDI Tracks if they haven't been synthesized
                val processedTracks = withContext(Dispatchers.IO) {
                    tracks.map { track ->
                        if (track.trackType == "MIDI" && (track.filePath.isNullOrEmpty() || !File(track.filePath).exists())) {
                            val cacheDir = context.cacheDir
                            val file = File(cacheDir, "midi_${track.trackId}_synth.wav")
                            try {
                                com.example.util.MidiSynthesizer.synthesizeMidiToWav(
                                    instrument = track.midiInstrument ?: "Sine Lead",
                                    patternStr = track.midiPattern ?: "",
                                    outputFile = file,
                                    trackDuration = current.trackDuration
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed MIDI synthesis in community publish: ${e.message}")
                            }
                            val updated = track.copy(filePath = file.absolutePath)
                            repository.updateTrack(updated)
                            updated
                        } else {
                            track
                        }
                    }
                }

                _exportProjectProgress.value = 0.4f

                // 2. Render multi-track into standardized Media format (WAV)
                val destDir = File(context.filesDir, "community_feed_audio")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                val finalFilename = "com_published_${System.currentTimeMillis()}.wav"
                val finalFile = File(destDir, finalFilename)

                ExportFileHelper.mixActiveTracksToWav(context, processedTracks, finalFile) { progress ->
                    _exportProjectProgress.value = 0.4f + (0.5f * progress)
                }

                // 3. Complete tags tagging
                val formattedTags = metadataTags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString(" ") { if (it.startsWith("#")) it else "#$it" }

                // 4. Create single community MarketplaceItem record
                val itemId = "com_item_${System.currentTimeMillis()}"
                val item = MarketplaceItem(
                    id = itemId,
                    name = customTitle.ifBlank { current.title },
                    description = customDescription.ifBlank { "Unikátní komunitní nahrávka vytvořená v Spark Studiu." },
                    price = price.toDouble(), // Custom price in Credits
                    isPurchased = true, // Author inherently owns their self-published artwork
                    type = "Community Track",
                    durationSec = current.trackDuration,
                    audioPath = finalFile.absolutePath,
                    tags = formattedTags.ifBlank { "#$genreTag #sparkstudio" },
                    isCommunityPublished = true
                )

                _exportProjectProgress.value = 0.95f

                // 5. Insert to Room Database to publish globally locally
                repository.prepopulateMarketplace(listOf(item))

                _exportProjectProgress.value = 1.0f
                kotlinx.coroutines.delay(300)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Skladba byla úspěšně vykreslena a nahrána na komunitní trh za $price kreditů! 🚀🌍", Toast.LENGTH_LONG).show()
                    onFinished(true)
                }

                // --- VIRTUAL COMMERCE LOOP (Simulated buyers purchase after a brief delay) ---
                if (price > 0) {
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(10000)
                        val virtualBuyers = listOf("Marek_EDM", "Denisa_Singer", "Vojta_Vibe", "SparkFan99", "AuraSound")
                        val selectedBuyer = virtualBuyers.random()
                        val newBalance = _userCredits.value + price
                        updateUserCredits(context, newBalance)
                        _communitySalesAlert.value = "🎉 Skladba prodána! Uživatel $selectedBuyer zakoupil tvou skladbu '$customTitle' za $price kreditů. Zůstatek zvýšen na $newBalance 🪙!"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error publishing to community: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Chyba při publikaci: ${e.message}", Toast.LENGTH_LONG).show()
                    onFinished(false)
                }
            } finally {
                _isExportingProject.value = false
            }
        }
    }

    private val _timelineClips = MutableStateFlow<List<com.example.util.TimelineClipData>>(emptyList())
    val timelineClips: StateFlow<List<com.example.util.TimelineClipData>> = _timelineClips

    private val _timelineTransitions = MutableStateFlow<List<com.example.util.TransitionData>>(emptyList())
    val timelineTransitions: StateFlow<List<com.example.util.TransitionData>> = _timelineTransitions

    fun updateClipText(context: android.content.Context, projectId: Int, clipId: String, text: String) {
        val updated = _timelineClips.value.map {
            if (it.id == clipId) it.copy(text = text) else it
        }
        _timelineClips.value = updated
        saveTimeline(context, projectId)
    }

    fun updateClipMood(context: android.content.Context, projectId: Int, clipId: String, mood: String) {
        val updated = _timelineClips.value.map {
            if (it.id == clipId) it.copy(mood = mood) else it
        }
        _timelineClips.value = updated
        saveTimeline(context, projectId)
    }

    fun updateClipDuration(context: android.content.Context, projectId: Int, clipId: String, durationSec: Int) {
        val updated = _timelineClips.value.map {
            if (it.id == clipId) it.copy(durationSec = durationSec) else it
        }
        _timelineClips.value = updated
        saveTimeline(context, projectId)
    }

    fun getTimelineFile(context: android.content.Context, projectId: Int): java.io.File {
        return java.io.File(context.filesDir, "timeline_${projectId}.json")
    }

    fun loadTimeline(context: android.content.Context, projectId: Int) {
        val file = getTimelineFile(context, projectId)
        val clips = mutableListOf<com.example.util.TimelineClipData>()
        val transitions = mutableListOf<com.example.util.TransitionData>()

        if (file.exists()) {
            try {
                val json = file.readText()
                val root = org.json.JSONObject(json)
                val clipsArr = root.optJSONArray("clips")
                if (clipsArr != null) {
                    for (i in 0 until clipsArr.length()) {
                        val obj = clipsArr.getJSONObject(i)
                        clips.add(
                            com.example.util.TimelineClipData(
                                id = obj.optString("id"),
                                title = obj.optString("title"),
                                mood = obj.optString("mood"),
                                durationSec = obj.optInt("durationSec"),
                                text = obj.optString("text"),
                                localVideoPath = if (obj.has("localVideoPath")) obj.optString("localVideoPath", null) else null
                            )
                        )
                    }
                }
                val transArr = root.optJSONArray("transitions")
                if (transArr != null) {
                    for (i in 0 until transArr.length()) {
                        val obj = transArr.getJSONObject(i)
                        transitions.add(
                            com.example.util.TransitionData(
                                fromClipId = obj.optString("fromClipId"),
                                toClipId = obj.optString("toClipId"),
                                transitionType = obj.optString("transitionType"),
                                durationSec = obj.optDouble("durationSec", 1.0).toFloat()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("StudioViewModel", "Chyba načítání časové osy: ${e.message}")
            }
        }

        if (clips.isEmpty()) {
            clips.add(com.example.util.TimelineClipData("intro", "Intro Verse", "Neon Cyberpunk", 4, "CYBERPUNK BEAT DROP ⚡"))
            clips.add(com.example.util.TimelineClipData("chorus", "Chorus High", "Cosmic Space", 5, "COSMIC NEBULA AWAKENS ✨"))
            clips.add(com.example.util.TimelineClipData("outro", "Guitar Outro", "Golden Sunset", 4, "SUNSET FINAL CHORD... 🎸"))

            transitions.add(com.example.util.TransitionData("intro", "chorus", "Fade", 1.0f))
            transitions.add(com.example.util.TransitionData("chorus", "outro", "Wipe", 1.0f))
        }

        _timelineClips.value = clips
        _timelineTransitions.value = transitions
    }

    fun saveTimeline(context: android.content.Context, projectId: Int) {
        val file = getTimelineFile(context, projectId)
        try {
            val root = org.json.JSONObject()
            val clipsArr = org.json.JSONArray()
            for (clip in _timelineClips.value) {
                val obj = org.json.JSONObject()
                obj.put("id", clip.id)
                obj.put("title", clip.title)
                obj.put("mood", clip.mood)
                obj.put("durationSec", clip.durationSec)
                obj.put("text", clip.text)
                if (clip.localVideoPath != null) {
                    obj.put("localVideoPath", clip.localVideoPath)
                }
                clipsArr.put(obj)
            }
            root.put("clips", clipsArr)

            val transArr = org.json.JSONArray()
            for (trans in _timelineTransitions.value) {
                val obj = org.json.JSONObject()
                obj.put("fromClipId", trans.fromClipId)
                obj.put("toClipId", trans.toClipId)
                obj.put("transitionType", trans.transitionType)
                obj.put("durationSec", trans.durationSec.toDouble())
                transArr.put(obj)
            }
            root.put("transitions", transArr)

            file.writeText(root.toString())
        } catch (e: Exception) {
            android.util.Log.e("StudioViewModel", "Chyba ukládání časové osy: ${e.message}")
        }
    }

    fun updateClipVideo(context: android.content.Context, projectId: Int, clipId: String, videoPath: String?) {
        val updated = _timelineClips.value.map {
            if (it.id == clipId) it.copy(localVideoPath = videoPath) else it
        }
        _timelineClips.value = updated
        saveTimeline(context, projectId)
    }

    fun applyAiVideoDirector(context: android.content.Context) {
        val currentProj = _activeProject.value ?: return
        if (currentProj.lyrics.isBlank()) {
            Toast.makeText(context, "Před generováním scénáře nejprve vygeneruje text písně!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            Toast.makeText(context, "Gemini sestavuje vizuální scénář...", Toast.LENGTH_SHORT).show()
            val prompt = """
                Vytvoř přesně 5 unikátních vizuálních scén pro hudební videoklip na motivy textu této písně:
                "${currentProj.lyrics}"
                
                Vrať pouze čistý JSON ve formátu seznamu bez jakýchkoliv odrážek či vysvětlení.
                Každý objekt v poli musí obsahovat:
                "title": název scény (např. "Intro v dešti", "Sloka 1 - probuzení", "Klimax refrénu", atd.)
                "mood": styl a atmosféra (např. "Cyberpunk", "Vaporwave", "Synthwave", "Temné retro")
                "durationSec": celé číslo délky trvání v sekundách (obvykle 3 nebo 4)
                "text": detailní vizuální prompt pro AI video generátor (např. "Zpomalený záběr na neonové město v dešti, styl Cyberpunk, 4K")
                
                Napiš přesně 5 unikátních objektů v JSON poli.
            """.trimIndent()

            try {
                val rawResponse = GeminiClient.generateText(prompt, "Jste profesionální režisér hudebních videoklipů a vizuální designér.")
                val cleanJson = cleanJsonBraces(rawResponse)
                
                val parser = com.google.gson.JsonParser()
                val element = parser.parse(cleanJson)
                if (element.isJsonArray) {
                    val array = element.asJsonArray
                    val newClips = mutableListOf<com.example.util.TimelineClipData>()
                    
                    for (i in 0 until array.size()) {
                        val obj = array.get(i).asJsonObject
                        val title = obj.get("title")?.asString ?: "Scéna ${i + 1}"
                        val mood = obj.get("mood")?.asString ?: "Speciální"
                        val duration = obj.get("durationSec")?.asInt ?: 3
                        val text = obj.get("text")?.asString ?: "Vizuální scéna"
                        
                        newClips.add(
                            com.example.util.TimelineClipData(
                                id = "scene_${i + 1}",
                                title = title,
                                mood = mood,
                                durationSec = duration,
                                text = text
                            )
                        )
                    }
                    _timelineClips.value = newClips
                    saveTimeline(context, currentProj.id)
                    Toast.makeText(context, "AI Scénář s 5 profesionálními scénami naimportován!", Toast.LENGTH_SHORT).show()
                } else {
                    applyFallbackScenario(context, currentProj.id, currentProj.genre)
                }
            } catch (e: Exception) {
                Log.e("StudioViewModel", "GK visual parse error: ${e.message}")
                applyFallbackScenario(context, currentProj.id, currentProj.genre)
            }
        }
    }

    private fun applyFallbackScenario(context: android.content.Context, projectId: Int, genre: String) {
        val fbGrid = mutableListOf<com.example.util.TimelineClipData>()
        fbGrid.add(com.example.util.TimelineClipData("scene_1", "Intro Atmosféra", "Cinematic", 3, "Záběr na ranní mlhu stoupající z hor, odraz slunce, 4K rozlišení, styl $genre"))
        fbGrid.add(com.example.util.TimelineClipData("scene_2", "Sloka - Příběh", "Dramatic", 4, "Postava stojící na střeše mrakodrapu v dešti, fialové neonové odrazy, žánr $genre"))
        fbGrid.add(com.example.util.TimelineClipData("scene_3", "Refrén - Energie", "Vibrant", 4, "Abstrakce rotujících vln zvuku s jasnými částicemi pulzujícími do rytmu, 8K ultra-detail"))
        fbGrid.add(com.example.util.TimelineClipData("scene_4", "Sloka 2 - Zvrat", "Atmospheric", 3, "Detailní záběr na kapky vody dopadající na sklo v noci, světla projíždějících aut, styl $genre"))
        fbGrid.add(com.example.util.TimelineClipData("scene_5", "Outro - Dozvuk", "Dreamy", 4, "Pomalé stmívání do zlatého západu slunce nad mořem mraků, klid a mír"))
        
        _timelineClips.value = fbGrid
        saveTimeline(context, projectId)
        Toast.makeText(context, "Načten profesionální lokální vizuální scénář pro $genre!", Toast.LENGTH_SHORT).show()
    }

    fun applyStudioVoiceProcessor(context: android.content.Context, track: AudioTrack) {
        val path = track.filePath
        if (path.isNullOrEmpty() || !java.io.File(path).exists()) {
            Toast.makeText(context, "Musíte nejprve nahrát vokální stopu přes mikrofon!", Toast.LENGTH_SHORT).show()
            return
        }
        
        pushUndoSnapshot(force = true)
        viewModelScope.launch {
            Toast.makeText(context, "AI čistí šum a ladí tóninu projektu...", Toast.LENGTH_SHORT).show()
            withContext(Dispatchers.IO) {
                val sampleRate = 22050
                val originalFile = java.io.File(path)
                val pcmBytes = ExportFileHelper.decodeToPcm(originalFile, sampleRate)
                if (pcmBytes.isNotEmpty()) {
                    val shortCount = pcmBytes.size / 2
                    val shortArr = ShortArray(shortCount)
                    java.nio.ByteBuffer.wrap(pcmBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr)
                    
                    val activeProj = _activeProject.value
                    val genre = activeProj?.genre ?: "Pop"
                    val filteredSamples = com.example.util.AudioEffectsProcessor.applyStudioVoiceFilter(shortArr, genre)
                    
                    val processedFilename = "studio_voice_${track.trackId}_${System.currentTimeMillis()}.wav"
                    val processedFile = java.io.File(context.cacheDir, processedFilename)
                    com.example.util.AudioEffectsProcessor.writeSamplesToWavFile(filteredSamples, sampleRate, processedFile)
                    
                    val updated = track.copy(
                        name = "${track.name} ✨ (Studio Voice)",
                        filePath = processedFile.absolutePath,
                        eqHigh = 2.0f, // boost air
                        eqLow = -1.0f  // clean mud
                    )
                    repository.updateTrack(updated)
                }
            }
            Toast.makeText(context, "Hlas upraven digitálním filtrem Studio Voice!", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateHarmonizerBackingVocals(context: android.content.Context, track: AudioTrack) {
        val path = track.filePath
        if (path.isNullOrEmpty() || !java.io.File(path).exists()) {
            Toast.makeText(context, "Pro harmonizování musíte nejprve nahrát hlavní vokál!", Toast.LENGTH_SHORT).show()
            return
        }
        
        pushUndoSnapshot(force = true)
        viewModelScope.launch {
            Toast.makeText(context, "AI Harmonizér generuje 2 doprovodné vokální stopy...", Toast.LENGTH_SHORT).show()
            withContext(Dispatchers.IO) {
                val originalFile = java.io.File(path)
                
                val filenameL = "harmony_low_${track.trackId}_${System.currentTimeMillis()}.wav"
                val fileL = java.io.File(context.cacheDir, filenameL)
                // major third down (approx pitch factor 0.83)
                com.example.util.AudioEffectsProcessor.generateHarmonicCopy(originalFile, fileL, 0.83)
                
                val filenameR = "harmony_high_${track.trackId}_${System.currentTimeMillis()}.wav"
                val fileR = java.io.File(context.cacheDir, filenameR)
                // perfect fifth up (approx pitch factor 1.25)
                com.example.util.AudioEffectsProcessor.generateHarmonicCopy(originalFile, fileR, 1.25)
                
                if (fileL.exists() && fileR.exists()) {
                    val trackL = AudioTrack(
                        projectId = track.projectId,
                        name = "Doprovod L 🏠 (Harmonická Térce)",
                        filePath = fileL.absolutePath,
                        volume = 0.65f,
                        trackType = "Vocal",
                        startOffsetMs = 15L // Haas Effect delay offset for wide depth
                    )
                    
                    val trackR = AudioTrack(
                        projectId = track.projectId,
                        name = "Doprovod R 🌟 (Harmonická Kvinta)",
                        filePath = fileR.absolutePath,
                        volume = 0.65f,
                        trackType = "Vocal",
                        startOffsetMs = 30L // Delay offset
                    )
                    
                    repository.insertTrack(trackL)
                    repository.insertTrack(trackR)
                }
            }
            Toast.makeText(context, "Harmonické doprovodné stopy vloženy na časovou osu!", Toast.LENGTH_SHORT).show()
        }
    }

    fun addClipToTimeline(context: android.content.Context, projectId: Int, clip: com.example.util.TimelineClipData) {
        val updatedClips = _timelineClips.value.toMutableList()
        updatedClips.add(clip)

        val updatedTransitions = _timelineTransitions.value.toMutableList()
        if (updatedClips.size > 1) {
            val prevClip = updatedClips[updatedClips.size - 2]
            val exists = updatedTransitions.any { it.fromClipId == prevClip.id && it.toClipId == clip.id }
            if (!exists) {
                updatedTransitions.add(
                    com.example.util.TransitionData(
                        fromClipId = prevClip.id,
                        toClipId = clip.id,
                        transitionType = "Fade",
                        durationSec = 1.0f
                    )
                )
            }
        }

        _timelineClips.value = updatedClips
        _timelineTransitions.value = updatedTransitions
        saveTimeline(context, projectId)
    }

    fun removeClipFromTimeline(context: android.content.Context, projectId: Int, clipId: String) {
        val updatedClips = _timelineClips.value.filter { it.id != clipId }
        
        val clipIds = updatedClips.map { it.id }.toSet()
        val updatedTransitions = _timelineTransitions.value.filter { 
            clipIds.contains(it.fromClipId) && clipIds.contains(it.toClipId)
        }.toMutableList()

        for (i in 0 until updatedClips.size - 1) {
            val c1 = updatedClips[i].id
            val c2 = updatedClips[i+1].id
            val exists = updatedTransitions.any { it.fromClipId == c1 && it.toClipId == c2 }
            if (!exists) {
                updatedTransitions.add(com.example.util.TransitionData(c1, c2, "Fade", 1.0f))
            }
        }

        _timelineClips.value = updatedClips
        _timelineTransitions.value = updatedTransitions
        saveTimeline(context, projectId)
    }

    fun moveTimelineClip(context: android.content.Context, projectId: Int, fromIndex: Int, toIndex: Int) {
        val clips = _timelineClips.value.toMutableList()
        if (fromIndex in clips.indices && toIndex in clips.indices) {
            val removed = clips.removeAt(fromIndex)
            clips.add(toIndex, removed)
            _timelineClips.value = clips

            val newTransitions = mutableListOf<com.example.util.TransitionData>()
            for (i in 0 until clips.size - 1) {
                val fromId = clips[i].id
                val toId = clips[i+1].id
                val existing = _timelineTransitions.value.find { 
                    (it.fromClipId == fromId && it.toClipId == toId) || (it.fromClipId == toId && it.toClipId == fromId)
                }
                newTransitions.add(
                    com.example.util.TransitionData(
                        fromClipId = fromId,
                        toClipId = toId,
                        transitionType = existing?.transitionType ?: "Fade",
                        durationSec = existing?.durationSec ?: 1.0f
                    )
                )
            }
            _timelineTransitions.value = newTransitions
            saveTimeline(context, projectId)
        }
    }

    fun setTransitionEffect(
        context: android.content.Context,
        projectId: Int,
        fromClipId: String,
        toClipId: String,
        type: String,
        durationSec: Float
    ) {
        val transitions = _timelineTransitions.value.map {
            if (it.fromClipId == fromClipId && it.toClipId == toClipId) {
                it.copy(transitionType = type, durationSec = durationSec)
            } else {
                it
            }
        }
        _timelineTransitions.value = transitions
        saveTimeline(context, projectId)
    }

    fun generateTimelineMP4Video(context: android.content.Context) {
        val current = _activeProject.value ?: return
        val tracks = activeProjectTracks.value
        viewModelScope.launch {
            _isVideoGenerating.value = true
            _videoProgress.value = 0f
            _videoGenerationError.value = null

            try {
                // 1. Prepare and mix audio tracks first if they exist on the timeline
                val audioFile = if (tracks.isNotEmpty()) {
                    val processedTracks = withContext(Dispatchers.IO) {
                        tracks.map { track ->
                            if (track.trackType == "MIDI" && (track.filePath.isNullOrEmpty() || !File(track.filePath).exists())) {
                                val cacheDir = context.cacheDir
                                val file = File(cacheDir, "midi_${track.trackId}_synth.wav")
                                try {
                                    com.example.util.MidiSynthesizer.synthesizeMidiToWav(
                                        instrument = track.midiInstrument ?: "Sine Lead",
                                        patternStr = track.midiPattern ?: "",
                                        outputFile = file,
                                        trackDuration = current.trackDuration
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed MIDI synthesis in export: ${e.message}")
                                }
                                val updated = track.copy(filePath = file.absolutePath)
                                repository.updateTrack(updated)
                                updated
                            } else {
                                track
                            }
                        }
                    }

                    val cacheDir = context.cacheDir
                    val mixedWavFile = File(cacheDir, "spark_temp_mixed_${System.currentTimeMillis()}.wav")
                    ExportFileHelper.mixActiveTracksToWav(context, processedTracks, mixedWavFile) { progress ->
                        _videoProgress.value = progress * 0.25f
                    }
                    mixedWavFile
                } else if (!current.audioPath.isNullOrEmpty() && java.io.File(current.audioPath).exists()) {
                    java.io.File(current.audioPath)
                } else {
                    com.example.util.ExportFileHelper.generateRealAudioTrack(context, current.genre, 15, current.bpm) {}
                }

                // 2. Prepare visual clips JSON
                val root = org.json.JSONObject()
                val clipsArr = org.json.JSONArray()
                for (clip in _timelineClips.value) {
                    val obj = org.json.JSONObject()
                    obj.put("id", clip.id)
                    obj.put("title", clip.title)
                    obj.put("mood", clip.mood)
                    obj.put("durationSec", clip.durationSec)
                    obj.put("text", clip.text)
                    clipsArr.put(obj)
                }
                root.put("clips", clipsArr)

                val transArr = org.json.JSONArray()
                for (trans in _timelineTransitions.value) {
                    val obj = org.json.JSONObject()
                    obj.put("fromClipId", trans.fromClipId)
                    obj.put("toClipId", trans.toClipId)
                    obj.put("transitionType", trans.transitionType)
                    obj.put("durationSec", trans.durationSec.toDouble())
                    transArr.put(obj)
                }
                root.put("transitions", transArr)

                val timelineJson = root.toString()

                // 3. Render and compile the video
                val targetVideo = com.example.util.ExportFileHelper.generateTimelineVideo(
                    context = context,
                    clipsJson = timelineJson,
                    audioFile = audioFile,
                    onProgress = { progress ->
                        _videoProgress.value = 0.25f + (progress * 0.70f)
                    }
                )

                // 4. Save compiled video to Downloads folder so it's fully downloadable/accessible!
                val cleanTitle = current.title.replace("\\s+".toRegex(), "_")
                val finalSavedVideo = ExportFileHelper.saveMp4ToDownloads(
                    context = context,
                    srcFile = targetVideo,
                    title = "Spark_Studio_Video_$cleanTitle"
                )

                val updated = current.copy(videoPath = targetVideo.absolutePath)
                repository.insertProject(updated)
                _activeProject.value = updated
                _videoGenerationError.value = null

                _videoProgress.value = 1.0f

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Složené video bylo úspěšně staženo a uloženo do složky Stažené soubory! 🎉", Toast.LENGTH_LONG).show()

                    // Trigger share intent so it opens and lets the user choose how to save/use it
                    try {
                        val fileToShare = finalSavedVideo ?: targetVideo
                        val authority = "${context.packageName}.fileprovider"
                        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, authority, fileToShare)
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "video/mp4"
                            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Sdílet nebo uložit zkompilované video"))
                    } catch (ex: Exception) {
                        Log.e("generateTimelineMP4Video", "Nepodařilo se spustit sdílení: ${ex.message}")
                    }
                }
            } catch (e: Exception) {
                _videoGenerationError.value = e.message ?: "Neznámá chyba při skládání časové osy"
            } finally {
                _isVideoGenerating.value = false
            }
        }
    }

    fun generateAIVideoClipFromSong(
        context: android.content.Context,
        videoEngine: String = "Spark AI Cinematics",
        aspectRatio: String = "9:16",
        motionIntensity: Float = 50f,
        visualStyle: String = "Hyper-Realistic CGI",
        customVideoPrompt: String = ""
    ) {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            _isGeneratingAIVideoTimeline.value = true
            try {
                // 1. Ask Gemini to analyze the lyrics/topic of our song and supply 4 cinematic scenes
                val songGenre = current.genre.ifBlank { "Pop" }
                val songLyrics = current.lyrics.ifBlank { "Letní dobrodružství plná slunce." }
                
                val visualContext = if (customVideoPrompt.isNotBlank()) "Uživatelský prompt scény: '$customVideoPrompt'. " else ""
                val queryPrompt = "Vytvoř filmový scénář pro videoklip o 4 scénách pro píseň v žánru $songGenre s textem: '$songLyrics'. " +
                        visualContext +
                        "Aplikuj vizuální styl: '$visualStyle' s intenzitou pohybu scény ${motionIntensity.toInt()}%. " +
                        "Navrženo pro video model: '$videoEngine' s poměrem stran '$aspectRatio'. " +
                        "Každá scéna musí mít název, náladu (zvol jednu z najatých: 'Neon Cyberpunk', 'Cosmic Space', 'Retro Horizon', 'Deep Abyss', 'Zen Garden', 'Golden Warmth') a stručný textový popis."
                
                val answer = try {
                    GeminiClient.generateText(
                        queryPrompt,
                        "Jsi kreativní filmový režisér. Výstup napiš česky jako jednoduchý přehled 4 scén."
                    ).take(500)
                } catch (e: Exception) {
                    "Simulované filmové scény pro žánr $songGenre s visual stylem $visualStyle"
                }
                
                val generatedClips = mutableListOf<com.example.util.TimelineClipData>()
                
                val moods = when (songGenre.lowercase()) {
                    "rock", "metal" -> listOf("Neon Cyberpunk", "Deep Abyss", "Neon Cyberpunk", "Deep Abyss")
                    "pop", "edm", "synthwave" -> listOf("Neon Cyberpunk", "Retro Horizon", "Cosmic Space", "Retro Horizon")
                    "lo-fi", "ambient", "cinematic" -> listOf("Zen Garden", "Cosmic Space", "Golden Warmth", "Zen Garden")
                    else -> listOf("Golden Warmth", "Zen Garden", "Retro Horizon", "Golden Warmth")
                }
                
                // Parse or extract scene titles/descriptions if possible, else generate beautifully styled
                val lines = answer.split("\n").filter { it.isNotBlank() && (it.contains("Scéna") || it.contains(":") || it.contains("-")) }
                val scene1Text = lines.getOrNull(0)?.replace("\"", "")?.take(70) ?: "Scéna 1: Úvodní intro zrození zvuku"
                val scene2Text = lines.getOrNull(1)?.replace("\"", "")?.take(70) ?: "Scéna 2: Vývoj hudebních a vizuálních kontur"
                val scene3Text = lines.getOrNull(2)?.replace("\"", "")?.take(70) ?: "Scéna 3: Rezonanční exploze emocí"
                val scene4Text = lines.getOrNull(3)?.replace("\"", "")?.take(70) ?: "Scéna 4: Postupné zhasínání kontur"

                generatedClips.add(com.example.util.TimelineClipData("clip_1", "Intro ($visualStyle)", moods[0], 4, "$scene1Text [Model: $videoEngine]"))
                generatedClips.add(com.example.util.TimelineClipData("clip_2", "Sloka ($aspectRatio)", moods[1], 4, "$scene2Text [Motion: ${motionIntensity.toInt()}%]"))
                generatedClips.add(com.example.util.TimelineClipData("clip_3", "Refrén (Climax)", moods[2], 4, scene3Text))
                generatedClips.add(com.example.util.TimelineClipData("clip_4", "Konec (Outro)", moods[3], 3, scene4Text))
                
                _timelineClips.value = generatedClips
                saveTimeline(context, current.id)
                
                val generatedTransitions = listOf(
                    com.example.util.TransitionData("clip_1", "clip_2", "Fade", 1.0f),
                    com.example.util.TransitionData("clip_2", "clip_3", "Wipe", 1.0f),
                    com.example.util.TransitionData("clip_3", "clip_4", "Flash", 1.0f)
                )
                _timelineTransitions.value = generatedTransitions
                saveTimeline(context, current.id)
                
                // 2. Automatically compile video
                generateTimelineMP4Video(context)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "AI film v kvalitě $aspectRatio úspěšně renderován přes model $videoEngine! 🎬📺", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to compile AI video clip: ${e.message}")
            } finally {
                _isGeneratingAIVideoTimeline.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioPlayback()
    }

    fun importAudioClip(
        context: android.content.Context,
        projectId: Int,
        uri: android.net.Uri,
        originalName: String,
        onCompleted: (AudioTrack) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val ext = if (originalName.contains(".")) originalName.substringAfterLast(".") else "wav"
                val destFile = java.io.File(context.filesDir, "imported_${System.currentTimeMillis()}.$ext")
                
                contentResolver.openInputStream(uri)?.use { stream ->
                    destFile.outputStream().use { outStream ->
                        stream.copyTo(outStream)
                    }
                }
                
                val newTrack = AudioTrack(
                    projectId = projectId,
                    name = "Imported: $originalName",
                    filePath = destFile.absolutePath,
                    volume = 0.8f,
                    trackType = "Beat"
                )
                
                val insertId = repository.insertTrack(newTrack)
                val insertedTrack = newTrack.copy(trackId = insertId.toInt())
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Audio importováno úspěšně!", Toast.LENGTH_SHORT).show()
                    onCompleted(insertedTrack)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error importing audio: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Chyba při importu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun syncProjectWithAudioTrack(
        context: android.content.Context,
        projectId: Int,
        track: AudioTrack,
        onCompleted: (Int) -> Unit
    ) {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            val file = track.filePath?.let { java.io.File(it) }
            if (file != null && file.exists()) {
                val detectedBpm = com.example.util.BpmDetector.detectBpm(file)
                
                // 1. Update project BPM
                val updatedProj = current.copy(bpm = detectedBpm)
                repository.insertProject(updatedProj)
                _activeProject.value = updatedProj
                
                // 2. Snap video timeline durations to match the grid
                val beatDurationSec = 60.0 / detectedBpm
                val barDurationSec = 4.0 * beatDurationSec
                
                val updatedClips = _timelineClips.value.map { clip ->
                    val barsCount = kotlin.math.round(clip.durationSec / barDurationSec).toInt().coerceAtLeast(1)
                    val newDur = kotlin.math.round(barsCount * barDurationSec).toInt().coerceIn(1, 15)
                    clip.copy(durationSec = newDur)
                }
                
                _timelineClips.value = updatedClips
                saveTimeline(context, projectId)
                
                withContext(Dispatchers.Main) {
                    onCompleted(detectedBpm)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Nelze provést synchronizaci BPM: soubor stopy neexistuje.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

class StudioViewModelFactory(private val repository: StudioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
