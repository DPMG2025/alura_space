package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay

/**
 * Boas-vindas logo depois de escolher o personagem: a criança chega andando ao terreno vazio,
 * onde a casinha vai nascer, e o narrador explica o objetivo. Um toque pula a cena.
 */
@Composable
fun TelaBoasVindas(estado: EstadoJogo, onFim: () -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val perfil = estado.perfil ?: return onFim()
    val andar = remember { Animatable(0f) }
    val passos = rememberInfiniteTransition(label = "passos")
    val bob by passos.animateFloat(0f, 1f, infiniteRepeatable(tween(220), RepeatMode.Reverse), label = "bob")

    LaunchedEffect(Unit) {
        val oi = if (perfil.nome.isBlank()) "Oi!" else "Oi, ${perfil.nome}!"
        narrador.falar("$oi Este é o seu terreno. Vamos construir a sua casinha? Jogue nos caminhos para ganhar as peças!")
        andar.animateTo(1f, tween(2600, easing = LinearEasing))
        sons.pousar()
        delay(4200)
        onFim()
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Cores.Ceu)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onFim() },
    ) {
        // O terreno vazio (sem o personagem: ele entra andando por cima).
        CenaDaCasa(estado.copy(perfil = null), Modifier.fillMaxSize(), preencherAltura = true)
        val andando = andar.value < 1f
        Crianca(
            perfil,
            Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = maxHeight * 0.06f)
                .offset(x = (maxWidth * 0.5f - 60.dp) * andar.value - 20.dp)
                .height(200.dp)
                .graphicsLayer { translationY = if (andando) -10f * bob * density else 0f },
            acenando = !andando,
        )
        Text(
            "Vamos construir a sua casinha!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = Cores.Texto,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}
