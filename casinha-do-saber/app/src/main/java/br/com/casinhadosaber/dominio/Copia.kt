package br.com.casinhadosaber.dominio

/*
 * Cópia do progresso sem internet: o estado inteiro do app (todas as crianças) vira um arquivo JSON
 * que a família guarda onde quiser (Drive, WhatsApp, Downloads). O app nunca envia nada sozinho.
 *
 * Kotlin puro (sem org.json), para testar na JVM: o Repositorio lê as preferências como um mapa de chave e
 * valor tipado ([ValorGuardado]); [Copia.paraTexto] transforma em texto e [Copia.deTexto] valida tudo antes
 * de devolver a cópia (nada é aplicado pela metade: ou a cópia inteira é válida, ou vem um [ResultadoDaCopia.Invalida]).
 */

/** Uma linha do resumo da cópia, para o adulto conferir antes de recuperar. */
data class ResumoDaCrianca(val nome: String, val fasesVencidas: Int)

/** Quem está na cópia e quantas fases cada criança venceu (nome em branco vira "Criança 2"). */
fun CopiaDoProgresso.resumo(): List<ResumoDaCrianca> = criancas.map { c ->
    val nome = (c.dados["nome"] as? ValorGuardado.Texto)?.valor?.takeIf { it.isNotBlank() } ?: "Criança ${c.id}"
    val estrelas = (c.dados["estrelas"] as? ValorGuardado.Texto)?.valor
    ResumoDaCrianca(nome, Serializacao.textoParaEstrelas(estrelas).size)
}

/** Um valor das preferências, com o tipo de origem (para voltar exatamente igual). */
sealed interface ValorGuardado {
    data class Texto(val valor: String) : ValorGuardado
    data class Inteiro(val valor: Int) : ValorGuardado
    data class Longo(val valor: Long) : ValorGuardado
    data class Decimal(val valor: Float) : ValorGuardado
    data class Logico(val valor: Boolean) : ValorGuardado
    /** Conjunto de textos (`putStringSet`): o app de hoje não grava, mas a cópia não pode perder um. */
    data class Conjunto(val valor: Set<String>) : ValorGuardado
}

/** Os dados de uma criança: o id do perfil e as chaves do arquivo dela. */
data class CriancaGuardada(val id: String, val dados: Map<String, ValorGuardado>)

/**
 * O estado inteiro do app: as crianças, a que estava jogando e os ajustes do aparelho que valem a pena levar
 * (som, música e limite de tempo; a contagem de hoje fica no aparelho).
 */
data class CopiaDoProgresso(
    val criancas: List<CriancaGuardada>,
    val ativa: String,
    val aparelho: Map<String, ValorGuardado> = emptyMap(),
    /** Dia em que a cópia foi feita (só para mostrar ao adulto). */
    val feitaEm: Data? = null,
)

sealed interface ResultadoDaCopia {
    data class Valida(val copia: CopiaDoProgresso) : ResultadoDaCopia
    /** [motivo] já vem no jeito de mostrar ao adulto. */
    data class Invalida(val motivo: String) : ResultadoDaCopia
}

object Copia {
    /** Versão do formato. Uma cópia de versão maior (app mais novo) é recusada com uma mensagem amigável. */
    const val VERSAO = 1
    const val APP = "casinha-do-saber"
    const val TIPO_DO_ARQUIVO = "application/json"

    /** Um arquivo maior que isso não é uma cópia da casinha (o progresso cheio tem bem menos). */
    const val TAMANHO_MAXIMO = 8 * 1024 * 1024

    const val NAO_E_COPIA = "Esse arquivo não é uma cópia da Casinha do Saber. Escolha o arquivo que começa com \"casinha-do-saber-copia\"."
    const val VERSAO_NOVA = "Essa cópia foi feita por uma versão mais nova do app. Atualize a Casinha do Saber e tente de novo."
    const val ESTRAGADA = "Essa cópia parece incompleta ou estragada. Nada foi mudado. Tente outra cópia."

    fun nomeDoArquivo(dia: Data): String =
        "casinha-do-saber-copia-%04d-%02d-%02d.json".format(java.util.Locale.ROOT, dia.ano, dia.mes, dia.dia)

    private val chaveValida = Regex("[A-Za-z0-9_.]{1,64}")
    private val idValido = Regex("[0-9]{1,6}")

    /**
     * Tipo de cada chave que o app lê (o mesmo do Repositorio). Uma cópia com o tipo trocado é recusada: ler
     * um texto como número derrubaria o app. Chaves desconhecidas passam (podem ser de uma versão mais nova).
     */
    val TIPOS_CONHECIDOS: Map<String, Char> = buildMap {
        listOf(
            "nome", "genero", "chapeu", "traje", "acessorios", "aniversario", "temas_favoritos", "irmao", "estrelas",
            "colocados", "cores", "respondidas", "acertos", "tentativas", "comprados", "historico", "visitantes",
            "moveis_dentro", "mascote", "presentes_especiais", "eventos_recebidos", "desenhos", "quadros_parede",
            "desafios_pais", "sessoes",
            // Relembrar, Descobrir o nível e Livrinhos (por criança, como o resto do progresso).
            "revisoes", "liberada_ate", "nivel_feito", "livrinhos_lidos",
        ).forEach { put(it, 's') }
        listOf("pele", "cabelo", "roupa", "estilo_cabelo", "moedas", "moedas_hoje", "cor_telhado", "cor_porta", "papel", "clima", "timer")
            .forEach { put(it, 'i') }
        listOf("segundos", "dia_moedas", "dia_missao", "dia", "segundos_hoje").forEach { put(it, 'l') }
        listOf("crianca_x", "crianca_y").forEach { put(it, 'f') }
        listOf("viu_dica_arrastar", "som", "musica", "dormindo").forEach { put(it, 'b') }
    }

    private fun ValorGuardado.tipo(): Char = when (this) {
        is ValorGuardado.Texto -> 's'
        is ValorGuardado.Inteiro -> 'i'
        is ValorGuardado.Longo -> 'l'
        is ValorGuardado.Decimal -> 'f'
        is ValorGuardado.Logico -> 'b'
        is ValorGuardado.Conjunto -> 'c'
    }

    /** Lança exceção se alguma chave conhecida vier com o tipo errado. */
    private fun conferirTipos(mapa: Map<String, ValorGuardado>) {
        for ((k, v) in mapa) TIPOS_CONHECIDOS[k]?.let { require(it == v.tipo()) { "tipo de $k" } }
    }

    fun paraTexto(copia: CopiaDoProgresso): String = buildString {
        append("{\n")
        append("  \"app\": ").append(Json.texto(APP)).append(",\n")
        append("  \"versao\": ").append(VERSAO).append(",\n")
        copia.feitaEm?.let { append("  \"feitaEm\": ").append(Json.texto("%04d-%02d-%02d".format(java.util.Locale.ROOT, it.ano, it.mes, it.dia))).append(",\n") }
        append("  \"ativa\": ").append(Json.texto(copia.ativa)).append(",\n")
        append("  \"aparelho\": ").append(mapaParaJson(copia.aparelho)).append(",\n")
        append("  \"criancas\": [")
        copia.criancas.forEachIndexed { i, c ->
            if (i > 0) append(",")
            append("\n    {\"id\": ").append(Json.texto(c.id)).append(", \"dados\": ").append(mapaParaJson(c.dados)).append("}")
        }
        append("\n  ]\n}\n")
    }

    private fun mapaParaJson(mapa: Map<String, ValorGuardado>): String =
        mapa.entries.sortedBy { it.key }.joinToString(", ", "{", "}") { (k, v) ->
            val (tipo, valor) = when (v) {
                is ValorGuardado.Texto -> "s" to Json.texto(v.valor)
                is ValorGuardado.Inteiro -> "i" to Json.texto(v.valor.toString())
                is ValorGuardado.Longo -> "l" to Json.texto(v.valor.toString())
                is ValorGuardado.Decimal -> "f" to Json.texto(v.valor.toString())
                is ValorGuardado.Logico -> "b" to Json.texto(v.valor.toString())
                // Conjunto: uma lista de textos, em ordem (a mesma cópia sai sempre igual).
                is ValorGuardado.Conjunto -> "c" to v.valor.sorted().joinToString(", ", "[", "]") { Json.texto(it) }
            }
            "${Json.texto(k)}: {\"t\": \"$tipo\", \"v\": $valor}"
        }

    /** Lê e valida a cópia inteira. Nunca lança exceção. */
    fun deTexto(textoLido: String?): ResultadoDaCopia {
        // Alguns editores e apps de nuvem gravam o arquivo com BOM (U+FEFF) no começo.
        val texto = textoLido?.removePrefix("\uFEFF")
        if (texto.isNullOrBlank() || texto.length > TAMANHO_MAXIMO) return ResultadoDaCopia.Invalida(NAO_E_COPIA)
        val raiz = runCatching { Json.ler(texto) }.getOrNull() as? Map<*, *>
            ?: return ResultadoDaCopia.Invalida(NAO_E_COPIA)
        if (raiz["app"] != APP) return ResultadoDaCopia.Invalida(NAO_E_COPIA)
        val versao = (raiz["versao"] as? Double)?.takeIf { it == Math.floor(it) }?.toInt()
            ?: return ResultadoDaCopia.Invalida(ESTRAGADA)
        if (versao > VERSAO) return ResultadoDaCopia.Invalida(VERSAO_NOVA)
        if (versao < 1) return ResultadoDaCopia.Invalida(ESTRAGADA)
        return runCatching { montar(raiz) }.getOrNull()?.let { ResultadoDaCopia.Valida(it) }
            ?: ResultadoDaCopia.Invalida(ESTRAGADA)
    }

    /** Lança exceção em qualquer coisa fora do formato (vira [ESTRAGADA] em [deTexto]). */
    private fun montar(raiz: Map<*, *>): CopiaDoProgresso {
        val lista = raiz["criancas"] as List<*>
        require(lista.isNotEmpty() && lista.size <= Familia.MAXIMO)
        val criancas = lista.map { item ->
            val obj = item as Map<*, *>
            val id = obj["id"] as String
            require(idValido.matches(id))
            CriancaGuardada(id, mapaDeJson(obj["dados"] as Map<*, *>).also(::conferirTipos))
        }
        require(criancas.map { it.id }.toSet().size == criancas.size)
        val ativa = (raiz["ativa"] as? String)?.takeIf { a -> criancas.any { it.id == a } } ?: criancas.first().id
        val aparelho = (raiz["aparelho"] as? Map<*, *>)?.let(::mapaDeJson).orEmpty()
            .filterKeys { it in Familia.AJUSTES_QUE_VIAJAM }.also(::conferirTipos)
        val feitaEm = (raiz["feitaEm"] as? String)?.let { t ->
            runCatching { t.split("-").map { it.toInt() }.let { (a, m, d) -> Data(a, m, d) } }.getOrNull()
        }
        return CopiaDoProgresso(criancas, ativa, aparelho, feitaEm)
    }

    private fun mapaDeJson(obj: Map<*, *>): Map<String, ValorGuardado> = obj.entries.associate { (k, v) ->
        val chave = k as String
        require(chaveValida.matches(chave))
        val campo = v as Map<*, *>
        if (campo["t"] == "c") {
            val lista = campo["v"] as List<*>
            return@associate chave to ValorGuardado.Conjunto(lista.map { it as String }.toSet())
        }
        val valor = campo["v"] as String
        chave to when (campo["t"]) {
            "s" -> ValorGuardado.Texto(valor)
            "i" -> ValorGuardado.Inteiro(valor.toInt())
            "l" -> ValorGuardado.Longo(valor.toLong())
            "f" -> ValorGuardado.Decimal(valor.toFloat().also { require(!it.isNaN()) })
            "b" -> ValorGuardado.Logico(valor.toBooleanStrict())
            else -> throw IllegalArgumentException("tipo")
        }
    }
}

/**
 * Um leitor e escritor de JSON pequeno, só com o que a cópia usa: objetos, listas, textos, números,
 * true, false e null. Números viram Double; objetos viram LinkedHashMap.
 */
internal object Json {
    fun texto(s: String): String = buildString {
        append('"')
        for (c in s) {
            when {
                c == '"' -> append("\\\"")
                c == '\\' -> append("\\\\")
                c == '\n' -> append("\\n")
                c == '\r' -> append("\\r")
                c == '\t' -> append("\\t")
                c < ' ' -> append("\\u%04x".format(java.util.Locale.ROOT, c.code))
                else -> append(c)
            }
        }
        append('"')
    }

    fun ler(texto: String): Any? {
        val l = Leitor(texto)
        val v = l.valor(0)
        l.espacos()
        require(l.i == texto.length) { "sobrou texto" }
        return v
    }

    private class Leitor(val s: String) {
        var i = 0

        fun espacos() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        fun valor(profundidade: Int): Any? {
            require(profundidade < 32)
            espacos()
            require(i < s.length)
            return when (s[i]) {
                '{' -> objeto(profundidade)
                '[' -> lista(profundidade)
                '"' -> texto()
                't' -> palavra("true", true)
                'f' -> palavra("false", false)
                'n' -> palavra("null", null)
                else -> numero()
            }
        }

        fun palavra(p: String, v: Any?): Any? {
            require(s.startsWith(p, i))
            i += p.length
            return v
        }

        fun objeto(prof: Int): Map<String, Any?> {
            val mapa = LinkedHashMap<String, Any?>()
            i++
            espacos()
            if (s.getOrNull(i) == '}') { i++; return mapa }
            while (true) {
                espacos()
                require(s.getOrNull(i) == '"')
                val k = texto()
                espacos()
                require(s.getOrNull(i) == ':'); i++
                mapa[k] = valor(prof + 1)
                espacos()
                when (s.getOrNull(i)) {
                    ',' -> i++
                    '}' -> { i++; return mapa }
                    else -> throw IllegalArgumentException("objeto")
                }
            }
        }

        fun lista(prof: Int): List<Any?> {
            val itens = ArrayList<Any?>()
            i++
            espacos()
            if (s.getOrNull(i) == ']') { i++; return itens }
            while (true) {
                itens += valor(prof + 1)
                espacos()
                when (s.getOrNull(i)) {
                    ',' -> i++
                    ']' -> { i++; return itens }
                    else -> throw IllegalArgumentException("lista")
                }
            }
        }

        fun texto(): String {
            i++
            val sb = StringBuilder()
            while (true) {
                require(i < s.length)
                val c = s[i++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        require(i < s.length)
                        when (val e = s[i++]) {
                            '"' -> sb.append('"'); '\\' -> sb.append('\\'); '/' -> sb.append('/')
                            'b' -> sb.append('\b'); 'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n'); 'r' -> sb.append('\r'); 't' -> sb.append('\t')
                            'u' -> {
                                require(i + 4 <= s.length)
                                sb.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4
                            }
                            else -> throw IllegalArgumentException("escape $e")
                        }
                    }
                    else -> sb.append(c)
                }
            }
        }

        fun numero(): Double {
            val inicio = i
            while (i < s.length && (s[i].isDigit() || s[i] in "+-.eE")) i++
            require(i > inicio)
            return s.substring(inicio, i).toDouble()
        }
    }
}
