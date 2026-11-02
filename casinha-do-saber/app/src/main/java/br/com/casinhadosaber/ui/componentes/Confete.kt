package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val coresConfete = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFC93C), Color(0xFF3DBE6B), Color(0xFF4D96FF), Color(0xFFB57BFF), Color(0xFFFF8FAB),
)

private class Papel(val angulo: Float, val velocidade: Float, val giro: Float, val cor: Color, val estrela: Boolean)

/** Explosão de confete. Muda o [disparo] para soltar outra. */
@Composable
fun Confete(disparo: Int, modifier: Modifier = Modifier, quantidade: Int = 36) {
    if (disparo == 0) return
    val progresso = remember(disparo) { Animatable(0f) }
    val papeis = remember(disparo) {
        val r = Random(disparo)
        List(quantidade) {
            Papel(
                angulo = (r.nextFloat() * 160f + 190f) * (Math.PI.toFloat() / 180f),
                velocidade = 0.6f + r.nextFloat() * 0.6f,
                giro = r.nextFloat() * 720f - 360f,
                cor = coresConfete[r.nextInt(coresConfete.size)],
                estrela = r.nextInt(4) == 0,
            )
        }
    }
    LaunchedEffect(disparo) { progresso.animateTo(1f, tween(1400, easing = LinearEasing)) }
    if (progresso.value >= 1f) return
    Canvas(modifier.fillMaxSize()) {
        val t = progresso.value
        val origem = Offset(size.width / 2, size.height * 0.55f)
        val alcance = size.minDimension * 0.7f
        for (p in papeis) {
            val d = alcance * p.velocidade * t
            val x = origem.x + cos(p.angulo) * d
            val y = origem.y + sin(p.angulo) * d + size.height * 0.5f * t * t
            val alfa = (1f - t).coerceIn(0f, 1f)
            rotate(p.giro * t, Offset(x, y)) {
                if (p.estrela) {
                    drawPath(caminhoEstrela(Offset(x, y), 9.dp.toPx()), p.cor.copy(alpha = alfa))
                } else {
                    drawRect(p.cor.copy(alpha = alfa), Offset(x - 5.dp.toPx(), y - 3.dp.toPx()), Size(10.dp.toPx(), 6.dp.toPx()))
                }
            }
        }
    }
}
