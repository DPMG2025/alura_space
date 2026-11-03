package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
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
import br.com.casinhadosaber.dominio.DirecaoDaSeta
import br.com.casinhadosaber.dominio.ProgramarRobo
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.SetaDesenhada
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.GestoDeArrastar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin

/** Um comando da fila: andar uma casinha para [direcao], [vezes] vezes (o bloco "2 vezes"). */
internal data class ComandoN4(val direcao: DirecaoDaSeta, val vezes: Int = 1)

/** Um passo do robô: do comando [comando], da casinha [de] para [para] (ou bateu, sem sair do lugar). */
internal data class PassoN4(val comando: Int, val de: Int, val para: Int, val bateu: Boolean)

private fun ProgramarRobo.vizinho(c: Int, d: DirecaoDaSeta): Int? {
    val l = c / colunas
    val k = c % colunas
    val (nl, nk) = when (d) {
        DirecaoDaSeta.CIMA -> l - 1 to k
        DirecaoDaSeta.BAIXO -> l + 1 to k
        DirecaoDaSeta.ESQUERDA -> l to k - 1
        DirecaoDaSeta.DIREITA -> l to k + 1
    }
    if (nl !in 0 until linhas || nk !in 0 until colunas) return null
    return nl * colunas + nk
}

/** Passos que o robô dá com a [fila]; para no primeiro choque (borda ou pedra). */
internal fun ProgramarRobo.passosN4(fila: List<ComandoN4>): List<PassoN4> {
    val passos = mutableListOf<PassoN4>()
    var c = inicio
    for ((i, cmd) in fila.withIndex()) repeat(cmd.vezes) {
        if (passos.lastOrNull()?.bateu == true) return passos
        val n = vizinho(c, cmd.direcao)
        if (n == null || n in obstaculos) {
            passos += PassoN4(i, c, c, true)
            return passos
        }
        passos += PassoN4(i, c, n, false)
        c = n
    }
    return passos
}

/** Distância (em passos, sem passar por pedras) de cada casinha até a chegada; -1 se não chega. */
internal fun ProgramarRobo.distanciasN4(): IntArray {
    val total = linhas * colunas
    val dist = IntArray(total) { -1 }
    dist[chegada] = 0
    val fila = ArrayDeque(listOf(chegada))
    while (fila.isNotEmpty()) {
        val c = fila.removeFirst()
        for (d in DirecaoDaSeta.entries) {
            val n = vizinho(c, d) ?: continue
            if (n in obstaculos || dist[n] >= 0) continue
            dist[n] = dist[c] + 1
            fila.addLast(n)
        }
    }
    return dist
}

/** O que está errado na fila: o índice do primeiro comando que bate ou se afasta da bateria, ou null. */
internal fun ProgramarRobo.primeiroDesvioN4(fila: List<ComandoN4>): Int? {
    val dist = distanciasN4()
    for (p in passosN4(fila)) {
        if (p.bateu) return p.comando
        if (dist[p.para] < 0 || dist[p.para] >= dist[p.de]) return p.comando
    }
    return null
}

/** Próxima seta que leva o robô para mais perto da bateria, a partir de onde a [fila] (certa até aqui) o deixa. */
internal fun ProgramarRobo.proximaSetaN4(fila: List<ComandoN4>): DirecaoDaSeta? {
    val dist = distanciasN4()
    val fim = passosN4(fila).lastOrNull()?.para ?: inicio
    if (fim == chegada) return null
    val preferida = fila.lastOrNull()?.direcao
    val boas = DirecaoDaSeta.entries.filter { d -> vizinho(fim, d)?.let { it !in obstaculos && dist[it] >= 0 && dist[it] < dist[fim] } == true }
    return if (preferida != null && preferida in boas) preferida else boas.firstOrNull()
}

/**
 * Programar o robô: a criança monta a fila de setas (tocando nas setas ou arrastando para a fila) e
 * toca no ▶; o robozinho anda casinha por casinha seguindo a fila. Tocar numa seta da fila tira ela.
 *
 * Acerto: o robô termina na bateria. Erro (um por ▶): bateu na pedra ou na borda, se afastou da bateria
 * ou parou antes; o robô volta ao começo, a seta errada (ou a casinha vazia, se faltou seta) pisca e o
 * narrador explica. Com [ProgramarRobo.repetir], o bloco "2×" dobra a última seta da fila. Depois de 2
 * erros (ou no exemplo) aparecem as pegadas do caminho e a seta certa brilha.
 *
 * Marcas de teste: "robo_seta_<CIMA|BAIXO|ESQUERDA|DIREITA>", "robo_repetir", "robo_play",
 * "robo_apagar" e "robo_fila_<i>".
 */
@Composable
internal fun AtividadeProgramarRobo(
    programarRobo: ProgramarRobo,
    fala: List<Trecho>,
    ativa: Boolean,
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val jogo = programarRobo
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val vibracao = LocalHapticFeedback.current
    val densidade = LocalDensity.current
    val escopo = rememberCoroutineScope()
    val ativaAgora by rememberUpdatedState(ativa)
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    fun celula(c: Int) = Offset((c % jogo.colunas).toFloat(), (c / jogo.colunas).toFloat())

    var fila by remember(jogo) { mutableStateOf(listOf<ComandoN4>()) }
    var rodando by remember { mutableStateOf(false) }
    var executando by remember { mutableIntStateOf(-1) }
    var errada by remember { mutableIntStateOf(-1) }
    var erros by remember { mutableIntStateOf(0) }
    var interacoes by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var parada by remember { mutableStateOf(false) }
    var humor by remember { mutableIntStateOf(0) } // 0 normal, 1 feliz, -1 ops
    var olhar by remember { mutableStateOf(DirecaoDaSeta.DIREITA) }
    val robo = remember(jogo) { Animatable(celula(jogo.inicio), Offset.VectorConverter) }
    val pulo = remember { Animatable(0f) }
    val carga = remember { Animatable(0f) }
    val tremidaFila = remember { Animatable(0f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val desvio = remember(fila) { jogo.primeiroDesvioN4(fila) }
    val chega = remember(fila) { jogo.passosN4(fila).let { p -> p.isNotEmpty() && !p.last().bateu && p.last().para == jogo.chegada && desvio == null } }
    val sugerida = remember(fila) { if (desvio == null && !chega && fila.size < jogo.maxComandos) jogo.proximaSetaN4(fila) else null }

    LaunchedEffect(interacoes, terminou, ativa, ajuda, fila.size, rodando) {
        parada = false
        if (!ativa || terminou || rodando) return@LaunchedEffect
        delay(
            when {
                ajudaInicial && interacoes == 0 -> 1200L
                interacoes == 0 -> 4500L
                ajuda -> 3000L
                else -> 7000L
            },
        )
        parada = true
    }

    fun mexeu() {
        interacoes++
        if (errada >= 0) errada = -1
    }

    fun adicionar(d: DirecaoDaSeta) {
        if (!ativaAgora || rodando || terminou) return
        mexeu()
        if (fila.size >= jogo.maxComandos) {
            sons.toque()
            narrador.falar("A fila está cheia. Toque no play!")
            escopo.launch { for (x in listOf(10f, -10f, 6f, -6f, 0f)) tremidaFila.animateTo(x, tween(55)) }
            return
        }
        fila = fila + ComandoN4(d)
        olhar = d
        sons.colocar()
        vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        narrador.falar(nomeDaDirecaoN4(d))
    }

    fun tirar(i: Int) {
        if (!ativaAgora || rodando || terminou || i !in fila.indices) return
        mexeu()
        fila = fila.filterIndexed { j, _ -> j != i }
        sons.pegar()
    }

    fun dobrar() {
        if (!ativaAgora || rodando || terminou) return
        mexeu()
        if (fila.isEmpty()) {
            sons.toque()
            narrador.falar("Primeiro coloque uma seta.")
            return
        }
        val ultimo = fila.last()
        fila = fila.dropLast(1) + ultimo.copy(vezes = if (ultimo.vezes == 1) 2 else 1)
        sons.colocar()
        narrador.falar(if (ultimo.vezes == 1) "Duas vezes!" else "Uma vez.")
    }

    fun play() {
        if (!ativaAgora || rodando || terminou) return
        mexeu()
        if (fila.isEmpty()) {
            sons.toque()
            narrador.falar("Primeiro coloque as setas!")
            return
        }
        rodando = true
        sons.toque()
        val programa = fila
        escopo.launch {
            val passos = jogo.passosN4(programa)
            val dist = jogo.distanciasN4()
            var problema: Int? = null
            var bateu = false
            for (p in passos) {
                executando = p.comando
                olhar = programa[p.comando].direcao
                val de = celula(p.de)
                if (p.bateu) {
                    val alvo = de + direcaoEmCelulas(olhar) * 0.38f
                    robo.animateTo(alvo, tween(200, easing = FastOutSlowInEasing))
                    sons.erro()
                    humor = -1
                    robo.animateTo(de, spring(dampingRatio = 0.35f))
                    bateu = true
                    problema = p.comando
                    break
                }
                sons.piu(2)
                robo.animateTo(celula(p.para), tween(430, easing = FastOutSlowInEasing))
                delay(90L)
                if (problema == null && (dist[p.para] < 0 || dist[p.para] >= dist[p.de])) problema = p.comando
            }
            executando = -1
            val fim = passos.lastOrNull()?.takeIf { !it.bateu }?.para ?: passos.lastOrNull()?.de ?: jogo.inicio
            if (!bateu && fim == jogo.chegada) {
                terminou = true
                humor = 1
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
                narrador.falar("O robô chegou na bateria!")
                launch { carga.animateTo(1f, tween(900)) }
                repeat(2) {
                    pulo.animateTo(1f, tween(170))
                    pulo.animateTo(0f, tween(220))
                }
                esperarNarradorN4(narrador, 3000L)
                concluir(erros == 0)
                return@launch
            }
            // Não chegou: conta um erro, mostra o que corrigir e volta ao começo.
            erros++
            avisarErro()
            if (!bateu) sons.erro()
            humor = -1
            val apontar = problema ?: programa.size.coerceAtMost(jogo.maxComandos - 1)
            errada = apontar
            narrador.falar(
                when {
                    bateu -> listOf(pt("Ops! O robô bateu."), pt("Troque a seta que está piscando."))
                    problema != null -> listOf(pt("Ops! O robô se afastou da bateria."), pt("Troque a seta que está piscando."))
                    else -> listOf(pt("Quase! Faltou andar mais."), pt("Coloque mais setas."))
                },
            )
            delay(900L)
            robo.animateTo(celula(jogo.inicio), tween(650, easing = FastOutSlowInEasing))
            humor = 0
            olhar = DirecaoDaSeta.DIREITA
            rodando = false
        }
    }

    // Coordenadas das peças (para arrastar e para a mãozinha), no espaço da área toda.
    var raiz by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val centros = remember { mutableStateMapOf<String, Offset>() }
    var areaFila by remember { mutableStateOf(Rect.Zero) }
    fun registrar(nome: String) = Modifier.onGloballyPositioned { c ->
        val r = raiz ?: return@onGloballyPositioned
        if (!r.isAttached || !c.isAttached) return@onGloballyPositioned
        centros[nome] = r.localPositionOf(c, Offset(c.size.width / 2f, c.size.height / 2f))
    }
    var arrastada by remember { mutableStateOf<DirecaoDaSeta?>(null) }
    var ponto by remember { mutableStateOf(Offset.Zero) }

    /** Seta arrastada da cruz: segue o dedo; solta perto da fila, entra nela. */
    fun arrastou(d: DirecaoDaSeta, soltou: Boolean, onde: Offset) {
        if (!soltou) {
            if (arrastada == null) {
                sons.pegar()
                mexeu()
            }
            arrastada = d
            ponto = onde
            return
        }
        arrastada = null
        val margem = with(densidade) { 36.dp.toPx() }
        val area = Rect(areaFila.left - margem, areaFila.top - margem * 1.5f, areaFila.right + margem, areaFila.bottom + margem * 1.5f)
        if (area.contains(onde)) adicionar(d)
    }

    Box(
        modifier
            .fillMaxSize()
            .onGloballyPositioned { raiz = it },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
            val botao = (minOf(maxWidth / 5.6f, maxHeight * 0.12f)).coerceIn(64.dp, 84.dp)
            val vaoFila = 6.dp
            val ladoFila = minOf((maxWidth - botao - 10.dp - 10.dp - 4.dp - vaoFila * (jogo.maxComandos - 1)) / jogo.maxComandos, 72.dp).coerceAtLeast(30.dp)
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                // ---------------------------------------------------------------- a grade
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GradeDoRobo(
                        jogo = jogo,
                        robo = robo.value,
                        olhar = olhar,
                        humor = humor,
                        pulo = pulo.value,
                        carga = carga.value,
                        pegadas = if (ajuda && !terminou) caminhoMaisCurto(jogo) else emptyList(),
                        cor = cor,
                    )
                }
                Spacer(Modifier.height(10.dp))
                // ---------------------------------------------------------------- a fila de comandos e o ▶
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier
                            .graphicsLayer { translationX = tremidaFila.value }
                            .onGloballyPositioned { c ->
                                val r = raiz ?: return@onGloballyPositioned
                                if (r.isAttached && c.isAttached) {
                                    val o = r.localPositionOf(c, Offset.Zero)
                                    areaFila = Rect(o, Size(c.size.width.toFloat(), c.size.height.toFloat()))
                                }
                            }
                            .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(vaoFila),
                    ) {
                        for (i in 0 until jogo.maxComandos) {
                            val cmd = fila.getOrNull(i)
                            val piscar = errada == i && !rodando
                            val sugerirTirar = ajuda && desvio == i && !rodando
                            Box(
                                Modifier
                                    .size(ladoFila)
                                    .then(registrar("fila_$i"))
                                    .testTag("robo_fila_$i")
                                    .semantics { contentDescription = cmd?.let { nomeDaDirecaoN4(it.direcao) } ?: "Casinha vazia" }
                                    .pointerInput(i) { detectTapGestures { if (fila.getOrNull(i) != null) tirar(i) } }
                                    .then(if (piscar) Modifier.pulsando() else Modifier)
                                    .brilhoN4(sugerirTirar && !piscar, 12.dp)
                                    .background(
                                        when {
                                            piscar -> Color(0xFFFFD6D6)
                                            executando == i -> Color(0xFFFFF3C4)
                                            cmd != null -> Color.White
                                            else -> Color.White.copy(alpha = 0.5f)
                                        },
                                        RoundedCornerShape(12.dp),
                                    )
                                    .border(
                                        if (piscar || executando == i) 4.dp else 2.dp,
                                        when {
                                            piscar -> Cores.Errado
                                            executando == i -> Cores.Estrela
                                            cmd != null -> cor
                                            else -> Color(0xFFD9CFF0)
                                        },
                                        RoundedCornerShape(12.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (cmd != null) {
                                    SetaDesenhada(cmd.direcao, Modifier.size(ladoFila * 0.78f))
                                    if (cmd.vezes > 1) {
                                        Box(
                                            Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = (-4).dp)
                                                .background(Cores.Roxo, CircleShape)
                                                .padding(horizontal = 5.dp, vertical = 1.dp),
                                        ) {
                                            Text("2×", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else if (piscar) {
                                    Text("?", fontSize = (ladoFila.value * 0.5f).sp, color = Cores.Errado, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    BotaoDoRobo(
                        tag = "robo_play",
                        descricao = "Play: o robô anda",
                        lado = botao,
                        corBotao = Cores.Certo,
                        destacar = ajuda && chega && !rodando && !terminou,
                        habilitado = !rodando && !terminou,
                        modifier = registrar("play"),
                        onToque = { play() },
                    ) {
                        Canvas(Modifier.size(botao * 0.46f)) {
                            val p = Path().apply {
                                moveTo(size.width * 0.18f, size.height * 0.08f)
                                lineTo(size.width * 0.92f, size.height * 0.5f)
                                lineTo(size.width * 0.18f, size.height * 0.92f)
                                close()
                            }
                            drawPath(p, Color.White)
                            drawPath(p, Color.White, style = Stroke(size.width * 0.12f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                // ---------------------------------------------------------------- setas (cruz) e botões extras
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BotaoDeSeta(DirecaoDaSeta.CIMA, botao, ajuda && sugerida == DirecaoDaSeta.CIMA, !rodando && !terminou, cor, registrar("seta_CIMA"), { centros["seta_CIMA"] }, ::adicionar, ::arrastou)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (d in listOf(DirecaoDaSeta.ESQUERDA, DirecaoDaSeta.BAIXO, DirecaoDaSeta.DIREITA)) {
                                BotaoDeSeta(d, botao, ajuda && sugerida == d, !rodando && !terminou, cor, registrar("seta_${d.name}"), { centros["seta_${d.name}"] }, ::adicionar, ::arrastou)
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (jogo.repetir) {
                            BotaoDoRobo(
                                tag = "robo_repetir",
                                descricao = "Duas vezes",
                                lado = botao,
                                corBotao = Cores.Roxo,
                                destacar = false,
                                habilitado = !rodando && !terminou,
                                onToque = { dobrar() },
                            ) {
                                Text("2×", fontSize = (botao.value * 0.34f).sp, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineLarge)
                            }
                        }
                        BotaoDoRobo(
                            tag = "robo_apagar",
                            descricao = "Apagar a última seta",
                            lado = botao,
                            corBotao = Color(0xFFFF9F9F),
                            destacar = false,
                            habilitado = !rodando && !terminou && fila.isNotEmpty(),
                            onToque = { tirar(fila.lastIndex) },
                        ) {
                            Canvas(Modifier.size(botao * 0.5f)) { desenharApagar() }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        // ---------------------------------------------------------------- seta sendo arrastada
        val d = arrastada
        if (d != null) {
            val meio = with(densidade) { 32.dp.toPx() }
            Box(
                Modifier
                    .zIndex(8f)
                    .offset { IntOffset((ponto.x - meio).roundToInt(), (ponto.y - meio).roundToInt()) }
                    .size(64.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(3.dp, cor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                SetaDesenhada(d, Modifier.size(50.dp))
            }
        }

        // ---------------------------------------------------------------- mãozinha
        val proxima = fila.size.coerceAtMost(jogo.maxComandos - 1)
        val origem = sugerida?.let { centros["seta_${it.name}"] }
        val destino = centros["fila_$proxima"]
        val mostrarArrasto = origem != null && destino != null && (ajuda || (interacoes == 0 && fila.isEmpty()))
        Box(Modifier.matchParentSize().zIndex(9f)) {
            if (mostrarArrasto && origem != null && destino != null) {
                androidx.compose.runtime.key(sugerida, fila.size) {
                    GestoDeArrastar(
                        origem = origem,
                        destino = destino,
                        ativa = ativa && !terminou && !rodando && parada && arrastada == null,
                        cor = escurecer(cor),
                        tamanhoCarga = 56.dp,
                    ) {
                        Box(
                            Modifier.size(56.dp).background(Color(0xFFFFF3C4), RoundedCornerShape(12.dp)).border(3.dp, Cores.Estrela, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) { SetaDesenhada(sugerida, Modifier.size(44.dp)) }
                    }
                }
            }
            // Fila montada e a criança parada: a mãozinha mostra o ▶.
            val play = centros["play"]
            if (play != null && fila.isNotEmpty() && !mostrarArrasto) {
                val meio = with(densidade) { 20.dp.toPx() }
                MaoGuia(
                    Modifier.offset { IntOffset((play.x - meio).roundToInt(), (play.y - meio).roundToInt()) },
                    ativa = ativa && !terminou && !rodando && parada && (chega || !ajuda),
                    espera = 0L,
                )
            }
        }
    }
}

private fun direcaoEmCelulas(d: DirecaoDaSeta) = when (d) {
    DirecaoDaSeta.CIMA -> Offset(0f, -1f)
    DirecaoDaSeta.BAIXO -> Offset(0f, 1f)
    DirecaoDaSeta.ESQUERDA -> Offset(-1f, 0f)
    DirecaoDaSeta.DIREITA -> Offset(1f, 0f)
}

/** Casinhas de um caminho mais curto do início até a chegada (as pegadas da ajuda). */
private fun caminhoMaisCurto(jogo: ProgramarRobo): List<Int> {
    val dist = jogo.distanciasN4()
    val caminho = mutableListOf<Int>()
    var c = jogo.inicio
    var guarda = 0
    while (c != jogo.chegada && guarda++ < 64) {
        val atual = c
        val prox = DirecaoDaSeta.entries.firstNotNullOfOrNull { d ->
            jogo.vizinho(atual, d)?.takeIf { it !in jogo.obstaculos && dist[it] >= 0 && dist[it] < dist[atual] }
        } ?: break
        if (prox != jogo.chegada) caminho += prox
        c = prox
    }
    return caminho
}

/** Botão de seta da cruz: tocar põe a seta na fila; arrastar leva uma cópia até a fila. */
@Composable
private fun BotaoDeSeta(
    d: DirecaoDaSeta,
    lado: Dp,
    destacar: Boolean,
    habilitado: Boolean,
    cor: Color,
    modifier: Modifier,
    /** Centro deste botão no espaço da área toda (onde a cópia arrastada é desenhada). */
    centroNaArea: () -> Offset?,
    onToque: (DirecaoDaSeta) -> Unit,
    /** (seta, soltou, ponto do dedo no espaço da área toda). */
    onArrasto: (DirecaoDaSeta, Boolean, Offset) -> Unit,
) {
    val habilitadoAgora by rememberUpdatedState(habilitado)
    var dedo by remember { mutableStateOf(Offset.Zero) }
    var arrastando by remember { mutableStateOf(false) }
    Box(
        modifier
            .size(lado)
            .testTag("robo_seta_${d.name}")
            .semantics { contentDescription = "Seta ${nomeDaDirecaoN4(d)}" }
            .pointerInput(d) { detectTapGestures { if (habilitadoAgora) onToque(d) } }
            .pointerInput(d) {
                fun naArea(): Offset? = centroNaArea()?.let { it + dedo - Offset(size.width / 2f, size.height / 2f) }
                detectDragGestures(
                    onDragStart = { inicio ->
                        arrastando = habilitadoAgora
                        dedo = inicio
                        if (arrastando) naArea()?.let { onArrasto(d, false, it) }
                    },
                    onDragEnd = {
                        if (arrastando) onArrasto(d, true, naArea() ?: Offset(-1e6f, -1e6f))
                        arrastando = false
                    },
                    onDragCancel = {
                        if (arrastando) onArrasto(d, true, Offset(-1e6f, -1e6f))
                        arrastando = false
                    },
                ) { mudanca, delta ->
                    if (!arrastando) return@detectDragGestures
                    mudanca.consume()
                    dedo += delta
                    naArea()?.let { onArrasto(d, false, it) }
                }
            }
            .brilhoN4(destacar, 16.dp)
            .then(if (destacar) Modifier.pulsando() else Modifier)
            .graphicsLayer { alpha = if (habilitado) 1f else 0.55f },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.matchParentSize().padding(top = 4.dp).background(escurecer(cor, 0.85f).copy(alpha = 0.5f), RoundedCornerShape(16.dp)))
        Box(Modifier.matchParentSize().padding(bottom = 4.dp).background(if (destacar) Color(0xFFFFF3C4) else Color.White, RoundedCornerShape(16.dp)).border(3.dp, if (destacar) Cores.Estrela else cor, RoundedCornerShape(16.dp)))
        SetaDesenhada(d, Modifier.padding(bottom = 4.dp).size(lado * 0.74f))
    }
}

/** Botão redondo-quadrado com base 3D (play, apagar, 2×). */
@Composable
private fun BotaoDoRobo(
    tag: String,
    descricao: String,
    lado: Dp,
    corBotao: Color,
    destacar: Boolean,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onToque: () -> Unit,
    conteudo: @Composable () -> Unit,
) {
    val habilitadoAgora by rememberUpdatedState(habilitado)
    Box(
        modifier
            .size(lado)
            .testTag(tag)
            .semantics { contentDescription = descricao }
            .pointerInput(tag) { detectTapGestures { if (habilitadoAgora) onToque() } }
            .brilhoN4(destacar, lado * 0.3f)
            .then(if (destacar) Modifier.pulsando() else Modifier)
            .graphicsLayer { alpha = if (habilitado) 1f else 0.5f },
        contentAlignment = Alignment.Center,
    ) {
        // Base escura embaixo (efeito 3D) sem sair do tamanho do botão.
        Box(Modifier.matchParentSize().padding(top = 5.dp).background(escurecer(corBotao), RoundedCornerShape(lado * 0.3f)))
        Box(Modifier.matchParentSize().padding(bottom = 5.dp).background(corBotao, RoundedCornerShape(lado * 0.3f)))
        Box(Modifier.padding(bottom = 5.dp)) { conteudo() }
    }
}

private fun DrawScope.desenharApagar() {
    val s = size.minDimension
    val corpo = Path().apply {
        moveTo(s * 0.3f, s * 0.2f)
        lineTo(s * 0.95f, s * 0.2f)
        lineTo(s * 0.95f, s * 0.8f)
        lineTo(s * 0.3f, s * 0.8f)
        lineTo(s * 0.05f, s * 0.5f)
        close()
    }
    drawPath(corpo, Color.White)
    val x = Color(0xFFE05252)
    drawLine(x, Offset(s * 0.48f, s * 0.36f), Offset(s * 0.76f, s * 0.64f), s * 0.09f, StrokeCap.Round)
    drawLine(x, Offset(s * 0.76f, s * 0.36f), Offset(s * 0.48f, s * 0.64f), s * 0.09f, StrokeCap.Round)
}

// ------------------------------------------------------------------------------------- a grade desenhada

@Composable
private fun GradeDoRobo(
    jogo: ProgramarRobo,
    robo: Offset,
    olhar: DirecaoDaSeta,
    humor: Int,
    pulo: Float,
    carga: Float,
    pegadas: List<Int>,
    cor: Color,
) {
    val t = rememberInfiniteTransition(label = "gradeRobo")
    val brilho by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "brilho")
    val piscar by t.animateFloat(0f, 1f, infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "piscar")
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val lado = minOf(maxWidth / jogo.colunas, maxHeight / jogo.linhas, 110.dp)
        Canvas(
            Modifier
                .size(lado * jogo.colunas, lado * jogo.linhas)
                .semantics { contentDescription = "Grade do robô" },
        ) {
            val c = size.width / jogo.colunas
            val raio = CornerRadius(c * 0.18f)
            // Tabuleiro: grama em xadrez com bordinha.
            drawRoundRect(Color(0xFF5FA85A), topLeft = Offset(-6f, -4f), size = Size(size.width + 12f, size.height + 12f), cornerRadius = CornerRadius(c * 0.3f))
            for (l in 0 until jogo.linhas) for (k in 0 until jogo.colunas) {
                val o = Offset(k * c, l * c)
                val claro = (l + k) % 2 == 0
                drawRoundRect(
                    if (claro) Color(0xFFA8DD8E) else Color(0xFF95D07C),
                    topLeft = o + Offset(c * 0.04f, c * 0.04f),
                    size = Size(c * 0.92f, c * 0.92f),
                    cornerRadius = raio,
                )
                // tufinhos de grama
                val g = Color(0xFF7CC56B)
                val base = o + Offset(c * (0.22f + 0.5f * ((l * 7 + k * 3) % 5) / 5f), c * 0.78f)
                drawLine(g, base, base + Offset(-c * 0.04f, -c * 0.1f), c * 0.03f, StrokeCap.Round)
                drawLine(g, base, base + Offset(c * 0.03f, -c * 0.12f), c * 0.03f, StrokeCap.Round)
            }
            // Pegadas da ajuda.
            for (p in pegadas) {
                val m = Offset((p % jogo.colunas + 0.5f) * c, (p / jogo.colunas + 0.5f) * c)
                drawCircle(Color.White.copy(alpha = 0.75f), c * 0.09f, m + Offset(-c * 0.1f, c * 0.04f))
                drawCircle(Color.White.copy(alpha = 0.75f), c * 0.09f, m + Offset(c * 0.1f, -c * 0.06f))
            }
            // Pedras.
            for (p in jogo.obstaculos) desenharPedra(Offset((p % jogo.colunas + 0.5f) * c, (p / jogo.colunas + 0.5f) * c), c)
            // A bateria, com um brilho que chama.
            val mc = Offset((jogo.chegada % jogo.colunas + 0.5f) * c, (jogo.chegada / jogo.colunas + 0.5f) * c)
            drawCircle(Cores.Estrela.copy(alpha = 0.25f + 0.25f * brilho), c * (0.42f + 0.04f * brilho), mc)
            desenharBateria(mc, c, carga)
            // O robô.
            val centro = Offset((robo.x + 0.5f) * c, (robo.y + 0.5f) * c - pulo * c * 0.25f)
            desenharRobo(centro, c, olhar, humor, piscar > 0.95f, cor)
            if (carga > 0f) faiscasN4(mc, c * 0.5f, carga)
        }
    }
}

private fun DrawScope.desenharPedra(m: Offset, c: Float) {
    val p = Path().apply {
        moveTo(m.x - c * 0.36f, m.y + c * 0.26f)
        cubicTo(m.x - c * 0.42f, m.y - c * 0.05f, m.x - c * 0.25f, m.y - c * 0.32f, m.x - c * 0.02f, m.y - c * 0.32f)
        cubicTo(m.x + c * 0.24f, m.y - c * 0.34f, m.x + c * 0.42f, m.y - c * 0.08f, m.x + c * 0.38f, m.y + c * 0.26f)
        close()
    }
    drawOval(Color(0x33000000), topLeft = Offset(m.x - c * 0.38f, m.y + c * 0.18f), size = Size(c * 0.76f, c * 0.16f))
    drawPath(p, Color(0xFF9A928C))
    drawPath(p, Color(0xFF6F6863), style = Stroke(c * 0.03f))
    drawCircle(Color.White.copy(alpha = 0.3f), c * 0.08f, Offset(m.x - c * 0.12f, m.y - c * 0.14f))
    drawLine(Color(0xFF6F6863), Offset(m.x + c * 0.05f, m.y - c * 0.1f), Offset(m.x + c * 0.14f, m.y + c * 0.05f), c * 0.025f, StrokeCap.Round)
}

private fun DrawScope.desenharBateria(m: Offset, c: Float, carga: Float) {
    val w = c * 0.36f
    val h = c * 0.56f
    val topo = Offset(m.x - w / 2f, m.y - h / 2f + c * 0.04f)
    drawRoundRect(Color(0x33000000), topLeft = topo + Offset(c * 0.03f, c * 0.04f), size = Size(w, h), cornerRadius = CornerRadius(w * 0.2f))
    drawRoundRect(Color(0xFF8A8F98), topLeft = Offset(m.x - w * 0.22f, topo.y - c * 0.06f), size = Size(w * 0.44f, c * 0.08f), cornerRadius = CornerRadius(c * 0.02f))
    drawRoundRect(Color(0xFF3D4452), topLeft = topo, size = Size(w, h), cornerRadius = CornerRadius(w * 0.2f))
    // Nível de carga (enche quando o robô chega).
    val nivel = 0.35f + 0.6f * carga
    val dentro = Size(w * 0.76f, (h * 0.86f) * nivel)
    drawRoundRect(
        if (carga > 0.5f) Color(0xFF4CE07A) else Color(0xFFFFC93C),
        topLeft = Offset(m.x - dentro.width / 2f, topo.y + h * 0.93f - dentro.height),
        size = dentro,
        cornerRadius = CornerRadius(w * 0.12f),
    )
    // Raio.
    val r = Path().apply {
        moveTo(m.x + w * 0.08f, topo.y + h * 0.18f)
        lineTo(m.x - w * 0.22f, topo.y + h * 0.56f)
        lineTo(m.x + w * 0.02f, topo.y + h * 0.56f)
        lineTo(m.x - w * 0.08f, topo.y + h * 0.84f)
        lineTo(m.x + w * 0.24f, topo.y + h * 0.42f)
        lineTo(m.x, topo.y + h * 0.42f)
        close()
    }
    drawPath(r, Color.White)
}

private fun DrawScope.desenharRobo(m: Offset, c: Float, olhar: DirecaoDaSeta, humor: Int, piscando: Boolean, cor: Color) {
    val corpo = Color(0xFFDDE6F0)
    val traco = Color(0xFF55606E)
    val dir = direcaoEmCelulas(olhar)
    // Sombra no chão.
    drawOval(Color(0x33000000), topLeft = Offset(m.x - c * 0.28f, m.y + c * 0.3f), size = Size(c * 0.56f, c * 0.12f))
    // Rodinhas.
    for (s in listOf(-1f, 1f)) drawCircle(traco, c * 0.07f, Offset(m.x + s * c * 0.17f, m.y + c * 0.33f))
    // Corpo.
    val corpoTopo = Offset(m.x - c * 0.22f, m.y + c * 0.04f)
    drawRoundRect(cor, topLeft = corpoTopo, size = Size(c * 0.44f, c * 0.28f), cornerRadius = CornerRadius(c * 0.08f))
    drawRoundRect(traco, topLeft = corpoTopo, size = Size(c * 0.44f, c * 0.28f), cornerRadius = CornerRadius(c * 0.08f), style = Stroke(c * 0.025f))
    drawCircle(Cores.Estrela, c * 0.045f, Offset(m.x, m.y + c * 0.18f))
    // Bracinhos.
    for (s in listOf(-1f, 1f)) {
        drawLine(traco, Offset(m.x + s * c * 0.22f, m.y + c * 0.12f), Offset(m.x + s * c * 0.32f, m.y + c * (if (humor == 1) -0.02f else 0.22f)), c * 0.04f, StrokeCap.Round)
    }
    // Antena.
    drawLine(traco, Offset(m.x, m.y - c * 0.3f), Offset(m.x, m.y - c * 0.4f), c * 0.03f, StrokeCap.Round)
    drawCircle(Color(0xFFFF6B6B), c * 0.05f, Offset(m.x, m.y - c * 0.42f))
    // Cabeça com tela.
    val cabeca = Offset(m.x - c * 0.26f, m.y - c * 0.31f)
    drawRoundRect(corpo, topLeft = cabeca, size = Size(c * 0.52f, c * 0.36f), cornerRadius = CornerRadius(c * 0.12f))
    drawRoundRect(traco, topLeft = cabeca, size = Size(c * 0.52f, c * 0.36f), cornerRadius = CornerRadius(c * 0.12f), style = Stroke(c * 0.025f))
    drawRoundRect(Color(0xFF2E3A4A), topLeft = cabeca + Offset(c * 0.05f, c * 0.05f), size = Size(c * 0.42f, c * 0.26f), cornerRadius = CornerRadius(c * 0.08f))
    // Olhos (olham para onde vai) e boca.
    val olhos = Offset(m.x + dir.x * c * 0.04f, m.y - c * 0.16f + dir.y * c * 0.025f)
    val olho = Color(0xFF7DF9FF)
    for (s in listOf(-1f, 1f)) {
        val o = olhos + Offset(s * c * 0.1f, 0f)
        if (piscando || humor == 1) {
            drawArc(olho, 200f, 140f, false, topLeft = o - Offset(c * 0.05f, c * 0.03f), size = Size(c * 0.1f, c * 0.08f), style = Stroke(c * 0.03f, cap = StrokeCap.Round))
        } else {
            drawCircle(olho, c * 0.045f, o)
            drawCircle(Color.White, c * 0.015f, o + Offset(-c * 0.012f, -c * 0.012f))
        }
    }
    val boca = Offset(m.x + dir.x * c * 0.03f, m.y - c * 0.08f)
    when (humor) {
        -1 -> drawCircle(olho, c * 0.025f, boca, style = Stroke(c * 0.02f))
        else -> drawArc(olho, 20f, 140f, false, topLeft = boca - Offset(c * 0.06f, c * 0.05f), size = Size(c * 0.12f, c * 0.06f), style = Stroke(c * 0.022f, cap = StrokeCap.Round))
    }
    if (humor == -1) {
        // Estrelinhas de tontura.
        for (i in 0..2) {
            val a = i * 2.1f
            drawCircle(Cores.Estrela, c * 0.025f, Offset(m.x + kotlin.math.cos(a) * c * 0.3f, m.y - c * 0.38f + sin(a) * c * 0.06f))
        }
    }
}
