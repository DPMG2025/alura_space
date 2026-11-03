package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import br.com.casinhadosaber.dominio.EstourarBaloes
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Stable
private class Balao(val indice: Int, val opcao: Opcao, /** Posição em [EstourarBaloes.certos], ou -1. */ val ordem: Int, val cor: Color) {
    var estourado by mutableStateOf(false)
    /** 0 a 1: o estouro (cresce, some e solta estrelinhas). */
    val estouro = Animatable(0f)
    val tremida = Animatable(0f)
    // Onde estourou (gravado no toque; o balão fica parado ali enquanto some).
    var paradoX = 0f
    var paradoY = 0f
}

/** Subida pré calculada: cada balão tem a sua coluna, o seu lugar na fila da coluna e o tamanho da volta. */
private class Revoada(n: Int) {
    val colunaX = FloatArray(n)
    val inicio = FloatArray(n)
    val volta = FloatArray(n)
    val periodo = FloatArray(n)

    /** Topo do balão: sobe do pé da tela até sumir no alto e volta por baixo. */
    fun topo(i: Int, t: Float, h: Float): Float {
        val f = inicio[i] + t / periodo[i]
        return h - (f - floor(f)) * volta[i]
    }

    fun centroX(i: Int, t: Float, balanco: Float) = colunaX[i] + balanco * sin(0.8f * t + i * 1.3f)
}

private val CeuAlto = Color(0xFF8FD3FF)
private val CeuBaixo = Color(0xFFDDF3FF)

/**
 * Estourar balões: os balões sobem devagar, cada um com uma resposta, e quem chega no alto volta por
 * baixo. Tocar num certo estoura (pof) e a resposta vai para a barra; com [EstourarBaloes.emOrdem] só
 * vale o próximo da lista. Balão errado (ou fora da ordem) só balança, diz o nome e conta um erro.
 */
@Composable
internal fun AtividadeBaloes(
    estourarBaloes: EstourarBaloes,
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

    val certos = estourarBaloes.certos
    val emOrdem = estourarBaloes.emOrdem
    val baloes = remember(estourarBaloes) {
        estourarBaloes.baloes.mapIndexed { i, o -> Balao(i, o, certos.indexOf(i), N1Paleta[(i * 5 + 2) % N1Paleta.size]) }
    }
    var erros by remember(estourarBaloes) { mutableIntStateOf(0) }
    var terminou by remember(estourarBaloes) { mutableStateOf(false) }
    var interacoes by remember(estourarBaloes) { mutableIntStateOf(0) }
    val achados = remember(estourarBaloes) { mutableStateOf(listOf<Opcao>()) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = n1Parada(interacoes, ativa, terminou, ajuda, ajudaInicial)
    val relogio = n1Relogio()

    /** O balão que vale agora: na ordem, só o próximo; sem ordem, qualquer certo ainda inteiro. */
    fun vale(b: Balao): Boolean =
        b.ordem >= 0 && !b.estourado && (!emOrdem || b.ordem == achados.value.size)

    BoxWithConstraints(modifier.fillMaxSize().clipToBounds()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val d = densidade.density
        val n = baloes.size
        val margem = 8f * d

        // ---------------------------------------------------------------- medidas e trajetórias (pixels)
        val alturaBarra = 72f * d
        var largura = min(w / 3.3f, 128f * d).coerceAtLeast(84f * d)
        val colunas = ((w - 2 * margem) / (largura * 1.12f)).toInt().coerceIn(2, 4).coerceAtMost(n)
        largura = min(largura, (w - 2 * margem) / (colunas * 1.08f))
        val alturaOval = largura * 1.18f
        val alturaCaixa = largura * 1.6f
        val revoada = remember(w, h, n) {
            Revoada(n).also { r ->
                val porColuna = IntArray(colunas)
                for (i in 0 until n) porColuna[i % colunas]++
                val passo = (w - 2 * margem) / colunas
                for (i in 0 until n) {
                    val c = i % colunas
                    val k = i / colunas
                    val m = porColuna[c]
                    r.colunaX[i] = margem + passo * (c + 0.5f)
                    r.volta[i] = max(h + alturaCaixa, m * alturaCaixa * 1.15f)
                    // Bem devagar: uns 40 dp por segundo.
                    r.periodo[i] = r.volta[i] / (40f * d)
                    val f = (k + 0.5f) / m + c * 0.23f / m
                    r.inicio[i] = f - floor(f)
                }
            }
        }
        val balanco = 9f * d

        fun xDo(b: Balao, t: Float) = if (b.estourado) b.paradoX else revoada.centroX(b.indice, t, balanco)
        fun yDo(b: Balao, t: Float) = if (b.estourado) b.paradoY else revoada.topo(b.indice, t, h)

        fun tocar(b: Balao) {
            if (!ativaAgora || terminou || b.estourado) return
            interacoes++
            if (vale(b)) {
                val t = relogio.floatValue
                b.paradoX = xDo(b, t)
                b.paradoY = yDo(b, t)
                b.estourado = true
                achados.value = achados.value + b.opcao
                sons.pegar()
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                narrador.falar(n1Nome(b.opcao))
                val acabou = achados.value.size == certos.size
                if (acabou) terminou = true
                escopo.launch {
                    b.estouro.animateTo(1f, tween(420))
                    if (acabou) {
                        n1Esperar(500)
                        concluir(erros == 0)
                    }
                }
            } else {
                // Balão errado ou fora da ordem: só balança, diz o nome e continua subindo.
                erros++
                avisarErro()
                sons.erro()
                val nome = n1Nome(b.opcao)
                narrador.falar(if (emOrdem && b.ordem >= 0) nome + pt("Ainda não!") else nome + falaAtual)
                escopo.launch { for (x in listOf(14f, -14f, 10f, -10f, 5f, -3f, 0f)) b.tremida.animateTo(x, tween(80)) }
            }
        }

        // ---------------------------------------------------------------- céu
        val ceu = remember(h) { Brush.verticalGradient(listOf(CeuAlto, CeuBaixo)) }
        Canvas(Modifier.matchParentSize()) {
            drawRect(ceu)
            val t = relogio.floatValue
            // Nuvens que andam bem devagar e voltam pelo outro lado.
            for (k in 0 until 3) {
                val faixa = w + 160f * d
                val x = ((k * 0.37f * faixa + t * (6f + 3f * k) * d) % faixa) - 80f * d
                val y = h * (0.22f + 0.27f * k)
                desenharNuvem(Offset(x, y), (22f + 6f * k) * d)
            }
            // Morrinhos embaixo.
            drawCircle(Color(0xFF9BD77E), w * 0.55f, Offset(w * 0.15f, h + w * 0.42f))
            drawCircle(Color(0xFF7CC56B), w * 0.6f, Offset(w * 0.9f, h + w * 0.47f))
        }

        // ---------------------------------------------------------------- balões
        val ladoX = with(densidade) { largura.toDp() }
        val ladoY = with(densidade) { alturaCaixa.toDp() }
        val ladoOval = with(densidade) { alturaOval.toDp() }
        for (b in baloes) {
            if (b.estouro.value >= 1f) continue
            val destacar = ajuda && vale(b) && !terminou
            Box(
                Modifier
                    .zIndex(if (b.estourado) 3f else 1f)
                    .offset {
                        val t = relogio.floatValue
                        IntOffset((xDo(b, t) - largura / 2f).roundToInt(), yDo(b, t).roundToInt())
                    }
                    .size(ladoX, ladoY)
                    .graphicsLayer {
                        val p = b.estouro.value
                        scaleX = 1f + 0.4f * p
                        scaleY = 1f + 0.4f * p
                        alpha = 1f - p
                        rotationZ = b.tremida.value
                        transformOrigin = TransformOrigin(0.5f, alturaOval / alturaCaixa)
                    }
                    .testTag(
                        when {
                            b.estourado -> "balao_estourado"
                            b.ordem >= 0 -> "balao_certo_${b.ordem}"
                            else -> "balao_errado"
                        },
                    )
                    .semantics {
                        contentDescription = "Balão: " + n1Descricao(b.opcao)
                        role = Role.Button
                        onClick { tocar(b); true }
                    }
                    .pointerInput(b, w, h) { detectTapGestures(onPress = { tocar(b) }) },
            ) {
                if (destacar) {
                    Canvas(Modifier.size(ladoX, ladoOval)) {
                        n1Brilho(center, largura * 0.5f, relogio.floatValue)
                    }
                }
                Canvas(Modifier.matchParentSize()) { desenharBalao(b.cor, alturaOval) }
                Box(Modifier.size(ladoX, ladoOval), contentAlignment = Alignment.Center) {
                    N1Figura(b.opcao, ladoX * 0.56f)
                }
            }
        }

        // Estrelinhas dos estouros (por cima de tudo, só enquanto o estouro anda).
        Canvas(Modifier.matchParentSize().zIndex(4f)) {
            for (b in baloes) {
                val p = b.estouro.value
                if (p > 0f && p < 1f) n1Estouro(Offset(b.paradoX, b.paradoY + alturaOval / 2f), largura * 0.5f, p, b.cor)
            }
        }

        // ---------------------------------------------------------------- barra do que já estourou
        Box(
            Modifier
                .zIndex(5f)
                .offset { IntOffset(0, (6f * d).roundToInt()) }
                .size(with(densidade) { w.toDp() }, with(densidade) { alturaBarra.toDp() }),
            contentAlignment = Alignment.TopCenter,
        ) {
            N1BarraDeAchados(certos.size, achados.value, escurecer(cor), with(densidade) { (w - 2 * margem).toDp() })
        }

        // ---------------------------------------------------------------- mãozinha: segue o balão que vale e mostra o toque
        val temAlvo = baloes.any { vale(it) }
        if (temAlvo && ativa && !terminou && parada) {
            val meio = 20f * d
            Box(
                Modifier
                    .zIndex(6f)
                    .offset {
                        // O balão que vale mais perto do meio da tela (se houver vários, a mão escolhe o mais fácil).
                        val t = relogio.floatValue
                        var melhor = -1
                        var distancia = Float.MAX_VALUE
                        for (b in baloes) {
                            if (!vale(b)) continue
                            val dist = abs(yDo(b, t) + alturaOval / 2f - h * 0.55f)
                            if (dist < distancia) {
                                distancia = dist
                                melhor = b.indice
                            }
                        }
                        val b = baloes[melhor.coerceAtLeast(0)]
                        IntOffset((xDo(b, t) - meio).roundToInt(), (yDo(b, t) + alturaOval / 2f - meio).roundToInt())
                    },
            ) { MaoGuia(ativa = true, espera = 0L) }
        }
    }
}

/** Balão com brilho, nó e barbante, ocupando a largura toda; o oval tem [alturaOval] de altura. */
private fun DrawScope.desenharBalao(cor: Color, alturaOval: Float) {
    val w = size.width
    val h = size.height
    val escura = escurecer(cor, 0.8f)
    // Barbante em zigue zague suave (linhas soltas).
    val x0 = w / 2f
    val y0 = alturaOval + w * 0.06f
    val passo = (h - y0) / 4f
    for (k in 0 until 4) {
        val xa = x0 + (if (k % 2 == 0) 0f else w * 0.05f)
        val xb = x0 + (if (k % 2 == 0) w * 0.05f else 0f)
        drawLine(Color(0xFF8A7F99), Offset(xa, y0 + passo * k), Offset(xb, y0 + passo * (k + 1)), w * 0.02f, cap = StrokeCap.Round)
    }
    // Nó
    val no = Path().apply {
        moveTo(x0, alturaOval * 0.97f)
        lineTo(x0 - w * 0.07f, alturaOval + w * 0.07f)
        lineTo(x0 + w * 0.07f, alturaOval + w * 0.07f)
        close()
    }
    drawPath(no, escura)
    // Corpo, sombra e brilho
    drawOval(escura, Offset(w * 0.03f, 0f), Size(w * 0.94f, alturaOval))
    drawOval(cor, Offset(w * 0.03f, 0f), Size(w * 0.88f, alturaOval * 0.96f))
    drawOval(Color.White.copy(alpha = 0.45f), Offset(w * 0.18f, alturaOval * 0.08f), Size(w * 0.2f, alturaOval * 0.22f))
    // Plaquinha para a resposta
    drawCircle(Color.White.copy(alpha = 0.9f), w * 0.33f, Offset(w / 2f, alturaOval / 2f))
}

private fun DrawScope.desenharNuvem(c: Offset, r: Float) {
    val branco = Color.White.copy(alpha = 0.9f)
    drawCircle(branco, r, c)
    drawCircle(branco, r * 0.8f, c + Offset(-r * 1.1f, r * 0.25f))
    drawCircle(branco, r * 0.85f, c + Offset(r * 1.1f, r * 0.2f))
    drawCircle(branco, r * 0.7f, c + Offset(r * 0.4f, -r * 0.6f))
}
