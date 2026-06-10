# ZÁSADY OCHRANY OSOBNÍCH ÚDAJŮ (PRIVACY POLICY)
**Poslední aktualizace: Červen 2026**

Aplikace **Spark Studio** (dále jen „Aplikace“), provozovaná vývojářským týmem Spark Studio (dále jen „My“), bere ochranu Vašich osobních údajů s maximální vážností. Tyto Zásady ochrany osobních údajů byly sestaveny v plném souladu s Nařízením Evropského parlamentu a Rady (EU) 2016/679 o ochraně fyzických osob v souvislosti se zpracováním osobních údajů (GDPR), autorským zákonem ČR (č. 121/2000 Sb.), občanským zákoníkem ČR (č. 89/2012 Sb.) a přísnými požadavky zásad vývojářů Google Play Console pro nakládání s osobními a citlivými finančními údaji.

---

## 1. SPRÁVCE OSOBNÍCH ÚDAJŮ A KONTAKT
Správcem osobních údajů je provozovatel aplikace Spark Studio. V případě jakýchkoli dotazů, žádostí o smazání účtu nebo uplatnění Vašich práv nás můžete kontaktovat na e-mailové adrese: **denulinkacervinkova@gmail.com** nebo přímo v sekci „Můj Profil“ v aplikaci.

---

## 2. ROZSAH A KATEGORIE ZPRACOVÁVANÝCH ÚDAJŮ
Zpracováváme pouze ty údaje, které jsou nezbytné pro bezproblémové fungování kreativních, cloudových, distribučních a monetizačních funkcí aplikace:

### A. Uživatelský profil a identifikační údaje
*   **Registrační jméno / přezdívka**: Slouží k identifikaci autora v komunitním feedu a na tržišti (Marketplace).
*   **Věk / Potvrzení plnoletosti (18+)**: Z důvodu ochrany nezletilých a finančních aspektů distribuce licencí vyžadujeme explicitní ověření věku na kartě „Můj Profil“.

### B. Fakturační a bankovní údaje pro vyplácení odměn (Citlivé finanční údaje)
*   **Celé jméno příjemce / název firmy**: Slouží k identifikaci příjemce při bankovních převodech a smluvním zpracování licencí.
*   **IČO (Identifikační číslo osoby)**: Nepovinné. Zpracovává se pouze u podnikajících subjektů (OSVČ / firmy).
*   **Číslo bankovního účtu (a kód banky, případně IBAN/BIC)**: Slouží výhradně k realizaci bezhotovostních převodů a odeslání 100 % autorského zisku z prodeje licencí přímo na Váš účet. Tyto údaje jsou přísně střeženy a chráněny proti zneužití v souladu s bezpečnostními protokoly systému Android a bankovních standardů.

### C. Generativní AI data (Vstupy a Výstupy)
*   **Textová zadání (Prompty)**: Texty zadané pro generování textů písní (Gemini API) nebo kompletních skladeb prostřednictvím cloudových AI generátorů.
*   **Hlasové nahrávky a vokály**: Audio data nahraná přes mikrofon uživatele, která slouží pro mixážní pult.
*   *Upozornění*: Všechny AI prompty a požadavky jsou zpracovávány bezpečně a šifrovaně přes oficiální HTTPS API Google Gemini a integrované cloudové služby. Nejsou využívány pro trénování veřejných modelů bez Vašeho výslovného souhlasu.

### D. Transakční a herní data (Kredity a Tržiště)
*   **Zůstatek kreditů (Spark Coins)**: Virtuální měna ukládaná lokálně (Shared Preferences) a v bezpečné herní databázi (Room DB).
*   **Nákupní historie**: Seznam zakoupených instrumentů, beatů nebo licencí děl jiných uživatelů na komunitním tržišti (Marketplace).

---

## 2A. SOULAD SE ZÁSADAMI GOOGLE PLAY (GOOGLE PLAY DEVELOPER POLICIES COHERENCE)
Aplikace Spark Studio je navržena s maximálním důrazem na bezpečnost a striktně splňuje veškerá pravidla Google Play Developer Program Policies:
1.  **Bezpečnost citlivých finančních údajů**: Číslo bankovního účtu a fakturační údaje jsou šifrovány a uchovávány lokálně v sandboxovém chráněném úložišti systému Android v souladu s nejvyššími standardy PCI-DSS a GDPR. Nikdy nejsou přenášeny nešifrovaně, ani prodávány či sdíleny s reklamními agenturami.
2.  **Garance prostředí zcela BEZ REKLAM (Ad-Free)**: Aplikace Spark Studio neobsahuje žádné reklamy, bannery ani vyskakovací okna. Nepoužíváme reklamní ID (AAID) a nesdílíme žádná uživatelská data s reklamními sítěmi třetích stran.
3.  **Absence neautorizovaného přístupu a phishingu**: Aplikace neobsahuje žádné neoficiální přihlašovací brány, weby pro zneužití cizích účtů ani techniky pro tunelování přihlašovacích údajů. Veškerá integrace pro pokročilé vývojáře je omezena na standardní privátní API rozhraní nebo probíhá stoprocentně bezpečně offline.
4.  **Transparentnost a šifrování**: Veškerý přenos textových promptů probíhá výhradně šifrovaným protokolem HTTPS k oficiálním serverům Google (Gemini API).

---

## 3. ZABEZPEČENÍ A ŠIFROVÁNÍ ÚDAJŮ
Všechna data jsou uložena a přenášena s využitím standardních průmyslových bezpečnostních postupů:
1.  **Šifrování HTTPS / TLS**: Všechna komunikace s Gemini API a herními servery probíhá výhradně přes šifrované protokoly.
2.  **Místní šifrování dat**: Citlivé preference (včetně uložení plnoletosti, bankovního spojení a zůstatku kreditů) jsou zabezpečeny v chráněném adresáři aplikace na systému Android.
3.  **Bezpečná databáze (Room)**: Projekty, nahrávky a nákupy jsou spravovány prostřednictvím izolovaného databázového jádra SQLite s omezeným přístupem třetích stran.

---

## 4. VAŠE PRÁVA DLE GDPR
Jako uživatel z Evropského hospodářského prostoru (EHP) máte v souvislosti se svými osobními údaji následující práva:

*   **Právo na přístup**: Můžete si vyžádat přehled veškerých dat, která o Vás uchováváme.
*   **Právo na opravu**: Kdykoli můžete změnit své autorské jméno, bankovní účet nebo profilové informace v záložce „Můj Profil“.
*   **Právo na výmaz („Právo být zapomenut“)**: V záložce „Můj Profil“ se nachází tlačítko „Smazat veškerá data (Reset)“, které okamžitě a neodvolatelně odstraní veškeré nahrávky, projekty, transakční logy, bankovní údaje a profilová data z Vašeho zařízení.
*   **Právo odvolat souhlas**: Svůj souhlas s těmito zásadami můžete kdykoli odvolat odinstalováním aplikace nebo resetováním profilu.

---

## 5. SOUHLAS A ZMĚNY ZÁSAD
Používáním aplikace Spark Studio vyjadřujete explicitní souhlas s těmito Zásadami ochrany osobních údajů. Tyto zásady můžeme příležitostně aktualizovat. O všech zásadních změnách Vás budeme informovat prostřednictvím oznámení přímo v aplikaci.
