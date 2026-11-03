package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Pescaria
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Stable
private class Peixe(val indice: Int, val opcao: Opcao, val certo: Boolean, val cor: Color) {
    /** Já foi pescado (está pulando para o balde ou já está nele). */
    var pescado by mutableStateOf(false)
    /** Fugindo depois de um toque errado (não aceita toque até voltar). */
    var fugindo by mutableStateOf(false)
    /** 0 a 1: o pulo do lago até o balde. */
    val pulo = Animatable(0f)
    /** 0 a 1: a fuga para fora do lago (e a volta, de 1 a 0). */
    val fuga = Animatable(0f)
    val tremida = Animatable(0f)
    // Onde o pulo começou e para que lado ele foge (gravados no toque; lidos no layout).
    var inicioX = 0f
    var inicioY = 0f
    var ladoDaFuga = 1f
}

/** Trajetória pré calculada de cada peixe: centro da faixa, ida e volta (seno) e balanço. */
private class Cardume(n: Int) {
    val baseX = FloatArray(n)
    val baseY = FloatArray(n)
    val amplitude = FloatArray(n)
    val omega = FloatArray(n)
    val fase = FloatArray(n)

    fun x(i: Int, t: Float) = baseX[i] + amplitude[i] * sin(omega[i] * t + fase[i])
    fun y(i: Int, t: Float, bob: Float) = baseY[i] + bob * sin(1.3f * t + i * 1.9f)

    /** Para onde o peixe está virado (-1 esquerda, 1 direita), suave na hora de virar. */
    fun virado(i: Int, t: Float): Float {
        if (amplitude[i] < 1f) return if (i % 2 == 0) 1f else -1f
        return (cos(omega[i] * t + fase[i]) * 3f).coerceIn(-1f, 1f)
    }
}

private val AguaClara = Color(0xFF9FE3F5)
private val AguaFunda = Color(0xFF3FA7D6)
private val Areia = Color(0xFFF3D9A4)
private val AreiaEscura = Color(0xFFDDB878)
private val GramaDaBeira = Color(0xFF7CC56B)
private val Folha = Color(0xFF5DBB63)
private val Balde = Color(0xFF5B8DEF)
private val BaldeEscuro = Color(0xFF3F6BC4)

/**
 * Pescaria: peixinhos nadam devagar, cada um com uma figura; tocar num peixe [Pescaria.certos] faz ele
 * pular para o balde. Peixe errado foge nadando, diz o nome dele e volta depois (conta um erro).
 * Termina quando todos os certos estão no balde.
 */
@Composable
internal fun AtividadePescaria(
    pescaria: Pescaria,
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
    val ativaAgora by rememberUpdatedState(ativa)
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)
    val falaAtual by rememberUpdatedState(fala)

    val peixes = remember(pescaria) {
        pescaria.peixes.mapIndexed { i, o -> Peixe(i, o, i in pescaria.certos, N1Paleta[(i * 4 + 1) % N1Paleta.size]) }
    }
    var erros by remember(pescaria) { mutableIntStateOf(0) }
    var terminou by remember(pescaria) { mutableStateOf(false) }
    var interacoes by remember(pescaria) { mutableIntStateOf(0) }
    val noBalde = remember(pescaria) { mutableStateOf(listOf<Opcao>()) }
    val pulinhoDoBalde = remember { Animatable(1f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = n1Parada(interacoes, ativa, terminou, ajuda, ajudaInicial)
    val relogio = n1Relogio()

    BoxWithConstraints(modifier.fillMaxSize().clipToBounds()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val d = densidade.density
        val n = peixes.size

        // ---------------------------------------------------------------- medidas (pixels)
        val praia = max(96f * d, h * 0.2f)
        val lagoTopo = 4f * d
        val lagoBase = h - praia
        val alturaLago = lagoBase - lagoTopo
        val margem = 6f * d
        var largura = 0f
        var faixas = 1
        for (f in 1..n) {
            val porFaixa = ceil(n / f.toFloat()).toInt()
            val l = min(min((w - 2 * margem) / (porFaixa * 1.16f), (alturaLago / f) / 0.8f), 156f * d)
            if (l > largura + 0.5f) {
                largura = l
                faixas = f
            }
        }
        val altura = largura * 0.7f
        val cardume = remember(w, h, n) {
            Cardume(n).also { c ->
                for (faixa in 0 until faixas) {
                    val membros = (0 until n).filter { it % faixas == faixa }
                    val m = membros.size
                    val vao = largura * 0.16f
                    val grupo = m * largura + (m - 1) * vao
                    val amp = max(0f, (w - 2 * margem - grupo) / 2f)
                    // Devagar: no máximo uns 45 dp por segundo, cada faixa num ritmo.
                    val periodo = max(10f + faixa * 1.7f, (2f * PI.toFloat() * amp) / (45f * d))
                    val y = lagoTopo + alturaLago * (faixa + 0.5f) / faixas
                    membros.forEachIndexed { k, i ->
                        c.baseX[i] = (w - grupo) / 2f + k * (largura + vao) + largura / 2f
                        c.baseY[i] = y
                        c.amplitude[i] = amp
                        c.omega[i] = 2f * PI.toFloat() / periodo
                        c.fase[i] = faixa * 2.1f + 0.6f
                    }
                }
            }
        }
        val baldeLado = min(praia * 0.86f, 104f * d)
        val baldeX = margem + 4f * d
        val baldeY = h - baldeLado - 4f * d
        val bocaDoBalde = Offset(baldeX + baldeLado / 2f, baldeY + baldeLado * 0.22f)
        val bob = 5f * d

        fun centroX(p: Peixe, t: Float): Float {
            val i = p.indice
            val nadando = cardume.x(i, t) + p.ladoDaFuga * p.fuga.value * w * 0.8f
            val e = p.pulo.value
            if (e <= 0f) return nadando
            return p.inicioX + (bocaDoBalde.x - p.inicioX) * FastOutSlowInEasing.transform(e)
        }

        fun centroY(p: Peixe, t: Float): Float {
            val e = p.pulo.value
            if (e <= 0f) return cardume.y(p.indice, t, bob)
            return p.inicioY + (bocaDoBalde.y - p.inicioY) * e - sin(PI.toFloat() * e) * alturaLago * 0.35f
        }

        fun tocar(p: Peixe) {
            if (!ativaAgora || terminou || p.pescado || p.fugindo) return
            interacoes++
            val t = relogio.floatValue
            if (p.certo) {
                p.pescado = true
                p.inicioX = centroX(p, t)
                p.inicioY = centroY(p, t)
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                narrador.falar(n1Nome(p.opcao))
                val acabou = peixes.all { !it.certo || it.pescado }
                if (acabou) terminou = true
                escopo.launch {
                    p.pulo.animateTo(1f, tween(800, easing = LinearOutSlowInEasing))
                    noBalde.value = noBalde.value + p.opcao
                    sons.pousar()
                    pulinhoDoBalde.snapTo(1.18f)
                    pulinhoDoBalde.animateTo(1f, spring(dampingRatio = 0.4f))
                    if (acabou) {
                        n1Esperar(350)
                        concluir(erros == 0)
                    }
                }
            } else {
                // Peixe errado: diz o nome, treme e foge nadando para o lado em que olha; volta depois.
                erros++
                avisarErro()
                sons.erro()
                narrador.falar(n1Nome(p.opcao) + falaAtual)
                p.fugindo = true
                p.ladoDaFuga = if (cardume.virado(p.indice, t) >= 0f) 1f else -1f
                escopo.launch {
                    for (x in listOf(12f, -12f, 9f, -9f, 4f, 0f)) p.tremida.animateTo(x, tween(55))
                    p.fuga.animateTo(1f, tween(900, easing = FastOutLinearInEasing))
                    n1Esperar(1500)
                    p.fuga.animateTo(0f, tween(1800, easing = LinearOutSlowInEasing))
                    p.fugindo = false
                }
            }
        }

        // ---------------------------------------------------------------- lago e beira
        val agua = remember(h) { Brush.verticalGradient(listOf(AguaClara, AguaFunda), startY = lagoTopo, endY = lagoBase) }
        val tracoOnda = remember(d) { Stroke(2.5f * d, cap = StrokeCap.Round) }
        Canvas(Modifier.matchParentSize()) {
            drawRect(agua, topLeft = Offset(0f, 0f), size = Size(w, lagoBase + 12f * d))
            val t = relogio.floatValue
            // Ondinhas que andam devagar (arcos soltos, sem caminho novo a cada quadro).
            val onda = 34f * d
            for (linha in 0 until 5) {
                val y = lagoTopo + alturaLago * (linha + 0.3f) / 5f
                val desliza = ((t * 9f * d * (if (linha % 2 == 0) 1f else -1f)) % (onda * 2f) + onda * 2f) % (onda * 2f)
                var x = -onda * 2f + desliza + (linha % 2) * onda
                while (x < w + onda) {
                    drawArc(
                        Color.White.copy(alpha = 0.35f),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(x, y),
                        size = Size(onda, onda * 0.4f),
                        style = tracoOnda,
                    )
                    x += onda * 2.6f
                }
            }
            // Vitórias régias nos cantos.
            desenharFolhaDeLirio(Offset(w * 0.93f, lagoTopo + alturaLago * 0.1f), 26f * d)
            desenharFolhaDeLirio(Offset(w * 0.06f, lagoTopo + alturaLago * 0.55f), 20f * d)
            // Beira: areia com grama.
            val beira = lagoBase
            drawRect(GramaDaBeira, Offset(0f, beira), Size(w, 14f * d))
            drawRect(Areia, Offset(0f, beira + 10f * d), Size(w, h - beira))
            for (k in 0 until 14) {
                val x = w * (k + 0.5f) / 14f
                drawCircle(AreiaEscura, 2.5f * d, Offset(x, beira + 30f * d + (k % 3) * 14f * d))
                drawLine(Folha, Offset(x, beira + 12f * d), Offset(x - 4f * d, beira + 2f * d), 3f * d, cap = StrokeCap.Round)
                drawLine(Folha, Offset(x + 3f * d, beira + 12f * d), Offset(x + 6f * d, beira), 3f * d, cap = StrokeCap.Round)
            }
            // Juncos na direita da beira.
            for (k in 0 until 3) {
                val x = w - 22f * d - k * 12f * d
                drawLine(Color(0xFF4E9A52), Offset(x, beira + 14f * d), Offset(x + 2f * d, beira - 46f * d + k * 10f * d), 4f * d, cap = StrokeCap.Round)
                drawOval(Color(0xFF8D5B3A), Offset(x - 3f * d, beira - 50f * d + k * 10f * d), Size(9f * d, 22f * d))
            }
        }

        // ---------------------------------------------------------------- balde e barra do que já foi pescado
        Box(
            Modifier
                .offset { IntOffset(baldeX.roundToInt(), baldeY.roundToInt()) }
                .size(with(densidade) { baldeLado.toDp() })
                .graphicsLayer {
                    scaleX = pulinhoDoBalde.value
                    scaleY = pulinhoDoBalde.value
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                }
                .testTag("balde")
                .zIndex(1f),
        ) {
            Canvas(Modifier.matchParentSize()) { desenharBalde(noBalde.value.size) }
        }
        val larguraBarra = w - (baldeX + baldeLado + 12f * d) - margem
        if (larguraBarra > 60f * d) {
            Box(
                Modifier
                    .offset {
                        IntOffset((baldeX + baldeLado + 12f * d).roundToInt(), (baldeY + baldeLado * 0.25f).roundToInt())
                    }
                    .zIndex(1f),
            ) {
                N1BarraDeAchados(pescaria.certos.size, noBalde.value, escurecer(cor), with(densidade) { larguraBarra.toDp() })
            }
        }

        // ---------------------------------------------------------------- peixes
        val ladoX = with(densidade) { largura.toDp() }
        val ladoY = with(densidade) { altura.toDp() }
        for (p in peixes) {
            if (p.pulo.value >= 1f) continue
            val destacar = ajuda && p.certo && !p.pescado && !terminou
            Box(
                Modifier
                    .zIndex(if (p.pescado) 3f else 2f)
                    .offset {
                        val t = relogio.floatValue
                        IntOffset((centroX(p, t) - largura / 2f).roundToInt(), (centroY(p, t) - altura / 2f).roundToInt())
                    }
                    .size(ladoX, ladoY)
                    .graphicsLayer {
                        val e = p.pulo.value
                        val escala = 1f - 0.55f * e
                        scaleX = escala
                        scaleY = escala
                        rotationZ = p.tremida.value + (if (e > 0f) -25f * sin(PI.toFloat() * e) else 0f)
                        alpha = 1f - 0.55f * p.fuga.value
                    }
                    .testTag(
                        when {
                            p.pescado -> "peixe_pescado"
                            p.certo -> "peixe_certo"
                            else -> "peixe_errado"
                        },
                    )
                    .semantics {
                        contentDescription = "Peixe: " + n1Descricao(p.opcao)
                        role = Role.Button
                        onClick { tocar(p); true }
                    }
                    .pointerInput(p, w, h) { detectTapGestures(onPress = { tocar(p) }) },
            ) {
                Canvas(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = if (p.fuga.value > 0f) p.ladoDaFuga else cardume.virado(p.indice, relogio.floatValue)
                        },
                ) {
                    desenharPeixe(p.cor)
                }
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(ladoY)
                        .graphicsLayer {
                            val v = if (p.fuga.value > 0f) p.ladoDaFuga else cardume.virado(p.indice, relogio.floatValue)
                            translationX = largura * 0.11f * v
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    // O anel dourado fica numa camada só dele: o peixe não é redesenhado a cada quadro.
                    if (destacar) {
                        Canvas(Modifier.matchParentSize()) {
                            n1Brilho(center, altura * 0.38f, relogio.floatValue)
                            drawCircle(Color.White, altura * 0.36f, center)
                        }
                    }
                    N1Figura(p.opcao, ladoY * 0.62f)
                }
            }
        }

        // ---------------------------------------------------------------- mãozinha: mostra o toque no peixe
        val alvo = peixes.firstOrNull { it.certo && !it.pescado && !it.fugindo }
        if (alvo != null && ativa && !terminou && (parada || ajudaInicial && interacoes == 0)) {
            val meio = 20f * d
            Box(
                Modifier
                    .zIndex(5f)
                    .offset {
                        val t = relogio.floatValue
                        IntOffset((centroX(alvo, t) - meio).roundToInt(), (centroY(alvo, t) - meio).roundToInt())
                    },
            ) { MaoGuia(ativa = true, espera = 0L) }
        }
    }
}

/** Peixe virado para a direita, ocupando o retângulo todo, com a plaquinha branca da figura. */
private fun DrawScope.desenharPeixe(cor: Color) {
    val w = size.width
    val h = size.height
    val escura = escurecer(cor, 0.8f)
    // Rabo
    val rabo = Path().apply {
        moveTo(w * 0.3f, h * 0.5f)
        quadraticTo(w * 0.1f, h * 0.2f, w * 0.01f, h * 0.14f)
        quadraticTo(w * 0.1f, h * 0.5f, w * 0.01f, h * 0.86f)
        quadraticTo(w * 0.1f, h * 0.8f, w * 0.3f, h * 0.5f)
        close()
    }
    drawPath(rabo, escura)
    // Barbatana de cima
    val barbatana = Path().apply {
        moveTo(w * 0.42f, h * 0.16f)
        quadraticTo(w * 0.55f, -h * 0.06f, w * 0.74f, h * 0.14f)
        close()
    }
    drawPath(barbatana, escura)
    // Corpo e barriga
    drawOval(cor, Offset(w * 0.2f, h * 0.06f), Size(w * 0.8f, h * 0.88f))
    drawOval(Color.White.copy(alpha = 0.25f), Offset(w * 0.3f, h * 0.5f), Size(w * 0.58f, h * 0.38f))
    // Listrinhas perto do rabo
    for (k in 0 until 2) {
        val x = w * (0.3f + k * 0.06f)
        drawArc(escura.copy(alpha = 0.6f), -55f, 110f, false, Offset(x - w * 0.06f, h * 0.24f), Size(w * 0.12f, h * 0.52f), style = Stroke(h * 0.05f, cap = StrokeCap.Round))
    }
    // Barbatana do lado (meio escondida pela plaquinha)
    drawOval(escura.copy(alpha = 0.7f), Offset(w * 0.3f, h * 0.62f), Size(w * 0.14f, h * 0.2f))
    // Plaquinha branca da figura
    val centro = Offset(w * 0.61f, h * 0.5f)
    val raio = h * 0.36f
    drawCircle(Color.White, raio, centro)
    drawCircle(escura.copy(alpha = 0.35f), raio, centro, style = Stroke(h * 0.035f))
    // Olho e sorriso
    val olho = Offset(w * 0.9f, h * 0.38f)
    drawCircle(Color.White, h * 0.085f, olho)
    drawCircle(Color(0xFF2B2140), h * 0.05f, olho + Offset(w * 0.008f, 0f))
    drawCircle(Color.White, h * 0.018f, olho + Offset(w * 0.016f, -h * 0.02f))
    drawArc(Color(0xFF2B2140), 20f, 100f, false, Offset(w * 0.88f, h * 0.5f), Size(w * 0.08f, h * 0.14f), style = Stroke(h * 0.03f, cap = StrokeCap.Round))
}

private fun DrawScope.desenharFolhaDeLirio(c: Offset, r: Float) {
    drawArc(Color(0xFF66BB6A), 20f, 320f, true, c - Offset(r, r * 0.6f), Size(r * 2f, r * 1.2f))
    drawArc(Color(0xFF43A047), 20f, 320f, true, c - Offset(r, r * 0.6f), Size(r * 2f, r * 1.2f), style = Stroke(r * 0.08f))
    drawCircle(Color(0xFFFF8FB8), r * 0.22f, c + Offset(-r * 0.3f, -r * 0.1f))
    drawCircle(Color(0xFFFFE082), r * 0.09f, c + Offset(-r * 0.3f, -r * 0.1f))
}

/** Balde azul com alça; [peixes] rabinhos aparecem saindo pela boca. */
private fun DrawScope.desenharBalde(peixes: Int) {
    val w = size.width
    val h = size.height
    // Alça
    drawArc(Color(0xFF8A8FA3), 180f, 180f, false, Offset(w * 0.14f, h * 0.0f), Size(w * 0.72f, h * 0.5f), style = Stroke(w * 0.05f, cap = StrokeCap.Round))
    // Rabinhos dos peixes pescados
    for (k in 0 until min(peixes, 5)) {
        val x = w * (0.3f + 0.1f * k)
        val c = N1Paleta[(k * 4 + 1) % N1Paleta.size]
        drawOval(c, Offset(x - w * 0.06f, h * 0.1f), Size(w * 0.12f, h * 0.2f))
    }
    val corpo = Path().apply {
        moveTo(w * 0.1f, h * 0.24f)
        lineTo(w * 0.9f, h * 0.24f)
        lineTo(w * 0.8f, h * 0.98f)
        lineTo(w * 0.2f, h * 0.98f)
        close()
    }
    drawPath(corpo, Balde)
    drawRect(BaldeEscuro, Offset(w * 0.13f, h * 0.55f), Size(w * 0.74f, h * 0.06f))
    drawOval(BaldeEscuro, Offset(w * 0.08f, h * 0.17f), Size(w * 0.84f, h * 0.14f))
    drawOval(Color(0xFF2E4F94), Offset(w * 0.12f, h * 0.19f), Size(w * 0.76f, h * 0.1f))
    drawLine(Color.White.copy(alpha = 0.45f), Offset(w * 0.24f, h * 0.36f), Offset(w * 0.28f, h * 0.88f), w * 0.05f, cap = StrokeCap.Round)
    // Peixinho desenhado na frente
    drawOval(Color(0xFFFFFFFF).copy(alpha = 0.85f), Offset(w * 0.42f, h * 0.68f), Size(w * 0.26f, h * 0.14f))
    val rabo = Path().apply {
        moveTo(w * 0.43f, h * 0.75f)
        lineTo(w * 0.34f, h * 0.68f)
        lineTo(w * 0.34f, h * 0.82f)
        close()
    }
    drawPath(rabo, Color.White.copy(alpha = 0.85f))
}
