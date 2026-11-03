package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.componentes.SetaDesenhada
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.figurasDe
import br.com.casinhadosaber.ui.telas.atividades.nomeDaFigura
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

/*
 * Peças comuns de Ligar pares, Quebra cabeça, Trem e Dominó (prefixo N2 para não esbarrar nas
 * outras telas novas do mesmo pacote).
 */

/** Cores alegres para linhas, vagões e peças (uma por item, sem repetir em sequência). */
internal val PaletaN2 = listOf(
    Color(0xFFFF6B6B),
    Color(0xFF4DABF7),
    Color(0xFF51CF66),
    Color(0xFFFFA94D),
    Color(0xFFB197FC),
    Color(0xFFF783AC),
    Color(0xFF3BC9DB),
    Color(0xFFFFD43B),
)

/** Amarelinho do brilho de ajuda (o mesmo tom da sílaba que brilha em Montar a palavra). */
internal val BrilhoN2 = Color(0xFFFFF3C4)

/** O que o narrador diz de um visual: a fala própria ou, sem ela, o texto ou o nome da figura. */
internal fun falaDoVisualN2(v: Visual, fala: List<Trecho>): List<Trecho> {
    if (fala.isNotEmpty()) return fala
    val texto = when (v) {
        is Visual.Texto -> v.texto.lowercase()
        is Visual.Emoji -> figurasDe(v.emoji).let { f ->
            val nome = nomeDaFigura(f.firstOrNull() ?: "")
            if (f.size > 1 && f.distinct().size == 1) f.size.toString() else nome
        }
        is Visual.Sombra -> nomeDaFigura(v.emoji)?.let { "sombra de $it" } ?: "sombra"
        is Visual.Cor, is Visual.Seta -> null
    }
    return if (texto.isNullOrBlank()) emptyList() else listOf(pt(texto))
}

internal fun falaDaOpcaoN2(o: Opcao) = falaDoVisualN2(o.visual, o.fala)

/** Descrição para acessibilidade (e para os testes acharem a peça pelo nome). */
internal fun descricaoN2(v: Visual, fala: List<Trecho>): String =
    falaDoVisualN2(v, fala).joinToString(" ") { it.texto }.ifBlank {
        when (v) {
            is Visual.Texto -> v.texto
            is Visual.Emoji -> v.emoji
            is Visual.Cor -> "cor"
            is Visual.Sombra -> "sombra"
            is Visual.Seta -> "seta"
        }
    }

/**
 * Desenha um [Visual] dentro de um quadrado de [lado]: texto que diminui para caber, uma figura
 * grande, várias figuras numa gradinha (quantidades), círculo de cor, sombra ou seta.
 */
@Composable
internal fun VisualN2(v: Visual, lado: Dp, modifier: Modifier = Modifier, corTexto: Color = Cores.Texto) {
    Box(modifier.size(lado), contentAlignment = Alignment.Center) {
        when (v) {
            is Visual.Texto -> {
                val n = v.texto.length.coerceAtLeast(1)
                // Até 2 letras ocupam bem o quadrado; palavras longas encolhem para caber numa linha
                // (letra de forma em negrito tem uns 0,72 da altura de largura: CACHORRO cabe inteiro).
                val tamanho = min(lado.value * 0.56f, lado.value * (if (n <= 2) 1.5f else 1.15f) / n)
                Text(
                    v.texto,
                    fontSize = tamanho.sp,
                    lineHeight = (tamanho * 1.1f).sp,
                    fontWeight = FontWeight.Bold,
                    color = corTexto,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            is Visual.Emoji -> {
                val figuras = figurasDe(v.emoji)
                if (figuras.size <= 1) {
                    val t = lado.value * 0.6f * v.escala.coerceIn(0.4f, 1.3f)
                    Text(v.emoji, fontSize = t.sp, lineHeight = (t * 1.15f).sp)
                } else {
                    // Quantidades numa grade quase quadrada: 4 vira 2 por 2, 6 vira 3 por 2.
                    val colunas = ceil(sqrt(figuras.size.toFloat())).toInt()
                    val linhas = ceil(figuras.size / colunas.toFloat()).toInt()
                    val celula = lado.value * 0.86f / maxOf(colunas, linhas)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        for (linha in figuras.chunked(colunas)) {
                            Row {
                                for (f in linha) {
                                    Box(Modifier.size(celula.dp), contentAlignment = Alignment.Center) {
                                        Text(f, fontSize = (celula * 0.74f).sp, lineHeight = (celula * 0.86f).sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is Visual.Sombra -> {
                val t = lado.value * 0.6f
                Text(
                    v.emoji,
                    fontSize = t.sp,
                    lineHeight = (t * 1.15f).sp,
                    modifier = Modifier
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            drawRect(Color(0xFF3D2C5E), blendMode = BlendMode.SrcIn)
                        },
                )
            }
            is Visual.Cor -> Box(
                Modifier
                    .size(lado * 0.7f)
                    .background(Color(v.argb), CircleShape)
                    .border(4.dp, Color(0x22000000), CircleShape),
            )
            is Visual.Seta -> SetaDesenhada(v.direcao, Modifier.size(lado * 0.7f))
        }
    }
}

/**
 * Fica verdadeiro quando a criança está parada há um tempo (a mãozinha volta a mostrar o gesto),
 * com os mesmos tempos de Montar a palavra: logo no começo, depois de tocar sem arrastar, e mais
 * cedo quando já está na ajuda.
 */
@Composable
internal fun paradaN2(
    ativa: Boolean,
    terminou: Boolean,
    ajudaInicial: Boolean,
    ajuda: Boolean,
    interacoes: Int,
    tocouSemArrastar: Boolean,
    progresso: Int,
): Boolean {
    var parada by remember { mutableStateOf(false) }
    LaunchedEffect(interacoes, terminou, ativa, ajuda, progresso) {
        parada = false
        if (!ativa || terminou) return@LaunchedEffect
        delay(
            when {
                ajudaInicial && interacoes == 0 -> 700L
                interacoes == 0 -> 2500L
                tocouSemArrastar -> 900L
                ajuda -> 3000L
                else -> 7000L
            },
        )
        parada = true
    }
    return parada
}
