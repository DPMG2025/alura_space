package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Relogio
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import br.com.casinhadosaber.ui.tema.Baloo2
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val CorPonteiroMinutos = Color(0xFF3F7FD0)
private val CorPonteiroHoras = Color(0xFFE5534B)
private val CorCasaRelogio = Color(0xFFB9784A)
private val CorCasaRelogioEscura = Color(0xFF7E4E2C)
private val CorTelhado = Color(0xFF8E3B2F)

/** Comprimento de cada ponteiro, em fração do raio do mostrador (as bolinhas de pegar ficam na ponta). */
private const val PONTA_MINUTOS = 0.78f
private const val PONTA_HORAS = 0.5f

private const val NENHUM = 0
private const val HORAS = 1
private const val MINUTOS = 2

/** Ângulo (graus, a partir do 12, no sentido do relógio) do ponto [p] em volta de [centro]. */
private fun anguloDe(p: Offset, centro: Offset): Float {
    val a = atan2((p.x - centro.x).toDouble(), (centro.y - p.y).toDouble()) * 180.0 / PI
    return ((a + 360.0) % 360.0).toFloat()
}

private fun pontoNoAngulo(centro: Offset, raio: Float, graus: Float): Offset {
    val r = graus * PI.toFloat() / 180f
    return centro + Offset(raio * sin(r), -raio * cos(r))
}

/** Ângulo equivalente a [alvo] mais perto de [atual] (o ponteiro nunca dá a volta errada). */
private fun maisPerto(atual: Float, alvo: Float): Float {
    var a = alvo
    while (a - atual > 180f) a -= 360f
    while (a - atual < -180f) a += 360f
    return a
}

private fun anguloDasHoras(hora: Int, minuto: Int) = ((hora % 12) + minuto / 60f) * 30f

/**
 * Acertar o relógio: um relógio de cuco com ponteiros grandes. A criança arrasta o ponteiro azul
 * (grande, dos minutos, pula de 5 em 5) e o vermelho (pequeno, das horas) até marcar a hora pedida.
 * * Acerto: os dois ponteiros na hora pedida; a portinha abre, o cuco canta e a pergunta termina.
 * * Erro: soltar um ponteiro num lugar errado (o narrador diz a hora que ficou marcada e repete o
 *   pedido). Soltar um ponteiro no lugar certo ganha um "isso!" e a dica do outro ponteiro.
 * Com [Relogio.digital] a hora aparece também num relógio digital em cima. Depois de
 * [ERROS_PARA_AJUDA] erros, ponteiros-fantasma mostram onde cada um deve ficar e a mãozinha mostra o gesto.
 */
@Composable
internal fun AtividadeRelogio(
    relogio: Relogio,
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
    val falaAtual by rememberUpdatedState(fala)
    val medidor = rememberTextMeasurer()

    // Começa com os minutos no 12 (horas cheias: só o pequeno precisa mexer) e as horas longe da pedida.
    var hora by remember(relogio) { mutableIntStateOf((relogio.hora + 4) % 12 + 1) }
    var minuto by remember(relogio) { mutableIntStateOf(0) }
    var segurando by remember { mutableIntStateOf(NENHUM) }
    var anguloLivre by remember { mutableFloatStateOf(0f) }
    var valorAoPegar by remember { mutableIntStateOf(-1) }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = paradaN3(interacoes, terminou, ativa, ajuda, ajudaInicial)

    val angMinutos = remember(relogio) { Animatable(0f) }
    val angHoras = remember(relogio) { Animatable(anguloDasHoras(hora, minuto)) }
    LaunchedEffect(minuto, segurando) {
        val alvo = maisPerto(angMinutos.value, minuto * 6f)
        if (segurando == MINUTOS) angMinutos.animateTo(alvo, tween(70)) else angMinutos.animateTo(alvo, spring(dampingRatio = 0.55f))
    }
    LaunchedEffect(hora, minuto, segurando, anguloLivre) {
        if (segurando == HORAS) angHoras.snapTo(maisPerto(angHoras.value, anguloLivre))
        else angHoras.animateTo(maisPerto(angHoras.value, anguloDasHoras(hora, minuto)), spring(dampingRatio = 0.55f))
    }
    val porta = remember(relogio) { Animatable(0f) }
    LaunchedEffect(terminou) {
        if (terminou) {
            porta.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 200f))
            repeat(2) {
                sons.piu(it)
                delay(380)
            }
        }
    }

    val minutosCerto = minuto == relogio.minuto
    val horasCerto = hora == relogio.hora

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        val alturaDigital = if (relogio.digital) px(70f) else 0f
        val diametro = min(w * 0.76f, (h - alturaDigital - px(16f)) / 1.62f)
        val raio = diametro / 2f
        val alturaTelhado = diametro * 0.24f
        val topoMostrador = alturaDigital + alturaTelhado + px(4f)
        val centro = Offset(w / 2f, topoMostrador + raio)
        val esquerdaMostrador = centro.x - raio

        fun pontaDosMinutos() = pontoNoAngulo(centro, raio * PONTA_MINUTOS, angMinutos.value)
        fun pontaDasHoras() = pontoNoAngulo(centro, raio * PONTA_HORAS, angHoras.value)

        fun nomeDoPonteiro(qual: Int) = if (qual == MINUTOS) "o ponteiro grande" else "o ponteiro pequeno"

        fun soltar() {
            val qual = segurando
            if (qual == NENHUM) return
            if (qual == HORAS) {
                // A hora fica no número mais perto, contando que o ponteiro pequeno anda com os minutos.
                val h0 = (anguloLivre / 30f - minuto / 60f).roundToInt().mod(12)
                hora = if (h0 == 0) 12 else h0
            }
            segurando = NENHUM
            if (terminou) return
            // Pegou e soltou no mesmo lugar: não conta nada.
            if ((if (qual == MINUTOS) minuto else hora) == valorAoPegar) return
            val certoAgora = if (qual == MINUTOS) minuto == relogio.minuto else hora == relogio.hora
            when {
                hora == relogio.hora && minuto == relogio.minuto -> {
                    terminou = true
                    vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    escopo.launch {
                        delay(1500)
                        concluir(erros == 0)
                    }
                }
                certoAgora -> {
                    sons.colocar()
                    narrador.falar("Isso! Agora ${nomeDoPonteiro(if (qual == MINUTOS) HORAS else MINUTOS)}.")
                }
                else -> {
                    erros++
                    avisarErro()
                    sons.erro()
                    narrador.falar(listOf(pt("Ficou ${horaPorExtensoN3(hora, minuto)}.")) + falaAtual)
                }
            }
        }

        fun comecar(p: Offset) {
            if (!ativaAgora || terminou) return
            val dm = (p - pontaDosMinutos()).getDistance()
            val dh = (p - pontaDasHoras()).getDistance()
            segurando = if (dm <= dh) MINUTOS else HORAS
            valorAoPegar = if (segurando == MINUTOS) minuto else hora
            interacoes++
            sons.pegar()
            anguloLivre = anguloDe(p, centro)
        }

        fun mover(p: Offset) {
            if (segurando == NENHUM) return
            val a = anguloDe(p, centro)
            anguloLivre = a
            if (segurando == MINUTOS) {
                val m = (a / 30f).roundToInt().mod(12) * 5
                if (m != minuto) {
                    minuto = m
                    sons.toque()
                }
            }
        }

        fun tocar(p: Offset) {
            if (!ativaAgora || terminou) return
            interacoes++
            val dm = (p - pontaDosMinutos()).getDistance()
            val dh = (p - pontaDasHoras()).getDistance()
            sons.toque()
            narrador.falar(
                if (dm <= dh) "Esse é o ponteiro grande, dos minutos. Arraste ele!"
                else "Esse é o ponteiro pequeno, das horas. Arraste ele!",
            )
        }

        val soltarAgora by rememberUpdatedState<() -> Unit> { soltar() }
        val comecarAgora by rememberUpdatedState<(Offset) -> Unit> { comecar(it) }
        val moverAgora by rememberUpdatedState<(Offset) -> Unit> { mover(it) }
        val tocarAgora by rememberUpdatedState<(Offset) -> Unit> { tocar(it) }

        // ---------------------------------------------------------------- relógio digital (mundos mais altos)
        if (relogio.digital) {
            Box(
                Modifier
                    .offset { IntOffset((w / 2f - px(80f)).roundToInt(), px(4f).roundToInt()) }
                    .size(160.dp, 60.dp)
                    .background(Color(0xFF2B2A3A), RoundedCornerShape(16.dp))
                    .border(4.dp, Color(0xFF55536B), RoundedCornerShape(16.dp))
                    .semantics { contentDescription = horaPorExtensoN3(relogio.hora, relogio.minuto) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    horaDigitalN3(relogio.hora, relogio.minuto),
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = Baloo2,
                    color = Color(0xFF7CF29B),
                )
            }
        }

        // ---------------------------------------------------------------- pêndulo, casinha, telhado e portinha do cuco
        val pendulo = rememberInfiniteTransition(label = "pendulo")
        val balanco by pendulo.animateFloat(-14f, 14f, infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse), label = "balanco")
        Canvas(Modifier.matchParentSize()) {
            desenharCasaDoRelogio(centro, raio, alturaTelhado, balanco)
        }
        // A portinha do cuco e o passarinho (sai quando acerta).
        val ladoPorta = alturaTelhado * 0.62f
        Box(
            Modifier
                .offset { IntOffset((centro.x - ladoPorta / 2f).roundToInt(), (topoMostrador - alturaTelhado * 0.86f).roundToInt()) }
                .size(with(densidade) { ladoPorta.toDp() }),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawRoundRect(Color(0xFF3B2416), cornerRadius = CornerRadius(size.width * 0.5f, size.width * 0.5f))
                // as duas folhas da porta abrem para os lados
                val aberta = porta.value.coerceIn(0f, 1f)
                val folha = size.width / 2f * (1f - aberta * 0.85f)
                drawRect(CorCasaRelogio, Offset(0f, 0f), Size(folha, size.height))
                drawRect(CorCasaRelogio, Offset(size.width - folha, 0f), Size(folha, size.height))
                drawLine(CorCasaRelogioEscura, Offset(folha, 0f), Offset(folha, size.height), 2f * density)
                drawLine(CorCasaRelogioEscura, Offset(size.width - folha, 0f), Offset(size.width - folha, size.height), 2f * density)
            }
            if (porta.value > 0.05f) {
                Text(
                    "🐦",
                    fontSize = (ladoPorta / densidade.density * 0.62f).sp,
                    lineHeight = (ladoPorta / densidade.density * 0.7f).sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = porta.value
                        scaleY = porta.value
                        translationY = -ladoPorta * 0.25f * porta.value
                    },
                )
            }
        }

        // ---------------------------------------------------------------- mostrador (recebe o arrasto)
        val ajudaMinutos = ajuda && !terminou && !minutosCerto
        val ajudaHoras = ajuda && !terminou && !horasCerto
        Box(
            Modifier
                .offset { IntOffset(esquerdaMostrador.roundToInt(), topoMostrador.roundToInt()) }
                .size(with(densidade) { diametro.toDp() })
                .testTag("relogio_mostrador")
                .semantics { contentDescription = "Relógio marcando ${horaPorExtensoN3(hora, minuto)}" }
                .pointerInput(w, h) {
                    val canto = Offset(esquerdaMostrador, topoMostrador)
                    detectDragGestures(
                        onDragStart = { comecarAgora(it + canto) },
                        onDragEnd = { soltarAgora() },
                        onDragCancel = { soltarAgora() },
                    ) { mudanca, _ ->
                        mudanca.consume()
                        moverAgora(mudanca.position + canto)
                    }
                }
                .pointerInput(w, h) {
                    val canto = Offset(esquerdaMostrador, topoMostrador)
                    detectTapGestures { tocarAgora(it + canto) }
                },
        ) {
            Canvas(Modifier.matchParentSize()) {
                val c = Offset(raio, raio)
                desenharMostrador(c, raio, medidor)
                // ponteiros-fantasma (ajuda): onde cada ponteiro deve ficar
                if (ajudaHoras) desenharPonteiro(c, raio * PONTA_HORAS, anguloDasHoras(relogio.hora, relogio.minuto), CorPonteiroHoras.copy(alpha = 0.3f), raio * 0.075f, fantasma = true)
                if (ajudaMinutos) desenharPonteiro(c, raio * PONTA_MINUTOS, relogio.minuto * 6f, CorPonteiroMinutos.copy(alpha = 0.3f), raio * 0.06f, fantasma = true)
                // ponteiros de verdade: o das horas embaixo, o dos minutos por cima
                desenharPonteiro(c, raio * PONTA_HORAS, angHoras.value, CorPonteiroHoras, raio * 0.075f, segurando = segurando == HORAS)
                desenharPonteiro(c, raio * PONTA_MINUTOS, angMinutos.value, CorPonteiroMinutos, raio * 0.06f, segurando = segurando == MINUTOS)
                drawCircle(Color(0xFF3D2C5E), raio * 0.07f, c)
                drawCircle(Color(0xFFFFE29A), raio * 0.03f, c)
                if (terminou) drawCircle(Cores.Certo, raio * 0.97f, c, style = Stroke(raio * 0.05f))
            }
        }

        // Marcas para os testes: as bolinhas de pegar de cada ponteiro (o arrasto é tratado pelo mostrador).
        val bolinha = px(56f)
        Box(
            Modifier
                .offset { pontaDosMinutos().let { IntOffset((it.x - bolinha / 2f).roundToInt(), (it.y - bolinha / 2f).roundToInt()) } }
                .size(56.dp)
                .testTag("relogio_ponteiro_minutos"),
        )
        Box(
            Modifier
                .offset { pontaDasHoras().let { IntOffset((it.x - bolinha / 2f).roundToInt(), (it.y - bolinha / 2f).roundToInt()) } }
                .size(56.dp)
                .testTag("relogio_ponteiro_horas"),
        )

        // ---------------------------------------------------------------- mãozinha
        // Sem ajuda, ensina só o gesto (gira o ponteiro grande um pedaço); com ajuda, leva até o lugar certo.
        val (origem, destino) = when {
            ajudaMinutos -> pontaDosMinutos() to pontoNoAngulo(centro, raio * PONTA_MINUTOS, relogio.minuto * 6f)
            ajudaHoras -> pontaDasHoras() to pontoNoAngulo(centro, raio * PONTA_HORAS, anguloDasHoras(relogio.hora, relogio.minuto))
            relogio.minuto == 0 -> pontaDasHoras() to pontoNoAngulo(centro, raio * PONTA_HORAS, angHoras.value + 60f)
            else -> pontaDosMinutos() to pontoNoAngulo(centro, raio * PONTA_MINUTOS, angMinutos.value + 60f)
        }
        Box(Modifier.matchParentSize().zIndex(7f)) {
            GestoDeArrastar(
                origem = origem,
                destino = destino,
                ativa = ativa && !terminou && parada && segurando == NENHUM,
                cor = escurecer(cor),
                tamanhoCarga = 40.dp,
            )
        }
    }
}

/** Casinha de madeira do relógio de cuco: telhado com beiral, corpo atrás do mostrador e o pêndulo. */
private fun DrawScope.desenharCasaDoRelogio(centro: Offset, raio: Float, alturaTelhado: Float, balanco: Float) {
    val d = density
    val topoMostrador = centro.y - raio
    // pêndulo (atrás de tudo)
    val presa = Offset(centro.x, centro.y + raio * 0.9f)
    val comprimento = raio * 0.55f
    val r = balanco * PI.toFloat() / 180f
    val ponta = presa + Offset(comprimento * sin(r), comprimento * cos(r))
    drawLine(Color(0xFFA9781C), presa, ponta, 4f * d, StrokeCap.Round)
    drawCircle(Color(0xFFA9781C), raio * 0.13f, ponta)
    drawCircle(Color(0xFFE8B84A), raio * 0.11f, ponta)
    drawCircle(Color(0xFFFFE29A), raio * 0.04f, ponta - Offset(raio * 0.03f, raio * 0.03f))
    // corpo da casinha
    val corpoL = raio * 2.16f
    drawRoundRect(
        CorCasaRelogioEscura,
        Offset(centro.x - corpoL / 2f, topoMostrador - alturaTelhado * 0.25f),
        Size(corpoL, raio * 2.08f + alturaTelhado * 0.25f),
        CornerRadius(raio * 0.16f),
    )
    drawRoundRect(
        CorCasaRelogio,
        Offset(centro.x - corpoL / 2f + 5f * d, topoMostrador - alturaTelhado * 0.25f + 4f * d),
        Size(corpoL - 10f * d, raio * 2.08f + alturaTelhado * 0.25f - 10f * d),
        CornerRadius(raio * 0.13f),
    )
    // telhado com beiral
    val beiral = raio * 0.1f
    val telhado = Path().apply {
        moveTo(centro.x - corpoL / 2f - beiral, topoMostrador - alturaTelhado * 0.12f)
        lineTo(centro.x, topoMostrador - alturaTelhado - raio * 0.02f)
        lineTo(centro.x + corpoL / 2f + beiral, topoMostrador - alturaTelhado * 0.12f)
        close()
    }
    drawPath(telhado, CorTelhado)
    drawPath(telhado, Color(0xFF5E241B), style = Stroke(4f * d))
    // telhas
    for (k in 1..3) {
        val f = k / 4f
        val y = topoMostrador - alturaTelhado - raio * 0.02f + (alturaTelhado * 0.88f) * f
        val meia = (corpoL / 2f + beiral) * f
        drawLine(Color(0x55FFFFFF), Offset(centro.x - meia, y), Offset(centro.x + meia, y), 2.5f * d)
    }
    // folhinhas de enfeite nos cantos de baixo
    for (lado in listOf(-1f, 1f)) {
        drawCircle(Color(0xFF59A84B), raio * 0.11f, Offset(centro.x + lado * corpoL * 0.42f, centro.y + raio * 0.98f))
        drawCircle(Color(0xFF7CC56B), raio * 0.07f, Offset(centro.x + lado * corpoL * 0.36f, centro.y + raio * 1.0f))
    }
}

private fun DrawScope.desenharMostrador(c: Offset, raio: Float, medidor: androidx.compose.ui.text.TextMeasurer) {
    drawCircle(Color(0xFF5E3A1E), raio, c)
    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFFFF4DC)), c, raio), raio * 0.92f, c)
    // marquinhas: uma por minuto, mais grossas de 5 em 5
    for (k in 0 until 60) {
        val forte = k % 5 == 0
        val a = k * 6f
        val dentro = if (forte) raio * 0.8f else raio * 0.85f
        drawLine(
            if (forte) Color(0xFF3D2C5E) else Color(0x883D2C5E),
            pontoNoAngulo(c, dentro, a),
            pontoNoAngulo(c, raio * 0.89f, a),
            if (forte) raio * 0.025f else raio * 0.01f,
            StrokeCap.Round,
        )
    }
    // números das horas
    val estilo = TextStyle(fontFamily = Baloo2, fontSize = (raio * 0.17f / density).sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D2C5E))
    for (n in 1..12) {
        val texto = medidor.measure("$n", estilo)
        val p = pontoNoAngulo(c, raio * 0.66f, n * 30f)
        drawText(texto, topLeft = p - Offset(texto.size.width / 2f, texto.size.height / 2f))
    }
}

private fun DrawScope.desenharPonteiro(
    c: Offset,
    comprimento: Float,
    graus: Float,
    cor: Color,
    grossura: Float,
    fantasma: Boolean = false,
    segurando: Boolean = false,
) {
    val ponta = pontoNoAngulo(c, comprimento, graus)
    val rabo = pontoNoAngulo(c, comprimento * 0.16f, graus + 180f)
    if (fantasma) {
        drawLine(cor, rabo, ponta, grossura, StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(grossura * 1.2f, grossura * 0.9f)))
        drawCircle(cor, c.y * 0.1f, ponta)
        return
    }
    drawLine(Color(0x33000000), rabo + Offset(2f * density, 3f * density), ponta + Offset(2f * density, 3f * density), grossura, StrokeCap.Round)
    drawLine(cor, rabo, ponta, grossura, StrokeCap.Round)
    // bolinha de pegar na ponta (maior enquanto a criança segura)
    val r = c.y * (if (segurando) 0.13f else 0.105f)
    drawCircle(escurecer(cor, 0.75f), r, ponta)
    drawCircle(cor, r * 0.82f, ponta)
    drawCircle(Color.White.copy(alpha = 0.6f), r * 0.3f, ponta - Offset(r * 0.25f, r * 0.25f))
}
