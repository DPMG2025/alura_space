package br.com.fipeplaca.ui

import androidx.lifecycle.viewModelScope
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.Placa
import br.com.fipeplaca.data.Texto
import br.com.fipeplaca.data.VeiculoPlaca
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
    /** Motivo quando nao foi possivel conferir o valor atual de um salvo (mostra o guardado). */
    val avisoAtualizacao: String? = null,
    /** true quando a placa ja estava nos salvos e foi aberta sem consultar o provedor pago. */
    val abertoSemCusto: Boolean = false,
)

/** Dependencias recebidas pelo construtor (fabrica em [br.com.fipeplaca.FipeApp]); testes passam simulados. */
class PlacaViewModel(
    private val repo: FipeRepositorio,
    private val salvos: br.com.fipeplaca.data.RepositorioSalvos,
    private val salvo: androidx.lifecycle.SavedStateHandle,
) : androidx.lifecycle.ViewModel() {
    // A placa digitada sobrevive se o sistema encerrar o app em segundo plano.
    private val _estado = MutableStateFlow(PlacaUi(placa = salvo.get<String>(CHAVE_PLACA).orEmpty()))
    val estado: StateFlow<PlacaUi> = _estado
    val itensSalvos: StateFlow<List<ConsultaSalva>> = salvos.itens
    private fun idAtual(): String? = _estado.value.veiculo?.let { ConsultaSalva.idPlaca(it.placa) }

    /** Historico local por veiculo e versao FIPE (a placa pode ter mais de uma versao). */
    val historico = HistoricoController(
        viewModelScope, repo,
        locais = { r -> if (mesmaVersao(r)) idAtual()?.let(salvos::pontosLocais).orEmpty() else emptyList() },
        aoCarregar = { r, pontos -> if (mesmaVersao(r)) idAtual()?.let { salvos.guardarPontos(it, pontos) } },
    )
    private var job: Job? = null

    init {
        // Depois de o sistema encerrar o app, reabre o resultado que estava na tela (vem dos salvos).
        salvo.get<String>(CHAVE_CONSULTA)?.takeIf { it.isNotBlank() }?.let { id ->
            viewModelScope.launch {
                val c = kotlinx.coroutines.withTimeoutOrNull(3_000) { salvos.itens.first { l -> l.any { it.id == id } } }
                    ?.firstOrNull { it.id == id }
                if (c != null && _estado.value.veiculo == null) abrirSalvo(c, atualizar = false)
            }
        }
    }

    private fun versaoAtual() = _estado.value.let { e -> e.veiculo?.fipe?.getOrNull(e.selecionado) }

    /** Compara pela identidade da versao (codigo FIPE e ano), nao pelo valor, que muda a cada mes. */
    private fun mesmaVersao(r: br.com.fipeplaca.data.FipeResultado) =
        versaoAtual()?.let { it.codigoFipe == r.codigoFipe && Texto.chave(it.anoModelo).filter(Char::isDigit) == Texto.chave(r.anoModelo).filter(Char::isDigit) } == true

    fun alterarPlaca(texto: String) {
        if (_estado.value.carregando) return
        salvo[CHAVE_PLACA] = Placa.normalizar(texto)
        _estado.update { it.copy(placa = Placa.normalizar(texto), erro = null, semToken = false) }
    }

    /**
     * Consulta a placa. Se ela ja estiver nos salvos, abre de la e atualiza o valor pela FIPE
     * (sem gastar credito do provedor); [forcarProvedor] consulta o provedor mesmo assim.
     */
    fun consultar(forcarProvedor: Boolean = false) {
        val placa = _estado.value.placa
        if (!forcarProvedor && Placa.valida(placa)) {
            salvos.buscar(ConsultaSalva.idPlaca(placa))?.takeIf { it.veiculo != null }?.let { c ->
                abrirSalvo(c)
                _estado.update { it.copy(abertoSemCusto = true) }
                return
            }
        }
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
        salvo[CHAVE_CONSULTA] = "" // se o processo morrer agora, nao reabre o veiculo anterior
        job = viewModelScope.launch {
            runCatching { repo.consultarPlaca(placa) }
                .onSuccess { v ->
                    val id = ConsultaSalva.idPlaca(v.placa)
                    // Abre na versao que o usuario ja tinha escolhido para esta placa
                    val versao = (kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { salvos.versaoSalva(id) } ?: 0)
                        .coerceIn(0, (v.fipe.size - 1).coerceAtLeast(0))
                    _estado.update { it.copy(carregando = false, veiculo = v, selecionado = versao) }
                    salvo[CHAVE_CONSULTA] = id
                    salvos.registrar(ConsultaSalva(id, System.currentTimeMillis(), veiculo = v, versao = versao))
                    v.fipe.getOrNull(versao)?.let { historico.carregar(it) }
                }
                .onFailure { e ->
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    _estado.update { it.copy(carregando = false, erro = MensagensErro.de(e)) }
                }
        }
    }

    /**
     * Reabre uma consulta salva sem gastar credito do servico de placas e, em seguida,
     * atualiza o valor FIPE pelo codigo (o valor guardado aparece ate chegar o novo).
     */
    fun abrirSalvo(c: ConsultaSalva, atualizar: Boolean = true) {
        val v = c.veiculo ?: return
        job?.cancel()
        val versao = (c.versao ?: 0).coerceIn(0, (v.fipe.size - 1).coerceAtLeast(0))
        salvo[CHAVE_PLACA] = v.placa
        salvo[CHAVE_CONSULTA] = c.id
        _estado.value = PlacaUi(placa = v.placa, veiculo = v, doSalvo = true, selecionado = versao)
        historico.limpar()
        v.fipe.getOrNull(versao)?.let { historico.carregar(it) }
        if (!atualizar) return
        job = viewModelScope.launch {
            val tentativa = runCatching { repo.atualizarValores(v) }
                .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }
            if (_estado.value.veiculo?.placa != v.placa) return@launch
            val novo = tentativa.getOrNull()
            if (novo == null) {
                // Nao deu para conferir o valor agora: a tela diz o motivo e que mostra o valor guardado
                val motivo = MensagensErro.de(tentativa.exceptionOrNull()!!)
                _estado.update { it.copy(avisoAtualizacao = "$motivo Mostrando o valor guardado.", doSalvo = false) }
                return@launch
            }
            _estado.update { it.copy(veiculo = novo, doSalvo = false, avisoAtualizacao = null) }
            // Versao do momento (o usuario pode ter trocado enquanto atualizava)
            val versaoAgora = _estado.value.selecionado
            if (novo != v) salvos.registrar(ConsultaSalva(c.id, System.currentTimeMillis(), veiculo = novo, versao = versaoAgora))
            // O historico foi carregado com o valor antigo: recarrega para o ultimo ponto bater com o cartao
            if (novo.fipe.getOrNull(versaoAgora)?.mesReferencia != v.fipe.getOrNull(versaoAgora)?.mesReferencia) {
                novo.fipe.getOrNull(versaoAgora)?.let { historico.carregar(it) }
            }
        }
    }

    fun limpar() {
        job?.cancel()
        salvo[CHAVE_PLACA] = ""
        salvo[CHAVE_CONSULTA] = ""
        historico.limpar()
        _estado.value = PlacaUi()
    }

    fun alternarFavorito() {
        val v = _estado.value.veiculo ?: return
        val id = ConsultaSalva.idPlaca(v.placa)
        salvos.alternarOuCriarFavorito(ConsultaSalva(id, System.currentTimeMillis(), veiculo = v, versao = _estado.value.selecionado))
    }

    fun selecionarVersao(indice: Int) {
        val v = _estado.value.veiculo ?: return
        val alvo = v.fipe.getOrNull(indice) ?: return
        _estado.update { it.copy(selecionado = indice) }
        salvos.definirVersao(ConsultaSalva.idPlaca(v.placa), indice)
        historico.carregar(alvo)
    }

    /** Atualiza o valor FIPE do veiculo exibido pelo codigo FIPE (sem consultar o provedor pago). */
    fun atualizarValor() {
        val v = _estado.value.veiculo ?: return
        val id = ConsultaSalva.idPlaca(v.placa)
        abrirSalvo(salvos.buscar(id) ?: ConsultaSalva(id, System.currentTimeMillis(), veiculo = v, versao = _estado.value.selecionado))
    }

    /** "Tentar novamente" do historico: busca de novo ignorando o cache. */
    fun recarregarHistorico() = historico.recarregar()

    private companion object {
        const val CHAVE_PLACA = "placa"
        const val CHAVE_CONSULTA = "consulta"
    }
}
