package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Language {
    CZ, EN
}

object TranslationUtility {
    private val _currentLanguage = MutableStateFlow(Language.CZ)
    val currentLanguage: StateFlow<Language> = _currentLanguage

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == Language.CZ) Language.EN else Language.CZ
    }

    private val translations = mapOf(
        "app_title" to mapOf(Language.CZ to "Spark Studio 🎧", Language.EN to "Spark Studio 🎧"),
        "app_subtitle" to mapOf(Language.CZ to "● AI Multitrack & Video Studio", Language.EN to "● AI Multitrack & Video Studio"),
        "active_project" to mapOf(Language.CZ to "AKTIVNÍ PROJEKT", Language.EN to "ACTIVE PROJECT"),
        "choose_or_create" to mapOf(Language.CZ to "Zvolte nebo vytvořte píseň", Language.EN to "Select or create a song"),
        "projects_btn" to mapOf(Language.CZ to "Projekty", Language.EN to "Projects"),
        "new_btn" to mapOf(Language.CZ to "+ Nový", Language.EN to "+ New"),
        "quick_guide" to mapOf(Language.CZ to "💡 RYCHLÝ PRŮVODCE STUDIEM", Language.EN to "💡 QUICK STUDIO GUIDE"),
        "step_1_title" to mapOf(Language.CZ to "✍️ 1. TEXTY & TÉMA", Language.EN to "✍️ 1. LYRICS & TOPIC"),
        "step_1_desc" to mapOf(Language.CZ to "Napište nebo vložte vlastní text písně dolů, nebo stiskněte AI TVORBA pro složení textu s Gemini.", Language.EN to "Type or paste your own lyrics below, or press AI GENERATE to write lyrics using Gemini."),
        "step_2_title" to mapOf(Language.CZ to "🎛️ 2. MIX & EFEKTY", Language.EN to "🎛️ 2. MIX & EFFECTS"),
        "step_2_desc" to mapOf(Language.CZ to "Zvolte náladu, efekt vokálu (např. Robot) a přírodní atmosféru (např. Rain) na ovládacím panelu.", Language.EN to "Choose a mood, vocal effect (e.g., Robot), and natural ambience (e.g., Rain) on the controls panel."),
        "step_3_title" to mapOf(Language.CZ to "▶️ 3. LIVE SYNTÉZA", Language.EN to "▶️ 3. LIVE SYNTHESIS"),
        "step_3_desc" to mapOf(Language.CZ to "Klikněte na zelené tlačítko 'PŘEHRÁT' v Mixéru. Náš offline syntezátor okamžitě vytvoří originální doprovod, basu a melodii generovanou přímo z vašich slov!", Language.EN to "Click the green 'PLAY' button in the Mixer. Our offline synthesizer will instantly create original backing, bass, and melody generated directly from your words!"),
        "step_4_title" to mapOf(Language.CZ to "✉️ 4. EXPORT A REÁLNÉ VIDEO", Language.EN to "✉️ 4. EXPORT & REAL VIDEO"),
        "step_4_desc" to mapOf(Language.CZ to "Stáhněte si hotový song ve formátu WAV/MP3, nebo vygenerujte plnohodnotné lyric video (MP4) s vizuálním ekvalizérem a běžícími texty!", Language.EN to "Download your finished song as WAV/MP3, or generate a full-featured lyric video (MP4) complete with visual equalizer and scrolling text!"),
        "load_promo_btn" to mapOf(Language.CZ to "▶ NAČÍST NOVINKU 'PROJEKT JEDNA DVĚ TŘI' ⚡", Language.EN to "▶ LOAD NEW FAVORITE 'PROJECT ONE TWO THREE' ⚡"),
        
        "nav_home" to mapOf(Language.CZ to "Domů", Language.EN to "Home"),
        "nav_studio" to mapOf(Language.CZ to "Studio", Language.EN to "Studio"),
        "nav_video" to mapOf(Language.CZ to "Video", Language.EN to "Video"),
        "nav_chat" to mapOf(Language.CZ to "Chat", Language.EN to "Chat"),
        "nav_market" to mapOf(Language.CZ to "Tržiště", Language.EN to "Market"),
        "nav_profile" to mapOf(Language.CZ to "Můj Denuli", Language.EN to "My Denuli"),
        
        "lyrics_tab" to mapOf(Language.CZ to "NÁVRH TEXTU PÍSNĚ", Language.EN to "SONG LYRICS DESIGN"),
        "lyrics_placeholder" to mapOf(Language.CZ to "Zadejte nebo vygeneruje text...", Language.EN to "Enter or generate lyrics..."),
        "generate_lyrics_btn" to mapOf(Language.CZ to "AI Generovat Text ✨", Language.EN to "AI Generate Lyrics ✨"),
        "topic_prompt" to mapOf(Language.CZ to "Námět písně (např. Láska ke hvězdám):", Language.EN to "Song theme (e.g. Love for the stars):"),
        
        "vibe_setup" to mapOf(Language.CZ to "NASTAVENÍ ZVUKU & ATMOSFÉRY", Language.EN to "SOUND & ATMOSPHERE SETUP"),
        "select_genre" to mapOf(Language.CZ to "Žánr (Zdroje zvuku):", Language.EN to "Genre (Sound Engine):"),
        "vocal_effect" to mapOf(Language.CZ to "Efekt vokálu:", Language.EN to "Vocal effect:"),
        "bg_ambience" to mapOf(Language.CZ to "Přírodní atmosféra:", Language.EN to "Natural ambience:"),
        
        "play_music" to mapOf(Language.CZ to "PŘEHRÁT SKLADBŮ 🎧", Language.EN to "PLAY COMPOSITION 🎧"),
        "stop_music" to mapOf(Language.CZ to "ZASTAVIT ⏹", Language.EN to "STOP ⏹"),
        "creating_audio" to mapOf(Language.CZ to "Syntetizuji zvuk...", Language.EN to "Synthesizing tone..."),
        
        "export_video" to mapOf(Language.CZ to "VYGENEROVAT VIDEOKLIP (MP4) 🎬", Language.EN to "GENERATE VIDEOCLIP (MP4) 🎬"),
        "video_status" to mapOf(Language.CZ to "Vizuální studio:", Language.EN to "Visual studio:"),
        "video_idle" to mapOf(Language.CZ to "Připraveno ke generování", Language.EN to "Ready to compile"),
        "export_audio" to mapOf(Language.CZ to "STÁHNOUT WAV ⚙️", Language.EN to "DOWNLOAD WAV ⚙️"),
        
        "market_title" to mapOf(Language.CZ to "SPARK TRŽIŠTĚ", Language.EN to "SPARK MARKETPLACE"),
        "market_sub" to mapOf(Language.CZ to "Kupte si profesionální beaty, vokální šablony a ambientní packy.", Language.EN to "Acquire premium beats, vocal filters, and sound expansion packs."),
        "price" to mapOf(Language.CZ to "Cena:", Language.EN to "Price:"),
        "purchased" to mapOf(Language.CZ to "Zakoupeno", Language.EN to "Purchased"),
        "buy" to mapOf(Language.CZ to "Koupit", Language.EN to "Buy"),
        
        "chat_assistant" to mapOf(Language.CZ to "AI ASISTENT STUDIA", Language.EN to "STUDIO AI CO-WRITER"),
        "chat_welcome" to mapOf(Language.CZ to "Ahoj! Jsem tvůj AI kreativní asistent. Zeptej se mě na cokoliv ohledně textů, aranží nebo jak ovládat hudební syntézu!", Language.EN to "Hello! I am your AI creative guide. Ask me anything about lyrics writing, arrangement, or the sound engine!"),
        "send" to mapOf(Language.CZ to "Odeslat", Language.EN to "Send"),
        "chat_hint" to mapOf(Language.CZ to "Napiš zprávu...", Language.EN to "Write a message..."),
        
        "legal_title" to mapOf(Language.CZ to "PODMÍNKY A SOUHLASY", Language.EN to "TERMS & VERIFICATION"),
        "legal_desc" to mapOf(Language.CZ to "Podmínky služby vyžadují ověření věku pro přístup k pokročilému AI generování.", Language.EN to "Terms of service require age verification to access deep generative AI workflows."),
        "age_verify" to mapOf(Language.CZ to "Potvrzuji, že mi je více než 15 let (nebo mám souhlas zákonného zástupce)", Language.EN to "I verify that I am over 15 years old (or have parental/guardian consent)"),
        "github_web" to mapOf(Language.CZ to "Otevřít oficiální web na GitHubu 🌐", Language.EN to "Open official web on GitHub 🌐"),
        "profile_title" to mapOf(Language.CZ to "PROFIL DENULI", Language.EN to "DENULI PROFILE"),
        "stats" to mapOf(Language.CZ to "STATISTIKY TVORBY", Language.EN to "STUDIO STATISTICS"),
        "total_projects" to mapOf(Language.CZ to "Celkem písní:", Language.EN to "Total songs:"),
        "saved_projects" to mapOf(Language.CZ to "ULOŽENÉ SKLADBY", Language.EN to "SAVED PROJECTS")
    )

    fun get(key: String): String {
        return translations[key]?.get(_currentLanguage.value) ?: key
    }
}
