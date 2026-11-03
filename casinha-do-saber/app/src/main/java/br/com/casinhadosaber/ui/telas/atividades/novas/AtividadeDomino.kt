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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Domino
import br.com.casinhadosaber.dominio.MeiaPeca
import br.com.casinhadosaber.dominio.PecaDomino
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import br.com.casinhadosaber.ui.telas.atividades.PecaMovel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
private class PecaNaMaoN2(val id: Int, val domino: PecaDomino) {
    val peca = PecaMovel()
    var colocada by mutableStateOf(false)
}

/** Peça já na fileira: [virada] quando entrou com a metade b encostada na ponta (gira ao encaixar). */
private class EncaixeN2(val id: Int, val esquerda: MeiaPeca, val direita: MeiaPeca, val virada: Boolean) {
    val giro = Animatable(if (virada) 0f else 1f)
}

private fun falaDaMeiaN2(m: MeiaPeca) = falaDoVisualN2(m.visual, m.fala)

/**
 * Dominó (regras na data class [Domino] e no catálogo de docs/plano-1000-fases.md).
 *
 * Em cima, a fileira numa mesa de madeira, com a ponta livre brilhando e, ao lado, o lugar
 * tracejado da próxima peça; embaixo, as peças da mão. Arrastar até o lugar uma peça que combina com
 * a ponta: ela encaixa (girando, se a metade que combina estava do outro lado), a fileira anda para a
 * esquerda e a outra metade vira a nova ponta. Peça que não combina volta tremendo, o narrador diz as
 * duas metades e conta um erro. Soltar longe da fileira só devolve a peça. Tocar na ponta ou numa
 * peça faz ouvir o nome. Depois de 2 erros (ou com [ajudaInicial]) a peça que serve brilha e a
 * mãozinha leva a peça até o lugar.
 */
@Composable
internal fun AtividadeDomino(
    domino: Domino,
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

    // A mão embaralhada (o índice em domino.mao fica guardado em cada peça).
    val mao = remember(domino) { domino.mao.mapIndexed { i, p -> PecaNaMaoN2(i, p) }.shuffled() }
    val fileira = remember(domino) { mutableStateListOf<EncaixeN2>() }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var tocouSemArrastar by remember { mutableStateOf(false) }
    val tremidaPonta = remember { Animatable(0f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN2(ativa, terminou, ajudaInicial, ajuda, interacoes, tocouSemArrastar, fileira.size)
    val ponta = fileira.lastOrNull()?.direita ?: domino.ponta
    val daVez = if (terminou) null else domino.ordem.getOrNull(fileira.size)?.let { i -> mao.first { it.id == i } }
    val andou by animateFloatAsState(fileira.size.toFloat(), spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow), label = "andou")

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas (em pixels)
        val margem = px(12f)
        val vao = px(14f)
        val n = mao.size
        var lado = min(px(92f), (w - 2 * margem) / 4.7f)
        var cols = 1
        var linhas = 1
        while (true) {
            cols = max(1, min(n, ((w - 2 * margem + vao) / (2 * lado + vao)).toInt()))
            linhas = ceil(n / cols.toFloat()).toInt()
            val total = lado + px(40f) + px(36f) + linhas * (lado + vao)
            if (total <= h || lado <= px(60f)) break
            lado -= px(2f)
        }
        val peca = 2 * lado
        val mesaY = px(20f)
        val sobra = max(0f, h - (lado + px(40f) + px(36f) + linhas * (lado + vao)))
        val fileiraY = mesaY + sobra * 0.15f
        val maoY = fileiraY + lado + px(40f) + px(36f) + sobra * 0.35f
        val vaoFileira = px(6f)
        // O lugar da próxima peça fica à direita; a fileira cresce para a esquerda.
        val alvo = Rect(Offset(w - margem - peca - px(4f), fileiraY), Size(peca, lado))
        val fimDaFileira = alvo.left - vaoFileira
        // Enquanto a fileira anda, as peças começam um passo à direita e deslizam.
        val atraso = (fileira.size - andou) * (peca + vaoFileira)
        fun xDaFileira(k: Int): Float {
            // k = 0 é a peça inicial; k >= 1 são as encaixadas. Todas com a largura de uma peça.
            val total = fileira.size + 1
            return fimDaFileira - (total - k) * (peca + vaoFileira) + vaoFileira + atraso
        }
        fun naMao(p: PecaNaMaoN2): Offset {
            val t = mao.indexOf(p)
            val linha = t / cols
            val naLinha = min(cols, n - linha * cols)
            val x0 = (w - (naLinha * peca + (naLinha - 1) * vao)) / 2f
            return Offset(x0 + (t % cols) * (peca + vao), maoY + linha * (lado + vao))
        }
        fun posicaoAtual(p: PecaNaMaoN2) = naMao(p) + p.peca.desvio.value + p.peca.arrasto
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun voltar(p: PecaNaMaoN2) {
            val agora = posicaoAtual(p)
            escopo.launch(start = CoroutineStart.UNDISPATCHED) { p.peca.pousar(agora - naMao(p), molinha) }
        }

        fun soltar(p: PecaNaMaoN2) {
            if (!p.peca.arrastando) return
            val centro = posicaoAtual(p) + Offset(peca / 2f, lado / 2f)
            // Vale soltar no lugar tracejado ou em cima da ponta (com folga).
            val perto = Rect(alvo.left - peca * 0.6f, alvo.top - lado * 0.8f, alvo.right + px(20f), alvo.bottom + lado * 0.8f).contains(centro)
            if (terminou || !perto) {
                voltar(p)
                return
            }
            val d = p.domino
            val chave = ponta.chave
            if (d.a.chave == chave || d.b.chave == chave) {
                val virada = d.a.chave != chave
                p.colocada = true
                p.peca.arrastando = false
                p.peca.arrasto = Offset.Zero
                val e = if (virada) EncaixeN2(p.id, d.b, d.a, true) else EncaixeN2(p.id, d.a, d.b, false)
                fileira += e
                if (virada) escopo.launch { e.giro.animateTo(1f, tween(500)) }
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (fileira.size >= domino.jogadas) {
                    terminou = true
                    sons.acerto()
                    escopo.launch {
                        delay(1100)
                        concluir(erros == 0)
                    }
                } else {
                    narrador.falar(falaDaMeiaN2(e.esquerda) + pt("Agora,") + falaDaMeiaN2(e.direita))
                }
            } else {
                erros++
                avisarErro()
                sons.erro()
                voltar(p)
                escopo.launch { p.peca.tremer() }
                escopo.launch { for (x in listOf(8f, -8f, 5f, -5f, 0f)) tremidaPonta.animateTo(x, tween(60)) }
                val dica = if (erros >= ERROS_PARA_AJUDA) "Veja a peça que está brilhando." else "Não combina. Tente outra vez!"
                narrador.falar(falaDaMeiaN2(d.a) + falaDaMeiaN2(d.b) + pt(dica))
            }
        }

        val ladoDp = with(densidade) { lado.toDp() }

        // ---------------------------------------------------------------- mesa e lugar da próxima peça
        Canvas(Modifier.matchParentSize()) {
            val mesa = Rect(0f, fileiraY - px(16f), w, fileiraY + lado + px(16f))
            drawRoundRect(Color(0x22000000), mesa.topLeft + Offset(0f, px(5f)), mesa.size, CornerRadius(px(18f)))
            drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFE5B27A), Color(0xFFC98B4F)), mesa.top, mesa.bottom), mesa.topLeft, mesa.size, CornerRadius(px(18f)))
            // Veios da madeira.
            for (k in 1..3) {
                val y = mesa.top + mesa.height * k / 4f
                drawLine(Color(0x22FFFFFF), Offset(px(10f), y), Offset(w - px(10f), y + px(3f)), strokeWidth = px(2f))
            }
            if (!terminou) {
                val brilha = ajuda
                drawRoundRect(Color.White.copy(alpha = if (brilha) 0.8f else 0.55f), alvo.topLeft, alvo.size, CornerRadius(px(14f)))
                drawRoundRect(
                    if (brilha) Cores.Estrela else Color.White,
                    alvo.topLeft,
                    alvo.size,
                    CornerRadius(px(14f)),
                    style = Stroke(px(3.5f), pathEffect = PathEffect.dashPathEffect(floatArrayOf(px(10f), px(7f)))),
                )
            }
        }
        Box(
            Modifier
                .offset { IntOffset(alvo.left.roundToInt(), alvo.top.roundToInt()) }
                .size(with(densidade) { alvo.width.toDp() }, ladoDp)
                .testTag("domino_alvo"),
        )

        // ---------------------------------------------------------------- a fileira (sai pela esquerda)
        Box(Modifier.matchParentSize().clipToBounds().zIndex(1f)) {
            // Peça inicial: metade lisa e a ponta.
            val primeiraEhPonta = fileira.isEmpty()
            Box(
                Modifier
                    .offset { IntOffset(xDaFileira(0).roundToInt(), fileiraY.roundToInt()) }
                    .graphicsLayer { rotationZ = if (primeiraEhPonta) tremidaPonta.value else 0f },
            ) {
                DesenhoDoDominoN2(null, domino.ponta, ladoDp, cor, pontaLivre = primeiraEhPonta && !terminou, ajuda = ajuda)
            }
            for ((k, e) in fileira.withIndex()) {
                val ultima = k == fileira.lastIndex
                Box(
                    Modifier
                        .offset { IntOffset(xDaFileira(k + 1).roundToInt(), fileiraY.roundToInt()) }
                        .graphicsLayer {
                            // Gira como uma moeda: na metade do giro as metades trocam de lado.
                            scaleX = abs(cos(PI.toFloat() * e.giro.value)).coerceAtLeast(0.05f)
                            rotationZ = if (ultima) tremidaPonta.value else 0f
                        },
                ) {
                    val trocou = e.giro.value >= 0.5f
                    val esq = if (trocou) e.esquerda else e.direita
                    val dir = if (trocou) e.direita else e.esquerda
                    DesenhoDoDominoN2(esq, dir, ladoDp, cor, pontaLivre = ultima && !terminou, ajuda = ajuda)
                }
            }
        }
        // Tocar na ponta faz ouvir o que ela é.
        Box(
            Modifier
                .zIndex(2f)
                .offset { IntOffset((fimDaFileira - lado).roundToInt(), fileiraY.roundToInt()) }
                .size(ladoDp)
                .testTag("domino_ponta")
                .semantics { contentDescription = descricaoN2(ponta.visual, ponta.fala) }
                .pointerInput(domino) {
                    detectTapGestures {
                        if (ativaAgora && !terminou) {
                            sons.toque()
                            interacoes++
                            escopo.launch { for (x in listOf(8f, -8f, 5f, -5f, 0f)) tremidaPonta.animateTo(x, tween(60)) }
                            narrador.falar(falaDaMeiaN2(ponta))
                        }
                    }
                },
        )

        // ---------------------------------------------------------------- a mão
        for (p in mao) {
            if (p.colocada) continue
            val escala by animateFloatAsState(if (p.peca.arrastando) 1.1f else 1f, spring(dampingRatio = 0.5f), label = "escala")
            val destacar = ajuda && p === daVez
            Box(
                Modifier
                    .zIndex(if (p.peca.arrastando) 5f else 3f)
                    .offset { posicaoAtual(p).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } }
                    .testTag("domino_peca_${p.id}")
                    .semantics {
                        contentDescription = descricaoN2(p.domino.a.visual, p.domino.a.fala) + " e " + descricaoN2(p.domino.b.visual, p.domino.b.fala)
                    }
                    .pointerInput(p, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou && !p.colocada) {
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
                            if (ativaAgora && !terminou) {
                                sons.toque()
                                tocouSemArrastar = true
                                interacoes++
                                escopo.launch { p.peca.tremer() }
                                narrador.falar(falaDaMeiaN2(p.domino.a) + falaDaMeiaN2(p.domino.b))
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        rotationZ = p.peca.tremida.value
                    }
                    .then(if (destacar) Modifier.pulsando() else Modifier),
            ) {
                DesenhoDoDominoN2(p.domino.a, p.domino.b, ladoDp, cor, destaque = destacar, erguida = p.peca.arrastando)
            }
        }

        // ---------------------------------------------------------------- mãozinha: mostra o gesto de arrastar
        if (daVez != null) {
            val exemplo = if (ajuda) daVez else null
            val origem = exemplo?.let { naMao(it) + Offset(peca / 2f, lado / 2f) } ?: Offset(w / 2f, maoY - px(26f))
            val destino = alvo.center
            val ninguemArrastando = mao.none { it.peca.arrastando }
            Box(Modifier.matchParentSize().zIndex(6f)) {
                key(exemplo?.id, fileira.size) {
                    GestoDeArrastar(
                        origem = origem,
                        destino = destino,
                        ativa = ativa && !terminou && parada && ninguemArrastando,
                        cor = escurecer(cor),
                        tamanhoCarga = ladoDp * 2,
                    ) {
                        if (exemplo != null) {
                            Box(Modifier.size(ladoDp * 2), contentAlignment = Alignment.Center) {
                                DesenhoDoDominoN2(exemplo.domino.a, exemplo.domino.b, ladoDp, cor, destaque = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Uma peça de dominó deitada: marfim, borda escura, risco no meio com um pino dourado. Metade
 * [esquerda] nula fica lisa (a peça que começa a fileira). [pontaLivre] faz a metade da direita
 * brilhar (é com ela que a próxima peça precisa combinar).
 */
@Composable
private fun DesenhoDoDominoN2(
    esquerda: MeiaPeca?,
    direita: MeiaPeca,
    lado: Dp,
    cor: Color,
    pontaLivre: Boolean = false,
    ajuda: Boolean = false,
    destaque: Boolean = false,
    erguida: Boolean = false,
) {
    val forma = RoundedCornerShape(lado * 0.18f)
    Box(
        Modifier
            .size(lado * 2, lado)
            .shadow(if (erguida) 10.dp else 3.dp, forma)
            .background(if (destaque) BrilhoN2 else Color(0xFFFFFBF0), forma)
            .border(if (destaque) 4.dp else 3.dp, if (destaque) Cores.Estrela else Color(0xFF5B4A73), forma),
    ) {
        Row {
            Box(Modifier.size(lado), contentAlignment = Alignment.Center) {
                if (esquerda != null) VisualN2(esquerda.visual, lado * 0.9f)
            }
            Box(
                Modifier
                    .size(lado)
                    .then(
                        if (pontaLivre) {
                            Modifier
                                .pulsando(ajuda)
                                .border(4.dp, if (ajuda) Cores.Estrela else cor.copy(alpha = 0.8f), forma)
                        } else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                VisualN2(direita.visual, lado * 0.9f)
            }
        }
        Canvas(Modifier.matchParentSize()) {
            val meio = size.width / 2f
            drawLine(Color(0xFF5B4A73), Offset(meio, size.height * 0.12f), Offset(meio, size.height * 0.88f), strokeWidth = 3.dp.toPx())
            drawCircle(Color(0xFFD9A93C), radius = 4.5.dp.toPx(), center = Offset(meio, size.height / 2f))
            drawCircle(Color(0xFFFFE08A), radius = 2.dp.toPx(), center = Offset(meio - 1.dp.toPx(), size.height / 2f - 1.dp.toPx()))
        }
    }
}
