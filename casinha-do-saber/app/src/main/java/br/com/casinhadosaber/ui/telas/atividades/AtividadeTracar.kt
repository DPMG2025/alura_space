package br.com.casinhadosaber.ui.telas.atividades

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import br.com.casinhadosaber.R
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * A letra vira um "molde" de pontinhos: [pontos] em coordenadas de 0 a 1, [contorno] em pixels de um
 * quadrado de lado [tamanho], e [passo] (distância entre pontinhos, de 0 a 1).
 */
private class Molde(val pontos: List<Offset>, val contorno: android.graphics.Path?, val tamanho: Float, val passo: Float)

/** Desenha a letra numa imagem pequena e marca pontinhos onde há tinta: funciona para qualquer letra. */
private fun criarMolde(letra: String, fonte: Typeface): Molde {
    val n = 320
    val vazio = Molde(emptyList(), null, n.toFloat(), 1f / 15f)
    return runCatching {
        val pincel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = fonte
            textSize = 200f
            color = android.graphics.Color.BLACK
        }
        val caixa = android.graphics.Rect()
        pincel.getTextBounds(letra, 0, letra.length, caixa)
        if (caixa.width() <= 0 || caixa.height() <= 0) return vazio
        pincel.textSize = 200f * (n * 0.74f) / max(caixa.width(), caixa.height())
        pincel.getTextBounds(letra, 0, letra.length, caixa)
        val x = (n - caixa.width()) / 2f - caixa.left
        val y = (n - caixa.height()) / 2f - caixa.top
        val contorno = android.graphics.Path()
        pincel.getTextPath(letra, 0, letra.length, x, y, contorno)
        val imagem = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        android.graphics.Canvas(imagem).drawText(letra, x, y, pincel)
        val passo = n / 15f
        val pontos = mutableListOf<Offset>()
        var py = passo / 2f
        while (py < n) {
            var px = passo / 2f
            while (px < n) {
                if (android.graphics.Color.alpha(imagem.getPixel(px.toInt(), py.toInt())) > 128) pontos += Offset(px / n, py / n)
                px += passo
            }
            py += passo
        }
        imagem.recycle()
        Molde(pontos, contorno, n.toFloat(), passo / n)
    }.getOrDefault(vazio)
}

/** Caminho curto pelos pontinhos, começando no mais alto: a mãozinha mostra como passar o dedo. */
private fun caminhoDeExemplo(m: Molde): List<Offset> {
    if (m.pontos.isEmpty()) return emptyList()
    val visitados = BooleanArray(m.pontos.size)
    var atual = m.pontos.minWith(compareBy<Offset>({ it.y }, { it.x }))
    val caminho = mutableListOf(atual)
    repeat(14) {
        m.pontos.forEachIndexed { i, p -> if ((p - atual).getDistance() <= m.passo * 1.6f) visitados[i] = true }
        val proximo = m.pontos.indices.filter { !visitados[it] }.minByOrNull { (m.pontos[it] - atual).getDistance() } ?: return caminho
        if ((m.pontos[proximo] - atual).getDistance() > m.passo * 2.6f) return caminho
        atual = m.pontos[proximo]
        caminho += atual
    }
    return caminho
}

/**
 * Traçar a letra: passar o dedo por cima dos pontinhos. Cada pontinho tocado acende e a barrinha
 * embaixo enche; com 85% acesos a letra está pronta. A tolerância é grande (dedinho de criança).
 * Errar = um rabisco comprido quase todo fora da letra (o narrador pede para seguir os pontinhos).
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
    val contexto = LocalContext.current
    val densidade = LocalDensity.current
    val escopo = rememberCoroutineScope()
    val ativaAgora by rememberUpdatedState(ativa)
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    val molde = remember(tracar.letra) {
        val fonte = runCatching { ResourcesCompat.getFont(contexto, R.font.fredoka_negrito) }.getOrNull()
            ?: Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        criarMolde(tracar.letra, fonte)
    }
    val contornoCompose = remember(molde) { molde.contorno?.asComposePath() }
    val exemplo = remember(molde) { caminhoDeExemplo(molde) }
    val total = molde.pontos.size
    val acesos = remember(molde) { BooleanArray(total) }
    var cobertos by remember(molde) { mutableIntStateOf(0) }
    var comprimento by remember(molde) { mutableStateOf(0f) }
    val tracos = remember(molde) { mutableStateListOf<List<Offset>>() }
    val tracoAtual = remember(molde) { mutableStateListOf<Offset>() }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var marco by remember { mutableIntStateOf(0) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA

    val progresso = if (total > 0) cobertos / total.toFloat() / 0.85f else comprimento / 2f
    val progressoAnimado by animateFloatAsState(if (terminou) 1f else progresso.coerceIn(0f, 1f), tween(250), label = "progresso")
    val preenchido by animateFloatAsState(if (terminou) 1f else 0f, tween(500), label = "preenchido")
    val brilho = rememberInfiniteTransition(label = "brilho")
    val pulso by brilho.animateFloat(0.8f, 1.25f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "pulso")

    // Medidas do traço atual, para saber se foi um rabisco fora da letra.
    var foraDoTraco by remember { mutableStateOf(0f) }
    var tamanhoDoTraco by remember { mutableStateOf(0f) }

    fun finalizar() {
        if (terminou) return
        terminou = true
        for (i in acesos.indices) acesos[i] = true
        cobertos = total
        escopo.launch {
            delay(450)
            concluir(erros == 0)
        }
    }

    /** Passa o dedo de [de] até [ate] (de 0 a 1), acendendo os pontinhos perto do caminho. */
    fun passar(de: Offset, ate: Offset, raio: Float) {
        val d = (ate - de).getDistance()
        comprimento += d
        tamanhoDoTraco += d
        val passos = max(1, (d / (raio * 0.5f)).toInt())
        var novos = 0
        for (s in 1..passos) {
            val q = de + (ate - de) * (s / passos.toFloat())
            var maisPerto = Float.MAX_VALUE
            for (i in 0 until total) {
                val dist = (molde.pontos[i] - q).getDistance()
                if (dist < maisPerto) maisPerto = dist
                if (!acesos[i] && dist <= raio) {
                    acesos[i] = true
                    novos++
                }
            }
            if (total > 0 && maisPerto > raio * 1.8f) foraDoTraco += d / passos
        }
        if (novos > 0) {
            cobertos += novos
            // Um "plim" a cada quinto do caminho: a criança ouve que está indo bem.
            val agora = (cobertos * 5) / max(1, total)
            if (agora > marco) {
                marco = agora
                sons.toque()
            }
        }
        val pronto = if (total > 0) cobertos >= (total * 0.85f) else comprimento >= 2f
        if (pronto) finalizar()
    }

    fun fimDoTraco() {
        if (tracoAtual.isNotEmpty()) tracos.add(tracoAtual.toList())
        tracoAtual.clear()
        // Rabisco comprido e quase todo fora dos pontinhos: pede com carinho para seguir a letra.
        if (!terminou && total > 0 && tamanhoDoTraco > 0.6f && foraDoTraco / tamanhoDoTraco > 0.55f) {
            erros++
            avisarErro()
            sons.erro()
            narrador.falar("Siga os pontinhos da letra ${Vocabulario.falaDaLetra(tracar.letra)}, devagarinho.")
        }
        foraDoTraco = 0f
        tamanhoDoTraco = 0f
    }

    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val lado = minOf(maxWidth, maxHeight - 52.dp, 440.dp).coerceAtLeast(120.dp)
        val ladoPx = with(densidade) { lado.toPx() }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(lado)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(3.dp, Color(0xFFEDE6FF), RoundedCornerShape(28.dp))
                    .testTag("area_tracar")
                    .semantics { contentDescription = "Letra ${tracar.letra} para traçar" },
            ) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(molde) {
                            awaitEachGesture {
                                val toque = awaitFirstDown()
                                if (!ativaAgora || terminou) return@awaitEachGesture
                                toque.consume()
                                val largura = size.width.toFloat().coerceAtLeast(1f)
                                // Tolerância generosa: 30dp ao redor do dedo, ou um pouco mais que o vão entre os pontinhos.
                                val raio = max(30.dp.toPx() / largura, molde.passo * 1.35f)
                                var ultimo = toque.position / largura
                                tracoAtual.add(ultimo)
                                passar(ultimo, ultimo, raio)
                                while (true) {
                                    val evento = awaitPointerEvent()
                                    val mudanca = evento.changes.firstOrNull { it.id == toque.id } ?: break
                                    if (!mudanca.pressed) break
                                    val q = mudanca.position / largura
                                    mudanca.consume()
                                    if (terminou) continue
                                    if ((q - ultimo).getDistance() * largura < 2f) continue
                                    passar(ultimo, q, raio)
                                    tracoAtual.add(q)
                                    ultimo = q
                                }
                                fimDoTraco()
                            }
                        },
                ) {
                    val l = size.width
                    // Lê o estado: redesenha quando acende um pontinho (os pontinhos ficam num BooleanArray).
                    if (cobertos < 0) return@Canvas
                    // Letra clarinha com contorno tracejado.
                    contornoCompose?.let { caminho ->
                        val s = l / molde.tamanho
                        scale(s, pivot = Offset.Zero) {
                            drawPath(caminho, Color(0xFFF3EEFF))
                            if (preenchido > 0f) drawPath(caminho, cor.copy(alpha = 0.85f * preenchido))
                            drawPath(
                                caminho,
                                Color(0xFFB9ABDD),
                                style = Stroke(
                                    width = 2.5.dp.toPx() / s,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx() / s, 7.dp.toPx() / s)),
                                ),
                            )
                        }
                    }
                    // Rastro do dedo.
                    val grossura = max(18.dp.toPx(), molde.passo * l * 0.9f)
                    for (t in tracos + listOf(tracoAtual.toList())) {
                        if (t.isEmpty()) continue
                        if (t.size == 1) {
                            drawCircle(cor.copy(alpha = 0.35f), grossura / 2f, t[0] * l)
                            continue
                        }
                        val p = Path()
                        p.moveTo(t[0].x * l, t[0].y * l)
                        for (i in 1 until t.size) p.lineTo(t[i].x * l, t[i].y * l)
                        drawPath(p, cor.copy(alpha = 0.35f), style = Stroke(grossura, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    // Pontinhos: apagados esperando o dedo; acesos na cor do caminho.
                    val r = molde.passo * l * 0.3f
                    for (i in 0 until total) {
                        val centro = molde.pontos[i] * l
                        if (acesos[i]) {
                            drawCircle(cor, r * 1.15f, centro)
                            drawCircle(Color.White.copy(alpha = 0.7f), r * 0.35f, centro + Offset(-r * 0.35f, -r * 0.35f))
                        } else {
                            drawCircle(Color(0xFFBCAEE0), r * (if (ajuda) 0.8f * pulso else 0.8f), centro)
                        }
                    }
                }
                // Sem o molde (não deveria acontecer), mostra a letra grandona para passar o dedo.
                if (total == 0) {
                    Text(
                        tracar.letra,
                        fontSize = (lado.value * 0.6f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD9CFF0),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                MaoQueSegue(
                    pontos = exemplo.map { it * ladoPx },
                    ativa = ativa && !terminou && (ajuda || (tracos.isEmpty() && tracoAtual.isEmpty())),
                    espera = if (ajudaInicial) 900L else if (ajuda) 1200L else 3000L,
                )
            }
            // Barrinha de progresso com a estrela no fim.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .width(lado * 0.72f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(3.dp, Color(0xFFEDE6FF), RoundedCornerShape(12.dp)),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressoAnimado.coerceIn(0f, 1f))
                            .background(if (terminou) Cores.Certo else cor, RoundedCornerShape(12.dp)),
                    )
                }
                Text("⭐", fontSize = 28.sp, modifier = Modifier.alpha(if (terminou) 1f else 0.3f + 0.5f * progressoAnimado.coerceIn(0f, 1f)))
            }
        }
    }
}
