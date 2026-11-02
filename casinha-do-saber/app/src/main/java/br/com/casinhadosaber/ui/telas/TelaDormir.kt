package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import kotlinx.coroutines.launch

/**
 * Fim do tempo combinado: a casinha vai dormir. Só um adulto libera,
 * segurando o botão por 3 segundos (difícil para uma criança pequena fazer sem querer).
 */
@Composable
fun TelaDormir(estado: EstadoJogo, onLiberar: () -> Unit) {
    val narrador = LocalNarrador.current
    val carga = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()

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
            "Acabou o tempo de jogo de hoje.",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        // Botão de adulto: segurar 3 segundos.
        Box(
            Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0x33FFFFFF))
                .semantics { contentDescription = "Adulto: segure para liberar" }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        val enchendo = escopo.launch {
                            carga.animateTo(1f, tween(3000, easing = LinearEasing))
                            onLiberar()
                        }
                        waitForUpOrCancellation()
                        enchendo.cancel()
                        escopo.launch { carga.animateTo(0f, tween(200)) }
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth(carga.value)
                    .height(56.dp)
                    .background(Color(0x66FFE08A)),
            )
            Text(
                "Adulto: segure aqui para continuar",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
