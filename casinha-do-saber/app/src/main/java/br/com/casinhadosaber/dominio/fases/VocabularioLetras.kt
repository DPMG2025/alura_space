package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.Palavra
import br.com.casinhadosaber.dominio.Vocabulario

/**
 * Vocabulário das fases 51 a 250 do caminho Letras (português) (mundos 6 a 25, plano em docs/plano-1000-fases.md).
 * Só o arquivo deste caminho usa este objeto: outro caminho que precisar da mesma lista copia ou
 * pede para mover para o vocabulário comum, para os caminhos poderem crescer em paralelo sem conflito.
 * O vocabulário das fases 1 a 50 continua em Vocabulario e VocabularioDosMundos.
 *
 * As palavras são escritas como "emoji SI-LA-BAS": as sílabas separadas por hífen seguem a divisão
 * escolar (CAR-RO, PÁS-SA-RO, LE-ÃO, PRAI-A) e juntas formam a palavra. Em cada lista o emoji é único,
 * para nunca aparecerem duas figuras iguais numa pergunta.
 */
object VocabularioLetras {

    /** "🐄 VA-CA" vira Palavra("🐄", "VACA", [VA, CA]). */
    fun p(s: String): Palavra {
        val (emoji, resto) = s.trim().split(" ", limit = 2)
        val silabas = resto.trim().split("-")
        return Palavra(emoji, silabas.joinToString(""), silabas)
    }

    private fun lista(vararg s: String) = s.map(::p)

    // ============================================================== sílabas simples (mundo 6)

    /**
     * Famílias silábicas simples (consoante e vogal). C e G só com A, O e U (o som muda com E e I); da
     * família do C ficam só CA e CO, para a voz nunca falar o pedacinho "cu" sozinho.
     */
    val familias: Map<String, List<String>> = buildMap {
        for (c in listOf("B", "D", "F", "J", "L", "M", "N", "P", "R", "S", "T", "V", "X", "Z")) put(c, Vocabulario.vogais.map { c + it })
        put("C", listOf("CA", "CO"))
        put("G", listOf("GA", "GO", "GU"))
    }

    val cv2 = lista(
        "🐄 VA-CA", "🦆 PA-TO", "🐓 GA-LO", "🐐 BO-DE", "🥚 O-VO", "🐱 GA-TO", "🐭 RA-TO", "🐸 SA-PO",
        "⚽ BO-LA", "👢 BO-TA", "🛏️ CA-MA", "🏠 CA-SA", "🥥 CO-CO", "🎲 DA-DO", "🔪 FA-CA", "🧚 FA-DA",
        "🦭 FO-CA", "🥫 LA-TA", "🧳 MA-LA", "🗺️ MA-PA", "🏍️ MO-TO", "🪁 PI-PA", "🔔 SI-NO", "🧃 SU-CO",
        "🕯️ VE-LA", "🧤 LU-VA", "🔍 LU-PA", "🎂 BO-LO", "🥛 CO-PO", "🍲 SO-PA", "🐺 LO-BO", "🪶 PE-NA",
        "🦃 PE-RU", "🧊 GE-LO", "🚙 JI-PE", "🫖 BU-LE", "🌙 LU-A", "🍇 U-VA", "💧 GO-TA", "👄 BO-CA",
    )

    val cv3 = lista(
        "🐴 CA-VA-LO", "🐒 MA-CA-CO", "🍿 PI-PO-CA", "👞 SA-PA-TO", "🍅 TO-MA-TE", "🍌 BA-NA-NA", "🥔 BA-TA-TA",
        "🐫 CA-ME-LO", "🖊️ CA-NE-TA", "🦉 CO-RU-JA", "🦒 GI-RA-FA", "🪟 JA-NE-LA", "🪙 MO-E-DA", "🥘 PA-NE-LA",
        "🥒 PE-PI-NO", "🪆 BO-NE-CA", "🧅 CE-BO-LA", "🥗 SA-LA-DA", "👕 CA-MI-SA", "🚢 NA-VI-O", "🎹 PI-A-NO",
        "🦍 GO-RI-LA", "👧 ME-NI-NA", "👦 ME-NI-NO", "🛍️ SA-CO-LA", "🛶 CA-NO-A",
    )

    val cv4 = lista("🥑 A-BA-CA-TE", "🍭 PI-RU-LI-TO", "☎️ TE-LE-FO-NE", "🍍 A-BA-CA-XI")

    /** Bichos e coisas da fazenda, para o tema do mundo 6. */
    val fazenda = lista(
        "🐄 VA-CA", "🦆 PA-TO", "🐓 GA-LO", "🐐 BO-DE", "🥚 O-VO", "🐱 GA-TO", "🐭 RA-TO", "🐸 SA-PO",
        "🦃 PE-RU", "🐴 CA-VA-LO", "🥔 BA-TA-TA", "🧅 CE-BO-LA", "🍅 TO-MA-TE", "🥒 PE-PI-NO", "🦉 CO-RU-JA",
    )

    // ============================================================== nasais e til (mundo 7)

    val nasaisAo = lista(
        "✋ MÃO", "🍞 PÃO", "🐕 CÃO", "🦁 LE-ÃO", "✈️ A-VI-ÃO", "🎈 BA-LÃO", "🍋 LI-MÃO", "🎸 VI-O-LÃO",
        "🍈 ME-LÃO", "🧼 SA-BÃO", "🦈 TU-BA-RÃO", "❤️ CO-RA-ÇÃO", "🦐 CA-MA-RÃO", "🚚 CA-MI-NHÃO",
        "👩 MA-MÃE", "🍎 MA-ÇÃ", "🧲 Í-MÃ",
    )

    val nasaisN = lista(
        "👼 AN-JO", "🐼 PAN-DA", "🐘 E-LE-FAN-TE", "🥪 LAN-CHE", "🥭 MAN-GA", "🍊 LA-RAN-JA", "🍓 MO-RAN-GO",
        "🦷 DEN-TE", "🌬️ VEN-TO", "⛺ TEN-DA", "🌶️ PI-MEN-TA", "🌱 SE-MEN-TE", "🎁 PRE-SEN-TE", "🐜 IN-SE-TO",
        "🌊 ON-DA", "🐆 ON-ÇA", "🌉 PON-TE", "🐚 CON-CHA", "🌍 MUN-DO", "🦘 CAN-GU-RU",
    )

    /** Palavras sem som nasal, para as perguntas "qual tem o til?". */
    val semNasal = cv2 + cv3

    // ============================================================== ditongos (mundo 8)

    val ditongos = lista(
        "📦 CAI-XA", "🩰 BAI-LA-RI-NA", "🐟 PEI-XE", "💋 BEI-JO", "🧀 QUEI-JO", "🧦 MEI-A", "🪑 CA-DEI-RA",
        "🐂 BOI", "🌃 NOI-TE", "🥇 OU-RO", "🥕 CE-NOU-RA", "✂️ TE-SOU-RA", "🌌 CÉU", "🎩 CHA-PÉU",
        "🏆 TRO-FÉU", "🏛️ MU-SEU", "🦜 PA-PA-GAI-O", "⚡ RAI-O", "🕸️ TEI-A", "🥛 LEI-TE", "🥬 COU-VE",
        "🚩 BAN-DEI-RA", "🏫 AU-LA",
    )

    // ============================================================== R, S e L no fim da sílaba (mundo 9)

    val travadasR = lista(
        "🌊 MAR", "⛵ BAR-CO", "✉️ CAR-TA", "🚪 POR-TA", "🍴 GAR-FO", "🪢 COR-DA", "🌳 ÁR-VO-RE",
        "🐢 TAR-TA-RU-GA", "🦋 BOR-BO-LE-TA", "🐷 POR-CO", "🐜 FOR-MI-GA", "🔨 MAR-TE-LO", "🍦 SOR-VE-TE",
        "🥧 TOR-TA", "🥩 CAR-NE", "🐻‍❄️ UR-SO", "🎪 CIR-CO",
    )

    val travadasS = lista(
        "⭐ ES-TRE-LA", "🏰 CAS-TE-LO", "🗡️ ES-PA-DA", "✏️ LÁ-PIS", "🚌 Ô-NI-BUS", "🧺 CES-TA", "🎭 MÁS-CA-RA",
        "🎉 FES-TA", "🥟 PAS-TEL", "🎿 ES-QUI", "🪥 ES-CO-VA", "🪜 ES-CA-DA", "🪰 MOS-CA", "👟 TÊ-NIS",
        "👓 Ó-CU-LOS", "🦢 CIS-NE",
    )

    val travadasL = lista(
        "☀️ SOL", "🍯 MEL", "💍 A-NEL", "📄 PA-PEL", "🎣 AN-ZOL", "🐌 CA-RA-COL", "🌴 PAL-MEI-RA",
        "🪣 BAL-DE", "👖 CAL-ÇA", "👜 BOL-SA", "🐬 GOL-FI-NHO", "🐙 POL-VO", "🥬 AL-FA-CE", "🧂 SAL",
        "⚽ FU-TE-BOL",
    )

    // ============================================================== RR, SS, QU, GU (mundo 10)

    val rr = lista(
        "🚗 CAR-RO", "🗼 TOR-RE", "🍾 GAR-RA-FA", "🐶 CA-CHOR-RO", "🍚 AR-ROZ", "🌎 TER-RA", "🪚 SER-RO-TE",
        "🎸 GUI-TAR-RA", "🏃 COR-RI-DA",
    )

    /** R sozinho entre vogais (som fraco), para comparar com RR. */
    val rFraco = lista(
        "💰 CA-RO", "🦐 CA-MA-RÃO", "🍐 PE-RA", "🦉 CO-RU-JA", "🐊 JA-CA-RÉ", "🦒 GI-RA-FA", "🍭 PI-RU-LI-TO",
        "🦍 GO-RI-LA", "🥕 CE-NOU-RA",
    )

    val ss = lista(
        "🐦 PÁS-SA-RO", "🦴 OS-SO", "🍑 PÊS-SE-GO", "🍝 MAS-SA", "🧹 VAS-SOU-RA", "🌻 GI-RAS-SOL",
        "🧭 BÚS-SO-LA", "🦕 DI-NOS-SAU-RO",
    )

    val qu = lista(
        "🧀 QUEI-JO", "🐿️ ES-QUI-LO", "🦟 MOS-QUI-TO", "🦜 PE-RI-QUI-TO", "🧸 BRIN-QUE-DO",
        "🌴 CO-QUEI-RO", "🧺 PI-QUE-NI-QUE",
    )

    val gu = lista("🚀 FO-GUE-TE", "🔥 FO-GUEI-RA", "🎸 GUI-TAR-RA", "🐆 GUE-PAR-DO", "🐧 PIN-GUIM", "🍔 HAM-BÚR-GUER")

    // ============================================================== C e Ç, G e J, S com som de Z (mundo 11)

    val cedilha = lista(
        "🍎 MA-ÇÃ", "🤡 PA-LHA-ÇO", "🎀 LA-ÇO", "❤️ CO-RA-ÇÃO", "🐆 ON-ÇA", "👖 CAL-ÇA", "💪 BRA-ÇO",
        "💨 FU-MA-ÇA", "🧪 PO-ÇÃO", "🧒 CRI-AN-ÇA",
    )

    /** C com som de S antes de E e I (sem cedilha). */
    val cSuave = lista(
        "🧅 CE-BO-LA", "🥕 CE-NOU-RA", "🎪 CIR-CO", "🦢 CIS-NE", "🍒 CE-RE-JA", "🎬 CI-NE-MA", "📱 CE-LU-LAR",
        "🚲 BI-CI-CLE-TA", "🤴 PRÍN-CI-PE", "🍬 DO-CE", "🥬 AL-FA-CE",
    )

    /** G com som de J antes de E e I. */
    val gSuave = lista("🧊 GE-LO", "🦒 GI-RA-FA", "🌻 GI-RAS-SOL", "🥣 TI-GE-LA", "🧙 MÁ-GI-CO", "🤸 GI-NÁS-TI-CA")

    val jota = lista(
        "🐊 JA-CA-RÉ", "🪟 JA-NE-LA", "🐞 JO-A-NI-NHA", "🚙 JI-PE", "🍊 LA-RAN-JA", "🦉 CO-RU-JA",
        "💋 BEI-JO", "🧀 QUEI-JO", "🐍 JI-BOI-A", "👼 AN-JO",
    )

    /** S entre vogais, com som de Z. */
    val sComSomDeZ = lista(
        "🏠 CA-SA", "👕 CA-MI-SA", "🌹 RO-SA", "🏺 VA-SO", "🎵 MÚ-SI-CA", "👚 BLU-SA", "🎁 PRE-SEN-TE",
        "✂️ TE-SOU-RA", "🏛️ MU-SEU", "👸 PRIN-CE-SA",
    )

    val zeta = lista("🦓 ZE-BRA", "🔟 DEZ", "👃 NA-RIZ", "♟️ XA-DREZ", "🍚 AR-ROZ", "🔵 A-ZUL", "💡 LUZ")

    // ============================================================== encontros consonantais (mundo 12)

    val encontrosL = lista(
        "👚 BLU-SA", "🧱 BLO-CO", "🚲 BI-CI-CLE-TA", "📎 CLI-PE", "🌸 FLOR", "🏹 FLE-CHA", "❄️ FLO-CO",
        "🌐 GLO-BO", "🪐 PLA-NE-TA", "🪴 PLAN-TA", "🪧 PLA-CA", "📚 BI-BLI-O-TE-CA",
    )

    val encontrosR = lista(
        "🧒 CRI-AN-ÇA", "🐊 CRO-CO-DI-LO", "🐉 DRA-GÃO", "🖼️ QUA-DRO", "🪨 PE-DRA", "🍗 FRAN-GO", "🥶 FRI-O",
        "🦗 GRI-LO", "🐯 TI-GRE", "👔 GRA-VA-TA", "📖 LI-VRO", "💪 BRA-ÇO", "🧙‍♀️ BRU-XA", "🐍 CO-BRA",
        "🍽️ PRA-TO", "🏖️ PRAI-A", "🚆 TREM", "🚜 TRA-TOR", "🏆 TRO-FÉU", "🦓 ZE-BRA", "🥦 BRÓ-CO-LIS",
    )

    // ============================================================== X e CH, H, Z e S (mundo 13)

    val xis = lista(
        "☕ XÍ-CA-RA", "🍍 A-BA-CA-XI", "📦 CAI-XA", "🐟 PEI-XE", "🧙‍♀️ BRU-XA", "♟️ XA-DREZ", "🗑️ LI-XO",
        "🤠 XE-RI-FE", "🟣 RO-XO", "🧴 XAM-PU",
    )

    val che = lista(
        "🔑 CHA-VE", "🌧️ CHU-VA", "🍵 CHÁ", "🎩 CHA-PÉU", "🍫 CHO-CO-LA-TE", "🎒 MO-CHI-LA", "🐶 CA-CHOR-RO",
        "🏹 FLE-CHA", "🥪 LAN-CHE", "🪓 MA-CHA-DO", "🩴 CHI-NE-LO", "🐚 CON-CHA",
    )

    val aga = lista(
        "👨 HO-MEM", "🦛 HI-PO-PÓ-TA-MO", "🚁 HE-LI-CÓP-TE-RO", "🏥 HOS-PI-TAL", "🍔 HAM-BÚR-GUER",
        "🏨 HO-TEL", "🦸 HE-RÓI", "⏰ HO-RA",
    )

    /** Palavras que começam com vogal (sem H), para comparar com as do H. */
    val semAga = lista("🥚 O-VO", "🍇 U-VA", "🐻 UR-SO", "🐘 E-LE-FAN-TE", "👓 Ó-CU-LOS", "🌳 ÁR-VO-RE", "👼 AN-JO", "🧲 Í-MÃ")

    // ============================================================== M antes de P e B, M e N no fim (mundo 14)

    val mpmb = lista(
        "🍫 BOM-BOM", "🥁 TAM-BOR", "🕊️ POM-BA", "💡 LÂM-PA-DA", "🧑‍🚒 BOM-BEI-RO", "🛎️ CAM-PA-I-NHA",
        "🧴 XAM-PU", "⛈️ TEM-PES-TA-DE", "🎋 BAM-BU", "💻 COM-PU-TA-DOR", "🏅 CAM-PE-ÃO",
    )

    /** N antes de outras consoantes (CANTOR, MONTANHA). */
    val nAntes = lista(
        "🎤 CAN-TOR", "⛰️ MON-TA-NHA", "🦘 CAN-GU-RU", "🔦 LAN-TER-NA", "🪴 PLAN-TA", "🐼 PAN-DA",
        "🦷 DEN-TE", "🌊 ON-DA", "🍊 LA-RAN-JA", "🐘 E-LE-FAN-TE", "🌶️ PI-MEN-TA",
    )

    /** M no fim da palavra. */
    val mFim = lista(
        "☁️ NU-VEM", "🚆 TREM", "🌷 JAR-DIM", "🍮 PU-DIM", "💄 BA-TOM", "🔊 SOM", "🐟 A-TUM", "👨 HO-MEM",
        "🐧 PIN-GUIM", "🌾 CA-PIM",
    )

    // ============================================================== acentos, sílaba forte (mundo 21)

    val acentos = lista(
        "🛋️ SO-FÁ", "☕ CA-FÉ", "🦶 PÉ", "🐊 JA-CA-RÉ", "🍦 PI-CO-LÉ", "🧢 BO-NÉ", "👵 VO-VÓ", "👴 VO-VÔ",
        "👶 BE-BÊ", "🤖 RO-BÔ", "🚌 Ô-NI-BUS", "💡 LÂM-PA-DA", "🍑 PÊS-SE-GO", "🐦 PÁS-SA-RO", "🌳 ÁR-VO-RE",
        "🎵 MÚ-SI-CA", "👓 Ó-CU-LOS", "👟 TÊ-NIS", "🎭 MÁS-CA-RA", "📷 CÂ-ME-RA", "🎃 A-BÓ-BO-RA", "✏️ LÁ-PIS",
        "🍵 CHÁ", "🥦 BRÓ-CO-LIS", "🧙 MÁ-GI-CO",
    )

    /** Sem acento nenhum, para "pesque as palavras com acento". */
    val semAcento = cv2 + cv3 + travadasR.filter { it.texto.none { c -> c in "ÁÉÍÓÚÂÊÔÃÕ" } }

    /** Palavras de 3 sílabas com o pedacinho forte no fim (oxítonas). */
    val forteNoFim = lista(
        "🐊 JA-CA-RÉ", "🍦 PI-CO-LÉ", "🦐 CA-MA-RÃO", "🦈 TU-BA-RÃO", "❤️ CO-RA-ÇÃO", "🎸 VI-O-LÃO",
        "📱 CE-LU-LAR", "🐌 CA-RA-COL", "⚽ FU-TE-BOL", "🌻 GI-RAS-SOL",
    )

    /** Palavras de 3 sílabas com o pedacinho forte no meio (paroxítonas). */
    val forteNoMeio = lista(
        "🐴 CA-VA-LO", "🐒 MA-CA-CO", "🍿 PI-PO-CA", "👞 SA-PA-TO", "🍅 TO-MA-TE", "🥔 BA-TA-TA",
        "🖊️ CA-NE-TA", "🪟 JA-NE-LA", "🪆 BO-NE-CA", "🧅 CE-BO-LA", "⭐ ES-TRE-LA", "🏰 CAS-TE-LO",
        "🔨 MAR-TE-LO", "🪑 CA-DEI-RA",
    )

    /** Palavras de 3 sílabas com o pedacinho forte no começo (proparoxítonas). */
    val forteNoComeco = lista(
        "🐦 PÁS-SA-RO", "🌳 ÁR-VO-RE", "💡 LÂM-PA-DA", "☕ XÍ-CA-RA", "🎵 MÚ-SI-CA", "🧙 MÁ-GI-CO",
        "🚌 Ô-NI-BUS", "🍑 PÊS-SE-GO", "🎭 MÁS-CA-RA", "👓 Ó-CU-LOS", "📷 CÂ-ME-RA", "🧭 BÚS-SO-LA",
        "🥦 BRÓ-CO-LIS",
    )

    // ============================================================== ditado com dificuldades (mundo 22)

    /** L no fim (que soa como U) e U no fim de verdade: PAPEL e CHAPÉU. */
    val lOuU = lista(
        "📄 PA-PEL", "🎣 AN-ZOL", "🐌 CA-RA-COL", "🥟 PAS-TEL", "💍 A-NEL", "⚽ FU-TE-BOL", "🌻 GI-RAS-SOL",
        "🏥 HOS-PI-TAL", "🏨 HO-TEL", "🎩 CHA-PÉU", "🏆 TRO-FÉU", "🌌 CÉU", "🏛️ MU-SEU",
    )

    // ============================================================== fonologia

    /** Trios de palavras que só mudam a primeira letra (GATO, PATO, RATO): consciência fonêmica. */
    val trocas: List<List<Palavra>> = listOf(
        lista("🐱 GA-TO", "🦆 PA-TO", "🐭 RA-TO"),
        lista("🧳 MA-LA", "🍬 BA-LA"),
        lista("🐄 VA-CA", "🔪 FA-CA"),
        lista("🦭 FO-CA", "👄 BO-CA"),
        lista("👢 BO-TA", "💧 GO-TA"),
        lista("🎂 BO-LO", "⚽ BO-LA"),
    )

    /** Grupos de palavras que rimam (o som do fim é igual). */
    val rimas: List<List<Palavra>> = listOf(
        lista("✋ MÃO", "🍞 PÃO", "🐕 CÃO", "🦁 LE-ÃO", "🎈 BA-LÃO", "🍋 LI-MÃO"),
        lista("🐱 GA-TO", "🦆 PA-TO", "🐭 RA-TO", "👞 SA-PA-TO"),
        lista("🧳 MA-LA", "🍬 BA-LA"),
        lista("🐄 VA-CA", "🔪 FA-CA", "🦭 FO-CA", "👄 BO-CA"),
        lista("🍫 BOM-BOM", "🔊 SOM", "💄 BA-TOM"),
        lista("🌷 JAR-DIM", "🍮 PU-DIM", "🌾 CA-PIM", "🐧 PIN-GUIM"),
        lista("🌌 CÉU", "🎩 CHA-PÉU", "🏆 TRO-FÉU"),
        lista("🦷 DEN-TE", "🌱 SE-MEN-TE", "🎁 PRE-SEN-TE"),
        lista("🐴 CA-VA-LO", "🐓 GA-LO"),
    )

    // ============================================================== leitura do alfabeto e dicionário (mundo 19)

    /** Palavras com a mesma primeira letra e a segunda diferente, para a ordem pela segunda letra. */
    val dicionario: List<List<Palavra>> = listOf(
        lista("🍬 BA-LA", "👶 BE-BÊ", "🚲 BI-CI-CLE-TA", "⚽ BO-LA", "🫖 BU-LE"),
        lista("🛏️ CA-MA", "🧅 CE-BO-LA", "🎬 CI-NE-MA", "🥥 CO-CO"),
        lista("🦆 PA-TO", "🐟 PEI-XE", "🪁 PI-PA", "🐷 POR-CO", "🍮 PU-DIM"),
        lista("🧳 MA-LA", "🍯 MEL", "🪱 MI-NHO-CA", "🏍️ MO-TO", "🌍 MUN-DO"),
        lista("🐸 SA-PO", "🌱 SE-MEN-TE", "🔔 SI-NO", "☀️ SOL", "🧃 SU-CO"),
        lista("🐱 GA-TO", "🧊 GE-LO", "🦒 GI-RA-FA", "🐬 GOL-FI-NHO", "🎸 GUI-TAR-RA"),
        lista("🥫 LA-TA", "🦁 LE-ÃO", "🗑️ LI-XO", "🐺 LO-BO", "🌙 LU-A"),
        lista("🔪 FA-CA", "🎉 FES-TA", "🎀 FI-TA", "🦭 FO-CA", "💨 FU-MA-ÇA"),
        lista("🥁 TAM-BOR", "☎️ TE-LE-FO-NE", "🐯 TI-GRE", "🍅 TO-MA-TE", "🦈 TU-BA-RÃO"),
        lista("🐄 VA-CA", "🕯️ VE-LA", "🎸 VI-O-LÃO", "👴 VO-VÔ", "🌋 VUL-CÃO"),
    )

    /** Palavras que começam igual (mesmas duas letras), para a ordem pela terceira letra. */
    val dicionarioTerceira: List<List<Palavra>> = listOf(
        lista("🐐 CA-BRA", "☕ CA-FÉ", "🛏️ CA-MA", "🏠 CA-SA", "🐴 CA-VA-LO"),
        lista("🍬 BA-LA", "🍌 BA-NA-NA", "⛵ BAR-CO", "🥔 BA-TA-TA"),
        lista("🐒 MA-CA-CO", "🧳 MA-LA", "🗺️ MA-PA", "🔨 MAR-TE-LO"),
        lista("🤡 PA-LHA-ÇO", "🐼 PAN-DA", "📄 PA-PEL", "🦆 PA-TO"),
        lista("🐍 CO-BRA", "🥥 CO-CO", "🥄 CO-LHER", "❤️ CO-RA-ÇÃO"),
    )

    // ============================================================== frases

    /** Uma frase escrita com a figura que combina com ela (a pontuação fica grudada na última palavra). */
    data class FraseL(val texto: String, val emoji: String) {
        val palavras: List<String> get() = texto.split(" ")
        val sinal: Char get() = texto.last()
        val semSinal: String get() = texto.dropLast(1)
    }

    private fun frases(vararg s: String) = s.map { val (e, t) = it.split(" ", limit = 2); FraseL(t, e) }

    /** Nomes de pessoas usados nas frases (sempre com letra maiúscula). */
    val nomes = listOf("Ana", "Bia", "Léo", "Lia", "Rui", "Davi", "Nina", "Caio", "Lucas", "Maria")

    /** Frases da ilha (mundo 15): 3 a 5 palavras, ponto final. */
    val frasesIlha = frases(
        "🏴‍☠️ O pirata tem um mapa.", "🐚 Ana achou uma concha.", "🐢 A tartaruga nada devagar.",
        "🦀 O caranguejo anda de lado.", "⛵ O barco tem vela.", "🐬 Bia viu um golfinho.",
        "🦜 O papagaio é verde.", "🥥 Nós comemos coco.", "💰 Davi achou o tesouro.", "🐳 A baleia pulou alto.",
        "🏖️ Léo brincou na areia.", "☀️ O sol brilha forte.", "🐙 O polvo tem oito braços.", "🌊 A onda molhou Lia.",
        "🐟 O peixe nada rápido.", "🏝️ Nina mora na ilha.",
    )

    /** Frases curtas da fábrica de doces (mundo 16): ponto, interrogação e exclamação. */
    val frasesDoces = frases(
        "🍭 Quem quer pirulito?", "🎂 Que bolo gostoso!", "🍓 O doce é de morango.", "🍫 Quem comeu o chocolate?",
        "🍦 Onde está o sorvete?", "🍬 A bala é azul.", "🎁 Oba, ganhei um presente!", "🍮 Onde está o pudim?",
        "🍪 Vovó fez biscoitos.", "🍵 Cuidado, o chá está quente!", "📦 Quantos doces há na caixa?",
        "🍿 Que pipoca gostosa!", "🧁 O bolinho é de chocolate.", "🍩 Qual rosquinha você quer?",
        "🍯 Que mel docinho!", "🥛 Maria bebeu o leite.",
    )

    /** Frases para ler e montar nos mundos mais altos (5 a 7 palavras). */
    val frasesLongas = frases(
        "🐶 O cachorro correu atrás da bola.", "🌧️ Hoje choveu muito na cidade.", "🎈 Nina ganhou um balão vermelho.",
        "🚲 Rui anda de bicicleta no parque.", "🐱 A gata dormiu em cima do sofá.", "📚 Lia leu um livro de dragões.",
        "🍎 Caio comeu uma maçã bem doce.", "🐦 O passarinho fez um ninho novo.", "⭐ As estrelas brilham no céu.",
        "🏰 O rei mora num castelo enorme.", "🐢 A tartaruga chegou bem devagar.", "🌻 O girassol cresceu no jardim.",
    )

    /** Frases com o nome de uma pessoa, para treinar a letra maiúscula. */
    val frasesComNome = (frasesIlha + frasesDoces + frasesLongas).filter { f -> f.palavras.any { w -> w.trimEnd('.', '!', '?', ',') in nomes } }

    // ============================================================== textos curtos

    data class PerguntaDeTexto(val pergunta: String, val certa: String, val erradas: List<String>)

    /** Texto curto (bilhete, lista, história, convite, receita) com perguntas de compreensão. */
    data class TextoCurto(val texto: String, val emoji: String, val perguntas: List<PerguntaDeTexto>)

    private fun q(pergunta: String, certa: String, vararg erradas: String) = PerguntaDeTexto(pergunta, certa, erradas.toList())

    /** Bilhetes e listas (mundo 16). */
    val bilhetes = listOf(
        TextoCurto("Bia, o bolo está na mesa. Beijos, mamãe.", "🎂", listOf(
            q("Quem escreveu o bilhete?", "mamãe", "Bia", "vovó", "papai"),
            q("Para quem é o bilhete?", "Bia", "mamãe", "Léo", "vovô"),
            q("Onde está o bolo?", "na mesa", "na caixa", "no forno", "na cama"),
        )),
        TextoCurto("Léo, fui à padaria e volto logo. Papai.", "🥖", listOf(
            q("Quem escreveu o bilhete?", "papai", "Léo", "mamãe", "vovó"),
            q("Aonde papai foi?", "à padaria", "à escola", "ao parque", "à praia"),
            q("Para quem é o bilhete?", "Léo", "papai", "Bia", "Nina"),
        )),
        TextoCurto("Nina, o doce de leite está na geladeira. Vovó.", "🍮", listOf(
            q("Quem escreveu o bilhete?", "vovó", "Nina", "papai", "Rui"),
            q("Que doce a vovó fez?", "doce de leite", "bolo", "pudim", "sorvete"),
            q("Onde está o doce?", "na geladeira", "na mesa", "no armário", "na caixa"),
        )),
        TextoCurto("Lista da festa: suco, bolo, balas e pipoca.", "📝", listOf(
            q("A lista é de quê?", "da festa", "da escola", "da praia", "do mercado"),
            q("O que tem na lista?", "pipoca", "pizza", "sopa", "peixe"),
            q("Que bebida tem na lista?", "suco", "leite", "chá", "café"),
        )),
        TextoCurto("Lista do mercado: leite, ovos, pão e queijo.", "🛒", listOf(
            q("A lista é de onde?", "do mercado", "da festa", "da escola", "do parque"),
            q("O que tem na lista?", "queijo", "banana", "bolo", "suco"),
            q("O que não está na lista?", "uva", "leite", "ovos", "pão"),
        )),
    )

    /** Histórias curtas da caverna (mundo 20): quem, onde, o que aconteceu. */
    val historias = listOf(
        TextoCurto("Lia entrou na caverna. Ela viu um cristal azul. Um morcego voou.", "🦇", listOf(
            q("Quem entrou na caverna?", "Lia", "Léo", "Bia", "Rui"),
            q("Onde Lia entrou?", "na caverna", "na escola", "no rio", "na praia"),
            q("O que Lia viu?", "um cristal", "um gato", "uma bola", "uma flor"),
            q("O que voou?", "um morcego", "um avião", "um balão", "uma pipa"),
        )),
        TextoCurto("Rui achou uma pedra brilhante. Ele guardou a pedra na mochila e foi para casa.", "💎", listOf(
            q("Quem achou a pedra?", "Rui", "Lia", "Nina", "Davi"),
            q("Onde Rui guardou a pedra?", "na mochila", "no bolso", "na caixa", "no chão"),
            q("Para onde Rui foi?", "para casa", "para a escola", "para o parque", "para a praia"),
        )),
        TextoCurto("O tatu cavou um buraco. Dentro do buraco tinha água. O tatu tomou banho.", "🕳️", listOf(
            q("Quem cavou o buraco?", "o tatu", "o gato", "o macaco", "o pato"),
            q("O que tinha no buraco?", "água", "areia", "ouro", "folhas"),
            q("O que o tatu fez?", "tomou banho", "dormiu", "comeu", "cantou"),
        )),
        TextoCurto("Nina e Davi brincavam na praça. Começou a chover. Eles correram para casa.", "🌧️", listOf(
            q("Quem brincava na praça?", "Nina e Davi", "Ana e Léo", "Bia e Rui", "Lia e Caio"),
            q("Onde eles brincavam?", "na praça", "na caverna", "na escola", "no rio"),
            q("O que aconteceu?", "choveu", "fez sol", "nevou", "ventou"),
            q("Para onde eles correram?", "para casa", "para a escola", "para o rio", "para a praia"),
        )),
        TextoCurto("A coruja mora na árvore. De noite, ela sai para caçar. De dia, ela dorme.", "🦉", listOf(
            q("Quem mora na árvore?", "a coruja", "o tatu", "o peixe", "a vaca"),
            q("Onde a coruja mora?", "na árvore", "na caverna", "no rio", "na casa"),
            q("Quando a coruja dorme?", "de dia", "de noite", "de tarde", "nunca"),
        )),
        TextoCurto("Caio perdeu a bola no jardim. O cachorro achou a bola. Caio ficou feliz.", "⚽", listOf(
            q("Quem perdeu a bola?", "Caio", "Rui", "Ana", "Lia"),
            q("Onde Caio perdeu a bola?", "no jardim", "na praia", "na escola", "no rio"),
            q("Quem achou a bola?", "o cachorro", "o gato", "a vovó", "o papai"),
            q("Como Caio ficou?", "feliz", "triste", "bravo", "com sono"),
        )),
    )

    /** Convites, receitas e poemas (mundo 23). */
    val textosDoDia = listOf(
        TextoCurto("Convite: festa da Nina, no sábado, às três horas, na casa da vovó.", "💌", listOf(
            q("De quem é a festa?", "da Nina", "da Ana", "da Bia", "do Léo"),
            q("Em que dia é a festa?", "sábado", "domingo", "segunda", "hoje"),
            q("Onde vai ser a festa?", "na casa da vovó", "na escola", "na praça", "no parque"),
            q("Que horas começa a festa?", "às três", "às duas", "às cinco", "às dez"),
        )),
        TextoCurto("Suco de laranja: corte as laranjas, esprema, ponha água e mexa.", "🍊", listOf(
            q("Que suco é a receita?", "de laranja", "de uva", "de limão", "de maçã"),
            q("O que se faz primeiro?", "cortar", "mexer", "beber", "espremer"),
            q("O que se faz por último?", "mexer", "cortar", "lavar", "comer"),
        )),
        TextoCurto("Salada de frutas: banana, maçã e uva. Corte tudo e misture.", "🥗", listOf(
            q("Que fruta vai na salada?", "banana", "limão", "coco", "abacaxi"),
            q("O que se faz com as frutas?", "corta e mistura", "assa no forno", "frita", "congela"),
        )),
        TextoCurto("O sapo pula no lago, o pato nada no rio, a vaca come capim e o gato sente frio.", "🐸", listOf(
            q("Quem pula no lago?", "o sapo", "o pato", "a vaca", "o gato"),
            q("O que a vaca come?", "capim", "peixe", "bolo", "milho"),
            q("Que palavra rima com rio?", "frio", "lago", "sapo", "capim"),
        )),
    )

    /** Poeminhas para completar com a palavra que rima. */
    val versosParaCompletar = listOf(
        q("A abelha voa no jardim, ela gosta de ___.", "jasmim", "rosa", "mel", "flor"),
        q("O macaco come banana, sentado na ___.", "cabana", "árvore", "casa", "pedra"),
        q("O gato dorme no tapete, sonhando com ___.", "sorvete", "peixe", "leite", "bola"),
        q("Na janela da cozinha, canta uma ___.", "andorinha", "pomba", "coruja", "arara"),
        q("O leão é o rei da selva e dorme na ___.", "relva", "cama", "pedra", "árvore"),
        q("Meu cachorro chama Totó, ele mora com a ___.", "vovó", "mamãe", "tia", "Ana"),
        q("O pato foi passear e caiu no ___.", "mar", "lago", "rio", "chão"),
        q("A lua brilha lá no céu, e o palhaço perdeu o ___.", "chapéu", "sapato", "nariz", "balão"),
    )

    /** Parlendas para seguir a sequência: as palavras na ordem em que aparecem. */
    val parlendas = listOf(
        "Um, dois, feijão com arroz" to listOf("UM", "DOIS", "FEIJÃO", "ARROZ"),
        "Três, quatro, feijão no prato" to listOf("TRÊS", "QUATRO", "FEIJÃO", "PRATO"),
        "Janela, janelinha, porta, campainha" to listOf("JANELA", "JANELINHA", "PORTA", "CAMPAINHA"),
        "Corre, cutia, na casa da tia" to listOf("CORRE", "CUTIA", "CASA", "TIA"),
        "Sapo cururu na beira do rio" to listOf("SAPO", "CURURU", "BEIRA", "RIO"),
        "Batatinha quando nasce espalha a rama pelo chão" to listOf("BATATINHA", "NASCE", "RAMA", "CHÃO"),
        "A galinha do vizinho bota ovo amarelinho" to listOf("GALINHA", "VIZINHO", "OVO", "AMARELINHO"),
    )

    /** Versos de parlenda para montar (palavras sem repetir). */
    val versos = frases(
        "🐸 O sapo não lava o pé.", "🪟 Janela, janelinha, porta, campainha.", "🍚 Um, dois, feijão com arroz.",
        "🍽️ Três, quatro, feijão no prato.", "🐔 A galinha do vizinho bota ovo amarelinho.",
        "🥔 Batatinha quando nasce espalha a rama.", "🐹 Corre, cutia, na casa da tia.",
    )

    // ============================================================== histórias em 3 quadros (mundo 24)

    /** Uma história de três quadros, na ordem: começo, meio e fim. */
    data class Historia(val quadros: List<Pair<String, String>>)

    val historiasEmQuadros = listOf(
        Historia(listOf("🥚" to "O ovo rachou.", "🐣" to "O pintinho nasceu.", "🐥" to "O pintinho cresceu.")),
        Historia(listOf("🌱" to "A semente brotou.", "🌷" to "A flor abriu.", "💐" to "Ana colheu as flores.")),
        Historia(listOf("🥣" to "Léo misturou a massa.", "🔥" to "O bolo assou no forno.", "🎂" to "Todos comeram o bolo.")),
        Historia(listOf("🌧️" to "Choveu muito.", "☀️" to "O sol voltou.", "🌈" to "O céu ficou colorido.")),
        Historia(listOf("😴" to "Bia acordou.", "🪥" to "Ela escovou os dentes.", "🎒" to "Ela foi para a escola.")),
        Historia(listOf("🌳" to "Rui subiu na árvore.", "🤕" to "Ele caiu e se machucou.", "🩹" to "A mãe fez um curativo.")),
        Historia(listOf("🎈" to "Nina ganhou um balão.", "🌬️" to "O vento soprou forte.", "😢" to "O balão voou para longe.")),
        Historia(listOf("🐛" to "A lagarta comeu muitas folhas.", "😴" to "Ela dormiu bastante.", "🦋" to "Ela virou borboleta.")),
    )

    /** Frases com um espaço para ligar as ideias com e, mas ou porque. */
    val conectivos = listOf(
        q("Léo levou o guarda-chuva ___ ia chover.", "porque", "e", "mas", "se"),
        q("Bia está com frio ___ esqueceu o casaco.", "porque", "e", "mas", "se"),
        q("Tomei água ___ estava com sede.", "porque", "e", "mas", "se"),
        q("Ana queria brincar, ___ estava muito cansada.", "mas", "e", "porque", "se"),
        q("O bolo é pequeno, ___ é muito gostoso.", "mas", "e", "porque", "se"),
        q("Corri bastante, ___ perdi o ônibus.", "mas", "e", "porque", "se"),
        q("Comprei pão ___ leite.", "e", "mas", "porque", "se"),
        q("O gato bebeu leite ___ dormiu.", "e", "mas", "porque", "se"),
        q("Davi tem um cachorro ___ um gato.", "e", "mas", "porque", "se"),
    )

    /** Frases com e, mas e porque para montar. */
    val frasesComConectivo = frases(
        "🐱 O gato comeu e dormiu.", "☂️ Levei guarda-chuva porque choveu.", "🍰 O bolo é pequeno, mas gostoso.",
        "🍞 Comprei pão e leite.", "🥤 Bebi água porque tive sede.", "😴 Quis brincar, mas dormi.",
        "🧥 Vesti casaco porque fazia frio.", "🐶 Rex late e pula.",
    )

    // ============================================================== flexões e sentidos (mundos 17 e 18)

    /** Um par de palavras ligadas (singular e plural, masculino e feminino, antônimos, sinônimos). */
    data class Par(val a: String, val b: String)

    private fun pares(vararg s: String) = s.map { val (a, b) = it.split("/"); Par(a, b) }

    val pluraisFaceis = pares(
        "GATO/GATOS", "BOLA/BOLAS", "CASA/CASAS", "PATO/PATOS", "BALA/BALAS", "FADA/FADAS", "ESTRELA/ESTRELAS",
        "SAPO/SAPOS", "ÁRVORE/ÁRVORES", "COGUMELO/COGUMELOS",
    )

    val pluraisDificeis = pares(
        "FLOR/FLORES", "MAR/MARES", "PÃO/PÃES", "CÃO/CÃES", "LEÃO/LEÕES", "BALÃO/BALÕES", "AVIÃO/AVIÕES",
        "LIMÃO/LIMÕES", "CORAÇÃO/CORAÇÕES", "MÃO/MÃOS", "ANEL/ANÉIS", "PAPEL/PAPÉIS", "CARACOL/CARACÓIS",
        "TREM/TRENS", "PUDIM/PUDINS", "NUVEM/NUVENS", "NARIZ/NARIZES", "TAMBOR/TAMBORES",
    )

    val masculinoFeminino = pares(
        "MENINO/MENINA", "GATO/GATA", "LEÃO/LEOA", "GALO/GALINHA", "BOI/VACA", "PAI/MÃE", "REI/RAINHA",
        "PRÍNCIPE/PRINCESA", "HOMEM/MULHER", "PATO/PATA", "TIO/TIA", "AVÔ/AVÓ", "IRMÃO/IRMÃ", "ALUNO/ALUNA",
        "PROFESSOR/PROFESSORA", "CAVALO/ÉGUA", "CANTOR/CANTORA", "PADEIRO/PADEIRA",
    )

    val antonimos = pares(
        "GRANDE/PEQUENO", "ALTO/BAIXO", "QUENTE/FRIO", "CHEIO/VAZIO", "ABERTO/FECHADO", "CLARO/ESCURO",
        "RÁPIDO/LENTO", "FELIZ/TRISTE", "DIA/NOITE", "LIMPO/SUJO", "LEVE/PESADO", "SUBIR/DESCER", "ENTRAR/SAIR",
        "DENTRO/FORA", "PERTO/LONGE", "NOVO/VELHO", "MOLHADO/SECO", "DOCE/SALGADO", "CEDO/TARDE", "MUITO/POUCO",
        "LIGADO/DESLIGADO", "GANHAR/PERDER", "CHORAR/RIR", "COMEÇO/FIM",
    )

    val sinonimos = pares(
        "BONITO/BELO", "CONTENTE/ALEGRE", "GAROTO/MENINO", "VELOZ/LIGEIRO", "COMEÇAR/INICIAR", "ACABAR/TERMINAR",
        "FALAR/DIZER", "ENORME/GIGANTE", "BRAVO/ZANGADO", "GOSTOSO/DELICIOSO", "ESPERTO/INTELIGENTE",
        "PEDRA/ROCHA", "BARULHO/RUÍDO", "LAR/CASA", "OLHAR/VER",
    )

    /** Palavra, diminutivo e aumentativo. */
    data class Tamanhos(val normal: String, val pequeno: String, val grande: String)

    val tamanhos = listOf(
        Tamanhos("CASA", "CASINHA", "CASARÃO"), Tamanhos("GATO", "GATINHO", "GATÃO"),
        Tamanhos("CARRO", "CARRINHO", "CARRÃO"), Tamanhos("PÉ", "PEZINHO", "PEZÃO"),
        Tamanhos("MÃO", "MÃOZINHA", "MÃOZONA"), Tamanhos("NARIZ", "NARIZINHO", "NARIGÃO"),
        Tamanhos("PATO", "PATINHO", "PATÃO"), Tamanhos("LIVRO", "LIVRINHO", "LIVRÃO"),
        Tamanhos("COPO", "COPINHO", "COPÃO"), Tamanhos("FOGO", "FOGUINHO", "FOGARÉU"),
        Tamanhos("DENTE", "DENTINHO", "DENTÃO"), Tamanhos("PEIXE", "PEIXINHO", "PEIXÃO"),
        Tamanhos("PANELA", "PANELINHA", "PANELÃO"), Tamanhos("SAPO", "SAPINHO", "SAPÃO"),
        Tamanhos("CHAPÉU", "CHAPEUZINHO", "CHAPELÃO"), Tamanhos("BOCA", "BOQUINHA", "BOCARRA"),
    )

    /** Frase com sujeito no singular e no plural, e o verbo nas formas ele, eles, nós e eu. */
    data class Concordancia(val sujeito: String, val sujeitoPlural: String, val verbo: List<String>, val resto: String)

    val concordancias = listOf(
        Concordancia("A fada", "As fadas", listOf("voa", "voam", "voamos", "voo"), "no bosque."),
        Concordancia("O sapo", "Os sapos", listOf("pula", "pulam", "pulamos", "pulo"), "na lagoa."),
        Concordancia("O gato", "Os gatos", listOf("dorme", "dormem", "dormimos", "durmo"), "no sofá."),
        Concordancia("A flor", "As flores", listOf("cresce", "crescem", "crescemos", "cresço"), "no jardim."),
        Concordancia("O passarinho", "Os passarinhos", listOf("canta", "cantam", "cantamos", "canto"), "na árvore."),
        Concordancia("A coruja", "As corujas", listOf("olha", "olham", "olhamos", "olho"), "a lua."),
        Concordancia("O coelho", "Os coelhos", listOf("come", "comem", "comemos", "como"), "cenoura."),
        Concordancia("O cogumelo", "Os cogumelos", listOf("nasce", "nascem", "nascemos", "nasço"), "na mata."),
    )

    /** Frases no masculino e no feminino: (masculino, feminino, verbo, adjetivo masculino, adjetivo feminino). */
    val frasesGenero = listOf(
        listOf("O menino", "A menina", "é", "alto.", "alta."),
        listOf("O gato", "A gata", "é", "branco.", "branca."),
        listOf("O leão", "A leoa", "está", "cansado.", "cansada."),
        listOf("O pato", "A pata", "é", "amarelo.", "amarela."),
        listOf("O rei", "A rainha", "é", "bondoso.", "bondosa."),
        listOf("O vovô", "A vovó", "é", "engraçado.", "engraçada."),
        listOf("O cavalo", "A égua", "está", "molhado.", "molhada."),
    )

    // ============================================================== figuras das cenas e listas

    /** Coisas da ilha com o artigo, para "ache o coco e a concha". */
    val itensDaIlha = listOf(
        "🥥" to "o coco", "🐚" to "a concha", "🦀" to "o caranguejo", "💰" to "o tesouro", "🗺️" to "o mapa",
        "🦜" to "o papagaio", "🐢" to "a tartaruga", "⚓" to "a âncora", "⛵" to "o barco", "🔭" to "a luneta",
        "🌴" to "o coqueiro", "🐙" to "o polvo",
    )

    /** Coisas da caverna, para "Lia achou um cristal e um morcego". */
    val itensDaCaverna = listOf(
        "💎" to "um cristal", "🦇" to "um morcego", "🔦" to "uma lanterna", "🪨" to "uma pedra", "🕷️" to "uma aranha",
        "🗝️" to "uma chave", "🪙" to "uma moeda", "🦴" to "um osso", "🍄" to "um cogumelo", "👑" to "uma coroa",
    )

    /** Listas de compras: o título e os itens (figura e nome). */
    val listasDeCompras = listOf(
        "Lista da festa: suco, bolo, balas e pipoca." to listOf("🧃" to "suco", "🎂" to "bolo", "🍬" to "balas", "🍿" to "pipoca"),
        "Lista do mercado: leite, ovos, pão e queijo." to listOf("🥛" to "leite", "🥚" to "ovos", "🍞" to "pão", "🧀" to "queijo"),
        "Lista da feira: banana, uva, cenoura e alface." to listOf("🍌" to "banana", "🍇" to "uva", "🥕" to "cenoura", "🥬" to "alface"),
        "Lista do lanche: sanduíche, maçã e suco." to listOf("🥪" to "sanduíche", "🍎" to "maçã", "🧃" to "suco"),
    )

    /** Coisas que não estão em nenhuma lista. */
    val foraDaLista = listOf(
        "🍕" to "pizza", "🐟" to "peixe", "🍔" to "hambúrguer", "🍉" to "melancia", "🍗" to "frango",
        "🥦" to "brócolis", "🍩" to "rosquinha", "🍋" to "limão", "🥥" to "coco", "🍫" to "chocolate",
    )

    /** Frases com o verbo terminado em AM (eles fazem): a parte "___" é a que se escreve. */
    val verbosAm = listOf(
        "Eles ___ no parque." to "BRINCAM", "As crianças ___ alto." to "CANTAM", "Os gatos ___ o muro." to "PULAM",
        "Os meninos ___ bola." to "JOGAM", "As meninas ___ na piscina." to "NADAM", "Os pássaros ___ alto." to "VOAM",
        "Elas ___ suco de uva." to "TOMAM", "Os amigos ___ juntos." to "ESTUDAM",
    )

    /**
     * Palavras de verdade que nunca podem aparecer como "escrita errada" (SOU no lugar de SOL soa igual;
     * MAÇA e CORO existem). Junto com todas as palavras das listas acima.
     */
    val palavrasQueExistem: Set<String> by lazy {
        val listas = listOf(
            cv2, cv3, cv4, nasaisAo, nasaisN, ditongos, travadasR, travadasS, travadasL, rr, rFraco, ss, qu, gu, cedilha,
            cSuave, gSuave, jota, sComSomDeZ, zeta, encontrosL, encontrosR, xis, che, aga, semAga, mpmb, nAntes, mFim, acentos, lOuU,
        ).flatten().map { it.texto }
        listas.toSet() + setOf(
            "SOU", "MAU", "MEU", "CORO", "MAÇA", "CAÇA", "CASSA", "ROÇA", "MOÇA", "PERRA", "MURO", "MURRO", "SELA", "CELA",
            "CEM", "SEM", "SELO", "CINTO", "SINTO", "AÇO", "ASSO", "PESO", "ASA", "ASSA", "PASSO", "COSER", "COZER", "LAÇO",
            "MALA", "BALA", "BOCA", "FOCA", "PATO", "GATO", "RATO", "GOTA", "BOTA", "VACA", "FACA", "CASA", "COCO", "CANO",
            "TELA", "VELA", "SAL", "MAL", "CÉU", "CEU", "VEU", "VÉU", "CHA", "XA", "OVO", "UVA", "HORA", "ORA", "OLA", "OLÁ",
        )
    }

    // ============================================================== narração

    /** Como o narrador fala o nome das letras com acento e do Ç (as outras vêm de Vocabulario). */
    private val nomeDasLetrasEspeciais = mapOf(
        "Ç" to "cê cedilha", "Ã" to "á com til", "Õ" to "ó com til", "Á" to "á com acento", "É" to "é com acento",
        "Í" to "i com acento", "Ó" to "ó com acento", "Ú" to "u com acento", "Â" to "â com chapeuzinho",
        "Ê" to "ê com chapeuzinho", "Ô" to "ô com chapeuzinho",
    )

    fun falaDaLetra(letra: String): String = nomeDasLetrasEspeciais[letra.uppercase()] ?: Vocabulario.falaDaLetra(letra)

    /** Pedacinhos que a voz do Android lê mal sozinhos: escritos do jeito que soam. */
    private val silabasFaladas = mapOf(
        "A" to "á", "E" to "é", "O" to "ó", "ÇA" to "sá", "ÇO" to "sô", "ÇU" to "su", "ÇÃO" to "são", "ÇÃ" to "sã",
        "QUI" to "qui", "GUI" to "gui", "GUIM" to "guim", "ÃO" to "ão", "XI" to "xi", "CHI" to "chi",
        "LHA" to "lhá", "NHA" to "nhá", "CHA" to "chá", "XA" to "chá", "LHAU" to "lháu",
    )

    /**
     * O pedacinho do jeito que a voz deve falar. Sozinhos, "de" e "do" viram "dji" e "du" na voz do
     * Android; com o acento ("dê", "dô") saem como na escola: bê, cê, dê... e bô, cô, dô...
     */
    fun falaDaSilaba(s: String): String {
        silabasFaladas[s]?.let { return it }
        val l = s.lowercase()
        val acentuada = l.any { it in "áéíóúâêôãõ" }
        if (!acentuada && l.length in 2..4 && l[0] !in "aeiou") {
            if (l.endsWith("e")) return l.dropLast(1) + "ê"
            if (l.endsWith("o")) return l.dropLast(1) + "ô"
        }
        return l
    }
}
