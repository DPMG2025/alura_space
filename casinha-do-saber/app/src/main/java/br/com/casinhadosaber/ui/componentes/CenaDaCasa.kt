package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.Desenho
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Posicao
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Altura da cena dividida pela largura. */
const val PROPORCAO_CENA = 1.15f

/** Medidas da casa em frações da cena (as mesmas do protótipo). */
private object Planta {
    const val ESQ = 0.10f
    const val DIR = 0.90f
    const val TOPO = 0.30f
    const val MEIO = 0.535f
    const val BASE = 0.78f
    const val DIVISAO_CIMA = 0.52f
    const val DIVISAO_BAIXO = 0.58f
}

/** Qual cômodo fica no ponto (x, y) da cena, para o modo de pintar. */
fun comodoEm(x: Float, y: Float): Comodo? {
    if (x !in Planta.ESQ..Planta.DIR) return null
    return when (y) {
        in Planta.TOPO..Planta.MEIO -> if (x < Planta.DIVISAO_CIMA) Comodo.QUARTO else Comodo.BANHEIRO
        in Planta.MEIO..Planta.BASE -> if (x < Planta.DIVISAO_BAIXO) Comodo.SALA else Comodo.COZINHA
        else -> null
    }
}

/**
 * A casinha completa: céu, terreno, estruturas, objetos e a criança.
 *
 * Quando [interativa] é verdadeiro, a criança pode tocar e arrastar os objetos.
 * Arrastar um objeto para baixo da cena devolve o objeto ao baú.
 */
@Composable
fun CenaDaCasa(
    estado: EstadoJogo,
    modifier: Modifier = Modifier,
    interativa: Boolean = false,
    pintando: Boolean = false,
    novaEstrutura: Estrutura? = null,
    destaqueId: String? = null,
    puloDaCrianca: Int = 0,
    onTocarItem: (ItemCasa) -> Unit = {},
    onMoverItem: (ItemCasa, Posicao) -> Unit = { _, _ -> },
    onGuardarItem: (ItemCasa) -> Unit = {},
    onPintar: (Comodo) -> Unit = {},
    onTocarCrianca: () -> Unit = {},
    onMoverCrianca: (Posicao) -> Unit = {},
) {
    val tempo = rememberInfiniteTransition(label = "cena")
    val t by tempo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "tempo",
    )
    val construcao = remember { Animatable(1f) }
    LaunchedEffect(novaEstrutura) {
        if (novaEstrutura != null) {
            construcao.snapTo(0f)
            construcao.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
        }
    }

    BoxWithConstraints(
        modifier
            .aspectRatio(1f / PROPORCAO_CENA)
            .clipToBounds(),
    ) {
        val larguraPx = constraints.maxWidth.toFloat()
        val alturaPx = larguraPx * PROPORCAO_CENA

        Canvas(
            Modifier
                .fillMaxSize()
                .then(
                    if (interativa && pintando) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { o ->
                                comodoEm(o.x / size.width, o.y / size.height)?.let(onPintar)
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            desenharCasa(estado, t, novaEstrutura, construcao.value)
        }

        if (!estado.temEstrutura(Estrutura.TERRENO)) {
            val densidade = LocalDensity.current
            Text(
                text = "Minha casa\nvai ficar aqui!",
                modifier = Modifier
                    .offset { IntOffset((larguraPx * 0.36f).roundToInt(), (alturaPx * 0.625f).roundToInt()) }
                    .size(with(densidade) { (larguraPx * 0.28f).toDp() }, with(densidade) { (alturaPx * 0.085f).toDp() }),
                textAlign = TextAlign.Center,
                color = Color(0xFF6B3E1F),
                style = TextStyle(
                    fontSize = with(densidade) { (larguraPx * 0.034f).toSp() },
                    lineHeight = with(densidade) { (larguraPx * 0.04f).toSp() },
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
        }

        for (item in ItensDaCasa.ordem) {
            if (item.ehEstrutura) continue
            val pos = estado.colocados[item.id] ?: continue
            ObjetoNaCena(
                item = item,
                pos = pos,
                larguraCena = larguraPx,
                alturaCena = alturaPx,
                interativo = interativa && !pintando,
                aparecer = item.id == destaqueId,
                onTocar = { onTocarItem(item) },
                onSoltar = { nova ->
                    if (nova.y > 1.04f) onGuardarItem(item) else onMoverItem(item, limitar(nova))
                },
            ) { w ->
                ConteudoDoItem(item, w)
            }
        }

        val perfil = estado.perfil
        if (perfil != null) {
            ObjetoNaCena(
                item = null,
                pos = estado.posicaoCrianca,
                larguraCena = larguraPx,
                alturaCena = alturaPx,
                larguraRelativa = 0.13f,
                proporcao = 1f / PROPORCAO_CRIANCA,
                interativo = interativa && !pintando,
                aparecer = false,
                pulo = puloDaCrianca,
                descricao = perfil.nome.ifBlank { "Você" },
                onTocar = onTocarCrianca,
                onSoltar = { onMoverCrianca(limitar(it)) },
            ) { _ ->
                Crianca(perfil, Modifier.fillMaxSize(), acenando = true)
            }
        }
    }
}

private fun limitar(p: Posicao) = Posicao(p.x.coerceIn(0.03f, 0.97f), p.y.coerceIn(0.08f, 1f))

/** Um objeto posicionado pelo pé, que pode ser tocado e arrastado. */
@Composable
private fun ObjetoNaCena(
    item: ItemCasa?,
    pos: Posicao,
    larguraCena: Float,
    alturaCena: Float,
    interativo: Boolean,
    aparecer: Boolean,
    onTocar: () -> Unit,
    onSoltar: (Posicao) -> Unit,
    larguraRelativa: Float = item?.tamanho ?: 0.1f,
    proporcao: Float = proporcaoDoItem(item),
    pulo: Int = 0,
    descricao: String = item?.nome ?: "",
    conteudo: @Composable (larguraPx: Float) -> Unit,
) {
    val densidade = LocalDensity.current
    val w = larguraRelativa * larguraCena
    val h = w * proporcao
    var arrasto by remember(pos) { mutableStateOf(Offset.Zero) }
    var arrastando by remember { mutableStateOf(false) }
    val escala = remember { Animatable(if (aparecer) 0f else 1f) }
    val salto = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()

    LaunchedEffect(aparecer) {
        if (aparecer) {
            escala.snapTo(0f)
            escala.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow))
        }
    }
    LaunchedEffect(pulo) {
        if (pulo > 0) {
            salto.animateTo(-h * 0.25f, tween(160))
            salto.animateTo(0f, spring(dampingRatio = 0.4f))
        }
    }

    val x = pos.x * larguraCena - w / 2 + arrasto.x
    val y = pos.y * alturaCena - h + arrasto.y
    Box(
        Modifier
            .offset { IntOffset(x.roundToInt(), (y + salto.value).roundToInt()) }
            .size(with(densidade) { w.toDp() }, with(densidade) { h.toDp() })
            .zIndex(if (arrastando) 10f else 0f)
            .graphicsLayer {
                val s = escala.value * if (arrastando) 1.15f else 1f
                scaleX = s
                scaleY = s
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
            }
            .semantics {
                contentDescription = descricao
                role = Role.Button
            }
            .then(
                if (interativo) {
                    Modifier
                        .pointerInput(pos) {
                            detectTapGestures {
                                onTocar()
                                escopo.launch {
                                    escala.animateTo(1.25f, tween(90))
                                    escala.animateTo(1f, spring(dampingRatio = 0.3f))
                                }
                            }
                        }
                        .pointerInput(pos) {
                            detectDragGestures(
                                onDragStart = { arrastando = true },
                                onDragEnd = {
                                    arrastando = false
                                    onSoltar(
                                        Posicao(
                                            pos.x + arrasto.x / larguraCena,
                                            pos.y + arrasto.y / alturaCena,
                                        ),
                                    )
                                    arrasto = Offset.Zero
                                },
                                onDragCancel = {
                                    arrastando = false
                                    arrasto = Offset.Zero
                                },
                            ) { mudanca, delta ->
                                mudanca.consume()
                                arrasto += delta
                            }
                        }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        conteudo(w)
    }
}

fun proporcaoDoItem(item: ItemCasa?): Float = when (item?.desenho) {
    Desenho.GELADEIRA -> 2.1f
    Desenho.FOGAO -> 1.35f
    Desenho.MESA -> 0.7f
    null -> 1f
}

/** Desenha o item: emoji ou móvel desenhado. Também é usado no baú. */
@Composable
fun ConteudoDoItem(item: ItemCasa, larguraPx: Float, modifier: Modifier = Modifier) {
    val desenho = item.desenho
    if (desenho != null) {
        Canvas(modifier.fillMaxSize()) { desenharMovel(desenho) }
    } else {
        EmojiGrande(item.icone, larguraPx, modifier)
    }
}

/** Ícone de um item dentro de um quadrado de lado [lado] (baú, prêmio), sem distorcer os móveis desenhados. */
@Composable
fun IconeDoItem(item: ItemCasa, lado: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val proporcao = proporcaoDoItem(item)
    val largura = if (proporcao > 1f) lado / proporcao else lado
    val larguraPx = with(LocalDensity.current) { largura.toPx() }
    Box(modifier.size(lado), contentAlignment = Alignment.Center) {
        Box(Modifier.size(largura, largura * proporcao), contentAlignment = Alignment.Center) {
            ConteudoDoItem(item, larguraPx)
        }
    }
}

@Composable
fun EmojiGrande(emoji: String, larguraPx: Float, modifier: Modifier = Modifier) {
    val tamanho = with(LocalDensity.current) { (larguraPx * 0.84f).toSp() }
    Text(
        text = emoji,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = tamanho,
            lineHeight = tamanho * 1.05f,
            fontWeight = FontWeight.Normal,
            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
        ),
        maxLines = 1,
        softWrap = false,
    )
}

// ---------------------------------------------------------------------------------------- desenho

private val CorTelhado = Color(0xFFEF476F)
private val CorTelhadoEscuro = Color(0xFFC7365A)
private val CorMadeira = Color(0xFFC98B5A)
private val CorMoldura = Color(0xFFE8C9A0)
private val CorVidro = Color(0xFFA9E1FF)

private fun DrawScope.desenharCasa(e: EstadoJogo, t: Float, nova: Estrutura?, progresso: Float) {
    val W = size.width
    val H = size.height
    fun X(v: Float) = v * W
    fun Y(v: Float) = v * H

    /** Estruturas novas "caem" do céu com um quique. */
    fun camada(estrutura: Estrutura, bloco: DrawScope.() -> Unit) {
        if (!e.temEstrutura(estrutura)) return
        if (estrutura == nova && progresso < 1f) {
            withTransform({ translate(0f, -(1f - progresso) * H * 0.25f) }) { bloco() }
        } else {
            bloco()
        }
    }

    // céu
    drawRect(Brush.verticalGradient(listOf(Color(0xFF7EC8FF), Color(0xFFCDEBFF)), endY = Y(0.8f)))
    // morros
    drawOval(Color(0xFFA8DD8F), topLeft = Offset(X(-0.2f), Y(0.68f)), size = Size(X(0.7f), Y(0.24f)))
    drawOval(Color(0xFF98D47E), topLeft = Offset(X(0.45f), Y(0.70f)), size = Size(X(0.8f), Y(0.2f)))

    // chão
    val temTerreno = e.temEstrutura(Estrutura.TERRENO)
    val chao = if (temTerreno) listOf(Color(0xFF7CC56B), Color(0xFF5FAE52)) else listOf(Color(0xFFC9A27A), Color(0xFFA97F57))
    drawRect(Brush.verticalGradient(chao, startY = Y(0.78f), endY = H), topLeft = Offset(0f, Y(0.78f)), size = Size(W, H - Y(0.78f)))
    if (temTerreno) {
        for (i in 0 until 14) {
            val x = (i * 0.073f + 0.03f) % 1f
            val y = 0.82f + ((i * 37) % 17) / 100f
            drawOval(Color(0xFF6BB85C), topLeft = Offset(X(x - 0.018f), Y(y - 0.006f)), size = Size(X(0.036f), Y(0.012f)))
        }
    } else {
        placaDoTerreno(W, H)
    }

    camada(Estrutura.CERCA) {
        val tinta = Color.White
        val borda = Color(0xFFD9CBB8)
        for ((a, b) in listOf(0f to 0.10f, 0.90f to 1f)) {
            drawRect(tinta, Offset(X(a), Y(0.755f)), Size(X(b - a), Y(0.012f)))
            drawRect(tinta, Offset(X(a), Y(0.785f)), Size(X(b - a), Y(0.012f)))
            var x = a + 0.005f
            while (x < b) {
                val tabua = Path().apply {
                    moveTo(X(x), Y(0.80f)); lineTo(X(x), Y(0.745f)); lineTo(X(x + 0.01f), Y(0.735f))
                    lineTo(X(x + 0.02f), Y(0.745f)); lineTo(X(x + 0.02f), Y(0.80f)); close()
                }
                drawPath(tabua, tinta)
                drawPath(tabua, borda, style = Stroke(1.5f))
                x += 0.03f
            }
        }
    }

    camada(Estrutura.LAGO) {
        drawOval(Color(0xFF4FB3E8), Offset(X(0.68f), Y(0.87f)), Size(X(0.29f), Y(0.11f)))
        val brilho = 0.02f * kotlin.math.sin(t * 2 * Math.PI).toFloat()
        drawOval(Color(0xFF8FD6FA), Offset(X(0.73f + brilho), Y(0.895f)), Size(X(0.12f), Y(0.03f)))
    }

    camada(Estrutura.CAMINHO) {
        for ((x, y, s) in listOf(Triple(0.31f, 0.80f, 0.03f), Triple(0.29f, 0.85f, 0.035f), Triple(0.32f, 0.905f, 0.04f), Triple(0.29f, 0.965f, 0.045f))) {
            drawOval(Color(0xFFD8D2C6), Offset(X(x - s), Y(y) - X(s) * 0.45f), Size(X(2 * s), X(s) * 0.9f))
        }
    }

    // chaminé fica atrás do telhado
    if (e.temEstrutura(Estrutura.TELHADO)) {
        camada(Estrutura.CHAMINE) {
            drawRect(Color(0xFFB5523B), Offset(X(0.68f), Y(0.12f)), Size(X(0.07f), Y(0.14f)))
            drawRect(Color(0xFF8E3B28), Offset(X(0.67f), Y(0.11f)), Size(X(0.09f), Y(0.025f)))
            for (i in 0 until 3) {
                val fase = (t + i / 3f) % 1f
                drawCircle(
                    Color.White.copy(alpha = 0.85f * (1f - fase)),
                    radius = X(0.018f + 0.025f * fase),
                    center = Offset(X(0.715f + 0.08f * fase), Y(0.10f - 0.09f * fase)),
                )
            }
        }
    }

    camada(Estrutura.PAREDES) {
        val L = Planta.ESQ; val R = Planta.DIR; val T = Planta.TOPO; val M = Planta.MEIO; val B = Planta.BASE
        drawRect(Color(e.corDo(Comodo.QUARTO)), Offset(X(L), Y(T)), Size(X(Planta.DIVISAO_CIMA - L), Y(M - T)))
        drawRect(Color(e.corDo(Comodo.BANHEIRO)), Offset(X(Planta.DIVISAO_CIMA), Y(T)), Size(X(R - Planta.DIVISAO_CIMA), Y(M - T)))
        drawRect(Color(e.corDo(Comodo.SALA)), Offset(X(L), Y(M)), Size(X(Planta.DIVISAO_BAIXO - L), Y(B - M)))
        drawRect(Color(e.corDo(Comodo.COZINHA)), Offset(X(Planta.DIVISAO_BAIXO), Y(M)), Size(X(R - Planta.DIVISAO_BAIXO), Y(B - M)))

        // azulejos do banheiro e da cozinha
        val linha = Color(0x0F000000)
        for ((a, b, faixa) in listOf(Triple(Planta.DIVISAO_CIMA, R, 0.44f to M), Triple(Planta.DIVISAO_BAIXO, R, 0.69f to B))) {
            var x = a
            while (x < b) { drawLine(linha, Offset(X(x), Y(faixa.first)), Offset(X(x), Y(faixa.second)), 1.5f); x += 0.035f }
            var y = faixa.first
            while (y < faixa.second) { drawLine(linha, Offset(X(a), Y(y)), Offset(X(b), Y(y)), 1.5f); y += 0.025f }
        }
        // pisos de madeira e parede do meio
        drawRect(CorMadeira, Offset(X(L), Y(M - 0.012f)), Size(X(R - L), Y(0.024f)))
        drawRect(CorMadeira, Offset(X(L), Y(B - 0.008f)), Size(X(R - L), Y(0.012f)))
        drawRect(Color(0xFFE9D8C4), Offset(X(Planta.DIVISAO_CIMA - 0.005f), Y(T)), Size(X(0.01f), Y(M - T - 0.012f)))
        drawRect(CorMoldura, Offset(X(L), Y(T)), Size(X(R - L), Y(B - T)), style = Stroke(X(0.018f)))
    }

    if (e.temEstrutura(Estrutura.PAREDES)) {
        camada(Estrutura.JANELAS) {
            janela(X(0.39f), Y(0.35f), X(0.08f), Y(0.08f))
            janela(X(0.45f), Y(0.585f), X(0.08f), Y(0.075f))
            janela(X(0.66f), Y(0.585f), X(0.08f), Y(0.075f))
            drawCircle(CorVidro, X(0.035f), Offset(X(0.82f), Y(0.39f)))
            drawCircle(Color.White, X(0.035f), Offset(X(0.82f), Y(0.39f)), style = Stroke(X(0.012f)))
        }
        camada(Estrutura.PORTA) {
            val r = X(0.04f)
            val porta = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(Offset(X(0.265f), Y(0.615f)), Size(X(0.085f), Y(0.16f))),
                        topLeft = CornerRadius(r), topRight = CornerRadius(r),
                        bottomLeft = CornerRadius.Zero, bottomRight = CornerRadius.Zero,
                    ),
                )
            }
            drawPath(porta, Color(0xFFA0522D))
            drawPath(porta, Color(0xFF7A3E22), style = Stroke(3f))
            drawCircle(Color(0xFFFFD166), X(0.009f), Offset(X(0.335f), Y(0.70f)))
            drawCircle(Color(0xFFFFE9A8), X(0.017f), Offset(X(0.3075f), Y(0.645f)))
        }
        camada(Estrutura.ESCADA) {
            val degraus = 7
            val alturaDegrau = 0.235f / degraus
            for (i in 0 until degraus) {
                val y = Planta.BASE - 0.008f - (i + 1) * alturaDegrau
                val largura = 0.14f - i * 0.018f
                drawRect(CorMadeira, Offset(X(0.105f), Y(y)), Size(X(largura), Y(alturaDegrau) + 1f))
                drawRect(Color(0xFFDDA577), Offset(X(0.105f), Y(y)), Size(X(largura), Y(0.006f)))
            }
            drawRect(Color(0xFFE9D8C4), Offset(X(0.105f), Y(Planta.MEIO - 0.012f)), Size(X(0.06f), Y(0.024f)))
        }
    }

    camada(Estrutura.TELHADO) {
        val telhado = Path().apply {
            moveTo(X(0.04f), Y(0.315f)); lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f)); close()
        }
        drawPath(telhado, CorTelhado)
        drawPath(telhado, CorTelhadoEscuro, style = Stroke(X(0.012f), join = StrokeJoin.Round))
        for (k in 1 until 5) {
            val y = 0.075f + k * 0.048f
            val metade = (y - 0.075f) / 0.24f * 0.46f
            drawLine(Color(0x14000000), Offset(X(0.5f - metade), Y(y)), Offset(X(0.5f + metade), Y(y)), 3f)
        }
        drawCircle(Color.White, X(0.04f), Offset(X(0.5f), Y(0.215f)))
        drawCircle(CorVidro, X(0.03f), Offset(X(0.5f), Y(0.215f)))
    }
}

private fun DrawScope.janela(x: Float, y: Float, w: Float, h: Float) {
    val r = CornerRadius(w * 0.08f)
    drawRect(Color(0xFFFF8FAB), Offset(x - w * 0.15f, y - h * 0.15f), Size(w * 0.15f, h * 1.3f))
    drawRect(Color(0xFFFF8FAB), Offset(x + w, y - h * 0.15f), Size(w * 0.15f, h * 1.3f))
    drawRoundRect(CorVidro, Offset(x, y), Size(w, h), r)
    drawRoundRect(Color.White, Offset(x, y), Size(w, h), r, style = Stroke(w * 0.15f))
    drawLine(Color.White, Offset(x + w / 2, y), Offset(x + w / 2, y + h), w * 0.075f)
    drawLine(Color.White, Offset(x, y + h / 2), Offset(x + w, y + h / 2), w * 0.075f)
}

private fun DrawScope.placaDoTerreno(W: Float, H: Float) {
    drawRect(Color(0xFF8D5A3B), Offset(W * 0.495f, H * 0.70f), Size(W * 0.012f, H * 0.12f))
    drawRoundRect(Color(0xFFF4D6A0), Offset(W * 0.36f, H * 0.62f), Size(W * 0.28f, H * 0.09f), CornerRadius(W * 0.02f))
    drawRoundRect(Color(0xFF8D5A3B), Offset(W * 0.36f, H * 0.62f), Size(W * 0.28f, H * 0.09f), CornerRadius(W * 0.02f), style = Stroke(W * 0.006f))
    // "tijolinhos" esperando a construção
    for (i in 0 until 3) {
        drawRoundRect(Color(0xFFD86A4A), Offset(W * (0.70f + i * 0.055f), H * 0.90f), Size(W * 0.05f, H * 0.025f), CornerRadius(4f))
    }
    drawRoundRect(Color(0xFFD86A4A), Offset(W * 0.7275f, H * 0.874f), Size(W * 0.05f, H * 0.025f), CornerRadius(4f))
}

/** Móveis sem emoji, desenhados para ocupar toda a área disponível. */
fun DrawScope.desenharMovel(d: Desenho) {
    val w = size.width
    val h = size.height
    val contorno = Color(0xFFB8C4D0)
    when (d) {
        Desenho.GELADEIRA -> {
            drawRoundRect(Color(0xFFF4F7FB), Offset.Zero, size, CornerRadius(w * 0.15f))
            drawRoundRect(contorno, Offset.Zero, size, CornerRadius(w * 0.15f), style = Stroke(2f))
            drawLine(contorno, Offset(0f, h * 0.38f), Offset(w, h * 0.38f), 2f)
            drawRect(Color(0xFF9AA8B6), Offset(w * 0.72f, h * 0.1f), Size(w * 0.08f, h * 0.18f))
            drawRect(Color(0xFF9AA8B6), Offset(w * 0.72f, h * 0.48f), Size(w * 0.08f, h * 0.22f))
            drawCircle(Color(0xFFFF6B6B), w * 0.08f, Offset(w * 0.35f, h * 0.2f))
            drawCircle(Color(0xFF4D96FF), w * 0.07f, Offset(w * 0.3f, h * 0.55f))
        }
        Desenho.FOGAO -> {
            val topo = h * 0.08f
            for (dx in listOf(0.28f, 0.72f)) drawOval(Color(0xFF3A3F47), Offset(w * dx - w * 0.16f, 0f), Size(w * 0.32f, topo * 1.4f))
            drawRoundRect(Color.White, Offset(0f, topo), Size(w, h - topo), CornerRadius(w * 0.1f))
            drawRoundRect(contorno, Offset(0f, topo), Size(w, h - topo), CornerRadius(w * 0.1f), style = Stroke(2f))
            for (dx in listOf(0.25f, 0.5f, 0.75f)) drawCircle(Color(0xFFFF6B6B), w * 0.05f, Offset(w * dx, topo + h * 0.12f))
            drawRoundRect(Color(0xFF3A3F47), Offset(w * 0.12f, topo + h * 0.25f), Size(w * 0.76f, h * 0.5f), CornerRadius(w * 0.06f))
            drawRect(Color(0xFFFFB347), Offset(w * 0.2f, topo + h * 0.3f), Size(w * 0.6f, h * 0.04f))
        }
        Desenho.MESA -> {
            val tampo = h * 0.14f
            drawRoundRect(Color(0xFFB0703C), Offset(0f, tampo * 0.6f), Size(w, tampo), CornerRadius(w * 0.04f))
            drawRoundRect(Color(0xF2FFFFFF), Offset(w * 0.05f, 0f), Size(w * 0.9f, tampo * 0.9f), CornerRadius(w * 0.03f))
            drawRect(Color(0xFFB0703C), Offset(w * 0.08f, tampo), Size(w * 0.08f, h - tampo))
            drawRect(Color(0xFFB0703C), Offset(w * 0.84f, tampo), Size(w * 0.08f, h - tampo))
        }
    }
}
