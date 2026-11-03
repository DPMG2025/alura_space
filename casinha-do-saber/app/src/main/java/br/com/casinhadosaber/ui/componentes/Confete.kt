package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Cores das festas (confete, explosões): vivas e alegres. */
val CoresDaFesta = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFC93C), Color(0xFF3DBE6B), Color(0xFF4D96FF), Color(0xFFB57BFF), Color(0xFFFF8FAB),
)

/** 0 = papel retangular, 1 = estrela, 2 = bolinha. */
private class Papel(val angulo: Float, val velocidade: Float, val giro: Float, val cor: Color, val forma: Int)

/** Explosão de confete (papéis, estrelas e bolinhas). Muda o [disparo] para soltar outra. */
@Composable
fun Confete(disparo: Int, modifier: Modifier = Modifier, quantidade: Int = 36) {
    if (disparo == 0) return
    val progresso = remember(disparo) { Animatable(0f) }
    val papeis = remember(disparo) {
        val r = Random(disparo)
        List(quantidade) {
            val sorteio = r.nextInt(8)
            Papel(
                angulo = (r.nextFloat() * 160f + 190f) * (PI.toFloat() / 180f),
                velocidade = 0.6f + r.nextFloat() * 0.6f,
                giro = r.nextFloat() * 720f - 360f,
                cor = CoresDaFesta[r.nextInt(CoresDaFesta.size)],
                forma = when (sorteio) { 0, 1 -> 1; 2 -> 2; else -> 0 },
            )
        }
    }
    LaunchedEffect(disparo) { progresso.animateTo(1f, tween(1400, easing = LinearEasing)) }
    // O progresso é lido só no desenho (camada própria); a composição só fica sabendo quando termina.
    val terminou = remember(progresso) { derivedStateOf { progresso.value >= 1f } }
    if (terminou.value) return
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = progresso.value
        val origem = Offset(size.width / 2, size.height * 0.55f)
        val alcance = size.minDimension * 0.7f
        for (p in papeis) {
            val d = alcance * p.velocidade * t
            val x = origem.x + cos(p.angulo) * d
            val y = origem.y + sin(p.angulo) * d + size.height * 0.5f * t * t
            val alfa = (1f - t).coerceIn(0f, 1f)
            when (p.forma) {
                1 -> rotate(p.giro * t, Offset(x, y)) { drawPath(caminhoEstrela(Offset(x, y), 9.dp.toPx()), p.cor.copy(alpha = alfa)) }
                2 -> drawCircle(p.cor.copy(alpha = alfa), 5.dp.toPx(), Offset(x, y))
                else -> rotate(p.giro * t, Offset(x, y)) {
                    drawRect(p.cor.copy(alpha = alfa), Offset(x - 5.dp.toPx(), y - 3.dp.toPx()), Size(10.dp.toPx(), 6.dp.toPx()))
                }
            }
        }
    }
}

private class Faisca(val angulo: Float, val alcance: Float, val tamanho: Float, val cor: Color, val estrela: Boolean, val giro: Float)

/**
 * Estouro de estrelas e bolinhas para os momentos de acerto e de prêmio: um anel de luz que se abre
 * e faíscas que saem do ponto ([centroX], [centroY], frações do espaço do modificador), crescem e somem
 * em [duracao] ms. Muda o [disparo] para soltar outro; 0 não mostra nada. Desenha só durante o estouro.
 */
@Composable
fun ExplosaoDeEstrelas(
    disparo: Int,
    modifier: Modifier = Modifier,
    centroX: Float = 0.5f,
    centroY: Float = 0.5f,
    quantidade: Int = 14,
    cores: List<Color> = CoresDaFesta,
    duracao: Int = 800,
) {
    if (disparo == 0) return
    val progresso = remember(disparo) { Animatable(0f) }
    val faiscas = remember(disparo, quantidade) {
        val r = Random(disparo * 31 + 7)
        List(quantidade) { i ->
            Faisca(
                angulo = (i + r.nextFloat() * 0.6f) * 2f * PI.toFloat() / quantidade,
                alcance = 0.65f + r.nextFloat() * 0.35f,
                tamanho = 0.7f + r.nextFloat() * 0.6f,
                cor = cores[i % cores.size],
                estrela = i % 3 != 2,
                giro = r.nextFloat() * 240f - 120f,
            )
        }
    }
    LaunchedEffect(disparo) { progresso.animateTo(1f, tween(duracao, easing = LinearEasing)) }
    // O progresso é lido só no desenho (camada própria); a composição só fica sabendo quando termina.
    val terminou = remember(progresso) { derivedStateOf { progresso.value >= 1f } }
    if (terminou.value) return
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = progresso.value
        // Sai rápido e freia (ease out cúbico); cresce no começo e encolhe no fim.
        val saida = 1f - (1f - t) * (1f - t) * (1f - t)
        val escala = if (t < 0.3f) t / 0.3f else 1f - (t - 0.3f) / 0.7f * 0.6f
        val alfa = if (t < 0.6f) 1f else (1f - (t - 0.6f) / 0.4f)
        val c = Offset(size.width * centroX, size.height * centroY)
        val alcance = size.minDimension * 0.42f
        // Clarão no meio e anel de luz que se abre por dentro das faíscas.
        if (t < 0.35f) drawCircle(Color.White.copy(alpha = 0.55f * (1f - t / 0.35f)), alcance * (0.12f + 0.5f * t), c)
        drawCircle(
            Color.White.copy(alpha = 0.7f * (1f - t)),
            radius = alcance * (0.15f + 0.55f * saida),
            center = c,
            style = Stroke((12.dp.toPx() * (1f - t)).coerceAtLeast(0.5f)),
        )
        for (f in faiscas) {
            val d = alcance * f.alcance * saida
            val p = Offset(c.x + cos(f.angulo) * d, c.y + sin(f.angulo) * d)
            val r = 13.dp.toPx() * f.tamanho * escala
            if (f.estrela) {
                rotate(f.giro * t, p) {
                    drawPath(caminhoEstrela(p, r), f.cor.copy(alpha = alfa))
                    drawPath(caminhoEstrela(p, r * 0.45f), Color.White.copy(alpha = 0.6f * alfa))
                }
            } else {
                drawCircle(f.cor.copy(alpha = alfa), r * 0.55f, p)
            }
        }
    }
}

/**
 * Raios de luz girando devagar atrás de um prêmio, do mascote que cresceu ou de um diploma:
 * [quantidade] fatias saindo do centro, que somem para fora. Uma só animação infinita, lida no desenho.
 * Com [girando] falso, os raios ficam parados (bom para capturas).
 */
@Composable
fun RaiosDeLuz(
    modifier: Modifier = Modifier,
    cor: Color = Color.White,
    quantidade: Int = 12,
    alfa: Float = 0.4f,
    girando: Boolean = true,
    centroX: Float = 0.5f,
    centroY: Float = 0.5f,
) {
    val angulo = if (girando && animacoesLigadas()) {
        rememberInfiniteTransition(label = "raios").animateFloat(0f, 360f, infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "giro")
    } else {
        null
    }
    Spacer(
        modifier
            .fillMaxSize()
            .clipToBounds()
            // Camada própria: o giro redesenha só os raios, não o que está em volta.
            .graphicsLayer()
            .drawWithCache {
                val c = Offset(size.width * centroX, size.height * centroY)
                val raio = size.minDimension * 0.75f
                val meia = (PI / quantidade / 2).toFloat()
                val fatias = Path()
                for (i in 0 until quantidade) {
                    val a = (i * 2 * PI / quantidade).toFloat()
                    fatias.moveTo(c.x, c.y)
                    fatias.lineTo(c.x + cos(a - meia) * raio, c.y + sin(a - meia) * raio)
                    fatias.lineTo(c.x + cos(a + meia) * raio, c.y + sin(a + meia) * raio)
                    fatias.close()
                }
                val pincel = Brush.radialGradient(
                    0f to cor.copy(alpha = alfa),
                    0.55f to cor.copy(alpha = alfa * 0.45f),
                    1f to Color.Transparent,
                    center = c,
                    radius = raio,
                )
                val miolo = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent), c, raio * 0.35f)
                onDrawBehind {
                    rotate(angulo?.value ?: 0f, c) { drawPath(fatias, pincel) }
                    drawCircle(miolo, raio * 0.35f, c)
                }
            },
    )
}
