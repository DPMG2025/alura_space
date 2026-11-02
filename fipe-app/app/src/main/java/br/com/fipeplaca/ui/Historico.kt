package br.com.fipeplaca.ui

import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.FipeResultado
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
    val pontos: List<PontoHistorico> = emptyList(),
    val erro: String? = null,
)

/** Carrega a evolucao do preco FIPE de um veiculo; usado pelas duas telas de busca. */
class HistoricoController(
    private val escopo: CoroutineScope,
    private val repo: FipeRepositorio,
) {
    private val _estado = MutableStateFlow(HistoricoUi())
    val estado: StateFlow<HistoricoUi> = _estado
    private var job: Job? = null
    private val cache = mutableMapOf<Pair<String, PeriodoHistorico>, List<PontoHistorico>>()

    fun limpar() {
        job?.cancel()
        _estado.value = HistoricoUi(periodo = _estado.value.periodo)
    }

    fun carregar(alvo: FipeResultado, periodo: PeriodoHistorico = _estado.value.periodo) {
        job?.cancel()
        val chave = "${alvo.tipo}|${alvo.codigoFipe}|${alvo.codigoAno ?: alvo.anoModelo}|${alvo.combustivel}" to periodo
        cache[chave]?.let {
            _estado.value = HistoricoUi(alvo = alvo, periodo = periodo, pontos = it)
            return
        }
        _estado.value = HistoricoUi(alvo = alvo, periodo = periodo, carregando = true)
        job = escopo.launch {
            runCatching {
                repo.historico(alvo, periodo) { feitos, total ->
                    _estado.update { it.copy(progresso = if (total == 0) 0f else feitos.toFloat() / total) }
                }
            }.onSuccess { pontos ->
                cache[chave] = pontos
                _estado.update { it.copy(carregando = false, pontos = pontos, erro = if (pontos.isEmpty()) "Sem histórico para o período." else null) }
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                _estado.update { it.copy(carregando = false, erro = FipeRepositorio.mensagemErro(e)) }
            }
        }
    }

    fun mudarPeriodo(periodo: PeriodoHistorico) {
        val alvo = _estado.value.alvo
        if (alvo == null) _estado.update { it.copy(periodo = periodo) } else carregar(alvo, periodo)
    }
}
