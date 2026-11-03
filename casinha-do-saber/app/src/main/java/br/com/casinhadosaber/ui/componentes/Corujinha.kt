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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/** A Corujinha Professora (narradora da história), desenhada em Canvas: óculos, capelo e um livrinho. */
@Composable
fun Corujinha(modifier: Modifier = Modifier, falando: Boolean = false) {
    val transicao = rememberInfiniteTransition(label = "coruja")
    val respiro by transicao.animateFloat(1f, 1.03f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "respiro")
    val piscar by transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(3600, easing = LinearEasing)), label = "piscar")
    val bico by transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(180), RepeatMode.Reverse), label = "bico")
    Canvas(
        modifier
            .aspectRatio(0.9f)
            .semantics { contentDescription = "Corujinha Professora" }
            .graphicsLayer {
                scaleY = respiro
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
    ) {
        desenharCorujinha(olhosFechados = piscar > 0.96f, bicoAberto = falando && bico > 0.5f)
    }
}

fun DrawScope.desenharCorujinha(olhosFechados: Boolean = false, bicoAberto: Boolean = false) {
    val w = size.width
    val h = size.height
    fun p(u: Float, v: Float) = Offset(u * w, v * h)
    val penas = Color(0xFFA9774E)
    val contorno = Color(0xFF6E4A2E)
    val peito = Color(0xFFF3DDB8)
    val traco = w * 0.015f

    // Sombra.
    drawOval(Color(0x26000000), p(0.2f, 0.95f), Size(w * 0.6f, h * 0.045f))
    // Galho onde ela está pousada.
    drawLine(Color(0xFF8A5A35), p(0.04f, 0.93f), p(0.96f, 0.91f), w * 0.05f, StrokeCap.Round)
    drawCircle(Color(0xFF7CC56B), w * 0.04f, p(0.12f, 0.88f))
    drawCircle(Color(0xFF7CC56B), w * 0.035f, p(0.88f, 0.86f))

    // Corpo.
    drawOval(penas, p(0.2f, 0.36f), Size(w * 0.6f, h * 0.56f))
    drawOval(contorno, p(0.2f, 0.36f), Size(w * 0.6f, h * 0.56f), style = Stroke(traco))
    drawOval(peito, p(0.31f, 0.5f), Size(w * 0.38f, h * 0.38f))
    // Peninhas do peito em "v".
    for (linha in 0..2) for (k in 0..(2 - linha % 2)) {
        val c = p(0.4f + k * 0.1f - (linha % 2) * 0.05f + (linha % 2) * 0.1f, 0.6f + linha * 0.08f)
        val v = Path().apply { moveTo(c.x - w * 0.03f, c.y); lineTo(c.x, c.y + h * 0.025f); lineTo(c.x + w * 0.03f, c.y) }
        drawPath(v, Color(0xFFD9B98A), style = Stroke(traco, cap = StrokeCap.Round))
    }
    // Asas.
    for (lado in listOf(-1f, 1f)) {
        val asa = Path().apply {
            val topo = p(0.5f + lado * 0.27f, 0.48f)
            moveTo(topo.x, topo.y)
            cubicTo(p(0.5f + lado * 0.4f, 0.6f).x, p(0.5f + lado * 0.4f, 0.6f).y, p(0.5f + lado * 0.36f, 0.8f).x, p(0.5f + lado * 0.36f, 0.8f).y, p(0.5f + lado * 0.24f, 0.86f).x, p(0.5f + lado * 0.24f, 0.86f).y)
            cubicTo(p(0.5f + lado * 0.25f, 0.7f).x, p(0.5f + lado * 0.25f, 0.7f).y, p(0.5f + lado * 0.22f, 0.6f).x, p(0.5f + lado * 0.22f, 0.6f).y, topo.x, topo.y)
        }
        drawPath(asa, Color(0xFF8C5E3C))
        drawPath(asa, contorno, style = Stroke(traco))
    }
    // Livrinho debaixo da asa direita.
    withTransform({ rotate(-12f, p(0.76f, 0.74f)) }) {
        drawRoundRect(Color(0xFF4D96FF), p(0.66f, 0.68f), Size(w * 0.2f, h * 0.12f), CornerRadius(w * 0.015f))
        drawRect(Color.White, p(0.68f, 0.69f), Size(w * 0.17f, h * 0.025f))
        drawLine(Color(0xFF2B59C3), p(0.66f, 0.68f), p(0.66f, 0.8f), traco * 1.5f)
    }
    // Pezinhos.
    for (u in listOf(0.42f, 0.58f)) for (k in -1..1) {
        drawLine(Color(0xFFF2A541), p(u, 0.89f), p(u + k * 0.025f, 0.925f), traco * 1.8f, StrokeCap.Round)
    }

    // Cabeça (um pouco mais larga que o corpo) com as orelhinhas de pena.
    for (lado in listOf(-1f, 1f)) {
        val tufo = Path().apply {
            moveTo(p(0.5f + lado * 0.3f, 0.2f).x, p(0.5f + lado * 0.3f, 0.2f).y)
            lineTo(p(0.5f + lado * 0.36f, 0.06f).x, p(0.5f + lado * 0.36f, 0.06f).y)
            lineTo(p(0.5f + lado * 0.16f, 0.15f).x, p(0.5f + lado * 0.16f, 0.15f).y)
            close()
        }
        drawPath(tufo, penas)
        drawPath(tufo, contorno, style = Stroke(traco))
    }
    drawOval(penas, p(0.16f, 0.1f), Size(w * 0.68f, h * 0.38f))
    drawOval(contorno, p(0.16f, 0.1f), Size(w * 0.68f, h * 0.38f), style = Stroke(traco))
    // Discos claros dos olhos.
    for (lado in listOf(-1f, 1f)) {
        val c = p(0.5f + lado * 0.14f, 0.29f)
        drawCircle(peito, w * 0.13f, c)
        if (olhosFechados) {
            drawArc(Color(0xFF3D2C5E), 0f, 180f, false, Offset(c.x - w * 0.05f, c.y - h * 0.02f), Size(w * 0.1f, h * 0.04f), style = Stroke(traco * 1.4f, cap = StrokeCap.Round))
        } else {
            drawCircle(Color(0xFF3D2C5E), w * 0.06f, c)
            drawCircle(Color.White, w * 0.022f, Offset(c.x + w * 0.02f, c.y - h * 0.02f))
        }
        // Óculos redondos de professora.
        drawCircle(Color(0xFF6C63FF), w * 0.105f, c, style = Stroke(traco * 1.4f))
        // Bochechas.
        drawOval(Color(0x55FF7AA2), Offset(c.x - w * 0.05f + lado * w * 0.07f, c.y + h * 0.07f), Size(w * 0.08f, h * 0.035f))
    }
    drawLine(Color(0xFF6C63FF), p(0.45f, 0.28f), p(0.55f, 0.28f), traco * 1.4f)
    // Bico.
    val bico = Path().apply {
        moveTo(p(0.46f, 0.34f).x, p(0.46f, 0.34f).y)
        lineTo(p(0.54f, 0.34f).x, p(0.54f, 0.34f).y)
        lineTo(p(0.5f, if (bicoAberto) 0.43f else 0.41f).x, p(0.5f, if (bicoAberto) 0.43f else 0.41f).y)
        close()
    }
    drawPath(bico, Color(0xFFF2A541))
    if (bicoAberto) drawLine(Color(0xFFC77A1E), p(0.47f, 0.37f), p(0.53f, 0.37f), traco)

    // Capelo de formatura: quadrado escuro, base e pingente dourado.
    val topo = p(0.5f, 0.11f)
    drawRoundRect(Color(0xFF3D2C5E), Offset(topo.x - w * 0.14f, topo.y - h * 0.01f), Size(w * 0.28f, h * 0.07f), CornerRadius(w * 0.02f))
    val tampa = Path().apply {
        moveTo(topo.x - w * 0.26f, topo.y - h * 0.02f)
        lineTo(topo.x, topo.y - h * 0.09f)
        lineTo(topo.x + w * 0.26f, topo.y - h * 0.02f)
        lineTo(topo.x, topo.y + h * 0.04f)
        close()
    }
    drawPath(tampa, Color(0xFF4A3A6E))
    drawLine(Color(0xFFFFC93C), Offset(topo.x, topo.y - h * 0.025f), Offset(topo.x + w * 0.2f, topo.y + h * 0.02f), traco)
    drawLine(Color(0xFFFFC93C), Offset(topo.x + w * 0.2f, topo.y + h * 0.02f), Offset(topo.x + w * 0.2f, topo.y + h * 0.1f), traco)
    drawCircle(Color(0xFFFFC93C), w * 0.025f, Offset(topo.x + w * 0.2f, topo.y + h * 0.11f))
}
