package br.com.casinhadosaber.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    val EstrelaApagada = Color(0xFFE6DCCB)
    val Cartao = Color(0xFFFFFFFF)
    val Madeira = Color(0xFFB9784A)
    val MadeiraClara = Color(0xFFE6B98F)
    val Roxo = Color(0xFF6C63FF)
}

private val base = TextStyle(fontFamily = Fredoka, color = Cores.Texto)

private val tipografia = Typography(
    displayLarge = base.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold, lineHeight = 60.sp),
    displayMedium = base.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
    headlineLarge = base.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    headlineMedium = base.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
    titleLarge = base.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    titleMedium = base.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    bodyLarge = base.copy(fontSize = 18.sp, lineHeight = 24.sp),
    bodyMedium = base.copy(fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = base.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
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
        ),
        typography = tipografia,
        content = conteudo,
    )
}
