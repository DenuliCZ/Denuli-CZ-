package com.example.util

object TranslationUtility {

    // Simple yet comprehensive in-memory translation dictionary for Czech and English prompts to 10+ major world languages
    private val translationDictionary = mapOf(
        "Můj Denuli" to mapOf(
            "CS" to "Můj Denuli Profile",
            "EN" to "My Denuli Settings",
            "SK" to "Môj Denuli Profil",
            "DE" to "Mein Denuli Profil",
            "ES" to "Mi Perfil Denuli",
            "FR" to "Mon Profil Denuli",
            "PL" to "Mój Profil Denuli",
            "IT" to "Il Mio Profilo Denuli",
            "UA" to "Мій Профіль Denuli",
            "VI" to "Hồ sơ Denuli của tôi"
        ),
        "EVIDENCE NÁKUPŮ & ÚČETNICTVÍ" to mapOf(
            "CS" to "EVIDENCE NÁKUPŮ & ÚČETNICTVÍ",
            "EN" to "PURCHASE HISTORY & ACCOUNTING",
            "SK" to "EVIDENCIA NÁKUPOV & ÚČTOVNÍCTVO",
            "DE" to "EINKAUFSVERZEICHNIS & BUCHHALTUNG",
            "ES" to "HISTORIAL DE COMPRAS Y CONTABILIDAD",
            "FR" to "HISTORIQUE D'ACHAT & COMPTABILITÉ",
            "PL" to "REJESTR ZAKUPÓW I KSIĘGOWOŚĆ",
            "IT" to "CRONOLOGIA ACQUISTI E CONTABILITÀ",
            "UA" to "ІСТОРІЯ ПОКУПОК ТА БУХГАЛТЕРІЯ",
            "VI" to "LỊCH SỬ MUA HÀNG & KẾ TOÁN"
        ),
        "AI RYCHLÁ POMOC & KOPILOT" to mapOf(
            "CS" to "AI RYCHLÁ POMOC & KOPILOT",
            "EN" to "AI QUICK HELP & CO-PILOT",
            "SK" to "AI RÝCHLA POMOC & KOPILOT",
            "DE" to "AI CO-PILOT SCHNELLHILFE",
            "ES" to "ASISTENCIA RÁPIDA DE IA Y COPILOTO",
            "FR" to "ASSISTANCE RAPIDE IA & COPILOTE",
            "PL" to "SZYBKA POMOC AI I KOPILOT",
            "IT" to "AI ASSISTENZA RAPIDA E COPILOTA",
            "UA" to "ШВИДКА ДОПОМОГА AI ТА КОПІЛОТ",
            "VI" to "HỖ TRỢ NHANH AI & TRỢ LÝ PHỤ"
        ),
        "Položit dotaz 🚀" to mapOf(
            "CS" to "Položit dotaz 🚀",
            "EN" to "Ask AI Co-Pilot 🚀",
            "SK" to "Položiť otázku 🚀",
            "DE" to "Frage stellen 🚀",
            "ES" to "Hacer pregunta 🚀",
            "FR" to "Poser une question 🚀",
            "PL" to "Zadaj pytanie 🚀",
            "IT" to "Fai una domanda 🚀",
            "UA" to "Задати питання 🚀",
            "VI" to "Đặt câu hỏi 🚀"
        ),
        "Zeptejte se na mixing nebo chatujte..." to mapOf(
            "CS" to "Zeptejte se na mixing nebo chatujte...",
            "EN" to "Ask about vocal tracks, mixing or chat...",
            "SK" to "Opýtajte sa na mixing alebo chatujte...",
            "DE" to "Fragen Sie nach Mixen oder chatten...",
            "ES" to "Pregunta sobre mezclas o chatea...",
            "FR" to "Demander sur le mixage ou discuter...",
            "PL" to "Zapytaj o miksowanie lub czatuj...",
            "IT" to "Chiedi del missaggio o chatta...",
            "UA" to "Запитати про міксування або чат...",
            "VI" to "Hỏi về phối nhạc hoặc nhắn tin..."
        ),
        "KOLABORATIVNÍ TÝMOVÝ CHAT" to mapOf(
            "CS" to "KOLABORATIVNÍ TÝMOVÝ CHAT",
            "EN" to "TEAM CHAT & COLLABORATION",
            "SK" to "KOLABORATÍVNY TÍMOVÝ CHAT",
            "DE" to "TEAMCHAT & ZUSAMMENARBEIT",
            "ES" to "CHAT DE EQUIPO Y COLABORACIÓN",
            "FR" to "CHAT D'ÉQUIPE & COLLABORATION",
            "PL" to "CZAT ZESPOŁOWY I WSPÓŁPRACA",
            "IT" to "CHAT DI GRUPPO E COLLABORAZIONE",
            "UA" to "КОЛЕКТИВНИЙ ЧАТ ТА СПІВПРАЦЯ",
            "VI" to "TRÒ CHUYỆN NHÓM & HỢP TÁC"
        ),
        "HLAVNÍ MULTITRACK ČASOVÁ OSA" to mapOf(
            "CS" to "HLAVNÍ MULTITRACK ČASOVÁ OSA",
            "EN" to "MAIN MULTI-TRACK AUDIO TIMELINE",
            "SK" to "HLAVNÁ MULTITRACK ČASOVÁ OSA",
            "DE" to "HAUPT-MULTITRACK-AUDIOZEITLEISTE",
            "ES" to "LÍNEA DE TIEMPO MULTIPISTA PRINCIPAL",
            "FR" to "LIGNE DE TEMPS MULTIPISTE PRINCIPALE",
            "PL" to "GŁÓWNA OŚ CZASU MULTITRACK",
            "IT" to "LINEA TEMPORALE MULTITRACCIA PRINCIPALE",
            "UA" to "ГОЛОВНА МУЛЬТИТРЕКОВА ЧАСОВА ШКАЛА",
            "VI" to "DÒNG THỜI GIAN NHẠC ĐA TRACK CHÍNH"
        ),
        "FONDY & LICENČNÍ STŘEDISKO" to mapOf(
            "CS" to "FONDY & LICENČNÍ STŘEDISKO",
            "EN" to "MUSIC RIGHTS & COMMERCIAL LICENSING",
            "SK" to "FONDY & LICENČNÉ STREDISKO",
            "DE" to "LIZENZZENTRUM & MUSIKRECHTE",
            "ES" to "CENTRO DE DERECHOS DE AUTOR Y LICENCIAS",
            "FR" to "CENTRE DES DROITS & LICENCES COMMERCIALES",
            "PL" to "CENTRUM LICENCJI I PRAW AUTORSKICH",
            "IT" to "CENTRO DIRITTI D'AUTORE E LICENZE",
            "UA" to "ЦЕНТР ЛІЦЕНЗУВАННЯ ТА АВТОРСЬКИХ ПРАВ",
            "VI" to "TRUNG TÂM BẢN QUYỀN & CẤP PHÉP"
        ),
        "PRÁVNÍ SOUHLAS S GDPR" to mapOf(
            "CS" to "PRÁVNÍ SOUHLAS S GDPR",
            "EN" to "GDPR COMPLIANCE & DATA PRIVACY",
            "SK" to "PRÁVNY SÚHLAS S GDPR",
            "DE" to "DSGVO-EINWILLIGUNG & DATENSCHUTZ",
            "ES" to "CONSENTIMIENTO DE PRIVACIDAD GDPR",
            "FR" to "SÉCURITÉ & CONFIDENTIALITÉ DES DONNÉES (RGPD)",
            "PL" to "ZGODNOŚĆ Z RODO I OCHRONA DANYCH",
            "IT" to "CONSENSO SULLA PRIVACY GDPR",
            "UA" to "ЗГОДА НА ОБРОБКУ ДАНИХ (GDPR)",
            "VI" to "TUÂN THỦ GDPR & BẢO MẬT DỮ LIỆU"
        )
    )

    /**
     * Translates Czech phrases to target languages. Falls back to English if target language key does not exist.
     */
    fun translate(inputText: String, lang: String): String {
        val entry = translationDictionary[inputText] ?: return inputText
        return entry[lang] ?: entry["EN"] ?: inputText
    }

    /**
     * Resolves localized text using a ternary-like multi language selector for 10 languages
     */
    fun resolve(lang: String, cs: String, en: String): String {
        return when (lang) {
            "CS" -> cs
            "EN" -> en
            "SK" -> translateSlovak(cs)
            "DE" -> translateGerman(en)
            "ES" -> translateSpanish(en)
            "FR" -> translateFrench(en)
            "PL" -> translatePolish(cs)
            "IT" -> translateItalian(en)
            "UA" -> translateUkrainian(en)
            "VI" -> translateVietnamese(en)
            else -> en
        }
    }

    private fun translateSlovak(cs: String): String {
        return cs
            .replace("Můj", "Môj")
            .replace("Domů", "Domov")
            .replace("účetnictví", "účtovníctvo")
            .replace("Uložit", "Uložiť")
            .replace("Zrušit", "Zrušiť")
            .replace("Zavřít", "Zatvoriť")
            .replace("Vytvořit", "Vytvoriť")
            .replace("Nahrávání", "Nahrávanie")
            .replace("projekt", "projekt")
            .replace("pomoc", "pomoc")
            .replace("Aktivní", "Aktívny")
            .replace("Nastavení", "Nastavenie")
    }

    private fun translateGerman(en: String): String {
        return when {
            en.contains("PURCHASE", true) -> "INTEGRIERTE EINKÄUFE"
            en.contains("TIMELINE", true) -> "ZEITLEISTE DES STUDIOS"
            en.contains("CHAT", true) -> "TEAM-DISKUSSIONEN"
            en.contains("MARKET", true) -> "MARKTPLATZ & FONTS"
            en.contains("SETTINGS", true) -> "EINSTELLUNGEN"
            en.contains("AI", true) -> "KÜNSTLICHE INTELLIGENZ"
            en.contains("PREMIUM", true) -> "PREMIUM LIZENZ"
            en.contains("PRIVACY", true) -> "DATENSCHUTZ CENTER"
            else -> en
        }
    }

    private fun translateSpanish(en: String): String {
        return when {
            en.contains("PURCHASE", true) -> "REGISTRO DE COMPRAS"
            en.contains("TIMELINE", true) -> "LÍNEA DE TIEMPO DEL ESTUDIO"
            en.contains("CHAT", true) -> "CHAT DE COLABORACIÓN IA"
            en.contains("MARKET", true) -> "MERCADO FINANCIERO"
            en.contains("SETTINGS", true) -> "AJUSTES"
            en.contains("AI", true) -> "INTELIGENCIA ARTIFICIAL"
            en.contains("PREMIUM", true) -> "LICENCIA DE EXPORTACIÓN VIP"
            else -> en
        }
    }

    private fun translateFrench(en: String): String {
        return when {
            en.contains("PURCHASE", true) -> "REGISTRE SÉCURISÉ DES ACHATS"
            en.contains("TIMELINE", true) -> "TABLE DE MIXAGE MULTIPISTE"
            en.contains("CHAT", true) -> "DISCUSSION INTERACTIVE"
            en.contains("MARKET", true) -> "BOUTIQUE D'EFFETS"
            en.contains("SETTINGS", true) -> "CONFIGURATIONS"
            else -> en
        }
    }

    private fun translatePolish(cs: String): String {
        return cs
            .replace("Můj", "Mój")
            .replace("Domů", "Główny")
            .replace("Skladby", "Utwory")
            .replace("Uložit", "Zapisz")
            .replace("Zrušit", "Anuluj")
    }

    private fun translateItalian(en: String): String {
        return when {
            en.contains("PURCHASE", true) -> "STORICO TRANSAZIONI"
            en.contains("TIMELINE", true) -> "SCALETTA TRACCE STUDIO"
            else -> en
        }
    }

    private fun translateUkrainian(en: String): String {
        return when {
            en.contains("PURCHASE", true) -> "ОБЛІК ОПЛАТ PLAY"
            en.contains("TIMELINE", true) -> "СТУДІЙНА ТАЙМЛАЙН-ШКАЛА"
            else -> en
        }
    }

    private fun translateVietnamese(en: String): String {
        return when {
            en.contains("PURCHASE", true) -> "QUẢN LÝ GIAO DỊCH"
            en.contains("TIMELINE", true) -> "TAYM-LAI MULTITRACK"
            else -> en
        }
    }
}
