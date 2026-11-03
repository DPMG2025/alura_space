package br.com.casinhadosaber.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.R

/** Fredoka: fonte arredondada, fácil de ler para quem está aprendendo as letras. */
val Fredoka = FontFamily(
    Font(R.font.fredoka_medio, FontWeight.Normal),
    Font(R.font.fredoka_medio, FontWeight.Medium),
    Font(R.font.fredoka_negrito, FontWeight.SemiBold),
    Font(R.font.fredoka_negrito, FontWeight.Bold),
    Font(R.font.fredoka_negrito, FontWeight.ExtraBold),
)

object Cores {
    val Fundo = Color(0xFFFFF8E7)
    val Ceu = Color(0xFF8FD3FF)
    val Grama = Color(0xFF7CC56B)
    val Texto = Color(0xFF3D2C5E)
    val TextoSuave = Color(0xFF7A6B94)
    val Certo = Color(0xFF3DBE6B)
    val Errado = Color(0xFFFF6B6B)
    val Estrela = Color(0xFFFFC93C)
    val EstrelaApagada = Color(0x40000000)
    val Cartao = Color(0xFFFFFFFF)
    val Madeira = Color(0xFFB9784A)
    val MadeiraClara = Color(0xFFE6B98F)
    val Roxo = Color(0xFF6C63FF)

    // Sistema de design (ver PRD, "Visual: sistema de design").
    /** Contorno de tudo que é tocável ou desenhado: ameixa escura a 25%. Sobre qualquer cor vira um tom mais escuro dela. */
    val Contorno = Color(0x403D2C5E)
    /** Sombra suave embaixo de botões, cartões e objetos (ameixa, nunca preto puro, para não sujar as cores). */
    val Sombra = Color(0x33231A3A)
    /** Brilho do topo dos botões e das bolhas (branco a 35%). */
    val Brilho = Color(0x59FFFFFF)
    /** Corpo creme dos cartões e janelas por cima da tela. */
    val Creme = Color(0xFFFFF6DC)
    /** Véu violeta atrás das janelas (o de antes, quase preto, ficava marrom sobre o fundo laranja). */
    val Veu = Color(0xC4372287)
}

/** Raios dos cantos: só estes quatro, para tudo combinar. */
object Raios {
    /** Chips, selos e células pequenas. */
    val Pequeno = 8.dp
    /** Ladrilhos de resposta e peças. */
    val Medio = 16.dp
    /** Botões grandes e painéis de cena. */
    val Grande = 24.dp
    /** Cartões e janelas. */
    val Enorme = 32.dp
}

/** As formas prontas dos [Raios]. */
object Formas {
    val Pequena = RoundedCornerShape(Raios.Pequeno)
    val Media = RoundedCornerShape(Raios.Medio)
    val Grande = RoundedCornerShape(Raios.Grande)
    val Enorme = RoundedCornerShape(Raios.Enorme)
}

/**
 * Degradês prontos. Os de cor única seguem a regra do fundo: o topo fica com a cor viva (sem cinza),
 * e o pé clareia na direção do creme.
 */
object Degrades {
    /** Faixa de brilho que some para baixo (topo dos botões e das bolhas). */
    val BrilhoDoTopo: Brush = Brush.verticalGradient(listOf(Cores.Brilho, Color.Transparent))
    /** Sombra que nasce embaixo (pé de painéis e cenas). */
    val SombraDoPe: Brush = Brush.verticalGradient(listOf(Color.Transparent, Cores.Sombra))
    /** Céu de dia, do azul vivo ao azul claro. */
    val Ceu: Brush = Brush.verticalGradient(listOf(Color(0xFF6EC3FF), Color(0xFFBFE8FF)))
    /** Prêmio e conquistas: violeta profundo para lilás. */
    val Premio: Brush = Brush.verticalGradient(listOf(Color(0xFF5B4BDB), Color(0xFF9B7BFF)))
    /** Balão de fala e cartões brancos: branco para um lilás quase branco. */
    val Cartao: Brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFF6F1FF)))

    /** Fundo de duas cores a partir de uma cor: viva em cima, clara em baixo. */
    fun fundo(cor: Color): Brush = Brush.verticalGradient(listOf(cor, lerp(cor, Cores.Fundo, 0.35f), lerp(cor, Cores.Fundo, 0.7f)))

    /** Corpo de botão: a cor um pouco mais clara no alto e a própria cor embaixo. */
    fun botao(cor: Color): Brush = Brush.verticalGradient(listOf(lerp(cor, Color.White, 0.12f), cor))
}

private val base = TextStyle(fontFamily = Fredoka, color = Cores.Texto)

// Os 15 estilos do Material 3, todos em Fredoka (os que faltavam caíam na Roboto).
private val tipografia = Typography(
    displayLarge = base.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold, lineHeight = 60.sp),
    displayMedium = base.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
    displaySmall = base.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    headlineLarge = base.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    headlineMedium = base.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
    headlineSmall = base.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleLarge = base.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    titleMedium = base.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    titleSmall = base.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    bodyLarge = base.copy(fontSize = 18.sp, lineHeight = 24.sp),
    bodyMedium = base.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodySmall = base.copy(fontSize = 14.sp, lineHeight = 18.sp),
    labelLarge = base.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    labelMedium = base.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp),
    labelSmall = base.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp),
)

@Composable
fun TemaCasinha(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Cores.Roxo,
            secondary = Color(0xFFFF9F1C),
            background = Cores.Fundo,
            surface = Cores.Cartao,
            onBackground = Cores.Texto,
            onSurface = Cores.Texto,
            // Componentes do Material que escurecem a tela usam o mesmo véu violeta.
            scrim = Cores.Veu,
        ),
        typography = tipografia,
        content = conteudo,
    )
}
