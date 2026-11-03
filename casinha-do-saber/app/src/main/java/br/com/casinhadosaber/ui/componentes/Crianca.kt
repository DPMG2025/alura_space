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
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Perfil
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.drawscope.clipPath
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
    // Roupa especial do guarda-roupa (comprada na loja), desenhada por cima da roupa de sempre.
    val traje = Loja.trajeDe(perfil)?.id
    val roupa = when (traje) {
        "pijama" -> Color(0xFF3B4C8C)
        "abelha" -> Color(0xFFFFC93C)
        "astronauta" -> Color(0xFFF2F4F8)
        else -> Color(perfil.corDaRoupa)
    }
    val calca = when (traje) {
        "pijama" -> Color(0xFF3B4C8C)
        "astronauta" -> Color(0xFFDCE1EA)
        "abelha" -> if (menina) pele else Color(0xFF2B2B2B)
        else -> if (menina) pele else Color(0xFF2E4A7D)
    }
    val sapato = if (traje == "astronauta") Color(0xFF9AA3B5) else Color(0xFF5A3E36)

    // sombra no chão
    drawOval(Color(0x26000000), topLeft = Offset(w * 0.08f, h * 0.975f), size = Size(w * 0.84f, h * 0.05f))

    // Atrás do corpo: capa de herói ou asinhas de fada.
    if (traje == "heroi") {
        val capa = Path().apply {
            moveTo(w * 0.24f, h * 0.47f); lineTo(w * 0.76f, h * 0.47f)
            lineTo(w * 0.96f, h * 0.9f); quadraticTo(w * 0.5f, h * 0.96f, w * 0.04f, h * 0.9f); close()
        }
        drawPath(capa, Color(0xFFE63946))
        drawPath(capa, Color(0xFFB0222F), style = Stroke(w * 0.02f))
    } else if (traje == "fada") {
        for (lado in listOf(-1f, 1f)) {
            val base = p(0.5f + lado * 0.2f, 0.52f)
            drawOval(Color(0xB3D9C2FF), Offset(base.x + (if (lado < 0) -w * 0.36f else 0f), base.y - h * 0.16f), Size(w * 0.36f, h * 0.17f))
            drawOval(Color(0xB3FFC2E8), Offset(base.x + (if (lado < 0) -w * 0.26f else 0f), base.y - h * 0.01f), Size(w * 0.26f, h * 0.12f))
        }
    }

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
    val corpo: Path
    if (menina) {
        corpo = Path().apply {
            moveTo(w * 0.26f, h * 0.46f); lineTo(w * 0.74f, h * 0.46f)
            lineTo(w * 0.9f, h * 0.8f); lineTo(w * 0.1f, h * 0.8f); close()
        }
        drawPath(corpo, roupa)
        if (traje == null) for (u in listOf(0.3f, 0.5f, 0.7f)) drawCircle(Color(0x80FFFFFF), w * 0.04f, p(u, 0.68f))
    } else {
        corpo = Path().apply { addRoundRect(RoundRect(w * 0.2f, h * 0.45f, w * 0.8f, h * 0.75f, CornerRadius(w * 0.14f))) }
        drawPath(corpo, roupa)
        drawRect(calca, topLeft = p(0.2f, 0.7f), size = Size(w * 0.6f, h * 0.06f))
        if (traje == null) drawPath(caminhoEstrela(p(0.5f, 0.58f), w * 0.1f), Color(0xFFFFD166))
    }
    if (traje != null) desenharTrajeNoCorpo(traje, corpo)

    // cabeça
    val centro = p(0.5f, 0.27f)
    val r = w * 0.34f
    val estilo = perfil.estiloCabelo
    if (menina && estilo == 0) for (lado in listOf(-1f, 1f)) drawCircle(cabelo, r * 0.36f, Offset(centro.x + lado * r * 1.05f, centro.y + r * 0.05f))
    if (menina && estilo == 2) {
        // Tranças: correntinhas de bolinhas descendo dos lados da cabeça.
        for (lado in listOf(-1f, 1f)) for (k in 0 until 4) {
            val c = Offset(centro.x + lado * r * (0.95f + k * 0.04f), centro.y + r * (0.15f + k * 0.42f))
            drawOval(cabelo, Offset(c.x - r * 0.2f, c.y - r * 0.24f), Size(r * 0.4f, r * 0.48f))
        }
        for (lado in listOf(-1f, 1f)) drawCircle(Color(0xFFFF4F8B), r * 0.1f, Offset(centro.x + lado * r * 1.07f, centro.y + r * 1.62f))
    }
    if (estilo == 1) {
        // Cacheado: bolinhas em volta da cabeça (mais volume na menina).
        val volta = if (menina) 1.12f else 1.0f
        var ang = 160f
        while (ang <= 380f) {
            val rad = Math.toRadians(ang.toDouble())
            drawCircle(cabelo, r * (if (menina) 0.3f else 0.24f), Offset(centro.x + (kotlin.math.cos(rad) * r * volta).toFloat(), centro.y + (kotlin.math.sin(rad) * r * volta).toFloat()))
            ang += 22f
        }
    }
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
    if (menina && perfil.chapeu.isEmpty() && estilo != 2) {
        val laco = Color(0xFFFF4F8B)
        val n = Offset(centro.x + r * 0.45f, centro.y - r * 0.85f)
        drawPath(Path().apply { moveTo(n.x, n.y); lineTo(n.x - r * 0.3f, n.y - r * 0.23f); lineTo(n.x - r * 0.25f, n.y + r * 0.23f); close() }, laco)
        drawPath(Path().apply { moveTo(n.x, n.y); lineTo(n.x + r * 0.33f, n.y - r * 0.15f); lineTo(n.x + r * 0.23f, n.y + r * 0.25f); close() }, laco)
        drawCircle(laco, r * 0.1f, n)
    } else if (!menina && estilo == 2) {
        // Espetadinho: pontinhas em cima da cabeça.
        for (k in -2..2) {
            val x = centro.x + k * r * 0.32f
            val ponta = Path().apply {
                moveTo(x - r * 0.2f, centro.y - r * 0.82f)
                lineTo(x + r * 0.06f * k, centro.y - r * 1.3f)
                lineTo(x + r * 0.2f, centro.y - r * 0.82f)
                close()
            }
            drawPath(ponta, cabelo)
        }
    } else if (!menina && estilo == 0) {
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
    // Na cabeça: anteninhas de abelha ou o capacete de vidro do astronauta.
    if (traje == "abelha") {
        for (lado in listOf(-1f, 1f)) {
            val ponta = Offset(centro.x + lado * r * 0.7f, centro.y - r * 1.55f)
            drawLine(Color(0xFF2B2B2B), Offset(centro.x + lado * r * 0.3f, centro.y - r * 0.9f), ponta, strokeWidth = r * 0.07f, cap = StrokeCap.Round)
            drawCircle(Color(0xFF2B2B2B), r * 0.13f, ponta)
        }
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
    if (traje == "astronauta") {
        drawCircle(Color(0x338FD3FF), r * 1.32f, centro)
        drawCircle(Color(0xFFDCE1EA), r * 1.32f, centro, style = Stroke(r * 0.12f))
        drawArc(
            Color(0x99FFFFFF), startAngle = 200f, sweepAngle = 50f, useCenter = false,
            topLeft = Offset(centro.x - r * 1.05f, centro.y - r * 1.05f), size = Size(r * 2.1f, r * 2.1f),
            style = Stroke(r * 0.1f, cap = StrokeCap.Round),
        )
    }
}

/** Estampas e detalhes da roupa especial, recortados no formato do corpo. */
private fun DrawScope.desenharTrajeNoCorpo(traje: String, corpo: Path) {
    val w = size.width
    val h = size.height
    val topo = h * 0.45f
    val base = h * 0.8f
    clipPath(corpo) {
        when (traje) {
            "listras" -> {
                var y = topo + h * 0.03f
                while (y < base) {
                    drawRect(Color(0xCCFFFFFF), Offset(0f, y), Size(w, h * 0.035f))
                    y += h * 0.075f
                }
            }
            "pijama" -> for (i in 0 until 7) {
                val u = 0.22f + (i * 0.37f) % 0.58f
                val v = 0.5f + (i * 0.13f) % 0.26f
                drawPath(caminhoEstrela(Offset(w * u, h * v), w * 0.055f), Color(0xFFFFD166))
            }
            "abelha" -> {
                var y = topo + h * 0.05f
                while (y < base) {
                    drawRect(Color(0xFF2B2B2B), Offset(0f, y), Size(w, h * 0.05f))
                    y += h * 0.1f
                }
            }
            "heroi" -> {
                val c = Offset(w * 0.5f, h * 0.57f)
                drawCircle(Color(0xFFFFD166), w * 0.13f, c)
                val raio = Path().apply {
                    moveTo(c.x + w * 0.03f, c.y - w * 0.1f); lineTo(c.x - w * 0.06f, c.y + w * 0.01f)
                    lineTo(c.x + w * 0.01f, c.y + w * 0.01f); lineTo(c.x - w * 0.03f, c.y + w * 0.1f)
                    lineTo(c.x + w * 0.07f, c.y - w * 0.02f); lineTo(c.x, c.y - w * 0.02f); close()
                }
                drawPath(raio, Color(0xFFE63946))
            }
            "fada" -> for (i in 0 until 6) {
                val u = 0.25f + (i * 0.29f) % 0.5f
                val v = 0.52f + (i * 0.11f) % 0.24f
                drawPath(caminhoEstrela(Offset(w * u, h * v), w * 0.04f, 4), Color(0xE6FFFFFF))
            }
            "astronauta" -> {
                drawRect(Color(0xFF9AA3B5), Offset(0f, h * 0.66f), Size(w, h * 0.035f))
                drawRoundRect(Color(0xFF4D96FF), Offset(w * 0.56f, h * 0.51f), Size(w * 0.14f, h * 0.07f), CornerRadius(w * 0.02f))
                drawCircle(Color(0xFFEF476F), w * 0.025f, Offset(w * 0.38f, h * 0.53f))
                drawCircle(Color(0xFF3DBE6B), w * 0.025f, Offset(w * 0.38f, h * 0.59f))
            }
        }
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
