package br.com.fipeplaca.ui

import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.MesclarHistorico
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.PontoHistorico
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoricoUi(
    val alvo: FipeResultado? = null,
    val periodo: PeriodoHistorico = PeriodoHistorico.MESES_12,
    val carregando: Boolean = false,
    val progresso: Float = 0f,
    val feitos: Int = 0,
    val total: Int = 0,
    val pontos: List<PontoHistorico> = emptyList(),
    val erro: String? = null,
    /** Meses pedidos e meses que exigem token (para avisar quando o historico vier parcial). */
    val solicitados: Int = 0,
    val exigemToken: Int = 0,
    /** true quando os pontos exibidos vieram so do aparelho (sem conexao). */
    val offline: Boolean = false,
)

/** Carrega a evolucao do preco FIPE de um veiculo; usado pelas duas telas de busca. */
class HistoricoController(
    private val escopo: CoroutineScope,
    private val repo: FipeRepositorio,
    /** Valores ja guardados no aparelho para o veiculo (historico local). */
    private val locais: (FipeResultado) -> List<PontoHistorico> = { emptyList() },
    /** Chamado com os pontos obtidos, para guarda-los no aparelho. */
    private val aoCarregar: (FipeResultado, List<PontoHistorico>) -> Unit = { _, _ -> },
) {
    private val _estado = MutableStateFlow(HistoricoUi())
    val estado: StateFlow<HistoricoUi> = _estado
    private var job: Job? = null
    private val cache = mutableMapOf<Pair<String, PeriodoHistorico>, br.com.fipeplaca.data.ResultadoHistorico>()

    fun limpar() {
        job?.cancel()
        geracao++
        _estado.value = HistoricoUi(periodo = _estado.value.periodo)
    }

    /** Numero da busca atual: respostas de buscas antigas sao ignoradas. */
    private var geracao = 0

    /** Mesma regra para resultado novo e vindo do cache: mescla com os valores guardados no aparelho. */
    private fun aplicar(alvo: FipeResultado, periodo: PeriodoHistorico, res: br.com.fipeplaca.data.ResultadoHistorico): HistoricoUi {
        val pontos = MesclarHistorico.mesclar(res.pontos, locais(alvo), periodo)
        return HistoricoUi(
            alvo = alvo, periodo = periodo, pontos = pontos, solicitados = res.solicitados,
            // meses vindos do aparelho reduzem os que faltam
            exigemToken = if (pontos.size >= res.solicitados) 0 else res.exigemToken,
            erro = if (pontos.isEmpty()) "Sem histórico para o período." else null,
        )
    }

    fun carregar(alvo: FipeResultado, periodo: PeriodoHistorico = _estado.value.periodo) {
        job?.cancel()
        val minha = ++geracao
        val chave = "${alvo.tipo}|${alvo.codigoFipe}|${alvo.codigoAno ?: alvo.anoModelo}|${alvo.combustivel}" to periodo
        cache[chave]?.let {
            _estado.value = aplicar(alvo, periodo, it)
            return
        }
        _estado.value = HistoricoUi(alvo = alvo, periodo = periodo, carregando = true)
        job = escopo.launch {
            runCatching {
                repo.historico(alvo, periodo, progresso = { feitos, total ->
                    if (minha == geracao) {
                        _estado.update { it.copy(progresso = if (total == 0) 0f else feitos.toFloat() / total, feitos = feitos, total = total) }
                    }
                })
            }.onSuccess { res ->
                if (minha != geracao) return@onSuccess
                cache[chave] = res
                aoCarregar(alvo, res.pontos)
                _estado.value = aplicar(alvo, periodo, res)
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (minha != geracao) return@onFailure
                // Sem rede ou sem fonte: mostra o que ja esta guardado no aparelho, se houver
                val guardados = MesclarHistorico.mesclar(emptyList(), locais(alvo), periodo)
                _estado.update {
                    if (guardados.size >= 2) it.copy(carregando = false, pontos = guardados, offline = true)
                    else it.copy(carregando = false, erro = FipeRepositorio.mensagemErro(e))
                }
            }
        }
    }

    /** Esquece o cache, por exemplo depois de informar um token novo. */
    fun esquecerCache() = cache.clear()

    fun mudarPeriodo(periodo: PeriodoHistorico) {
        val alvo = _estado.value.alvo
        if (alvo == null) _estado.update { it.copy(periodo = periodo) } else carregar(alvo, periodo)
    }
}
