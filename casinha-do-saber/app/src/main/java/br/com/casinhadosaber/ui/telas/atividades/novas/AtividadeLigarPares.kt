package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.LigarPares
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Um traço que a criança está puxando: de que lado saiu, de qual item e onde está o dedo. */
private class TracoN2(val daEsquerda: Boolean, val indice: Int, val inicio: Offset) {
    var dedo by mutableStateOf(inicio)
}

/**
 * Ligar pares (regras na data class [LigarPares] e no catálogo de docs/plano-1000-fases.md).
 *
 * Duas colunas de cartões; a criança arrasta o dedo de um cartão até o outro (de qualquer lado para o
 * outro) e uma linha colorida, macia como barbante, segue o dedo. Soltar no par certo deixa a linha
 * desenhada e os dois cartões ficam verdinhos; soltar num cartão errado deixa a linha vermelha um
 * instante, ela some, o narrador diz o nome do cartão tocado e conta um erro. Soltar no vazio só
 * recolhe a linha (não é erro). Tocar num cartão faz ouvir o nome dele.
 * Depois de 2 erros (ou com [ajudaInicial]) o próximo par brilha e a mãozinha liga os dois.
 */
@Composable
internal fun AtividadeLigarPares(
    ligarPares: LigarPares,
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

    val esquerda = ligarPares.esquerda
    val direita = ligarPares.direita
    // Ligados: índice da esquerda -> cor da linha (na ordem em que foram ligados).
    val ligados = remember(ligarPares) { mutableStateMapOf<Int, Color>() }
    var traco by remember(ligarPares) { mutableStateOf<TracoN2?>(null) }
    // A linha errada: fica vermelha e some.
    var erradaDe by remember(ligarPares) { mutableStateOf<Pair<Offset, Offset>?>(null) }
    val someErrada = remember { Animatable(0f) }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var tocouSemArrastar by remember { mutableStateOf(false) }
    val tremidas = remember(ligarPares) { List(esquerda.size + direita.size) { Animatable(0f) } }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN2(ativa, terminou, ajudaInicial, ajuda, interacoes, tocouSemArrastar, ligados.size)
    // O próximo par da ajuda: o primeiro da esquerda ainda solto.
    val proximo = esquerda.indices.firstOrNull { it !in ligados }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas (em pixels)
        val linhasMax = max(esquerda.size, direita.size)
        val vao = px(14f)
        val margem = px(12f)
        // Cartões quadrados; o meio (onde a linha passa) fica com pelo menos 30% da largura.
        val lado = min(min((h - margem * 2 - vao * (linhasMax - 1)) / linhasMax, w * 0.34f), px(132f)).coerceAtLeast(px(64f))
        val xEsq = max(margem, w * 0.06f)
        val xDir = w - xEsq - lado
        fun topo(n: Int, i: Int): Float {
            val total = n * lado + (n - 1) * vao
            return (h - total) / 2f + i * (lado + vao)
        }
        fun cartao(daEsquerda: Boolean, i: Int): Rect {
            val y = topo(if (daEsquerda) esquerda.size else direita.size, i)
            val x = if (daEsquerda) xEsq else xDir
            return Rect(x, y, x + lado, y + lado)
        }
        // Pontinho de onde a linha sai: na borda de dentro do cartão.
        fun pino(daEsquerda: Boolean, i: Int): Offset {
            val r = cartao(daEsquerda, i)
            return if (daEsquerda) Offset(r.right, r.center.y) else Offset(r.left, r.center.y)
        }
        // Acerto de dedo generoso: o cartão inteiro mais uma folga em volta.
        fun achar(p: Offset, daEsquerda: Boolean): Int? {
            val n = if (daEsquerda) esquerda.size else direita.size
            return (0 until n).firstOrNull { cartao(daEsquerda, it).inflate(vao / 2f + px(6f)).contains(p) }
        }
        fun ligadoNaDireita(j: Int) = ligados.keys.any { ligarPares.pares[it] == j }
        fun livre(daEsquerda: Boolean, i: Int) = if (daEsquerda) i !in ligados else !ligadoNaDireita(i)
        fun opcao(daEsquerda: Boolean, i: Int): Opcao = if (daEsquerda) esquerda[i] else direita[i]
        fun tremer(daEsquerda: Boolean, i: Int) {
            val t = tremidas[if (daEsquerda) i else esquerda.size + i]
            escopo.launch { for (x in listOf(10f, -10f, 7f, -7f, 3f, 0f)) t.animateTo(x, tween(55)) }
        }

        fun soltar() {
            val t = traco ?: return
            traco = null
            if (terminou) return
            val alvo = achar(t.dedo, !t.daEsquerda)
            if (alvo == null || !livre(!t.daEsquerda, alvo)) return // soltou no vazio: só recolhe
            val iEsq = if (t.daEsquerda) t.indice else alvo
            val jDir = if (t.daEsquerda) alvo else t.indice
            if (ligarPares.pares[iEsq] == jDir) {
                ligados[iEsq] = PaletaN2[ligados.size % PaletaN2.size]
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (ligados.size == esquerda.size) {
                    terminou = true
                    sons.acerto()
                    escopo.launch {
                        delay(650)
                        concluir(erros == 0)
                    }
                } else {
                    narrador.falar(falaDaOpcaoN2(esquerda[iEsq]) + falaDaOpcaoN2(direita[jDir]))
                }
            } else {
                erros++
                avisarErro()
                sons.erro()
                erradaDe = pino(t.daEsquerda, t.indice) to pino(!t.daEsquerda, alvo)
                escopo.launch {
                    someErrada.snapTo(1f)
                    delay(350)
                    someErrada.animateTo(0f, tween(450))
                    erradaDe = null
                }
                tremer(!t.daEsquerda, alvo)
                val dica = if (erros >= ERROS_PARA_AJUDA) "Veja o par que está brilhando." else "Tente outra vez!"
                narrador.falar(falaDaOpcaoN2(opcao(!t.daEsquerda, alvo)) + pt(dica))
            }
        }

        // ---------------------------------------------------------------- linhas (por baixo dos cartões)
        Canvas(Modifier.matchParentSize()) {
            for ((i, c) in ligados) {
                val j = ligarPares.pares.getValue(i)
                barbante(pino(true, i), pino(false, j), c, 1f)
            }
            erradaDe?.let { (a, b) -> barbante(a, b, Cores.Errado, someErrada.value) }
            traco?.let { barbante(pino(it.daEsquerda, it.indice), it.dedo, cor, 1f) }
        }

        // ---------------------------------------------------------------- cartões
        val ladoDp = with(densidade) { lado.toDp() }
        for (daEsquerda in listOf(true, false)) {
            val itens = if (daEsquerda) esquerda else direita
            for (i in itens.indices) {
                val ligado = !livre(daEsquerda, i)
                val corLinha = if (daEsquerda) ligados[i] else ligados.entries.firstOrNull { ligarPares.pares[it.key] == i }?.value
                val brilha = ajuda && !terminou && proximo != null &&
                    (if (daEsquerda) i == proximo else i == ligarPares.pares[proximo])
                val puxando = traco?.let { it.daEsquerda == daEsquerda && it.indice == i } == true
                val r = cartao(daEsquerda, i)
                val tremida = tremidas[if (daEsquerda) i else esquerda.size + i]
                val o = itens[i]
                Box(
                    Modifier
                        .zIndex(2f)
                        .offset { IntOffset(r.left.roundToInt(), r.top.roundToInt()) }
                        .size(ladoDp)
                        .testTag(if (daEsquerda) "ligar_esquerda_$i" else "ligar_direita_$i")
                        .semantics { contentDescription = descricaoN2(o.visual, o.fala) }
                        .graphicsLayer {
                            rotationZ = tremida.value
                            val e = if (puxando) 1.08f else 1f
                            scaleX = e
                            scaleY = e
                        }
                        .then(if (brilha) Modifier.pulsando() else Modifier)
                        .shadow(if (puxando) 8.dp else 3.dp, RoundedCornerShape(22.dp))
                        .background(
                            when {
                                ligado -> Color(0xFFDFF7E7)
                                brilha -> BrilhoN2
                                else -> Color.White
                            },
                            RoundedCornerShape(22.dp),
                        )
                        .border(
                            if (ligado || brilha) 4.dp else 3.dp,
                            when {
                                ligado -> corLinha ?: Cores.Certo
                                brilha -> Cores.Estrela
                                else -> cor.copy(alpha = 0.55f)
                            },
                            RoundedCornerShape(22.dp),
                        ),
                ) {
                    VisualN2(o.visual, ladoDp)
                }
            }
        }

        // ---------------------------------------------------------------- pinos (onde a linha prende)
        Canvas(Modifier.matchParentSize().zIndex(3f)) {
            for (daEsquerda in listOf(true, false)) {
                val n = if (daEsquerda) esquerda.size else direita.size
                for (i in 0 until n) {
                    val p = pino(daEsquerda, i)
                    val corPino = when {
                        daEsquerda && i in ligados -> ligados.getValue(i)
                        !daEsquerda -> ligados.entries.firstOrNull { ligarPares.pares[it.key] == i }?.value ?: escurecer(cor)
                        else -> escurecer(cor)
                    }
                    drawCircle(Color.White, radius = px(11f), center = p)
                    drawCircle(corPino, radius = px(7.5f), center = p)
                }
            }
        }

        // ---------------------------------------------------------------- dedo: puxar a linha e tocar para ouvir
        Box(
            Modifier
                .matchParentSize()
                .zIndex(4f)
                .pointerInput(ligarPares, w, h) {
                    detectDragGestures(
                        onDragStart = { p ->
                            if (!ativaAgora || terminou) return@detectDragGestures
                            val lado0 = achar(p, true)?.let { true to it } ?: achar(p, false)?.let { false to it }
                            if (lado0 != null && livre(lado0.first, lado0.second)) {
                                traco = TracoN2(lado0.first, lado0.second, p)
                                tocouSemArrastar = false
                                interacoes++
                                sons.pegar()
                            }
                        },
                        onDragEnd = { soltar() },
                        onDragCancel = { traco = null },
                    ) { mudanca, delta ->
                        traco?.let {
                            mudanca.consume()
                            it.dedo = it.dedo + delta
                        }
                    }
                }
                .pointerInput(ligarPares, w, h) {
                    detectTapGestures { p ->
                        if (!ativaAgora || terminou) return@detectTapGestures
                        val achado = achar(p, true)?.let { true to it } ?: achar(p, false)?.let { false to it } ?: return@detectTapGestures
                        sons.toque()
                        interacoes++
                        if (livre(achado.first, achado.second)) tocouSemArrastar = true
                        tremer(achado.first, achado.second)
                        narrador.falar(falaDaOpcaoN2(opcao(achado.first, achado.second)))
                    }
                },
        )

        // ---------------------------------------------------------------- mãozinha: mostra o gesto de ligar
        if (proximo != null) {
            // Com ajuda, liga o par certo; sem ajuda, só puxa a linha até o meio (não entrega a resposta).
            val origem = pino(true, proximo)
            val destino = if (ajuda) cartao(false, ligarPares.pares.getValue(proximo)).center else Offset(w / 2f, origem.y)
            Box(Modifier.matchParentSize().zIndex(6f)) {
                key(proximo, ajuda) {
                    GestoDeArrastar(
                        origem = origem,
                        destino = destino,
                        ativa = ativa && !terminou && parada && traco == null,
                        cor = escurecer(cor),
                        tamanhoCarga = 24.dp,
                    )
                }
            }
        }
    }
}

/** Linha de barbante: uma curva suave com contorno branco e bolinha na ponta. */
private fun DrawScope.barbante(a: Offset, b: Offset, cor: Color, alfa: Float) {
    if (alfa <= 0f) return
    val caminho = Path().apply {
        moveTo(a.x, a.y)
        val meioX = (a.x + b.x) / 2f
        cubicTo(meioX, a.y, meioX, b.y, b.x, b.y)
    }
    val grossura = 9.dp.toPx()
    drawPath(caminho, Color.White.copy(alpha = 0.9f * alfa), style = Stroke(grossura + 6.dp.toPx(), cap = StrokeCap.Round))
    drawPath(caminho, cor.copy(alpha = alfa), style = Stroke(grossura, cap = StrokeCap.Round))
    // Brilhinho por cima, para parecer um cordão.
    drawPath(caminho, Color.White.copy(alpha = 0.35f * alfa), style = Stroke(grossura * 0.3f, cap = StrokeCap.Round))
    drawCircle(Color.White.copy(alpha = alfa), radius = grossura * 0.95f, center = b)
    drawCircle(cor.copy(alpha = alfa), radius = grossura * 0.7f, center = b)
}
