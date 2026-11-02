package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.PortaoDeAdulto
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Fim do tempo do dia: a casinha vai dormir até amanhã (ou até um adulto liberar). */
@Composable
fun TelaDormir(estado: EstadoJogo, onLiberar: () -> Unit) {
    val narrador = LocalNarrador.current

    LaunchedEffect(Unit) {
        narrador.falar(listOf(pt("Que dia divertido! Agora a casinha vai dormir. Até amanhã!"), en("Good night!")))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF16204A))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Boa noite! 🌙", style = MaterialTheme.typography.displayMedium, color = Color.White)
        Spacer(Modifier.height(12.dp))
        CenaDaCasa(estado, Modifier.clip(RoundedCornerShape(24.dp)).weight(1f, fill = false), noite = true)
        Spacer(Modifier.height(16.dp))
        Text(
            "A casinha volta amanhã! 💤",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        // Só um adulto libera (continha de multiplicação). Sem isso, a casinha acorda sozinha amanhã.
        var mostrarPortao by remember { mutableStateOf(false) }
        if (mostrarPortao) {
            PortaoDeAdulto(onLiberar = onLiberar)
        } else {
            Text(
                "🔒 Adulto",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0x33FFFFFF))
                    .clickable { mostrarPortao = true }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}
