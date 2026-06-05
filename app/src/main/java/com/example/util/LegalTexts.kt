package com.example.util

object LegalTexts {
    val PRIVACY_POLICY = """
        ZÁSADY OCHRANY OSOBNÍCH ÚDAJŮ (PRIVACY POLICY)
        Poslední aktualizace: Červen 2026

        Aplikace Spark Studio (dále jen „Aplikace“), provozovaná vývojářským týmem Spark Studio (dále jen „My“), bere ochranu Vašich osobních údajů s maximální vážností. Tyto Zásady ochrany osobních údajů byly sestaveny v plném souladu s Nařízením Evropského parlamentu a Rady (EU) 2016/679 o ochraně fyzických osob v souvislosti se zpracováním osobních údajů (GDPR) a požadavky zásad vývojářů Google Play Console.

        ---

        1. SPRÁVCE OSOBNÍCH ÚDAJŮ A KONTAKT
        Správcem osobních údajů je provozovatel aplikace Spark Studio. V případě jakýchkoli dotazů, žádostí o smazání účtu nebo uplatnění Vašich práv nás můžete kontaktovat na e-mailové adrese: support@studiodenuli.spark nebo přímo v sekci „Můj Profil“ v aplikaci.

        ---

        2. ROZSAH A KATEGORIE ZPRACOVÁVANÝCH ÚDAJŮ
        Zpracováváme pouze ty údaje, které jsou nezbytné pro bezproblémové fungování kreativních, cloudových a monetizačních funkcí aplikace:

        A. Uživatelský profil a identifikační údaje
        * Registrační jméno / přezdívka: Slouží k identifikaci autora v komunitním feedu a na tržišti (Marketplace).
        * Věk / Potvrzení plnoletosti (18+): Z důvodu ochrany nezletilých a finančních aspektů tržiště vyžadujeme explicitní ověření věku na kartě „Můj Profil“.

        B. Generativní AI data (Vstupy a Výstupy)
        * Textová zadání (Prompty): Texty zadané pro generování textů písní (Gemini API) nebo kompletních skladeb (Suno/Udio).
        * Hlasové nahrávky a vokály: Audio data nahraná přes mikrofon uživatele, která slouží pro mixážní pult.
        * Upozornění: Všechny AI prompty a požadavky jsou zpracovávány bezpečně a šifrovaně přes oficiální HTTPS API Google Gemini a integrované cloudové služby. Nejsou využívány pro trénování veřejných modelů bez Vašeho výslovného souhlasu.

        C. Transakční a herní data (Kredity a Tržiště)
        * Zůstatek kreditů (Spark Coins): Virtuální měna ukládaná lokálně (Shared Preferences) a v bezpečné herní databázi (Room DB).
        * Nákupní historie: Seznam zakoupených instrumentů, beatů nebo projektů jiných uživatelů na komunitním tržišti (Marketplace).

        ---

        3. ZABEZPEČENÍ A ŠIFROVÁNÍ ÚDAJŮ
        Všechna data jsou uložena a přenášena s využitím standardních průmyslových bezpečnostních postupů:
        1. Šifrování HTTPS / TLS: Všechna komunikace s Gemini API a herními servery probíhá výhradně přes šifrované protokoly.
        2. Místní šifrování dat: Citlivé preference (včetně uložení plnoletosti a zůstatku kreditů) jsou zabezpečeny v chráněném adresáři aplikace na systému Android.
        3. Bezpečná databáze (Room): Projekty, nahrávky a nákupy jsou spravovány prostřednictvím izolovaného databázového jádra SQLite s omezeným přístupem třetích stran.

        ---

        4. VAŠE PRÁVA DLE GDPR
        Jako uživatel z Evropského hospodářského prostoru (EHP) máte v souvislosti se svými osobními údaji následující práva:

        * Právo na přístup: Můžete si vyžádat přehled veškerých dat, která o Vás uchováváme.
        * Právo na opravu: Kdykoli můžete změnit své autorské jméno nebo profilové informace v záložce „Můj Profil“.
        * Právo na výmaz („Právo být zapomenut“): V záložce „Můj Profil“ se nachází tlačítko „Smazat veškerá data (Reset)“, které okamžitě a neodvolatelně odstraní veškeré nahrávky, projekty, transakční logy a profilová data z Vašeho zařízení.
        * Právo odvolat souhlas: Svůj souhlas s těmito zásadami můžete kdykoli odvolat odinstalováním aplikace nebo resetováním profilu.

        ---

        5. POUŽÍVÁNÍ SLUŽEB TŘETÍCH STRAN
        Aplikace využívá prověřené služby poskytovatelů, kteří rovněž splňují standardy ochrany dat:
        * Google Services / Android OS: Poskytování systémového rozhraní, uložení preferencí a zpracování audio kodeků.
        * Google Gemini AI API: Generování kreativních textů plynoucích ze zadání (prompty jsou přenášeny v souladu se smluvními podmínkami pro enterprise rozhraní).

        ---

        6. SOUHLAS A ZMĚNY ZÁSAD
        Používáním aplikace Spark Studio vyjadřujete explicitní souhlas s těmito Zásadami ochrany osobních údajů. Tyto zásady můžeme příležitostně aktualizovat. O všech zásadních změnách Vás budeme informovat prostřednictvím oznámení přímo v aplikaci.
    """.trimIndent()

    val TERMS_OF_SERVICE = """
        SMLUVNÍ PODMÍNKY UŽÍVÁNÍ SLUŽBY (TERMS OF SERVICE)
        Poslední aktualizace: Červen 2026

        Vítejte v aplikaci Spark Studio (dále jen „Aplikace“). Tyto Smluvní podmínky (dále jen „Podmínky“) upravují práva a povinnosti mezi Vámi (dále jen „Uživatel“ nebo „Vy“) a vývojářským týmem Spark Studio (dále jen „Provozovatel“) při užívání naší mobilní platformy pro hudební mixáž, filmový střih, AI generování a zapojení do komunitního tržiště.

        ---

        1. PŘIJETÍ PODMÍNEK A PLNOLETOST
        Instalací, registrací přezdívky nebo aktivním užíváním Aplikace vyjadřujete bezvýhradný souhlas s těmito Podmínkami a našimi Zásadami ochrany osobních údajů (GDPR). 

        * Aplikace je určena uživatelům starším 18 let (nebo od 15 let s dohledem zákonného zástupce). Vstupem do sekce „Předplatné / Tržiště“ potvrzujete splnění věkových splatností podle zákonů ČR.
        * Pokud s Podmínkami nesouhlasíte, nejste oprávněni Aplikaci nadále používat.

        ---

        2. LICENČNÍ UJEDNÁNÍ PRO GENERATIVNÍ AI (SUNO/UDIO/GEMINI)
        Aplikace obsahuje prémiové cloudové integrace pro generování hotových písní a videoklipů na bázi umělé inteligence.

        1. Vlastnictví promptů: Uživatel prohlašuje, že jím zadané téma, nápady či texty neporušují autorská práva třetích stran, neobsahují vulgarismy a jsou v souladu se zákony České republiky.
        2. Autorská práva k AI výstupům: V souladu s licenčními podmínkami integrovaných modelů přenechává Spark Obsah kompletní komerční práva na vygenerovanou píseň (hudba i vokály) uživateli, který generování inicioval, za předpokladu splnění podmínek registrace a ověření v sekci „Můj Profil“.
        3. Záruka unikátnosti: Výstupy z generativních zdrojů jsou dynamické. Provozovatel negarantuje absolutní právní unikátnost každého vygenerovaného motivu, uživatel nese plnou odpovědnost za případný komerční prodej mimo platformu.

        ---

        3. VIRTUÁLNÍ MĚNA (SPARK COINS / KREDITY)
        Aplikace využívá vnitřní bodový systém vyjádřený v kreditech (Spark Mince / Spark Coins 🪙).

        * Získávání kreditů: Uživatelé získávají startovní kredity (např. 500 🪙) jako dárek při onboarding ověření a mohou je dále generovat prodejem vlastních vytvořených projektů (píseň + video) v komunitním feedu.
        * Žádná reálná peněžní hodnota: Kredity jsou výhradně in-app herními žetony bez reálné finanční hodnoty. Nelze je nárokovat na vyplacení v reálných měnách (CZK, EUR, USD) a slouží pouze k barterové a autorské výměně děl v rámci simulovaného komunitního ekosystému.
        * Exspirace a zneužití: Při podezření na podvodný přenos nebo manipulaci se zůstatky si provozovatel vyhrazuje právo kredity uživatele anulovat.

        ---

        4. PRAVIDLA KOMUNITNÍHO TRŽIŠTĚ (MARKETPLACE)
        Komunitní tržiště umožňuje uživatelům publikovat své hotové projekty za autorsky stanovenou cenu v kreditech.

        1. Zveřejňování projektů: Publikování díla do společného feedu vyžaduje nahrání plnohodnotného WAV mixed audia s metadaty.
        2. Stanovení ceny: Uživatel má právo při exportu nastavit libovolnou prodejní cenu (např. 0 🪙 až 500 🪙).
        3. Prodeje a provize: Jakmile jiný uživatel zakoupí Vaši skladbu na Marketplace, dojde k okamžitému odpočtu z jeho účtu a připsání plné výše ceny na Váš účet. O události budete okamžitě informováni pop-up upozorněním v záložce „Studio“.
        4. Autorské právo k nákupům: Zakoupením licence na nahrávku získává kupující právo použít ji jako doprovodnou stopu pro své vlastní videoklipy, remixy a osobní přehrávání.

        ---

        5. ZÁKAZ ZNEUŽÍVÁNÍ PLATFORMY
        Je přísně zakázáno:
        * Pokoušet se dekompilovat, modifikovat databázi RoomDB nebo obcházet kontrolní mechanismy bezplatného započtení kreditů.
        * Zneužívat mikrofon pro nahrávání urážlivých, nenávistných či autorsky chráněných děl třetích stran.
        * Vysílat nevhodný či urážlifý textový obsah prostřednictvím chatovacího simulátoru nebo komunitního feedu.

        ---

        6. OMEZENÍ ODPOVĚDNOSTI A ZÁVĚR
        Provozovatel neodpovídá za případné technické výpadky cloudových služeb (Gemini API) ani za poškození projektů z důvodu nedostatku místa na telefonu uživatele. Tyto podmínky jsou platné a účinné od momentu spuštění verze 31.
    """.trimIndent()
}
