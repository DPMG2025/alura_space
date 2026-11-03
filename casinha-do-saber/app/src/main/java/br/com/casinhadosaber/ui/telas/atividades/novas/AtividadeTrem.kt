package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Trem
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.clarear
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.telas.atividades.festejo
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import br.com.casinhadosaber.ui.telas.atividades.PecaMovel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Um vagão solto: [lugarCerto] é o engate dele (-1 para os extras, que não entram no trem). */
@Stable
private class VagaoN2(val id: Int, val lugarCerto: Int, val opcao: Opcao, val cor: Color) {
    val peca = PecaMovel()
    var engate by mutableIntStateOf(-1)
}

/**
 * Trem para ordenar (regras na data class [Trem] e no catálogo de docs/plano-1000-fases.md).
 *
 * A locomotiva (de frente para a esquerda, soltando fumaça) puxa engates vazios, numa ou em duas
 * linhas de trilho, na ordem de leitura; os vagões soltos ficam nos trilhos de baixo. Arrastar um
 * vagão até um engate vazio: se for o lugar dele, engata com um clique e o narrador diz o nome; se
 * não for (ou se for um vagão extra), ele volta tremendo, o narrador diz o nome e conta um erro.
 * Soltar fora do trem só devolve o vagão. Com tudo engatado, o trem apita e sai andando.
 * Depois de 2 erros (ou com [ajudaInicial]) o primeiro engate vazio e o vagão dele brilham e a
 * mãozinha leva um até o outro.
 */
@Composable
internal fun AtividadeTrem(
    trem: Trem,
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

    val n = trem.vagoes.size
    val coresDosVagoes = remember(trem) { List(n + trem.extras.size) { PaletaN2[it % PaletaN2.size] }.shuffled() }
    val soltos = remember(trem) {
        val doTrem = trem.vagoes.indices.filter { it !in trem.fixos }.map { VagaoN2(it, it, trem.vagoes[it], coresDosVagoes[it]) }
        val extras = trem.extras.mapIndexed { k, o -> VagaoN2(n + k, -1, o, coresDosVagoes[n + k]) }
        (doTrem + extras).shuffled()
    }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var tocouSemArrastar by remember { mutableStateOf(false) }
    val saida = remember { Animatable(0f) }
    val engatados = soltos.count { it.engate >= 0 }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN2(ativa, terminou, ajudaInicial, ajuda, interacoes, tocouSemArrastar, engatados)
    val ocupados = soltos.filter { it.engate >= 0 }.map { it.engate }.toSet() + trem.fixos
    val engateDaVez = (0 until n).firstOrNull { it !in ocupados }
    val vagaoDaVez = soltos.firstOrNull { it.lugarCerto == engateDaVez && it.engate < 0 }
    LaunchedEffect(terminou) {
        if (terminou) {
            delay(350)
            saida.animateTo(1f, tween(1500, easing = FastOutLinearInEasing))
        }
    }
    val fumaca = rememberInfiniteTransition(label = "fumaca")
    val sopro by fumaca.animateFloat(0f, 1f, infiniteRepeatable(tween(if (terminou) 700 else 1800, easing = LinearEasing)), label = "sopro")

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas (em pixels)
        val margem = px(10f)
        val vao = px(8f)
        val m = soltos.size
        fun larguraPara(porLinha: Int) = (w - 2 * margem - vao * porLinha) / (porLinha + 1.25f)
        val umaLinha = larguraPara(n) >= px(68f)
        val porLinha = if (umaLinha) n else ceil(n / 2f).toInt()
        val linhasDoTrem = if (umaLinha) 1 else 2
        var vw = min(px(112f), larguraPara(porLinha))
        val trilho = px(14f)
        var colsSoltos = 1
        var linhasSoltos = 1
        while (true) {
            val vh = vw * 0.92f
            colsSoltos = max(1, min(m, ((w - 2 * margem + vao) / (vw + vao)).toInt()))
            linhasSoltos = ceil(m / colsSoltos.toFloat()).toInt()
            val total = (linhasDoTrem + linhasSoltos) * (vh + trilho + px(10f)) + px(30f)
            if (total <= h || vw <= px(60f)) break
            vw -= px(2f)
        }
        val vh = vw * 0.92f
        val loco = vw * 1.25f
        val faixa = vh + trilho + px(10f)
        val sobra = max(0f, h - ((linhasDoTrem + linhasSoltos) * faixa + px(30f)))
        val tremY = px(6f) + sobra * 0.2f
        val soltosY = tremY + linhasDoTrem * faixa + px(30f) + sobra * 0.45f
        val tremX = (w - (loco + vao + porLinha * (vw + vao) - vao)) / 2f
        val primeiroX = tremX + loco + vao

        fun engate(i: Int): Offset {
            val linha = i / porLinha
            return Offset(primeiroX + (i % porLinha) * (vw + vao), tremY + linha * faixa)
        }
        fun casaSolta(v: VagaoN2): Offset {
            val t = soltos.indexOf(v)
            val linha = t / colsSoltos
            val naLinha = min(colsSoltos, m - linha * colsSoltos)
            val x0 = (w - (naLinha * vw + (naLinha - 1) * vao)) / 2f
            return Offset(x0 + (t % colsSoltos) * (vw + vao), soltosY + linha * faixa)
        }
        // Na saída o trem inteiro anda para a esquerda até sumir.
        val andou = saida.value * (primeiroX + porLinha * (vw + vao) + px(20f))
        fun lugar(v: VagaoN2) = if (v.engate >= 0) engate(v.engate) - Offset(andou, 0f) else casaSolta(v)
        fun posicaoAtual(v: VagaoN2) = lugar(v) + v.peca.desvio.value + v.peca.arrasto
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun levar(v: VagaoN2, novo: Int) {
            val agora = posicaoAtual(v)
            v.engate = novo
            val destino = lugar(v)
            escopo.launch(start = CoroutineStart.UNDISPATCHED) { v.peca.pousar(agora - destino, molinha) }
        }

        fun soltar(v: VagaoN2) {
            if (!v.peca.arrastando) return
            val centro = posicaoAtual(v) + Offset(vw / 2f, vh / 2f)
            val alvo = (0 until n)
                .filter { it !in ocupados && Rect(engate(it), Size(vw, vh)).inflate(vw * 0.3f).contains(centro) }
                .minByOrNull { (engate(it) + Offset(vw / 2f, vh / 2f) - centro).getDistance() }
            if (terminou || alvo == null) {
                levar(v, -1)
                return
            }
            if (alvo == v.lugarCerto) {
                levar(v, alvo)
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (soltos.filter { it.lugarCerto >= 0 }.all { it.engate >= 0 }) {
                    terminou = true
                    sons.acerto()
                    escopo.launch {
                        delay(2000)
                        concluir(erros == 0)
                    }
                } else {
                    narrador.falar(falaDaOpcaoN2(v.opcao))
                }
            } else {
                erros++
                avisarErro()
                sons.erro()
                levar(v, -1)
                escopo.launch { v.peca.tremer() }
                val dica = when {
                    erros >= ERROS_PARA_AJUDA -> "Veja o vagão que está brilhando."
                    v.lugarCerto < 0 -> "Esse vagão não entra no trem. Tente outra vez!"
                    else -> "Tente outra vez!"
                }
                narrador.falar(falaDaOpcaoN2(v.opcao) + pt(dica))
            }
        }

        // ---------------------------------------------------------------- trilhos, locomotiva e engates vazios
        val brilhoEngate = if (ajuda && !terminou) engateDaVez else null
        Canvas(Modifier.matchParentSize()) {
            // Paisagem: morrinhos ao longe atrás do trem, e o pátio de grama onde esperam os vagões soltos.
            val baseDoTrem = tremY + linhasDoTrem * faixa
            val patio = soltosY - px(26f)
            if (patio - baseDoTrem > px(40f)) {
                drawOval(Color(0xFFDDEFC8), Offset(-w * 0.25f, baseDoTrem + px(4f)), Size(w * 0.8f, (patio - baseDoTrem) * 2f))
                drawOval(Color(0xFFD2EABB), Offset(w * 0.4f, baseDoTrem + (patio - baseDoTrem) * 0.35f), Size(w * 0.9f, (patio - baseDoTrem) * 2f))
                // Arvorezinhas no morro.
                for (k in 0 until 3) {
                    val x = w * (0.16f + 0.33f * k)
                    val y = baseDoTrem + (patio - baseDoTrem) * (0.55f + 0.12f * (k % 2))
                    drawLine(Color(0xFFB98A5E), Offset(x, y), Offset(x, y - px(16f)), px(5f), cap = StrokeCap.Round)
                    drawCircle(Color(0xFF8FCB72), px(14f), Offset(x, y - px(24f)))
                    drawCircle(Color.White.copy(alpha = 0.25f), px(5f), Offset(x - px(4f), y - px(29f)))
                }
            }
            drawRoundRect(Color(0xFFBFE3A0), Offset(-px(20f), patio), Size(w + px(40f), h - patio + px(40f)), CornerRadius(px(40f)))
            drawRoundRect(Color(0xFFCBEAAE), Offset(-px(20f), patio + px(8f)), Size(w + px(40f), h - patio + px(40f)), CornerRadius(px(40f)))
            for (k in 0 until 12) {
                val x = w * ((k * 37 + 7) % 100) / 100f
                val y = patio + px(16f) + (h - patio - px(24f)) * (((k * 53) % 100) / 100f)
                val folha = Color(0xFF8CC96E)
                drawLine(folha, Offset(x, y), Offset(x - px(3f), y - px(7f)), px(2.2f), cap = StrokeCap.Round)
                drawLine(folha, Offset(x, y), Offset(x + px(3f), y - px(7f)), px(2.2f), cap = StrokeCap.Round)
            }
            for (linha in 0 until linhasDoTrem) trilhos(tremY + linha * faixa + vh + px(2f), trilho)
            for (linha in 0 until linhasSoltos) trilhos(soltosY + linha * faixa + vh + px(2f), trilho)
            // Na linha de baixo do trem, um gancho mostra que os vagões continuam.
            if (linhasDoTrem == 2) {
                val fim = engate(porLinha - 1) + Offset(vw, vh * 0.6f) - Offset(andou, 0f)
                val comeco = engate(porLinha) + Offset(0f, vh * 0.6f) - Offset(andou, 0f)
                val curva = Path().apply {
                    moveTo(fim.x, fim.y)
                    cubicTo(fim.x + vw * 0.5f, fim.y + faixa * 0.55f, comeco.x - vw * 0.7f, comeco.y - faixa * 0.45f, comeco.x, comeco.y)
                }
                drawPath(
                    curva,
                    escurecer(cor).copy(alpha = 0.45f * (1f - saida.value)),
                    style = Stroke(px(4f), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(px(2f), px(9f)))),
                )
            }
            for (i in 0 until n) {
                if (i in ocupados) continue
                val e = engate(i) - Offset(andou, 0f)
                engateVazio(e, vw, vh, if (i == brilhoEngate) Cores.Estrela else escurecer(cor).copy(alpha = 0.5f), i == brilhoEngate)
            }
            locomotiva(Offset(tremX - andou, tremY), loco, vh, cor, sopro, terminou)
        }
        val vwDp = with(densidade) { vw.toDp() }
        val vhDp = with(densidade) { vh.toDp() }
        for (i in 0 until n) {
            Box(
                Modifier
                    .offset { engate(i).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } }
                    .size(vwDp, vhDp)
                    .testTag("trem_engate_$i"),
            )
        }

        // ---------------------------------------------------------------- vagões fixos (pistas)
        for (i in trem.fixos) {
            Box(
                Modifier
                    .zIndex(1f)
                    .offset { (engate(i) - Offset(andou, 0f)).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } }
                    .size(vwDp, vhDp)
                    .semantics { contentDescription = descricaoN2(trem.vagoes[i].visual, trem.vagoes[i].fala) },
            ) {
                DesenhoDoVagaoN2(trem.vagoes[i], coresDosVagoes[i], vwDp, vh / densidade.density, brilho = false)
            }
        }

        // ---------------------------------------------------------------- vagões soltos
        for (v in soltos) {
            val escala by animateFloatAsState(if (v.peca.arrastando) 1.1f else 1f, spring(dampingRatio = 0.5f), label = "escala")
            val destacar = ajuda && !terminou && v === vagaoDaVez
            Box(
                Modifier
                    .zIndex(if (v.peca.arrastando) 5f else if (v.engate >= 0) 2f else 3f)
                    .offset { posicaoAtual(v).let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) } }
                    .size(vwDp, vhDp)
                    .testTag(if (v.lugarCerto >= 0) "trem_vagao_${v.lugarCerto}" else "trem_extra_${v.id - n}")
                    .semantics { contentDescription = descricaoN2(v.opcao.visual, v.opcao.fala) }
                    .pointerInput(v, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou && v.engate < 0) {
                                    v.peca.arrastando = true
                                    tocouSemArrastar = false
                                    interacoes++
                                    sons.pegar()
                                    escopo.launch { v.peca.desvio.stop() }
                                }
                            },
                            onDragEnd = { soltar(v) },
                            onDragCancel = { soltar(v) },
                        ) { mudanca, delta ->
                            if (v.peca.arrastando) {
                                mudanca.consume()
                                v.peca.arrasto += delta
                            }
                        }
                    }
                    .pointerInput(v, w, h) {
                        detectTapGestures {
                            if (ativaAgora && !terminou) {
                                sons.toque()
                                if (v.engate < 0) tocouSemArrastar = true
                                interacoes++
                                escopo.launch { v.peca.tremer() }
                                narrador.falar(falaDaOpcaoN2(v.opcao))
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        translationX = v.peca.tremida.value * density
                    }
                    .festejo(v.engate >= 0)
                    .then(if (destacar) Modifier.pulsando() else Modifier),
            ) {
                DesenhoDoVagaoN2(v.opcao, v.cor, vwDp, vh / densidade.density, brilho = destacar, erguido = v.peca.arrastando)
            }
        }

        // ---------------------------------------------------------------- mãozinha: mostra o gesto de arrastar
        if (engateDaVez != null) {
            val exemplo = if (ajuda) vagaoDaVez else null
            val meio = Offset(vw / 2f, vh / 2f)
            val origem = exemplo?.let { casaSolta(it) + meio } ?: Offset(w / 2f, soltosY - px(16f))
            val destino = engate(engateDaVez) + meio
            val ninguemArrastando = soltos.none { it.peca.arrastando }
            Box(Modifier.matchParentSize().zIndex(6f)) {
                key(exemplo?.id, engatados) {
                    GestoDeArrastar(
                        origem = origem,
                        destino = destino,
                        ativa = ativa && !terminou && parada && ninguemArrastando,
                        cor = escurecer(cor),
                        tamanhoCarga = vwDp,
                    ) {
                        if (exemplo != null) DesenhoDoVagaoN2(exemplo.opcao, exemplo.cor, vwDp, vh / densidade.density, brilho = true)
                    }
                }
            }
        }
    }
}

/** Um vagão: caixa colorida com teto, plaquinha branca com o conteúdo e duas rodas. */
@Composable
private fun DesenhoDoVagaoN2(o: Opcao, cor: Color, largura: androidx.compose.ui.unit.Dp, alturaDp: Float, brilho: Boolean, erguido: Boolean = false) {
    Box(Modifier.size(largura, alturaDp.dp), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.matchParentSize()) {
            val lw = size.width
            val lh = size.height
            val corpo = Rect(lw * 0.04f, lh * 0.1f, lw * 0.96f, lh * 0.8f)
            if (erguido) drawRoundRect(Color(0x33000000), Offset(corpo.left + 4.dp.toPx(), corpo.top + 8.dp.toPx()), corpo.size, CornerRadius(lw * 0.1f))
            // Engates dos dois lados.
            drawLine(Color(0xFF5B4A73), Offset(0f, lh * 0.66f), Offset(lw, lh * 0.66f), strokeWidth = lh * 0.06f, cap = StrokeCap.Round)
            if (brilho) drawRoundRect(Cores.Estrela, corpo.topLeft - Offset(5.dp.toPx(), 5.dp.toPx()), Size(corpo.width + 10.dp.toPx(), corpo.height + 10.dp.toPx()), CornerRadius(lw * 0.14f))
            drawRoundRect(escurecer(cor, 0.82f), corpo.topLeft, corpo.size, CornerRadius(lw * 0.1f))
            drawRoundRect(cor, corpo.topLeft, Size(corpo.width, corpo.height * 0.86f), CornerRadius(lw * 0.1f))
            // Teto com beiral.
            drawRoundRect(escurecer(cor, 0.7f), Offset(lw * 0.01f, lh * 0.04f), Size(lw * 0.98f, lh * 0.1f), CornerRadius(lh * 0.05f))
            drawRoundRect(clarear(cor, 0.35f), Offset(lw * 0.06f, lh * 0.055f), Size(lw * 0.88f, lh * 0.03f), CornerRadius(lh * 0.02f))
            // Rebites.
            for (x in listOf(0.11f, 0.89f)) {
                drawCircle(clarear(cor, 0.5f), radius = lh * 0.025f, center = Offset(lw * x, lh * 0.22f))
                drawCircle(clarear(cor, 0.5f), radius = lh * 0.025f, center = Offset(lw * x, lh * 0.7f))
            }
            // Plaquinha.
            val placa = Rect(lw * 0.17f, lh * 0.17f, lw * 0.83f, lh * 0.73f)
            drawRoundRect(Color.White, placa.topLeft, placa.size, CornerRadius(lw * 0.08f))
            // Rodas.
            for (x in listOf(0.27f, 0.73f)) {
                val c = Offset(lw * x, lh * 0.86f)
                drawCircle(Color(0xFF3D2C5E), radius = lh * 0.13f, center = c)
                drawCircle(Color(0xFFB8B0C8), radius = lh * 0.06f, center = c)
                drawCircle(Color(0xFF3D2C5E), radius = lh * 0.025f, center = c)
            }
        }
        val lado = min(largura.value * 0.66f, alturaDp * 0.56f)
        Box(Modifier.offset(y = (alturaDp * 0.45f - lado / 2f).dp)) {
            VisualN2(o.visual, lado.dp)
        }
    }
}

/** Dois trilhos com dormentes de madeira, ocupando a largura toda na altura [y]. */
private fun DrawScope.trilhos(y: Float, altura: Float) {
    val passo = altura * 1.6f
    var x = -passo / 2f
    while (x < size.width) {
        drawRoundRect(Color(0xFFB9875A), Offset(x, y + altura * 0.15f), Size(altura * 0.6f, altura * 0.85f), CornerRadius(altura * 0.15f))
        x += passo
    }
    drawLine(Color(0xFF8F8AA0), Offset(0f, y + altura * 0.3f), Offset(size.width, y + altura * 0.3f), strokeWidth = altura * 0.2f)
    drawLine(Color(0xFFCFCBDA), Offset(0f, y + altura * 0.25f), Offset(size.width, y + altura * 0.25f), strokeWidth = altura * 0.07f)
}

/** Engate vazio: o contorno tracejado de um vagão com as rodinhas apagadas. */
private fun DrawScope.engateVazio(canto: Offset, lw: Float, lh: Float, cor: Color, brilho: Boolean) {
    val corpo = Rect(canto.x + lw * 0.04f, canto.y + lh * 0.1f, canto.x + lw * 0.96f, canto.y + lh * 0.8f)
    drawLine(Color(0xFF5B4A73).copy(alpha = 0.5f), Offset(canto.x, canto.y + lh * 0.66f), Offset(canto.x + lw, canto.y + lh * 0.66f), strokeWidth = lh * 0.05f, cap = StrokeCap.Round)
    drawRoundRect(Color.White.copy(alpha = if (brilho) 0.85f else 0.6f), corpo.topLeft, corpo.size, CornerRadius(lw * 0.1f))
    if (brilho) drawRoundRect(Cores.Estrela.copy(alpha = 0.35f), corpo.topLeft, corpo.size, CornerRadius(lw * 0.1f))
    drawRoundRect(
        cor,
        corpo.topLeft,
        corpo.size,
        CornerRadius(lw * 0.1f),
        style = Stroke(if (brilho) 4.dp.toPx() else 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 6.dp.toPx()))),
    )
    for (x in listOf(0.27f, 0.73f)) {
        drawCircle(cor.copy(alpha = 0.35f), radius = lh * 0.12f, center = Offset(canto.x + lw * x, canto.y + lh * 0.86f))
    }
}

/**
 * Locomotiva de frente para a esquerda (para onde vai), com cabine do lado dos vagões, chaminé
 * soltando fumaça ([sopro] de 0 a 1) e limpa trilhos. [canto] é o canto de cima à esquerda.
 */
private fun DrawScope.locomotiva(canto: Offset, lw: Float, lh: Float, cor: Color, sopro: Float, apitando: Boolean) {
    val x = canto.x
    val y = canto.y
    val escura = escurecer(cor, 0.7f)
    // Fumaça: três nuvenzinhas subindo e crescendo.
    for (k in 0 until 3) {
        val f = (sopro + k / 3f) % 1f
        val c = Offset(x + lw * (0.24f - 0.1f * f), y + lh * (0.12f - 0.45f * f))
        drawCircle(Color.White.copy(alpha = (if (apitando) 0.95f else 0.8f) * (1f - f)), radius = lh * (0.07f + 0.1f * f), center = c)
    }
    // Caldeira.
    drawRoundRect(escura, Offset(x + lw * 0.08f, y + lh * 0.36f), Size(lw * 0.62f, lh * 0.44f), CornerRadius(lh * 0.2f))
    drawRoundRect(cor, Offset(x + lw * 0.08f, y + lh * 0.36f), Size(lw * 0.62f, lh * 0.36f), CornerRadius(lh * 0.18f))
    drawRoundRect(clarear(cor, 0.4f), Offset(x + lw * 0.14f, y + lh * 0.4f), Size(lw * 0.5f, lh * 0.06f), CornerRadius(lh * 0.03f))
    // Faixas douradas na caldeira.
    for (fx in listOf(0.3f, 0.48f)) {
        drawRect(Cores.Estrela, Offset(x + lw * fx, y + lh * 0.36f), Size(lw * 0.04f, lh * 0.4f))
    }
    // Chaminé.
    drawRect(Color(0xFF3D2C5E), Offset(x + lw * 0.19f, y + lh * 0.16f), Size(lw * 0.1f, lh * 0.22f))
    drawRoundRect(Color(0xFF3D2C5E), Offset(x + lw * 0.15f, y + lh * 0.12f), Size(lw * 0.18f, lh * 0.07f), CornerRadius(lh * 0.03f))
    // Farol na frente.
    drawCircle(Cores.Estrela, radius = lh * 0.07f, center = Offset(x + lw * 0.09f, y + lh * 0.48f))
    drawCircle(Color.White, radius = lh * 0.03f, center = Offset(x + lw * 0.08f, y + lh * 0.47f))
    // Cabine com janela e telhado.
    drawRoundRect(escura, Offset(x + lw * 0.6f, y + lh * 0.08f), Size(lw * 0.4f, lh * 0.72f), CornerRadius(lh * 0.06f))
    drawRoundRect(cor, Offset(x + lw * 0.62f, y + lh * 0.14f), Size(lw * 0.36f, lh * 0.6f), CornerRadius(lh * 0.05f))
    drawRoundRect(Color(0xFF3D2C5E), Offset(x + lw * 0.56f, y + lh * 0.03f), Size(lw * 0.47f, lh * 0.09f), CornerRadius(lh * 0.04f))
    drawRoundRect(Color(0xFFDFF4FF), Offset(x + lw * 0.68f, y + lh * 0.2f), Size(lw * 0.24f, lh * 0.2f), CornerRadius(lh * 0.04f))
    // Limpa trilhos na frente.
    val bico = Path().apply {
        moveTo(x + lw * 0.1f, y + lh * 0.72f)
        lineTo(x, y + lh * 0.9f)
        lineTo(x + lw * 0.16f, y + lh * 0.9f)
        close()
    }
    drawPath(bico, Color(0xFFFF6B6B))
    // Rodas: duas pequenas na frente, uma grande atrás, com a biela.
    val rodas = listOf(Triple(0.25f, 0.85f, 0.1f), Triple(0.45f, 0.85f, 0.1f), Triple(0.78f, 0.8f, 0.16f))
    for ((rx, ry, rr) in rodas) {
        val c = Offset(x + lw * rx, y + lh * ry)
        drawCircle(Color(0xFF3D2C5E), radius = lh * rr, center = c)
        drawCircle(Color(0xFFB8B0C8), radius = lh * rr * 0.5f, center = c)
    }
    drawLine(Color(0xFFB8B0C8), Offset(x + lw * 0.25f, y + lh * 0.85f), Offset(x + lw * 0.78f, y + lh * 0.8f), strokeWidth = lh * 0.035f, cap = StrokeCap.Round)
    // Engate para o primeiro vagão.
    drawLine(Color(0xFF5B4A73), Offset(x + lw * 0.98f, y + lh * 0.66f), Offset(x + lw * 1.08f, y + lh * 0.66f), strokeWidth = lh * 0.06f, cap = StrokeCap.Round)
}
