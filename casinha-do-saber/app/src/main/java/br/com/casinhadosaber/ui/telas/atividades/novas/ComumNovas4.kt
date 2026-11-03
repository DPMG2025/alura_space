package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.dominio.Idioma
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.componentes.SetaDesenhada
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ehTexto
import br.com.casinhadosaber.ui.telas.atividades.figurasDe
import br.com.casinhadosaber.ui.telas.atividades.nomeDaFigura
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/*
 * Peças comuns às telas de Siga a sequência, Ditado, Cartas viradas, Programar o robô e Montar a
 * frase (sufixo N4 para não se confundir com as ajudas das outras telas novas).
 */

/** O que o narrador diz de uma opção: a fala dela ou, sem fala, o nome do número, da letra ou da figura. */
internal fun falaDaOpcaoN4(o: Opcao): List<Trecho> {
    if (o.fala.isNotEmpty()) return o.fala
    return when (val v = o.visual) {
        is Visual.Texto -> listOf(falaDeTextoN4(v.texto, Idioma.PT))
        is Visual.Emoji -> {
            val figuras = figurasDe(v.emoji)
            if (figuras.size > 1) listOf(pt(Vocabulario.porExtenso(figuras.size)))
            else nomeDaFigura(v.emoji)?.let { listOf(pt(it)) } ?: emptyList()
        }
        is Visual.Sombra -> nomeDaFigura(v.emoji)?.let { listOf(pt(it)) } ?: emptyList()
        is Visual.Seta -> listOf(pt(nomeDaDirecaoN4(v.direcao)))
        is Visual.Cor -> emptyList()
    }
}

/** Fala de uma letra, sílaba, número ou palavra escrita. */
internal fun falaDeTextoN4(texto: String, idioma: Idioma): Trecho = when {
    texto.isNotEmpty() && texto.all { it.isDigit() } && texto.length <= 6 ->
        if (idioma == Idioma.EN) Trecho(texto, Idioma.EN) else pt(Vocabulario.porExtenso(texto.toInt()))
    texto.length == 1 && texto[0].isLetter() ->
        if (idioma == Idioma.EN) Trecho(texto.uppercase(), Idioma.EN) else pt(Vocabulario.falaDaLetra(texto))
    else -> Trecho(texto.lowercase(), idioma)
}

internal fun nomeDaDirecaoN4(d: br.com.casinhadosaber.dominio.DirecaoDaSeta): String = when (d) {
    br.com.casinhadosaber.dominio.DirecaoDaSeta.CIMA -> "para cima"
    br.com.casinhadosaber.dominio.DirecaoDaSeta.BAIXO -> "para baixo"
    br.com.casinhadosaber.dominio.DirecaoDaSeta.ESQUERDA -> "para a esquerda"
    br.com.casinhadosaber.dominio.DirecaoDaSeta.DIREITA -> "para a direita"
}

/** Texto para leitores de tela e para os testes. */
internal fun descricaoN4(o: Opcao): String = when (val v = o.visual) {
    is Visual.Texto -> v.texto
    else -> falaDaOpcaoN4(o).joinToString(" ") { it.texto }.ifBlank {
        when (v) {
            is Visual.Emoji -> v.emoji
            is Visual.Sombra -> "Sombra"
            is Visual.Cor -> "Cor"
            else -> "Figura"
        }
    }
}

/**
 * Espera o narrador terminar de falar (no máximo [limite] ms). Dá um instante para a fala começar,
 * porque o TTS demora um pouco a ligar o "falando".
 */
internal suspend fun esperarNarradorN4(narrador: Narrador, limite: Long = 6000L) {
    withTimeoutOrNull(500L) { snapshotFlow { narrador.falando }.first { it } }
    withTimeoutOrNull(limite) { snapshotFlow { narrador.falando }.first { !it } }
    delay(120L)
}

/** Desenha o visual de uma opção num quadrado de lado [lado] (figura, letra, número, cor, sombra ou seta). */
@Composable
internal fun VisualN4(v: Visual, lado: Dp, corTexto: Color = Cores.Texto, modifier: Modifier = Modifier) {
    Box(modifier.size(lado), contentAlignment = Alignment.Center) {
        when (v) {
            is Visual.Texto -> {
                val n = v.texto.length.coerceAtLeast(1)
                val fator = when (n) {
                    1 -> 0.62f
                    2 -> 0.48f
                    3 -> 0.38f
                    4 -> 0.3f
                    5 -> 0.25f
                    else -> 1.3f / n
                }
                Text(
                    v.texto,
                    fontSize = (lado.value * fator).sp,
                    lineHeight = (lado.value * fator * 1.1f).sp,
                    fontWeight = FontWeight.Bold,
                    color = corTexto,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = MaterialTheme.typography.displayLarge,
                )
            }
            is Visual.Emoji -> {
                val figuras = figurasDe(v.emoji)
                if (figuras.size <= 1) {
                    val t = lado.value * 0.6f * v.escala.coerceIn(0.4f, 1.3f)
                    Text(v.emoji, fontSize = t.sp, lineHeight = (t * 1.15f).sp)
                } else {
                    // Quantidade: grade como num dado.
                    val porLinha = if (figuras.size <= 4) 2 else 3
                    val celula = lado * 0.86f / porLinha
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        for (linha in figuras.chunked(porLinha)) {
                            Row {
                                for (f in linha) {
                                    Box(Modifier.size(celula), contentAlignment = Alignment.Center) {
                                        Text(f, fontSize = (celula.value * 0.7f).sp, lineHeight = (celula.value * 0.84f).sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is Visual.Sombra -> Text(
                v.emoji,
                fontSize = (lado.value * 0.58f).sp,
                lineHeight = (lado.value * 0.68f).sp,
                modifier = Modifier
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        drawRect(Color(0xFF3D2C5E), blendMode = BlendMode.SrcIn)
                    },
            )
            is Visual.Seta -> SetaDesenhada(v.direcao, Modifier.size(lado * 0.8f))
            is Visual.Cor -> Canvas(Modifier.size(lado * 0.72f)) {
                val r = size.minDimension / 2f
                drawCircle(Color(0x22000000), r, center + Offset(0f, r * 0.08f))
                drawCircle(Color(v.argb), r * 0.94f)
                drawCircle(Color.White.copy(alpha = 0.35f), r * 0.28f, center + Offset(-r * 0.35f, -r * 0.35f))
            }
        }
    }
}

/**
 * Brilho dourado que pulsa em volta da peça certa (a ajuda depois de 2 erros). Desenhado atrás do
 * conteúdo, com cantos de raio [raio].
 */
@Composable
internal fun Modifier.brilhoN4(ativo: Boolean, raio: Dp = 20.dp): Modifier {
    if (!ativo) return this
    val t = rememberInfiniteTransition(label = "brilhoN4")
    val f by t.animateFloat(0f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "f")
    return this.drawBehind {
        val extra = 6.dp.toPx() + 6.dp.toPx() * f
        val r = raio.toPx() + extra
        drawRoundRect(
            Cores.Estrela.copy(alpha = 0.25f + 0.3f * f),
            topLeft = Offset(-extra, -extra),
            size = Size(size.width + extra * 2, size.height + extra * 2),
            cornerRadius = CornerRadius(r, r),
        )
        drawRoundRect(
            Cores.Estrela,
            topLeft = Offset(-extra * 0.5f, -extra * 0.5f),
            size = Size(size.width + extra, size.height + extra),
            cornerRadius = CornerRadius(r, r),
            style = Stroke(3.dp.toPx()),
        )
    }
}

/** Alto-falante desenhado em Canvas; as ondinhas se mexem quando [tocando]. */
@Composable
internal fun AltoFalanteN4(tocando: Boolean, modifier: Modifier = Modifier, cor: Color = Color.White) {
    val t = rememberInfiniteTransition(label = "altoFalanteN4")
    val f by t.animateFloat(0f, 1f, infiniteRepeatable(tween(700)), label = "f")
    Canvas(modifier) { desenharAltoFalanteN4(cor, if (tocando) f else 0.6f) }
}

private fun DrawScope.desenharAltoFalanteN4(cor: Color, fase: Float) {
    val s = min(size.width, size.height)
    val ox = (size.width - s) / 2f
    val oy = (size.height - s) / 2f
    fun p(x: Float, y: Float) = Offset(ox + x * s, oy + y * s)
    val corpo = Path().apply {
        moveTo(p(0.12f, 0.38f).x, p(0.12f, 0.38f).y)
        lineTo(p(0.3f, 0.38f).x, p(0.3f, 0.38f).y)
        lineTo(p(0.52f, 0.18f).x, p(0.52f, 0.18f).y)
        lineTo(p(0.52f, 0.82f).x, p(0.52f, 0.82f).y)
        lineTo(p(0.3f, 0.62f).x, p(0.3f, 0.62f).y)
        lineTo(p(0.12f, 0.62f).x, p(0.12f, 0.62f).y)
        close()
    }
    drawPath(corpo, cor)
    drawPath(corpo, cor, style = Stroke(s * 0.06f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    // Duas ondinhas: a de fora aparece e some conforme a fase.
    for (k in 0..1) {
        val raio = s * (0.16f + 0.14f * k)
        val alfa = if (k == 0) 1f else (0.35f + 0.65f * sin(fase * PI.toFloat()))
        drawArc(
            cor.copy(alpha = alfa),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = p(0.5f, 0.5f) - Offset(raio, raio),
            size = Size(raio * 2, raio * 2),
            style = Stroke(s * 0.07f, cap = StrokeCap.Round),
        )
    }
}

/** Estrelinhas que saem de [centro] quando algo dá certo ([f] de 0 a 1). */
internal fun DrawScope.faiscasN4(centro: Offset, raio: Float, f: Float, cor: Color = Cores.Estrela) {
    if (f <= 0f || f >= 1f) return
    val alfa = 1f - f
    for (i in 0 until 8) {
        val ang = i * PI.toFloat() / 4f + 0.3f
        val d = raio * (0.6f + 0.9f * f)
        val c = centro + Offset(cos(ang) * d, sin(ang) * d)
        val r = raio * 0.09f * (1f - f * 0.5f)
        drawCircle(cor.copy(alpha = alfa), r, c)
        drawCircle(Color.White.copy(alpha = alfa), r * 0.45f, c)
    }
}
