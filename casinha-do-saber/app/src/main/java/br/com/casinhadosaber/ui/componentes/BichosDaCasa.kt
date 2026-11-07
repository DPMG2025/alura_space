package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.Gesto
import br.com.casinhadosaber.dominio.Passeio
import br.com.casinhadosaber.dominio.Passo
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.ui.LocalSons
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Bichinhos da casa desenhados de corpo inteiro, de lado, no traço da casa (o cachorrinho e o gatinho).
 * São diferentes do mascote ([MascoteAnimado], de frente, no canto): estes moram no jardim e passeiam.
 */
enum class Bicho { CACHORRO, GATO }

/** Cores de um bicho: pelo, barriga e focinho, orelha e manchas, coleira. */
data class PeloDoBicho(val pelo: Color, val claro: Color, val escuro: Color, val coleira: Color)

private val pelagensDeCachorro = listOf(
    PeloDoBicho(Color(0xFFD08F4E), Color(0xFFFFEBD2), Color(0xFF8A5528), Color(0xFFE5484D)),
    PeloDoBicho(Color(0xFFF4F1EC), Color(0xFFFFFFFF), Color(0xFF3B3B3B), Color(0xFF3D8BFD)),
    PeloDoBicho(Color(0xFF9A8C82), Color(0xFFE9E2DC), Color(0xFF5D524B), Color(0xFF2FB36A)),
)

private val pelagensDeGato = listOf(
    PeloDoBicho(Color(0xFFF5A548), Color(0xFFFFE9C9), Color(0xFFC9711F), Color(0xFF7B61FF)),
    PeloDoBicho(Color(0xFF8E9AA6), Color(0xFFE6EBF0), Color(0xFF5B6772), Color(0xFFFF6FAE)),
    PeloDoBicho(Color(0xFF3A3A40), Color(0xFFF7F7F7), Color(0xFF1F1F24), Color(0xFFFFC93C)),
)

/** Qual bicho desenhado um item é (null quando é emoji): o cachorrinho e o gatinho da casa e os da área do pet. */
fun bichoDesenhadoDe(id: String): Pair<Bicho, PeloDoBicho>? {
    val tipo = CasaQueCresce.tipoDe(id)
    val copia = id.split('.').getOrNull(2)?.toIntOrNull() ?: 1
    // Na área do pet, o primeiro cachorrinho é de outra cor que o da casa: dá para ver que é outro.
    val indice = if ('.' in id) copia else 0
    return when (tipo) {
        "cachorro" -> Bicho.CACHORRO to pelagensDeCachorro[indice % pelagensDeCachorro.size]
        "gato" -> Bicho.GATO to pelagensDeGato[indice % pelagensDeGato.size]
        else -> null
    }
}

/** Pose do bicho fora da cena (baú, prêmio): parado, olhando para a direita. */
internal val PARADO_NO_BAU = Passo(0f, true, Gesto.PARADO, 0f)

/** Relógio do passeio (0 a [Passeio.CICLO_MS] ms, sem parar). Animação infinita: não segura o teste do emulador. */
@Composable
fun rememberRelogioDoPasseio(): State<Float> =
    rememberInfiniteTransition(label = "passeio").animateFloat(
        initialValue = 0f,
        targetValue = Passeio.CICLO_MS.toFloat(),
        animationSpec = infiniteRepeatable(tween(Passeio.CICLO_MS.toInt(), easing = LinearEasing)),
        label = "relogio",
    )

/** Desenha o bicho no tamanho dado; [passo] é lido só na hora de desenhar (não recompõe a cada quadro). */
@Composable
fun BichoDesenhado(bicho: Bicho, pelagem: PeloDoBicho, passo: () -> Passo, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val p = passo()
        scale(if (p.paraDireita) 1f else -1f, 1f) {
            when (bicho) {
                Bicho.CACHORRO -> desenharCachorro(p, pelagem)
                Bicho.GATO -> desenharGato(p, pelagem)
            }
        }
    }
}

private fun DrawScope.o(x: Float, y: Float) = Offset(x * size.width, y * size.height)
private fun DrawScope.t(w: Float, h: Float) = Size(w * size.width, h * size.height)

/** Uma perninha que gira no quadril ([angulo] em graus): retângulo arredondado com a patinha clara. */
private fun DrawScope.perna(x: Float, topo: Float, comprimento: Float, angulo: Float, cor: Color, pata: Color) {
    rotate(angulo, o(x, topo)) {
        drawRoundRect(cor, o(x - 0.035f, topo), t(0.07f, comprimento), androidx.compose.ui.geometry.CornerRadius(size.width * 0.035f))
        drawOval(pata, o(x - 0.042f, topo + comprimento - 0.03f), t(0.084f, 0.05f))
    }
}

private fun balanco(p: Passo, defasagem: Float) = sin(((p.passada + defasagem) * 2f * PI).toFloat())

private fun DrawScope.desenharCachorro(p: Passo, c: PeloDoBicho) {
    val contorno = Stroke(width = size.width * 0.018f)
    val sentado = p.gesto == Gesto.SENTADO
    val andar = if (p.gesto == Gesto.ANDANDO) 24f else 0f
    // Sombra no chão.
    drawOval(Color(0x33000000), o(0.18f, 0.93f), t(0.66f, 0.07f))
    // Rabo: abana rápido quando está feliz (ABANANDO), devagar andando.
    val abano = when (p.gesto) {
        Gesto.ABANANDO -> 34f * sin(p.passada * 4f * PI.toFloat())
        Gesto.ANDANDO -> 14f * balanco(p, 0.25f)
        else -> 0f
    }
    val quadril = if (sentado) o(0.30f, 0.80f) else o(0.27f, 0.58f)
    rotate(-20f + abano, quadril) {
        val r = Path().apply {
            moveTo(quadril.x, quadril.y)
            quadraticTo(quadril.x - size.width * 0.12f, quadril.y - size.height * 0.04f, quadril.x - size.width * 0.13f, quadril.y - size.height * 0.20f)
        }
        drawPath(r, c.pelo, style = Stroke(width = size.width * 0.06f, cap = StrokeCap.Round))
        drawCircle(c.claro, size.width * 0.035f, Offset(quadril.x - size.width * 0.13f, quadril.y - size.height * 0.20f))
    }
    if (sentado) {
        // Sentado: o corpo inclinado para cima, a coxa no chão e as patas da frente esticadas.
        drawOval(c.escuro, o(0.22f, 0.70f), t(0.30f, 0.24f))
        drawOval(c.pelo, o(0.20f, 0.68f), t(0.30f, 0.24f))
        perna(0.62f, 0.62f, 0.33f, 0f, c.pelo, c.claro)
        perna(0.54f, 0.62f, 0.33f, 0f, c.pelo, c.claro)
        rotate(-32f, o(0.32f, 0.80f)) {
            drawOval(c.pelo, o(0.24f, 0.50f), t(0.48f, 0.27f))
            drawOval(c.claro, o(0.40f, 0.62f), t(0.28f, 0.14f))
            drawOval(c.escuro, o(0.30f, 0.50f), t(0.18f, 0.10f))
        }
    } else {
        // Pernas de trás e da frente, em pares que se alternam.
        perna(0.32f, 0.68f, 0.24f, andar * balanco(p, 0f), c.escuro, c.claro)
        perna(0.66f, 0.68f, 0.24f, andar * balanco(p, 0.5f), c.escuro, c.claro)
        drawOval(c.pelo, o(0.22f, 0.50f), t(0.56f, 0.28f))
        drawOval(c.claro, o(0.36f, 0.64f), t(0.34f, 0.13f))
        drawOval(c.escuro, o(0.30f, 0.50f), t(0.20f, 0.11f))
        perna(0.38f, 0.68f, 0.24f, andar * balanco(p, 0.5f), c.pelo, c.claro)
        perna(0.72f, 0.68f, 0.24f, andar * balanco(p, 0f), c.pelo, c.claro)
    }
    // Cabeça (um pouco mais alta sentado), com coleira, focinho, nariz, olho e orelha caída.
    val cy = if (sentado) 0.30f else 0.40f
    val cx = if (sentado) 0.68f else 0.78f
    drawRoundRect(c.coleira, o(cx - 0.10f, cy + 0.09f), t(0.13f, 0.06f), androidx.compose.ui.geometry.CornerRadius(size.width * 0.03f))
    drawCircle(Color(0xFFFFD166), size.width * 0.022f, o(cx - 0.03f, cy + 0.16f))
    drawCircle(c.pelo, size.width * 0.15f, o(cx, cy))
    drawOval(c.claro, o(cx + 0.03f, cy + 0.01f), t(0.17f, 0.11f))
    drawCircle(Color(0xFF2B1B12), size.width * 0.03f, o(cx + 0.19f, cy + 0.02f))
    drawCircle(Color(0xFF2B1B12), size.width * 0.026f, o(cx + 0.04f, cy - 0.04f))
    drawCircle(Color.White, size.width * 0.009f, o(cx + 0.05f, cy - 0.05f))
    if (p.gesto == Gesto.ABANANDO || sentado) {
        drawOval(Color(0xFFFF7A90), o(cx + 0.10f, cy + 0.08f), t(0.05f, 0.07f))
    }
    val giroOrelha = if (p.gesto == Gesto.ANDANDO) 8f * balanco(p, 0f) else 0f
    rotate(18f + giroOrelha, o(cx - 0.06f, cy - 0.10f)) {
        drawOval(c.escuro, o(cx - 0.11f, cy - 0.11f), t(0.09f, 0.20f))
    }
    drawCircle(Color(0x22000000), size.width * 0.15f, o(cx, cy), style = contorno)
}

private fun DrawScope.desenharGato(p: Passo, c: PeloDoBicho) {
    val sentado = p.gesto == Gesto.SENTADO
    val andar = if (p.gesto == Gesto.ANDANDO) 20f else 0f
    drawOval(Color(0x33000000), o(0.20f, 0.93f), t(0.62f, 0.07f))
    // Rabo comprido em pé, com a pontinha que mexe.
    val mexe = when (p.gesto) {
        Gesto.ABANANDO -> 18f * balanco(p, 0f)
        Gesto.ANDANDO -> 8f * balanco(p, 0.25f)
        else -> 4f
    }
    val base = if (sentado) o(0.28f, 0.84f) else o(0.26f, 0.58f)
    rotate(mexe, base) {
        val r = Path().apply {
            moveTo(base.x, base.y)
            cubicTo(
                base.x - size.width * 0.16f, base.y - size.height * 0.02f,
                base.x - size.width * 0.04f, base.y - size.height * 0.30f,
                base.x - size.width * 0.14f, base.y - size.height * 0.40f,
            )
        }
        drawPath(r, c.pelo, style = Stroke(width = size.width * 0.05f, cap = StrokeCap.Round))
    }
    if (sentado) {
        drawOval(c.pelo, o(0.20f, 0.66f), t(0.32f, 0.27f))
        drawOval(c.escuro, o(0.24f, 0.70f), t(0.08f, 0.04f))
        drawOval(c.escuro, o(0.26f, 0.77f), t(0.08f, 0.04f))
        perna(0.56f, 0.60f, 0.35f, 0f, c.pelo, c.claro)
        perna(0.48f, 0.60f, 0.35f, 0f, c.pelo, c.claro)
        rotate(-40f, o(0.32f, 0.82f)) {
            drawOval(c.pelo, o(0.24f, 0.55f), t(0.44f, 0.22f))
            drawOval(c.claro, o(0.40f, 0.65f), t(0.24f, 0.11f))
        }
    } else {
        perna(0.32f, 0.66f, 0.27f, andar * balanco(p, 0f), c.escuro, c.claro)
        perna(0.64f, 0.66f, 0.27f, andar * balanco(p, 0.5f), c.escuro, c.claro)
        drawOval(c.pelo, o(0.22f, 0.52f), t(0.52f, 0.22f))
        drawOval(c.claro, o(0.36f, 0.63f), t(0.30f, 0.10f))
        // Listrinhas.
        for (i in 0 until 3) drawOval(c.escuro, o(0.32f + i * 0.09f, 0.52f), t(0.04f, 0.08f))
        perna(0.38f, 0.66f, 0.27f, andar * balanco(p, 0.5f), c.pelo, c.claro)
        perna(0.70f, 0.66f, 0.27f, andar * balanco(p, 0f), c.pelo, c.claro)
    }
    val cy = if (sentado) 0.32f else 0.42f
    val cx = if (sentado) 0.62f else 0.76f
    drawRoundRect(c.coleira, o(cx - 0.09f, cy + 0.08f), t(0.11f, 0.05f), androidx.compose.ui.geometry.CornerRadius(size.width * 0.025f))
    // Orelhas pontudas.
    for (dx in listOf(-0.10f, 0.04f)) {
        val a = o(cx + dx, cy - 0.06f)
        val b = o(cx + dx + 0.03f, cy - 0.21f)
        val c2 = o(cx + dx + 0.09f, cy - 0.08f)
        val orelha = Path().apply {
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c2.x, c2.y)
            close()
        }
        drawPath(orelha, c.pelo)
    }
    drawCircle(c.pelo, size.width * 0.13f, o(cx, cy))
    drawOval(c.claro, o(cx + 0.02f, cy + 0.0f), t(0.13f, 0.09f))
    drawCircle(Color(0xFFFF8FA3), size.width * 0.018f, o(cx + 0.13f, cy + 0.01f))
    // Olho: fechadinho quando está sentado, de bem com a vida.
    if (sentado) {
        drawArc(Color(0xFF2B1B12), 0f, 180f, false, o(cx + 0.01f, cy - 0.06f), t(0.06f, 0.04f), style = Stroke(width = size.width * 0.014f))
    } else {
        drawOval(Color(0xFF2E7D32), o(cx + 0.01f, cy - 0.07f), t(0.045f, 0.06f))
        drawOval(Color(0xFF1B1B1B), o(cx + 0.025f, cy - 0.065f), t(0.018f, 0.05f))
    }
    // Bigodes.
    for (k in -1..1) {
        drawLine(Color(0x99FFFFFF), o(cx + 0.12f, cy + 0.03f), o(cx + 0.24f, cy + 0.03f + k * 0.03f), strokeWidth = size.width * 0.008f)
    }
}

/**
 * Visitante (da loja ou da semana) passeando no jardim: anda, para, senta e vira pelo [Passeio], sempre em
 * volta do seu cantinho. O emoji de rosto vira o bicho de corpo inteiro quando a fonte tem
 * ([Passeio.corpoInteiro]). Tocar faz ele pular de alegria.
 *
 * [pePx] é a altura do pé na tela e [tamanho] a largura do bicho, em pixels.
 */
@Composable
internal fun VisitanteAndando(
    emoji: String,
    indice: Int,
    relogio: State<Float>,
    larguraPx: Float,
    pePx: Float,
    tamanho: Float,
    onTocar: (String) -> Unit,
) {
    val densidade = LocalDensity.current
    val sons = LocalSons.current
    val cantinho = 0.2f + 0.6f * ((indice * 0.37f + 0.1f) % 1f)
    val alcance = Passeio.alcance(cantinho, 0.3f, 0.06f, 0.94f)
    val semente = indice * 7 + emoji.hashCode()
    val salto = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()
    val corpo = remember(emoji) { Passeio.corpoInteiro(emoji) }
    // O bicho em 3D quando há figura dele (as das perguntas); sem ela, o emoji de corpo inteiro.
    val contexto = LocalContext.current
    val figura = remember(corpo) { FigurasDaPergunta.idPara(ArteDoApp.manifesto(contexto), corpo) ?: FigurasDaPergunta.idPara(ArteDoApp.manifesto(contexto), emoji) }
    val toque = maxOf(tamanho, with(densidade) { 48.dp.toPx() })
    Box(
        Modifier
            .offset {
                val p = Passeio.em(relogio.value.toLong(), semente)
                val x = (cantinho + p.deslocamento * alcance) * larguraPx
                IntOffset((x - toque / 2).roundToInt(), (pePx - toque + salto.value).roundToInt())
            }
            .size(with(densidade) { toque.toDp() })
            .pointerInput(emoji) {
                detectTapGestures {
                    sons.piu(indice)
                    onTocar(emoji)
                    escopo.launch {
                        salto.animateTo(-tamanho * 0.6f, tween(170))
                        salto.animateTo(0f, spring(dampingRatio = 0.35f))
                    }
                }
            }
            .graphicsLayer {
                val p = Passeio.em(relogio.value.toLong(), semente)
                transformOrigin = TransformOrigin(0.5f, 1f)
                // Os emojis de bicho olham para a esquerda: virar quando anda para a direita (a figura 3D não
                // vira, para a luz não trocar de lado).
                scaleX = if (p.paraDireita && figura == null) -1f else 1f
                when (p.gesto) {
                    Gesto.ANDANDO -> translationY = -abs(sin(p.passada * 2f * PI.toFloat())) * tamanho * 0.1f
                    Gesto.SENTADO -> scaleY = 0.9f
                    Gesto.ABANANDO -> rotationZ = 8f * sin(p.passada * 4f * PI.toFloat())
                    Gesto.PARADO -> Unit
                }
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(Modifier.size(with(densidade) { tamanho.toDp() })) {
            if (figura == null) {
                EmojiGrande(corpo, tamanho)
            } else {
                ImagemDeArte(figura, Modifier.fillMaxSize(), alinhamento = Alignment.BottomCenter) { EmojiGrande(corpo, tamanho) }
            }
        }
    }
}
