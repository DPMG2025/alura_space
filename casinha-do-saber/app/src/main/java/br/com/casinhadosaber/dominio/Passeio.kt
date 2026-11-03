package br.com.casinhadosaber.dominio

/** O que o bichinho está fazendo no passeio. */
enum class Gesto { ANDANDO, PARADO, ABANANDO, SENTADO }

/**
 * Um instante do passeio: [deslocamento] vai de -1 (o ponto mais à esquerda do passeio) a 1 (o mais à
 * direita), [paraDireita] diz para onde ele olha e [passada] (0 a 1) é a fase das patinhas andando (e do rabo abanando).
 */
data class Passo(val deslocamento: Float, val paraDireita: Boolean, val gesto: Gesto, val passada: Float)

/**
 * O passeio dos bichinhos da casa (o cachorrinho, o gatinho, os visitantes e os bichos das áreas novas):
 * anda uns passinhos, para, abana o rabo, senta, vira e volta. É um roteiro fixo de [CICLO_MS] que se repete,
 * começando num ponto diferente para cada bicho ([semente]): nada é sorteado, e o bicho nunca sai do seu
 * pedaço (o deslocamento fica sempre entre -1 e 1; quem desenha multiplica pelo alcance).
 */
object Passeio {
    const val CICLO_MS = 16_000L
    private const val PASSADA_MS = 520L

    /** Trechos do roteiro: (fim em ms, gesto, deslocamento no começo, deslocamento no fim, olhando para a direita). */
    private data class Trecho(val fim: Long, val gesto: Gesto, val de: Float, val ate: Float, val direita: Boolean)

    private val roteiro = listOf(
        Trecho(3_000, Gesto.ANDANDO, -0.2f, 1f, true),
        Trecho(5_000, Gesto.ABANANDO, 1f, 1f, true),
        Trecho(8_000, Gesto.SENTADO, 1f, 1f, true),
        Trecho(8_600, Gesto.PARADO, 1f, 1f, false),
        Trecho(12_200, Gesto.ANDANDO, 1f, -1f, false),
        Trecho(14_000, Gesto.PARADO, -1f, -1f, false),
        Trecho(14_400, Gesto.PARADO, -1f, -1f, true),
        Trecho(CICLO_MS, Gesto.ANDANDO, -1f, -0.2f, true),
    )

    fun em(ms: Long, semente: Int = 0): Passo {
        val defasagem = (semente.toLong() and 0xFFFF) * 2_731L
        val t = Math.floorMod(ms + defasagem, CICLO_MS)
        var inicio = 0L
        for (tr in roteiro) {
            if (t < tr.fim) {
                val f = (t - inicio).toFloat() / (tr.fim - inicio)
                val d = tr.de + (tr.ate - tr.de) * f
                // A passada corre sempre: serve para as patinhas andando e para o rabo abanando.
                val passada = Math.floorMod(t, PASSADA_MS).toFloat() / PASSADA_MS
                return Passo(d, tr.direita, tr.gesto, passada)
            }
            inicio = tr.fim
        }
        return Passo(-0.2f, true, Gesto.ANDANDO, 0f)
    }

    /**
     * Alcance do passeio (fração da largura) para um bicho com o pé em [x], sem sair de [esq]..[dir]:
     * no máximo [maximo] para cada lado.
     */
    fun alcance(x: Float, maximo: Float, esq: Float = 0.05f, dir: Float = 0.95f): Float =
        minOf(maximo, (x - esq).coerceAtLeast(0f), (dir - x).coerceAtLeast(0f))

    /** Bichos de emoji que só mostram o rosto trocam pelo bicho de corpo inteiro, quando a fonte tem. */
    fun corpoInteiro(emoji: String): String = when (emoji) {
        "🐶" -> "🐕"
        "🐱" -> "🐈"
        "🐰" -> "🐇"
        "🐭" -> "🐁"
        "🐷" -> "🐖"
        "🐮" -> "🐄"
        "🐯" -> "🐅"
        "🐵" -> "🐒"
        "🐴" -> "🐎"
        else -> emoji
    }
}
