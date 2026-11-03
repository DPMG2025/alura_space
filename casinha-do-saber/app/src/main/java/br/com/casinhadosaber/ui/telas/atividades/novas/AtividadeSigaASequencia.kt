package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.SigaASequencia
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Siga a sequência (Simon de sons e cores): os botões acendem e tocam a [SigaASequencia.sequencia]
 * devagar (cada um com o seu som e o nome falado); depois é a vez da criança repetir, no tempo dela.
 *
 * Acerto: repetir a sequência inteira na ordem. Erro: tocar fora da ordem; o botão treme, o narrador
 * diz o nome do que foi tocado, conta um erro e o jogo mostra a sequência de novo (a criança recomeça
 * a repetição, nunca o jogo). Enquanto a sequência é mostrada, os toques não contam. O botão 👀 mostra
 * de novo quando quiser, sem contar erro. Depois de 2 erros (ou no exemplo) o próximo botão brilha.
 *
 * Marcas de teste: "seq_botao_<i>" (i = índice em botoes) e "seq_repetir".
 */
@Composable
internal fun AtividadeSigaASequencia(
    sigaASequencia: SigaASequencia,
    fala: List<Trecho>,
    ativa: Boolean,
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val vibracao = LocalHapticFeedback.current
    val escopo = rememberCoroutineScope()
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    val botoes = sigaASequencia.botoes
    val sequencia = sigaASequencia.sequencia
    val cores = remember(sigaASequencia) { sigaASequencia.cores.map { Color(it) } }

    var aceso by remember { mutableIntStateOf(-1) }
    var mostrando by remember { mutableStateOf(true) }
    var rodada by remember { mutableIntStateOf(0) }
    var pos by remember { mutableIntStateOf(0) }
    var erros by remember { mutableIntStateOf(0) }
    var toques by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    val tremidas = remember(sigaASequencia) { botoes.map { Animatable(0f) } }
    val festa = remember { Animatable(0f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val esperado = sequencia.getOrNull(pos)

    // Mostra a sequência: cada botão acende, toca o seu som e diz o seu nome.
    LaunchedEffect(rodada, ativa) {
        if (!ativa || terminou) return@LaunchedEffect
        mostrando = true
        aceso = -1
        // A primeira vez espera a pergunta ser lida; nas outras, a fala do erro.
        delay(if (rodada == 0) 600L else 300L)
        esperarNarradorN4(narrador, 7000L)
        delay(350L)
        val passo = if (sequencia.size >= 6) 820L else 950L
        for (i in sequencia) {
            aceso = i
            sons.piu(i)
            falaDaOpcaoN4(botoes[i]).takeIf { it.isNotEmpty() }?.let { narrador.falar(it) }
            delay(passo)
            aceso = -1
            delay(260L)
        }
        mostrando = false
        narrador.falar("Sua vez!")
    }

    fun tocar(i: Int) {
        if (!ativa || mostrando || terminou) return
        toques++
        val certo = esperado
        escopo.launch {
            aceso = i
            delay(260L)
            if (aceso == i) aceso = -1
        }
        if (i == certo) {
            sons.piu(i)
            vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            pos++
            if (pos == sequencia.size) {
                terminou = true
                narrador.falar(falaDaOpcaoN4(botoes[i]))
                escopo.launch {
                    delay(300L)
                    festa.animateTo(1f, tween(900))
                    concluir(erros == 0)
                }
            } else {
                falaDaOpcaoN4(botoes[i]).takeIf { it.isNotEmpty() }?.let { narrador.falar(it) }
            }
        } else {
            erros++
            avisarErro()
            sons.erro()
            escopo.launch {
                for (x in listOf(12f, -12f, 8f, -8f, 4f, 0f)) tremidas[i].animateTo(x, tween(55))
            }
            narrador.falar(falaDaOpcaoN4(botoes[i]) + pt("Ops! Olhe de novo."))
            pos = 0
            mostrando = true
            escopo.launch {
                delay(900L)
                rodada++
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        val n = botoes.size
        val altoDoTopo = 76.dp
        val vao = 16.dp
        val colunas = if (n == 2 && maxHeight > maxWidth * 1.15f) 1 else 2
        val linhas = (n + colunas - 1) / colunas
        val lado = minOf(
            (maxWidth - vao * (colunas - 1)) / colunas,
            (maxHeight - altoDoTopo - vao * linhas) / linhas,
            210.dp,
        ).coerceAtLeast(72.dp)

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Topo: bolinhas de quantos toques faltam e o botão de ver de novo.
            Row(
                Modifier.fillMaxWidth().height(altoDoTopo),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ProgressoN4(sequencia.size, if (terminou) sequencia.size else pos, mostrando, cor)
                Box(
                    Modifier
                        .size(64.dp)
                        .testTag("seq_repetir")
                        .semantics { contentDescription = "Ver a sequência de novo" }
                        .clickable(
                            remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            enabled = ativa && !mostrando && !terminou,
                        ) {
                            sons.toque()
                            pos = 0
                            mostrando = true
                            rodada++
                        }
                        .graphicsLayer { alpha = if (mostrando || terminou) 0.45f else 1f }
                        .background(Color.White, CircleShape)
                        .border(3.dp, cor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🔁", fontSize = 30.sp)
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(verticalArrangement = Arrangement.spacedBy(vao), horizontalAlignment = Alignment.CenterHorizontally) {
                    for (linha in botoes.indices.chunked(colunas)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(vao)) {
                            for (i in linha) {
                                val destacar = ajuda && !mostrando && !terminou && i == esperado
                                Box {
                                    BotaoDeSom(
                                        indice = i,
                                        descricao = descricaoN4(botoes[i]),
                                        conteudo = { VisualN4(botoes[i].visual, lado * 0.42f) },
                                        corBotao = cores[i],
                                        lado = lado,
                                        aceso = aceso == i || (festa.value > 0f && festa.value < 1f && ((festa.value * n * 2f).toInt() % n) == i),
                                        apagado = mostrando && aceso != i,
                                        destacar = destacar,
                                        tremida = tremidas[i].value,
                                        onToque = { tocar(i) },
                                    )
                                    // Mãozinha: na primeira vez mostra o gesto de tocar; com ajuda, o botão da vez.
                                    if (i == esperado && !terminou) {
                                        MaoGuia(
                                            Modifier.align(Alignment.Center),
                                            ativa = ativa && !mostrando && (destacar || (toques == 0 && pos == 0)),
                                            espera = if (destacar) 1200L else 4000L,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Bolinhas da sequência: cheias as que a criança já repetiu; enquanto mostra, um olhinho. */
@Composable
private fun ProgressoN4(total: Int, feitos: Int, mostrando: Boolean, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(44.dp).background(if (mostrando) Cores.Estrela else Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (mostrando) "👀" else "👆", fontSize = 22.sp)
        }
        Canvas(Modifier.size(width = (18 * total + 6 * (total - 1)).dp, height = 20.dp)) {
            val r = 9.dp.toPx()
            for (k in 0 until total) {
                val c = Offset(r + k * (2 * r + 6.dp.toPx()), size.height / 2f)
                if (k < feitos) {
                    drawCircle(Cores.Certo, r, c)
                    drawCircle(Color.White.copy(alpha = 0.5f), r * 0.35f, c + Offset(-r * 0.3f, -r * 0.3f))
                } else {
                    drawCircle(Color.White, r, c)
                    drawCircle(escurecer(cor, 0.9f).copy(alpha = 0.6f), r, c, style = Stroke(2.5f.dp.toPx()))
                }
            }
        }
    }
}

/**
 * Botão grande e redondinho, desenhado em Canvas: base mais escura (efeito 3D), brilho de cima e
 * figura num círculo branco. Aceso, ele clareia, cresce um pouco e solta um halo.
 */
@Composable
private fun BotaoDeSom(
    indice: Int,
    descricao: String,
    conteudo: @Composable () -> Unit,
    corBotao: Color,
    lado: Dp,
    aceso: Boolean,
    apagado: Boolean,
    destacar: Boolean,
    tremida: Float,
    onToque: () -> Unit,
) {
    val luz by animateFloatAsState(if (aceso) 1f else 0f, tween(if (aceso) 90 else 260), label = "luz")
    val escala by animateFloatAsState(if (aceso) 1.07f else 1f, spring(dampingRatio = 0.45f), label = "escala")
    Box(
        Modifier
            .size(lado)
            .testTag("seq_botao_$indice")
            .semantics { contentDescription = descricao }
            .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = onToque)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
                translationX = tremida * density
                alpha = if (apagado) 0.82f else 1f
            }
            .brilhoN4(destacar, raio = lado * 0.22f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val raio = size.minDimension * 0.22f
            val queda = size.minDimension * 0.06f
            // Halo de luz quando aceso.
            if (luz > 0f) {
                drawRoundRect(
                    corBotao.copy(alpha = 0.35f * luz),
                    topLeft = Offset(-12.dp.toPx() * luz, -12.dp.toPx() * luz),
                    size = Size(size.width + 24.dp.toPx() * luz, size.height + 24.dp.toPx() * luz),
                    cornerRadius = CornerRadius(raio * 1.3f),
                )
            }
            // Base (sombra 3D).
            drawRoundRect(escurecer(corBotao, 0.7f), topLeft = Offset(0f, queda), size = Size(size.width, size.height - queda), cornerRadius = CornerRadius(raio))
            // Face do botão: clareia quando acende.
            val clara = lerpCor(corBotao, Color.White, 0.18f + 0.42f * luz)
            drawRoundRect(
                Brush.verticalGradient(listOf(clara, lerpCor(corBotao, Color.White, 0.25f * luz))),
                size = Size(size.width, size.height - queda),
                cornerRadius = CornerRadius(raio),
            )
            // Reflexo em cima.
            drawRoundRect(
                Color.White.copy(alpha = 0.28f + 0.3f * luz),
                topLeft = Offset(size.width * 0.12f, size.height * 0.07f),
                size = Size(size.width * 0.76f, size.height * 0.12f),
                cornerRadius = CornerRadius(size.height * 0.06f),
            )
            // Círculo branco da figura.
            val c = Offset(size.width / 2f, (size.height - queda) / 2f)
            drawCircle(Color.White.copy(alpha = 0.92f), size.minDimension * 0.31f, c)
            if (luz > 0f) {
                drawCircle(Color.White.copy(alpha = 0.8f * luz), size.minDimension * (0.33f + 0.08f * luz), c, style = Stroke(5.dp.toPx()))
            }
        }
        Box(Modifier.padding(bottom = lado * 0.06f), contentAlignment = Alignment.Center) { conteudo() }
    }
}

private fun lerpCor(a: Color, b: Color, t: Float) = Color(
    a.red + (b.red - a.red) * t,
    a.green + (b.green - a.green) * t,
    a.blue + (b.blue - a.blue) * t,
    1f,
)
