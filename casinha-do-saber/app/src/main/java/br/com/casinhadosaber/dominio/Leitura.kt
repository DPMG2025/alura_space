package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.VocabularioLetras

/**
 * O que a criança já sabe ler em cada ponto do caminho Letras: as sílabas ensinadas até cada fase.
 * Os livrinhos ([Livrinhos]) só usam palavras que dá para ler juntando essas sílabas, e cada livrinho abre
 * quando a criança vence a fase que ensina a última sílaba que ele usa.
 *
 * Cada [EtapaDeLeitura] é uma fase do caminho Letras que traz sílabas novas. As sílabas vêm de dois lugares:
 * as próprias palavras que as fases daquele mundo usam ([VocabularioLetras], [VocabularioDosMundos]) e as
 * regras que a fase ensina para qualquer consoante (a família silábica, o som nasal com N, o R, S e L no fim
 * da sílaba, os encontros BR e PL...). Tudo é calculado uma vez, sem sorteio.
 */
data class EtapaDeLeitura(
    /** Índice da fase do caminho Letras (começa em 0, como [Fase.numero]). */
    val fase: Int,
    val nome: String,
    /** Sílabas abertas novas (terminam em vogal). */
    val abertas: Set<String> = emptySet(),
    /** Sílabas que aparecem nas palavras das fases desta etapa (entram como estão). */
    val dasPalavras: Set<String> = emptySet(),
    /** Letras que a partir daqui podem fechar uma sílaba (N, R, S, L, Z, M). */
    val codas: Set<Char> = emptySet(),
    /** A partir daqui, as sílabas com A também valem com til (MÃO, MÃE, MAÇÃ, PÕE). */
    val til: Boolean = false,
    /** A partir daqui, as vogais também valem com acento (PÉ, VOVÓ, VOCÊ). */
    val acentos: Boolean = false,
)

object Leitura {

    private val vogais = Vocabulario.vogais

    private fun familia(inicio: String) = vogais.map { inicio + it }.toSet()
    private fun silabasDe(vararg listas: List<Palavra>): Set<String> = listas.flatMap { l -> l.flatMap { it.silabas } }.toSet()

    /** As etapas, na ordem do caminho Letras. */
    val etapas: List<EtapaDeLeitura> = listOf(
        // Fase 27, "Famílias: BA BE BI": vogais e famílias silábicas simples (C e G só com A, O e U).
        EtapaDeLeitura(26, "Famílias silábicas", abertas = vogais.toSet() + VocabularioLetras.familias.values.flatten()),
        // Fase 33, "CH, LH e NH".
        EtapaDeLeitura(
            32, "CH, LH e NH",
            abertas = familia("CH") + familia("LH") + familia("NH"),
            dasPalavras = silabasDe(VocabularioDosMundos.palavrasComplexas.filter { p -> p.silabas.any { 'H' in it } }),
        ),
        // Fase 39, "BR, PR e TR".
        EtapaDeLeitura(38, "BR, PR e TR", abertas = familia("BR") + familia("PR") + familia("TR")),
        // Fase 51, mundo 6 (Fazenda): palavras de 2, 3 e 4 sílabas, com CE, CI, GE e GI.
        EtapaDeLeitura(
            50, "Palavras da fazenda",
            abertas = setOf("CE", "CI", "GE", "GI"),
            dasPalavras = silabasDe(VocabularioLetras.cv2, VocabularioLetras.cv3, VocabularioLetras.cv4, VocabularioLetras.fazenda),
        ),
        // Fase 61, mundo 7 (Fundo do Mar): som nasal com til e com N (MÃO, PANDA).
        EtapaDeLeitura(60, "Som nasal", dasPalavras = silabasDe(VocabularioLetras.nasaisAo, VocabularioLetras.nasaisN), codas = setOf('N'), til = true),
        // Fase 71, mundo 8 (Selva): dois sons juntinhos (PEIXE, CHAPÉU).
        EtapaDeLeitura(70, "Ditongos", dasPalavras = silabasDe(VocabularioLetras.ditongos)),
        // Fase 81, mundo 9 (Polo Norte): R, S e L no fim da sílaba (MAR, FESTA, SOL).
        EtapaDeLeitura(
            80, "R, S e L no fim",
            dasPalavras = silabasDe(VocabularioLetras.travadasR, VocabularioLetras.travadasS, VocabularioLetras.travadasL),
            codas = setOf('R', 'S', 'L'),
        ),
        // Fase 91, mundo 10 (Circo): RR, SS, QU e GU.
        EtapaDeLeitura(
            90, "RR, SS, QU e GU",
            abertas = setOf("QUE", "QUI", "GUE", "GUI"),
            dasPalavras = silabasDe(VocabularioLetras.rr, VocabularioLetras.rFraco, VocabularioLetras.ss, VocabularioLetras.qu, VocabularioLetras.gu),
        ),
        // Fase 101, mundo 11 (Deserto): Ç, C e G suaves, J e o Z no fim (DEZ, NARIZ).
        EtapaDeLeitura(
            100, "Ç, GE, GI e Z",
            abertas = setOf("ÇA", "ÇO", "ÇU") + familia("J"),
            dasPalavras = silabasDe(
                VocabularioLetras.cedilha, VocabularioLetras.cSuave, VocabularioLetras.gSuave,
                VocabularioLetras.jota, VocabularioLetras.sComSomDeZ, VocabularioLetras.zeta,
            ),
            codas = setOf('Z'),
        ),
        // Fase 111, mundo 12 (Cidade): encontros consonantais (BLUSA, PEDRA, FLOR).
        EtapaDeLeitura(
            110, "Encontros consonantais",
            abertas = listOf("B", "C", "D", "F", "G", "P", "T", "V").flatMap { c -> familia(c + "R") + familia(c + "L") }.toSet(),
            dasPalavras = silabasDe(VocabularioLetras.encontrosL, VocabularioLetras.encontrosR),
        ),
        // Fase 121, mundo 13 (Castelo): X e CH, o H do começo (HORA, HOTEL).
        EtapaDeLeitura(
            120, "X, CH e H",
            abertas = familia("H"),
            dasPalavras = silabasDe(VocabularioLetras.xis, VocabularioLetras.che, VocabularioLetras.aga, VocabularioLetras.semAga),
        ),
        // Fase 131, mundo 14 (Vale dos Dinossauros): M antes de P e B, M e N no fim (BOMBOM, TREM).
        EtapaDeLeitura(
            130, "M e N no fim",
            dasPalavras = silabasDe(VocabularioLetras.mpmb, VocabularioLetras.nAntes, VocabularioLetras.mFim),
            codas = setOf('M'),
        ),
        // Fase 204, mundo 21 (Parque de Diversões): "Pescaria de acentos" (CAFÉ, VOVÓ, VOCÊ).
        EtapaDeLeitura(
            203, "Acentos",
            dasPalavras = silabasDe(
                VocabularioLetras.acentos, VocabularioLetras.forteNoFim, VocabularioLetras.forteNoMeio,
                VocabularioLetras.forteNoComeco, VocabularioLetras.lOuU,
            ),
            acentos = true,
        ),
    )

    private val comAcento = mapOf('A' to "ÁÂ", 'E' to "ÉÊ", 'I' to "Í", 'O' to "ÓÔ", 'U' to "Ú")

    /** As sílabas que se lê até a etapa de índice [k] (inclusive), com as regras aplicadas. */
    private val inventarios: List<Set<String>> = etapas.indices.map { k ->
        val ate = etapas.take(k + 1)
        val abertas = ate.flatMap { it.abertas }.toMutableSet()
        if (ate.any { it.til }) {
            abertas += abertas.filter { it.endsWith("A") }.flatMap { s -> listOf("ÃO", "ÃE", "Ã").map { s.dropLast(1) + it } }
            abertas += abertas.filter { it.endsWith("O") }.map { it.dropLast(1) + "ÕE" }
        }
        if (ate.any { it.acentos }) {
            abertas += abertas.flatMap { s -> comAcento[s.last()]?.map { a -> s.dropLast(1) + a }.orEmpty() }
        }
        val codas = ate.flatMap { it.codas }.toSet()
        val fechadas = abertas.flatMap { s -> codas.map { s + it } }
        abertas + fechadas + ate.flatMap { it.dasPalavras }
    }

    /** Índice da etapa vigente na fase [fase] do caminho Letras (-1 antes da primeira). */
    fun etapaDa(fase: Int): Int = etapas.indexOfLast { it.fase <= fase }

    /** As sílabas que a criança já encontrou até a fase [fase] do caminho Letras. */
    fun silabasAte(fase: Int): Set<String> = etapaDa(fase).let { if (it < 0) emptySet() else inventarios[it] }

    /** As palavras de uma frase (sem pontuação), como estão escritas. */
    fun palavras(texto: String): List<String> =
        texto.split(' ', '\n').map { it.trim { c -> !c.isLetter() } }.filter { it.isNotEmpty() }

    /**
     * Divide [palavra] nas sílabas conhecidas até a fase [fase] (null se não dá para ler ainda). Entre as divisões
     * possíveis, fica a de menos pedaços (empate: o primeiro pedaço maior), que é a divisão da escola nas
     * palavras dos livrinhos.
     */
    fun silabas(palavra: String, fase: Int): List<String>? =
        dividir(palavra.uppercase(), silabasAte(fase), vogaisJuntas = etapaDa(fase) >= ETAPA_DITONGOS)

    private const val VOGAIS = "AEIOUÁÉÍÓÚÂÊÔÃÕ"

    /**
     * Etapa "Ditongos" (fase 71): só a partir dela se leem duas vogais na mesma sílaba (VAI, NOITE, AREIA,
     * POUSA, CÉU). Encontros que são duas sílabas (LI-A, LU-A, BO-A, RI-O) valem desde o começo.
     */
    private val ETAPA_DITONGOS = etapas.indexOfFirst { it.fase == 70 }

    /** Que tipo de pedaço começa o resto: 0 = outro, 1 = começa com I, 2 = começa com U (sem acento). */
    private fun tipo(pedaco: String) = when (pedaco.first()) { 'I' -> 1; 'U' -> 2; else -> 0 }

    /** [antes] terminando nessa vogal, seguido de um pedaço do [tipo], faria um ditongo (AI, EI, OI, UI, AU, EU, IU, OU)? */
    private fun fariaDitongo(antes: Char, tipo: Int) = when (tipo) {
        1 -> antes in "AEOU"
        2 -> antes in "AEIO"
        else -> false
    }

    /**
     * Pode cortar a palavra antes da posição [j]? Nunca no meio de CH, LH, NH (OLHA é O-LHA, não OL-HA)
     * nem de QU e GU (A-QUI, A-GUA).
     */
    private fun podeCortar(p: String, j: Int): Boolean {
        if (j <= 0 || j >= p.length) return true
        val antes = p[j - 1]
        val depois = p[j]
        if (depois == 'H' && antes in "CLN") return false
        if (depois == 'U' && antes in "QG") return false
        return true
    }

    private fun dividir(p: String, conhecidas: Set<String>, vogaisJuntas: Boolean): List<String>? {
        if (p.isEmpty()) return null
        // Para cada resto p[i..], a melhor divisão de cada tipo (começando com I, com U ou com outra coisa):
        // antes dos ditongos, VA + I ou POU... não valem, então um pedaço que termina em vogal só pode vir
        // antes de um resto que não forme ditongo com ele.
        // Custo: 10 por pedaço e 1 quando uma sílaba fechada vem antes de uma que começa com vogal
        // (CA-SA, nunca CAS-A: a consoante do meio vai com a vogal seguinte).
        val n = p.length
        val melhor = Array(3) { arrayOfNulls<List<String>>(n + 1) }
        val custo = Array(3) { IntArray(n + 1) { Int.MAX_VALUE } }
        melhor[0][n] = emptyList()
        custo[0][n] = 0
        for (i in n - 1 downTo 0) {
            for (j in minOf(n, i + 5) downTo i + 1) {
                val pedaco = p.substring(i, j)
                if (pedaco !in conhecidas || !podeCortar(p, j)) continue
                val ultima = pedaco.last()
                val vogalNoFim = ultima in VOGAIS
                val meu = tipo(pedaco)
                for (t in 0..2) {
                    val resto = melhor[t][j] ?: continue
                    if (!vogaisJuntas && fariaDitongo(ultima, t)) continue
                    val emenda = if (!vogalNoFim && resto.firstOrNull()?.first()?.let { it in VOGAIS } == true) 1 else 0
                    val c = 10 + emenda + custo[t][j]
                    if (c < custo[meu][i]) {
                        custo[meu][i] = c
                        melhor[meu][i] = listOf(pedaco) + resto
                    }
                }
            }
        }
        return (0..2).filter { melhor[it][0] != null }.minByOrNull { custo[it][0] }?.let { melhor[it][0] }
    }

    fun decodificavel(palavra: String, fase: Int): Boolean = silabas(palavra, fase) != null

    /** A fase (índice) da primeira etapa em que todas as palavras de [textos] dão para ler, ou null. */
    fun faseMinima(textos: List<String>): Int? {
        val todas = textos.flatMap(::palavras).map { it.uppercase() }.distinct()
        return etapas.indices.firstOrNull { k -> todas.all { dividir(it, inventarios[k], vogaisJuntas = k >= ETAPA_DITONGOS) != null } }?.let { etapas[it].fase }
    }

    /** As palavras de [textos] que ainda não dá para ler na fase [fase] (para o teste mostrar o que falta). */
    fun palavrasDificeis(textos: List<String>, fase: Int): List<String> =
        textos.flatMap(::palavras).distinct().filter { !decodificavel(it, fase) }

    /**
     * Como o narrador lê os pedacinhos de uma palavra: cada sílaba do jeito que soa sozinha
     * ([VocabularioLetras.falaDaSilaba]) e depois a palavra inteira.
     */
    fun falaDosPedacinhos(palavra: String): List<String> {
        val pedacos = dividir(palavra.uppercase(), inventarios.last(), vogaisJuntas = true) ?: return listOf(palavra.lowercase())
        return pedacos.map(VocabularioLetras::falaDaSilaba) + palavra.lowercase()
    }
}
