package br.com.fipeplaca.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Lista de consultas salvas, persistida em JSON nas SharedPreferences.
 * Leitura, alteracoes e gravacao acontecem numa fila propria (fora da thread principal),
 * sempre em ordem; a interface observa o resultado pelo StateFlow.
 */
class RepositorioSalvos(context: Context) {
    private val sp = context.getSharedPreferences("salvos", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val fila = Executors.newSingleThreadExecutor { r -> Thread(r, "salvos").apply { isDaemon = true } }
    private val _itens = MutableStateFlow<List<ConsultaSalva>>(emptyList())
    val itens: StateFlow<List<ConsultaSalva>> = _itens
    private val _carregado = MutableStateFlow(false)
    /** false ate a primeira leitura terminar (a tela nao mostra "vazio" antes disso). */
    val carregado: StateFlow<Boolean> = _carregado

    init { fila.execute { _itens.value = ler(); _carregado.value = true } }

    /**
     * Le os salvos sem nunca perder dados: se o JSON nao puder ser lido (corrompido ou de um formato
     * incompativel), o conteudo bruto e guardado numa chave de seguranca antes de qualquer gravacao.
     * Itens incompletos (registros antigos) sao descartados um a um, sem perder os demais.
     */
    private fun ler(): List<ConsultaSalva> {
        val json = sp.getString(CHAVE, null) ?: return emptyList()
        return try {
            val tipo = object : TypeToken<List<ConsultaSalva?>>() {}.type
            gson.fromJson<List<ConsultaSalva?>>(json, tipo).orEmpty().mapNotNull(::validar)
        } catch (e: Exception) {
            // Uma copia por conteudo: reabrir o app nao acumula copias iguais
            val jaGuardado = sp.all.any { (k, v) -> k.startsWith("${CHAVE}_seguranca_") && v == json }
            if (!jaGuardado) sp.edit().putString("${CHAVE}_seguranca_${System.currentTimeMillis()}", json).apply()
            android.util.Log.w("RepositorioSalvos", "Salvos ilegiveis guardados em copia de seguranca", e)
            emptyList()
        }
    }

    /** O Gson ignora a nulidade do Kotlin: confere os campos obrigatorios de cada item. */
    @Suppress("SENSELESS_COMPARISON")
    private fun validar(c: ConsultaSalva?): ConsultaSalva? {
        if (c == null || c.id == null) return null
        val r = c.resultado
        if (r != null && (r.tipo == null || r.codigoFipe == null)) return null
        val v = c.veiculo
        if (v != null && (v.placa == null || v.tipo == null || v.fipe == null || v.fipe.any { it == null || it.tipo == null })) return null
        if (r == null && v == null) return null
        return c
    }

    /** Aplica uma alteracao na fila: le o estado atual, calcula o novo e grava. */
    private fun alterar(bloco: (List<ConsultaSalva>) -> List<ConsultaSalva>) = fila.execute {
        val nova = bloco(_itens.value)
        if (nova != _itens.value) {
            _itens.value = nova
            sp.edit().putString(CHAVE, gson.toJson(nova)).apply()
        }
    }

    fun registrar(nova: ConsultaSalva) = alterar { atual -> comRegistro(atual, nova) }

    /** Insere ou atualiza um item, mantendo favorito, versao, historico local e o valor do dia em que foi salvo. */
    private fun comRegistro(atual: List<ConsultaSalva>, nova: ConsultaSalva): List<ConsultaSalva> {
        val antigo = atual.firstOrNull { it.id == nova.id }
        val versao = nova.versao ?: antigo?.versao
        // Historico local e por versao: se a versao mudou, recomeca
        val base = if (antigo != null && antigo.versao == versao) antigo.historicoLocal.orEmpty() else emptyList()
        val item = nova.copy(favorito = antigo?.favorito ?: nova.favorito, versao = versao)
        val ponto = item.principal?.let { r -> Dinheiro.paraDouble(r.valor)?.let { PontoHistorico(r.mesReferencia, r.valor.orEmpty(), it) } }
        // Valor do dia em que foi salvo: fica fixo (recomeca so se a versao mudar)
        val aoSalvar = (if (antigo != null && antigo.versao == versao) antigo.valorAoSalvar else null) ?: ponto
        return OrganizarSalvos.inserir(
            atual,
            item.copy(historicoLocal = MesclarHistorico.acumular(base + nova.historicoLocal.orEmpty(), listOfNotNull(ponto)), valorAoSalvar = aoSalvar),
            LIMITE_RECENTES,
        )
    }

    fun alternarFavorito(id: String) = alterar { l -> l.map { if (it.id == id) it.copy(favorito = !it.favorito) else it } }

    /**
     * Alterna o favorito decidindo dentro da fila (sem corrida com uma gravacao ainda pendente):
     * se o item ainda nao existe, cria ja como favorito.
     */
    fun alternarOuCriarFavorito(novo: ConsultaSalva) = alterar { l ->
        if (l.any { it.id == novo.id }) l.map { if (it.id == novo.id) it.copy(favorito = !it.favorito) else it }
        else comRegistro(l, novo.copy(favorito = true))
    }

    fun remover(id: String) = alterar { l -> l.filterNot { it.id == id } }

    fun limparRecentes() = alterar { l -> l.filter { it.favorito } }

    /** Guarda no aparelho os valores mensais obtidos para um veiculo ja salvo. */
    fun guardarPontos(id: String, pontos: List<PontoHistorico>) {
        if (pontos.isEmpty()) return
        alterar { l -> l.map { if (it.id == id) it.copy(historicoLocal = MesclarHistorico.acumular(it.historicoLocal.orEmpty(), pontos)) else it } }
    }

    /** Guarda a versao FIPE escolhida e recomeca o historico local (que e por versao). */
    fun definirVersao(id: String, versao: Int) = alterar { l ->
        l.map {
            if (it.id == id && it.versao != versao) {
                val nova = it.copy(versao = versao, historicoLocal = emptyList())
                val r = nova.principal
                nova.copy(valorAoSalvar = r?.let { x -> Dinheiro.paraDouble(x.valor)?.let { PontoHistorico(x.mesReferencia, x.valor.orEmpty(), it) } })
            } else it
        }
    }

    /** Desfaz uma exclusao: devolve o item ao topo, respeitando o limite de recentes. */
    fun restaurar(item: ConsultaSalva) = alterar { l -> OrganizarSalvos.inserir(l, item, LIMITE_RECENTES) }

    /** Desfaz varias exclusoes de uma vez, devolvendo cada item a posicao em que estava. */
    fun restaurarNasPosicoes(itens: List<Pair<Int, ConsultaSalva>>) = alterar { l ->
        val nova = l.filterNot { atual -> itens.any { it.second.id == atual.id } }.toMutableList()
        itens.sortedBy { it.first }.forEach { (i, item) -> nova.add(i.coerceIn(0, nova.size), item) }
        nova
    }

    /** Posicao atual de um item (para desfazer a exclusao no mesmo lugar). */
    fun posicao(id: String): Int = _itens.value.indexOfFirst { it.id == id }

    fun pontosLocais(id: String): List<PontoHistorico> = _itens.value.firstOrNull { it.id == id }?.historicoLocal.orEmpty()

    /** Versao escolhida, depois de terminar as gravacoes pendentes (chamar fora da thread principal). */
    fun versaoSalva(id: String): Int? { aguardar(); return _itens.value.firstOrNull { it.id == id }?.versao }

    fun buscar(id: String): ConsultaSalva? = _itens.value.firstOrNull { it.id == id }

    fun favorito(id: String): Boolean = _itens.value.any { it.id == id && it.favorito }

    /** Espera a fila terminar (usado nos testes). */
    fun aguardar() { fila.submit {}.get(5, TimeUnit.SECONDS) }

    /** Apaga tudo (usado nos testes de ponta a ponta). */
    fun limparTudo() = alterar { emptyList() }

    companion object {
        private const val CHAVE = "consultas"
        const val LIMITE_RECENTES = 30
    }
}
