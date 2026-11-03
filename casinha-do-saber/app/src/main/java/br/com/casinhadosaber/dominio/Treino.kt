package br.com.casinhadosaber.dominio

import kotlin.random.Random

/**
 * Treino sem fim de um caminho. As perguntas vêm das fases já vencidas, com mais peso nas que a
 * criança mais erra de primeira ([EstadoJogo.historico]). Começa pelas fases mais antigas e, a
 * cada [ACERTOS_PARA_SUBIR] acertos seguidos, abre fases mais novas (mais difíceis); com
 * [ERROS_PARA_DESCER] erros seguidos, volta um pouco.
 */
class Treino(
    val trilha: Trilha,
    estado: EstadoJogo,
    private val gerador: GeradorDeFases = GeradorDeFases(),
    private val random: Random = Random.Default,
) {
    /** Fases que podem cair, da mais fácil para a mais difícil (só as vencidas; sem nenhuma, a primeira). */
    val fases: List<Fase> = gerador.fases(trilha).filter { estado.estrelasDa(it) > 0 }
        .ifEmpty { listOf(gerador.fases(trilha).first()) }

    private val precisao: Map<Fase, Int?> = fases.associateWith { estado.precisaoDaFase(EstadoJogo.chave(it.trilha, it.numero)) }

    /** Quantas fases (das mais fáceis) estão valendo agora. Cresce com acertos seguidos. */
    var teto: Int = inicioDoTeto(fases.size)
        private set

    /** Nível do treino (1, 2, 3...): sobe junto com o teto. */
    var nivel: Int = 1
        private set

    var acertosSeguidos: Int = 0
        private set
    private var errosSeguidos = 0

    /** As fases das perguntas, na ordem em que foram feitas (para guardar o histórico de cada uma). */
    val feitas: List<Fase> get() = _feitas
    private val _feitas = mutableListOf<Fase>()
    private var resultadosVistos = 0
    private var subiu = false

    /** Peso de cada fase no sorteio agora: errar mais = cair mais; fases mais novas pesam mais quando a criança acerta em sequência. */
    fun pesos(): Map<Fase, Double> {
        val janela = fases.take(teto)
        val ultima = _feitas.lastOrNull()
        return janela.mapIndexed { i, f ->
            val p = precisao[f]
            // 100% de primeira = 1; 50% = 6; nunca respondida no histórico = 3.
            var peso = if (p == null) 3.0 else 1.0 + (100 - p) / 10.0
            // As fases do topo da janela (as mais difíceis liberadas) ganham força com acertos seguidos.
            if (janela.size > 1) peso *= 1.0 + (acertosSeguidos.coerceAtMost(6) / 3.0) * (i.toDouble() / (janela.size - 1))
            if (f == ultima && janela.size > 1) peso *= 0.15
            f to peso
        }.toMap()
    }

    /** Sorteia a próxima fase pelos pesos. */
    fun sortearFase(): Fase {
        val p = pesos()
        var r = random.nextDouble() * p.values.sum()
        for ((f, peso) in p) {
            r -= peso
            if (r <= 0) return f
        }
        return p.keys.last()
    }

    /** Conta um resultado (acertou de primeira?) e ajusta a dificuldade. */
    fun registrar(acertou: Boolean) {
        if (acertou) {
            acertosSeguidos++
            errosSeguidos = 0
            if (acertosSeguidos % ACERTOS_PARA_SUBIR == 0 && teto < fases.size) {
                teto = (teto + PASSO_DO_TETO).coerceAtMost(fases.size)
                nivel++
                subiu = true
            }
        } else {
            errosSeguidos++
            acertosSeguidos = 0
            if (errosSeguidos >= ERROS_PARA_DESCER) {
                teto = (teto - PASSO_DO_TETO).coerceAtLeast(minOf(MINIMO_DO_TETO, fases.size))
                nivel = (nivel - 1).coerceAtLeast(1)
                errosSeguidos = 0
            }
        }
    }

    /**
     * A próxima pergunta. [resultados] são todos os resultados da partida até agora (o mesmo que a
     * tela de jogo guarda); os que ainda não foram contados ajustam a dificuldade antes do sorteio.
     */
    fun proxima(resultados: List<Boolean> = emptyList()): Pergunta {
        while (resultadosVistos < resultados.size) registrar(resultados[resultadosVistos++])
        val fase = sortearFase()
        _feitas += fase
        val p = gerador.perguntaDaFase(fase)
        if (!subiu) return p
        subiu = false
        return p.copy(fala = listOf(pt("Você está indo muito bem! Agora vêm perguntas mais difíceis.")) + p.fala)
    }

    companion object {
        const val ACERTOS_PARA_SUBIR = 3
        const val ERROS_PARA_DESCER = 2
        const val PASSO_DO_TETO = 3
        const val MINIMO_DO_TETO = 3

        /** Começa com mais ou menos 60% das fases vencidas (pelo menos 3). */
        fun inicioDoTeto(total: Int): Int = maxOf(minOf(MINIMO_DO_TETO, total), (total * 0.6).toInt()).coerceAtMost(total)
    }
}

/** Resultado do treino para guardar: moedinhas e histórico de cada fase que caiu. */
data class ResultadoDoTreino(val estado: EstadoJogo, val moedas: Int, val acertos: Int, val total: Int)

/**
 * Guarda um treino: uma moedinha por acerto de primeira, dentro do limite do dia
 * ([EstadoJogo.MOEDAS_POR_DIA], o mesmo das fases); o histórico de cada fase recebe as respostas
 * (assim o treino também tira a fase da lista "para reforçar" dos pais). Treino não dá estrelas nem peças.
 */
fun EstadoJogo.registrarTreino(respostas: List<Pair<Fase, Boolean>>, dia: Long): ResultadoDoTreino {
    if (respostas.isEmpty()) return ResultadoDoTreino(this, 0, 0, 0)
    val acertos = respostas.count { it.second }
    val hojeJa = if (diaDasMoedas == dia) moedasHoje else 0
    val ganhas = acertos.coerceAtMost((EstadoJogo.MOEDAS_POR_DIA - hojeJa).coerceAtLeast(0))
    var novoHistorico = historico
    for ((fase, acertou) in respostas) {
        val k = EstadoJogo.chave(fase.trilha, fase.numero)
        novoHistorico = novoHistorico + (k to ((novoHistorico[k] ?: "") + if (acertou) "1" else "0").takeLast(10))
    }
    val trilhas = respostas.groupBy { it.first.trilha }
    val novo = copy(
        moedas = moedas + ganhas,
        moedasHoje = hojeJa + ganhas,
        diaDasMoedas = dia,
        historico = novoHistorico,
        respondidas = respondidas + trilhas.mapValues { (t, r) -> (respondidas[t] ?: 0) + r.size },
        acertosDePrimeira = acertosDePrimeira + trilhas.mapValues { (t, r) -> (acertosDePrimeira[t] ?: 0) + r.count { it.second } },
    )
    return ResultadoDoTreino(novo, ganhas, acertos, respostas.size)
}
