package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.MaoGuia
import androidx.compose.foundation.border
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
    moedas: Int = 0,
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
        narrador.falar(if (item != null) "$parabens Toque no presente três vezes para abrir!" else parabens)
        for (i in 1..estrelas) {
            delay(380)
            estrelasVisiveis = i
            sons.toque()
        }
        if (item == null) confete++
        if (moedas > 0) {
            delay(250)
            sons.moeda()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(fase.trilha.corArgb), Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
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
            if (moedas > 0) {
                Spacer(Modifier.height(6.dp))
                Text("+$moedas 🪙", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }
            Spacer(Modifier.height(16.dp))

            if (item != null) {
                Text(
                    if (aberto) "Você ganhou:" else "Um presente para a sua casa!",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(8.dp))
                Presente(item, aberto, onToque = { sons.chacoalhar(); vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove) }, onAbrir = ::abrir)
                if (aberto) {
                    Text(item.nome, style = MaterialTheme.typography.headlineLarge)
                    Text("Em inglês: ${item.nomeEn}", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
                }
                perfil?.let {
                    Spacer(Modifier.height(12.dp))
                    Crianca(it, Modifier.height(if (aberto) 110.dp else 160.dp), acenando = true)
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
                                modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        BotaoRedondo("🗺️", "Voltar para o caminho", onVoltar, tamanho = 60.dp)
                        if (temProxima) {
                            if (item == null) {
                                BotaoFofo(onClick = onProxima, cor = Color(fase.trilha.corArgb), corBase = Color(fase.trilha.corEscuraArgb), modifier = Modifier.pulsando(), descricao = "Próxima fase") {
                                    Text("Próxima ▶", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp))
                                }
                            } else {
                                BotaoRedondo("▶️", "Próxima fase", onProxima, tamanho = 60.dp)
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
private fun Presente(item: ItemCasa, aberto: Boolean, onToque: () -> Unit, onAbrir: () -> Unit) {
    var toques by remember { mutableIntStateOf(0) }
    val escala = remember { Animatable(1f) }
    val giro = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()
    val raios = rememberInfiniteTransition(label = "raios")
    val anguloRaios by raios.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "angulo")
    // Mesmo sem tocar, a caixa dá pulinhos chamando a criança.
    val chamar by raios.animateFloat(-4f, 4f, infiniteRepeatable(tween(260), RepeatMode.Reverse), label = "chamar")

    LaunchedEffect(aberto) {
        if (aberto) {
            escala.snapTo(0.2f)
            escala.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
        }
    }

    fun tocar() {
        if (aberto) return
        toques++
        if (toques >= 3) {
            onAbrir()
            return
        }
        onToque()
        escopo.launch {
            val forca = 8f * toques
            for (x in listOf(forca, -forca, forca * 0.6f, -forca * 0.6f, 0f)) giro.animateTo(x, tween(60))
        }
        escopo.launch { escala.animateTo(1f + 0.12f * toques, spring(dampingRatio = 0.4f)) }
    }

    Box(
        Modifier
            .size(230.dp)
            .semantics { contentDescription = if (aberto) item.nome else "Presente. Toque para abrir." }
            .clickable(enabled = !aberto, role = Role.Button, onClick = ::tocar),
        contentAlignment = Alignment.Center,
    ) {
        if (aberto) {
            Canvas(Modifier.fillMaxSize()) {
                val c = center
                val r = size.minDimension / 2
                rotate(anguloRaios, c) {
                    for (k in 0 until 12) {
                        val a1 = Math.toRadians(k * 30.0)
                        val a2 = Math.toRadians(k * 30.0 + 14)
                        val raio = Path().apply {
                            moveTo(c.x, c.y)
                            lineTo(c.x + (cos(a1) * r).toFloat(), c.y + (sin(a1) * r).toFloat())
                            lineTo(c.x + (cos(a2) * r).toFloat(), c.y + (sin(a2) * r).toFloat())
                            close()
                        }
                        drawPath(raio, Color(0x66FFE680))
                    }
                }
                drawCircle(Color.White.copy(alpha = 0.9f), r * 0.68f, c)
            }
            IconeDoItem(item, 130.dp, Modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value })
        } else {
            MaoGuia(Modifier.align(Alignment.BottomEnd), espera = 1200)
            Box(
                Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🎁",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp, lineHeight = 108.sp),
                    modifier = Modifier.graphicsLayer {
                        rotationZ = giro.value + if (toques == 0) chamar else 0f
                        scaleX = escala.value
                        scaleY = escala.value
                    },
                )
                // Três bolinhas: cada toque acende uma (sem depender de leitura).
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { k ->
                        Box(
                            Modifier
                                .size(18.dp)
                                .background(if (k < toques) Cores.Estrela else Color.White, CircleShape)
                                .border(3.dp, Color(0xFFE0A800), CircleShape),
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.fillMaxWidth().height(8.dp))
}
