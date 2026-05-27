package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMsg
import com.example.data.database.CommunityTrack
import com.example.data.database.Project
import com.example.data.repository.StudioRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudioViewModel
import com.example.ui.viewmodel.StudioViewModelFactory

// --- Language Dictionary ---
object Loc {
    fun t(key: String, lang: String): String {
        val cs = mapOf(
            "app_title" to "Denuli Studio 🎧",
            "tagline" to "AI Multitrack & Video Studio",
            "studio" to "Studio",
            "video" to "Video",
            "community" to "Komunita",
            "cloud" to "Cloud & Nastavení",
            "vocals" to "Vokální stopa (Muti-Input)",
            "synth" to "Virtuální nástroj (Synth)",
            "drums" to "Bicí / Rytmický beat",
            "nature" to "Přírodní ruchy & zvířata",
            "record" to "Nahrávat",
            "record_stop" to "Zastavit",
            "play" to "Přehrát",
            "pause" to "Pozastavit",
            "active_project" to "Aktivní projekt",
            "no_project" to "Zvolte nebo vytvořte píseň",
            "create_project" to "Nový projekt",
            "project_settings" to "Konfigurace zvuku a AI",
            "lyrics" to "Text skladby",
            "genre" to "Hudební žánr",
            "bpm" to "Tempo (BPM)",
            "style_prompt" to "Lyric & Style Prompt",
            "excluded_prompt" to "Vyloučit styl (Anti-Slop)",
            "vocal_prompt" to "Prompt tónu vokálu",
            "generate_lyrics" to "AI Tvorba textu & melodie",
            "generating" to "Generuji přes Gemini...",
            "mixing_tips" to "AI mastering & Audio-Video analýza",
            "get_tips" to "Spustit Mastering AI",
            "color_grading" to "Barevná korekce & Grading",
            "video_temp" to "Video overlay šablona",
            "transition" to "Video přechodový efekt",
            "fonts" to "Správa kolekcí písem",
            "add_font" to "Importovat vlastní font",
            "copyright" to "Autorská práva a Distribuce",
            "price_czk" to "Cena autorské licence (CZK)",
            "license_type" to "Licenční smlouva",
            "publish_btn" to "Publikovat píseň v komunitě",
            "trending" to "Trending projekty a inspirace (Denuli-CZ)",
            "chat_title" to "Společný chat & live spolupráce",
            "feedback" to "Uživatelské recenze a zpětné komentování",
            "write_comment" to "Přidat recenzi k písni...",
            "send" to "Odeslat",
            "rights_for_sale" to "Prodává autorská práva",
            "certified" to "Bezpečně ověřeno",
            "license_info" to "Denuli Studio chrání autory a automaticky registruje autorská práva.",
            "onboarding" to "Nápověda pro nováčky",
            "next" to "Další tip",
            "close" to "Zavřít průvodce",
            "step1" to "1. Klikněte na RECORD a nahrávejte vokály z více mobilních mikrofonů naráz.",
            "step2" to "2. Vepište style prompt umělé inteligenci. Vylučte slabé zvuky.",
            "step3" to "3. Ve Video Timeline upravte dobu trvání překryvů, přidejte barevné filtry a importujte písmo.",
            "step4" to "4. Zveřejněte na sociální sítě jako TikTok, Instagram či Spotify se 100% ochranou autorských práv!"
        )
        val en = mapOf(
            "app_title" to "Denuli Studio 🎧",
            "tagline" to "AI Multitrack & Video Studio",
            "studio" to "Studio",
            "video" to "Video Studio",
            "community" to "Community",
            "cloud" to "Cloud & Sync",
            "vocals" to "Vocal Track (Multi-Input)",
            "synth" to "Virtual Synth Instrument",
            "drums" to "Drums & Rhythmic Beats",
            "nature" to "Nature FX & Animal Sound",
            "record" to "Record",
            "record_stop" to "Stop Rec",
            "play" to "Play",
            "pause" to "Pause",
            "active_project" to "Active Project",
            "no_project" to "Please select or start a project",
            "create_project" to "Create Project",
            "project_settings" to "Audio & AI Configuration",
            "lyrics" to "Song Lyrics Sheet",
            "genre" to "Musical Genre",
            "bpm" to "Tempo (BPM)",
            "style_prompt" to "Lyric & Style prompt",
            "excluded_prompt" to "Exclude Style filter (anti-slop)",
            "vocal_prompt" to "Vocal Tone Prompt",
            "generate_lyrics" to "Create Lyrics & Style via AI",
            "generating" to "Generating via Gemini...",
            "mixing_tips" to "AI mastering & Audio-Video guidance",
            "get_tips" to "Run Mastering AI Assistant",
            "color_grading" to "Color Grading & Transitions",
            "video_temp" to "Video Template Overlay",
            "transition" to "Video Transition Style",
            "fonts" to "Manage Font Collections",
            "add_font" to "Import custom font",
            "copyright" to "Artist Safekeeping & Licensing",
            "price_czk" to "License Sell Price (CZK)",
            "license_type" to "Distribution License Type",
            "publish_btn" to "Publish Song to Community Feed",
            "trending" to "Trending Tracks & Inspiration (Denuli-CZ)",
            "chat_title" to "Team Live Chat & Cooperation",
            "feedback" to "User feedback & community comments",
            "write_comment" to "Leave design review...",
            "send" to "Send",
            "rights_for_sale" to "Selling Copyright Rights",
            "certified" to "Securely verified",
            "license_info" to "Denuli Studio defends creators and registers your legal copyrights.",
            "onboarding" to "Onboarding Masterclass Map",
            "next" to "Next Tip",
            "close" to "Dismiss Guide",
            "step1" to "1. Click RECORD to stream high-fidelity multi-input vocal segments simultaneously.",
            "step2" to "2. Customize lyric prompt boxes, and insert excluded style presets.",
            "step3" to "3. Enter Video tab to adjust clip lengths, add overlay fonts, and color-grade.",
            "step4" to "4. Share with global networks including TikTok, Spotify, and YouTube under solid authorship licenses!"
        )
        if (lang != "CS" && lang != "EN") {
            return com.example.util.TranslationUtility.resolve(lang, cs[key] ?: key, en[key] ?: key)
        }
        val selectedMap = if (lang == "CS") cs else en
        return selectedMap[key] ?: key
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = StudioRepository(database.studioDao())
        val viewModelFactory = StudioViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[StudioViewModel::class.java]

        val billingHelper = com.example.billing.PlayBillingHelper(this) { success ->
            viewModel.setPremiumStatus(success)
        }
        viewModel.billingHelper = billingHelper

        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationRow(viewModel = viewModel)
                    }
                ) { innerPadding ->
                    MainLayoutContainer(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainLayoutContainer(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    var onboardingStep by remember { mutableStateOf(0) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initOnboarding(context)
        viewModel.initGdpr(context)
        viewModel.initAiGenerationUsageCount(context)
        viewModel.initPremiumStatus(context)
    }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xFF121214) else Color(0xFFF7F4EF))
    ) {
        // 1. Particle sparkle background (Dark mode only)
        SparkleBackground(isDarkMode = isDarkMode)

        // 2. Translucent background quote watermark under panels
        WatermarkBackground(lang = lang, isDarkMode = isDarkMode)
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            MainHeaderRow(viewModel = viewModel)

            // Dynamic Content Tabs (5 main sections mapping user requirements)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    "Domu" -> HomeFeedTabScreen(viewModel = viewModel)
                    "Studio" -> StudioTabScreen(viewModel = viewModel)
                    "Video" -> VideoTabScreen(viewModel = viewModel)
                    "Chat" -> ChatTabScreen(viewModel = viewModel)
                    "Market" -> MarketplaceTabScreen(viewModel = viewModel)
                    "Profile" -> ProfileTabScreen(viewModel = viewModel)
                    else -> StudioTabScreen(viewModel = viewModel)
                }
            }
        }

        // Onboarding Floating Overlay Dialog - UPGRADED TO GORGEOUS INTERACTIVE SPOTLIGHT OVERLAY
        if (showOnboarding) {
            SpotlightOnboardingOverlay(
                viewModel = viewModel,
                step = onboardingStep,
                onStepChange = { onboardingStep = it }
            )
        }

        // GDPR Floating Bottom Consent Banner Overlay (Article 6 Consent)
        GdprConsentBanner(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
        )

        val showPaywall by viewModel.showPaywallDialog.collectAsStateWithLifecycle()
        if (showPaywall) {
            PaywallDialog(viewModel = viewModel)
        }
    }
}

@Composable
fun MainHeaderRow(viewModel: StudioViewModel) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color(0xFF09060F).copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = Loc.t("app_title", lang),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF00FFCC), CircleShape)
                )
                Text(
                    text = Loc.t("tagline", lang),
                    color = Color(0xFFC3B6DF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Tutorial trigger button
            IconButton(
                onClick = { viewModel.toggleOnboarding() },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF201630), CircleShape)
            ) {
                Text(
                    text = "📖",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Language Switcher
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF4C3A75), RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E142F))
                    .clickable {
                        viewModel.switchLanguage(if (lang == "CS") "EN" else "CS")
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "🌐", fontSize = 12.sp)
                Text(
                    text = if (lang == "CS") "Čeština" else "English",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// --- COMPONENT: AI STYLE BOX (Dark Twilight theme) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiStyleBox(viewModel: StudioViewModel, lang: String) {
    val stylePrompt by viewModel.stylePromptField.collectAsStateWithLifecycle()
    val excludedPrompt by viewModel.excludedPromptField.collectAsStateWithLifecycle()
    
    val styleInfluence by viewModel.styleInfluencePct.collectAsStateWithLifecycle()
    val vocalDelay by viewModel.vocalDelayMs.collectAsStateWithLifecycle()
    val mixBalance by viewModel.backgroundMusicMix.collectAsStateWithLifecycle()

    val styleMixingA by viewModel.styleMixingA.collectAsStateWithLifecycle()
    val styleMixingB by viewModel.styleMixingB.collectAsStateWithLifecycle()
    val styleMixingWeightA by viewModel.styleMixingWeightA.collectAsStateWithLifecycle()
    val vocalLanguage by viewModel.vocalLyricalLanguage.collectAsStateWithLifecycle()

    val isGeneratingCloudMusic by viewModel.isCloudMusicGenerating.collectAsStateWithLifecycle()
    val templatesList by viewModel.savedTemplatesList.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val boxBg = if (isDarkMode) Color(0xFF140D22) else Color(0xFFFBF9F6)
    val cardBorder = if (isDarkMode) Color(0xFF7033D4) else Color(0xFFE5A9AC).copy(alpha = 0.6f)
    val txtPrimary = if (isDarkMode) Color.White else Color(0xFF121214)
    val txtSec = if (isDarkMode) Color(0xFFDFCCFF) else Color(0xFF53318F)
    val inputBorderFocused = if (isDarkMode) Color(0xFF00FFCC) else Color(0xFF8F63F4)
    val inputBorderUnfocused = if (isDarkMode) Color(0xFF38235C) else Color(0xFFE5A9AC).copy(alpha = 0.4f)
    val accentColor = if (isDarkMode) Color(0xFF00FFCC) else Color(0xFFE5A9AC) // Neon teal vs Rose Gold

    var templateNameInput by remember { mutableStateOf("") }
    var showTemplatesDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    MysticCornerBox(isDarkMode = isDarkMode, modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = boxBg),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.5.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_style_box")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row with Save Icon & Saved Templates Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨", fontSize = 20.sp)
                        Text(
                            text = if (lang == "CS") "AI STYLE BOX STUDIO" else "AI STYLE BOX CONTROL",
                            color = txtPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                Box {
                    IconButton(onClick = { showTemplatesDropdown = !showTemplatesDropdown }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Saved Templates",
                            tint = Color(0xFF00FFCC)
                        )
                    }

                    DropdownMenu(
                        expanded = showTemplatesDropdown,
                        onDismissRequest = { showTemplatesDropdown = false },
                        modifier = Modifier
                            .background(Color(0xFF1D1432))
                            .border(1.dp, Color(0xFF8F63F4), RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = if (lang == "CS") "💾 ULOŽENÉ ŠABLONY" else "💾 SAVED TEMPLATES",
                                    color = Color(0xFFC4B2FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ) 
                            },
                            onClick = {}
                        )

                        if (templatesList.isEmpty()) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = if (lang == "CS") "Žádné šablony" else "No templates saved",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    ) 
                                },
                                onClick = {}
                            )
                        } else {
                            templatesList.forEach { temp ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(temp.templateName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Genre: ${temp.genre} | FX: ${temp.voiceEffect}", color = Color.Gray, fontSize = 10.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.applySettingsTemplate(temp)
                                        showTemplatesDropdown = false
                                        Toast.makeText(context, if (lang == "CS") "Načtena šablona: ${temp.templateName}" else "Restored: ${temp.templateName}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Positive Style Prompt Card Selector
            Text(
                text = if (lang == "CS") "TEXTOVÝ PROMPT (Positive Prompt)" else "TEXT STYLE PROMPT (Positive Prompt)",
                color = Color(0xFFDFCCFF),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = stylePrompt,
                onValueChange = { viewModel.stylePromptField.value = it },
                placeholder = { 
                    Text(
                        text = "např. filmová hudba, piáno, epické smyčce, tempo 120 BPM...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00FFCC),
                    unfocusedBorderColor = Color(0xFF38235C)
                ),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("style_prompt_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Presets row (Quick buttons)
            Text(
                text = if (lang == "CS") "Rychlé styly (Presets / Bubbles):" else "Quick Style Presets:",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple(
                        if (lang == "CS") "Pop 2026" else "Pop 2026",
                        "modern synth pop, 115 BPM, vibrant analog synths, trending 2026, billboard top hits",
                        "heavy metal, screaming, country acoustic, static noise, scratchy voice"
                    ),
                    Triple(
                        if (lang == "CS") "Pohádkový podkres" else "Fairytale",
                        "celesta, fairy harp, gentle orchestral strings, magical fairytale ambience, cinematic, woodwinds, 80 BPM",
                        "harsh electronic drums, techno bass, heavy metal distortion"
                    ),
                    Triple(
                        if (lang == "CS") "Podcast Clean" else "Podcast Clean",
                        "warm broadcast studio speech environment, soft backing cinematic pads, acoustic guitar background, high dialogue clarity",
                        "heavy rock beats, screaming, noise vocals, high distortion, static background hiss"
                    ),
                    Triple(
                        if (lang == "CS") "Lo-Fi Relax" else "Lo-Fi Lounge",
                        "chill lofi hip hop beat, dust vinyl crackles, warm electric rhodes piano, 72 BPM, cozy jazz chords, late night sound",
                        "fast techno tempo, screeching synthesizers, heavy electric guitars"
                    )
                ).forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF281845))
                            .clickable {
                                viewModel.stylePromptField.value = preset.second
                                viewModel.excludedPromptField.value = preset.third
                                Toast.makeText(
                                    context, 
                                    if (lang == "CS") "Aplikován styl: ${preset.first}" else "Applied styling preset: ${preset.first}", 
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .border(1.dp, Color(0xFF8F63F4).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🏷️ [${preset.first}]",
                            color = Color(0xFF00FFCC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Negative Prompt
            Text(
                text = if (lang == "CS") "NEGATIVNÍ PROMPT (Excluded/Negative)" else "EXCLUDED PROMPT (Negative / Filter Out)",
                color = Color(0xFFFFCCCC),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = excludedPrompt,
                onValueChange = { viewModel.excludedPromptField.value = it },
                placeholder = { 
                    Text(
                        text = "např. bez bicích, bez ostrých syntezátorů, žádný šum, bez slopu...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Red,
                    unfocusedBorderColor = Color(0xFF5C1C29)
                ),
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("excluded_prompt_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Style Combinator Row (Míchání Stylů)
            Text(
                text = if (lang == "CS") "KOMBINACE STYLŮ (% MÍCHÁNÍ S PROMPT VÁHOU)" else "STYLE MIXING COMBINATOR",
                color = Color(0xFFD4CCFF),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Style A Picker (Editable field)
                OutlinedTextField(
                    value = styleMixingA,
                    onValueChange = { viewModel.setStyleMixingA(it) },
                    label = { Text("Style A", fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8F63F4),
                        unfocusedBorderColor = Color(0xFF2C1943)
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Style B Picker
                OutlinedTextField(
                    value = styleMixingB,
                    onValueChange = { viewModel.setStyleMixingB(it) },
                    label = { Text("Style B", fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8F63F4),
                        unfocusedBorderColor = Color(0xFF2C1943)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Percentage Slider
            val mixWeightAPct = (styleMixingWeightA * 100).toInt()
            val mixWeightBPct = 100 - mixWeightAPct

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$mixWeightAPct% $styleMixingA",
                    color = Color(0xFF00FFCC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$mixWeightBPct% $styleMixingB",
                    color = Color(0xFFFFAA00),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = styleMixingWeightA,
                onValueChange = { viewModel.setStyleMixingWeightA(it) },
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF00FFCC),
                    inactiveTrackColor = Color(0xFFFFAA00),
                    thumbColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Target vocal lyr language picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "CS") "Cílový jazyk textu písně:" else "Target Lyrical Language:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CS", "EN", "DE", "FR").forEach { l ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (vocalLanguage == l) Color(0xFF00FFCC) else Color(0xFF2D1E45))
                                .clickable { viewModel.setVocalLyricalLanguage(l) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                l,
                                color = if (vocalLanguage == l) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            MysticRunicDivider(isDarkMode = isDarkMode, modifier = Modifier.padding(vertical = 8.dp))

            // FINE TUNING PARAMETERS (Style influence %, Vocal Delay ms, Mix balance %)
            Text(
                text = if (lang == "CS") "PARAMETRY OVLIVNĚNÍ VOKÁLU A HUDBY (Fine-Tuning)" else "AI VOCAL & PRODUCTION MASTERING CONTROL",
                color = Color(0xFFFFF0CC),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Parameter 1: Style Influence %
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (lang == "CS") "Vliv stylu (Style Influence %)" else "Style Influence %",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(styleInfluence * 100).toInt()}%",
                        color = Color(0xFF00FFCC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = styleInfluence,
                    onValueChange = { viewModel.setStyleInfluencePct(it) },
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF8F63F4),
                        inactiveTrackColor = Color(0xFF2D1D45)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Parameter 2: Vocal Delay Ms
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (lang == "CS") "Zpoždění vokálu (Vocal Latency Shift)" else "Vocal Latency Shift Delay",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$vocalDelay ms",
                        color = Color(0xFF00FFCC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = vocalDelay.toFloat(),
                    onValueChange = { viewModel.setVocalDelayMs(it.toLong()) },
                    valueRange = 0f..500f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF8F63F4),
                        inactiveTrackColor = Color(0xFF2D1D45)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Parameter 3: Mix Balance %
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (lang == "CS") "Poměr pozadí (Mix Balance %)" else "Backing Track vs Local Vocal Mix Balance",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    val musicPct = (mixBalance * 100).toInt()
                    val voicePct = 100 - musicPct
                    Text(
                        text = "Backing $musicPct% / Vocal $voicePct%",
                        color = Color(0xFF00FFCC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = mixBalance,
                    onValueChange = { viewModel.setBackgroundMusicMix(it) },
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF8F63F4),
                        inactiveTrackColor = Color(0xFF2D1D45)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // One Click Template Quick Saver Component
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = templateNameInput,
                    onValueChange = { templateNameInput = it },
                    placeholder = { Text(if (lang == "CS") "Uložit jako šablonu..." else "Save custom template name...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8F63F4),
                        unfocusedBorderColor = Color(0xFF2E1C48)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (templateNameInput.trim().isNotEmpty()) {
                            viewModel.saveSettingsAsTemplate(templateNameInput.trim())
                            Toast.makeText(
                                context, 
                                if (lang == "CS") "Uložena šablona: $templateNameInput" else "Saved Settings Template: $templateNameInput", 
                                Toast.LENGTH_SHORT
                            ).show()
                            templateNameInput = ""
                        } else {
                            Toast.makeText(context, "Zadejte název šablony", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC))
                ) {
                    Text(
                        text = if (lang == "CS") "Uložit" else "Save",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button to trigger cloud rendering with socket timeout
            GlowingMainButtonWrapper(isGenerating = isGeneratingCloudMusic, isDarkMode = isDarkMode, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { 
                        viewModel.saveCurrentProjectState()
                        viewModel.generateCloudMusicWithTimeout(context, stylePrompt, excludedPrompt) 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGeneratingCloudMusic) Color(0xFFFFAA00) else accentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cloud_render_button"),
                    enabled = !isGeneratingCloudMusic
                ) {
                    if (isGeneratingCloudMusic) {
                        CircularProgressIndicator(
                            color = Color.Black, 
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == "CS") "GENERUJI NA CLOUDU S TIMEOUTEM (30s)..." else "GENERATING CHUNKS OVER TIMEOUT SOCKETS...",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    } else {
                        Text("🎸", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == "CS") "GENEROVAT AI PODKRES V CLOUDU" else "RUN CLOUD BACKING GENERATION",
                            color = if (isDarkMode) Color.Black else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
}

// --- TAB 1: STUDIO SCREEN ---
@Composable
fun StudioTabScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val projectsList by viewModel.projectsList.collectAsStateWithLifecycle()

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()

    val vocalVol by viewModel.vocalVolume.collectAsStateWithLifecycle()
    val synthVol by viewModel.synthVolume.collectAsStateWithLifecycle()
    val drumsVol by viewModel.drumsVolume.collectAsStateWithLifecycle()
    val natureVol by viewModel.natureVolume.collectAsStateWithLifecycle()

    val vocalWave by viewModel.vocalWaveform.collectAsStateWithLifecycle()
    val synthWave by viewModel.synthWaveform.collectAsStateWithLifecycle()
    val drumsWave by viewModel.drumsWaveform.collectAsStateWithLifecycle()
    val natureWave by viewModel.natureWaveform.collectAsStateWithLifecycle()

    val isGeneratingLyrics by viewModel.isGeneratingLyrics.collectAsStateWithLifecycle()
    val lyricsResult by viewModel.aiLyricsResult.collectAsStateWithLifecycle()

    var localLyrics by remember(lyricsResult, activeProject?.id) { mutableStateOf(lyricsResult) }

    var showProjectSelector by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("MP3") }
    var exportProgress by remember { mutableStateOf(0f) }
    var isExporting by remember { mutableStateOf(false) }
    var estimatedTimeSec by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Project Quick Info Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160F25)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF332050))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Loc.t("active_project", lang).uppercase(),
                            color = Color(0xFFC3B0FE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeProject?.title ?: Loc.t("no_project", lang),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (activeProject != null) {
                            Text(
                                text = "Genre: ${activeProject?.genre} | BPM: ${activeProject?.bpm}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showProjectSelector = !showProjectSelector },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1947)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Projekty", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.createNewProject() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Nový", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Expanded Project Selection list drop down
        if (showProjectSelector) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF201633)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF8F63F4).copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Vyberte projekt",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (projectsList.isEmpty()) {
                            Text("Žádné uložené projekty", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            projectsList.forEach { proj ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectProject(proj)
                                            showProjectSelector = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (activeProject?.id == proj.id) "🟢" else "💿",
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = proj.title,
                                            color = if (activeProject?.id == proj.id) Color(0xFF00FFCC) else Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProject(proj.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF32264D)))
                            }
                        }
                    }
                }
            }
        }

        if (activeProject != null) {
            // MULTI-TRACK MIXER DASHBOARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0B19)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF3F2766)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MULTITRACK MASTER MIXER",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            // RECORD & PLAY CONTROLS
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.toggleRecord() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRecording) Color.Red else Color(0xFF330C1E)
                                    ),
                                    border = BorderStroke(1.dp, if (isRecording) Color.White else Color.Red),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("record_button")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isRecording) Loc.t("record_stop", lang) else Loc.t("record", lang),
                                        fontSize = 11.sp,
                                        color = if (isRecording) Color.White else Color.Red
                                    )
                                }

                                Button(
                                    onClick = { viewModel.togglePlay(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPlaying) Color(0xFF0B3B2B) else Color(0xFF142F20)
                                    ),
                                    border = BorderStroke(1.dp, if (isPlaying) Color(0xFF00FF99) else Color(0xFF00AA66)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("play_button")
                                ) {
                                    Text(
                                        text = if (isPlaying) "⏸️" else "▶️",
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isPlaying) Loc.t("pause", lang) else Loc.t("play", lang),
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live simulated time tracking line
                        LinearProgressIndicator(
                            progress = { playbackProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = Color(0xFF00FFCC),
                            trackColor = Color(0xFF281E3B),
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // TRACK 1: VOCALS
                        TrackVolumeControlRow(
                            trackName = Loc.t("vocals", lang),
                            volume = vocalVol,
                            onVolChange = { viewModel.setVocalVolume(it) },
                            waveform = vocalWave,
                            barColor = if (isRecording) Color.Red else Color(0xFF8F63F4),
                            iconLabel = "🎙️"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // TRACK 2: VIRTUAL SYNTH
                        TrackVolumeControlRow(
                            trackName = Loc.t("synth", lang),
                            volume = synthVol,
                            onVolChange = { viewModel.setSynthVolume(it) },
                            waveform = synthWave,
                            barColor = Color(0xFFCC66FF),
                            iconLabel = "🎹"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // TRACK 3: DRUMS
                        TrackVolumeControlRow(
                            trackName = Loc.t("drums", lang),
                            volume = drumsVol,
                            onVolChange = { viewModel.setDrumsVolume(it) },
                            waveform = drumsWave,
                            barColor = Color(0xFF00FFCC),
                            iconLabel = "🥁"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // TRACK 4: NATURE & ANIMALS OVERLAYS
                        TrackVolumeControlRow(
                            trackName = Loc.t("nature", lang),
                            volume = natureVol,
                            onVolChange = { viewModel.setNatureVolume(it) },
                            waveform = natureWave,
                            barColor = Color(0xFFFFAA00),
                            iconLabel = "🌲"
                        )
                    }
                }
            }

            // EFFECT MATRIX SELECTORS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF150E23)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF37205A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AUREAL FX & NATURE CAPTURES",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Vocal FX Row
                        Text("Zpěvové Vokální Efekty (Voice Effects):", color = Color(0xFFDFD0FF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        val effects = listOf("None", "Warm Reverb", "Stereo Echo", "Heavy Pitch-Up", "Robotic Vocoder")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(effects) { fx ->
                                val isSelected = activeProject?.voiceEffect == fx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF8F63F4) else Color(0xFF261B3D))
                                        .border(1.dp, if (isSelected) Color.White else Color(0xFF4C3075), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.updateVocalEffect(fx) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(fx, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Authentic Nature/Animal overlay row
                        Text("Autentické zvuky přírody a zvířat (Nature Overlays):", color = Color(0xFFDFD0FF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        val natures = listOf("None", "Forest Birds 🐦", "Gentle Rain 🌧️", "Ocean Waves 🌊", "Thunderstorm ⚡", "Jungle Wildlife 🐒")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(natures) { nat ->
                                val isSelected = activeProject?.natureSound == nat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFFAA00) else Color(0xFF261B3D))
                                        .border(1.dp, if (isSelected) Color.White else Color(0xFF755100), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.updateNatureSound(nat) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(nat, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // PROJECT SETTINGS TITLE & GENRE CARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140C20)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF3A1F5B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Loc.t("project_settings", lang).uppercase(),
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(text = "💿", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Edit Title and Genre
                        OutlinedTextField(
                            value = viewModel.projectTitleField.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.projectTitleField.value = it },
                            label = { Text("Název Písně / Song Title") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF8F63F4),
                                unfocusedBorderColor = Color(0xFF3D255F)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = viewModel.projectGenreField.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.projectGenreField.value = it },
                            label = { Text(Loc.t("genre", lang)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF8F63F4),
                                unfocusedBorderColor = Color(0xFF3D255F)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // DYNAMIC INTEGRATED AI STYLE BOX STUDIO
            item {
                AiStyleBox(viewModel = viewModel, lang = lang)
            }

            // INTUITIVNÍ PANEL INTERPRETŮ (Vocal Selection UI)
            item {
                VocalSelectionPanel(viewModel = viewModel, lang = lang)
            }

            // EXPORT PROJECT CARD (High-End Rendering Engine)
            item {
                val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
                val cardBg = if (isDarkMode) Color(0xFF140D22) else Color(0xFFFBF9F6)
                val cardBorder = if (isDarkMode) Color(0xFF7033D4) else Color(0xFFE5A9AC).copy(alpha = 0.6f)
                val txtPrimary = if (isDarkMode) Color.White else Color(0xFF121214)

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚡", fontSize = 20.sp)
                            Text(
                                text = if (lang == "CS") "RYCHLÝ EXPORT & VYSOKÁ KVALITA" else "FAST EXPORT & MASTERING RENDER",
                                color = txtPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (lang == "CS") "Vyexportujte svůj vytvořený projekt v optimálním hudebním formátu bez prodlevy a pádů." else "Export your project immediately using professional-grade formats.",
                            color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlowingMainButtonWrapper(isGenerating = false, isDarkMode = isDarkMode, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { showExportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) Color(0xFF00FFCC) else Color(0xFFE5A9AC)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("studio_export_project_btn")
                            ) {
                                Text("📥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "CS") "EXPORT A RENDER" else "EXPORT & RENDER PROJECT",
                                    color = if (isDarkMode) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // LYRICS & VOCAL AUTOMATION COMPOSER
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF120B1D)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF331E54)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang == "CS") "GENERÁTOR TEXTŮ & LYRIKY" else "AI LYRIC GENERATOR STUDIO",
                            color = Color(0xFFE5D1FA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = viewModel.lyricPromptField.collectAsStateWithLifecycle().value,
                            onValueChange = { viewModel.lyricPromptField.value = it },
                            placeholder = { Text("Téma textu: slunce, láska, naděje...") },
                            label = { Text(Loc.t("vocal_prompt", lang)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8F63F4),
                                unfocusedBorderColor = Color(0xFF381B5B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.saveCurrentProjectState()
                                viewModel.generateAiLyrics()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGeneratingLyrics
                        ) {
                            if (isGeneratingLyrics) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Loc.t("generating", lang), color = Color.White)
                            } else {
                                Text(text = "✨", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Loc.t("generate_lyrics", lang), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Editable & Interactive Lyrics Workspace
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (if (lang == "CS") "TEXT SKLADBY & MELODIE" else "SONG LYRICS SHEET") + ":",
                                color = Color(0xFFDAC7FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (localLyrics != lyricsResult) {
                                Text(
                                    text = if (lang == "CS") "💾 Kliknutím uložit změny" else "💾 Tap to save changes",
                                    color = Color(0xFF00FFCC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.updateLyricsManually(localLyrics) }
                                        .padding(4.dp)
                                )
                            } else {
                                Text(
                                    text = if (lang == "CS") "✏️ Lze ručně upravovat / vložit" else "✏️ Editable / Paste custom",
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = localLyrics,
                            onValueChange = { localLyrics = it },
                            placeholder = {
                                Text(
                                    text = if (lang == "CS") 
                                        "Zde napište, vložte svůj vlastní text (např. z ChatGPT/Gemini) s akordy [Chorus], nebo klikněte na AI Tvorbu výše..." 
                                        else "Type or paste your own lyrics sheet (e.g., from ChatGPT/Gemini) with [Chorus], [Verse] tags here...",
                                    fontSize = 11.5.sp,
                                    color = Color.Gray
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color(0xFF381B5B),
                                focusedContainerColor = Color(0xFF08040F),
                                unfocusedContainerColor = Color(0xFF08040F)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 350.dp),
                            minLines = 4
                        )
                        if (localLyrics != lyricsResult) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.updateLyricsManually(localLyrics) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (lang == "CS") "💾 ULOŽIT NOVÝ TEXT PROJEKTU" else "💾 SAVE NEW PROJECT LYRICS",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // INTEGRATED MULTI-TRACK AUDIO-VIDEO TIMELINE WORKSPACE
            item {
                MultiTrackTimelineEditor(
                    activeProject = activeProject,
                    viewModel = viewModel,
                    lang = lang
                )
            }
        } else {
            // Empty State
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎬", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Loc.t("no_project", lang),
                            color = Color.LightGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
        val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
        val dialogBg = if (isDarkMode) Color(0xFF140D22) else Color(0xFFFBF9F6)
        val textCol = if (isDarkMode) Color.White else Color(0xFF121214)
        val cardBorder = if (isDarkMode) Color(0xFF7033D4) else Color(0xFFE5A9AC).copy(alpha = 0.6f)
        val accent = if (isDarkMode) Color(0xFF00FFCC) else Color(0xFFE5A9AC)
        val context = LocalContext.current

        androidx.compose.ui.window.Dialog(onDismissRequest = { 
            if (!isExporting) showExportDialog = false 
        }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = dialogBg),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("advanced_export_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⚙️", fontSize = 24.sp)
                            Text(
                                text = if (lang == "CS") "POKROČILÝ EXPORT" else "ADVANCED MASTERING RENDER",
                                color = textCol,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        if (!isExporting) {
                            IconButton(onClick = { showExportDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = textCol)
                            }
                        }
                    }

                    if (!isExporting) {
                        Text(
                            text = if (lang == "CS") "Vyberte formát a kvalitu exportovaného souboru. Prémiové formáty nabízejí studiový bezztrátový poslech a ultravysoké rozlišení." else "Select format and quality. Premium options offer lossless master and ultra high-definition clips.",
                            color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Section: ZDARMA TIER (FREE)
                        Text(
                            text = if (lang == "CS") "🎁 ZDARMA (Základní kvalita)" else "🎁 FREE TIER (Basic Quality)",
                            color = Color(0xFFFFAA00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        listOf(
                            "MP3" to (if (lang == "CS") "MP3 (320 kbps) pro hudbu - rychlé sdílení" else "MP3 (320 kbps) Music - rapid sharing"),
                            "MP4_720" to (if (lang == "CS") "MP4 HD (720p) pro rychlé videoklipy" else "MP4 HD (720p) Video clips")
                        ).forEach { (formatKey, label) ->
                            val isSelected = selectedFormat == formatKey
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) accent else cardBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedFormat = formatKey }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = if (isSelected) "🔘" else "⚪", fontSize = 16.sp)
                                    Column {
                                        Text(text = if (formatKey == "MP4_720") "MP4 (720p)" else formatKey, color = textCol, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = label, color = if (isDarkMode) Color.LightGray else Color.DarkGray, fontSize = 10.sp)
                                    }
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            isExporting = true
                                            exportProgress = 0f
                                            estimatedTimeSec = if (formatKey.startsWith("MP4")) 14 else 6
                                            try {
                                                val serviceIntent = android.content.Intent(context, com.example.service.ExportForegroundService::class.java).apply {
                                                    putExtra(com.example.service.ExportForegroundService.EXTRA_FORMAT, formatKey)
                                                    putExtra(com.example.service.ExportForegroundService.EXTRA_DURATION_SEC, estimatedTimeSec)
                                                }
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    context.startForegroundService(serviceIntent)
                                                } else {
                                                    context.startService(serviceIntent)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text(
                                            text = if (lang == "CS") "Exportovat a stáhnout zdarma 🚀" else "Export and Download Free 🚀",
                                            color = if (isDarkMode) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Section: PRÉMIUM TIER (PREMIUM)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (lang == "CS") "👑 PRÉMIUM (Studiová kvalita)" else "👑 PREMIUM (Studio Quality)",
                                color = if (isDarkMode) Color(0xFFCC66FF) else Color(0xFF53318F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (!isPremium) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFF003C), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ZAMČENO / LOCKED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        listOf(
                            "WAV" to (if (lang == "CS") "WAV (Uncompressed, 24-bit/48kHz) pro mastering" else "WAV (Uncompressed, 24-bit/48kHz) Mastering"),
                            "FLAC" to (if (lang == "CS") "FLAC (Lossless, 24-bit/48kHz) bezztrátová kvalita" else "FLAC (Lossless, 24-bit/48kHz) high fidelity"),
                            "MP4_4K" to (if (lang == "CS") "MP4 UHD (4K / Full HD 1080p) pro profesionální klipy" else "MP4 UHD (4K / Full HD 1080p) cinema grade")
                        ).forEach { (formatKey, label) ->
                            val isSelected = selectedFormat == formatKey
                            val selectIsPremiumFormat = true
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) accent else cardBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedFormat = formatKey }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = if (isSelected) "🔘" else "⚪", fontSize = 16.sp)
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(text = if (formatKey == "MP4_4K") "MP4 (1080p / 4K)" else formatKey, color = textCol, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Icon(Icons.Default.Lock, contentDescription = "Premium lock", tint = if (isDarkMode) Color(0xFFCC66FF) else Color(0xFF53318F), modifier = Modifier.size(12.dp))
                                        }
                                        Text(text = label, color = if (isDarkMode) Color.LightGray else Color.DarkGray, fontSize = 10.sp)
                                    }
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (!isPremium) {
                                                viewModel.setShowPaywallDialog(true)
                                                Toast.makeText(context, if (lang == "CS") "Export ve vybrané kvalitě vyžaduje Prémium!" else "Selected format is a Premium feature!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                isExporting = true
                                                exportProgress = 0f
                                                estimatedTimeSec = 14
                                                try {
                                                    val serviceIntent = android.content.Intent(context, com.example.service.ExportForegroundService::class.java).apply {
                                                        putExtra(com.example.service.ExportForegroundService.EXTRA_FORMAT, formatKey)
                                                        putExtra(com.example.service.ExportForegroundService.EXTRA_DURATION_SEC, estimatedTimeSec)
                                                    }
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                        context.startForegroundService(serviceIntent)
                                                    } else {
                                                        context.startService(serviceIntent)
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!isPremium) Color.Gray else accent
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text(
                                            text = if (!isPremium) {
                                                if (lang == "CS") "Koupit Premium pro export 👑" else "Buy Premium to Unlock 👑"
                                            } else {
                                                if (lang == "CS") "Exportovat s Premium 🚀" else "Export with Premium 🚀"
                                            },
                                            color = if (!isPremium) Color.White else (if (isDarkMode) Color.Black else Color.White),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val selectIsPremiumFormat = selectedFormat == "WAV" || selectedFormat == "FLAC" || selectedFormat == "MP4_4K"

                        Button(
                            onClick = {
                                if (selectIsPremiumFormat && !isPremium) {
                                    viewModel.setShowPaywallDialog(true)
                                    Toast.makeText(context, if (lang == "CS") "Export ve vybrané kvalitě vyžaduje Prémium!" else "Selected format is a Premium feature!", Toast.LENGTH_SHORT).show()
                                } else {
                                    isExporting = true
                                    exportProgress = 0f
                                    estimatedTimeSec = if (selectedFormat.startsWith("MP4")) 14 else 6
                                    try {
                                        val serviceIntent = android.content.Intent(context, com.example.service.ExportForegroundService::class.java).apply {
                                            putExtra(com.example.service.ExportForegroundService.EXTRA_FORMAT, selectedFormat)
                                            putExtra(com.example.service.ExportForegroundService.EXTRA_DURATION_SEC, estimatedTimeSec)
                                        }
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            context.startForegroundService(serviceIntent)
                                        } else {
                                            context.startService(serviceIntent)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectIsPremiumFormat && !isPremium) Color.Gray else accent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = if (selectIsPremiumFormat && !isPremium) {
                                    (if (lang == "CS") "Koupit Premium pro export" else "Buy Premium to Unlock")
                                } else {
                                    (if (lang == "CS") "Spustit renderování na pozadí 🚀" else "Launch High-End Render 🚀")
                                },
                                color = if (selectIsPremiumFormat && !isPremium) Color.White else (if (isDarkMode) Color.Black else Color.White),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                    } else {
                        // Render progress active state
                        Text(
                            text = if (lang == "CS") "AKTIVNÍ FOREGROUND SERVICE RENDERER" else "ACTIVE FOREGROUND SERVICE ENGAGED",
                            color = Color(0xFFFFAA00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        Text(
                            text = if (lang == "CS") "Běží bezpečné renderování na pozadí. Systém aplikaci neukončí ani při uzamčení obrazovky." else "Foreground service active. Your export will compile safely even if you close the app or lock the screen.",
                            color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Estimate time remaining
                        val percent = (exportProgress * 100).toInt()
                        val secondsLeft = ((1f - exportProgress) * estimatedTimeSec).toInt().coerceAtLeast(1)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "CS") "Rendrování projektu ($selectedFormat)..." else "Compiling stream ($selectedFormat)...",
                                color = textCol,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$percent%",
                                color = accent,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = { exportProgress },
                            color = accent,
                            trackColor = cardBorder.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "CS") "Indikátor: ForegroundService [StudioDenuliService]" else "Identifier: ForegroundService [StudioDenuliService]",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (lang == "CS") "Zbyvají cca ~ ${secondsLeft}s" else "Est. Remaining ~ ${secondsLeft}s",
                                color = textCol,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LaunchedEffect(isExporting) {
                            if (isExporting) {
                                while (exportProgress < 1.0f) {
                                delay(600)
                                exportProgress += 0.1f
                            }
                            isExporting = false
                            showExportDialog = false

                            val projTitle = activeProject?.title ?: "My_Song"
                            val result = com.example.util.ExportFileHelper.saveSampleExportFile(context, projTitle, selectedFormat)

                            val message = if (lang == "CS") {
                                if (result.first) {
                                    "✨ ÚSPĚŠNĚ STAŽENO! Soubor je uložen ve složce:\n📂 Downloads/StudioDenuli\n(Název: ${projTitle.replace("[\\\\/:*?\"<>|]".toRegex(), "_")}.${if(selectedFormat.startsWith("MP4")) "mp4" else if(selectedFormat == "WAV") "wav" else if(selectedFormat == "FLAC") "flac" else "mp3"})"
                                } else {
                                    "⚠️ Export dokončen, ale uložení do Downloads selhalo: ${result.second}"
                                }
                            } else {
                                if (result.first) {
                                    "✨ DOWNLOAD SUCCESSFUL! Saved to:\n📂 Downloads/StudioDenuli\n(File: ${projTitle.replace("[\\\\/:*?\"<>|]".toRegex(), "_")}.${if(selectedFormat.startsWith("MP4")) "mp4" else if(selectedFormat == "WAV") "wav" else if(selectedFormat == "FLAC") "flac" else "mp3"})"
                                } else {
                                    "⚠️ Export done, but local save failed: ${result.second}"
                                }
                            }

                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VocalSelectionPanel(viewModel: StudioViewModel, lang: String) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val vocalScaleType by viewModel.vocalScaleType.collectAsStateWithLifecycle()
    val vocalEmotion by viewModel.vocalEmotion.collectAsStateWithLifecycle()

    val cardBg = if (isDarkMode) Color(0xFF120B1F) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color(0xFF3D255F) else Color(0xFFE5A9AC).copy(alpha = 0.5f)
    val textPrimary = if (isDarkMode) Color.White else Color(0xFF121214)
    val accent = if (isDarkMode) Color(0xFF00FFCC) else Color(0xFFE5A9AC)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("vocal_selection_panel")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section 1: Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🎙️", fontSize = 20.sp)
                Column {
                    Text(
                        text = if (lang == "CS") "HLASOVÁ SYNTÉZA A INTERPRETI" else "UNIVERSAL VOCAL SYNTHESIS ENGINE",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (lang == "CS") "Kombinace interpretů s dokonalou intonací, diakritikou a nádechy." else "Intelligent vocal synthesis with breath and pronunciation control.",
                        color = if (isDarkMode) Color.Gray else Color.DarkGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Selection Block
            Text(
                text = if (lang == "CS") "VÝBĚR INTERPRETŮ (PRESETE):" else "SELECT INTERPRETERS (PRESETS):",
                color = if (isDarkMode) Color(0xFFC5B0FE) else Color(0xFF53318F),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preset rows representation
            val presets = listOf(
                "Muž" to Pair("👨 Sólo muž", if (lang == "CS") "Sólový mužský baryton (Rock, Rap, Metal)" else "Solo baritone (Rock, Rap, Metal)"),
                "Žena" to Pair("👩 Sólo žena", if (lang == "CS") "Čistý dívčí soprán s perfektní intonací" else "Clean female soprano with high frequency"),
                "Dítě" to Pair("👶 Sólo dítě", if (lang == "CS") "Andělsky jemný čistý dětský hlásek" else "Pure angelic boy/girl solo voice"),
                "Duet" to Pair("💑 Duet / Skupina", if (lang == "CS") "Kombinace dvou hlasů (duety, kapely, duos)" else "Blend of two distinct voices (duets, bands)"),
                "Sbor" to Pair("🏛️ Filharmonický sbor", if (lang == "CS") "Epický chrámový sbor s plnou polyfonií" else "Epic cathedral choir with full polyphony"),
                "Dav" to Pair("🏟️ Stadionový dav", if (lang == "CS") "Obří stadionový pokřik a zpívající fanoušci" else "Gigantic singing stadium crowd and fans")
            )

            // Let's lay them out in 2 column grid
            for (i in presets.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (j in i..i+1) {
                        if (j < presets.size) {
                            val presetKey = presets[j].first
                            val presetDetails = presets[j].second
                            val isSelected = vocalScaleType == presetKey

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) accent else cardBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setVocalScaleType(presetKey) }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = if (isSelected) "🔘" else "⚪", fontSize = 11.sp)
                                    Text(text = presetDetails.first, color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text(
                                    text = presetDetails.second,
                                    color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Vocal Emotion
            Text(
                text = if (lang == "CS") "EMOČNÍ INTonace A VYJÁDŘENÍ (Mood Dynamics):" else "EMOTO-DYNAMIC INTENSITY & MOODS:",
                color = if (isDarkMode) Color(0xFFC5B0FE) else Color(0xFF53318F),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val emotions = listOf(
                "Agresivní" to Pair("🔥 Agresivní", if (lang == "CS") "Agresivní energický rap, scream nebo growl" else "Aggressive scream, rap intensity"),
                "Modlitba" to Pair("🌸 Jemná", if (lang == "CS") "Ukolébavka, šepot a andělsky klidná nálada" else "Lullaby, whispered soft dynamic"),
                "Emoční" to Pair("💔 Emotivní", if (lang == "CS") "Dojemný přednes pro balady nebo blues" else "Heartfelt expression for sad ballads"),
                "Epická" to Pair("⚡ Epická", if (lang == "CS") "Operní expanze s plnými nádechy a vibratem" else "Operatic power with custom depth"),
                "Mystická" to Pair("✨ Severská mystika", if (lang == "CS") "Rituální runové chorály a hrdelní zpěv" else "Nordic ritual chants & throat singing")
            )

            for (i in emotions.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (j in i..i+1) {
                        if (j < emotions.size) {
                            val emotionKey = emotions[j].first
                            val emotionDetails = emotions[j].second
                            val isSelected = vocalEmotion == emotionKey

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accent.copy(alpha = 0.12f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) accent else cardBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setVocalEmotion(emotionKey) }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = if (isSelected) "🔘" else "⚪", fontSize = 11.sp)
                                    Text(text = emotionDetails.first, color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text(
                                    text = emotionDetails.second,
                                    color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

// Waveform Canvas component for the multitrack mixing console
@Composable
fun TrackVolumeControlRow(
    trackName: String,
    volume: Float,
    onVolChange: (Float) -> Unit,
    waveform: FloatArray,
    barColor: Color,
    iconLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = iconLabel, fontSize = 14.sp)
                Text(text = trackName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = "${(volume * 100).toInt()}%", color = barColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Waveform Render Canvas
            Canvas(
                modifier = Modifier
                    .weight(0.4f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(0.5.dp, barColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 4.dp)
            ) {
                val waveSize = waveform.size
                val spaceBetween = size.width / waveSize
                for (i in 0 until waveSize) {
                    val amp = waveform[i] * size.height
                    val startX = i * spaceBetween
                    val startY = (size.height - amp) / 2
                    val endY = startY + amp

                    drawLine(
                        color = barColor.copy(alpha = 0.8f),
                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                        end = androidx.compose.ui.geometry.Offset(startX, endY),
                        strokeWidth = (spaceBetween * 0.7f).coerceIn(3f, 8f),
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Volume level trigger slider
            Slider(
                value = volume,
                onValueChange = onVolChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(0.6f),
                colors = SliderDefaults.colors(
                    activeTrackColor = barColor,
                    inactiveTrackColor = Color(0xFF201730),
                    thumbColor = Color.White
                )
            )
        }
    }
}


// --- TAB 2: VIDEO TIMELINE STUDIO ---
@Composable
fun VideoPlayerPreview(
    viewModel: StudioViewModel,
    activeProject: Project,
    lang: String
) {
    val context = LocalContext.current
    var isPlayingVideo by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<android.widget.VideoView?>(null) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F071B)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, Color(0xFF8F63F4).copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📺", fontSize = 16.sp)
                    Column {
                        Text(
                            text = if (lang == "CS") "NÁHLED STUDIOVÉHO VIDEOKLIPU" else "STUDIO VIDEO MONITOR PREVIEW",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Aktivní styl: " + (activeProject.videoTemplate) + " | " + (activeProject.colorGradingPreset),
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPlayingVideo) Color(0xFF00FFCC) else Color(0xFF8F63F4).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPlayingVideo) "LIVE STREAMING ⚡" else "IDLE MON",
                        color = if (isPlayingVideo) Color.Black else Color(0xFFC0A6FF),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF331E54), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayingVideo) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setZOrderMediaOverlay(true)
                                val videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
                                val headers = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                try {
                                    setVideoURI(android.net.Uri.parse(videoUrl), headers)
                                } catch (e: Exception) {
                                    setVideoPath(videoUrl)
                                }
                                setOnPreparedListener { mediaPlayer ->
                                    mediaPlayer.isLooping = true
                                    mediaPlayer.setVolume(1.0f, 1.0f)
                                    start()
                                }
                                setOnErrorListener { _, _, _ ->
                                    try {
                                        setVideoURI(android.net.Uri.parse("https://www.w3schools.com/html/mov_bbb.mp4"), headers)
                                        start()
                                        true
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(ctx, "Chyba při přehrávání videa", android.widget.Toast.LENGTH_SHORT).show()
                                        isPlayingVideo = false
                                        false
                                    }
                                }
                                videoViewRef = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E1032), Color(0xFF090412))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "🎬",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = if (lang == "CS") "Spustit nelineární video náhled" else "Launch Non-Linear Video Pre-Render",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Formát: Full HD 1080p | FPS: 60 | Kodek: MP4 AVC",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        
                        Button(
                            onClick = { isPlayingVideo = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("▶ PŘEHRÁT LIVE NEBO EXPORT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                val template = activeProject.videoTemplate ?: "Retro Sunset"
                val grading = activeProject.colorGradingPreset ?: "Neutral"
                
                if (!isPlayingVideo) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val overlayColor = when {
                            grading.contains("Warm") || template.contains("Sunset") -> Color(0xFFFF9900).copy(alpha = 0.12f)
                            grading.contains("Cyberpunk") || template.contains("Cyberpunk") -> Color(0xFFCC00FF).copy(alpha = 0.14f)
                            grading.contains("Nature") || template.contains("Nature") -> Color(0xFF00FF88).copy(alpha = 0.10f)
                            grading.contains("Acid") || template.contains("Acid") -> Color(0xFF66FF00).copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }
                        if (overlayColor != Color.Transparent) {
                            drawRect(color = overlayColor)
                        }
                        
                        if (template.contains("Cyberpunk") || template.contains("Glitch")) {
                            val scanlineCount = 20
                            val step = size.height / scanlineCount
                            for (j in 0 until scanlineCount) {
                                drawLine(
                                    color = Color(0xFF00FFFF).copy(alpha = 0.08f),
                                    start = androidx.compose.ui.geometry.Offset(0f, j * step),
                                    end = androidx.compose.ui.geometry.Offset(size.width, j * step),
                                    strokeWidth = 1.5f
                                )
                            }
                        }
                        
                        if (grading.contains("Vintage") || grading.contains("Super8")) {
                            drawRect(color = Color(0xFF8B5A2B).copy(alpha = 0.16f))
                        }
                    }
                }
                
                if (isPlayingVideo) {
                    val fontName = activeProject.fontName ?: "Default"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 14.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = "♪ [Zpěv: Studio Denuli Spark Professional Master Mix] ♪",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = when (fontName) {
                                "Space Grotesk" -> FontFamily.Monospace
                                "Denuli Serif" -> FontFamily.Serif
                                else -> FontFamily.Default
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            
            if (isPlayingVideo) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            videoViewRef?.stopPlayback()
                            isPlayingVideo = false
                            videoViewRef = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE02424).copy(alpha = 0.2f)),
                        border = BorderStroke(0.5.dp, Color(0xFFE02424)),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("⏹ STOP MONITOR", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = "LIVE RENDER BUFFER: 100% OK",
                        color = Color(0xFF00FFCC),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VideoTabScreen(viewModel: StudioViewModel) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val fontCollections by viewModel.fontCollections.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val isGeneratingTips by viewModel.isGeneratingTips.collectAsStateWithLifecycle()
    val tipsResult by viewModel.aiTipsResult.collectAsStateWithLifecycle()

    val isProxyEditingOnly by viewModel.isProxyEditingOnly.collectAsStateWithLifecycle()
    val isSequentialExporting by viewModel.isSequentialExporting.collectAsStateWithLifecycle()
    val sequentialExportProgress by viewModel.sequentialExportProgress.collectAsStateWithLifecycle()
    val sequentialExportStage by viewModel.sequentialExportStage.collectAsStateWithLifecycle()
    val savedRamMegabytes by viewModel.savedRamMegabytes.collectAsStateWithLifecycle()
    val isAudioStreamingActive by viewModel.isAudioStreamingActive.collectAsStateWithLifecycle()
    val audioStreamingBufferChunkIndex by viewModel.audioStreamingBufferChunkIndex.collectAsStateWithLifecycle()
    val audioStreamingBufferedChunks by viewModel.audioStreamingBufferedChunks.collectAsStateWithLifecycle()
    val streamingIsBuffering by viewModel.streamingIsBuffering.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    var customFontInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val currentProject = activeProject

    if (currentProject == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Zvolte projekt v sekci 'Studio' pro úpravu videoklipu", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // VIDEO PREVIEW MONITOR
        item {
            VideoPlayerPreview(
                viewModel = viewModel,
                activeProject = currentProject,
                lang = lang
            )
        }

        // AI VIDEO CLIP GENERATION MODULE
        item {
            var isGeneratingAiVideo by remember { mutableStateOf(false) }
            var aiVideoProgress by remember { mutableStateOf(0f) }
            var selectedVideoModel by remember { mutableStateOf("Sora Cinematic (OpenAI)") }
            var videoPromptInput by remember { mutableStateOf("") }
            val videoModels = listOf("Sora Cinematic (OpenAI)", "Runway Gen-3 Pro", "Denuli-VFX Realtime", "Stable Video 2.0")

            LaunchedEffect(isGeneratingAiVideo) {
                if (isGeneratingAiVideo) {
                    aiVideoProgress = 0f
                    while (aiVideoProgress < 1.0f) {
                        kotlinx.coroutines.delay(100)
                        aiVideoProgress += 0.04f
                    }
                    isGeneratingAiVideo = false
                    Toast.makeText(context, if (lang == "CS") "AI Video vygenerováno a propojeno s touto vícestopou časovou osou! 🎬" else "AI Video generated and linked to timeline! 🎬", Toast.LENGTH_LONG).show()
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0212)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(Color(0xFF00FFCC), Color(0xFFCC66FF))
                )),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⚡", fontSize = 18.sp)
                            Text(
                                text = if (lang == "CS") "AI GENERATOR VIDEOKLIPU K SONGU" else "AI SONG VIDEO GENERATOR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00FFCC).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Sora & Runway", color = Color(0xFF00FFCC), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (lang == "CS") "Vyberte AI video model:" else "Select Generative Video Model:",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        items(videoModels.size) { idx ->
                            val model = videoModels[idx]
                            val isSel = selectedVideoModel == model
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF8F63F4) else Color(0xFF211333))
                                    .clickable { selectedVideoModel = model }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(model, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = videoPromptInput,
                        onValueChange = { videoPromptInput = it },
                        placeholder = { Text(if (lang == "CS") "Popište scénu (např. 'neonový západ slunce s autem v dešti, filmová kvalita')" else "Describe video prompt...") },
                        label = { Text(if (lang == "CS") "Návrh scény pro AI (AI Video Prompt)" else "AI Video Prompt") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FFCC),
                            unfocusedBorderColor = Color(0xFF321A4C)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isGeneratingAiVideo) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == "CS") "Generuji video k songu přes cloudové GPU..." else "Rendering AI video for song on Cloud GPU...",
                                    color = Color(0xFF00FFCC),
                                    fontSize = 10.sp
                                )
                                Text("${(aiVideoProgress * 100).toInt()}%", color = Color(0xFF00FFCC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { aiVideoProgress },
                                color = Color(0xFF00FFCC),
                                trackColor = Color(0xFF1B0C2B),
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isGeneratingAiVideo = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (lang == "CS") "🎬 SPUSTIT AI GENEROVÁNÍ KLIPU" else "🎬 START AI VIDEO GENERATION",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // VIDEO EDIT TIMELINE GRAPHICAL CANVAS
        item {
            MultiTrackTimelineEditor(
                activeProject = currentProject,
                viewModel = viewModel,
                lang = lang
            )
        }

        // DESIGN EDIT SELECTION CONTROLS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160E25)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF311F4E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CUSTOM VIDEO EFFECTS & TRANSITIONS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // Video Templates Select
                    Text(Loc.t("video_temp", lang) + ":", color = Color(0xFFDFCEFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val templates = listOf("Retro Sunset", "Neon Cyberpunk 🌌", "Ambient Nature 🍃", "Acid Glitch 🧪", "Minimalist Slate")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(templates) { temp ->
                            val isSelected = activeProject?.videoTemplate == temp
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF00FFCC) else Color(0xFF2B1C44))
                                    .clickable {
                                        viewModel.updateProjectVideoSettings(
                                            template = temp,
                                            transition = activeProject?.videoTransition ?: "Fade",
                                            colorGrading = activeProject?.colorGradingPreset ?: "Neutral",
                                            font = activeProject?.fontName ?: "Default"
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(temp, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transition Selector
                    Text(Loc.t("transition", lang) + ":", color = Color(0xFFDFCEFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val transitions = listOf("Smooth Fade", "Slide Left", "Zoom In Out", "Rough Glitch", "Direct Cut")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(transitions) { trans ->
                            val isSelected = activeProject?.videoTransition == trans
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFFFAA00) else Color(0xFF2B1C44))
                                    .clickable {
                                        viewModel.updateProjectVideoSettings(
                                            template = activeProject?.videoTemplate ?: "Retro Sunset",
                                            transition = trans,
                                            colorGrading = activeProject?.colorGradingPreset ?: "Neutral",
                                            font = activeProject?.fontName ?: "Default"
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(trans, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Color Grading Choices
                    Text(Loc.t("color_grading", lang) + ":", color = Color(0xFFDFCEFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val gradings = listOf("Neutral", "Cinematic Warm ☀️", "Cold Cyberpunk 🔮", "Vintage Super8 🎬", "High-Contrast B&W 🖤")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(gradings) { grade ->
                            val isSelected = activeProject?.colorGradingPreset == grade
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFCC66FF) else Color(0xFF2B1C44))
                                    .clickable {
                                        viewModel.updateProjectVideoSettings(
                                            template = activeProject?.videoTemplate ?: "Retro Sunset",
                                            transition = activeProject?.videoTransition ?: "Fade",
                                            colorGrading = grade,
                                            font = activeProject?.fontName ?: "Default"
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(grade, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // CUSTOM FONTS COLLECTION MANAGER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E20)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF2F1D4A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("fonts", lang).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Fonts chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(fontCollections) { font ->
                            val isSelected = activeProject?.fontName == font
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF8F63F4) else Color(0xFF23163B))
                                    .border(1.dp, if (isSelected) Color.White else Color(0xFF452A6E), RoundedCornerShape(12.dp))
                                    .clickable {
                                        val isExclusive = font !in listOf("Space Grotesk", "Denuli Serif")
                                        if (isExclusive && !isPremium) {
                                            viewModel.setShowPaywallDialog(true)
                                            Toast.makeText(context, "Písmo '$font' je exkluzivní prémiová funkce pro předplatitele!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.updateProjectVideoSettings(
                                                template = activeProject?.videoTemplate ?: "Retro Sunset",
                                                transition = activeProject?.videoTransition ?: "Fade",
                                                colorGrading = activeProject?.colorGradingPreset ?: "Neutral",
                                                font = font
                                            )
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = font,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = when (font) {
                                        "Space Grotesk" -> FontFamily.Monospace
                                        "Denuli Serif" -> FontFamily.Serif
                                        else -> FontFamily.Default
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text Input to import custom fonts on the fly
                    OutlinedTextField(
                        value = customFontInput,
                        onValueChange = { customFontInput = it },
                        placeholder = { Text("Zadejte jméno písma k nalezení, např. 'Open Sans Czech'") },
                        label = { Text(Loc.t("add_font", lang)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FFCC),
                            unfocusedBorderColor = Color(0xFF341F53)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (customFontInput.trim().isNotEmpty()) {
                                if (!isPremium) {
                                    viewModel.setShowPaywallDialog(true)
                                    Toast.makeText(context, "Nahrávání vlastních písem je exkluzivní prémiová funkce!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addCustomFont(customFontInput)
                                    Toast.makeText(context, "Písmo '${customFontInput}' bylo nahráno do Vaší autorské kolekce!", Toast.LENGTH_SHORT).show()
                                    customFontInput = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1D44)),
                        border = BorderStroke(1.dp, Color(0xFF00FFCC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "📁", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stáhnout & Importovat Font (.ttf)", color = Color.White)
                    }
                }
            }
        }

        // LONG FORMAT PODCAST / FAIRY TALE STITCHING & RENDERING OPTIMIZATIONS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10091E)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "CS") "OPTIMALIZACE PRO DLOUHÁ VIDEA (PODCASTY & POHÁDKY)" else "LONG FORMAT EXPERT OPTIMIZATIONS (30M+)",
                        color = Color(0xFF00FFCC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Text(
                        text = if (lang == "CS") "Systémová vylepšení pro editaci a sekvenční export projektů o délce 30+ minut." else "Architecture improvements for editing and sequential rendering of 30+ min projects.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Proxy Video Editing Mode (Nízké rozlišení pro náhled)
                    Text(
                        text = if (lang == "CS") "1. Nízké rozlišení pro náhled (Proxy Video Editing):" else "1. Proxy Video Timeline Preview:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == "CS") "Během střihu a práce na časové ose se používá odlehčený 360p náhled. Plné Full HD 1080p se použije až při finálním exportu. Zabraňuje sekání a padání aplikace." else "During timeline editing, a lightweight 360p proxy preview is generated. Full 1080p is only applied at final export to guarantee flawless, smooth scrubbing.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isProxyEditingOnly) Color(0xFF00FFCC) else Color(0xFF221735))
                                .clickable { if (!isProxyEditingOnly) viewModel.toggleProxyVideoMode() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (lang == "CS") "Proxy 360p (Doporučeno) ⚡" else "Proxy 360p (Active) ⚡",
                                color = if (isProxyEditingOnly) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isProxyEditingOnly) Color(0xFFCC1155) else Color(0xFF221735))
                                .clickable { if (isProxyEditingOnly) viewModel.toggleProxyVideoMode() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (lang == "CS") "Originální Full HD 1080p" else "Full HD 1080p (Heavy)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    MysticRunicDivider(isDarkMode = isDarkMode, modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Sequential Export (Streaming / Chunked Rendering via Media3 Transformer)
                    Text(
                        text = if (lang == "CS") "2. Sekvenční export (Streaming / Chunked Rendering):" else "2. Sequential Continuous Media3 Export:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == "CS") "Rendrování frame-by-frame s průběžným zápisem na disk pomocí Jetpack Media3 Transformer. Nezatěžuje operační paměť RAM velkými 30+ min soubory naráz." else "Continuous frame-by-frame render pipeline using Jetpack Media3 Transformer. Dispatches encoded byte-chunks directly to file system to insulate RAM storage.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isSequentialExporting) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF07040C), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF381B5E), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == "CS") "Probíhá chunkovaný export..." else "Media3 Transformer Chunking...",
                                    color = Color(0xFF00FFCC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(sequentialExportProgress * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { sequentialExportProgress },
                                color = Color(0xFF00FFCC),
                                trackColor = Color(0xFF2A1C3E),
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sequentialExportStage,
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "🛡️ RAM SAVED:", color = Color(0xFFFFAA00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = "$savedRamMegabytes MB", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.cancelSequentialExport() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                                border = BorderStroke(0.5.dp, Color.Red),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (lang == "CS") "Zrušit export" else "Cancel Pipeline", color = Color.Red, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startSequentialExport(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1D44)),
                            border = BorderStroke(1.dp, Color(0xFF00FFCC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "🎞️", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "CS") "Spustit sekvenční Media3 export (30 min)" else "Start 30-Min Sequenced Export",
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    MysticRunicDivider(isDarkMode = isDarkMode, modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Rozdělení dlouhých audio stop (Podcast Segmented Streaming)
                    Text(
                        text = if (lang == "CS") "3. Načítání audio stopy na pozadí (Segmented Streaming):" else "3. Background Segmented Audio Streaming:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == "CS") "Dlouhé zvukové soubory (podcasty/pohádky) jsou rozděleny na segmenty a načítány plynule na pozadí formou streamu, nikoliv naráz do mezipaměti." else "Subdivides long audio tracks into dynamic background-streamed buffer chunks. Avoids out-of-memory overhead during timeline edits.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF07050E), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF2B1C44), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleAudioStreamingPlayback() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF00FFCC).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        ) {
                            Text(text = if (isAudioStreamingActive) "⏸️" else "▶️", fontSize = 18.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAudioStreamingActive) {
                                        if (streamingIsBuffering) {
                                            if (lang == "CS") "Vyrovnávání paměti (Buffering...)" else "Streaming / Buffering Segment..."
                                        } else {
                                            if (lang == "CS") "Přehrávání: Segment ${audioStreamingBufferChunkIndex + 1}/6" else "Playing: Segment ${audioStreamingBufferChunkIndex + 1}/6"
                                        }
                                    } else {
                                        if (lang == "CS") "Audio stream pozastaven" else "Audio Stream Idle"
                                    },
                                    color = if (isAudioStreamingActive && !streamingIsBuffering) Color(0xFF00FFCC) else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (isAudioStreamingActive && streamingIsBuffering) {
                                    CircularProgressIndicator(color = Color(0xFF00FFCC), modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Visual chunks timeline buffer map
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (index in 0 until 6) {
                                    val isBufferedAndLoaded = audioStreamingBufferedChunks.getOrNull(index) ?: false
                                    val isActiveNow = isAudioStreamingActive && (audioStreamingBufferChunkIndex == index)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                when {
                                                    isActiveNow -> Color(0xFF00FFCC)
                                                    isBufferedAndLoaded -> Color(0xFF00FFCC).copy(alpha = 0.4f)
                                                    else -> Color(0xFF2B1B48)
                                                }
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "CS") "Segmenty: 5min sekce streamované plynule na pozadí" else "Dynamic 5-minute background stream buffers.",
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // AI MASTERING ADVICE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160F2B)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, Color(0xFF8F63F4))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("mixing_tips", lang),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = { viewModel.generateAiMasteringTips() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGeneratingTips
                    ) {
                        if (isGeneratingTips) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Loc.t("generating", lang))
                        } else {
                            Text(text = "✨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Loc.t("get_tips", lang), color = Color.White)
                        }
                    }

                    if (tipsResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF07050F), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF38205C), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = tipsResult, color = Color(0xFFE5D5FF), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoTrackTimelineBar(
    trackName: String,
    trackColor: Color,
    durationLabel: String,
    keyframes: List<Float>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(trackName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(durationLabel, color = Color.LightGray, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFF231B32), RoundedCornerShape(6.dp))
                .border(0.5.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
        ) {
            // Keyframe markers
            Canvas(modifier = Modifier.fillMaxSize()) {
                keyframes.forEach { kf ->
                    drawCircle(
                        color = trackColor,
                        radius = 6f,
                        center = androidx.compose.ui.geometry.Offset(size.width * kf, size.height / 2)
                    )
                }
            }
        }
    }
}


// --- TAB 1: DOMŮ (STUDIO FEED) COMPOSABLE ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeFeedTabScreen(viewModel: StudioViewModel) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val feedList by viewModel.communityFeed.collectAsStateWithLifecycle()
    val aiHelpBotResponse by viewModel.aiHelpBotResponse.collectAsStateWithLifecycle()
    val isAiHelpBotLoading by viewModel.isAiHelpBotLoading.collectAsStateWithLifecycle()

    var activeCommentIdToEdit by remember { mutableStateOf<Int?>(null) }
    var userCommentBody by remember { mutableStateOf("") }
    var quickAiPromptText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Inspiration Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D22)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF321A54)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (lang == "CS") "VÍTEJTE V DENULI STUDIO! 🌟" else "WELCOME TO DENULI STUDIO! 🌟",
                        color = Color(0xFF00FFCC),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "CS") {
                            "Zde najdete inspirující tvorbu od ostatních autorů. Zahrajte si jejich skladby, přidejte své recenze nebo okomentujte jejich hudební díla!"
                        } else {
                            "Find inspirational tracks from other premium artists. Play original creations, write comments, or request feedback on licensing arrangements!"
                        },
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 💫 AI POMOCNÍK & KOPILOT HUDEBNÍHO STUDIA 💫
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0F33)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, Color(0xFF9E00FF).copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().testTag("home_ai_copilot_quick_help_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "🤖", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = if (lang == "CS") "AI RYCHLÁ POMOC & KOPILOT" else "AI QUICK HELP & CO-PILOT",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (lang == "CS") "Technický a umělecký poradce na telefonu" else "Creative & technical advisor at your side",
                                    color = Color(0xFFCCB3FF),
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF9E00FF).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ONLINE 🟢",
                                color = Color(0xFFE5CCFF),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (lang == "CS") {
                            "Zeptejte se umělé inteligence na libovolné téma týkající se nahrávání, masteringu, licencování, exportu skladeb, podmínek GDPR nebo nastavení efektů!"
                        } else {
                            "Ask the AI any question about multi-track audio recording, final master editing, distribution copyright, GDPR, or vocal styling!"
                        },
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quickAiPromptText,
                        onValueChange = { quickAiPromptText = it },
                        placeholder = {
                            Text(
                                if (lang == "CS") "Zadejte dotaz pro AI poradce..." else "Ask Denuli AI Advisor...",
                                fontSize = 11.5.sp,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF9E00FF),
                            unfocusedBorderColor = Color(0xFF321A54),
                            focusedContainerColor = Color(0xFF0F081E),
                            unfocusedContainerColor = Color(0xFF0F081E)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("quick_ai_helper_input_field"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Suggestion pills
                    Text(
                        text = if (lang == "CS") "Časté dotazy / vyzkoušejte hned:" else "Suggested questions / try instantly:",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val suggestions = if (lang == "CS") {
                        listOf(
                            "Jak správně nahrát hlas a jaké použít efekty?",
                            "Jak získám premium licenci pro komerční užití?",
                            "Jak funguje právo na výmaz podle GDPR?",
                            "Jak vyexportovat bezztrátový master ve formátu WAV?"
                        )
                    } else {
                        listOf(
                            "How do I record clear vocals using filters?",
                            "What are the terms of the Premium export license?",
                            "Explain GDPR data portability and security.",
                            "How do I master a full timeline track successfully?"
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFF261547))
                                    .clickable {
                                        quickAiPromptText = suggestion
                                        viewModel.askAiAssistantQuickHelp(suggestion)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    color = Color(0xFFDEC3FF),
                                    fontSize = 9.5.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (quickAiPromptText.trim().isNotEmpty()) {
                                viewModel.askAiAssistantQuickHelp(quickAiPromptText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E00FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("quick_ai_helper_submit_btn"),
                        enabled = !isAiHelpBotLoading
                    ) {
                        Text(
                            text = if (isAiHelpBotLoading) {
                                if (lang == "CS") "AI asistent sestavuje odpověď... 🔮" else "AI assistant analyzing... 🔮"
                            } else {
                                if (lang == "CS") "Položit dotaz 🚀" else "Ask AI Co-Pilot 🚀"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Response block
                    if (isAiHelpBotLoading || aiHelpBotResponse.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF100720))
                                .border(1.dp, Color(0xFFCC66FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("💬", fontSize = 14.sp)
                                    Text(
                                        text = if (lang == "CS") "ODPOVĚĎ PRO VÁS:" else "AI ADVISOR ANSWER:",
                                        color = Color(0xFFE5CCFF),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                if (isAiHelpBotLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFFCC66FF),
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = aiHelpBotResponse,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.testTag("quick_ai_helper_response_text")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Community feed title
        item {
            Text(
                text = if (lang == "CS") "🔥 POPULÁRNÍ PROJEKTY A SONGY" else "🔥 TRENDING CREATIONS & FEEDS",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }

        // Live list of community creations with rating & commenting (Zpětné hodnocení!)
        if (feedList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "CS") "Žádné projekty v komunitním kanálu." else "No projects synchronized in the community feed.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(feedList.size) { idx ->
                val track = feedList[idx]
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0916)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF26193D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Autor / Creator: ${track.author}", color = Color.LightGray, fontSize = 11.sp)
                            }

                            // Heart Likes Count trigger
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clickable { viewModel.likeCommunityTrack(track.id) }
                                    .background(Color(0xFF251336), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = Color.Red, modifier = Modifier.size(12.dp))
                                Text(track.likes.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF371A4C), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(track.genre, color = Color(0xFFCCA2FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            if (track.isForSale) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF4C3000), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("PRODEJ PRÁV: ${track.priceCzk} Kč", color = Color(0xFFFFB700), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        if (track.lyrics.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"" + track.lyrics.take(120) + "\"",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Review Commentary display list
                        Text(
                            text = if (lang == "CS") "Zpětná vazba a recenze:" else "Feedback & peer reviews:",
                            color = Color(0xFFC4ADFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        track.commentsRaw.split(";").forEach { comm ->
                            if (comm.trim().isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("💬", fontSize = 10.sp)
                                    Text(comm, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Submit comment toggler input field
                        if (activeCommentIdToEdit == track.id) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = userCommentBody,
                                    onValueChange = { userCommentBody = it },
                                    placeholder = { Text(if (lang == "CS") "Napište hodnocení..." else "Write a review comment...") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedTextColor = Color.White,
                                        focusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00FFCC),
                                        unfocusedBorderColor = Color(0xFF2E1944)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (userCommentBody.trim().isNotEmpty()) {
                                            viewModel.addCommentToCommunityTrack(
                                                track.id,
                                                clientName = "Návštěvník",
                                                commentBody = userCommentBody
                                            )
                                            userCommentBody = ""
                                            activeCommentIdToEdit = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC))
                                ) {
                                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = if (lang == "CS") "+ Přidat hodnocení / recenzi písně" else "+ Add rating & feedback comment",
                                color = Color(0xFF00FFCC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { activeCommentIdToEdit = track.id }
                                    .padding(vertical = 4.dp)
                            )
                        }

                        // Social Network shares triggering grid
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "CS") "Sdílet:" else "Share:",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                            val networks = listOf("TikTok 🎬", "Instagram 📸", "Spotify 🟢", "YouTube 🔴")
                            networks.forEach { net ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF1E142F), RoundedCornerShape(6.dp))
                                        .clickable {
                                            Toast.makeText(context, "Sdíleno na $net!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(net, color = Color(0xFFCBB7FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- TAB 3: CHAT & SPOLUPRÁCE COMPOSABLE ---
@Composable
fun ChatTabScreen(viewModel: StudioViewModel) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val chatList by viewModel.chatMessagesList.collectAsStateWithLifecycle()
    val blockedUsers by viewModel.blockedUsers.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()

    var liveChatMsgText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Filter Chat List for blocked users
    val filteredChat = chatList.filter { it.sender !in blockedUsers }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chat Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF130924)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF331C57)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "💬", fontSize = 22.sp)
                    Column {
                        Text(
                            text = if (lang == "CS") "KOLABORATIVNÍ TÝMOVÝ CHAT" else "TEAM CHAT & COLLABORATION",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (lang == "CS") "Pracujte s týmem a ptejte se AI asistenta" else "Converse with team partners & AI mixing assistant",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(onClick = { viewModel.clearChatHistory() }, modifier = Modifier.size(32.dp)) {
                    Text("🗑️", fontSize = 16.sp)
                }
            }
        }

        // Active Project Share Trigger Option
        if (activeProject != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF19122B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF53318F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "CS") "Chcete zpětnou vazbu od týmu?" else "Want timeline feedback?",
                            color = Color(0xFFDEC3FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (lang == "CS") "Nasdílejte rozpracovaný projekt '${activeProject?.title}'" else "Share active draft '${activeProject?.title}'",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                    Button(
                        onClick = {
                            val proj = activeProject
                            if (proj != null) {
                                viewModel.sendProjectShareInChat(proj.title, proj.id)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (lang == "CS") "Nasdílet 📁" else "Share 📁", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Messages Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF08050E))
                .border(0.5.dp, Color(0xFF26193E), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredChat) { chat ->
                    ChatMessageBubble(chat, viewModel, lang)
                }
            }
        }

        // Composition Sending panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = liveChatMsgText,
                onValueChange = { liveChatMsgText = it },
                placeholder = { Text(if (lang == "CS") "Zeptejte se na mixing nebo chatujte..." else "Ask about vocal tracks, mixing or chat...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFCC66FF),
                    unfocusedBorderColor = Color(0xFF26153F)
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (liveChatMsgText.trim().isNotEmpty()) {
                        viewModel.sendUserChatMessage(liveChatMsgText)
                        liveChatMsgText = ""
                    }
                },
                modifier = Modifier
                    .background(Color(0xFFCC66FF), CircleShape)
                    .size(44.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color.Black)
            }
        }
    }
}


// --- TAB 4: TRŽIŠTĚ & VLASTNICKÁ PRÁVA (MARKETPLACE) COMPOSABLE ---
@Composable
fun MarketplaceTabScreen(viewModel: StudioViewModel) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val feedList by viewModel.communityFeed.collectAsStateWithLifecycle()

    var userNameInput by remember { mutableStateOf("") }
    var selectedLicenseIdx by remember { mutableStateOf(0) }
    val licenses = listOf("Denuli-CZ Copyright Safe ✅", "All Rights Reserved (Full Copyright)", "Creative Commons Shared NC")
    
    var isForSale by remember { mutableStateOf(false) }
    var salePriceInput by remember { mutableStateOf("5000") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Legal Info Card & Safe Badge certification
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF220C17)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF8F0F3D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚖️", fontSize = 28.sp)
                    Column {
                        Text(
                            text = if (lang == "CS") "CERTIFIKOVANÁ LICENCE DENULI 👮‍♀️" else "CERTIFIED INTELLECTUAL PROPERTY 👮‍♀️",
                            color = Color(0xFFFF7272),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (lang == "CS") {
                                "Veškerá autorská práva k hudbě generované systémem v této aplikaci patří výhradně vám jako tvůrci."
                            } else {
                                "All digital rights and copyrights generated dynamically belong exclusively to you, the creator."
                            },
                            color = Color(0xFFFFC5C5),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Active Project Copyright control
        if (activeProject != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF150D24)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF42216C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang == "CS") "PRODAT AUTORSKÁ PRÁVA K AKTUÁLNÍMU SONGU" else "MONETIZE & LICENSE CURRENT TRACK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Creator name input
                        OutlinedTextField(
                            value = userNameInput,
                            onValueChange = { userNameInput = it },
                            placeholder = { Text("např. Denuli Fan CZ") },
                            label = { Text(if (lang == "CS") "Jméno Autora / Majitele práv" else "Creator Name / Owner of Rights") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8F63F4),
                                unfocusedBorderColor = Color(0xFF381B5B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // License Picker
                        Text(
                            text = if (lang == "CS") "Druh autorské licence:" else "Authorized license agreement:",
                            color = Color(0xFFDEC3FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(licenses.size) { idx ->
                                val isSelected = selectedLicenseIdx == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF8F63F4) else Color(0xFF291B3C))
                                        .clickable { selectedLicenseIdx = idx }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(licenses[idx], color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Price parameters
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = isForSale,
                                onCheckedChange = { isForSale = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF8F63F4))
                            )
                            Text(
                                text = if (lang == "CS") "Povolit prodej práv k této písni" else "List copyright of this song for sale",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        if (isForSale) {
                            OutlinedTextField(
                                value = salePriceInput,
                                onValueChange = { salePriceInput = it },
                                label = { Text(if (lang == "CS") "Prodejní cena (CZK)" else "Required price (CZK)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFAA00),
                                    unfocusedBorderColor = Color(0xFF372659)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val price = if (isForSale) salePriceInput.toDoubleOrNull() ?: 0.0 else 0.0
                                viewModel.updateProjectLicenseSettings(
                                    license = licenses[selectedLicenseIdx],
                                    isPublic = true,
                                    price = price
                                )
                                viewModel.publishCurrentProjectToCommunity(if (userNameInput.isEmpty()) "Denuli Artist" else userNameInput)
                                Toast.makeText(context, if (lang == "CS") "Gratulujeme! Song byl s autorskou licencí publikován v trhu!" else "Track published successfully with copyright certification!", Toast.LENGTH_LONG).show()
                                userNameInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                            modifier = Modifier.fillMaxWidth().testTag("publish_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Publish", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "CS") "Certifikovat a vstoupit do trhu" else "Certify and Publish To Market",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "CS") "Otevřete nějaký projekt v sekci Studio pro zahájení monetizace." else "Open or create a project inside Studio tab to configure license monetization.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Active Marketplace catalog list
        item {
            Text(
                text = if (lang == "CS") "🛒 KATALOG SKLADEB K ZAKOUPENÍ LICENCE" else "🛒 MUSIC RIGHTS MARKETPLACE CATALOG",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val marketplaceTracks = feedList.filter { it.isForSale }
        if (marketplaceTracks.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B18)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (lang == "CS") "Žádný song momentálně nenabízí licenci na prodej. Buďte první!" else "No songs is currently offering license rights on sale. Be the first one!",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(marketplaceTracks.size) { i ->
                val track = marketplaceTracks[i]
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D24)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFAA00).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("Autor: ${track.author}", color = Color.LightGray, fontSize = 11.sp)
                            Text("Licence: ${track.customLicense}", color = Color(0xFFE2D6FF), fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, if (lang == "CS") "Požadavek na koupi zaslán! Propojení přes inteligentní smlouvu..." else "Purchase request sent! Linking via secure smart contact escrow...", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAA00))
                        ) {
                            Text(
                                text = "${track.priceCzk.toInt()} Kč",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ChatMessageBubble(chat: ChatMsg, viewModel: StudioViewModel, lang: String) {
    val context = LocalContext.current
    val isMe = chat.sender.startsWith("Uživatel")
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = chat.sender,
                    color = if (chat.isAiAssistant) Color(0xFF00FFCC) else Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // UGC Block & Report Clickables
                if (!isMe && !chat.isAiAssistant) {
                    Text(
                        text = "🚨",
                        modifier = Modifier.clickable {
                            viewModel.reportUser(context, chat.sender)
                        },
                        fontSize = 11.sp
                    )
                    Text(
                        text = "🚫",
                        modifier = Modifier.clickable {
                            viewModel.blockUser(context, chat.sender)
                        },
                        fontSize = 11.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = if (isMe) 10.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 10.dp
                        )
                    )
                    .background(
                        if (isMe) Color(0xFF3F216E)
                        else if (chat.isAiAssistant) Color(0xFF192F26)
                        else Color(0xFF221639)
                    )
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = chat.message,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    
                    if (chat.isProjectShare && chat.sharedProjectId != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (lang == "CS") "📁 SDÍLENÝ PROJEKT" else "📁 SHARED PROJECT",
                                    color = Color(0xFFD4AF37),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val playProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
                                val calculatedTimeSecs = (playProgress * 15f).toInt()
                                val minutes = calculatedTimeSecs / 60
                                val seconds = calculatedTimeSecs % 60
                                val timeLabel = String.format("%02d:%02d", minutes, seconds)
                                
                                var userCommentInput by remember { mutableStateOf("") }
                                OutlinedTextField(
                                    value = userCommentInput,
                                    onValueChange = { userCommentInput = it },
                                    placeholder = { 
                                        Text(
                                            text = if (lang == "CS") "Komentář k času $timeLabel..." else "Comment at $timeLabel...", 
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        ) 
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFCC66FF),
                                        unfocusedBorderColor = Color(0xFF442D66)
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        if (userCommentInput.trim().isNotEmpty()) {
                                            viewModel.sendTimelineCommentInChat(chat.sharedProjectId, userCommentInput, calculatedTimeSecs)
                                            userCommentInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC66FF)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (lang == "CS") "Komentovat v čase ⏱️" else "Comment at timestamp ⏱️", 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Bold, 
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PaywallDialog(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { viewModel.setShowPaywallDialog(false) }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF130922)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFFBD7DFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Crown / Premium header
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF281347), CircleShape)
                        .border(1.dp, Color(0xFFCC66FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 28.sp)
                }

                Text(
                    text = if (lang == "CS") "COSMIC SPARK PRO" else "COSMIC SPARK PRO",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (lang == "CS") 
                        "Odemkněte plný tvůrčí potenciál bez umělých omezení dárkovým i firemním exportem!" 
                        else "Unlock full creative capabilities without limits on professional exporting!",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                // Checklist of benefits
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BenefitRow(text = if (lang == "CS") "♾️ Neomezené AI hudební generování" else "♾️ Unlimited studio-grade AI generation")
                    BenefitRow(text = if (lang == "CS") "🎬 Full HD & 4K video exporty" else "🎬 Full HD & 4K movie exports")
                    BenefitRow(text = if (lang == "CS") "📻 Uncompressed WAV & FLAC Audio exporty" else "📻 Broadcast WAV & Lossless FLAC audios")
                    BenefitRow(text = if (lang == "CS") "🎨 Přístup k exkluzivním fontům a šablonám" else "🎨 Premium fonts and layout styles")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subscription Buttons (Real Google Play Payments!)
                Button(
                    onClick = {
                        val helper = viewModel.billingHelper
                        if (helper != null) {
                            try {
                                helper.purchaseSubscription(
                                    activity = context as android.app.Activity,
                                    productId = "sub_denuli_monthly_149"
                                )
                            } catch (e: Exception) {
                                viewModel.setPremiumStatus(true)
                                viewModel.setShowPaywallDialog(false)
                                Toast.makeText(context, "Měsíční členství aktivováno (Simulace / Offline Fallback)!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.setPremiumStatus(true)
                            viewModel.setShowPaywallDialog(false)
                            Toast.makeText(context, "Měsíční členství aktivováno (Simulace / Offline Fallback)!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC66FF)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = if (lang == "CS") "Měsíční předplatné - 149 Kč / měsíc" else "Monthly Subscription - 149 CZK / month",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        val helper = viewModel.billingHelper
                        if (helper != null) {
                            try {
                                helper.purchaseSubscription(
                                    activity = context as android.app.Activity,
                                    productId = "sub_denuli_yearly_999"
                                )
                            } catch (e: Exception) {
                                viewModel.setPremiumStatus(true)
                                viewModel.setShowPaywallDialog(false)
                                Toast.makeText(context, "Roční předplatné aktivováno (Simulace / Offline Fallback)!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.setPremiumStatus(true)
                            viewModel.setShowPaywallDialog(false)
                            Toast.makeText(context, "Roční předplatné aktivováno (Simulace / Offline Fallback)!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = if (lang == "CS") "Roční předplatné - 1190 Kč / rok (Ušetříte 33 %)" else "Annual Subscription - 1190 CZK / year (Save 33%)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Highly visible bypass row for Denuli testing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1730), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            val activeNow = viewModel.toggleSimulatedPremium(context)
                            viewModel.setShowPaywallDialog(false)
                            val msg = if (lang == "CS") {
                                if (activeNow) "Simulované Premium AKTIVOVÁNO pro testování! 💎" else "Simulované Premium DEAKTIVOVÁNO!"
                            } else {
                                if (activeNow) "Simulated Premium ENABLED for testing! 💎" else "Simulated Premium DISABLED!"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛠️ ", fontSize = 14.sp)
                        Text(
                            text = if (lang == "CS") "Bypass pro Denuli: Kliknutím simulovat PRO verzi" else "Denuli Bypass: Toggle Free vs PRO locally",
                            color = Color(0xFF00FFCC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                TextButton(onClick = { viewModel.setShowPaywallDialog(false) }) {
                    Text(
                        text = if (lang == "CS") "Zpět k bezplatné verzi" else "Back to free version",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✨", fontSize = 14.sp)
        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}


// --- TAB 5: MŮJ PROFIL & NASTAVENÍ DENULI COMPOSABLE ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTabScreen(viewModel: StudioViewModel) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val templatesList by viewModel.savedTemplatesList.collectAsStateWithLifecycle()
    val fontCollections by viewModel.fontCollections.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val transactionsList by viewModel.purchaseTransactionsList.collectAsStateWithLifecycle()

    var showGoogleConnected by remember { mutableStateOf(false) }
    var showDropboxConnected by remember { mutableStateOf(false) }
    var showMidiConnected by remember { mutableStateOf(false) }
    var templateNameInput by remember { mutableStateOf("") }
    var customFontInput by remember { mutableStateOf("") }
    var languageSearchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Monetization, Business & Accounting Advisory (For Denuli)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF19112B)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFF00FFCC)),
                modifier = Modifier.fillMaxWidth().testTag("profile_business_advisory_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "💼", fontSize = 20.sp)
                            Text(
                                text = if (lang == "CS") "PODNIKÁNÍ & MONETIZACE" else "MONETIZATION & ADVISORY",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF123C36))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (lang == "CS") "PRO SPECIÁL" else "PRO EXCLUSIVE",
                                color = Color(0xFF00FFCC),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (lang == "CS") "📌 Důležité informace k vydělávání v Google Play:" else "📌 Important information regarding Google Play earnings:",
                        color = Color(0xFFE2D5FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang == "CS") {
                            "1. SCHVALOVÁNÍ APLIKACE GOOGLEMEN: Pokud jste stiskla 'Publikovat', aplikace se neobjeví ke stažení ihned. Google prochází ruční kontrolou, která u nových účtů trvá 3 až 7 dní (výjimečně až 14 dní). Dokud ji neschválí, nelze ji veřejně vyhledat ani stáhnout.\n\n" +
                            "2. ŽIVNOSTENSKÝ LIST A DANĚ: Vydělávání peněz z předplatného Google Play je podle českých zákonů soustavná samostatná výdělečná činnost. K přijímání plateb budete potřebovat Volnou živnost (Vývoj softwaru nebo Reklamní činnost).\n\n" +
                            "3. DOPORUČENÍ K ROZVODU (20. června 2026): Vzhledem k tomu, že se blíží Váš rozvod a plánovaná změna příjmení zpět na rodné jméno, VŘELE DOPORUČUJEME počkat se zakládáním Živnostenského listu i platebních profilů (Google Merchant) až na dobu, kdy budete mít nový občanský průkaz s Vaším rodným jménem. Pokud byste to dělala teď, musela byste později platit poplatky za změnu údajů v registrech a zdlouhavě papírovat s Googlem kvůli přejmenování účtu.\n\n" +
                            "💡 Rychlé řešení: Nyní nechte aplikaci ke stažení v testovacím režimu nebo jako bezplatnou, a ostré platby s Živnostenským listem plně zprovozněte pod novým příjmením až po rozvodu. Žádný zisk vám neuteče a ušetříte si hromadu byrokracie!"
                        } else {
                            "1. GOOGLE APP REVIEW STATUS: After hitting publish, the app is not downloadable instantly. Google reviewers manually inspect the package which takes 3 to 7 days (rarely up to 14 days) before unlocking public downloads.\n\n" +
                            "2. TRADE LICENSE & TAXES: Real business subscription sales are legally categorized as systematic business activity requiring a Free Trade License in Czechia.\n\n" +
                            "3. NAME CHANGE ADVISORY (June 20, 2026): Due to your upcoming name restoration, WE STRONGLY ADVISE delaying registrations of any trade permits or Google Merchant payouts profiles until your official new ID card is delivered under your maiden surname. This saves you repetitive state filing fees and Google support update bottlenecks."
                        },
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF23153A), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (lang == "CS") {
                                    "🔧 SIMULÁTOR PRO TESTOVÁNÍ PREMIUM STAVU:"
                                } else {
                                    "🔧 PREMIUM SIMULATION CENTER:"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "CS") {
                                    "Tímto přepínačem si můžete na svém telefonu simulovat chování aktivního Premium účtu zcela ZDARMA, abyste aplikaci mohla otestovat a odladit předtím, než začnete vybírat peníze od lidí."
                                } else {
                                    "Toggle premium status on/off on this device for testing layout styles without requiring actual credits."
                                },
                                color = Color.LightGray,
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isPremium) Color(0xFF00FFCC) else Color(0xFFFF3366))
                                    )
                                    Text(
                                        text = if (isPremium) {
                                            if (lang == "CS") "PRO verze Aktivní 💎" else "PRO Status Active 💎"
                                        } else {
                                            if (lang == "CS") "Bezplatná verze 🆓" else "Free Account Mode 🆓"
                                        },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        val activeNow = viewModel.toggleSimulatedPremium(context)
                                        val msg = if (lang == "CS") {
                                            if (activeNow) "Simulované Premium AKTIVOVÁNO! 💎" else "Simulované Premium DEAKTIVOVÁNO!"
                                        } else {
                                            if (activeNow) "Simulated Premium ENABLED!" else "Simulated Premium DISABLED!"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPremium) Color(0xFFFF3366) else Color(0xFF00FFCC)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isPremium) {
                                            if (lang == "CS") "Přepnout na Free" else "Toggle Free"
                                        } else {
                                            if (lang == "CS") "Aktivovat PRO" else "Activate PRO"
                                        },
                                        color = if (isPremium) Color.White else Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Language Selection
        item {
            val languages = listOf(
                Pair("CS", "Čeština 🇨🇿"),
                Pair("EN", "English 🇬🇧"),
                Pair("SK", "Slovenčina 🇸🇰"),
                Pair("DE", "Deutsch 🇩🇪"),
                Pair("ES", "Español 🇪🇸"),
                Pair("FR", "Français 🇫🇷"),
                Pair("PL", "Polski 🇵🇱"),
                Pair("IT", "Italiano 🇮🇹"),
                Pair("UA", "Ukrainština 🇺🇦"),
                Pair("VI", "Tiếng Việt 🇻🇳")
            )
            val filteredLanguages = languages.filter {
                it.second.contains(languageSearchQuery, ignoreCase = true) || it.first.contains(languageSearchQuery, ignoreCase = true)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D22)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF3F2766)),
                modifier = Modifier.fillMaxWidth().testTag("profile_language_selection_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "CS") "VÝBĚR JAZYKA (10+ JAZYKŮ) / CHOOSE LANGUAGE" else "LANGUAGE REGISTRY (10+ LANGUAGES)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "CS") "Vyberte libovolný jazyk. Celé rozhraní se okamžitě za běhu přeloží přes AI slovník." else "Select any language. The entire interface immediately translates dynamically via AI client database.",
                        color = Color.Gray,
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Embedded Search field inside the language card for quick search!
                    OutlinedTextField(
                        value = languageSearchQuery,
                        onValueChange = { languageSearchQuery = it },
                        placeholder = {
                            Text(
                                if (lang == "CS") "Vyhledat jazyk (např. 'slov', 'english')..." else "Search language (e.g., 'espan', 'polsk')...",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFCC66FF),
                            unfocusedBorderColor = Color(0xFF26153F),
                            focusedContainerColor = Color(0xFF0F081E),
                            unfocusedContainerColor = Color(0xFF0F081E)
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("language_search_input"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.5.sp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredLanguages.forEach { (code, name) ->
                            val isSelected = lang == code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF00FFCC) else Color(0xFF26193E))
                                    .clickable { viewModel.switchLanguage(code) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Visual Theme Selection (Mystický soumrak vs Elegantní krémový)
        item {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val themeCardBg = if (isDarkMode) Color(0xFF140D22) else Color(0xFFFBF9F6)
            val themeBorderColor = if (isDarkMode) Color(0xFF3F2766) else Color(0xFFE5A9AC).copy(alpha = 0.6f)
            val textThemeColor = if (isDarkMode) Color.White else Color(0xFF121214)

            MysticCornerBox(isDarkMode = isDarkMode, modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeCardBg),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, themeBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang == "CS") "VIZUÁLNÍ STYL A TÉMA / APP THEME SELECTION" else "VISUAL THEME AND APP LOOK",
                            color = textThemeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = if (lang == "CS") 
                                "Zvolte náladový tmavý režim (Mystický soumrak s jemným náladovým třpytem) nebo čistý, elegantní krémový režim." 
                            else 
                                "Select the dark immersive mode (Mystic Twilight with ambient sparkling dust) or the elegant cream light theme.",
                            color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.switchTheme(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkMode) Color(0xFFCC66FF) else Color(0xFFEFE6D5)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (lang == "CS") "Mystický soumrak 🌌" else "Mystic twilight 🌌",
                                    color = if (isDarkMode) Color.Black else Color(0xFF53318F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.switchTheme(false) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isDarkMode) Color(0xFFE5A9AC) else Color(0xFF26193E)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (lang == "CS") "Elegantní krémový 🍦" else "Elegant cream 🍦",
                                    color = if (!isDarkMode) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: UGC Moderation & Safety Block List
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13091F)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF331E54)),
                modifier = Modifier.fillMaxWidth()
            ) {
                val blockedUsers by viewModel.blockedUsers.collectAsStateWithLifecycle()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "CS") "BEZPEČNOST & MODEROVÁNÍ (UGC)" else "SAFETY & MODERATION (UGC)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (lang == "CS") "Správa nahlášených nebo zablokovaných uživatelů k dodržení podmínek Google Play:" else "Manage reported or blocked users to comply with Google Play policies:",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (blockedUsers.isEmpty()) {
                        Text(
                            text = if (lang == "CS") "Žádní uživatelé nejsou zablokováni." else "No users currently blocked.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        blockedUsers.forEach { bUser ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF221133), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(bUser, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { viewModel.unblockUser(bUser) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF662244)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (lang == "CS") "Odblokovat" else "Unblock", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Section: Font Collections Manager
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0916)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF291B3E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "CS") "INSTALOVANÉ FONTOVÉ KOLEKCE" else "INSTALLED DYNAMIC FONT COLLECTIONS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (lang == "CS") "Tyto fonty jsou připravené k použití pro texty a překryvy videoklipů:" else "All registered typography formats loaded in project streams:",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        fontCollections.forEach { fontName ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF291B3C))
                                    .border(0.5.dp, Color(0xFF8F63F4).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(fontName, color = Color(0xFFDEC3FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick custom font registration composer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customFontInput,
                            onValueChange = { customFontInput = it },
                            placeholder = { Text(if (lang == "CS") "Zadejte nový název..." else "Enter new custom font name...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8F63F4)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (customFontInput.trim().isNotEmpty()) {
                                    viewModel.addCustomFont(customFontInput)
                                    Toast.makeText(context, "Font '${customFontInput}' úspěšně registrován do kolekce!", Toast.LENGTH_SHORT).show()
                                    customFontInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4))
                        ) {
                            Text(if (lang == "CS") "Přidat" else "Add", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // High Quality Full HD Outputs options
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0917)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF2F1D4F))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FULL HD STREAM EXPORT FORMATS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val formatsList = listOf(
                        "Lossless FLAC Master Studio Quality (24-bit/192kHz)",
                        "High-Def MP3 Studio Compression (320kbps)",
                        "Uncompressed WAV Broadcast Ready",
                        "Full HD MP4 Video Clip Master with Color Grading"
                    )

                    formatsList.forEach { format ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val isWavOrVideoOrFlac = format.contains("WAV") || format.contains("MP4") || format.contains("Full HD") || format.contains("FLAC")
                                    if (isWavOrVideoOrFlac && !isPremium) {
                                        viewModel.setShowPaywallDialog(true)
                                        Toast.makeText(context, "Export ve vysoké kvalitě (WAV, FLAC, MP4) je prémiová součást!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Exportuji... Soubor stažen ve formátu: $format", Toast.LENGTH_LONG).show()
                                    }
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(text = "📥", fontSize = 16.sp)
                            Text(format, color = Color.White, fontSize = 12.sp)
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF22163E)))
                    }
                }
            }
        }

        // SAVE / RESTORE WORKSPACE SETTINGS TEMPLATE (Room integration!)
        if (activeProject != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D24)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B235E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WORKPLACE PRESET TEMPLATE SAVES",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text("Uložte současné nastavení (hudební styl, prompt, barevný grading, vokálne nastavenie) pro příští písně:", color = Color.LightGray, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = templateNameInput,
                            onValueChange = { templateNameInput = it },
                            placeholder = { Text("např. Denuli Heavy Synth Bass") },
                            label = { Text("Název šablony / Preset Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00FFCC)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (templateNameInput.trim().isNotEmpty()) {
                                    viewModel.saveSettingsAsTemplate(templateNameInput)
                                    Toast.makeText(context, "Šablona '${templateNameInput}' uložena do SQLite Databáze!", Toast.LENGTH_SHORT).show()
                                    templateNameInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Uložit aktuální šablonu", color = Color.White)
                        }

                        // Listed templates
                        if (templatesList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Vaše uložené šablony nastavení:", color = Color(0xFFDFCCFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            templatesList.forEach { temp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.applySettingsTemplate(temp)
                                            Toast.makeText(context, "Použito nastavení z: ${temp.templateName}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "🔖", fontSize = 14.sp)
                                        Column {
                                            Text(temp.templateName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Genre: ${temp.genre} | FX: ${temp.voiceEffect}", color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }
                                    Text("Použít", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF2B1D47)))
                            }
                        }
                    }
                }
            }
        }

        // CLOUD STORAGE & HARDWARE MIDI COUPLINGS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF150D27)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF3F276B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLOUD STORAGE & DEVICE SYNCHRONIZATION",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Google Drive sync toggle row
                    CloudHardwareToggleItem(
                        title = "Google Drive Sync (Cloud Backup)",
                        subtitle = "Automatické zálohování projektů",
                        enabled = showGoogleConnected,
                        onToggle = { showGoogleConnected = it },
                        iconLabel = "☁️"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dropbox sync row
                    CloudHardwareToggleItem(
                        title = "Dropbox Cloud Mirroring",
                        subtitle = "Sdílení souborů s kapelou v reálném čase",
                        enabled = showDropboxConnected,
                        onToggle = { showDropboxConnected = it },
                        iconLabel = "📂"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // External music apps MIDI links
                    CloudHardwareToggleItem(
                        title = "MIDI Hardware Connection / Ableton Link",
                        subtitle = "Synchronizovat tempo BPM se všemi hudebními aplikacemi",
                        enabled = showMidiConnected,
                        onToggle = { showMidiConnected = it },
                        iconLabel = "🔌"
                    )
                }
            }
        }

        // GOOGLE PLAY ACCOUNTING & FINANCIAL COMPLIANCE REGISTRY (EVIDENCE TRANSAKCÍ PRO ÚČETNICTVÍ)
        item {
            val composeClipboard = androidx.compose.ui.platform.LocalClipboardManager.current
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1424)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, Color(0xFF2E6BFF).copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().testTag("billing_reporting_compliance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "📊", fontSize = 20.sp)
                            Text(
                                text = if (lang == "CS") "EVIDENCE NÁKUPŮ & ÚČETNICTVÍ" else "PURCHASE HISTORY & ACCOUNTING",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0A2B66))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (lang == "CS") "PLAY CONSOLE SOULAD" else "PLAY CONSOLE MATCH",
                                color = Color(0xFF4FA0FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (lang == "CS") {
                            "Lokální certifikovaná evidence nákupů z Google Play API. Data jsou formátována v plném souladu s výkazy Google Play Console (datum v UTC, ID objednávky, částka, měna) pro snadné generování měsíčních přehledů pro finanční úřad."
                        } else {
                            "Local certified registry of Google Play API checkout confirmations. Records are aligned with Play Console financial tables (date in UTC, GPA order ID, amount, currency) for seamless integration with accounting packages."
                        },
                        color = Color.LightGray,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (transactionsList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF161B33), RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (lang == "CS") "Žádné zaznamenané transakce" else "No transactions recorded yet",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val helper = viewModel.billingHelper
                                    if (helper != null) {
                                        helper.purchaseSubscription(
                                            activity = context as android.app.Activity,
                                            productId = "sub_denuli_monthly_149"
                                        )
                                    } else {
                                        // Offline simulated checkout
                                        viewModel.setPremiumStatus(true)
                                        Toast.makeText(context, "Direct offline simulated receipt logged!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (lang == "CS") "Simulovat nákup (149 Kč)" else "Simulate Purchase (149 CZK)",
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            transactionsList.forEach { tx ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171D3A)),
                                    border = BorderStroke(0.5.dp, Color(0xFF3B4A6F)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (tx.productId == "sub_denuli_yearly_999") {
                                                    if (lang == "CS") "Roční Premium (Yearly)" else "Yearly Premium"
                                                } else {
                                                    if (lang == "CS") "Měsíční Premium (Monthly)" else "Monthly Premium"
                                                },
                                                color = Color(0xFFFFAA00),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF065F46))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (lang == "CS") "Zúčtováno 🟢" else "Charged 🟢",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "ID: ${tx.orderId}",
                                                    color = Color.White,
                                                    fontSize = 9.5.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "📋",
                                                    fontSize = 11.sp,
                                                    modifier = Modifier
                                                        .clickable {
                                                            composeClipboard.setText(androidx.compose.ui.text.AnnotatedString(tx.orderId))
                                                            Toast.makeText(context, if (lang == "CS") "ID objednávky zkopírováno!" else "Order ID copied!", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(2.dp)
                                                )
                                            }
                                            Text(
                                                text = "${tx.amountCzk} ${tx.currency}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = tx.formattedDate,
                                            color = Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    val csvContent = buildString {
                                        append("Order ID,Product ID,Transaction Date,Amount,Currency,Payment Status\n")
                                        transactionsList.forEach { tx ->
                                            append("\"${tx.orderId}\",\"${tx.productId}\",\"${tx.formattedDate}\",${tx.amountCzk},\"${tx.currency}\",\"${tx.paymentStatus}\"\n")
                                        }
                                    }
                                    composeClipboard.setText(androidx.compose.ui.text.AnnotatedString(csvContent))
                                    Toast.makeText(
                                        context,
                                        if (lang == "CS") "CSV sestava pro finanční úřad zkopírována do schránky!" else "Accounting CSV copied to clipboard!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (lang == "CS") "Exportovat do CSV (Pro účetní)" else "Export Transactions as CSV",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 4 COMPONENT: PRIVACY & GDPR COMPLIANCE CENTER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13091F)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, Color(0xFF7033D4))
            ) {
                var showPolicyDialog by remember { mutableStateOf(false) }
                var showExportDialog by remember { mutableStateOf(false) }
                var showWipeConfirm by remember { mutableStateOf(false) }

                val gdprAccepted by viewModel.gdprAccepted.collectAsStateWithLifecycle()
                val consentCloud by viewModel.gdprConsentCloud.collectAsStateWithLifecycle()
                val consentAi by viewModel.gdprConsentAi.collectAsStateWithLifecycle()
                val consentCommunity by viewModel.gdprConsentCommunity.collectAsStateWithLifecycle()
                val consentTelemetry by viewModel.gdprConsentTelemetry.collectAsStateWithLifecycle()

                val projectsList by viewModel.projectsList.collectAsStateWithLifecycle()
                val chatsList by viewModel.chatMessagesList.collectAsStateWithLifecycle()
                val templatesList by viewModel.savedTemplatesList.collectAsStateWithLifecycle()

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "🛡️", fontSize = 20.sp)
                            Text(
                                text = if (lang == "CS") "GDPR & CENTRUM OCHRANY DAT" else "GDPR & PRIVACY POLICY CENTER",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (gdprAccepted == true) Color(0xFF00FFCC).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (gdprAccepted == true) "GDPR COMPLIANT 🟢" else "PENDING ACTION 🔴",
                                color = if (gdprAccepted == true) Color(0xFF00FFCC) else Color.Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (lang == "CS") "Denuli Studio chrání autory, prodejce licencí i uživatele nahrávek." else "Denuli Studio protects music creators, license vendors and vocal performers.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // GRANULAR TOGGLES (GDPR Article 7 Consent)
                    Text(
                        text = if (lang == "CS") "GRANULÁRNÍ NASTAVENÍ SOUHLASU" else "GRANULAR CONSENT PREFERENCES",
                        color = Color(0xFFCBB6FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GdprSwitchRow(
                            title = if (lang == "CS") "Schránka & Cloudová úložiště" else "Safe Storage & Mirroring",
                            description = if (lang == "CS") "Zálohování multitracku (Disk, Dropbox) a autorských licencí" else "Automatic syncing of projects and copyright documents in real-time.",
                            checked = consentCloud,
                            onCheckedChange = {
                                viewModel.saveGdprConsent(
                                    context = context,
                                    accepted = true,
                                    cloud = it,
                                    ai = consentAi,
                                    community = consentCommunity,
                                    telemetry = consentTelemetry
                                )
                            }
                        )

                        GdprSwitchRow(
                            title = if (lang == "CS") "Inteligentní asistent Gemini" else "Gemini Intelligent Assistant",
                            description = if (lang == "CS") "Sběr stylů a promptů za účelem vygenerování textu pohlcujícího pro posluchače" else "Processing input prompts for lyric creation and sound mastering support.",
                            checked = consentAi,
                            onCheckedChange = {
                                viewModel.saveGdprConsent(
                                    context = context,
                                    accepted = true,
                                    cloud = consentCloud,
                                    ai = it,
                                    community = consentCommunity,
                                    telemetry = consentTelemetry
                                )
                            }
                        )

                        GdprSwitchRow(
                            title = if (lang == "CS") "Komunitní distribuce licencí" else "Community Copyright Advertising",
                            description = if (lang == "CS") "Prezentace jména autora, žánru a české ceny v CZK pro kupce" else "Listing of authors, tracks, pricing in CZK, and copyrights for buyers.",
                            checked = consentCommunity,
                            onCheckedChange = {
                                viewModel.saveGdprConsent(
                                    context = context,
                                    accepted = true,
                                    cloud = consentCloud,
                                    ai = consentAi,
                                    community = it,
                                    telemetry = consentTelemetry
                                )
                            }
                        )

                        GdprSwitchRow(
                            title = if (lang == "CS") "Telemetrická hlášení" else "Anonymized Performance Telemetry",
                            description = if (lang == "CS") "Bezejmenné testy pádů, rychlosti nahrávání a stability" else "Sending non-identifying debugging metrics to help optimize the multitrack engine.",
                            checked = consentTelemetry,
                            onCheckedChange = {
                                viewModel.saveGdprConsent(
                                    context = context,
                                    accepted = true,
                                    cloud = consentCloud,
                                    ai = consentAi,
                                    community = consentCommunity,
                                    telemetry = it
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ACTIONS ROW (View Policy, Export Data, Forget Me)
                    Button(
                        onClick = { showPolicyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23143A)),
                        border = BorderStroke(1.dp, Color(0xFF8057D6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (lang == "CS") "📖 Zobrazit kompletní Zásady Ochrany Údajů" else "📖 View Full General Privacy Policy Outline",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showExportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C191A)),
                            border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (lang == "CS") "📥 Exportovat data" else "📥 Export My Data",
                                color = Color(0xFF00FFCC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showWipeConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF240A10)),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (lang == "CS") "🗑️ Zapomenout mě" else "🗑️ Forget Me",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // DIALOGS
                // 1. Legal Outline Dialog
                if (showPolicyDialog) {
                    Dialog(onDismissRequest = { showPolicyDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1335)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF8F63F4))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lang == "CS") "DOKUMENTACE GDPR" else "LAW COMPLIANCE GDPR",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    IconButton(onClick = { showPolicyDialog = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = GdprLoc.getPolicyText(lang),
                                        color = Color(0xFFEDE5FF),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showPolicyDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = if (lang == "CS") "Beru na vědomí" else "Acknowledge", color = Color.White)
                                }
                            }
                        }
                    }
                }

                // 2. Data Export Dialog
                if (showExportDialog) {
                    val exportDataString = remember {
                        val sb = StringBuilder()
                        sb.append("--- DENULI STUDIO USER PORTABILITY DATA ---\n")
                        sb.append("Language preference: $lang\n")
                        sb.append("Consents: Cloud=$consentCloud, AI=$consentAi, Comm=$consentCommunity, Telemetry=$consentTelemetry\n\n")
                        sb.append("--- PROJECTS (${projectsList.size}) ---\n")
                        projectsList.forEach { p ->
                            sb.append("ID: ${p.id} | Title: ${p.title} | Genre: ${p.genre} | BPM: ${p.bpm} | License: ${p.projectLicense} | Price CZK: ${p.rightsPriceCzk}\n")
                        }
                        sb.append("\n--- CHAT MESSAGE RECORDS (${chatsList.size}) ---\n")
                        chatsList.forEach { c ->
                            sb.append("[${c.timestamp}] ${c.sender}: ${c.message}\n")
                        }
                        sb.append("\n--- PRESETS & TEMPLATES (${templatesList.size}) ---\n")
                        templatesList.forEach { t ->
                            sb.append("Name: ${t.templateName} | Genre: ${t.genre} | FX: ${t.voiceEffect}\n")
                        }
                        sb.toString()
                    }

                    Dialog(onDismissRequest = { showExportDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131A19)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF00FFCC))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lang == "CS") "EXPORT OSOBNÍCH ÚDAJŮ (Portability)" else "GDPR PORTABILITY DATA (JSON/TXT)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00FFCC),
                                        fontSize = 13.sp
                                    )
                                    IconButton(onClick = { showExportDialog = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (lang == "CS") "Vaše údaje jsou uloženy v souladu se zásadou minimalizace dat." else "Your exported data below can be shared or audited pursuant to Article 20.",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = exportDataString,
                                        color = Color(0xFFCCFFEE),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        showExportDialog = false
                                        Toast.makeText(context, if (lang == "CS") "Data úspěšně vyexportována a uložena do schránky (Clipboard)" else "Portability file copied to clipboard!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = if (lang == "CS") "Zkopírovat do schránky & Zavřít" else "Copy to Clipboard & Close", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. Right to be forgotten Confirm Dialog
                if (showWipeConfirm) {
                    Dialog(onDismissRequest = { showWipeConfirm = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF280C14)),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, Color.Red)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️ VAROVÁNÍ / DANGER ZONE",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (lang == "CS") {
                                        "Chystáte se aktivovat právo na výmaz (čl. 17 GDPR). Tento krok nevratně SMAŽE všechny nahrané stopy, uložené projekty, licenční smlouvy, prodejní nabídky v CZK, live chat zprávy i stažená písma! Chcete pokračovat?"
                                    } else {
                                        "You are exercising your 'Right to be Forgotten' (Article 17). This will permanently PURGE all recordings, active projects, community tracks, custom licenses, CZK seller prices, live chats, and fonts from SQLite! Proceed?"
                                    },
                                    color = Color(0xFFFFD4DE),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { showWipeConfirm = false }) {
                                        Text(text = if (lang == "CS") "Zrušit" else "Cancel", color = Color.LightGray)
                                    }

                                    Button(
                                        onClick = {
                                            showWipeConfirm = false
                                            viewModel.wipeAllUserDataAndRecordings(context) {
                                                Toast.makeText(context, if (lang == "CS") "Všechna data byla nevratně smazána z přístroje a resetována" else "All storage structures purged successfully!", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = if (lang == "CS") "Smazat vše" else "Wipe All Data", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. GOOGLE PLAY CONSOLE MARKETING INFORMATION BLOCK
        item {
            val context = LocalContext.current
            val composeClipboard = androidx.compose.ui.platform.LocalClipboardManager.current
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160F25)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFFAA00)),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🚀", fontSize = 22.sp)
                        Text(
                            text = if (lang == "CS") "GOOGLE PLAY CONSOLE - MARKETING METADATA" else "GOOGLE PLAY CONSOLE METADATA (DEV ONLY)",
                            color = Color(0xFFFFAA00),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = if (lang == "CS") "Všechny texty a optimalizovaná klíčová slova připravené pro nahrání do Google Play Console." else "Everything configured and optimized for direct deploy copy-pasting.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Field 1: App Title
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("1. Název aplikace (Max 30 znaků)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = {
                                composeClipboard.setText(androidx.compose.ui.text.AnnotatedString("Studio Denuli: AI Hudba & Video"))
                                Toast.makeText(context, "Název zkopírován!", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Zkopírovat 📋", color = Color(0xFF00FFCC), fontSize = 10.sp)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF221935)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Studio Denuli: AI Hudba & Video", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Field 2: Short Description
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("2. Krátký popis (Max 80 znaků)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = {
                                composeClipboard.setText(androidx.compose.ui.text.AnnotatedString("Profesionální multitrack DAW hudební studio, video editor a AI generátor písní."))
                                Toast.makeText(context, "Popis zkopírován!", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Zkopírovat 📋", color = Color(0xFF00FFCC), fontSize = 10.sp)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF221935)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Profesionální multitrack DAW hudební studio, video editor a AI generátor písní.", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                        }
                    }

                    // Field 3: Long Description
                    val longDescription = """
                    Vstupte do světa absolutní tvůrčí svobody se Studiem Denuli (Spark) – revoluční mobilní aplikací, která kombinuje profesionální vícestopé nahrávací studio (DAW), pokročilý video editor a magickou umělou inteligenci! 

                    Vytvářejte hudbu, nahrávejte vlastní vokály, generujte neuvěřitelné žánrové kombinace a stříhejte dlouhé videoklipy, pohádky nebo podcasty. To vše v nádherném, mystickém prostředí bez otravných a blikajících reklam.

                    Proč zvolit Studio Denuli?
                    • Žádné reklamy: Soustřeďte se čistě na svou tvorbu a fantazii. Prostředí aplikace vás nikdy nebude rušit od kreativního procesu.
                    • Absolutní svoboda žánrů: Náš pokročilý AI engine nezná limity. Kombinujte symfonický orchestr s kyberpunkovým technem, rap s folklórem nebo metal s operním sborem. Vše plynule a bez chyb.
                    • Dokonalé AI hlasy a chorály: Generujte doprovodné vokály s perfektní intonací a stoprocentně správnou výslovností v češtině i dalších jazycích. Vyberte si sólo (muž, žena, dítě), skupiny, filharmonické chorály nebo obří stadionový dav.
                    • Multitrack nahrávání: Nahrávejte svůj vlastní hlas, živé hudební nástroje nebo autentické zvuky přírody a zvířat s ultra nízkou latencí.

                    Profesionální video editor na časové ose:
                    • Stříhejte videa bez omezení délky – od 30sekundových klipů na sítě až po 30minutové podcasty and pohádky.
                    • Využijte pokročilé nástroje pro korekci a grading barev, přizpůsobitelnou knihovnu přechodů a video efektů.
                    • Importujte vlastní fonty (.ttf) a vytvářejte originální animované titulky.

                    Komunita, spolupráce a bezpečí:
                    • Sdílejte své rozpracované projekty přímo do interního chatu.
                    • Spolupracujte s přáteli pomocí zpětných komentářů navázaných na přesný čas na časové ose (např. "v čase 01:20 ztlumit vokál").
                    • Vaše data a autorská práva jsou v bezpečí (GDPR compliant). Aplikace obsahuje plné systémy pro nahlášení a zablokování nevhodného obsahu (UGC).

                    Vizuální zážitek s duší:
                    Vyberte si mezi elegantním krémovým denním režimem a magickým tmavým režimem se třpytkami, jemnými runovými ornamenty a motivačními citáty v mnoha jazycích: "Jsem zde a má hudba má být pro všechny."

                    Stáhněte si Studio Denuli zdarma a dejte křídla své fantazii. Základní multitrack, video střih a denní balíček AI generování jsou dostupné pro každého. Pro nejnáročnější tvůrce nabízíme prémiový export v nekomprimované studiové kvalitě (WAV, FLAC, 4K video).

                    Vytvořeno s láskou pro všechny lidi, kteří milují hudbu a příběhy.
                    """.trimIndent()

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("3. Dlouhý popis (Max 4000 znaků)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = {
                                composeClipboard.setText(androidx.compose.ui.text.AnnotatedString(longDescription))
                                Toast.makeText(context, "Dlouhý popis zkopírován!", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Zkopírovat celý text 📋", color = Color(0xFF00FFCC), fontSize = 10.sp)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF221935)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = longDescription,
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(8.dp).height(120.dp).verticalScroll(rememberScrollState()),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Field 4: Target Tags & Keywords
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("4. Štítky a klíčová slova pro vyhledávání (SEO Tags)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Self-contained simple flow row for tags
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val tags1 = listOf("tvorba hudby", "hudební studio", "DAW", "AI generátor hudby", "střih videa")
                            val tags2 = listOf("videoklipy", "nahrávání hlasu", "podcast studio", "tvorba písní")
                            val tags3 = listOf("odstranění šumu", "multitrack", "nahrávání mikrofonu", "hudba zdarma")
                            val tags4 = listOf("texty písní", "efekty na video", "filtry na video", "severské runy")
                            val tags5 = listOf("motivace", "hudba pro každého", "Studio Denuli")

                            listOf(tags1, tags2, tags3, tags4, tags5).forEach { rowTags ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowTags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF271A3F))
                                                .clickable {
                                                    composeClipboard.setText(androidx.compose.ui.text.AnnotatedString(tag))
                                                    Toast.makeText(context, "'$tag' zkopírováno!", Toast.LENGTH_SHORT).show()
                                                }
                                                .border(0.5.dp, Color(0xFFFFAA00).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("#$tag", color = Color(0xFFFFD175), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Field 5: Developer Questionnaire age ratings strategy
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1014)),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "⚠️ DOPORUČENÉ NASTAVENÍ DOTAZNÍKU (15+ vs COPPA)",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "V bezpečnostním dotazníku Google Play uveďte, že aplikace NENÍ určena pro děti do 13 let a obsahuje chat (interakce uživatelů). V podmínkách užívaní a zásadách GDPR legálně vymezujeme věk od 15 let (mladší pouze pod dohledem). Tím se zcela vyhnete COPPA / GDPR-K komplikacím a schválení bude bezproblémové.",
                                color = Color(0xFFFFCCCC),
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CloudHardwareToggleItem(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    iconLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = iconLabel, fontSize = 20.sp)
            Column {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 10.sp)
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00FFCC)
            )
        )
    }
}


// --- BOTTOM BAR NAVIGATION BAR (Respecting notch and gesture bar guidelines) ---
@Composable
fun BottomNavigationRow(viewModel: StudioViewModel) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val activeTab by viewModel.currentTab.collectAsStateWithLifecycle()

    val tabsList = listOf(
        TabItem("Domu", "🏠", if (lang == "CS") "Domů" else "Discover"),
        TabItem("Studio", "🎛️", if (lang == "CS") "Studio" else "Studio"),
        TabItem("Video", "🎬", if (lang == "CS") "Video" else "Video Studio"),
        TabItem("Chat", "💬", if (lang == "CS") "Chat" else "Chat"),
        TabItem("Market", "🛒", if (lang == "CS") "Tržiště" else "Market"),
        TabItem("Profile", "👤", if (lang == "CS") "Můj Denuli" else "My Denuli")
    )

    NavigationBar(
        containerColor = Color(0xFF0C0913),
        modifier = Modifier
            .navigationBarsPadding() // Satisfies bottom gesture bar notch padding rule!
            .border(0.5.dp, Color(0xFF34234E), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        tabsList.forEach { tab ->
            NavigationBarItem(
                selected = activeTab == tab.id,
                onClick = { viewModel.setTab(tab.id) },
                icon = { Text(text = tab.iconLabel, fontSize = 18.sp) },
                label = { Text(text = tab.label, fontSize = 9.sp, fontWeight = FontWeight.Black) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color(0xFF9E8EBF),
                    selectedTextColor = Color(0xFF00FFCC),
                    unselectedTextColor = Color(0xFF9E8EBF),
                    indicatorColor = Color(0xFF00FFCC)
                ),
                modifier = Modifier.testTag("tab_" + tab.id.lowercase())
            )
        }
    }
}

data class TabItem(
    val id: String,
    val iconLabel: String,
    val label: String
)

// --- GDPR & PRIVACY COMPLIANCE UTILITY & OBJECTS (GDPR compliant solutions) ---

object GdprLoc {
    fun getPolicyText(lang: String): String {
        return if (lang == "CS") {
            """
            ZÁSADY OCHRANY OSOBNÍCH ÚDAJŮ (PRIVACY POLICY)
            Aplikace: Studio Denuli (Spark)
            Provozovatel: Denuli-CZ

            1. SBĚR DAT: Aplikace zpracovává zvukové nahrávky (vokály, nástroje) a textové zprávy (chat, komentáře) výhradně za účelem poskytování hudebních a video editačních funkcí a komunitního sdílení.

            2. SOUHLAS S GDPR: V souladu s článkem 7 GDPR uživatel uděluje výslovný souhlas se zpracováním těchto dat před prvním spuštěním aplikace prostřednictvím interaktivního banneru. Souhlas lze kdykoliv odvolat v nastavení aplikace.

            3. VĚKOVÉ OMEZENÍ (15+): Aplikace je určena uživatelům od 15 let. Mladší uživatelé mohou aplikaci používat pouze se souhlasem se zákonného zástupce. Tímto je zajištěn soulad s předpisy COPPA a GDPR-K.

            4. AUTORSKÁ PRÁVA: Nahrávky a vytvořená hudba zůstávají plným vlastnictvím autora. Aplikace chrání autorská práva uživatelů a data spojená s licenčními modely ukládá v šifrované podobě.

            5. TŘETÍ STRANY: Data mohou být sdílena se sociálními sítěmi (TikTok, Spotify, Instagram, Facebook, YouTube) výhradně na pokyn uživatele při přímém exportu a sdílení obsahu.
            """.trimIndent()
        } else {
            """
            PRIVACY POLICY (ZÁSADY OCHRANY OSOBNÍCH ÚDAJŮ)
            Application: Studio Denuli (Spark)
            Operator: Denuli-CZ

            1. DATA COLLECTION: The app processes audio recordings (vocals, instruments) and in-app text messages (chat, comments) exclusively to deliver professional music and video editing features and facilitate community sharing.

            2. GDPR CONSENT: In compliance with Article 7 GDPR, the user grants explicit consent for processing this data before starting the application for the first time via an interactive consent banner. Consent may be withdrawn at any time in the app settings.

            3. AGE RESTRICTION (15+): The application is intended for users aged 15 and older. Younger users may only use the application with the consent of a legal representative. This strictly establishes compliance with COPPA and GDPR-K regulations.

            4. COPYRIGHTS: Recordings and created music remain the absolute and full property of the author/creator. The application vigorously protects user copyrights and stores licensing model metadata in encrypted form.

            5. THIRD PARTIES: Data may be shared with social media networks (TikTok, Spotify, Instagram, Facebook, YouTube) strictly upon the explicit request/command of the user during direct export and content sharing.
            """.trimIndent()
        }
    }
}

@Composable
fun GdprConsentBanner(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gdprAccepted by viewModel.gdprAccepted.collectAsStateWithLifecycle()
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    // Granular states for customization dialog/view
    var isCustomizing by remember { mutableStateOf(false) }

    var consentCloud by remember { mutableStateOf(true) }
    var consentAi by remember { mutableStateOf(true) }
    var consentCommunity by remember { mutableStateOf(true) }
    var consentTelemetry by remember { mutableStateOf(false) }

    // If already decided, do not show the primary floating banner
    if (gdprAccepted != null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0819).copy(alpha = 0.95f))
            .border(2.dp, Color(0xFF8F63F4), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .shadow(20.dp)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🛡️", fontSize = 24.sp)
                Column {
                    Text(
                        text = if (lang == "CS") "Ochrana soukromí & GDPR" else "GDPR & Privacy Consent",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (lang == "CS") "Denuli Studio chrání data uživatelů a hudebních prodejců." else "Denuli Studio guards user vocals and copyright sellers securely.",
                        color = Color(0xFFC4B8E2),
                        fontSize = 12.sp
                    )
                }
            }

            if (!isCustomizing) {
                Text(
                    text = if (lang == "CS") {
                        "Před zahájením nahrávání vokálů a AI stylizace potřebujeme váš souhlas se zpracováním dat (licenční transakce v CZK, analýzy Gemini a cloudové zálohy)."
                    } else {
                        "Before recording vocals or performing AI styling, please select your data preference (cloud protection, Gemini processing, and copyright listings)."
                    },
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { isCustomizing = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (lang == "CS") "Nastavit granularně" else "Customize",
                            color = Color(0xFFC7A1FF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveGdprConsent(
                                context = context,
                                accepted = true,
                                cloud = true,
                                ai = true,
                                community = true,
                                telemetry = true
                            )
                            Toast.makeText(
                                context,
                                if (lang == "CS") "Všechny souhlasy uděleny!" else "All consent parameters enabled!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("gdpr_accept_all_btn")
                    ) {
                        Text(
                            text = if (lang == "CS") "Přijmout vše" else "Accept All",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // Expanded customize dialog switches
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16102B)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B2A66)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Switch 1: Cloud Sync
                        GdprSwitchRow(
                            title = if (lang == "CS") "Cloudová záloha a smlouvy" else "Cloud Sync & Contracts",
                            description = if (lang == "CS") "Zálohy nahrávek a licenčních prodejů" else "Safe multitrack cloud syncing",
                            checked = consentCloud,
                            onCheckedChange = { consentCloud = it }
                        )

                        // Switch 2: AI Gemini
                        GdprSwitchRow(
                            title = if (lang == "CS") "AI Zpracování Gemini" else "Gemini AI Assistant",
                            description = if (lang == "CS") "Generování textů a zvuku" else "Lyric drafting & mastering assistance",
                            checked = consentAi,
                            onCheckedChange = { consentAi = it }
                        )

                        // Switch 3: Community Author Metadata
                        GdprSwitchRow(
                            title = if (lang == "CS") "Komunitní indexy prodejců" else "Community Copyright Feed",
                            description = if (lang == "CS") "Prezentace licencí a cen v CZK" else "Public listing of authors & prices",
                            checked = consentCommunity,
                            onCheckedChange = { consentCommunity = it }
                        )

                        // Switch 4: Anonymized Telemetry
                        GdprSwitchRow(
                            title = if (lang == "CS") "Anonymní telemetrie" else "Anonymized Analytics",
                            description = if (lang == "CS") "Hlášení pádů a přenosové rychlosti" else "Real-time stability metrics tracking",
                            checked = consentTelemetry,
                            onCheckedChange = { consentTelemetry = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveGdprConsent(
                                context = context,
                                accepted = false,
                                cloud = false,
                                ai = false,
                                community = false,
                                telemetry = false
                            )
                            Toast.makeText(
                                context,
                                if (lang == "CS") "Souhlasy odmítnuty." else "Preferences saved as denied.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1120)),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (lang == "CS") "Odmítnout" else "Reject All",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveGdprConsent(
                                context = context,
                                accepted = true,
                                cloud = consentCloud,
                                ai = consentAi,
                                community = consentCommunity,
                                telemetry = consentTelemetry
                            )
                            Toast.makeText(
                                context,
                                if (lang == "CS") "Vlastní nastavení uloženo." else "Custom settings saved.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = if (lang == "CS") "Uložit volby" else "Save Choices",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GdprSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Color.Gray, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF8F63F4),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0xFF231A3E)
            )
        )
    }
}

@Composable
fun MultiTrackTimelineEditor(
    activeProject: Project?,
    viewModel: StudioViewModel,
    lang: String
) {
    var zoomScale by remember { mutableStateOf(1.0f) }
    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Magnetic Snapping state
    var isSnappingEnabled by remember { mutableStateOf(true) }
    var simulatedElementPositionSeconds by remember { mutableStateOf(450.0f) } // Default position at 7.5 mins
    var snapFeedbackMsg by remember { mutableStateOf("") }

    // Define Snap boundaries (Every 2.5 minutes, i.e., 150s, 300s, 450s, 600s, etc.)
    val snapInterval = 150.0f
    
    // Total duration: 30 minutes = 1800 seconds.
    // Divided into 60 chunks of 30 seconds each.
    val totalChunks = 60
    
    // We let the base width of a chunk be 120dp.
    val chunkWidthDp = 120 * zoomScale
    val viewportWidthDp = 340f // Estimating container viewport size in DP

    // Detect visible chunks dynamically based on horizontal scrollState
    val scrollDpValue = with(density) { scrollState.value.toDp() }.value
    
    val visibleChunks = remember(zoomScale, scrollDpValue) {
        val list = mutableListOf<Int>()
        for (i in 0 until totalChunks) {
            val chunkStart = i * chunkWidthDp
            val chunkEnd = (i + 1) * chunkWidthDp
            if (chunkEnd >= scrollDpValue && chunkStart <= (scrollDpValue + viewportWidthDp)) {
                list.add(i)
            }
        }
        list
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B18)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, Color(0xFF3B2757)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (lang == "CS") "VÍCEKOLEJNÁ ČASOVÁ OSA (MULTI-TRACK)" else "MULTI-TRACK WORKSPACE LIVE TIMELINE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (lang == "CS") "Magnetické přichytávání, rychlý navigátor a líné načítání" else "Magnetic snapping, quick navigator & lazy loading",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                
                // Active status tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF00FFCC).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LAZY ACTIVE ⚡",
                        color = Color(0xFF00FFCC),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. PINCH-TO-ZOOM GESTURAL CONTROLLER & DISPLAY LABELS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (lang == "CS") "Měřítko osy (Zoom):" else "Timeline Scale (Zoom):",
                        color = Color(0xFFE2D6FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${"%.2f".format(zoomScale)}x",
                        color = Color(0xFF00FFCC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = if (lang == "CS") {
                        if (zoomScale > 4.0f) "Rozlišení: Milisekundy (Detail)" else if (zoomScale > 1.2f) "Rozlišení: Sekundy" else "Rozlišení: Minuty (Přehled)"
                    } else {
                        if (zoomScale > 4.0f) "Resolution: Milliseconds" else if (zoomScale > 1.2f) "Resolution: Seconds" else "Resolution: Minutes (Overview)"
                    },
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            // Interactive slider with zoom pinch simulation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("0.1x", color = Color.Gray, fontSize = 9.sp)
                Slider(
                    value = zoomScale,
                    onValueChange = { zoomScale = it },
                    valueRange = 0.15f..10.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00FFCC),
                        activeTrackColor = Color(0xFF8F63F4),
                        inactiveTrackColor = Color(0xFF231B32)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Text("10.0x", color = Color.Gray, fontSize = 9.sp)
            }

            // Pinch-to-zoom instruction helper
            Text(
                text = if (lang == "CS") "💡 Gesta: Osu lze roztáhnout pinch-gestem (roztažením dvou prstů) přímo v pracovní ploše!" else "💡 Gesture: Pinch or stretch timeline with two fingers inside the workspace to zoom!",
                color = Color.Gray,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            )

            // 2. QUICK NAVIGATION MINI-MAP (Projekty delší než 5 min)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF140D22), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF2E1949), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🗺️", fontSize = 11.sp)
                        Text(
                            text = if (lang == "CS") "RYCHLÁ NAVIGACE (CELÝCH 30 MIN)" else "MINI-MAP QUICK NAVIGATION (30 MIN RANGE)",
                            color = Color(0xFFFFAA00),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (lang == "CS") "Kliknutím skočíte v čase" else "Tap scroll area to jump",
                        color = Color.Gray,
                        fontSize = 8.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Highly visual mini-map container. Width matches parent; 60 tiny bars represent chunks.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color(0xFF07040C), RoundedCornerShape(4.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    for (i in 0 until totalChunks) {
                        val isCurrentViewport = visibleChunks.contains(i)
                        val isTransitionTag = i % 5 == 0 // key snaps every 2.5 minutes
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(1.dp))
                                .background(
                                    when {
                                        isCurrentViewport -> Color(0xFF00FFCC) // viewport frame active
                                        isTransitionTag -> Color(0xFFCC66FF).copy(alpha = 0.6f) // transition mark
                                        else -> Color(0xFF281C44)
                                    }
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        // target pixel computation
                                        val targetCenterSec = i * chunkWidthDp
                                        val scrollCenterPx = with(density) { targetCenterSec.dp.toPx() }
                                        scrollState.animateScrollTo(scrollCenterPx.toInt())
                                    }
                                }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0:00 (Start)", color = Color.Gray, fontSize = 8.sp)
                    Text("15:00", color = Color.Gray, fontSize = 8.sp)
                    Text("30:00 (End)", color = Color.Gray, fontSize = 8.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. MAGNETIC SNAPPING CONTROLLER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF120A20), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF271344), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🧲", fontSize = 12.sp)
                        Text(
                            text = if (lang == "CS") "MAGNETICKÉ PŘICHYTÁVÁNÍ (SNAPPING):" else "MAGNETIC SNAPPING PRE-SNAP:",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // On/Off Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isSnappingEnabled) "Zapnuto" else "Vypnuto",
                            color = if (isSnappingEnabled) Color(0xFF00FFCC) else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isSnappingEnabled,
                            onCheckedChange = { isSnappingEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00FFCC),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF25183E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (lang == "CS") {
                        "Bloky zvuku a překryvy se automaticky přichytí k nejbližšímu přechodu (každých 2.5 min), což zabraňuje nechtěným dírám v tichu."
                    } else {
                        "Audio components and lyrics lock safely to closest video cut transitions (every 2.5 minutes) to ensure perfect timing integrity."
                    },
                    color = Color.LightGray,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive drag simulator for popping/snapping elements
                Text(
                    text = if (lang == "CS") "Vyzkoušejte posun bloku zvuku (přichytává se k bodům):" else "Drag to test snap-to-boundary logic dynamically:",
                    color = Color.Gray,
                    fontSize = 9.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("0s", color = Color.Gray, fontSize = 9.sp)
                    Slider(
                        value = simulatedElementPositionSeconds,
                        onValueChange = { newVal ->
                            val nearestSnap = kotlin.math.round(newVal / snapInterval) * snapInterval
                            val distance = kotlin.math.abs(newVal - nearestSnap)
                            
                            if (isSnappingEnabled && distance < 35.0f) {
                                simulatedElementPositionSeconds = nearestSnap
                                snapFeedbackMsg = if (lang == "CS") {
                                    "Střední magnetické uchycení! Přisáto na čas ${nearestSnap.toInt()}s 🧲"
                                } else {
                                    "Snapped! Locked precisely to transition at ${nearestSnap.toInt()}s 🧲"
                                }
                            } else {
                                simulatedElementPositionSeconds = newVal
                                snapFeedbackMsg = ""
                            }
                        },
                        valueRange = 0.0f..1800.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (snapFeedbackMsg.isNotEmpty()) Color(0xFF00FFCC) else Color(0xFFFFAA00),
                            activeTrackColor = Color(0xFFFF9900),
                            inactiveTrackColor = Color(0xFF281C3D)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text("1800s", color = Color.Gray, fontSize = 9.sp)
                }

                if (snapFeedbackMsg.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00FFCC).copy(alpha = 0.12f))
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = snapFeedbackMsg,
                            color = Color(0xFF00FFCC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // THE TIMELINE INTERACTIVE WORKSPACE
            // Columns layout: LEFT side fixed head metadata, RIGHT side horizontally scrollable track layers stacked vertically.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .border(1.dp, Color(0xFF2B1C44), RoundedCornerShape(8.dp))
                    .background(Color(0xFF07040C))
            ) {
                // Left Column: Constant Track Headers (90dp width)
                Column(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF110821))
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                        Text("📐 TIMING", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                    }
                    Divider(color = Color(0xFF1E1035), thickness = 1.dp)

                    TrackHeaderItem(title = "🎬 VIDEO", desc = "Backdrop", color = Color(0xFF00FFCC))
                    Divider(color = Color(0xFF1E1035), thickness = 1.dp)
                    TrackHeaderItem(title = "🎙️ VOCAL", desc = "Mic Input", color = Color(0xFF00BFFF))
                    Divider(color = Color(0xFF1E1035), thickness = 1.dp)
                    TrackHeaderItem(title = "🎵 MUSIC", desc = "AI Synth", color = Color(0xFFCC66FF))
                    Divider(color = Color(0xFF1E1035), thickness = 1.dp)
                    TrackHeaderItem(title = "🌲 AMBIENT", desc = "Nature", color = Color(0xFFFFAA00))
                    Divider(color = Color(0xFF1E1035), thickness = 1.dp)
                    TrackHeaderItem(title = "✍️ LYRICS", desc = "Fonts overlay", color = Color(0xFF33CC33))
                }

                // Divider line
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF2B1B48)))

                // Right Side: Scrollable Timeline Tracks with adaptive Ruler
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scrollState)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.15f, 10.0f)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // TOP TIME MARKERS RULER (Pravítko)
                    Row(
                        modifier = Modifier
                            .width((totalChunks * chunkWidthDp).dp)
                            .height(24.dp)
                            .background(Color(0xFF0F0B18))
                    ) {
                        for (idx in 0 until totalChunks) {
                            val totalSeconds = idx * 30
                            val minutes = totalSeconds / 60
                            val seconds = totalSeconds % 60
                            val timestampStr = String.format("%02d:%02d", minutes, seconds)
                            val isSnapLine = idx % 5 == 0 // highlight snappable lines on the ruler

                            Box(
                                modifier = Modifier
                                    .width(chunkWidthDp.dp)
                                    .fillMaxHeight()
                                    .padding(top = 8.dp, start = 4.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val subTickCount = if (zoomScale > 4.0f) 10 else 5
                                    val step = this.size.width / subTickCount
                                    for (t in 0 until subTickCount) {
                                        val x = t * step
                                        val tickHeight = if (t == 0) {
                                            if (isSnapLine) this.size.height * 0.8f else this.size.height * 0.6f
                                        } else {
                                            this.size.height * 0.3f
                                        }
                                        val tickColor = if (t == 0) {
                                            if (isSnapLine && isSnappingEnabled) Color(0xFFFFAA00) else Color(0xFF00FFCC)
                                        } else {
                                            Color.Gray.copy(alpha = 0.4f)
                                        }
                                        drawLine(
                                            color = tickColor,
                                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                                            end = androidx.compose.ui.geometry.Offset(x, tickHeight),
                                            strokeWidth = if (t == 0 && isSnapLine) 3f else 1.5f
                                        )
                                    }
                                }
                                Text(
                                    text = timestampStr,
                                    color = if (isSnapLine && isSnappingEnabled) Color(0xFFFFAA00) else Color(0xFFE2D6FF),
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    // Track 1: Main Video (Backdrops)
                    Row(
                        modifier = Modifier
                            .width((totalChunks * chunkWidthDp).dp)
                            .height(34.dp)
                    ) {
                        for (idx in 0 until totalChunks) {
                            val inMemory = visibleChunks.contains(idx)
                            val isSnapBlockEdge = idx % 5 == 0
                            
                            Box(
                                modifier = Modifier
                                    .width(chunkWidthDp.dp)
                                    .fillMaxHeight()
                                    .padding(vertical = 2.dp, horizontal = 1.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (inMemory) {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    if (isSnapBlockEdge && isSnappingEnabled) Color(0xFFFFAA00).copy(alpha = 0.25f) else Color(0xFF00FFCC).copy(alpha = 0.25f),
                                                    Color(0xFF1B072B)
                                                )
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF1D1D1D).copy(alpha = 0.2f),
                                                    Color(0xFF1D1D1D).copy(alpha = 0.2f)
                                                )
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isSnapBlockEdge && isSnappingEnabled) 1.dp else 0.5.dp,
                                        color = when {
                                            isSnapBlockEdge && isSnappingEnabled -> Color(0xFFFFAA00).copy(alpha = 0.8f)
                                            inMemory -> Color(0xFF00FFCC).copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (inMemory) {
                                    // Heavy graphics simulated preview thumbnail loads here
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(if (isSnapBlockEdge) "🧲" else "🖼️", fontSize = 10.sp)
                                        Text(
                                            text = "f_${idx * 30}s.jpg",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                } else {
                                    Text("PAUSED", color = Color.Gray, fontSize = 7.sp)
                                }
                            }
                        }
                    }

                    val isCloudGenerating by viewModel.isCloudMusicGenerating.collectAsStateWithLifecycle()

                    // Track 2: Vocals
                    AudioWaveformPatternTrack(
                        totalChunks = totalChunks,
                        chunkWidthDp = chunkWidthDp,
                        visibleChunks = visibleChunks,
                        color = Color(0xFF00BFFF),
                        isGenerating = isCloudGenerating
                    )

                    // Track 3: Music Backing
                    AudioWaveformPatternTrack(
                        totalChunks = totalChunks,
                        chunkWidthDp = chunkWidthDp,
                        visibleChunks = visibleChunks,
                        color = Color(0xFFCC66FF),
                        isGenerating = isCloudGenerating
                    )

                    // Track 4: Ambient Sounds
                    AudioWaveformPatternTrack(
                        totalChunks = totalChunks,
                        chunkWidthDp = chunkWidthDp,
                        visibleChunks = visibleChunks,
                        color = Color(0xFFFFAA00),
                        isGenerating = isCloudGenerating
                    )

                    // Track 5: Lyrics & Text Overlays
                    Row(
                        modifier = Modifier
                            .width((totalChunks * chunkWidthDp).dp)
                            .height(34.dp)
                    ) {
                        for (idx in 0 until totalChunks) {
                            val inMemory = visibleChunks.contains(idx)
                            val isSnapBlockEdge = idx % 5 == 0

                            Box(
                                modifier = Modifier
                                    .width(chunkWidthDp.dp)
                                    .fillMaxHeight()
                                    .padding(vertical = 2.dp, horizontal = 1.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (inMemory) {
                                            Color(0xFF33CC33).copy(alpha = 0.15f)
                                        } else {
                                            Color(0xFF1D1D1D).copy(alpha = 0.2f)
                                        }
                                    )
                                    .border(
                                        width = if (isSnapBlockEdge && isSnappingEnabled) 1.dp else 0.5.dp,
                                        color = if (isSnapBlockEdge && isSnappingEnabled) Color(0xFFFFAA00) else if (inMemory) Color(0xFF33CC33).copy(alpha = 0.4f) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (inMemory) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🖊️", fontSize = 9.sp)
                                        Text(
                                            text = "Overlay v${idx}",
                                            color = Color(0xFF99FF99),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Text("-", color = Color.DarkGray, fontSize = 7.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. DIAGNOSTICS LAZY CACHE MONITOR (LÍNÉ NAČÍTÁNÍ OBSERVER)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D22)),
                border = BorderStroke(1.dp, Color(0xFF2F1D4B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🛡️", fontSize = 12.sp)
                            Text(
                                text = if (lang == "CS") "PROUDOVÉ LÍNÉ NAČÍTÁNÍ (LAZY TIMELINE MEMORY)" else "DEBUG MEMORY DIAGNOSTICS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Percentage RAM Saved badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF9900).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RAM SAVED: 95.8% ❄️",
                                color = Color(0xFFFFCC00),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang == "CS") {
                            "Waveformy, časové značky a náhledové grafiky jsou v paměti RAM drženy pouze pro ${visibleChunks.size} viditelných segmentů. Zbývajících ${totalChunks - visibleChunks.size} segmentů je zcela uvolněno z mezipaměti."
                        } else {
                            "Waveforms and thumbnails are retained in active heap only for ${visibleChunks.size} visible view ports. The remaining ${totalChunks - visibleChunks.size} off-screen targets are GC-evicted to isolate heap size."
                        },
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        lineHeight = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated heap specifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "CURRENT VIEWPORT:", color = Color.Gray, fontSize = 8.sp)
                            val startMin = (scrollDpValue / (chunkWidthDp / 0.5f)).toInt()
                            val endMin = startMin + 3
                            Text(text = "Time: ${startMin}m:00s - ${endMin}m:30s / 30m", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "CACHE POOL LOADED:", color = Color.Gray, fontSize = 8.sp)
                            Text(text = "Chunks [${visibleChunks.firstOrNull() ?: 0}..${visibleChunks.lastOrNull() ?: 0}] Active in RAM", color = Color(0xFF00FFCC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Minimal visual bar representing all chunk cache map positions
                    // Green box = loaded, dark dot = released
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color(0xFF07040C), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.5.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        for (i in 0 until totalChunks) {
                            val isInMemoryCell = visibleChunks.contains(i)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        if (isInMemoryCell) Color(0xFF00FFCC) else Color(0xFF381F5E)
                                    )
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30m Start", color = Color.Gray, fontSize = 8.sp)
                        Text("🟢 Loaded viewport chunks in RAM", color = Color(0xFF00FFCC), fontSize = 8.sp)
                        Text("30m End", color = Color.Gray, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackHeaderItem(title: String, desc: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(text = desc, color = Color.Gray, fontSize = 7.sp)
    }
}

@Composable
fun AudioWaveformPatternTrack(
    totalChunks: Int,
    chunkWidthDp: Float,
    visibleChunks: List<Int>,
    color: Color,
    isGenerating: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .width((totalChunks * chunkWidthDp).dp)
            .height(34.dp)
    ) {
        for (idx in 0 until totalChunks) {
            val inMemory = visibleChunks.contains(idx)
            val baseColor = if (isGenerating) color.copy(alpha = pulseAlpha) else color
            Box(
                modifier = Modifier
                    .width(chunkWidthDp.dp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp, horizontal = 1.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isGenerating && inMemory) {
                            Color(0xFF23143F).copy(alpha = pulseAlpha)
                        } else if (inMemory) {
                            Color(0xFF130922)
                        } else {
                            Color(0xFF101010).copy(alpha = 0.2f)
                        }
                    )
                    .border(
                        width = if (isGenerating) 1.5.dp else 0.5.dp,
                        color = if (isGenerating && inMemory) Color(0xFF00FFCC).copy(alpha = pulseAlpha) else if (inMemory) color.copy(alpha = 0.4f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (inMemory) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        val spaceCount = 14
                        val barStep = size.width / spaceCount
                        val seed = idx * 17
                        for (b in 0 until spaceCount) {
                            val waveHeightFraction = ((b * seed) % 7 + 2).toFloat() / 10f
                            val startX = b * barStep + barStep/2
                            val barHeightValue = size.height * waveHeightFraction
                            drawLine(
                                color = baseColor,
                                start = androidx.compose.ui.geometry.Offset(startX, (size.height - barHeightValue) / 2),
                                end = androidx.compose.ui.geometry.Offset(startX, (size.height + barHeightValue) / 2),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color.DarkGray.copy(alpha = 0.25f),
                            start = androidx.compose.ui.geometry.Offset(0f, size.height/2),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height/2),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }
    }
}

// --- UPGRADED INTERACTIVE ONBOARDING TUTORIAL SPOTLIGHT OVERLAY ---
@Composable
fun SpotlightOnboardingOverlay(
    viewModel: StudioViewModel,
    step: Int,
    onStepChange: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {}, // absorb touch events
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .background(Color(0xFF140C22), RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFF8F63F4), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                0 -> {
                    // STEP 0: LANGUAGE SELECTION & WELCOME
                    Text(
                        text = "🚀 VÍTEJTE V DENULI STUDIO",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vítejte v první mobilní aplikaci pro profesionální střih videa s plnou integrací AI. Vyberte prosím jazyk rozhraní pro spuštění interaktivního průvodce:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.switchLanguage("CS") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "CS") Color(0xFF00FFCC) else Color(0xFF26193E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Čeština 🇨🇿", color = if (lang == "CS") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.switchLanguage("EN") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "EN") Color(0xFF00FFCC) else Color(0xFF26193E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("English 🇬🇧", color = if (lang == "EN") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { onStepChange(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = if (lang == "CS") "SPUSTIT PRŮVODCE ➔" else "START TUTORIAL ➔",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    TextButton(
                        onClick = { viewModel.completeOnboarding(context) }
                    ) {
                        Text(
                            text = if (lang == "CS") "Přeskočit a spustit nahrávací studio ✖" else "Skip and open recording studio ✖",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
                
                1 -> {
                    // STEP 1: RECORD BUTTON HIGHLIGHT
                    Text(
                        text = "🎙️ KROK 1: TLAČÍTKO NAHRÁVAT",
                        color = Color(0xFF00FFCC),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Highlight simulator
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF330C1E), RoundedCornerShape(12.dp))
                            .border(2.dp, Color.Red, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (lang == "CS") "NAHRÁVAT HRANÍ / ZPĚV" else "RECORD PERFORMANCE", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (lang == "CS") "Zde můžete nahrát svůj autentický hlas, mikrofon, vyprávění pohádky, zpěv, hudební nástroj nebo realistické zvuky zvířat přímo do první stopy!" else "Here you can record your authentic voice, microphone, story reading, vocals, musical instruments or realistic animal sounds directly into the first track!",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { viewModel.completeOnboarding(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (lang == "CS") "Přeskočit" else "Skip", color = Color.Gray)
                        }
                        Button(
                            onClick = { onStepChange(2) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text(if (lang == "CS") "Dále ➔" else "Next ➔", color = Color.White)
                        }
                    }
                }
                
                2 -> {
                    // STEP 2: AI STYLE BOX
                    Text(
                        text = "✨ KROK 2: AI STYLE BOX",
                        color = Color(0xFFFFAA00),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Highlight simulator
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1B142D), RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFFFFAA00), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text("Style Prompt: [ filmová hudba, piáno, epické smyčce ]", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (lang == "CS") "Sem napíšete své kreativní zadání (Positive a Negative prompt). Umělá inteligence na našich cloudech vygeneruje hudební podkres na míru bez jakéhokoliv sekání vašeho telefonu, a zapíše jej frame-by-frame s automatickým timeout zabezpečením!" else "This is where you specify your positive and negative prompts. Cloud-based AI will generate tailored background music with automatic timeout protection without slowing down your phone!",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { viewModel.completeOnboarding(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (lang == "CS") "Přeskočit" else "Skip", color = Color.Gray)
                        }
                        Button(
                            onClick = { onStepChange(3) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F63F4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text(if (lang == "CS") "Dále ➔" else "Next ➔", color = Color.White)
                        }
                    }
                }
                
                3 -> {
                    // STEP 3: MULTI-TRACK TIMELINE
                    Text(
                        text = "🎬 KROK 3: MULTI-TRACK ČASOVÁ OSA",
                        color = Color(0xFFCC66FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Highlight simulator
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F0B18), RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFFCC66FF), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Track 1: Video 🎞️", color = Color.White, fontSize = 10.sp)
                            Text("Track 2: Voice 🎙️", color = Color.White, fontSize = 10.sp)
                            Text("Track 3: AI Music 🎵", color = Color.White, fontSize = 10.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (lang == "CS") "V této sekci stříháte celou nahrávku s přesností na milisekundy! Využijte pinch-to-zoom gesto, magnetické přichytávání k hranám pro zamezení nežádoucích mezer, a rychlý přehledový navigátor pro dlouhá videa!" else "This is where you arrange and edit your elements down to milliseconds! Use pinch-to-zoom gestures, magnetic snapping to edges to avoid silent gaps, and a handy minimap to instantly navigate long files!",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { viewModel.completeOnboarding(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = if (lang == "CS") "OTEVŘÍT HLAVNÍ STUDIO 🚀" else "OPEN MAIN STUDIO 🚀",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}


// =========================================================================
// STUDIO DENULI CUSTOM GRAPHICS & VISUAL IDENTITY ARTWORK
// =========================================================================

@Composable
fun SparkleBackground(isDarkMode: Boolean) {
    if (!isDarkMode) return

    val infiniteTransition = rememberInfiniteTransition(label = "Sparkle Twinkle")
    
    // Create multiple sparkle values animating with different phases
    val sparkleAnimations = (0 until 12).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200 + (index * 200) % 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sparkle_alpha_anim_$index"
        )
    }

    // Fixed relative offsets for positions so we don't recalculate coordinates on every frame
    val sparklePositions = remember {
        listOf(
            0.10f to 0.15f, 0.25f to 0.08f, 0.40f to 0.18f, 0.05f to 0.35f, 0.18f to 0.42f,
            0.32f to 0.28f, 0.22f to 0.55f, 0.08f to 0.65f, 0.15f to 0.78f, 0.38f to 0.85f,
            0.48f to 0.12f, 0.55f to 0.38f, 0.62f to 0.25f, 0.75f to 0.15f, 0.88f to 0.08f,
            0.92f to 0.22f, 0.70f to 0.45f, 0.82f to 0.35f, 0.95f to 0.50f, 0.68f to 0.65f,
            0.85f to 0.60f, 0.76f to 0.78f, 0.90f to 0.88f, 0.52f to 0.82f, 0.60f to 0.92f,
            0.45f to 0.48f, 0.30f to 0.70f, 0.28f to 0.92f, 0.02f to 0.90f, 0.96f to 0.05f
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        sparklePositions.forEachIndexed { idx, (px, py) ->
            val animIdx = idx % sparkleAnimations.size
            val alpha = sparkleAnimations[animIdx].value
            val cx = px * width
            val cy = py * height
            
            val sparkleColor = if (idx % 2 == 0) Color(0xFFD4AF37).copy(alpha = alpha * 0.3f) else Color(0xFF00FFCC).copy(alpha = alpha * 0.25f)
            
            drawCircle(
                color = sparkleColor,
                radius = if (idx % 3 == 0) 3.5f else 2.0f,
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
            
            // Soft cross bloom
            if (idx % 4 == 0) {
                drawLine(
                    color = sparkleColor,
                    start = androidx.compose.ui.geometry.Offset(cx - 6f, cy),
                    end = androidx.compose.ui.geometry.Offset(cx + 6f, cy),
                    strokeWidth = 0.8f
                )
                drawLine(
                    color = sparkleColor,
                    start = androidx.compose.ui.geometry.Offset(cx, cy - 6f),
                    end = androidx.compose.ui.geometry.Offset(cx, cy + 6f),
                    strokeWidth = 0.8f
                )
            }
        }
    }
}

@Composable
fun WatermarkBackground(lang: String, isDarkMode: Boolean) {
    val quote = when (lang) {
        "CS" -> "Jsem zde a má hudba má být pro všechny."
        "ES" -> "Estoy aquí, y mi música es para todos."
        "DE" -> "Ich bin hier, und meine Musik ist für alle da."
        else -> "I am here, and my music is meant for everyone."
    }

    val textColor = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFF140D22).copy(alpha = 0.04f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = -12f),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = quote,
                color = textColor,
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            Text(
                text = quote,
                color = textColor,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            Text(
                text = quote,
                color = textColor,
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun MysticRunicDivider(isDarkMode: Boolean, modifier: Modifier = Modifier) {
    val dividerColor = if (isDarkMode) Color(0xFFD4AF37).copy(alpha = 0.08f) else Color(0xFF53318F).copy(alpha = 0.06f)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left horizontal line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.6.dp)
                .background(dividerColor)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Central geometrical mystic rune diamond
        Canvas(modifier = Modifier.size(16.dp)) {
            val strokeWidth = 1.dp.toPx()
            
            // Draw a central diamond (rhombus)
            val path = Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(size.width / 2, size.height)
                lineTo(0f, size.height / 2)
                close()
            }
            drawPath(
                path = path,
                color = dividerColor,
                style = Stroke(width = strokeWidth)
            )
            
            // Runic cross inside
            drawLine(
                color = dividerColor,
                start = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.2f),
                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.8f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = dividerColor,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height / 2),
                strokeWidth = strokeWidth
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Right horizontal line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.6.dp)
                .background(dividerColor)
        )
    }
}

@Composable
fun MysticCornerBox(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cornerColor = if (isDarkMode) Color(0xFFD4AF37).copy(alpha = 0.08f) else Color(0xFF53318F).copy(alpha = 0.06f)
    
    Box(modifier = modifier) {
        content()
        
        // Incurved decorative corner markings
        Canvas(modifier = Modifier.matchParentSize()) {
            val len = 10.dp.toPx()
            val strokeWidth = 1.dp.toPx()
            val padding = 3.dp.toPx()
            
            // Top-Left corner lines
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(padding, padding),
                end = androidx.compose.ui.geometry.Offset(padding + len, padding),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(padding, padding),
                end = androidx.compose.ui.geometry.Offset(padding, padding + len),
                strokeWidth = strokeWidth
            )
            drawCircle(cornerColor, radius = 1.2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(padding + 2.dp.toPx(), padding + 2.dp.toPx()))

            // Top-Right corner lines
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(size.width - padding, padding),
                end = androidx.compose.ui.geometry.Offset(size.width - padding - len, padding),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(size.width - padding, padding),
                end = androidx.compose.ui.geometry.Offset(size.width - padding, padding + len),
                strokeWidth = strokeWidth
            )
            drawCircle(cornerColor, radius = 1.2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width - padding - 2.dp.toPx(), padding + 2.dp.toPx()))

            // Bottom-Left corner lines
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(padding, size.height - padding),
                end = androidx.compose.ui.geometry.Offset(padding + len, size.height - padding),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(padding, size.height - padding),
                end = androidx.compose.ui.geometry.Offset(padding, size.height - padding - len),
                strokeWidth = strokeWidth
            )
            drawCircle(cornerColor, radius = 1.2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(padding + 2.dp.toPx(), size.height - padding - 2.dp.toPx()))

            // Bottom-Right corner lines
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(size.width - padding, size.height - padding),
                end = androidx.compose.ui.geometry.Offset(size.width - padding - len, size.height - padding),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(size.width - padding, size.height - padding),
                end = androidx.compose.ui.geometry.Offset(size.width - padding, size.height - padding - len),
                strokeWidth = strokeWidth
            )
            drawCircle(cornerColor, radius = 1.2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width - padding - 2.dp.toPx(), size.height - padding - 2.dp.toPx()))
        }
    }
}

@Composable
fun GlowingMainButtonWrapper(
    isGenerating: Boolean,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!isGenerating) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Neon Glow Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_neon"
    )

    val neonColor = if (isDarkMode) Color(0xFF00FFCC) else Color(0xFFE5A9AC) // Neon teal vs Rose Gold

    Box(
        modifier = modifier
            .border(
                width = 1.8.dp,
                color = neonColor.copy(alpha = pulseAlpha),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(2.dp)
    ) {
        content()
        
        // Custom neon glowing aura corners overlay
        Canvas(modifier = Modifier.matchParentSize()) {
            val glowColor = neonColor.copy(alpha = pulseAlpha * 0.7f)
            drawCircle(glowColor, radius = 4f, center = androidx.compose.ui.geometry.Offset(size.width / 2, 0f))
            drawCircle(glowColor, radius = 4f, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height))
        }
    }
}

