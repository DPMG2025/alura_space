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

    /**
     * Converte valores no formato brasileiro em numero: "R$ 45.320,00" e "R$ 45.320" viram 45320.0.
     * O ponto e sempre separador de milhar e a virgula, de centavos. Null quando nao houver numero.
     */
    fun paraDouble(texto: String?): Double? {
        if (texto.isNullOrBlank()) return null
        val limpo = texto.filter { it.isDigit() || it == ',' || it == '.' || it == '-' }
        if (limpo.none { it.isDigit() }) return null
        // Formato com ponto decimal (ex.: 45320.5 vindo de um provedor): sem virgula e 1 ou 2 digitos apos o ultimo ponto
        if (',' !in limpo && Regex("^-?\\d+\\.\\d{1,2}$").matches(limpo)) return limpo.toDoubleOrNull()
        val inteiro = limpo.substringBefore(',').replace(".", "")
        val centavos = limpo.substringAfter(',', "").filter { it.isDigit() }
        return (if (centavos.isEmpty()) inteiro else "$inteiro.$centavos").toDoubleOrNull()
    }

    /** "R$ 45.320,00", mantendo o espaco nao separavel entre R$ e o numero. */
    fun formatar(valor: Double): String = formato.format(valor)

    /** Valor aproximado para estimativas: arredonda para a centena ("≈ R$ 45.400"). */
    fun aproximado(valor: Double): String {
        val f = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).apply { maximumFractionDigits = 0 }
        return "≈ " + f.format(Math.round(valor / 100.0) * 100.0)
    }
}

object Texto {
    /** Minusculas e sem acentos, para filtros de busca. */
    private val ACENTOS = Regex("\\p{Mn}+")

    fun chave(texto: String): String =
        Normalizer.normalize(texto.lowercase(Locale.ROOT), Normalizer.Form.NFD).replace(ACENTOS, "")

    /**
     * "GOL 1.0L MC4" vira "Gol 1.0L MC4" e "MOBI DRIVE 1.0 Flex" vira "Mobi Drive 1.0 Flex": palavras so de
     * letras, com 3 ou mais letras e todas maiusculas, ganham so a inicial maiuscula (siglas conhecidas ficam).
     */
    fun modelo(texto: String): String {
        // Nome ja com minusculas (padrao FIPE): so palavras longas em caixa alta mudam; 3 letras costumam ser siglas (MSI, GTS)
        val minimo = if (texto.any { it.isLowerCase() }) 4 else 3
        return texto.split(' ').joinToString(" ") { p ->
            if (p.length >= minimo && p.all { it.isLetter() && it.isUpperCase() } && p !in SIGLAS)
                p.lowercase(Locale("pt", "BR")).replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
            else p
        }
    }

    /** Siglas de versao que ficam em maiusculas. */
    private val SIGLAS = setOf("TSI", "GTI", "GLS", "GLX", "LTZ", "CVT", "AMG", "TDI", "SUV", "DCT", "GSR", "ELX", "EXL",
        "XEI", "GLI", "XLS", "LXS", "SRV", "SRX", "HGT", "TFSI", "MPI", "FSI", "VTEC", "AWD", "ABS")

    /**
     * Nome de marca para exibicao: "VW - VolksWagen" vira "Volkswagen" e
     * "GM - Chevrolet" vira "Chevrolet". Siglas (BMW, BYD) ficam como estao.
     */
    fun marca(nome: String): String {
        val limpo = nome.trim().removePrefix("I/").substringBefore('/').trim()
        SIGLAS_MARCA[limpo.uppercase(Locale.ROOT)]?.let { return it }
        val base = (if (" - " in limpo) limpo.substringAfter(" - ") else limpo).trim()
        return base.split(' ').joinToString(" ") { p ->
            val misto = p.drop(1).any { it.isUpperCase() } && p.any { it.isLowerCase() }
            // "ARROW" vira "Arrow"; siglas curtas (BMW, BYD, GWM) ficam
            val caixaAlta = p.length >= 4 && p.all { it.isLetter() && it.isUpperCase() }
            if (misto || caixaAlta) p.lowercase(Locale("pt", "BR")).replaceFirstChar { it.titlecase(Locale("pt", "BR")) } else p
        }
    }

    /** Siglas usadas pelos provedores de placa e pela FIPE para algumas marcas. */
    private val SIGLAS_MARCA = mapOf(
        "VW" to "Volkswagen", "VOLKSWAGEN" to "Volkswagen", "GM" to "Chevrolet", "CHEVROLET" to "Chevrolet",
        "M.BENZ" to "Mercedes-Benz", "MBENZ" to "Mercedes-Benz", "MERCEDES" to "Mercedes-Benz", "MERCEDES-BENZ" to "Mercedes-Benz",
        "FIAT" to "Fiat", "FORD" to "Ford", "HONDA" to "Honda", "TOYOTA" to "Toyota", "HYUNDAI" to "Hyundai", "RENAULT" to "Renault",
        "NISSAN" to "Nissan", "JEEP" to "Jeep", "PEUGEOT" to "Peugeot", "CITROEN" to "Citroën", "MITSUBISHI" to "Mitsubishi",
        "YAMAHA" to "Yamaha", "SUZUKI" to "Suzuki", "KAWASAKI" to "Kawasaki", "VOLVO" to "Volvo", "SCANIA" to "Scania", "IVECO" to "Iveco",
        "KIA" to "Kia Motors", "CAOA CHERY" to "CAOA Chery", "CHERY" to "CAOA Chery",
    )

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
    fun filtrar(referencias: List<ReferenciaV2>, periodo: PeriodoHistorico, anoAtual: Int? = null): List<ReferenciaV2> {
        if (referencias.isEmpty()) return emptyList()
        val selecionadas = when (periodo) {
            PeriodoHistorico.MESES_12 -> referencias.take(12)
            PeriodoHistorico.MESES_24 -> referencias.take(24)
            PeriodoHistorico.ANO_ATUAL -> {
                // O ano vem do valor atual: a lista de meses pode estar um mes atrasada (virada de ano).
                val ano = anoAtual ?: ano(referencias.first().mes)
                referencias.filter { ano(it.mes) == ano }
            }
        }
        return selecionadas.reversed()
    }
}

object Datas {
    private val hora = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    private val data = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy")

    /** "Hoje, 14:05", "Ontem, 09:10" ou "28/10/26". */
    fun relativa(epochMs: Long, agoraMs: Long = System.currentTimeMillis(), zona: java.time.ZoneId = java.time.ZoneId.systemDefault()): String {
        val quando = java.time.Instant.ofEpochMilli(epochMs).atZone(zona)
        val hoje = java.time.Instant.ofEpochMilli(agoraMs).atZone(zona).toLocalDate()
        return when (quando.toLocalDate()) {
            hoje -> "Hoje, ${quando.format(hora)}"
            hoje.minusDays(1) -> "Ontem, ${quando.format(hora)}"
            else -> quando.format(data)
        }
    }
}
