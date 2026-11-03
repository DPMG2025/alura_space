package br.com.casinhadosaber.ui.telas.atividades

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
private class PecaDeSilaba(val id: Int, val texto: String) {
    val peca = PecaMovel()
    /** Encaixe onde está, ou -1 se ainda está embaixo. */
    var encaixe by mutableIntStateOf(-1)
}

/**
 * Montar a palavra: arrastar as sílabas, na ordem, para os encaixes. Há sílabas a mais.
 * Errar = soltar no encaixe uma sílaba que não é a próxima (ela volta e o narrador diz o nome dela).
 * Tocar numa sílaba só faz ouvir o som dela, sem contar como erro.
 */
@Composable
internal fun AtividadeMontarPalavra(
    montar: Atividade.MontarPalavra,
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

    val silabas = montar.silabas
    val pecas = remember(montar) {
        (silabas + montar.extras).shuffled().mapIndexed { i, s -> PecaDeSilaba(i, s) }
    }
    var erros by remember { mutableIntStateOf(0) }
    var arrastou by remember { mutableStateOf(false) }
    var tocouSemArrastar by remember { mutableStateOf(false) }
    var terminou by remember { mutableStateOf(false) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val preenchidos = pecas.count { it.encaixe >= 0 }
    val esperado = silabas.getOrNull(preenchidos)

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas (em pixels)
        val vao = px(10f)
        val k = silabas.size
        val m = pecas.size
        val larguraPeca = min((w - vao * (k - 1)) / k, px(124f))
        val colunas = max(2, min(m, ((w + vao) / (larguraPeca + vao)).toInt()))
        val linhas = ceil(m / colunas.toFloat()).toInt()
        val alturaFigura = min(h * 0.27f, px(140f))
        val livre = h - alturaFigura - px(8f) - px(24f) - vao * (linhas - 1)
        val alturaPeca = min(min(larguraPeca * 0.8f, px(92f)), livre / (1 + linhas)).coerceAtLeast(px(48f))
        val encaixesY = alturaFigura + px(8f)
        val encaixesX = (w - (larguraPeca * k + vao * (k - 1))) / 2f
        val pecasY0 = h - (linhas * alturaPeca + (linhas - 1) * vao)

        fun encaixe(i: Int) = Offset(encaixesX + i * (larguraPeca + vao), encaixesY)
        fun casa(p: PecaDeSilaba): Offset {
            val linha = p.id / colunas
            val naLinha = min(colunas, m - linha * colunas)
            val x0 = (w - (naLinha * larguraPeca + (naLinha - 1) * vao)) / 2f
            return Offset(x0 + (p.id % colunas) * (larguraPeca + vao), pecasY0 + linha * (alturaPeca + vao))
        }
        fun lugar(p: PecaDeSilaba) = if (p.encaixe >= 0) encaixe(p.encaixe) else casa(p)
        fun posicaoAtual(p: PecaDeSilaba) = lugar(p) + p.peca.desvio.value + p.peca.arrasto

        val areaDosEncaixes = Rect(
            encaixesX - px(28f),
            encaixesY - alturaPeca * 0.7f,
            encaixesX + larguraPeca * k + vao * (k - 1) + px(28f),
            encaixesY + alturaPeca * 1.7f,
        )
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun levarPara(p: PecaDeSilaba, novoEncaixe: Int) {
            val agora = posicaoAtual(p)
            p.encaixe = novoEncaixe
            val destino = lugar(p)
            escopo.launch(start = CoroutineStart.UNDISPATCHED) { p.peca.pousar(agora - destino, molinha) }
        }

        fun soltar(p: PecaDeSilaba) {
            if (!p.peca.arrastando) return
            val centro = posicaoAtual(p) + Offset(larguraPeca / 2f, alturaPeca / 2f)
            val proximo = pecas.count { it.encaixe >= 0 }
            val certo = silabas.getOrNull(proximo)
            if (terminou || certo == null || !areaDosEncaixes.contains(centro)) {
                levarPara(p, -1)
                return
            }
            when {
                p.texto == certo -> {
                    levarPara(p, proximo)
                    sons.colocar()
                    vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (proximo == k - 1) {
                        terminou = true
                        escopo.launch {
                            delay(450)
                            concluir(erros == 0)
                        }
                    } else {
                        narrador.falar(p.texto.lowercase())
                    }
                }
                else -> {
                    erros++
                    avisarErro()
                    sons.erro()
                    levarPara(p, -1)
                    escopo.launch { p.peca.tremer() }
                    val dica = if (erros >= ERROS_PARA_AJUDA) "Procure o pedacinho ${certo.lowercase()}, que está brilhando." else "Tente outra vez!"
                    narrador.falar(listOf(pt(p.texto.lowercase() + "."), pt(dica)))
                }
            }
        }

        // ---------------------------------------------------------------- a figura (tocar diz a palavra)
        val alturaFiguraDp = alturaFigura / densidade.density
        Box(
            Modifier
                .offset { IntOffset(((w - alturaFigura * 1.3f) / 2f).roundToInt(), 0) }
                .size(with(densidade) { (alturaFigura * 1.3f).toDp() }, with(densidade) { alturaFigura.toDp() })
                .semantics { contentDescription = montar.palavra.lowercase() }
                .pointerInput(montar) {
                    detectTapGestures {
                        sons.toque()
                        narrador.falar(montar.palavra.lowercase())
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                montar.emoji,
                fontSize = (alturaFiguraDp * 0.68f).sp,
                lineHeight = (alturaFiguraDp * 0.8f).sp,
                modifier = Modifier.pulsando(terminou),
            )
        }

        // ---------------------------------------------------------------- encaixes
        val larguraPecaDp = with(densidade) { larguraPeca.toDp() }
        val alturaPecaDp = with(densidade) { alturaPeca.toDp() }
        val tamanhoTexto = min(alturaPecaDp.value * 0.46f, larguraPecaDp.value * 0.34f).sp
        for (i in 0 until k) {
            val proximo = i == preenchidos && !terminou
            Box(
                Modifier
                    .offset {
                        val e = encaixe(i)
                        IntOffset(e.x.roundToInt(), e.y.roundToInt())
                    }
                    .size(larguraPecaDp, alturaPecaDp)
                    .then(if (proximo && ajuda) Modifier.pulsando() else Modifier)
                    .background(if (proximo) Color.White else Color.White.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .border(
                        if (proximo) 4.dp else 3.dp,
                        if (proximo) cor else Color(0xFFD9CFF0),
                        RoundedCornerShape(18.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Na ajuda, o próximo encaixe mostra a sílaba bem clarinha.
                if (proximo && ajuda && esperado != null) {
                    Text(
                        esperado,
                        fontSize = tamanhoTexto,
                        fontWeight = FontWeight.Bold,
                        color = Cores.Texto.copy(alpha = 0.22f),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
            }
        }

        // ---------------------------------------------------------------- sílabas
        val primeiraCerta = pecas.firstOrNull { it.encaixe < 0 && it.texto == esperado }
        for (p in pecas) {
            val encaixada = p.encaixe >= 0
            val escala by animateFloatAsState(if (p.peca.arrastando) 1.12f else 1f, spring(dampingRatio = 0.5f), label = "escala")
            val destacar = ajuda && !terminou && p === primeiraCerta
            Box(
                Modifier
                    .zIndex(if (p.peca.arrastando) 5f else if (encaixada) 2f else 3f)
                    .offset {
                        val q = posicaoAtual(p)
                        IntOffset(q.x.roundToInt(), q.y.roundToInt())
                    }
                    .size(larguraPecaDp, alturaPecaDp)
                    .testTag(if (p.texto in silabas) "silaba_certa" else "silaba_extra")
                    .semantics { contentDescription = p.texto.lowercase() }
                    .pointerInput(p, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou && p.encaixe < 0) {
                                    p.peca.arrastando = true
                                    arrastou = true
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
                                if (p.encaixe < 0) tocouSemArrastar = true
                                escopo.launch { p.peca.tremer() }
                                narrador.falar(p.texto.lowercase())
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        rotationZ = p.peca.tremida.value
                    }
                    .then(if (destacar) Modifier.pulsando() else Modifier)
                    .shadow(if (p.peca.arrastando) 10.dp else 3.dp, RoundedCornerShape(18.dp))
                    .background(
                        when {
                            encaixada -> Color(0xFFDFF7E7)
                            destacar -> Color(0xFFFFF3C4)
                            else -> Color.White
                        },
                        RoundedCornerShape(18.dp),
                    )
                    .border(
                        3.dp,
                        when {
                            encaixada -> Cores.Certo
                            destacar -> Cores.Estrela
                            else -> cor
                        },
                        RoundedCornerShape(18.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    p.texto,
                    fontSize = tamanhoTexto,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = Cores.Texto,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }

        // ---------------------------------------------------------------- mãozinha: mostra o gesto de arrastar
        val proximoEncaixe = if (preenchidos < k) encaixe(preenchidos) + Offset(larguraPeca / 2f, alturaPeca / 2f) else null
        val origemDaMao = if (ajuda) {
            primeiraCerta?.let { casa(it) + Offset(larguraPeca / 2f, alturaPeca / 2f) }
        } else {
            // Sem ajuda, a mãozinha sai do vão entre as sílabas, para não entregar a resposta.
            Offset(w / 2f, pecasY0 - px(12f))
        }
        val mostrarMao = ativa && !terminou && proximoEncaixe != null && origemDaMao != null && (!arrastou || ajuda)
        Box(Modifier.matchParentSize().zIndex(6f)) {
            key(tocouSemArrastar, ajuda, preenchidos) {
                MaoQueSegue(
                    pontos = listOfNotNull(origemDaMao, proximoEncaixe),
                    ativa = mostrarMao,
                    espera = when {
                        ajudaInicial -> 900L
                        tocouSemArrastar || ajuda -> 1200L
                        else -> 3500L
                    },
                )
            }
        }
    }
}
