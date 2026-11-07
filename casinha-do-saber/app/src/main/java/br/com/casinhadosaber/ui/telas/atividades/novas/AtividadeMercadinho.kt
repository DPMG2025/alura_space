package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Mercadinho
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.FiguraDaPergunta
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import br.com.casinhadosaber.ui.telas.atividades.PecaMovel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
private class Dinheiro(val id: Int, val valor: Int) {
    val peca = PecaMovel()
    var noBalcao by mutableStateOf(false)
    var ordem by mutableIntStateOf(0)
    val cedula: Boolean get() = valor >= 200
}

/** Tamanho de cada moeda ou cédula, em dp (moedas no tamanho relativo das de verdade). */
private fun tamanhoDoDinheiro(valor: Int): Size = when (valor) {
    5 -> Size(66f, 66f)
    10 -> Size(64f, 64f)
    25 -> Size(76f, 76f)
    50 -> Size(70f, 70f)
    100 -> Size(82f, 82f)
    else -> Size(132f, 68f)
}

/** Cor de fundo e bicho de cada cédula (do jeito das cédulas do real, mas de brinquedo). */
private fun cedulaDe(valor: Int): Pair<Color, String> = when (valor) {
    200 -> Color(0xFF6F9FCB) to "🐢"
    500 -> Color(0xFFA77CC6) to "🐦"
    1000 -> Color(0xFFE0785C) to "🦜"
    2000 -> Color(0xFFF1B53F) to "🐒"
    5000 -> Color(0xFFC88A4C) to "🐆"
    else -> Color(0xFF4F86C6) to "🐟"
}

private val CorBalcao = Color(0xFF8BC48A)
private val CorBalcaoEscuro = Color(0xFF4E8F57)
private val CorCouro = Color(0xFF9C6B43)
private val CorCouroEscuro = Color(0xFF6B4528)

/** Moedas e cédulas que somam [valor], das maiores para as menores (para desenhar o que o cliente pagou). */
private fun decompor(valor: Int): List<Int> {
    val saida = mutableListOf<Int>()
    var resto = valor
    for (v in Mercadinho.VALORES.sortedDescending()) while (resto >= v) {
        saida += v
        resto -= v
    }
    return saida
}

/**
 * Mercadinho: arrastar moedas e cédulas da carteira para o balcão até juntar o valor exato (o preço,
 * ou o troco quando o cliente já pagou).
 * * Acerto: o balcão chega exatamente em [Mercadinho.alvo]; a moeda faz "plim" e a pergunta termina.
 * * Erro: soltar uma moeda que faria passar do valor. Ela volta tremendo para a carteira e o
 *   narrador diz quanto ia dar e quanto é o preço.
 * Tocar numa moeda da carteira diz o nome dela; tocar numa do balcão devolve para a carteira.
 */
@Composable
internal fun AtividadeMercadinho(
    mercadinho: Mercadinho,
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

    val alvo = mercadinho.alvo
    // Cédulas primeiro (ficam em cima na carteira), depois as moedas; a ordem da lista vale para o índice.
    val dinheiro = remember(mercadinho) { mercadinho.dinheiro.mapIndexed { i, v -> Dinheiro(i, v) } }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN3(interacoes, terminou, ativa, ajuda, ajudaInicial)

    val noBalcao = dinheiro.filter { it.noBalcao }.sortedBy { it.ordem }
    val total = noBalcao.sumOf { it.valor }
    val solucao = if (ajuda && !terminou) escolhaQueSomaN3(mercadinho.dinheiro, alvo, noBalcao.map { it.id }.toSet()) else null

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- carteira (embaixo): fileiras que quebram
        val margem = px(12f)
        val vao = px(10f)
        val ordemNaCarteira = dinheiro.sortedWith(compareByDescending<Dinheiro> { it.cedula }.thenByDescending { it.valor })
        fun montarCarteira(escala: Float): Pair<Map<Int, Offset>, Float> {
            val centros = mutableMapOf<Int, Offset>()
            val fileiras = mutableListOf<MutableList<Dinheiro>>(mutableListOf())
            var x = 0f
            for (d in ordemNaCarteira) {
                val lw = px(tamanhoDoDinheiro(d.valor).width) * escala
                if (fileiras.last().isNotEmpty() && x + lw > w - margem * 2) {
                    fileiras += mutableListOf<Dinheiro>()
                    x = 0f
                }
                fileiras.last() += d
                x += lw + vao
            }
            var y = 0f
            for (f in fileiras) {
                val altura = f.maxOf { px(tamanhoDoDinheiro(it.valor).height) * escala }
                val largura = f.sumOf { (px(tamanhoDoDinheiro(it.valor).width) * escala).toDouble() }.toFloat() + vao * (f.size - 1)
                var cx = (w - largura) / 2f
                for (d in f) {
                    val lw = px(tamanhoDoDinheiro(d.valor).width) * escala
                    centros[d.id] = Offset(cx + lw / 2f, y + altura / 2f)
                    cx += lw + vao
                }
                y += altura + vao
            }
            return centros to (y - vao)
        }
        val alturaMaxCarteira = h * 0.44f
        var escalaCarteira = 1f
        var carteira = montarCarteira(1f)
        while (carteira.second > alturaMaxCarteira - px(28f) && escalaCarteira > 0.78f) {
            escalaCarteira -= 0.04f
            carteira = montarCarteira(escalaCarteira)
        }
        val alturaCarteira = carteira.second + px(30f)
        val topoCarteira = h - alturaCarteira
        fun centroNaCarteira(d: Dinheiro) = carteira.first.getValue(d.id) + Offset(0f, topoCarteira + px(16f))

        // ---------------------------------------------------------------- balcão (meio) e loja (em cima)
        val topoBalcao = max(h * 0.34f, topoCarteira - px(150f))
        val balcao = Rect(px(8f), topoBalcao, w - px(8f), topoCarteira - px(10f))
        val escalaNoBalcao = 0.8f
        fun centroNoBalcao(d: Dinheiro): Offset {
            val k = noBalcao.indexOf(d).coerceAtLeast(0)
            val m = noBalcao.size.coerceAtLeast(1)
            val larguras = noBalcao.map { px(tamanhoDoDinheiro(it.valor).width) * escalaNoBalcao }
            val soma = larguras.sum() + vao * (m - 1)
            val util = balcao.width - px(120f)
            // Se não couber, as moedas se sobrepõem um pouco (como num balcão de verdade).
            val aperto = if (soma > util) (util - larguras.sum()) / (m - 1).coerceAtLeast(1) else vao
            var x = balcao.left + px(10f) + (if (soma <= util) (util - soma) / 2f else 0f)
            for (i in 0 until k) x += larguras[i] + aperto
            return Offset(x + larguras.getOrElse(k) { 0f } / 2f, balcao.center.y + px(8f))
        }
        fun descanso(d: Dinheiro) = if (d.noBalcao) centroNoBalcao(d) else centroNaCarteira(d)
        fun posicaoAtual(d: Dinheiro) = descanso(d) + d.peca.desvio.value + d.peca.arrasto
        val areaDeSoltar = Rect(balcao.left, balcao.top - px(60f), balcao.right, balcao.bottom + px(12f))
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun levar(d: Dinheiro, paraOBalcao: Boolean) {
            val antes = dinheiro.associateWith { posicaoAtual(it) }
            if (paraOBalcao) d.ordem = (dinheiro.maxOfOrNull { it.ordem } ?: 0) + 1
            d.noBalcao = paraOBalcao
            val agora = dinheiro.filter { it.noBalcao }.sortedBy { it.ordem }
            // Recalcula o lugar no balcão com a lista nova.
            fun novo(q: Dinheiro): Offset {
                if (!q.noBalcao) return centroNaCarteira(q)
                val larguras = agora.map { px(tamanhoDoDinheiro(it.valor).width) * escalaNoBalcao }
                val m = agora.size
                val soma = larguras.sum() + vao * (m - 1)
                val util = balcao.width - px(120f)
                val aperto = if (soma > util) (util - larguras.sum()) / (m - 1).coerceAtLeast(1) else vao
                var x = balcao.left + px(10f) + (if (soma <= util) (util - soma) / 2f else 0f)
                val k = agora.indexOf(q)
                for (i in 0 until k) x += larguras[i] + aperto
                return Offset(x + larguras[k] / 2f, balcao.center.y + px(8f))
            }
            for (q in dinheiro) {
                if (q !== d && q.peca.arrastando) continue
                val dif = antes.getValue(q) - novo(q)
                if (q === d || dif.getDistance() > 1f) escopo.launch(start = CoroutineStart.UNDISPATCHED) { q.peca.pousar(dif, molinha) }
            }
        }

        fun colocar(d: Dinheiro) {
            val novoTotal = total + d.valor
            when {
                novoTotal > alvo -> {
                    // Passou: a moeda volta para a carteira tremendo.
                    erros++
                    avisarErro()
                    sons.erro()
                    vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
                    levar(d, false)
                    escopo.launch { d.peca.tremer() }
                    narrador.falar(
                        "Com ${reaisPorExtensoN3(d.valor)}, ia dar ${reaisPorExtensoN3(novoTotal)}. Passou! " +
                            (if (mercadinho.troco) "O troco é menos." else "O preço é ${reaisPorExtensoN3(alvo)}."),
                    )
                }
                novoTotal == alvo -> {
                    levar(d, true)
                    sons.moeda()
                    vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    terminou = true
                    escopo.launch {
                        delay(900)
                        concluir(erros == 0)
                    }
                }
                else -> {
                    levar(d, true)
                    sons.moeda()
                    vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    narrador.falar("Já tem ${reaisPorExtensoN3(novoTotal)}.")
                }
            }
        }

        fun devolver(d: Dinheiro) {
            levar(d, false)
            sons.pousar()
            val resta = dinheiro.filter { it.noBalcao }.sumOf { it.valor }
            narrador.falar(if (resta == 0) "O balcão está vazio." else "Ficou ${reaisPorExtensoN3(resta)}.")
        }

        fun soltar(d: Dinheiro) {
            if (!d.peca.arrastando) return
            val dentro = areaDeSoltar.contains(posicaoAtual(d))
            when {
                terminou -> levar(d, d.noBalcao)
                dentro && !d.noBalcao -> colocar(d)
                !dentro && d.noBalcao -> devolver(d)
                else -> levar(d, d.noBalcao)
            }
        }

        fun tocar(d: Dinheiro) {
            if (!ativaAgora || terminou) return
            interacoes++
            if (d.noBalcao) {
                devolver(d)
            } else {
                sons.toque()
                escopo.launch { d.peca.tremer() }
                narrador.falar(reaisPorExtensoN3(d.valor))
            }
        }
        val soltarAgora by rememberUpdatedState<(Dinheiro) -> Unit> { soltar(it) }
        val tocarAgora by rememberUpdatedState<(Dinheiro) -> Unit> { tocar(it) }

        // ---------------------------------------------------------------- cenário: prateleira, balcão e carteira
        Canvas(Modifier.matchParentSize()) {
            desenharLoja(balcao, topoCarteira, w, h)
        }

        // ---------------------------------------------------------------- produto com etiqueta (e o que o cliente pagou)
        val alturaLoja = balcao.top - px(6f)
        val ladoProduto = min(alturaLoja * 0.78f, px(130f))
        val xProduto = if (mercadinho.troco) w * 0.27f else w * 0.36f
        Box(
            Modifier
                .offset { IntOffset((xProduto - ladoProduto / 2f).roundToInt(), (alturaLoja - ladoProduto - px(4f)).roundToInt()) }
                .size(with(densidade) { ladoProduto.toDp() })
                .semantics { contentDescription = mercadinho.nome },
            contentAlignment = Alignment.Center,
        ) {
            val l = ladoProduto / densidade.density
            FiguraDaPergunta(mercadinho.produto, (l * 0.92f).dp) {
                Text(mercadinho.produto, fontSize = (l * 0.72f).sp, lineHeight = (l * 0.84f).sp)
            }
        }
        // Etiqueta pendurada no produto.
        val etiquetaL = px(124f)
        val etiquetaA = px(54f)
        val etiquetaX = xProduto + ladoProduto * 0.32f
        val etiquetaY = (alturaLoja - ladoProduto * 0.98f).coerceAtLeast(px(4f))
        Box(
            Modifier
                .zIndex(1f)
                .offset { IntOffset(etiquetaX.roundToInt(), etiquetaY.roundToInt()) }
                .size(with(densidade) { etiquetaL.toDp() }, with(densidade) { etiquetaA.toDp() })
                .graphicsLayer { rotationZ = -6f },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.matchParentSize()) { desenharEtiqueta() }
            Text(
                reaisN3(mercadinho.preco),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB3261E),
                modifier = Modifier.padding(start = 14.dp),
            )
        }
        if (mercadinho.troco) {
            // O cliente (à direita) já pagou: o dinheiro dele fica na mão, pequeno.
            val pagou = remember(mercadinho) { decompor(mercadinho.pago) }
            Column(
                Modifier
                    .offset { IntOffset((w * 0.56f).roundToInt(), px(6f).roundToInt()) }
                    .size(with(densidade) { (w * 0.42f).toDp() }, with(densidade) { (alturaLoja - px(8f)).toDp() }),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🧒", fontSize = 34.sp, lineHeight = 38.sp)
                Box(
                    Modifier
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("pagou ${reaisN3(mercadinho.pago)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Cores.Texto)
                }
                Box(Modifier.padding(top = 4.dp)) {
                    for ((k, v) in pagou.take(4).withIndex()) {
                        val t = tamanhoDoDinheiro(v)
                        Box(
                            Modifier
                                .offset(x = (k * 18).dp, y = (k * 6).dp)
                                .size((t.width * 0.5f).dp, (t.height * 0.5f).dp),
                        ) { DinheiroDesenhado(v, 0.5f) }
                    }
                }
            }
        }

        // Quanto já tem no balcão (e quanto falta, com ajuda).
        val passouDoMeio = noBalcao.isNotEmpty()
        Column(
            Modifier
                .zIndex(1f)
                .offset { IntOffset((balcao.right - px(112f)).roundToInt(), (balcao.top + px(10f)).roundToInt()) }
                .size(with(densidade) { px(104f).toDp() }, with(densidade) { (balcao.height - px(20f)).toDp() }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Text(if (mercadinho.troco) "troco" else "no balcão", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Box(
                Modifier
                    .background(if (terminou) Cores.Certo else Color.White, RoundedCornerShape(12.dp))
                    .border(3.dp, CorBalcaoEscuro, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    reaisN3(total),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (terminou) Color.White else Cores.Texto,
                )
            }
            if (ajuda && !terminou && passouDoMeio && total < alvo) {
                Text("falta ${reaisN3(alvo - total)}", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Alvo invisível do balcão (os testes automáticos soltam o dinheiro no meio dele).
        Box(
            Modifier
                .offset { IntOffset(balcao.left.roundToInt(), balcao.top.roundToInt()) }
                .size(with(densidade) { (balcao.width - px(120f)).toDp() }, with(densidade) { balcao.height.toDp() })
                .testTag("mercado_balcao"),
        )

        // ---------------------------------------------------------------- moedas e cédulas
        for (d in dinheiro) {
            val t = tamanhoDoDinheiro(d.valor)
            val lw = px(t.width)
            val lh = px(t.height)
            val escala by animateFloatAsState(
                when {
                    d.peca.arrastando -> 1.12f
                    d.noBalcao -> escalaNoBalcao
                    else -> escalaCarteira
                },
                spring(dampingRatio = 0.5f),
                label = "escalaDinheiro",
            )
            val brilhar = solucao != null && d.id in solucao && !d.noBalcao
            val tirar = solucao != null && d.noBalcao && d.id !in solucao
            val forma = if (d.cedula) RoundedCornerShape(10.dp) else CircleShape
            Box(
                Modifier
                    .zIndex(if (d.peca.arrastando) 6f else if (d.noBalcao) 2f + d.ordem * 0.01f else 3f)
                    .offset {
                        val c = posicaoAtual(d)
                        IntOffset((c.x - lw / 2f).roundToInt(), (c.y - lh / 2f).roundToInt())
                    }
                    .size(t.width.dp, t.height.dp)
                    .testTag("mercado_dinheiro_${d.id}")
                    .semantics { contentDescription = reaisPorExtensoN3(d.valor) }
                    .pointerInput(d, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou) {
                                    d.peca.arrastando = true
                                    interacoes++
                                    sons.pegar()
                                    escopo.launch { d.peca.desvio.stop() }
                                }
                            },
                            onDragEnd = { soltarAgora(d) },
                            onDragCancel = { soltarAgora(d) },
                        ) { mudanca, delta ->
                            if (d.peca.arrastando) {
                                mudanca.consume()
                                d.peca.arrasto += delta
                            }
                        }
                    }
                    .pointerInput(d, w, h) { detectTapGestures { tocarAgora(d) } }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        rotationZ = if (d.noBalcao && d.cedula) (d.ordem % 3 - 1) * 4f else 0f
                        translationX = d.peca.tremida.value * density
                    }
                    .then(if (brilhar || tirar) Modifier.pulsando() else Modifier)
                    .then(
                        when {
                            brilhar -> Modifier.border(5.dp, Cores.Estrela, forma)
                            tirar -> Modifier.border(5.dp, Cores.Errado, forma)
                            else -> Modifier
                        },
                    ),
            ) { DinheiroDesenhado(d.valor, 1f) }
        }

        // ---------------------------------------------------------------- mãozinha
        val ninguemArrastando = dinheiro.none { it.peca.arrastando }
        val sobrando = if (solucao != null) noBalcao.firstOrNull { it.id !in solucao } else null
        val exemplo = when {
            sobrando != null -> sobrando
            solucao != null -> ordemNaCarteira.firstOrNull { it.id in solucao && !it.noBalcao }
            else -> ordemNaCarteira.lastOrNull { !it.noBalcao && it.valor <= alvo - total }
        }
        Box(Modifier.matchParentSize().zIndex(7f)) {
            if (exemplo != null) {
                key(exemplo.id, exemplo.noBalcao) {
                    val t = tamanhoDoDinheiro(exemplo.valor)
                    GestoDeArrastar(
                        origem = descanso(exemplo),
                        destino = if (exemplo.noBalcao) centroNaCarteira(exemplo) else Offset(balcao.left + (balcao.width - px(120f)) / 2f, balcao.center.y),
                        ativa = ativa && !terminou && parada && ninguemArrastando,
                        cor = escurecer(cor),
                        tamanhoCarga = max(t.width, t.height).dp,
                    ) {
                        Box(Modifier.size(t.width.dp, t.height.dp)) { DinheiroDesenhado(exemplo.valor, 1f) }
                    }
                }
            }
        }
    }
}

/** Moeda ou cédula desenhada (de brinquedo), ocupando o espaço todo; [escalaTexto] para as miniaturas. */
@Composable
private fun DinheiroDesenhado(valor: Int, escalaTexto: Float) {
    if (valor >= 200) {
        val (fundo, bicho) = cedulaDe(valor)
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.matchParentSize()) {
                val r = CornerRadius(size.height * 0.14f)
                drawRoundRect(escurecer(fundo, 0.7f), cornerRadius = r)
                drawRoundRect(fundo, Offset(size.height * 0.04f, size.height * 0.04f), Size(size.width - size.height * 0.08f, size.height - size.height * 0.08f), r)
                // faixa clara e o medalhão do bicho
                drawRoundRect(
                    Color.White.copy(alpha = 0.35f),
                    Offset(size.width * 0.06f, size.height * 0.62f),
                    Size(size.width * 0.5f, size.height * 0.2f),
                    CornerRadius(size.height * 0.1f),
                )
                drawCircle(Color.White.copy(alpha = 0.55f), size.height * 0.36f, Offset(size.width * 0.74f, size.height * 0.5f))
                drawCircle(escurecer(fundo, 0.75f), size.height * 0.36f, Offset(size.width * 0.74f, size.height * 0.5f), style = Stroke(size.height * 0.04f))
                // ondinhas de enfeite
                for (k in 0..2) {
                    val y = size.height * (0.18f + k * 0.07f)
                    drawLine(Color.White.copy(alpha = 0.25f), Offset(size.width * 0.06f, y), Offset(size.width * 0.5f, y), size.height * 0.02f)
                }
            }
            Text(
                "${valor / 100}",
                fontSize = (30 * escalaTexto).sp,
                lineHeight = (32 * escalaTexto).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = (10 * escalaTexto).dp, bottom = (14 * escalaTexto).dp),
            )
            Text(
                "REAIS",
                fontSize = (10 * escalaTexto).sp,
                lineHeight = (11 * escalaTexto).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = (12 * escalaTexto).dp, bottom = (8 * escalaTexto).dp),
            )
            Text(
                bicho,
                fontSize = (26 * escalaTexto).sp,
                lineHeight = (30 * escalaTexto).sp,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = (9 * escalaTexto).dp),
            )
        }
    } else {
        val (borda, miolo) = when (valor) {
            5 -> Color(0xFFA35A2E) to Color(0xFFD38452)
            10, 25 -> Color(0xFFB98A1F) to Color(0xFFE9BE4A)
            50 -> Color(0xFF8E959E) to Color(0xFFD5DAE0)
            else -> Color(0xFFC79A2A) to Color(0xFFD5DAE0) // 1 real: anel dourado, miolo prateado
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.matchParentSize()) { desenharMoeda(valor, borda, miolo) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (valor == 100) "1" else "$valor",
                    fontSize = ((if (valor == 100) 28 else 22) * escalaTexto).sp,
                    lineHeight = ((if (valor == 100) 28 else 22) * escalaTexto).sp,
                    fontWeight = FontWeight.Bold,
                    color = escurecer(borda, 0.55f),
                )
                Text(
                    if (valor == 100) "REAL" else "centavos",
                    fontSize = (9 * escalaTexto).sp,
                    lineHeight = (10 * escalaTexto).sp,
                    fontWeight = FontWeight.Bold,
                    color = escurecer(borda, 0.55f),
                )
            }
        }
    }
}

private fun DrawScope.desenharMoeda(valor: Int, borda: Color, miolo: Color) {
    val r = size.minDimension / 2f
    val c = center
    drawCircle(Color(0x33000000), r, c + Offset(0f, r * 0.06f))
    drawCircle(escurecer(borda, 0.8f), r * 0.98f, c)
    drawCircle(borda, r * 0.92f, c)
    // serrilhado da borda
    val dentes = 28
    for (k in 0 until dentes) {
        val a = k * 2.0 * Math.PI / dentes
        val dx = kotlin.math.cos(a).toFloat()
        val dy = kotlin.math.sin(a).toFloat()
        drawLine(escurecer(borda, 0.7f), c + Offset(dx * r * 0.84f, dy * r * 0.84f), c + Offset(dx * r * 0.92f, dy * r * 0.92f), r * 0.04f)
    }
    val interno = if (valor == 100) r * 0.66f else r * 0.8f
    drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.6f), miolo), c - Offset(r * 0.3f, r * 0.3f), r * 1.1f), interno, c)
    drawCircle(escurecer(miolo, 0.8f), interno, c, style = Stroke(r * 0.04f))
}

private fun DrawScope.desenharEtiqueta() {
    val w = size.width
    val h = size.height
    val p = Path().apply {
        moveTo(0f, h / 2f)
        lineTo(h * 0.42f, 0f)
        lineTo(w - h * 0.16f, 0f)
        quadraticTo(w, 0f, w, h * 0.16f)
        lineTo(w, h * 0.84f)
        quadraticTo(w, h, w - h * 0.16f, h)
        lineTo(h * 0.42f, h)
        close()
    }
    drawPath(p, Color(0xFFFFF4C9))
    drawPath(p, Color(0xFFD9A441), style = Stroke(h * 0.06f))
    drawCircle(Color(0xFFD9A441), h * 0.1f, Offset(h * 0.36f, h / 2f))
    drawCircle(Color.White, h * 0.05f, Offset(h * 0.36f, h / 2f))
}

/** Prateleira com toldo listrado, balcão verde (onde se paga) e a carteira de couro embaixo. */
private fun DrawScope.desenharLoja(balcao: Rect, topoCarteira: Float, w: Float, h: Float) {
    val d = density
    // toldo listrado no alto
    val faixa = 26f * d
    var x = 0f
    var k = 0
    while (x < w) {
        drawRect(if (k % 2 == 0) Color(0xFFEF6F6C) else Color.White, Offset(x, 0f), Size(faixa, 14f * d))
        drawCircle(if (k % 2 == 0) Color(0xFFEF6F6C) else Color.White, faixa / 2f, Offset(x + faixa / 2f, 14f * d))
        x += faixa
        k++
    }
    // prateleira de madeira onde fica o produto
    drawRoundRect(Color(0xFFB9784A), Offset(w * 0.06f, balcao.top - 14f * d), Size(w * 0.88f, 10f * d), CornerRadius(5f * d))
    // balcão (feltro verde com borda de madeira)
    drawRoundRect(Color(0xFF7E4E2C), balcao.topLeft + Offset(0f, 6f * d), balcao.size, CornerRadius(20f * d))
    drawRoundRect(CorBalcao, balcao.topLeft, balcao.size, CornerRadius(20f * d))
    drawRoundRect(
        CorBalcaoEscuro,
        balcao.topLeft + Offset(8f * d, 8f * d),
        Size(balcao.width - 16f * d, balcao.height - 16f * d),
        CornerRadius(14f * d),
        style = Stroke(3f * d, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f * d, 9f * d))),
    )
    // carteira: couro com costura
    val topo = topoCarteira + 2f * d
    drawRoundRect(CorCouroEscuro, Offset(2f * d, topo + 4f * d), Size(w - 4f * d, h - topo - 4f * d), CornerRadius(22f * d))
    drawRoundRect(CorCouro, Offset(2f * d, topo), Size(w - 4f * d, h - topo - 6f * d), CornerRadius(22f * d))
    drawRoundRect(
        Color(0xFFE8C9A0),
        Offset(10f * d, topo + 8f * d),
        Size(w - 20f * d, h - topo - 22f * d),
        CornerRadius(16f * d),
        style = Stroke(2f * d, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f * d, 6f * d))),
    )
}
