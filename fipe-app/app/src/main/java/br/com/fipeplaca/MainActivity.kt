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
            TemaFipe(escuro = escuro) { AppRaiz() }
        }
    }
}
