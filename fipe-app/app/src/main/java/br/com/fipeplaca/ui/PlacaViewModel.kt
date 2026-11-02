package br.com.fipeplaca.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fipeplaca.FipeApp
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.Placa
import br.com.fipeplaca.data.VeiculoPlaca
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacaUi(
    val placa: String = "",
    val carregando: Boolean = false,
    val veiculo: VeiculoPlaca? = null,
    val erro: String? = null,
    val semToken: Boolean = false,
    val selecionado: Int = 0,
    /** true quando o resultado veio dos salvos, sem nova consulta. */
    val doSalvo: Boolean = false,
)

class PlacaViewModel(app: Application, private val salvo: androidx.lifecycle.SavedStateHandle) : AndroidViewModel(app) {
    private val repo = (app as FipeApp).repositorio
    private val salvos = (app as FipeApp).salvos
    // A placa digitada sobrevive se o sistema encerrar o app em segundo plano.
    private val _estado = MutableStateFlow(PlacaUi(placa = salvo.get<String>(CHAVE_PLACA).orEmpty()))
    val estado: StateFlow<PlacaUi> = _estado
    val itensSalvos: StateFlow<List<ConsultaSalva>> = salvos.itens
    private fun idAtual(): String? = _estado.value.veiculo?.let { ConsultaSalva.idPlaca(it.placa) }

    /** Historico local por veiculo e versao FIPE (a placa pode ter mais de uma versao). */
    val historico = HistoricoController(
        viewModelScope, repo,
        locais = { r -> if (r == versaoAtual()) idAtual()?.let(salvos::pontosLocais).orEmpty() else emptyList() },
        aoCarregar = { r, pontos -> if (r == versaoAtual()) idAtual()?.let { salvos.guardarPontos(it, pontos) } },
    )
    private var job: Job? = null

    private fun versaoAtual() = _estado.value.let { e -> e.veiculo?.fipe?.getOrNull(e.selecionado) }

    fun alterarPlaca(texto: String) {
        if (_estado.value.carregando) return
        salvo[CHAVE_PLACA] = Placa.normalizar(texto)
        _estado.update { it.copy(placa = Placa.normalizar(texto), erro = null, semToken = false) }
    }

    fun consultar() {
        val placa = _estado.value.placa
        if (!Placa.valida(placa)) {
            _estado.update { it.copy(erro = "Placa inválida. Use o formato ABC1234 ou ABC1D23 (Mercosul).") }
            return
        }
        if (!repo.temTokenPlaca()) {
            _estado.update { it.copy(semToken = true, erro = null) }
            return
        }
        job?.cancel()
        historico.limpar()
        _estado.update { PlacaUi(placa = placa, carregando = true) }
        job = viewModelScope.launch {
            runCatching { repo.consultarPlaca(placa) }
                .onSuccess { v ->
                    _estado.update { it.copy(carregando = false, veiculo = v) }
                    salvos.registrar(ConsultaSalva(ConsultaSalva.idPlaca(v.placa), System.currentTimeMillis(), veiculo = v))
                    v.fipe.firstOrNull()?.let { historico.carregar(it) }
                }
                .onFailure { e ->
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    _estado.update { it.copy(carregando = false, erro = FipeRepositorio.mensagemErro(e)) }
                }
        }
    }

    /** Reabre uma consulta salva sem gastar credito do servico de placas. */
    fun abrirSalvo(c: ConsultaSalva) {
        val v = c.veiculo ?: return
        job?.cancel()
        val versao = (c.versao ?: 0).coerceIn(0, (v.fipe.size - 1).coerceAtLeast(0))
        salvo[CHAVE_PLACA] = v.placa
        _estado.value = PlacaUi(placa = v.placa, veiculo = v, doSalvo = true, selecionado = versao)
        historico.limpar()
        v.fipe.getOrNull(versao)?.let { historico.carregar(it) }
    }

    fun limpar() {
        job?.cancel()
        salvo[CHAVE_PLACA] = ""
        historico.limpar()
        _estado.value = PlacaUi()
    }

    fun alternarFavorito() {
        val v = _estado.value.veiculo ?: return
        val id = ConsultaSalva.idPlaca(v.placa)
        if (salvos.itens.value.none { it.id == id }) {
            salvos.registrar(ConsultaSalva(id, System.currentTimeMillis(), favorito = true, veiculo = v))
        } else salvos.alternarFavorito(id)
    }

    fun selecionarVersao(indice: Int) {
        val v = _estado.value.veiculo ?: return
        val alvo = v.fipe.getOrNull(indice) ?: return
        _estado.update { it.copy(selecionado = indice) }
        salvos.definirVersao(ConsultaSalva.idPlaca(v.placa), indice)
        historico.carregar(alvo)
    }

    private companion object {
        const val CHAVE_PLACA = "placa"
    }
}
