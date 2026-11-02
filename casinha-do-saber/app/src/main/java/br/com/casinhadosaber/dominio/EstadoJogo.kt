package br.com.casinhadosaber.dominio

enum class Genero { MENINO, MENINA }

/** Personagem da criança. Os índices apontam para as paletas em [Aparencia]. */
data class Perfil(
    val nome: String = "",
    val genero: Genero = Genero.MENINA,
    val pele: Int = 0,
    val cabelo: Int = 0,
    /** Cor da roupa; -1 usa a cor padrão do menino (azul) ou da menina (rosa). */
    val roupa: Int = -1,
    /** Chapéu comprado na loja (emoji), ou vazio. */
    val chapeu: String = "",
) {
    val corDaRoupa: Long
        get() = Aparencia.roupas.getOrNull(roupa) ?: if (genero == Genero.MENINA) Aparencia.roupas[1] else Aparencia.roupas[0]
}

object Aparencia {
    val peles = listOf(0xFFFFDBB4, 0xFFF1C27D, 0xFFC68642, 0xFF8D5524)
    val cabelos = listOf(0xFF3B2314, 0xFF1C1C1C, 0xFFE6BE8A, 0xFFB55239)
    val roupas = listOf(0xFF4D96FF, 0xFFFF6FAE, 0xFF3DBE6B, 0xFFFFA62B, 0xFF9B6BFF, 0xFFFF5E5B)
}

/** Lugar de um objeto na cena (mesmo sistema de coordenadas de [ItemCasa]). */
data class Posicao(val x: Float, val y: Float)

data class EstadoJogo(
    val perfil: Perfil? = null,
    /** "TRILHA:fase" para estrelas (1 a 3). Só fases vencidas aparecem aqui. */
    val estrelas: Map<String, Int> = emptyMap(),
    /** Itens já colocados na casa. Estruturas também entram aqui (a posição delas é ignorada). */
    val colocados: Map<String, Posicao> = emptyMap(),
    val coresComodos: Map<Comodo, Int> = emptyMap(),
    /** Onde a criança (o personagem) está na cena. */
    val posicaoCrianca: Posicao = Posicao(0.47f, 0.99f),
    /** Para a área dos pais: perguntas respondidas e acertadas de primeira, por caminho. */
    val respondidas: Map<Trilha, Int> = emptyMap(),
    val acertosDePrimeira: Map<Trilha, Int> = emptyMap(),
    val segundosJogados: Long = 0,
    /** Derrotas seguidas em cada fase ("TRILHA:fase"). Com 2 ou mais, a fase fica mais fácil. */
    val tentativasSemVencer: Map<String, Int> = emptyMap(),
    /** Loja: moedinhas, o que já foi comprado e o que está em uso. */
    val moedas: Int = 0,
    val moedasHoje: Int = 0,
    val diaDasMoedas: Long = -1,
    val comprados: Set<String> = emptySet(),
    val corTelhado: Int = 0,
    val corPorta: Int = 0,
    val papelDeParede: Int = 0,
    val clima: Int = 0,
    val visitantes: Set<String> = emptySet(),
    /** Últimos resultados de primeira por fase ("TRILHA:fase" -> "1101..."), no máximo 10. */
    val historico: Map<String, String> = emptyMap(),
) {
    val totalDeEstrelas: Int get() = estrelas.values.sum()

    /**
     * Toca num produto da loja: se já é seu, só passa a usar (ou tira, no caso de chapéu e visitante);
     * se não é, compra com moedinhas. Devolve null quando faltam moedas.
     */
    fun usar(p: Produto): EstadoJogo? {
        val base = if (p.id in comprados) {
            this
        } else {
            val preco = Loja.preco(p, this)
            if (moedas < preco) return null
            copy(moedas = moedas - preco, comprados = comprados + p.id)
        }
        val v = p.valor
        return when (p.categoria) {
            Categoria.TELHADO -> base.copy(corTelhado = if (corTelhado.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.PORTA -> base.copy(corPorta = if (corPorta.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.PAREDE -> base.copy(papelDeParede = if (papelDeParede.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.CLIMA -> base.copy(clima = if (clima.toString() == v && p.id in comprados) 0 else v.toInt())
            Categoria.CHAPEU -> base.copy(perfil = base.perfil?.copy(chapeu = if (base.perfil.chapeu == v) "" else v))
            Categoria.VISITANTE -> base.copy(visitantes = if (v in visitantes && p.id in comprados) visitantes - v else visitantes + v)
        }
    }

    /** Fase em que a criança mais erra de primeira, entre as já liberadas (para os pais). */
    fun precisaoDaFase(chave: String): Int? = historico[chave]?.takeIf { it.isNotEmpty() }?.let { 100 * it.count { c -> c == '1' } / it.length }

    /** Depois de 2 tentativas sem vencer, a fase vem com só 2 opções por pergunta. */
    fun facilitada(fase: Fase): Boolean = (tentativasSemVencer[chave(fase.trilha, fase.numero)] ?: 0) >= 2

    /** Porcentagem de acertos de primeira no caminho (null se ainda não jogou). */
    fun precisao(t: Trilha): Int? {
        val total = respondidas[t] ?: 0
        return if (total == 0) null else (100 * (acertosDePrimeira[t] ?: 0)) / total
    }

    /**
     * Registra uma partida. A fase só é vencida com pelo menos [MINIMO_PARA_VENCER] acertos de primeira:
     * assim o presente da casa vem do aprendizado, não de tocar em tudo.
     */
    fun registrarPartida(fase: Fase, acertos: Int, total: Int, dia: Long = 0, resultados: List<Boolean> = emptyList()): ResultadoPartida {
        // Moedinhas: uma por acerto de primeira, até MOEDAS_POR_DIA por dia (incentiva voltar, sem maratona).
        val hojeJa = if (diaDasMoedas == dia) moedasHoje else 0
        val ganhas = acertos.coerceAtMost((MOEDAS_POR_DIA - hojeJa).coerceAtLeast(0))
        val k0 = chave(fase.trilha, fase.numero)
        val novoHistorico = ((historico[k0] ?: "") + resultados.joinToString("") { if (it) "1" else "0" }).takeLast(10)
        val comEstatistica = copy(
            respondidas = respondidas + (fase.trilha to (respondidas[fase.trilha] ?: 0) + total),
            acertosDePrimeira = acertosDePrimeira + (fase.trilha to (acertosDePrimeira[fase.trilha] ?: 0) + acertos),
            moedas = moedas + ganhas,
            moedasHoje = hojeJa + ganhas,
            diaDasMoedas = dia,
            historico = if (resultados.isEmpty()) historico else historico + (k0 to novoHistorico),
        )
        val estrelas = estrelasPorAcertos(acertos, total)
        val k = chave(fase.trilha, fase.numero)
        if (estrelas == 0) {
            val derrotas = comEstatistica.copy(tentativasSemVencer = tentativasSemVencer + (k to (tentativasSemVencer[k] ?: 0) + 1))
            return ResultadoPartida(derrotas, 0, null, ganhas)
        }
        val (novo, item) = comEstatistica.copy(tentativasSemVencer = tentativasSemVencer - k).registrarVitoria(fase, estrelas)
        return ResultadoPartida(novo, estrelas, item, ganhas)
    }
    val fasesVencidas: Int get() = estrelas.size
    val itensGanhos: List<ItemCasa> get() = ItensDaCasa.ganhos(fasesVencidas)
    val itensNoBau: List<ItemCasa> get() = itensGanhos.filter { it.id !in colocados }

    fun estrelasDa(fase: Fase): Int = estrelas[chave(fase.trilha, fase.numero)] ?: 0

    fun vencidasNa(trilha: Trilha): Int = estrelas.keys.count { it.startsWith(trilha.name + ":") }

    /** A primeira fase de cada trilha está sempre aberta; as outras abrem quando a anterior é vencida. */
    fun liberada(fase: Fase): Boolean =
        fase.numero == 0 || estrelas.containsKey(chave(fase.trilha, fase.numero - 1))

    fun temEstrutura(e: Estrutura): Boolean =
        ItensDaCasa.ordem.firstOrNull { it.estrutura == e }?.id?.let { it in colocados } ?: false

    fun corDo(comodo: Comodo): Long = comodo.cores[(coresComodos[comodo] ?: 0) % comodo.cores.size]

    /**
     * Registra uma fase vencida. Retorna o novo estado e o item ganho, se a fase foi vencida pela primeira vez.
     * Repetir uma fase pode melhorar as estrelas, mas não dá outro item.
     */
    fun registrarVitoria(fase: Fase, novasEstrelas: Int): Pair<EstadoJogo, ItemCasa?> {
        val k = chave(fase.trilha, fase.numero)
        val primeiraVez = k !in estrelas
        val melhor = maxOf(estrelas[k] ?: 0, novasEstrelas.coerceIn(1, 3))
        val novo = copy(estrelas = estrelas + (k to melhor))
        val premio = if (primeiraVez) ItensDaCasa.ordem.getOrNull(fasesVencidas) else null
        return novo to premio
    }

    fun colocar(item: ItemCasa, pos: Posicao = Posicao(item.x, item.y)): EstadoJogo =
        copy(colocados = colocados + (item.id to pos))

    fun guardar(item: ItemCasa): EstadoJogo =
        if (item.ehEstrutura) this else copy(colocados = colocados - item.id)

    fun pintar(comodo: Comodo): EstadoJogo =
        copy(coresComodos = coresComodos + (comodo to ((coresComodos[comodo] ?: 0) + 1) % comodo.cores.size))

    companion object {
        fun chave(trilha: Trilha, fase: Int) = "${trilha.name}:$fase"

        const val MOEDAS_POR_DIA = 30

        /** 4 de 5 de primeira: chutando ao acaso, quase nunca se vence (cerca de 4%). */
        const val MINIMO_PARA_VENCER = 4

        /** 5 de primeira = 3 estrelas; 4 = 2 estrelas; menos = tentar de novo. Na fase facilitada, 4 vale 1 estrela. */
        fun estrelasPorAcertos(acertos: Int, total: Int): Int = when {
            acertos < MINIMO_PARA_VENCER -> 0
            acertos >= total -> 3
            else -> 2
        }
    }
}

data class ResultadoPartida(val estado: EstadoJogo, val estrelas: Int, val item: ItemCasa?, val moedasGanhas: Int = 0) {
    val venceu get() = estrelas > 0
}

/** Converte o estado em textos simples para guardar no aparelho (e de volta). */
object Serializacao {

    fun estrelasParaTexto(m: Map<String, Int>): String = m.entries.joinToString(";") { "${it.key}=${it.value}" }

    fun textoParaEstrelas(s: String?): Map<String, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val n = v.toIntOrNull() ?: return@mapNotNull null
        val trilha = k.substringBefore(":")
        if (Trilha.entries.none { it.name == trilha }) return@mapNotNull null
        k to n.coerceIn(1, 3)
    }.toMap()

    fun posicoesParaTexto(m: Map<String, Posicao>): String =
        m.entries.joinToString(";") { "${it.key},${it.value.x},${it.value.y}" }

    fun textoParaPosicoes(s: String?): Map<String, Posicao> = s.orEmpty().split(";").mapNotNull { parte ->
        val p = parte.split(",")
        if (p.size != 3 || p[0] !in ItensDaCasa.porId) return@mapNotNull null
        val x = p[1].toFloatOrNull() ?: return@mapNotNull null
        val y = p[2].toFloatOrNull() ?: return@mapNotNull null
        p[0] to Posicao(x, y)
    }.toMap()

    fun trilhasParaTexto(m: Map<Trilha, Int>): String = m.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    fun textoParaTrilhas(s: String?): Map<Trilha, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val t = Trilha.entries.firstOrNull { it.name == k } ?: return@mapNotNull null
        t to (v.toIntOrNull() ?: return@mapNotNull null)
    }.toMap()

    fun mapaParaTexto(m: Map<String, Int>): String = m.entries.joinToString(";") { "${it.key}=${it.value}" }

    fun textoParaMapa(s: String?): Map<String, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        k to (v.toIntOrNull() ?: return@mapNotNull null)
    }.toMap()

    fun mapaDeTextoParaTexto(m: Map<String, String>): String = m.entries.joinToString(";") { "${it.key}=${it.value}" }

    fun textoParaMapaDeTexto(s: String?): Map<String, String> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        if (v.any { it != '0' && it != '1' }) return@mapNotNull null
        k to v
    }.toMap()

    fun coresParaTexto(m: Map<Comodo, Int>): String = m.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    fun textoParaCores(s: String?): Map<Comodo, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val c = Comodo.entries.firstOrNull { it.name == k } ?: return@mapNotNull null
        c to (v.toIntOrNull() ?: 0)
    }.toMap()
}
