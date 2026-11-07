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

/** Fredoka (500 e 700): fonte arredondada dos textos, fácil de ler para quem está aprendendo as letras. */
val Fredoka = FontFamily(
    Font(R.font.fredoka_medio, FontWeight.Normal),
    Font(R.font.fredoka_medio, FontWeight.Medium),
    Font(R.font.fredoka_negrito, FontWeight.SemiBold),
    Font(R.font.fredoka_negrito, FontWeight.Bold),
    Font(R.font.fredoka_negrito, FontWeight.ExtraBold),
)

/**
 * Baloo 2 (700 e 800): títulos e números, como nas maquetes do visual brinquedo 3D. Pesos abaixo de 700
 * também usam o 700 (a Baloo só entra em títulos; texto corrido continua na [Fredoka]).
 */
val Baloo2 = FontFamily(
    Font(R.font.baloo2_negrito, FontWeight.Normal),
    Font(R.font.baloo2_negrito, FontWeight.Medium),
    Font(R.font.baloo2_negrito, FontWeight.SemiBold),
    Font(R.font.baloo2_negrito, FontWeight.Bold),
    Font(R.font.baloo2_extranegrito, FontWeight.ExtraBold),
    Font(R.font.baloo2_extranegrito, FontWeight.Black),
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

    // Visual brinquedo 3D (docs/visual-3d/prototipo/ui2.css).
    /** Lábio lilás embaixo dos botões redondos claros. */
    val LabioLilas = Color(0xFFB9A8EC)
    /** Lábio lilás das pílulas e dos balões (um tom mais claro). */
    val LabioClaro = Color(0xFFD3C7F5)
    /** Contorno dos títulos brancos (`.titulo-brinquedo`) e o tom mais fundo do lábio deles. */
    val ContornoDoTitulo = Color(0xFF7C4DFF)
    val LabioDoTitulo = Color(0xFF5A2FD1)
    /** Contorno escuro translúcido dos números brancos sobre blocos coloridos (`.contorno-escuro`). */
    val ContornoEscuro = Color(0x52000000)
    /** Texto das pílulas (números do placar). */
    val TextoDaPilula = Color(0xFF4B2FA8)
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

    /**
     * Face do bloco de brinquedo (`.bloco` do protótipo): a cor clareada no alto, a própria cor no meio e um
     * pouco mais funda embaixo. Branco vira branco para lilás bem claro (como as pílulas).
     */
    fun bloco(cor: Color): Brush =
        if (cor == Color.White) Brush.verticalGradient(listOf(Color.White, Color(0xFFF1ECFF)))
        else Brush.verticalGradient(0f to lerp(cor, Color.White, 0.22f), 0.55f to cor, 1f to lerp(cor, Color.Black, 0.08f))

    /** Corpo de botão: a cor um pouco mais clara no alto e a própria cor embaixo. */
    fun botao(cor: Color): Brush = Brush.verticalGradient(listOf(lerp(cor, Color.White, 0.12f), cor))
}

private val base = TextStyle(fontFamily = Fredoka, color = Cores.Texto)
private val titulo = TextStyle(fontFamily = Baloo2, color = Cores.Texto)

// Os 15 estilos do Material 3 (nenhum cai na Roboto): display e headline (títulos e números grandes) em
// Baloo 2; title, body e label (textos, botões, legendas) em Fredoka. As alturas de linha não mudaram,
// para nenhuma tela crescer: a Baloo tem a letra um pouco menor que a Fredoka no mesmo tamanho, nunca maior.
private val tipografia = Typography(
    displayLarge = titulo.copy(fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 60.sp),
    displayMedium = titulo.copy(fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 44.sp),
    displaySmall = titulo.copy(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 40.sp),
    headlineLarge = titulo.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp),
    headlineMedium = titulo.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
    headlineSmall = titulo.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
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
