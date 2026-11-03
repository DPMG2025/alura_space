package br.com.casinhadosaber.dominio

/*
 * Várias crianças no mesmo aparelho (irmãos, primos). Cada criança tem um arquivo de preferências só dela
 * (perfil, casa, estrelas, moedas, mascote, álbum, desenhos, registro das partidas); um arquivo pequeno do
 * aparelho guarda a lista de crianças, quem está jogando e os ajustes que valem para o aparelho todo.
 *
 * A criança 1 usa o mesmo arquivo de antes ("casinha_do_saber"): o progresso de quem já jogava vira a
 * criança 1 sem copiar nada (nenhum dado se perde). Os ajustes do aparelho saem desse arquivo uma vez só
 * ([Familia.migrar]).
 *
 * O limite de tempo é do aparelho, não de cada criança: a contagem de hoje soma o tempo de todas, e quando a
 * casinha dorme, dorme para todas. Assim trocar de criança não vira um jeito de ganhar mais tela.
 */

/** Uma criança da lista: o id do arquivo dela e o perfil (null enquanto ainda não foi criado). */
data class CriancaDaCasa(val id: String, val perfil: Perfil?) {
    /** Nome para mostrar: o que o adulto escreveu, ou "Criança 2" quando ficou em branco. */
    fun nomeParaMostrar(): String = perfil?.nome?.takeIf { it.isNotBlank() } ?: "Criança $id"
}

object Familia {
    /** Mais que isso não cabe bem na tela de escolher (e não é o caso de uma família). */
    const val MAXIMO = 8

    const val ARQUIVO_DO_APARELHO = "casinha_aparelho"
    const val ARQUIVO_LEGADO = "casinha_do_saber"
    const val PRIMEIRA = "1"

    // Chaves do arquivo do aparelho.
    const val CHAVE_CRIANCAS = "criancas"
    const val CHAVE_ATIVA = "ativa"
    const val CHAVE_MIGRADO = "versao_familia"

    // Ajustes do aparelho (antes ficavam no arquivo único).
    const val SOM = "som"
    const val MUSICA = "musica"
    const val TIMER = "timer"
    const val DORMINDO = "dormindo"
    const val DIA = "dia"
    const val SEGUNDOS_HOJE = "segundos_hoje"

    val AJUSTES_DO_APARELHO = setOf(SOM, MUSICA, TIMER, DORMINDO, DIA, SEGUNDOS_HOJE)

    /** Ajustes que vão na cópia: som, música e limite. A contagem de hoje e o "dormindo" ficam no aparelho. */
    val AJUSTES_QUE_VIAJAM = setOf(SOM, MUSICA, TIMER)

    /** A criança 1 continua no arquivo antigo; as outras ganham um arquivo próprio. */
    fun arquivo(id: String): String = if (id == PRIMEIRA) ARQUIVO_LEGADO else "${ARQUIVO_LEGADO}_crianca_$id"

    /** Menor número livre (a primeira criança de um app novo é sempre a 1, no arquivo de sempre). */
    fun proximoId(ids: List<String>): String =
        generateSequence(1) { it + 1 }.first { it.toString() !in ids }.toString()

    /** Tamanho máximo do nome da criança, em caracteres que a pessoa vê (um emoji conta como um). */
    const val TAMANHO_DO_NOME = 16

    /**
     * Corta [nome] em [maximo] pontos de código, sem partir um emoji ao meio (um par substituto cortado
     * viraria um caractere estragado). Emojis compostos (família, bandeira) podem perder um pedaço, mas
     * nunca deixam um caractere inválido.
     */
    fun cortarNome(nome: String, maximo: Int = TAMANHO_DO_NOME): String {
        val pontos = nome.codePointCount(0, nome.length)
        if (pontos <= maximo) return nome
        return nome.substring(0, nome.offsetByCodePoints(0, maximo))
    }

    fun idsDeTexto(texto: String?): List<String> =
        texto.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() && it.all(Char::isDigit) }.distinct()

    fun idsParaTexto(ids: List<String>): String = ids.joinToString(",")

    /** Resultado da migração do save de antes (uma criança só) para a família. */
    data class Migracao(
        /** O que vai para o arquivo do aparelho: a lista com a criança 1, ela ativa e os ajustes de antes. */
        val aparelho: Map<String, ValorGuardado>,
        /** O que fica no arquivo da criança 1 (o mesmo arquivo de antes, sem os ajustes do aparelho). */
        val crianca1: Map<String, ValorGuardado>,
    )

    /**
     * Separa o arquivo único de antes: tudo que é do jogo fica na criança 1 (exatamente como estava) e os
     * ajustes do aparelho (som, música, limite e a contagem de hoje) passam para o arquivo do aparelho.
     */
    fun migrar(legado: Map<String, ValorGuardado>): Migracao = Migracao(
        aparelho = legado.filterKeys { it in AJUSTES_DO_APARELHO } + mapOf(
            CHAVE_CRIANCAS to ValorGuardado.Texto(PRIMEIRA),
            CHAVE_ATIVA to ValorGuardado.Texto(PRIMEIRA),
            CHAVE_MIGRADO to ValorGuardado.Inteiro(1),
        ),
        crianca1 = legado.filterKeys { it !in AJUSTES_DO_APARELHO },
    )
}
