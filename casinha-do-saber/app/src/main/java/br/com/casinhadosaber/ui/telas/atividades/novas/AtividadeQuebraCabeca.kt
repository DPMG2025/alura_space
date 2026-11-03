package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.QuebraCabeca
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.clarear
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import br.com.casinhadosaber.ui.telas.atividades.PecaMovel
import br.com.casinhadosaber.ui.telas.atividades.figurasDe
import br.com.casinhadosaber.ui.telas.atividades.nomeDaFigura
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Stable
private class PecaDoQuebraN2(val id: Int, val giro: Float) {
    val peca = PecaMovel()
    var encaixada by mutableStateOf(false)
}

/** Profundidade do dente da peça e a folga em volta da casinha (frações do lado da casinha). */
private const val DENTE_N2 = 0.22f
private const val FOLGA_N2 = 0.27f

/**
 * Quebra cabeça de arrastar (regras na data class [QuebraCabeca] e no catálogo de
 * docs/plano-1000-fases.md).
 *
 * A figura fica clarinha na moldura de madeira, com o contorno das peças tracejado; as peças de
 * verdade (com dentes e encaixes, cortadas da mesma figura) ficam espalhadas embaixo, um pouco
 * menores e tortinhas. Arrastar uma peça até a moldura: se for a casinha dela, encaixa com um clique;
 * se for outra casinha vazia, volta tremendo, o narrador diz o rótulo da peça e conta um erro. Soltar
 * fora da moldura só devolve a peça. Pronta a figura, ela pula e balança inteira.
 * Depois de 2 erros (ou com [ajudaInicial]) a próxima peça e a casinha dela brilham e a mãozinha leva
 * uma até a outra.
 */
@Composable
internal fun AtividadeQuebraCabeca(
    quebraCabeca: QuebraCabeca,
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

    val linhas = quebraCabeca.linhas
    val colunas = quebraCabeca.colunas
    val rotulos = quebraCabeca.rotulos
    // As peças soltas, já na ordem embaralhada da bandeja (as fixas começam no lugar).
    val pecas = remember(quebraCabeca) {
        (0 until quebraCabeca.pecas).filter { it !in quebraCabeca.fixas }.shuffled()
            .map { PecaDoQuebraN2(it, listOf(-9f, 6f, -4f, 8f, -7f, 4f, -5f, 9f)[it % 8]) }
    }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var tocouSemArrastar by remember { mutableStateOf(false) }
    val festa = remember { Animatable(0f) }
    val encaixadas = pecas.count { it.encaixada }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN2(ativa, terminou, ajudaInicial, ajuda, interacoes, tocouSemArrastar, encaixadas)
    // A peça da ajuda: a de menor número ainda solta (seguindo a ordem dos rótulos, quando há).
    val daVez = pecas.filter { !it.encaixada }.minByOrNull { it.id }
    LaunchedEffect(terminou) {
        if (terminou) festa.animateTo(1f, tween(1100))
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas (em pixels)
        val m = pecas.size
        val margemLado = px(12f)
        val vaoMeio = px(22f)
        var s = min(px(150f), (w - 2 * margemLado - px(16f)) / colunas)
        var escalaBandeja = 0.82f
        var colsBandeja = 1
        var linhasBandeja = 1
        var passo = 1f
        while (true) {
            escalaBandeja = if (s * 0.82f >= px(64f)) 0.82f else min(1f, px(64f) / s)
            passo = s * escalaBandeja * 1.22f
            colsBandeja = max(1, min(m, ((w - 2 * margemLado) / passo).toInt()))
            linhasBandeja = ceil(m / colsBandeja.toFloat()).toInt()
            val total = linhas * s + px(16f) + vaoMeio + linhasBandeja * passo
            if (total <= h || s <= px(52f)) break
            s -= px(2f)
        }
        val folga = s * FOLGA_N2
        val caixa = s + 2 * folga
        val larguraFig = colunas * s
        val alturaFig = linhas * s
        val fx = (w - larguraFig) / 2f
        val sobra = max(0f, h - (alturaFig + px(16f) + vaoMeio + linhasBandeja * passo))
        val fy = px(8f) + sobra * 0.25f
        val bandejaY = fy + alturaFig + px(8f) + vaoMeio + sobra * 0.35f

        fun casinha(i: Int) = Offset(fx + (i % colunas) * s, fy + (i / colunas) * s)
        fun naBandeja(p: PecaDoQuebraN2): Offset {
            val t = pecas.indexOf(p)
            val linha = t / colsBandeja
            val naLinha = min(colsBandeja, m - linha * colsBandeja)
            val x0 = (w - naLinha * passo) / 2f
            return Offset(x0 + (t % colsBandeja + 0.5f) * passo, bandejaY + (linha + 0.5f) * passo)
        }
        // Canto da caixa da peça (a caixa é maior que a casinha por causa dos dentes).
        fun lugar(p: PecaDoQuebraN2) =
            if (p.encaixada) casinha(p.id) - Offset(folga, folga) else naBandeja(p) - Offset(caixa / 2f, caixa / 2f)
        fun posicaoAtual(p: PecaDoQuebraN2) = lugar(p) + p.peca.desvio.value + p.peca.arrasto
        val ocupadas = pecas.filter { it.encaixada }.map { it.id }.toSet() + quebraCabeca.fixas
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun levar(p: PecaDoQuebraN2, encaixar: Boolean) {
            val agora = posicaoAtual(p)
            p.encaixada = encaixar
            val destino = lugar(p)
            escopo.launch(start = CoroutineStart.UNDISPATCHED) { p.peca.pousar(agora - destino, molinha) }
        }

        fun soltar(p: PecaDoQuebraN2) {
            if (!p.peca.arrastando) return
            val centro = posicaoAtual(p) + Offset(caixa / 2f, caixa / 2f)
            // A casinha vazia mais perto do centro da peça, se ele caiu nela (com folga de um quarto).
            val alvo = (0 until quebraCabeca.pecas)
                .filter { it !in ocupadas }
                .filter { Rect(casinha(it), Size(s, s)).inflate(s * 0.25f).contains(centro) }
                .minByOrNull { (casinha(it) + Offset(s / 2f, s / 2f) - centro).getDistance() }
            if (terminou || alvo == null) {
                levar(p, false)
                return
            }
            if (alvo == p.id) {
                levar(p, true)
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (pecas.all { it.encaixada }) {
                    terminou = true
                    sons.acerto()
                    escopo.launch {
                        delay(1300)
                        concluir(erros == 0)
                    }
                } else {
                    rotulos.getOrNull(p.id)?.let { narrador.falar(it.lowercase()) }
                }
            } else {
                erros++
                avisarErro()
                sons.erro()
                levar(p, false)
                escopo.launch { p.peca.tremer() }
                val nome = rotulos.getOrNull(p.id)?.let { listOf(pt(it.lowercase() + ".")) } ?: emptyList()
                val dica = when {
                    erros >= ERROS_PARA_AJUDA -> "Veja a peça que está brilhando."
                    nome.isEmpty() -> "Essa peça não é daí. Tente outra vez!"
                    else -> "Tente outra vez!"
                }
                narrador.falar(nome + pt(dica))
            }
        }

        val sDp = with(densidade) { s.toDp() }
        val caixaDp = with(densidade) { caixa.toDp() }
        val larguraFigDp = with(densidade) { larguraFig.toDp() }
        val alturaFigDp = with(densidade) { alturaFig.toDp() }

        // ---------------------------------------------------------------- moldura de madeira, figura clarinha e contornos
        val borda = px(10f)
        Canvas(Modifier.matchParentSize()) {
            val r = Rect(fx - borda, fy - borda, fx + larguraFig + borda, fy + alturaFig + borda)
            drawRoundRect(Color(0x33000000), r.topLeft + Offset(0f, px(4f)), r.size, CornerRadius(px(16f)))
            drawRoundRect(Color(0xFFC98B4F), r.topLeft, r.size, CornerRadius(px(16f)))
            drawRoundRect(Color(0xFFE0A86A), r.topLeft + Offset(px(3f), px(3f)), Size(r.width - px(6f), r.height - px(6f)), CornerRadius(px(13f)))
            drawRect(Color(0xFFFFF8EC), Offset(fx, fy), Size(larguraFig, alturaFig))
        }
        Box(
            Modifier
                .offset { IntOffset(fx.roundToInt(), fy.roundToInt()) }
                .size(larguraFigDp, alturaFigDp)
                .graphicsLayer { alpha = 0.28f },
        ) {
            FiguraDoQuebraN2(quebraCabeca.figura, larguraFigDp, alturaFigDp, cor)
        }
        // Casinhas: o contorno tracejado de cada peça; a da ajuda brilha.
        val alvoDaAjuda = if (ajuda && !terminou) daVez?.id else null
        val brilhoCasinha by animateFloatAsState(if (alvoDaAjuda != null) 1f else 0f, label = "brilhoCasinha")
        Canvas(Modifier.matchParentSize()) {
            val tracejado = PathEffect.dashPathEffect(floatArrayOf(px(7f), px(6f)))
            for (i in 0 until quebraCabeca.pecas) {
                if (i in ocupadas) continue
                val canto = casinha(i) - Offset(folga, folga)
                val caminho = caminhoDaPecaN2(i / colunas, i % colunas, linhas, colunas, s, folga)
                translate(canto.x, canto.y) {
                    if (i == alvoDaAjuda) drawPath(caminho, Cores.Estrela.copy(alpha = 0.45f * brilhoCasinha))
                    drawPath(caminho, escurecer(cor).copy(alpha = 0.55f), style = Stroke(px(2.5f), pathEffect = tracejado))
                }
            }
        }
        for (i in 0 until quebraCabeca.pecas) {
            Box(
                Modifier
                    .offset { casinha(i).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } }
                    .size(sDp)
                    .testTag("quebra_lugar_$i"),
            )
        }

        // ---------------------------------------------------------------- peças fixas (já no lugar)
        for (i in quebraCabeca.fixas) {
            Box(
                Modifier
                    .zIndex(1f)
                    .offset { (casinha(i) - Offset(folga, folga)).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } },
            ) {
                DesenhoDaPecaN2(quebraCabeca, i, s, folga, caixaDp, larguraFigDp, alturaFigDp, cor, Color.White.copy(alpha = 0.7f))
            }
        }

        // ---------------------------------------------------------------- peças soltas
        for (p in pecas) {
            val escala by animateFloatAsState(
                when {
                    p.peca.arrastando -> 1.06f
                    p.encaixada -> 1f
                    else -> escalaBandeja
                },
                spring(dampingRatio = 0.55f),
                label = "escala",
            )
            val giro by animateFloatAsState(if (p.encaixada || p.peca.arrastando) 0f else p.giro, label = "giro")
            val destacar = ajuda && !terminou && p === daVez
            Box(
                Modifier
                    .zIndex(if (p.peca.arrastando) 5f else if (p.encaixada) 2f else 3f)
                    .offset { posicaoAtual(p).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } }
                    .size(caixaDp)
                    .testTag("quebra_peca_${p.id}")
                    .semantics { contentDescription = rotulos.getOrNull(p.id)?.lowercase() ?: "peça ${p.id + 1}" }
                    .pointerInput(p, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou && !p.encaixada) {
                                    p.peca.arrastando = true
                                    tocouSemArrastar = false
                                    interacoes++
                                    sons.pegar()
                                    escopo.launch { p.peca.desvio.stop() }
                                }
                            },
                            onDragEnd = { soltar(p) },
                            onDragCancel = { soltar(p) },
                        ) { mudanca, delta ->
                            if (p.peca.arrastando) {
                                mudanca.consume()
                                p.peca.arrasto += delta
                            }
                        }
                    }
                    .pointerInput(p, w, h) {
                        detectTapGestures {
                            if (ativaAgora && !terminou && !p.encaixada) {
                                sons.toque()
                                tocouSemArrastar = true
                                interacoes++
                                escopo.launch { p.peca.tremer() }
                                rotulos.getOrNull(p.id)?.let { narrador.falar(it.lowercase()) }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        rotationZ = giro + p.peca.tremida.value
                    }
                    .then(if (destacar) Modifier.pulsando() else Modifier),
            ) {
                DesenhoDaPecaN2(
                    quebraCabeca, p.id, s, folga, caixaDp, larguraFigDp, alturaFigDp, cor,
                    when {
                        destacar -> Cores.Estrela
                        p.encaixada -> Color.White.copy(alpha = 0.7f)
                        else -> Color.White
                    },
                    elevacao = if (p.peca.arrastando) 10.dp else if (p.encaixada) 0.dp else 4.dp,
                )
            }
        }

        // ---------------------------------------------------------------- festa: a figura inteira pula e balança
        if (festa.value > 0f) {
            val f = festa.value
            Box(
                Modifier
                    .zIndex(7f)
                    .offset { IntOffset(fx.roundToInt(), fy.roundToInt()) }
                    .size(larguraFigDp, alturaFigDp)
                    .graphicsLayer {
                        alpha = (f * 5f).coerceAtMost(1f)
                        val pulo = 1f + 0.12f * sin(PI.toFloat() * f)
                        scaleX = pulo
                        scaleY = pulo
                        rotationZ = 7f * sin(4f * PI.toFloat() * f) * (1f - f)
                    }
                    .shadow(8.dp, RoundedCornerShape(6.dp)),
            ) {
                FiguraDoQuebraN2(quebraCabeca.figura, larguraFigDp, alturaFigDp, cor)
            }
        }

        // ---------------------------------------------------------------- mãozinha: mostra o gesto de arrastar
        val exemplo = if (ajuda) daVez else null
        val origem = exemplo?.let { naBandeja(it) } ?: Offset(w / 2f, bandejaY - vaoMeio / 2f)
        val destino = exemplo?.let { casinha(it.id) + Offset(s / 2f, s / 2f) } ?: Offset(w / 2f, fy + alturaFig / 2f)
        val ninguemArrastando = pecas.none { it.peca.arrastando }
        if (daVez != null) {
            Box(Modifier.matchParentSize().zIndex(6f)) {
                key(exemplo?.id, encaixadas) {
                    GestoDeArrastar(
                        origem = origem,
                        destino = destino,
                        ativa = ativa && !terminou && parada && ninguemArrastando,
                        cor = escurecer(cor),
                        tamanhoCarga = caixaDp,
                    ) {
                        if (exemplo != null) {
                            DesenhoDaPecaN2(quebraCabeca, exemplo.id, s, folga, caixaDp, larguraFigDp, alturaFigDp, cor, Cores.Estrela)
                        }
                    }
                }
            }
        }
    }
}

/** A figura inteira (fundo de céu com bolinhas e o emoji grande no meio), do tamanho da moldura. */
@Composable
private fun FiguraDoQuebraN2(figura: String, largura: Dp, altura: Dp, cor: Color, modifier: Modifier = Modifier) {
    val claro = clarear(cor, 0.82f)
    val medio = clarear(cor, 0.55f)
    Box(modifier.requiredSize(largura, altura), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFFFFDF5), claro, medio)))
            // Bolinhas e um chão de grama: toda peça tem alguma coisa para olhar.
            val passo = size.minDimension / 4.5f
            var y = passo * 0.5f
            var linha = 0
            while (y < size.height) {
                var x = if (linha % 2 == 0) passo * 0.4f else passo * 0.9f
                while (x < size.width) {
                    drawCircle(Color.White.copy(alpha = 0.55f), radius = passo * 0.12f, center = Offset(x, y))
                    x += passo
                }
                y += passo
                linha++
            }
            drawOval(Color(0xFF8CD47E), Offset(-size.width * 0.1f, size.height * 0.8f), Size(size.width * 1.2f, size.height * 0.45f))
            drawOval(Color(0xFF6CC266), Offset(size.width * 0.35f, size.height * 0.86f), Size(size.width * 0.9f, size.height * 0.4f))
        }
        val figuras = figurasDe(figura)
        val t = min(altura.value * 0.74f, largura.value * 0.74f / max(1, figuras.size))
        Text(figura, fontSize = t.sp, lineHeight = (t * 1.12f).sp, maxLines = 1, softWrap = false)
    }
}

/** Uma peça: a parte da figura recortada com dentes, borda colorida e o rótulo (se houver). */
@Composable
private fun DesenhoDaPecaN2(
    q: QuebraCabeca,
    id: Int,
    s: Float,
    folga: Float,
    caixa: Dp,
    larguraFig: Dp,
    alturaFig: Dp,
    cor: Color,
    corBorda: Color,
    elevacao: Dp = 0.dp,
) {
    val r = id / q.colunas
    val c = id % q.colunas
    val forma = remember(id, s, q.linhas, q.colunas) {
        GenericShape { _, _ -> addPath(caminhoDaPecaN2(r, c, q.linhas, q.colunas, s, folga)) }
    }
    val densidade = LocalDensity.current
    Box(
        Modifier
            .size(caixa)
            .then(if (elevacao > 0.dp) Modifier.shadow(elevacao, forma) else Modifier)
            .clip(forma)
            .background(Color.White),
    ) {
        FiguraDoQuebraN2(
            q.figura, larguraFig, alturaFig, cor,
            Modifier
                .offset { IntOffset((folga - c * s).roundToInt(), (folga - r * s).roundToInt()) }
                .wrapContentSize(Alignment.TopStart, unbounded = true),
        )
        q.rotulos.getOrNull(id)?.let { rotulo ->
            val altura = with(densidade) { (s * 0.34f).toDp() }.coerceAtLeast(30.dp)
            Box(
                Modifier
                    .offset { IntOffset((folga + s * 0.08f).roundToInt(), (folga + s * 0.08f).roundToInt()) }
                    .height(altura)
                    .widthIn(min = altura)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(50))
                    .border(2.dp, cor, RoundedCornerShape(50))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    rotulo,
                    fontSize = (altura.value * 0.56f).sp,
                    lineHeight = (altura.value * 0.62f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.Texto,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
    // Borda por cima do recorte (fora do clip, para não ficar cortada pela metade).
    Box(Modifier.size(caixa).border(3.dp, corBorda, forma))
}

/**
 * Contorno da peça [r], [c] dentro da caixa (lado da casinha [s] mais [folga] de cada lado): bordas
 * da figura retas; entre duas peças, um dente numa e o buraco igual na vizinha.
 */
private fun caminhoDaPecaN2(r: Int, c: Int, linhas: Int, colunas: Int, s: Float, folga: Float): Path {
    fun direita(r: Int, c: Int) = if ((r + c) % 2 == 0) 1 else -1
    fun baixo(r: Int, c: Int) = if ((r + c) % 2 == 0) -1 else 1
    val ladoCima = if (r == 0) 0 else -baixo(r - 1, c)
    val ladoDireita = if (c == colunas - 1) 0 else direita(r, c)
    val ladoBaixo = if (r == linhas - 1) 0 else baixo(r, c)
    val ladoEsquerda = if (c == 0) 0 else -direita(r, c - 1)
    val dente = s * DENTE_N2
    val a = Offset(folga, folga)
    val b = Offset(folga + s, folga)
    val cc = Offset(folga + s, folga + s)
    val d = Offset(folga, folga + s)
    return Path().apply {
        moveTo(a.x, a.y)
        fun lado(p: Offset, q: Offset, normal: Offset, sinal: Int) {
            if (sinal == 0) {
                lineTo(q.x, q.y)
                return
            }
            fun ponto(t: Float, alto: Float) = p + (q - p) * t + normal * (alto * sinal * dente)
            val p1 = ponto(0.37f, 0f)
            lineTo(p1.x, p1.y)
            val c1 = ponto(0.43f, 0.35f)
            val c2 = ponto(0.28f, 1.05f)
            val topo = ponto(0.5f, 1.05f)
            cubicTo(c1.x, c1.y, c2.x, c2.y, topo.x, topo.y)
            val c3 = ponto(0.72f, 1.05f)
            val c4 = ponto(0.57f, 0.35f)
            val p2 = ponto(0.63f, 0f)
            cubicTo(c3.x, c3.y, c4.x, c4.y, p2.x, p2.y)
            lineTo(q.x, q.y)
        }
        lado(a, b, Offset(0f, -1f), ladoCima)
        lado(b, cc, Offset(1f, 0f), ladoDireita)
        lado(cc, d, Offset(0f, 1f), ladoBaixo)
        lado(d, a, Offset(-1f, 0f), ladoEsquerda)
        close()
    }
}
