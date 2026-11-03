package br.com.casinhadosaber.ui.telas.atividades

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private val VerdeDoComeco = Color(0xFF2FB463)
private val LilasDoCaminho = Color(0xFFB9ABDD)

/** Quantos pontinhos à frente do último alcançado o dedo pode "pular" (tolerância de dedinho). */
private const val JANELA = 9

/**
 * Traçar a letra do jeito que se escreve: um traço de cada vez, na ordem (1, 2, 3...).
 * Cada traço começa num ponto verde pulsando, tem uma setinha numerada mostrando para onde ir e é
 * um caminho fino de bolinhas. O dedo precisa passar perto do caminho, do começo para o fim; a
 * tolerância é grande e soltar o dedo não perde o que já foi feito (dá para continuar de onde parou).
 * Errar = um rabisco comprido longe de todos os traços da letra.
 */
@Composable
internal fun AtividadeTracar(
    tracar: Atividade.Tracar,
    ativa: Boolean,
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val densidade = LocalDensity.current
    val escopo = rememberCoroutineScope()
    val ativaAgora by rememberUpdatedState(ativa)
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)
    val medidor = rememberTextMeasurer()

    val tracos = remember(tracar.letra) { TracosDasLetras.tracosDe(tracar.letra) }
    val pontos = remember(tracos) { tracos.map { TracosDasLetras.pontosDoTraco(it, 0.025f) } }
    val total = pontos.sumOf { it.size }.coerceAtLeast(1)

    var tracoAtual by remember(tracos) { mutableIntStateOf(0) }
    var alcancado by remember(tracos) { mutableIntStateOf(0) }
    val rastro = remember(tracos) { mutableStateListOf<Offset>() }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var interacoes by remember { mutableIntStateOf(0) }
    var parada by remember { mutableStateOf(false) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA

    val feitos = pontos.take(tracoAtual).sumOf { it.size } + alcancado
    val progresso by animateFloatAsState(if (terminou) 1f else feitos / total.toFloat(), tween(250), label = "progresso")
    val brilho = rememberInfiniteTransition(label = "brilho")
    val pulso by brilho.animateFloat(0f, 1f, infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart), label = "pulso")

    // A mãozinha faz o traço da vez logo no começo e de novo quando a criança fica parada.
    LaunchedEffect(interacoes, terminou, ativa, tracoAtual, ajuda) {
        parada = false
        if (!ativa || terminou) return@LaunchedEffect
        delay(
            when {
                interacoes == 0 && tracoAtual == 0 -> if (ajudaInicial) 600L else 1200L
                ajuda -> 2500L
                else -> 6000L
            },
        )
        parada = true
    }

    fun finalizar() {
        if (terminou) return
        terminou = true
        tracoAtual = tracos.size
        alcancado = 0
        escopo.launch {
            delay(450)
            concluir(erros == 0)
        }
    }

    fun completarTraco() {
        sons.colocar()
        if (tracoAtual >= tracos.lastIndex) {
            finalizar()
        } else {
            tracoAtual++
            alcancado = 0
        }
    }

    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val barra = 36.dp
        val vao = 12.dp
        val alturaLivre = (maxHeight - barra - vao).coerceAtLeast(140.dp)
        // A área ocupa a altura que houver (até 1,3 vez a largura), sem faixa vazia em cima e embaixo.
        val largura = minOf(maxWidth, 600.dp, alturaLivre)
        val altura = minOf(alturaLivre, largura * 1.3f)
        val larguraPx = with(densidade) { largura.toPx() }
        val alturaPx = with(densidade) { altura.toPx() }
        val emTela = remember(pontos, larguraPx, alturaPx) {
            pontos.map { t -> t.map { TracosDasLetras.paraTela(it, larguraPx, alturaPx) } }
        }
        val raio = max(with(densidade) { 38.dp.toPx() }, min(larguraPx, alturaPx) * 0.11f)

        /** Distância de [q] até o traço [t] (aproximada pelos pontinhos dele). */
        fun distancia(q: Offset, t: List<Offset>): Float = t.minOf { (it - q).getDistance() }

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(vao, Alignment.CenterVertically),
        ) {
            Box(
                Modifier
                    .size(largura, altura)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(3.dp, Color(0xFFEDE6FF), RoundedCornerShape(28.dp))
                    .testTag("area_tracar")
                    .semantics { contentDescription = "Letra ${tracar.letra} para traçar" },
            ) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(emTela) {
                            awaitEachGesture {
                                val toque = awaitFirstDown()
                                if (!ativaAgora || terminou) return@awaitEachGesture
                                toque.consume()
                                interacoes++
                                var ultimo = toque.position
                                var comprimento = 0f
                                var longe = 0f
                                var avancou = false
                                var pertoDeOutro = false
                                rastro.clear()
                                rastro.add(ultimo)

                                /** Anda pelo caminho do traço da vez, na ordem, perto de [q]. */
                                fun seguir(q: Offset) {
                                    if (terminou) return
                                    val t = emTela.getOrNull(tracoAtual) ?: return
                                    val ate = min(t.size, alcancado + JANELA)
                                    var melhor = -1
                                    for (j in alcancado until ate) if ((t[j] - q).getDistance() <= raio) melhor = j
                                    if (melhor >= 0) {
                                        val antes = (alcancado * 4) / t.size
                                        alcancado = melhor + 1
                                        avancou = true
                                        // Um "plim" a cada quarto do traço: a criança ouve que está indo bem.
                                        if ((alcancado * 4) / t.size > antes && alcancado < t.size) sons.toque()
                                        if (alcancado >= t.size) completarTraco()
                                    }
                                }

                                seguir(ultimo)
                                while (true) {
                                    val evento = awaitPointerEvent()
                                    val mudanca = evento.changes.firstOrNull { it.id == toque.id } ?: break
                                    if (!mudanca.pressed) break
                                    mudanca.consume()
                                    val q = mudanca.position
                                    val d = (q - ultimo).getDistance()
                                    if (d < 2f || terminou) continue
                                    // Passos curtos: um movimento rápido não pula o caminho.
                                    val passos = max(1, (d / (raio * 0.4f)).toInt())
                                    for (s in 1..passos) {
                                        val p = ultimo + (q - ultimo) * (s / passos.toFloat())
                                        seguir(p)
                                        val atual = emTela.getOrNull(tracoAtual)
                                        if (atual != null && distancia(p, atual) > raio * 1.6f) {
                                            if (emTela.all { distancia(p, it) > raio * 1.6f }) longe += d / passos else pertoDeOutro = true
                                        }
                                    }
                                    comprimento += d
                                    rastro.add(q)
                                    ultimo = q
                                }
                                rastro.clear()
                                if (terminou) return@awaitEachGesture
                                // Soltou quase no fim do traço: vale (dedinho de criança).
                                val t = emTela.getOrNull(tracoAtual)
                                if (t != null && alcancado >= t.size * 0.8f) {
                                    completarTraco()
                                    return@awaitEachGesture
                                }
                                val grande = min(larguraPx, alturaPx) * 0.5f
                                when {
                                    // Rabisco comprido longe da letra: pede com carinho para seguir o caminho.
                                    !avancou && comprimento > grande && longe / comprimento > 0.6f -> {
                                        erros++
                                        avisarErro()
                                        sons.erro()
                                        narrador.falar("Comece no pontinho verde e siga as bolinhas da letra ${Vocabulario.falaDaLetra(tracar.letra)}.")
                                    }
                                    // Fez outro traço antes da hora: só lembra da ordem, sem contar erro.
                                    !avancou && pertoDeOutro && comprimento > grande * 0.5f -> {
                                        sons.pousar()
                                        narrador.falar("Agora é o número ${tracoAtual + 1}. Comece no pontinho verde.")
                                    }
                                }
                            }
                        },
                ) {
                    val fino = 2.dp.toPx()
                    val bolinha = 7.dp.toPx()
                    val tinta = 18.dp.toPx()
                    val pontilhado = PathEffect.dashPathEffect(floatArrayOf(0.1f, 15.dp.toPx()))

                    // 1) O caminho de cada traço: linha bem fina e bolinhas (os que faltam).
                    emTela.forEachIndexed { i, t ->
                        if (i < tracoAtual || t.size < 2) return@forEachIndexed
                        val caminho = caminhoDe(t)
                        drawPath(caminho, Color(0xFFEDE6FF), style = Stroke(fino * 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        val daVez = i == tracoAtual
                        drawPath(
                            caminho,
                            if (daVez) cor.copy(alpha = 0.75f) else LilasDoCaminho,
                            style = Stroke(if (daVez) bolinha * 1.25f else bolinha, cap = StrokeCap.Round, pathEffect = pontilhado),
                        )
                    }
                    // 2) O que já foi traçado: tinta grossa na cor do caminho.
                    emTela.forEachIndexed { i, t ->
                        val fim = when {
                            i < tracoAtual -> t.size
                            i == tracoAtual -> alcancado
                            else -> 0
                        }
                        if (fim >= 2) drawPath(caminhoDe(t.take(fim)), cor, style = Stroke(tinta, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        else if (fim == 1) drawCircle(cor, tinta / 2f, t[0])
                    }
                    // 3) O rastro do dedo, clarinho.
                    if (rastro.size >= 2) {
                        drawPath(caminhoDe(rastro.toList()), cor.copy(alpha = 0.22f), style = Stroke(tinta * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    // 4) Setas numeradas e o ponto verde do começo (os traços que faltam, do último ao da vez).
                    if (!terminou) {
                        for (i in emTela.indices.reversed()) {
                            if (i < tracoAtual) continue
                            val t = emTela[i]
                            val daVez = i == tracoAtual
                            val corDaMarca = if (daVez) VerdeDoComeco else LilasDoCaminho
                            setinha(t, 0.45f, corDaMarca, if (daVez) 15.dp.toPx() else 11.dp.toPx())
                            if (t.size > 12) setinha(t, 0.85f, corDaMarca.copy(alpha = 0.7f), if (daVez) 12.dp.toPx() else 9.dp.toPx())
                            val inicio = if (daVez && alcancado > 0) t[min(alcancado, t.lastIndex)] else t[0]
                            val r = if (daVez) 19.dp.toPx() else 14.dp.toPx()
                            if (daVez && alcancado == 0) {
                                // Pulso: um anel que cresce e some.
                                drawCircle(VerdeDoComeco.copy(alpha = 0.5f * (1f - pulso)), r * (1f + 1.1f * pulso), inicio)
                            }
                            drawCircle(Color.White, r + 3.dp.toPx(), inicio)
                            drawCircle(corDaMarca, r, inicio)
                            if (daVez && alcancado > 0) continue
                            val numero = medidor.measure(
                                "${i + 1}",
                                TextStyle(fontSize = if (daVez) 20.sp else 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                            drawText(
                                numero,
                                topLeft = Offset(inicio.x - numero.size.width / 2f, inicio.y - numero.size.height / 2f),
                            )
                        }
                    }
                }
                // A mãozinha faz o traço da vez (do ponto verde até o fim, seguindo a seta).
                key(tracoAtual) {
                    MaoQueSegue(
                        pontos = emTela.getOrNull(tracoAtual).orEmpty(),
                        ativa = ativa && !terminou && parada && rastro.isEmpty(),
                        espera = 0L,
                    )
                }
            }
            // Barrinha de progresso com a estrela no fim.
            Row(
                Modifier.height(barra),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .width(largura * 0.72f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(3.dp, Color(0xFFEDE6FF), RoundedCornerShape(12.dp)),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progresso.coerceIn(0f, 1f))
                            .background(if (terminou) Cores.Certo else cor, RoundedCornerShape(12.dp)),
                    )
                }
                Text("⭐", fontSize = 28.sp, modifier = Modifier.alpha(if (terminou) 1f else 0.3f + 0.5f * progresso.coerceIn(0f, 1f)))
            }
        }
    }
}

private fun caminhoDe(t: List<Offset>): Path = Path().apply {
    moveTo(t[0].x, t[0].y)
    for (i in 1 until t.size) lineTo(t[i].x, t[i].y)
}

/** Ponta de seta (triângulo) no ponto a uma fração [k] do traço, apontando para onde o traço vai. */
private fun DrawScope.setinha(t: List<Offset>, k: Float, cor: Color, tamanho: Float) {
    if (t.size < 3) return
    val a = pontoNoCaminho(t, (k - 0.04f).coerceAtLeast(0f))
    val b = pontoNoCaminho(t, k)
    val d = b - a
    val n = sqrt(d.x * d.x + d.y * d.y)
    if (n < 0.5f) return
    val dir = Offset(d.x / n, d.y / n)
    val lado = Offset(-dir.y, dir.x)
    val ponta = b + dir * (tamanho * 0.6f)
    val tras = b - dir * (tamanho * 0.6f)
    val seta = Path().apply {
        moveTo(ponta.x, ponta.y)
        lineTo(tras.x + lado.x * tamanho * 0.7f, tras.y + lado.y * tamanho * 0.7f)
        lineTo(tras.x - lado.x * tamanho * 0.7f, tras.y - lado.y * tamanho * 0.7f)
        close()
    }
    drawPath(seta, Color.White, style = Stroke(5.dp.toPx(), join = StrokeJoin.Round))
    drawPath(seta, cor)
}
