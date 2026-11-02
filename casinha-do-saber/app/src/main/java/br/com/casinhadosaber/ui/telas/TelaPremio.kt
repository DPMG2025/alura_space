package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.IconeDoItem
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Estrela
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay

/**
 * Comemoração ao vencer uma fase. Na primeira vitória, um presente aparece:
 * a criança toca para abrir e descobre a peça nova da casa.
 */
@Composable
fun TelaPremio(
    fase: Fase,
    estrelas: Int,
    item: ItemCasa?,
    perfil: Perfil?,
    temProxima: Boolean,
    onColocarNaCasa: () -> Unit,
    onProxima: () -> Unit,
    onVoltar: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var estrelasVisiveis by remember { mutableIntStateOf(0) }
    var aberto by remember { mutableStateOf(false) }
    var confete by remember { mutableIntStateOf(0) }
    val nome = perfil?.nome.orEmpty()
    val vibracao = LocalHapticFeedback.current

    fun abrir() {
        if (aberto || item == null) return
        aberto = true
        confete++
        vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
        sons.premio()
        val ultima = item == ItensDaCasa.ordem.last()
        narrador.falar(
            listOf(pt("Você ganhou ${item.comArtigo}! Em inglês:"), en(item.nomeEn)) +
                if (ultima) listOf(pt("Sua casa está completa! Você é um campeão!")) else listOf(pt("Vamos colocar na sua casa?")),
        )
    }

    LaunchedEffect(Unit) {
        sons.acerto()
        val parabens = if (nome.isBlank()) "Parabéns! Você terminou a fase!" else "Parabéns, $nome! Você terminou a fase!"
        narrador.falar(if (item != null) "$parabens Toque no presente para abrir!" else parabens)
        for (i in 1..estrelas) {
            delay(380)
            estrelasVisiveis = i
            sons.toque()
        }
        if (item == null) confete++
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(fase.trilha.corArgb), Cores.Fundo))),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (nome.isBlank()) "Parabéns!" else "Parabéns, $nome!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..3) {
                    val escala = remember { Animatable(0.6f) }
                    LaunchedEffect(estrelasVisiveis >= i) {
                        if (estrelasVisiveis >= i) {
                            escala.animateTo(1.3f, tween(140))
                            escala.animateTo(1f, spring(dampingRatio = 0.4f))
                        }
                    }
                    Estrela(
                        estrelasVisiveis >= i,
                        Modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value },
                        tamanho = 64.dp,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            if (item != null) {
                Text(
                    if (aberto) "Você ganhou:" else "Um presente para a sua casa!",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(8.dp))
                Presente(item, aberto, onAbrir = ::abrir)
                if (aberto) {
                    Text(item.nome, style = MaterialTheme.typography.headlineLarge)
                    Text("Em inglês: ${item.nomeEn}", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
                }
            } else {
                perfil?.let { Crianca(it, Modifier.height(170.dp), acenando = true) }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Você já ganhou o presente desta fase.\nJogue uma fase nova para ganhar mais!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))
            AnimatedVisibility(visible = item == null || aberto, enter = fadeIn() + scaleIn()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (item != null) {
                        BotaoFofo(onClick = onColocarNaCasa, cor = Cores.Certo, modifier = Modifier.pulsando(), descricao = "Colocar na casa") {
                            Text(
                                "🏠 Colocar na casa",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotaoFofo(onClick = onVoltar, cor = Color.White, corBase = Color(0xFFD9CFF0), descricao = "Voltar para o caminho") {
                            Text("↩ Caminho", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
                        }
                        if (temProxima) {
                            BotaoFofo(onClick = onProxima, cor = Color(fase.trilha.corArgb), corBase = Color(fase.trilha.corEscuraArgb), descricao = "Próxima fase") {
                                Text(
                                    "Próxima ▶",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        Confete(confete)
    }
}

@Composable
private fun Presente(item: ItemCasa, aberto: Boolean, onAbrir: () -> Unit) {
    val escala = remember { Animatable(1f) }
    LaunchedEffect(aberto) {
        if (aberto) {
            escala.snapTo(0.2f)
            escala.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
        }
    }
    Box(
        Modifier
            .size(190.dp)
            .background(Color.White.copy(alpha = 0.85f), CircleShape)
            .semantics { contentDescription = if (aberto) item.nome else "Presente. Toque para abrir." }
            .clickable(enabled = !aberto, role = Role.Button, onClick = onAbrir),
        contentAlignment = Alignment.Center,
    ) {
        if (aberto) {
            IconeDoItem(item, 130.dp, Modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value })
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎁", style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp, lineHeight = 108.sp), modifier = Modifier.pulsando())
                Text("Toque para abrir!", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    Spacer(Modifier.fillMaxWidth().height(8.dp))
}
