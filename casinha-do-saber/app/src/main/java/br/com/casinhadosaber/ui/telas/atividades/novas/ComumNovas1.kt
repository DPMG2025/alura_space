package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.componentes.FiguraDaPergunta
import br.com.casinhadosaber.ui.componentes.SetaDesenhada
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.figurasDe
import br.com.casinhadosaber.ui.telas.atividades.nomeDaFigura
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Peças comuns de Pescaria, Balões, Caça na cena e Esconde esconde (prefixo N1 para não colidir com os
 * auxiliares dos outros tipos novos, feitos em paralelo).
 */

/** Cores alegres dos peixes e balões (sem vermelho puro, que aqui lembra erro). */
internal val N1Paleta = listOf(
    Color(0xFFFF9F43), Color(0xFF54A0FF), Color(0xFF1DD1A1), Color(0xFFFF6FB5),
    Color(0xFFFECA57), Color(0xFF9B7BFF), Color(0xFF48DBFB), Color(0xFFFF8A65), Color(0xFF7BD66B),
)

internal val N1Dourado = Color(0xFFFFC93C)

/** O que o narrador diz quando a criança toca nesta opção: a fala dela ou, sem fala, o nome da figura. */
internal fun n1Nome(o: Opcao): List<Trecho> {
    if (o.fala.isNotEmpty()) return o.fala
    val nome = when (val v = o.visual) {
        is Visual.Emoji -> nomeDaFigura(v.emoji)
        is Visual.Texto -> v.texto.lowercase()
        is Visual.Sombra -> nomeDaFigura(v.emoji)
        is Visual.Cor, is Visual.Seta -> null
    }
    return if (nome.isNullOrBlank()) emptyList() else listOf(pt(nome))
}

/** Descrição para acessibilidade e para os testes encontrarem a figura. */
internal fun n1Descricao(o: Opcao): String =
    n1Nome(o).joinToString(" ") { it.texto }.ifBlank {
        when (val v = o.visual) {
            is Visual.Emoji -> v.emoji
            is Visual.Texto -> v.texto
            is Visual.Sombra -> "Sombra"
            is Visual.Cor -> "Cor"
            is Visual.Seta -> "Seta"
        }
    }

/**
 * Relógio da cena em segundos, contado pelos quadros da tela: no app anda junto com a tela, e nas
 * capturas do Robolectric anda com o relógio do teste. Quem lê o valor dentro de `offset { }` ou de um
 * Canvas só refaz o layout ou o desenho (sem recompor e sem criar objetos a cada quadro).
 */
@Composable
internal fun n1Relogio(): MutableFloatState {
    val tempo = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        // Animação sem fim: nos testes com relógio automático (emulador) ela fica parada, senão a tela
        // nunca fica ociosa; no app e nas capturas (relógio manual) anda normalmente.
        var antes = withInfiniteAnimationFrameNanos { it }
        while (true) {
            val agora = withInfiniteAnimationFrameNanos { it }
            // Um quadro muito atrasado (app em segundo plano) não faz nada pular longe.
            tempo.floatValue += ((agora - antes) / 1_000_000_000f).coerceIn(0f, 0.1f)
            antes = agora
        }
    }
    return tempo
}

/** Espera [ms] contando pelos quadros (ver [n1Relogio]): nas capturas o tempo anda com o relógio do teste. */
internal suspend fun n1Esperar(ms: Long) {
    val inicio = withFrameNanos { it }
    while (withFrameNanos { it } - inicio < ms * 1_000_000L) {
        // só espera o próximo quadro
    }
}

/**
 * Quando a mãozinha deve aparecer: logo no começo (a primeira vez ensina o gesto), depois de cada
 * parada longa, e mais depressa quando a ajuda está ligada. Volta a contar a cada [interacoes].
 */
@Composable
internal fun n1Parada(
    interacoes: Int,
    ativa: Boolean,
    terminou: Boolean,
    ajuda: Boolean,
    ajudaInicial: Boolean,
    primeira: Long = 1500L,
    semAjuda: Long = 8000L,
): Boolean {
    var parada by remember { mutableStateOf(false) }
    LaunchedEffect(interacoes, terminou, ativa, ajuda) {
        parada = false
        if (!ativa || terminou) return@LaunchedEffect
        delay(
            when {
                ajudaInicial && interacoes == 0 -> 700L
                interacoes == 0 -> primeira
                ajuda -> 2500L
                else -> semAjuda
            },
        )
        parada = true
    }
    return parada
}

/**
 * Halo dourado que pulsa atrás da resposta certa (depois de 2 erros). Desenhe a figura por cima.
 * Só círculos cheios: nada é criado a cada quadro. [tempo] em segundos.
 */
internal fun DrawScope.n1Brilho(centro: Offset, raio: Float, tempo: Float) {
    val pulso = 0.5f + 0.5f * sin(tempo * 5f)
    drawCircle(N1Dourado.copy(alpha = 0.22f + 0.18f * pulso), raio * (1.12f + 0.1f * pulso), centro)
    drawCircle(N1Dourado, raio * (0.98f + 0.05f * pulso), centro)
    drawCircle(Color(0xFFFFF6D5), raio * 0.86f, centro)
}

/** Estrelinhas e raios de um estouro ou de um achado ([p] de 0 a 1). */
internal fun DrawScope.n1Estouro(centro: Offset, raio: Float, p: Float, cor: Color) {
    if (p <= 0f || p >= 1f) return
    val a = 1f - p
    val r0 = raio * (0.4f + 0.9f * p)
    val r1 = raio * (0.7f + 1.1f * p)
    for (k in 0 until 10) {
        val ang = k * 0.6283185f
        val c = kotlin.math.cos(ang)
        val s = sin(ang)
        drawLine(
            (if (k % 2 == 0) cor else N1Dourado).copy(alpha = a),
            Offset(centro.x + c * r0, centro.y + s * r0),
            Offset(centro.x + c * r1, centro.y + s * r1),
            strokeWidth = raio * 0.12f * a + 1f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
    drawCircle(Color.White.copy(alpha = 0.6f * a), raio * (0.5f + 0.8f * p), centro, style = Stroke(raio * 0.08f * a + 1f))
}

/**
 * A figura de uma opção (emoji, texto, cor, sombra ou seta) do tamanho [lado], centralizada.
 * Emojis repetidos ("🍎🍎🍎") viram uma gradinha, para dar para contar.
 */
@Composable
internal fun N1Figura(o: Opcao, lado: Dp, modifier: Modifier = Modifier, corTexto: Color = Cores.Texto) {
    Box(modifier.size(lado), contentAlignment = Alignment.Center) {
        when (val v = o.visual) {
            is Visual.Emoji -> {
                val figuras = remember(v.emoji) { figurasDe(v.emoji) }
                if (figuras.size <= 1) {
                    val tam = lado.value * 0.74f * v.escala.coerceIn(0.4f, 1.3f)
                    FiguraDaPergunta(v.emoji, (tam * 1.2f).dp) {
                        Text(v.emoji, fontSize = tam.sp, lineHeight = (tam * 1.1f).sp)
                    }
                } else {
                    val colunas = ceil(sqrt(figuras.size.toFloat())).toInt()
                    val linhas = ceil(figuras.size / colunas.toFloat()).toInt()
                    val cel = lado.value * 0.9f / maxOf(colunas, linhas)
                    androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        for (linha in figuras.chunked(colunas)) {
                            Row(horizontalArrangement = Arrangement.Center) {
                                for (f in linha) {
                                    Box(Modifier.size(cel.dp), contentAlignment = Alignment.Center) {
                                        FiguraDaPergunta(f, (cel * 0.92f).dp) {
                                            Text(f, fontSize = (cel * 0.78f).sp, lineHeight = (cel * 0.9f).sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is Visual.Texto -> {
                val fator = when (v.texto.length) {
                    in 0..2 -> 0.56f
                    3 -> 0.42f
                    4 -> 0.34f
                    5 -> 0.28f
                    else -> 0.22f
                }
                Text(
                    v.texto,
                    fontSize = (lado.value * fator).sp,
                    lineHeight = (lado.value * fator * 1.1f).sp,
                    fontWeight = FontWeight.Bold,
                    color = corTexto,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                )
            }
            is Visual.Cor -> Box(
                Modifier
                    .size(lado * 0.78f)
                    .background(Color(v.argb), CircleShape)
                    .border(3.dp, Color(0x33000000), CircleShape),
            )
            is Visual.Sombra -> Text(
                v.emoji,
                fontSize = (lado.value * 0.72f).sp,
                lineHeight = (lado.value * 0.8f).sp,
                modifier = Modifier
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        drawRect(Cores.Texto, blendMode = BlendMode.SrcIn)
                    },
            )
            is Visual.Seta -> SetaDesenhada(v.direcao, Modifier.size(lado * 0.8f))
        }
    }
}

/**
 * Barra de achados: [total] casinhas; as primeiras [achados] mostram o que já foi pescado, estourado
 * ou achado. Fica no alto ou embaixo da atividade e não recebe toques.
 */
@Composable
internal fun N1BarraDeAchados(total: Int, achados: List<Opcao>, cor: Color, larguraMax: Dp, modifier: Modifier = Modifier) {
    val lado = minOf(54.dp, (larguraMax - 12.dp) / total.coerceAtLeast(1) - 6.dp).coerceAtLeast(26.dp)
    Row(
        modifier
            .testTag("barra_achados")
            .background(Color.White.copy(alpha = 0.95f), CircleShape)
            .border(3.dp, cor.copy(alpha = 0.5f), CircleShape)
            .size(width = (lado + 6.dp) * total + 12.dp, height = lado + 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until total) {
            val o = achados.getOrNull(i)
            Box(Modifier.size(lado + 6.dp), contentAlignment = Alignment.Center) {
                if (o != null) {
                    Box(
                        Modifier
                            .size(lado)
                            .background(Color.White, CircleShape)
                            .border(3.dp, Cores.Certo, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { N1Figura(o, lado * 0.86f) }
                } else {
                    Canvas(Modifier.size(lado)) {
                        drawCircle(cor.copy(alpha = 0.12f))
                        drawCircle(
                            cor.copy(alpha = 0.55f),
                            style = Stroke(2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                            radius = size.minDimension / 2f - 2.dp.toPx(),
                        )
                    }
                }
            }
        }
    }
}
