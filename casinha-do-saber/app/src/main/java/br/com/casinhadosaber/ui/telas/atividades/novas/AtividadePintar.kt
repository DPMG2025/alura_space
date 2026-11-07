package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.CasaDePintar
import br.com.casinhadosaber.dominio.CorDaLegenda
import br.com.casinhadosaber.dominio.PintarPorCodigo
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Baloo2
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.MaoQueSegue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
private class CasaPintavel(val indice: Int, val casa: CasaDePintar) {
    var pintada by mutableStateOf(false)
    /** Tinta crescendo do meio para as bordas (0 a 1). */
    val crescer = Animatable(0f)
    /** Tinta errada escorrendo e sumindo (0 a 1); [corErrada] é a tinta que escorre. */
    val escorrer = Animatable(1f)
    var corErrada by mutableStateOf(Color.Transparent)
    val tremida = Animatable(0f)
}

private fun corDe(l: CorDaLegenda) = Color(l.argb)

/** Rótulo como se fala: "2+1" vira "2 mais 1". */
private fun rotuloFalado(r: String) = r.replace("+", " mais ").replace("−", " menos ").replace("-", " menos ").replace("×", " vezes ").trim()

/**
 * Pintar por código: um desenho quadriculado em que cada casinha tem um rótulo (número, conta, letra,
 * palavra em inglês). A criança toca num pote de tinta da legenda e depois nas casinhas que levam
 * aquela cor; o desenho aparece aos poucos.
 * * Acerto: todas as casinhas com código pintadas; os números somem, o desenho aparece inteiro e um
 *   brilho passa por cima.
 * * Erro: tocar com uma tinta numa casinha que não é dela (inclusive as que ficam brancas e têm
 *   rótulo). A tinta escorre, a casinha volta a ficar branca e o narrador diz o rótulo tocado.
 * Tocar numa casinha sem ter escolhido a tinta só lembra de escolher (não conta erro).
 */
@Composable
internal fun AtividadePintar(
    pintarPorCodigo: PintarPorCodigo,
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
    val densidade = LocalDensity.current
    val escopo = rememberCoroutineScope()
    val ativaAgora by rememberUpdatedState(ativa)
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    val desenho = pintarPorCodigo
    val casas = remember(desenho) { desenho.casas.mapIndexed { i, c -> CasaPintavel(i, c) } }
    val legenda = desenho.legenda
    var pote by remember(desenho) { mutableStateOf<String?>(null) }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var lembrarPote by remember { mutableIntStateOf(0) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN3(interacoes, terminou, ativa, ajuda, ajudaInicial)
    val brilho = remember(desenho) { Animatable(0f) }
    val sumirRotulos by animateFloatAsState(if (terminou) 0f else 1f, tween(500), label = "rotulos")
    LaunchedEffect(terminou) {
        if (terminou) {
            delay(250)
            brilho.animateTo(1f, tween(1100, easing = LinearEasing))
        }
    }

    val faltam = casas.filter { it.casa.codigo != null && !it.pintada }
    // Ajuda: a tinta escolhida ainda tem casinhas? Brilham elas. Senão, brilha um pote que ainda serve.
    val poteServe = pote != null && faltam.any { it.casa.codigo == pote }
    val poteSugerido = if (poteServe) pote else faltam.firstOrNull()?.casa?.codigo

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas
        val alturaLegenda = px(112f)
        val colunas = desenho.colunas
        val linhas = desenho.linhas
        val vaoCasa = px(3f)
        val lado = min(min((w - px(20f)) / colunas, (h - alturaLegenda - px(20f)) / linhas), px(92f))
        val larguraGrade = lado * colunas
        val alturaGrade = lado * linhas
        val gradeX = (w - larguraGrade) / 2f
        val gradeY = ((h - alturaLegenda - alturaGrade) / 2f).coerceAtLeast(px(4f))
        fun cantoDaCasa(i: Int) = Offset(gradeX + (i % colunas) * lado, gradeY + (i / colunas) * lado)

        val ladoPote = min(px(86f), (w - px(16f)) / legenda.size * 0.9f)
        val passoPote = min((w - px(16f)) / legenda.size, ladoPote * 1.22f)
        fun centroDoPote(k: Int) = Offset(w / 2f + (k - (legenda.size - 1) / 2f) * passoPote, h - alturaLegenda / 2f)

        fun escolher(l: CorDaLegenda) {
            if (!ativaAgora || terminou) return
            interacoes++
            pote = l.codigo
            sons.toque()
            narrador.falar(l.fala.ifEmpty { listOf(pt(rotuloFalado(l.codigo))) })
        }

        fun tocar(c: CasaPintavel) {
            if (!ativaAgora || terminou) return
            interacoes++
            val rotulo = c.casa.rotulo
            when {
                c.pintada -> {
                    sons.toque()
                    if (rotulo.isNotBlank()) narrador.falar(rotuloFalado(rotulo))
                }
                pote == null -> {
                    sons.toque()
                    lembrarPote++
                    narrador.falar("Primeiro escolha uma tinta lá embaixo!")
                }
                c.casa.codigo == null && rotulo.isBlank() -> {
                    // Fundo sem rótulo: nada a pintar (e não é um erro de conta).
                    sons.toque()
                    escopo.launch { for (x in listOf(5f, -5f, 3f, 0f)) c.tremida.animateTo(x, tween(50)) }
                }
                c.casa.codigo == pote -> {
                    c.pintada = true
                    sons.colocar()
                    escopo.launch { c.crescer.animateTo(1f, tween(320, easing = FastOutSlowInEasing)) }
                    if (casas.all { it.casa.codigo == null || it.pintada }) {
                        terminou = true
                        escopo.launch {
                            delay(1600)
                            concluir(erros == 0)
                        }
                    }
                }
                else -> {
                    erros++
                    avisarErro()
                    sons.erro()
                    c.corErrada = legenda.firstOrNull { it.codigo == pote }?.let { corDe(it) } ?: Color.Gray
                    escopo.launch {
                        c.escorrer.snapTo(0f)
                        c.escorrer.animateTo(1f, tween(1100, easing = LinearEasing))
                    }
                    escopo.launch { for (x in listOf(7f, -7f, 5f, -5f, 2f, 0f)) c.tremida.animateTo(x, tween(55)) }
                    narrador.falar(
                        if (c.casa.codigo == null) "${rotuloFalado(rotulo)}! Essa casinha fica branca."
                        else "${rotuloFalado(rotulo)}! Não é dessa cor.",
                    )
                }
            }
        }
        val tocarAgora by rememberUpdatedState<(CasaPintavel) -> Unit> { tocar(it) }
        val escolherAgora by rememberUpdatedState<(CorDaLegenda) -> Unit> { escolher(it) }

        // ---------------------------------------------------------------- moldura do quadro
        Canvas(Modifier.matchParentSize()) {
            val m = px(8f)
            drawRoundRect(Color(0x33000000), Offset(gradeX - m, gradeY - m + px(4f)), Size(larguraGrade + m * 2, alturaGrade + m * 2), CornerRadius(px(14f)))
            drawRoundRect(Color(0xFFB9784A), Offset(gradeX - m, gradeY - m), Size(larguraGrade + m * 2, alturaGrade + m * 2), CornerRadius(px(14f)))
            drawRoundRect(Color(0xFFE6B98F), Offset(gradeX - m * 0.5f, gradeY - m * 0.5f), Size(larguraGrade + m, alturaGrade + m), CornerRadius(px(10f)))
            drawRect(Color.White, Offset(gradeX, gradeY), Size(larguraGrade, alturaGrade))
        }

        // ---------------------------------------------------------------- casinhas
        val ladoDp = lado / densidade.density
        for (c in casas) {
            val canto = cantoDaCasa(c.indice)
            val destacar = ajuda && !terminou && poteServe && !c.pintada && c.casa.codigo == pote
            val corCerta = legenda.firstOrNull { it.codigo == c.casa.codigo }?.let { corDe(it) }
            Box(
                Modifier
                    .zIndex(if (c.escorrer.value < 1f) 2f else 1f)
                    .offset { IntOffset(canto.x.roundToInt(), canto.y.roundToInt()) }
                    .size(with(densidade) { lado.toDp() })
                    .testTag("pintar_casa_${c.indice}")
                    .semantics { contentDescription = c.casa.rotulo.ifBlank { "casinha" } }
                    .pointerInput(c) { detectTapGestures { tocarAgora(c) } }
                    .graphicsLayer { translationX = c.tremida.value * densidade.density }
                    .then(if (destacar) Modifier.pulsando() else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.matchParentSize()) {
                    val v = if (terminou) 0f else vaoCasa
                    val dentro = Size(size.width - v, size.height - v)
                    val topo = Offset(v / 2f, v / 2f)
                    // Casinha branca de cantos redondinhos, com uma sombrinha por dentro (parece um azulejo
                    // fundo); pronto o desenho, as casinhas viram quadrados e o desenho aparece inteiro.
                    val canto = if (terminou) 0f else dentro.minDimension * 0.16f
                    val forma = Path().apply { addRoundRect(RoundRect(topo.x, topo.y, topo.x + dentro.width, topo.y + dentro.height, CornerRadius(canto))) }
                    if (terminou) {
                        drawRect(Color.White, topo, dentro)
                    } else {
                        drawPath(forma, Color(0xFFE6E0F0))
                        clipPath(forma) {
                            drawRoundRect(Color.White, topo + Offset(px(1.5f), px(3f)), Size(dentro.width - px(1.5f), dentro.height - px(1.5f)), CornerRadius(canto))
                        }
                        drawPath(forma, Color(0xFFD2CAE3), style = Stroke(px(1.2f)))
                    }
                    if (c.pintada && corCerta != null) {
                        // A tinta cresce do meio.
                        clipPath(forma) {
                            drawCircle(corCerta, size.maxDimension * 0.75f * c.crescer.value, center)
                            if (!terminou) drawRoundRect(Color.Black.copy(alpha = 0.1f), topo, Size(dentro.width, px(3f)))
                        }
                        if (!terminou) {
                            // Um brilhinho de tinta fresca.
                            drawRoundRect(Color.White.copy(alpha = 0.22f), topo + Offset(dentro.width * 0.12f, dentro.height * 0.12f), Size(dentro.width * 0.3f, dentro.height * 0.12f), CornerRadius(dentro.height * 0.06f))
                        }
                    }
                    if (destacar) drawPath(forma, Cores.Estrela, style = Stroke(px(4f)))
                    // tinta errada escorrendo
                    val e = c.escorrer.value
                    if (e < 1f) desenharEscorrido(c.corErrada, e, topo, dentro)
                }
                if (c.casa.rotulo.isNotBlank() && (!c.pintada || c.crescer.value < 0.6f) && sumirRotulos > 0.01f) {
                    val tamanho = ladoDp * when {
                        c.casa.rotulo.length <= 1 -> 0.5f
                        c.casa.rotulo.length <= 2 -> 0.42f
                        c.casa.rotulo.length <= 3 -> 0.32f
                        else -> 0.24f
                    }
                    Text(
                        c.casa.rotulo,
                        fontSize = tamanho.sp,
                        lineHeight = (tamanho * 1.1f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Baloo2,
                        color = Cores.Texto.copy(alpha = sumirRotulos),
                        maxLines = 1,
                    )
                }
            }
        }

        // ---------------------------------------------------------------- brilho que passa no fim
        if (terminou) {
            Canvas(
                Modifier
                    .matchParentSize()
                    .zIndex(3f),
            ) {
                val b = brilho.value
                if (b in 0.001f..0.999f) {
                    clipRect(gradeX, gradeY, gradeX + larguraGrade, gradeY + alturaGrade) {
                        val x = gradeX - larguraGrade * 0.4f + (larguraGrade * 1.8f) * b
                        val faixa = Path().apply {
                            moveTo(x, gradeY - px(10f))
                            lineTo(x + larguraGrade * 0.22f, gradeY - px(10f))
                            lineTo(x - larguraGrade * 0.18f, gradeY + alturaGrade + px(10f))
                            lineTo(x - larguraGrade * 0.4f, gradeY + alturaGrade + px(10f))
                            close()
                        }
                        drawPath(faixa, Color.White.copy(alpha = 0.55f))
                    }
                }
                // estrelinhas nos cantos do quadro
                val s = (1f - kotlin.math.abs(b * 2f - 1f)).coerceIn(0f, 1f)
                for ((k, p) in listOf(
                    Offset(gradeX, gradeY), Offset(gradeX + larguraGrade, gradeY),
                    Offset(gradeX, gradeY + alturaGrade), Offset(gradeX + larguraGrade, gradeY + alturaGrade),
                ).withIndex()) {
                    desenharEstrelinha(p, px(10f + 8f * s + (k % 2) * 3f), Cores.Estrela)
                }
            }
        }

        // ---------------------------------------------------------------- legenda: potes de tinta
        val pulo by animateFloatAsState(if (lembrarPote % 2 == 0) 0f else 1f, spring(dampingRatio = 0.3f), label = "lembrarPote")
        for ((k, l) in legenda.withIndex()) {
            val c = centroDoPote(k)
            val escolhido = pote == l.codigo
            val sugerir = !terminou && (ajuda || (interacoes == 0 && parada) || (lembrarPote > 0 && pote == null)) && poteSugerido == l.codigo && !escolhido
            val subir by animateFloatAsState(if (escolhido) 1f else 0f, spring(dampingRatio = 0.5f), label = "subirPote")
            Box(
                Modifier
                    .zIndex(4f)
                    .offset { IntOffset((c.x - ladoPote / 2f).roundToInt(), (c.y - ladoPote / 2f - subir * px(10f) - pulo * px(4f)).roundToInt()) }
                    .size(with(densidade) { ladoPote.toDp() })
                    .testTag("pintar_pote_${l.codigo}")
                    .semantics { contentDescription = "Tinta ${l.codigo}" }
                    .pointerInput(l) { detectTapGestures { escolherAgora(l) } }
                    .then(if (sugerir) Modifier.pulsando() else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                val corPote = corDe(l)
                Canvas(Modifier.matchParentSize()) { desenharPote(corPote, escolhido, sugerir) }
                val tam = (ladoPote / densidade.density) * when {
                    l.codigo.length <= 2 -> 0.3f
                    l.codigo.length <= 4 -> 0.2f
                    else -> 0.15f
                }
                Text(
                    l.codigo,
                    fontSize = tam.sp,
                    lineHeight = (tam * 1.1f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Baloo2,
                    color = Cores.Texto,
                    maxLines = 1,
                    modifier = Modifier.padding(top = (ladoPote / densidade.density * 0.2f).dp),
                )
            }
        }

        // ---------------------------------------------------------------- mãozinha que toca
        // Primeiro mostra o pote; com a tinta escolhida (e a ajuda valendo), uma casinha dela.
        val alvoMao: Offset? = when {
            terminou -> null
            pote == null || !poteServe -> poteSugerido?.let { cod -> centroDoPote(legenda.indexOfFirst { it.codigo == cod }) }
            ajuda -> faltam.firstOrNull { it.casa.codigo == pote }?.let { cantoDaCasa(it.indice) + Offset(lado / 2f, lado / 2f) }
            else -> null
        }
        Box(Modifier.matchParentSize().zIndex(7f)) {
            if (alvoMao != null) {
                MaoQueSegue(listOf(alvoMao), ativa = ativa && parada, espera = 0L)
            }
        }
    }
}

/** Pote de tinta: vidro com a tinta, tampa aberta e uma etiqueta branca (o código fica escrito nela). */
private fun DrawScope.desenharPote(cor: Color, escolhido: Boolean, sugerir: Boolean) {
    val s = size.minDimension
    val corpo = Path().apply {
        moveTo(s * 0.18f, s * 0.24f)
        lineTo(s * 0.82f, s * 0.24f)
        lineTo(s * 0.86f, s * 0.86f)
        quadraticTo(s * 0.86f, s * 0.96f, s * 0.76f, s * 0.96f)
        lineTo(s * 0.24f, s * 0.96f)
        quadraticTo(s * 0.14f, s * 0.96f, s * 0.14f, s * 0.86f)
        close()
    }
    if (escolhido) drawCircle(Cores.Certo.copy(alpha = 0.3f), s * 0.52f, center)
    if (sugerir) drawCircle(Cores.Estrela.copy(alpha = 0.45f), s * 0.52f, center)
    drawPath(corpo, escurecer(cor, 0.7f))
    drawPath(corpo, Brush.verticalGradient(listOf(lerp3(cor, Color.White, 0.25f), cor), startY = s * 0.24f, endY = s * 0.96f))
    // boca do pote com a tinta aparecendo e um pingo escorrendo
    drawOval(escurecer(cor, 0.6f), Offset(s * 0.16f, s * 0.16f), Size(s * 0.68f, s * 0.16f))
    drawOval(cor, Offset(s * 0.2f, s * 0.18f), Size(s * 0.6f, s * 0.11f))
    drawRoundRect(cor, Offset(s * 0.68f, s * 0.24f), Size(s * 0.08f, s * 0.16f), CornerRadius(s * 0.04f))
    drawCircle(cor, s * 0.05f, Offset(s * 0.72f, s * 0.41f))
    // etiqueta
    drawRoundRect(Color.White, Offset(s * 0.22f, s * 0.46f), Size(s * 0.56f, s * 0.36f), CornerRadius(s * 0.08f))
    drawRoundRect(escurecer(cor, 0.75f), Offset(s * 0.22f, s * 0.46f), Size(s * 0.56f, s * 0.36f), CornerRadius(s * 0.08f), style = Stroke(s * 0.025f))
    drawPath(corpo, escurecer(cor, 0.55f), style = Stroke(s * (if (escolhido) 0.05f else 0.03f)))
    // pincel em cima do pote escolhido
    if (escolhido) {
        drawLine(Color(0xFFB9784A), Offset(s * 0.6f, s * 0.02f), Offset(s * 0.5f, s * 0.24f), s * 0.07f)
        drawRoundRect(cor, Offset(s * 0.43f, s * 0.2f), Size(s * 0.12f, s * 0.12f), CornerRadius(s * 0.05f))
    }
}

/** Tinta errada: uma mancha que escorre pela casinha em gotas e some ([e] de 0 a 1). */
private fun DrawScope.desenharEscorrido(cor: Color, e: Float, topo: Offset, dentro: Size) {
    val alfa = (1f - e).coerceIn(0f, 1f)
    drawRect(cor.copy(alpha = 0.85f * alfa), topo, Size(dentro.width, dentro.height * (1f - e * 0.4f)))
    val gotas = 3
    for (k in 0 until gotas) {
        val x = topo.x + dentro.width * (0.22f + k * 0.28f)
        val comprimento = dentro.height * (0.25f + 0.35f * e) * (1f + (k % 2) * 0.4f)
        val y0 = topo.y + dentro.height * 0.6f
        drawRoundRect(cor.copy(alpha = alfa), Offset(x - dentro.width * 0.06f, y0), Size(dentro.width * 0.12f, comprimento), CornerRadius(dentro.width * 0.06f))
        drawCircle(cor.copy(alpha = alfa), dentro.width * 0.09f, Offset(x, y0 + comprimento))
    }
}

private fun DrawScope.desenharEstrelinha(c: Offset, r: Float, cor: Color) {
    val p = Path()
    for (k in 0 until 10) {
        val raio = if (k % 2 == 0) r else r * 0.45f
        val a = Math.PI / 5 * k - Math.PI / 2
        val x = c.x + (raio * kotlin.math.cos(a)).toFloat()
        val y = c.y + (raio * kotlin.math.sin(a)).toFloat()
        if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, cor)
    drawPath(p, escurecer(cor, 0.7f), style = Stroke(r * 0.12f))
}

private fun lerp3(a: Color, b: Color, f: Float) = Color(
    a.red + (b.red - a.red) * f,
    a.green + (b.green - a.green) * f,
    a.blue + (b.blue - a.blue) * f,
    a.alpha,
)
