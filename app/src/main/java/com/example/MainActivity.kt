package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import android.widget.MediaController
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.database.AppDatabase
import com.example.data.database.MarketplaceItem
import com.example.data.database.Project
import com.example.ui.viewmodel.StudioViewModel
import com.example.ui.viewmodel.StudioViewModelFactory
import com.example.util.Language
import com.example.util.TranslationUtility
import com.example.ui.components.WaveformRenderer
import java.io.File
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init repository and database
        val database = AppDatabase.getDatabase(this)
        val repository = com.example.data.repository.StudioRepository(database.studioDao())
        val factory = StudioViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[StudioViewModel::class.java]

        // Load saved Gemini API Key
        val sharedPrefs = getSharedPreferences("spark_settings", MODE_PRIVATE)
        val savedKey = sharedPrefs.getString("gemini_key", "") ?: ""
        com.example.data.network.GeminiClient.customApiKey = savedKey

        viewModel.initializeOnboarding(this)

        setContent {
            SparkStudioTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0C0717) // Ultra-premium deep dark background
                ) { innerPadding ->
                    SparkStudioApp(
                        viewModel = viewModel,
                        paddingValues = innerPadding
                    )
                }
            }
        }
    }
}

// Custom Premium Spark Studio Color Palette
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val DarkPurpleBg = Color(0xFF0A0515)
val CardBackground = Color(0xFF130D26)
val AccentNeonCyan = Color(0xFF00FFC2)
val AccentPurple = Color(0xFF7E3FF2)

val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    secondary = AccentNeonCyan,
    tertiary = Pink80,
    background = DarkPurpleBg,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onSurface = Color(0xFFE5E2F5)
)

@Composable
fun SparkStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparkStudioApp(
    viewModel: StudioViewModel,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // Tabs: 0=Domů, 1=Studio, 2=Video, 3=Chat, 4=Tržiště, 5=Můj Denuli
    val isAgeVerified by viewModel.isAgeVerified.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val currentLang by TranslationUtility.currentLanguage.collectAsStateWithLifecycle()
    val userCredits by viewModel.userCredits.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // --- 1. Top Premium Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular app logo
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E133B)),
                    contentAlignment = Alignment.Center
                ) {
                    // Load splash logo directly
                    val logoFile = File(context.filesDir, "../app_app/src/main/res/drawable/img_splash_logo.png")
                    val logoRes = if (logoFile.exists()) logoFile.absolutePath else android.R.drawable.ic_media_play
                    AsyncImage(
                        model = logoRes,
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(android.R.drawable.ic_lock_power_off) // Safe fallback to verify
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = TranslationUtility.get("app_title"),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = TranslationUtility.get("app_subtitle"),
                        fontSize = 11.sp,
                        color = Color(0xFF8E8CA4)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glow Spark Coins balance Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF261D45))
                        .border(1.dp, Color(0xFF3B2D6B), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🪙",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$userCredits",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                // Language selection toggle
                IconButton(
                    onClick = { TranslationUtility.toggleLanguage() },
                    modifier = Modifier
                        .testTag("lang_toggle_btn")
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF261D45))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Language toggle",
                        tint = AccentNeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLang == Language.CZ) "Čeština" else "English",
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Settings gear button
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .testTag("top_settings_gear_btn")
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF261D45))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Nastavení",
                        tint = AccentNeonCyan
                    )
                }
            }
        }

        // Horizontal separator trace
        HorizontalDivider(color = Color(0xFF221742), thickness = 1.dp)

        // --- 2. Main Tabbed Content Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Force user register / legal verification screen if not age confirmed
            if (!isAgeVerified) {
                AgeVerificationScreen(
                    onVerify = { viewModel.setAgeVerification(context, true) }
                )
            } else {
                when (selectedTab) {
                    0 -> GuideTab(viewModel = viewModel, onNavigateToStudio = { selectedTab = 1 })
                    1 -> StudioTab(viewModel = viewModel)
                    2 -> VideoTab(viewModel = viewModel)
                    3 -> ChatTab(viewModel = viewModel)
                    4 -> MarketplaceTab(viewModel = viewModel)
                    5 -> LegalAndProfileTab(viewModel = viewModel, onSelectProject = { proj ->
                        viewModel.selectProject(proj, context)
                        selectedTab = 1
                    })
                }
            }
        }

        // --- 3. Premium Active Project Quickbar ---
        if (isAgeVerified && activeProject != null && selectedTab != 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF13092A), Color(0xFF0D0621))))
                    .border(1.dp, Color(0xFF26174A), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable { selectedTab = 1 }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Music",
                            tint = AccentNeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activeProject?.title ?: "No project",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Mixer active: ${activeProject?.genre} / ${activeProject?.vocalEffect}",
                                color = Color(0xFF8E8CA4),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "OTEVŘÍT STUDIO", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 4. Navigation Bottom Bar (Material 3 with custom insets) ---
        NavigationBar(
            containerColor = Color(0xFF090412),
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(68.dp),
            tonalElevation = 8.dp
        ) {
            val tabsInfo = listOf(
                Triple("nav_home", Icons.Default.Home, Icons.Outlined.Home),
                Triple("nav_studio", Icons.Default.Edit, Icons.Outlined.Edit),
                Triple("nav_video", Icons.Default.PlayArrow, Icons.Outlined.PlayArrow),
                Triple("nav_chat", Icons.Default.Info, Icons.Outlined.Info),
                Triple("nav_market", Icons.Default.ShoppingCart, Icons.Outlined.ShoppingCart),
                Triple("nav_profile", Icons.Default.Person, Icons.Outlined.Person)
            )

            tabsInfo.forEachIndexed { index, pair ->
                val label = TranslationUtility.get(pair.first)
                val isSelected = selectedTab == index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) pair.second else pair.third,
                            contentDescription = label,
                            tint = if (isSelected) Color.Black else Color(0xFF867E96)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentNeonCyan else Color(0xFF867E96),
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = AccentNeonCyan
                    ),
                    modifier = Modifier.testTag("nav_item_${pair.first}")
                )
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var apiKeyInput by remember { mutableStateOf(com.example.data.network.GeminiClient.customApiKey) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf<Boolean?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Nastavení",
                    tint = AccentNeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "NASTAVENÍ A BEZPEČNOST 🔒",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info block
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130D26)),
                    border = BorderStroke(1.dp, Color(0xFF2E1A5E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Klíče prostředí (Environment)",
                            color = AccentNeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Zabezpečený klíč GEMINI_API_KEY slouží k pohánění veškeré umělé inteligence v aplikaci (složení textů, generování scény, chat atd.). Pokud je nastaven, aplikace komunikuje přímo s moderními servery Google Gemini v reálném čase.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // API Key Field Box
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "VÁŠ GEMINI_API_KEY",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            testResult = null
                            isTestSuccess = null
                        },
                        placeholder = { Text("AIzaSy...", color = Color.Gray, fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Edit else Icons.Default.Lock,
                                    contentDescription = if (passwordVisible) "Skrýt" else "Zobrazit",
                                    tint = AccentNeonCyan
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentNeonCyan,
                            unfocusedBorderColor = Color(0xFF251A4D)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_api_key_field")
                    )
                }

                // Testing block / Connection state indicator
                if (testResult != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTestSuccess == true) Color(0xFF0C2417) else Color(0xFF240C12)
                        ),
                        border = BorderStroke(1.dp, if (isTestSuccess == true) Color(0xFF00FFC2) else Color(0xFFFF5252)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isTestSuccess == true) "✅" else "❌",
                                fontSize = 16.sp
                            )
                            Text(
                                text = testResult ?: "",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Actions Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save key locally
                    Button(
                        onClick = {
                            val p = context.getSharedPreferences("spark_settings", android.content.Context.MODE_PRIVATE)
                            p.edit().putString("gemini_key", apiKeyInput.trim()).apply()
                            com.example.data.network.GeminiClient.customApiKey = apiKeyInput.trim()
                            Toast.makeText(context, "Klíč byl uložen do prostředí aplikace! 💾", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan),
                        modifier = Modifier.weight(1f).testTag("settings_save_api_key_btn")
                    ) {
                        Text("ULOŽIT KLÍČ 💾", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }

                    // Test key function button
                    Button(
                        onClick = {
                            if (apiKeyInput.trim().isBlank()) {
                                Toast.makeText(context, "Nejprve zadejte API klíč pro otestování", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isTestingConnection = true
                            testResult = "Právě se připojuji k API serverům..."
                            isTestSuccess = null
                            coroutineScope.launch {
                                try {
                                    // Set temporarily to test
                                    val originalKey = com.example.data.network.GeminiClient.customApiKey
                                    com.example.data.network.GeminiClient.customApiKey = apiKeyInput.trim()
                                    val response = com.example.data.network.GeminiClient.generateText(
                                        "Zkontroluj toto spojení. Odpověz pouze jediným slovem: 'OK'."
                                    )
                                    isTestingConnection = false
                                    if (response.contains("OK", ignoreCase = true) || response.isNotBlank()) {
                                        isTestSuccess = true
                                        testResult = "Zkouška úspěšná! Spojení se servery Google Gemini funguje bezchybně. 🎉 Odpověď modelu: $response"
                                        // Save officially since it passed
                                        val p = context.getSharedPreferences("spark_settings", android.content.Context.MODE_PRIVATE)
                                        p.edit().putString("gemini_key", apiKeyInput.trim()).apply()
                                    } else {
                                        isTestSuccess = false
                                        testResult = "API klíč vrátil neúplnou odpověď: $response"
                                        com.example.data.network.GeminiClient.customApiKey = originalKey
                                    }
                                } catch (e: Exception) {
                                    isTestingConnection = false
                                    isTestSuccess = false
                                    testResult = "Připojení selhalo: ${e.message}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D45)),
                        enabled = !isTestingConnection,
                        modifier = Modifier.weight(1f).border(1.dp, Color(0xFF32245C), RoundedCornerShape(100)).testTag("settings_test_api_key_btn")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = AccentNeonCyan)
                        } else {
                            Text("TEST SPOJENÍ 🔍", color = AccentNeonCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Explain environment setup alternative
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0A1F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "💡 Doporučená integrace pro Google AI Studio",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Klíč můžete rovněž zadat do nativního Secrets panelu (Tajemství) přímo v Google AI Studio (ikona klíče v levé boční liště) pod názvem GEMINI_API_KEY. Náš build systém ho poté automaticky načte jako systémovou proměnnou prostředí při každém sestavení aplikace, což je mnohem bezpečnější pro produkční nasazení.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan)
            ) {
                Text("HOTOVO", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF0C0717),
        tonalElevation = 6.dp
    )
}

// --- ONBOARDING: Age Verification Checkbox ---
@Composable
fun AgeVerificationScreen(
    onVerify: () -> Unit
) {
    val context = LocalContext.current
    var isChecked by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Zásady ochrany osobních údajů (GDPR)",
                    color = AccentNeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Box(modifier = Modifier.height(280.dp)) {
                    val scrollState = rememberScrollState()
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        Text(
                            text = com.example.util.LegalTexts.PRIVACY_POLICY,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                ) {
                    Text("Rozumím", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF140D2F)
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Smluvní podmínky užívání služby",
                    color = AccentNeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Box(modifier = Modifier.height(280.dp)) {
                    val scrollState = rememberScrollState()
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        Text(
                            text = com.example.util.LegalTexts.TERMS_OF_SERVICE,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                ) {
                    Text("Souhlasím", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF140D2F)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2E1C5E), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Legal Security",
                    tint = AccentNeonCyan,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = TranslationUtility.get("legal_title"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = TranslationUtility.get("legal_desc"),
                    fontSize = 14.sp,
                    color = Color(0xFF8E8CA4),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChecked = !isChecked }
                        .background(Color(0xFF1E1339), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentNeonCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationUtility.get("age_verify"),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Číst zásady GDPR 📄",
                        color = AccentNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { showPrivacyDialog = true }
                            .padding(4.dp)
                    )
                    Text(
                        text = "Smluvní podmínky 📜",
                        color = AccentNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { showTermsDialog = true }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://denulicz.github.io/Denuli-CZ-/"))
                        context.startActivity(browserIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D45)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = TranslationUtility.get("github_web"), color = AccentNeonCyan, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onVerify,
                    enabled = isChecked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentNeonCyan,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF1C1A24)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_verify_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "VSTOUPIT DO STUDIA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// --- TAB 0: HOME / GUIDE TAB ---
@Composable
fun GuideTab(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit
) {
    val context = LocalContext.current
    var isNewProjectDialogShown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = TranslationUtility.get("active_project"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentNeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TranslationUtility.get("choose_or_create"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Row {
                        Button(
                            onClick = onNavigateToStudio,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13092A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = TranslationUtility.get("projects_btn"), fontSize = 12.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { isNewProjectDialogShown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = TranslationUtility.get("new_btn"), fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF261D45), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = TranslationUtility.get("quick_guide"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonCyan
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    GuideStepItem(
                        title = TranslationUtility.get("step_1_title"),
                        desc = TranslationUtility.get("step_1_desc")
                    )
                    HorizontalDivider(color = Color(0xFF21163E), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    GuideStepItem(
                        title = TranslationUtility.get("step_2_title"),
                        desc = TranslationUtility.get("step_2_desc")
                    )
                    HorizontalDivider(color = Color(0xFF21163E), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    GuideStepItem(
                        title = TranslationUtility.get("step_3_title"),
                        desc = TranslationUtility.get("step_3_desc")
                    )
                    HorizontalDivider(color = Color(0xFF21163E), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    GuideStepItem(
                        title = TranslationUtility.get("step_4_title"),
                        desc = TranslationUtility.get("step_4_desc")
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.createNewProject(
                        "PROJEKT JEDNA DVĚ TŘI ⚡",
                        "Skládám hudbu,\nje to můj kreativní svět.\nKde myšlenky plynou ...",
                        "Pop",
                        "Normal",
                        "Rain"
                    )
                    onNavigateToStudio()
                    Toast.makeText(context, "Načten výchozí projekt jedna dvě tři!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Flash")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationUtility.get("load_promo_btn"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (isNewProjectDialogShown) {
        var titleInput by remember { mutableStateOf("") }
        var genreInput by remember { mutableStateOf("Pop") }
        val genres = listOf("Pop", "Rock", "HipHop", "Cinematic", "Synthwave", "Lo-Fi", "Metal", "EDM", "Country")

        AlertDialog(
            onDismissRequest = { isNewProjectDialogShown = false },
            title = { Text(text = "Nový projekt") },
            containerColor = CardBackground,
            text = {
                Column {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Název písně") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentNeonCyan),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Zvolit Hudební žánr:", color = Color.White, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.forEach { g ->
                            val isSel = genreInput == g
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AccentNeonCyan else Color(0xFF21163E))
                                    .clickable { genreInput = g }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            viewModel.createNewProject(titleInput, "", genreInput, "Normal", "None")
                            isNewProjectDialogShown = false
                            onNavigateToStudio()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                ) {
                    Text("Vytvořit")
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewProjectDialogShown = false }) {
                    Text("Zrušit", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun GuideStepItem(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = desc, fontSize = 12.sp, color = Color(0xFF908DA1))
    }
}

// --- TAB 1: STUDIO (MULTITRACK MASTER) ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StudioTab(
    viewModel: StudioViewModel
) {
    val context = LocalContext.current
    val activeProj by viewModel.activeProject.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    
    val isGeneratingLyrics by viewModel.isGeneratingLyrics.collectAsStateWithLifecycle()
    val isSynthesizingAudio by viewModel.isSynthesizingAudio.collectAsStateWithLifecycle()
    val isVideoGenerating by viewModel.isVideoGenerating.collectAsStateWithLifecycle()
    val audioProgress by viewModel.audioProgress.collectAsStateWithLifecycle()
    val videoProgress by viewModel.videoProgress.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val videoGenerationError by viewModel.videoGenerationError.collectAsStateWithLifecycle()

    val activeTracks by viewModel.activeProjectTracks.collectAsStateWithLifecycle()
    val isRecordingState by viewModel.isRecordingState.collectAsStateWithLifecycle()
    val recordingTrackId by viewModel.recordingTrackId.collectAsStateWithLifecycle()

    val isExportingProject by viewModel.isExportingProject.collectAsStateWithLifecycle()
    val exportProjectProgress by viewModel.exportProjectProgress.collectAsStateWithLifecycle()

    val isGeneratingCompleteSong by viewModel.isGeneratingCompleteSong.collectAsStateWithLifecycle()
    val completeSongProgress by viewModel.completeSongProgress.collectAsStateWithLifecycle()
    val userCredits by viewModel.userCredits.collectAsStateWithLifecycle()
    val communitySalesAlert by viewModel.communitySalesAlert.collectAsStateWithLifecycle()

    var topicInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var activeMidiTrackToEdit by remember { mutableStateOf<com.example.data.database.AudioTrack?>(null) }
    var trackForEffectsRack by remember { mutableStateOf<com.example.data.database.AudioTrack?>(null) }

    if (communitySalesAlert != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSalesAlert() },
            title = {
                Text(
                    text = "🪙 SKLADBA PRODÁNA!",
                    fontWeight = FontWeight.Bold,
                    color = AccentNeonCyan,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = communitySalesAlert ?: "",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearSalesAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                ) {
                    Text("Paráda!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF140D2F)
        )
    }

    if (activeProj == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF150D2E)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2C1E55), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Project Settings",
                                tint = AccentNeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SPRÁVCE PROJEKTŮ  🎛️",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Vytvářejte nové skladby, spravujte a rychle přepínejte mezi rozpracovanými projekty. Všechny vaše lyrics, vygenerované stopy a nastavení atmosféry jsou bezpečně a automaticky ukládány.",
                            fontSize = 12.sp,
                            color = Color(0xFF908DA1),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Create New Project Action card
            item {
                Button(
                    onClick = { isCreateDialogOpen = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("create_project_manager_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentNeonCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New project",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VYTVOŘIT NOVÝ HUDEBNÍ PROJEKT  ➕",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Search Filter Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Vyhledat píseň podle názvu...", color = Color(0xFF8E8CA4), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Hledat",
                            tint = Color(0xFF8E8CA4)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Smazat text",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentNeonCyan,
                        unfocusedBorderColor = Color(0xFF261D45)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_search_input")
                )
            }

            // Filtered projects selector
            val filteredProjects = allProjects.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }

            if (filteredProjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Žádné projekty",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (allProjects.isEmpty()) "Nemáte žádné uložené projekty. Klikněte na tlačítko nahoře pro vytvoření!" else "Nebyly nalezeny žádné písně odpovídající hledání.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredProjects) { proj ->
                    val formattedDate = try {
                        java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(proj.timestamp))
                    } catch (e: Exception) {
                        ""
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF231846), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectProject(proj, context) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF21133B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Project icon",
                                        tint = AccentNeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = proj.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = proj.genre,
                                            color = AccentNeonCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = " • ${proj.vocalEffect} • " + (if (proj.backgroundAmbience == "None") "Žádné ambient" else proj.backgroundAmbience),
                                            color = Color(0xFF8E8CA4),
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (formattedDate.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "Uloženo: $formattedDate",
                                            color = Color(0xFF5E5A7A),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0F2622))
                                        .border(0.5.dp, AccentNeonCyan, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("AUTO-SAVE", fontSize = 8.sp, color = AccentNeonCyan, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deleteProject(proj) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Smazat projekt",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isCreateDialogOpen) {
            var newTitleInput by remember { mutableStateOf("") }
            var newGenreInput by remember { mutableStateOf("Pop") }
            val genresList = listOf("Pop", "Rock", "HipHop", "Cinematic", "Synthwave", "Lo-Fi", "Metal", "EDM", "Country")

            AlertDialog(
                onDismissRequest = { isCreateDialogOpen = false },
                title = { Text(text = "Nový hudební projekt  🎵", color = Color.White, fontWeight = FontWeight.Bold) },
                containerColor = CardBackground,
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTitleInput,
                            onValueChange = { newTitleInput = it },
                            label = { Text("Název písně", color = Color(0xFF8E8CA4)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentNeonCyan,
                                unfocusedBorderColor = Color(0xFF261D45)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Zvolte hudební žánr:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            genresList.forEach { g ->
                                val isSel = newGenreInput == g
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentNeonCyan else Color(0xFF1E1339))
                                        .clickable { newGenreInput = g }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g,
                                        color = if (isSel) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitleInput.isNotBlank()) {
                                viewModel.createNewProject(newTitleInput, "", newGenreInput, "Normal", "None")
                                isCreateDialogOpen = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                    ) {
                        Text("Vytvořit projekt", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isCreateDialogOpen = false }) {
                        Text("Zrušit", color = Color.White)
                    }
                }
            )
        }
    } else {
        val proj = activeProj!!

        // JETPACK COMPOSE STEP SEQUENCER DIALOG / PIANO ROLL OVERLAY
        val track = activeMidiTrackToEdit
        if (track != null) {
            val seqScope = rememberCoroutineScope()
            var selectedInst by remember(track.trackId) { mutableStateOf(track.midiInstrument ?: "Sine Lead") }
            val cells = remember(track.trackId, track.midiPattern) {
                val map = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()
                val rawPattern = track.midiPattern
                if (!rawPattern.isNullOrBlank()) {
                    for (cell in rawPattern.split(",")) {
                        if (cell.isNotBlank()) {
                            map[cell] = true
                        }
                    }
                }
                map
            }

            // Real-time Sequencing & Recording State
            var isPlayingLoop by remember { mutableStateOf(false) }
            var isRecordingSeq by remember { mutableStateOf(false) }
            var currentStep by remember { mutableStateOf(-1) }
            val metronomeEnabled = remember { mutableStateOf(true) }
            val originalIndices = com.example.util.MidiSynthesizer.NOTES.indices.reversed().toList()

            // Sequencing playback/recording timer loop
            LaunchedEffect(isPlayingLoop, isRecordingSeq, proj.bpm) {
                if (isPlayingLoop || isRecordingSeq) {
                    val actualBpm = proj.bpm.coerceAtLeast(40)
                    val stepDurationMs = ((60f / actualBpm) / 4f * 1000f).toLong()
                    currentStep = 0
                    while (isPlayingLoop || isRecordingSeq) {
                        // 1. Play active notes on currentStep
                        val activeNoteIndices = originalIndices.filter { idx ->
                            cells["${currentStep}_$idx"] == true
                        }
                        if (activeNoteIndices.isNotEmpty()) {
                            seqScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    val stepFile = File(context.cacheDir, "midi_step_${track.trackId}_temp.wav")
                                    val stepPattern = activeNoteIndices.joinToString(",") { "0_$it" }
                                    com.example.util.MidiSynthesizer.synthesizeMidiToWav(
                                        instrument = selectedInst,
                                        patternStr = stepPattern,
                                        outputFile = stepFile,
                                        trackDuration = 1
                                    )
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        try {
                                            android.media.MediaPlayer().apply {
                                                setDataSource(stepFile.absolutePath)
                                                prepare()
                                                start()
                                                setOnCompletionListener { mp -> mp.release() }
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("MidiSequencer", "Step playback failed", e)
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MidiSequencer", "Step synthesis failed", e)
                                }
                            }
                        }

                        // 2. Metronome click on major beats (0, 4, 8, 12)
                        if (metronomeEnabled.value && currentStep % 4 == 0) {
                            seqScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    val clickFile = File(context.cacheDir, "metronome_click.wav")
                                    com.example.util.MidiSynthesizer.synthesizeMidiToWav("Tri Pluck", "0_7", clickFile, 1)
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        try {
                                            android.media.MediaPlayer().apply {
                                                setDataSource(clickFile.absolutePath)
                                                setVolume(0.12f, 0.12f)
                                                prepare()
                                                start()
                                                setOnCompletionListener { mp -> mp.release() }
                                            }
                                        } catch (e: Exception) {}
                                    }
                                } catch (e: Exception) {}
                            }
                        }

                        kotlinx.coroutines.delay(stepDurationMs)
                        currentStep = (currentStep + 1) % 16
                    }
                } else {
                    currentStep = -1
                }
            }

            AlertDialog(
                onDismissRequest = { 
                    isPlayingLoop = false
                    isRecordingSeq = false
                    activeMidiTrackToEdit = null 
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NÁSTROJOVÝ SEKVENCER 🎹",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${track.name} • ${proj.bpm} BPM",
                                color = Color(0xFF8D8A9F),
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = { 
                            isPlayingLoop = false
                            isRecordingSeq = false
                            activeMidiTrackToEdit = null 
                        }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Zavřít", tint = Color.White)
                        }
                    }
                },
                containerColor = Color(0xFF160E2E),
                text = {
                    Column {
                        // MIDI ENGINE CONTROLS ROW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF261D45), RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0B24))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play/Stop Loop
                            Button(
                                onClick = {
                                    isRecordingSeq = false
                                    isPlayingLoop = !isPlayingLoop
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlayingLoop) Color.Red else Color(0xFF00FFCC)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isPlayingLoop) "STOP ⏹️" else "PŘEHRÁT 🔄",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Real-time RECORD Buttons
                            Button(
                                onClick = {
                                    isPlayingLoop = false
                                    isRecordingSeq = !isRecordingSeq
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecordingSeq) Color(0xFFFF3B30) else Color(0xFF1C143B)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isRecordingSeq) "REK NAŽIVO ⏹️" else "NAHRÁVAT 🔴",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Metronome Switch
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = metronomeEnabled.value,
                                    onCheckedChange = { metronomeEnabled.value = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AccentNeonCyan,
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Text("Metronom 🥁", color = Color.White, fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Instrument picker row
                        Text("Zvolit virtuální nástroj:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Sine Lead", "Chiptune Pulse", "Retro Saw", "Tri Pluck").forEach { inst ->
                                val isSel = selectedInst == inst
                                Button(
                                    onClick = {
                                        selectedInst = inst
                                        val patternSerialized = cells.keys.joinToString(",")
                                        viewModel.updateMidiTrackPattern(context, track, inst, patternSerialized)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) AccentNeonCyan else Color(0xFF1C143B)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when(inst) {
                                            "Sine Lead" -> "Sine 🔊"
                                            "Chiptune Pulse" -> "Pulse 👾"
                                            "Retro Saw" -> "Saw 🎸"
                                            "Tri Pluck" -> "Tri 📯"
                                            else -> inst
                                        },
                                        color = if (isSel) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Templates row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rychlé šablony:", color = Color.LightGray, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Arpeggio template
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2C1E55))
                                        .clickable {
                                            cells.clear()
                                            for (i in 0 until 16) {
                                                cells["${i}_${i % 8}"] = true
                                            }
                                            val patternSerialized = cells.keys.joinToString(",")
                                            viewModel.updateMidiTrackPattern(context, track, selectedInst, patternSerialized)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Arpeggio 📈", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                // Bass loop template
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2C1E55))
                                        .clickable {
                                            cells.clear()
                                            for (i in 0 until 16 step 2) {
                                                cells["${i}_0"] = true // C4 bass pulse
                                                if (i % 4 == 2) cells["${i}_4"] = true
                                            }
                                            val patternSerialized = cells.keys.joinToString(",")
                                            viewModel.updateMidiTrackPattern(context, track, selectedInst, patternSerialized)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Bass Beat 🕺", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                // Techno loop template
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2C1E55))
                                        .clickable {
                                            cells.clear()
                                            for (step in 0..15 step 4) {
                                                cells["${step}_0"] = true
                                                cells["${step}_2"] = true
                                            }
                                            for (step in 2..15 step 4) {
                                                cells["${step}_4"] = true
                                            }
                                            val patternSerialized = cells.keys.joinToString(",")
                                            viewModel.updateMidiTrackPattern(context, track, selectedInst, patternSerialized)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Techno ⚡", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                // Ambient loop template
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2C1E55))
                                        .clickable {
                                            cells.clear()
                                            for (step in 0..15) {
                                                if (step < 8) {
                                                    cells["${step}_0"] = true
                                                    cells["${step}_2"] = true
                                                    cells["${step}_4"] = true
                                                    cells["${step}_7"] = true
                                                } else {
                                                    cells["${step}_1"] = true
                                                    cells["${step}_3"] = true
                                                    cells["${step}_5"] = true
                                                    cells["${step}_6"] = true
                                                }
                                            }
                                            val patternSerialized = cells.keys.joinToString(",")
                                            viewModel.updateMidiTrackPattern(context, track, selectedInst, patternSerialized)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Ambience 🌌", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid container
                        val scrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF2C1E55), RoundedCornerShape(8.dp))
                                .background(Color(0xFF100826))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.horizontalScroll(scrollState)
                            ) {
                                // Column headers: step numbers 1 to 16
                                Row(modifier = Modifier.padding(start = 45.dp)) {
                                    for (step in 0 until 16) {
                                        val isCurrentPlayhead = step == currentStep
                                        Box(
                                            modifier = Modifier.width(30.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (step + 1).toString(),
                                                color = if (isCurrentPlayhead) Color.Red else if (step % 4 == 0) AccentNeonCyan else Color.Gray,
                                                fontSize = 9.sp,
                                                fontWeight = if (isCurrentPlayhead || step % 4 == 0) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Draw notes rows from top (E5) down to bottom (C4)
                                val reversedNotes = com.example.util.MidiSynthesizer.NOTES.reversed()
                                val originalIndices = com.example.util.MidiSynthesizer.NOTES.indices.reversed().toList()

                                reversedNotes.forEachIndexed { displayIdx, noteName ->
                                    val origNoteIdx = originalIndices[displayIdx]
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        // Note label button (plays note and records dynamically if record mode ON)
                                        Box(
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(26.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (noteName.startsWith("C")) Color(0xFF2E1C5E) else Color(0xFF1A1235))
                                                .clickable {
                                                    // Dynamic Key recording!
                                                    if (isRecordingSeq && currentStep in 0..15) {
                                                        val recordCellKey = "${currentStep}_$origNoteIdx"
                                                        cells[recordCellKey] = true
                                                        val patternSerialized = cells.keys.joinToString(",")
                                                        viewModel.updateMidiTrackPattern(context, track, selectedInst, patternSerialized)
                                                    }

                                                    // Preview note sound by synthesizing a 250ms preview
                                                    val cacheDir = context.cacheDir
                                                    val previewFile = File(cacheDir, "midi_note_preview.wav")
                                                    seqScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                        try {
                                                            com.example.util.MidiSynthesizer.synthesizeMidiToWav(
                                                                instrument = selectedInst,
                                                                patternStr = "0_$origNoteIdx",
                                                                outputFile = previewFile,
                                                                trackDuration = 1
                                                            )
                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                try {
                                                                    android.media.MediaPlayer().apply {
                                                                        setDataSource(previewFile.absolutePath)
                                                                        prepare()
                                                                        start()
                                                                        setOnCompletionListener { mp -> mp.release() }
                                                                    }
                                                                } catch(e: Exception) {
                                                                    android.util.Log.e("MidiPreview", "Error playing preview note: ${e.message}")
                                                                }
                                                            }
                                                        } catch(ep: Exception) {
                                                            android.util.Log.e("MidiPreview", "Error synthesizing preview: ${ep.message}")
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(noteName, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.width(3.dp))

                                        // Draw the 16 steps row cells with sweeping playhead visualization
                                        for (step in 0 until 16) {
                                            val cellKey = "${step}_${origNoteIdx}"
                                            val isActive = cells[cellKey] ?: false
                                            val isSweepingPlayhead = step == currentStep

                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp)
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isActive) {
                                                            when (selectedInst) {
                                                                "Chiptune Pulse" -> if (isSweepingPlayhead) Color.White else Color(0xFFFF0055)
                                                                "Retro Saw" -> if (isSweepingPlayhead) Color.White else Color(0xFFFFD700)
                                                                "Tri Pluck" -> if (isSweepingPlayhead) Color.White else AccentNeonCyan
                                                                else -> if (isSweepingPlayhead) Color.White else Color(0xFF9D4EDD) // Sine Lead
                                                            }
                                                        } else {
                                                            if (isSweepingPlayhead) {
                                                                Color(0xFF3E2D75)
                                                            } else {
                                                                if (step % 4 == 0) Color(0xFF1E153A) else Color(0xFF140D2B)
                                                            }
                                                        }
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSweepingPlayhead) Color.Red else if (isActive) Color.White else Color(0xFF261A4D),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        if (isActive) {
                                                            cells.remove(cellKey)
                                                        } else {
                                                            cells[cellKey] = true
                                                        }
                                                        val patternSerialized = cells.keys.joinToString(",")
                                                        viewModel.updateMidiTrackPattern(context, track, selectedInst, patternSerialized)
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "* Tip pro naživo: Klepněte na 'NAHRÁVAT 🔴' pro spuštění smyčky a klepejte na názvy tónů (např. C5) vlevo. Tón se zaznamená přesně v daném kroku!",
                            color = Color(0xFF8E8CA4),
                            fontSize = 9.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            cells.clear()
                            viewModel.updateMidiTrackPattern(context, track, selectedInst, "")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                    ) {
                        Text("Smazat 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { 
                            isPlayingLoop = false
                            isRecordingSeq = false
                            activeMidiTrackToEdit = null 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                    ) {
                        Text("Hotovo ✔️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        var lyricsState by remember(proj.id) { mutableStateOf(proj.lyrics) }
        var genreState by remember(proj.id) { mutableStateOf(proj.genre) }
        var vocalState by remember(proj.id) { mutableStateOf(proj.vocalEffect) }
        var ambienceState by remember(proj.id) { mutableStateOf(proj.backgroundAmbience) }

        LaunchedEffect(activeProj) {
            val p = activeProj
            if (p != null) {
                lyricsState = p.lyrics
                genreState = p.genre
                vocalState = p.vocalEffect
                ambienceState = p.backgroundAmbience
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.selectProject(null) }, // Close active project and go back to list
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1339))
                    ) {
                        Text("< Zpět do seznamu", color = Color.White, fontSize = 12.sp)
                    }
                    Text(
                        text = proj.title,
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonCyan,
                        fontSize = 18.sp
                    )
                }
            }

            // Background State Persistence & Autosave Dashboard Section
            item {
                val lastAutosaveTime by viewModel.lastAutosaveTime.collectAsStateWithLifecycle()
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B24)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF261D45), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (lastAutosaveTime > 0) Color(0xFF00FFCC) else Color.Yellow)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lokální zálohování na pozadí (30s)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "STAV: AKTIVNÍ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FFCC)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (lastAutosaveTime > 0) {
                                        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                            .format(java.util.Date(lastAutosaveTime))
                                        "Poslední záloha uložena v $timeStr"
                                    } else {
                                        "Čekání na první automatické uložení..."
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8CA4)
                                )
                                Text(
                                    text = "Úložiště: SharedPreferences (Zrcadlo LocalStorage)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF5D5870)
                                )
                            }
                            
                            Button(
                                onClick = {
                                    val success = viewModel.restoreProjectFromLocalStorage(context)
                                    if (!success) {
                                        Toast.makeText(context, "Žádná záloha k obnovení nebyla nalezena.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B163B)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "Načíst zálohu 🔄",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // --- NOVOST V30: AI CLOUD SONG GENERATOR ---
            item {
                var aiSongTopic by remember { mutableStateOf("") }
                var aiSongGenre by remember { mutableStateOf("Synthwave") }
                val aiSongGenres = listOf("Pop", "Rock", "Synthwave", "EDM", "Lo-Fi", "Acoustic")

                var showAdvancedModal by remember { mutableStateOf(false) }
                var aiPromptStyle by remember { mutableStateOf("") }
                var aiNegativePrompt by remember { mutableStateOf("") }
                var aiSoundInfluence by remember { mutableStateOf(80f) }
                var aiStyleInfluence by remember { mutableStateOf(75f) }
                var aiVocalVoice by remember { mutableStateOf("Human") } // Human, Duet, Vocaloid, Robot
                var aiCustomLyrics by remember { mutableStateOf("") }

                val coroutineScope = rememberCoroutineScope()
                var isPreGeneratingLyrics by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, AccentNeonCyan, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                                contentDescription = "AI Generate",
                                tint = AccentNeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CLOUD GENERÁTOR SKLADEB (Spark AI)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Napište téma a zvolte styl. Vygeneruje se kompletní hotový song (hudba + doprovod + zpěv/vokály), který se automaticky načte jako hlavní stopa.",
                            fontSize = 12.sp,
                            color = Color(0xFF908DAF)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Topic Prompt
                        Text(
                            text = "NÁPAD / TÉMA SKLADBY:",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = aiSongTopic,
                            onValueChange = { aiSongTopic = it },
                            placeholder = { Text("např. jízda nočním městem, zlomené srdce, letní vibes...", fontSize = 12.sp, color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentNeonCyan,
                                unfocusedBorderColor = Color(0xFF261D45),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                             ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_song_topic_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Style/Genre selection
                        Text(
                            text = "ZÁKLADNÍ ŽÁNR SKLADBY:",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            aiSongGenres.take(3).forEach { g ->
                                val isSelected = aiSongGenre == g
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentNeonCyan else Color(0xFF1E133F))
                                        .clickable { aiSongGenre = g }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            aiSongGenres.drop(3).forEach { g ->
                                val isSelected = aiSongGenre == g
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentNeonCyan else Color(0xFF1E133F))
                                        .clickable { aiSongGenre = g }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trigger to open Advanced Generation Modal
                        Button(
                            onClick = { showAdvancedModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E133F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                                contentDescription = "Advanced",
                                tint = AccentNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "POKROČILÁ GENERACE ⚙️",
                                color = AccentNeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Display a summary of active advanced settings if they are non-default
                        val isCustomized = aiPromptStyle.isNotBlank() ||
                                           aiNegativePrompt.isNotBlank() ||
                                           aiCustomLyrics.isNotBlank() ||
                                           aiSoundInfluence != 80f ||
                                           aiStyleInfluence != 75f ||
                                           aiVocalVoice != "Human"

                        if (isCustomized) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E133F)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF261D45), RoundedCornerShape(10.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Aktivní pokročilé parametry syntézy:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentNeonCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (aiCustomLyrics.isNotBlank()) {
                                        Text("• Vlastní text (Lyrics): ${if(aiCustomLyrics.length > 30) aiCustomLyrics.take(30) + "..." else aiCustomLyrics}", fontSize = 10.sp, color = Color(0xFFE5E2F5))
                                    }
                                    if (aiPromptStyle.isNotBlank()) {
                                        Text("• Prompt stylu: $aiPromptStyle", fontSize = 10.sp, color = Color(0xFFE5E2F5))
                                    }
                                    if (aiNegativePrompt.isNotBlank()) {
                                        Text("• Negativní prompt: $aiNegativePrompt", fontSize = 10.sp, color = Color(0xFFE5E2F5))
                                    }
                                    Text("• Voice Influence: ${aiSoundInfluence.toInt()}%", fontSize = 10.sp, color = Color(0xFFE5E2F5))
                                    Text("• Style Influence: ${aiStyleInfluence.toInt()}%", fontSize = 10.sp, color = Color(0xFFE5E2F5))
                                    Text("• Charakter hlasu: $aiVocalVoice", fontSize = 10.sp, color = Color(0xFFE5E2F5))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Resetovat na výchozí",
                                        color = Color.Red,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                aiCustomLyrics = ""
                                                aiPromptStyle = ""
                                                aiNegativePrompt = ""
                                                aiSoundInfluence = 80f
                                                aiStyleInfluence = 75f
                                                aiVocalVoice = "Human"
                                                Toast.makeText(context, "Nastavení resetováno!", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // 'Advanced Generation' modal
                        if (showAdvancedModal) {
                            var tempCustomLyrics by remember { mutableStateOf(aiCustomLyrics) }
                            var tempPromptStyle by remember { mutableStateOf(aiPromptStyle) }
                            var tempNegativePrompt by remember { mutableStateOf(aiNegativePrompt) }
                            var tempSoundInfluence by remember { mutableStateOf(aiSoundInfluence) }
                            var tempStyleInfluence by remember { mutableStateOf(aiStyleInfluence) }
                            var tempVocalVoice by remember { mutableStateOf(aiVocalVoice) }
                            var isTempPreGeneratingLyrics by remember { mutableStateOf(false) }

                            AlertDialog(
                                onDismissRequest = { showAdvancedModal = false },
                                containerColor = Color(0xFF140D2F),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, AccentNeonCyan, RoundedCornerShape(20.dp)),
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                                            contentDescription = "Pokročilá generace",
                                            tint = AccentNeonCyan,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = "ADVANCED GENERATION ⚙️",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 400.dp) // Bound the maximum height to prevent screen overflow
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            text = "Zde můžete jemně vyladit parametry AI generátoru hudby, specifikovat nežádoucí zvuky, upravit procento vlivu prvků nebo vložit vlastní text skladby.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF908DAF)
                                        )

                                        // 1. Lyrics Input
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "TEXT SKLADBY / LYRICS (Vlastní text):",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isTempPreGeneratingLyrics) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(12.dp),
                                                        color = AccentNeonCyan,
                                                        strokeWidth = 1.dp
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Předgenerovat text 🪄",
                                                        color = AccentNeonCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .clickable {
                                                                coroutineScope.launch {
                                                                    isTempPreGeneratingLyrics = true
                                                                    try {
                                                                        val lyricsTopic = if (aiSongTopic.isNotBlank()) aiSongTopic else "noční jízda"
                                                                        val response = com.example.data.network.GeminiClient.generateSongLyrics(lyricsTopic, aiSongGenre)
                                                                        tempCustomLyrics = response
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(context, "Služba chybovala: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                    } finally {
                                                                        isTempPreGeneratingLyrics = false
                                                                    }
                                                                }
                                                            }
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = tempCustomLyrics,
                                                onValueChange = { tempCustomLyrics = it },
                                                placeholder = { Text("Zadejte vlastní kompletní text (sloky, refrén...), který AI přezpívá, nebo si jej nechte předgenerovat tlačítkem výše.", fontSize = 11.sp, color = Color.Gray) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AccentNeonCyan,
                                                    unfocusedBorderColor = Color(0xFF261D45),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                maxLines = 6,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        // 2. Style Prompt Input
                                        Column {
                                            Text(
                                                text = "DETAILNÍ PROMPT STYLU / STYLE PROMPT:",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = tempPromptStyle,
                                                onValueChange = { tempPromptStyle = it },
                                                placeholder = { Text("např. heavy vintage analog synth, wide chorus vocals, retro beats...", fontSize = 11.sp, color = Color.Gray) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AccentNeonCyan,
                                                    unfocusedBorderColor = Color(0xFF261D45),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        // 3. Negative Prompt Input
                                        Column {
                                            Text(
                                                text = "NEŽÁDOUCÍ ZVUKY / NEGATIVE PROMPT:",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = tempNegativePrompt,
                                                onValueChange = { tempNegativePrompt = it },
                                                placeholder = { Text("např. sharp metallic static noise, distorted clicks, compression artifacts...", fontSize = 11.sp, color = Color.Gray) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AccentNeonCyan,
                                                    unfocusedBorderColor = Color(0xFF261D45),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        // 4. Voice Influence Percentage Slider
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "VOICE INFLUENCE (VLIV HLASU / DOPROVODU):",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${tempSoundInfluence.toInt()}%",
                                                    color = AccentNeonCyan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Slider(
                                                value = tempSoundInfluence,
                                                onValueChange = { tempSoundInfluence = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = AccentNeonCyan,
                                                    activeTrackColor = AccentNeonCyan,
                                                    inactiveTrackColor = Color(0xFF261D45)
                                                )
                                            )
                                        }

                                        // 5. Style Influence Percentage Slider
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "STYLE INFLUENCE (VLIV INTENZITY STYLU):",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${tempStyleInfluence.toInt()}%",
                                                    color = AccentNeonCyan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Slider(
                                                value = tempStyleInfluence,
                                                onValueChange = { tempStyleInfluence = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = AccentNeonCyan,
                                                    activeTrackColor = AccentNeonCyan,
                                                    inactiveTrackColor = Color(0xFF261D45)
                                                )
                                            )
                                        }

                                        // 6. Character / Model Selection
                                        Column {
                                            Text(
                                                text = "CHARAKTER AI HLASU / VOICE MODEL:",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                listOf(
                                                    "Human" to "🎤 Lidský",
                                                    "Duet" to "👥 Duet",
                                                    "Vocaloid" to "⚡ Vocaloid",
                                                    "Robot" to "🤖 Robot"
                                                ).forEach { (code, label) ->
                                                    val isVoiceSel = tempVocalVoice == code
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isVoiceSel) AccentNeonCyan else Color(0xFF1E133F))
                                                            .clickable { tempVocalVoice = code }
                                                            .padding(vertical = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isVoiceSel) Color.Black else Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            aiCustomLyrics = tempCustomLyrics
                                            aiPromptStyle = tempPromptStyle
                                            aiNegativePrompt = tempNegativePrompt
                                            aiSoundInfluence = tempSoundInfluence
                                            aiStyleInfluence = tempStyleInfluence
                                            aiVocalVoice = tempVocalVoice
                                            showAdvancedModal = false
                                            Toast.makeText(context, "Nastavení úspěšně použito!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                                    ) {
                                        Text("POUŽÍT NASTAVENÍ", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showAdvancedModal = false }
                                    ) {
                                        Text("ZRUŠIT", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isGeneratingCompleteSong) {
                            val activeStep = when {
                                completeSongProgress < 0.25f -> "1/4 🔮 Gemini analyzuje koncept a tvoří kompletní text..."
                                completeSongProgress < 0.60f -> "2/4 🎸 Skládání syntezátorových harmonií a basy..."
                                completeSongProgress < 0.85f -> "3/4 🎤 Syntéza AI zpěvu a vocaloidních formátů..."
                                else -> "4/4 🎛️ Finální mastering mixu stopy..."
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = activeStep,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentNeonCyan
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = completeSongProgress,
                                    color = AccentNeonCyan,
                                    trackColor = Color(0xFF261D45),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(completeSongProgress * 100).toInt()}% dokončeno",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.generateCompleteSong(
                                        context = context,
                                        topic = aiSongTopic,
                                        selectedGenre = aiSongGenre,
                                        promptStyle = aiPromptStyle,
                                        negativePrompt = aiNegativePrompt,
                                        soundInfluencePercent = aiSoundInfluence,
                                        styleInfluencePercent = aiStyleInfluence,
                                        vocalVoice = aiVocalVoice,
                                        customLyrics = aiCustomLyrics
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_ai_song_gen"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "AI GENEROVAT PLNOU SKLADBU (Cloud API) ⚡",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // A. Lyrics Generator section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF261D45), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = TranslationUtility.get("lyrics_tab"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = lyricsState,
                            onValueChange = {
                                lyricsState = it
                                viewModel.updateActiveProject(it, genreState, vocalState, ambienceState)
                            },
                            placeholder = { Text(TranslationUtility.get("lyrics_placeholder"), color = Color(0xFF8E8CA4)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentNeonCyan,
                                unfocusedBorderColor = Color(0xFF231846)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            textStyle = TextStyle(fontSize = 13.sp, color = Color.White, lineHeight = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(TranslationUtility.get("topic_prompt"), color = Color(0xFF8D8A9F), fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = topicInput,
                                onValueChange = { topicInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lyrics_topic_input"),
                                singleLine = true,
                                placeholder = { Text("Láska, slunce, rap, hory...", fontSize = 12.sp, color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentNeonCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.generateAILyrics(topicInput, genreState)
                                },
                                enabled = !isGeneratingLyrics && topicInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                modifier = Modifier.height(52.dp)
                            ) {
                                if (isGeneratingLyrics) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(TranslationUtility.get("generate_lyrics_btn"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // B. Sound options and Mix setting
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF261D45), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = TranslationUtility.get("vibe_setup"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Genres
                        Text(TranslationUtility.get("select_genre"), color = Color(0xFF8E8CA4), fontSize = 12.sp)
                        val genres = listOf("Pop", "Rock", "HipHop", "Cinematic", "Synthwave", "Lo-Fi", "Metal", "EDM", "Country")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            genres.forEach { g ->
                                val isSel = genreState == g
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentNeonCyan else Color(0xFF1B1233))
                                        .clickable {
                                            genreState = g
                                            viewModel.updateActiveProject(lyricsState, g, vocalState, ambienceState)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(g, color = if (isSel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Vocal Filters
                        Text(TranslationUtility.get("vocal_effect"), color = Color(0xFF8E8CA4), fontSize = 12.sp)
                        val vocals = listOf("Normal", "Robot", "Eco", "HighPitch")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            vocals.forEach { v ->
                                val isSel = vocalState == v
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentNeonCyan else Color(0xFF1B1233))
                                        .clickable {
                                            vocalState = v
                                            viewModel.updateActiveProject(lyricsState, genreState, v, ambienceState)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(v, color = if (isSel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. Ambience sound
                        Text(TranslationUtility.get("bg_ambience"), color = Color(0xFF8E8CA4), fontSize = 12.sp)
                        val ambiences = listOf("None", "Rain", "Cafe", "Forest")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ambiences.forEach { a ->
                                val isSel = ambienceState == a
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentNeonCyan else Color(0xFF1B1233))
                                        .clickable {
                                            ambienceState = a
                                            viewModel.updateActiveProject(lyricsState, genreState, vocalState, a)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(a, color = if (isSel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // C. Offline procedural synthesizer & Dynamic Waveform player
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Display bouncing waves if playing
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFF090412), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlaying) {
                                LiveFrequencySpectrumWave()
                            } else {
                                Text(
                                    text = if (isSynthesizingAudio) "${TranslationUtility.get("creating_audio")} (${(audioProgress * 100).toInt()}%)" else "Syntezátor připraven",
                                    color = Color(0xFF8E8CA4),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.generateAIMusicInCloud(context) },
                            enabled = !isSynthesizingAudio,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE), contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("generate_cloud_music_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSynthesizingAudio) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("GENEROVAT AI PODKRES V CLOUDU ✨", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isPlaying) {
                                Button(
                                    onClick = { viewModel.stopAudioPlayback() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(TranslationUtility.get("stop_music"), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.playSynthesizedAudio(context) },
                                    enabled = !isSynthesizingAudio,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC2), contentColor = Color.Black),
                                    modifier = Modifier.weight(1f).testTag("play_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isSynthesizingAudio) {
                                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text(TranslationUtility.get("play_music"), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // E. MULTI-TRACK AUDIO RECORDER & MIXER INTERFACE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF261D45), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "VÍCÉSTOPÉ NAHRÁVÁNÍ & MIXÁŽNY PULT 🎛️",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Nahrávejte vlastní zpěv do vrstev a míchejte hlasitost",
                                    color = Color(0xFF8D8A9F),
                                    fontSize = 11.sp
                                )
                            }
                            
                            var isAddTrackDialogOpen by remember { mutableStateOf(false) }
                            
                            IconButton(onClick = { isAddTrackDialogOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Přidat stopu",
                                    tint = AccentNeonCyan
                                )
                            }

                            if (isAddTrackDialogOpen) {
                                var newTrackName by remember { mutableStateOf("") }
                                var selectedTrackType by remember { mutableStateOf("Vocal") }

                                AlertDialog(
                                    onDismissRequest = { isAddTrackDialogOpen = false },
                                    title = { Text("Přidat novou zvukovou stopu", color = Color.White) },
                                    containerColor = Color(0xFF1E1339),
                                    text = {
                                        Column {
                                            OutlinedTextField(
                                                value = newTrackName,
                                                onValueChange = { newTrackName = it },
                                                label = { Text("Název stopy (např. Doprovodné vocals)", color = Color.Gray) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AccentNeonCyan,
                                                    unfocusedBorderColor = Color(0xFF261D45),
                                                    focusedContainerColor = Color(0xFF160E2E),
                                                    unfocusedContainerColor = Color(0xFF160E2E)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("Typ stopy:", color = Color.White, fontSize = 12.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                listOf("Vocal", "MIDI", "Synth", "Ambience").forEach { type ->
                                                    val isSel = selectedTrackType == type
                                                    Button(
                                                        onClick = { selectedTrackType = type },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isSel) AccentNeonCyan else Color(0xFF130D26)
                                                        )
                                                    ) {
                                                        Text(type, color = if (isSel) Color.Black else Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                if (newTrackName.isNotBlank()) {
                                                    viewModel.addCustomTrack(proj.id, newTrackName, selectedTrackType)
                                                    isAddTrackDialogOpen = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                                        ) {
                                            Text("Přidat", fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { isAddTrackDialogOpen = false }) {
                                            Text("Zrušit", color = Color.White)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sync play/stop all button row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.playMultiTrack(context, activeTracks) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black),
                                modifier = Modifier.weight(1f).testTag("mix_play_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("PŘEHRÁT VŠECHNY STOPY ▶️", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.stopAudioPlayback() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("mix_stop_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("STOP VŠECHHY ⏹️", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val globalPlayProgress = if (isPlaying) {
                            val durationSec = proj.trackDuration.coerceAtLeast(1)
                            val transition = rememberInfiniteTransition(label = "global_playhead")
                            val elapsedMs by transition.animateFloat(
                                initialValue = 0f,
                                targetValue = (durationSec * 1000).toFloat(),
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationSec * 1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "playback_sweep"
                            )
                            elapsedMs / (durationSec * 1000f)
                        } else {
                            0f
                        }

                        val timelineClips by viewModel.timelineClips.collectAsStateWithLifecycle()
                        
                        com.example.ui.components.InteractiveTimeline(
                            viewModel = viewModel,
                            project = proj,
                            activeTracks = activeTracks,
                            videoClips = timelineClips,
                            isPlaying = isPlaying,
                            globalProgress = globalPlayProgress
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Audio track list
                        if (activeTracks.isEmpty()) {
                            Text("Žádné stopy nenalezeny.", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            activeTracks.forEach { track ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130D26)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(0.5.dp, Color(0xFF241648), RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = when (track.trackType) {
                                                        "Beat" -> "🎵"
                                                        "Vocal" -> "🎙️"
                                                        "Ambience" -> "🌧️"
                                                        else -> "🎹"
                                                    },
                                                    fontSize = 16.sp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = track.name,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = if (!track.filePath.isNullOrEmpty() && File(track.filePath).exists()) {
                                                            "Obsahuje data 🟢"
                                                        } else {
                                                            "Prázdná stopa 🔴"
                                                        },
                                                        color = if (!track.filePath.isNullOrEmpty() && File(track.filePath).exists()) Color.Green else Color.Red,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            // Recording actions (microphone for inputs)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (track.trackType == "MIDI") {
                                                    Button(
                                                        onClick = { activeMidiTrackToEdit = track },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(30.dp).testTag("midi_sequencer_open_btn"),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text("SEKVENCER 🎹", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }

                                                if (track.trackType == "Vocal") {
                                                    val isThisRecording = isRecordingState && recordingTrackId == track.trackId
                                                    if (isThisRecording) {
                                                        Button(
                                                            onClick = { viewModel.stopRecording(context) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(30.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(8.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color.White)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("STOP ⏹️", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                            }
                                                        }
                                                    } else {
                                                        val micPermissionLauncher = rememberLauncherForActivityResult(
                                                            contract = ActivityResultContracts.RequestPermission()
                                                        ) { isGranted ->
                                                            if (isGranted) {
                                                                viewModel.startRecording(context, track)
                                                            } else {
                                                                Toast.makeText(context, "Je vyžadován přístup k mikrofonu!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }

                                                        Button(
                                                            onClick = {
                                                                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                                            },
                                                            enabled = !isRecordingState,
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(30.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text("NAHRÁVAT 🎙️", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }

                                                // Clean custom tracks delete
                                                if (track.trackId > 3) {
                                                    IconButton(
                                                        onClick = { viewModel.removeTrack(track) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Smazat stopu",
                                                            tint = Color.Gray,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Canvas-based audio waveform renderer
                                        WaveformRenderer(
                                            filePath = track.filePath,
                                            isMuted = track.isMuted,
                                            playProgress = globalPlayProgress,
                                            activeColor = AccentNeonCyan,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(38.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Slider & Mute control
                                        Row(
                                             verticalAlignment = Alignment.CenterVertically,
                                             modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "MIX: ${(track.volume * 100).toInt()}%",
                                                color = Color(0xFF8E8CA4),
                                                fontSize = 11.sp,
                                                modifier = Modifier.width(72.dp)
                                            )
                                            Slider(
                                                value = track.volume,
                                                onValueChange = { viewModel.updateTrackVolume(track, it) },
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            // Mute icon toggle button
                                            IconButton(
                                                onClick = { viewModel.toggleTrackMute(track) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text(
                                                    text = if (track.isMuted) "🔇" else "🔊",
                                                    fontSize = 14.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // Solo tag toggle button
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (track.isSolo) Color(0xFFFFD700) else Color(0xFF1E1339))
                                                    .clickable { viewModel.toggleTrackSolo(track) }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "SOLO",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (track.isSolo) Color.Black else Color.LightGray
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Effects rack toggle button
                                            val hasEq = track.eqLow != 0.0f || track.eqMid != 0.0f || track.eqHigh != 0.0f
                                            val hasFx = hasEq || track.compEnabled || track.reverbEnabled
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (hasFx) AccentNeonCyan else Color(0xFF1E1339))
                                                    .clickable { trackForEffectsRack = track }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                                    .testTag("effects_rack_btn_${track.trackId}")
                                            ) {
                                                Text(
                                                    text = "EFFECTS 🎛️",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hasFx) Color.Black else Color.White
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

            // D. Video Generating and Background Exporter triggers
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF261D45), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = TranslationUtility.get("video_status"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isVideoGenerating) {
                            LinearProgressIndicator(
                                progress = videoProgress,
                                color = AccentNeonCyan,
                                trackColor = Color(0xFF1A1339),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Kóduji H.264 video stopy: ${(videoProgress*100).toInt()}%",
                                color = AccentNeonCyan,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(TranslationUtility.get("video_idle"), color = Color(0xFF908DA1), fontSize = 12.sp)
                        }

                        if (videoGenerationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF331118)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF8A1E31), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "Upozornění: Bezpečnostní filtr zablokoval standardní generátor. Vyzkoušejte tvorbu s alternativním uměleckým tónem.",
                                        color = Color(0xFFFFCDD2),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.generateAlternativeMP4LyricVideo(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("VYGENEROVAT ALTERNATIVNÍ VIZUÁL ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.generateMP4LyricVideo(context) },
                            enabled = !isVideoGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            modifier = Modifier.fillMaxWidth().testTag("generate_video_btn")
                        ) {
                            Text(TranslationUtility.get("export_video"), fontWeight = FontWeight.Bold)
                        }

                        if (isExportingProject) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Míchám a exportuji projekt... ${(exportProjectProgress * 100).toInt()}% 🎛️",
                                    color = AccentNeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = exportProjectProgress,
                                    color = AccentNeonCyan,
                                    trackColor = Color(0xFF1E1339),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        var showPublishDialog by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportProjectAudio(context, activeTracks) },
                                enabled = !isExportingProject,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1C5E)),
                                modifier = Modifier.weight(1f).testTag("mix_project_export_wav_btn")
                            ) {
                                Text(
                                    text = if (isExportingProject) "MÍCHÁNÍ..." else TranslationUtility.get("export_audio"),
                                    fontSize = 11.sp,
                                    color = AccentNeonCyan
                                )
                            }
                            Button(
                                onClick = { viewModel.triggerBackgroundServiceExport(context, "MP4") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1C5E)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("EXPORT NA POZADÍ 🎬", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Publish to Community Button
                        Button(
                            onClick = { showPublishDialog = true },
                            enabled = !isExportingProject && activeTracks.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5E4D)),
                            modifier = Modifier.fillMaxWidth().testTag("publish_to_community_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Zveřejnit",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ZVEŘEJNIT DO KOMUNITY 🚀",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        val currentRackTrack = trackForEffectsRack
                        if (currentRackTrack != null) {
                            val freshTrack = activeTracks.firstOrNull { it.trackId == currentRackTrack.trackId } ?: currentRackTrack
                            EffectsRackDialog(
                                track = freshTrack,
                                viewModel = viewModel,
                                onDismiss = { trackForEffectsRack = null }
                            )
                        }

                        if (showPublishDialog) {
                            PublishProjectDialog(
                                viewModel = viewModel,
                                activeTracks = activeTracks,
                                onDismiss = { showPublishDialog = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun LiveFrequencySpectrumWave() {
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val points = 50
        val stepX = width / (points - 1)

        for (i in 0 until points) {
            val progress = i.toFloat() / points
            val x = i * stepX
            val vibration = sin(progress * PI.toFloat() * 4f + waveOffset) * (centerY - 10f) * sin(progress * PI.toFloat())
            
            // Draw dual bouncing glowing audio bands
            drawCircle(
                color = AccentNeonCyan,
                radius = 4f,
                center = androidx.compose.ui.geometry.Offset(x, centerY + vibration)
            )
            drawCircle(
                color = Color(0xFF9D4EDD),
                radius = 3f,
                center = androidx.compose.ui.geometry.Offset(x, centerY - vibration * 0.7f)
            )
        }
    }
}

// --- TAB 2: VIDEO REELS / PLAYER ---
@Composable
fun VideoTab(
    viewModel: StudioViewModel
) {
    val activeProj by viewModel.activeProject.collectAsStateWithLifecycle()
    val timelineClips by viewModel.timelineClips.collectAsStateWithLifecycle()
    val timelineTransitions by viewModel.timelineTransitions.collectAsStateWithLifecycle()
    val isVideoGenerating by viewModel.isVideoGenerating.collectAsStateWithLifecycle()
    val videoProgress by viewModel.videoProgress.collectAsStateWithLifecycle()
    val videoGenerationError by viewModel.videoGenerationError.collectAsStateWithLifecycle()
    val isGeneratingAIVideoTimeline by viewModel.isGeneratingAIVideoTimeline.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Trigger loading timeline for active project
    LaunchedEffect(activeProj?.id) {
        activeProj?.id?.let { pid ->
            viewModel.loadTimeline(context, pid)
        }
    }

    var activeTabMode by remember { mutableStateOf(1) } // 0 = Player, 1 = Timeline Editor
    var selectedClipId by remember { mutableStateOf<String?>(null) }
    
    // Automatically select first clip if none selected
    LaunchedEffect(timelineClips) {
        if (selectedClipId == null && timelineClips.isNotEmpty()) {
            selectedClipId = timelineClips.first().id
        }
    }

    // Interactive Preview Playing parameters
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var previewFrame by remember { mutableStateOf(0f) }

    // Coroutine running live frame loops
    LaunchedEffect(isPreviewPlaying, timelineClips) {
        if (isPreviewPlaying && timelineClips.isNotEmpty()) {
            val frameRate = 15
            val maxFrames = (timelineClips.sumOf { it.durationSec } * frameRate).coerceAtLeast(1)
            while (true) {
                kotlinx.coroutines.delay(66) // ~15 FPS
                previewFrame = (previewFrame + 1) % maxFrames
            }
        }
    }

    // Active configuration dialogues
    var activeEditingTransition by remember { mutableStateOf<com.example.util.TransitionData?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    var aiVideoEngine by remember { mutableStateOf("Spark AI Cinematics") }
    val videoEngines = listOf("Spark AI Cinematics", "Runway Gen-3 Alpha", "Luma Dream Machine", "Sora Cinema Ultra", "Kling AI", "Stable Video")
    
    var aiVideoAspect by remember { mutableStateOf("9:16") }
    val videoAspects = listOf("9:16", "16:9", "1:1")
    
    var aiMotionIntensity by remember { mutableStateOf(50f) }
    
    var aiVisualStyle by remember { mutableStateOf("Hyper-Realistic CGI") }
    val videoStyles = listOf("Hyper-Realistic CGI", "Retro Japan Anime", "Vaporwave Neon 3D", "Oil Painting Art", "Cyberspace Matrix")
    
    var aiVideoPrompt by remember { mutableStateOf("") }
    var isSuggestingVideoPrompt by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // --- Tab Selection Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF120824))
                .border(1.dp, Color(0xFF2E174F), RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (activeTabMode == 1) Color(0xFF8644FF) else Color.Transparent)
                    .clickable { 
                        activeTabMode = 1
                        isPreviewPlaying = true 
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✂️ HLAVNÍ ČASOVÁ OSA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTabMode == 1) Color.White else Color(0xFF8F88A2)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (activeTabMode == 0) Color(0xFF8644FF) else Color.Transparent)
                    .clickable { 
                        activeTabMode = 0
                        isPreviewPlaying = false
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎬 VIDEOPŘEHRÁVAČ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTabMode == 0) Color.White else Color(0xFF8F88A2)
                )
            }
        }

        if (activeTabMode == 1) {
            // --- WORKSTATION: TIMELINE EDITOR ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Item 1: Live Interactive Monitor Canvas
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF090412))
                            .border(1.dp, Color(0xFF2E174F), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🖥️ INTERAKTIVNÍ PREVIEW",
                                color = AccentNeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isPreviewPlaying) Color.Green else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPreviewPlaying) "LIVE SPUŠTĚNO" else "PREVIEW STOPNUTO",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val frameRate = 15
                        val clipStartFrames = remember(timelineClips) { IntArray(timelineClips.size) }
                        val clipEndFrames = remember(timelineClips) { IntArray(timelineClips.size) }
                        val totalFrames = remember(timelineClips) {
                            var total = 0
                            for (idx in timelineClips.indices) {
                                clipStartFrames[idx] = total
                                total += timelineClips[idx].durationSec * frameRate
                                clipEndFrames[idx] = total
                            }
                            total
                        }

                        // Format Timecodes
                        val currentFrameInt = if (totalFrames > 0) (previewFrame % totalFrames).toInt() else 0
                        val currentSecs = currentFrameInt / frameRate
                        val displayMin = currentSecs / 60
                        val displaySec = currentSecs % 60
                        val displayFrame = currentFrameInt % frameRate
                        val timeCodeString = String.format("%02d:%02d:%02d", displayMin, displaySec, displayFrame)

                        val totalSecs = totalFrames / frameRate
                        val totalMin = totalSecs / 60
                        val totalSec = totalSecs % 60
                        val totalFrame = totalFrames % frameRate
                        val totalTimeCodeString = String.format("%02d:%02d:%02d", totalMin, totalSec, totalFrame)

                        // Compute active clip details & transition
                        var activeClipName = "Žádný záběr"
                        var transitionStatusText = ""
                        var activeClipIdx = -1
                        if (totalFrames > 0) {
                            for (i in timelineClips.indices) {
                                if (currentFrameInt >= clipStartFrames[i] && currentFrameInt < clipEndFrames[i]) {
                                    activeClipIdx = i
                                    break
                                }
                            }
                            if (activeClipIdx in timelineClips.indices) {
                                val currentClip = timelineClips[activeClipIdx]
                                activeClipName = "${activeClipIdx + 1}. ${currentClip.title} (${currentClip.mood})"
                                
                                val currentEnd = clipEndFrames[activeClipIdx]
                                val nextIdx = activeClipIdx + 1
                                if (nextIdx < timelineClips.size) {
                                    val trans = timelineTransitions.find { it.fromClipId == currentClip.id && it.toClipId == timelineClips[nextIdx].id }
                                    if (trans != null && trans.transitionType != "None") {
                                        val transFrames = (trans.durationSec * frameRate).toInt().coerceIn(1, 150)
                                        val transitionStartFrame = currentEnd - transFrames
                                        if (currentFrameInt >= transitionStartFrame) {
                                            val progress = (currentFrameInt - transitionStartFrame).toFloat() / transFrames
                                            transitionStatusText = "PŘECHOD: ${trans.transitionType.uppercase()} (${(progress * 100).toInt()}%)"
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render Canvas Aspect Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(1.dp, Color(0xFF21153D), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (timelineClips.isEmpty()) {
                                Text("Časová osa neobsahuje žádné záběry.", color = Color.Gray)
                            } else {
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val canvasWidth = size.width.toInt()
                                    val canvasHeight = size.height.toInt()
                                    val nativeCanvas = drawContext.canvas.nativeCanvas

                                    if (totalFrames > 0) {
                                        val activeFrame = (previewFrame % totalFrames).toInt()
                                        var clipIdx = 0
                                        for (i in timelineClips.indices) {
                                            if (activeFrame >= clipStartFrames[i] && activeFrame < clipEndFrames[i]) {
                                                clipIdx = i
                                                break
                                            }
                                        }

                                        val currentClip = timelineClips[clipIdx]
                                        val currentEnd = clipEndFrames[clipIdx]
                                        val nextIdx = clipIdx + 1

                                        var isTransitionActive = false
                                        var transType = "None"
                                        var transProgress = 0.0f

                                        if (nextIdx < timelineClips.size) {
                                            val trans = timelineTransitions.find { it.fromClipId == currentClip.id && it.toClipId == timelineClips[nextIdx].id }
                                            if (trans != null && trans.transitionType != "None") {
                                                val transFrames = (trans.durationSec * frameRate).toInt().coerceIn(1, 150)
                                                val transitionStartFrame = currentEnd - transFrames
                                                if (activeFrame >= transitionStartFrame) {
                                                    isTransitionActive = true
                                                    transType = trans.transitionType
                                                    transProgress = (activeFrame - transitionStartFrame).toFloat() / transFrames
                                                }
                                            }
                                        }

                                        if (!isTransitionActive) {
                                            com.example.util.ExportFileHelper.drawTimelineScene(
                                                canvas = nativeCanvas,
                                                mood = currentClip.mood,
                                                frame = activeFrame.toFloat(),
                                                width = canvasWidth,
                                                height = canvasHeight,
                                                text = currentClip.text,
                                                speed = 1.0f
                                            )
                                        } else {
                                            val nextClip = timelineClips[nextIdx]
                                            
                                            when (transType) {
                                                "Fade" -> {
                                                    val alphaA = 1.0f - transProgress
                                                    val alphaB = transProgress

                                                    val layerPaintA = android.graphics.Paint().apply { alpha = (alphaA * 255).toInt() }
                                                    val tokenA = nativeCanvas.saveLayer(null, layerPaintA)
                                                    com.example.util.ExportFileHelper.drawTimelineScene(
                                                        canvas = nativeCanvas,
                                                        mood = currentClip.mood,
                                                        frame = activeFrame.toFloat(),
                                                        width = canvasWidth,
                                                        height = canvasHeight,
                                                        text = currentClip.text,
                                                        speed = 1.0f
                                                    )
                                                    nativeCanvas.restoreToCount(tokenA)

                                                    val layerPaintB = android.graphics.Paint().apply { alpha = (alphaB * 255).toInt() }
                                                    val tokenB = nativeCanvas.saveLayer(null, layerPaintB)
                                                    com.example.util.ExportFileHelper.drawTimelineScene(
                                                        canvas = nativeCanvas,
                                                        mood = nextClip.mood,
                                                        frame = activeFrame.toFloat(),
                                                        width = canvasWidth,
                                                        height = canvasHeight,
                                                        text = nextClip.text,
                                                        speed = 1.0f
                                                    )
                                                    nativeCanvas.restoreToCount(tokenB)
                                                }
                                                "Wipe" -> {
                                                    com.example.util.ExportFileHelper.drawTimelineScene(
                                                        canvas = nativeCanvas,
                                                        mood = currentClip.mood,
                                                        frame = activeFrame.toFloat(),
                                                        width = canvasWidth,
                                                        height = canvasHeight,
                                                        text = currentClip.text,
                                                        speed = 1.0f
                                                    )

                                                    nativeCanvas.save()
                                                    nativeCanvas.clipRect(0f, 0f, transProgress * canvasWidth, canvasHeight.toFloat())
                                                    com.example.util.ExportFileHelper.drawTimelineScene(
                                                        canvas = nativeCanvas,
                                                        mood = nextClip.mood,
                                                        frame = activeFrame.toFloat(),
                                                        width = canvasWidth,
                                                        height = canvasHeight,
                                                        text = nextClip.text,
                                                        speed = 1.0f
                                                    )
                                                    nativeCanvas.restore()
                                                }
                                                "Flash" -> {
                                                    val flashAlpha = 1.0f - kotlin.math.abs(transProgress - 0.5f) * 2f
                                                    if (transProgress < 0.5f) {
                                                        com.example.util.ExportFileHelper.drawTimelineScene(
                                                            canvas = nativeCanvas,
                                                            mood = currentClip.mood,
                                                            frame = activeFrame.toFloat(),
                                                            width = canvasWidth,
                                                            height = canvasHeight,
                                                            text = currentClip.text,
                                                            speed = 1.0f
                                                        )
                                                    } else {
                                                        com.example.util.ExportFileHelper.drawTimelineScene(
                                                            canvas = nativeCanvas,
                                                            mood = nextClip.mood,
                                                            frame = activeFrame.toFloat(),
                                                            width = canvasWidth,
                                                            height = canvasHeight,
                                                            text = nextClip.text,
                                                            speed = 1.0f
                                                        )
                                                    }

                                                    val flashPaint = android.graphics.Paint().apply {
                                                        color = android.graphics.Color.WHITE
                                                        style = android.graphics.Paint.Style.FILL
                                                        alpha = (flashAlpha.coerceIn(0f, 1f) * 255).toInt()
                                                    }
                                                    nativeCanvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), flashPaint)
                                                }
                                                "Zoom" -> {
                                                    val scaleA = 1.0f + 0.5f * transProgress
                                                    val alphaA = 1.0f - transProgress

                                                    val scaleB = 0.5f + 0.5f * transProgress
                                                    val alphaB = transProgress

                                                    val layerPaintA = android.graphics.Paint().apply { alpha = (alphaA * 255).toInt() }
                                                    val tokenA = nativeCanvas.saveLayer(null, layerPaintA)
                                                    nativeCanvas.save()
                                                    nativeCanvas.translate(canvasWidth / 2f, canvasHeight / 2f)
                                                    nativeCanvas.scale(scaleA, scaleA)
                                                    nativeCanvas.translate(-canvasWidth / 2f, -canvasHeight / 2f)
                                                    com.example.util.ExportFileHelper.drawTimelineScene(
                                                        canvas = nativeCanvas,
                                                        mood = currentClip.mood,
                                                        frame = activeFrame.toFloat(),
                                                        width = canvasWidth,
                                                        height = canvasHeight,
                                                        text = currentClip.text,
                                                        speed = 1.0f
                                                    )
                                                    nativeCanvas.restore()
                                                    nativeCanvas.restoreToCount(tokenA)

                                                    val layerPaintB = android.graphics.Paint().apply { alpha = (alphaB * 255).toInt() }
                                                    val tokenB = nativeCanvas.saveLayer(null, layerPaintB)
                                                    nativeCanvas.save()
                                                    nativeCanvas.translate(canvasWidth / 2f, canvasHeight / 2f)
                                                    nativeCanvas.scale(scaleB, scaleB)
                                                    nativeCanvas.translate(-canvasWidth / 2f, -canvasHeight / 2f)
                                                    com.example.util.ExportFileHelper.drawTimelineScene(
                                                        canvas = nativeCanvas,
                                                        mood = nextClip.mood,
                                                        frame = activeFrame.toFloat(),
                                                        width = canvasWidth,
                                                        height = canvasHeight,
                                                        text = nextClip.text,
                                                        speed = 1.0f
                                                    )
                                                    nativeCanvas.restore()
                                                    nativeCanvas.restoreToCount(tokenB)
                                                }
                                                else -> {
                                                    if (transProgress < 0.5f) {
                                                        com.example.util.ExportFileHelper.drawTimelineScene(
                                                            canvas = nativeCanvas,
                                                            mood = currentClip.mood,
                                                            frame = activeFrame.toFloat(),
                                                            width = canvasWidth,
                                                            height = canvasHeight,
                                                            text = currentClip.text,
                                                            speed = 1.0f
                                                        )
                                                    } else {
                                                        com.example.util.ExportFileHelper.drawTimelineScene(
                                                            canvas = nativeCanvas,
                                                            mood = nextClip.mood,
                                                            frame = activeFrame.toFloat(),
                                                            width = canvasWidth,
                                                            height = canvasHeight,
                                                            text = nextClip.text,
                                                            speed = 1.0f
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // --- Real-Time Synchronized Info Row & Playhead Slider ---
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeClipName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                if (transitionStatusText.isNotEmpty()) {
                                    Text(
                                        text = transitionStatusText,
                                        color = AccentNeonCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$timeCodeString / $totalTimeCodeString",
                                color = Color.Gray,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Visual Aligned Track Map
                        if (totalFrames > 0 && timelineClips.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF1E1335))
                                    .border(1.dp, Color(0xFF2E174F), RoundedCornerShape(3.dp))
                            ) {
                                timelineClips.forEachIndexed { idx, clip ->
                                    val isCurrent = idx == activeClipIdx
                                    val blockColor = when (clip.mood) {
                                        "Neon Cyberpunk" -> Color(0xFF8644FF)
                                        "Cosmic Space" -> Color(0xFF0D47A1)
                                        "Warm Retro" -> Color(0xFFE65100)
                                        "Hot Lava" -> Color(0xFFB71C1C)
                                        "Emerald Forest" -> Color(0xFF1B5E20)
                                        "Golden Sunset" -> Color(0xFFFFB300)
                                        else -> Color(0xFF6A1B9A)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(clip.durationSec.toFloat().coerceAtLeast(1f))
                                            .fillMaxHeight()
                                            .background(if (isCurrent) blockColor else blockColor.copy(alpha = 0.45f))
                                            .border(
                                                width = if (isCurrent) 1.dp else 0.dp,
                                                color = if (isCurrent) Color(0xFF00FFC2) else Color.Transparent
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Playhead Slider (Continuous realtime scrub)
                        if (totalFrames > 0) {
                            Slider(
                                value = (previewFrame % totalFrames).coerceIn(0f, (totalFrames - 1).toFloat()),
                                onValueChange = {
                                    previewFrame = it
                                },
                                valueRange = 0f..(totalFrames - 1).toFloat(),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFF00FFC2),
                                    inactiveTrackColor = Color(0xFF1B112D),
                                    thumbColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .testTag("timeline_playhead_slider")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // --- NOVOST V30: AI VIDEO GENERATOR TO SONG ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF130A2B)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF2B1C4C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎬 REŽISÉRSKÉ STUDIO AI VIDEA",
                                    color = AccentNeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Generování filmového videoklipu synchronizovaného s tempem vaší nahrávky.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Select Video Engine
                                Text("1. TECHNICKÝ VIDEO ENGINE: ⚙️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    videoEngines.forEach { eng ->
                                        val isSel = aiVideoEngine == eng
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) AccentNeonCyan else Color(0xFF1E133F))
                                                .clickable { aiVideoEngine = eng }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(eng, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else Color.White)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Select Aspect Ratio and Visual Style side-by-side
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("POMĚR STRAN:", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            videoAspects.forEach { asp ->
                                                val isSel = aiVideoAspect == asp
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSel) AccentNeonCyan else Color(0xFF1E133F))
                                                        .clickable { aiVideoAspect = asp }
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(asp, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else Color.White)
                                                }
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("UMĚLECKÝ STYL:", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF1E133F))
                                                .clickable {
                                                    // Cycle style
                                                    val nextIdx = (videoStyles.indexOf(aiVisualStyle) + 1) % videoStyles.size
                                                    aiVisualStyle = videoStyles[nextIdx]
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎨 $aiVisualStyle 🔄", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Motion intensity slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("DYNAMIKA INTENZITY POHYBU (STŘIHU):", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("${aiMotionIntensity.toInt()}%", color = AccentNeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = aiMotionIntensity,
                                    onValueChange = { aiMotionIntensity = it },
                                    valueRange = 10f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentNeonCyan,
                                        activeTrackColor = AccentNeonCyan,
                                        inactiveTrackColor = Color(0xFF25154A)
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // AI Prompt for the Video clip Scene
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("VIZUÁLNÍ POPIS SCÉNY (VIDEO PROMPT):", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    if (isSuggestingVideoPrompt) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AccentNeonCyan, strokeWidth = 1.2.dp)
                                    } else {
                                        Text(
                                            text = "Navrhnout z lyrics 🪄",
                                            color = AccentNeonCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                coroutineScope.launch {
                                                    isSuggestingVideoPrompt = true
                                                    try {
                                                        val lyricsText = activeProj?.lyrics ?: ""
                                                        val genreText = activeProj?.genre ?: ""
                                                        val prompt = "Navrhni jeden stručný, vysoce vizuální anglický popis scény (video prompt) pro filmový model o délce do 15 slov, který se hodí k písní v žánru $genreText s tématem: '$lyricsText'. Napiš čistě jen ten prompt v angličtině bez uvozovek a úvodů."
                                                        val response = com.example.data.network.GeminiClient.generateText(prompt, "Jsi filmový prompt inženýr.")
                                                        aiVideoPrompt = response.trim().replace("\"", "")
                                                    } catch (e: Exception) {
                                                        aiVideoPrompt = "surreal cinematic cosmic energy pulse, glowing visualizer"
                                                    } finally {
                                                        isSuggestingVideoPrompt = false
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = aiVideoPrompt,
                                    onValueChange = { aiVideoPrompt = it },
                                    placeholder = { Text("nepř. neon glowing synth waves moving over futuristic highway, cyberpunk cgi, ultra sharp...", fontSize = 10.sp, color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentNeonCyan,
                                        unfocusedBorderColor = Color(0xFF261D45),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Generate Button
                                if (isGeneratingAIVideoTimeline) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(color = AccentNeonCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Generuji a renderuji video přes $aiVideoEngine...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            isPreviewPlaying = false
                                            viewModel.generateAIVideoClipFromSong(
                                                context = context,
                                                videoEngine = aiVideoEngine,
                                                aspectRatio = aiVideoAspect,
                                                motionIntensity = aiMotionIntensity,
                                                visualStyle = aiVisualStyle,
                                                customVideoPrompt = aiVideoPrompt
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("submit_ai_video_gen")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                                                contentDescription = "AI Video Gen",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SPUSTIT AI GENERÁTOR VIDEOKLIPU 🎬",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                Button(
                                    onClick = { isPreviewPlaying = !isPreviewPlaying },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isPreviewPlaying) Color(0xFFCE0037) else Color(0xFF14751E)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isPreviewPlaying) "⏸️ PAUZA" else "▶️ PREHRÁT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { previewFrame = 0f },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D4C)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("↩️ STARTOVAT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Button(
                                onClick = { 
                                    isPreviewPlaying = false
                                    viewModel.generateTimelineMP4Video(context) 
                                },
                                enabled = !isVideoGenerating && timelineClips.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC2)),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("📼 EXPORT MP4", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    }
                }

                // Item 2: Progressive Compilation Bar
                if (isVideoGenerating) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D161F)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCE0037)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = Color(0xFFCE0037), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Kompletní programový export MP4 videa...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Generuji snímky scény, synchronizuji a překódovávám audio: ${(videoProgress*100).toInt()}%", color = Color.LightGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = videoProgress,
                                    color = Color(0xFFCE0037),
                                    trackColor = Color(0xFF2C1625),
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }

                if (videoGenerationError != null) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF330B0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ CHYBA: $videoGenerationError",
                                modifier = Modifier.padding(12.dp),
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Item 3: Header Section Timeline list
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "🎞️ SEKVENCE KLIPŮ A PŘECHODŮ (Kliknutím vyberte pro editaci):",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Item 4: Horizontally Scrollable Timeline Line
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        timelineClips.forEachIndexed { i, clip ->
                            val isSelected = clip.id == selectedClipId
                            
                            // Visual Clip Card representation
                            Card(
                                modifier = Modifier
                                    .width(135.dp)
                                    .height(115.dp)
                                    .clickable { selectedClipId = clip.id }
                                    .testTag("timeline_clip_${clip.id}"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF00FFC2) else Color(0xFF2E174F)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (clip.mood) {
                                        "Neon Cyberpunk" -> Color(0xFF0F041F)
                                        "Cosmic Space" -> Color(0xFF03102B)
                                        "Warm Retro" -> Color(0xFF2A1C16)
                                        "Hot Lava" -> Color(0xFF380707)
                                        "Emerald Forest" -> Color(0xFF042211)
                                        "Golden Sunset" -> Color(0xFF2E2203)
                                        else -> Color(0xFF13092A)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${i + 1}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AccentNeonCyan else Color.Gray
                                        )
                                        Text(
                                            text = "${clip.durationSec}s",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = clip.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Téma: ${clip.mood}",
                                            fontSize = 9.sp,
                                            color = Color.LightGray,
                                            maxLines = 1
                                        )
                                    }

                                    Text(
                                        text = if (clip.text.isNotBlank()) "📝 \"${clip.text}\"" else "📭 Bez textu",
                                        fontSize = 8.sp,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }

                            // If there is an adjacent clip, render the Transition Effector pill
                            if (i < timelineClips.size - 1) {
                                val nextClip = timelineClips[i + 1]
                                val transition = timelineTransitions.find { it.fromClipId == clip.id && it.toClipId == nextClip.id }
                                val transitionLabel = transition?.transitionType ?: "None"
                                val transitionDuration = transition?.durationSec ?: 1.0f

                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF2E174F))
                                            .border(1.dp, Color(0xFF00FFC2), RoundedCornerShape(20.dp))
                                            .clickable { 
                                                activeEditingTransition = transition ?: com.example.util.TransitionData(clip.id, nextClip.id, "None", 1.0f)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = when (transitionLabel) {
                                                    "Fade" -> "⇆ Fade"
                                                    "Wipe" -> "⚡ Wipe"
                                                    "Flash" -> "💥 Flash"
                                                    "Zoom" -> "🔍 Zoom"
                                                    else -> "❌ Řez"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF00FFC2)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "(${transitionDuration}s)",
                                                fontSize = 8.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add action button card
                        Spacer(modifier = Modifier.width(10.dp))
                        Card(
                            modifier = Modifier
                                .width(64.dp)
                                .height(115.dp)
                                .clickable {
                                    activeProj?.id?.let { pid ->
                                        val newId = System.currentTimeMillis().toString()
                                        val templateClips = listOf(
                                            com.example.util.TimelineClipData(newId, "Píseň", "Neon Cyberpunk", 4, "DALŠÍ SCÉNA")
                                        )
                                        viewModel.addClipToTimeline(context, pid, templateClips.first())
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF2E174F)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF100721))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Přidat",
                                    tint = AccentNeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Item 5: Selected Clip Editing Panel
                item {
                    val activeClip = timelineClips.find { it.id == selectedClipId }
                    if (activeClip != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF13092A))
                                .border(1.dp, Color(0xFF2E174F), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "🎨 EDITACE ZÁBĚRU: ${activeClip.title.uppercase()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Subtitle text entry
                            Text("Titulek / Text scény:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = activeClip.text,
                                onValueChange = { valText ->
                                    activeProj?.id?.let { pid ->
                                        viewModel.updateClipText(context, pid, activeClip.id, valText)
                                    }
                                },
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF090412),
                                    unfocusedContainerColor = Color(0xFF090412),
                                    focusedIndicatorColor = Color(0xFF8644FF),
                                    unfocusedIndicatorColor = Color(0xFF23143F)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("clip_text_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mood theme picker
                            Text("Vizuální Vibe / Téma scény:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val vibes = listOf("Neon Cyberpunk", "Cosmic Space", "Warm Retro", "Hot Lava", "Emerald Forest", "Golden Sunset")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                vibes.forEach { vb ->
                                    val isVbSelected = activeClip.mood == vb
                                    val flagIcon = when (vb) {
                                        "Neon Cyberpunk" -> "⚡"
                                        "Cosmic Space" -> "🪐"
                                        "Warm Retro" -> "📺"
                                        "Hot Lava" -> "🌋"
                                        "Emerald Forest" -> "🌲"
                                        "Golden Sunset" -> "🌅"
                                        else -> "🎨"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isVbSelected) Color(0xFF8644FF) else Color(0xFF090412))
                                            .border(1.dp, if (isVbSelected) AccentNeonCyan else Color(0xFF23143F), RoundedCornerShape(8.dp))
                                            .clickable {
                                                activeProj?.id?.let { pid ->
                                                    viewModel.updateClipMood(context, pid, activeClip.id, vb)
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$flagIcon $vb", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Duration multiplier picker
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Trvání záběru:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = {
                                                activeProj?.id?.let { pid ->
                                                    if (activeClip.durationSec > 1) {
                                                        viewModel.updateClipDuration(context, pid, activeClip.id, activeClip.durationSec - 1)
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2259)),
                                            contentPadding = PaddingValues(horizontal = 10.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("-", color = Color.White, fontSize = 14.sp)
                                        }
                                        Text(
                                            text = "${activeClip.durationSec}s",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        Button(
                                            onClick = {
                                                activeProj?.id?.let { pid ->
                                                    if (activeClip.durationSec < 15) {
                                                        viewModel.updateClipDuration(context, pid, activeClip.id, activeClip.durationSec + 1)
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2259)),
                                            contentPadding = PaddingValues(horizontal = 10.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("+", color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                }

                                // Clip Title rename field inline
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Reorganizace:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        val curIdx = timelineClips.indexOfFirst { it.id == activeClip.id }
                                        Button(
                                            onClick = {
                                                activeProj?.id?.let { pid ->
                                                    viewModel.moveTimelineClip(context, pid, curIdx, curIdx - 1)
                                                }
                                            },
                                            enabled = curIdx > 0,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D4C)),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("◀", color = Color.White, fontSize = 10.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Button(
                                            onClick = {
                                                activeProj?.id?.let { pid ->
                                                    viewModel.moveTimelineClip(context, pid, curIdx, curIdx + 1)
                                                }
                                            },
                                            enabled = curIdx < timelineClips.size - 1,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D4C)),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("▶", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Delete current slice action
                            Button(
                                onClick = {
                                    activeProj?.id?.let { pid ->
                                        selectedClipId = null
                                        viewModel.removeClipFromTimeline(context, pid, activeClip.id)
                                    }
                                },
                                enabled = timelineClips.size > 1,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B0B0B)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Smazat záběr",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SMAZAT TENTO ZÁBĚR Z TIMELINU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(60.dp)) // Avoid final drawer overlaps
                }
            }
        } else {
            // --- WORKSTATION: CLASSIC PLAYER VIEW ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activeProj?.videoPath.isNullOrEmpty() || !File(activeProj?.videoPath!!).exists()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Chybí klip",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Projekt nemá žádný exportovaný videoklip",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Přejděte do záložky časové osy, sestavte své scény a klepněte na tlačítko exporování!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { activeTabMode = 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8644FF))
                        ) {
                            Text("OTEVŘÍT EDITOR OSY", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val videoFile = File(activeProj?.videoPath!!)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "▶ ZKOUPILOVANÉ VIDEO: ${activeProj?.title}",
                            fontWeight = FontWeight.Bold,
                            color = AccentNeonCyan,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(1.dp, Color(0xFF231846), RoundedCornerShape(16.dp))
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(Uri.fromFile(videoFile))
                                        val controller = MediaController(ctx)
                                        controller.setAnchorView(this)
                                        setMediaController(controller)
                                        start()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(videoFile))
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Sdílet videoklip"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Nelze sdílet soubor: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SDÍLET VIDEO S PŘÁTELI ✉️", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Interactive Custom Transition Selection Dialog
    activeEditingTransition?.let { editingTrans ->
        AlertDialog(
            onDismissRequest = { activeEditingTransition = null },
            title = {
                Text(
                    text = "⚙️ NASTAVENÍ PŘECHODU",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Vyberte typ přechodu mezi klipy a jeho celkovou délku trvání:",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Efekt přechodu:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val effectsList = listOf("None", "Fade", "Wipe", "Flash", "Zoom")
                    var selectedTypeLocal by remember { mutableStateOf(editingTrans.transitionType) }
                    var selectedDurLocal by remember { mutableStateOf(editingTrans.durationSec) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        effectsList.forEach { eff ->
                            val isEffSelected = selectedTypeLocal == eff
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isEffSelected) Color(0xFF8644FF) else Color(0xFF0F041F))
                                    .border(1.dp, if (isEffSelected) AccentNeonCyan else Color(0xFF2E174F), RoundedCornerShape(8.dp))
                                    .clickable { selectedTypeLocal = eff }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (eff) {
                                        "Fade" -> "⇆ Fade"
                                        "Wipe" -> "⚡ Wipe"
                                        "Flash" -> "💥 Flash"
                                        "Zoom" -> "🔍 Zoom"
                                        else -> "❌ Řez"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Délka trvání přechodu (sekundy):", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val durations = listOf(0.5f, 1.0f, 1.5f, 2.0f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durations.forEach { dur ->
                            val isDurSelected = kotlin.math.abs(selectedDurLocal - dur) < 0.1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDurSelected) Color(0xFF8644FF) else Color(0xFF0F041F))
                                    .border(1.dp, if (isDurSelected) AccentNeonCyan else Color(0xFF2E174F), RoundedCornerShape(8.dp))
                                    .clickable { selectedDurLocal = dur }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${dur}s", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { activeEditingTransition = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("STORNO", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                activeProj?.id?.let { pid ->
                                    viewModel.setTransitionEffect(
                                        context = context,
                                        projectId = pid,
                                        fromClipId = editingTrans.fromClipId,
                                        toClipId = editingTrans.toClipId,
                                        type = selectedTypeLocal,
                                        durationSec = selectedDurLocal
                                    )
                                }
                                activeEditingTransition = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC2))
                        ) {
                            Text("POUŽÍT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF13092A)
        )
    }
}

// --- TAB 3: CHAT ASSISTANT ---
@Composable
fun ChatTab(
    viewModel: StudioViewModel
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var promptInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = TranslationUtility.get("chat_assistant"),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.second
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                )
                            )
                            .background(if (isUser) AccentPurple else Color(0xFF1B1233))
                            .padding(12.dp)
                    ) {
                        Text(text = msg.first, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1B1233), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        CircularProgressIndicator(color = AccentNeonCyan, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_prompt_input"),
                placeholder = { Text(TranslationUtility.get("chat_hint"), color = Color.Gray, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentNeonCyan),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.sendChatMessage(promptInput)
                    promptInput = ""
                },
                enabled = promptInput.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black),
                modifier = Modifier.height(52.dp)
            ) {
                Text(TranslationUtility.get("send"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TAB 4: MARKETPLACE & COMMUNITY FEED ---
@Composable
fun MarketplaceTab(
    viewModel: StudioViewModel
) {
    val items by viewModel.allMarketplaceItems.collectAsStateWithLifecycle()
    val playingCommunityItemId by viewModel.playingCommunityItemId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(0) } // 0: Vše, 1: Nástroje (Premium), 2: Komunita (Skladby)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "KOMUNITNÍ CENTRUM & TRŽIŠTĚ 🎧",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 18.sp
        )
        Text(
            text = "Sdílejte své skladby, procházejte výtvory ostatních a odemykejte prémiové efekty.",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Vyhledat skladbu, žánr nebo tag...", color = Color.Gray, fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentNeonCyan,
                unfocusedBorderColor = Color(0xFF261D45),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("community_search_input"),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Vyhledat", tint = Color.Gray)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Smazat", tint = Color.Gray)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("Všechno 🔍", "Prémiové nástroje 💎", "Komunitní skladby 🌍", "Spoluautoři 👥", "Distribuce & Výdělky 📈")
            categories.forEachIndexed { index, name ->
                val isSelected = selectedCategory == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AccentNeonCyan else Color(0xFF130D2E))
                        .border(1.dp, if (isSelected) AccentNeonCyan else Color(0xFF231846), RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedCategory == 3) {
            // Render Co-authors section
            var coAuthorMessage by remember { mutableStateOf("Ahoj, líbí se mi tvůj styl. Chci do projektu zapojit tvé vokály!") }
            var isProposalSent by remember { mutableStateOf<String?>(null) } // Name of recipient
            var showProposalDialog by remember { mutableStateOf<String?>(null) }

            val coAuthors = listOf(
                Pair("Karolína S. (Zpěvačka / R&B)", "🎤 Krásné, teplé vokály a lyrická hloubka. Hledám melodičtější skladby k nazpívání."),
                Pair("Vojtěch D. (Rock Kytarista)", "⚡ Elektrické kytary, metalová sóla a rify. Mohu dodat energii tvému beatu."),
                Pair("DJ Nova (Vocal Tuning / EDM)", "🎹 Ladění vokálů, vocoder, beatmaking. Pomohu ti vdechnout moderní klubovou duši.")
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "SPOLUPRACOVAT S OSTATNÍMI AUTORY 🤝",
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonCyan,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Nemusíte doručit vše sami! Spojte se s ostatními talentovanými členy komunity a vytvořte hit.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(coAuthors) { auth ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0B24), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF231846), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(auth.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(auth.second, color = Color.LightGray, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { showProposalDialog = auth.first },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Navrhnout", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (showProposalDialog != null) {
                AlertDialog(
                    onDismissRequest = { showProposalDialog = null },
                    containerColor = Color(0xFF130D2E),
                    title = { Text("🤝 SMLOUVA O KO-AUTORSTVÍ", color = AccentNeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Adresát: ${showProposalDialog}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Navrhujete spolupráci na aktivním projektu. Vyberte royalty split:", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            var selectedSplit by remember { mutableStateOf(50) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(50, 60, 70).forEach { split ->
                                    val isSplitSel = selectedSplit == split
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSplitSel) AccentNeonCyan else Color(0xFF1B113A))
                                            .clickable { selectedSplit = split }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("${split}% / ${(100 - split)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSplitSel) Color.Black else Color.White)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = coAuthorMessage,
                                onValueChange = { coAuthorMessage = it },
                                label = { Text("Zpráva pro spoluautora", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentNeonCyan)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isProposalSent = showProposalDialog
                                showProposalDialog = null
                                Toast.makeText(context, "Ko-autorský návrh odeslán!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
                        ) {
                            Text("Odeslat", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showProposalDialog = null }) {
                            Text("Zpět", color = Color.White)
                        }
                    }
                )
            }

            if (isProposalSent != null) {
                AlertDialog(
                    onDismissRequest = { isProposalSent = null },
                    containerColor = Color(0xFF0F0B24),
                    title = { Text("📬 NÁVRH ODESLÁN", color = AccentNeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "Váš autorský návrh s rozdělením autorských práv byl úspěšně zaznamenán a odeslán autorovi ${isProposalSent}.\n\nOzveme se vám ihned, jakmile druhá strana schválí digitální ko-autorskou smlouvu.",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    },
                    confirmButton = {
                        Button(onClick = { isProposalSent = null }, colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan)) {
                            Text("Zavřít", color = Color.Black)
                        }
                    }
                )
            }
        } else if (selectedCategory == 4) {
            // Render Distribution & Earnings
            var isPublishedToSpotify by remember { mutableStateOf(false) }
            var isPublishedToTiktok by remember { mutableStateOf(false) }
            var licenseFee by remember { mutableStateOf(49f) }
            var totalStreamsGoal by remember { mutableStateOf(2500f) }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "MONETIZACE, PRODEJ & DISTRIBUCE 📈",
                        fontWeight = FontWeight.Bold,
                        color = AccentNeonCyan,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Vydělávejte peníze ze své tvořivosti. Nastavte licenční politiku a publikujte song na streamovací služby jedním kliknutím.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13092A)),
                        border = BorderStroke(1.dp, Color(0xFF23144C)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("PUBLIKAČNÍ BRÁNA 🌍", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Odeslat na Spotify & Apple Music", color = Color.LightGray, fontSize = 11.sp)
                                Switch(
                                    checked = isPublishedToSpotify,
                                    onCheckedChange = { isPublishedToSpotify = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = AccentNeonCyan, checkedTrackColor = Color(0xFF2C164F))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Zpřístupnit na TikTok & Instagram Reels", color = Color.LightGray, fontSize = 11.sp)
                                Switch(
                                    checked = isPublishedToTiktok,
                                    onCheckedChange = { isPublishedToTiktok = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = AccentNeonCyan, checkedTrackColor = Color(0xFF2C164F))
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B24)),
                        border = BorderStroke(1.dp, Color(0xFF231846)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("LICENCE PRO REKLAMY (USD):", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("$${licenseFee.toInt()}", color = AccentNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = licenseFee,
                                onValueChange = { licenseFee = it },
                                valueRange = 5f..500f,
                                colors = SliderDefaults.colors(thumbColor = AccentNeonCyan, activeTrackColor = AccentNeonCyan)
                            )
                            Text("*Uživatelé her a tvůrci na YouTube si mohou zakoupit neexkluzivní licenci k vašemu videoklipu pro podklad.", color = Color.Gray, fontSize = 8.sp)
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B113A)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("OČEKÁVANÉ STREAMY (MĚSÍČNĚ):", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${totalStreamsGoal.toInt()} přehrání", color = AccentNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = totalStreamsGoal,
                                onValueChange = { totalStreamsGoal = it },
                                valueRange = 1000f..100000f,
                                colors = SliderDefaults.colors(thumbColor = AccentNeonCyan, activeTrackColor = AccentNeonCyan)
                            )

                            // Royaltie calculation
                            val streamEarnings = (totalStreamsGoal * 0.09) // 0.09 CZK per stream
                            val licenseSales = (licenseFee * 24 * 3) // estimating 3 license sales per month
                            val estimatedMonTotal = streamEarnings + licenseSales

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("📊 ODHADY MĚSÍČNÍCH TRŽEB SMLOUVY:", color = AccentNeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Streaming Royalties:", color = Color.LightGray, fontSize = 10.sp)
                                Text("${streamEarnings.toInt()} Kč", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Prodeje licencí k videoklipu:", color = Color.LightGray, fontSize = 10.sp)
                                Text("${licenseSales.toInt()} Kč", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = Color(0xFF2C1E55), modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Celkový měsíční odhad:", color = AccentNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${estimatedMonTotal.toInt()} Kč", color = AccentNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Služba zahájila digitální mastering a odeslala track na schválení partnerům distributora! 🚀", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ZAHÁJIT DISTRIBUCI A PRODEJ LICENCÍ 🚀", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        } else {
            // Filter the items list based on selected tab and search query
            val filteredItems = items.filter { item ->
                // Category filter
                val matchesCategory = when (selectedCategory) {
                    1 -> !item.isCommunityPublished
                    2 -> item.isCommunityPublished
                    else -> true
                }

                // Search query filter
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    item.name.contains(searchQuery, ignoreCase = true) ||
                            item.description.contains(searchQuery, ignoreCase = true) ||
                            item.type.contains(searchQuery, ignoreCase = true) ||
                            (item.tags ?: "").contains(searchQuery, ignoreCase = true)
                }

                matchesCategory && matchesSearch
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nebyly nalezeny žádné nahrávky splňující filtry.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    if (item.isCommunityPublished) {
                        // Community published composition card style
                        val isPlaying = playingCommunityItemId == item.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F0B24), RoundedCornerShape(14.dp))
                                .border(1.dp, if (isPlaying) AccentNeonCyan else Color(0xFF231846), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🎵 ${item.name}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF231A47))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.type, // e.g., Primary Genre
                                            color = AccentNeonCyan,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${item.durationSec}s",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    color = Color(0xFFC7C5D6),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // Render comma-separated tags
                                if (!item.tags.isNullOrBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        item.tags.split(" ").forEach { tag ->
                                            if (tag.isNotBlank()) {
                                                Text(
                                                    text = tag,
                                                    color = Color(0xFF8E8CA4),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Action: Play/Stop
                            IconButton(
                                onClick = { viewModel.playCommunityItem(context, item) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color.Red else Color(0xFF2D165C))
                                    .testTag("play_community_item_${item.id}")
                            ) {
                                Text(
                                    text = if (isPlaying) "⏹️" else "▶️",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Premium standard tool asset card style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBackground, RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFF231846), RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.name, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF321A4E))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.type,
                                            color = AccentPurple,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(item.description, color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${TranslationUtility.get("price")} $${item.price}",
                                    color = AccentNeonCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { viewModel.buyMarketplaceAsset(context, item) },
                                enabled = !item.isPurchased,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentNeonCyan,
                                    contentColor = Color.Black,
                                    disabledContainerColor = Color(0xFF1A132C),
                                    disabledContentColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("buy_item_${item.id}")
                            ) {
                                Text(
                                    text = if (item.isPurchased) TranslationUtility.get("purchased") else TranslationUtility.get("buy"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
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
fun PublishProjectDialog(
    viewModel: StudioViewModel,
    activeTracks: List<com.example.data.database.AudioTrack>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val projectState by viewModel.activeProject.collectAsStateWithLifecycle()
    val proj = projectState ?: return

    var titleInput by remember { mutableStateOf(proj.title) }
    var descriptionInput by remember { mutableStateOf("") }
    var genreTag by remember { mutableStateOf(proj.genre.ifBlank { "Electronic" }) }
    var tagsInput by remember { mutableStateOf("lyrics, instrumental, vocal, seq") }
    var priceInput by remember { mutableStateOf("50") }

    val genres = listOf("Electronic", "Lo-Fi", "Rock", "Hip Hop", "Ambient", "Pop")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ZVEŘEJNIT DO KOMUNITY 🚀",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        containerColor = Color(0xFF160E2E),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Váše skladba bude zkompilována s veškerým nastavením, syntetizována a následně nahrána do komunitního feedu pro ostatní.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Název skladby") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentNeonCyan,
                        unfocusedBorderColor = Color(0xFF261D45),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = AccentNeonCyan,
                        unfocusedLabelColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("publish_title_input")
                )

                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    label = { Text("Stručný popis") },
                    placeholder = { Text("Přidejte vzkaz o své nahrávce...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentNeonCyan,
                        unfocusedBorderColor = Color(0xFF261D45),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = AccentNeonCyan,
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("publish_desc_input")
                )

                Column {
                    Text("Primární žánr:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.take(3).forEach { genre ->
                            val isSel = genreTag == genre
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AccentNeonCyan else Color(0xFF1F1640))
                                    .clickable { genreTag = genre }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = genre,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.drop(3).forEach { genre ->
                            val isSel = genreTag == genre
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AccentNeonCyan else Color(0xFF1F1640))
                                    .clickable { genreTag = genre }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = genre,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Metadata Tagy (oddělené čárkou)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentNeonCyan,
                        unfocusedBorderColor = Color(0xFF261D45),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = AccentNeonCyan,
                        unfocusedLabelColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("publish_tags_input")
                )

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Prodejní cena v kreditech (M3 mince) 🪙") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentNeonCyan,
                        unfocusedBorderColor = Color(0xFF261D45),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = AccentNeonCyan,
                        unfocusedLabelColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("publish_price_input")
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit", color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.isBlank()) {
                        Toast.makeText(context, "Název skladby nesmí být prázdný!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val targetPrice = priceInput.toIntOrNull() ?: 0
                    viewModel.publishProjectToCommunity(
                        context = context,
                        tracks = activeTracks,
                        customTitle = titleInput,
                        customDescription = descriptionInput,
                        genreTag = genreTag,
                        metadataTags = tagsInput,
                        price = targetPrice,
                        onFinished = { success ->
                            if (success) {
                                onDismiss()
                            }
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
            ) {
                Text("Vykreslit & Publikovat ✔️", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// --- TAB 5: LEGAL CONSENT & MY PROFILE ---
@Composable
fun LegalAndProfileTab(
    viewModel: StudioViewModel,
    onSelectProject: (com.example.data.database.Project) -> Unit
) {
    val context = LocalContext.current
    val verified by viewModel.isAgeVerified.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = TranslationUtility.get("profile_title"),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = TranslationUtility.get("stats"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(TranslationUtility.get("total_projects"), color = Color.Gray, fontSize = 13.sp)
                        Text("${allProjects.size}", color = AccentNeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            var apiKeyInput by remember { mutableStateOf(com.example.data.network.GeminiClient.customApiKey) }
            var isSavedSuccessfully by remember { mutableStateOf(false) }
            var passwordVisible by remember { mutableStateOf(false) }
            var isTestingConnection by remember { mutableStateOf(false) }
            var testResult by remember { mutableStateOf<String?>(null) }
            var isTestSuccess by remember { mutableStateOf<Boolean?>(null) }
            val coroutineScope = rememberCoroutineScope()

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Color(0xFF2E1A5E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_settings_gemini_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTEGRACE GEMINI API ⚡",
                            fontWeight = FontWeight.Bold,
                            color = AccentNeonCyan,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (com.example.data.network.GeminiClient.customApiKey.isNotBlank()) AccentNeonCyan else Color(0xFF2E2055))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (com.example.data.network.GeminiClient.customApiKey.isNotBlank()) "AKTIVNÍ KEY" else "OFFLINE FALLBACK",
                                color = if (com.example.data.network.GeminiClient.customApiKey.isNotBlank()) Color.Black else Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vložte sem svůj tajný Gemini API klíč pro okamžité generování textů, analýzu hudby a vizuálů přímo přes servery Google AI v reálném čase. Bez klíče aplikace automaticky běží v kreativním offline režimu.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { 
                            apiKeyInput = it
                            isSavedSuccessfully = false
                            testResult = null
                            isTestSuccess = null
                        },
                        placeholder = { Text("AIzaSy...", color = Color.Gray, fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Edit else Icons.Default.Lock,
                                    contentDescription = if (passwordVisible) "Skrýt" else "Zobrazit",
                                    tint = AccentNeonCyan
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentNeonCyan,
                            unfocusedBorderColor = Color(0xFF251A4D)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("profile_gemini_api_key_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (testResult != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTestSuccess == true) Color(0xFF0C2417) else Color(0xFF240C12)
                            ),
                            border = BorderStroke(1.dp, if (isTestSuccess == true) Color(0xFF00FFC2) else Color(0xFFFF5252)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isTestSuccess == true) "✅" else "❌",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = testResult ?: "",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val p = context.getSharedPreferences("spark_settings", android.content.Context.MODE_PRIVATE)
                                p.edit().putString("gemini_key", apiKeyInput.trim()).apply()
                                com.example.data.network.GeminiClient.customApiKey = apiKeyInput.trim()
                                isSavedSuccessfully = true
                                Toast.makeText(context, "Klíč byl uložen do prostředí aplikace! 💾", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSavedSuccessfully) Color(0xFF00C750) else AccentNeonCyan),
                            modifier = Modifier.weight(1f).testTag("profile_save_api_key_btn")
                        ) {
                            Text(
                                text = if (isSavedSuccessfully) "ULOŽENO! ✅" else "ULOŽIT KLÍČ 💾",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (apiKeyInput.trim().isBlank()) {
                                    Toast.makeText(context, "Nejprve zadejte API klíč", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isTestingConnection = true
                                testResult = "Zkouším spojení s Google Gemini API..."
                                isTestSuccess = null
                                coroutineScope.launch {
                                    try {
                                        val originalKey = com.example.data.network.GeminiClient.customApiKey
                                        com.example.data.network.GeminiClient.customApiKey = apiKeyInput.trim()
                                        val response = com.example.data.network.GeminiClient.generateText(
                                            "Zkontroluj toto spojení. Odpověz pouze jediným slovem: 'OK'."
                                        )
                                        isTestingConnection = false
                                        if (response.contains("OK", ignoreCase = true) || response.isNotBlank()) {
                                            isTestSuccess = true
                                            testResult = "Zkouška úspěšná! Spojení funguje. 🎉 Odpověď: $response"
                                            val p = context.getSharedPreferences("spark_settings", android.content.Context.MODE_PRIVATE)
                                            p.edit().putString("gemini_key", apiKeyInput.trim()).apply()
                                            isSavedSuccessfully = true
                                        } else {
                                            isTestSuccess = false
                                            testResult = "Klíč nevrátil správný tvar odpovědi."
                                            com.example.data.network.GeminiClient.customApiKey = originalKey
                                        }
                                    } catch (e: Exception) {
                                        isTestingConnection = false
                                        isTestSuccess = false
                                        testResult = "Chyba testu: ${e.message}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D45)),
                            enabled = !isTestingConnection,
                            modifier = Modifier.weight(1f).border(1.dp, Color(0xFF32245C), RoundedCornerShape(100)).testTag("profile_test_api_key_btn")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = AccentNeonCyan)
                            } else {
                                Text("TEST SPOJENÍ 🔍", color = AccentNeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Clickable verify link
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = TranslationUtility.get("legal_title"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setAgeVerification(context, !verified) }
                            .background(Color(0xFF0F0A1F), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = verified,
                            onCheckedChange = { viewModel.setAgeVerification(context, it) },
                            colors = CheckboxDefaults.colors(checkedColor = AccentNeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = TranslationUtility.get("age_verify"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://denulicz.github.io/Denuli-CZ-/"))
                            context.startActivity(webIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF251D42)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OTEVŘÍT DENULI WEB 🌐", color = AccentNeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = TranslationUtility.get("saved_projects"),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        }

        items(allProjects) { proj ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .clickable { onSelectProject(proj) }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(proj.title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Styl: ${proj.genre}", color = Color.Gray, fontSize = 12.sp)
                }
                IconButton(onClick = { viewModel.deleteProject(proj) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Smazat", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun EffectsRackDialog(
    track: com.example.data.database.AudioTrack,
    viewModel: StudioViewModel,
    onDismiss: () -> Unit
) {
    var eqLow by remember(track) { mutableStateOf(track.eqLow) }
    var eqMid by remember(track) { mutableStateOf(track.eqMid) }
    var eqHigh by remember(track) { mutableStateOf(track.eqHigh) }

    var compEnabled by remember(track) { mutableStateOf(track.compEnabled) }
    var compThreshold by remember(track) { mutableStateOf(track.compThreshold) }
    var compRatio by remember(track) { mutableStateOf(track.compRatio) }

    var reverbEnabled by remember(track) { mutableStateOf(track.reverbEnabled) }
    var reverbWet by remember(track) { mutableStateOf(track.reverbWet) }
    var reverbFeedback by remember(track) { mutableStateOf(track.reverbFeedback) }

    var activeTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "STUDIO PLUGINS RACK 🎚️",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Text(
                    text = "Track: ${track.name}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        },
        containerColor = Color(0xFF130D2E),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0B24), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf("Equalizer 🎛️", "Compressor 📉", "Reverb 🌫️")
                    tabs.forEachIndexed { idx, title ->
                        val isSel = activeTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) AccentNeonCyan else Color.Transparent)
                                .clickable { activeTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.Black else Color.White
                            )
                        }
                    }
                }

                when (activeTab) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "3-PÁSMOVÝ PARAMETRICKÝ EQ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentNeonCyan
                            )
                            Text(
                                "Upravte frekvenční charakteristiku stopy od hlubokých basů po čisté výšky.",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Basy (Low Band < 250Hz)", color = Color.White, fontSize = 12.sp)
                                    Text("${if (eqLow > 0) "+" else ""}${String.format("%.1f", eqLow)} dB", color = AccentNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = eqLow,
                                    onValueChange = {
                                        eqLow = it
                                        viewModel.updateTrackEffects(track, it, eqMid, eqHigh, compEnabled, compThreshold, compRatio, reverbEnabled, reverbWet, reverbFeedback)
                                    },
                                    valueRange = -12f..12f,
                                    modifier = Modifier.testTag("eq_low_slider")
                                )
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Středy (Mid Band)", color = Color.White, fontSize = 12.sp)
                                    Text("${if (eqMid > 0) "+" else ""}${String.format("%.1f", eqMid)} dB", color = AccentNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = eqMid,
                                    onValueChange = {
                                        eqMid = it
                                        viewModel.updateTrackEffects(track, eqLow, it, eqHigh, compEnabled, compThreshold, compRatio, reverbEnabled, reverbWet, reverbFeedback)
                                    },
                                    valueRange = -12f..12f,
                                    modifier = Modifier.testTag("eq_mid_slider")
                                )
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Výšky (High Band > 4kHz)", color = Color.White, fontSize = 12.sp)
                                    Text("${if (eqHigh > 0) "+" else ""}${String.format("%.1f", eqHigh)} dB", color = AccentNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = eqHigh,
                                    onValueChange = {
                                        eqHigh = it
                                        viewModel.updateTrackEffects(track, eqLow, eqMid, it, compEnabled, compThreshold, compRatio, reverbEnabled, reverbWet, reverbFeedback)
                                    },
                                    valueRange = -12f..12f,
                                    modifier = Modifier.testTag("eq_high_slider")
                                )
                            }
                        }
                    }
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "DYNAMICKÁ KOMPRESE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentNeonCyan
                                    )
                                    Text(
                                        "Zarovná hlasitostní špičky a dodá stopě hutnost.",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = compEnabled,
                                    onCheckedChange = {
                                        compEnabled = it
                                        viewModel.updateTrackEffects(track, eqLow, eqMid, eqHigh, it, compThreshold, compRatio, reverbEnabled, reverbWet, reverbFeedback)
                                    },
                                    modifier = Modifier.testTag("compressor_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Column(modifier = Modifier.alpha(if (compEnabled) 1.0f else 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Práh (Threshold)", color = Color.White, fontSize = 12.sp)
                                    Text("${String.format("%.1f", compThreshold)} dB", color = AccentNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = compThreshold,
                                    onValueChange = {
                                        compThreshold = it
                                        viewModel.updateTrackEffects(track, eqLow, eqMid, eqHigh, compEnabled, it, compRatio, reverbEnabled, reverbWet, reverbFeedback)
                                    },
                                    valueRange = -40f..0f,
                                    enabled = compEnabled,
                                    modifier = Modifier.testTag("comp_threshold_slider")
                                )
                            }

                            Column(modifier = Modifier.alpha(if (compEnabled) 1.0f else 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Poměr (Ratio)", color = Color.White, fontSize = 12.sp)
                                    Text("${String.format("%.1f", compRatio)}:1", color = AccentNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = compRatio,
                                    onValueChange = {
                                        compRatio = it
                                        viewModel.updateTrackEffects(track, eqLow, eqMid, eqHigh, compEnabled, compThreshold, it, reverbEnabled, reverbWet, reverbFeedback)
                                    },
                                    valueRange = 1f..10f,
                                    enabled = compEnabled,
                                    modifier = Modifier.testTag("comp_ratio_slider")
                                )
                            }
                        }
                    }
                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "LUSH SCHROEDER REVERB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentNeonCyan
                                    )
                                    Text(
                                        "Simuluje dozvuk velkého koncertního sálu pro celestiální hloubku.",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = reverbEnabled,
                                    onCheckedChange = {
                                        reverbEnabled = it
                                        viewModel.updateTrackEffects(track, eqLow, eqMid, eqHigh, compEnabled, compThreshold, compRatio, it, reverbWet, reverbFeedback)
                                    },
                                    modifier = Modifier.testTag("reverb_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Column(modifier = Modifier.alpha(if (reverbEnabled) 1.0f else 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Úroveň efektu (Wet Mix)", color = Color.White, fontSize = 12.sp)
                                    Text("${(reverbWet * 100).toInt()}%", color = AccentNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = reverbWet,
                                    onValueChange = {
                                        reverbWet = it
                                        viewModel.updateTrackEffects(track, eqLow, eqMid, eqHigh, compEnabled, compThreshold, compRatio, reverbEnabled, it, reverbFeedback)
                                    },
                                    valueRange = 0.0f..1.0f,
                                    enabled = reverbEnabled,
                                    modifier = Modifier.testTag("reverb_wet_slider")
                                )
                            }

                            Column(modifier = Modifier.alpha(if (reverbEnabled) 1.0f else 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Zpětná vazba (Decay Tail)", color = Color.White, fontSize = 12.sp)
                                    Text("${(reverbFeedback * 100).toInt()}%", color = AccentNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = reverbFeedback,
                                    onValueChange = {
                                        reverbFeedback = it
                                        viewModel.updateTrackEffects(track, eqLow, eqMid, eqHigh, compEnabled, compThreshold, compRatio, reverbEnabled, reverbWet, it)
                                    },
                                    valueRange = 0.0f..0.95f,
                                    enabled = reverbEnabled,
                                    modifier = Modifier.testTag("reverb_feedback_slider")
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeonCyan, contentColor = Color.Black)
            ) {
                Text("Hotovo ✔️", fontWeight = FontWeight.Bold)
            }
        }
    )
}
