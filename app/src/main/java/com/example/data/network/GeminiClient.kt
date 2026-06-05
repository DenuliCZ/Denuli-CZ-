package com.example.data.network

import com.studiodenuli.spark.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import android.util.Log

// --- GSON Entities for Gemini REST API ---
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null,
    val safetySettings: List<SafetySetting>? = null
)

data class SafetySetting(
    val category: String,
    val threshold: String
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 1500
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    suspend fun generateText(prompt: String, systemPrompt: String? = null): String {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE") {
            Log.e(TAG, "Gemini API Key is not set in BuildConfig")
            return "Chyba: API klíč není nakonfigurovaný v AI Studio Secrets panelu."
        }

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(text = it))) },
            safetySettings = listOf(
                SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"),
                SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"),
                SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"),
                SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE")
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Prázdná odpověď od modelu."
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini: ${e.message}", e)
            throw e
        }
    }

    suspend fun generateSongLyrics(topic: String, genre: String): String {
        val systemPrompt = "Jste zkušený hudební textař a skladatel písní. Píšete chytlavé, emočně silné texty v češtině se strukturou VERŠ, REFRÉN, VERŠ, REFRÉN, OUTRO. Nepoužívejte žádné vysvětlivky, napište pouze samotný text písně."
        val prompt = "Napiš kompletní píseň na téma '$topic' v žánru '$genre'. Zapoj zajímavá rýmová schémata a rytmiku typickou pro daný hudební styl."
        return generateText(prompt, systemPrompt)
    }

    suspend fun generateMusicBlueprint(lyrics: String, genre: String): String {
        val systemPrompt = "Jste hlavní hudební inženýr a skladatel (Lyria Cloud System)."
        val prompt = """
            Zanalyzuj tento text písně: "$lyrics"
            Navrhni optimální hudební uspořádání pro žánr "$genre".
            Vyjádři v 1 krátké větě, jaké tempo (BPM), nástroje a vokální aranžmá AI zvolilo pro syntézu v cloudu.
        """.trimIndent()
        return try {
            generateText(prompt, systemPrompt)
        } catch (e: Exception) {
            "Generuji hluboké syntezátory, rytmické bicí a efektové filtry optimalizované pro žánr $genre."
        }
    }

    suspend fun analyzeMoodAndRecommendVisuals(lyrics: String): String {
        val systemPrompt = "Jste hlavní kreativní ředitel pro videoklipy a vizuální efekty."
        val prompt = """
            Zanalyzuj náladu následujícího textu písně a navrhni pro ni odpovídající vizuální styl:
            Text: "$lyrics"
            
            Uveď pouze krátký, výstižný popis (max 2 věty) popisující barvy, vizuální prvky (částice, vlny apod.) a celkovou náladu pro generování videoklipu.
        """.trimIndent()
        
        return try {
            generateText(prompt, systemPrompt)
        } catch (e: Exception) {
            "Energická fialovo-tyrkysová vizuální show s elektrickým ekvalizérem a částicemi."
        }
    }
}
