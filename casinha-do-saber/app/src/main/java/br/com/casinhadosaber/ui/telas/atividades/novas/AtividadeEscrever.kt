package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.CadernoEmArquivo
import br.com.casinhadosaber.dominio.EscreverPalavra
import br.com.casinhadosaber.dominio.Idioma
import br.com.casinhadosaber.dominio.LeitorDeLetras
import br.com.casinhadosaber.dominio.PaginaDoCaderno
import br.com.casinhadosaber.dominio.Ponto
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.MaoQueSegue
import br.com.casinhadosaber.ui.telas.atividades.ModelosDasLetras
import br.com.casinhadosaber.ui.telas.atividades.TracosDasLetras
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

/** Espera depois de levantar o dedo antes de ler a letra (dá tempo de fazer o próximo traço). */
private const val ESPERA_PARA_LER = 1300L

/** Depois de tantas tentativas numa letra, ela é escrita junto (nunca trava a criança). */
private const val TENTATIVAS_ATE_ESCREVER_JUNTO = 4

private val LilasDoGuia = Color(0xFFB9ABDD)
private val LinhaDoCaderno = Color(0xFFBFD7F2)
private val MargemDoCaderno = Color(0xFFF4B6B6)

/**
 * Escrever a palavra com o dedo: o narrador diz a palavra (com a figura) e a criança escreve uma letra
 * de cada vez na área grande, com as linhas do caderno (a de cima, a do meio pontilhada e a de baixo).
 * Ao levantar o dedo e esperar um pouquinho, a letra é lida ([LeitorDeLetras], com os modelos de
 * [TracosDasLetras]); a letra que vale vai para a casinha dela, desenhada do jeito que a criança fez.
 *
 * Nunca pune: letra que não vale some com carinho e o narrador diz o nome da letra de novo. Depois de
 * [ERROS_PARA_AJUDA] tentativas na mesma letra aparece o "mostrar como" (a letra pontilhada com a
 * mãozinha fazendo os traços); depois de [TENTATIVAS_ATE_ESCREVER_JUNTO], a letra é escrita junto.
 * Vale como acerto de primeira quando nenhuma letra precisou do "mostrar como".
 *
 * A palavra pronta vai para o "Meu caderno" da área dos pais (só no aparelho).
 *
 * Marcas de teste: "escrever_area", "escrever_ouvir", "escrever_mostrar", "escrever_apagar",
 * "escrever_casinha_<k>" e "escrever_letra_certa" (ação de acessibilidade que escreve a letra da vez
 * pelo modelo, passando pelo mesmo leitor; usada pelo teste no emulador).
 */
@Composable
internal fun AtividadeEscrever(
    escrever: EscreverPalavra,
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
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val ativaAgora by rememberUpdatedState(ativa)
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    val letras = escrever.letras
    val idioma = escrever.idioma
    var atual by remember(escrever) { mutableIntStateOf(0) }
    /** Letras já escritas, cada uma com os traços de 0 a 1 dentro da área. */
    val feitas = remember(escrever) { mutableStateListOf<List<List<Ponto>>>() }
    /** Traços da letra da vez (em pixels da área) e o traço que o dedo está fazendo. */
    val tracos = remember(escrever) { mutableStateListOf<List<Offset>>() }
    val emAndamento = remember(escrever) { mutableStateListOf<Offset>() }
    /** A letra que não valeu, sumindo devagarinho. */
    val sumindo = remember(escrever) { mutableStateListOf<List<Offset>>() }
    var tentativas by remember(escrever) { mutableIntStateOf(0) }
    var precisouDeAjuda by remember(escrever) { mutableStateOf(false) }
    var terminou by remember(escrever) { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var parada by remember { mutableStateOf(false) }
    var tamanhoDaArea by remember { mutableStateOf(Offset.Zero) }
    val sumir = remember { Animatable(1f) }
    val ouviu = remember { Animatable(1f) }
    var espera by remember { mutableStateOf<Job?>(null) }

    val esperada = letras.getOrNull(atual)

    /** A letra da vez lida na hora (as funções abaixo também rodam dentro dos gestos, que guardam a primeira versão delas). */
    fun daVez(): Char? = letras.getOrNull(atual)
    val mostrarComo = !terminou && (ajudaInicial || tentativas >= ERROS_PARA_AJUDA)

    fun falaDaLetra(c: Char): Trecho = if (idioma == Idioma.EN) Trecho(c.toString(), Idioma.EN) else pt(Vocabulario.falaDaLetra(c.toString()))
    val palavraFalada = remember(escrever) { Trecho(escrever.palavra.lowercase(), idioma) }

    /** "[antes] bê [depois]" numa frase só em português; em inglês, o nome da letra sai com a voz em inglês. */
    fun comLetra(antes: String, c: Char, depois: String = ""): List<Trecho> =
        if (idioma == Idioma.EN) listOfNotNull(pt(antes), falaDaLetra(c), depois.takeIf { it.isNotBlank() }?.let { pt(it) })
        else listOf(pt(("$antes ${falaDaLetra(c).texto}" + if (depois.isBlank()) "." else ". $depois").trim()))

    fun ouvir() {
        val c = letras.getOrNull(atual)
        narrador.falar(if (c == null) listOf(palavraFalada) else listOf(palavraFalada) + comLetra("Agora a letra", c))
        escopo.launch {
            ouviu.snapTo(1.12f)
            ouviu.animateTo(1f, spring(dampingRatio = 0.4f))
        }
    }

    // Depois do enunciado, o narrador diz qual é a primeira letra.
    LaunchedEffect(escrever, ativa) {
        if (!ativa) return@LaunchedEffect
        delay(500L)
        esperarNarradorN4(narrador, 7000L)
        if (!terminou && interacoes == 0) narrador.falar(comLetra("Primeiro, a letra", letras.first(), "Escreva aqui com o dedo."))
    }
    // A mãozinha mostra os traços quando o "mostrar como" está aberto e a criança fica parada.
    LaunchedEffect(interacoes, atual, mostrarComo, ativa, terminou) {
        parada = false
        if (!ativa || terminou || !mostrarComo) return@LaunchedEffect
        delay(if (interacoes == 0) 900L else 2500L)
        parada = true
    }

    /** Pontos de 0 a 1 dentro da área (o que vai para a casinha e para o caderno). */
    fun normalizar(t: List<List<Offset>>): List<List<Ponto>> {
        val w = tamanhoDaArea.x.coerceAtLeast(1f)
        val h = tamanhoDaArea.y.coerceAtLeast(1f)
        return t.map { traco -> traco.map { Ponto((it.x / w).coerceIn(0f, 1f), (it.y / h).coerceIn(0f, 1f)) } }
    }

    /** Os traços do modelo da letra [c], em pixels da área. */
    fun modeloNaArea(c: Char): List<List<Offset>> = TracosDasLetras.tracosDe(c.toString()).map { t ->
        TracosDasLetras.pontosDoTraco(t, 0.02f).map { TracosDasLetras.paraTela(it, tamanhoDaArea.x, tamanhoDaArea.y) }
    }

    fun terminar() {
        terminou = true
        val dia = System.currentTimeMillis().let { it + java.util.TimeZone.getDefault().getOffset(it) } / 86_400_000L
        val pagina = PaginaDoCaderno(escrever.palavra, dia, feitas.toList())
        escopo.launch {
            // O caderno fica só no aparelho (área dos pais, "Meu caderno").
            runCatching { CadernoEmArquivo.em(contexto.filesDir).guardar(pagina) }
            delay(700L)
            concluir(!precisouDeAjuda)
        }
    }

    fun aceitar(t: List<List<Offset>>) {
        feitas += normalizar(t)
        tracos.clear()
        emAndamento.clear()
        val primeiraLetra = atual == 0
        val deCara = tentativas == 0
        tentativas = 0
        atual++
        sons.colocar()
        val proxima = letras.getOrNull(atual)
        if (proxima == null) {
            terminar()
            return
        }
        val partes = buildList {
            add(br.com.casinhadosaber.dominio.ParteDaFala(falaDaLetra(letras[atual - 1])))
            if (primeiraLetra && deCara) add(br.com.casinhadosaber.dominio.VozesDaFamilia.parte("Que letra bonita!"))
            for (t in comLetra("Agora a letra", proxima)) add(br.com.casinhadosaber.dominio.ParteDaFala(t))
        }
        narrador.falarPartes(partes)
    }

    fun naoValeu(parecida: Char?) {
        val c = daVez() ?: return
        tentativas++
        avisarErro()
        sons.pousar()
        if (tentativas >= ERROS_PARA_AJUDA) precisouDeAjuda = true
        sumindo.clear()
        sumindo.addAll(tracos)
        tracos.clear()
        escopo.launch {
            sumir.snapTo(1f)
            sumir.animateTo(0f, tween(450))
            sumindo.clear()
        }
        when {
            // Escreve junto: a letra entra pelo modelo e a palavra continua (o narrador diz a letra e a próxima).
            tentativas >= TENTATIVAS_ATE_ESCREVER_JUNTO -> aceitar(modeloNaArea(c))
            tentativas >= ERROS_PARA_AJUDA ->
                narrador.falar(comLetra("Olhe como se faz a letra", c, "Siga a mãozinha e escreva por cima."))
            parecida != null && parecida != c ->
                narrador.falar(comLetra("Parece a letra", parecida) + comLetra("Vamos fazer a letra", c, "Tente de novo!"))
            else -> narrador.falar(comLetra("Quase! Vamos tentar de novo a letra", c))
        }
    }

    /** Lê a letra escrita ([t], em pixels da área) e decide. Toque sozinho ou risquinho não conta nada. */
    fun ler(t: List<List<Offset>>) {
        val c = daVez() ?: return
        if (terminou || t.isEmpty()) return
        val lado = min(tamanhoDaArea.x, tamanhoDaArea.y).coerceAtLeast(1f)
        val pontos = t.map { traco -> traco.map { Ponto(it.x, it.y) } }
        if (LeitorDeLetras.comprimento(pontos) < LeitorDeLetras.COMPRIMENTO_MINIMO * lado) {
            tracos.clear()
            return
        }
        val leitura = ModelosDasLetras.leitor.ler(pontos, c)
        // "Parece a letra X" só quando o desenho parece mesmo uma letra (rabisco não ganha nome).
        if (leitura.aceita) aceitar(t) else naoValeu(leitura.melhor.takeIf { leitura.distanciaMelhor <= LeitorDeLetras.LONGE_DEMAIS })
    }

    fun apagar() {
        espera?.cancel()
        tracos.clear()
        emAndamento.clear()
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val alto = maxHeight
        val largo = maxWidth
        // Figura e alto falante em cima, casinhas das letras, e a área de escrever com o resto da altura.
        val ladoTopo = min(96f, alto.value * 0.14f).coerceAtLeast(64f).dp
        val ladoCasinha = min(76f, (largo.value - 8f * (letras.size - 1)) / letras.size).coerceAtLeast(40f).dp
        val alturaCasinha = ladoCasinha * 1.2f
        val livre = (alto - ladoTopo - alturaCasinha - 36.dp).coerceAtLeast(160.dp)
        val larguraArea = minOf(largo - 76.dp, livre / TracosDasLetras.PROPORCAO, 460.dp)
        val alturaArea = larguraArea * TracosDasLetras.PROPORCAO

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            // ------------------------------------------------------------ figura, ouvir e mostrar como
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                val figura = escrever.figura
                if (figura != null) {
                    Box(
                        Modifier
                            .size(ladoTopo)
                            .background(Color.White, RoundedCornerShape(22.dp))
                            .border(3.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                            .semantics { contentDescription = "Figura da palavra" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(figura, fontSize = (ladoTopo.value * 0.6f).sp, lineHeight = (ladoTopo.value * 0.7f).sp)
                    }
                }
                Box(
                    Modifier
                        .size(ladoTopo)
                        .testTag("escrever_ouvir")
                        .semantics { contentDescription = "Ouvir a palavra" }
                        .pointerInput(escrever) {
                            detectTapGestures {
                                sons.toque()
                                ouvir()
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
                    AltoFalanteN4(tocando = narrador.falando, modifier = Modifier.size(ladoTopo * 0.58f))
                }
                if (tentativas >= ERROS_PARA_AJUDA || ajudaInicial) {
                    Box(
                        Modifier
                            .size(ladoTopo)
                            .testTag("escrever_mostrar")
                            .semantics { contentDescription = "Mostrar como se escreve" }
                            .pointerInput(escrever, atual) {
                                detectTapGestures {
                                    sons.toque()
                                    interacoes++
                                    daVez()?.let { narrador.falar(comLetra("Olhe como se faz a letra", it)) }
                                }
                            }
                            .pulsando(parada)
                            .background(Cores.Estrela, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("💡", fontSize = (ladoTopo.value * 0.45f).sp)
                    }
                }
            }

            // ------------------------------------------------------------ as casinhas das letras
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (k in letras.indices) {
                    val ehDaVez = k == atual && !terminou
                    Box(
                        Modifier
                            .size(ladoCasinha, alturaCasinha)
                            .testTag("escrever_casinha_$k")
                            .semantics {
                                contentDescription = when {
                                    k < feitas.size -> "Letra ${letras[k]} escrita"
                                    ehDaVez -> "Letra da vez"
                                    else -> "Letra que falta"
                                }
                            }
                            .then(if (ehDaVez && ativa) Modifier.pulsando() else Modifier)
                            .background(if (k < feitas.size) Color(0xFFDFF7E7) else Color.White, RoundedCornerShape(14.dp))
                            .border(if (ehDaVez) 4.dp else 3.dp, if (ehDaVez) cor else if (k < feitas.size) Cores.Certo else Color(0xFFD9CFF0), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        val escrita = feitas.getOrNull(k)
                        when {
                            escrita != null -> LetraEscrita(escrita, escurecer(Cores.Certo), Modifier.fillMaxSize())
                            ehDaVez && mostrarComo -> Text(
                                letras[k].toString(),
                                fontSize = (alturaCasinha.value * 0.5f).sp,
                                fontWeight = FontWeight.Bold,
                                color = Cores.Texto.copy(alpha = 0.25f),
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        }
                    }
                }
            }

            // ------------------------------------------------------------ a área de escrever
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .size(larguraArea, alturaArea)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .border(3.dp, Color(0xFFEDE6FF), RoundedCornerShape(26.dp))
                        .onSizeChanged { tamanhoDaArea = Offset(it.width.toFloat(), it.height.toFloat()) }
                        .testTag("escrever_area")
                        .semantics { contentDescription = "Área para escrever a letra" },
                ) {
                    val guia = remember(esperada, tamanhoDaArea) { if (esperada != null && tamanhoDaArea.x > 0f) modeloNaArea(esperada) else emptyList() }
                    Canvas(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(escrever) {
                                awaitEachGesture {
                                    val toque = awaitFirstDown()
                                    if (!ativaAgora || terminou) return@awaitEachGesture
                                    toque.consume()
                                    interacoes++
                                    espera?.cancel()
                                    emAndamento.clear()
                                    emAndamento.add(toque.position)
                                    while (true) {
                                        val evento = awaitPointerEvent()
                                        val mudanca = evento.changes.firstOrNull { it.id == toque.id } ?: break
                                        if (!mudanca.pressed) break
                                        mudanca.consume()
                                        val q = mudanca.position
                                        if ((q - emAndamento.last()).getDistance() >= 2f) emAndamento.add(q)
                                    }
                                    if (terminou) return@awaitEachGesture
                                    tracos.add(emAndamento.toList())
                                    emAndamento.clear()
                                    val agora = tracos.toList()
                                    espera = escopo.launch {
                                        delay(ESPERA_PARA_LER)
                                        ler(agora)
                                    }
                                }
                            },
                    ) {
                        linhasDoCaderno()
                        // "Mostrar como": a letra pontilhada, para escrever por cima.
                        if (mostrarComo) {
                            val pontilhado = PathEffect.dashPathEffect(floatArrayOf(0.1f, 16.dp.toPx()))
                            for (t in guia) if (t.size >= 2) {
                                drawPath(caminho(t), Color(0xFFEDE6FF), style = Stroke(22.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                drawPath(caminho(t), LilasDoGuia, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round, pathEffect = pontilhado))
                            }
                        }
                        val tinta = Stroke(14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        for (t in sumindo) if (t.size >= 2) drawPath(caminho(t), LilasDoGuia.copy(alpha = sumir.value), style = tinta)
                        for (t in tracos) {
                            if (t.size >= 2) drawPath(caminho(t), cor, style = tinta)
                            else if (t.size == 1) drawCircle(cor, 7.dp.toPx(), t[0])
                        }
                        if (emAndamento.size >= 2) drawPath(caminho(emAndamento.toList()), cor, style = tinta)
                        else if (emAndamento.size == 1) drawCircle(cor, 7.dp.toPx(), emAndamento[0])
                    }
                    // A mãozinha faz os traços da letra (no "mostrar como"), do primeiro ao último.
                    key(atual) {
                        MaoQueSegue(
                            pontos = guia.flatten(),
                            ativa = ativa && mostrarComo && parada && tracos.isEmpty() && emAndamento.isEmpty(),
                            espera = 0L,
                        )
                    }
                    // Ação de acessibilidade (e do teste no emulador): escreve a letra da vez pelo modelo, lida pelo mesmo leitor.
                    Box(
                        Modifier
                            .size(1.dp)
                            .testTag("escrever_letra_certa")
                            .semantics {
                                role = Role.Button
                                contentDescription = "Escrever a letra para mim"
                                onClick {
                                    val c = daVez()
                                    if (c != null && ativaAgora && !terminou && tamanhoDaArea.x > 0f) {
                                        espera?.cancel()
                                        interacoes++
                                        ler(modeloNaArea(c))
                                    }
                                    true
                                }
                            },
                    )
                }
                // Apagar a letra da vez (sem contar erro).
                Box(
                    Modifier
                        .size(64.dp)
                        .testTag("escrever_apagar")
                        .semantics { contentDescription = "Apagar a letra" }
                        .pointerInput(escrever) {
                            detectTapGestures {
                                sons.toque()
                                apagar()
                            }
                        }
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(3.dp, Color(0xFFD9CFF0), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🧽", fontSize = 30.sp)
                }
            }
        }
    }
}

private fun caminho(t: List<Offset>): Path = Path().apply {
    moveTo(t[0].x, t[0].y)
    for (i in 1 until t.size) lineTo(t[i].x, t[i].y)
}

/** Linhas do caderno de caligrafia, na altura das letras de [TracosDasLetras]: em cima, no meio (pontilhada) e embaixo. */
private fun DrawScope.linhasDoCaderno() {
    val fino = 2.dp.toPx()
    val esquerda = TracosDasLetras.paraTela(Offset(0f, 0f), size.width, size.height).x
    for ((y, tracejada) in listOf(0.15f to false, 0.5f to true, 0.85f to false)) {
        val py = TracosDasLetras.paraTela(Offset(0f, y), size.width, size.height).y
        drawLine(
            if (y == 0.85f) LinhaDoCaderno.copy(alpha = 1f) else LinhaDoCaderno.copy(alpha = 0.8f),
            Offset(0f, py),
            Offset(size.width, py),
            strokeWidth = if (y == 0.85f) fino * 1.6f else fino,
            pathEffect = if (tracejada) PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())) else null,
        )
    }
    // A margem vermelhinha do caderno.
    drawLine(MargemDoCaderno, Offset(esquerda + 10.dp.toPx(), 0f), Offset(esquerda + 10.dp.toPx(), size.height), strokeWidth = fino)
}

/**
 * Uma letra escrita pela criança ([tracos] de 0 a 1 dentro da área de escrever, que tem a proporção da
 * letra), desenhada numa caixinha com a linha de baixo do caderno. Usada nas casinhas e no "Meu caderno".
 */
@Composable
internal fun LetraEscrita(tracos: List<List<Ponto>>, cor: Color, modifier: Modifier = Modifier, linhas: Boolean = false) {
    Canvas(modifier) {
        val w = min(size.width, size.height / TracosDasLetras.PROPORCAO)
        val h = w * TracosDasLetras.PROPORCAO
        val x0 = (size.width - w) / 2f
        val y0 = (size.height - h) / 2f
        if (linhas) {
            val base = y0 + h * 0.85f
            drawLine(LinhaDoCaderno, Offset(0f, base), Offset(size.width, base), strokeWidth = 1.5.dp.toPx())
            val topo = y0 + h * 0.15f
            drawLine(LinhaDoCaderno.copy(alpha = 0.6f), Offset(0f, topo), Offset(size.width, topo), strokeWidth = 1.dp.toPx())
        }
        val grossura = (w * 0.07f).coerceIn(2.dp.toPx(), 6.dp.toPx())
        for (t in tracos) {
            val pontos = t.map { Offset(x0 + it.x * w, y0 + it.y * h) }
            if (pontos.size >= 2) drawPath(caminho(pontos), cor, style = Stroke(grossura, cap = StrokeCap.Round, join = StrokeJoin.Round))
            else if (pontos.size == 1) drawCircle(cor, grossura / 2f, pontos[0])
        }
    }
}
