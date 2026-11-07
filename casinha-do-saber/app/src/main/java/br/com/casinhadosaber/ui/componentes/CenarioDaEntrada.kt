package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.ui.tema.Cores
import kotlin.math.roundToInt

// Peças de cenário das telas de entrada, do mascote e das conquistas no visual brinquedo 3D (Fase 8, grupo A):
// o céu em degradê da maquete 1, nuvens 3D que boiam devagar, a ilhinha de grama (`item_terreno`) onde os bonecos
// pisam e o texto dos botões com contorno escuro (maquete 3).

/** Céu da maquete 1: azul em cima, pêssego e rosa embaixo (o mesmo da tela inicial). */
val CeuDaEntrada: Brush = Brush.verticalGradient(
    0f to Color(0xFF7CC4FA),
    0.38f to Color(0xFFB8E2FF),
    0.66f to Color(0xFFFFE6D6),
    1f to Color(0xFFFFD0DC),
)

/** Verde do botão principal (o "Pôr na casinha" do prêmio) e o lábio dele. */
val VerdeDoPronto = Color(0xFF34C46A)
val LabioDoVerde = Color(0xFF1C9552)

/**
 * Duas nuvens 3D (`ceu_nuvem_1/2`) no alto do céu, boiando devagar para os lados (uma camada só, pequena).
 * Decorativas; ficam atrás do conteúdo. [animado] = false para as deixar paradas.
 */
@Composable
fun BoxScope.NuvensDoCeu(animado: Boolean = true) {
    val vai by rememberInfiniteTransition(label = "nuvens")
        .animateFloat(0f, 1f, infiniteRepeatable(tween(9000), RepeatMode.Reverse), label = "vai")
    val passo = if (animado) vai else 0.5f
    ImagemDeArte(
        PecasDasTelas.NUVEM_GRANDE,
        Modifier
            .align(Alignment.TopStart)
            .padding(top = 64.dp)
            .width(118.dp)
            .graphicsLayer { translationX = (passo - 0.5f) * 24.dp.toPx() - 18.dp.toPx(); alpha = 0.95f },
    )
    ImagemDeArte(
        PecasDasTelas.NUVEM_PEQUENA,
        Modifier
            .align(Alignment.TopEnd)
            .padding(top = 118.dp)
            .width(84.dp)
            .graphicsLayer { translationX = (0.5f - passo) * 20.dp.toPx() + 14.dp.toPx(); alpha = 0.9f },
    )
}

/**
 * Só a ilha, para pôr atrás de quem pisa nela: a grama de cima fica a [pes] do fundo da caixa do chamador
 * ([BoxScope]), centralizada, com [largura] de ilha visível.
 */
@Composable
fun BoxScope.IlhaAtras(largura: Dp, pes: Dp, modifier: Modifier = Modifier) {
    // Na imagem (320 x 320), a ilha vai de x 36 a 283 (77% da largura) e de y 71 a 246; a grama onde se pisa fica
    // perto de y 176 (55%). A imagem pode passar da caixa (a folga dela é transparente): mede-se sem limite.
    val lado = largura / 0.77f
    Box(
        modifier
            .matchParentSize()
            .layout { medida, limites ->
                val px = lado.roundToPx()
                val imagem = medida.measure(Constraints.fixed(px, px))
                layout(limites.maxWidth, limites.maxHeight) {
                    imagem.place((limites.maxWidth - px) / 2, limites.maxHeight - pes.roundToPx() - (px * 0.55f).roundToInt())
                }
            },
    ) {
        ImagemDeArte(PecasDasTelas.ILHA, Modifier.fillMaxSize())
    }
}

/**
 * Título da tela em branco com contorno roxo (maquete 1). Em cima de fundo claro, o contorno dá o contraste.
 */
@Composable
fun TituloDeBrinquedo(
    texto: String,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.headlineLarge,
    maxLinhas: Int = 2,
) {
    TextoComContorno(texto, modifier, estilo = estilo, maxLinhas = maxLinhas, espessura = 5.dp)
}

/** Texto de botão colorido: branco com contorno escuro (`.contorno-escuro` do protótipo). */
@Composable
fun TextoDoBotao(texto: String, modifier: Modifier = Modifier, estilo: TextStyle = MaterialTheme.typography.headlineMedium) {
    TextoComContorno(texto, modifier, estilo = estilo, corDoContorno = Cores.ContornoEscuro, espessura = 4.dp, profundidade = 2.dp)
}
