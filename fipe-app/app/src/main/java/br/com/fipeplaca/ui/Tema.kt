package br.com.fipeplaca.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Claro = lightColorScheme(
    primary = Color(0xFF0D47A1),
    secondary = Color(0xFF00796B),
    tertiary = Color(0xFF2E7D32),
)
private val Escuro = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFA5D6A7),
)

@Composable
fun TemaFipe(conteudo: @Composable () -> Unit) {
    val escuro = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val cores = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (escuro) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        escuro -> Escuro
        else -> Claro
    }
    MaterialTheme(colorScheme = cores, content = conteudo)
}
