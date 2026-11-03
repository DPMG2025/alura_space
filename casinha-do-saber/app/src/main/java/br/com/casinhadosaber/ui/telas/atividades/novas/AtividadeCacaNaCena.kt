package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.CacaNaCena
import br.com.casinhadosaber.dominio.ItemNaCena
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.telas.atividades.ERROS_PARA_AJUDA
import br.com.casinhadosaber.ui.telas.atividades.painelDaCena
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Stable
private class Escondido(val indice: Int, val item: ItemNaCena, val certo: Boolean) {
    /** Já foi achado (está voando para a barra ou já está nela). */
    var achado by mutableStateOf(false)
    /** 0 a 1: o voo da cena até a casinha da barra. */
    val voo = Animatable(0f)
    val tremida = Animatable(0f)
    var tremendo by mutableStateOf(false)
    /** Casinha da barra para onde vai (gravada no toque). */
    var vaga = 0
}

/** Cores e jeito do fundo de cada mundo (a cena combina com o mundo da fase). */
private class Cenario(
    val ceuAlto: Color,
    val ceuBaixo: Color,
    val chao: Color,
    val chaoLonge: Color,
    val moita: Color,
    val pedra: Color,
    val noite: Boolean = false,
)

private fun cenarioDo(mundo: Int): Cenario {
    val tom = Color(Mundos.todos[mundo.coerceIn(0, Mundos.todos.lastIndex)].corArgb)
    val ceu = Color(0xFF8FD3FF)
    val verde = Cenario(ceu, lerp(Color(0xFFDDF3FF), tom, 0.5f), Color(0xFF7CC56B), Color(0xFF9BD77E), Color(0xFF4CAF50), Color(0xFFB0A8B9))
    return when (mundo) {
        2, 10, 14, 22 -> Cenario(ceu, lerp(Color(0xFFFFF6DA), tom, 0.4f), Color(0xFFF3D9A4), Color(0xFFEBCB8B), Color(0xFF6DBE5B), Color(0xFFC9A27A))
        3, 8 -> Cenario(Color(0xFFA9DBFF), Color(0xFFEAF6FF), Color(0xFFF4F9FF), Color(0xFFDDEBF7), Color(0xFF7FB8A4), Color(0xFFA8B4C4))
        6 -> Cenario(Color(0xFF4FB3E0), Color(0xFF9FE3F5), Color(0xFFF1D9A6), Color(0xFF7FCBE6), Color(0xFF2FA37B), Color(0xFFB79DC9))
        4, 24, 19 -> Cenario(Color(0xFF26245A), lerp(Color(0xFF4B3F8F), tom, 0.25f), Color(0xFF6B6FA8), Color(0xFF585C96), Color(0xFF4FA3A0), Color(0xFF9D96C7), noite = true)
        11, 21 -> Cenario(ceu, Color(0xFFE7EEF2), Color(0xFFB8C4CC), Color(0xFFCFD8DC), Color(0xFF66BB6A), Color(0xFF90A4AE))
        15 -> Cenario(Color(0xFFFFC1DD), Color(0xFFFFE9F3), Color(0xFFF8BBD0), Color(0xFFFFD6E6), Color(0xFFB388FF), Color(0xFFFFE082))
        17 -> Cenario(Color(0xFFFFB38A), Color(0xFFFFE0CC), Color(0xFFB98B6E), Color(0xFFCFA182), Color(0xFF7CB342), Color(0xFF8D6E63))
        18 -> Cenario(Color(0xFF9FD8FF), Color(0xFFE6F6FF), Color(0xFFFFFFFF), Color(0xFFEAF4FF), Color(0xFFB3E5FC), Color(0xFFD7E3F0))
        else -> verde
    }
}

/** Enfeite do fundo pré calculado: moita, pedra ou flor, em frações da cena. */
private class Enfeite(val x: Float, val y: Float, val r: Float, val tipo: Int)

/**
 * Caça ao tesouro: uma cena grande do mundo com figuras espalhadas, algumas meio escondidas atrás de
 * moitas, pedras ou nuvens. Tocar numa figura [CacaNaCena.certos] faz ela brilhar e voar para a barra de
 * achados; figura errada diz o próprio nome, balança e conta um erro.
 */
@Composable
internal fun AtividadeCacaNaCena(
    cacaNaCena: CacaNaCena,
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

    val itens = remember(cacaNaCena) { cacaNaCena.itens.mapIndexed { i, it -> Escondido(i, it, i in cacaNaCena.certos) } }
    val total = cacaNaCena.certos.size
    val cenario = remember(cacaNaCena.mundo) { cenarioDo(cacaNaCena.mundo) }
    val enfeites = remember(cacaNaCena.mundo) {
        val r = Random(cacaNaCena.mundo * 31 + 7)
        List(9) { k -> Enfeite(r.nextFloat(), 0.42f + 0.55f * r.nextFloat(), 0.05f + 0.04f * r.nextFloat(), k % 3) }
    }
    var erros by remember(cacaNaCena) { mutableIntStateOf(0) }
    var terminou by remember(cacaNaCena) { mutableStateOf(false) }
    var interacoes by remember(cacaNaCena) { mutableIntStateOf(0) }
    var encontrados by remember(cacaNaCena) { mutableIntStateOf(0) }
    val naBarra = remember(cacaNaCena) { mutableStateOf(listOf<Opcao>()) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val parada = n1Parada(interacoes, ativa, terminou, ajuda, ajudaInicial)
    val relogio = n1Relogio()

    BoxWithConstraints(modifier.fillMaxSize().painelDaCena()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val d = densidade.density
        val margem = 8f * d

        // ---------------------------------------------------------------- medidas (pixels)
        val ladoBarra = min(54f * d, (w - 2 * margem - 12f * d) / total - 6f * d).coerceAtLeast(26f * d)
        val alturaBarra = ladoBarra + 12f * d
        val cenaTopo = alturaBarra + 12f * d
        val cenaW = w - 2 * margem
        val cenaH = h - cenaTopo - margem
        val horizonte = cenaTopo + cenaH * 0.34f
        val base = (min(cenaW, cenaH) / 4.6f).coerceIn(56f * d, 100f * d)

        fun ladoFigura(e: Escondido) = base * e.item.escala
        fun ladoToque(e: Escondido) = max(ladoFigura(e) * 1.05f, 68f * d)
        fun centro(e: Escondido): Offset {
            val folga = ladoToque(e) / 2f
            val x = margem + folga + e.item.x * (cenaW - 2 * folga)
            val y = cenaTopo + folga + e.item.y * (cenaH - 2 * folga)
            return Offset(x, y)
        }
        // Casinhas da barra (mesma conta de N1BarraDeAchados: casinhas de lado + 6 dp, 6 dp de borda).
        val larguraLinha = (ladoBarra + 6f * d) * total + 12f * d
        fun vagaNaBarra(k: Int) = Offset((w - larguraLinha) / 2f + 6f * d + (ladoBarra + 6f * d) * (k + 0.5f), 6f * d + alturaBarra / 2f)

        fun tocar(e: Escondido) {
            if (!ativaAgora || terminou || e.achado || e.tremendo) return
            interacoes++
            if (e.certo) {
                e.achado = true
                e.vaga = encontrados
                encontrados++
                sons.colocar()
                vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                narrador.falar(n1Nome(e.item.opcao))
                val acabou = encontrados == total
                if (acabou) terminou = true
                escopo.launch {
                    e.voo.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
                    naBarra.value = naBarra.value + e.item.opcao
                    sons.pousar()
                    if (acabou) {
                        n1Esperar(400)
                        concluir(erros == 0)
                    }
                }
            } else {
                erros++
                avisarErro()
                sons.erro()
                narrador.falar(n1Nome(e.item.opcao) + falaAtual)
                e.tremendo = true
                escopo.launch {
                    for (x in listOf(14f, -14f, 10f, -10f, 5f, 0f)) e.tremida.animateTo(x, tween(70))
                    e.tremendo = false
                }
            }
        }

        // ---------------------------------------------------------------- fundo do mundo
        val ceu = remember(cenario, h) { Brush.verticalGradient(listOf(cenario.ceuAlto, cenario.ceuBaixo), endY = horizonte) }
        Canvas(Modifier.matchParentSize()) {
            drawRect(ceu)
            if (cenario.noite) {
                for (k in 0 until 24) {
                    val x = ((k * 73) % 100) / 100f * w
                    val y = cenaTopo + ((k * 41) % 100) / 100f * (horizonte - cenaTopo)
                    drawCircle(Color.White.copy(alpha = 0.5f + 0.4f * ((k * 7) % 3) / 2f), (1.5f + (k % 3)) * d, Offset(x, y))
                }
                drawCircle(Color(0xFFFFF3C4), 22f * d, Offset(w * 0.82f, cenaTopo + 30f * d))
            } else {
                drawCircle(Color(0x55FFE082), 40f * d, Offset(w * 0.84f, cenaTopo + 34f * d))
                drawCircle(Color(0xFFFFD54F), 26f * d, Offset(w * 0.84f, cenaTopo + 34f * d))
                desenharNuvemDaCena(Offset(w * 0.22f, cenaTopo + 40f * d), 18f * d)
            }
            // Morro do fundo e chão
            drawOval(cenario.chaoLonge, Offset(-w * 0.3f, horizonte - cenaH * 0.12f), Size(w * 0.9f, cenaH * 0.4f))
            drawOval(cenario.chaoLonge, Offset(w * 0.35f, horizonte - cenaH * 0.08f), Size(w * 0.95f, cenaH * 0.4f))
            drawRect(cenario.chao, Offset(0f, horizonte), Size(w, h - horizonte))
            for (e in enfeites) {
                val c = Offset(margem + e.x * cenaW, cenaTopo + e.y * cenaH)
                val r = e.r * cenaW
                // Enfeite do fundo nunca encosta numa figura (senão parece que ela está em cima da pedra).
                if (itens.any { (centro(it) - c).getDistance() < ladoToque(it) * 0.62f + r * 1.4f }) continue
                when (e.tipo) {
                    0 -> desenharMoita(c, r * 0.8f, cenario.moita)
                    1 -> desenharPedra(c, r * 0.6f, cenario.pedra)
                    else -> {
                        drawCircle(Color.White, r * 0.18f, c)
                        drawCircle(Color(0xFFFFD54F), r * 0.08f, c)
                    }
                }
            }
        }

        // ---------------------------------------------------------------- figuras
        for (e in itens) {
            if (e.voo.value >= 1f) continue
            val lado = ladoToque(e)
            val ladoDp = with(densidade) { lado.toDp() }
            val destacar = ajuda && e.certo && !e.achado && !terminou
            Box(
                Modifier
                    .zIndex(if (e.achado) 4f else 1f)
                    .offset {
                        val c0 = centro(e)
                        val v = FastOutSlowInEasing.transform(e.voo.value)
                        val alvo = vagaNaBarra(e.vaga)
                        val x = c0.x + (alvo.x - c0.x) * v
                        val y = c0.y + (alvo.y - c0.y) * v - sin(PI.toFloat() * v) * cenaH * 0.15f
                        IntOffset((x - lado / 2f).roundToInt(), (y - lado / 2f).roundToInt())
                    }
                    .size(ladoDp)
                    .graphicsLayer {
                        val v = e.voo.value
                        // Cresce um pouquinho (o brilho do achado) e encolhe até o tamanho da casinha.
                        val escala = if (v < 0.25f) 1f + v * 1.2f else 1.3f - (1.3f - ladoBarra / lado) * ((v - 0.25f) / 0.75f)
                        scaleX = escala
                        scaleY = escala
                        translationX = e.tremida.value * density
                    }
                    .testTag(
                        when {
                            e.achado -> "cena_achado"
                            e.certo -> "cena_certo"
                            else -> "cena_errado"
                        },
                    )
                    .semantics {
                        contentDescription = n1Descricao(e.item.opcao)
                        role = Role.Button
                        onClick { tocar(e); true }
                    }
                    .pointerInput(e, w, h) { detectTapGestures(onPress = { tocar(e) }) },
                contentAlignment = Alignment.Center,
            ) {
                if (destacar) {
                    Canvas(Modifier.matchParentSize()) { n1Brilho(center, size.minDimension * 0.5f, relogio.floatValue) }
                }
                N1Figura(e.item.opcao, with(densidade) { ladoFigura(e).toDp() })
            }
        }

        // ---------------------------------------------------------------- moitas, pedras e nuvens na frente de algumas figuras
        Canvas(Modifier.matchParentSize().zIndex(2f)) {
            for (e in itens) {
                if (e.indice % 3 != 1 || e.voo.value > 0f) continue
                val c = centro(e)
                val l = ladoFigura(e)
                // Só o pé da figura fica escondido (um terço, no máximo): a criança sempre reconhece o que é.
                val frente = Offset(c.x + (if (e.indice % 2 == 0) -l * 0.12f else l * 0.12f), c.y + l * 0.5f)
                when {
                    c.y + l * 0.3f < horizonte -> desenharNuvemDaCena(frente, l * 0.18f)
                    e.indice % 2 == 0 -> desenharPedra(frente, l * 0.3f, cenario.pedra)
                    else -> desenharMoita(frente, l * 0.25f, cenario.moita)
                }
            }
            // Brilho de quem acabou de ser achado.
            for (e in itens) {
                val v = e.voo.value
                if (v > 0f && v < 0.6f) n1Estouro(centro(e), ladoFigura(e) * 0.55f, v / 0.6f, cor)
            }
        }

        // ---------------------------------------------------------------- barra de achados
        Box(
            Modifier
                .zIndex(3f)
                .offset { IntOffset(0, (6f * d).roundToInt()) }
                .size(with(densidade) { w.toDp() }, with(densidade) { alturaBarra.toDp() }),
            contentAlignment = Alignment.TopCenter,
        ) {
            N1BarraDeAchados(total, naBarra.value, escurecer(cor), with(densidade) { (w - 2 * margem).toDp() })
        }

        // ---------------------------------------------------------------- mãozinha
        val alvo = itens.firstOrNull { it.certo && !it.achado }
        if (alvo != null && ativa && !terminou && parada) {
            val c = centro(alvo)
            val meio = 20f * d
            Box(
                Modifier
                    .zIndex(6f)
                    .offset { IntOffset((c.x - meio).roundToInt(), (c.y - meio).roundToInt()) },
            ) { MaoGuia(ativa = true, espera = 0L) }
        }
    }
}

private fun DrawScope.desenharMoita(c: Offset, r: Float, cor: Color) {
    val escura = escurecer(cor, 0.82f)
    drawCircle(escura, r, c + Offset(-r * 0.9f, r * 0.25f))
    drawCircle(escura, r, c + Offset(r * 0.9f, r * 0.25f))
    drawCircle(cor, r * 1.15f, c)
    drawCircle(cor, r * 0.85f, c + Offset(-r * 0.7f, r * 0.1f))
    drawCircle(cor, r * 0.85f, c + Offset(r * 0.7f, r * 0.1f))
    drawCircle(Color.White.copy(alpha = 0.18f), r * 0.4f, c + Offset(-r * 0.3f, -r * 0.45f))
    drawRect(cor, Offset(c.x - r * 1.6f, c.y + r * 0.2f), Size(r * 3.2f, r * 0.8f))
}

private fun DrawScope.desenharPedra(c: Offset, r: Float, cor: Color) {
    drawOval(escurecer(cor, 0.8f), Offset(c.x - r * 1.5f, c.y - r * 0.8f), Size(r * 3f, r * 1.8f))
    drawOval(cor, Offset(c.x - r * 1.45f, c.y - r * 0.85f), Size(r * 2.7f, r * 1.6f))
    drawOval(Color.White.copy(alpha = 0.3f), Offset(c.x - r * 0.9f, c.y - r * 0.6f), Size(r * 0.9f, r * 0.45f))
}

private fun DrawScope.desenharNuvemDaCena(c: Offset, r: Float) {
    val branco = Color.White.copy(alpha = 0.95f)
    drawCircle(branco, r * 1.3f, c)
    drawCircle(branco, r, c + Offset(-r * 1.4f, r * 0.3f))
    drawCircle(branco, r * 1.05f, c + Offset(r * 1.4f, r * 0.25f))
    drawCircle(branco, r * 0.9f, c + Offset(r * 0.5f, -r * 0.8f))
}
