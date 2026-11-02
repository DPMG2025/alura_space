package br.com.casinhadosaber.dominio

enum class Genero { MENINO, MENINA }

/** Personagem da criança. Os índices apontam para as paletas em [Aparencia]. */
data class Perfil(
    val nome: String = "",
    val genero: Genero = Genero.MENINA,
    val pele: Int = 0,
    val cabelo: Int = 0,
)

object Aparencia {
    val peles = listOf(0xFFFFDBB4, 0xFFF1C27D, 0xFFC68642, 0xFF8D5524)
    val cabelos = listOf(0xFF3B2314, 0xFF1C1C1C, 0xFFE6BE8A, 0xFFB55239)
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
) {
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

        /** 3 estrelas sem erros, 2 com até 2 erros, 1 com mais. */
        fun estrelasPorErros(erros: Int): Int = when {
            erros == 0 -> 3
            erros <= 2 -> 2
            else -> 1
        }
    }
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

    fun coresParaTexto(m: Map<Comodo, Int>): String = m.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    fun textoParaCores(s: String?): Map<Comodo, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val c = Comodo.entries.firstOrNull { it.name == k } ?: return@mapNotNull null
        c to (v.toIntOrNull() ?: 0)
    }.toMap()
}
