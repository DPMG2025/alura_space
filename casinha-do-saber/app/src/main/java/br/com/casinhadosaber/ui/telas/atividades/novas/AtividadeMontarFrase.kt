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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.MontarFrase
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Stable
private class CartaoDePalavra(val id: Int, val texto: String, /** Posição na frase (ou -1 se é extra). */ val naFrase: Int) {
    val peca = PecaMovel()
    /** Lugar da frase onde está (ou -1 embaixo). */
    var lugar by mutableIntStateOf(-1)
}

/** Fileiras centralizadas de larguras [larguras] em [w]: devolve o canto de cada item e quantas fileiras. */
private fun arrumarEmFileiras(larguras: List<Float>, w: Float, vao: Float, altura: Float, vaoLinha: Float, y0: Float): Pair<List<Offset>, Int> {
    val linhas = mutableListOf<MutableList<Int>>()
    var atual = mutableListOf<Int>()
    var usado = 0f
    for ((i, l) in larguras.withIndex()) {
        val precisa = if (atual.isEmpty()) l else usado + vao + l
        if (atual.isNotEmpty() && precisa > w) {
            linhas += atual
            atual = mutableListOf()
            usado = 0f
        }
        usado = if (atual.isEmpty()) l else usado + vao + l
        atual += i
    }
    if (atual.isNotEmpty()) linhas += atual
    val cantos = MutableList(larguras.size) { Offset.Zero }
    for ((n, linha) in linhas.withIndex()) {
        val total = linha.sumOf { larguras[it].toDouble() }.toFloat() + vao * (linha.size - 1)
        var x = (w - total) / 2f
        for (i in linha) {
            cantos[i] = Offset(x, y0 + n * (altura + vaoLinha))
            x += larguras[i] + vao
        }
    }
    return cantos to linhas.size
}

private class ArranjoDaFrase(
    val fonte: Float,
    val largurasPalavras: List<Float>,
    val largurasCartoes: List<Float>,
    val cantosLugares: List<Offset>,
    val cantosCartoes: List<Offset>,
    val linhasFrase: Int,
    val cartoesY0: Float,
    val alturaLugar: Float,
)

/** Mede as palavras e arruma a linha da frase e os cartões, com a maior letra que cabe na altura [h]. */
private fun arrumarFrase(
    palavras: List<String>,
    cartoes: List<String>,
    w: Float,
    h: Float,
    topo: Float,
    densidade: Float,
    medir: (String, Float) -> Float,
): ArranjoDaFrase {
    fun px(v: Float) = v * densidade
    val vao = px(10f)
    val altura = px(64f)
    var ultimo: ArranjoDaFrase? = null
    // Os lugares da frase podem ficar mais baixos (não são tocados); os cartões ficam sempre com 64 dp.
    for ((fonte, alturaLugar) in listOf(30f to 64f, 26f to 64f, 22f to 64f, 22f to 50f, 19f to 46f).map { (f, a) -> f to px(a) }) {
        fun largura(s: String) = (medir(s, fonte) + px(30f)).coerceIn(px(64f), w)
        val lp = palavras.map { largura(it) }
        val lc = cartoes.map { largura(it) }
        val fraseY = topo + px(12f)
        val (lugares, nl) = arrumarEmFileiras(lp, w, px(8f), alturaLugar, px(10f), fraseY)
        val fimFrase = fraseY + nl * alturaLugar + (nl - 1) * px(10f)
        val y0 = fimFrase + px(34f)
        val (cs, nc) = arrumarEmFileiras(lc, w, vao, altura, vao, y0)
        val fimCartoes = y0 + nc * altura + (nc - 1) * vao
        if (fimCartoes <= h - px(4f)) {
            // Sobra: desce um pouco os cartões para longe da linha (soltar parado não conta).
            val sobra = (h - px(4f) - fimCartoes) * 0.35f
            return ArranjoDaFrase(fonte, lp, lc, lugares, cs.map { it + Offset(0f, sobra) }, nl, y0 + sobra, alturaLugar)
        }
        ultimo = ArranjoDaFrase(fonte, lp, lc, lugares, cs, nl, y0, alturaLugar)
    }
    return ultimo!!
}

/**
 * Montar a frase: os cartões de palavras (com alguns extras) aparecem embaralhados; a criança arrasta
 * cada um para a linha da frase, na ordem. Tocar num cartão faz ouvir a palavra (não conta erro); o
 * alto-falante lê a frase inteira. Quando completa, o narrador lê a frase (no idioma dela) e a figura
 * dança.
 *
 * Erro: cartão solto na linha que não é a próxima palavra: volta tremendo, o narrador diz a palavra e
 * conta um erro. Soltar fora da linha não conta. Depois de 2 erros (ou no exemplo) o cartão certo brilha
 * e o próximo lugar mostra a palavra clarinha.
 *
 * Marcas de teste: "frase_certa_<k>" (k = posição na frase), "frase_extra", "frase_lugar_<k>" e
 * "frase_ouvir".
 */
@Composable
internal fun AtividadeMontarFrase(
    montarFrase: MontarFrase,
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
    val medidor = rememberTextMeasurer()
    val escopo = rememberCoroutineScope()
    val ativaAgora by rememberUpdatedState(ativa)
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    val palavras = montarFrase.palavras
    val idioma = montarFrase.idioma
    val fraseFalada = remember(montarFrase) { listOf(Trecho(montarFrase.frase, idioma)) }
    val cartoes = remember(montarFrase) {
        var embaralhados = (palavras.mapIndexed { k, s -> s to k } + montarFrase.extras.map { it to -1 }).shuffled()
        // Nunca começa já na ordem certa.
        var tentativas = 0
        while (embaralhados.filter { it.second >= 0 }.map { it.second } == palavras.indices.toList() && tentativas++ < 8) {
            embaralhados = embaralhados.shuffled()
        }
        embaralhados.mapIndexed { i, (s, k) -> CartaoDePalavra(i, s, k) }
    }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var parada by remember { mutableStateOf(false) }
    val festa = remember { Animatable(0f) }
    val ouviu = remember { Animatable(1f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val colocados = cartoes.count { it.lugar >= 0 }
    val esperado = palavras.getOrNull(colocados)

    fun ouvirFrase() {
        narrador.falar(fraseFalada)
        escopo.launch {
            ouviu.snapTo(1.12f)
            ouviu.animateTo(1f, spring(dampingRatio = 0.4f))
        }
    }

    LaunchedEffect(interacoes, terminou, ativa, ajuda, colocados) {
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

        // ---------------------------------------------------------------- medidas
        val topo = min(h * 0.22f, px(124f)).coerceAtLeast(px(80f))
        val vao = px(10f)
        val alturaCartao = px(64f)
        // Escolhe o maior tamanho de letra em que tudo cabe na altura (medido uma vez por tamanho de tela).
        val estiloTexto = MaterialTheme.typography.headlineLarge
        val arranjo = remember(w, h, cartoes, estiloTexto) {
            arrumarFrase(palavras, cartoes.map { it.texto }, w, h, topo, densidade.density) { s, f ->
                medidor.measure(s, estiloTexto.copy(fontSize = f.sp, fontWeight = FontWeight.Bold)).size.width.toFloat()
            }
        }
        val fonte = arranjo.fonte
        val largurasPalavras = arranjo.largurasPalavras
        val largurasCartoes = arranjo.largurasCartoes
        val cantosLugares = arranjo.cantosLugares
        val cantosCartoes = arranjo.cantosCartoes
        val linhasFrase = arranjo.linhasFrase
        val cartoesY0 = arranjo.cartoesY0
        val alturaLugar = arranjo.alturaLugar
        val fimDaFrase = topo + px(12f) + linhasFrase * alturaLugar + (linhasFrase - 1) * px(10f)

        fun casa(c: CartaoDePalavra) = cantosCartoes[c.id]
        fun lugarDe(c: CartaoDePalavra) = if (c.lugar >= 0) cantosLugares[c.lugar] else casa(c)
        fun posicaoAtual(c: CartaoDePalavra) = lugarDe(c) + c.peca.desvio.value + c.peca.arrasto
        fun larguraDe(c: CartaoDePalavra) = if (c.lugar >= 0) largurasPalavras[c.lugar] else largurasCartoes[c.id]

        val areaDaFrase = Rect(0f, topo - px(10f), w, fimDaFrase + px(26f))
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun levarPara(c: CartaoDePalavra, novo: Int) {
            val agora = posicaoAtual(c)
            c.lugar = novo
            val destino = lugarDe(c)
            escopo.launch(start = CoroutineStart.UNDISPATCHED) { c.peca.pousar(agora - destino, molinha) }
        }

        fun soltar(c: CartaoDePalavra) {
            if (!c.peca.arrastando) return
            val centro = posicaoAtual(c) + Offset(larguraDe(c) / 2f, alturaCartao / 2f)
            val proximo = cartoes.count { it.lugar >= 0 }
            val certo = palavras.getOrNull(proximo)
            if (terminou || certo == null || !areaDaFrase.contains(centro)) {
                levarPara(c, -1)
                return
            }
            if (c.texto == certo) {
                levarPara(c, proximo)
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (proximo == palavras.lastIndex) {
                    terminou = true
                    escopo.launch {
                        delay(450L)
                        narrador.falar(fraseFalada)
                        festa.animateTo(1f, tween(1400))
                        esperarNarradorN4(narrador, 6000L)
                        concluir(erros == 0)
                    }
                } else {
                    narrador.falar(listOf(Trecho(c.texto, idioma)))
                }
            } else {
                erros++
                avisarErro()
                sons.erro()
                levarPara(c, -1)
                escopo.launch { c.peca.tremer() }
                val dica = if (erros >= ERROS_PARA_AJUDA) pt("Olhe o cartão que está brilhando.") else pt("Não é essa agora. Tente outra!")
                narrador.falar(listOf(Trecho(c.texto, idioma), dica))
            }
        }

        // ---------------------------------------------------------------- topo: figura e alto-falante
        val figura = montarFrase.figura
        val ladoTopo = min(topo * 0.9f, px(110f))
        val ladoOuvir = min(ladoTopo * 0.8f, px(88f)).coerceAtLeast(px(64f))
        val larguraTopo = if (figura != null) ladoTopo * 1.3f + px(16f) + ladoOuvir else ladoOuvir
        val xTopo = (w - larguraTopo) / 2f
        if (figura != null) {
            Box(
                Modifier
                    .offset { IntOffset(xTopo.roundToInt(), ((topo - ladoTopo) / 2f).roundToInt()) }
                    .size((ladoTopo * 1.3f).paraDp(), ladoTopo.paraDp())
                    .graphicsLayer {
                        // A figura dança quando a frase fica pronta.
                        val f = festa.value
                        rotationZ = if (f > 0f && f < 1f) sin(f * PI.toFloat() * 4f) * 10f * (1f - f) else 0f
                        val s = 1f + if (f > 0f && f < 1f) 0.1f * sin(f * PI.toFloat()) else 0f
                        scaleX = s
                        scaleY = s
                    }
                    .shadow(3.dp, RoundedCornerShape(24.dp))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .semantics { contentDescription = "Figura" },
                contentAlignment = Alignment.Center,
            ) {
                Text(figura, fontSize = (ladoTopo.paraDp().value * 0.6f).sp, lineHeight = (ladoTopo.paraDp().value * 0.7f).sp)
            }
        }
        Box(
            Modifier
                .offset { IntOffset((xTopo + larguraTopo - ladoOuvir).roundToInt(), ((topo - ladoOuvir) / 2f).roundToInt()) }
                .size(ladoOuvir.paraDp())
                .testTag("frase_ouvir")
                .semantics { contentDescription = "Ouvir a frase" }
                .pointerInput(montarFrase) {
                    detectTapGestures {
                        sons.toque()
                        interacoes++
                        ouvirFrase()
                    }
                }
                .graphicsLayer {
                    scaleX = ouviu.value
                    scaleY = ouviu.value
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.matchParentSize().offset(y = 5.dp).background(escurecer(cor), CircleShape))
            Box(Modifier.matchParentSize().background(cor, CircleShape))
            AltoFalanteN4(tocando = narrador.falando, modifier = Modifier.size((ladoOuvir * 0.58f).paraDp()))
        }

        // ---------------------------------------------------------------- a linha da frase (caderno)
        Canvas(Modifier.fillMaxSize()) {
            for (n in 0 until linhasFrase) {
                val y = topo + px(12f) + n * (alturaLugar + px(10f)) + alturaLugar + px(3f)
                drawLine(escurecer(cor).copy(alpha = 0.35f), Offset(px(6f), y), Offset(w - px(6f), y), px(3f), StrokeCap.Round)
            }
        }
        for (k in palavras.indices) {
            val proximo = k == colocados && !terminou
            val canto = cantosLugares[k]
            Box(
                Modifier
                    .offset { IntOffset(canto.x.roundToInt(), canto.y.roundToInt()) }
                    .size(largurasPalavras[k].paraDp(), alturaLugar.paraDp())
                    .testTag("frase_lugar_$k")
                    .then(if (proximo && ativa) Modifier.pulsando() else Modifier)
                    .background(if (proximo) Color.White else Color.White.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .border(if (proximo) 4.dp else 2.dp, if (proximo) cor else Color(0xFFD9CFF0), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (proximo && ajuda && esperado != null) {
                    Text(esperado, fontSize = fonte.sp,
                    lineHeight = (fonte * 1.15f).sp, fontWeight = FontWeight.Bold, color = Cores.Texto.copy(alpha = 0.22f), style = estiloTexto, maxLines = 1)
                }
            }
        }

        // ---------------------------------------------------------------- cartões
        val primeiroCerto = cartoes.firstOrNull { it.lugar < 0 && it.texto == esperado }
        for (c in cartoes) {
            val naLinha = c.lugar >= 0
            val escala by animateFloatAsState(if (c.peca.arrastando) 1.1f else 1f, spring(dampingRatio = 0.5f), label = "escala")
            val destacar = ajuda && !terminou && c === primeiroCerto
            val onda = if (festa.value > 0f && naLinha) {
                val fase = festa.value * 2f - c.lugar * (1f / palavras.size)
                if (fase in 0f..1f) sin(fase * PI.toFloat()) else 0f
            } else 0f
            Box(
                Modifier
                    .zIndex(if (c.peca.arrastando) 5f else if (naLinha) 2f else 3f)
                    .offset {
                        val q = posicaoAtual(c)
                        IntOffset(q.x.roundToInt(), (q.y - onda * px(12f)).roundToInt())
                    }
                    .size(larguraDe(c).paraDp(), (if (naLinha) alturaLugar else alturaCartao).paraDp())
                    .testTag(if (c.naFrase >= 0) "frase_certa_${c.naFrase}" else "frase_extra")
                    .semantics { contentDescription = c.texto }
                    .pointerInput(c, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou && c.lugar < 0) {
                                    c.peca.arrastando = true
                                    interacoes++
                                    sons.pegar()
                                    escopo.launch { c.peca.desvio.stop() }
                                }
                            },
                            onDragEnd = { soltar(c) },
                            onDragCancel = { soltar(c) },
                        ) { mudanca, delta ->
                            if (c.peca.arrastando) {
                                mudanca.consume()
                                c.peca.arrasto += delta
                            }
                        }
                    }
                    .pointerInput(c, w, h) {
                        detectTapGestures {
                            if (ativaAgora) {
                                sons.toque()
                                interacoes++
                                if (c.lugar < 0) escopo.launch { c.peca.tremer() }
                                narrador.falar(listOf(Trecho(c.texto, idioma)))
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        rotationZ = c.peca.tremida.value
                    }
                    .brilhoN4(destacar, 16.dp)
                    .shadow(if (c.peca.arrastando) 10.dp else if (naLinha) 1.dp else 3.dp, RoundedCornerShape(16.dp))
                    .background(
                        when {
                            naLinha -> Color(0xFFDFF7E7)
                            destacar -> Color(0xFFFFF3C4)
                            else -> Color.White
                        },
                        RoundedCornerShape(16.dp),
                    )
                    .border(
                        3.dp,
                        when {
                            naLinha -> Cores.Certo
                            destacar -> Cores.Estrela
                            else -> cor
                        },
                        RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    c.texto,
                    fontSize = fonte.sp,
                    lineHeight = (fonte * 1.15f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = Cores.Texto,
                    style = estiloTexto,
                )
            }
        }

        // ---------------------------------------------------------------- mãozinha
        val proximoLugar = if (colocados < palavras.size) cantosLugares[colocados] + Offset(largurasPalavras[colocados] / 2f, alturaLugar / 2f) else null
        val exemplo = if (ajuda) primeiroCerto else null
        val origemDaMao = exemplo?.let { casa(it) + Offset(largurasCartoes[it.id] / 2f, alturaCartao / 2f) }
            ?: Offset(w / 2f, max(fimDaFrase + px(17f), cartoesY0 - px(17f)))
        Box(Modifier.matchParentSize().zIndex(6f)) {
            if (proximoLugar != null) {
                key(exemplo?.id, colocados) {
                    GestoDeArrastar(
                        origem = origemDaMao,
                        destino = proximoLugar,
                        ativa = ativa && !terminou && parada && cartoes.none { it.peca.arrastando },
                        cor = escurecer(cor),
                        tamanhoCarga = (exemplo?.let { largurasCartoes[it.id] } ?: px(72f)).paraDp(),
                    ) {
                        if (exemplo != null) {
                            Box(
                                Modifier
                                    .size(largurasCartoes[exemplo.id].paraDp(), alturaCartao.paraDp())
                                    .background(Color(0xFFFFF3C4), RoundedCornerShape(16.dp))
                                    .border(3.dp, Cores.Estrela, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(exemplo.texto, fontSize = fonte.sp,
                    lineHeight = (fonte * 1.15f).sp, fontWeight = FontWeight.Bold, color = Cores.Texto, style = estiloTexto, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
