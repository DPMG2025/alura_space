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

    private fun p(x: Float, y: Float) = Offset(x, y)

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

    private fun reta(vararg pontos: Offset) = pontos.toList()

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

    /** Letras que já têm os traços desenhados. */
    val conhecidas: Set<Char> get() = letras.keys

    /**
     * Traços da [letra] (maiúscula ou minúscula, com ou sem acento comum). Se a letra não for
     * conhecida, devolve um traço reto de cima para baixo: a criança ainda consegue terminar.
     */
    fun tracosDe(letra: String): List<List<Offset>> {
        val c = letra.trim().firstOrNull()?.uppercaseChar() ?: return listOf(reta(p(0.5f, TOPO), p(0.5f, BASE)))
        val semAcento = when (c) {
            'Á', 'À', 'Â', 'Ã' -> 'A'
            'É', 'Ê' -> 'E'
            'Í' -> 'I'
            'Ó', 'Ô', 'Õ' -> 'O'
            'Ú' -> 'U'
            'Ç' -> 'C'
            else -> c
        }
        return letras[semAcento] ?: listOf(reta(p(0.5f, TOPO), p(0.5f, BASE)))
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
