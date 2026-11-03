package br.com.casinhadosaber.ui.telas.atividades

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Estilo comum das telas de atividade (grupo B do plano gráfico): painel da cena com cantos redondos
 * e contorno, ladrilho branco com "lábio" colorido, festejo do acerto (pulinho que achata e estica e
 * estrelinhas) e o balanço de lado do erro.
 *
 * Valores locais deste pacote. Quando os tokens comuns (Raios, Contorno) existirem em ui/tema, basta
 * trocar as constantes abaixo por eles.
 */

/** Raio dos painéis das cenas (pescaria, caça, esconde, balões...). */
internal val RAIO_PAINEL = 24.dp

/** Raio dos ladrilhos (respostas, sílabas, palavras, peças). */
internal val RAIO_LADRILHO = 16.dp

/** Contorno suave (ameixa escura, 25%), o mesmo dos painéis e ladrilhos. */
internal val ContornoDoJogo = Color(0x403D2C5E)

/** Branco do brilho de cima. */
internal val BrilhoDoJogo = Color(0x59FFFFFF)

private val FormaDoPainel = RoundedCornerShape(RAIO_PAINEL)

/** Painel da cena: corta o desenho com cantos redondos e põe um contorno por cima. */
internal fun Modifier.painelDaCena(): Modifier =
    this
        .clip(FormaDoPainel)
        .border(3.dp, ContornoDoJogo, FormaDoPainel)

/**
 * Ladrilho do jogo: corpo [fundo] (branco), um lábio de [labio] embaixo na cor [cor] e um contorno fino.
 * O lábio fica dentro do tamanho do ladrilho (não muda o layout de quem usa).
 */
internal fun Modifier.ladrilho(
    cor: Color,
    raio: Dp = RAIO_LADRILHO,
    fundo: Color = Color.White,
    labio: Dp = 4.dp,
    contorno: Color = ContornoDoJogo,
    larguraContorno: Dp = 1.5.dp,
): Modifier = drawBehind {
    val r = CornerRadius(raio.toPx())
    val l = labio.toPx()
    drawRoundRect(cor, cornerRadius = r)
    drawRoundRect(fundo, size = Size(size.width, size.height - l), cornerRadius = r)
    // Brilho de cima: uma faixa clarinha no terço de cima.
    drawRoundRect(
        Color.White.copy(alpha = 0.6f),
        topLeft = Offset(size.width * 0.08f, size.height * 0.07f),
        size = Size(size.width * 0.84f, size.height * 0.16f),
        cornerRadius = CornerRadius(size.height * 0.08f),
    )
    val meia = larguraContorno.toPx() / 2f
    drawRoundRect(
        contorno,
        topLeft = Offset(meia, meia),
        size = Size(size.width - 2 * meia, size.height - 2 * meia),
        cornerRadius = CornerRadius((raio.toPx() - meia).coerceAtLeast(0f)),
        style = Stroke(larguraContorno.toPx()),
    )
}

/** Cor um pouco mais escura (para lábios e sombras). */
internal fun maisEscura(c: Color, f: Float = 0.78f) = Color(c.red * f, c.green * f, c.blue * f, c.alpha)

// ------------------------------------------------------------------------------------- festejo do acerto

/**
 * Festejo do acerto: quando [acertou] passa de falso para verdadeiro, a figura achata, estica e volta
 * com uma molinha, e solta estrelinhas em volta (com [aoAparecer], também quando já nasce acertada). Animações finitas (não seguram o emulador ocupado).
 */
@Composable
internal fun Modifier.festejo(
    acertou: Boolean,
    cor: Color = Color(0xFFFFC93C),
    /** Festeja também quando já nasce acertada (a peça que acabou de entrar na fileira). */
    aoAparecer: Boolean = false,
): Modifier {
    val sx = remember { Animatable(1f) }
    val sy = remember { Animatable(1f) }
    val estrelas = remember { Animatable(1f) }
    var antes by remember { mutableStateOf(acertou && !aoAparecer) }
    LaunchedEffect(acertou) {
        val agora = acertou && !antes
        antes = acertou
        if (agora) {
            launch {
                estrelas.snapTo(0f)
                estrelas.animateTo(1f, tween(700, easing = LinearOutSlowInEasing))
            }
            launch {
                sx.animateTo(1.18f, tween(90))
                sx.animateTo(0.93f, tween(120))
                sx.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
            }
            sy.animateTo(0.84f, tween(90))
            sy.animateTo(1.1f, tween(120))
            sy.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
        }
    }
    return this
        .graphicsLayer {
            scaleX = sx.value
            scaleY = sy.value
        }
        .drawWithContent {
            drawContent()
            val p = estrelas.value
            if (p < 1f) estrelinhasDoAcerto(center, size.minDimension * 0.5f, p, cor)
        }
}

/**
 * Estrelinhas do acerto: 8 estrelas saem do centro, giram, encolhem e somem. [p] vai de 0 a 1.
 */
internal fun DrawScope.estrelinhasDoAcerto(centro: Offset, raio: Float, p: Float, cor: Color) {
    if (p <= 0f || p >= 1f) return
    val alcance = raio * (0.7f + 0.9f * p)
    val alfa = (1f - p).coerceIn(0f, 1f)
    val tamanho = raio * 0.2f * (1f - 0.5f * p)
    val caminho = Path()
    for (k in 0 until 8) {
        val ang = (k * 45f + 20f * p) * PI.toFloat() / 180f
        val c = Offset(centro.x + cos(ang) * alcance, centro.y + sin(ang) * alcance)
        val r = if (k % 2 == 0) tamanho else tamanho * 0.7f
        caminho.reset()
        estrela(caminho, c, r, 300f * p + k * 20f)
        drawPath(caminho, (if (k % 2 == 0) cor else Color.White).copy(alpha = alfa))
        drawPath(caminho, maisEscura(cor, 0.85f).copy(alpha = alfa * 0.6f), style = Stroke(r * 0.18f))
    }
    // Um anel que abre e some.
    drawCircle(cor.copy(alpha = 0.45f * alfa), radius = raio * (0.6f + 0.7f * p), center = centro, style = Stroke(raio * 0.08f * (1f - p)))
}

/** Estrela de 5 pontas no [caminho] (sem fechar outros contornos). */
internal fun estrela(caminho: Path, c: Offset, r: Float, giroGraus: Float = 0f) {
    val base = (giroGraus - 90f) * PI.toFloat() / 180f
    for (i in 0 until 10) {
        val rr = if (i % 2 == 0) r else r * 0.45f
        val a = base + i * PI.toFloat() / 5f
        val x = c.x + cos(a) * rr
        val y = c.y + sin(a) * rr
        if (i == 0) caminho.moveTo(x, y) else caminho.lineTo(x, y)
    }
    caminho.close()
}
