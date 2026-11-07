package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import br.com.casinhadosaber.dominio.Ditado
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.FiguraDaPergunta
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.telas.atividades.ContornoDoJogo
import br.com.casinhadosaber.ui.telas.atividades.festejo
import br.com.casinhadosaber.ui.telas.atividades.ladrilho
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

@Stable
private class TeclaDoDitado(val id: Int, val texto: String, /** Posição na palavra (ou -1 se é extra). */ val naPalavra: Int) {
    val peca = PecaMovel()
    /** Casinha onde está (ou -1 no teclado). */
    var casinha by mutableIntStateOf(-1)
    /** De 0 (onde estava) a 1 (dentro da casinha): o voo até a casinha. */
    val voo = Animatable(1f)
    var origem by mutableStateOf(Offset.Zero)
}

/**
 * Ditado: o narrador fala a palavra (o alto-falante grande repete quando a criança quiser) e ela
 * monta a palavra com as teclas grandes de letras ou sílabas: arrastando a tecla até as casinhas ou só
 * tocando nela, como num teclado. A peça certa voa para a próxima casinha e o narrador diz o som dela.
 *
 * Erro: tecla que não é a próxima da palavra (tocada ou solta nas casinhas): volta tremendo, o narrador
 * diz o nome dela e repete a palavra. Soltar fora das casinhas não conta. Depois de 2 erros (ou no
 * exemplo) a tecla certa brilha e a próxima casinha mostra a letra clarinha.
 *
 * Marcas de teste: "ditado_certa_<k>" (k = posição na palavra), "ditado_extra", "ditado_ouvir".
 */
@Composable
internal fun AtividadeDitado(
    ditado: Ditado,
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

    val partes = ditado.partes
    val idioma = ditado.idioma
    val palavraFalada = remember(ditado) { listOf(Trecho(ditado.palavra.lowercase(), idioma)) }
    val teclas = remember(ditado) {
        val todas = partes.mapIndexed { k, s -> s to k } + ditado.extras.map { it to -1 }
        todas.shuffled().mapIndexed { i, (s, k) -> TeclaDoDitado(i, s, k) }
    }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var parada by remember { mutableStateOf(false) }
    val ouviu = remember { Animatable(1f) }
    val festa = remember { Animatable(0f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val preenchidas = teclas.count { it.casinha >= 0 }
    val esperado = partes.getOrNull(preenchidas)

    fun falaDaTecla(t: TeclaDoDitado): Trecho = falaDeTextoN4(t.texto, idioma)

    fun ouvir() {
        narrador.falar(palavraFalada)
        escopo.launch {
            ouviu.snapTo(1.12f)
            ouviu.animateTo(1f, spring(dampingRatio = 0.4f))
        }
    }

    // Depois do enunciado, o narrador diz a palavra.
    LaunchedEffect(ditado, ativa) {
        if (!ativa) return@LaunchedEffect
        delay(500L)
        esperarNarradorN4(narrador, 7000L)
        if (!terminou && interacoes == 0) ouvir()
    }
    // A mãozinha volta a mostrar o gesto quando a criança fica parada.
    LaunchedEffect(interacoes, terminou, ativa, ajuda, preenchidas) {
        parada = false
        if (!ativa || terminou) return@LaunchedEffect
        delay(
            when {
                ajudaInicial && interacoes == 0 -> 1200L
                interacoes == 0 -> 4500L
                ajuda -> 3000L
                else -> 8000L
            },
        )
        parada = true
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density
        fun Float.paraDp() = with(densidade) { this@paraDp.toDp() }

        // ---------------------------------------------------------------- medidas (em pixels)
        val k = partes.size
        val m = teclas.size
        val vao = px(10f)
        val topo = min(h * 0.24f, px(140f)).coerceAtLeast(px(84f))
        val ladoOuvir = min(topo * 0.86f, px(108f)).coerceAtLeast(px(72f))
        // Casinhas: todas do mesmo tamanho, numa fileira só.
        val vaoCasinha = px(8f)
        val larguraCasinha = min((w - vaoCasinha * (k - 1)) / k, px(96f))
        val alturaCasinha = min(larguraCasinha * 1.15f, px(92f)).coerceAtLeast(px(48f))
        val casinhasY = topo + px(14f)
        val casinhasX = (w - (larguraCasinha * k + vaoCasinha * (k - 1))) / 2f
        // Teclado: teclas de pelo menos 64 dp.
        val colunas = min(m, max(3, ((w + vao) / (px(70f) + vao)).toInt()))
        val linhas = ceil(m / colunas.toFloat()).toInt()
        val larguraTecla = min((w - vao * (colunas - 1)) / colunas, px(112f)).coerceAtLeast(px(64f))
        val tecladoY0Min = casinhasY + alturaCasinha + px(30f)
        val alturaTecla = min(larguraTecla * 0.9f, (h - tecladoY0Min - vao * (linhas - 1) - px(6f)) / linhas)
            .coerceIn(px(64f), px(100f))
        val alturaTeclado = linhas * alturaTecla + (linhas - 1) * vao
        val sobra = max(0f, h - tecladoY0Min - alturaTeclado)
        val tecladoY0 = tecladoY0Min + sobra * 0.35f

        fun casinha(i: Int) = Offset(casinhasX + i * (larguraCasinha + vaoCasinha), casinhasY)
        fun noTeclado(t: TeclaDoDitado): Offset {
            val linha = t.id / colunas
            val naLinha = min(colunas, m - linha * colunas)
            val x0 = (w - (naLinha * larguraTecla + (naLinha - 1) * vao)) / 2f
            return Offset(x0 + (t.id % colunas) * (larguraTecla + vao), tecladoY0 + linha * (alturaTecla + vao))
        }
        fun posicao(t: TeclaDoDitado): Offset =
            if (t.casinha < 0) noTeclado(t) + t.peca.desvio.value + t.peca.arrasto
            else t.origem + (casinha(t.casinha) - t.origem) * t.voo.value
        fun tamanho(t: TeclaDoDitado): Pair<Float, Float> =
            if (t.casinha < 0) larguraTecla to alturaTecla
            else {
                val f = t.voo.value
                (larguraTecla + (larguraCasinha - larguraTecla) * f) to (alturaTecla + (alturaCasinha - alturaTecla) * f)
            }

        val areaDasCasinhas = Rect(
            casinhasX - px(28f),
            casinhasY - alturaCasinha * 0.8f,
            casinhasX + larguraCasinha * k + vaoCasinha * (k - 1) + px(28f),
            casinhasY + alturaCasinha * 1.6f,
        )
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun voltar(t: TeclaDoDitado) {
            val agora = posicao(t)
            escopo.launch(start = CoroutineStart.UNDISPATCHED) { t.peca.pousar(agora - noTeclado(t), molinha) }
        }

        /** Tenta pôr a tecla na próxima casinha (tocando ou soltando). */
        fun tentar(t: TeclaDoDitado) {
            if (terminou || t.casinha >= 0) return
            val proxima = teclas.count { it.casinha >= 0 }
            val certo = partes.getOrNull(proxima) ?: return
            if (t.texto.equals(certo, ignoreCase = true)) {
                t.origem = posicao(t)
                t.peca.arrasto = Offset.Zero
                t.peca.arrastando = false
                t.casinha = proxima
                escopo.launch(start = CoroutineStart.UNDISPATCHED) {
                    t.peca.desvio.snapTo(Offset.Zero)
                    t.voo.snapTo(0f)
                    t.voo.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow))
                }
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (proxima == k - 1) {
                    terminou = true
                    escopo.launch {
                        delay(450L)
                        narrador.falar(palavraFalada)
                        festa.animateTo(1f, tween(1100))
                        esperarNarradorN4(narrador, 4000L)
                        concluir(erros == 0)
                    }
                } else {
                    narrador.falar(listOf(falaDaTecla(t)))
                }
            } else {
                erros++
                avisarErro()
                sons.erro()
                voltar(t)
                escopo.launch { t.peca.tremer() }
                val dica = if (erros >= ERROS_PARA_AJUDA) pt("Olhe a peça que está brilhando.") else pt("Escute de novo:")
                narrador.falar(listOf(falaDaTecla(t), pt("Não é essa."), dica) + palavraFalada)
            }
        }

        fun soltar(t: TeclaDoDitado) {
            if (!t.peca.arrastando) return
            val (lw, lh) = tamanho(t)
            val centro = posicao(t) + Offset(lw / 2f, lh / 2f)
            if (!terminou && areaDasCasinhas.contains(centro)) tentar(t)
            if (t.casinha < 0) {
                t.peca.arrastando = false
                if (t.peca.arrasto != Offset.Zero) voltar(t)
            }
        }

        // ---------------------------------------------------------------- topo: ouvir (e a figura, se houver)
        val figura = ditado.figura
        val larguraTopo = if (figura != null) ladoOuvir * 2f + px(20f) else ladoOuvir
        val xTopo = (w - larguraTopo) / 2f
        val yOuvir = (topo - ladoOuvir) / 2f
        if (figura != null) {
            Box(
                Modifier
                    .offset { IntOffset(xTopo.roundToInt(), yOuvir.roundToInt()) }
                    .size(ladoOuvir.paraDp())
                    .graphicsLayer {
                        val pulo = if (festa.value > 0f) 1f + 0.12f * kotlin.math.sin(festa.value * Math.PI.toFloat() * 3f) else 1f
                        scaleX = pulo
                        scaleY = pulo
                    }
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .semantics { contentDescription = "Figura" },
                contentAlignment = Alignment.Center,
            ) {
                FiguraDaPergunta(figura, ladoOuvir.paraDp() * 0.78f) {
                    Text(figura, fontSize = (ladoOuvir.paraDp().value * 0.6f).sp, lineHeight = (ladoOuvir.paraDp().value * 0.7f).sp)
                }
            }
        }
        Box(
            Modifier
                .offset { IntOffset((xTopo + larguraTopo - ladoOuvir).roundToInt(), yOuvir.roundToInt()) }
                .size(ladoOuvir.paraDp())
                .testTag("ditado_ouvir")
                .semantics { contentDescription = "Ouvir a palavra" }
                .pointerInput(ditado) {
                    detectTapGestures {
                        sons.toque()
                        interacoes++
                        ouvir()
                    }
                }
                .graphicsLayer {
                    scaleX = ouviu.value
                    scaleY = ouviu.value
                }
                .then(if (!terminou && ativa && interacoes == 0) Modifier.pulsando() else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            BotaoDeOuvirN4(cor, narrador.falando, (ladoOuvir * 0.58f).paraDp())
        }

        // ---------------------------------------------------------------- casinhas
        val larguraCasinhaDp = larguraCasinha.paraDp()
        val alturaCasinhaDp = alturaCasinha.paraDp()
        val textoCasinha = min(alturaCasinhaDp.value * 0.5f, larguraCasinhaDp.value * (if (partes.maxOf { it.length } > 1) 0.36f else 0.6f)).sp
        for (i in 0 until k) {
            val proxima = i == preenchidas && !terminou
            Box(
                Modifier
                    .offset {
                        val c = casinha(i)
                        IntOffset(c.x.roundToInt(), c.y.roundToInt())
                    }
                    .size(larguraCasinhaDp, alturaCasinhaDp)
                    .then(if (proxima && ativa) Modifier.pulsando() else Modifier)
                    .background(if (proxima) Color.White else Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .border(if (proxima) 4.dp else 3.dp, if (proxima) cor else Color(0xFFD9CFF0), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (proxima && ajuda && esperado != null) {
                    Text(esperado, fontSize = textoCasinha, fontWeight = FontWeight.Bold, color = Cores.Texto.copy(alpha = 0.22f), style = MaterialTheme.typography.headlineLarge)
                }
            }
        }

        // ---------------------------------------------------------------- teclas
        val primeiraCerta = teclas.firstOrNull { it.casinha < 0 && esperado != null && it.texto.equals(esperado, ignoreCase = true) }
        val textoTecla = min(px(64f), alturaTecla).paraDp().value
        for (t in teclas) {
            val (lw, lh) = tamanho(t)
            val naCasinha = t.casinha >= 0
            val escala by animateFloatAsState(if (t.peca.arrastando) 1.12f else 1f, spring(dampingRatio = 0.5f), label = "escala")
            val destacar = ajuda && !terminou && t === primeiraCerta
            val onda = if (festa.value > 0f && naCasinha) {
                val fase = festa.value * 2.2f - t.casinha * (1f / k)
                if (fase in 0f..1f) kotlin.math.sin(fase * Math.PI.toFloat()) else 0f
            } else 0f
            val fonte = if (naCasinha) textoCasinha.value else textoTecla * (if (t.texto.length > 1) 0.5f else 0.66f)
            Box(
                Modifier
                    .zIndex(if (t.peca.arrastando) 5f else if (naCasinha) 2f else 3f)
                    .offset {
                        val q = posicao(t)
                        IntOffset(q.x.roundToInt(), (q.y - onda * px(14f)).roundToInt())
                    }
                    .size(lw.paraDp(), lh.paraDp())
                    .testTag(if (t.naPalavra >= 0) "ditado_certa_${t.naPalavra}" else "ditado_extra")
                    .semantics { contentDescription = t.texto.lowercase() }
                    .pointerInput(t, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou && t.casinha < 0) {
                                    t.peca.arrastando = true
                                    interacoes++
                                    sons.pegar()
                                    escopo.launch { t.peca.desvio.stop() }
                                }
                            },
                            onDragEnd = { soltar(t) },
                            onDragCancel = { soltar(t) },
                        ) { mudanca, delta ->
                            if (t.peca.arrastando) {
                                mudanca.consume()
                                t.peca.arrasto += delta
                            }
                        }
                    }
                    .pointerInput(t, w, h) {
                        detectTapGestures {
                            if (ativaAgora && !terminou && t.casinha < 0) {
                                interacoes++
                                tentar(t)
                            } else if (t.casinha >= 0) {
                                narrador.falar(listOf(falaDaTecla(t)))
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        translationX = t.peca.tremida.value * density
                    }
                    .brilhoN4(destacar, 18.dp)
                    .festejo(naCasinha)
                    .shadow(if (t.peca.arrastando) 10.dp else if (naCasinha) 1.dp else 3.dp, RoundedCornerShape(18.dp))
                    .ladrilho(
                        cor = when {
                            naCasinha -> Cores.Certo
                            destacar -> Cores.Estrela
                            else -> cor
                        },
                        raio = 18.dp,
                        fundo = when {
                            naCasinha -> Color(0xFFDFF7E7)
                            destacar -> Color(0xFFFFF3C4)
                            else -> Color.White
                        },
                        labio = 5.dp,
                        contorno = ContornoDoJogo,
                        larguraContorno = 2.dp,
                    )
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    t.texto,
                    fontSize = fonte.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = Cores.Texto,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }

        // ---------------------------------------------------------------- mãozinha: arrastar (ou tocar) a tecla
        val proximaCasinha = if (preenchidas < k) casinha(preenchidas) + Offset(larguraCasinha / 2f, alturaCasinha / 2f) else null
        val exemplo = if (ajuda) primeiraCerta else null
        val origemDaMao = exemplo?.let { noTeclado(it) + Offset(larguraTecla / 2f, alturaTecla / 2f) }
            ?: Offset(w / 2f, tecladoY0 - px(15f))
        Box(Modifier.matchParentSize().zIndex(6f)) {
            if (proximaCasinha != null) {
                key(exemplo?.id, preenchidas) {
                    GestoDeArrastar(
                        origem = origemDaMao,
                        destino = proximaCasinha,
                        ativa = ativa && !terminou && parada && teclas.none { it.peca.arrastando },
                        cor = escurecer(cor),
                        tamanhoCarga = larguraTecla.paraDp(),
                    ) {
                        if (exemplo != null) {
                            Box(
                                Modifier
                                    .size(larguraTecla.paraDp(), alturaTecla.paraDp())
                                    .background(Color(0xFFFFF3C4), RoundedCornerShape(18.dp))
                                    .border(3.dp, Cores.Estrela, RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(exemplo.texto, fontSize = (textoTecla * 0.6f).sp, fontWeight = FontWeight.Bold, color = Cores.Texto)
                            }
                        }
                    }
                }
            }
        }
    }
}

