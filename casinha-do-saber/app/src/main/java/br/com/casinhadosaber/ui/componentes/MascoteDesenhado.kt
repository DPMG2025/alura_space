package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.casinhadosaber.dominio.AcessorioMascote
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.EstagioMascote

// Mascote da casa desenhado em Canvas (cachorrinho, gatinho e dragãozinho), no mesmo traço da criança:
// formas cheias, contorno suave, olhos grandes com brilho e bochechas rosadas. Sempre alegre.

/** Cores de cada espécie: corpo, contorno, barriga, orelha (ou chifre) e detalhe. */
private class Pelagem(val corpo: Color, val contorno: Color, val barriga: Color, val orelha: Color, val detalhe: Color)

private fun pelagem(e: EspecieMascote) = when (e) {
    EspecieMascote.CACHORRINHO -> Pelagem(Color(0xFFE8B07A), Color(0xFFB07040), Color(0xFFFFF1DC), Color(0xFF9A5B30), Color(0xFF5A3E36))
    EspecieMascote.GATINHO -> Pelagem(Color(0xFFFFB85C), Color(0xFFD9822B), Color(0xFFFFF4E0), Color(0xFFFF9FB8), Color(0xFFE58A2E))
    EspecieMascote.DRAGAOZINHO -> Pelagem(Color(0xFF7ED67A), Color(0xFF3E9A4C), Color(0xFFFFEBA6), Color(0xFFFFF4D6), Color(0xFF4FB2A0))
}

/**
 * O mascote animado (respira, pisca e dá pulinhos de alegria). O tamanho do desenho cresce com o
 * [estagio] dentro do mesmo espaço, então o bebê parece pequeno e o campeão ocupa tudo.
 */
@Composable
fun MascoteAnimado(
    especie: EspecieMascote,
    estagio: EstagioMascote,
    modifier: Modifier = Modifier,
    acessorio: AcessorioMascote? = null,
    nome: String = especie.nome,
    /** Dá pulinhos (na festa de crescimento e na vitória). */
    pulando: Boolean = false,
) {
    val transicao = rememberInfiniteTransition(label = "mascote")
    val respiro by transicao.animateFloat(1f, 1.03f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "respiro")
    val piscar by transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "piscar")
    val pulo by transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "pulo")
    Canvas(
        modifier
            .aspectRatio(1f)
            .semantics { contentDescription = nome }
            .graphicsLayer {
                scaleY = respiro
                transformOrigin = TransformOrigin(0.5f, 1f)
                if (pulando) translationY = -size.height * 0.08f * pulo
            },
    ) {
        desenharMascote(especie, estagio, acessorio, olhosFechados = piscar > 0.96f)
    }
}

/** Desenho parado (para as capturas e para miniaturas). */
@Composable
fun MascoteParado(especie: EspecieMascote, estagio: EstagioMascote, modifier: Modifier = Modifier, acessorio: AcessorioMascote? = null) {
    Canvas(modifier.aspectRatio(1f)) { desenharMascote(especie, estagio, acessorio) }
}

fun DrawScope.desenharMascote(
    especie: EspecieMascote,
    estagio: EstagioMascote,
    acessorio: AcessorioMascote? = null,
    olhosFechados: Boolean = false,
) {
    val s = size.minDimension
    val ox = (size.width - s) / 2f
    val oy = size.height - s
    // 0 no bebê, 1 no campeão: o corpo fica mais comprido e a cabeça proporcionalmente menor.
    val t = estagio.ordinal / (EstagioMascote.entries.size - 1f)
    val c = pelagem(especie)
    fun p(u: Float, v: Float) = Offset(ox + u * s, oy + v * s)
    fun oval(cx: Float, cy: Float, rx: Float, ry: Float) = Rect(ox + (cx - rx) * s, oy + (cy - ry) * s, ox + (cx + rx) * s, oy + (cy + ry) * s)
    val traco = s * 0.018f

    withTransform({ scale(estagio.escala, estagio.escala, pivot = Offset(size.width / 2f, size.height)) }) {
        val cabecaR = 0.25f - 0.03f * t
        val cabecaY = 0.40f + 0.02f * (1 - t)
        val corpoRy = 0.16f + 0.05f * t
        val corpoRx = 0.18f + 0.03f * t
        val corpoY = 0.95f - corpoRy - 0.03f

        // Brilho dourado em volta do campeão (e do grande, mais fraquinho).
        if (estagio.ordinal >= EstagioMascote.GRANDE.ordinal) {
            val forte = estagio == EstagioMascote.CAMPEAO
            drawCircle(Color(0xFFFFE27A).copy(alpha = if (forte) 0.45f else 0.25f), s * 0.47f, p(0.5f, 0.55f))
            for (k in 0 until if (forte) 6 else 3) {
                val ang = k * 1.047f + 0.4f
                val centro = p(0.5f + 0.44f * kotlin.math.cos(ang), 0.52f + 0.4f * kotlin.math.sin(ang))
                drawPath(caminhoEstrela(centro, s * (if (forte) 0.045f else 0.035f)), Color(0xFFFFC93C))
            }
        }

        // Sombra no chão.
        drawOval(Color(0x26000000), topLeft = p(0.24f, 0.94f), size = Size(s * 0.52f, s * 0.05f))

        // Capa de estrelas (atrás de tudo).
        if (acessorio == AcessorioMascote.CAPA) {
            val capa = Path().apply {
                moveTo(p(0.36f, corpoY - corpoRy * 0.7f).x, p(0.36f, corpoY - corpoRy * 0.7f).y)
                lineTo(p(0.64f, corpoY - corpoRy * 0.7f).x, p(0.64f, corpoY - corpoRy * 0.7f).y)
                lineTo(p(0.8f, 0.93f).x, p(0.8f, 0.93f).y)
                lineTo(p(0.2f, 0.93f).x, p(0.2f, 0.93f).y)
                close()
            }
            drawPath(capa, Color(0xFF7B4DFF))
            drawPath(capa, Color(0xFF5631C9), style = Stroke(traco))
            for ((u, v) in listOf(0.27f to 0.86f, 0.73f to 0.84f, 0.3f to 0.74f, 0.7f to 0.72f)) {
                drawPath(caminhoEstrela(p(u, v), s * 0.025f), Color(0xFFFFE27A))
            }
        }

        // Rabo e asinhas (atrás do corpo).
        when (especie) {
            EspecieMascote.CACHORRINHO -> {
                val rabo = Path().apply {
                    val a = p(0.62f, corpoY + 0.02f); val b = p(0.86f, corpoY - 0.02f); val d = p(0.82f, corpoY - 0.16f - 0.03f * t)
                    moveTo(a.x, a.y); cubicTo(b.x, b.y, b.x, b.y, d.x, d.y)
                }
                drawPath(rabo, c.contorno, style = Stroke(s * 0.075f, cap = StrokeCap.Round))
                drawPath(rabo, c.corpo, style = Stroke(s * 0.045f, cap = StrokeCap.Round))
            }
            EspecieMascote.GATINHO -> {
                val rabo = Path().apply {
                    val a = p(0.62f, corpoY + 0.05f); val b = p(0.95f, corpoY + 0.02f); val d = p(0.86f, corpoY - 0.24f - 0.04f * t)
                    moveTo(a.x, a.y); cubicTo(b.x, b.y, p(0.8f, corpoY - 0.05f).x, p(0.8f, corpoY - 0.05f).y, d.x, d.y)
                }
                drawPath(rabo, c.contorno, style = Stroke(s * 0.07f, cap = StrokeCap.Round))
                drawPath(rabo, c.corpo, style = Stroke(s * 0.042f, cap = StrokeCap.Round))
                drawCircle(c.barriga, s * 0.026f, p(0.86f, corpoY - 0.24f - 0.04f * t))
            }
            EspecieMascote.DRAGAOZINHO -> {
                // Asinhas: crescem um pouco com o estágio.
                val asa = 0.12f + 0.06f * t
                for (lado in listOf(-1f, 1f)) {
                    val base = p(0.5f + lado * 0.1f, corpoY - corpoRy * 0.5f)
                    val ponta = p(0.5f + lado * (0.14f + asa * 1.6f), corpoY - corpoRy * 0.5f - asa * 1.3f)
                    val baixo = p(0.5f + lado * (0.12f + asa * 1.4f), corpoY + 0.02f)
                    val w = Path().apply {
                        moveTo(base.x, base.y)
                        lineTo(ponta.x, ponta.y)
                        quadraticTo(p(0.5f + lado * (0.1f + asa), corpoY - 0.02f).x, p(0.5f + lado * (0.1f + asa), corpoY - 0.02f).y, baixo.x, baixo.y)
                        close()
                    }
                    drawPath(w, c.detalhe)
                    drawPath(w, Color(0xFF2E7F70), style = Stroke(traco))
                }
                val rabo = Path().apply {
                    moveTo(p(0.6f, corpoY - 0.02f).x, p(0.6f, corpoY - 0.02f).y)
                    quadraticTo(p(0.9f, corpoY + 0.05f).x, p(0.9f, corpoY + 0.05f).y, p(0.95f, corpoY - 0.1f).x, p(0.95f, corpoY - 0.1f).y)
                    quadraticTo(p(0.84f, corpoY + 0.14f).x, p(0.84f, corpoY + 0.14f).y, p(0.58f, corpoY + 0.1f).x, p(0.58f, corpoY + 0.1f).y)
                    close()
                }
                drawPath(rabo, c.corpo)
                drawPath(rabo, c.contorno, style = Stroke(traco))
                // Pontinha do rabo em forma de coração.
                val ponta = p(0.95f, corpoY - 0.1f)
                drawCircle(Color(0xFFFF8FB8), s * 0.03f, Offset(ponta.x - s * 0.015f, ponta.y))
                drawCircle(Color(0xFFFF8FB8), s * 0.03f, Offset(ponta.x + s * 0.015f, ponta.y - s * 0.02f))
            }
        }

        // Mochila (atrás do corpo, aparece dos lados).
        if (acessorio == AcessorioMascote.MOCHILA) {
            drawRoundRect(Color(0xFFFF7A3D), oval(0.5f, corpoY - 0.01f, corpoRx + 0.05f, corpoRy * 0.75f).topLeft, oval(0.5f, corpoY - 0.01f, corpoRx + 0.05f, corpoRy * 0.75f).size, CornerRadius(s * 0.05f))
        }

        // Pés.
        for (u in listOf(0.4f, 0.6f)) {
            drawOval(c.contorno, oval(u, 0.925f, 0.07f, 0.04f).topLeft, oval(u, 0.925f, 0.07f, 0.04f).size)
            drawOval(c.corpo, oval(u, 0.92f, 0.06f, 0.032f).topLeft, oval(u, 0.92f, 0.06f, 0.032f).size)
        }
        // Corpo e barriga.
        val corpo = oval(0.5f, corpoY, corpoRx, corpoRy)
        drawOval(c.corpo, corpo.topLeft, corpo.size)
        drawOval(c.contorno, corpo.topLeft, corpo.size, style = Stroke(traco))
        val barriga = oval(0.5f, corpoY + corpoRy * 0.15f, corpoRx * 0.62f, corpoRy * 0.7f)
        drawOval(c.barriga, barriga.topLeft, barriga.size)
        if (especie == EspecieMascote.DRAGAOZINHO) {
            // Listrinhas da barriga do dragão.
            for (k in 1..3) {
                val y = barriga.top + barriga.height * k / 4f
                drawLine(Color(0xFFE5C76A), Offset(barriga.left + barriga.width * 0.22f, y), Offset(barriga.right - barriga.width * 0.22f, y), traco * 0.8f, StrokeCap.Round)
            }
        }
        if (especie == EspecieMascote.GATINHO) {
            // Listras do gatinho nos lados do corpo.
            for (lado in listOf(-1f, 1f)) for (k in 0..1) {
                val y = corpoY - corpoRy * 0.3f + k * corpoRy * 0.45f
                drawLine(c.detalhe, p(0.5f + lado * (corpoRx - 0.02f), y), p(0.5f + lado * (corpoRx - 0.07f), y + 0.01f), traco * 1.4f, StrokeCap.Round)
            }
        }
        // Estrela dourada no peito do campeão.
        if (estagio == EstagioMascote.CAMPEAO && acessorio != AcessorioMascote.MEDALHA) {
            drawPath(caminhoEstrela(p(0.5f, corpoY + 0.01f), s * 0.05f), Color(0xFFFFC93C))
        }
        // Bracinhos (um acenando).
        drawOval(c.corpo, oval(0.5f - corpoRx + 0.01f, corpoY - 0.01f, 0.045f, 0.06f).topLeft, oval(0.5f - corpoRx + 0.01f, corpoY - 0.01f, 0.045f, 0.06f).size)
        drawOval(c.contorno, oval(0.5f - corpoRx + 0.01f, corpoY - 0.01f, 0.045f, 0.06f).topLeft, oval(0.5f - corpoRx + 0.01f, corpoY - 0.01f, 0.045f, 0.06f).size, style = Stroke(traco))
        val braco = oval(0.5f + corpoRx + 0.01f, corpoY - corpoRy * 0.75f, 0.045f, 0.06f)
        drawOval(c.corpo, braco.topLeft, braco.size)
        drawOval(c.contorno, braco.topLeft, braco.size, style = Stroke(traco))

        // Mochila: alças na frente.
        if (acessorio == AcessorioMascote.MOCHILA) {
            for (lado in listOf(-1f, 1f)) {
                drawLine(Color(0xFFD95A22), p(0.5f + lado * corpoRx * 0.55f, corpoY - corpoRy * 0.8f), p(0.5f + lado * corpoRx * 0.6f, corpoY + corpoRy * 0.3f), traco * 2f, StrokeCap.Round)
            }
        }
        // Coleira com plaquinha (a partir do pequeno), menos no dragão.
        val pescocoY = cabecaY + cabecaR * 0.92f
        if (estagio.ordinal >= EstagioMascote.PEQUENO.ordinal && especie != EspecieMascote.DRAGAOZINHO && acessorio != AcessorioMascote.CACHECOL) {
            drawLine(Color(0xFFE63946), p(0.5f - 0.11f, pescocoY), p(0.5f + 0.11f, pescocoY), traco * 2.6f, StrokeCap.Round)
            drawCircle(Color(0xFFFFC93C), s * 0.024f, p(0.5f, pescocoY + 0.025f))
        }

        // Orelhas e chifres atrás da cabeça.
        when (especie) {
            EspecieMascote.GATINHO -> for (lado in listOf(-1f, 1f)) {
                val ore = Path().apply {
                    moveTo(p(0.5f + lado * cabecaR * 0.9f, cabecaY - cabecaR * 0.25f).x, p(0.5f + lado * cabecaR * 0.9f, cabecaY - cabecaR * 0.25f).y)
                    lineTo(p(0.5f + lado * cabecaR * 0.85f, cabecaY - cabecaR * 1.35f).x, p(0.5f + lado * cabecaR * 0.85f, cabecaY - cabecaR * 1.35f).y)
                    lineTo(p(0.5f + lado * cabecaR * 0.2f, cabecaY - cabecaR * 0.85f).x, p(0.5f + lado * cabecaR * 0.2f, cabecaY - cabecaR * 0.85f).y)
                    close()
                }
                drawPath(ore, c.corpo)
                drawPath(ore, c.contorno, style = Stroke(traco))
                val dentro = Path().apply {
                    moveTo(p(0.5f + lado * cabecaR * 0.75f, cabecaY - cabecaR * 0.5f).x, p(0.5f + lado * cabecaR * 0.75f, cabecaY - cabecaR * 0.5f).y)
                    lineTo(p(0.5f + lado * cabecaR * 0.78f, cabecaY - cabecaR * 1.1f).x, p(0.5f + lado * cabecaR * 0.78f, cabecaY - cabecaR * 1.1f).y)
                    lineTo(p(0.5f + lado * cabecaR * 0.38f, cabecaY - cabecaR * 0.82f).x, p(0.5f + lado * cabecaR * 0.38f, cabecaY - cabecaR * 0.82f).y)
                    close()
                }
                drawPath(dentro, c.orelha)
            }
            EspecieMascote.DRAGAOZINHO -> for (lado in listOf(-1f, 1f)) {
                // Chifres grossinhos e curvos para fora, com o mesmo contorno do corpo e dois anéis.
                val baseFora = p(0.5f + lado * cabecaR * 0.66f, cabecaY - cabecaR * 0.72f)
                val baseDentro = p(0.5f + lado * cabecaR * 0.28f, cabecaY - cabecaR * 0.9f)
                val ponta = p(0.5f + lado * cabecaR * (0.86f + 0.15f * t), cabecaY - cabecaR * (1.5f + 0.25f * t))
                val curvaFora = p(0.5f + lado * cabecaR * 0.84f, cabecaY - cabecaR * 1.02f)
                val curvaDentro = p(0.5f + lado * cabecaR * 0.48f, cabecaY - cabecaR * 1.25f)
                val chifre = Path().apply {
                    moveTo(baseFora.x, baseFora.y)
                    quadraticTo(curvaFora.x, curvaFora.y, ponta.x, ponta.y)
                    quadraticTo(curvaDentro.x, curvaDentro.y, baseDentro.x, baseDentro.y)
                    close()
                }
                drawPath(chifre, c.orelha)
                // Anéis do chifre e um brilho do lado de dentro.
                for (f in listOf(0.3f, 0.55f)) {
                    val a = Offset(baseFora.x + (ponta.x - baseFora.x) * f, baseFora.y + (ponta.y - baseFora.y) * f)
                    val b = Offset(baseDentro.x + (ponta.x - baseDentro.x) * f, baseDentro.y + (ponta.y - baseDentro.y) * f)
                    drawLine(Color(0xFFE2C682), a, b, traco * 0.9f, StrokeCap.Round)
                }
                drawLine(Color(0xB3FFFFFF), Offset(baseDentro.x + (ponta.x - baseDentro.x) * 0.15f + lado * s * 0.01f, baseDentro.y + (ponta.y - baseDentro.y) * 0.15f), Offset(baseDentro.x + (ponta.x - baseDentro.x) * 0.45f + lado * s * 0.01f, baseDentro.y + (ponta.y - baseDentro.y) * 0.45f), traco * 0.8f, StrokeCap.Round)
                drawPath(chifre, c.contorno, style = Stroke(traco, join = StrokeJoin.Round))
            }
            else -> Unit
        }

        // Cabeça.
        val cabeca = p(0.5f, cabecaY)
        drawCircle(c.corpo, s * cabecaR, cabeca)
        drawCircle(c.contorno, s * cabecaR, cabeca, style = Stroke(traco))

        // Detalhes do rosto de cada espécie.
        when (especie) {
            EspecieMascote.CACHORRINHO -> {
                // Orelhas caídas, na frente da cabeça.
                for (lado in listOf(-1f, 1f)) {
                    val o = oval(0.5f + lado * cabecaR * 0.95f, cabecaY + cabecaR * 0.05f, cabecaR * 0.32f, cabecaR * 0.62f)
                    withTransform({ rotate(lado * 18f, o.center) }) {
                        drawOval(c.orelha, o.topLeft, o.size)
                    }
                }
                // Manchinha no olho e focinho clarinho.
                drawOval(Color(0xFFD4925A), oval(0.5f + cabecaR * 0.35f, cabecaY - cabecaR * 0.12f, cabecaR * 0.3f, cabecaR * 0.28f).topLeft, oval(0.5f + cabecaR * 0.35f, cabecaY - cabecaR * 0.12f, cabecaR * 0.3f, cabecaR * 0.28f).size)
                val focinho = oval(0.5f, cabecaY + cabecaR * 0.38f, cabecaR * 0.42f, cabecaR * 0.3f)
                drawOval(c.barriga, focinho.topLeft, focinho.size)
            }
            EspecieMascote.GATINHO -> {
                // Listrinhas na testa.
                for (k in -1..1) {
                    drawLine(c.detalhe, p(0.5f + k * cabecaR * 0.22f, cabecaY - cabecaR * 0.95f), p(0.5f + k * cabecaR * 0.18f, cabecaY - cabecaR * 0.65f), traco * 1.4f, StrokeCap.Round)
                }
                val focinho = oval(0.5f, cabecaY + cabecaR * 0.38f, cabecaR * 0.38f, cabecaR * 0.26f)
                drawOval(c.barriga, focinho.topLeft, focinho.size)
            }
            EspecieMascote.DRAGAOZINHO -> {
                // Cristinha na cabeça (mais pontas quanto maior).
                val pontas = 2 + estagio.ordinal / 2
                for (k in 0 until pontas) {
                    val u = 0.5f + (k - (pontas - 1) / 2f) * 0.05f
                    drawPath(caminhoEstrela(p(u, cabecaY - cabecaR * 1.02f), s * 0.022f, pontas = 3), c.detalhe)
                }
                val focinho = oval(0.5f, cabecaY + cabecaR * 0.4f, cabecaR * 0.48f, cabecaR * 0.3f)
                drawOval(Color(0xFF9BE596), focinho.topLeft, focinho.size)
            }
        }

        // Olhos grandes com brilho (ou fechadinhos, ao piscar).
        for (lado in listOf(-1f, 1f)) {
            val olho = p(0.5f + lado * cabecaR * 0.38f, cabecaY - cabecaR * 0.02f)
            if (olhosFechados) {
                drawArc(Color(0xFF3D2C5E), 0f, 180f, false, Offset(olho.x - s * 0.035f, olho.y - s * 0.02f), Size(s * 0.07f, s * 0.04f), style = Stroke(traco * 1.3f, cap = StrokeCap.Round))
            } else {
                drawOval(Color(0xFF3D2C5E), Offset(olho.x - s * 0.034f, olho.y - s * 0.045f), Size(s * 0.068f, s * 0.09f))
                drawCircle(Color.White, s * 0.016f, Offset(olho.x + s * 0.012f, olho.y - s * 0.02f))
                drawCircle(Color.White, s * 0.007f, Offset(olho.x - s * 0.012f, olho.y + s * 0.015f))
            }
            // Bochechas.
            drawOval(Color(0x66FF7AA2), Offset(olho.x + lado * s * 0.03f - s * 0.035f, olho.y + s * 0.05f), Size(s * 0.07f, s * 0.04f))
        }
        // Nariz e sorriso (sempre feliz).
        val nariz = p(0.5f, cabecaY + cabecaR * 0.28f)
        when (especie) {
            EspecieMascote.CACHORRINHO -> drawOval(Color(0xFF3D2C5E), Offset(nariz.x - s * 0.03f, nariz.y - s * 0.02f), Size(s * 0.06f, s * 0.04f))
            EspecieMascote.GATINHO -> {
                val n = Path().apply {
                    moveTo(nariz.x - s * 0.02f, nariz.y - s * 0.012f); lineTo(nariz.x + s * 0.02f, nariz.y - s * 0.012f); lineTo(nariz.x, nariz.y + s * 0.012f); close()
                }
                drawPath(n, Color(0xFFFF6F91))
                // Bigodes.
                for (lado in listOf(-1f, 1f)) for (k in 0..1) {
                    drawLine(Color(0x993D2C5E), Offset(nariz.x + lado * s * 0.06f, nariz.y + k * s * 0.02f), Offset(nariz.x + lado * s * 0.14f, nariz.y - s * 0.01f + k * s * 0.03f), traco * 0.7f, StrokeCap.Round)
                }
            }
            EspecieMascote.DRAGAOZINHO -> for (lado in listOf(-1f, 1f)) drawCircle(Color(0xFF2E7F3C), s * 0.008f, Offset(nariz.x + lado * s * 0.025f, nariz.y))
        }
        val boca = Path().apply {
            moveTo(nariz.x - s * 0.045f, nariz.y + s * 0.025f)
            cubicTo(nariz.x - s * 0.03f, nariz.y + s * 0.06f, nariz.x + s * 0.03f, nariz.y + s * 0.06f, nariz.x + s * 0.045f, nariz.y + s * 0.025f)
        }
        drawPath(boca, Color(0xFF3D2C5E), style = Stroke(traco * 1.1f, cap = StrokeCap.Round))
        if (especie == EspecieMascote.CACHORRINHO) {
            // Linguinha de alegria.
            drawOval(Color(0xFFFF7A9C), Offset(nariz.x - s * 0.018f, nariz.y + s * 0.04f), Size(s * 0.036f, s * 0.035f))
        }
        // Topetinho do bebê.
        if (estagio == EstagioMascote.BEBE && especie != EspecieMascote.DRAGAOZINHO) {
            val topo = p(0.5f, cabecaY - cabecaR)
            val cacho = Path().apply {
                moveTo(topo.x, topo.y + s * 0.01f)
                cubicTo(topo.x - s * 0.03f, topo.y - s * 0.05f, topo.x + s * 0.04f, topo.y - s * 0.06f, topo.x + s * 0.02f, topo.y - s * 0.02f)
            }
            drawPath(cacho, c.contorno, style = Stroke(traco * 1.4f, cap = StrokeCap.Round))
        }

        desenharAcessorio(acessorio, s, ::p, cabecaY, cabecaR, pescocoY, corpoY, traco)
    }
}

private fun DrawScope.desenharAcessorio(
    a: AcessorioMascote?,
    s: Float,
    p: (Float, Float) -> Offset,
    cabecaY: Float,
    cabecaR: Float,
    pescocoY: Float,
    corpoY: Float,
    traco: Float,
) {
    when (a) {
        null, AcessorioMascote.CAPA, AcessorioMascote.MOCHILA -> Unit
        AcessorioMascote.LACINHO -> {
            val c = p(0.5f + cabecaR * 0.55f, cabecaY - cabecaR * 0.85f)
            for (lado in listOf(-1f, 1f)) {
                val laco = Path().apply {
                    moveTo(c.x, c.y)
                    lineTo(c.x + lado * s * 0.07f, c.y - s * 0.045f)
                    lineTo(c.x + lado * s * 0.07f, c.y + s * 0.045f)
                    close()
                }
                drawPath(laco, Color(0xFFFF5C9A))
                drawPath(laco, Color(0xFFD93A78), style = Stroke(traco))
            }
            drawCircle(Color(0xFFD93A78), s * 0.02f, c)
        }
        AcessorioMascote.BONE -> {
            val topo = p(0.5f, cabecaY - cabecaR * 0.55f)
            drawArc(Color(0xFF2B59C3), 180f, 180f, true, Offset(topo.x - s * cabecaR * 0.85f, topo.y - s * cabecaR * 0.55f), Size(s * cabecaR * 1.7f, s * cabecaR * 1.1f))
            drawRoundRect(Color(0xFF1E3F8F), Offset(topo.x - s * cabecaR * 0.1f, topo.y - s * 0.012f), Size(s * cabecaR * 1.25f, s * 0.03f), CornerRadius(s * 0.015f))
            drawCircle(Color(0xFFFFC93C), s * 0.012f, Offset(topo.x, topo.y - s * cabecaR * 0.55f))
        }
        AcessorioMascote.OCULOS -> {
            for (lado in listOf(-1f, 1f)) {
                drawCircle(Color(0xFF3D2C5E), s * 0.05f, p(0.5f + lado * cabecaR * 0.38f, cabecaY - cabecaR * 0.02f), style = Stroke(traco * 1.3f))
            }
            drawLine(Color(0xFF3D2C5E), p(0.5f - cabecaR * 0.15f, cabecaY - cabecaR * 0.05f), p(0.5f + cabecaR * 0.15f, cabecaY - cabecaR * 0.05f), traco * 1.2f)
        }
        AcessorioMascote.CACHECOL -> {
            drawLine(Color(0xFFE63946), p(0.5f - 0.12f, pescocoY), p(0.5f + 0.12f, pescocoY), s * 0.05f, StrokeCap.Round)
            drawLine(Color(0xFFE63946), p(0.5f + 0.07f, pescocoY), p(0.5f + 0.1f, pescocoY + 0.12f), s * 0.045f, StrokeCap.Round)
            for (k in 0..2) {
                drawLine(Color.White, p(0.5f - 0.08f + k * 0.07f, pescocoY - 0.015f), p(0.5f - 0.08f + k * 0.07f, pescocoY + 0.015f), traco * 1.3f)
            }
        }
        AcessorioMascote.MEDALHA -> {
            drawLine(Color(0xFF4D96FF), p(0.44f, pescocoY), p(0.5f, corpoY - 0.02f), traco * 1.6f)
            drawLine(Color(0xFF4D96FF), p(0.56f, pescocoY), p(0.5f, corpoY - 0.02f), traco * 1.6f)
            drawCircle(Color(0xFFE0A800), s * 0.045f, p(0.5f, corpoY + 0.01f))
            drawPath(caminhoEstrela(p(0.5f, corpoY + 0.01f), s * 0.03f), Color(0xFFFFE27A))
        }
        AcessorioMascote.COROA -> {
            val base = p(0.5f, cabecaY - cabecaR * 0.82f)
            val w = s * cabecaR * 1.1f
            val h = s * 0.08f
            val coroa = Path().apply {
                moveTo(base.x - w / 2, base.y)
                lineTo(base.x - w / 2, base.y - h)
                lineTo(base.x - w / 4, base.y - h * 0.5f)
                lineTo(base.x, base.y - h * 1.15f)
                lineTo(base.x + w / 4, base.y - h * 0.5f)
                lineTo(base.x + w / 2, base.y - h)
                lineTo(base.x + w / 2, base.y)
                close()
            }
            drawPath(coroa, Color(0xFFFFC93C))
            drawPath(coroa, Color(0xFFE0A800), style = Stroke(traco))
            drawCircle(Color(0xFFE63946), s * 0.012f, Offset(base.x, base.y - h * 0.45f))
        }
    }
}
