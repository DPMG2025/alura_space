package br.com.casinhadosaber.dominio

/**
 * "Relembrar": revisão espaçada do que a criança errou de primeira (no ritmo dela, nunca obrigatória).
 *
 * A fila é calculada só do registro das partidas ([RegistroDeSessao], que já guarda os acertos de primeira
 * de cada fase) e do registro das revisões ([RegistroDeRevisao]); nada lê o relógio (o dia vem de fora) e nada
 * é sorteado. Cada habilidade é uma fase de um caminho, como no resumo da semana dos pais.
 *
 * As regras:
 * * Errar de primeira numa fase põe a fase na fila (ou volta para o começo dela): relembrar no dia seguinte.
 * * Acertar tudo numa revisão (ou jogar a fase inteira sem errar, quando ela já está para relembrar) sobe um
 *   degrau no espaço: 1, 3, 7 e 14 dias ([INTERVALOS]). A fase sai da fila quando acerta no último degrau
 *   (a revisão de 14 dias), como prometido aos pais.
 * * Errar numa revisão não tira nada: só zera a contagem e chama de novo no dia seguinte (o degrau fica).
 */
data class RegistroDeRevisao(val dia: Long, val trilha: Trilha, val fase: Int, val certo: Boolean)

/** Uma fase na fila: [passo] é o degrau do espaço ([Relembrar.INTERVALOS]) e [proximoDia] quando relembrar. */
data class ItemDeRevisao(
    val trilha: Trilha,
    val fase: Int,
    val passo: Int,
    val acertosSeguidos: Int,
    val proximoDia: Long,
) {
    fun paraHoje(dia: Long): Boolean = proximoDia <= dia
}

object Relembrar {
    /** Dias até a próxima revisão em cada degrau. */
    val INTERVALOS = listOf(1L, 3L, 7L, 14L)
    const val PERGUNTAS = 5
    /** Prêmio fixo da rodada (dentro do limite diário de moedinhas), tanto faz quantas acertou. */
    const val MOEDAS = 3
    private const val DIAS_GUARDADOS = 60L
    private const val MAXIMO_DE_REGISTROS = 600

    /**
     * A fila de hoje, refeita do começo pelos registros. No mesmo dia, as partidas contam antes das revisões;
     * revisão de uma fase que não estava para relembrar (por exemplo, errada de novo na partida do mesmo dia)
     * não conta.
     */
    fun fila(sessoes: List<RegistroDeSessao>, revisoes: List<RegistroDeRevisao>): List<ItemDeRevisao> {
        val itens = linkedMapOf<Pair<Trilha, Int>, ItemDeRevisao>()
        fun acertou(k: Pair<Trilha, Int>, dia: Long) {
            val i = itens[k] ?: return
            if (!i.paraHoje(dia)) return
            val acertos = i.acertosSeguidos + 1
            if (i.passo >= INTERVALOS.lastIndex) {
                // Acertou a revisão de 14 dias: aprendeu, sai da fila.
                itens.remove(k)
            } else {
                val passo = (i.passo + 1).coerceAtMost(INTERVALOS.lastIndex)
                itens[k] = i.copy(passo = passo, acertosSeguidos = acertos, proximoDia = dia + INTERVALOS[passo])
            }
        }
        val eventos = sessoes.map { Triple(it.dia, 0, it) } + revisoes.map { Triple(it.dia, 1, it) }
        for ((_, _, e) in eventos.sortedWith(compareBy({ it.first }, { it.second }))) {
            when (e) {
                is RegistroDeSessao -> {
                    val k = e.trilha to e.fase
                    if (e.erros > 0) {
                        itens.remove(k)
                        itens[k] = ItemDeRevisao(e.trilha, e.fase, 0, 0, e.dia + INTERVALOS[0])
                    } else if (e.total > 0) {
                        acertou(k, e.dia)
                    }
                }
                is RegistroDeRevisao -> {
                    val k = e.trilha to e.fase
                    if (e.certo) {
                        acertou(k, e.dia)
                    } else {
                        val i = itens[k]
                        if (i != null && i.paraHoje(e.dia)) itens[k] = i.copy(acertosSeguidos = 0, proximoDia = e.dia + INTERVALOS[0])
                    }
                }
            }
        }
        return itens.values.sortedWith(compareBy<ItemDeRevisao>({ it.proximoDia }, { it.trilha.ordinal }, { it.fase }))
    }

    /** O que está para relembrar no [dia] (de um caminho só, se [trilha] vier), o mais antigo primeiro. */
    fun paraHoje(fila: List<ItemDeRevisao>, dia: Long, trilha: Trilha? = null): List<ItemDeRevisao> =
        fila.filter { it.paraHoje(dia) && (trilha == null || it.trilha == trilha) && it.fase in 0 until GeradorDeFases.FASES_POR_TRILHA }

    /**
     * As fases das [PERGUNTAS] perguntas da rodada: até 5 fases para relembrar, uma pergunta de cada, girando
     * quando há menos de 5 (com uma fase só, as 5 perguntas vêm dela).
     */
    fun fasesDaRodada(paraHoje: List<ItemDeRevisao>): List<Pair<Trilha, Int>> {
        val escolhidas = paraHoje.take(PERGUNTAS).map { it.trilha to it.fase }
        if (escolhidas.isEmpty()) return emptyList()
        return List(PERGUNTAS) { escolhidas[it % escolhidas.size] }
    }

    /** Uma revisão por fase: certa se todas as perguntas daquela fase saíram certas de primeira. */
    fun revisoesDaRodada(fases: List<Pair<Trilha, Int>>, resultados: List<Boolean>, dia: Long): List<RegistroDeRevisao> =
        fases.zip(resultados).groupBy({ it.first }, { it.second })
            .map { (k, rs) -> RegistroDeRevisao(dia, k.first, k.second, rs.all { it }) }

    /** Moedinhas da rodada: [MOEDAS], dentro do que ainda cabe no limite do dia. */
    fun moedas(estado: EstadoJogo, dia: Long): Int {
        val hojeJa = if (estado.diaDasMoedas == dia) estado.moedasHoje else 0
        return MOEDAS.coerceAtMost((EstadoJogo.MOEDAS_POR_DIA - hojeJa).coerceAtLeast(0))
    }

    /** O convite da Corujinha (nunca obrigatório). */
    fun convite(nome: String?, quantas: Int): String {
        val oi = if (nome.isNullOrBlank()) "Oi!" else "Oi, $nome!"
        val coisas = if (quantas == 1) "uma coisinha" else "umas coisinhas"
        return "$oi Vamos relembrar $coisas que você já aprendeu? São só ${PERGUNTAS} perguntinhas, e errar faz parte."
    }

    // ------------------------------------------------------------------ registro

    fun cortar(registros: List<RegistroDeRevisao>): List<RegistroDeRevisao> {
        val maisNovo = registros.maxOfOrNull { it.dia } ?: return registros
        return registros.filter { it.dia > maisNovo - DIAS_GUARDADOS }.takeLast(MAXIMO_DE_REGISTROS)
    }

    /** "dia,TRILHA,fase,1;..." */
    fun paraTexto(registros: List<RegistroDeRevisao>): String =
        registros.joinToString(";") { "${it.dia},${it.trilha.name},${it.fase},${if (it.certo) 1 else 0}" }

    /** Saves antigos (sem a chave) ou texto estragado: as linhas que não dá para ler somem. */
    fun deTexto(s: String?): List<RegistroDeRevisao> = cortar(
        s.orEmpty().split(";").mapNotNull { linha ->
            val p = linha.split(",")
            if (p.size != 4) return@mapNotNull null
            val dia = p[0].toLongOrNull() ?: return@mapNotNull null
            val trilha = Trilha.entries.firstOrNull { it.name == p[1] } ?: return@mapNotNull null
            val fase = p[2].toIntOrNull()?.takeIf { it >= 0 } ?: return@mapNotNull null
            val certo = when (p[3]) { "1" -> true; "0" -> false; else -> return@mapNotNull null }
            RegistroDeRevisao(dia, trilha, fase, certo)
        },
    )
}

/** A fila de revisão de agora (calculada dos registros). */
val EstadoJogo.filaDeRevisao: List<ItemDeRevisao> get() = Relembrar.fila(sessoes, revisoes)

/** O que está para relembrar hoje (de um caminho, ou de todos). */
fun EstadoJogo.paraRelembrar(dia: Long, trilha: Trilha? = null): List<ItemDeRevisao> =
    // Fase fechada de novo (os pais desfizeram o nível) não aparece para relembrar.
    Relembrar.paraHoje(filaDeRevisao, dia, trilha).filter { liberada(it.trilha, it.fase) }

/**
 * Guarda a rodada do Relembrar: uma revisão por fase e o prêmio fixo ([Relembrar.MOEDAS], dentro do limite do
 * dia). Devolve o novo estado e as moedinhas ganhas. Nada é tirado quando a criança erra.
 */
fun EstadoJogo.concluirRelembrar(fases: List<Pair<Trilha, Int>>, resultados: List<Boolean>, dia: Long): Pair<EstadoJogo, Int> {
    if (resultados.isEmpty()) return this to 0
    val ganhas = Relembrar.moedas(this, dia)
    val hojeJa = if (diaDasMoedas == dia) moedasHoje else 0
    val novas = Relembrar.revisoesDaRodada(fases, resultados, dia)
    return copy(
        revisoes = Relembrar.cortar(revisoes + novas),
        moedas = moedas + ganhas,
        moedasHoje = hojeJa + ganhas,
        diaDasMoedas = dia,
    ) to ganhas
}
