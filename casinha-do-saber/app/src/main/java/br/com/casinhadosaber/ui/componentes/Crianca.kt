package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import kotlin.math.ceil
import kotlin.math.roundToInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.ParteDoCorpo
import br.com.casinhadosaber.dominio.Perfil
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.Rect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Proporção do desenho antigo da criança (feito com formas), usado como reserva. */
const val PROPORCAO_DESENHO_CRIANCA = 0.62f

/**
 * A criança (menino ou menina). Monta as camadas 3D pré-renderizadas (`assets/arte/crianca_*`) para o perfil,
 * o humor e a pose, tingidas com a pele, o cabelo e a cor da roupa; com traje ou acessório da loja (que ainda
 * não têm camadas) ou se faltar alguma camada, usa o desenho antigo, feito com formas simples.
 * [acenando] faz um braço dar tchau.
 */
@Composable
fun Crianca(
    perfil: Perfil,
    modifier: Modifier = Modifier,
    acenando: Boolean = false,
    humor: Humor = Humor.FELIZ,
    /** Mexe a boca, como se estivesse falando (junto com o narrador). */
    falando: Boolean = false,
    /**
     * false = figura parada (miniaturas de listas, do guarda-roupa e da loja): desenhada uma vez numa imagem
     * guardada, sem respirar, piscar nem acenar. Poupa os aparelhos mais simples.
     */
    animado: Boolean = true,
) {
    val descricao = if (perfil.genero == Genero.MENINA) "Menina" else "Menino"
    val contexto = LocalContext.current
    val emCamadas = remember(perfil) { ComposicaoDosPersonagens.podeUsar(contexto, PersonagemDaArte.CRIANCA, perfil) }
    BoxWithConstraints(
        modifier
            .aspectRatio(PROPORCAO_CRIANCA)
            .semantics { contentDescription = perfil.nome.ifBlank { descricao } },
    ) {
        val vivo = animado && animacoesLigadas()
        if (emCamadas) {
            PersonagemEmCamadas(
                personagem = PersonagemDaArte.CRIANCA,
                perfil = perfil,
                humor = humor,
                acenando = acenando,
                falando = falando,
                vivo = vivo,
                largura = maxWidth,
                altura = maxHeight,
                chapeu = perfil.chapeu,
                ritmoDoPiscar = 3800,
                ginga = 0f,
                reserva = { q -> desenharCrianca(perfil, null, q.olhosFechados, q.humor) },
            )
        } else {
            if (vivo) {
                CriancaAnimada(perfil, acenando, humor, falando)
            } else {
                CriancaParada(perfil, humor)
            }
            // Chapéu comprado na loja, em cima da cabeça do desenho antigo (centrado, na altura toda).
            if (perfil.chapeu.isNotEmpty()) {
                // Cabeça: centro em 27% da altura, raio = 34% da largura. O chapéu tem ~90% da largura da cabeça
                // e a aba fica um pouco acima dos olhos.
                val larguraDoDesenho = if (maxWidth < maxHeight * PROPORCAO_DESENHO_CRIANCA) maxWidth else maxHeight * PROPORCAO_DESENHO_CRIANCA
                val topoDoDesenho = maxHeight - larguraDoDesenho / PROPORCAO_DESENHO_CRIANCA
                val raio = larguraDoDesenho * 0.34f
                val largura = raio * 2f * 0.9f
                val fonte = largura * 0.82f
                val baseDoChapeu = topoDoDesenho + (maxHeight - topoDoDesenho) * 0.27f - raio * 0.45f
                val densidade = LocalDensity.current
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = baseDoChapeu - fonte * 1.05f)
                        .size(largura, fonte * 1.1f),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        perfil.chapeu,
                        fontSize = with(densidade) { fonte.toSp() },
                        lineHeight = with(densidade) { (fonte * 1.05f).toSp() },
                    )
                }
            }
        }
    }
}

/**
 * A criança viva. Só o que muda é lido no desenho: o piscar vira um estado que troca só quando o olho fecha
 * ou abre (e não a cada quadro), o aceno e a boca só existem enquanto a criança acena ou fala, e a respiração
 * fica na camada (sem redesenhar a figura). Parada, a criança é redesenhada só na piscada.
 */
@Composable
private fun CriancaAnimada(perfil: Perfil, acenando: Boolean, humor: Humor, falando: Boolean) {
    val transicao = rememberInfiniteTransition(label = "crianca")
    val balanco = if (acenando) {
        transicao.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
            label = "balanco",
        )
    } else {
        null
    }
    val piscar = transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing)),
        label = "piscar",
    )
    val olhosFechados = remember(piscar) { derivedStateOf { piscar.value > 0.96f } }
    val respiro = transicao.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "respiro",
    )
    val bocaAberta = if (falando) {
        val boca = transicao.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(160), RepeatMode.Reverse),
            label = "boca",
        )
        remember(boca) { derivedStateOf { boca.value > 0.5f } }
    } else {
        null
    }
    Canvas(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Respiração: o corpo sobe e desce bem de leve, a partir dos pés.
                scaleY = respiro.value
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
            },
    ) {
        desenharCrianca(
            perfil,
            balanco?.value,
            olhosFechados = olhosFechados.value,
            humor = if (bocaAberta?.value == true) Humor.SURPRESO else humor,
        )
    }
}

/**
 * Criança parada: desenhada uma única vez numa imagem (com folga para o contorno e a sombra) e depois só
 * copiada. Rolar uma lista de miniaturas não refaz a camada do contorno a cada quadro.
 */
@Composable
private fun CriancaParada(perfil: Perfil, humor: Humor) {
    Spacer(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                if (w < 1f || h < 1f) return@drawWithCache onDrawBehind { }
                val traco = (w * 0.022f).coerceAtLeast(1f)
                val fx = ceil(w * FOLGA_X + traco)
                val fy = ceil(h * FOLGA_Y + traco)
                val imagem = ImageBitmap(ceil(w + fx * 2).toInt(), ceil(h + fy * 2).toInt())
                CanvasDrawScope().draw(this, layoutDirection, ComposeCanvas(imagem), size) {
                    translate(fx, fy) { desenharCrianca(perfil, null, olhosFechados = false, humor = humor) }
                }
                onDrawBehind { drawImage(imagem, topLeft = Offset(-fx, -fy)) }
            },
    )
}

/** Cor principal da roupa especial (ou a da roupa de sempre). Também pinta os cabides do guarda-roupa. */
fun corDaRoupaDoTraje(traje: String?, perfil: Perfil): Color = when (traje) {
    "pijama" -> Color(0xFF3B4C8C)
    "abelha" -> Color(0xFFFFC93C)
    "astronauta" -> Color(0xFFF2F4F8)
    "futebol", "chuva" -> Color(0xFFFFD23F)
    "bailarina" -> Color(0xFFFFB3D1)
    "medico", "chef", "pirata" -> Color(0xFFF7F9FC)
    "realeza" -> Color(0xFF7B3FC4)
    "dinossauro" -> Color(0xFF5CC46B)
    // Listras, herói, fada, vestido e macacão usam a cor escolhida no espelho.
    else -> Color(perfil.corDaRoupa)
}

/**
 * Formato do corpo com a roupa especial: true = vestido ou casaco comprido (trapézio), false = blusa e calça.
 * Sem traje (ou nos trajes antigos), segue o padrão de sempre: vestido na menina, blusa e calça no menino.
 * Qualquer criança pode vestir qualquer roupa.
 */
private fun corpoComprido(traje: String?, menina: Boolean): Boolean = when (traje) {
    "vestido", "bailarina", "chuva", "medico", "realeza" -> true
    "macacao", "futebol", "dinossauro", "pirata", "chef" -> false
    else -> menina
}

/** Contorno escuro da criança (o mesmo tom ameixa dos traços dos mascotes e da Corujinha). */
private val ContornoDaCrianca = Color(0xE6533A4E)

/** Quanto o desenho pode passar das bordas (mãos, asinhas, sombra), em fração da largura e da altura. */
private const val FOLGA_X = 0.1f
private const val FOLGA_Y = 0.04f

/** Tinta da camada do contorno: criada uma vez só (e não a cada quadro). */
private val TintaDoContorno = Paint().apply { colorFilter = ColorFilter.tint(ContornoDaCrianca, BlendMode.SrcIn) }

/**
 * A criança inteira: sombra no chão, um contorno fino em volta da silhueta (o desenho repetido em 8 direções
 * numa camada pintada de uma cor só, então as juntas de dentro, como braço e corpo, ficam sem linha) e, por
 * cima, o desenho colorido.
 */
fun DrawScope.desenharCrianca(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    noQuadroDoDesenho(PROPORCAO_DESENHO_CRIANCA) { desenharCriancaNoQuadro(perfil, aceno, olhosFechados, humor) }
}

/**
 * Roda [bloco] numa área com a [proporcao] do desenho antigo, a maior que cabe nesta, centrada e encostada
 * embaixo (os pés no chão): o desenho com formas não fica esticado no quadro novo, mais largo.
 */
internal inline fun DrawScope.noQuadroDoDesenho(proporcao: Float, bloco: DrawScope.() -> Unit) {
    val w = size.width
    val h = size.height
    val largura = minOf(w, h * proporcao)
    val altura = largura / proporcao
    val lado = (w - largura) / 2f
    val cima = h - altura
    if (lado < 0.5f && cima < 0.5f) {
        bloco()
    } else {
        inset(lado, cima, lado, 0f) { bloco() }
    }
}

private fun DrawScope.desenharCriancaNoQuadro(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    val w = size.width
    val h = size.height
    if (w < 1f || h < 1f) return
    // sombra no chão
    drawOval(Color(0x26000000), topLeft = Offset(w * 0.08f, h * 0.975f), size = Size(w * 0.84f, h * 0.05f))
    val traco = (w * 0.022f).coerceAtLeast(1f)
    val fx = ceil(w * FOLGA_X + traco)
    val fy = ceil(h * FOLGA_Y + traco)
    // O contorno (camada com tinta) é desenhado na CPU, numa imagem guardada: na GPU essa camada derrubava o
    // emulador do Android 14. Cada pose (aceno em 9 passos, olhos, humor) é desenhada uma vez e só copiada.
    val acenoEmPassos = aceno?.let { (it.coerceIn(-1f, 1f) * 4f).roundToInt() }
    val chave = ChaveDaCrianca(perfil, ceil(w).toInt(), ceil(h).toInt(), acenoEmPassos, olhosFechados, humor)
    val imagem = PosesDaCrianca[chave] ?: ImageBitmap(ceil(w + fx * 2).toInt(), ceil(h + fy * 2).toInt()).also { bmp ->
        CanvasDrawScope().draw(this, layoutDirection, ComposeCanvas(bmp), Size(w, h)) {
            translate(fx, fy) { desenharFiguraComContorno(perfil, acenoEmPassos?.let { it / 4f }, olhosFechados, humor) }
        }
        PosesDaCrianca[chave] = bmp
    }
    drawImage(imagem, topLeft = Offset(-fx, -fy))
}

private data class ChaveDaCrianca(val perfil: Perfil, val w: Int, val h: Int, val aceno: Int?, val olhos: Boolean, val humor: Humor)

/** As últimas poses desenhadas (poucas: cada uma é uma imagem do tamanho da criança). */
private object PosesDaCrianca : LinkedHashMap<ChaveDaCrianca, ImageBitmap>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ChaveDaCrianca, ImageBitmap>?): Boolean = size > 10
}

private fun DrawScope.desenharFiguraComContorno(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    val w = size.width
    val h = size.height
    val traco = (w * 0.022f).coerceAtLeast(1f)
    // A camada só precisa da área da figura (as mãos e as asinhas passam um pouco das bordas) mais a espessura
    // do contorno: bem menor que antes, então mais leve.
    val folgaX = w * FOLGA_X + traco
    val folgaY = h * FOLGA_Y + traco
    drawIntoCanvas { it.saveLayer(Rect(-folgaX, -folgaY, w + folgaX, h + folgaY), TintaDoContorno) }
    for (k in 0 until 8) {
        val a = k * PI.toFloat() / 4f
        translate(cos(a) * traco, sin(a) * traco) { desenharFiguraDaCrianca(perfil, aceno, olhosFechados, humor) }
    }
    drawIntoCanvas { it.restore() }
    desenharFiguraDaCrianca(perfil, aceno, olhosFechados, humor)
}

private fun DrawScope.desenharFiguraDaCrianca(perfil: Perfil, aceno: Float?, olhosFechados: Boolean, humor: Humor) {
    val w = size.width
    val h = size.height
    fun p(u: Float, v: Float) = Offset(u * w, v * h)

    val pele = Color(Aparencia.peles.getOrElse(perfil.pele) { Aparencia.peles[0] })
    val cabelo = Color(Aparencia.cabelos.getOrElse(perfil.cabelo) { Aparencia.cabelos[0] })
    val menina = perfil.genero == Genero.MENINA
    // Roupa especial do guarda-roupa (comprada na loja), desenhada por cima da roupa de sempre.
    val traje = Loja.trajeDe(perfil)?.id
    // Acessórios: um por parte do corpo (ids desconhecidos são ignorados).
    val rosto = Loja.acessorioDe(perfil, ParteDoCorpo.ROSTO)?.id
    val pescoco = Loja.acessorioDe(perfil, ParteDoCorpo.PESCOCO)?.id
    val costas = Loja.acessorioDe(perfil, ParteDoCorpo.COSTAS)?.id
    val naMao = Loja.acessorioDe(perfil, ParteDoCorpo.MAO)?.id
    val nosPes = Loja.acessorioDe(perfil, ParteDoCorpo.PES)?.id
    val noCabelo = Loja.acessorioDe(perfil, ParteDoCorpo.CABELO)?.id
    val semChapeu = perfil.chapeu.isEmpty()

    val roupa = corDaRoupaDoTraje(traje, perfil)
    var calca = if (menina) pele else Color(0xFF2E4A7D)
    var meia: Color? = if (menina) Color.White else null
    var bermuda: Color? = null
    var sapato = Color(0xFF5A3E36)
    when (traje) {
        "pijama" -> calca = Color(0xFF3B4C8C)
        "astronauta" -> { calca = Color(0xFFDCE1EA); sapato = Color(0xFF9AA3B5) }
        "abelha" -> if (!menina) calca = Color(0xFF2B2B2B)
        "vestido" -> { calca = pele; meia = Color.White }
        "bailarina" -> { calca = Color(0xFFFFD6E6); meia = null; sapato = Color(0xFFFF8FB8) }
        "chuva" -> sapato = Color(0xFFF2B705)
        "medico" -> { calca = Color(0xFF6FB7C9); meia = null }
        "realeza" -> { calca = Color(0xFFF5F0FF); meia = null; sapato = Color(0xFFE0A800) }
        "macacao" -> { calca = Color(0xFF3E6FB0); meia = null }
        "futebol" -> { calca = pele; meia = Color.White; bermuda = Color(0xFF2B59C3); sapato = Color(0xFF1E1E1E) }
        "dinossauro" -> { calca = Color(0xFF5CC46B); meia = null; sapato = Color(0xFF3E9A4C) }
        "pirata" -> { calca = Color(0xFF3A2A20); meia = null; sapato = Color(0xFF1E1E1E) }
        "chef" -> { calca = Color(0xFF3A3A3A); meia = null; sapato = Color(0xFF1E1E1E) }
    }
    val comprido = corpoComprido(traje, menina)

    // Atrás do corpo: capa de herói, asinhas de fada, rabo de dinossauro e o acessório das costas.
    if (traje == "heroi") {
        desenharCapa(Color(0xFFE63946), Color(0xFFB0222F), estrelas = false)
    } else if (traje == "fada") {
        for (lado in listOf(-1f, 1f)) {
            val base = p(0.5f + lado * 0.2f, 0.52f)
            drawOval(Color(0xB3D9C2FF), Offset(base.x + (if (lado < 0) -w * 0.36f else 0f), base.y - h * 0.16f), Size(w * 0.36f, h * 0.17f))
            drawOval(Color(0xB3FFC2E8), Offset(base.x + (if (lado < 0) -w * 0.26f else 0f), base.y - h * 0.01f), Size(w * 0.26f, h * 0.12f))
        }
    } else if (traje == "dinossauro") {
        val rabo = Path().apply {
            moveTo(w * 0.6f, h * 0.6f)
            quadraticTo(w * 0.92f, h * 0.66f, w * 0.99f, h * 0.9f)
            quadraticTo(w * 0.8f, h * 0.8f, w * 0.6f, h * 0.76f)
            close()
        }
        drawPath(rabo, Color(0xFF4FB25E))
        for ((u, v) in listOf(0.74f to 0.635f, 0.85f to 0.7f, 0.93f to 0.79f)) {
            val c = p(u, v)
            drawPath(
                Path().apply { moveTo(c.x - w * 0.05f, c.y); lineTo(c.x + w * 0.04f, c.y - h * 0.05f); lineTo(c.x + w * 0.04f, c.y + h * 0.01f); close() },
                Color(0xFFFF9F1C),
            )
        }
    }
    when (costas) {
        "capa" -> desenharCapa(Color(0xFF7B4DFF), Color(0xFF5631C9), estrelas = true)
        "asas" -> for (lado in listOf(-1f, 1f)) {
            // Asas de borboleta: duas de cima maiores, duas de baixo menores, com bolinhas.
            val cima = Offset(if (lado < 0) w * 0.0f else w * 0.66f, h * 0.36f)
            val baixo = Offset(if (lado < 0) w * 0.06f else w * 0.68f, h * 0.53f)
            drawOval(Color(0xF2FF9F1C), cima, Size(w * 0.34f, h * 0.2f))
            drawOval(Color(0xFFB5651D), cima, Size(w * 0.34f, h * 0.2f), style = Stroke(w * 0.02f))
            drawOval(Color(0xF2FFD23F), baixo, Size(w * 0.26f, h * 0.15f))
            drawOval(Color(0xFFB5651D), baixo, Size(w * 0.26f, h * 0.15f), style = Stroke(w * 0.02f))
            drawCircle(Color.White, w * 0.035f, Offset(cima.x + w * (if (lado < 0) 0.1f else 0.24f), cima.y + h * 0.08f))
            drawCircle(Color(0xFF4D96FF), w * 0.025f, Offset(baixo.x + w * 0.13f, baixo.y + h * 0.075f))
        }
        "mochila" -> {
            // A mochila aparece dos dois lados do corpo; as alças são desenhadas na frente, depois do corpo.
            drawRoundRect(Color(0xFFFF7A3D), p(0.13f, 0.47f), Size(w * 0.74f, h * 0.25f), CornerRadius(w * 0.08f))
            drawRoundRect(Color(0xFFD95A22), p(0.13f, 0.47f), Size(w * 0.74f, h * 0.25f), CornerRadius(w * 0.08f), style = Stroke(w * 0.02f))
        }
    }

    // pernas
    for (u in listOf(0.37f, 0.63f)) {
        drawLine(calca, p(u, 0.72f), p(u, 0.93f), strokeWidth = w * 0.17f, cap = StrokeCap.Round)
        meia?.let { drawLine(it, p(u, if (traje == "futebol") 0.84f else 0.86f), p(u, 0.93f), strokeWidth = w * 0.17f, cap = StrokeCap.Round) }
        bermuda?.let { drawLine(it, p(u, 0.72f), p(u, 0.79f), strokeWidth = w * 0.19f, cap = StrokeCap.Butt) }
    }
    // pés: sapato de sempre ou o acessório dos pés
    for (u in listOf(0.33f, 0.67f)) {
        val c = p(u, 0.96f)
        when (nosPes) {
            "tenis" -> {
                drawOval(Color(0xFFFF4F8B), Offset(c.x - w * 0.13f, c.y - h * 0.04f), Size(w * 0.26f, h * 0.07f))
                drawRoundRect(Color.White, Offset(c.x - w * 0.14f, c.y + h * 0.008f), Size(w * 0.28f, h * 0.025f), CornerRadius(w * 0.03f))
                drawLine(Color(0xFF4D96FF), Offset(c.x - w * 0.1f, c.y - h * 0.005f), Offset(c.x + w * 0.1f, c.y - h * 0.005f), strokeWidth = w * 0.025f, cap = StrokeCap.Round)
                drawCircle(Color(0xFFFFD166), w * 0.022f, Offset(c.x - w * 0.03f, c.y - h * 0.025f))
                drawCircle(Color(0xFFFFD166), w * 0.022f, Offset(c.x + w * 0.03f, c.y - h * 0.025f))
            }
            "botas" -> {
                drawRoundRect(Color(0xFF8D5A3B), Offset(c.x - w * 0.1f, h * 0.83f), Size(w * 0.2f, h * 0.15f), CornerRadius(w * 0.05f))
                drawOval(Color(0xFF8D5A3B), Offset(c.x - w * 0.14f, c.y - h * 0.03f), Size(w * 0.28f, h * 0.065f))
                drawRoundRect(Color(0xFF6B3E22), Offset(c.x - w * 0.115f, h * 0.83f), Size(w * 0.23f, h * 0.03f), CornerRadius(w * 0.03f))
                drawRect(Color(0xFF4A2A14), Offset(c.x - w * 0.14f, c.y + h * 0.018f), Size(w * 0.28f, h * 0.014f))
            }
            "pantufas" -> {
                // Pantufas de coelhinho: fofas, com orelhinhas, olhinhos e narizinho.
                for (lado in listOf(-1f, 1f)) {
                    drawOval(Color(0xFFFFFFFF), Offset(c.x + lado * w * 0.05f - w * 0.035f, c.y - h * 0.085f), Size(w * 0.07f, h * 0.07f))
                    drawOval(Color(0xFFFFB3C7), Offset(c.x + lado * w * 0.05f - w * 0.018f, c.y - h * 0.075f), Size(w * 0.036f, h * 0.05f))
                }
                drawOval(Color(0xFFFFF4F8), Offset(c.x - w * 0.15f, c.y - h * 0.045f), Size(w * 0.3f, h * 0.085f))
                drawOval(Color(0xFFFFC9D9), Offset(c.x - w * 0.15f, c.y - h * 0.045f), Size(w * 0.3f, h * 0.085f), style = Stroke(w * 0.015f))
                drawCircle(Color(0xFF2B2B2B), w * 0.016f, Offset(c.x - w * 0.045f, c.y - h * 0.012f))
                drawCircle(Color(0xFF2B2B2B), w * 0.016f, Offset(c.x + w * 0.045f, c.y - h * 0.012f))
                drawCircle(Color(0xFFFF6FAE), w * 0.02f, Offset(c.x, c.y + h * 0.005f))
            }
            else -> drawOval(sapato, topLeft = Offset(c.x - w * 0.13f, c.y - h * 0.035f), size = Size(w * 0.26f, h * 0.07f))
        }
    }

    // braços (o direito pode acenar). Os ombros ficam por dentro do corpo e as manguinhas, desenhadas
    // depois do corpo, cobrem a junta: o braço sai da roupa, sem ficar solto do lado.
    val ombroE = p(0.24f, 0.5f)
    val torcendo = humor == Humor.TORCENDO
    val pensando = humor == Humor.PENSANDO
    // Torcendo: os dois punhos para cima. Pensando: a mão no queixo.
    val maoE = if (torcendo) p(0.05f, 0.29f) else p(0.07f, 0.7f)
    val ombroD = p(0.76f, 0.5f)
    // Tchau: o braço dobra no cotovelo, e só o antebraço balança (a mão não sai da área da criança).
    val cotoveloD = if (aceno != null && !torcendo && !pensando) Offset(w * 0.9f, ombroD.y - w * 0.1f) else null
    val maoD = if (torcendo) {
        p(0.95f, 0.29f)
    } else if (pensando) {
        p(0.62f, 0.42f)
    } else if (cotoveloD != null && aceno != null) {
        val ang = (-65f + aceno * 13f) * PI.toFloat() / 180f
        val comp = w * 0.21f
        Offset(cotoveloD.x + cos(ang) * comp, cotoveloD.y + sin(ang) * comp)
    } else {
        p(0.93f, 0.7f)
    }
    drawLine(pele, ombroE, maoE, strokeWidth = w * 0.13f, cap = StrokeCap.Round)
    if (cotoveloD != null) {
        drawLine(pele, ombroD, cotoveloD, strokeWidth = w * 0.13f, cap = StrokeCap.Round)
        drawLine(pele, cotoveloD, maoD, strokeWidth = w * 0.13f, cap = StrokeCap.Round)
    } else {
        drawLine(pele, ombroD, maoD, strokeWidth = w * 0.13f, cap = StrokeCap.Round)
    }
    drawCircle(pele, w * 0.085f, maoE)
    drawCircle(pele, w * 0.085f, maoD)

    // corpo
    val corpo: Path
    if (comprido) {
        corpo = Path().apply {
            moveTo(w * 0.26f, h * 0.46f); lineTo(w * 0.74f, h * 0.46f)
            lineTo(w * 0.9f, h * 0.8f); lineTo(w * 0.1f, h * 0.8f); close()
        }
        drawPath(corpo, roupa)
        if (traje == null) for (u in listOf(0.3f, 0.5f, 0.7f)) drawCircle(Color(0x80FFFFFF), w * 0.04f, p(u, 0.68f))
    } else {
        corpo = Path().apply { addRoundRect(RoundRect(w * 0.2f, h * 0.45f, w * 0.8f, h * 0.75f, CornerRadius(w * 0.14f))) }
        drawPath(corpo, roupa)
        drawRect(bermuda ?: calca, topLeft = p(0.2f, 0.7f), size = Size(w * 0.6f, h * 0.06f))
        if (traje == null) drawPath(caminhoEstrela(p(0.5f, 0.58f), w * 0.1f), Color(0xFFFFD166))
    }
    if (traje != null) desenharTrajeNoCorpo(traje, corpo, if (comprido) h * 0.8f else h * 0.75f)
    // Volume da roupa: luz do lado esquerdo, sombra do lado direito e embaixo do queixo.
    clipPath(corpo) {
        drawRect(
            Brush.horizontalGradient(
                0f to Color(0x2EFFFFFF), 0.3f to Color(0x00FFFFFF), 0.68f to Color(0x00000000), 1f to Color(0x2B000000),
                startX = w * 0.1f, endX = w * 0.9f,
            ),
        )
        drawOval(Color(0x24000000), Offset(w * 0.3f, h * 0.43f), Size(w * 0.4f, h * 0.06f))
    }
    // Manguinhas (por cima do corpo, na cor da roupa): o braço nasce de dentro da roupa.
    val manga = w * 0.155f
    val pontaE = Offset(ombroE.x + (maoE.x - ombroE.x) * 0.3f, ombroE.y + (maoE.y - ombroE.y) * 0.3f)
    val alvoD = cotoveloD ?: maoD
    val pontaD = Offset(ombroD.x + (alvoD.x - ombroD.x) * 0.42f, ombroD.y + (alvoD.y - ombroD.y) * 0.42f)
    drawLine(roupa, ombroE, pontaE, strokeWidth = manga, cap = StrokeCap.Round)
    drawLine(roupa, ombroD, pontaD, strokeWidth = manga, cap = StrokeCap.Round)
    if (traje == "bailarina") {
        // Saia de tule por cima do corpo: duas fileiras de babados.
        for (i in 0 until 7) drawCircle(Color(0xCCFFC7DD), w * 0.1f, p(0.13f + i * 0.124f, 0.76f))
        for (i in 0 until 6) drawCircle(Color(0xE6FFE0EC), w * 0.09f, p(0.19f + i * 0.124f, 0.7f))
        drawRect(Color(0xFFFF8FB8), p(0.22f, 0.64f), Size(w * 0.56f, h * 0.025f))
    }
    // Na frente do corpo: alças da mochila e o fecho da capa.
    if (costas == "mochila") {
        for (lado in listOf(-1f, 1f)) {
            drawLine(Color(0xFFD95A22), p(0.5f + lado * 0.16f, 0.465f), p(0.5f + lado * 0.2f, 0.66f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
        }
    }

    // cabeça
    val centro = p(0.5f, 0.27f)
    val r = w * 0.34f
    val estilo = perfil.estiloCabelo
    if (menina && estilo == 0) for (lado in listOf(-1f, 1f)) drawCircle(cabelo, r * 0.36f, Offset(centro.x + lado * r * 1.05f, centro.y + r * 0.05f))
    if (menina && estilo == 2) {
        // Tranças: correntinhas de bolinhas descendo dos lados da cabeça.
        for (lado in listOf(-1f, 1f)) for (k in 0 until 4) {
            val c = Offset(centro.x + lado * r * (0.95f + k * 0.04f), centro.y + r * (0.15f + k * 0.42f))
            drawOval(cabelo, Offset(c.x - r * 0.2f, c.y - r * 0.24f), Size(r * 0.4f, r * 0.48f))
        }
        for (lado in listOf(-1f, 1f)) drawCircle(Color(0xFFFF4F8B), r * 0.1f, Offset(centro.x + lado * r * 1.07f, centro.y + r * 1.62f))
    }
    if (estilo == 1) {
        // Cacheado: bolinhas em volta da cabeça (mais volume na menina).
        val volta = if (menina) 1.12f else 1.0f
        var ang = 160f
        while (ang <= 380f) {
            val rad = Math.toRadians(ang.toDouble())
            drawCircle(cabelo, r * (if (menina) 0.3f else 0.24f), Offset(centro.x + (kotlin.math.cos(rad) * r * volta).toFloat(), centro.y + (kotlin.math.sin(rad) * r * volta).toFloat()))
            ang += 22f
        }
    }
    // Capuz da capa de chuva e da fantasia de dinossauro (só sem chapéu), atrás da cabeça.
    val capuz = when (traje) {
        "chuva" -> Color(0xFFFFD23F)
        "dinossauro" -> Color(0xFF5CC46B)
        else -> null
    }
    if (capuz != null && semChapeu) {
        if (traje == "dinossauro") {
            for (graus in listOf(-160f, -125f, -90f, -55f, -20f)) {
                val a = graus * PI.toFloat() / 180f
                val base1 = Offset(centro.x + cos(a - 0.2f) * r * 1.1f, centro.y + sin(a - 0.2f) * r * 1.1f)
                val base2 = Offset(centro.x + cos(a + 0.2f) * r * 1.1f, centro.y + sin(a + 0.2f) * r * 1.1f)
                val ponta = Offset(centro.x + cos(a) * r * 1.45f, centro.y + sin(a) * r * 1.45f)
                drawPath(Path().apply { moveTo(base1.x, base1.y); lineTo(ponta.x, ponta.y); lineTo(base2.x, base2.y); close() }, Color(0xFFFF9F1C))
            }
        }
        drawCircle(capuz, r * 1.14f, centro)
    }
    for (lado in listOf(-1f, 1f)) drawCircle(pele, r * 0.2f, Offset(centro.x + lado * r * 0.98f, centro.y + r * 0.1f))
    drawCircle(pele, r, centro)
    sombrearCabeca(centro, r)

    // cabelo: calota com mechas que descem sobre a testa
    val franja = franjaComMechas(centro, r, curta = !menina)
    // Sombrinha do cabelo na testa, logo abaixo das mechas.
    clipPath(Path().apply { addOval(Rect(centro, r)) }) {
        translate(0f, r * 0.06f) { drawPath(franja, Color(0x1F5B2A22)) }
    }
    drawPath(franja, cabelo)
    // Brilho no cabelo (uma faixa clarinha do lado da luz).
    drawArc(
        Color(0x47FFFFFF), startAngle = 218f, sweepAngle = 40f, useCenter = false,
        topLeft = Offset(centro.x - r * 0.86f, centro.y - r * 0.86f), size = Size(r * 1.72f, r * 1.72f),
        style = Stroke(r * 0.1f, cap = StrokeCap.Round),
    )
    if (menina && semChapeu && estilo != 2 && noCabelo == null) {
        desenharLaco(Offset(centro.x + r * 0.45f, centro.y - r * 0.85f), r, Color(0xFFFF4F8B))
    } else if (!menina && estilo == 2) {
        // Espetadinho: pontinhas em cima da cabeça.
        for (k in -2..2) {
            val x = centro.x + k * r * 0.32f
            val ponta = Path().apply {
                moveTo(x - r * 0.2f, centro.y - r * 0.82f)
                lineTo(x + r * 0.06f * k, centro.y - r * 1.3f)
                lineTo(x + r * 0.2f, centro.y - r * 0.82f)
                close()
            }
            drawPath(ponta, cabelo)
        }
    } else if (!menina && estilo == 0) {
        val topete = Path().apply {
            moveTo(centro.x - r * 0.1f, centro.y - r * 0.95f)
            quadraticTo(centro.x + r * 0.05f, centro.y - r * 1.35f, centro.x + r * 0.3f, centro.y - r * 1.2f)
            quadraticTo(centro.x + r * 0.1f, centro.y - r * 1.05f, centro.x + r * 0.15f, centro.y - r * 0.9f)
            close()
        }
        drawPath(topete, cabelo)
    }
    // Na cabeça, por cima do cabelo: lenço de pirata e chapéu de chef (só sem chapéu da loja).
    if (semChapeu && traje == "pirata") {
        val lenco = Path().apply {
            arcTo(androidx.compose.ui.geometry.Rect(centro, r * 1.07f), 195f, 150f, forceMoveTo = true)
            close()
        }
        drawPath(lenco, Color(0xFFE63946))
        for (k in -1..1) drawCircle(Color.White, r * 0.07f, Offset(centro.x + k * r * 0.4f, centro.y - r * 0.7f + kotlin.math.abs(k) * r * 0.12f))
        val no = Offset(centro.x + r * 1.0f, centro.y - r * 0.32f)
        drawPath(Path().apply { moveTo(no.x, no.y); lineTo(no.x + r * 0.35f, no.y + r * 0.1f); lineTo(no.x + r * 0.15f, no.y + r * 0.35f); close() }, Color(0xFFE63946))
    }
    if (semChapeu && traje == "chef") {
        for (dx in listOf(-0.4f, 0.4f, 0f)) drawCircle(Color.White, r * 0.34f, Offset(centro.x + dx * r, centro.y - r * (if (dx == 0f) 1.3f else 1.15f)))
        drawRoundRect(Color.White, Offset(centro.x - r * 0.62f, centro.y - r * 1.08f), Size(r * 1.24f, r * 0.34f), CornerRadius(r * 0.08f))
        drawRoundRect(Color(0xFFDCE1EA), Offset(centro.x - r * 0.62f, centro.y - r * 1.08f), Size(r * 1.24f, r * 0.34f), CornerRadius(r * 0.08f), style = Stroke(r * 0.04f))
    }
    // Acessório do cabelo.
    when (noCabelo) {
        "laco" -> desenharLaco(Offset(centro.x + r * 0.42f, centro.y - r * 0.92f), r * 1.7f, Color(0xFFE63946))
        "tiara" -> {
            drawArc(
                Color(0xFFFFD166), startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(centro.x - r * 0.98f, centro.y - r * 0.98f), size = Size(r * 1.96f, r * 1.96f),
                style = Stroke(r * 0.12f, cap = StrokeCap.Round),
            )
            val topo = Offset(centro.x, centro.y - r * 0.98f)
            drawPath(Path().apply { moveTo(topo.x - r * 0.18f, topo.y + r * 0.02f); lineTo(topo.x, topo.y - r * 0.3f); lineTo(topo.x + r * 0.18f, topo.y + r * 0.02f); close() }, Color(0xFFFFD166))
            drawCircle(Color(0xFFFF4F8B), r * 0.09f, Offset(topo.x, topo.y - r * 0.08f))
            for (lado in listOf(-1f, 1f)) {
                val a = (270f + lado * 38f) * PI.toFloat() / 180f
                drawCircle(Color(0xFF8FD3FF), r * 0.07f, Offset(centro.x + cos(a) * r * 0.98f, centro.y + sin(a) * r * 0.98f))
            }
        }
        "presilha" -> {
            val c = Offset(centro.x - r * 0.52f, centro.y - r * 0.72f)
            drawPath(caminhoEstrela(c, r * 0.24f), Color(0xFFFFD23F))
            drawPath(caminhoEstrela(c, r * 0.24f), Color(0xFFE0A800), style = Stroke(r * 0.04f))
        }
    }

    // rosto
    if (rosto == "mascara") {
        // Máscara de herói: faixa nos olhos com furinhos (os olhos aparecem por cima).
        val y = centro.y + r * 0.12f
        drawRoundRect(Color(0xFF3557D6), Offset(centro.x - r * 0.72f, y - r * 0.24f), Size(r * 1.44f, r * 0.44f), CornerRadius(r * 0.22f))
        for (lado in listOf(-1f, 1f)) {
            drawLine(Color(0xFF3557D6), Offset(centro.x + lado * r * 0.7f, y - r * 0.05f), Offset(centro.x + lado * r * 1.05f, y - r * 0.2f), strokeWidth = r * 0.12f, cap = StrokeCap.Round)
            drawOval(Color.White, Offset(centro.x + lado * r * 0.36f - r * 0.17f, y - r * 0.18f), Size(r * 0.34f, r * 0.34f))
        }
    }
    desenharBochechas(centro, r, humor)
    desenharNariz(centro, r, pele)
    desenharOlhos(
        centro, r, olhosFechados, humor,
        cilios = menina,
        corDaSobrancelha = lerp(cabelo, Color(0xFF2B1A14), 0.35f),
        // Com máscara ou óculos escuros as sobrancelhas ficam escondidas (e não brigam com o acessório).
        sobrancelhas = rosto != "mascara",
    )
    // Tapa-olho do pirata (só quando não há nada no rosto).
    if (traje == "pirata" && rosto == null) {
        val c = Offset(centro.x - r * 0.36f, centro.y + r * 0.12f)
        drawLine(Color(0xFF1E1E1E), Offset(centro.x - r * 0.95f, centro.y - r * 0.3f), Offset(centro.x + r * 0.9f, centro.y - r * 0.05f), strokeWidth = r * 0.05f)
        drawOval(Color(0xFF1E1E1E), Offset(c.x - r * 0.19f, c.y - r * 0.21f), Size(r * 0.38f, r * 0.4f))
    }
    when (rosto) {
        "oculos" -> {
            val aro = Color(0xFFE63946)
            for (lado in listOf(-1f, 1f)) {
                val c = Offset(centro.x + lado * r * 0.36f, centro.y + r * 0.12f)
                drawCircle(Color(0x33FFFFFF), r * 0.22f, c)
                drawCircle(aro, r * 0.22f, c, style = Stroke(r * 0.07f))
                drawLine(aro, Offset(centro.x + lado * r * 0.58f, c.y - r * 0.04f), Offset(centro.x + lado * r * 0.96f, c.y - r * 0.12f), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
            }
            drawLine(aro, Offset(centro.x - r * 0.14f, centro.y + r * 0.08f), Offset(centro.x + r * 0.14f, centro.y + r * 0.08f), strokeWidth = r * 0.06f)
        }
        "oculosdesol" -> {
            for (lado in listOf(-1f, 1f)) {
                val c = Offset(centro.x + lado * r * 0.36f, centro.y + r * 0.12f)
                drawRoundRect(Color(0xFF222831), Offset(c.x - r * 0.25f, c.y - r * 0.17f), Size(r * 0.5f, r * 0.34f), CornerRadius(r * 0.14f))
                drawLine(Color(0x99FFFFFF), Offset(c.x - r * 0.14f, c.y - r * 0.08f), Offset(c.x - r * 0.02f, c.y - r * 0.08f), strokeWidth = r * 0.05f, cap = StrokeCap.Round)
                drawLine(Color(0xFF222831), Offset(centro.x + lado * r * 0.6f, c.y - r * 0.06f), Offset(centro.x + lado * r * 0.96f, c.y - r * 0.12f), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
            }
            drawLine(Color(0xFF222831), Offset(centro.x - r * 0.12f, centro.y + r * 0.06f), Offset(centro.x + r * 0.12f, centro.y + r * 0.06f), strokeWidth = r * 0.07f)
        }
    }
    // Na cabeça: anteninhas de abelha ou o capacete de vidro do astronauta.
    if (traje == "abelha") {
        for (lado in listOf(-1f, 1f)) {
            val ponta = Offset(centro.x + lado * r * 0.7f, centro.y - r * 1.55f)
            drawLine(Color(0xFF2B2B2B), Offset(centro.x + lado * r * 0.3f, centro.y - r * 0.9f), ponta, strokeWidth = r * 0.07f, cap = StrokeCap.Round)
            drawCircle(Color(0xFF2B2B2B), r * 0.13f, ponta)
        }
    }
    desenharBoca(centro, r, humor)

    // Acessório do pescoço (embaixo do queixo) e o fecho da capa.
    when (pescoco) {
        "colar" -> {
            val cores = listOf(Color(0xFFFF4F8B), Color(0xFFFFD166), Color(0xFF4D96FF))
            for (i in 0..8) {
                val t = -1f + i * 0.25f
                drawCircle(cores[i % cores.size], w * 0.032f, Offset(w * (0.5f + t * 0.17f), h * (0.478f + (1f - t * t) * 0.04f)))
            }
        }
        "cachecol" -> {
            val cor = Color(0xFF3DBE6B)
            drawRoundRect(cor, p(0.29f, 0.468f), Size(w * 0.42f, h * 0.045f), CornerRadius(w * 0.05f))
            drawRoundRect(cor, p(0.55f, 0.49f), Size(w * 0.1f, h * 0.15f), CornerRadius(w * 0.03f))
            for (k in 0..2) drawRect(Color(0xCCFFFFFF), Offset(w * (0.36f + k * 0.11f), h * 0.468f), Size(w * 0.035f, h * 0.045f))
            drawRect(Color(0xCCFFFFFF), p(0.55f, 0.56f), Size(w * 0.1f, h * 0.02f))
            for (k in 0..3) drawLine(cor, p(0.565f + k * 0.024f, 0.638f), p(0.565f + k * 0.024f, 0.665f), strokeWidth = w * 0.014f)
        }
        "gravata" -> {
            val c = p(0.5f, 0.505f)
            val cor = Color(0xFFE63946)
            for (lado in listOf(-1f, 1f)) {
                drawPath(Path().apply { moveTo(c.x, c.y); lineTo(c.x + lado * w * 0.12f, c.y - h * 0.032f); lineTo(c.x + lado * w * 0.12f, c.y + h * 0.032f); close() }, cor)
            }
            drawCircle(Color(0xFFB0222F), w * 0.035f, c)
        }
    }
    if (costas == "capa") drawCircle(Color(0xFFFFD166), w * 0.04f, p(0.5f, 0.485f))

    if (traje == "astronauta") {
        drawCircle(Color(0x338FD3FF), r * 1.32f, centro)
        drawCircle(Color(0xFFDCE1EA), r * 1.32f, centro, style = Stroke(r * 0.12f))
        drawArc(
            Color(0x99FFFFFF), startAngle = 200f, sweepAngle = 50f, useCenter = false,
            topLeft = Offset(centro.x - r * 1.05f, centro.y - r * 1.05f), size = Size(r * 2.1f, r * 2.1f),
            style = Stroke(r * 0.1f, cap = StrokeCap.Round),
        )
    }

    // Acessório da mão (na mão esquerda: a direita é a que dá tchau), por cima de tudo.
    when (naMao) {
        "varinha" -> {
            val ponta = Offset(maoE.x + w * 0.02f, maoE.y - h * 0.16f)
            drawLine(Color(0xFF5A3E36), Offset(maoE.x, maoE.y + h * 0.01f), ponta, strokeWidth = w * 0.035f, cap = StrokeCap.Round)
            drawPath(caminhoEstrela(ponta, w * 0.09f), Color(0xFFFFD23F))
            drawPath(caminhoEstrela(ponta, w * 0.09f), Color(0xFFE0A800), style = Stroke(w * 0.012f))
            drawCircle(Color(0xFFFFF3B0), w * 0.018f, Offset(ponta.x + w * 0.1f, ponta.y - h * 0.03f))
            drawCircle(Color(0xFFFFF3B0), w * 0.013f, Offset(ponta.x - w * 0.08f, ponta.y - h * 0.05f))
        }
        "balao" -> {
            val centroBalao = Offset(maxOf(maoE.x + w * 0.05f, w * 0.12f), minOf(maoE.y - h * 0.4f, h * 0.15f))
            val baixo = Offset(centroBalao.x, centroBalao.y + w * 0.16f)
            drawPath(
                Path().apply { moveTo(maoE.x, maoE.y); quadraticTo(maoE.x + w * 0.06f, (maoE.y + baixo.y) / 2f, baixo.x, baixo.y) },
                Color(0xFF8A8A8A), style = Stroke(w * 0.012f),
            )
            drawOval(Color(0xFFFF4F6D), Offset(centroBalao.x - w * 0.12f, centroBalao.y - w * 0.16f), Size(w * 0.24f, w * 0.32f))
            drawPath(Path().apply { moveTo(baixo.x, baixo.y - w * 0.01f); lineTo(baixo.x - w * 0.03f, baixo.y + w * 0.035f); lineTo(baixo.x + w * 0.03f, baixo.y + w * 0.035f); close() }, Color(0xFFD93654))
            drawOval(Color(0x80FFFFFF), Offset(centroBalao.x - w * 0.07f, centroBalao.y - w * 0.11f), Size(w * 0.06f, w * 0.1f))
        }
        "ursinho" -> {
            val corpoUrso = Offset(maoE.x + w * 0.08f, maoE.y - h * 0.01f)
            val marrom = Color(0xFFB07A4F)
            val cabeca = Offset(corpoUrso.x, corpoUrso.y - w * 0.15f)
            drawCircle(marrom, w * 0.1f, corpoUrso)
            drawCircle(Color(0xFFE6C29A), w * 0.055f, corpoUrso)
            for (lado in listOf(-1f, 1f)) drawCircle(marrom, w * 0.035f, Offset(cabeca.x + lado * w * 0.06f, cabeca.y - w * 0.06f))
            drawCircle(marrom, w * 0.075f, cabeca)
            drawCircle(Color(0xFFE6C29A), w * 0.032f, Offset(cabeca.x, cabeca.y + w * 0.02f))
            drawCircle(Color(0xFF2B2B2B), w * 0.012f, Offset(cabeca.x, cabeca.y + w * 0.012f))
            for (lado in listOf(-1f, 1f)) drawCircle(Color(0xFF2B2B2B), w * 0.011f, Offset(cabeca.x + lado * w * 0.028f, cabeca.y - w * 0.015f))
            // a mãozinha da criança segurando o ursinho
            drawCircle(pele, w * 0.07f, maoE)
        }
    }
}

/** Capa presa nos ombros, atrás do corpo (traje de herói ou a capa mágica das costas). */
private fun DrawScope.desenharCapa(cor: Color, contorno: Color, estrelas: Boolean) {
    val w = size.width
    val h = size.height
    val capa = Path().apply {
        moveTo(w * 0.24f, h * 0.47f); lineTo(w * 0.76f, h * 0.47f)
        lineTo(w * 0.96f, h * 0.9f); quadraticTo(w * 0.5f, h * 0.96f, w * 0.04f, h * 0.9f); close()
    }
    drawPath(capa, cor)
    drawPath(capa, contorno, style = Stroke(w * 0.02f))
    if (estrelas) {
        for ((u, v) in listOf(0.12f to 0.84f, 0.88f to 0.84f, 0.16f to 0.66f, 0.84f to 0.66f)) {
            drawPath(caminhoEstrela(Offset(w * u, h * v), w * 0.045f), Color(0xFFFFD166))
        }
    }
}

/** Laço de fita no cabelo, com o nó em [n]. [r] é o raio da cabeça (ou maior, para um laço maior). */
private fun DrawScope.desenharLaco(n: Offset, r: Float, cor: Color) {
    drawPath(Path().apply { moveTo(n.x, n.y); lineTo(n.x - r * 0.3f, n.y - r * 0.23f); lineTo(n.x - r * 0.25f, n.y + r * 0.23f); close() }, cor)
    drawPath(Path().apply { moveTo(n.x, n.y); lineTo(n.x + r * 0.33f, n.y - r * 0.15f); lineTo(n.x + r * 0.23f, n.y + r * 0.25f); close() }, cor)
    drawCircle(cor, r * 0.1f, n)
}

/** Estampas e detalhes da roupa especial, recortados no formato do corpo (que termina em [base]). */
private fun DrawScope.desenharTrajeNoCorpo(traje: String, corpo: Path, base: Float) {
    val w = size.width
    val h = size.height
    val topo = h * 0.45f
    clipPath(corpo) {
        when (traje) {
            "listras" -> {
                var y = topo + h * 0.03f
                while (y < base) {
                    drawRect(Color(0xCCFFFFFF), Offset(0f, y), Size(w, h * 0.035f))
                    y += h * 0.075f
                }
            }
            "pijama" -> for (i in 0 until 7) {
                val u = 0.22f + (i * 0.37f) % 0.58f
                val v = 0.5f + (i * 0.13f) % 0.26f
                drawPath(caminhoEstrela(Offset(w * u, h * v), w * 0.055f), Color(0xFFFFD166))
            }
            "abelha" -> {
                var y = topo + h * 0.05f
                while (y < base) {
                    drawRect(Color(0xFF2B2B2B), Offset(0f, y), Size(w, h * 0.05f))
                    y += h * 0.1f
                }
            }
            "heroi" -> {
                val c = Offset(w * 0.5f, h * 0.57f)
                drawCircle(Color(0xFFFFD166), w * 0.13f, c)
                val raio = Path().apply {
                    moveTo(c.x + w * 0.03f, c.y - w * 0.1f); lineTo(c.x - w * 0.06f, c.y + w * 0.01f)
                    lineTo(c.x + w * 0.01f, c.y + w * 0.01f); lineTo(c.x - w * 0.03f, c.y + w * 0.1f)
                    lineTo(c.x + w * 0.07f, c.y - w * 0.02f); lineTo(c.x, c.y - w * 0.02f); close()
                }
                drawPath(raio, Color(0xFFE63946))
            }
            "fada" -> for (i in 0 until 6) {
                val u = 0.25f + (i * 0.29f) % 0.5f
                val v = 0.52f + (i * 0.11f) % 0.24f
                drawPath(caminhoEstrela(Offset(w * u, h * v), w * 0.04f, 4), Color(0xE6FFFFFF))
            }
            "astronauta" -> {
                drawRect(Color(0xFF9AA3B5), Offset(0f, h * 0.66f), Size(w, h * 0.035f))
                drawRoundRect(Color(0xFF4D96FF), Offset(w * 0.56f, h * 0.51f), Size(w * 0.14f, h * 0.07f), CornerRadius(w * 0.02f))
                drawCircle(Color(0xFFEF476F), w * 0.025f, Offset(w * 0.38f, h * 0.53f))
                drawCircle(Color(0xFF3DBE6B), w * 0.025f, Offset(w * 0.38f, h * 0.59f))
            }
            "vestido" -> {
                // Florzinhas brancas e uma faixa na cintura.
                drawRect(Color(0x33000000), Offset(0f, h * 0.58f), Size(w, h * 0.025f))
                for ((u, v) in listOf(0.38f to 0.52f, 0.62f to 0.54f, 0.3f to 0.68f, 0.5f to 0.72f, 0.7f to 0.68f, 0.42f to 0.77f, 0.6f to 0.77f)) {
                    val c = Offset(w * u, h * v)
                    for (k in 0 until 5) {
                        val a = k * 2f * PI.toFloat() / 5f
                        drawCircle(Color(0xE6FFFFFF), w * 0.022f, Offset(c.x + cos(a) * w * 0.028f, c.y + sin(a) * w * 0.028f))
                    }
                    drawCircle(Color(0xFFFFD166), w * 0.018f, c)
                }
            }
            "macacao" -> {
                val jeans = Color(0xFF3E6FB0)
                drawRoundRect(jeans, Offset(w * 0.32f, h * 0.53f), Size(w * 0.36f, h * 0.3f), CornerRadius(w * 0.04f))
                drawRect(jeans, Offset(0f, h * 0.66f), Size(w, h * 0.2f))
                for (lado in listOf(-1f, 1f)) {
                    drawLine(jeans, Offset(w * (0.5f + lado * 0.2f), h * 0.44f), Offset(w * (0.5f + lado * 0.15f), h * 0.55f), strokeWidth = w * 0.06f)
                    drawCircle(Color(0xFFFFD166), w * 0.025f, Offset(w * (0.5f + lado * 0.14f), h * 0.555f))
                }
                drawRoundRect(Color(0xFF2F5A94), Offset(w * 0.42f, h * 0.58f), Size(w * 0.16f, h * 0.06f), CornerRadius(w * 0.02f), style = Stroke(w * 0.015f))
            }
            "futebol" -> {
                val verde = Color(0xFF2A9452)
                drawPath(Path().apply { moveTo(w * 0.38f, topo); lineTo(w * 0.5f, h * 0.52f); lineTo(w * 0.62f, topo); close() }, verde)
                drawRect(verde, Offset(w * 0.18f, topo), Size(w * 0.06f, base - topo))
                drawRect(verde, Offset(w * 0.76f, topo), Size(w * 0.06f, base - topo))
                // Bolinha de futebol no peito.
                val bola = Offset(w * 0.62f, h * 0.58f)
                drawCircle(Color.White, w * 0.065f, bola)
                drawCircle(Color(0xFF1E1E1E), w * 0.065f, bola, style = Stroke(w * 0.01f))
                drawPath(caminhoEstrela(bola, w * 0.03f), Color(0xFF1E1E1E))
            }
            "bailarina" -> for (i in 0 until 5) {
                drawPath(caminhoEstrela(Offset(w * (0.36f + (i * 0.17f) % 0.3f), h * (0.5f + i * 0.03f)), w * 0.03f, 4), Color(0xE6FFFFFF))
            }
            "chuva" -> {
                drawLine(Color(0x33000000), Offset(w * 0.5f, topo), Offset(w * 0.5f, base), strokeWidth = w * 0.015f)
                for (k in 0..2) drawRoundRect(Color(0xFF8D5A3B), Offset(w * 0.45f, h * (0.52f + k * 0.08f)), Size(w * 0.1f, h * 0.018f), CornerRadius(w * 0.01f))
                drawRoundRect(Color(0x26000000), Offset(w * 0.24f, h * 0.68f), Size(w * 0.14f, h * 0.05f), CornerRadius(w * 0.02f))
                drawRoundRect(Color(0x26000000), Offset(w * 0.62f, h * 0.68f), Size(w * 0.14f, h * 0.05f), CornerRadius(w * 0.02f))
            }
            "medico" -> {
                val cinza = Color(0xFFB8C2CF)
                drawLine(cinza, Offset(w * 0.4f, topo), Offset(w * 0.5f, h * 0.58f), strokeWidth = w * 0.02f)
                drawLine(cinza, Offset(w * 0.6f, topo), Offset(w * 0.5f, h * 0.58f), strokeWidth = w * 0.02f)
                drawLine(cinza, Offset(w * 0.5f, h * 0.58f), Offset(w * 0.5f, base), strokeWidth = w * 0.015f)
                drawRoundRect(cinza, Offset(w * 0.58f, h * 0.6f), Size(w * 0.13f, h * 0.06f), CornerRadius(w * 0.015f), style = Stroke(w * 0.012f))
                drawRect(Color(0xFFE63946), Offset(w * 0.635f, h * 0.608f), Size(w * 0.02f, h * 0.04f))
                drawRect(Color(0xFFE63946), Offset(w * 0.61f, h * 0.62f), Size(w * 0.07f, h * 0.013f))
                // Estetoscópio: o tubo desce do pescoço até o círculo de metal.
                val tubo = Path().apply { moveTo(w * 0.36f, topo); quadraticTo(w * 0.3f, h * 0.56f, w * 0.4f, h * 0.62f) }
                drawPath(tubo, Color(0xFF4A5568), style = Stroke(w * 0.02f))
                drawCircle(Color(0xFFB8C2CF), w * 0.035f, Offset(w * 0.4f, h * 0.635f))
            }
            "chef" -> {
                drawPath(Path().apply { moveTo(w * 0.4f, topo); lineTo(w * 0.5f, h * 0.5f); lineTo(w * 0.6f, topo); close() }, Color(0xFFE63946))
                for (col in listOf(0.42f, 0.58f)) for (k in 0..2) drawCircle(Color(0xFF5A6575), w * 0.018f, Offset(w * col, h * (0.54f + k * 0.06f)))
            }
            "pirata" -> {
                var y = topo + h * 0.02f
                while (y < base) {
                    drawRect(Color(0xFFE63946), Offset(0f, y), Size(w, h * 0.03f))
                    y += h * 0.065f
                }
                drawRect(Color(0xFF2B2B2B), Offset(0f, topo), Size(w * 0.33f, base - topo))
                drawRect(Color(0xFF2B2B2B), Offset(w * 0.67f, topo), Size(w * 0.33f, base - topo))
                drawRect(Color(0xFF6B3E22), Offset(0f, h * 0.66f), Size(w, h * 0.035f))
                drawRect(Color(0xFFFFD166), Offset(w * 0.46f, h * 0.655f), Size(w * 0.08f, h * 0.045f))
            }
            "realeza" -> {
                val ouro = Color(0xFFFFD166)
                drawRect(ouro, Offset(0f, topo), Size(w, h * 0.035f))
                drawLine(ouro, Offset(w * 0.5f, topo), Offset(w * 0.5f, base), strokeWidth = w * 0.03f)
                for (k in 0..2) drawCircle(ouro, w * 0.024f, Offset(w * 0.5f, h * (0.53f + k * 0.07f)))
                // Barra branca de pelúcia com pintinhas pretas.
                drawRect(Color.White, Offset(0f, base - h * 0.05f), Size(w, h * 0.05f))
                for (k in 0 until 6) drawCircle(Color(0xFF2B2B2B), w * 0.012f, Offset(w * (0.18f + k * 0.13f), base - h * 0.025f))
            }
            "dinossauro" -> {
                drawOval(Color(0xFFB8E986), Offset(w * 0.32f, h * 0.5f), Size(w * 0.36f, h * 0.25f))
                for (k in 0..3) drawLine(Color(0x334FB25E), Offset(w * 0.36f, h * (0.54f + k * 0.05f)), Offset(w * 0.64f, h * (0.54f + k * 0.05f)), strokeWidth = w * 0.012f)
            }
        }
    }
}

fun caminhoEstrela(centro: Offset, raio: Float, pontas: Int = 5): Path = Path().apply {
    for (i in 0 until pontas * 2) {
        val ang = -PI / 2 + i * PI / pontas
        val rr = if (i % 2 == 0) raio else raio * 0.45f
        val x = centro.x + cos(ang).toFloat() * rr
        val y = centro.y + sin(ang).toFloat() * rr
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
