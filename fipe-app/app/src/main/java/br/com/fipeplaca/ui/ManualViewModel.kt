package br.com.fipeplaca.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fipeplaca.FipeApp
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.ItemFipe
import br.com.fipeplaca.data.TipoVeiculo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Etapa { MARCAS, MODELOS, ANOS, RESULTADO }

data class ManualUi(
    val tipo: TipoVeiculo = TipoVeiculo.CARROS,
    val etapa: Etapa = Etapa.MARCAS,
    val marcas: List<ItemFipe> = emptyList(),
    val modelos: List<ItemFipe> = emptyList(),
    val anos: List<ItemFipe> = emptyList(),
    val marca: ItemFipe? = null,
    val modelo: ItemFipe? = null,
    val ano: ItemFipe? = null,
    val resultado: FipeResultado? = null,
    val filtro: String = "",
    val carregando: Boolean = false,
    val erro: String? = null,
)

class ManualViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as FipeApp).repositorio
    private val _estado = MutableStateFlow(ManualUi())
    val estado: StateFlow<ManualUi> = _estado
    val historico = HistoricoController(viewModelScope, repo)
    private var job: Job? = null

    init { carregarMarcas() }

    private fun executar(bloco: suspend () -> Unit) {
        job?.cancel()
        _estado.update { it.copy(carregando = true, erro = null) }
        job = viewModelScope.launch {
            runCatching { bloco() }
                .onSuccess { _estado.update { it.copy(carregando = false) } }
                .onFailure { e ->
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    _estado.update { it.copy(carregando = false, erro = FipeRepositorio.mensagemErro(e)) }
                }
        }
    }

    private fun carregarMarcas() = executar {
        val tipo = _estado.value.tipo
        val lista = repo.marcas(tipo)
        _estado.update { it.copy(marcas = lista) }
    }

    fun selecionarTipo(tipo: TipoVeiculo, filtro: String = "") {
        historico.limpar()
        _estado.value = ManualUi(tipo = tipo, filtro = filtro)
        carregarMarcas()
    }

    fun alterarFiltro(texto: String) = _estado.update { it.copy(filtro = texto) }

    fun selecionarMarca(m: ItemFipe) {
        _estado.update { it.copy(marca = m, etapa = Etapa.MODELOS, filtro = "", modelos = emptyList()) }
        executar {
            val lista = repo.modelos(_estado.value.tipo, m.codigo)
            _estado.update { it.copy(modelos = lista) }
        }
    }

    fun selecionarModelo(m: ItemFipe) {
        val s = _estado.value
        _estado.update { it.copy(modelo = m, etapa = Etapa.ANOS, filtro = "", anos = emptyList()) }
        executar {
            val lista = repo.anos(s.tipo, s.marca!!.codigo, m.codigo)
            _estado.update { it.copy(anos = lista) }
        }
    }

    fun selecionarAno(a: ItemFipe) {
        val s = _estado.value
        _estado.update { it.copy(ano = a, etapa = Etapa.RESULTADO, filtro = "", resultado = null) }
        historico.limpar()
        executar {
            val r = repo.valor(s.tipo, s.marca!!.codigo, s.modelo!!.codigo, a.codigo)
            _estado.update { it.copy(resultado = r) }
            historico.carregar(r)
        }
    }

    /** Volta uma etapa. Retorna false quando ja esta na primeira. */
    fun voltar(): Boolean {
        val s = _estado.value
        job?.cancel()
        val nova = when (s.etapa) {
            Etapa.MARCAS -> return false
            Etapa.MODELOS -> s.copy(etapa = Etapa.MARCAS, modelo = null)
            Etapa.ANOS -> s.copy(etapa = Etapa.MODELOS, ano = null)
            Etapa.RESULTADO -> s.copy(etapa = Etapa.ANOS, resultado = null).also { historico.limpar() }
        }
        _estado.value = nova.copy(filtro = "", carregando = false, erro = null)
        return true
    }

    fun tentarNovamente() {
        val s = _estado.value
        when (s.etapa) {
            Etapa.MARCAS -> carregarMarcas()
            Etapa.MODELOS -> s.marca?.let(::selecionarMarca)
            Etapa.ANOS -> s.modelo?.let(::selecionarModelo)
            Etapa.RESULTADO -> s.ano?.let(::selecionarAno)
        }
    }

    /** Abre a busca manual ja filtrando pela marca vinda da placa (ex.: "VW", "FIAT"). */
    fun preencherPorPlaca(tipo: TipoVeiculo, marca: String?) {
        val chave = marca?.trim()?.removePrefix("I/")?.substringBefore('/')?.trim().orEmpty()
        selecionarTipo(tipo, chave)
    }
}
