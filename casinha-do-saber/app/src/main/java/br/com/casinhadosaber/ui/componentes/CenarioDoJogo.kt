package br.com.casinhadosaber.ui.componentes

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Cenário das telas de jogo: em vez de um fundo de uma cor só, a criança joga num lugar de verdade, ao ar livre,
 * perto da casinha: céu com a cor do caminho, sol, nuvens fofas, morros, arbustos e florzinhas na cor do
 * caminho. É tudo parado (nada se mexe atrás das atividades, para não distrair) e desenhado uma vez por
 * tamanho e cor.
 */

/** Azul do céu de dia; o céu de cada caminho é este azul puxado para a cor do caminho. */
private val CeuAzul = Color(0xFF7CC8FF)
private val Grama = Color(0xFF7BCB5A)
private val GramaClara = Color(0xFFA8DE7E)
private val GramaFunda = Color(0xFF5DB443)

/** Fundo de cenário das telas de jogo, na cor do caminho ([cor]). */
fun Modifier.cenarioDoJogo(cor: Color): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    onDrawBehind { if (w >= 1f && h >= 1f) desenharCenarioDoJogo(cor) }
}

/** O cenário inteiro no tamanho do [DrawScope]. Separado do modificador para poder ser visto fora do app. */
fun DrawScope.desenharCenarioDoJogo(cor: Color) {
    val w = size.width
    val h = size.height
    // O céu é a cor do caminho clareada (laranja vira céu de fim de tarde, roxo vira céu lilás); misturar com
    // azul deixava o laranja cinzento.
    val ceuDeCima = lerp(cor, Color.White, 0.3f)
    val ceuDeBaixo = lerp(lerp(cor, Color.White, 0.82f), Color(0xFFFFF7E6), 0.35f)
    // Onde começam os morros: abaixo do meio, para a grama aparecer entre a figura e as respostas.
    val horizonte = h * 0.6f
    drawRect(Brush.verticalGradient(0f to ceuDeCima, 1f to ceuDeBaixo, startY = 0f, endY = horizonte + h * 0.1f))

    // Sol com um halo largo, à direita, logo abaixo do balão da pergunta.
    val sol = Offset(w * 0.86f, h * 0.27f)
    val raioSol = minOf(w, h) * 0.06f
    drawCircle(Brush.radialGradient(0f to Color(0x66FFF3B0), 1f to Color(0x00FFF3B0), center = sol, radius = raioSol * 3.2f), raioSol * 3.2f, sol)
    for (k in 0 until 10) {
        val a = k * 2f * PI.toFloat() / 10f
        drawLine(
            Color(0xB3FFD23F),
            Offset(sol.x + cos(a) * raioSol * 1.35f, sol.y + sin(a) * raioSol * 1.35f),
            Offset(sol.x + cos(a) * raioSol * 1.75f, sol.y + sin(a) * raioSol * 1.75f),
            strokeWidth = raioSol * 0.22f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
    drawCircle(Color(0xFFFFD84D), raioSol, sol)
    drawCircle(Color(0x66FFFFFF), raioSol * 0.55f, Offset(sol.x - raioSol * 0.3f, sol.y - raioSol * 0.3f))

    // Nuvens fofas (bolinhas brancas com a barriga um pouco mais escura).
    nuvem(Offset(w * 0.16f, h * 0.28f), w * 0.09f)
    nuvem(Offset(w * 0.55f, h * 0.25f), w * 0.06f)
    nuvem(Offset(w * 0.06f, h * 0.5f), w * 0.06f)
    nuvem(Offset(w * 0.94f, h * 0.47f), w * 0.07f)

    // Morro do fundo (mais claro, azulado pela distância) e morro da frente.
    val morroDeTras = Path().apply {
        moveTo(0f, horizonte + h * 0.03f)
        cubicTo(w * 0.2f, horizonte - h * 0.07f, w * 0.38f, horizonte - h * 0.05f, w * 0.55f, horizonte + h * 0.0f)
        cubicTo(w * 0.72f, horizonte - h * 0.08f, w * 0.9f, horizonte - h * 0.06f, w, horizonte - h * 0.01f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(morroDeTras, lerp(GramaClara, CeuAzul, 0.25f))
    // Casinha pequenininha no morro de trás: o caminho de volta para casa.
    casinhaAoLonge(Offset(w * 0.8f, horizonte + h * 0.005f), w * 0.06f)
    val morroDaFrente = Path().apply {
        moveTo(0f, horizonte + h * 0.08f)
        cubicTo(w * 0.3f, horizonte + h * 0.0f, w * 0.6f, horizonte + h * 0.1f, w, horizonte + h * 0.04f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(morroDaFrente, Brush.verticalGradient(0f to Grama, 1f to GramaFunda, startY = horizonte, endY = h))
    // Brilho na crista do morro da frente.
    drawPath(
        Path().apply {
            moveTo(0f, horizonte + h * 0.08f)
            cubicTo(w * 0.3f, horizonte + h * 0.0f, w * 0.6f, horizonte + h * 0.1f, w, horizonte + h * 0.04f)
        },
        Color(0x55FFFFFF),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = h * 0.006f),
    )

    // Arbustos nos cantos e florzinhas espalhadas (na cor do caminho e em amarelo), sempre nas mesmas posições.
    arbusto(Offset(w * 0.06f, horizonte + h * 0.09f), w * 0.07f)
    arbusto(Offset(w * 0.95f, horizonte + h * 0.07f), w * 0.08f)
    val corDaFlor = lerp(cor, Color.White, 0.1f)
    val flores = listOf(
        0.12f to 0.7f, 0.3f to 0.665f, 0.47f to 0.69f, 0.62f to 0.675f, 0.88f to 0.72f,
        0.2f to 0.82f, 0.58f to 0.86f, 0.9f to 0.88f, 0.05f to 0.93f, 0.38f to 0.95f, 0.74f to 0.97f,
    )
    for ((i, f) in flores.withIndex()) {
        val c = Offset(w * f.first, h * f.second)
        val ehAmarela = i % 3 == 1
        flor(c, w * 0.014f, if (ehAmarela) Color(0xFFFFD23F) else corDaFlor)
    }
    // Tufinhos de grama.
    for ((u, v) in listOf(0.24f to 0.72f, 0.52f to 0.69f, 0.78f to 0.78f, 0.14f to 0.88f, 0.66f to 0.92f)) {
        tufo(Offset(w * u, h * v), w * 0.02f)
    }
}

private fun DrawScope.nuvem(c: Offset, r: Float) {
    val sombra = Color(0x1F6E8BB5)
    drawCircle(sombra, r * 0.62f, Offset(c.x - r * 0.62f, c.y + r * 0.12f))
    drawCircle(sombra, r * 0.78f, Offset(c.x + r * 0.02f, c.y + r * 0.08f))
    drawCircle(sombra, r * 0.6f, Offset(c.x + r * 0.68f, c.y + r * 0.14f))
    drawCircle(Color.White, r * 0.58f, Offset(c.x - r * 0.62f, c.y + r * 0.04f))
    drawCircle(Color.White, r * 0.8f, c)
    drawCircle(Color.White, r * 0.56f, Offset(c.x + r * 0.66f, c.y + r * 0.06f))
    drawRect(Color.White, Offset(c.x - r * 0.62f, c.y), Size(r * 1.3f, r * 0.6f))
}

private fun DrawScope.arbusto(c: Offset, r: Float) {
    drawCircle(GramaFunda, r * 0.75f, Offset(c.x - r * 0.6f, c.y))
    drawCircle(GramaFunda, r, Offset(c.x, c.y - r * 0.25f))
    drawCircle(GramaFunda, r * 0.7f, Offset(c.x + r * 0.65f, c.y + r * 0.05f))
    drawCircle(Color(0x3DFFFFFF), r * 0.35f, Offset(c.x - r * 0.25f, c.y - r * 0.55f))
    drawCircle(Color(0xFFFF6B6B), r * 0.1f, Offset(c.x + r * 0.3f, c.y - r * 0.3f))
    drawCircle(Color(0xFFFF6B6B), r * 0.1f, Offset(c.x - r * 0.55f, c.y + r * 0.05f))
}

private fun DrawScope.flor(c: Offset, r: Float, cor: Color) {
    drawLine(GramaFunda, c, Offset(c.x, c.y + r * 2.4f), strokeWidth = r * 0.45f)
    for (k in 0 until 5) {
        val a = k * 2f * PI.toFloat() / 5f
        drawCircle(cor, r * 0.62f, Offset(c.x + cos(a) * r * 0.75f, c.y + sin(a) * r * 0.75f))
    }
    drawCircle(Color(0xFFFFF3B0), r * 0.5f, c)
}

private fun DrawScope.tufo(c: Offset, r: Float) {
    for (k in -1..1) {
        drawLine(GramaFunda, Offset(c.x + k * r * 0.35f, c.y), Offset(c.x + k * r * 0.8f, c.y - r * (1.2f - kotlin.math.abs(k) * 0.3f)), strokeWidth = r * 0.28f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

/** Casinha de longe, igual à do jogo (telhado vermelho). [c] é o meio da base. */
private fun DrawScope.casinhaAoLonge(c: Offset, lado: Float) {
    val parede = Color(0xFFFFF4DC)
    drawRect(parede, Offset(c.x - lado / 2f, c.y - lado * 0.75f), Size(lado, lado * 0.75f))
    drawPath(
        Path().apply {
            moveTo(c.x - lado * 0.65f, c.y - lado * 0.72f)
            lineTo(c.x, c.y - lado * 1.3f)
            lineTo(c.x + lado * 0.65f, c.y - lado * 0.72f)
            close()
        },
        // O mesmo telhado vermelho da casinha do jogo.
        Color(0xFFEF476F),
    )
    drawRect(Color(0xFF9A6A4A), Offset(c.x - lado * 0.12f, c.y - lado * 0.38f), Size(lado * 0.24f, lado * 0.38f))
    drawRect(Color(0xFF8FD3FF), Offset(c.x + lado * 0.2f, c.y - lado * 0.6f), Size(lado * 0.2f, lado * 0.18f))
}
