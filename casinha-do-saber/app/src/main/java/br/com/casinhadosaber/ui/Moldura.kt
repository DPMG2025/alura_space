package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** Verdadeiro quando a escala de tablet já foi aplicada mais acima (evita aplicar duas vezes). */
val LocalEscalaAplicada = staticCompositionLocalOf { false }

/** Área mínima de toque de qualquer botão do app (dedos de criança de 4 anos). */
val TOQUE_MINIMO = 64.dp

/** Largura máxima das telas comuns (jogo, caminho, prêmio): tamanho de um celular grande. */
val LARGURA_DE_CELULAR = 640.dp

/** Largura máxima das telas que se reorganizam em duas colunas na paisagem (início e casa). */
val LARGURA_DE_PAISAGEM = 1180.dp

/**
 * Quanto o app cresce num tablet. Em retrato, 30% (botões e textos do tamanho da mão da criança).
 * Em paisagem a altura é a medida curta, então a escala diminui para que caibam pelo menos 620 dp
 * "de app" na altura (a mesma altura de um celular pequeno, onde todas as telas já foram conferidas).
 */
fun escalaDeTablet(menorLarguraDp: Int, alturaDp: Int): Float {
    if (menorLarguraDp < 600) return 1f
    return (alturaDp / 620f).coerceIn(1f, 1.3f)
}

/** A tela está deitada (mais larga que alta)? */
@Composable
fun emPaisagem(): Boolean {
    val c = LocalConfiguration.current
    return c.screenWidthDp > c.screenHeightDp
}

/**
 * Aplica a escala de tablet e a área mínima de toque de [TOQUE_MINIMO] ao [conteudo].
 * Pode ser chamada mais de uma vez na mesma árvore: só a de fora vale.
 */
@Composable
fun EscalaDeTablet(conteudo: @Composable () -> Unit) {
    if (LocalEscalaAplicada.current) {
        conteudo()
        return
    }
    val config = LocalConfiguration.current
    val densidade = LocalDensity.current
    val escala = escalaDeTablet(config.smallestScreenWidthDp, config.screenHeightDp)
    val densidadeDoApp = if (escala != 1f) Density(densidade.density * escala, densidade.fontScale) else densidade
    val base = LocalViewConfiguration.current
    // Toque estendido: o Compose aceita toques até 64 dp em volta do centro de botões menores.
    val toque = remember(base) {
        object : ViewConfiguration by base {
            override val minimumTouchTargetSize: DpSize = DpSize(TOQUE_MINIMO, TOQUE_MINIMO)
        }
    }
    CompositionLocalProvider(
        LocalDensity provides densidadeDoApp,
        LocalViewConfiguration provides toque,
        LocalEscalaAplicada provides true,
    ) {
        conteudo()
    }
}

/**
 * A moldura de cada tela: escala de tablet e conteúdo centralizado com largura máxima.
 * Com [larga] em paisagem, a tela pode usar até [LARGURA_DE_PAISAGEM] (ela se divide em duas colunas).
 */
@Composable
fun MolduraDoApp(larga: Boolean = false, conteudo: @Composable () -> Unit) {
    val paisagem = emPaisagem()
    EscalaDeTablet {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val maxima: Dp = if (larga && paisagem) LARGURA_DE_PAISAGEM else LARGURA_DE_CELULAR
            // Quando sobra céu dos lados (tablet deitado), a coluna ganha uma sombra suave nas bordas
            // de fora: parece uma página em cima do céu, e não um recorte com a borda dura.
            val sobraLado = maxWidth > maxima + 8.dp
            val coluna = Modifier.widthIn(max = maxima).fillMaxHeight()
            Box(if (sobraLado) coluna.drawBehind { sombraDosLados(16.dp.toPx()) } else coluna) { conteudo() }
        }
    }
}

/** Duas faixas de sombra que somem para fora, à esquerda e à direita da área desenhada. */
private fun DrawScope.sombraDosLados(largura: Float) {
    val escura = Color(0x22203050)
    drawRect(
        Brush.horizontalGradient(listOf(Color.Transparent, escura), startX = -largura, endX = 0f),
        topLeft = Offset(-largura, 0f),
        size = Size(largura, size.height),
    )
    drawRect(
        Brush.horizontalGradient(listOf(escura, Color.Transparent), startX = size.width, endX = size.width + largura),
        topLeft = Offset(size.width, 0f),
        size = Size(largura, size.height),
    )
}
