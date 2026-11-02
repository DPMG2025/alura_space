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

class PlacaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as FipeApp).repositorio
    private val salvos = (app as FipeApp).salvos
    private val _estado = MutableStateFlow(PlacaUi())
    val estado: StateFlow<PlacaUi> = _estado
    val itensSalvos: StateFlow<List<ConsultaSalva>> = salvos.itens
    val historico = HistoricoController(viewModelScope, repo)
    private var job: Job? = null

    fun alterarPlaca(texto: String) {
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
        _estado.value = PlacaUi(placa = v.placa, veiculo = v, doSalvo = true)
        historico.limpar()
        v.fipe.firstOrNull()?.let { historico.carregar(it) }
    }

    fun limpar() {
        job?.cancel()
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
        historico.carregar(alvo)
    }
}
