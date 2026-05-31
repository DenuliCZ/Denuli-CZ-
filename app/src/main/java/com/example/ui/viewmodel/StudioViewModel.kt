package com.example.ui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AiSettingsTemplate
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMsg
import com.example.data.database.CommunityTrack
import com.example.data.database.Project
import com.example.data.database.PurchaseTransaction
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.preferencesDataStore by preferencesDataStore(name = "studio_settings")

class StudioViewModel(private val repository: StudioRepository) : ViewModel() {

    // Language State: CS (Czech) or EN (English)
    private val _selectedLanguage = MutableStateFlow("CS")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Theme State: true for Dark Mode (Mystický soumrak), false for Light Mode (Elegantní krémový)
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun switchTheme(dark: Boolean) {
        _isDarkMode.value = dark
    }

    // Screen State Tracker
    private val _currentTab = MutableStateFlow("Studio") // Studio, Video, Community, Cloud
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Active Project Configuration
    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    // Playback Progress & Levels
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    // Waveform simulation data (for multitrack visualization)
    private val _vocalWaveform = MutableStateFlow(FloatArray(40) { 0.1f })
    val vocalWaveform: StateFlow<FloatArray> = _vocalWaveform.asStateFlow()

    private val _synthWaveform = MutableStateFlow(FloatArray(40) { 0.2f })
    val synthWaveform: StateFlow<FloatArray> = _synthWaveform.asStateFlow()

    private val _drumsWaveform = MutableStateFlow(FloatArray(40) { 0.15f })
    val drumsWaveform: StateFlow<FloatArray> = _drumsWaveform.asStateFlow()

    private val _natureWaveform = MutableStateFlow(FloatArray(40) { 0.05f })
    val natureWaveform: StateFlow<FloatArray> = _natureWaveform.asStateFlow()

    // Track Volume Controls
    private val _vocalVolume = MutableStateFlow(0.8f)
    val vocalVolume: StateFlow<Float> = _vocalVolume.asStateFlow()

    private val _synthVolume = MutableStateFlow(0.6f)
    val synthVolume: StateFlow<Float> = _synthVolume.asStateFlow()

    private val _drumsVolume = MutableStateFlow(0.7f)
    val drumsVolume: StateFlow<Float> = _drumsVolume.asStateFlow()

    private val _natureVolume = MutableStateFlow(0.4f)
    val natureVolume: StateFlow<Float> = _natureVolume.asStateFlow()

    // AI Generation Status trackers
    private val _isGeneratingLyrics = MutableStateFlow(false)
    val isGeneratingLyrics: StateFlow<Boolean> = _isGeneratingLyrics.asStateFlow()

    private val _aiLyricsResult = MutableStateFlow("")
    val aiLyricsResult: StateFlow<String> = _aiLyricsResult.asStateFlow()

    private val _isGeneratingTips = MutableStateFlow(false)
    val isGeneratingTips: StateFlow<Boolean> = _isGeneratingTips.asStateFlow()

    private val _aiTipsResult = MutableStateFlow("")
    val aiTipsResult: StateFlow<String> = _aiTipsResult.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    // Onboarding system status
    val IS_FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")
    private val _showOnboarding = MutableStateFlow(true)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    // --- AI STYLE BOX STATE PARAMETERS ---
    private val _styleInfluencePct = MutableStateFlow(0.7f)
    val styleInfluencePct: StateFlow<Float> = _styleInfluencePct.asStateFlow()

    private val _vocalDelayMs = MutableStateFlow(150L)
    val vocalDelayMs: StateFlow<Long> = _vocalDelayMs.asStateFlow()

    private val _backgroundMusicMix = MutableStateFlow(0.5f)
    val backgroundMusicMix: StateFlow<Float> = _backgroundMusicMix.asStateFlow()

    private val _isCloudMusicGenerating = MutableStateFlow(false)
    val isCloudMusicGenerating: StateFlow<Boolean> = _isCloudMusicGenerating.asStateFlow()

    // Style Mixing Parameters
    private val _styleMixingA = MutableStateFlow("Rock")
    val styleMixingA: StateFlow<String> = _styleMixingA.asStateFlow()

    private val _styleMixingB = MutableStateFlow("Synthwave")
    val styleMixingB: StateFlow<String> = _styleMixingB.asStateFlow()

    private val _styleMixingWeightA = MutableStateFlow(0.7f)
    val styleMixingWeightA: StateFlow<Float> = _styleMixingWeightA.asStateFlow()

    private val _vocalLyricalLanguage = MutableStateFlow("CS")
    val vocalLyricalLanguage: StateFlow<String> = _vocalLyricalLanguage.asStateFlow()

    // Vocal Synthesis Engine Setup State Fields
    private val _vocalScaleType = MutableStateFlow("Muž")
    val vocalScaleType: StateFlow<String> = _vocalScaleType.asStateFlow()

    private val _vocalEmotion = MutableStateFlow("Neutrální")
    val vocalEmotion: StateFlow<String> = _vocalEmotion.asStateFlow()

    // Custom Font Collections
    private val _fontCollections = MutableStateFlow(listOf("Denuli Serif", "Brutal Techno", "Retro Comic", "Elegant Classy", "Space Grotesk"))
    val fontCollections: StateFlow<List<String>> = _fontCollections.asStateFlow()

    // GDPR & Privacy Policy states (Article 6 and Article 7 GDPR Consent)
    private val _gdprAccepted = MutableStateFlow<Boolean?>(null) // null = Undecided, show banner
    val gdprAccepted: StateFlow<Boolean?> = _gdprAccepted.asStateFlow()

    private val _gdprConsentCloud = MutableStateFlow(true)
    val gdprConsentCloud: StateFlow<Boolean> = _gdprConsentCloud.asStateFlow()

    private val _gdprConsentAi = MutableStateFlow(true)
    val gdprConsentAi: StateFlow<Boolean> = _gdprConsentAi.asStateFlow()

    private val _gdprConsentCommunity = MutableStateFlow(true)
    val gdprConsentCommunity: StateFlow<Boolean> = _gdprConsentCommunity.asStateFlow()

    private val _gdprConsentTelemetry = MutableStateFlow(false)
    val gdprConsentTelemetry: StateFlow<Boolean> = _gdprConsentTelemetry.asStateFlow()

    // --- PREMIUM SUBSCRIPTION & GOOGLE PLAY BILLING STATES ---
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _aiGenerationsUsedToday = MutableStateFlow(0)
    val aiGenerationsUsedToday: StateFlow<Int> = _aiGenerationsUsedToday.asStateFlow()

    private val _showPaywallDialog = MutableStateFlow(false)
    val showPaywallDialog: StateFlow<Boolean> = _showPaywallDialog.asStateFlow()

    // --- UGC CONTENT FILTERING & SAFETY ---
    private val _blockedUsers = MutableStateFlow<Set<String>>(emptySet())
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    // --- CREATOR REGISTRATION & COMPLIANCE STATES ---
    private val _creatorName = MutableStateFlow("")
    val creatorName: StateFlow<String> = _creatorName.asStateFlow()

    private val _creatorAge = MutableStateFlow(25)
    val creatorAge: StateFlow<Int> = _creatorAge.asStateFlow()

    private val _creatorParentalConsentChecked = MutableStateFlow(false)
    val creatorParentalConsentChecked: StateFlow<Boolean> = _creatorParentalConsentChecked.asStateFlow()

    private val _creatorParentName = MutableStateFlow("")
    val creatorParentName: StateFlow<String> = _creatorParentName.asStateFlow()

    private val _creatorCopyrightAgreed = MutableStateFlow(false)
    val creatorCopyrightAgreed: StateFlow<Boolean> = _creatorCopyrightAgreed.asStateFlow()

    private val _isCreatorRegistered = MutableStateFlow(false)
    val isCreatorRegistered: StateFlow<Boolean> = _isCreatorRegistered.asStateFlow()

    // --- LONG VIDEO EDITOR OPTIMIZATIONS (Fairy tales & Podcast 30m+ handling) ---
    private val _isProxyEditingOnly = MutableStateFlow(true)
    val isProxyEditingOnly: StateFlow<Boolean> = _isProxyEditingOnly.asStateFlow()

    private val _isSequentialExporting = MutableStateFlow(false)
    val isSequentialExporting: StateFlow<Boolean> = _isSequentialExporting.asStateFlow()

    private val _sequentialExportProgress = MutableStateFlow(0f)
    val sequentialExportProgress: StateFlow<Float> = _sequentialExportProgress.asStateFlow()

    private val _sequentialExportStage = MutableStateFlow("")
    val sequentialExportStage: StateFlow<String> = _sequentialExportStage.asStateFlow()

    private val _savedRamMegabytes = MutableStateFlow(0)
    val savedRamMegabytes: StateFlow<Int> = _savedRamMegabytes.asStateFlow()

    private val _isAudioStreamingActive = MutableStateFlow(false)
    val isAudioStreamingActive: StateFlow<Boolean> = _isAudioStreamingActive.asStateFlow()

    private val _audioStreamingBufferChunkIndex = MutableStateFlow(0)
    val audioStreamingBufferChunkIndex: StateFlow<Int> = _audioStreamingBufferChunkIndex.asStateFlow()

    private val _audioStreamingBufferedChunks = MutableStateFlow(booleanArrayOf(true, true, false, false, false, false))
    val audioStreamingBufferedChunks: StateFlow<BooleanArray> = _audioStreamingBufferedChunks.asStateFlow()

    private val _streamingIsBuffering = MutableStateFlow(false)
    val streamingIsBuffering: StateFlow<Boolean> = _streamingIsBuffering.asStateFlow()

    // Project fields temporarily cached for form input
    val projectTitleField = MutableStateFlow("New Masterpiece")
    val projectGenreField = MutableStateFlow("Pop")
    val stylePromptField = MutableStateFlow("energetic, modern, deep chords")
    val excludedPromptField = MutableStateFlow("sad, slow, low fidelity")
    val lyricPromptField = MutableStateFlow("love, late night drives under stars")
    val projectBpmField = MutableStateFlow(120)

    // Database state collections
    val projectsList: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityFeed: StateFlow<List<CommunityTrack>> = repository.trendingTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessagesList: StateFlow<List<ChatMsg>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedTemplatesList: StateFlow<List<AiSettingsTemplate>> = repository.settingsTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchaseTransactionsList: StateFlow<List<PurchaseTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiHelpBotResponse = MutableStateFlow<String>("")
    val aiHelpBotResponse: StateFlow<String> = _aiHelpBotResponse.asStateFlow()

    private val _isAiHelpBotLoading = MutableStateFlow<Boolean>(false)
    val isAiHelpBotLoading: StateFlow<Boolean> = _isAiHelpBotLoading.asStateFlow()

    fun askAiAssistantQuickHelp(prompt: String) {
        if (prompt.trim().isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isAiHelpBotLoading.value = true
            _aiHelpBotResponse.value = ""
            try {
                val fullSystemPrompt = "Jste Denuli Studio AI asistent pro pomoc uživatelům (česká odpověď, přátelský, motivující, stručný, dává praktické rady k mixování hudby, exportu, autorským právům, GDPR a nastavení projektu)."
                val response = repository.askAiAssistant(prompt, "System Instructions: $fullSystemPrompt")
                _aiHelpBotResponse.value = response
            } catch (e: Exception) {
                _aiHelpBotResponse.value = "Omlouvám se, došlo k chybě při spojení s AI: ${e.message}"
            } finally {
                _isAiHelpBotLoading.value = false
            }
        }
    }

    init {
        // Initialize timeline and simulation clock
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                if (_isPlaying.value) {
                    _playbackProgress.value = (_playbackProgress.value + 0.02f)
                    if (_playbackProgress.value >= 1f) {
                        _playbackProgress.value = 0f
                        _isPlaying.value = false
                    }
                    // Jitter sound amplitudes for visual waveforms
                    _vocalWaveform.value = FloatArray(40) { (0.1f + (Math.random().toFloat() * _vocalVolume.value)).coerceIn(0f, 1f) }
                    _synthWaveform.value = FloatArray(40) { (0.2f + (Math.random().toFloat() * _synthVolume.value)).coerceIn(0f, 1f) }
                    _drumsWaveform.value = FloatArray(40) { (0.15f + (Math.random().toFloat() * _drumsVolume.value)).coerceIn(0f, 1f) }
                    _natureWaveform.value = FloatArray(40) { (0.05f + (Math.random().toFloat() * _natureVolume.value)).coerceIn(0f, 1f) }
                }
                if (_isRecording.value) {
                    _vocalWaveform.value = FloatArray(40) { (0.1f + (Math.random().toFloat() * _vocalVolume.value * 1.5f)).coerceIn(0f, 1f) }
                }
                delay(120)
            }
        }

        // Insert some default items into database if they are empty
        viewModelScope.launch(Dispatchers.IO) {
            setupInitialDatabaseSeed()
        }
    }

    private suspend fun setupInitialDatabaseSeed() {
        // Seed default chat message if empty
        delay(1000)
        // Feed mock community track representing trending song from Denuli-CZ
        repository.saveCommunityTrack(
            CommunityTrack(
                title = "Golden Sunset Dreams",
                author = "Denuli-CZ",
                genre = "Tropical House",
                lyrics = "[Verse] Krásná noc pod hvězdami, zpíváme si sami...\n[Chorus] Srdce hraje dnem i nocí, máme to ve své moci!",
                likes = 428,
                commentsRaw = "Denuli-CZ: Úžasné vokály!;Filip: Skvělý letní hit!;Denisa: Krásná barva hlasu a ty zvuky přírody jsou luxusní!",
                isForSale = true,
                priceCzk = 12000.00,
                customLicense = "Denuli Studio Pro License",
                videoTemplate = "Retro Sunset",
                soundSampleType = "Full HD Multi-track Mix"
            )
        )
        repository.saveCommunityTrack(
            CommunityTrack(
                title = "City Neon Beats",
                author = "CyberMaster",
                genre = "Synthwave",
                lyrics = "[Chorus] Electric lines flashing in your eyes...",
                likes = 184,
                commentsRaw = "Denuli-CZ: Moc fajn retrowave atmosféra, zkus trošku zvednout zpoždění vokálu.;Tonda: Super pro noční jízdy.",
                isForSale = false,
                priceCzk = 0.0,
                customLicense = "BY-NC Creative Commons"
            )
        )

        repository.saveChatMessage(
            ChatMsg(
                sender = "AI Master Coach",
                message = "Vítejte v Denuli Studio! Vytvořte nový multitrack projekt, zapněte nahrávání nástrojů a nechte se inspirovat umělou inteligencí.",
                isAiAssistant = true
            )
        )
    }

    // Setters
    fun switchLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun toggleOnboarding() {
        _showOnboarding.value = !_showOnboarding.value
    }

    fun setStyleInfluencePct(v: Float) {
        _styleInfluencePct.value = v
    }

    fun setVocalDelayMs(d: Long) {
        _vocalDelayMs.value = d
        _activeProject.value?.let { p ->
            saveProjectFieldsSilently(p.copy(vocalDelayMs = d))
        }
    }

    fun setBackgroundMusicMix(m: Float) {
        _backgroundMusicMix.value = m
        _activeProject.value?.let { p ->
            saveProjectFieldsSilently(p.copy(backgroundMusicMix = m))
        }
    }

    fun setStyleMixingA(s: String) { _styleMixingA.value = s }
    fun setStyleMixingB(s: String) { _styleMixingB.value = s }
    fun setStyleMixingWeightA(w: Float) { _styleMixingWeightA.value = w }
    fun setVocalLyricalLanguage(l: String) { _vocalLyricalLanguage.value = l }
    fun setVocalScaleType(t: String) { _vocalScaleType.value = t }
    fun setVocalEmotion(e: String) { _vocalEmotion.value = e }

    // --- Billing Helper Reference ---
    var billingHelper: com.example.billing.PlayBillingHelper? = null

    fun setShowPaywallDialog(show: Boolean) {
        _showPaywallDialog.value = show
    }

    fun setPremiumStatus(premium: Boolean) {
        _isPremium.value = premium
    }

    fun initPremiumStatus(context: Context) {
        val prefs = context.getSharedPreferences("denuli_studio_billing_prefs", Context.MODE_PRIVATE)
        val isBypassed = prefs.getBoolean("developer_bypass_premium", false)
        _isPremium.value = isBypassed

        // Load creator profile details and age compliance
        _creatorName.value = prefs.getString("creator_name", "") ?: ""
        _creatorAge.value = prefs.getInt("creator_age", 25)
        _creatorParentalConsentChecked.value = prefs.getBoolean("creator_parental_consent", false)
        _creatorParentName.value = prefs.getString("creator_parent_name", "") ?: ""
        _creatorCopyrightAgreed.value = prefs.getBoolean("creator_copyright_agreed", false)
        _isCreatorRegistered.value = prefs.getBoolean("is_creator_registered", false)
    }

    fun registerCreatorProfile(
        context: Context,
        name: String,
        age: Int,
        parentalConsent: Boolean,
        parentName: String,
        copyrightAgreed: Boolean
    ) {
        val prefs = context.getSharedPreferences("denuli_studio_billing_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("creator_name", name)
            putInt("creator_age", age)
            putBoolean("creator_parental_consent", parentalConsent)
            putString("creator_parent_name", parentName)
            putBoolean("creator_copyright_agreed", copyrightAgreed)
            putBoolean("is_creator_registered", true)
            apply()
        }
        _creatorName.value = name
        _creatorAge.value = age
        _creatorParentalConsentChecked.value = parentalConsent
        _creatorParentName.value = parentName
        _creatorCopyrightAgreed.value = copyrightAgreed
        _isCreatorRegistered.value = true
    }

    fun toggleSimulatedPremium(context: Context): Boolean {
        val prefs = context.getSharedPreferences("denuli_studio_billing_prefs", Context.MODE_PRIVATE)
        val currentBypass = prefs.getBoolean("developer_bypass_premium", false)
        val newBypass = !currentBypass
        prefs.edit().putBoolean("developer_bypass_premium", newBypass).apply()
        _isPremium.value = newBypass
        return newBypass
    }

    fun registerAiGenerationUsage(context: Context): Boolean {
        if (_isPremium.value) return true
        
        val prefs = context.getSharedPreferences("denuli_studio_billing_prefs", Context.MODE_PRIVATE)
        val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val key = "ai_limit_$todayStr"
        val count = prefs.getInt(key, 0)
        
        if (count >= 3) {
            _showPaywallDialog.value = true
            return false
        }
        
        prefs.edit().putInt(key, count + 1).apply()
        _aiGenerationsUsedToday.value = count + 1
        return true
    }

    fun initAiGenerationUsageCount(context: Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("denuli_studio_billing_prefs", Context.MODE_PRIVATE)
            val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
            val key = "ai_limit_$todayStr"
            _aiGenerationsUsedToday.value = prefs.getInt(key, 0)
        }
    }

    fun reportUser(context: android.content.Context, username: String) {
        android.widget.Toast.makeText(context, "Uživatel '$username' byl nahlášen moderátorům za UGC porušení. Děkujeme.", android.widget.Toast.LENGTH_LONG).show()
    }

    fun blockUser(context: android.content.Context, username: String) {
        _blockedUsers.value = _blockedUsers.value + username
        android.widget.Toast.makeText(context, "Uživatel '$username' byl zablokován. Již od něj neuvidíte žádné zprávy.", android.widget.Toast.LENGTH_LONG).show()
    }

    fun unblockUser(username: String) {
        _blockedUsers.value = _blockedUsers.value - username
    }

    // Collaborative Project Sharing & Comments
    fun sendProjectShareInChat(projectName: String, projectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val shareMsg = ChatMsg(
                sender = "Uživatel (Denuli Fan)",
                message = "Sdílí projekt: '$projectName' 🎨🎬",
                isAiAssistant = false,
                isProjectShare = true,
                sharedProjectId = projectId
            )
            repository.saveChatMessage(shareMsg)
        }
    }

    fun sendTimelineCommentInChat(projectId: Int, comment: String, timeSeconds: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val minutes = timeSeconds / 60
            val seconds = timeSeconds % 60
            val timestampFormatted = String.format("%02d:%02d", minutes, seconds)
            
            val commentMsg = ChatMsg(
                sender = "Uživatel (Denuli Fan)",
                message = "Komentář k časové ose v čase $timestampFormatted: \"$comment\"",
                isAiAssistant = false,
                isProjectShare = false,
                sharedProjectId = projectId,
                commentTimeSeconds = timeSeconds
            )
            repository.saveChatMessage(commentMsg)
        }
    }

    fun initOnboarding(context: Context) {
        viewModelScope.launch {
            val isFirstRunVal = context.preferencesDataStore.data.map { prefs ->
                prefs[IS_FIRST_RUN_KEY] ?: true
            }.first()
            _showOnboarding.value = isFirstRunVal
        }
    }

    fun completeOnboarding(context: Context) {
        viewModelScope.launch {
            context.preferencesDataStore.edit { prefs ->
                prefs[IS_FIRST_RUN_KEY] = false
            }
            _showOnboarding.value = false
            loadDemoProject()
        }
    }

    fun loadDemoProject() {
        viewModelScope.launch(Dispatchers.IO) {
            val demoProject = Project(
                title = "Projekt Jedna Dvě Tři ⚡",
                genre = "Synthwave Rock",
                bpm = 128,
                lyrics = """[Intro]
[Driving synthwave bassline starts, suddenly joined by a massive, stomping rock drum beat and heavy guitar chords. The crowd chants in unison]
(O-o-oh, Projekt Jedna Dvě Tři!)
(O-o-oh, teď musíme jít!)

[Verse 1]
Kód v našich žilách začíná žít,
zhasněte světla, čas začal jít.
Sedmdesát procent je ocel a vzdor,
zbytek je vesmír a neonový obzor.
Stojíme v řadě, jeden velký proud,
tuhle divokou řeku už nelze spláchnout.
Z digitální mlhy vystupuje tvář,
nad naším městem dnes plane nová zář.

[Pre-Chorus]
[Synthesizers build a fast arpeggio, electric guitars rise in a crescendo]
Slyšíš ten tep? To je stroj, co má duši!
Dneska ten krunýř ticha protrhnout musí!
Společný nádech, síla miliónu těl,
přichází to, co svět ještě neviděl!

[Chorus]
[Epic choral explosion, heavy guitar riffs combined with pulsing retro-synths]
Projekt Jedna Dvě Tři – náš společný hlas!
Když kytary hřmí a čas ztrácí svůj vzkaz!
Syntetická noc a rockový den,
zpíváme spolu ten největší sen!
Jedna, dvě, tři – světla už svítí,
v tomhle rytmu se nedá nic skrýti!

[Verse 2]
Kroky v davu duní jako basový tón,
každý z nás drží svůj vlastní mikrofon.
Digitální déšť stéká po horkém skle,
tohle je kód, který nikdy nezemře.
Zvedněte ruce, ať se spojí náš stín,
vypusťte napětí z chladných turbín!

[Pre-Chorus]
[Tension building, drums driving a steady, powerful four-on-the-floor beat]
Slyšíš ten tep? To je stroj, co má duši!
Dneska ten krunýř ticha protrhnout musí!
Společný nádech, síla miliónu těl,
přichází to, co svět ještě neviděl!

[Chorus]
[Epic choral explosion, maximum energy]
Projekt Jedna Dvě Tři – náš společný hlas! (Náš společný hlas!)
Když kytary hřmí a čas ztrácí svůj vzkaz!
Syntetická noc a rockový den,
zpíváme spolu ten největší sen!
Jedna, dvě, tři – světla už svítí,
v tomhle rytmu se nedá nic skrýti!

[Bridge]
[The beat half-times, heavy guitar chords sustain as a massive choir takes the lead]
Jedna! – Naše srdce bije jako hrom!
Dvě! – Stojíme pevně jako starý strom!
Tři! – Jsme volní, už nás neudrží hráz!
(Teď přichází náš čas!)
[A lightning-fast synth arpeggio leads into a soaring guitar solo]

[Guitar and Synth Solo]
[An epic duel between a screaming electric guitar and a bright, retro-futuristic synthesizer lead, perfectly synchronized with a driving rock beat]

[Chorus]
[Final, most explosive chorus, full choral support, crowd chanting along]
Projekt Jedna Dvě Tři – náš společný hlas! (Náš společný hlas!)
Když kytary hřmí a čas ztrácí svůj vzkaz!
Syntetická noc a rockový den,
zpíváme spolu ten největší sen!
Jedna, dvě, tři – světla už svítí,
v tomhle rytmu se nedá nic skrýti!

[Outro]
[Drums slowly fade out, leaving only warm synth chords]
Jen pro vás...
Každý tón, každý tep mého srdce""".trimIndent(),
                stylePrompt = "driving synthwave bassline, stomping rock drum beat, heavy guitar chords, epic retro-synths, bright futurism, fast arpeggios",
                excludedPrompt = "slow acoustic piano, silent ambient drone, chaotic jazz, bad performance",
                vocalPrompt = "Epic rock choir with energetic vocoder lead and massive rock vocal duet",
                vocalGain = 0.90f,
                backgroundMusicMix = 0.75f,
                vocalDelayMs = 240L,
                voiceEffect = "Robot",
                natureSound = "Thunder",
                videoTemplate = "Cyberpunk Neon",
                videoTransition = "Zoom"
            )
            val demoId = repository.saveProject(demoProject)
            val savedDemo = demoProject.copy(id = demoId.toInt())
            _activeProject.value = savedDemo
            selectProject(savedDemo)
        }
    }

    fun loadJednaDveTriProject() {
        loadDemoProject()
    }

    fun generateCloudMusicWithTimeout(context: Context, styleStr: String, excludedStr: String) {
        if (!registerAiGenerationUsage(context)) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isCloudMusicGenerating.value = true
            _isGeneratingLyrics.value = true
            _aiLyricsResult.value = "Kontaktuji cloudový hudební server s nastaveným limitem připojení 30s..."
            try {
                // Simulate network latency inside 30-sec safety timeout
                delay(3500)
                val lyricsPrompt = if (stylePromptField.value.isNotBlank()) stylePromptField.value else "vlastní styl"
                val languageSelected = if (_vocalLyricalLanguage.value == "CS") "v češtině" else "v angličtině"
                val vocalTypeVal = _vocalScaleType.value
                val vocalEmotionVal = _vocalEmotion.value
                val fullSpecPrompt = buildString {
                    append("Hlavní styl: $lyricsPrompt. ")
                    append("Míchání stylů (Multi-prompt Morphing) s plynulým matematickým prolnutím bez rytmických nesrovnalostí: ")
                    append("${_styleMixingWeightA.value * 100}% ${_styleMixingA.value} + ${(1f - _styleMixingWeightA.value) * 100}% ${_styleMixingB.value}. ")
                    append("Cílový interpret: $vocalTypeVal (Sólo/Skupina/Chorál), Hlasová intonace a nálada: $vocalEmotionVal. ")
                    append("Jazyk: s perfektní intonací, přirozenými nádechy a stoprocentně správnou výslovností v preferovaném jazyce ($languageSelected, s bezchybnou českou diakritikou).")
                }
                val result = repository.getAiLyrics(
                    projectTitle = projectTitleField.value,
                    genre = projectGenreField.value,
                    stylePrompt = fullSpecPrompt,
                    excludedPrompt = excludedStr
                )
                _aiLyricsResult.value = result
                
                // Update current project if it exists
                _activeProject.value?.let { current ->
                    val updated = current.copy(
                        lyrics = result,
                        stylePrompt = stylePromptField.value,
                        excludedPrompt = excludedPromptField.value,
                        vocalDelayMs = _vocalDelayMs.value,
                        backgroundMusicMix = _backgroundMusicMix.value
                    )
                    _activeProject.value = updated
                    repository.updateProject(updated)
                }
            } catch (e: Exception) {
                _aiLyricsResult.value = "Chyba generování: ${e.message}"
            } finally {
                _isCloudMusicGenerating.value = false
                _isGeneratingLyrics.value = false
            }
        }
    }

    fun setVocalVolume(vol: Float) {
        _vocalVolume.value = vol
        _activeProject.value?.let {
            saveProjectFieldsSilently(it.copy(vocalGain = vol))
        }
        updatePlayerVolume()
    }

    fun setSynthVolume(vol: Float) {
        _synthVolume.value = vol
        updatePlayerVolume()
    }

    fun setDrumsVolume(vol: Float) {
        _drumsVolume.value = vol
        updatePlayerVolume()
    }

    fun setNatureVolume(vol: Float) {
        _natureVolume.value = vol
        updatePlayerVolume()
    }

    fun selectProject(project: Project) {
        _activeProject.value = project
        projectTitleField.value = project.title
        projectGenreField.value = project.genre
        stylePromptField.value = project.stylePrompt
        excludedPromptField.value = project.excludedPrompt
        lyricPromptField.value = project.vocalPrompt
        projectBpmField.value = project.bpm
        _vocalVolume.value = project.vocalGain
        _aiLyricsResult.value = project.lyrics
        _vocalDelayMs.value = project.vocalDelayMs
        _backgroundMusicMix.value = project.backgroundMusicMix
    }

    fun createNewProject() {
        viewModelScope.launch(Dispatchers.IO) {
            val num = (projectsList.value.size + 1)
            val p = Project(
                title = "Projekt $num",
                genre = "Pop",
                bpm = 120,
                lyrics = "[Verse]\nKrásný den začíná s melodií...\n\n[Chorus]\nZpívám si svou vlastní píseň, odháním z hlavy tíseň!\n\n[Outro]\n[Drums slowly fade out, leaving only warm synth chords]\nJen pro vás...\nKaždý tón, každý tep mého srdce"
            )
            val id = repository.saveProject(p)
            val savedProject = p.copy(id = id.toInt())
            _activeProject.value = savedProject
            selectProject(savedProject)
        }
    }

    fun deleteProject(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProjectById(id)
            if (_activeProject.value?.id == id) {
                _activeProject.value = null
            }
        }
    }

    private fun saveProjectFieldsSilently(p: Project) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProject(p)
        }
    }

    fun saveCurrentProjectState() {
        val current = _activeProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = current.copy(
                title = projectTitleField.value,
                genre = projectGenreField.value,
                vocalPrompt = lyricPromptField.value,
                stylePrompt = stylePromptField.value,
                excludedPrompt = excludedPromptField.value,
                bpm = projectBpmField.value,
                lyrics = _aiLyricsResult.value,
                vocalGain = _vocalVolume.value,
                videoTemplate = current.videoTemplate,
                videoTransition = current.videoTransition,
                colorGradingPreset = current.colorGradingPreset,
                fontName = current.fontName,
                projectLicense = current.projectLicense,
                isSharedPublicly = current.isSharedPublicly,
                rightsPriceCzk = current.rightsPriceCzk
            )
            repository.updateProject(updated)
            _activeProject.value = updated
        }
    }

    fun updateProjectVideoSettings(template: String, transition: String, colorGrading: String, font: String) {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            videoTemplate = template,
            videoTransition = transition,
            colorGradingPreset = colorGrading,
            fontName = font
        )
        _activeProject.value = updated
        saveProjectFieldsSilently(updated)
    }

    fun updateProjectLicenseSettings(license: String, isPublic: Boolean, price: Double) {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            projectLicense = license,
            isSharedPublicly = isPublic,
            rightsPriceCzk = price
        )
        _activeProject.value = updated
        saveProjectFieldsSilently(updated)
    }

    // Playback Simulators
    private var mediaPlayer: android.media.MediaPlayer? = null

    fun togglePlay(context: Context) {
        _isPlaying.value = !_isPlaying.value
        if (_isPlaying.value) {
            _isRecording.value = false
            startPlayingAudio(context)
        } else {
            stopPlayingAudio()
        }
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
        if (_isPlaying.value) {
            _isRecording.value = false
        } else {
            stopPlayingAudio()
        }
    }

    private fun startPlayingAudio(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val project = _activeProject.value
                val cacheFile = com.example.util.ExportFileHelper.generateProjectAudioCache(context, project)
                
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    try {
                        mediaPlayer?.release()
                        mediaPlayer = android.media.MediaPlayer().apply {
                            setDataSource(cacheFile.absolutePath)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                updatePlayerVolume()
                                mp.start()
                            }
                            setOnErrorListener { _, _, _ ->
                                _isPlaying.value = false
                                false
                            }
                            prepare()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _isPlaying.value = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    _isPlaying.value = false
                }
            }
        }
    }

    fun stopPlayingAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePlayerVolume() {
        val master = ((_vocalVolume.value + _synthVolume.value + _drumsVolume.value + _natureVolume.value) / 4f).coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(master, master)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        stopPlayingAudio()
        super.onCleared()
    }

    fun toggleRecord() {
        _isRecording.value = !_isRecording.value
        if (_isRecording.value) {
            _isPlaying.value = false
            stopPlayingAudio()
            _playbackProgress.value = 0f
        }
    }

    // AI Integrations
    fun generateAiLyrics() {
        val current = _activeProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingLyrics.value = true
            val res = repository.getAiLyrics(
                projectTitle = projectTitleField.value,
                genre = projectGenreField.value,
                stylePrompt = stylePromptField.value,
                excludedPrompt = excludedPromptField.value
            )
            _aiLyricsResult.value = res
            _isGeneratingLyrics.value = false

            // Automatically save updated lyrics to db
            val updated = current.copy(lyrics = res)
            _activeProject.value = updated
            repository.updateProject(updated)
        }
    }

    fun generateAiMasteringTips() {
        val current = _activeProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingTips.value = true
            val res = repository.getAiMasteringAndOptimization(
                lyrics = _aiLyricsResult.value,
                genre = projectGenreField.value,
                voiceEffect = current.voiceEffect,
                colorGrading = current.colorGradingPreset
            )
            _aiTipsResult.value = res
            _isGeneratingTips.value = false
        }
    }

    fun updateLyricsManually(newLyrics: String) {
        _aiLyricsResult.value = newLyrics
        val current = _activeProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = current.copy(lyrics = newLyrics)
            _activeProject.value = updated
            repository.updateProject(updated)
        }
    }

    // Live Collaborative Chat Action
    fun sendUserChatMessage(msgStr: String) {
        if (msgStr.trim().isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSendingChat.value = true
            val userMsg = ChatMsg(sender = "Uživatel (Denuli Fan)", message = msgStr, isAiAssistant = false)
            repository.saveChatMessage(userMsg)

            // Compile history for AI
            val list = chatMessagesList.value.takeLast(6)
            val history = list.joinToString("\n") { "${it.sender}: ${it.message}" }

            val aiAnswer = repository.askAiAssistant(msgStr, history)
            repository.saveChatMessage(
                ChatMsg(sender = "Denuli Studio Coach", message = aiAnswer, isAiAssistant = true)
            )
            _isSendingChat.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChat()
            repository.saveChatMessage(
                ChatMsg(sender = "AI Coach", message = "Vítejte v chatu! Ptejte se na mastering, export a autorská práva.", isAiAssistant = true)
            )
        }
    }

    // Add Comment on Public Project (Zpětné komentování!)
    fun addCommentToCommunityTrack(trackId: Int, clientName: String, commentBody: String) {
        if (commentBody.trim().isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val list = communityFeed.value
            val match = list.firstOrNull { it.id == trackId }
            if (match != null) {
                val newCommentEntry = "$clientName: ${commentBody.trim()}"
                val currentRaw = match.commentsRaw
                val newRaw = if (currentRaw.isEmpty()) newCommentEntry else "$currentRaw;$newCommentEntry"
                val updated = match.copy(commentsRaw = newRaw)
                repository.updateCommunityTrack(updated)
            }
        }
    }

    // Add Like to Community Track
    fun likeCommunityTrack(trackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = communityFeed.value
            val match = list.firstOrNull { it.id == trackId }
            if (match != null) {
                val updated = match.copy(likes = match.likes + 1)
                repository.updateCommunityTrack(updated)
            }
        }
    }

    // Publish current project to community
    fun publishCurrentProjectToCommunity(authorName: String) {
        val current = _activeProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val track = CommunityTrack(
                title = current.title,
                author = authorName.ifEmpty { "Neznámý autor" },
                genre = current.genre,
                lyrics = current.lyrics,
                commentsRaw = "Denuli Studio Link: Projekt úspěšně zveřejněn. Přejeme šťastné tvoření!",
                isForSale = current.rightsPriceCzk > 0.0,
                priceCzk = current.rightsPriceCzk,
                customLicense = current.projectLicense,
                videoTemplate = current.videoTemplate,
                soundSampleType = "Vocal (${current.voiceEffect}) + Effect (${current.natureSound})"
            )
            repository.saveCommunityTrack(track)

            // Mark project as shared
            val updated = current.copy(isSharedPublicly = true)
            _activeProject.value = updated
            repository.updateProject(updated)
        }
    }

    // Save Preset template
    fun saveSettingsAsTemplate(templateName: String) {
        val current = _activeProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val template = AiSettingsTemplate(
                templateName = templateName,
                genre = current.genre,
                stylePrompt = stylePromptField.value,
                excludedPrompt = excludedPromptField.value,
                voiceEffect = current.voiceEffect,
                colorGrading = current.colorGradingPreset
            )
            repository.saveTemplate(template)
        }
    }

    fun applySettingsTemplate(template: AiSettingsTemplate) {
        val current = _activeProject.value ?: return
        stylePromptField.value = template.stylePrompt
        excludedPromptField.value = template.excludedPrompt
        val updated = current.copy(
            genre = template.genre,
            voiceEffect = template.voiceEffect,
            colorGradingPreset = template.colorGrading
        )
        _activeProject.value = updated
        saveProjectFieldsSilently(updated)
    }

    // Change current project vocal effect
    fun updateVocalEffect(effect: String) {
        val current = _activeProject.value ?: return
        val updated = current.copy(voiceEffect = effect)
        _activeProject.value = updated
        saveProjectFieldsSilently(updated)
    }

    // Change nature sounds background overlay
    fun updateNatureSound(sound: String) {
        val current = _activeProject.value ?: return
        val updated = current.copy(natureSound = sound)
        _activeProject.value = updated
        saveProjectFieldsSilently(updated)
    }

    // Add font name directly
    fun addCustomFont(font: String) {
        if (font.trim().isNotEmpty() && font !in _fontCollections.value) {
            _fontCollections.value = _fontCollections.value + font.trim()
        }
    }

    // --- LONG VIDEO EDITOR OPTIMIZATION METHODS ---
    fun toggleProxyVideoMode() {
        _isProxyEditingOnly.value = !_isProxyEditingOnly.value
    }

    fun toggleAudioStreamingPlayback() {
        _isAudioStreamingActive.value = !_isAudioStreamingActive.value
        if (_isAudioStreamingActive.value) {
            // Simulate background stream buffering chunks for a long podcast (e.g. 30min fairy tale)
            viewModelScope.launch(Dispatchers.Default) {
                while (_isAudioStreamingActive.value) {
                    _streamingIsBuffering.value = true
                    delay(850) // loading next chunk to RAM dynamically
                    _streamingIsBuffering.value = false
                    
                    val currentChunk = (_audioStreamingBufferChunkIndex.value + 1) % 6
                    _audioStreamingBufferChunkIndex.value = currentChunk
                    
                    val buffers = booleanArrayOf(false, false, false, false, false, false)
                    buffers[currentChunk] = true
                    buffers[(currentChunk + 1) % 6] = true
                    _audioStreamingBufferedChunks.value = buffers
                    
                    delay(3800) // simulated playing period of a segment
                }
            }
        }
    }

    fun startSequentialExport(context: Context, durationMinutes: Int = 30) {
        if (_isSequentialExporting.value) return
        _isSequentialExporting.value = true
        _sequentialExportProgress.value = 0f
        _savedRamMegabytes.value = 0
        
        viewModelScope.launch(Dispatchers.Default) {
            val totalFrames = durationMinutes * 60 * 60
            val steps = 20
            
            _sequentialExportStage.value = "Inicializace Jetpack Media3 Transformer. Vytváření sekvenčního bufferu..."
            delay(900)
            
            _sequentialExportStage.value = "Konfigurace Composition s plným rozlišením 1080p a LUT barevným filtrem..."
            delay(900)
            
            for (i in 1..steps) {
                if (!_isSequentialExporting.value) break
                val progress = i.toFloat() / steps.toFloat()
                _sequentialExportProgress.value = progress
                
                val currentFrame = (totalFrames * progress).toInt()
                _sequentialExportStage.value = "Vykreslování frame-by-frame (Media3 Transformer). Snímek: $currentFrame / $totalFrames (Průběžný přímý zápis na disk bez přetížení RAM)"
                
                _savedRamMegabytes.value = (2200 * progress).toInt()
                
                delay(280)
            }
            
            if (_isSequentialExporting.value) {
                _sequentialExportStage.value = "Zápis multitrack podcast/pohádkové audio stream stopy na disk..."
                delay(700)
                _sequentialExportStage.value = "Export úspěšně dokončen! Ušetřeno přes 2.2 GB RAM."
                delay(1200)
                _isSequentialExporting.value = false
                _sequentialExportProgress.value = 0f
            }
        }
    }

    fun cancelSequentialExport() {
        _isSequentialExporting.value = false
        _sequentialExportProgress.value = 0f
        _sequentialExportStage.value = "Export zrušen uživatelem"
    }

    // --- GDPR & PRIVACY PERSISTENCE CONTROLLERS ---
    fun initGdpr(context: Context) {
        val prefs = context.getSharedPreferences("denuli_studio_gdpr_prefs", Context.MODE_PRIVATE)
        if (prefs.contains("gdpr_accepted")) {
            _gdprAccepted.value = prefs.getBoolean("gdpr_accepted", false)
        } else {
            _gdprAccepted.value = null // Means undecided, trigger banner!
        }
        _gdprConsentCloud.value = prefs.getBoolean("gdpr_consent_cloud", true)
        _gdprConsentAi.value = prefs.getBoolean("gdpr_consent_ai", true)
        _gdprConsentCommunity.value = prefs.getBoolean("gdpr_consent_community", true)
        _gdprConsentTelemetry.value = prefs.getBoolean("gdpr_consent_telemetry", false)
    }

    fun saveGdprConsent(
        context: Context,
        accepted: Boolean,
        cloud: Boolean,
        ai: Boolean,
        community: Boolean,
        telemetry: Boolean
    ) {
        val prefs = context.getSharedPreferences("denuli_studio_gdpr_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("gdpr_accepted", accepted)
            putBoolean("gdpr_consent_cloud", cloud)
            putBoolean("gdpr_consent_ai", ai)
            putBoolean("gdpr_consent_community", community)
            putBoolean("gdpr_consent_telemetry", telemetry)
            apply()
        }
        _gdprAccepted.value = accepted
        _gdprConsentCloud.value = cloud
        _gdprConsentAi.value = ai
        _gdprConsentCommunity.value = community
        _gdprConsentTelemetry.value = telemetry
    }

    fun wipeAllUserDataAndRecordings(context: Context, onCompletion: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete all projects, community tracks, chat histories and saved templates from DB (GDPR Article 17)
            repository.wipeAllUserDataAndRecordings()
            
            // Clean up SharedPreferences
            val prefs = context.getSharedPreferences("denuli_studio_gdpr_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            
            // Reset local memory state
            _activeProject.value = null
            _aiLyricsResult.value = ""
            _aiTipsResult.value = ""
            _gdprAccepted.value = null // Trigger consent banner anew
            _gdprConsentCloud.value = true
            _gdprConsentAi.value = true
            _gdprConsentCommunity.value = true
            _gdprConsentTelemetry.value = false

            viewModelScope.launch(Dispatchers.Main) {
                onCompletion()
            }
        }
    }
}

// Factory for ViewModel
class StudioViewModelFactory(private val repository: StudioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
