package br.com.casinhadosaber.dominio

import kotlin.random.Random

/**
 * Quebra-cabeças (Fase 11). A imagem é um desenho do Livro de Colorir (a versão bonita ou a obra que a criança
 * pintou), cortado em grade na hora de desenhar: nenhuma imagem nova no APK. Sem tempo, sem perder nada ao errar.
 *
 * Dois jogos: **montar** (arrastar peças para a moldura, com ímã) e **deslizar** (empurrar peças para o espaço
 * vazio). A tela é o quadrado da moldura de 0 a 1; a bandeja com as peças soltas fica logo abaixo (y de 1,05 a 1,55).
 */
data class NivelDePuzzle(val numero: Int, val colunas: Int, val linhas: Int) {
    val pecas: Int get() = colunas * linhas
}

object Puzzle {
    /** Oito níveis por coleção: de 4 a 36 peças. */
    val niveis: List<NivelDePuzzle> = listOf(
        NivelDePuzzle(1, 2, 2),
        NivelDePuzzle(2, 3, 2),
        NivelDePuzzle(3, 3, 3),
        NivelDePuzzle(4, 4, 3),
        NivelDePuzzle(5, 4, 4),
        NivelDePuzzle(6, 5, 4),
        NivelDePuzzle(7, 5, 5),
        NivelDePuzzle(8, 6, 6),
    )

    const val NIVEL_MAXIMO = 8

    /**
     * Topo e fundo da bandeja, em unidades da moldura. A bandeja tem o tamanho da moldura (uma casa para cada peça),
     * então nenhuma peça fica em cima de outra e a criança consegue pegar todas.
     */
    const val BANDEJA_DE = 1.05f
    const val BANDEJA_ATE = 2.05f

    /** O ímã pega a peça quando ela chega a esta fração do tamanho da casa. Com a ajuda "ímã largo", [IMA_LARGO]. */
    const val IMA = 0.38f
    const val IMA_LARGO = 0.6f

    fun nivel(n: Int): NivelDePuzzle = niveis[(n - 1).coerceIn(0, niveis.size - 1)]

    // ----------------------------------------------------------------------------------------------
    // Montar
    // ----------------------------------------------------------------------------------------------

    /** Uma peça: [id] diz a casa certa (coluna = id % colunas, linha = id / colunas). x e y = canto de cima à esquerda. */
    data class Peca(val id: Int, val x: Float, val y: Float, val colocada: Boolean)

    /**
     * [pecas] vem na ordem de desenho: a última fica por cima (a que a criança mexeu por último).
     * [dicas] e [arrastos] entram na conta das estrelas.
     */
    data class Montagem(
        val fonte: String,
        val nivel: Int,
        val pecas: List<Peca>,
        val dicas: Int = 0,
        val arrastos: Int = 0,
    ) {
        val grade: NivelDePuzzle get() = nivel(nivel)
        val largura: Float get() = 1f / grade.colunas
        val altura: Float get() = 1f / grade.linhas
        val venceu: Boolean get() = pecas.isNotEmpty() && pecas.all { it.colocada }
        val faltam: Int get() = pecas.count { !it.colocada }
    }

    fun alvoX(m: Montagem, id: Int): Float = (id % m.grade.colunas) * m.largura
    fun alvoY(m: Montagem, id: Int): Float = (id / m.grade.colunas) * m.altura

    /** Põe as peças embaralhadas nas casas da bandeja, uma em cada (nenhuma começa colocada). Mesma [semente], mesma ordem. */
    fun montar(fonte: String, nivel: Int, semente: Long): Montagem {
        val g = nivel(nivel)
        val sorteio = Random(semente)
        val w = 1f / g.colunas
        val h = 1f / g.linhas
        val ordem = (0 until g.pecas).shuffled(sorteio)
        // A peça que cai na casa k da bandeja: a lista fica na ordem das casas, a última é a que fica por cima.
        val pecas = ordem.mapIndexed { casa, id ->
            Peca(id, (casa % g.colunas) * w, BANDEJA_DE + (casa / g.colunas) * h, colocada = false)
        }
        return Montagem(fonte, g.numero, pecas)
    }

    /**
     * A criança soltou a peça [id] com o canto em ([x], [y]). Se está perto da casa certa (dentro do ímã), a peça
     * encaixa exatamente e fica parada; senão fica onde foi solta (dentro da área de jogo). Peça já colocada não mexe.
     */
    fun soltar(m: Montagem, id: Int, x: Float, y: Float, imaLargo: Boolean = false): Montagem {
        val i = m.pecas.indexOfFirst { it.id == id }
        if (i < 0 || m.pecas[i].colocada) return m
        val ax = alvoX(m, id)
        val ay = alvoY(m, id)
        val ima = if (imaLargo) IMA_LARGO else IMA
        val perto = kotlin.math.abs(x - ax) <= m.largura * ima && kotlin.math.abs(y - ay) <= m.altura * ima
        val nova = if (perto) {
            m.pecas[i].copy(x = ax, y = ay, colocada = true)
        } else {
            m.pecas[i].copy(x = x.coerceIn(0f, 1f - m.largura), y = y.coerceIn(0f, BANDEJA_ATE - m.altura))
        }
        // A peça mexida vai para o fim da lista (fica por cima); as colocadas ficam no começo (por baixo).
        val resto = m.pecas.filterIndexed { k, _ -> k != i }
        val pecas = if (perto) listOf(nova) + resto else resto + nova
        return m.copy(pecas = pecas, arrastos = m.arrastos + 1)
    }

    /** Dica: a peça solta de menor número (a casa dela pisca). Conta uma dica. Devolve o id, ou -1 se não falta nenhuma. */
    fun dica(m: Montagem): Pair<Int, Montagem> {
        val alvo = m.pecas.filter { !it.colocada }.minByOrNull { it.id } ?: return -1 to m
        return alvo.id to m.copy(dicas = m.dicas + 1)
    }

    /** "Só as bordas primeiro": as peças da borda da moldura, para quem quer começar por elas. */
    fun ehBorda(m: Montagem, id: Int): Boolean {
        val c = id % m.grade.colunas
        val l = id / m.grade.colunas
        return c == 0 || l == 0 || c == m.grade.colunas - 1 || l == m.grade.linhas - 1
    }

    // ----------------------------------------------------------------------------------------------
    // Deslizar
    // ----------------------------------------------------------------------------------------------

    /** [casas] guarda o número da peça em cada casa (0 = espaço vazio). Resolvido: 1, 2, ..., n*n-1, 0. */
    data class Deslizante(val lado: Int, val casas: List<Int>, val toques: Int = 0) {
        val resolvido: Boolean get() = casas == meta(lado)
        val vazio: Int get() = casas.indexOf(0)
    }

    fun meta(lado: Int): List<Int> = List(lado * lado) { if (it == lado * lado - 1) 0 else it + 1 }

    private fun vizinhos(lado: Int, i: Int): List<Int> {
        val c = i % lado
        val l = i / lado
        return buildList {
            if (c > 0) add(i - 1)
            if (c < lado - 1) add(i + 1)
            if (l > 0) add(i - lado)
            if (l < lado - 1) add(i + lado)
        }
    }

    /** Embaralha só com jogadas legais a partir do resolvido, então sempre tem solução. [lado] de 2 a 5. */
    fun deslizar(lado: Int, semente: Long): Deslizante {
        require(lado in 2..5) { "lado de 2 a 5" }
        val sorteio = Random(semente)
        var casas = meta(lado)
        var vazio = casas.indexOf(0)
        var anterior = -1
        repeat(lado * lado * 12) {
            val opcoes = vizinhos(lado, vazio).filter { it != anterior }
            val v = opcoes[sorteio.nextInt(opcoes.size)]
            casas = casas.toMutableList().also { it[vazio] = it[v]; it[v] = 0 }
            anterior = vazio
            vazio = v
        }
        // Se por azar voltou ao resolvido, faz uma jogada.
        if (casas == meta(lado)) {
            val v = vizinhos(lado, vazio).first()
            casas = casas.toMutableList().also { it[vazio] = it[v]; it[v] = 0 }
        }
        return Deslizante(lado, casas)
    }

    /** Toque numa casa: se encosta no vazio, troca. Senão nada (e não conta toque). */
    fun tocar(d: Deslizante, casa: Int): Deslizante {
        if (casa !in d.casas.indices || casa == d.vazio) return d
        if (casa !in vizinhos(d.lado, d.vazio)) return d
        val novas = d.casas.toMutableList().also { it[d.vazio] = it[casa]; it[casa] = 0 }
        return d.copy(casas = novas, toques = d.toques + 1)
    }

    /** Dica do deslizante: a casa a tocar para pôr no lugar a primeira peça fora dela (ou qualquer jogada que ajude). */
    fun dicaDeslizante(d: Deslizante): Int {
        if (d.resolvido) return -1
        val v = d.vazio
        val alvo = meta(d.lado)
        // Uma jogada que põe uma peça no lugar certo, se existir.
        vizinhos(d.lado, v).firstOrNull { d.casas[it] == alvo[v] }?.let { return it }
        // Senão, qualquer vizinho que não tire do lugar uma peça já certa.
        return vizinhos(d.lado, v).firstOrNull { d.casas[it] != alvo[it] } ?: vizinhos(d.lado, v).first()
    }

    // ----------------------------------------------------------------------------------------------
    // Progresso e prêmios
    // ----------------------------------------------------------------------------------------------

    /** 1 estrela por montar, 2 por montar sem dica, 3 por montar sem dica e sem desperdiçar arrastos (até 2 por peça). */
    fun estrelas(m: Montagem): Int {
        if (!m.venceu) return 0
        return when {
            m.dicas > 0 -> 1
            m.arrastos <= m.pecas.size * 2 -> 3
            else -> 2
        }
    }

    fun estrelasDoDeslizante(d: Deslizante, dicas: Int): Int {
        if (!d.resolvido) return 0
        return when {
            dicas > 0 -> 1
            d.toques <= d.lado * d.lado * 6 -> 3
            else -> 2
        }
    }

    fun chave(desenhoId: String, nivel: Int) = "$desenhoId:$nivel"

    /** Maior nível já vencido na coleção (qualquer desenho), 0 se nenhum. */
    fun nivelMaximo(colecao: String, feitos: Map<String, Int>, todos: List<DesenhoDeColorir> = Desenhos.todos): Int {
        val ids = todos.filter { it.colecao == colecao }.map { it.id }.toSet()
        return feitos.keys.mapNotNull { k ->
            val p = k.split(":")
            val nivel = p.getOrNull(1)?.toIntOrNull()
            if (p.getOrNull(0) in ids) nivel else null
        }.maxOrNull() ?: 0
    }

    /** Até que nível a criança pode escolher na coleção: o seguinte ao maior vencido (o 1 está sempre aberto). */
    fun nivelAberto(colecao: String, feitos: Map<String, Int>, todos: List<DesenhoDeColorir> = Desenhos.todos): Int =
        (nivelMaximo(colecao, feitos, todos) + 1).coerceIn(1, NIVEL_MAXIMO)

    /**
     * Nível sugerido para o próximo: 3 vitórias seguidas sem dica sobem um nível; travar (mais de 120 s sem
     * encaixar nada) desce um na vez seguinte. Nunca sai de 1 a [NIVEL_MAXIMO] e nunca passa do aberto.
     */
    fun sugerir(nivelAtual: Int, seguidasSemDica: Int, travou: Boolean, aberto: Int): Int {
        val alvo = when {
            travou -> nivelAtual - 1
            seguidasSemDica >= 3 -> nivelAtual + 1
            else -> nivelAtual
        }
        return alvo.coerceIn(1, minOf(NIVEL_MAXIMO, aberto.coerceAtLeast(1)))
    }

    data class Marco(val total: Int, val nome: String, val moedas: Int)

    val marcos: List<Marco> = listOf(
        Marco(1, "Primeiro quebra-cabeça", 5),
        Marco(10, "Montador", 15),
        Marco(25, "Craque das peças", 30),
        Marco(50, "Mestre dos quebra-cabeças", 60),
    )

    data class Premio(
        val moedas: Int,
        val estrelas: Int,
        val primeiraVez: Boolean,
        val pecaDourada: Int,
        val marco: Marco?,
        val colecaoCompleta: Boolean,
    )

    /**
     * O que a criança ganha ao vencer o [nivel] de [desenhoId] com [estrelas]. [feitos] = "desenho:nível" -> melhores
     * estrelas, antes desta vitória. Repetir só paga se melhorar as estrelas. Peças douradas: 1 por (desenho, nível)
     * novo e mais 1 ao chegar às 3 estrelas pela primeira vez.
     */
    fun premio(desenhoId: String, nivel: Int, estrelas: Int, feitos: Map<String, Int>, todos: List<DesenhoDeColorir> = Desenhos.todos): Premio? {
        if (estrelas !in 1..3) return null
        val d = todos.firstOrNull { it.id == desenhoId } ?: return null
        val k = chave(desenhoId, nivel)
        val antes = feitos[k] ?: 0
        val primeira = antes == 0
        val melhorou = estrelas > antes
        val depois = if (melhorou) feitos + (k to estrelas) else feitos
        val moedas = when {
            primeira -> 3 + nivel * 2 + 2 * estrelas
            melhorou -> 2 * (estrelas - antes)
            else -> 0
        }
        val dourada = (if (primeira) 1 else 0) + (if (estrelas == 3 && antes < 3) 1 else 0)
        val marco = if (primeira) marcos.firstOrNull { it.total == depois.size } else null
        val completa = primeira && nivelMaximo(d.colecao, depois, todos) == NIVEL_MAXIMO && nivelMaximo(d.colecao, feitos, todos) < NIVEL_MAXIMO
        return Premio(moedas + (marco?.moedas ?: 0) + if (completa) 25 else 0, estrelas, primeira, dourada, marco, completa)
    }

    /** Peças douradas ganhas até agora (derivado dos feitos, nada a guardar à parte). */
    fun pecasDouradas(feitos: Map<String, Int>): Int = feitos.size + feitos.values.count { it == 3 }

    // ----------------------------------------------------------------------------------------------
    // Guardar no aparelho
    // ----------------------------------------------------------------------------------------------

    /** "desenho:nível:estrelas" separados por vírgula. */
    fun feitosParaTexto(feitos: Map<String, Int>): String = feitos.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun textoParaFeitos(s: String?, todos: List<DesenhoDeColorir> = Desenhos.todos): Map<String, Int> {
        val ids = todos.map { it.id }.toSet()
        return s.orEmpty().split(",").mapNotNull {
            val p = it.split(":")
            if (p.size != 3) return@mapNotNull null
            val nivel = p[1].toIntOrNull() ?: return@mapNotNull null
            val e = p[2].toIntOrNull() ?: return@mapNotNull null
            if (p[0] in ids && nivel in 1..NIVEL_MAXIMO && e in 1..3) chave(p[0], nivel) to e else null
        }.toMap()
    }
}
