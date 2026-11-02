package br.com.fipeplaca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.fipeplaca.ui.AppRaiz
import br.com.fipeplaca.ui.TemaFipe

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TemaFipe { AppRaiz() } }
    }
}
