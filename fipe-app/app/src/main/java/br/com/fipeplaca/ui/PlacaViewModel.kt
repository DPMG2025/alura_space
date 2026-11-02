package br.com.fipeplaca.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fipeplaca.FipeApp
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
)

class PlacaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as FipeApp).repositorio
    private val _estado = MutableStateFlow(PlacaUi())
    val estado: StateFlow<PlacaUi> = _estado
    val historico = HistoricoController(viewModelScope, repo)
    private var job: Job? = null

    fun alterarPlaca(texto: String) {
        _estado.update { it.copy(placa = Placa.normalizar(texto), erro = null) }
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
        _estado.update { it.copy(carregando = true, erro = null, veiculo = null, semToken = false, selecionado = 0) }
        job = viewModelScope.launch {
            runCatching { repo.consultarPlaca(placa) }
                .onSuccess { v ->
                    _estado.update { it.copy(carregando = false, veiculo = v) }
                    v.fipe.firstOrNull()?.let { historico.carregar(it) }
                }
                .onFailure { e ->
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    _estado.update { it.copy(carregando = false, erro = FipeRepositorio.mensagemErro(e)) }
                }
        }
    }

    /** Quando a placa tem mais de uma versao FIPE possivel, o usuario escolhe qual ver no historico. */
    fun selecionarVersao(indice: Int) {
        val v = _estado.value.veiculo ?: return
        val alvo = v.fipe.getOrNull(indice) ?: return
        _estado.update { it.copy(selecionado = indice) }
        historico.carregar(alvo)
    }
}
