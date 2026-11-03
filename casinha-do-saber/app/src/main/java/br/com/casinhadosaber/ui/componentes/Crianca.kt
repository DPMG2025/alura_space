package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Proporção largura/altura do desenho da criança. */
const val PROPORCAO_CRIANCA = 0.62f

enum class Humor { FELIZ, PENSANDO, SURPRESO, TORCENDO }

/**
 * Desenho da criança (menino ou menina), feito com formas simples para ficar leve
 * e igual em qualquer aparelho. [acenando] faz um braço dar tchau.
 */
@Composable
fun Crianca(
    perfil: Perfil,
    modifier: Modifier = Modifier,
    acenando: Boolean = false,
    humor: Humor = Humor.FELIZ,
    /** Mexe a boca, como se estivesse falando (junto com o narrador). */
    falando: Boolean = false,
) {
    val transicao = rememberInfiniteTransition(label = "crianca")
    val balanco by transicao.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "balanco",
    )
    val piscar by transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing)),
        label = "piscar",
    )
    val respiro by transicao.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "respiro",
    )
    val boca by transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(160), RepeatMode.Reverse),
        label = "boca",
    )
    val descricao = if (perfil.genero == Genero.MENINA) "Menina" else "Menino"
    BoxWithConstraints(
        modifier
            .aspectRatio(PROPORCAO_CRIANCA)
            .semantics { contentDescription = perfil.nome.ifBlank { descricao } },
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Respiração: o corpo sobe e desce bem de leve, a partir dos pés.
                    scaleY = respiro
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                },
        ) {
            desenharCrianca(
                perfil,
                if (acenando) balanco else null,
                olhosFechados = piscar > 0.96f,
                humor = if (falando && boca > 0.5f) Humor.SURPRESO else humor,
            )
        }
        // Chapéu comprado na loja, em cima da cabeça.
        if (perfil.chapeu.isNotEmpty()) {
            // Cabeça: centro em 27% da altura, raio = 34% da largura. O chapéu tem ~90% da largura da cabeça
            // e a aba fica um pouco acima dos olhos.
            val raio = maxWidth * 0.34f
            val largura = raio * 2f * 0.9f
            val fonte = largura * 0.82f
            val baseDoChapeu = maxHeight * 0.27f - raio * 0.45f
            val densidade = LocalDensity.current
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = baseDoChapeu - fonte * 1.05f)
                    .size(largura, fonte * 1.1f),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    perfil.chapeu,
                    fontSize = with(densidade) { fonte.toSp() },
                    lineHeight = with(densidade) { (fonte * 1.05f).toSp() },
                )
            }
        }
    }
}

fun DrawScope.desenharCrianca(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    val w = size.width
    val h = size.height
    fun p(u: Float, v: Float) = Offset(u * w, v * h)

    val pele = Color(Aparencia.peles.getOrElse(perfil.pele) { Aparencia.peles[0] })
    val cabelo = Color(Aparencia.cabelos.getOrElse(perfil.cabelo) { Aparencia.cabelos[0] })
    val menina = perfil.genero == Genero.MENINA
    val roupa = Color(perfil.corDaRoupa)
    val calca = if (menina) pele else Color(0xFF2E4A7D)
    val sapato = Color(0xFF5A3E36)

    // sombra no chão
    drawOval(Color(0x26000000), topLeft = Offset(w * 0.08f, h * 0.975f), size = Size(w * 0.84f, h * 0.05f))

    // pernas
    for (u in listOf(0.37f, 0.63f)) {
        drawLine(calca, p(u, 0.72f), p(u, 0.93f), strokeWidth = w * 0.17f, cap = StrokeCap.Round)
        if (menina) drawLine(Color.White, p(u, 0.86f), p(u, 0.93f), strokeWidth = w * 0.17f, cap = StrokeCap.Round)
    }
    for (u in listOf(0.33f, 0.67f)) {
        val c = p(u, 0.96f)
        drawOval(sapato, topLeft = Offset(c.x - w * 0.13f, c.y - h * 0.035f), size = Size(w * 0.26f, h * 0.07f))
    }

    // braços (o direito pode acenar)
    val ombroE = p(0.2f, 0.5f)
    val torcendo = humor == Humor.TORCENDO
    val pensando = humor == Humor.PENSANDO
    // Torcendo: os dois punhos para cima. Pensando: a mão no queixo.
    val maoE = if (torcendo) p(0.0f, 0.3f) else p(0.06f, 0.7f)
    val ombroD = p(0.8f, 0.5f)
    val maoD = if (torcendo) {
        p(1.0f, 0.3f)
    } else if (pensando) {
        p(0.62f, 0.42f)
    } else if (aceno != null) {
        val ang = (-60f + aceno * 18f) * PI.toFloat() / 180f
        val comp = h * 0.24f
        Offset(ombroD.x + cos(ang) * comp, ombroD.y + sin(ang) * comp)
    } else {
        p(0.94f, 0.7f)
    }
    drawLine(pele, ombroE, maoE, strokeWidth = w * 0.13f, cap = StrokeCap.Round)
    drawLine(pele, ombroD, maoD, strokeWidth = w * 0.13f, cap = StrokeCap.Round)
    drawCircle(pele, w * 0.085f, maoE)
    drawCircle(pele, w * 0.085f, maoD)

    // corpo
    if (menina) {
        val vestido = Path().apply {
            moveTo(w * 0.26f, h * 0.46f); lineTo(w * 0.74f, h * 0.46f)
            lineTo(w * 0.9f, h * 0.8f); lineTo(w * 0.1f, h * 0.8f); close()
        }
        drawPath(vestido, roupa)
        for (u in listOf(0.3f, 0.5f, 0.7f)) drawCircle(Color(0x80FFFFFF), w * 0.04f, p(u, 0.68f))
    } else {
        drawRoundRect(roupa, topLeft = p(0.2f, 0.45f), size = Size(w * 0.6f, h * 0.3f), cornerRadius = CornerRadius(w * 0.14f))
        drawRect(Color(0xFF2E4A7D), topLeft = p(0.2f, 0.7f), size = Size(w * 0.6f, h * 0.06f))
        drawPath(caminhoEstrela(p(0.5f, 0.58f), w * 0.1f), Color(0xFFFFD166))
    }

    // cabeça
    val centro = p(0.5f, 0.27f)
    val r = w * 0.34f
    if (menina) for (lado in listOf(-1f, 1f)) drawCircle(cabelo, r * 0.36f, Offset(centro.x + lado * r * 1.05f, centro.y + r * 0.05f))
    for (lado in listOf(-1f, 1f)) drawCircle(pele, r * 0.2f, Offset(centro.x + lado * r * 0.98f, centro.y + r * 0.1f))
    drawCircle(pele, r, centro)

    // cabelo
    val franja = Path().apply {
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(centro, r * 1.04f),
            startAngleDegrees = 183.6f, sweepAngleDegrees = 172.8f, forceMoveTo = true,
        )
        quadraticTo(centro.x + r * 0.4f, centro.y - r * 0.55f, centro.x - r * 0.1f, centro.y - r * 0.45f)
        quadraticTo(centro.x - r * 0.6f, centro.y - r * 0.35f, centro.x - r * 1.03f, centro.y - r * 0.05f)
        close()
    }
    drawPath(franja, cabelo)
    if (menina && perfil.chapeu.isEmpty()) {
        val laco = Color(0xFFFF4F8B)
        val n = Offset(centro.x + r * 0.45f, centro.y - r * 0.85f)
        drawPath(Path().apply { moveTo(n.x, n.y); lineTo(n.x - r * 0.3f, n.y - r * 0.23f); lineTo(n.x - r * 0.25f, n.y + r * 0.23f); close() }, laco)
        drawPath(Path().apply { moveTo(n.x, n.y); lineTo(n.x + r * 0.33f, n.y - r * 0.15f); lineTo(n.x + r * 0.23f, n.y + r * 0.25f); close() }, laco)
        drawCircle(laco, r * 0.1f, n)
    } else {
        val topete = Path().apply {
            moveTo(centro.x - r * 0.1f, centro.y - r * 0.95f)
            quadraticTo(centro.x + r * 0.05f, centro.y - r * 1.35f, centro.x + r * 0.3f, centro.y - r * 1.2f)
            quadraticTo(centro.x + r * 0.1f, centro.y - r * 1.05f, centro.x + r * 0.15f, centro.y - r * 0.9f)
            close()
        }
        drawPath(topete, cabelo)
    }

    // rosto
    val olho = Color(0xFF2B2B2B)
    for (lado in listOf(-1f, 1f)) {
        val c = Offset(centro.x + lado * r * 0.36f, centro.y + r * 0.12f)
        if (olhosFechados) {
            drawLine(olho, Offset(c.x - r * 0.1f, c.y), Offset(c.x + r * 0.1f, c.y), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
        } else {
            drawOval(olho, topLeft = Offset(c.x - r * 0.11f, c.y - r * 0.14f), size = Size(r * 0.22f, r * 0.28f))
            drawCircle(Color.White, r * 0.04f, Offset(c.x + r * 0.04f, c.y - r * 0.06f))
        }
        drawOval(
            Color(0x73FF6E82),
            topLeft = Offset(centro.x + lado * r * 0.62f - r * 0.14f, centro.y + r * 0.33f),
            size = Size(r * 0.28f, r * 0.18f),
        )
    }
    val boca = Color(0xFF7A3B2E)
    val bocaCentro = Offset(centro.x, centro.y + r * 0.38f)
    when (humor) {
        Humor.FELIZ, Humor.TORCENDO -> drawArc(
            boca, startAngle = 27f, sweepAngle = 126f, useCenter = false,
            topLeft = Offset(bocaCentro.x - r * 0.22f, bocaCentro.y - r * 0.22f), size = Size(r * 0.44f, r * 0.44f),
            style = Stroke(width = r * 0.08f, cap = StrokeCap.Round),
        )
        Humor.SURPRESO -> drawOval(boca, topLeft = Offset(bocaCentro.x - r * 0.09f, bocaCentro.y), size = Size(r * 0.18f, r * 0.22f))
        Humor.PENSANDO -> drawLine(
            boca, Offset(bocaCentro.x - r * 0.12f, bocaCentro.y + r * 0.12f), Offset(bocaCentro.x + r * 0.12f, bocaCentro.y + r * 0.06f),
            strokeWidth = r * 0.08f, cap = StrokeCap.Round,
        )
    }
}

fun caminhoEstrela(centro: Offset, raio: Float, pontas: Int = 5): Path = Path().apply {
    for (i in 0 until pontas * 2) {
        val ang = -PI / 2 + i * PI / pontas
        val rr = if (i % 2 == 0) raio else raio * 0.45f
        val x = centro.x + cos(ang).toFloat() * rr
        val y = centro.y + sin(ang).toFloat() * rr
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
