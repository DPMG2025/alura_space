package br.com.fipeplaca.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.fipeplaca.FipeApp
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.ItemFipe
import br.com.fipeplaca.data.TipoVeiculo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Etapa(val rotulo: String) { MARCAS("Marca"), MODELOS("Modelo"), ANOS("Ano"), RESULTADO("Valor") }

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
    private val salvos = (app as FipeApp).salvos
    private val _estado = MutableStateFlow(ManualUi())
    val estado: StateFlow<ManualUi> = _estado
    val itensSalvos: StateFlow<List<ConsultaSalva>> = salvos.itens
    val historico = HistoricoController(
        viewModelScope, repo,
        locais = { r -> salvos.pontosLocais(ConsultaSalva.idManual(r)) },
        aoCarregar = { r, pontos -> salvos.guardarPontos(ConsultaSalva.idManual(r), pontos) },
    )
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
        val lista = repo.marcas(_estado.value.tipo)
        _estado.update { it.copy(marcas = lista) }
    }

    private fun carregarModelos() = executar {
        val s = _estado.value
        val lista = repo.modelos(s.tipo, s.marca!!.codigo)
        _estado.update { it.copy(modelos = lista) }
    }

    private fun carregarAnos() = executar {
        val s = _estado.value
        val lista = repo.anos(s.tipo, s.marca!!.codigo, s.modelo!!.codigo)
        _estado.update { it.copy(anos = lista) }
    }

    private fun carregarValor() = executar {
        val s = _estado.value
        val r = repo.valor(s.tipo, s.marca!!.codigo, s.modelo!!.codigo, s.ano!!.codigo)
        _estado.update { it.copy(resultado = r) }
        salvos.registrar(
            ConsultaSalva(ConsultaSalva.idManual(r), System.currentTimeMillis(), resultado = r,
                marca = s.marca, modelo = s.modelo, ano = s.ano)
        )
        historico.carregar(r)
    }

    fun selecionarTipo(tipo: TipoVeiculo, filtro: String = "") {
        historico.limpar()
        _estado.value = ManualUi(tipo = tipo, filtro = filtro)
        carregarMarcas()
    }

    fun alterarFiltro(texto: String) = _estado.update { it.copy(filtro = texto) }

    fun selecionarMarca(m: ItemFipe) {
        _estado.update { it.copy(marca = m, modelo = null, ano = null, etapa = Etapa.MODELOS, filtro = "", modelos = emptyList()) }
        carregarModelos()
    }

    fun selecionarModelo(m: ItemFipe) {
        _estado.update { it.copy(modelo = m, ano = null, etapa = Etapa.ANOS, filtro = "", anos = emptyList()) }
        carregarAnos()
    }

    fun selecionarAno(a: ItemFipe) {
        historico.limpar()
        _estado.update { it.copy(ano = a, etapa = Etapa.RESULTADO, filtro = "", resultado = null) }
        carregarValor()
    }

    /** Volta uma etapa. Retorna false quando ja esta na primeira. */
    fun voltar(): Boolean {
        val s = _estado.value
        if (s.etapa == Etapa.MARCAS) return false
        job?.cancel()
        // Sem a trilha completa (salvo antigo), recomeca pela escolha da marca.
        if (s.marca == null || (s.etapa >= Etapa.ANOS && s.modelo == null)) {
            novaConsulta()
            return true
        }
        val nova = when (s.etapa) {
            Etapa.MARCAS -> return false
            Etapa.MODELOS -> s.copy(etapa = Etapa.MARCAS, marca = null, modelo = null)
            Etapa.ANOS -> s.copy(etapa = Etapa.MODELOS, modelo = null, ano = null)
            Etapa.RESULTADO -> s.copy(etapa = Etapa.ANOS, ano = null, resultado = null).also { historico.limpar() }
        }
        _estado.value = nova.copy(filtro = "", carregando = false, erro = null)
        // Listas podem estar vazias quando a tela foi aberta a partir dos salvos.
        when (nova.etapa) {
            Etapa.MARCAS -> if (nova.marcas.isEmpty()) carregarMarcas()
            Etapa.MODELOS -> if (nova.modelos.isEmpty()) carregarModelos()
            Etapa.ANOS -> if (nova.anos.isEmpty()) carregarAnos()
            Etapa.RESULTADO -> Unit
        }
        return true
    }

    /** Vai direto para uma etapa anterior (toque no indicador de etapas). */
    fun irPara(etapa: Etapa) {
        while (_estado.value.etapa.ordinal > etapa.ordinal) if (!voltar()) break
    }

    fun tentarNovamente() {
        when (_estado.value.etapa) {
            Etapa.MARCAS -> carregarMarcas()
            Etapa.MODELOS -> carregarModelos()
            Etapa.ANOS -> carregarAnos()
            Etapa.RESULTADO -> carregarValor()
        }
    }

    fun novaConsulta() = selecionarTipo(_estado.value.tipo)

    fun alternarFavorito() {
        val s = _estado.value
        val r = s.resultado ?: return
        val id = ConsultaSalva.idManual(r)
        if (salvos.itens.value.none { it.id == id }) {
            salvos.registrar(ConsultaSalva(id, System.currentTimeMillis(), favorito = true, resultado = r,
                marca = s.marca, modelo = s.modelo, ano = s.ano))
        } else salvos.alternarFavorito(id)
    }

    fun abrirSalvo(c: ConsultaSalva) {
        val r = c.resultado ?: return
        job?.cancel()
        historico.limpar()
        _estado.value = ManualUi(tipo = r.tipo, etapa = Etapa.RESULTADO, marca = c.marca, modelo = c.modelo, ano = c.ano, resultado = r)
        historico.carregar(r)
    }

    /** Abre a busca manual ja filtrando pela marca vinda da placa (ex.: "VW", "I/FIAT"). */
    fun preencherPorPlaca(tipo: TipoVeiculo, marca: String?) {
        val chave = marca?.trim()?.removePrefix("I/")?.substringBefore('/')?.trim().orEmpty()
        selecionarTipo(tipo, chave)
    }
}
