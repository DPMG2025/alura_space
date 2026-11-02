package br.com.fipeplaca.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Lista de consultas salvas, persistida em JSON nas SharedPreferences. */
class RepositorioSalvos(context: Context) {
    private val sp = context.getSharedPreferences("salvos", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _itens = MutableStateFlow(ler())
    val itens: StateFlow<List<ConsultaSalva>> = _itens

    private fun ler(): List<ConsultaSalva> = runCatching {
        val json = sp.getString(CHAVE, null) ?: return emptyList()
        val tipo = object : TypeToken<List<ConsultaSalva>>() {}.type
        gson.fromJson<List<ConsultaSalva>>(json, tipo).orEmpty()
    }.getOrDefault(emptyList())

    private fun gravar(lista: List<ConsultaSalva>) {
        _itens.value = lista
        sp.edit().putString(CHAVE, gson.toJson(lista)).apply()
    }

    @Synchronized
    fun registrar(nova: ConsultaSalva) {
        val atual = _itens.value
        val antigo = atual.firstOrNull { it.id == nova.id }
        val atual = listOfNotNull(nova.principal?.let { r ->
            Dinheiro.paraDouble(r.valor)?.let { PontoHistorico(r.mesReferencia, r.valor.orEmpty(), it) }
        })
        val item = nova.copy(
            favorito = antigo?.favorito ?: nova.favorito,
            historicoLocal = MesclarHistorico.acumular(antigo?.historicoLocal.orEmpty() + nova.historicoLocal.orEmpty(), atual),
        )
        gravar(OrganizarSalvos.inserir(atual, item, LIMITE_RECENTES))
    }

    @Synchronized
    fun alternarFavorito(id: String) =
        gravar(_itens.value.map { if (it.id == id) it.copy(favorito = !it.favorito) else it })

    @Synchronized
    fun remover(id: String) = gravar(_itens.value.filterNot { it.id == id })

    @Synchronized
    fun limparRecentes() = gravar(_itens.value.filter { it.favorito })

    /** Guarda no aparelho os valores mensais obtidos para um veiculo ja salvo. */
    @Synchronized
    fun guardarPontos(id: String, pontos: List<PontoHistorico>) {
        if (pontos.isEmpty() || _itens.value.none { it.id == id }) return
        gravar(_itens.value.map { if (it.id == id) it.copy(historicoLocal = MesclarHistorico.acumular(it.historicoLocal.orEmpty(), pontos)) else it })
    }

    fun pontosLocais(id: String): List<PontoHistorico> = _itens.value.firstOrNull { it.id == id }?.historicoLocal.orEmpty()

    fun favorito(id: String): Boolean = _itens.value.any { it.id == id && it.favorito }

    companion object {
        private const val CHAVE = "consultas"
        const val LIMITE_RECENTES = 30
    }
}

