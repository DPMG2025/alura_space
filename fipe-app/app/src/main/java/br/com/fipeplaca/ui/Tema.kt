package br.com.fipeplaca.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Cores proprias da marca que nao existem no esquema do Material. */
@Immutable
data class CoresExtras(
    val alta: Color,
    val baixa: Color,
    val gradienteInicio: Color,
    val gradienteFim: Color,
    val placaAzul: Color,
)

private val ExtrasClaro = CoresExtras(
    alta = Color(0xFF157A3F),
    baixa = Color(0xFFC62828),
    gradienteInicio = Color(0xFF0A2F7A),
    gradienteFim = Color(0xFF1565C0),
    placaAzul = Color(0xFF003399),
)
private val ExtrasEscuro = CoresExtras(
    alta = Color(0xFF5BD68C),
    baixa = Color(0xFFFF8A80),
    gradienteInicio = Color(0xFF0B1E4A),
    gradienteFim = Color(0xFF1A3F8F),
    placaAzul = Color(0xFF003399),
)

val LocalCoresExtras = staticCompositionLocalOf { ExtrasClaro }

private val Claro = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondary = Color(0xFF00695C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEFEA),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFFFFB300),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF151A23),
    surface = Color.White,
    onSurface = Color(0xFF151A23),
    surfaceVariant = Color(0xFFEDF1F8),
    onSurfaceVariant = Color(0xFF586174),
    surfaceContainer = Color(0xFFF0F3F9),
    surfaceContainerHigh = Color(0xFFE9EDF5),
    outline = Color(0xFF8C95A6), // contraste >= 3:1 para bordas de componentes
    outlineVariant = Color(0xFFDDE2EC),
    error = Color(0xFFC62828),
    errorContainer = Color(0xFFFFE3E0),
    onErrorContainer = Color(0xFF5F1412),
)

private val Escuro = darkColorScheme(
    primary = Color(0xFF9DBBFF),
    onPrimary = Color(0xFF00296B),
    primaryContainer = Color(0xFF1C3A75),
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFF5DD5C4),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF0E4A43),
    onSecondaryContainer = Color(0xFFCDEFEA),
    tertiary = Color(0xFFFFCA4F),
    background = Color(0xFF0D121B),
    onBackground = Color(0xFFE3E7EF),
    surface = Color(0xFF151C28),
    onSurface = Color(0xFFE3E7EF),
    surfaceVariant = Color(0xFF1E2735),
    onSurfaceVariant = Color(0xFFA7B0C2),
    surfaceContainer = Color(0xFF19212E),
    surfaceContainerHigh = Color(0xFF212A39),
    outline = Color(0xFF4A5468),
    outlineVariant = Color(0xFF2A3445),
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFF5F1412),
    onErrorContainer = Color(0xFFFFDAD5),
)

private val Tipografia = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.25).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun TemaFipe(escuro: Boolean = isSystemInDarkTheme(), conteudo: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalCoresExtras provides if (escuro) ExtrasEscuro else ExtrasClaro,
    ) {
        MaterialTheme(
            colorScheme = if (escuro) Escuro else Claro,
            typography = Tipografia,
            shapes = Formas,
            content = conteudo,
        )
    }
}

object Extras {
    val cores: CoresExtras
        @Composable get() = LocalCoresExtras.current
}
