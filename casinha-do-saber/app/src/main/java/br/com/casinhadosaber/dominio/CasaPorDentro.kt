package br.com.casinhadosaber.dominio

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot

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
enum class Acao { NENHUMA, GELADEIRA, GUARDA_ROUPA, TORNEIRA, TV, FOGAO, BANHEIRA, CAMA, ESPELHO, DESCARGA, ESCADA }

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
        daPeca("sofa", Comodo.SALA, 1.25f, 0.1f, 3.0f, 0.98f, 0.88f),
        daPeca("gato", Comodo.SALA, 1.85f, 0.35f, 2.35f, 0.85f, 0.45f, z = 0.5f, bloqueia = false),
        daPeca("livros", Comodo.SALA, 3.1f, 0.12f, 3.55f, 0.57f, 0.5f),
        daPeca("tv", Comodo.SALA, 3.7f, 0.1f, 4.85f, 0.6f, 1.3f, acao = Acao.TV),
        Movel("tapete", "Tapete", "rug", Comodo.SALA, 1.5f, 1.45f, 3.9f, 3.45f, 0f, bloqueia = false, noChao = true),
        // Cozinha: a mesa com o bolo, o fogão e a geladeira (no canto, encostada na parede da cozinha).
        daPeca("mesa", Comodo.COZINHA, 5.6f, 2.0f, 6.7f, 2.98f, 0.72f),
        daPeca("bolo", Comodo.COZINHA, 5.9f, 2.25f, 6.4f, 2.75f, 0.45f, z = 0.72f, bloqueia = false),
        daPeca("fogao", Comodo.COZINHA, 5.95f, 0.1f, 6.8f, 0.92f, 0.85f, acao = Acao.FOGAO),
        daPeca("geladeira", Comodo.COZINHA, 6.98f, 0.1f, 7.9f, 0.95f, 2.0f, acao = Acao.GELADEIRA),
        // Andar de cima. Quarto: a escada que desce, o quadro, a cama, o criado-mudo com o abajur e o violão.
        Movel("escadaDeCima", "Escada", "stairs", Comodo.QUARTO, 0.08f, 0.08f, 0.95f, 2.6f, 0.75f, acao = Acao.ESCADA),
        daPeca("quadro", Comodo.QUARTO, 1.45f, 0f, 2.3f, 0.04f, 0.65f, z = 1.5f, bloqueia = false, naParede = true),
        daPeca("cama", Comodo.QUARTO, 1.25f, 0.1f, 2.5f, 1.98f, 0.55f, acao = Acao.CAMA),
        daPeca("ursinho", Comodo.QUARTO, 1.6f, 1.2f, 2.15f, 1.7f, 0.5f, z = 0.55f, bloqueia = false),
        Movel("criado", "Criado-mudo", "nightstand", Comodo.QUARTO, 2.65f, 0.1f, 3.2f, 0.65f, 0.5f),
        daPeca("abajur", Comodo.QUARTO, 2.7f, 0.13f, 3.15f, 0.62f, 0.55f, z = 0.5f, bloqueia = false),
        daPeca("violao", Comodo.QUARTO, 3.32f, 0.12f, 3.8f, 0.6f, 0.9f),
        Movel("guardaroupa", "Guarda-roupa", "wardrobe", Comodo.QUARTO, 0.1f, 3.0f, 0.72f, 4.85f, 2.2f, frente = Frente.X, acao = Acao.GUARDA_ROUPA),
        daPeca("planta", Comodo.QUARTO, 3.3f, 4.3f, 3.85f, 4.85f, 0.7f),
        // Banheiro: pia com o espelho em cima, vaso e banheira (embaixo da janela redonda).
        Movel("pia", "Pia", "sink", Comodo.BANHEIRO, 4.3f, 0.1f, 5.25f, 0.75f, 0.9f, acao = Acao.TORNEIRA),
        daPeca("espelho", Comodo.BANHEIRO, 4.4f, 0f, 5.15f, 0.04f, 0.8f, z = 1.3f, acao = Acao.ESPELHO, bloqueia = false, naParede = true),
        Movel("vaso", "Vaso sanitário", "toilet", Comodo.BANHEIRO, 5.45f, 0.1f, 6.05f, 0.7f, 0.7f, acao = Acao.DESCARGA),
        daPeca("banheira", Comodo.BANHEIRO, 6.25f, 0.1f, 7.9f, 1.35f, 0.6f, acao = Acao.BANHEIRA),
    )

    val porId: Map<String, Movel> = moveis.associateBy { it.id }

    /** As peças da casa que moram dentro e aparecem aqui (as outras ficam no jardim ou no céu). */
    val pecasDeDentro: Set<String> = moveis.mapNotNull { it.itemId }.toSet()

    /** Móveis que aparecem para este estado: os fixos e as peças já ganhas. */
    fun moveisDe(e: EstadoJogo): List<Movel> {
        val ganhos = e.itensGanhos.mapTo(HashSet()) { it.id }
        return moveis.filter { it.itemId == null || it.itemId in ganhos }
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
