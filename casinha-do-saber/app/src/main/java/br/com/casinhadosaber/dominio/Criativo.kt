package br.com.casinhadosaber.dominio

/**
 * Modo criativo (sugestão 5): desenhos em quadradinhos que podem ser pendurados nas paredes da casa e
 * um piano com músicas simples para seguir (ou tocar livre). Nada aqui tem certo ou errado.
 */

/**
 * Uma pintura do ateliê (o nome Desenho já é do traço dos itens da casa): [LADO] por [LADO] quadradinhos, cada um com o índice de uma cor de [Atelie.paleta]
 * (0 = branco, o fundo). [celulas] vai linha por linha, da esquerda para a direita.
 */
data class Pintura(val id: String, val celulas: List<Int>) {
    init {
        require(celulas.size == LADO * LADO) { "Pintura precisa de ${LADO * LADO} quadradinhos" }
    }

    fun cor(linha: Int, coluna: Int): Int = celulas[linha * LADO + coluna]

    val vazio: Boolean get() = celulas.all { it == 0 }

    /** Pinta um quadradinho (fora do quadro ou cor desconhecida não muda nada). */
    fun pintar(linha: Int, coluna: Int, cor: Int): Pintura {
        if (linha !in 0 until LADO || coluna !in 0 until LADO || cor !in Atelie.paleta.indices) return this
        val i = linha * LADO + coluna
        if (celulas[i] == cor) return this
        return copy(celulas = celulas.toMutableList().also { it[i] = cor })
    }

    /**
     * Pinta a linha reta entre dois quadradinhos (o dedo arrastado vira um traço sem buracos).
     * Algoritmo de Bresenham.
     */
    fun tracar(l0: Int, c0: Int, l1: Int, c1: Int, cor: Int): Pintura {
        if (cor !in Atelie.paleta.indices) return this
        val novas = celulas.toMutableList()
        var l = l0
        var c = c0
        val dl = kotlin.math.abs(l1 - l0)
        val dc = kotlin.math.abs(c1 - c0)
        val sl = if (l0 < l1) 1 else -1
        val sc = if (c0 < c1) 1 else -1
        var erro = dc - dl
        while (true) {
            if (l in 0 until LADO && c in 0 until LADO) novas[l * LADO + c] = cor
            if (l == l1 && c == c1) break
            val e2 = 2 * erro
            if (e2 > -dl) { erro -= dl; c += sc }
            if (e2 < dc) { erro += dc; l += sl }
        }
        return copy(celulas = novas)
    }

    /** Balde de tinta: pinta a mancha da mesma cor ligada ao quadradinho tocado. */
    fun encher(linha: Int, coluna: Int, cor: Int): Pintura {
        if (linha !in 0 until LADO || coluna !in 0 until LADO || cor !in Atelie.paleta.indices) return this
        val alvo = cor(linha, coluna)
        if (alvo == cor) return this
        val novas = celulas.toMutableList()
        val pilha = ArrayDeque<Int>().apply { add(linha * LADO + coluna) }
        while (pilha.isNotEmpty()) {
            val i = pilha.removeLast()
            if (novas[i] != alvo) continue
            novas[i] = cor
            val l = i / LADO
            val c = i % LADO
            if (l > 0) pilha.add(i - LADO)
            if (l < LADO - 1) pilha.add(i + LADO)
            if (c > 0) pilha.add(i - 1)
            if (c < LADO - 1) pilha.add(i + 1)
        }
        return copy(celulas = novas)
    }

    fun limpar(): Pintura = copy(celulas = List(LADO * LADO) { 0 })

    companion object {
        const val LADO = 12

        fun vazio(id: String): Pintura = Pintura(id, List(LADO * LADO) { 0 })
    }
}

object Atelie {
    /** Quantos desenhos ficam guardados. */
    const val MAXIMO_DE_DESENHOS = 12

    /** Quantos quadros cabem nas paredes da casa. */
    const val MAXIMO_NA_PAREDE = 3

    /** Cores do lápis (0xAARRGGBB) e o nome falado. O índice 0 é o branco do papel (borracha). */
    val paleta: List<Pair<Long, String>> = listOf(
        0xFFFFFFFF to "branco",
        0xFF1C1C1C to "preto",
        0xFFEF476F to "vermelho",
        0xFFFF9F1C to "laranja",
        0xFFFFD166 to "amarelo",
        0xFF3DBE6B to "verde",
        0xFF4D96FF to "azul",
        0xFF9B6BFF to "roxo",
        0xFFFF6FAE to "rosa",
        0xFF8D5A3B to "marrom",
        0xFF90A4AE to "cinza",
        0xFF7FDBFF to "azul clarinho",
    )

    private const val DIGITOS = "0123456789abcdefghijklmnopqrstuvwxyz"

    /** Um id novo, sempre diferente dos que existem ("d1", "d2"...). */
    fun novoId(desenhos: List<Pintura>): String =
        "d" + ((desenhos.mapNotNull { it.id.removePrefix("d").toIntOrNull() }.maxOrNull() ?: 0) + 1)

    /** "id:quadradinhos;id:quadradinhos", um caractere por quadradinho. */
    fun desenhosParaTexto(desenhos: List<Pintura>): String =
        desenhos.joinToString(";") { d -> d.id + ":" + d.celulas.joinToString("") { DIGITOS[it].toString() } }

    /** Saves antigos ou texto estragado: os desenhos que não dá para ler somem, os outros continuam. */
    fun textoParaDesenhos(s: String?): List<Pintura> = s.orEmpty().split(";").mapNotNull { parte ->
        val id = parte.substringBefore(":", "")
        val codigo = parte.substringAfter(":", "")
        if (id.isEmpty() || id.any { it == ',' } || codigo.length != Pintura.LADO * Pintura.LADO) return@mapNotNull null
        val celulas = codigo.map { DIGITOS.indexOf(it) }
        if (celulas.any { it !in paleta.indices }) return@mapNotNull null
        Pintura(id, celulas)
    }.distinctBy { it.id }.take(MAXIMO_DE_DESENHOS)

    fun paredeParaTexto(ids: List<String>): String = ids.joinToString(",")

    /** Só ids de desenhos que existem, sem repetir, no máximo [MAXIMO_NA_PAREDE]. */
    fun textoParaParede(s: String?, desenhos: List<Pintura>): List<String> {
        val existentes = desenhos.map { it.id }.toSet()
        return s.orEmpty().split(",").filter { it in existentes }.distinct().take(MAXIMO_NA_PAREDE)
    }
}

/** Notas do piano (uma oitava, de dó a dó), com a frequência em Hz e a cor da tecla. */
enum class Nota(val nome: String, val frequencia: Double, val corArgb: Long) {
    DO("dó", 261.63, 0xFFEF476F),
    RE("ré", 293.66, 0xFFFF9F1C),
    MI("mi", 329.63, 0xFFFFD166),
    FA("fá", 349.23, 0xFF3DBE6B),
    SOL("sol", 392.00, 0xFF2EC4B6),
    LA("lá", 440.00, 0xFF4D96FF),
    SI("si", 493.88, 0xFF9B6BFF),
    DO_AGUDO("dó", 523.25, 0xFFFF6FAE),
}

/** Uma nota da música e quanto ela dura ([tempos] = 1 para curta, 2 para longa). */
data class NotaDaMusica(val nota: Nota, val tempos: Int = 1)

/** Música para seguir: a tecla da vez brilha e a criança toca. Melodias tradicionais, de domínio público. */
data class Musica(val id: String, val titulo: String, val emoji: String, val notas: List<NotaDaMusica>)

object Piano {
    private fun m(texto: String): List<NotaDaMusica> = texto.trim().split(Regex("\\s+")).map { t ->
        val longa = t.endsWith("-")
        val nome = t.removeSuffix("-")
        NotaDaMusica(Nota.valueOf(nome), if (longa) 2 else 1)
    }

    val musicas: List<Musica> = listOf(
        Musica(
            "brilha", "Brilha, brilha, estrelinha", "⭐",
            m(
                "DO DO SOL SOL LA LA SOL- FA FA MI MI RE RE DO- " +
                    "SOL SOL FA FA MI MI RE- SOL SOL FA FA MI MI RE- " +
                    "DO DO SOL SOL LA LA SOL- FA FA MI MI RE RE DO-",
            ),
        ),
        Musica(
            "alegria", "Hino da alegria", "🎶",
            m(
                "MI MI FA SOL SOL FA MI RE DO DO RE MI MI- RE RE- " +
                    "MI MI FA SOL SOL FA MI RE DO DO RE MI RE- DO DO-",
            ),
        ),
        Musica(
            "ovelhinha", "A ovelhinha da Maria", "🐑",
            m("MI RE DO RE MI MI MI- RE RE RE- MI SOL SOL- MI RE DO RE MI MI MI MI RE RE MI RE DO-"),
        ),
        Musica("escala", "Escadinha das notas", "🎹", m("DO RE MI FA SOL LA SI DO_AGUDO- DO_AGUDO SI LA SOL FA MI RE DO-")),
    )

    val porId: Map<String, Musica> = musicas.associateBy { it.id }

    /**
     * Seguir a música: a criança tocou [tocada] quando a vez era da nota [posicao]. Devolve a próxima posição
     * (avança só no acerto; tocar outra tecla não volta nada). Quando chega em notas.size, a música acabou.
     */
    fun seguir(musica: Musica, posicao: Int, tocada: Nota): Int =
        if (posicao in musica.notas.indices && musica.notas[posicao].nota == tocada) posicao + 1 else posicao

    fun terminou(musica: Musica, posicao: Int): Boolean = posicao >= musica.notas.size
}
