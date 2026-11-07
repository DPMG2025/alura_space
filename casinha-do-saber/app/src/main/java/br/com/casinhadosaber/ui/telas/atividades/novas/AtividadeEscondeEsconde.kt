package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.EscondeEsconde
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.FiguraDaPergunta
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.painelDaCena
import kotlin.math.cos
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** O que sai de um esconderijo vazio (sempre algo engraçado, nunca um susto). */
private val Surpresas = listOf("🦋", "🐸", "🐞", "🐌", "🐤", "🐝")

private enum class Momento { INICIO, ACENANDO, ESCONDENDO, TROCANDO, TOCAR, FIM }

@Stable
private class Esconderijo(val indice: Int, val opcao: Opcao, val certo: Boolean) {
    val surpresa = Surpresas[indice % Surpresas.size]
    /** Troca de lugar: vai da casinha [de] para [para]; [arco] 1 passa por cima, -1 por baixo. */
    var de by mutableIntStateOf(indice)
    var para by mutableIntStateOf(indice)
    var arco = 1f
    val troca = Animatable(1f)
    var aberto by mutableStateOf(false)
    /** 0 a 1: o esconderijo levanta e inclina (abre). */
    val abertura = Animatable(0f)
    /** 0 a 1: a surpresa sai voando. */
    val saida = Animatable(0f)
    val tremida = Animatable(0f)
}

/** Jeito dos esconderijos: caixas, moitas ou baldes (sempre iguais numa mesma pergunta). */
private enum class Tipo { CAIXA, MOITA, BALDE }

/**
 * Esconde esconde: o bicho acena e se esconde atrás de um esconderijo. Com trocas, os esconderijos trocam
 * de lugar devagar e a criança segue com o olho; sem trocas, a pista vem na fala. Tocar no
 * [EscondeEsconde.certo] faz o bicho pular de alegria; no errado sai uma surpresa engraçada e conta um erro.
 */
@Composable
internal fun AtividadeEscondeEsconde(
    escondeEsconde: EscondeEsconde,
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

    val n = escondeEsconde.esconderijos.size
    val lugares = remember(escondeEsconde) {
        escondeEsconde.esconderijos.mapIndexed { i, o -> Esconderijo(i, o, i == escondeEsconde.certo) }
    }
    val tipo = remember(escondeEsconde) {
        if (escondeEsconde.esconderijos.any { it.visual is Visual.Cor }) Tipo.CAIXA
        else Tipo.entries[abs(escondeEsconde.bicho.hashCode()) % Tipo.entries.size]
    }
    // As trocas são sorteadas uma vez (pares de casinhas); metade delas leva o esconderijo do bicho.
    val trocas = remember(escondeEsconde) {
        val r = Random(escondeEsconde.hashCode())
        val ocupante = IntArray(n) { it }
        List(escondeEsconde.trocas) { k ->
            val casaDoBicho = ocupante.indexOf(escondeEsconde.certo)
            val a = if (k % 2 == 0) casaDoBicho else r.nextInt(n)
            var b = r.nextInt(n - 1)
            if (b >= a) b++
            val tmp = ocupante[a]
            ocupante[a] = ocupante[b]
            ocupante[b] = tmp
            a to b
        }
    }
    var momento by remember(escondeEsconde) { mutableStateOf(Momento.INICIO) }
    var erros by remember(escondeEsconde) { mutableIntStateOf(0) }
    var interacoes by remember(escondeEsconde) { mutableIntStateOf(0) }
    val terminou = momento == Momento.FIM
    // O bicho: aceno (giro), salto até o esconderijo, descida para trás dele, subida (sem trocas) e pulos de alegria.
    val aceno = remember(escondeEsconde) { Animatable(0f) }
    val salto = remember(escondeEsconde) { Animatable(0f) }
    val desce = remember(escondeEsconde) { Animatable(0f) }
    val sobe = remember(escondeEsconde) { Animatable(0f) }
    val pulo = remember(escondeEsconde) { Animatable(0f) }
    val festa = remember(escondeEsconde) { Animatable(0f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val podeTocar = momento == Momento.TOCAR
    // Aqui a mãozinha nunca entrega a resposta logo de cara: só com ajuda ou depois de uma parada longa.
    val parada = n1Parada(interacoes, ativa && podeTocar, terminou, ajuda, ajudaInicial, primeira = 8000L)
    val relogio = n1Relogio()

    BoxWithConstraints(modifier.fillMaxSize().painelDaCena()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val d = densidade.density
        val margem = 8f * d

        // ---------------------------------------------------------------- medidas (pixels)
        val fileiras = if (n <= 4) 1 else 2
        val porFileira = ceil(n / fileiras.toFloat()).toInt()
        val topoDosLugares = h * 0.4f
        val alturaFileira = (h - topoDosLugares - margem) / fileiras
        val lado = min(min((w - 2 * margem) / (porFileira * 1.15f), alturaFileira / 1.12f), 150f * d)
        val casaX = remember(w, h, n) {
            FloatArray(n) { s ->
                val f = s / porFileira
                val naFileira = min(porFileira, n - f * porFileira)
                val passo = (w - 2 * margem) / naFileira
                margem + passo * (s - f * porFileira + 0.5f)
            }
        }
        val casaY = remember(w, h, n) { FloatArray(n) { s -> topoDosLugares + alturaFileira * (s / porFileira + 0.55f) } }
        val ladoBicho = min(min(lado * 0.82f, h * 0.26f), 116f * d)
        val inicioBicho = Offset(w / 2f, h * 0.2f)

        fun centroX(e: Esconderijo): Float {
            val p = FastOutSlowInEasing.transform(e.troca.value)
            return casaX[e.de] + (casaX[e.para] - casaX[e.de]) * p
        }

        fun centroY(e: Esconderijo): Float {
            val p = FastOutSlowInEasing.transform(e.troca.value)
            return casaY[e.de] + (casaY[e.para] - casaY[e.de]) * p - e.arco * sin(PI.toFloat() * p) * lado * 0.5f
        }

        val certo = lugares[escondeEsconde.certo]

        // ---------------------------------------------------------------- a brincadeira: acena, esconde e troca
        LaunchedEffect(escondeEsconde, ativa) {
            if (!ativa || momento != Momento.INICIO) return@LaunchedEffect
            momento = Momento.ACENANDO
            n1Esperar(500)
            sons.piu(1)
            repeat(3) {
                aceno.animateTo(16f, tween(220))
                aceno.animateTo(-16f, tween(220))
            }
            aceno.animateTo(0f, tween(160))
            momento = Momento.ESCONDENDO
            if (escondeEsconde.trocas > 0) {
                // A criança vê onde ele entra.
                salto.animateTo(1f, tween(1000, easing = LinearEasing))
                sons.pousar()
                desce.animateTo(1f, tween(500))
            } else {
                // Sem trocas: ele some num pulo e os esconderijos chacoalham; a pista vem na fala.
                sons.zum()
                sobe.animateTo(1f, tween(800))
                sons.chacoalhar()
                coroutineScope {
                    for (e in lugares) launch { for (x in listOf(8f, -8f, 6f, -6f, 3f, 0f)) e.tremida.animateTo(x, tween(70)) }
                }
            }
            n1Esperar(450)
            if (trocas.isNotEmpty()) {
                momento = Momento.TROCANDO
                for ((a, b) in trocas) {
                    val ea = lugares.first { it.para == a }
                    val eb = lugares.first { it.para == b }
                    ea.de = a; ea.para = b; ea.arco = 1f
                    eb.de = b; eb.para = a; eb.arco = -1f
                    ea.troca.snapTo(0f)
                    eb.troca.snapTo(0f)
                    sons.toque()
                    // Devagar, para o olho de quem tem 4 anos acompanhar.
                    coroutineScope {
                        launch { ea.troca.animateTo(1f, tween(1150, easing = LinearEasing)) }
                        launch { eb.troca.animateTo(1f, tween(1150, easing = LinearEasing)) }
                    }
                    ea.de = b
                    eb.de = a
                    n1Esperar(350)
                }
            }
            momento = Momento.TOCAR
            narrador.falar(falaAtual)
        }

        fun tocar(e: Esconderijo) {
            if (!ativaAgora || momento != Momento.TOCAR || e.aberto) return
            interacoes++
            e.aberto = true
            if (e.certo) {
                momento = Momento.FIM
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                narrador.falar(n1Nome(e.opcao) + pt("Achou!"))
                escopo.launch {
                    salto.snapTo(1f)
                    sobe.snapTo(0f)
                    desce.snapTo(1f)
                    launch { e.abertura.animateTo(1f, spring(dampingRatio = 0.5f)) }
                    desce.animateTo(0f, tween(450))
                    sons.piu(2)
                    launch { festa.animateTo(1f, tween(900)) }
                    repeat(3) {
                        pulo.animateTo(1f, tween(220))
                        pulo.animateTo(0f, tween(220))
                    }
                    n1Esperar(200)
                    concluir(erros == 0)
                }
            } else {
                // Esconderijo errado: sai uma surpresa engraçada e ele fica abertinho (não conta de novo).
                erros++
                avisarErro()
                sons.erro()
                narrador.falar(n1Nome(e.opcao) + pt("Aqui não!"))
                escopo.launch {
                    launch { e.saida.animateTo(1f, tween(1900, easing = LinearEasing)) }
                    launch { for (x in listOf(9f, -9f, 6f, -6f, 3f, 0f)) e.tremida.animateTo(x, tween(60)) }
                    e.abertura.animateTo(1f, spring(dampingRatio = 0.5f))
                    n1Esperar(900)
                    e.abertura.animateTo(0.2f, tween(400))
                }
            }
        }

        // ---------------------------------------------------------------- fundo: gramado com cerquinha
        val fundo = remember(h) { Brush.verticalGradient(listOf(Color(0xFF7CC4FA), Color(0xFFFFE6D6)), endY = h * 0.38f) }
        Canvas(Modifier.matchParentSize()) {
            drawRect(fundo)
            drawRect(Color(0xFFA9E56F), Offset(0f, h * 0.36f), Size(w, h))
            drawOval(Color(0xFF96D862), Offset(-w * 0.2f, h * 0.3f), Size(w * 0.8f, h * 0.16f))
            drawOval(Color(0xFF96D862), Offset(w * 0.45f, h * 0.31f), Size(w * 0.8f, h * 0.14f))
            // Cerquinha de madeira no horizonte.
            val passo = 26f * d
            var x = passo / 2f
            while (x < w) {
                drawRoundRect(Color(0xFFE6B98F), Offset(x - 5f * d, h * 0.3f), Size(10f * d, h * 0.08f), CornerRadius(4f * d))
                x += passo
            }
            drawRect(Color(0xFFD9A066), Offset(0f, h * 0.32f), Size(w, 5f * d))
            drawRect(Color(0xFFD9A066), Offset(0f, h * 0.355f), Size(w, 5f * d))
            // Chão de verdade: faixas de grama mais escura, tufinhos e florzinhas (a metade de baixo não fica vazia).
            drawOval(Color(0xFF8FD072), Offset(-w * 0.3f, h * 0.84f), Size(w * 0.9f, h * 0.3f))
            drawOval(Color(0xFF8FD072), Offset(w * 0.5f, h * 0.88f), Size(w * 0.8f, h * 0.3f))
            for (k in 0 until 16) {
                val fx = ((k * 37 + 11) % 100) / 100f
                val fy = if (k % 2 == 0) 0.45f + ((k * 13) % 10) / 100f else 0.86f + ((k * 7) % 10) / 100f
                val c = Offset(fx * w, fy * h)
                val folha = Color(0xFF6DB85A)
                drawLine(folha, c, c + Offset(-4f * d, -9f * d), 2.5f * d, cap = StrokeCap.Round)
                drawLine(folha, c, c + Offset(0f, -11f * d), 2.5f * d, cap = StrokeCap.Round)
                drawLine(folha, c, c + Offset(4f * d, -9f * d), 2.5f * d, cap = StrokeCap.Round)
                if (k % 3 == 0) {
                    val flor = c + Offset(9f * d, -6f * d)
                    val petala = if (k % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFFFB3D1)
                    for (p in 0 until 5) {
                        val a = p * 1.2566f
                        drawCircle(petala, 3.2f * d, flor + Offset(cos(a) * 3.6f * d, sin(a) * 3.6f * d))
                    }
                    drawCircle(Color(0xFFFFD54F), 2.4f * d, flor)
                }
            }
        }

        // ---------------------------------------------------------------- surpresas (atrás dos esconderijos)
        for (e in lugares) {
            if (e.certo || e.saida.value <= 0f || e.saida.value >= 1f) continue
            val ladoSurpresa = with(densidade) { (lado * 0.5f).toDp() }
            Box(
                Modifier
                    .zIndex(1f)
                    .offset {
                        val s = e.saida.value
                        val x = centroX(e) + sin(s * 14f) * lado * 0.18f
                        val y = centroY(e) - s * lado * 1.9f
                        IntOffset((x - lado * 0.25f).roundToInt(), (y - lado * 0.25f).roundToInt())
                    }
                    .size(ladoSurpresa)
                    .graphicsLayer { alpha = if (e.saida.value > 0.75f) (1f - e.saida.value) / 0.25f else 1f },
                contentAlignment = Alignment.Center,
            ) {
                FiguraDaPergunta(e.surpresa, (lado * 0.5f / d).dp) {
                    Text(e.surpresa, fontSize = (lado * 0.42f / d).sp, lineHeight = (lado * 0.48f / d).sp)
                }
            }
        }

        // ---------------------------------------------------------------- o bicho
        val ladoBichoDp = with(densidade) { ladoBicho.toDp() }
        Box(
            Modifier
                .zIndex(1f)
                .offset {
                    val s = salto.value
                    val acimaX = centroX(certo)
                    val acimaY = centroY(certo) - lado * 0.8f
                    val x = inicioBicho.x + (acimaX - inicioBicho.x) * s
                    var y = inicioBicho.y + (acimaY - inicioBicho.y) * s - sin(PI.toFloat() * s) * lado * 0.9f
                    y += lado * 0.8f * desce.value
                    y -= h * 0.5f * sobe.value
                    y -= lado * 0.55f * pulo.value
                    IntOffset((x - ladoBicho / 2f).roundToInt(), (y - ladoBicho / 2f).roundToInt())
                }
                .size(ladoBichoDp)
                .graphicsLayer {
                    alpha = (1f - desce.value * 1.4f).coerceIn(0f, 1f) * (1f - sobe.value)
                    rotationZ = aceno.value
                    transformOrigin = TransformOrigin(0.5f, 0.9f)
                    val amassa = pulo.value
                    scaleX = 1f + 0.08f * amassa
                    scaleY = 1f + 0.06f * amassa
                }
                .testTag("bicho"),
            contentAlignment = Alignment.Center,
        ) {
            FiguraDaPergunta(escondeEsconde.bicho, (ladoBicho * 0.96f / d).dp) {
                Text(escondeEsconde.bicho, fontSize = (ladoBicho * 0.8f / d).sp, lineHeight = (ladoBicho * 0.9f / d).sp)
            }
        }
        if (momento == Momento.ACENANDO) {
            Box(
                Modifier
                    .zIndex(1f)
                    .offset { IntOffset((inicioBicho.x + ladoBicho * 0.45f).roundToInt(), (inicioBicho.y - ladoBicho * 0.6f).roundToInt()) }
                    .graphicsLayer { rotationZ = aceno.value * 1.5f },
            ) { Text("👋", fontSize = (ladoBicho * 0.36f / d).sp) }
        }

        // ---------------------------------------------------------------- esconderijos
        val ladoDp = with(densidade) { lado.toDp() }
        for (e in lugares) {
            val destacar = ajuda && e.certo && podeTocar
            Box(
                Modifier
                    .zIndex(2f)
                    .offset { IntOffset((centroX(e) - lado / 2f).roundToInt(), (centroY(e) - lado / 2f).roundToInt()) }
                    .size(ladoDp)
                    .graphicsLayer {
                        val a = e.abertura.value
                        translationY = -a * lado * 0.42f
                        rotationZ = -12f * a
                        translationX = e.tremida.value * density
                        transformOrigin = TransformOrigin(0.15f, 1f)
                        alpha = if (e.aberto && !e.certo && e.saida.value >= 1f) 0.55f else 1f
                    }
                    .testTag(
                        when {
                            e.aberto && !e.certo -> "esconderijo_aberto"
                            e.certo -> "esconderijo_certo"
                            else -> "esconderijo_errado"
                        },
                    )
                    .semantics {
                        contentDescription = "Esconderijo: " + n1Descricao(e.opcao)
                        role = Role.Button
                        onClick { tocar(e); true }
                    }
                    .pointerInput(e, w, h) { detectTapGestures(onPress = { tocar(e) }) },
                contentAlignment = Alignment.Center,
            ) {
                if (destacar) {
                    Canvas(Modifier.matchParentSize()) { n1Brilho(center, size.minDimension * 0.56f, relogio.floatValue) }
                }
                val corDoLugar = (e.opcao.visual as? Visual.Cor)?.let { Color(it.argb) }
                Canvas(Modifier.matchParentSize()) {
                    when (tipo) {
                        Tipo.CAIXA -> desenharCaixa(corDoLugar ?: Color(0xFFD9A066))
                        Tipo.MOITA -> desenharMoitaGrande()
                        Tipo.BALDE -> desenharBaldeVirado(N1Paleta[(e.indice * 2 + 1) % N1Paleta.size])
                    }
                }
                if (corDoLugar == null) {
                    Box(
                        Modifier
                            .offset(y = ladoDp * 0.1f)
                            .size(ladoDp * 0.52f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.matchParentSize()) {
                            drawCircle(Color.White)
                            drawCircle(Color(0x22000000), style = Stroke(2f * d))
                        }
                        N1Figura(e.opcao, ladoDp * 0.46f)
                    }
                }
            }
        }

        // Estrelinhas quando o bicho aparece.
        if (festa.value > 0f && festa.value < 1f) {
            Canvas(Modifier.matchParentSize().zIndex(3f)) {
                n1Estouro(Offset(centroX(certo), centroY(certo) - lado * 0.8f), lado * 0.6f, festa.value, cor)
            }
        }

        // ---------------------------------------------------------------- mãozinha
        if (ativa && podeTocar && parada) {
            val meio = 20f * d
            Box(
                Modifier
                    .zIndex(5f)
                    .offset { IntOffset((centroX(certo) - meio).roundToInt(), (centroY(certo) - meio).roundToInt()) },
            ) { MaoGuia(ativa = true, espera = 0L) }
        }
    }
}

/** Caixa de papelão (ou da cor pedida) com abas, fita e sombra. */
private fun DrawScope.desenharCaixa(cor: Color) {
    val w = size.width
    val h = size.height
    val escura = escurecer(cor, 0.78f)
    drawOval(Color(0x33000000), Offset(w * 0.06f, h * 0.88f), Size(w * 0.88f, h * 0.12f))
    drawRoundRect(escura, Offset(w * 0.06f, h * 0.2f), Size(w * 0.88f, h * 0.74f), CornerRadius(w * 0.06f))
    drawRoundRect(cor, Offset(w * 0.06f, h * 0.2f), Size(w * 0.8f, h * 0.7f), CornerRadius(w * 0.06f))
    // Abas abertas em cima
    val abaE = Path().apply {
        moveTo(w * 0.08f, h * 0.22f)
        lineTo(w * 0.0f, h * 0.08f)
        lineTo(w * 0.4f, h * 0.1f)
        lineTo(w * 0.5f, h * 0.22f)
        close()
    }
    val abaD = Path().apply {
        moveTo(w * 0.5f, h * 0.22f)
        lineTo(w * 0.6f, h * 0.1f)
        lineTo(w * 1.0f, h * 0.08f)
        lineTo(w * 0.92f, h * 0.22f)
        close()
    }
    drawPath(abaE, escura)
    drawPath(abaD, escurecer(cor, 0.88f))
    drawRect(Color.White.copy(alpha = 0.18f), Offset(w * 0.12f, h * 0.26f), Size(w * 0.08f, h * 0.58f))
}

/** Moita redonda e cheia, grande o bastante para esconder um bicho. */
private fun DrawScope.desenharMoitaGrande() {
    val w = size.width
    val h = size.height
    val cor = Color(0xFF5DBB63)
    val escura = Color(0xFF3F9A47)
    drawOval(Color(0x33000000), Offset(w * 0.06f, h * 0.86f), Size(w * 0.88f, h * 0.12f))
    drawCircle(escura, w * 0.26f, Offset(w * 0.26f, h * 0.62f))
    drawCircle(escura, w * 0.26f, Offset(w * 0.74f, h * 0.62f))
    drawCircle(cor, w * 0.3f, Offset(w * 0.5f, h * 0.48f))
    drawCircle(cor, w * 0.24f, Offset(w * 0.28f, h * 0.6f))
    drawCircle(cor, w * 0.24f, Offset(w * 0.72f, h * 0.6f))
    drawRect(cor, Offset(w * 0.1f, h * 0.62f), Size(w * 0.8f, h * 0.26f))
    drawCircle(Color.White.copy(alpha = 0.2f), w * 0.1f, Offset(w * 0.4f, h * 0.32f))
    for (k in 0 until 3) drawCircle(Color(0xFFFF8FB8), w * 0.035f, Offset(w * (0.25f + 0.25f * k), h * (0.4f + 0.1f * (k % 2))))
}

/** Balde virado de boca para baixo, com alça. */
private fun DrawScope.desenharBaldeVirado(cor: Color) {
    val w = size.width
    val h = size.height
    val escura = escurecer(cor, 0.78f)
    drawOval(Color(0x33000000), Offset(w * 0.06f, h * 0.86f), Size(w * 0.88f, h * 0.12f))
    drawArc(Color(0xFF8A8FA3), 200f, 140f, false, Offset(w * 0.2f, h * 0.1f), Size(w * 0.6f, h * 0.4f), style = Stroke(w * 0.04f, cap = StrokeCap.Round))
    val corpo = Path().apply {
        moveTo(w * 0.24f, h * 0.18f)
        lineTo(w * 0.76f, h * 0.18f)
        lineTo(w * 0.92f, h * 0.9f)
        lineTo(w * 0.08f, h * 0.9f)
        close()
    }
    drawPath(corpo, cor)
    drawRoundRect(escura, Offset(w * 0.05f, h * 0.84f), Size(w * 0.9f, h * 0.09f), CornerRadius(w * 0.04f))
    drawRoundRect(escura, Offset(w * 0.22f, h * 0.15f), Size(w * 0.56f, h * 0.07f), CornerRadius(w * 0.03f))
    drawLine(Color.White.copy(alpha = 0.35f), Offset(w * 0.3f, h * 0.26f), Offset(w * 0.2f, h * 0.78f), w * 0.04f, cap = StrokeCap.Round)
}
