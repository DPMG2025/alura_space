package br.com.casinhadosaber.ui.telas.atividades

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Como se escreve cada letra (letra de forma maiúscula, a "letra bastão" da escola): a lista de
 * traços na ordem certa, e cada traço é uma linha de pontos no sentido em que o lápis anda.
 *
 * As coordenadas vão de 0 a 1 nos dois eixos (x para a direita, y para baixo), dentro do quadro de
 * traçar; a letra ocupa mais ou menos de 0,15 a 0,85. Curvas viram linhas de muitos pontinhos.
 */
object TracosDasLetras {

    private const val TOPO = 0.15f
    private const val BASE = 0.85f
    private const val MEIO = 0.5f

    private fun p(x: Float, y: Float) = x to y

    /**
     * Arco de elipse com centro ([cx], [cy]) e raios [rx], [ry], de [de] até [ate] graus.
     * Ângulos com o y para baixo: 0 é a direita, 90 é embaixo, -90 é em cima. Ângulo crescendo =
     * sentido do relógio na tela.
     */
    private fun arco(cx: Float, cy: Float, rx: Float, ry: Float, de: Float, ate: Float): List<Offset> {
        val n = (kotlin.math.abs(ate - de) / 12f).toInt().coerceAtLeast(4)
        return (0..n).map { i ->
            val a = (de + (ate - de) * i / n) * PI.toFloat() / 180f
            Offset(cx + rx * cos(a), cy + ry * sin(a))
        }
    }

    /** Junta pedaços (retas e arcos) num traço só, sem repetir o ponto da emenda. */
    private fun traco(vararg partes: List<Offset>): List<Offset> {
        val r = mutableListOf<Offset>()
        for (parte in partes) for (q in parte) if (r.isEmpty() || (r.last() - q).getDistance() > 0.001f) r += q
        return r
    }

    /** Linha reta (ou quebrada) pelos pontos dados. Offset é classe de valor e não pode ser vararg. */
    private fun reta(vararg pontos: Pair<Float, Float>): List<Offset> = pontos.map { (x, y) -> Offset(x, y) }

    private val oval = arco(0.5f, MEIO, 0.32f, 0.35f, -90f, -450f)

    private val letras: Map<Char, List<List<Offset>>> = mapOf(
        'A' to listOf(
            reta(p(0.5f, TOPO), p(0.2f, BASE)),
            reta(p(0.5f, TOPO), p(0.8f, BASE)),
            reta(p(0.33f, 0.6f), p(0.67f, 0.6f)),
        ),
        'B' to listOf(
            reta(p(0.3f, TOPO), p(0.3f, BASE)),
            traco(reta(p(0.3f, TOPO), p(0.54f, TOPO)), arco(0.54f, 0.325f, 0.18f, 0.175f, -90f, 90f), reta(p(0.54f, MEIO), p(0.3f, MEIO))),
            traco(reta(p(0.3f, MEIO), p(0.56f, MEIO)), arco(0.56f, 0.675f, 0.2f, 0.175f, -90f, 90f), reta(p(0.56f, BASE), p(0.3f, BASE))),
        ),
        'C' to listOf(arco(0.53f, MEIO, 0.32f, 0.35f, -45f, -315f)),
        'D' to listOf(
            reta(p(0.28f, TOPO), p(0.28f, BASE)),
            traco(reta(p(0.28f, TOPO), p(0.44f, TOPO)), arco(0.44f, MEIO, 0.3f, 0.35f, -90f, 90f), reta(p(0.44f, BASE), p(0.28f, BASE))),
        ),
        'E' to listOf(
            reta(p(0.3f, TOPO), p(0.3f, BASE)),
            reta(p(0.3f, TOPO), p(0.72f, TOPO)),
            reta(p(0.3f, MEIO), p(0.64f, MEIO)),
            reta(p(0.3f, BASE), p(0.72f, BASE)),
        ),
        'F' to listOf(
            reta(p(0.32f, TOPO), p(0.32f, BASE)),
            reta(p(0.32f, TOPO), p(0.74f, TOPO)),
            reta(p(0.32f, MEIO), p(0.66f, MEIO)),
        ),
        'G' to listOf(
            arco(0.52f, MEIO, 0.32f, 0.35f, -45f, -360f),
            reta(p(0.58f, MEIO), p(0.84f, MEIO)),
        ),
        'H' to listOf(
            reta(p(0.27f, TOPO), p(0.27f, BASE)),
            reta(p(0.73f, TOPO), p(0.73f, BASE)),
            reta(p(0.27f, MEIO), p(0.73f, MEIO)),
        ),
        'I' to listOf(
            reta(p(0.5f, TOPO), p(0.5f, BASE)),
            reta(p(0.34f, TOPO), p(0.66f, TOPO)),
            reta(p(0.34f, BASE), p(0.66f, BASE)),
        ),
        'J' to listOf(traco(reta(p(0.64f, TOPO), p(0.64f, 0.64f)), arco(0.46f, 0.64f, 0.18f, 0.21f, 0f, 180f))),
        'K' to listOf(
            reta(p(0.3f, TOPO), p(0.3f, BASE)),
            reta(p(0.72f, TOPO), p(0.3f, 0.54f)),
            reta(p(0.44f, 0.43f), p(0.74f, BASE)),
        ),
        'L' to listOf(reta(p(0.32f, TOPO), p(0.32f, BASE), p(0.72f, BASE))),
        'M' to listOf(
            reta(p(0.2f, TOPO), p(0.2f, BASE)),
            reta(p(0.2f, TOPO), p(0.5f, 0.62f), p(0.8f, TOPO)),
            reta(p(0.8f, TOPO), p(0.8f, BASE)),
        ),
        'N' to listOf(
            reta(p(0.27f, TOPO), p(0.27f, BASE)),
            reta(p(0.27f, TOPO), p(0.73f, BASE)),
            reta(p(0.73f, BASE), p(0.73f, TOPO)),
        ),
        'O' to listOf(oval),
        'P' to listOf(
            reta(p(0.3f, TOPO), p(0.3f, BASE)),
            traco(reta(p(0.3f, TOPO), p(0.52f, TOPO)), arco(0.52f, 0.33f, 0.2f, 0.18f, -90f, 90f), reta(p(0.52f, 0.51f), p(0.3f, 0.51f))),
        ),
        'Q' to listOf(oval, reta(p(0.58f, 0.66f), p(0.84f, 0.9f))),
        'R' to listOf(
            reta(p(0.3f, TOPO), p(0.3f, BASE)),
            traco(reta(p(0.3f, TOPO), p(0.52f, TOPO)), arco(0.52f, 0.33f, 0.2f, 0.18f, -90f, 90f), reta(p(0.52f, 0.51f), p(0.3f, 0.51f))),
            reta(p(0.48f, 0.51f), p(0.74f, BASE)),
        ),
        'S' to listOf(traco(arco(0.5f, 0.325f, 0.22f, 0.175f, -20f, -270f), arco(0.5f, 0.675f, 0.22f, 0.175f, -90f, 160f))),
        'T' to listOf(
            reta(p(0.22f, TOPO), p(0.78f, TOPO)),
            reta(p(0.5f, TOPO), p(0.5f, BASE)),
        ),
        'U' to listOf(traco(reta(p(0.28f, TOPO), p(0.28f, 0.6f)), arco(0.5f, 0.6f, 0.22f, 0.25f, 180f, 0f), reta(p(0.72f, 0.6f), p(0.72f, TOPO)))),
        'V' to listOf(
            reta(p(0.22f, TOPO), p(0.5f, BASE)),
            reta(p(0.78f, TOPO), p(0.5f, BASE)),
        ),
        'W' to listOf(reta(p(0.12f, TOPO), p(0.3f, BASE), p(0.5f, 0.38f), p(0.7f, BASE), p(0.88f, TOPO))),
        'X' to listOf(
            reta(p(0.25f, TOPO), p(0.75f, BASE)),
            reta(p(0.75f, TOPO), p(0.25f, BASE)),
        ),
        'Y' to listOf(
            reta(p(0.25f, TOPO), p(0.5f, MEIO)),
            reta(p(0.75f, TOPO), p(0.5f, MEIO), p(0.5f, BASE)),
        ),
        'Z' to listOf(reta(p(0.25f, TOPO), p(0.75f, TOPO), p(0.25f, BASE), p(0.75f, BASE))),
    )

    /** Linha de cima das letras pequenas (a altura do "x") e ponta das que descem (g, j, p, q, y). */
    private const val XT = 0.45f
    private const val DESCE = 0.96f
    private const val BOJO = 0.625f

    /** Bolinha das letras pequenas (o "o" do a, b, d, g, p, q). */
    private fun bojo(cx: Float, de: Float, ate: Float) = arco(cx, BOJO, 0.2f, 0.175f, de, ate)

    /** Pinguinho do i e do j: um traço bem curtinho. */
    private fun pingo(x: Float) = reta(p(x, 0.27f), p(x, 0.32f))

    /** Letras pequenas (minúsculas de forma), na ordem e no sentido em que se escreve na escola. */
    private val minusculas: Map<Char, List<List<Offset>>> = mapOf(
        'a' to listOf(bojo(0.47f, -20f, -380f), reta(p(0.67f, XT), p(0.67f, BASE))),
        'b' to listOf(reta(p(0.3f, TOPO), p(0.3f, BASE)), bojo(0.5f, 180f, 540f)),
        'c' to listOf(bojo(0.52f, -40f, -320f)),
        'd' to listOf(bojo(0.47f, -20f, -380f), reta(p(0.67f, TOPO), p(0.67f, BASE))),
        'e' to listOf(traco(reta(p(0.28f, BOJO), p(0.68f, BOJO)), arco(0.48f, BOJO, 0.2f, 0.175f, 0f, -310f))),
        'f' to listOf(
            traco(arco(0.6f, 0.27f, 0.12f, 0.12f, -20f, -180f), reta(p(0.48f, 0.27f), p(0.48f, BASE))),
            reta(p(0.34f, XT), p(0.66f, XT)),
        ),
        'g' to listOf(bojo(0.47f, -20f, -380f), traco(reta(p(0.67f, XT), p(0.67f, 0.84f)), arco(0.47f, 0.84f, 0.2f, 0.12f, 0f, 150f))),
        'h' to listOf(reta(p(0.3f, TOPO), p(0.3f, BASE)), traco(arco(0.48f, 0.6f, 0.18f, 0.15f, 180f, 360f), reta(p(0.66f, 0.6f), p(0.66f, BASE)))),
        'i' to listOf(reta(p(0.5f, XT), p(0.5f, BASE)), pingo(0.5f)),
        'j' to listOf(traco(reta(p(0.56f, XT), p(0.56f, 0.84f)), arco(0.42f, 0.84f, 0.14f, 0.11f, 0f, 160f)), pingo(0.56f)),
        'k' to listOf(reta(p(0.32f, TOPO), p(0.32f, BASE)), reta(p(0.66f, XT), p(0.32f, 0.66f)), reta(p(0.44f, 0.6f), p(0.68f, BASE))),
        'l' to listOf(reta(p(0.5f, TOPO), p(0.5f, BASE))),
        'm' to listOf(
            reta(p(0.2f, XT), p(0.2f, BASE)),
            traco(arco(0.335f, 0.6f, 0.135f, 0.15f, 180f, 360f), reta(p(0.47f, 0.6f), p(0.47f, BASE))),
            traco(arco(0.605f, 0.6f, 0.135f, 0.15f, 180f, 360f), reta(p(0.74f, 0.6f), p(0.74f, BASE))),
        ),
        'n' to listOf(reta(p(0.3f, XT), p(0.3f, BASE)), traco(arco(0.48f, 0.6f, 0.18f, 0.15f, 180f, 360f), reta(p(0.66f, 0.6f), p(0.66f, BASE)))),
        'o' to listOf(bojo(0.5f, -90f, -450f)),
        'p' to listOf(reta(p(0.3f, XT), p(0.3f, DESCE)), bojo(0.5f, 180f, 540f)),
        'q' to listOf(bojo(0.47f, -20f, -380f), reta(p(0.67f, XT), p(0.67f, DESCE))),
        'r' to listOf(reta(p(0.36f, XT), p(0.36f, BASE)), arco(0.52f, 0.6f, 0.16f, 0.14f, 180f, 300f)),
        's' to listOf(traco(arco(0.5f, 0.5375f, 0.17f, 0.0875f, -20f, -270f), arco(0.5f, 0.7125f, 0.17f, 0.0875f, -90f, 160f))),
        't' to listOf(reta(p(0.48f, 0.25f), p(0.48f, BASE)), reta(p(0.32f, XT), p(0.66f, XT))),
        'u' to listOf(traco(reta(p(0.3f, XT), p(0.3f, 0.65f)), arco(0.48f, 0.65f, 0.18f, 0.15f, 180f, 0f)), reta(p(0.66f, XT), p(0.66f, BASE))),
        'v' to listOf(reta(p(0.28f, XT), p(0.5f, BASE), p(0.72f, XT))),
        'w' to listOf(reta(p(0.15f, XT), p(0.32f, BASE), p(0.5f, 0.55f), p(0.68f, BASE), p(0.85f, XT))),
        'x' to listOf(reta(p(0.3f, XT), p(0.7f, BASE)), reta(p(0.7f, XT), p(0.3f, BASE))),
        'y' to listOf(reta(p(0.3f, XT), p(0.5f, BASE)), reta(p(0.7f, XT), p(0.4f, DESCE))),
        'z' to listOf(reta(p(0.3f, XT), p(0.7f, XT), p(0.3f, BASE), p(0.7f, BASE))),
    )

    /** Algarismos de 0 a 9. */
    private val algarismos: Map<Char, List<List<Offset>>> = mapOf(
        '0' to listOf(arco(0.5f, MEIO, 0.25f, 0.35f, -90f, -450f)),
        '1' to listOf(reta(p(0.36f, 0.3f), p(0.55f, TOPO), p(0.55f, BASE))),
        '2' to listOf(traco(arco(0.5f, 0.34f, 0.22f, 0.19f, -160f, 20f), reta(p(0.71f, 0.405f), p(0.28f, BASE), p(0.74f, BASE)))),
        '3' to listOf(traco(arco(0.5f, 0.325f, 0.2f, 0.175f, -160f, 90f), arco(0.5f, 0.675f, 0.2f, 0.175f, -90f, 160f))),
        '4' to listOf(reta(p(0.62f, TOPO), p(0.25f, 0.62f), p(0.78f, 0.62f)), reta(p(0.62f, TOPO), p(0.62f, BASE))),
        '5' to listOf(
            traco(reta(p(0.33f, TOPO), p(0.31f, 0.47f)), arco(0.48f, 0.64f, 0.22f, 0.19f, -130f, 150f)),
            reta(p(0.33f, TOPO), p(0.72f, TOPO)),
        ),
        '6' to listOf(traco(arco(0.52f, MEIO, 0.24f, 0.35f, -70f, -180f), arco(0.5f, 0.66f, 0.22f, 0.19f, 180f, -180f))),
        '7' to listOf(reta(p(0.27f, TOPO), p(0.73f, TOPO), p(0.4f, BASE))),
        '8' to listOf(
            traco(
                arco(0.5f, 0.325f, 0.19f, 0.175f, -20f, -270f),
                arco(0.5f, 0.675f, 0.22f, 0.175f, -90f, 270f),
                arco(0.5f, 0.325f, 0.19f, 0.175f, 90f, -20f),
            ),
        ),
        '9' to listOf(arco(0.5f, 0.35f, 0.22f, 0.2f, -20f, -380f), reta(p(0.72f, 0.35f), p(0.68f, BASE))),
    )

    /** Letras que já têm os traços desenhados. */
    val conhecidas: Set<Char> get() = letras.keys

    /** Letras pequenas e algarismos que já têm os traços desenhados. */
    val minusculasConhecidas: Set<Char> get() = minusculas.keys
    val algarismosConhecidos: Set<Char> get() = algarismos.keys

    /**
     * Traços da [letra]: maiúscula, minúscula (letra pequena) ou algarismo, com ou sem acento comum.
     * Se não for conhecida, devolve um traço reto de cima para baixo: a criança ainda consegue terminar.
     */
    fun tracosDe(letra: String): List<List<Offset>> {
        val original = letra.trim().firstOrNull() ?: return listOf(reta(p(0.5f, TOPO), p(0.5f, BASE)))
        algarismos[original]?.let { return it }
        if (original.isLowerCase()) minusculas[semAcento(original.uppercaseChar()).lowercaseChar()]?.let { return it }
        return letras[semAcento(original.uppercaseChar())] ?: listOf(reta(p(0.5f, TOPO), p(0.5f, BASE)))
    }

    private fun semAcento(c: Char): Char =
        when (c) {
            'Á', 'À', 'Â', 'Ã' -> 'A'
            'É', 'Ê' -> 'E'
            'Í' -> 'I'
            'Ó', 'Ô', 'Õ' -> 'O'
            'Ú' -> 'U'
            'Ç' -> 'C'
            else -> c
        }

    /** Altura da letra dividida pela largura (a letra de forma é um pouco mais alta que larga). */
    const val PROPORCAO = 1.2f

    /**
     * Leva um ponto da letra (de 0 a 1) para a tela, numa área de [largura] por [altura] pixels:
     * a letra fica no maior retângulo de proporção [PROPORCAO] que cabe, centralizado.
     */
    fun paraTela(p: Offset, largura: Float, altura: Float): Offset {
        val quadroL = min(largura, altura / PROPORCAO)
        val quadroA = min(altura, largura * PROPORCAO)
        return Offset((largura - quadroL) / 2f + p.x * quadroL, (altura - quadroA) / 2f + p.y * quadroA)
    }

    /** Reparte o traço em pontos a cada [passo] (de 0 a 1), para acompanhar o dedo na ordem. */
    fun pontosDoTraco(traco: List<Offset>, passo: Float = 0.03f): List<Offset> {
        if (traco.size < 2) return traco
        val r = mutableListOf(traco.first())
        for ((a, b) in traco.zipWithNext()) {
            val d = (b - a).getDistance()
            val n = (d / passo).toInt().coerceAtLeast(1)
            for (i in 1..n) r += a + (b - a) * (i / n.toFloat())
        }
        return r
    }
}
