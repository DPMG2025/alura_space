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
            // Meses reais entre o primeiro e o ultimo ponto (pode haver meses faltando no meio)
            val meses = (Meses.distancia(pontos.first().mesReferencia, pontos.last().mesReferencia) ?: (pontos.size - 1)).coerceAtLeast(1)
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
            val zero = s.none { it in '1'..'9' }
            return when {
                zero -> s.removePrefix("-") + "%" // sem "-0,0%"
                v > 0 -> "+$s%"
                else -> "$s%"
            }
        }
    }
}

/**
 * Projecao simples pela tendencia (regressao linear dos ultimos meses).
 * E apenas uma estimativa: a FIPE pode mudar de rumo a qualquer mes.
 */
object Projecao {
    /** Valor estimado daqui a [meses] meses; null com menos de 6 pontos. */
    fun estimar(pontos: List<PontoHistorico>, meses: Int = 12): Double? {
        if (pontos.size < 6) return null
        // Eixo X em meses reais (indice absoluto); sem data reconhecivel, usa a posicao.
        val xs = pontos.mapIndexed { i, p -> (Meses.indice(p.mesReferencia) ?: i).toDouble() }
        val n = pontos.size
        val xm = xs.sum() / n
        val ym = pontos.sumOf { it.valor } / n
        var num = 0.0
        var den = 0.0
        pontos.forEachIndexed { i, p -> num += (xs[i] - xm) * (p.valor - ym); den += (xs[i] - xm) * (xs[i] - xm) }
        val inclinacao = if (den == 0.0) 0.0 else num / den
        val valor = ym + inclinacao * ((xs.last() + meses) - xm)
        return valor.coerceAtLeast(0.0)
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

    private val COMPLETOS = listOf("janeiro", "fevereiro", "marco", "abril", "maio", "junho", "julho", "agosto",
        "setembro", "outubro", "novembro", "dezembro")

    /** Indice absoluto do mes (ano * 12 + mes), para ordenar e comparar; null se nao reconhecer. */
    fun indice(mes: String): Int? {
        val chave = Texto.chave(mes.trim())
        val m = COMPLETOS.indexOfFirst { chave.startsWith(it) }.takeIf { it >= 0 }
            ?: NOMES.indexOfFirst { chave.startsWith(it) }.takeIf { it >= 0 }
            ?: chave.substringBefore('/').toIntOrNull()?.minus(1)?.takeIf { it in 0..11 }
            ?: return null
        val ano = Referencias.ano(mes) ?: return null
        return ano * 12 + m
    }

    /** Meses entre duas referencias ("janeiro de 2026" a "novembro de 2026" = 10). */
    fun distancia(de: String, ate: String): Int? {
        val a = indice(de) ?: return null
        val b = indice(ate) ?: return null
        return b - a
    }

    fun capitalizar(mes: String): String = mes.trim().replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
}

/** Texto para compartilhar um resultado (WhatsApp, e-mail etc.). */
object Compartilhar {
    fun texto(r: FipeResultado, veiculo: VeiculoPlaca? = null, historico: List<PontoHistorico> = emptyList()): String =
        buildString {
            appendLine("Consulta Tabela FIPE")
            veiculo?.let { appendLine("Placa: ${Placa.formatar(it.placa)}") }
            appendLine("${Texto.marca(r.marca)} ${Texto.modelo(r.modelo)}".trim())
            if (r.anoModelo.isNotBlank()) appendLine("Ano modelo: ${r.anoModelo}${if (r.combustivel.isNotBlank()) " (${r.combustivel})" else ""}")
            appendLine("Valor FIPE: ${r.valor ?: "indisponível"}")
            if (r.mesReferencia.isNotBlank()) appendLine("Referência: ${r.mesReferencia}")
            if (r.codigoFipe.isNotBlank()) appendLine("Código FIPE: ${r.codigoFipe}")
            Estatisticas.de(historico)?.takeIf { historico.size > 1 }?.let { e ->
                appendLine(
                    "Variação de ${Meses.curto(historico.first().mesReferencia)} a ${Meses.curto(historico.last().mesReferencia)}: " +
                        Estatisticas.percentual(e.variacaoPercentual)
                )
            }
            append("Gerado pelo app FIPE pela Placa")
        }
}

/** Junta o historico da API com os valores guardados no aparelho (regras testadas em unidade). */
object MesclarHistorico {
    /**
     * Une as duas listas sem repetir mes (a da API prevalece), ordena do mais antigo ao
     * mais recente e recorta para o periodo, tomando o mes mais recente como referencia.
     */
    fun mesclar(api: List<PontoHistorico>, locais: List<PontoHistorico>, periodo: PeriodoHistorico): List<PontoHistorico> {
        val porMes = LinkedHashMap<Int, PontoHistorico>()
        (locais + api).forEach { p -> Meses.indice(p.mesReferencia)?.let { porMes[it] = p } }
        if (porMes.isEmpty()) return api
        val ultimo = porMes.keys.max()
        val inicio = when (periodo) {
            PeriodoHistorico.MESES_12 -> ultimo - 11
            PeriodoHistorico.MESES_24 -> ultimo - 23
            PeriodoHistorico.ANO_ATUAL -> (ultimo / 12) * 12
        }
        return porMes.filterKeys { it >= inicio }.toSortedMap().values.toList()
    }

    /** Une pontos guardados com novos, sem repetir mes, mantendo os ultimos [limite] meses. */
    fun acumular(atuais: List<PontoHistorico>, novos: List<PontoHistorico>, limite: Int = 36): List<PontoHistorico> {
        val porMes = LinkedHashMap<Int, PontoHistorico>()
        (atuais + novos).forEach { p -> Meses.indice(p.mesReferencia)?.let { porMes[it] = p } }
        return porMes.toSortedMap().values.toList().takeLast(limite)
    }
}
