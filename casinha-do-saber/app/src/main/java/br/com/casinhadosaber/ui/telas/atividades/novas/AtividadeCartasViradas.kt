package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.CartasViradas
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
private class CartaVirada(val id: Int, val opcao: Opcao, /** Posição na ordem (ou -1 se não entra). */ val naOrdem: Int) {
    var achada by mutableStateOf(false)
    /** De 0 (na mesa) a 1 (na fila de cima). */
    val voo = Animatable(0f)
    val tremida = Animatable(0f)
}

/**
 * Cartas viradas em ordem: as cartas ficam abertas um instante (espiadinha) e viram para baixo. A
 * criança vira uma por vez procurando a próxima da ordem (1, 2, 3; A, B, C). A certa fica aberta e
 * voa para a fila de cima; a outra fica aberta um instante (para lembrar onde está), diz o próprio nome,
 * vira de volta e conta um erro. Sem relógio. Depois de 2 erros (ou no exemplo) a carta da vez brilha.
 *
 * Marcas de teste: "virada_ordem_<k>" (a k-ésima da ordem) e "virada_outra".
 */
@Composable
internal fun AtividadeCartasViradas(
    cartasViradas: CartasViradas,
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
    val densidade = LocalDensity.current
    val escopo = rememberCoroutineScope()
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    val ordem = cartasViradas.ordem
    val cartas = remember(cartasViradas) {
        cartasViradas.cartas.mapIndexed { i, o -> CartaVirada(i, o, ordem.indexOf(i)) }
    }
    var espiando by remember { mutableStateOf(true) }
    var aberta by remember { mutableIntStateOf(-1) }
    var pos by remember { mutableIntStateOf(0) }
    var erros by remember { mutableIntStateOf(0) }
    var toques by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var fechar by remember { mutableStateOf<Job?>(null) }
    val festa = remember { Animatable(0f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val daVez = ordem.getOrNull(pos)

    // Espiadinha: todas abertas no começo, depois viram.
    LaunchedEffect(cartasViradas) {
        delay((1600L + cartas.size * 220L).coerceAtMost(4200L))
        espiando = false
        sons.toque()
    }

    fun virar(c: CartaVirada) {
        if (!ativa || espiando || terminou || c.achada || c.id == aberta) return
        fechar?.cancel()
        toques++
        sons.toque()
        if (c.id == daVez) {
            aberta = -1
            c.achada = true
            pos++
            val ultima = pos == ordem.size
            if (ultima) terminou = true
            narrador.falar(falaDaOpcaoN4(c.opcao))
            escopo.launch {
                delay(380L)
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                c.voo.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
                if (ultima) {
                    festa.animateTo(1f, tween(900))
                    concluir(erros == 0)
                }
            }
        } else {
            aberta = c.id
            erros++
            avisarErro()
            sons.erro()
            val dica = if (erros >= ERROS_PARA_AJUDA) pt("Olhe a carta que está brilhando.") else pt("Não é essa. Lembre onde ela está!")
            narrador.falar(falaDaOpcaoN4(c.opcao) + dica)
            escopo.launch { for (x in listOf(8f, -8f, 5f, -5f, 0f)) c.tremida.animateTo(x, tween(60)) }
            fechar = escopo.launch {
                delay(1500L)
                if (aberta == c.id) aberta = -1
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density
        fun Float.paraDp(): Dp = with(densidade) { this@paraDp.toDp() }

        // ---------------------------------------------------------------- fila de cima
        val k = ordem.size
        val vaoFila = px(6f)
        val larguraFila = min((w - vaoFila * (k - 1)) / k, px(70f))
        val alturaFila = larguraFila * 1.22f
        val filaX = (w - (larguraFila * k + vaoFila * (k - 1))) / 2f
        val filaY = px(4f)
        fun naFila(i: Int) = Offset(filaX + i * (larguraFila + vaoFila), filaY)

        // ---------------------------------------------------------------- mesa (escolhe as colunas que deixam as cartas maiores)
        val n = cartas.size
        val vao = px(10f)
        val mesaY0 = filaY + alturaFila + px(20f)
        val alturaMesa = h - mesaY0 - px(4f)
        var colunas = 2
        var larguraCarta = 0f
        for (c in 2..5) {
            val linhas = (n + c - 1) / c
            val porLargura = (w - vao * (c - 1)) / c
            val porAltura = ((alturaMesa - vao * (linhas - 1)) / linhas) / 1.22f
            val lado = min(porLargura, porAltura)
            if (lado > larguraCarta) {
                larguraCarta = lado
                colunas = c
            }
        }
        larguraCarta = larguraCarta.coerceIn(px(64f), px(150f))
        val alturaCarta = larguraCarta * 1.22f
        val linhas = (n + colunas - 1) / colunas
        val altura = linhas * alturaCarta + (linhas - 1) * vao
        val mesaY = mesaY0 + max(0f, (alturaMesa - altura) / 2f)
        fun naMesa(c: CartaVirada): Offset {
            val linha = c.id / colunas
            val naLinha = min(colunas, n - linha * colunas)
            val x0 = (w - (naLinha * larguraCarta + (naLinha - 1) * vao)) / 2f
            return Offset(x0 + (c.id % colunas) * (larguraCarta + vao), mesaY + linha * (alturaCarta + vao))
        }

        // Casinhas da fila (a da vez pulsa com um "?").
        for (i in 0 until k) {
            val daVezNaFila = i == pos && !terminou
            Box(
                Modifier
                    .offset { naFila(i).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } }
                    .size(larguraFila.paraDp(), alturaFila.paraDp())
                    .then(if (daVezNaFila && ativa && !espiando) Modifier.pulsando() else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = CornerRadius(12.dp.toPx())
                    drawRoundRect(Color.White.copy(alpha = if (daVezNaFila) 0.95f else 0.55f), cornerRadius = r)
                    drawRoundRect(
                        if (daVezNaFila) cor else Color(0xFFD9CFF0),
                        cornerRadius = r,
                        style = Stroke(3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))),
                    )
                }
                if (daVezNaFila) {
                    if (ajuda) {
                        Box(Modifier.graphicsLayer { alpha = 0.3f }) { VisualN4(cartas[ordem[i]].opcao.visual, larguraFila.paraDp() * 0.9f) }
                    } else {
                        Text("?", fontSize = (larguraFila.paraDp().value * 0.5f).sp, fontWeight = FontWeight.Bold, color = escurecer(cor))
                    }
                }
            }
        }

        // ---------------------------------------------------------------- cartas
        for (c in cartas) {
            val mesa = naMesa(c)
            val noAr = c.achada && c.voo.value > 0f
            // Lugar vazio na mesa depois que a carta voa.
            if (c.achada) {
                Canvas(
                    Modifier
                        .offset { IntOffset(mesa.x.roundToInt(), mesa.y.roundToInt()) }
                        .size(larguraCarta.paraDp(), alturaCarta.paraDp()),
                ) {
                    drawRoundRect(
                        Color.White.copy(alpha = 0.5f),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx()))),
                    )
                }
            }
            val destino = if (c.naOrdem >= 0) naFila(c.naOrdem) else mesa
            val f = c.voo.value
            val largura = larguraCarta + (larguraFila - larguraCarta) * f
            val alturaAgora = alturaCarta + (alturaFila - alturaCarta) * f
            val brilhar = ajuda && !terminou && !espiando && c.id == daVez && aberta != c.id
            val onda = if (festa.value > 0f && c.achada) {
                val fase = festa.value * 2f - c.naOrdem * (1f / k)
                if (fase in 0f..1f) kotlin.math.sin(fase * PI.toFloat()) else 0f
            } else 0f
            Box(
                Modifier
                    .zIndex(if (noAr) 5f else 1f)
                    .offset {
                        val q = mesa + (destino - mesa) * f
                        IntOffset(q.x.roundToInt(), (q.y - onda * px(12f)).roundToInt())
                    }
                    .size(largura.paraDp(), alturaAgora.paraDp()),
            ) {
                CartaN4(
                    carta = c,
                    aberta = espiando || c.achada || aberta == c.id,
                    brilhar = brilhar,
                    cor = cor,
                    largura = largura.paraDp(),
                    onToque = { virar(c) },
                )
                if (c.id == daVez && !terminou) {
                    MaoGuia(
                        Modifier.align(Alignment.Center),
                        ativa = ativa && !espiando && aberta < 0 && (brilhar || (toques == 0 && pos == 0)),
                        espera = if (brilhar) 1200L else 4500L,
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaN4(carta: CartaVirada, aberta: Boolean, brilhar: Boolean, cor: Color, largura: Dp, onToque: () -> Unit) {
    val giro by animateFloatAsState(if (aberta) 1f else 0f, tween(320), label = "giro")
    val mostrandoFrente = giro > 0.5f
    val forma = RoundedCornerShape(if (carta.achada) 12.dp else 16.dp)
    Box(
        Modifier
            .fillMaxSize()
            .testTag(if (carta.naOrdem >= 0) "virada_ordem_${carta.naOrdem}" else "virada_outra")
            .semantics { contentDescription = if (mostrandoFrente) descricaoN4(carta.opcao) else "Carta virada" }
            .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = onToque)
            .brilhoN4(brilhar, 16.dp)
            .graphicsLayer {
                scaleX = abs(cos(giro * PI.toFloat())).coerceAtLeast(0.02f)
                rotationZ = carta.tremida.value
            }
            .shadow(if (carta.achada) 2.dp else 4.dp, forma)
            .background(
                when {
                    !mostrandoFrente -> cor
                    carta.achada -> Color(0xFFDFF7E7)
                    else -> Color.White
                },
                forma,
            )
            .border(
                if (brilhar) 5.dp else 3.dp,
                when {
                    brilhar -> Cores.Estrela
                    !mostrandoFrente -> escurecer(cor)
                    carta.achada -> Cores.Certo
                    else -> Color(0xFFEDE6FF)
                },
                forma,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (mostrandoFrente) {
            VisualN4(carta.opcao.visual, largura * 0.9f)
        } else {
            // Verso: losangos claros e um ponto de interrogação desenhados.
            Canvas(Modifier.fillMaxSize()) {
                val passo = size.width / 4f
                for (i in -1..5) for (j in -1..6) {
                    if ((i + j) % 2 != 0) continue
                    val c = Offset(i * passo, j * passo)
                    drawCircle(Color.White.copy(alpha = 0.12f), passo * 0.32f, c)
                }
                drawRoundRect(
                    Color.White.copy(alpha = 0.5f),
                    topLeft = Offset(size.width * 0.1f, size.height * 0.08f),
                    size = Size(size.width * 0.8f, size.height * 0.84f),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(2.dp.toPx()),
                )
            }
            Text("?", fontSize = (largura.value * 0.42f).sp, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.displayLarge)
        }
    }
}
