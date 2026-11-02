package br.com.fipeplaca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fipeplaca.ui.AppRaiz
import br.com.fipeplaca.ui.TemaFipe

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as FipeApp
        setContent {
            val tema by app.tema.collectAsStateWithLifecycle()
            val escuro = when (tema) {
                "claro" -> false
                "escuro" -> true
                else -> isSystemInDarkTheme()
            }
            // Barras do sistema seguem o tema do app (e nao o do sistema): icones claros sobre o cabecalho azul
            androidx.compose.runtime.DisposableEffect(escuro) {
                enableEdgeToEdge(
                    statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (escuro) androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                )
                // Fundo da janela (abertura, teclado) segue o tema escolhido no app, nao o do sistema
                window.decorView.setBackgroundColor(if (escuro) 0xFF0D121B.toInt() else 0xFFF4F6FB.toInt())
                onDispose {}
            }
            TemaFipe(escuro = escuro) { AppRaiz() }
        }
    }
}
