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

    init { fila.execute { _itens.value = ler() } }

    private fun ler(): List<ConsultaSalva> = runCatching {
        val json = sp.getString(CHAVE, null) ?: return emptyList()
        val tipo = object : TypeToken<List<ConsultaSalva>>() {}.type
        gson.fromJson<List<ConsultaSalva>>(json, tipo).orEmpty()
    }.getOrDefault(emptyList())

    /** Aplica uma alteracao na fila: le o estado atual, calcula o novo e grava. */
    private fun alterar(bloco: (List<ConsultaSalva>) -> List<ConsultaSalva>) = fila.execute {
        val nova = bloco(_itens.value)
        if (nova != _itens.value) {
            _itens.value = nova
            sp.edit().putString(CHAVE, gson.toJson(nova)).apply()
        }
    }

    fun registrar(nova: ConsultaSalva) = alterar { atual ->
        val antigo = atual.firstOrNull { it.id == nova.id }
        val versao = nova.versao ?: antigo?.versao
        // Historico local e por versao: se a versao mudou, recomeca
        val base = if (antigo != null && antigo.versao == versao) antigo.historicoLocal.orEmpty() else emptyList()
        val item = nova.copy(favorito = antigo?.favorito ?: nova.favorito, versao = versao)
        val pontoAtual = listOfNotNull(item.principal?.let { r ->
            Dinheiro.paraDouble(r.valor)?.let { PontoHistorico(r.mesReferencia, r.valor.orEmpty(), it) }
        })
        OrganizarSalvos.inserir(atual, item.copy(historicoLocal = MesclarHistorico.acumular(base + nova.historicoLocal.orEmpty(), pontoAtual)), LIMITE_RECENTES)
    }

    fun alternarFavorito(id: String) = alterar { l -> l.map { if (it.id == id) it.copy(favorito = !it.favorito) else it } }

    fun remover(id: String) = alterar { l -> l.filterNot { it.id == id } }

    fun limparRecentes() = alterar { l -> l.filter { it.favorito } }

    /** Guarda no aparelho os valores mensais obtidos para um veiculo ja salvo. */
    fun guardarPontos(id: String, pontos: List<PontoHistorico>) {
        if (pontos.isEmpty()) return
        alterar { l -> l.map { if (it.id == id) it.copy(historicoLocal = MesclarHistorico.acumular(it.historicoLocal.orEmpty(), pontos)) else it } }
    }

    /** Guarda a versao FIPE escolhida e recomeca o historico local (que e por versao). */
    fun definirVersao(id: String, versao: Int) = alterar { l ->
        l.map { if (it.id == id && it.versao != versao) it.copy(versao = versao, historicoLocal = emptyList()) else it }
    }

    /** Desfaz uma exclusao: devolve o item ao topo, respeitando o limite de recentes. */
    fun restaurar(item: ConsultaSalva) = alterar { l -> OrganizarSalvos.inserir(l, item, LIMITE_RECENTES) }

    fun pontosLocais(id: String): List<PontoHistorico> = _itens.value.firstOrNull { it.id == id }?.historicoLocal.orEmpty()

    fun versaoSalva(id: String): Int? = _itens.value.firstOrNull { it.id == id }?.versao

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
