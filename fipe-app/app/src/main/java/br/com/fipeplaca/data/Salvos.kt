package br.com.fipeplaca.data

/**
 * Consulta guardada no aparelho (recente ou favorita). Guarda o resultado completo,
 * entao reabrir uma placa nao gasta credito do servico de placas.
 */
data class ConsultaSalva(
    val id: String,
    val data: Long,
    val favorito: Boolean = false,
    val veiculo: VeiculoPlaca? = null,
    val resultado: FipeResultado? = null,
    val marca: ItemFipe? = null,
    val modelo: ItemFipe? = null,
    val ano: ItemFipe? = null,
    /** Valores FIPE ja vistos para este veiculo, por mes (anulavel: registros antigos nao tem). */
    val historicoLocal: List<PontoHistorico>? = null,
    /** Versao FIPE escolhida pelo usuario quando a placa tem mais de uma (anulavel: registros antigos). */
    val versao: Int? = null,
    /** Valor e mes no momento em que o veiculo foi salvo (base da "variacao desde que salvou"). */
    val valorAoSalvar: PontoHistorico? = null,
) {
    val porPlaca: Boolean get() = veiculo != null
    val principal: FipeResultado? get() = resultado ?: veiculo?.fipe?.let { it.getOrNull(versao ?: 0) ?: it.firstOrNull() }
    val titulo: String
        get() = principal?.modelo?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(veiculo?.marca, veiculo?.modelo).joinToString(" ").ifBlank { "Veículo" }

    companion object {
        fun idPlaca(placa: String) = "P:" + Placa.normalizar(placa)
        fun idManual(r: FipeResultado) = "M:${r.tipo}:${r.codigoFipe}:${r.codigoAno ?: r.anoModelo}"
    }
}

/** Regras puras da lista de salvos (testadas em unidade). */
object OrganizarSalvos {
    /** Coloca o item no topo, sem duplicar, e descarta recentes antigos acima do limite (favoritos ficam). */
    fun inserir(lista: List<ConsultaSalva>, item: ConsultaSalva, limite: Int): List<ConsultaSalva> {
        val sem = lista.filterNot { it.id == item.id }
        val nova = listOf(item) + sem
        var recentes = 0
        return nova.filter { c ->
            if (c.favorito) true else { recentes++; recentes <= limite }
        }
    }
}
