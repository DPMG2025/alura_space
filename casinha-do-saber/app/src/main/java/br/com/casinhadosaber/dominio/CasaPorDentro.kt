package br.com.casinhadosaber.dominio

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot

/**
 * A casa por dentro, vista de cima em perspectiva isométrica (2,5D). Aqui fica só a "planta":
 * ladrilhos, paredes, portas, onde cada móvel mora, por onde a criança pode andar e em que ordem
 * desenhar as coisas. Kotlin puro, testável sem Android. O desenho fica em ui/telas/TelaCasaPorDentro.kt.
 *
 * Coordenadas em ladrilhos: x cresce para baixo e para a direita na tela, y cresce para baixo e para a
 * esquerda. A planta tem [LADO] x [LADO] ladrilhos, dividida em 4 cômodos por duas paredes internas
 * (em x = [MEIO] e em y = [MEIO]), cada uma com duas passagens (portas).
 *
 *            (0,0)
 *        QUARTO   BANHEIRO
 *        SALA     COZINHA
 *            (8,8)
 */

/** Para que lado fica a frente do móvel (o lado que a câmera enxerga). */
enum class Frente { X, Y }

/** O que acontece ao tocar no móvel. */
enum class Acao { NENHUMA, GELADEIRA, GUARDA_ROUPA, TORNEIRA, TV, FOGAO, BANHEIRA, CAMA, ESPELHO, DESCARGA }

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
}

/** Algo que precisa de ordem de desenho: um móvel, um pedaço de parede interna, etc. (caixa no chão). */
data class Caixa(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val z0: Float = 0f, val z1: Float = 1f)

object PlantaDaCasa {
    const val LADO = 8
    const val MEIO = 4

    /** Altura das paredes do fundo e das paredes internas (cortadas baixinhas, para ver dentro). */
    const val ALTURA_PAREDE = 3.0f
    const val ALTURA_PAREDE_INTERNA = 0.45f

    /** Na parede interna x = MEIO, as passagens ficam nas linhas y destes ladrilhos. */
    val portasNaParedeX = setOf(1, 6)

    /** Na parede interna y = MEIO, as passagens ficam nas colunas x destes ladrilhos. */
    val portasNaParedeY = setOf(2, 6)

    /** Onde a criança aparece quando entra (na sala, perto da porta da rua). */
    val entrada = Posicao(2.5f, 6.5f)

    /** Ponto das lâmpadas penduradas no meio de cada cômodo. */
    fun centroDo(c: Comodo): Posicao = when (c) {
        Comodo.QUARTO -> Posicao(2f, 2f)
        Comodo.BANHEIRO -> Posicao(6f, 2f)
        Comodo.SALA -> Posicao(2f, 6f)
        Comodo.COZINHA -> Posicao(6f, 6f)
    }

    fun limitesDo(c: Comodo): Caixa = when (c) {
        Comodo.QUARTO -> Caixa(0f, 0f, MEIO.toFloat(), MEIO.toFloat())
        Comodo.BANHEIRO -> Caixa(MEIO.toFloat(), 0f, LADO.toFloat(), MEIO.toFloat())
        Comodo.SALA -> Caixa(0f, MEIO.toFloat(), MEIO.toFloat(), LADO.toFloat())
        Comodo.COZINHA -> Caixa(MEIO.toFloat(), MEIO.toFloat(), LADO.toFloat(), LADO.toFloat())
    }

    fun comodoEm(x: Float, y: Float): Comodo = when {
        x < MEIO && y < MEIO -> Comodo.QUARTO
        y < MEIO -> Comodo.BANHEIRO
        x < MEIO -> Comodo.SALA
        else -> Comodo.COZINHA
    }

    private fun item(id: String) = ItensDaCasa.porId.getValue(id)

    private fun daPeca(
        id: String, comodo: Comodo, x0: Float, y0: Float, x1: Float, y1: Float, altura: Float,
        z: Float = 0f, frente: Frente = Frente.Y, acao: Acao = Acao.NENHUMA, bloqueia: Boolean = true, naParede: Boolean = false,
    ): Movel {
        val i = item(id)
        return Movel(id, i.nome, i.nomeEn, comodo, x0, y0, x1, y1, altura, z, frente, acao, bloqueia, naParede, false, id)
    }

    /** Todos os móveis possíveis. As peças ganhas aparecem no cômodo certo; os fixos estão sempre lá. */
    val moveis: List<Movel> = listOf(
        // Quarto
        daPeca("cama", Comodo.QUARTO, 0.12f, 0.12f, 1.3f, 2.1f, 0.55f, acao = Acao.CAMA),
        Movel("criado", "Criado-mudo", "nightstand", Comodo.QUARTO, 1.45f, 0.12f, 2.02f, 0.7f, 0.5f),
        daPeca("abajur", Comodo.QUARTO, 1.5f, 0.15f, 1.97f, 0.65f, 0.55f, z = 0.5f, bloqueia = false),
        daPeca("ursinho", Comodo.QUARTO, 0.45f, 1.3f, 1.0f, 1.8f, 0.5f, z = 0.55f, bloqueia = false),
        Movel("guardaroupa", "Guarda-roupa", "wardrobe", Comodo.QUARTO, 2.4f, 0.1f, 3.88f, 0.7f, 2.2f, acao = Acao.GUARDA_ROUPA),
        daPeca("violao", Comodo.QUARTO, 0.1f, 3.0f, 0.55f, 3.6f, 0.9f),
        // Banheiro
        Movel("pia", "Pia", "sink", Comodo.BANHEIRO, 4.3f, 0.1f, 5.3f, 0.75f, 0.9f, acao = Acao.TORNEIRA),
        daPeca("espelho", Comodo.BANHEIRO, 4.4f, 0f, 5.2f, 0.04f, 0.8f, z = 1.3f, acao = Acao.ESPELHO, bloqueia = false, naParede = true),
        daPeca("banheira", Comodo.BANHEIRO, 6.0f, 0.12f, 7.88f, 1.35f, 0.6f, acao = Acao.BANHEIRA),
        Movel("vaso", "Vaso sanitário", "toilet", Comodo.BANHEIRO, 7.2f, 2.8f, 7.88f, 3.5f, 0.7f, frente = Frente.X, acao = Acao.DESCARGA),
        // Sala (o gatinho dorme em cima do sofá)
        daPeca("tv", Comodo.SALA, 0.4f, 4.1f, 1.6f, 4.6f, 1.3f, acao = Acao.TV),
        daPeca("sofa", Comodo.SALA, 0.1f, 5.4f, 1.0f, 7.6f, 0.75f, frente = Frente.X),
        daPeca("quadro", Comodo.SALA, 0f, 5.6f, 0.04f, 7.0f, 0.7f, z = 1.45f, frente = Frente.X, bloqueia = false, naParede = true),
        daPeca("livros", Comodo.SALA, 1.2f, 7.3f, 1.75f, 7.85f, 0.5f),
        daPeca("planta", Comodo.SALA, 3.3f, 7.3f, 3.85f, 7.85f, 0.7f),
        daPeca("gato", Comodo.SALA, 0.3f, 6.5f, 0.8f, 7.0f, 0.45f, z = 0.75f, bloqueia = false),
        Movel("tapete", "Tapete", "rug", Comodo.SALA, 1.3f, 5.0f, 3.4f, 7.0f, 0f, bloqueia = false, noChao = true),
        // Cozinha
        daPeca("geladeira", Comodo.COZINHA, 4.1f, 4.1f, 4.95f, 4.95f, 2.0f, frente = Frente.X, acao = Acao.GELADEIRA),
        daPeca("fogao", Comodo.COZINHA, 4.1f, 5.05f, 4.95f, 5.9f, 0.85f, frente = Frente.X, acao = Acao.FOGAO),
        daPeca("mesa", Comodo.COZINHA, 6.0f, 6.0f, 7.3f, 7.3f, 0.72f),
        daPeca("bolo", Comodo.COZINHA, 6.4f, 6.4f, 6.9f, 6.9f, 0.45f, z = 0.72f, bloqueia = false),
    )

    val porId: Map<String, Movel> = moveis.associateBy { it.id }

    /** As peças da casa que moram dentro e aparecem aqui (as outras ficam no jardim ou no céu). */
    val pecasDeDentro: Set<String> = moveis.mapNotNull { it.itemId }.toSet()

    /** Móveis que aparecem para este estado: os fixos e as peças já ganhas. */
    fun moveisDe(e: EstadoJogo): List<Movel> {
        val ganhos = e.itensGanhos.mapTo(HashSet()) { it.id }
        return moveis.filter { it.itemId == null || it.itemId in ganhos }
    }

    /**
     * Pode passar do ladrilho (ax, ay) para o vizinho (bx, by)? Só bloqueia quando cruza uma parede
     * interna fora das portas. Vizinhos na diagonal são tratados em [Mapa].
     */
    fun atravessa(ax: Int, ay: Int, bx: Int, by: Int): Boolean {
        if (ax != bx && minOf(ax, bx) == MEIO - 1 && maxOf(ax, bx) == MEIO && ay == by) return ay in portasNaParedeX
        if (ay != by && minOf(ay, by) == MEIO - 1 && maxOf(ay, by) == MEIO && ax == bx) return ax in portasNaParedeY
        return true
    }

    /** Pedaços (de um ladrilho) das paredes internas, sem as portas. Para desenhar e ordenar. */
    val paredesInternas: List<Caixa> = buildList {
        val e = 0.06f
        for (y in 0 until LADO) if (y !in portasNaParedeX) add(Caixa(MEIO - e, y.toFloat(), MEIO + e, y + 1f, 0f, ALTURA_PAREDE_INTERNA))
        for (x in 0 until LADO) if (x !in portasNaParedeY) add(Caixa(x.toFloat(), MEIO - e, x + 1f, MEIO + e, 0f, ALTURA_PAREDE_INTERNA))
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
}

/**
 * Mapa de passagem pré-calculado para um conjunto de móveis: quais ladrilhos estão livres
 * e como chegar de um ponto a outro sem atravessar paredes nem móveis.
 */
class Mapa(moveis: List<Movel>) {
    private val n = PlantaDaCasa.LADO
    private val bloqueado = BooleanArray(n * n)

    init {
        val margem = 0.05f
        for (m in moveis) if (m.bloqueia) {
            for (cy in 0 until n) for (cx in 0 until n) {
                if (m.x0 < cx + 1 - margem && m.x1 > cx + margem && m.y0 < cy + 1 - margem && m.y1 > cy + margem) bloqueado[cy * n + cx] = true
            }
        }
    }

    fun livre(cx: Int, cy: Int): Boolean = cx in 0 until n && cy in 0 until n && !bloqueado[cy * n + cx]

    private fun celula(v: Float) = floor(v).toInt().coerceIn(0, n - 1)

    /** Dá para ir de um ladrilho ao vizinho (inclusive na diagonal, sem cortar quina de parede ou móvel)? */
    fun passa(ax: Int, ay: Int, bx: Int, by: Int): Boolean {
        if (!livre(bx, by)) return false
        if (ax == bx || ay == by) return PlantaDaCasa.atravessa(ax, ay, bx, by)
        // Diagonal: os dois caminhos em L precisam estar livres.
        return livre(bx, ay) && livre(ax, by) &&
            PlantaDaCasa.atravessa(ax, ay, bx, ay) && PlantaDaCasa.atravessa(bx, ay, bx, by) &&
            PlantaDaCasa.atravessa(ax, ay, ax, by) && PlantaDaCasa.atravessa(ax, by, bx, by)
    }

    /** O ladrilho livre mais perto (sem atravessar paredes), começando por (cx, cy). */
    fun livreMaisPerto(cx: Int, cy: Int): Pair<Int, Int> {
        val inicio = cy.coerceIn(0, n - 1) * n + cx.coerceIn(0, n - 1)
        val visto = BooleanArray(n * n)
        val fila = IntArray(n * n)
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
                if (vx !in 0 until n || vy !in 0 until n) continue
                val v = vy * n + vx
                if (!visto[v] && PlantaDaCasa.atravessa(x, y, vx, vy)) {
                    visto[v] = true
                    fila[fim++] = v
                }
            }
        }
        return x0y0Livre()
    }

    private fun x0y0Livre(): Pair<Int, Int> {
        for (i in 0 until n * n) if (!bloqueado[i]) return (i % n) to (i / n)
        return 0 to 0
    }

    /**
     * Caminho de [de] até [para] (em ladrilhos), sem o ponto de partida. Se o destino está ocupado,
     * vai até o ladrilho livre mais perto. Os pontos intermediários são "puxados" em linha reta quando
     * nada atrapalha, para a criança não andar em zigue-zague. Lista vazia: já está lá ou não há caminho.
     */
    fun caminho(de: Posicao, para: Posicao): List<Posicao> {
        val sx = celula(de.x)
        val sy = celula(de.y)
        var tx = celula(para.x)
        var ty = celula(para.y)
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
        val total = n * n
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
                if (vx !in 0 until n || vy !in 0 until n) continue
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
        var cy = celula(a.y)
        for (k in 1..passos) {
            val t = k.toFloat() / passos
            val px = a.x + (b.x - a.x) * t
            val py = a.y + (b.y - a.y) * t
            // O corpo da criança tem um pouco de largura: verifica também os lados.
            for ((ox, oy) in LARGURA) {
                val qx = celula(px + ox)
                val qy = celula(py + oy)
                if (!livre(qx, qy) && !(qx == celula(a.x) && qy == celula(a.y))) return false
            }
            val nx = celula(px)
            val ny = celula(py)
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
        val ini = celula(de.x) to celula(de.y)
        fila.add(ini)
        visto.add(ini)
        while (fila.isNotEmpty()) {
            val (x, y) = fila.removeFirst()
            for ((dx, dy) in VIZINHOS8) {
                val v = (x + dx) to (y + dy)
                if (v !in visto && v.first in 0 until n && v.second in 0 until n && passa(x, y, v.first, v.second)) {
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
