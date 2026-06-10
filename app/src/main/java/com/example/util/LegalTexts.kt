package com.example.util

object LegalTexts {
    val PRIVACY_POLICY = """
        ZÁSADY OCHRANY OSOBNÍCH ÚDAJŮ (PRIVACY POLICY)
        Poslední aktualizace: Červen 2026

        Aplikace Spark Studio (dále jen „Aplikace“), provozovaná vývojářským týmem Spark Studio (dále jen „My“), bere ochranu Vašich osobních údajů s maximální vážností. Tyto Zásady ochrany osobních údajů byly sestaveny v plném souladu s Nařízením Evropského parlamentu a Rady (EU) 2016/679 o ochraně fyzických osob v souvislosti se zpracováním osobních údajů (GDPR), autorským zákonem ČR (č. 121/2000 Sb.), občanským zákoníkem ČR (č. 89/2012 Sb.) a přísnými požadavky zásad vývojářů Google Play Console pro nakládání s osobními a citlivými finančními údaji.

        ---

        1. SPRÁVCE OSOBNÍCH ÚDAJŮ A KONTAKT
        Správcem osobních údajů je provozovatel aplikace Spark Studio. V případě jakýchkoli dotazů, žádostí o smazání účtu nebo uplatnění Vašich práv nás můžete kontaktovat na e-mailové adrese: denulinkacervinkova@gmail.com nebo přímo v sekci „Můj Profil“ v aplikaci.

        ---

        2. ROZSAH A KATEGORIE ZPRACOVÁVANÝCH ÚDAJŮ
        Zpracováváme pouze ty údaje, které jsou nezbytné pro bezproblémové fungování kreativních, cloudových, distribučních a monetizačních funkcí aplikace:

        A. Uživatelský profil a identifikační údaje
        * Registrační jméno / přezdívka: Slouží k identifikaci autora v komunitním feedu a na tržišti (Marketplace).
        * Věk / Potvrzení plnoletosti (18+): Z důvodu ochrany nezletilých a finančních aspektů distribuce licencí vyžadujeme explicitní ověření věku na kartě „Můj Profil“.

        B. Fakturační a bankovní údaje pro vyplácení odměn (Citlivé finanční údaje)
        * Celé jméno příjemce / název firmy: Slouží k identifikaci příjemce při bankovních převodech a smluvním zpracování licencí.
        * IČO (Identifikační číslo osoby): Nepovinné. Zpracovává se pouze u podnikajících subjektů (OSVČ / firmy).
        * Číslo bankovního účtu (a kód banky, případně IBAN/BIC): Slouží výhradně k realizaci bezhotovostních převodů a odeslání 100 % autorského zisku z prodeje licencí přímo na Váš účet. Tyto údaje jsou přísně střeženy a chráněny proti zneužití v souladu s bezpečnostními protokoly M3 a bankovních standardů.

        C. Generativní AI data (Vstupy a Výstupy)
        * Textová zadání (Prompty): Texty zadané pro generování textů písní (Gemini API) nebo kompletních skladeb prostřednictvím cloudových AI generátorů.
        * Hlasové nahrávky a vokály: Audio data nahraná přes mikrofon uživatele, která slouží pro mixážní pult.
        * Upozornění: Všechny AI prompty a požadavky jsou zpracovávány bezpečně a šifrovaně přes oficiální HTTPS API Google Gemini a integrované cloudové služby. Nejsou využívány pro trénování veřejných modelů bez Vašeho výslovného souhlasu.

        D. Transakční a herní data (Kredity a Tržiště)
        * Zůstatek kreditů (Spark Coins): Virtuální měna ukládaná lokálně (Shared Preferences) a v bezpečné herní databázi (Room DB).
        * Nákupní historie: Seznam zakoupených instrumentů, beatů nebo licencí děl jiných uživatelů na komunitním tržišti (Marketplace).

        ---

        2A. SOULAD SE ZÁSADAMI GOOGLE PLAY (GOOGLE PLAY COHERENCE)
        Aplikace Spark Studio striktně splňuje veškerá pravidla Google Play Developer Program Policies:
        1. Bezpečnost citlivých finančních údajů: Číslo bankovního účtu a fakturační údaje jsou šifrovány a uchovávány lokálně v sandboxovém chráněném úložišti systému Android v souladu s nejvyššími standardy PCI-DSS a GDPR. Nikdy nejsou přenášeny nešifrovaně, ani prodávány či sdíleny s reklamními agenturami.
        2. Garance prostředí zcela BEZ REKLAM (Ad-Free): Aplikace Spark Studio neobsahuje žádné reklamy, bannery ani vyskakovací okna. Nepoužíváme reklamní ID (AAID) a nesdílíme žádná uživatelská data s reklamními sítěmi třetích stran.
        3. Absence neautorizovaného přístupu a phishingu: Aplikace neobsahuje žádné neoficiální přihlašovací brány, weby pro zneužití cizích účtů ani techniky pro tunelování přihlašovacích údajů. Veškerá integrace pro pokročilé vývojáře je omezena na standardní privátní API rozhraní nebo probíhá stoprocentně bezpečně offline.
        4. Transparentnost a šifrování: Veškerý přenos textových promptů probíhá výhradně šifrovaným protokolem HTTPS k oficiálním serverům Google (Gemini API).

        ---

        3. ZABEZPEČENÍ A ŠIFROVÁNÍ ÚDAJŮ
        Všechna data jsou uložena a přenášena s využitím standardních průmyslových bezpečnostních postupů:
        1. Šifrování HTTPS / TLS: Všechna komunikace s Gemini API a herními servery probíhá výhradně přes šifrované protokoly.
        2. Místní šifrování dat: Citlivé preference (včetně uložení plnoletosti, bankovního spojení a zůstatku kreditů) jsou zabezpečeny v chráněném adresáři aplikace na systému Android.
        3. Bezpečná databáze (Room): Projekty, nahrávky a nákupy jsou spravovány prostřednictvím izolovaného databázového jádra SQLite s omezeným přístupem třetích stran.

        ---

        4. VAŠE PRÁVA DLE GDPR
        Jako uživatel máte v souvislosti se svými osobními údaji následující práva:
        * Právo na přístup: Můžete si vyžádat přehled veškerých dat, která o Vás uchováváme.
        * Právo na opravu: Kdykoli můžete změnit své autorské jméno, bankovní účet nebo profilové informace v záložce „Můj Profil“.
        * Právo na výmaz („Právo být zapomenut“): V záložce „Můj Profil“ se nachází tlačítko „Smazat veškerá data (Reset)“, které okamžitě a neodvolatelně odstraní veškeré nahrávky, projekty, transakční logy, bankovní údaje a profilová data z Vašeho zařízení.
        * Právo odvolat souhlas: Svůj souhlas s těmito zásadami můžete kdykoli odvolat odinstalováním aplikace nebo resetováním profilu.

        ---

        5. SOUHLAS A ZMĚNY ZÁSAD
        Používáním aplikace Spark Studio vyjadřujete explicitní souhlas s těmito Zásadami ochrany osobních údajů. Tyto zásady můžeme příležitostně aktualizovat. O všech zásadních změnách Vás budeme informovat prostřednictvím oznámení přímo v aplikaci.
    """.trimIndent()

    val TERMS_OF_SERVICE = """
        SMLUVNÍ PODMÍNKY UŽÍVÁNÍ SLUŽBY (TERMS OF SERVICE)
        Poslední aktualizace: Červen 2026

        Vítejte v aplikaci Spark Studio (dále jen „Aplikace“). Tyto Smluvní podmínky (dále jen „Podmínky“) upravují práva a povinnosti mezi Vámi (dále jen „Uživatel“ nebo „Vy“) a vývojářským týmem Spark Studio (dále jen „Provozovatel“) při užívání naší mobilní platformy pro hudební mixáž, filmový střih, AI generování, prodej autorských děl prostřednictvím Tržiště a distribuci neexkluzivních hudebních licencí.

        ---

        1. PŘIJETÍ PODMÍNEK A PLNOLETOST
        Instalací, registrací přezdívky, zadáním bankovního spojení nebo aktivním užíváním Aplikace vyjadřujete bezvýhradný souhlas s těmito Podmínkami a našimi Zásadami ochrany osobních údajů (GDPR). 

        * Aplikace je určena uživatelům starším 18 let (nebo od 15 let s dohledem zákonného zástupce). Vstupem do sekce distribuce a vyplněním bankovního spojení potvrzujete splnění věkových a právních náležitostí podle zákonů České republiky.
        * Pokud s Podmínkami nesouhlasíte, nejste oprávněni Aplikaci nadále používat.

        ---

        2. AUTORSKÁ DISTRIBUCE JEDNOTLIVÝCH SKLADEB (100% PRO AUTORA)
        Prostřednictvím platformy Spark Studio mohou uživatelé nabízet své originální hudební skladby k licencování třetím stranám (např. tvůrcům videí, YouTuberům, herním vývojářům), kteří hledají neexkluzivní hudební licence pro své komerční či nekomerční audiovizuální projekty.

        1. 100% zisk pro Vás: Spark Studio jako provozovatel si nestrhává žádné provize z distribuce ani prodejů licencí – veškeré finanční prostředky (100 % určené částky) náleží výhradně vám jako autorovi hudby a jsou zaslány přímo na váš bankovní účet.
        2. Nutnost bankovního spojení: Aby mohl zájemce o licenci zaslat platbu a mohl být vytvořen řádný licenční vztah, je nutné, aby autor v sekci „Můj Profil“ vyplnil své celé jméno příjemce (případně IČO, pokud podniká) a funkční číslo bankovního účtu v českém/slovenském národním formátu nebo ve tvaru IBAN. Bez vyplnění těchto údajů není technicky ani právně možné platby realizovat a licence zůstávají nedostupné.
        3. Odpovědnost za zdanění příjmů: V souladu se zákonem o daních z příjmů ČR (č. 586/1992 Sb.) nesete jako příjemce finančních prostředků plnou odpovědnost za řádné přiznání a zdanění těchto autorských příjmů v rámci vašeho ročního daňového přiznání. Provozovatel aplikace neplní úlohu plátce srážkové daně a neodvádí žádné srážky.

        ---

        3. PRÁVNÍ RÁMEC NEEXKLUZIVNÍCH LICENCÍ (AUTORSKÝ ZÁKON ČR)
        Poskytnutí a nákup licencí na Spark Studio podléhá těmto právním úpravám podle autorského zákona ČR (č. 121/2000 Sb. a Nového občanského zákoníku č. 89/2012 Sb.):

        1. Neexkluzivní licence: Poskytnutím skladby k licencování udělujete zájemcům neexkluzivní, časově a místně neomezené právo k užití vašeho hudebního díla jako doprovodného audia pro jejich vlastní videa, streams, prezentace, hry nebo příspěvky na sociálních sítích. Jako autor si nadále ponecháváte plná autorská i vlastnická práva a skladbu můžete nadále libovolně šířit, prodávat nebo upravovat.
        2. Prohlášení o původnosti: Uživatel se zavazuje, že skladba nabízená k distribuci je jeho vlastním, původním kreativním dílem a neporušuje autorská, osobnostní ani jiná práva třetích stran.

        ---

        4. LICENČNÍ UJEDNÁNÍ PRO GENERATIVNÍ AI (CLOUD SYNTHESIS & GEMINI)
        Aplikace obsahuje integrované cloudové funkce pro generování textů a hudby na bázi umělé inteligence.

        1. Vlastnictví promptů: Uživatel prohlašuje, že jím zadané téma, nápady či texty neporušují autorská práva třetích stran, neobsahují vulgarismy a jsou v souladu se zákony České republiky.
        2. Autorská práva k AI výstupům: V souladu s licenčními podmínkami integrovaných modelů přenechává Spark Obsah kompletní komerční práva na vygenerovanou píseň (hudba i vokály) uživateli, který generování inicioval, za předpokladu splnění podmínek registrace a ověření v sekci „Můj Profil“.

        ---

        5. VIRTUÁLNÍ MĚNA (SPARK COINS / KREDITY)
        Aplikace využívá vnitřní herní a bodový systém vyjádřený v kreditech (Spark Mince / Spark Coins 🪙).
        * Kredity slouží k interní barterové a autorské výměně děl v rámci uzavřené simulované komunity Spark Studio. Kredity nemají reálnou peněžní hodnotu a nelze požadovat jejich vyplacení či směnu za reálné měny. Realizace skutečných finančních nákupů neexkluzivních licencí probíhá odděleně formou přímých bankovních převodů autorům v sekci distribuce.

        ---

        6. COMPLIANCE GARANCE BEZ REKLAM (AD-FREE)
        * Provozovatel garantuje, že Aplikace je a navždy zůstane 100% bez reklam. Nejsou zde implementovány žádné reklamní SDK (AdMob apod.), žádné reklamní bannery, ani vyskakovací videoreklamy pro přerušování kreativity uživatele.

        ---

        7. OMEZENÍ ODPOVĚDNOSTI A ZÁVĚR
        Provozovatel neodpovídá za případné technické výpadky cloudových služeb ani za poškození projektů z důvodu nedostatku místa na telefonu uživatele. Tyto podmínky jsou platné a účinné od momentu spuštění verze 62.
    """.trimIndent()
}
