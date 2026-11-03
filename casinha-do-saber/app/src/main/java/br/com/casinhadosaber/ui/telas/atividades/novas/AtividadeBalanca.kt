package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import br.com.casinhadosaber.dominio.Balanca
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import br.com.casinhadosaber.ui.telas.atividades.PecaMovel
import br.com.casinhadosaber.ui.telas.atividades.figurasDe
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin

@Stable
private class PesoDaBalanca(val id: Int, val valor: Int) {
    val peca = PecaMovel()
    var noPrato by mutableStateOf(false)
    /** Ordem de chegada no prato (para empilhar). */
    var ordem by mutableIntStateOf(0)
}

private val CorMadeira = Color(0xFFB9784A)
private val CorMadeiraEscura = Color(0xFF7E4E2C)
private val CorMadeiraClara = Color(0xFFE6B98F)
private val CorLatao = Color(0xFFE8B84A)
private val CorLataoEscuro = Color(0xFFA9781C)
private val CorLataoClaro = Color(0xFFFFE29A)

/** Cores dos pesos: cada valor tem a sua (o 3 é sempre do mesmo jeito, ajuda a lembrar). */
private val CoresDosPesos = listOf(
    Color(0xFF4FA3E0), Color(0xFFEF6F6C), Color(0xFF59C173), Color(0xFFF2A541),
    Color(0xFF9B7BE0), Color(0xFF2EC4B6), Color(0xFFE76FB0), Color(0xFF8D6E63),
    Color(0xFF5C7CFA), Color(0xFFD9822B),
)

private fun corDoPeso(valor: Int) = CoresDosPesos[(valor - 1).mod(CoresDosPesos.size)]

/** Inclinação máxima do travessão, em graus. */
private const val INCLINACAO_MAXIMA = 11f

/**
 * Balança de dois pratos: o da esquerda já vem com peso; a criança arrasta pesos da prateleira para
 * o prato da direita até a balança ficar reta (soma igual). O travessão inclina com uma mola macia
 * (balança um pouquinho antes de parar) e os pratos acompanham.
 * * Acerto: soma igual ao lado esquerdo; a balança fica reta, o ponteiro fica verde e a pergunta termina.
 * * Erro: soltar um peso que faz passar do lado esquerdo. A balança pende para a direita, o
 *   narrador diz quanto ficou e pede para tirar um peso (arrastar de volta ou tocar no peso do prato).
 * Depois de [ERROS_PARA_AJUDA] erros, os pesos de uma resposta certa brilham (e o que sobra no prato
 * pisca em vermelho) e a mãozinha mostra o gesto.
 */
@Composable
internal fun AtividadeBalanca(
    balanca: Balanca,
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

    val numeros = balanca.mostrarNumeros
    val alvo = balanca.alvo
    val pesos = remember(balanca) { balanca.pesos.mapIndexed { i, v -> PesoDaBalanca(i, v) } }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN3(interacoes, terminou, ativa, ajuda, ajudaInicial)

    val noPrato = pesos.filter { it.noPrato }.sortedBy { it.ordem }
    val somaDireita = noPrato.sumOf { it.valor }
    val diferenca = somaDireita - alvo
    val angulo = remember(balanca) { Animatable(-INCLINACAO_MAXIMA) }
    LaunchedEffect(diferenca) {
        // Qualquer diferença já inclina bem (dá para ver), e diferenças maiores inclinam mais.
        val alvoAngulo = if (diferenca == 0) 0f
        else sign(diferenca.toFloat()) * INCLINACAO_MAXIMA * (0.45f + 0.55f * min(1f, kotlin.math.abs(diferenca) / alvo.toFloat()))
        angulo.animateTo(alvoAngulo, spring(dampingRatio = 0.32f, stiffness = 45f))
    }
    val solucao = if (ajuda && !terminou) escolhaQueSomaN3(balanca.pesos, alvo, noPrato.map { it.id }.toSet()) else null

    fun nomeDoValor(v: Int) = Vocabulario.porExtenso(v)

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas (em pixels)
        val n = pesos.size
        val porLinha = if (n <= 4) n else ceil(n / 2f).toInt()
        val linhas = ceil(n / porLinha.toFloat()).toInt()
        val lado = min(px(88f), (w - px(16f)) / porLinha * 0.88f).coerceAtLeast(px(64f))
        val alturaPrateleira = linhas * lado * 1.1f + px(22f)
        val topoPrateleira = h - alturaPrateleira
        val regiao = topoPrateleira - px(6f)
        val pivo = Offset(w / 2f, regiao * 0.2f)
        val braco = min(w * 0.29f, px(210f))
        val larguraPrato = min(w * 0.37f, px(230f))
        val corda = regiao * 0.66f - pivo.y
        val ladoNoPrato = lado * 0.62f
        val porFileiraNoPrato = (larguraPrato * 0.96f / (ladoNoPrato * 1.02f)).toInt().coerceIn(2, 4)

        val rad = angulo.value * PI.toFloat() / 180f
        // Os pratos balançam um tiquinho para o lado contrário do movimento (estão pendurados).
        val balanco = (-angulo.velocity * px(0.35f)).coerceIn(-px(12f), px(12f))
        val pontaEsquerda = pivo + Offset(-braco * cos(rad), -braco * sin(rad))
        val pontaDireita = pivo + Offset(braco * cos(rad), braco * sin(rad))
        val pratoEsquerdo = pontaEsquerda + Offset(balanco, corda)
        val pratoDireito = pontaDireita + Offset(balanco, corda)

        fun centroNoPrato(base: Offset, k: Int, total: Int): Offset {
            val fileira = k / porFileiraNoPrato
            val coluna = k % porFileiraNoPrato
            val naFileira = min(porFileiraNoPrato, total - fileira * porFileiraNoPrato)
            val x = base.x + (coluna - (naFileira - 1) / 2f) * ladoNoPrato * 1.02f + (if (fileira % 2 == 1) ladoNoPrato * 0.18f else 0f)
            val y = base.y - ladoNoPrato / 2f - px(4f) - fileira * ladoNoPrato * 0.9f
            return Offset(x, y)
        }

        fun centroNaPrateleira(i: Int): Offset {
            val linha = i / porLinha
            val naLinha = min(porLinha, n - linha * porLinha)
            val passo = min(w / porLinha, lado * 1.16f)
            val coluna = i % porLinha
            val x = w / 2f + (coluna - (naLinha - 1) / 2f) * passo
            val y = topoPrateleira + px(10f) + lado * 0.55f + linha * lado * 1.1f
            return Offset(x, y)
        }

        fun descanso(p: PesoDaBalanca): Offset =
            if (p.noPrato) centroNoPrato(pratoDireito, noPrato.indexOf(p).coerceAtLeast(0), noPrato.size) else centroNaPrateleira(p.id)

        fun posicaoAtual(p: PesoDaBalanca) = descanso(p) + p.peca.desvio.value + p.peca.arrasto

        // Área do prato da direita para soltar (larga, pela altura toda acima do prato).
        val pratoParado = pivo + Offset(braco, corda)
        val areaDoPrato = Rect(
            pratoParado.x - larguraPrato * 0.7f,
            pivo.y + px(10f),
            pratoParado.x + larguraPrato * 0.7f,
            pratoParado.y + px(46f),
        )
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        /** Muda o peso de lugar (prato ou prateleira); quem estava no prato se rearruma deslizando. */
        fun levar(p: PesoDaBalanca, paraOPrato: Boolean) {
            val antes = pesos.associateWith { posicaoAtual(it) }
            if (paraOPrato) p.ordem = (pesos.maxOfOrNull { it.ordem } ?: 0) + 1
            p.noPrato = paraOPrato
            val agora = pesos.filter { it.noPrato }.sortedBy { it.ordem }
            fun novoDescanso(q: PesoDaBalanca) =
                if (q.noPrato) centroNoPrato(pratoDireito, agora.indexOf(q), agora.size) else centroNaPrateleira(q.id)
            for (q in pesos) {
                if (q !== p && q.peca.arrastando) continue
                val dif = antes.getValue(q) - novoDescanso(q)
                if (q === p || dif.getDistance() > 1f) escopo.launch(start = CoroutineStart.UNDISPATCHED) { q.peca.pousar(dif, molinha) }
            }
        }

        fun conferir() {
            val soma = pesos.filter { it.noPrato }.sumOf { it.valor }
            when {
                soma == alvo -> {
                    terminou = true
                    escopo.launch {
                        // Espera a balança assentar reta antes de comemorar.
                        delay(1100)
                        concluir(erros == 0)
                    }
                }
                soma > alvo -> {
                    erros++
                    avisarErro()
                    sons.erro()
                    vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
                    narrador.falar(
                        if (numeros) "Passou! Tem ${nomeDoValor(soma)} aqui e ${nomeDoValor(alvo)} do outro lado. Tire um peso."
                        else "Ficou pesado demais! Tire um peso.",
                    )
                }
                else -> {
                    sons.colocar()
                    if (numeros) narrador.falar(nomeDoValor(soma))
                }
            }
        }

        fun soltar(p: PesoDaBalanca) {
            if (!p.peca.arrastando) return
            val c = posicaoAtual(p)
            val dentro = areaDoPrato.contains(c)
            when {
                terminou -> levar(p, p.noPrato)
                dentro && !p.noPrato -> {
                    vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    levar(p, true)
                    conferir()
                }
                !dentro && p.noPrato -> {
                    levar(p, false)
                    sons.pousar()
                    if (numeros) narrador.falar(nomeDoValor(pesos.filter { it.noPrato }.sumOf { it.valor }))
                }
                else -> levar(p, p.noPrato)
            }
        }

        fun tocar(p: PesoDaBalanca) {
            if (!ativaAgora || terminou) return
            interacoes++
            if (p.noPrato) {
                // Tocar no peso do prato tira ele (jeito fácil de desfazer).
                levar(p, false)
                sons.pousar()
                if (numeros) narrador.falar(nomeDoValor(pesos.filter { it.noPrato }.sumOf { it.valor }))
            } else {
                sons.toque()
                escopo.launch { p.peca.tremer() }
                narrador.falar(if (numeros) nomeDoValor(p.valor) else "${nomeDoValor(p.valor)}, arraste para o prato")
            }
        }
        // Os gestos guardam a versão mais nova (a posição dos pratos muda com a inclinação).
        val soltarAgora by rememberUpdatedState<(PesoDaBalanca) -> Unit> { soltar(it) }
        val tocarAgora by rememberUpdatedState<(PesoDaBalanca) -> Unit> { tocar(it) }

        // ---------------------------------------------------------------- balança (atrás dos pesos)
        val reta = terminou || (diferenca == 0 && noPrato.isNotEmpty())
        Canvas(Modifier.matchParentSize()) {
            desenharPrateleira(topoPrateleira, w, h)
            desenharBalanca(pivo, regiao, braco, angulo.value, pontaEsquerda, pontaDireita, pratoEsquerdo, pratoDireito, larguraPrato, reta)
        }

        // ---------------------------------------------------------------- lado esquerdo (já pesado, não mexe)
        val esquerda = balanca.ladoEsquerdo
        for ((k, v) in esquerda.withIndex()) {
            val c = centroNoPrato(pratoEsquerdo, k, esquerda.size)
            Box(
                Modifier
                    .zIndex(1f)
                    .offset { IntOffset((c.x - lado / 2f).roundToInt(), (c.y - lado / 2f).roundToInt()) }
                    .size(with(densidade) { lado.toDp() })
                    .graphicsLayer {
                        scaleX = 0.62f
                        scaleY = 0.62f
                    },
            ) { PesoDesenhado(v, balanca.figura, numeros, lado / densidade.density) }
        }

        // ---------------------------------------------------------------- pesos que se arrastam
        for (p in pesos) {
            val escala by animateFloatAsState(
                when {
                    p.peca.arrastando -> 1.12f
                    p.noPrato -> 0.62f
                    else -> 1f
                },
                spring(dampingRatio = 0.5f),
                label = "escalaPeso",
            )
            val brilhar = solucao != null && p.id in solucao && !p.noPrato
            val tirar = solucao != null && p.noPrato && p.id !in solucao
            Box(
                Modifier
                    .zIndex(if (p.peca.arrastando) 6f else if (p.noPrato) 2f else 3f)
                    .offset {
                        val c = posicaoAtual(p)
                        IntOffset((c.x - lado / 2f).roundToInt(), (c.y - lado / 2f).roundToInt())
                    }
                    .size(with(densidade) { lado.toDp() })
                    .testTag("balanca_peso_${p.id}")
                    .semantics { contentDescription = if (numeros) "Peso ${p.valor}" else "Peso com ${p.valor}" }
                    .pointerInput(p, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                if (ativaAgora && !terminou) {
                                    p.peca.arrastando = true
                                    interacoes++
                                    sons.pegar()
                                    escopo.launch { p.peca.desvio.stop() }
                                }
                            },
                            onDragEnd = { soltarAgora(p) },
                            onDragCancel = { soltarAgora(p) },
                        ) { mudanca, delta ->
                            if (p.peca.arrastando) {
                                mudanca.consume()
                                p.peca.arrasto += delta
                            }
                        }
                    }
                    .pointerInput(p, w, h) { detectTapGestures { tocarAgora(p) } }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        translationX = p.peca.tremida.value * density
                    }
                    .then(if (brilhar || tirar) Modifier.pulsando() else Modifier)
                    .then(
                        when {
                            brilhar -> Modifier.background(Color(0x66FFF3C4), RoundedCornerShape(22.dp)).border(4.dp, Cores.Estrela, RoundedCornerShape(22.dp))
                            tirar -> Modifier.border(4.dp, Cores.Errado, RoundedCornerShape(22.dp))
                            else -> Modifier
                        },
                    ),
            ) { PesoDesenhado(p.valor, balanca.figura, numeros, lado / densidade.density) }
        }

        // ---------------------------------------------------------------- quanto tem em cada lado
        if (numeros) {
            val selo = px(44f)
            for ((base, valor, lado2) in listOf(Triple(pratoEsquerdo, alvo, 0), Triple(pratoDireito, somaDireita, 1))) {
                val passou = lado2 == 1 && somaDireita > alvo
                val igual = lado2 == 1 && somaDireita == alvo
                Box(
                    Modifier
                        .zIndex(4f)
                        .offset { IntOffset((base.x - selo / 2f).roundToInt(), (base.y + px(16f)).roundToInt()) }
                        .size(with(densidade) { selo.toDp() })
                        .background(
                            when {
                                passou -> Cores.Errado
                                igual -> Cores.Certo
                                else -> Color.White
                            },
                            CircleShape,
                        )
                        .border(3.dp, CorMadeiraEscura, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$valor",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (passou || igual) Color.White else Cores.Texto,
                    )
                }
            }
        }

        // Alvo invisível do prato da direita (os testes automáticos soltam os pesos no meio dele).
        Box(
            Modifier
                .offset { IntOffset((pratoParado.x - larguraPrato / 2f).roundToInt(), (pratoParado.y - lado).roundToInt()) }
                .size(with(densidade) { larguraPrato.toDp() }, with(densidade) { lado.toDp() })
                .testTag("balanca_prato"),
        )

        // ---------------------------------------------------------------- mãozinha: mostra o gesto
        val ninguemArrastando = pesos.none { it.peca.arrastando }
        val sobrando = if (solucao != null) noPrato.firstOrNull { it.id !in solucao } else null
        val exemplo = when {
            sobrando != null -> sobrando
            solucao != null -> pesos.firstOrNull { it.id in solucao && !it.noPrato }
            else -> pesos.filter { !it.noPrato }.minByOrNull { it.valor }
        }
        Box(Modifier.matchParentSize().zIndex(7f)) {
            if (exemplo != null) {
                key(exemplo.id, exemplo.noPrato) {
                    GestoDeArrastar(
                        origem = if (exemplo.noPrato) descanso(exemplo) else centroNaPrateleira(exemplo.id),
                        destino = if (exemplo.noPrato) centroNaPrateleira(exemplo.id) else pratoParado - Offset(0f, ladoNoPrato * 0.6f),
                        ativa = ativa && !terminou && parada && ninguemArrastando,
                        cor = escurecer(cor),
                        tamanhoCarga = with(densidade) { (lado * 0.8f).toDp() },
                    ) { PesoDesenhado(exemplo.valor, balanca.figura, numeros, lado * 0.8f / densidade.density) }
                }
            }
        }
    }
}

/**
 * Um peso: com número, é um peso de ferro colorido com alça e o número grande; com figuras, é uma
 * caixinha de madeira com a figura repetida [valor] vezes (dá para contar).
 */
@Composable
private fun PesoDesenhado(valor: Int, figura: String, numeros: Boolean, ladoDp: Float) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (numeros) {
            val c = corDoPeso(valor)
            Canvas(Modifier.fillMaxSize()) {
                val s = size.minDimension
                // alça
                drawArc(
                    escurecer(c, 0.6f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(s * 0.3f, s * 0.06f),
                    size = Size(s * 0.4f, s * 0.36f),
                    style = Stroke(s * 0.09f, cap = StrokeCap.Round),
                )
                // corpo (trapézio arredondado)
                val corpo = Path().apply {
                    moveTo(s * 0.24f, s * 0.26f)
                    lineTo(s * 0.76f, s * 0.26f)
                    quadraticTo(s * 0.84f, s * 0.27f, s * 0.86f, s * 0.36f)
                    lineTo(s * 0.95f, s * 0.86f)
                    quadraticTo(s * 0.96f, s * 0.95f, s * 0.86f, s * 0.95f)
                    lineTo(s * 0.14f, s * 0.95f)
                    quadraticTo(s * 0.04f, s * 0.95f, s * 0.05f, s * 0.86f)
                    lineTo(s * 0.14f, s * 0.36f)
                    quadraticTo(s * 0.16f, s * 0.27f, s * 0.24f, s * 0.26f)
                    close()
                }
                drawPath(corpo, escurecer(c, 0.72f), alpha = 1f)
                drawPath(corpo, Brush.verticalGradient(listOf(lerpCor(c, Color.White, 0.25f), c), startY = s * 0.26f, endY = s * 0.95f))
                drawPath(corpo, escurecer(c, 0.6f), style = Stroke(s * 0.035f))
                // brilho
                drawLine(Color.White.copy(alpha = 0.55f), Offset(s * 0.2f, s * 0.42f), Offset(s * 0.15f, s * 0.78f), s * 0.05f, StrokeCap.Round)
            }
            Text(
                "$valor",
                fontSize = (ladoDp * (if (valor >= 10) 0.36f else 0.44f)).sp,
                lineHeight = (ladoDp * 0.5f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.offset(y = (ladoDp * 0.1f).dp),
            )
        } else {
            Canvas(Modifier.fillMaxSize()) {
                val s = size.minDimension
                drawRoundRect(CorMadeiraEscura, Offset(s * 0.04f, s * 0.08f), Size(s * 0.92f, s * 0.88f), CornerRadius(s * 0.14f))
                drawRoundRect(CorMadeiraClara, Offset(s * 0.08f, s * 0.11f), Size(s * 0.84f, s * 0.8f), CornerRadius(s * 0.11f))
            }
            val emoji = figurasDe(figura).firstOrNull() ?: figura
            val porLinha = when {
                valor <= 1 -> 1
                valor <= 4 -> 2
                else -> 3
            }
            val linhas = ceil(valor / porLinha.toFloat()).toInt()
            val tamanho = min(ladoDp * 0.78f / maxOf(porLinha, linhas), ladoDp * 0.5f)
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (l in 0 until linhas) {
                    androidx.compose.foundation.layout.Row {
                        for (k in 0 until min(porLinha, valor - l * porLinha)) {
                            Text(emoji, fontSize = (tamanho * 0.8f).sp, lineHeight = (tamanho * 0.95f).sp)
                        }
                    }
                }
            }
        }
    }
}

private fun lerpCor(a: Color, b: Color, f: Float) = Color(
    a.red + (b.red - a.red) * f,
    a.green + (b.green - a.green) * f,
    a.blue + (b.blue - a.blue) * f,
    a.alpha,
)

/** Prateleira de madeira onde ficam os pesos para escolher. */
private fun DrawScope.desenharPrateleira(topo: Float, w: Float, h: Float) {
    val r = 18.dp.toPx()
    drawRoundRect(Color(0x33A0743A), Offset(4.dp.toPx(), topo), Size(w - 8.dp.toPx(), h - topo), CornerRadius(r))
    drawRoundRect(CorMadeira, Offset(0f, h - 14.dp.toPx()), Size(w, 14.dp.toPx()), CornerRadius(7.dp.toPx()))
    drawRoundRect(CorMadeiraClara, Offset(0f, h - 14.dp.toPx()), Size(w, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
}

/** A balança: pé e coluna de madeira, travessão de latão que inclina, cordinhas e os dois pratos. */
private fun DrawScope.desenharBalanca(
    pivo: Offset,
    regiao: Float,
    braco: Float,
    angulo: Float,
    pontaEsquerda: Offset,
    pontaDireita: Offset,
    pratoEsquerdo: Offset,
    pratoDireito: Offset,
    larguraPrato: Float,
    reta: Boolean,
) {
    val d = density
    // pé
    val chao = regiao - 4f * d
    drawOval(Color(0x22000000), Offset(pivo.x - 70f * d, chao - 6f * d), Size(140f * d, 16f * d))
    drawRoundRect(CorMadeiraEscura, Offset(pivo.x - 62f * d, chao - 22f * d), Size(124f * d, 22f * d), CornerRadius(10f * d))
    drawRoundRect(CorMadeira, Offset(pivo.x - 58f * d, chao - 22f * d), Size(116f * d, 10f * d), CornerRadius(6f * d))
    // coluna
    drawRoundRect(CorMadeiraEscura, Offset(pivo.x - 11f * d, pivo.y), Size(22f * d, chao - 20f * d - pivo.y), CornerRadius(8f * d))
    drawRoundRect(CorMadeira, Offset(pivo.x - 7f * d, pivo.y), Size(9f * d, chao - 22f * d - pivo.y), CornerRadius(5f * d))
    // mostrador do ponteiro: um arco com o meio verde
    val raioArco = 46f * d
    drawArc(
        Color.White.copy(alpha = 0.9f),
        startAngle = 240f,
        sweepAngle = 60f,
        useCenter = true,
        topLeft = Offset(pivo.x - raioArco, pivo.y - raioArco),
        size = Size(raioArco * 2f, raioArco * 2f),
    )
    drawArc(
        Cores.Certo,
        startAngle = 266f,
        sweepAngle = 8f,
        useCenter = true,
        topLeft = Offset(pivo.x - raioArco, pivo.y - raioArco),
        size = Size(raioArco * 2f, raioArco * 2f),
    )
    drawArc(
        CorMadeiraEscura,
        startAngle = 240f,
        sweepAngle = 60f,
        useCenter = true,
        topLeft = Offset(pivo.x - raioArco, pivo.y - raioArco),
        size = Size(raioArco * 2f, raioArco * 2f),
        style = Stroke(2.5f * d),
    )
    // cordinhas (em V) até a borda de cada prato
    for ((ponta, prato) in listOf(pontaEsquerda to pratoEsquerdo, pontaDireita to pratoDireito)) {
        val meio = larguraPrato / 2f
        drawLine(Color(0xFF6B5B4B), ponta, prato + Offset(-meio * 0.92f, -2f * d), 2.5f * d)
        drawLine(Color(0xFF6B5B4B), ponta, prato + Offset(meio * 0.92f, -2f * d), 2.5f * d)
        drawCircle(CorLataoEscuro, 6f * d, ponta)
    }
    // travessão e ponteiro (giram juntos)
    rotate(angulo, pivo) {
        drawLine(CorLatao, pivo, pivo - Offset(0f, raioArco * 0.92f), 5f * d, StrokeCap.Round)
        drawLine(if (reta) Cores.Certo else Cores.Errado, pivo - Offset(0f, raioArco * 0.6f), pivo - Offset(0f, raioArco * 0.95f), 6f * d, StrokeCap.Round)
        drawRoundRect(CorLataoEscuro, Offset(pivo.x - braco - 8f * d, pivo.y - 9f * d), Size(braco * 2f + 16f * d, 18f * d), CornerRadius(9f * d))
        drawRoundRect(CorLatao, Offset(pivo.x - braco - 6f * d, pivo.y - 8f * d), Size(braco * 2f + 12f * d, 12f * d), CornerRadius(6f * d))
        drawRoundRect(CorLataoClaro, Offset(pivo.x - braco, pivo.y - 7f * d), Size(braco * 2f, 3.5f * d), CornerRadius(2f * d))
    }
    drawCircle(CorLataoEscuro, 13f * d, pivo)
    drawCircle(CorLatao, 9f * d, pivo)
    drawCircle(CorLataoClaro, 3.5f * d, pivo - Offset(2.5f * d, 2.5f * d))
    // pratos: tigela rasa de latão
    for (prato in listOf(pratoEsquerdo, pratoDireito)) {
        val meio = larguraPrato / 2f
        val fundo = Path().apply {
            moveTo(prato.x - meio, prato.y - 3f * d)
            quadraticTo(prato.x, prato.y + 34f * d, prato.x + meio, prato.y - 3f * d)
            close()
        }
        drawPath(fundo, CorLataoEscuro)
        val dentro = Path().apply {
            moveTo(prato.x - meio * 0.96f, prato.y - 3f * d)
            quadraticTo(prato.x, prato.y + 28f * d, prato.x + meio * 0.96f, prato.y - 3f * d)
            close()
        }
        drawPath(dentro, Brush.verticalGradient(listOf(CorLataoClaro, CorLatao), startY = prato.y - 3f * d, endY = prato.y + 16f * d))
        drawRoundRect(CorLataoEscuro, Offset(prato.x - meio - 3f * d, prato.y - 6f * d), Size(larguraPrato + 6f * d, 7f * d), CornerRadius(3.5f * d))
        if (reta) drawCircle(Cores.Certo.copy(alpha = 0.25f), meio * 0.7f, prato - Offset(0f, 20f * d))
    }
}
