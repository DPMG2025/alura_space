package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

/** Proporção do desenho antigo do irmãozinho (feito com formas), usado como reserva. */
const val PROPORCAO_DESENHO_IRMAO = 0.74f

/**
 * O irmãozinho (ou a irmãzinha) da criança: pequeno, cabeçudo, de jardineira, com a mesma pele e o mesmo
 * cabelo da criança do [perfil] (os dois são da mesma família). O gênero e o nome vêm de `perfil.irmao`.
 * Monta as camadas 3D (`assets/arte/irmao_*`); se faltar alguma, usa o desenho antigo.
 * [humor] usa os mesmos rostos da criança; [acenando] faz um bracinho dar tchau. Com [animado] falso, a figura
 * é composta uma vez e só copiada (listas e miniaturas).
 */
@Composable
fun Irmaozinho(
    perfil: Perfil,
    modifier: Modifier = Modifier,
    humor: Humor = Humor.FELIZ,
    acenando: Boolean = false,
    animado: Boolean = true,
) {
    val contexto = LocalContext.current
    val emCamadas = remember(perfil) { ComposicaoDosPersonagens.podeUsar(contexto, PersonagemDaArte.IRMAO, perfil) }
    BoxWithConstraints(
        modifier
            .aspectRatio(PROPORCAO_IRMAO)
            .semantics { contentDescription = perfil.irmao.nomeDeFrase },
    ) {
        val vivo = animado && animacoesLigadas()
        if (emCamadas) {
            PersonagemEmCamadas(
                personagem = PersonagemDaArte.IRMAO,
                perfil = perfil,
                humor = humor,
                acenando = acenando,
                falando = false,
                vivo = vivo,
                largura = maxWidth,
                altura = maxHeight,
                chapeu = "",
                // Pisca num ritmo diferente da criança: os dois nunca piscam juntos.
                ritmoDoPiscar = 3100,
                // Bebê balança de um lado para o outro (e não só respira), bem de leve.
                ginga = 2.5f,
                reserva = { q -> desenharIrmaozinho(perfil, null, q.olhosFechados, q.humor) },
            )
        } else if (vivo) {
            IrmaoAnimado(perfil, humor, acenando)
        } else {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawBehind { desenharIrmaozinho(perfil, null, olhosFechados = false, humor = humor) }
                    },
            )
        }
    }
}

@Composable
private fun IrmaoAnimado(perfil: Perfil, humor: Humor, acenando: Boolean) {
    val transicao = rememberInfiniteTransition(label = "irmao")
    val balanco = if (acenando) {
        transicao.animateFloat(-1f, 1f, infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse), label = "balanco")
    } else {
        null
    }
    // Pisca num ritmo diferente da criança: os dois nunca piscam juntos.
    val piscar = transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(3100, easing = LinearEasing)), label = "piscar")
    val olhosFechados = remember(piscar) { derivedStateOf { piscar.value > 0.95f } }
    // Bebê balança de um lado para o outro (e não só respira), bem de leve.
    val ginga = transicao.animateFloat(-1f, 1f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "ginga")
    Canvas(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = ginga.value * 2.5f
                scaleY = 1f + (ginga.value + 1f) * 0.012f
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
    ) {
        desenharIrmaozinho(perfil, balanco?.value, olhosFechados.value, humor)
    }
}

/** Contorno ameixa, o mesmo da criança. */
private val ContornoDoIrmao = Color(0xE6533A4E)
private val TintaDoContornoDoIrmao = Paint().apply { colorFilter = ColorFilter.tint(ContornoDoIrmao, BlendMode.SrcIn) }
private const val FOLGA_IRMAO = 0.12f

private data class ChaveDoIrmao(val perfil: Perfil, val w: Int, val h: Int, val aceno: Int?, val olhos: Boolean, val humor: Humor)

/** As últimas poses desenhadas do irmão (cada uma é uma imagem pequena). */
private object PosesDoIrmao : LinkedHashMap<ChaveDoIrmao, ImageBitmap>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ChaveDoIrmao, ImageBitmap>?): Boolean = size > 10
}

/**
 * O irmão inteiro: sombra, contorno em volta da silhueta (o mesmo jeito da criança: o desenho repetido em 8
 * direções numa camada de uma cor só, feita na CPU e guardada por pose) e o desenho colorido por cima.
 */
fun DrawScope.desenharIrmaozinho(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    noQuadroDoDesenho(PROPORCAO_DESENHO_IRMAO) { desenharIrmaozinhoNoQuadro(perfil, aceno, olhosFechados, humor) }
}

private fun DrawScope.desenharIrmaozinhoNoQuadro(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    val w = size.width
    val h = size.height
    if (w < 1f || h < 1f) return
    drawOval(Color(0x26000000), topLeft = Offset(w * 0.12f, h * 0.965f), size = Size(w * 0.76f, h * 0.05f))
    val traco = (w * 0.024f).coerceAtLeast(1f)
    val f = ceil(w * FOLGA_IRMAO + traco)
    val acenoEmPassos = aceno?.let { (it.coerceIn(-1f, 1f) * 4f).roundToInt() }
    val chave = ChaveDoIrmao(perfil, ceil(w).toInt(), ceil(h).toInt(), acenoEmPassos, olhosFechados, humor)
    val imagem = PosesDoIrmao[chave] ?: ImageBitmap(ceil(w + f * 2).toInt(), ceil(h + f * 2).toInt()).also { bmp ->
        CanvasDrawScope().draw(this, layoutDirection, ComposeCanvas(bmp), Size(w, h)) {
            translate(f, f) {
                drawIntoCanvas { it.saveLayer(Rect(-f, -f, w + f, h + f), TintaDoContornoDoIrmao) }
                for (k in 0 until 8) {
                    val a = k * PI.toFloat() / 4f
                    translate(cos(a) * traco, sin(a) * traco) { desenharFiguraDoIrmao(perfil, acenoEmPassos?.let { it / 4f }, olhosFechados, humor) }
                }
                drawIntoCanvas { it.restore() }
                desenharFiguraDoIrmao(perfil, acenoEmPassos?.let { it / 4f }, olhosFechados, humor)
            }
        }
        PosesDoIrmao[chave] = bmp
    }
    drawImage(imagem, topLeft = Offset(-f, -f))
}

private fun DrawScope.desenharFiguraDoIrmao(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    val w = size.width
    val h = size.height
    fun p(u: Float, v: Float) = Offset(u * w, v * h)
    val pele = Color(Aparencia.peles.getOrElse(perfil.pele) { Aparencia.peles[0] })
    val cabelo = Color(Aparencia.cabelos.getOrElse(perfil.cabelo) { Aparencia.cabelos[0] })
    val menina = perfil.irmao.genero == Genero.MENINA
    // Cores da jardineira: o irmãozinho de amarelo e verde-água, a irmãzinha de lilás e coral.
    val blusa = if (menina) Color(0xFFC9A7FF) else Color(0xFFFFD23F)
    val jardineira = if (menina) Color(0xFFFF7A8A) else Color(0xFF2EC4B6)
    val jardineiraEscura = lerp(jardineira, Color.Black, 0.18f)
    val sapato = if (menina) Color(0xFFFF4F8B) else Color(0xFFE63946)

    // perninhas e pezinhos
    for (u in listOf(0.4f, 0.6f)) {
        drawLine(pele, p(u, 0.8f), p(u, 0.92f), strokeWidth = w * 0.15f, cap = StrokeCap.Round)
    }
    for (u in listOf(0.37f, 0.63f)) {
        val c = p(u, 0.95f)
        drawOval(sapato, Offset(c.x - w * 0.12f, c.y - h * 0.035f), Size(w * 0.24f, h * 0.07f))
        drawOval(Color(0x59FFFFFF), Offset(c.x - w * 0.07f, c.y - h * 0.028f), Size(w * 0.09f, h * 0.022f))
    }

    // bracinhos: parado, torcendo (os dois para cima), pensando (dedinho na boca) ou dando tchau
    val ombroE = p(0.3f, 0.66f)
    val ombroD = p(0.7f, 0.66f)
    val maoE = when (humor) {
        Humor.TORCENDO -> p(0.1f, 0.47f)
        else -> p(0.15f, 0.82f)
    }
    val maoD = when {
        humor == Humor.TORCENDO -> p(0.9f, 0.47f)
        humor == Humor.PENSANDO -> p(0.6f, 0.6f)
        aceno != null -> {
            val ang = (-70f + aceno * 16f) * PI.toFloat() / 180f
            Offset(ombroD.x + cos(ang) * w * 0.27f, ombroD.y + sin(ang) * w * 0.27f)
        }
        else -> p(0.85f, 0.82f)
    }
    drawLine(pele, ombroE, maoE, strokeWidth = w * 0.12f, cap = StrokeCap.Round)
    drawLine(pele, ombroD, maoD, strokeWidth = w * 0.12f, cap = StrokeCap.Round)
    drawCircle(pele, w * 0.075f, maoE)
    drawCircle(pele, w * 0.075f, maoD)

    // corpo: blusa redondinha e a jardineira por cima
    val corpo = Path().apply { addRoundRect(RoundRect(w * 0.24f, h * 0.6f, w * 0.76f, h * 0.88f, CornerRadius(w * 0.16f))) }
    drawPath(corpo, blusa)
    clipPath(corpo) {
        drawRoundRect(jardineira, p(0.33f, 0.68f), Size(w * 0.34f, h * 0.2f), CornerRadius(w * 0.06f))
        drawRect(jardineira, p(0.2f, 0.78f), Size(w * 0.6f, h * 0.12f))
        // alças com botõezinhos
        for (lado in listOf(-1f, 1f)) {
            drawLine(jardineira, Offset(w * (0.5f + lado * 0.2f), h * 0.6f), Offset(w * (0.5f + lado * 0.14f), h * 0.7f), strokeWidth = w * 0.07f)
            drawCircle(Color(0xFFFFF3B0), w * 0.028f, Offset(w * (0.5f + lado * 0.13f), h * 0.705f))
        }
        // bolsinho com um desenho: coração (irmãzinha) ou estrela (irmãozinho)
        val bolso = p(0.5f, 0.775f)
        if (menina) {
            drawPath(caminhoCoracao(bolso, w * 0.075f), Color(0xFFFFF0F3))
        } else {
            drawPath(caminhoEstrela(bolso, w * 0.075f), Color(0xFFFFF3B0))
        }
        drawLine(jardineiraEscura, p(0.5f, 0.86f), p(0.5f, 0.9f), strokeWidth = w * 0.02f)
        // volume: luz à esquerda, sombra à direita e embaixo do queixo
        drawRect(
            Brush.horizontalGradient(
                0f to Color(0x2EFFFFFF), 0.35f to Color(0x00FFFFFF), 0.65f to Color(0x00000000), 1f to Color(0x2B000000),
                startX = w * 0.24f, endX = w * 0.76f,
            ),
        )
        drawOval(Color(0x26000000), p(0.32f, 0.585f), Size(w * 0.36f, h * 0.05f))
    }
    // manguinhas
    val manga = w * 0.14f
    for ((ombro, mao) in listOf(ombroE to maoE, ombroD to maoD)) {
        drawLine(blusa, ombro, Offset(ombro.x + (mao.x - ombro.x) * 0.3f, ombro.y + (mao.y - ombro.y) * 0.3f), strokeWidth = manga, cap = StrokeCap.Round)
    }

    // cabeça (grandona, de criança pequena)
    val centro = p(0.5f, 0.36f)
    val r = w * 0.36f
    if (menina) {
        // Rabinho no alto da cabeça (palmeirinha) com laço.
        val topo = Offset(centro.x + r * 0.1f, centro.y - r * 1.0f)
        drawOval(cabelo, Offset(topo.x - r * 0.3f, topo.y - r * 0.42f), Size(r * 0.6f, r * 0.5f))
        drawOval(cabelo, Offset(topo.x - r * 0.05f, topo.y - r * 0.5f), Size(r * 0.42f, r * 0.36f))
    }
    for (lado in listOf(-1f, 1f)) drawCircle(pele, r * 0.2f, Offset(centro.x + lado * r * 0.98f, centro.y + r * 0.12f))
    drawCircle(pele, r, centro)
    sombrearCabeca(centro, r)
    // Cabelinho ralo de criança pequena: só a calota com mechas, sem volume dos lados.
    val franja = franjaComMechas(centro, r, curta = true)
    clipPath(Path().apply { addOval(Rect(centro, r)) }) {
        translate(0f, r * 0.06f) { drawPath(franja, Color(0x1F5B2A22)) }
    }
    drawPath(franja, cabelo)
    drawArc(
        Color(0x47FFFFFF), startAngle = 218f, sweepAngle = 40f, useCenter = false,
        topLeft = Offset(centro.x - r * 0.86f, centro.y - r * 0.86f), size = Size(r * 1.72f, r * 1.72f),
        style = Stroke(r * 0.09f, cap = StrokeCap.Round),
    )
    if (menina) {
        val no = Offset(centro.x + r * 0.08f, centro.y - r * 0.98f)
        val laco = Color(0xFFFF4F8B)
        drawPath(Path().apply { moveTo(no.x, no.y); lineTo(no.x - r * 0.3f, no.y - r * 0.2f); lineTo(no.x - r * 0.26f, no.y + r * 0.2f); close() }, laco)
        drawPath(Path().apply { moveTo(no.x, no.y); lineTo(no.x + r * 0.3f, no.y - r * 0.2f); lineTo(no.x + r * 0.26f, no.y + r * 0.2f); close() }, laco)
        drawCircle(lerp(laco, Color(0xFFE0A800), 0.6f), r * 0.09f, no)
    } else {
        // Cachinho em cima da cabeça.
        val base = Offset(centro.x + r * 0.05f, centro.y - r * 0.95f)
        val cacho = Path().apply {
            moveTo(base.x - r * 0.12f, base.y + r * 0.05f)
            cubicTo(base.x - r * 0.2f, base.y - r * 0.4f, base.x + r * 0.35f, base.y - r * 0.45f, base.x + r * 0.28f, base.y - r * 0.18f)
            cubicTo(base.x + r * 0.22f, base.y - r * 0.02f, base.x + r * 0.05f, base.y - r * 0.08f, base.x + r * 0.1f, base.y - r * 0.2f)
            cubicTo(base.x + r * 0.0f, base.y - r * 0.15f, base.x + r * 0.02f, base.y + r * 0.02f, base.x + r * 0.12f, base.y + r * 0.05f)
            close()
        }
        drawPath(cacho, cabelo)
    }
    desenharBochechas(centro, r * 1.1f, humor)
    desenharNariz(centro, r, pele)
    desenharOlhos(
        centro, r, olhosFechados, humor,
        cilios = menina,
        corDaSobrancelha = lerp(cabelo, Color(0xFF2B1A14), 0.2f).copy(alpha = 0.7f),
    )
    desenharBoca(centro, r, humor)
}

/** Coraçãozinho com o centro em [c] e meia largura [raio]. */
fun caminhoCoracao(c: Offset, raio: Float): Path = Path().apply {
    moveTo(c.x, c.y + raio * 0.9f)
    cubicTo(c.x - raio * 1.4f, c.y - raio * 0.1f, c.x - raio * 0.7f, c.y - raio * 1.1f, c.x, c.y - raio * 0.4f)
    cubicTo(c.x + raio * 0.7f, c.y - raio * 1.1f, c.x + raio * 1.4f, c.y - raio * 0.1f, c.x, c.y + raio * 0.9f)
    close()
}
