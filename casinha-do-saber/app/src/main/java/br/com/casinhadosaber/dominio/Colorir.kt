package br.com.casinhadosaber.dominio

/**
 * Livro de Colorir (Fase 10): desenhos de contorno feitos de regiões (formas num quadrado de 0 a 1) que a criança
 * pinta com o balde ou com o pincel. Diferente do ateliê (quadradinhos livres): aqui o desenho já vem pronto.
 * Nada aqui depende do Android, então tudo é testado na JVM. Sem tempo, sem nota baixa, sem perder nada.
 */

/** Um ponto no quadrado do desenho (x e y de 0 a 1). */
data class Ponto(val x: Float, val y: Float)

/** Forma fechada de uma região. [contem] diz se um toque cai dentro dela. */
sealed interface Forma {
    fun contem(x: Float, y: Float): Boolean

    data class Elipse(val cx: Float, val cy: Float, val rx: Float, val ry: Float) : Forma {
        override fun contem(x: Float, y: Float): Boolean {
            if (rx <= 0f || ry <= 0f) return false
            val dx = (x - cx) / rx
            val dy = (y - cy) / ry
            return dx * dx + dy * dy <= 1f
        }
    }

    /** Polígono fechado (o último ponto liga ao primeiro). Teste do raio, vale para formas côncavas. */
    data class Poligono(val pontos: List<Ponto>) : Forma {
        override fun contem(x: Float, y: Float): Boolean {
            var dentro = false
            var j = pontos.size - 1
            for (i in pontos.indices) {
                val a = pontos[i]
                val b = pontos[j]
                if ((a.y > y) != (b.y > y) && x < (b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x) dentro = !dentro
                j = i
            }
            return dentro
        }
    }
}

/** Uma região pintável. Quanto mais para o fim da lista, mais "por cima" ela fica. */
data class Regiao(val forma: Forma, val nome: String = "")

data class Desenho(
    val id: String,
    val titulo: String,
    val colecao: String,
    val emoji: String,
    val regioes: List<Regiao>,
    /** Cores da versão bonita (índice de [Tintas.todas] por região). Serve de modelo e de imagem do quebra-cabeça. */
    val coresPadrao: List<Int>,
) {
    init {
        require(regioes.isNotEmpty()) { "Desenho $id sem regiões" }
        require(coresPadrao.size == regioes.size) { "Desenho $id: ${coresPadrao.size} cores para ${regioes.size} regiões" }
        require(coresPadrao.all { it in 1 until Tintas.todas.size }) { "Desenho $id usa tinta que não existe" }
    }

    /** A região que fica por cima no ponto (o toque), ou -1 se não há nenhuma. */
    fun regiaoEm(x: Float, y: Float): Int {
        for (i in regioes.indices.reversed()) if (regioes[i].forma.contem(x, y)) return i
        return -1
    }

    /** Dificuldade pelo número de regiões: 1 (poucas e grandes) a 3 (muitas). */
    val dificuldade: Int get() = when {
        regioes.size <= 8 -> 1
        regioes.size <= 14 -> 2
        else -> 3
    }
}

enum class TipoDeTinta { LISA, METAL, BRILHANTE, ARCO_IRIS }

/** Uma cor do estojo. [liberadaCom] = quantos desenhos terminados abrem a cor (0 = já vem no estojo). */
data class Tinta(val id: Int, val nome: String, val argb: Long, val tipo: TipoDeTinta, val liberadaCom: Int)

object Tintas {
    /** O índice 0 é o papel em branco (região ainda sem tinta). */
    val todas: List<Tinta> = listOf(
        Tinta(0, "branco", 0xFFFFFFFF, TipoDeTinta.LISA, 0),
        Tinta(1, "vermelho", 0xFFEF476F, TipoDeTinta.LISA, 0),
        Tinta(2, "laranja", 0xFFFF9F1C, TipoDeTinta.LISA, 0),
        Tinta(3, "amarelo", 0xFFFFD166, TipoDeTinta.LISA, 0),
        Tinta(4, "verde", 0xFF3DBE6B, TipoDeTinta.LISA, 0),
        Tinta(5, "verde água", 0xFF2EC4B6, TipoDeTinta.LISA, 0),
        Tinta(6, "azul claro", 0xFF8ED1FF, TipoDeTinta.LISA, 0),
        Tinta(7, "azul", 0xFF4D96FF, TipoDeTinta.LISA, 0),
        Tinta(8, "roxo", 0xFF9B6BFF, TipoDeTinta.LISA, 0),
        Tinta(9, "rosa", 0xFFFF6FAE, TipoDeTinta.LISA, 0),
        Tinta(10, "marrom", 0xFF9C6B3F, TipoDeTinta.LISA, 0),
        Tinta(11, "cinza", 0xFFB5B5C6, TipoDeTinta.LISA, 0),
        Tinta(12, "preto", 0xFF3D2C5E, TipoDeTinta.LISA, 0),
        Tinta(13, "dourado", 0xFFE8B923, TipoDeTinta.METAL, 1),
        Tinta(14, "prata", 0xFFC9D2DC, TipoDeTinta.METAL, 2),
        Tinta(15, "arco-íris", 0xFFFF6FAE, TipoDeTinta.ARCO_IRIS, 3),
        Tinta(16, "brilho rosa", 0xFFFF8FC4, TipoDeTinta.BRILHANTE, 5),
        Tinta(17, "brilho azul", 0xFF6FB8FF, TipoDeTinta.BRILHANTE, 8),
        Tinta(18, "brilho verde", 0xFF6FE3A0, TipoDeTinta.BRILHANTE, 12),
        Tinta(19, "brilho roxo", 0xFFB894FF, TipoDeTinta.BRILHANTE, 18),
        Tinta(20, "cobre", 0xFFD9824B, TipoDeTinta.METAL, 25),
    )

    const val PRIMEIRA_ESPECIAL = 13

    fun especial(id: Int): Boolean = id >= PRIMEIRA_ESPECIAL

    /** Tintas que a criança pode usar depois de [terminados] desenhos terminados. */
    fun liberadas(terminados: Int): List<Tinta> = todas.drop(1).filter { terminados >= it.liberadaCom }

    /** Tinta que passa a abrir exatamente com [terminados] desenhos (para avisar "ganhou uma cor nova"). */
    fun novaCom(terminados: Int): Tinta? = todas.drop(1).firstOrNull { it.liberadaCom == terminados && it.liberadaCom > 0 }
}

/** Um traço de pincel. [regiao] = onde o dedo começou (-1 = pincel livre, sem prender). Pontos no quadrado 0..1. */
data class Traco(val regiao: Int, val tinta: Int, val largura: Float, val pontos: List<Ponto>)

/**
 * O que a criança fez num desenho: a tinta de cada região (balde) e os traços de pincel. Imutável: cada toque
 * devolve uma obra nova, o que dá desfazer e refazer de graça.
 */
data class Obra(val desenhoId: String, val tintas: List<Int>, val tracos: List<Traco> = emptyList()) {
    /** Regiões já pintadas com o balde ou cobertas de pincel. */
    fun pintadas(): Int = tintas.indices.count { tintas[it] != 0 }

    fun vazia(): Boolean = tintas.all { it == 0 } && tracos.isEmpty()

    /** Cores diferentes usadas (balde e pincel). */
    fun coresUsadas(): Set<Int> = (tintas.filter { it != 0 } + tracos.map { it.tinta }).toSet()
}

object Colorir {
    const val LARGURAS_DO_PINCEL = 3
    const val MAXIMO_DE_TRACOS = 80
    const val MAXIMO_DE_PONTOS = 48

    fun obraVazia(d: Desenho) = Obra(d.id, List(d.regioes.size) { 0 })

    /** A obra com as cores da versão bonita (modelo). */
    fun obraModelo(d: Desenho) = Obra(d.id, d.coresPadrao)

    /** Balde: enche a região tocada com a tinta. Toque fora de qualquer região ou tinta inexistente não muda nada. */
    fun balde(d: Desenho, o: Obra, x: Float, y: Float, tinta: Int): Obra {
        if (tinta !in 1 until Tintas.todas.size) return o
        val r = d.regiaoEm(x, y)
        if (r < 0 || o.tintas[r] == tinta) return o
        // O balde tapa o pincel que estava nessa região, como tinta nova sobre o papel.
        return o.copy(tintas = o.tintas.toMutableList().also { it[r] = tinta }, tracos = o.tracos.filter { it.regiao != r })
    }

    /** Borracha: limpa a região tocada (tinta e pincel). */
    fun borracha(d: Desenho, o: Obra, x: Float, y: Float): Obra {
        val r = d.regiaoEm(x, y)
        if (r < 0) return o
        if (o.tintas[r] == 0 && o.tracos.none { it.regiao == r }) return o
        return o.copy(tintas = o.tintas.toMutableList().also { it[r] = 0 }, tracos = o.tracos.filter { it.regiao != r })
    }

    /**
     * Fecha um traço de pincel. Com [dentroDaLinha], o traço fica preso à região onde o dedo começou (o que cai
     * fora dela nem é desenhado). Sem ele, é livre. Traço começado fora de região, com a trava ligada, é ignorado.
     */
    fun pincel(d: Desenho, o: Obra, pontos: List<Ponto>, tinta: Int, largura: Float, dentroDaLinha: Boolean): Obra {
        if (pontos.isEmpty() || tinta !in 1 until Tintas.todas.size) return o
        val regiao = if (dentroDaLinha) d.regiaoEm(pontos.first().x, pontos.first().y) else -1
        if (dentroDaLinha && regiao < 0) return o
        val enxuto = reduzir(pontos)
        val novo = Traco(regiao, tinta, largura, enxuto)
        return o.copy(tracos = (o.tracos + novo).takeLast(MAXIMO_DE_TRACOS))
    }

    /** Guarda no máximo [MAXIMO_DE_PONTOS] pontos, sempre com o primeiro e o último. */
    fun reduzir(pontos: List<Ponto>): List<Ponto> {
        if (pontos.size <= MAXIMO_DE_PONTOS) return pontos
        val passo = (pontos.size - 1).toFloat() / (MAXIMO_DE_PONTOS - 1)
        return List(MAXIMO_DE_PONTOS) { pontos[kotlin.math.round(it * passo).toInt().coerceIn(0, pontos.size - 1)] }
    }

    /** Próxima região a pintar (a dica): a primeira ainda sem tinta, da maior para a menor não, na ordem do desenho. */
    fun proximaRegiao(o: Obra): Int = o.tintas.indexOfFirst { it == 0 }

    // ----------------------------------------------------------------------------------------------
    // Prêmios
    // ----------------------------------------------------------------------------------------------

    /** Terminado = toda região tem tinta (balde) ou pincel cobrindo. Pelo menos 90% das regiões, para não travar nas minúsculas. */
    fun terminou(d: Desenho, o: Obra): Boolean {
        val cobertas = d.regioes.indices.count { o.tintas[it] != 0 || o.tracos.any { t -> t.regiao == it } }
        return cobertas * 10 >= d.regioes.size * 9
    }

    /** 1 estrela por terminar, 2 por usar 5 cores diferentes, 3 por usar uma tinta especial. 0 se não terminou. */
    fun estrelas(d: Desenho, o: Obra): Int {
        if (!terminou(d, o)) return 0
        val usadas = o.coresUsadas()
        return when {
            usadas.any { Tintas.especial(it) } && usadas.size >= 5 -> 3
            usadas.size >= 5 -> 2
            else -> 1
        }
    }

    data class Premio(val moedas: Int, val estrelas: Int, val primeiraVez: Boolean, val colecaoCompleta: Boolean, val tintaNova: Tinta?)

    /**
     * O que a criança ganha ao dizer "Pronto!". [feitos] são os desenhos já terminados (id -> melhores estrelas),
     * antes deste. Terminar de novo só dá prêmio se melhorar as estrelas; nunca tira nada. Devolve null se ainda não terminou.
     */
    fun premio(d: Desenho, o: Obra, feitos: Map<String, Int>, todos: List<Desenho> = Desenhos.todos): Premio? {
        val e = estrelas(d, o)
        if (e == 0) return null
        val antes = feitos[d.id] ?: 0
        val primeira = antes == 0
        val melhorou = e > antes
        val depois = if (melhorou) feitos + (d.id to e) else feitos
        val terminadosAntes = feitos.size
        val terminadosDepois = depois.size
        val moedas = when {
            primeira -> 5 + 3 * e
            melhorou -> 3 * (e - antes)
            else -> 0
        }
        val daColecao = todos.filter { it.colecao == d.colecao }.map { it.id }
        val completa = primeira && daColecao.all { it in depois }
        val tintaNova = if (primeira) Tintas.novaCom(terminadosDepois).takeIf { terminadosDepois > terminadosAntes } else null
        return Premio(moedas + if (completa) 20 else 0, e, primeira, completa, tintaNova)
    }

    // ----------------------------------------------------------------------------------------------
    // Guardar no aparelho (texto curto por obra, tolera lixo)
    // ----------------------------------------------------------------------------------------------

    private const val DIGITOS = "0123456789abcdefghijklmnopqrstuvwxyz"

    fun obrasParaTexto(obras: Map<String, Obra>): String = obras.values.joinToString("~") { o ->
        val cores = o.tintas.map { DIGITOS[it.coerceIn(0, DIGITOS.length - 1)] }.joinToString("")
        val tracos = o.tracos.joinToString("|") { t ->
            val pts = t.pontos.joinToString(";") { p -> "${centesimos(p.x)},${centesimos(p.y)}" }
            "${t.regiao}:${t.tinta}:${centesimos(t.largura)}:$pts"
        }
        "${o.desenhoId}#$cores#$tracos"
    }

    private fun centesimos(v: Float): Int = kotlin.math.round(v.coerceIn(0f, 1f) * 1000f).toInt()

    fun textoParaObras(s: String?, todos: List<Desenho> = Desenhos.todos): Map<String, Obra> {
        if (s.isNullOrBlank()) return emptyMap()
        val porId = todos.associateBy { it.id }
        val saida = LinkedHashMap<String, Obra>()
        for (item in s.split("~")) {
            val partes = item.split("#")
            if (partes.size < 2) continue
            val d = porId[partes[0]] ?: continue
            val tintas = partes[1].map { DIGITOS.indexOf(it) }
            if (tintas.size != d.regioes.size || tintas.any { it !in 0 until Tintas.todas.size }) continue
            val tracos = partes.getOrNull(2).orEmpty().split("|").mapNotNull { lerTraco(it, d) }.take(MAXIMO_DE_TRACOS)
            saida[d.id] = Obra(d.id, tintas, tracos)
        }
        return saida
    }

    private fun lerTraco(s: String, d: Desenho): Traco? {
        if (s.isBlank()) return null
        val p = s.split(":")
        if (p.size != 4) return null
        val regiao = p[0].toIntOrNull() ?: return null
        val tinta = p[1].toIntOrNull() ?: return null
        val largura = (p[2].toIntOrNull() ?: return null) / 1000f
        if (regiao !in -1 until d.regioes.size || tinta !in 1 until Tintas.todas.size) return null
        val pontos = p[3].split(";").mapNotNull {
            val xy = it.split(",")
            val x = xy.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val y = xy.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            Ponto(x / 1000f, y / 1000f)
        }.take(MAXIMO_DE_PONTOS)
        if (pontos.isEmpty()) return null
        return Traco(regiao, tinta, largura, pontos)
    }

    /** "id:estrelas" por desenho terminado. */
    fun feitosParaTexto(feitos: Map<String, Int>): String = feitos.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun textoParaFeitos(s: String?, todos: List<Desenho> = Desenhos.todos): Map<String, Int> {
        val ids = todos.map { it.id }.toSet()
        return s.orEmpty().split(",").mapNotNull {
            val p = it.split(":")
            val id = p.getOrNull(0) ?: return@mapNotNull null
            val e = p.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            if (id in ids && e in 1..3) id to e else null
        }.toMap()
    }
}

/** Histórico de desfazer e refazer (20 passos). Só vive na tela, não é guardado. */
data class Historico(val passado: List<Obra>, val atual: Obra, val futuro: List<Obra> = emptyList()) {
    fun fazer(nova: Obra): Historico =
        if (nova == atual) this else Historico((passado + atual).takeLast(PASSOS), nova, emptyList())

    fun desfazer(): Historico = if (passado.isEmpty()) this else Historico(passado.dropLast(1), passado.last(), listOf(atual) + futuro)

    fun refazer(): Historico = if (futuro.isEmpty()) this else Historico(passado + atual, futuro.first(), futuro.drop(1))

    val podeDesfazer: Boolean get() = passado.isNotEmpty()
    val podeRefazer: Boolean get() = futuro.isNotEmpty()

    companion object {
        const val PASSOS = 20
        fun de(o: Obra) = Historico(emptyList(), o)
    }
}
