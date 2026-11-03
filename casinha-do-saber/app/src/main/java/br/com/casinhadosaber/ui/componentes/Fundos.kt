package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.ui.tema.Degrades
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** O que flutua no [FundoDecorado]. */
enum class EnfeiteDoFundo {
    /** Bolhas grandes e bem suaves (bokeh). */
    Bolhas,
    /** Estrelinhas de quatro pontas que piscam devagar. */
    Estrelas,
    /** Só o degradê. */
    Nenhum,
}

/**
 * Fundo das telas: degradê de duas cores a partir de [cor] (viva em cima, sem cinza; clara em baixo),
 * um brilho largo no alto e [enfeite] bem suaves (bolhas a 7 a 12%, estrelinhas a 10 a 18%), que derivam devagar.
 *
 * Barato: uma única animação infinita (`rememberInfiniteTransition`, que deixa a tela ficar ociosa
 * nos testes) lida só na hora de desenhar, então nada é recomposto a cada quadro.
 */
@Composable
fun FundoDecorado(
    cor: Color,
    modifier: Modifier = Modifier,
    enfeite: EnfeiteDoFundo = EnfeiteDoFundo.Bolhas,
    animado: Boolean = true,
    conteudo: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier.fundoDecorado(cor, enfeite, animado), content = conteudo)
}

/** O mesmo fundo do [FundoDecorado] como modificador (para telas que já têm o seu `Box`). */
@Composable
fun Modifier.fundoDecorado(
    cor: Color,
    enfeite: EnfeiteDoFundo = EnfeiteDoFundo.Bolhas,
    animado: Boolean = true,
): Modifier {
    val fase: State<Float> = if (animado) {
        rememberInfiniteTransition(label = "fundo")
            .animateFloat(0f, 1f, infiniteRepeatable(tween(16000, easing = LinearEasing)), label = "deriva")
    } else {
        remember { mutableFloatStateOf(0.15f) }
    }
    return drawWithCache {
        val degrade = Degrades.fundo(cor)
        val luz = Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(size.width * 0.5f, 0f),
            radius = maxOf(size.width, 1f) * 0.9f,
        )
        val estrela = Path()
        val bolhasProntas = if (enfeite == EnfeiteDoFundo.Bolhas) montarBolhas(size.width, size.height) else null
        onDrawBehind {
            drawRect(degrade)
            drawRect(luz)
            when (enfeite) {
                EnfeiteDoFundo.Bolhas -> if (bolhasProntas != null) bolhas(bolhasProntas, fase.value)
                EnfeiteDoFundo.Estrelas -> estrelinhas(fase.value, estrela)
                EnfeiteDoFundo.Nenhum -> Unit
            }
        }
    }
}

/** x, y (fração da tela), raio (fração da largura), opacidade. */
private val BOLHAS = floatArrayOf(
    0.12f, 0.10f, 0.22f, 0.10f,
    0.86f, 0.18f, 0.30f, 0.08f,
    0.55f, 0.42f, 0.16f, 0.07f,
    0.08f, 0.62f, 0.26f, 0.08f,
    0.92f, 0.70f, 0.20f, 0.10f,
    0.40f, 0.88f, 0.34f, 0.07f,
    0.70f, 0.95f, 0.16f, 0.09f,
)

/** Pincéis das bolhas, montados uma vez por tamanho de tela (a deriva só translada). */
private class Bolhas(val pinceis: List<Brush>, val centros: List<Offset>, val raios: FloatArray)

private fun montarBolhas(w: Float, h: Float): Bolhas {
    val pinceis = ArrayList<Brush>()
    val centros = ArrayList<Offset>()
    val raios = FloatArray(BOLHAS.size / 4)
    var i = 0
    while (i < BOLHAS.size) {
        val c = Offset(BOLHAS[i] * w, BOLHAS[i + 1] * h)
        val r = maxOf(BOLHAS[i + 2] * w, 1f)
        val alfa = BOLHAS[i + 3]
        // Disco com a borda um pouco mais forte que some de repente (luz fora de foco).
        pinceis += Brush.radialGradient(
            0f to Color.White.copy(alpha = alfa * 0.7f),
            0.8f to Color.White.copy(alpha = alfa),
            0.92f to Color.White.copy(alpha = alfa * 1.2f),
            1f to Color.Transparent,
            center = c,
            radius = r,
        )
        centros += c
        raios[i / 4] = r
        i += 4
    }
    return Bolhas(pinceis, centros, raios)
}

private fun DrawScope.bolhas(b: Bolhas, fase: Float) {
    val deriva = 14.dp.toPx()
    for (k in b.pinceis.indices) {
        val a = 2f * PI.toFloat() * (fase + k * 0.17f)
        translate(sin(a) * deriva, cos(a) * deriva * 1.4f) {
            drawCircle(b.pinceis[k], b.raios[k], b.centros[k])
        }
    }
}

/** x, y (fração), tamanho (dp). */
private val ESTRELINHAS = floatArrayOf(
    0.10f, 0.08f, 9f, 0.32f, 0.15f, 6f, 0.78f, 0.06f, 10f, 0.92f, 0.28f, 7f,
    0.62f, 0.22f, 6f, 0.18f, 0.38f, 8f, 0.84f, 0.48f, 9f, 0.45f, 0.55f, 6f,
    0.06f, 0.72f, 10f, 0.70f, 0.68f, 7f, 0.28f, 0.84f, 9f, 0.90f, 0.88f, 8f,
    0.52f, 0.96f, 6f, 0.38f, 0.04f, 7f,
)

private fun DrawScope.estrelinhas(fase: Float, p: Path) {
    val w = size.width
    val h = size.height
    var i = 0
    var k = 0
    while (i < ESTRELINHAS.size) {
        val a = 2f * PI.toFloat() * (fase * 2f + k * 0.29f)
        val alfa = 0.10f + 0.08f * (0.5f + 0.5f * sin(a))
        val cx = ESTRELINHAS[i] * w + sin(a * 0.5f) * 6.dp.toPx()
        val cy = ESTRELINHAS[i + 1] * h
        val r = ESTRELINHAS[i + 2].dp.toPx() * 1.3f
        p.reset()
        p.moveTo(cx, cy - r)
        p.cubicTo(cx, cy, cx, cy, cx + r, cy)
        p.cubicTo(cx, cy, cx, cy, cx, cy + r)
        p.cubicTo(cx, cy, cx, cy, cx - r, cy)
        p.cubicTo(cx, cy, cx, cy, cx, cy - r)
        p.close()
        drawPath(p, Color.White.copy(alpha = alfa))
        i += 3
        k++
    }
}
