package br.com.fipeplaca.data

import java.util.Locale

/** Estatisticas do historico de precos. */
data class Estatisticas(
    val maximo: PontoHistorico,
    val minimo: PontoHistorico,
    val media: Double,
    val variacaoPercentual: Double,
    val diferenca: Double,
    val variacaoMensalMedia: Double,
) {
    companion object {
        fun de(pontos: List<PontoHistorico>): Estatisticas? {
            if (pontos.isEmpty()) return null
            val primeiro = pontos.first().valor
            val ultimo = pontos.last().valor
            val variacao = if (primeiro > 0) (ultimo - primeiro) / primeiro * 100 else 0.0
            val meses = (pontos.size - 1).coerceAtLeast(1)
            return Estatisticas(
                maximo = pontos.maxBy { it.valor },
                minimo = pontos.minBy { it.valor },
                media = pontos.sumOf { it.valor } / pontos.size,
                variacaoPercentual = variacao,
                diferenca = ultimo - primeiro,
                variacaoMensalMedia = if (pontos.size < 2) 0.0 else variacao / meses,
            )
        }

        fun percentual(v: Double, casas: Int = 1): String {
            val s = "%.${casas}f".format(Locale("pt", "BR"), v)
            return if (v > 0 && s.any { it in '1'..'9' }) "+$s%" else "$s%"
        }
    }
}

object Meses {
    private val NOMES = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

    /** "novembro de 2026" ou "novembro/2026" vira "nov/26". */
    fun curto(mes: String): String {
        val chave = Texto.chave(mes.trim())
        val nome = NOMES.firstOrNull { chave.startsWith(it) } ?: return mes.trim()
        val ano = Referencias.ano(mes)?.rem(100)?.let { "%02d".format(it) } ?: return nome
        return "$nome/$ano"
    }

    fun capitalizar(mes: String): String = mes.trim().replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
}

/** Texto para compartilhar um resultado (WhatsApp, e-mail etc.). */
object Compartilhar {
    fun texto(r: FipeResultado, veiculo: VeiculoPlaca? = null, historico: List<PontoHistorico> = emptyList()): String =
        buildString {
            appendLine("Consulta Tabela FIPE")
            veiculo?.let { appendLine("Placa: ${Placa.formatar(it.placa)}") }
            appendLine("${r.marca} ${r.modelo}".trim())
            if (r.anoModelo.isNotBlank()) appendLine("Ano modelo: ${r.anoModelo}${if (r.combustivel.isNotBlank()) " (${r.combustivel})" else ""}")
            appendLine("Valor FIPE: ${r.valor ?: "indisponível"}")
            if (r.mesReferencia.isNotBlank()) appendLine("Referência: ${r.mesReferencia}")
            if (r.codigoFipe.isNotBlank()) appendLine("Código FIPE: ${r.codigoFipe}")
            Estatisticas.de(historico)?.takeIf { historico.size > 1 }?.let { e ->
                appendLine(
                    "Variação em ${historico.size} meses: ${Estatisticas.percentual(e.variacaoPercentual)} " +
                        "(${Meses.curto(historico.first().mesReferencia)} a ${Meses.curto(historico.last().mesReferencia)})"
                )
            }
            append("Gerado pelo app FIPE pela Placa")
        }
}
