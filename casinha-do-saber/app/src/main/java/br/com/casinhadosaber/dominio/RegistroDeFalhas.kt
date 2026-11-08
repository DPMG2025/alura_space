package br.com.casinhadosaber.dominio

/**
 * Quando o app fecha sozinho (erro não tratado), guardamos no próprio aparelho o que aconteceu, para os pais
 * verem na área deles e mandarem o detalhe. Sem internet: nada sai do aparelho sem o adulto copiar.
 * Esta parte é pura (montar, guardar em texto e ler de volta), testada na JVM.
 */
data class Falha(
    /** Hora do fechamento (milissegundos desde 1970). */
    val quando: Long,
    /** Tela em que a criança estava (nome da tela, sem dados dela). */
    val tela: String,
    /** Tipo do erro (por exemplo, OutOfMemoryError). */
    val tipo: String,
    val mensagem: String,
    /** As primeiras linhas da pilha, já limpas. */
    val pilha: List<String>,
)

object RegistroDeFalhas {
    /** Quantos fechamentos ficam guardados (os mais novos). */
    const val MAXIMO = 5
    const val LINHAS_DA_PILHA = 25
    private const val TAMANHO_DA_MENSAGEM = 300
    private const val SEPARADOR_DE_FALHAS = "\u001E"
    private const val SEPARADOR_DE_CAMPOS = "\u001F"

    /** Monta a falha a partir do erro, com a causa mais funda junto (é ela que costuma dizer o motivo). */
    fun montar(quando: Long, tela: String, erro: Throwable): Falha {
        var raiz = erro
        val vistos = HashSet<Throwable>()
        while (raiz.cause != null && raiz.cause !== raiz && vistos.add(raiz)) raiz = raiz.cause!!
        // Metade das linhas para o erro e metade para a causa: as do topo de cada pilha são as que dizem onde foi.
        val linhas = ArrayList<String>()
        val metade = LINHAS_DA_PILHA / 2
        linhas += erro.stackTrace.take(if (raiz !== erro) metade else LINHAS_DA_PILHA).map { "  em $it" }
        if (raiz !== erro) {
            linhas += "Causa: ${raiz.javaClass.name}: ${raiz.message.orEmpty().take(TAMANHO_DA_MENSAGEM)}"
            linhas += raiz.stackTrace.take(LINHAS_DA_PILHA - metade - 1).map { "  em $it" }
        }
        return Falha(
            quando = quando,
            tela = limpar(tela).take(80),
            tipo = erro.javaClass.name,
            mensagem = limpar(erro.message.orEmpty()).take(TAMANHO_DA_MENSAGEM),
            pilha = linhas.map(::limpar).take(LINHAS_DA_PILHA),
        )
    }

    /** Põe a falha nova no começo e guarda só as [MAXIMO] mais novas. */
    fun acrescentar(antes: List<Falha>, nova: Falha): List<Falha> = (listOf(nova) + antes).take(MAXIMO)

    private fun limpar(s: String): String = s.replace(SEPARADOR_DE_FALHAS, " ").replace(SEPARADOR_DE_CAMPOS, " ").replace('\n', ' ').replace('\r', ' ')

    fun paraTexto(falhas: List<Falha>): String = falhas.joinToString(SEPARADOR_DE_FALHAS) { f ->
        (listOf(f.quando.toString(), f.tela, f.tipo, f.mensagem) + f.pilha).joinToString(SEPARADOR_DE_CAMPOS)
    }

    /** Lê de volta; o que estiver estragado é ignorado. */
    fun deTexto(s: String?): List<Falha> {
        if (s.isNullOrEmpty()) return emptyList()
        return s.split(SEPARADOR_DE_FALHAS).mapNotNull { item ->
            val c = item.split(SEPARADOR_DE_CAMPOS)
            if (c.size < 4) return@mapNotNull null
            val quando = c[0].toLongOrNull() ?: return@mapNotNull null
            Falha(quando, c[1], c[2], c[3], c.drop(4))
        }.take(MAXIMO)
    }

    /** O texto que o adulto copia e manda para quem cuida do app. */
    fun textoParaCopiar(f: Falha, versao: String, aparelho: String, dataHora: String): String = buildString {
        appendLine("Casinha do Saber $versao, $aparelho")
        appendLine("Fechou em $dataHora, na tela ${f.tela}")
        appendLine("${f.tipo}: ${f.mensagem}")
        f.pilha.forEach { appendLine(it) }
    }.trimEnd()

    /** Nome curto do erro para a tela dos pais (sem o pacote). */
    fun nomeCurto(f: Falha): String = f.tipo.substringAfterLast('.')
}
