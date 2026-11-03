package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.Desenho
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Lugar
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
    noite: Boolean = false,
    /** Uso interno: quem chama já desenhou o céu (modo de preencher a altura). */
    semCeu: Boolean = false,
    /** Ocupa toda a altura dada: a casa fica embaixo e o céu continua para cima (sem faixas vazias). */
    preencherAltura: Boolean = false,
    onTocarItem: (ItemCasa) -> Unit = {},
    onMoverItem: (ItemCasa, Posicao) -> Unit = { _, _ -> },
    onGuardarItem: (ItemCasa) -> Unit = {},
    /** O objeto foi solto num lugar onde ele não mora (cama no jardim, árvore na sala) e voltou. */
    onRecusarItem: (ItemCasa) -> Unit = {},
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

    if (preencherAltura) {
        BoxWithConstraints(modifier.clipToBounds(), contentAlignment = Alignment.TopCenter) {
            val alturaCena = minOf(maxWidth * PROPORCAO_CENA, maxHeight)
            // A sobra de altura vai mais para o jardim (65%) do que para o céu (35%).
            val sobra = maxHeight - alturaCena
            val ceuExtra = sobra * 0.35f
            val corteDoChao = alturaCena * 0.22f + sobra * 0.65f
            Canvas(Modifier.fillMaxSize()) {
                val ceuInteiro = if (noite) listOf(Color(0xFF16204A), Color(0xFF3B4C8C)) else listOf(Color(0xFF7EC8FF), Color(0xFFCDEBFF))
                drawRect(Brush.verticalGradient(ceuInteiro, endY = size.height - corteDoChao.toPx()))
                // Grama: dos lados da casa e o jardim que continua para baixo, com tufinhos e flores.
                val chao = if (noite) listOf(Color(0xFF3F7A45), Color(0xFF2F5E35)) else listOf(Color(0xFF7CC56B), Color(0xFF4F9B44))
                val topoChao = size.height - corteDoChao.toPx()
                drawRect(Brush.verticalGradient(chao, startY = topoChao, endY = size.height), Offset(0f, topoChao), Size(size.width, corteDoChao.toPx()))
                val baseCena = ceuExtra.toPx() + alturaCena.toPx()
                val altoJardim = size.height - baseCena
                if (altoJardim > 40f) jardimDeBaixo(baseCena, altoJardim, noite, estado.temEstrutura(Estrutura.CAMINHO), estado.temEstrutura(Estrutura.TERRENO))
                for (i in 0 until 24) {
                    val x = ((i * 0.137f + 0.03f) % 1f) * size.width
                    val y = baseCena + ((i * 0.29f) % 1f) * (size.height - baseCena)
                    if (y < baseCena + 4f) continue
                    if (i % 4 == 0) {
                        drawCircle(Color(0xFFFFE066), 5f, Offset(x, y))
                        drawCircle(Color.White, 3f, Offset(x - 6f, y))
                        drawCircle(Color.White, 3f, Offset(x + 6f, y))
                    } else {
                        drawOval(if (noite) Color(0xFF356B3B) else Color(0xFF6BB85C), Offset(x - 14f, y - 4f), Size(28f, 8f))
                    }
                }
            }
            CenaDaCasa(
                estado, Modifier.padding(top = ceuExtra).height(alturaCena), interativa, pintando, novaEstrutura, destaqueId, puloDaCrianca, noite, true, false,
                onTocarItem, onMoverItem, onGuardarItem, onRecusarItem, onPintar, onTocarCrianca, onMoverCrianca,
            )
        }
        return
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
            desenharCasa(estado, t, novaEstrutura, construcao.value, noite, semCeu)
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
            // À noite não tem arco-íris, pipa nem borboleta.
            if (noite && item.id in setOf("arcoiris", "pipa", "borboleta")) continue
            val pos = estado.colocados[item.id] ?: continue
            key(item.id) { ObjetoNaCena(
                item = item,
                pos = pos,
                larguraCena = larguraPx,
                alturaCena = alturaPx,
                interativo = interativa && !pintando,
                aparecer = item.id == destaqueId,
                vida = vidaDo(item.id),
                tempo = t,
                noite = noite,
                onTocar = { onTocarItem(item) },
                onSoltar = { nova ->
                    if (nova.y > 1.04f) {
                        onGuardarItem(item)
                        null
                    } else {
                        val destino = encaixar(item.id, limitar(nova), estado)
                        if (destino == null) {
                            onRecusarItem(item)
                            pos
                        } else {
                            destino.also { onMoverItem(item, it) }
                        }
                    }
                },
            ) { w ->
                if (noite && item.id == "sol") EmojiGrande("🌙", w) else ConteudoDoItem(item, w)
            } }
        }

        // Visitantes comprados na loja passeiam pelo jardim, cada um no seu ritmo.
        estado.visitantes.forEachIndexed { i, bicho ->
            val volta = ((t + i * 0.37f) % 1f)
            val ida = volta < 0.5f
            val fracao = if (ida) volta * 2f else (1f - volta) * 2f
            val x = 0.08f + 0.84f * fracao
            val y = 0.86f + (i % 3) * 0.045f
            val tamanho = larguraPx * 0.065f
            val densidade = LocalDensity.current
            Box(
                Modifier
                    .offset { IntOffset((x * larguraPx - tamanho / 2).roundToInt(), (y * alturaPx - tamanho).roundToInt()) }
                    .size(with(densidade) { tamanho.toDp() })
                    .graphicsLayer { scaleX = if (ida) -1f else 1f },
            ) {
                EmojiGrande(bicho, tamanho)
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
                noite = noite,
                tempo = t,
                descricao = perfil.nome.ifBlank { "Você" },
                onTocar = onTocarCrianca,
                onSoltar = { nova -> (encaixar("crianca", limitar(nova), estado) ?: estado.posicaoCrianca).also(onMoverCrianca) },
            ) { _ ->
                Crianca(perfil, Modifier.fillMaxSize(), acenando = true)
            }
        }
    }
}

/** Pequenas animações que deixam a casa viva. */
enum class Vida { PARADO, BALANCAR, RESPIRAR, BOIAR, VOAR, DERIVAR, BRILHAR, DANCAR }

fun vidaDo(id: String): Vida = when (id) {
    "arvore", "flores", "planta", "girassol" -> Vida.BALANCAR
    "cachorro", "gato", "ursinho" -> Vida.RESPIRAR
    "pato" -> Vida.BOIAR
    "borboleta", "passarinho" -> Vida.VOAR
    "nuvem", "arcoiris" -> Vida.DERIVAR
    "estrela", "abajur", "sol" -> Vida.BRILHAR
    "pipa", "balao" -> Vida.DANCAR
    else -> Vida.PARADO
}

private fun androidx.compose.ui.graphics.GraphicsLayerScope.aplicarVida(
    vida: Vida,
    fase: Float,
    larguraCena: Float,
    alturaCena: Float,
    densidade: Float,
) {
    val a = fase * 2f * Math.PI.toFloat()
    val seno = kotlin.math.sin(a)
    when (vida) {
        Vida.PARADO -> Unit
        Vida.BALANCAR -> rotationZ = 3f * seno
        Vida.RESPIRAR -> scaleY *= 1f + 0.05f * kotlin.math.sin(a * 2f)
        Vida.BOIAR -> {
            translationY = 3f * densidade * seno
            rotationZ = 5f * kotlin.math.cos(a)
        }
        Vida.VOAR -> {
            translationX = 0.04f * larguraCena * seno
            translationY = 0.025f * alturaCena * kotlin.math.sin(a * 2f)
            scaleX *= 0.75f + 0.25f * kotlin.math.abs(kotlin.math.cos(a * 6f))
        }
        Vida.DERIVAR -> translationX = 0.03f * larguraCena * seno
        Vida.BRILHAR -> {
            scaleX *= 1f + 0.06f * seno
            scaleY *= 1f + 0.06f * seno
        }
        Vida.DANCAR -> {
            rotationZ = 10f * seno
            translationY = 4f * densidade * kotlin.math.cos(a * 2f)
        }
    }
}

/** Objetos que ficam na parede ou em cima de outro objeto: não caem até o chão. */
private val objetosDeParede = setOf("abajur", "quadro", "espelho", "livros", "bolo", "ursinho")

/** Está dentro das paredes da casa? */
fun dentroDaCasa(p: Posicao, estado: EstadoJogo): Boolean =
    estado.temEstrutura(Estrutura.PAREDES) && p.x in 0.11f..0.89f && p.y in 0.28f..0.80f

/**
 * Para onde vai um objeto solto em [p]. Móveis pousam no piso do cômodo; coisas do jardim, na grama.
 * Devolve null se o objeto não mora ali (a cama não vai para o jardim, a árvore não entra na sala).
 */
fun encaixar(id: String, p: Posicao, estado: EstadoJogo): Posicao? {
    val dentro = dentroDaCasa(p, estado)
    return when (ItensDaCasa.lugarDe(id)) {
        Lugar.CEU -> p
        Lugar.DENTRO -> when {
            !dentro -> null
            id in objetosDeParede -> desviarDasJanelas(id, p)
            else -> pousar(p, dentro = true)
        }
        Lugar.FORA -> if (dentro) null else pousar(p, dentro = false)
        Lugar.QUALQUER -> if (id in objetosDeParede) p else pousar(p, dentro)
    }
}

/** Janelas (x1, y1, x2, y2), em frações da cena. */
private val janelas = listOf(
    floatArrayOf(0.37f, 0.33f, 0.49f, 0.45f),
    floatArrayOf(0.43f, 0.565f, 0.55f, 0.68f),
    floatArrayOf(0.64f, 0.565f, 0.76f, 0.68f),
    floatArrayOf(0.78f, 0.35f, 0.86f, 0.43f),
)

/** Quadros, espelhos e livros não cobrem as janelas: são empurrados para o lado mais perto. */
private fun desviarDasJanelas(id: String, p: Posicao): Posicao {
    val meia = (ItensDaCasa.porId[id]?.tamanho ?: 0.06f) / 2
    val cy = p.y - meia
    for (j in janelas) {
        if (p.x + meia > j[0] && p.x - meia < j[2] && cy > j[1] && cy < j[3]) {
            val esquerda = j[0] - meia - 0.005f
            val direita = j[2] + meia + 0.005f
            return Posicao(if (p.x - esquerda < direita - p.x) esquerda else direita, p.y)
        }
    }
    return p
}

private fun pousar(p: Posicao, dentro: Boolean): Posicao {
    val y = when {
        dentro && p.y < 0.535f -> 0.528f
        dentro -> 0.775f
        p.y < 0.82f -> 0.84f
        else -> p.y
    }
    return Posicao(p.x, y)
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
    /** Recebe onde o objeto foi solto e devolve onde ele fica (ou null se foi guardado no baú). */
    onSoltar: (Posicao) -> Posicao?,
    larguraRelativa: Float = item?.tamanho ?: 0.1f,
    proporcao: Float = proporcaoDoItem(item),
    pulo: Int = 0,
    vida: Vida = Vida.PARADO,
    tempo: Float = 0f,
    noite: Boolean = false,
    descricao: String = item?.nome ?: "",
    conteudo: @Composable (larguraPx: Float) -> Unit,
) {
    val faisca = remember { Animatable(1f) }
    // Cada objeto começa a animação num ponto diferente, para não mexerem todos juntos.
    val defasagem = remember(item?.id) { ((item?.id?.hashCode() ?: 7) and 0xFF) / 255f }
    val densidade = LocalDensity.current
    val w = larguraRelativa * larguraCena
    val h = w * proporcao
    var arrasto by remember { mutableStateOf(Offset.Zero) }
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

    // Área de toque de pelo menos 48dp, mesmo para objetos pequenos (dedos de criança).
    val minimo = with(densidade) { 48.dp.toPx() }
    val toqueW = maxOf(w, minimo)
    val toqueH = maxOf(h, minimo)
    val x = pos.x * larguraCena - toqueW / 2 + arrasto.x
    val y = pos.y * alturaCena - toqueH + arrasto.y
    Box(
        Modifier
            .offset { IntOffset(x.roundToInt(), (y + salto.value).roundToInt()) }
            .size(with(densidade) { toqueW.toDp() }, with(densidade) { toqueH.toDp() })
            .zIndex(if (arrastando) 10f else 0f)
            .graphicsLayer {
                val s = escala.value * if (arrastando) 1.15f else 1f
                scaleX = s
                scaleY = s
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                if (!arrastando) aplicarVida(vida, (tempo + defasagem) % 1f, larguraCena, alturaCena, densidade.density)
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
                                    faisca.snapTo(0f)
                                    faisca.animateTo(1f, tween(700))
                                }
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
                                    val solto = Posicao(pos.x + arrasto.x / larguraCena, pos.y + arrasto.y / alturaCena)
                                    val final = onSoltar(solto)
                                    if (final == null) {
                                        arrasto = Offset.Zero
                                    } else {
                                        // O objeto "cai" de onde foi solto até o chão do cômodo, com um quique.
                                        val diferenca = Offset((solto.x - final.x) * larguraCena, (solto.y - final.y) * alturaCena)
                                        arrasto = diferenca
                                        escopo.launch {
                                            animate(0f, 1f, animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) { f, _ ->
                                                arrasto = diferenca * (1f - f)
                                            }
                                        }
                                    }
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
        Box(
            Modifier
                .size(with(densidade) { w.toDp() }, with(densidade) { h.toDp() })
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    // À noite tudo fica azulado, menos o que brilha (as janelas, desenhadas na cena).
                    if (noite) drawRect(Color(0x70142045), blendMode = BlendMode.SrcAtop)
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            conteudo(w)
        }
        if (faisca.value < 1f) {
            Canvas(Modifier.matchParentSize()) {
                val p = faisca.value
                val centro = Offset(size.width / 2, size.height / 2)
                val raio = size.minDimension * (0.4f + 0.6f * p)
                for (k in 0 until 6) {
                    val ang = k * (Math.PI.toFloat() / 3f) + p
                    val c = Offset(centro.x + kotlin.math.cos(ang) * raio, centro.y + kotlin.math.sin(ang) * raio)
                    drawPath(caminhoEstrela(c, 5.dp.toPx() * (1f - p * 0.5f)), Color(0xFFFFD84D).copy(alpha = 1f - p))
                }
            }
        }
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

private val CorMadeira = Color(0xFFC98B5A)
private val CorMoldura = Color(0xFFE8C9A0)
private val CorVidro = Color(0xFFA9E1FF)
private val CorLuz = Color(0xFFFFE08A)

private fun DrawScope.desenharCasa(e: EstadoJogo, t: Float, nova: Estrutura?, progresso: Float, noite: Boolean, semCeu: Boolean = false) {
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
    val ceu = if (noite) listOf(Color(0xFF16204A), Color(0xFF3B4C8C)) else listOf(Color(0xFF7EC8FF), Color(0xFFCDEBFF))
    if (!semCeu) drawRect(Brush.verticalGradient(ceu, endY = Y(0.8f)))
    if (noite) {
        for (i in 0 until 28) {
            val x = ((i * 0.137f + 0.05f) % 1f)
            val y = ((i * 0.071f + 0.02f) % 0.62f)
            val brilho = 0.45f + 0.55f * kotlin.math.abs(kotlin.math.sin((t + i * 0.13f) * Math.PI.toFloat()))
            drawCircle(Color.White.copy(alpha = brilho), radius = X(0.004f + (i % 3) * 0.002f), center = Offset(X(x), Y(y)))
        }
    }
    // Nuvens e passarinhos de enfeite, sempre passeando (a cena nunca fica parada).
    if (!noite) {
        for (k in 0 until 2) {
            // Passeiam só pela metade direita do céu (o sol e o arco-íris ficam à esquerda).
            val x = 0.38f + ((t * 0.3f + k * 0.5f) % 1f) * 0.62f
            nuvemDeFundo(X(x), Y(0.04f + k * 0.06f), X(0.06f))
        }
        for (k in 0 until 3) {
            val bx = X(((t + k * 0.31f) % 1f) * 1.1f - 0.05f)
            val by = Y(0.22f + k * 0.03f) + kotlin.math.sin((t * 6f + k) * Math.PI.toFloat()) * X(0.006f)
            val asa = X(0.012f)
            drawLine(Color(0xFF3D2C5E), Offset(bx - asa, by - asa * 0.5f), Offset(bx, by), 3f)
            drawLine(Color(0xFF3D2C5E), Offset(bx, by), Offset(bx + asa, by - asa * 0.5f), 3f)
        }
    }
    // morros
    drawOval(if (noite) Color(0xFF4C7F55) else Color(0xFFA8DD8F), topLeft = Offset(X(-0.2f), Y(0.68f)), size = Size(X(0.7f), Y(0.24f)))
    drawOval(if (noite) Color(0xFF447549) else Color(0xFF98D47E), topLeft = Offset(X(0.45f), Y(0.70f)), size = Size(X(0.8f), Y(0.2f)))

    // chão
    val temTerreno = e.temEstrutura(Estrutura.TERRENO)
    val chao = when {
        temTerreno && noite -> listOf(Color(0xFF3F7A45), Color(0xFF2F5E35))
        temTerreno -> listOf(Color(0xFF7CC56B), Color(0xFF5FAE52))
        noite -> listOf(Color(0xFF7A6450), Color(0xFF5E4C3C))
        else -> listOf(Color(0xFFC9A27A), Color(0xFFA97F57))
    }
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

    // Terreno ainda vazio: o contorno pontilhado da casa que vai nascer ali.
    if (!e.temEstrutura(Estrutura.PAREDES)) {
        val tracejado = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(X(0.025f), X(0.018f)))
        val contorno = Path().apply {
            moveTo(X(Planta.ESQ), Y(Planta.BASE)); lineTo(X(Planta.ESQ), Y(Planta.TOPO)); lineTo(X(0.04f), Y(0.315f))
            lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f)); lineTo(X(Planta.DIR), Y(Planta.TOPO)); lineTo(X(Planta.DIR), Y(Planta.BASE))
        }
        drawPath(contorno, Color.White.copy(alpha = 0.75f), style = Stroke(X(0.008f), pathEffect = tracejado, join = StrokeJoin.Round))
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
        papelDeParede(e.papelDeParede, X(L), Y(T), X(R - L), Y(B - T))

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
        if (noite) {
            // Com o abajur, a casa fica mais clarinha por dentro.
            val escuro = if ("abajur" in e.colocados) Color(0x33101A40) else Color(0x66101A40)
            drawRect(escuro, Offset(X(L), Y(T)), Size(X(R - L), Y(B - T)))
        }
        drawRect(CorMoldura, Offset(X(L), Y(T)), Size(X(R - L), Y(B - T)), style = Stroke(X(0.018f)))
    }

    if (e.temEstrutura(Estrutura.PAREDES)) {
        camada(Estrutura.JANELAS) {
            val vidro = if (noite) CorLuz else CorVidro
            janela(X(0.39f), Y(0.35f), X(0.08f), Y(0.08f), vidro)
            janela(X(0.45f), Y(0.585f), X(0.08f), Y(0.075f), vidro)
            janela(X(0.66f), Y(0.585f), X(0.08f), Y(0.075f), vidro)
            drawCircle(vidro, X(0.035f), Offset(X(0.82f), Y(0.39f)))
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
            val corPorta = Color(Loja.coresDaPorta.getOrElse(e.corPorta) { Loja.coresDaPorta[0] })
            drawPath(porta, corPorta)
            drawPath(porta, escurecer(corPorta, 0.7f), style = Stroke(3f))
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

    // (o clima é desenhado por cima de tudo, no fim)
    camada(Estrutura.TELHADO) {
        val telhado = Path().apply {
            moveTo(X(0.04f), Y(0.315f)); lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f)); close()
        }
        val cor = Color(Loja.coresDoTelhado.getOrElse(e.corTelhado) { Loja.coresDoTelhado[0] })
        val borda = Color(Loja.coresDoTelhadoEscuras.getOrElse(e.corTelhado) { Loja.coresDoTelhadoEscuras[0] })
        drawPath(telhado, if (noite) escurecer(cor, 0.7f) else cor)
        drawPath(telhado, if (noite) escurecer(borda, 0.7f) else borda, style = Stroke(X(0.012f), join = StrokeJoin.Round))
        for (k in 1 until 5) {
            val y = 0.075f + k * 0.048f
            val metade = (y - 0.075f) / 0.24f * 0.46f
            drawLine(Color(0x14000000), Offset(X(0.5f - metade), Y(y)), Offset(X(0.5f + metade), Y(y)), 3f)
        }
        drawCircle(Color.White, X(0.04f), Offset(X(0.5f), Y(0.215f)))
        drawCircle(if (noite) CorLuz else CorVidro, X(0.03f), Offset(X(0.5f), Y(0.215f)))
    }

    // Chuva ou neve compradas na loja.
    when (e.clima) {
        1 -> for (i in 0 until 40) {
            val x = ((i * 0.173f) % 1f)
            val y = ((i * 0.071f + t * 2f) % 1f)
            drawLine(Color(0xAA7FB8FF), Offset(X(x), Y(y)), Offset(X(x - 0.008f), Y(y + 0.03f)), 3f)
        }
        2 -> for (i in 0 until 36) {
            val x = ((i * 0.173f) % 1f) + 0.01f * kotlin.math.sin((t * 4f + i) * Math.PI.toFloat())
            val y = ((i * 0.071f + t * 0.8f) % 1f)
            drawCircle(Color.White.copy(alpha = 0.9f), X(0.006f + (i % 3) * 0.002f), Offset(X(x), Y(y)))
        }
    }
}

/** Nuvem feita de um único caminho (as bolhas se juntam sem marcas de sobreposição). */
private fun DrawScope.nuvemDeFundo(x: Float, y: Float, r: Float) {
    val nuvem = Path().apply {
        addOval(Rect(Offset(x, y), r * 0.6f))
        addOval(Rect(Offset(x + r * 0.7f, y - r * 0.2f), r * 0.8f))
        addOval(Rect(Offset(x + r * 1.4f, y), r * 0.6f))
        addRoundRect(RoundRect(Rect(x, y - r * 0.1f, x + r * 1.4f, y + r * 0.6f), CornerRadius(r * 0.3f)))
    }
    drawPath(nuvem, Color.White.copy(alpha = 0.8f))
}

/** O pedaço de jardim que aparece em telas altas: canteiro, arbustos, cogumelos e o caminho continuando. */
private fun DrawScope.jardimDeBaixo(topo: Float, alto: Float, noite: Boolean, temCaminho: Boolean, temGrama: Boolean) {
    if (!temGrama) return
    val w = size.width
    val folha = if (noite) Color(0xFF2E5F34) else Color(0xFF4FA046)
    // arbustos nos cantos
    for ((cx, r) in listOf(0.06f to 0.07f, 0.94f to 0.08f)) {
        val c = Offset(w * cx, topo + alto * 0.45f)
        drawCircle(folha, w * r, c)
        drawCircle(folha, w * r * 0.8f, c + Offset(w * r * 0.8f * (if (cx < 0.5f) 1f else -1f), w * r * 0.2f))
    }
    // canteiro com flores
    val canteiro = Rect(Offset(w * 0.55f, topo + alto * 0.3f), Size(w * 0.3f, alto * 0.4f))
    drawRoundRect(if (noite) Color(0xFF5A4232) else Color(0xFF8D5A3B), canteiro.topLeft, canteiro.size, CornerRadius(12f))
    for (i in 0 until 6) {
        val fx = canteiro.left + canteiro.width * (0.12f + i * 0.15f)
        val fy = canteiro.top + canteiro.height * 0.45f
        drawLine(folha, Offset(fx, fy), Offset(fx, fy + canteiro.height * 0.3f), 4f)
        val corFlor = listOf(Color(0xFFFF6FAE), Color(0xFFFFD166), Color(0xFFB57BFF))[i % 3]
        drawCircle(if (noite) escurecer(corFlor, 0.6f) else corFlor, canteiro.width * 0.045f, Offset(fx, fy))
    }
    // caminho de pedrinhas continuando até a borda
    if (temCaminho) {
        var y = topo + alto * 0.1f
        var k = 0
        while (y < topo + alto) {
            val x = w * (0.30f + if (k % 2 == 0) 0.01f else -0.01f)
            drawOval(Color(0xFFD8D2C6), Offset(x - w * 0.045f, y), Size(w * 0.09f, w * 0.04f))
            y += w * 0.07f; k++
        }
    }
    // cogumelos
    for (cx in listOf(0.16f, 0.45f)) {
        val base = Offset(w * cx, topo + alto * 0.75f)
        drawRect(Color(0xFFF4E9D8), Offset(base.x - 5f, base.y - 16f), Size(10f, 16f))
        drawArc(Color(0xFFEF476F), 180f, 180f, true, Offset(base.x - 18f, base.y - 30f), Size(36f, 28f))
        drawCircle(Color.White, 3.5f, Offset(base.x - 6f, base.y - 22f))
        drawCircle(Color.White, 3f, Offset(base.x + 7f, base.y - 20f))
    }
}

/** Desenhos do papel de parede (comprados na loja): listras, bolinhas ou estrelinhas. */
private fun DrawScope.papelDeParede(tipo: Int, x0: Float, y0: Float, w: Float, h: Float) {
    if (tipo == 0) return
    val cor = Color(0x14000000)
    val passo = w / 18f
    when (tipo) {
        1 -> {
            var x = x0
            while (x < x0 + w) { drawRect(cor, Offset(x, y0), Size(passo / 2, h)); x += passo }
        }
        2 -> {
            var y = y0 + passo / 2
            var linha = 0
            while (y < y0 + h) {
                var x = x0 + if (linha % 2 == 0) passo / 2 else passo
                while (x < x0 + w) { drawCircle(cor, passo * 0.18f, Offset(x, y)); x += passo }
                y += passo; linha++
            }
        }
        else -> {
            var y = y0 + passo
            var linha = 0
            while (y < y0 + h) {
                var x = x0 + if (linha % 2 == 0) passo else passo * 2
                while (x < x0 + w) { drawPath(caminhoEstrela(Offset(x, y), passo * 0.3f), Color(0x26FFC93C)); x += passo * 2 }
                y += passo * 1.5f; linha++
            }
        }
    }
}

private fun DrawScope.janela(x: Float, y: Float, w: Float, h: Float, vidro: Color = CorVidro) {
    if (vidro == CorLuz) drawCircle(CorLuz.copy(alpha = 0.25f), w * 1.1f, Offset(x + w / 2, y + h / 2))
    val r = CornerRadius(w * 0.08f)
    drawRect(Color(0xFFFF8FAB), Offset(x - w * 0.15f, y - h * 0.15f), Size(w * 0.15f, h * 1.3f))
    drawRect(Color(0xFFFF8FAB), Offset(x + w, y - h * 0.15f), Size(w * 0.15f, h * 1.3f))
    drawRoundRect(vidro, Offset(x, y), Size(w, h), r)
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
