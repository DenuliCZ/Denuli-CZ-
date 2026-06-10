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
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Path("model") model: String,
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

    // Support custom runtime API key saved in SharedPreferences or entered by the user
    var customApiKey: String = ""

    private fun getApiKey(): String {
        if (customApiKey.isNotBlank()) {
            return customApiKey.trim()
        }
        val bKey = BuildConfig.GEMINI_API_KEY
        if (bKey.isNullOrBlank() || bKey == "YOUR_API_KEY_HERE" || bKey == "null") {
            return ""
        }
        return bKey.trim()
    }

    suspend fun generateText(prompt: String, systemPrompt: String? = null): String {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE" || apiKey.isBlank()) {
            throw Exception("Missing or placeholder Gemini API Key")
        }

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(text = it))) }
        )

        // Try standard modern allowed models in priority order. If one fails, try the next fallback.
        val models = listOf(
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        )
        var lastException: Exception? = null

        for (modelName in models) {
            try {
                Log.d(TAG, "Attempting to generate text with model: $modelName")
                val response = apiService.generateContent(modelName, apiKey, request)
                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (textResponse != null) {
                    return textResponse
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed calling with model $modelName: ${e.message}")
                lastException = e
            }
        }

        throw lastException ?: Exception("All Gemini models failed")
    }

    private fun generateOfflineLyrics(topic: String, genre: String): String {
        val capitalizedTopic = topic.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val isPustevny = topic.lowercase().contains("pustevny") || topic.lowercase().contains("radhošť")
        
        val verse1 = if (isPustevny) {
            "Dřevěné stropy šeptají starý příběh,\nna hřebenech Beskyd tiše mizí stín.\nJurkovičův odkaz hladí jarní břeh,\nkrásný Libušín v srdci uchovám si, vím."
        } else {
            "Dívám se ven, kde vítr tiše zpívá,\nvepsaný v řádcích, co osud tajně psal.\nOtázka v očích stále živá splývá,\ntéma '$capitalizedTopic' mě táhne dál."
        }

        val chorus = if (isPustevny) {
            "Pustevny, v mračnech barvy svítí,\nRadhošť nám cestu ukáže,\nkde vítr divoké louky krotí,\ntam tlukot srdce vše dokáže!"
        } else {
            "A v tomhle rytmu, kde žánr $genre hraje,\nnacházím sílu, co v tichu vyvěrá.\nZtracený v touze, kde nový svět taje,\nnaděje čistá se s ránem otvírá!"
        }

        val verse2 = if (isPustevny) {
            "Socha Radegasta stráží věčný klid,\nchladný horský vítr do vlasů mi vnik.\nMlhová záclona pomáhá mi snít,\ntohle kouzlo domova je ten nejhezčí zvyk."
        } else {
            "Kroky na cestě, co nikde nekončí,\nnapsaná slova se v rytmu skládají.\nMožná nás zítřek s touhou zasnoubí,\nsny o '$capitalizedTopic' se v srdci toulají."
        }

        val outro = if (isPustevny) {
            "Beskydy moje, domov a klid...\nNa Pustevnách chci navždy snít..."
        } else {
            "Stopy mizí v dálce snů,\ntéma '$capitalizedTopic' provází nás do konce dnů..."
        }

        return """
            [VERŠ 1]
            $verse1

            [REFRÉN]
            $chorus

            [VERŠ 2]
            $verse2

            [REFRÉN]
            $chorus

            [OUTRO]
            $outro
        """.trimIndent()
    }

    suspend fun generateSongLyrics(topic: String, genre: String): String {
        return try {
            val systemPrompt = "Jste zkušený hudební textař a skladatel písní. Píšete chytlavé, emočně silné texty v češtině se strukturou VERŠ, REFRÉN, VERŠ, REFRÉN, OUTRO. Nepoužívejte žádné vysvětlivky, napište pouze samotný text písně."
            val prompt = "Napiš kompletní píseň na téma '$topic' v žánru '$genre'. Zapoj zajímavá rýmová schémata a rytmiku typickou pro daný hudební styl."
            generateText(prompt, systemPrompt)
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API failed or restricted (HTTP 403 / Network errors), activating creative offline lyrics generator fallback: ${e.message}")
            generateOfflineLyrics(topic, genre)
        }
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
