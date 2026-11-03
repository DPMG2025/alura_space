package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import br.com.casinhadosaber.dominio.DirecaoDaSeta

/**
 * Seta grossa e arredondada, desenhada em Canvas no traço do app (labirinto). Quem toca e fala é o cartão
 * em volta; aqui é só o desenho, com sombrinha embaixo como os botões fofos.
 */
@Composable
fun SetaDesenhada(
    direcao: DirecaoDaSeta,
    modifier: Modifier = Modifier,
    cor: Color = Color(0xFFFF9F43),
    corSombra: Color = Color(0xFFD9772B),
) {
    Canvas(modifier) { desenharSeta(direcao, cor, corSombra) }
}

fun DrawScope.desenharSeta(direcao: DirecaoDaSeta, cor: Color, corSombra: Color) {
    val s = minOf(size.width, size.height)
    val ox = (size.width - s) / 2f
    val oy = (size.height - s) / 2f
    // Seta para a direita num quadrado de lado s; as outras direções giram em volta do meio.
    fun p(x: Float, y: Float) = Offset(ox + x * s, oy + y * s)
    val contorno = Path().apply {
        val pts = listOf(p(0.16f, 0.39f), p(0.5f, 0.39f), p(0.5f, 0.18f), p(0.86f, 0.5f), p(0.5f, 0.82f), p(0.5f, 0.61f), p(0.16f, 0.61f))
        moveTo(pts[0].x, pts[0].y)
        for (k in 1 until pts.size) lineTo(pts[k].x, pts[k].y)
        close()
    }
    val arredondar = Stroke(width = s * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    rotate(direcao.graus, pivot = Offset(ox + s / 2f, oy + s / 2f)) {
        // A sombra cai sempre para baixo na tela, qualquer que seja a direção.
        val queda = s * 0.05f
        val rad = Math.toRadians(-direcao.graus.toDouble())
        val dx = (-kotlin.math.sin(rad) * queda).toFloat()
        val dy = (kotlin.math.cos(rad) * queda).toFloat()
        translate(dx, dy) {
            drawPath(contorno, corSombra)
            drawPath(contorno, corSombra, style = arredondar)
        }
        drawPath(contorno, cor)
        drawPath(contorno, cor, style = arredondar)
        // brilho na haste
        drawLine(Color(0x66FFFFFF), p(0.2f, 0.44f), p(0.46f, 0.44f), strokeWidth = s * 0.045f, cap = StrokeCap.Round)
    }
}
