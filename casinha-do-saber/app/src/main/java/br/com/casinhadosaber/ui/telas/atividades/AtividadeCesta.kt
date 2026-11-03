package br.com.casinhadosaber.ui.telas.atividades

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Stable
private class FiguraDaCesta(val id: Int, val emoji: String, val certa: Boolean) {
    val peca = PecaMovel()
    /** Lugar dentro da cesta, ou -1 se está fora. */
    var vaga by mutableIntStateOf(-1)
    /** Já foi recusada pela cesta (fica mais clarinha, para a criança lembrar). */
    var recusada by mutableStateOf(false)
}

private val CorPalha = Color(0xFFD9A066)
private val CorPalhaEscura = Color(0xFFA8702F)
private val CorPalhaClara = Color(0xFFF0C88E)
private val CorFundoDaCesta = Color(0xFF6E4520)

/**
 * Cesta: arrastar as figuras para dentro.
 * * Contar: colocar [Atividade.Cesta.alvo] figuras e tocar no certinho. Errar = tocar no certinho
 *   com a quantidade errada (o narrador diz quantas há e quantas faltam ou sobram).
 * * Classificar: só as figuras do grupo entram. Errar = soltar na cesta uma figura de fora do grupo
 *   (ela volta para o lugar e o narrador explica). Termina sozinho quando todas as do grupo entram.
 */
@Composable
internal fun AtividadeCesta(
    cesta: Atividade.Cesta,
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

    val classificar = cesta.classificar
    val figuras = remember(cesta) {
        (cesta.certos.map { it to true } + cesta.errados.map { it to false })
            .shuffled()
            .mapIndexed { i, (emoji, certa) -> FiguraDaCesta(i, emoji, certa) }
    }
    val feminino = remember(cesta) {
        val e = cesta.certos.firstOrNull().orEmpty().replace("\uFE0F", "")
        Vocabulario.objetosParaContar.firstOrNull { it.emoji.replace("\uFE0F", "") == e }?.feminino ?: false
    }
    var erros by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    // A mãozinha mostra o gesto quando a criança fica parada: logo no começo e de novo se ela parar.
    var interacoes by remember { mutableIntStateOf(0) }
    var tocouSemArrastar by remember { mutableStateOf(false) }
    var parada by remember { mutableStateOf(false) }
    val tremidaDaCesta = remember { Animatable(0f) }
    val ajuda = ajudaInicial || erros >= ERROS_PARA_AJUDA
    val naCesta = figuras.count { it.vaga >= 0 }
    LaunchedEffect(interacoes, terminou, ativa, ajuda) {
        parada = false
        if (!ativa || terminou) return@LaunchedEffect
        delay(
            when {
                ajudaInicial && interacoes == 0 -> 700L
                interacoes == 0 -> 1200L
                tocouSemArrastar -> 900L
                ajuda -> 3500L
                else -> 7000L
            },
        )
        parada = true
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        fun px(v: Float) = v * densidade.density

        // ---------------------------------------------------------------- medidas (em pixels)
        // A cesta fica embaixo; no modo contar, o certinho aparece à direita dela (o lugar fica reservado).
        val botao = px(80f)
        val reservaBotao = if (classificar) 0f else botao + px(14f)
        val larguraCesta = min(w * (if (classificar) 0.72f else 0.64f), px(440f)).coerceAtMost(w - reservaBotao - px(8f))
        val alturaCesta = min(larguraCesta * 0.52f, h * 0.27f)
        val alcaCesta = alturaCesta * 0.42f
        val cestaX = (w - (larguraCesta + reservaBotao)) / 2f
        val cestaY = h - alturaCesta
        val bandeja = (cestaY - alcaCesta - px(10f)).coerceAtLeast(px(120f))

        // Figuras grandes: a maior célula que cabe, até 140dp.
        val n = figuras.size
        var colunas = 2
        var celula = 0f
        for (c in 2..6) {
            if (c < 3 && n > 6) continue
            val linhas = ceil(n / c.toFloat()).toInt()
            val cel = min(w / c, bandeja / linhas)
            if (cel > celula + 0.5f) { celula = cel; colunas = c }
        }
        celula = min(celula, px(140f))
        val lado = celula * 0.94f
        val linhasBandeja = ceil(n / colunas.toFloat()).toInt()
        val inicioBandejaX = (w - celula * colunas) / 2f
        // O que sobra de altura fica mais em cima: as figuras ficam perto da cesta.
        val inicioBandejaY = max(0f, (bandeja - celula * linhasBandeja) * 0.6f)

        /**
         * Lugar fora da cesta: as figuras que sobram se reagrupam em fileiras arrumadas, encostadas
         * embaixo (perto da cesta), com a última fileira centralizada.
         */
        fun casa(f: FiguraDaCesta): Offset {
            val fora = figuras.filter { it.vaga < 0 || it === f }
            val r = fora.indexOf(f).coerceAtLeast(0)
            val m = fora.size
            val linhasAgora = ceil(m / colunas.toFloat()).toInt()
            val linha = r / colunas
            val naLinha = min(colunas, m - linha * colunas)
            val x0 = inicioBandejaX + (colunas - naLinha) * celula / 2f
            val y0 = inicioBandejaY + (linhasBandeja - linhasAgora) * celula
            return Offset(x0 + (r % colunas) * celula + (celula - lado) / 2f, y0 + linha * celula + (celula - lado) / 2f)
        }

        val porFileira = max(3, min(5, (larguraCesta * 0.86f / (lado * 0.55f)).toInt()))
        fun vaga(v: Int): Offset {
            val fileira = v / porFileira
            val coluna = v % porFileira
            val passo = larguraCesta * 0.78f / porFileira
            val cx = cestaX + larguraCesta / 2f + (coluna - (porFileira - 1) / 2f) * passo + (if (fileira % 2 == 1) passo * 0.3f else 0f)
            val cy = cestaY + alturaCesta * 0.08f - fileira * lado * 0.3f
            return Offset(cx - lado / 2f, cy - lado / 2f)
        }

        fun lugar(f: FiguraDaCesta) = if (f.vaga >= 0) vaga(f.vaga) else casa(f)
        fun posicaoAtual(f: FiguraDaCesta) = lugar(f) + f.peca.desvio.value + f.peca.arrasto

        val areaDaCesta = Rect(cestaX - px(28f), cestaY - alcaCesta - lado * 0.4f, cestaX + larguraCesta + px(28f), h)
        val molinha = spring<Offset>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)

        fun contarEmVozAlta() {
            val q = figuras.count { it.vaga >= 0 }
            narrador.falar(Vocabulario.porExtenso(q, feminino))
        }

        /**
         * Muda a figura de lugar (cesta ou fora). Ela desliza de onde está até o novo lugar, e as
         * outras que estão fora se reagrupam, também deslizando (nada pula na tela).
         */
        fun levarPara(f: FiguraDaCesta, novaVaga: Int) {
            val antes = figuras.associateWith { posicaoAtual(it) }
            f.vaga = novaVaga
            for (g in figuras) {
                if (g !== f && g.peca.arrastando) continue
                val dif = antes.getValue(g) - lugar(g)
                if (g === f || dif.getDistance() > 1f) {
                    escopo.launch(start = CoroutineStart.UNDISPATCHED) { g.peca.pousar(dif, molinha) }
                }
            }
        }

        fun soltar(f: FiguraDaCesta) {
            if (!f.peca.arrastando) return
            val centro = posicaoAtual(f) + Offset(lado / 2f, lado / 2f)
            val dentro = areaDaCesta.contains(centro)
            when {
                terminou -> levarPara(f, f.vaga)
                dentro && f.vaga >= 0 -> levarPara(f, f.vaga)
                dentro && (f.certa || !classificar) -> {
                    val livre = (0 until n).first { v -> figuras.none { it.vaga == v } }
                    levarPara(f, livre)
                    sons.colocar()
                    vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (classificar) {
                        if (figuras.all { !it.certa || it.vaga >= 0 }) {
                            terminou = true
                            escopo.launch {
                                delay(450)
                                concluir(erros == 0)
                            }
                        } else {
                            narrador.falar(nomeDaFigura(f.emoji)?.let { "$it, isso!" } ?: "Isso!")
                        }
                    } else {
                        contarEmVozAlta()
                    }
                }
                dentro -> {
                    // Figura de fora do grupo: volta pulando e o narrador explica.
                    erros++
                    avisarErro()
                    f.recusada = true
                    sons.erro()
                    levarPara(f, -1)
                    escopo.launch { f.peca.tremer() }
                    val nome = nomeDaFigura(f.emoji)
                    narrador.falar(listOf(pt(if (nome != null) "$nome não entra!" else "Esse não entra!")) + falaAtual)
                }
                f.vaga >= 0 -> {
                    levarPara(f, -1)
                    sons.pousar()
                    if (!classificar) contarEmVozAlta()
                }
                else -> levarPara(f, -1)
            }
        }

        fun conferir() {
            if (!ativaAgora || terminou) return
            val q = figuras.count { it.vaga >= 0 }
            if (q == cesta.alvo) {
                terminou = true
                concluir(erros == 0)
            } else {
                erros++
                avisarErro()
                sons.erro()
                escopo.launch { for (x in listOf(8f, -8f, 6f, -6f, 3f, 0f)) tremidaDaCesta.animateTo(x, tween(55)) }
                val pedido = Vocabulario.porExtenso(cesta.alvo, feminino)
                val tem = Vocabulario.porExtenso(q, feminino)
                narrador.falar(
                    if (q < cesta.alvo) "Tem $tem na cesta. Eu pedi $pedido. Coloque mais!"
                    else "Tem $tem na cesta. Passou! Eu pedi $pedido. Tire um pouquinho.",
                )
            }
        }

        // ---------------------------------------------------------------- fundo da cesta (atrás das figuras)
        Canvas(
            Modifier
                .matchParentSize()
                .graphicsLayer { translationX = tremidaDaCesta.value * densidade.density },
        ) {
            val boca = Rect(cestaX, cestaY, cestaX + larguraCesta, cestaY + alturaCesta * 0.32f)
            // Alça
            drawArc(
                CorPalhaEscura,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cestaX + larguraCesta * 0.14f, boca.center.y - alcaCesta - alturaCesta * 0.05f),
                size = Size(larguraCesta * 0.72f, (alcaCesta + alturaCesta * 0.05f) * 2f),
                style = Stroke(px(9f), cap = StrokeCap.Round),
            )
            // Boca da cesta (fundo escuro)
            drawOval(CorFundoDaCesta, boca.topLeft, boca.size)
            drawOval(CorPalhaEscura, boca.topLeft, boca.size, style = Stroke(px(5f)))
        }

        // ---------------------------------------------------------------- figuras
        for (f in figuras) {
            val dentroDaCesta = f.vaga >= 0
            val escala by animateFloatAsState(
                when {
                    f.peca.arrastando -> 1.18f
                    dentroDaCesta -> 0.8f
                    else -> 1f
                },
                spring(dampingRatio = 0.5f),
                label = "escala",
            )
            val giro = remember(f.id) { ((f.id * 37) % 15 - 7).toFloat() }
            val destacar = ajuda && classificar && f.certa && !dentroDaCesta && !terminou
            Box(
                Modifier
                    .zIndex(
                        when {
                            f.peca.arrastando -> 5f
                            dentroDaCesta -> 1f + f.vaga * 0.01f
                            else -> 3f
                        },
                    )
                    .offset {
                        val p = posicaoAtual(f)
                        IntOffset(p.x.roundToInt(), p.y.roundToInt())
                    }
                    .size(with(densidade) { lado.toDp() })
                    .testTag(if (f.certa) "figura_certa" else "figura_errada")
                    .semantics { contentDescription = nomeDaFigura(f.emoji) ?: f.emoji }
                    .pointerInput(f, w, h) {
                        detectDragGestures(
                            onDragStart = {
                                val travada = classificar && f.vaga >= 0
                                if (ativaAgora && !terminou && !travada) {
                                    f.peca.arrastando = true
                                    tocouSemArrastar = false
                                    interacoes++
                                    sons.pegar()
                                    escopo.launch { f.peca.desvio.stop() }
                                }
                            },
                            onDragEnd = { soltar(f) },
                            onDragCancel = { soltar(f) },
                        ) { mudanca, delta ->
                            if (f.peca.arrastando) {
                                mudanca.consume()
                                f.peca.arrasto += delta
                            }
                        }
                    }
                    .pointerInput(f, w, h) {
                        detectTapGestures {
                            if (ativaAgora && !terminou) {
                                // Tocar só diz o nome: para pôr na cesta é preciso arrastar (a mãozinha mostra).
                                sons.toque()
                                tocouSemArrastar = true
                                interacoes++
                                escopo.launch { f.peca.tremer() }
                                nomeDaFigura(f.emoji)?.let { narrador.falar(it) }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        rotationZ = (if (dentroDaCesta) giro * 1.6f else giro * 0.6f) + f.peca.tremida.value
                        alpha = if (f.recusada && !f.peca.arrastando) 0.55f else 1f
                    }
                    .then(if (destacar) Modifier.pulsando() else Modifier)
                    .then(
                        if (destacar) Modifier.background(Color(0xFFFFF3C4), CircleShape).border(3.dp, Cores.Estrela, CircleShape)
                        else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val ladoDp = lado / densidade.density
                Text(f.emoji, fontSize = (ladoDp * 0.76f).sp, lineHeight = (ladoDp * 0.86f).sp)
            }
        }

        // ---------------------------------------------------------------- frente da cesta (por cima das figuras que entraram)
        Canvas(
            Modifier
                .matchParentSize()
                .zIndex(2f)
                .graphicsLayer { translationX = tremidaDaCesta.value * densidade.density },
        ) {
            val boca = Rect(cestaX, cestaY, cestaX + larguraCesta, cestaY + alturaCesta * 0.32f)
            val corpo = Path().apply {
                moveTo(boca.left, boca.center.y)
                arcTo(boca, 180f, -180f, false)
                lineTo(cestaX + larguraCesta * 0.9f, h)
                lineTo(cestaX + larguraCesta * 0.1f, h)
                close()
            }
            drawPath(corpo, CorPalha)
            clipPath(corpo) {
                // Trançado: faixas e varetas.
                val faixas = 4
                for (i in 0..faixas) {
                    val y = boca.center.y + (h - boca.center.y) * i / faixas
                    drawLine(CorPalhaEscura.copy(alpha = 0.55f), Offset(cestaX, y), Offset(cestaX + larguraCesta, y), px(3f))
                }
                val varetas = 9
                for (i in 0..varetas) {
                    val topo = cestaX + larguraCesta * i / varetas
                    val baixo = cestaX + larguraCesta * 0.1f + larguraCesta * 0.8f * i / varetas
                    drawLine(CorPalhaClara.copy(alpha = 0.7f), Offset(topo, boca.center.y), Offset(baixo, h), px(4f))
                }
            }
            // Borda da frente da boca.
            drawArc(
                CorPalhaEscura,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = boca.topLeft,
                size = boca.size,
                style = Stroke(px(8f), cap = StrokeCap.Round),
            )
            // Ajuda na contagem: bolinhas na cesta mostram quantas faltam (embaixo do número).
            if (!classificar && ajuda) {
                val raio = min(px(8f), larguraCesta * 0.7f / (cesta.alvo * 2.6f))
                val passo = raio * 2.6f
                val y = cestaY + alturaCesta * 0.88f
                val x0 = cestaX + larguraCesta / 2f - passo * (cesta.alvo - 1) / 2f
                for (i in 0 until cesta.alvo) {
                    val c = Offset(x0 + passo * i, y)
                    if (i < naCesta) drawCircle(cor, raio, c) else drawCircle(Color.White.copy(alpha = 0.85f), raio, c)
                    drawCircle(CorPalhaEscura, raio, c, style = Stroke(px(2f)))
                }
            }
        }

        // Alvo invisível do tamanho da cesta (os testes automáticos soltam as figuras no meio dela).
        Box(
            Modifier
                .offset { IntOffset(cestaX.roundToInt(), (cestaY - alcaCesta * 0.5f).roundToInt()) }
                .size(with(densidade) { larguraCesta.toDp() }, with(densidade) { (alturaCesta + alcaCesta * 0.5f).toDp() })
                .testTag("cesta"),
        )

        // ---------------------------------------------------------------- número dentro da cesta e certinho (modo contar)
        if (!classificar) {
            val passou = naCesta > cesta.alvo && ajuda
            val selo = min(alturaCesta * 0.5f, px(64f))
            val pulinho = remember { Animatable(1f) }
            LaunchedEffect(naCesta) {
                if (naCesta > 0) {
                    pulinho.snapTo(1.25f)
                    pulinho.animateTo(1f, spring(dampingRatio = 0.4f))
                }
            }
            Box(
                Modifier
                    .zIndex(4f)
                    .offset {
                        IntOffset(
                            (cestaX + larguraCesta / 2f - selo / 2f + tremidaDaCesta.value * densidade.density).roundToInt(),
                            (cestaY + alturaCesta * 0.6f - selo / 2f).roundToInt(),
                        )
                    }
                    .size(with(densidade) { selo.toDp() })
                    .graphicsLayer {
                        scaleX = pulinho.value
                        scaleY = pulinho.value
                    }
                    .background(if (passou) Cores.Errado else Color.White, CircleShape)
                    .border(4.dp, CorPalhaEscura, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$naCesta",
                    fontSize = (selo / densidade.density * 0.52f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (passou) Color.White else Cores.Texto,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            // O certinho só aparece quando já há fruta na cesta (apagado, parecia um botão para tocar).
            val aparecer by animateFloatAsState(if (naCesta > 0 || terminou) 1f else 0f, spring(dampingRatio = 0.5f), label = "certinho")
            if (naCesta > 0 || terminou || aparecer > 0.02f) {
                val pronto = naCesta > 0 && ativa && !terminou
                BotaoFofo(
                    onClick = { conferir() },
                    cor = Cores.Certo,
                    corBase = Color(0xFF2A9452),
                    forma = CircleShape,
                    descricao = "Pronto",
                    habilitado = pronto,
                    preencher = true,
                    modifier = Modifier
                        .zIndex(4f)
                        .offset {
                            IntOffset(
                                (cestaX + larguraCesta + px(14f)).roundToInt(),
                                (cestaY + alturaCesta * 0.55f - botao / 2f).roundToInt(),
                            )
                        }
                        .size(with(densidade) { botao.toDp() })
                        .graphicsLayer {
                            scaleX = aparecer
                            scaleY = aparecer
                            alpha = aparecer.coerceIn(0f, 1f)
                        }
                        .testTag("atividade_pronto")
                        .then(if (pronto && (naCesta == cesta.alvo && ajuda)) Modifier.pulsando() else Modifier),
                ) {
                    Text("✔", fontSize = 38.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ---------------------------------------------------------------- mãozinha: mostra o gesto de arrastar
        // Pega uma figura (uma do grupo, ao classificar) e leva até a boca da cesta, com rastro de bolinhas.
        val alvoDaMao = Offset(cestaX + larguraCesta / 2f, cestaY + alturaCesta * 0.12f)
        val exemploDaMao = figuras.lastOrNull { it.vaga < 0 && (it.certa || !classificar) }
        val ninguemArrastando = figuras.none { it.peca.arrastando }
        Box(Modifier.matchParentSize().zIndex(6f)) {
            if (exemploDaMao != null) {
                key(exemploDaMao.id) {
                    GestoDeArrastar(
                        origem = casa(exemploDaMao) + Offset(lado / 2f, lado / 2f),
                        destino = alvoDaMao,
                        ativa = ativa && !terminou && parada && ninguemArrastando,
                        cor = escurecer(cor),
                        tamanhoCarga = with(densidade) { lado.toDp() },
                    ) {
                        val ladoDp = lado / densidade.density
                        Text(exemploDaMao.emoji, fontSize = (ladoDp * 0.76f).sp, lineHeight = (ladoDp * 0.86f).sp)
                    }
                }
            }
        }
    }
}
