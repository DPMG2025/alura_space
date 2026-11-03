package br.com.casinhadosaber.dominio

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * A casa por dentro, vista de cima em perspectiva isométrica (2,5D). Aqui fica só a "planta":
 * ladrilhos, paredes, portas, onde cada móvel mora, por onde a criança pode andar e em que ordem
 * desenhar as coisas. Kotlin puro, testável sem Android. O desenho fica em ui/telas/TelaCasaPorDentro.kt.
 *
 * É a mesma casa de fora (CenaDaCasa): dois andares ligados pela escada. Em cima, o quarto (à esquerda)
 * e o banheiro; embaixo, a sala (com a escada e a porta da rua) e a cozinha. Cada andar tem
 * [PlantaDaCasa.LARGURA] x [PlantaDaCasa.FUNDO] ladrilhos e uma parede interna baixinha em
 * x = [PlantaDaCasa.meio], com uma passagem.
 *
 * Coordenadas em ladrilhos: x cresce para baixo e para a direita na tela (da esquerda para a direita da
 * fachada), y cresce para baixo e para a esquerda (do fundo da casa para a frente). As paredes do fundo
 * ficam em y = 0 (a comprida, onde os móveis se encostam) e em x = 0.
 *
 *     andar de cima:   QUARTO | BANHEIRO
 *     andar de baixo:  SALA   | COZINHA
 */

/** Os dois andares da casa. */
enum class Andar(val nome: String, val nomeEn: String) {
    DE_BAIXO("Andar de baixo", "downstairs"),
    DE_CIMA("Andar de cima", "upstairs");

    val outro: Andar get() = if (this == DE_BAIXO) DE_CIMA else DE_BAIXO
}

/** Em que andar fica cada cômodo (igual à casa de fora). */
val Comodo.andar: Andar get() = if (this == Comodo.QUARTO || this == Comodo.BANHEIRO) Andar.DE_CIMA else Andar.DE_BAIXO

/** Para que lado fica a frente do móvel (o lado que a câmera enxerga). */
enum class Frente { X, Y }

/** O que acontece ao tocar no móvel. */
enum class Acao { NENHUMA, GELADEIRA, GUARDA_ROUPA, TORNEIRA, TV, FOGAO, BANHEIRA, CAMA, ESPELHO, DESCARGA, ESCADA, PATINHO, LIXEIRA }

/**
 * Um móvel da casa por dentro. A "pegada" no chão vai de ([x0], [y0]) a ([x1], [y1]) em ladrilhos;
 * [altura] e [z] (onde começa, acima do chão) usam a mesma unidade. [itemId] liga o móvel a uma peça
 * ganha ([ItensDaCasa]); móveis sem peça ([fixo]) existem sempre.
 */
data class Movel(
    val id: String,
    val nome: String,
    val nomeEn: String,
    val comodo: Comodo,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
    val altura: Float,
    val z: Float = 0f,
    val frente: Frente = Frente.Y,
    val acao: Acao = Acao.NENHUMA,
    /** Bloqueia a passagem da criança. */
    val bloqueia: Boolean = true,
    /** Pendurado numa parede do fundo (quadro, espelho): é desenhado junto com a parede. */
    val naParede: Boolean = false,
    /** Plano no chão (tapete): desenhado junto com o piso. */
    val noChao: Boolean = false,
    val itemId: String? = null,
) {
    val fixo get() = itemId == null
    val centroX get() = (x0 + x1) / 2
    val centroY get() = (y0 + y1) / 2
    val andar: Andar get() = comodo.andar

    /** O mesmo móvel [dx] ladrilhos para a direita e [dy] para a frente, no cômodo onde ele cai. */
    fun deslocado(dx: Int, dy: Int): Movel {
        if (dx == 0 && dy == 0) return this
        val n = copy(x0 = x0 + dx, y0 = y0 + dy, x1 = x1 + dx, y1 = y1 + dy)
        return n.copy(comodo = PlantaDaCasa.comodoEm(andar, n.centroX, n.centroY))
    }
}

/** Algo que precisa de ordem de desenho: um móvel, um pedaço de parede interna, etc. (caixa no chão). */
data class Caixa(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val z0: Float = 0f, val z1: Float = 1f)

/**
 * As prateleiras (abas) da geladeira: cada comida mora numa delas.
 * [icone] é o desenho da aba; o narrador fala [nome] e [nomeEn] quando a criança toca nela.
 */
enum class PrateleiraDaGeladeira(val icone: String, val nome: String, val nomeEn: String, val emojis: Set<String>) {
    FRUTAS("🍎", "Frutas e legumes", "fruits", setOf("🍎", "🍌", "🥕", "🍓", "🍉")),
    LEITE("🥛", "Leite, queijo e ovos", "milk", setOf("🥛", "🧀", "🥚")),
    CONGELADOR("🧊", "Congelador, bem geladinho", "freezer", setOf("🍦", "🍕"));

    companion object {
        /** A prateleira de uma comida (pelo emoji); o que não estiver em nenhuma vai para as frutas. */
        fun de(emoji: String): PrateleiraDaGeladeira = entries.firstOrNull { emoji in it.emojis } ?: FRUTAS

        /** As comidas desta prateleira, na ordem em que vieram. */
        fun filtrar(comidas: List<Produto>, p: PrateleiraDaGeladeira): List<Produto> = comidas.filter { de(it.valor) == p }

        /** A aba que abre primeiro: a primeira que tem comida (ou as frutas, se a geladeira estiver vazia). */
        fun inicial(comidas: List<Produto>): PrateleiraDaGeladeira = entries.firstOrNull { p -> comidas.any { de(it.valor) == p } } ?: FRUTAS
    }
}

object PlantaDaCasa {
    /** Ladrilhos de cada andar: da esquerda para a direita da fachada ([LARGURA]) e do fundo para a frente ([FUNDO]). */
    const val LARGURA = 8
    const val FUNDO = 5

    /** Altura das paredes do fundo e das paredes internas (cortadas baixinhas, para ver dentro). */
    const val ALTURA_PAREDE = 3.0f
    const val ALTURA_PAREDE_INTERNA = 0.45f

    /** Onde fica a parede entre os dois cômodos do andar (como na fachada: a sala é maior que a cozinha). */
    fun meio(andar: Andar): Int = if (andar == Andar.DE_CIMA) 4 else 5

    /** Na parede interna, as passagens ficam nas linhas y destes ladrilhos (na frente, longe dos móveis). */
    val portasNaParedeInterna = setOf(3, 4)

    /** Os dois cômodos do andar: o da esquerda e o da direita. */
    fun comodosDo(andar: Andar): List<Comodo> =
        if (andar == Andar.DE_CIMA) listOf(Comodo.QUARTO, Comodo.BANHEIRO) else listOf(Comodo.SALA, Comodo.COZINHA)

    /** Onde a criança aparece quando entra pela porta da rua (na sala). */
    val entrada = Posicao(0.6f, 4.3f)

    /** A porta da rua, na parede x = 0 da sala: de [portaDaRua].x a [portaDaRua].y em ladrilhos. */
    val portaDaRua = Posicao(3.35f, 4.55f)

    /** Onde a criança pisa para subir ou descer (ao pé da escada, igual nos dois andares). */
    val peDaEscada = Posicao(1.45f, 3.2f)

    /** Ponto das lâmpadas penduradas no meio de cada cômodo. */
    fun centroDo(c: Comodo): Posicao = limitesDo(c).let { Posicao((it.x0 + it.x1) / 2, (it.y0 + it.y1) / 2) }

    fun limitesDo(c: Comodo): Caixa {
        val m = meio(c.andar).toFloat()
        val esquerda = comodosDo(c.andar)[0] == c
        return if (esquerda) Caixa(0f, 0f, m, FUNDO.toFloat()) else Caixa(m, 0f, LARGURA.toFloat(), FUNDO.toFloat())
    }

    fun comodoEm(andar: Andar, x: Float, y: Float): Comodo = comodosDo(andar)[if (x < meio(andar)) 0 else 1]

    private fun item(id: String) = ItensDaCasa.porId.getValue(id)

    private fun daPeca(
        id: String, comodo: Comodo, x0: Float, y0: Float, x1: Float, y1: Float, altura: Float,
        z: Float = 0f, frente: Frente = Frente.Y, acao: Acao = Acao.NENHUMA, bloqueia: Boolean = true, naParede: Boolean = false,
    ): Movel {
        val i = item(id)
        return Movel(id, i.nome, i.nomeEn, comodo, x0, y0, x1, y1, altura, z, frente, acao, bloqueia, naParede, false, id)
    }

    /**
     * Todos os móveis possíveis, na mesma ordem da fachada (da esquerda para a direita), encostados na parede
     * do fundo. As peças ganhas aparecem no cômodo certo; os fixos (escada, guarda-roupa, pia...) estão sempre lá.
     */
    val moveis: List<Movel> = listOf(
        // Andar de baixo. Sala: escada, porta da rua, sofá com o gatinho, livros e TV.
        Movel("escada", "Escada", "stairs", Comodo.SALA, 0.08f, 0.08f, 0.95f, 2.6f, 2.7f, acao = Acao.ESCADA),
        daPeca("sofa", Comodo.SALA, 1.05f, 0.1f, 2.7f, 0.98f, 0.88f),
        daPeca("gato", Comodo.SALA, 1.65f, 0.35f, 2.15f, 0.85f, 0.45f, z = 0.5f, bloqueia = false),
        // Os livros ficam entre o sofá e a TV, recuados o bastante para a TV (mais funda) não tapar a pilha na tela.
        daPeca("livros", Comodo.SALA, 2.8f, 0.12f, 3.22f, 0.54f, 0.5f),
        daPeca("tv", Comodo.SALA, 3.7f, 0.1f, 4.85f, 0.6f, 1.3f, acao = Acao.TV),
        Movel("tapete", "Tapete", "rug", Comodo.SALA, 1.5f, 1.45f, 3.9f, 3.45f, 0f, bloqueia = false, noChao = true),
        // Cozinha: a mesa com o bolo, o fogão e a geladeira (no canto, encostada na parede da cozinha).
        daPeca("mesa", Comodo.COZINHA, 5.6f, 2.0f, 6.7f, 2.98f, 0.72f),
        daPeca("bolo", Comodo.COZINHA, 5.9f, 2.25f, 6.4f, 2.75f, 0.45f, z = 0.72f, bloqueia = false),
        Movel("lixeira", "Lixeira", "trash can", Comodo.COZINHA, 5.17f, 0.12f, 5.62f, 0.57f, 0.55f, acao = Acao.LIXEIRA),
        daPeca("fogao", Comodo.COZINHA, 5.95f, 0.1f, 6.8f, 0.92f, 0.85f, acao = Acao.FOGAO),
        daPeca("geladeira", Comodo.COZINHA, 6.98f, 0.1f, 7.9f, 0.95f, 2.0f, acao = Acao.GELADEIRA),
        // Andar de cima. Quarto: a escada que desce, o quadro, a cama, o criado-mudo com o abajur e o violão.
        Movel("escadaDeCima", "Escada", "stairs", Comodo.QUARTO, 0.08f, 0.08f, 0.95f, 2.6f, 0.75f, acao = Acao.ESCADA),
        daPeca("quadro", Comodo.QUARTO, 1.45f, 0f, 2.3f, 0.04f, 0.65f, z = 1.5f, bloqueia = false, naParede = true),
        daPeca("cama", Comodo.QUARTO, 1.25f, 0.1f, 2.5f, 1.98f, 0.55f, acao = Acao.CAMA),
        daPeca("ursinho", Comodo.QUARTO, 1.6f, 1.2f, 2.15f, 1.7f, 0.5f, z = 0.55f, bloqueia = false),
        Movel("criado", "Criado-mudo", "nightstand", Comodo.QUARTO, 2.65f, 0.1f, 3.2f, 0.65f, 0.5f),
        daPeca("abajur", Comodo.QUARTO, 2.7f, 0.13f, 3.15f, 0.62f, 0.55f, z = 0.5f, bloqueia = false),
        // O violão fica no canto da frente do quarto: na tela, nada fica atrás dele (nem a cama nem o abajur).
        daPeca("violao", Comodo.QUARTO, 3.3f, 4.3f, 3.85f, 4.85f, 0.9f),
        Movel("guardaroupa", "Guarda-roupa", "wardrobe", Comodo.QUARTO, 0.1f, 3.0f, 0.72f, 4.85f, 2.2f, frente = Frente.X, acao = Acao.GUARDA_ROUPA),
        // A plantinha enfeita o canto da frente do banheiro (como na casa de fora, perto do banheiro).
        daPeca("planta", Comodo.BANHEIRO, 7.3f, 4.3f, 7.85f, 4.85f, 0.7f),
        // Banheiro: banheira (embaixo da janela redonda), vaso e pia com o espelho em cima. A banheira, funda e
        // baixinha, fica atrás de tudo (à esquerda); o vaso vem depois dela e a pia, rasa, no canto: na tela,
        // nada tapa o vaso.
        daPeca("banheira", Comodo.BANHEIRO, 4.15f, 0.1f, 5.8f, 1.35f, 0.6f, acao = Acao.BANHEIRA),
        Movel("vaso", "Vaso sanitário", "toilet", Comodo.BANHEIRO, 5.92f, 0.1f, 6.47f, 0.7f, 0.7f, acao = Acao.DESCARGA),
        Movel("pia", "Pia", "sink", Comodo.BANHEIRO, 6.98f, 0.1f, 7.9f, 0.7f, 0.9f, acao = Acao.TORNEIRA),
        daPeca("espelho", Comodo.BANHEIRO, 7.07f, 0f, 7.81f, 0.04f, 0.8f, z = 1.3f, acao = Acao.ESPELHO, bloqueia = false, naParede = true),
        // O patinho de borracha boia na banheira (só aparece quando a banheira já chegou).
        Movel(
            "patinho", "Patinho de borracha", "rubber duck", Comodo.BANHEIRO, 4.65f, 0.45f, 5.1f, 0.9f, 0.4f,
            z = 0.6f, acao = Acao.PATINHO, bloqueia = false,
        ),
    )

    val porId: Map<String, Movel> = moveis.associateBy { it.id }

    /** As peças da casa que moram dentro e aparecem aqui (as outras ficam no jardim ou no céu). */
    val pecasDeDentro: Set<String> = moveis.mapNotNull { it.itemId }.toSet()

    /**
     * Móveis que aparecem para este estado: os fixos e as peças já ganhas, cada um no lugar para onde a
     * criança o arrastou ([EstadoJogo.moveisDentro]) ou, se nunca mexeu (ou se o lugar salvo não vale mais),
     * no lugar de sempre.
     */
    fun moveisDe(e: EstadoJogo): List<Movel> {
        val ganhos = e.itensGanhos.mapTo(HashSet()) { it.id }
        val presentes = moveis.filter { it.itemId == null || it.itemId in ganhos }
        val ids = presentes.mapTo(HashSet()) { it.id }
        // Um enfeite fixo que fica em cima de uma peça (o patinho na banheira) só aparece com ela.
        val visiveis = presentes.filter { m -> m.itemId != null || apoios[m.id]?.let { it in ids } ?: true }
        return aplicarPosicoes(visiveis, e.moveisDentro)
    }

    // ------------------------------------------------------------------------------------------
    // Arrastar móveis: a criança segura um móvel e solta noutro ladrilho livre do mesmo andar.
    // ------------------------------------------------------------------------------------------

    /** Enfeites que ficam em cima de outro móvel e vão junto quando ele é arrastado (enfeite -> móvel de baixo). */
    val apoios: Map<String, String> = mapOf(
        "gato" to "sofa", "bolo" to "mesa", "ursinho" to "cama", "abajur" to "criado", "patinho" to "banheira",
    )

    /** Presos na parede ou no encanamento: não saem do lugar. */
    private val presos = setOf("escada", "escadaDeCima", "guardaroupa", "pia", "vaso")

    /** Folga mínima entre dois móveis arrastados (em ladrilhos), para os desenhos não se encostarem. */
    const val FOLGA = 0.08f

    /** Este móvel pode ser arrastado? (no chão, sólido, sem estar preso na parede). */
    fun arrastavel(m: Movel): Boolean =
        m.id !in presos && m.acao != Acao.ESCADA && !m.naParede && !m.noChao && m.bloqueia && m.z == 0f

    /** Quem se mexe quando a criança arrasta [m]: ele mesmo, ou o móvel de baixo (arrastar o gato leva o sofá). */
    fun quemArrasta(m: Movel): String? = (apoios[m.id] ?: m.id).takeIf { id -> porId[id]?.let { arrastavel(it) } == true }

    /** Ladrilhos que a pegada do móvel ocupa (a mesma conta do [Mapa]). */
    fun ladrilhosDe(m: Movel): List<Pair<Int, Int>> {
        val margem = 0.05f
        val lista = ArrayList<Pair<Int, Int>>()
        for (cy in 0 until FUNDO) for (cx in 0 until LARGURA) {
            if (m.x0 < cx + 1 - margem && m.x1 > cx + margem && m.y0 < cy + 1 - margem && m.y1 > cy + margem) lista += cx to cy
        }
        return lista
    }

    /**
     * Ladrilhos que ficam sempre livres: o pé da escada, as passagens da parede interna, a porta da rua
     * (embaixo) e a frente do guarda-roupa (em cima, onde as portas abrem).
     */
    fun reservados(andar: Andar): Set<Pair<Int, Int>> = buildSet {
        add(floor(peDaEscada.x).toInt() to floor(peDaEscada.y).toInt())
        val m = meio(andar)
        val y = portasNaParedeInterna.min()
        add(m - 1 to y)
        add(m to y)
        if (andar == Andar.DE_BAIXO) {
            for (yy in floor(portaDaRua.x).toInt()..floor(portaDaRua.y).toInt().coerceAtMost(FUNDO - 1)) add(0 to yy)
            add(floor(entrada.x).toInt() to floor(entrada.y).toInt())
        } else {
            val g = porId.getValue("guardaroupa")
            val frente = floor(g.x1 + 0.5f).toInt()
            for (yy in floor(g.y0).toInt()..floor(g.y1 - 0.01f).toInt()) add(frente to yy)
        }
    }

    /** Até onde dá para empurrar o móvel sem sair do piso do andar: o deslocamento já limitado. */
    fun limitarDeslocamento(m: Movel, dx: Int, dy: Int): Pair<Int, Int> {
        val minX = ceil(-m.x0 - 0.001f).toInt()
        val maxX = floor(LARGURA - m.x1 + 0.001f).toInt()
        val minY = ceil(-m.y0 - 0.001f).toInt()
        val maxY = floor(FUNDO - m.y1 + 0.001f).toInt()
        return dx.coerceIn(minX, maxOf(minX, maxX)) to dy.coerceIn(minY, maxOf(minY, maxY))
    }

    /** Desloca [id] (e o que está em cima dele) sem conferir nada: para mostrar a prévia enquanto arrasta. */
    fun deslocar(atuais: List<Movel>, id: String, dx: Int, dy: Int): List<Movel> =
        atuais.map { if (it.id == id || apoios[it.id] == id) it.deslocado(dx, dy) else it }

    /**
     * O móvel [novo] (já no lugar novo) cabe ali? Dentro de um cômodo só (sem atravessar a parede interna),
     * longe dos outros móveis sólidos do andar e fora dos ladrilhos [reservados].
     */
    fun cabeNoLugar(atuais: List<Movel>, novo: Movel): Boolean {
        val eps = 0.001f
        val andar = novo.andar
        if (novo.x0 < -eps || novo.y0 < -eps || novo.x1 > LARGURA + eps || novo.y1 > FUNDO + eps) return false
        val c = limitesDo(comodoEm(andar, novo.centroX, novo.centroY))
        val parede = 0.06f
        val esq = if (c.x0 > 0f) c.x0 + parede else c.x0
        val dir = if (c.x1 < LARGURA) c.x1 - parede else c.x1
        if (novo.x0 < esq - eps || novo.x1 > dir + eps) return false
        val reservado = reservados(andar)
        if (ladrilhosDe(novo).any { it in reservado }) return false
        for (o in atuais) {
            if (o.id == novo.id || o.andar != andar || !o.bloqueia || o.naParede || o.noChao || apoios[o.id] == novo.id) continue
            val cruza = novo.x0 < o.x1 + FOLGA && o.x0 < novo.x1 + FOLGA && novo.y0 < o.y1 + FOLGA && o.y0 < novo.y1 + FOLGA
            if (cruza) return false
        }
        return true
    }

    /** Todo ladrilho livre do andar continua alcançável a partir do pé da escada (nada fica isolado). */
    fun tudoAlcancavel(moveis: List<Movel>, andar: Andar): Boolean {
        val mapa = Mapa(moveis, andar)
        if (!mapa.livre(floor(peDaEscada.x).toInt(), floor(peDaEscada.y).toInt())) return false
        val alcance = mapa.alcancaveis(peDaEscada)
        for (y in 0 until FUNDO) for (x in 0 until LARGURA) if (mapa.livre(x, y) && (x to y) !in alcance) return false
        return true
    }

    /**
     * Arrasta o móvel [id] [dx] ladrilhos para a direita e [dy] para a frente (encaixado na grade), levando o
     * que está em cima dele. Devolve a lista nova, ou null se ali não pode (fora do piso, atravessando parede,
     * em cima de outro móvel, tapando a escada ou uma porta, ou deixando algum canto sem caminho).
     */
    fun mover(atuais: List<Movel>, id: String, dx: Int, dy: Int): List<Movel>? {
        val m = atuais.firstOrNull { it.id == id } ?: return null
        if (!arrastavel(m)) return null
        if (dx == 0 && dy == 0) return atuais
        val novo = m.deslocado(dx, dy)
        if (!cabeNoLugar(atuais, novo)) return null
        val resultado = deslocar(atuais, id, dx, dy)
        if (!tudoAlcancavel(resultado, m.andar)) return null
        return resultado
    }

    /**
     * Para a dica da mãozinha: um móvel do [andar] e um passo pequeno que cabe e não cai no ladrilho da
     * criança ([crianca]). Prefere os móveis grandes e conhecidos (sofá, mesa, cama). Null se nada cabe.
     */
    fun sugestaoDeArraste(atuais: List<Movel>, andar: Andar, crianca: Pair<Int, Int>): Pair<Movel, Pair<Int, Int>>? {
        val preferidos = listOf("sofa", "mesa", "cama", "tv", "livros", "violao", "planta")
        val candidatos = atuais
            .filter { it.andar == andar && arrastavel(it) }
            .sortedBy { m -> preferidos.indexOf(m.id).let { if (it < 0) 99 else it } }
        for (m in candidatos) for ((dx, dy) in listOf(0 to 1, 1 to 0, -1 to 0, 0 to -1, 0 to 2, 2 to 0)) {
            if (crianca in ladrilhosDe(m.deslocado(dx, dy))) continue
            if (mover(atuais, m.id, dx, dy) != null) return m to (dx to dy)
        }
        return null
    }

    /**
     * Põe cada móvel no lugar salvo. Um lugar que não vale mais (por exemplo, uma peça nova chegou
     * onde ele estava) é ignorado e o móvel volta para o lugar de sempre, que fica livre por construção.
     */
    fun aplicarPosicoes(padrao: List<Movel>, salvas: Map<String, Posicao>): List<Movel> {
        if (salvas.isEmpty()) return padrao
        var atual = padrao
        for ((id, p) in salvas) {
            val m = atual.firstOrNull { it.id == id } ?: continue
            val dx = (p.x - m.x0).roundToInt()
            val dy = (p.y - m.y0).roundToInt()
            if (dx == 0 && dy == 0) continue
            atual = mover(atual, id, dx, dy) ?: atual
        }
        return atual
    }

    /** Móveis de um andar só. */
    fun moveisDe(e: EstadoJogo, andar: Andar): List<Movel> = moveisDe(e).filter { it.andar == andar }

    /** A escada do andar (a que sobe, embaixo; a que desce, em cima). */
    fun escadaDo(andar: Andar): Movel = porId.getValue(if (andar == Andar.DE_BAIXO) "escada" else "escadaDeCima")

    /**
     * Pode passar do ladrilho (ax, ay) para o vizinho (bx, by)? Só bloqueia quando cruza a parede
     * interna fora das passagens. Vizinhos na diagonal são tratados em [Mapa].
     */
    fun atravessa(andar: Andar, ax: Int, ay: Int, bx: Int, by: Int): Boolean {
        val m = meio(andar)
        if (ax != bx && minOf(ax, bx) == m - 1 && maxOf(ax, bx) == m && ay == by) return ay in portasNaParedeInterna
        return true
    }

    /** Pedaços (de um ladrilho) da parede interna do andar, sem as passagens. Para desenhar e ordenar. */
    fun paredesInternas(andar: Andar): List<Caixa> = buildList {
        val e = 0.06f
        val m = meio(andar)
        for (y in 0 until FUNDO) if (y !in portasNaParedeInterna) add(Caixa(m - e, y.toFloat(), m + e, y + 1f, 0f, ALTURA_PAREDE_INTERNA))
    }

    /**
     * A está atrás de B (deve ser desenhada antes)? Vale para caixas que não se atravessam: quem fica
     * inteiro do lado de menor x (ou menor y) está atrás. Se uma está em cima da outra, a de cima vem depois.
     */
    fun atras(a: Caixa, b: Caixa): Boolean {
        val eps = 0.001f
        // Na tela, só importa se as caixas se sobrepõem na horizontal (x - y).
        val sobrepoe = a.x0 - a.y1 < b.x1 - b.y0 - eps && b.x0 - b.y1 < a.x1 - a.y0 - eps
        if (!sobrepoe) return false
        if (a.x1 <= b.x0 + eps || a.y1 <= b.y0 + eps) return true
        if (b.x1 <= a.x0 + eps || b.y1 <= a.y0 + eps) return false
        return a.z1 <= b.z0 + eps
    }

    /**
     * Ordena para desenhar do fundo para a frente (algoritmo do pintor com dependências), feito uma vez só.
     * Empates (e qualquer ciclo) se resolvem pela soma x + y do centro.
     */
    fun ordenarParaDesenho(caixas: List<Caixa>): IntArray {
        val n = caixas.size
        val antesDe = Array(n) { ArrayList<Int>() }
        val grau = IntArray(n)
        for (i in 0 until n) for (j in 0 until n) if (i != j && atras(caixas[i], caixas[j])) {
            antesDe[i].add(j)
            grau[j]++
        }
        fun chave(i: Int) = caixas[i].let { (it.x0 + it.x1 + it.y0 + it.y1) / 2 + it.z0 * 0.01f }
        val feitos = BooleanArray(n)
        val ordem = IntArray(n)
        for (k in 0 until n) {
            var melhor = -1
            for (i in 0 until n) if (!feitos[i] && grau[i] == 0 && (melhor < 0 || chave(i) < chave(melhor))) melhor = i
            if (melhor < 0) for (i in 0 until n) if (!feitos[i] && (melhor < 0 || chave(i) < chave(melhor))) melhor = i
            feitos[melhor] = true
            ordem[k] = melhor
            for (j in antesDe[melhor]) grau[j]--
        }
        return ordem
    }

    /** A criança (um ponto no chão) fica na frente desta caixa? */
    fun criancaNaFrente(x: Float, y: Float, c: Caixa): Boolean = x >= c.x1 || y >= c.y1

    fun caixaDo(m: Movel) = Caixa(m.x0, m.y0, m.x1, m.y1, m.z, m.z + m.altura)

    /**
     * Enquadramento: quanto mede a planta de um andar na tela, em larguras de ladrilho ([largura]) e
     * de cima (alto da parede do fundo) até embaixo (espessura do piso e sombra) ([altura]). [acima] é
     * quanto da altura fica acima do canto (0, 0) do chão. [entreAndares] é a distância vertical entre o canto
     * (0, 0) de um andar e o do outro quando os dois aparecem empilhados, sem se encostarem.
     * [unidadeZ] é a altura de uma unidade z, em ladrilhos (a mesma de ui/componentes/Isometrico.kt).
     */
    data class Enquadramento(val largura: Float, val altura: Float, val acima: Float, val entreAndares: Float)

    fun enquadramento(unidadeZ: Float = 0.56f, espessuraParede: Float = 0.14f, espessuraPiso: Float = 0.35f): Enquadramento {
        val l = LARGURA.toFloat()
        val f = FUNDO.toFloat()
        val largura = (l + f + 2 * espessuraParede) / 2f
        val acima = espessuraParede * 2 / 4f + ALTURA_PAREDE * unidadeZ
        val abaixo = (l + f) / 4f + espessuraPiso * unidadeZ + 0.3f
        // Empilhados: a beirada da frente do piso de cima tem de ficar acima do alto das paredes de baixo, com
        // folga. No mesmo ponto da tela, a beirada (y = FUNDO) e a parede do fundo (y = 0) diferem de FUNDO / 2.
        val entre = f / 2f + espessuraPiso * unidadeZ + ALTURA_PAREDE * unidadeZ + 0.4f
        return Enquadramento(largura, acima + abaixo, acima, entre)
    }

    /**
     * Ladrilho que faz a planta caber em [largura] x [altura] pixels (já sem as margens).
     * Com [doisAndares], os dois andares aparecem empilhados.
     */
    fun ladrilhoQueCabe(largura: Float, altura: Float, doisAndares: Boolean, enq: Enquadramento = enquadramento()): Float {
        val alturaTotal = enq.altura + if (doisAndares) enq.entreAndares else 0f
        return minOf(largura / enq.largura, altura / alturaTotal).coerceAtLeast(1f)
    }

    /**
     * Mostra os dois andares empilhados quando cabem quase do mesmo tamanho que um andar sozinho; senão
     * (painel aberto ou tela baixa), mostra só o andar onde a criança está, bem grande.
     */
    fun mostrarDoisAndares(largura: Float, altura: Float, enq: Enquadramento = enquadramento()): Boolean =
        ladrilhoQueCabe(largura, altura, true, enq) >= 0.85f * ladrilhoQueCabe(largura, altura, false, enq)
}

/**
 * Mapa de passagem pré-calculado para um conjunto de móveis: quais ladrilhos estão livres
 * e como chegar de um ponto a outro sem atravessar paredes nem móveis.
 */
class Mapa(moveis: List<Movel>, val andar: Andar) {
    private val n = PlantaDaCasa.LARGURA
    private val f = PlantaDaCasa.FUNDO
    private val bloqueado = BooleanArray(n * f)

    init {
        val margem = 0.05f
        for (m in moveis) if (m.bloqueia && m.andar == andar) {
            for (cy in 0 until f) for (cx in 0 until n) {
                if (m.x0 < cx + 1 - margem && m.x1 > cx + margem && m.y0 < cy + 1 - margem && m.y1 > cy + margem) bloqueado[cy * n + cx] = true
            }
        }
    }

    fun livre(cx: Int, cy: Int): Boolean = cx in 0 until n && cy in 0 until f && !bloqueado[cy * n + cx]

    private fun celula(v: Float) = floor(v).toInt().coerceIn(0, n - 1)
    private fun celulaY(v: Float) = floor(v).toInt().coerceIn(0, f - 1)
    private fun atravessa(ax: Int, ay: Int, bx: Int, by: Int) = PlantaDaCasa.atravessa(andar, ax, ay, bx, by)

    /** Dá para ir de um ladrilho ao vizinho (inclusive na diagonal, sem cortar quina de parede ou móvel)? */
    fun passa(ax: Int, ay: Int, bx: Int, by: Int): Boolean {
        if (!livre(bx, by)) return false
        if (ax == bx || ay == by) return atravessa(ax, ay, bx, by)
        // Diagonal: os dois caminhos em L precisam estar livres.
        return livre(bx, ay) && livre(ax, by) &&
            atravessa(ax, ay, bx, ay) && atravessa(bx, ay, bx, by) &&
            atravessa(ax, ay, ax, by) && atravessa(ax, by, bx, by)
    }

    /** O ladrilho livre mais perto (sem atravessar paredes), começando por (cx, cy). */
    fun livreMaisPerto(cx: Int, cy: Int): Pair<Int, Int> {
        val inicio = cy.coerceIn(0, f - 1) * n + cx.coerceIn(0, n - 1)
        val visto = BooleanArray(n * f)
        val fila = IntArray(n * f)
        var ini = 0
        var fim = 0
        fila[fim++] = inicio
        visto[inicio] = true
        while (ini < fim) {
            val c = fila[ini++]
            val x = c % n
            val y = c / n
            if (!bloqueado[c]) return x to y
            for ((dx, dy) in VIZINHOS4) {
                val vx = x + dx
                val vy = y + dy
                if (vx !in 0 until n || vy !in 0 until f) continue
                val v = vy * n + vx
                if (!visto[v] && atravessa(x, y, vx, vy)) {
                    visto[v] = true
                    fila[fim++] = v
                }
            }
        }
        return x0y0Livre()
    }

    private fun x0y0Livre(): Pair<Int, Int> {
        for (i in 0 until n * f) if (!bloqueado[i]) return (i % n) to (i / n)
        return 0 to 0
    }

    /**
     * Caminho de [de] até [para] (em ladrilhos), sem o ponto de partida. Se o destino está ocupado,
     * vai até o ladrilho livre mais perto. Os pontos intermediários são "puxados" em linha reta quando
     * nada atrapalha, para a criança não andar em zigue-zague. Lista vazia: já está lá ou não há caminho.
     */
    fun caminho(de: Posicao, para: Posicao): List<Posicao> {
        val sx = celula(de.x)
        val sy = celulaY(de.y)
        var tx = celula(para.x)
        var ty = celulaY(para.y)
        var destino = Posicao(para.x.coerceIn(tx + MARGEM, tx + 1 - MARGEM), para.y.coerceIn(ty + MARGEM, ty + 1 - MARGEM))
        if (!livre(tx, ty)) {
            val (lx, ly) = livreMaisPerto(tx, ty)
            tx = lx
            ty = ly
            destino = Posicao(tx + 0.5f, ty + 0.5f)
        }
        if (sx == tx && sy == ty) {
            return if (hypot(destino.x - de.x, destino.y - de.y) < 0.01f) emptyList() else listOf(destino)
        }
        // Busca em largura (8 vizinhos; diagonal custa o mesmo, mas o caminho é alisado depois).
        val total = n * f
        val anterior = IntArray(total) { -1 }
        val fila = IntArray(total)
        var ini = 0
        var fim = 0
        val inicio = sy * n + sx
        fila[fim++] = inicio
        anterior[inicio] = inicio
        val alvo = ty * n + tx
        while (ini < fim) {
            val c = fila[ini++]
            if (c == alvo) break
            val x = c % n
            val y = c / n
            for ((dx, dy) in VIZINHOS8) {
                val vx = x + dx
                val vy = y + dy
                if (vx !in 0 until n || vy !in 0 until f) continue
                val v = vy * n + vx
                if (anterior[v] < 0 && passa(x, y, vx, vy)) {
                    anterior[v] = c
                    fila[fim++] = v
                }
            }
        }
        if (anterior[alvo] < 0) return emptyList()
        val celulas = ArrayList<Posicao>()
        var c = alvo
        while (c != inicio) {
            celulas.add(Posicao(c % n + 0.5f, c / n + 0.5f))
            c = anterior[c]
        }
        celulas.reverse()
        celulas[celulas.size - 1] = destino
        // Alisar: de cada ponto, pula direto para o ponto mais distante que se enxerga em linha reta.
        val liso = ArrayList<Posicao>()
        var atual = de
        var i = 0
        while (i < celulas.size) {
            var j = celulas.size - 1
            while (j > i && !linhaLivre(atual, celulas[j])) j--
            liso.add(celulas[j])
            atual = celulas[j]
            i = j + 1
        }
        return liso
    }

    /** Uma linha reta de [a] a [b] passa só por ladrilhos livres, sem atravessar paredes ou cortar quinas? */
    fun linhaLivre(a: Posicao, b: Posicao): Boolean {
        val dist = hypot(b.x - a.x, b.y - a.y)
        val passos = (dist / 0.05f).toInt().coerceAtLeast(1)
        var cx = celula(a.x)
        var cy = celulaY(a.y)
        for (k in 1..passos) {
            val t = k.toFloat() / passos
            val px = a.x + (b.x - a.x) * t
            val py = a.y + (b.y - a.y) * t
            // O corpo da criança tem um pouco de largura: verifica também os lados.
            for ((ox, oy) in LARGURA) {
                val qx = celula(px + ox)
                val qy = celulaY(py + oy)
                if (!livre(qx, qy) && !(qx == celula(a.x) && qy == celulaY(a.y))) return false
            }
            val nx = celula(px)
            val ny = celulaY(py)
            if (nx != cx || ny != cy) {
                if (abs(nx - cx) > 1 || abs(ny - cy) > 1 || !passa(cx, cy, nx, ny)) return false
                cx = nx
                cy = ny
            }
        }
        return true
    }

    /** Um ladrilho livre que encosta no móvel (para a criança ir até ele quando é tocado). */
    fun pertoDo(m: Movel): Posicao {
        val (x, y) = livreMaisPerto(floor(m.centroX).toInt(), floor(m.centroY).toInt())
        return Posicao(x + 0.5f, y + 0.5f)
    }

    /** Todos os ladrilhos livres que dá para alcançar a partir de [de] (para testes e para sortear passeios). */
    fun alcancaveis(de: Posicao): Set<Pair<Int, Int>> {
        val visto = HashSet<Pair<Int, Int>>()
        val fila = ArrayDeque<Pair<Int, Int>>()
        val ini = celula(de.x) to celulaY(de.y)
        fila.add(ini)
        visto.add(ini)
        while (fila.isNotEmpty()) {
            val (x, y) = fila.removeFirst()
            for ((dx, dy) in VIZINHOS8) {
                val v = (x + dx) to (y + dy)
                if (v !in visto && v.first in 0 until n && v.second in 0 until f && passa(x, y, v.first, v.second)) {
                    visto.add(v)
                    fila.add(v)
                }
            }
        }
        return visto
    }

    private companion object {
        const val MARGEM = 0.3f
        val VIZINHOS4 = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        val VIZINHOS8 = VIZINHOS4 + listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
        val LARGURA = listOf(0f to 0f, 0.18f to 0f, -0.18f to 0f, 0f to 0.18f, 0f to -0.18f)
    }
}

/**
 * Guarda o lugar novo de um móvel arrastado (o canto (x0, y0) da pegada). Voltar ao lugar de sempre
 * apaga o registro, para o save ficar pequeno e saves antigos (sem nada) continuarem iguais.
 */
fun EstadoJogo.comMovelEm(id: String, x0: Float, y0: Float): EstadoJogo {
    val padrao = PlantaDaCasa.porId[id] ?: return this
    return if (abs(x0 - padrao.x0) < 0.01f && abs(y0 - padrao.y0) < 0.01f) {
        copy(moveisDentro = moveisDentro - id)
    } else {
        copy(moveisDentro = moveisDentro + (id to Posicao(x0, y0)))
    }
}
