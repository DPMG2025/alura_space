package br.com.fipeplaca.data

import java.text.NumberFormat
import java.text.Normalizer
import java.util.Locale

object Placa {
    private val REGEX = Regex("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$")

    /** Remove tudo que nao for letra ou numero e coloca em maiusculas. */
    fun normalizar(texto: String): String =
        texto.uppercase(Locale.ROOT).filter { it in 'A'..'Z' || it in '0'..'9' }.take(7)

    /** Aceita placa antiga (ABC1234) e Mercosul (ABC1D23). */
    fun valida(placa: String): Boolean = REGEX.matches(normalizar(placa))

    /** Exibe a placa antiga com hifen e a Mercosul sem separador, como na placa fisica. */
    fun formatar(placa: String): String {
        val p = normalizar(placa)
        return if (p.length == 7 && p[4].isDigit()) "${p.take(3)}-${p.drop(3)}" else p
    }
}

object Dinheiro {
    private val formato: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    /** Converte "R$ 45.320,00" em 45320.0. Retorna null quando nao houver numero. */
    fun paraDouble(texto: String?): Double? {
        if (texto.isNullOrBlank()) return null
        val limpo = texto.filter { it.isDigit() || it == ',' || it == '.' }
        if (limpo.none { it.isDigit() }) return null
        val normal = if (limpo.contains(',')) limpo.replace(".", "").replace(',', '.') else limpo
        return normal.toDoubleOrNull()
    }

    fun formatar(valor: Double): String = formato.format(valor).replace(' ', ' ')
}

object Texto {
    /** Minusculas e sem acentos, para filtros de busca. */
    fun chave(texto: String): String =
        Normalizer.normalize(texto.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    /** A FIPE usa 32000 para veiculos zero km. */
    fun nomeAno(nome: String): String = nome.replace("32000", "Zero KM")
}

object Referencias {
    private val ANO = Regex("(\\d{4})")

    /** Extrai o ano de um mes de referencia como "novembro de 2026" ou "novembro/2026". */
    fun ano(mes: String): Int? = ANO.find(mes)?.value?.toIntOrNull()

    /**
     * Recebe as referencias em ordem da mais recente para a mais antiga e devolve
     * as que entram no periodo pedido, em ordem cronologica.
     */
    fun filtrar(referencias: List<ReferenciaV2>, periodo: PeriodoHistorico): List<ReferenciaV2> {
        if (referencias.isEmpty()) return emptyList()
        val selecionadas = when (periodo) {
            PeriodoHistorico.MESES_12 -> referencias.take(12)
            PeriodoHistorico.MESES_24 -> referencias.take(24)
            PeriodoHistorico.ANO_ATUAL -> {
                val anoAtual = ano(referencias.first().mes)
                referencias.takeWhile { ano(it.mes) == anoAtual }
            }
        }
        return selecionadas.reversed()
    }
}
