package br.com.casinhadosaber.ui.componentes

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Trilha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Cenário das telas de jogo. No visual brinquedo 3D (Fase 4 do PRD visual 3D, maquete 2) é o céu em degradê do app
 * com a ilha de grama do caminho (`jogo_<caminho>_ilha`) por cima, composta uma vez por tamanho; as cenas das
 * perguntas (lagoa, mesa, varal, cercado, tapete) e as figuras 3D (`fig_*`) ficam em [FigurasNaCena].
 * Sem a arte, o cenário desenhado de antes: céu com a cor do caminho, sol, nuvens fofas, morros, arbustos e
 * florzinhas. É tudo parado (nada se mexe atrás das atividades, para não distrair).
 */

/** Azul do céu de dia; o céu de cada caminho é este azul puxado para a cor do caminho. */
private val CeuAzul = Color(0xFF7CC8FF)
private val Grama = Color(0xFF7BCB5A)
private val GramaClara = Color(0xFFA8DE7E)
private val GramaFunda = Color(0xFF5DB443)

/** Fundo de cenário das telas de jogo, na cor do caminho ([cor]). */
fun Modifier.cenarioDoJogo(cor: Color): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    onDrawBehind { if (w >= 1f && h >= 1f) desenharCenarioDoJogo(cor) }
}

/** O cenário inteiro no tamanho do [DrawScope]. Separado do modificador para poder ser visto fora do app. */
fun DrawScope.desenharCenarioDoJogo(cor: Color) {
    val w = size.width
    val h = size.height
    // O céu é a cor do caminho clareada (laranja vira céu de fim de tarde, roxo vira céu lilás); misturar com
    // azul deixava o laranja cinzento.
    val ceuDeCima = lerp(cor, Color.White, 0.3f)
    val ceuDeBaixo = lerp(lerp(cor, Color.White, 0.82f), Color(0xFFFFF7E6), 0.35f)
    // Onde começam os morros: abaixo do meio, para a grama aparecer entre a figura e as respostas.
    val horizonte = h * 0.6f
    drawRect(Brush.verticalGradient(0f to ceuDeCima, 1f to ceuDeBaixo, startY = 0f, endY = horizonte + h * 0.1f))

    // Sol com um halo largo, à direita, logo abaixo do balão da pergunta.
    val sol = Offset(w * 0.86f, h * 0.27f)
    val raioSol = minOf(w, h) * 0.06f
    drawCircle(Brush.radialGradient(0f to Color(0x66FFF3B0), 1f to Color(0x00FFF3B0), center = sol, radius = raioSol * 3.2f), raioSol * 3.2f, sol)
    for (k in 0 until 10) {
        val a = k * 2f * PI.toFloat() / 10f
        drawLine(
            Color(0xB3FFD23F),
            Offset(sol.x + cos(a) * raioSol * 1.35f, sol.y + sin(a) * raioSol * 1.35f),
            Offset(sol.x + cos(a) * raioSol * 1.75f, sol.y + sin(a) * raioSol * 1.75f),
            strokeWidth = raioSol * 0.22f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
    drawCircle(Color(0xFFFFD84D), raioSol, sol)
    drawCircle(Color(0x66FFFFFF), raioSol * 0.55f, Offset(sol.x - raioSol * 0.3f, sol.y - raioSol * 0.3f))

    // Nuvens fofas (bolinhas brancas com a barriga um pouco mais escura).
    nuvem(Offset(w * 0.16f, h * 0.28f), w * 0.09f)
    nuvem(Offset(w * 0.55f, h * 0.25f), w * 0.06f)
    nuvem(Offset(w * 0.06f, h * 0.5f), w * 0.06f)
    nuvem(Offset(w * 0.94f, h * 0.47f), w * 0.07f)

    // Morro do fundo (mais claro, azulado pela distância) e morro da frente.
    val morroDeTras = Path().apply {
        moveTo(0f, horizonte + h * 0.03f)
        cubicTo(w * 0.2f, horizonte - h * 0.07f, w * 0.38f, horizonte - h * 0.05f, w * 0.55f, horizonte + h * 0.0f)
        cubicTo(w * 0.72f, horizonte - h * 0.08f, w * 0.9f, horizonte - h * 0.06f, w, horizonte - h * 0.01f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(morroDeTras, lerp(GramaClara, CeuAzul, 0.25f))
    // Casinha pequenininha no morro de trás: o caminho de volta para casa.
    casinhaAoLonge(Offset(w * 0.8f, horizonte + h * 0.005f), w * 0.06f)
    val morroDaFrente = Path().apply {
        moveTo(0f, horizonte + h * 0.08f)
        cubicTo(w * 0.3f, horizonte + h * 0.0f, w * 0.6f, horizonte + h * 0.1f, w, horizonte + h * 0.04f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(morroDaFrente, Brush.verticalGradient(0f to Grama, 1f to GramaFunda, startY = horizonte, endY = h))
    // Brilho na crista do morro da frente.
    drawPath(
        Path().apply {
            moveTo(0f, horizonte + h * 0.08f)
            cubicTo(w * 0.3f, horizonte + h * 0.0f, w * 0.6f, horizonte + h * 0.1f, w, horizonte + h * 0.04f)
        },
        Color(0x55FFFFFF),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = h * 0.006f),
    )

    // Arbustos nos cantos e florzinhas espalhadas (na cor do caminho e em amarelo), sempre nas mesmas posições.
    arbusto(Offset(w * 0.06f, horizonte + h * 0.09f), w * 0.07f)
    arbusto(Offset(w * 0.95f, horizonte + h * 0.07f), w * 0.08f)
    val corDaFlor = lerp(cor, Color.White, 0.1f)
    val flores = listOf(
        0.12f to 0.7f, 0.3f to 0.665f, 0.47f to 0.69f, 0.62f to 0.675f, 0.88f to 0.72f,
        0.2f to 0.82f, 0.58f to 0.86f, 0.9f to 0.88f, 0.05f to 0.93f, 0.38f to 0.95f, 0.74f to 0.97f,
    )
    for ((i, f) in flores.withIndex()) {
        val c = Offset(w * f.first, h * f.second)
        val ehAmarela = i % 3 == 1
        flor(c, w * 0.014f, if (ehAmarela) Color(0xFFFFD23F) else corDaFlor)
    }
    // Tufinhos de grama.
    for ((u, v) in listOf(0.24f to 0.72f, 0.52f to 0.69f, 0.78f to 0.78f, 0.14f to 0.88f, 0.66f to 0.92f)) {
        tufo(Offset(w * u, h * v), w * 0.02f)
    }
}

private fun DrawScope.nuvem(c: Offset, r: Float) {
    val sombra = Color(0x1F6E8BB5)
    drawCircle(sombra, r * 0.62f, Offset(c.x - r * 0.62f, c.y + r * 0.12f))
    drawCircle(sombra, r * 0.78f, Offset(c.x + r * 0.02f, c.y + r * 0.08f))
    drawCircle(sombra, r * 0.6f, Offset(c.x + r * 0.68f, c.y + r * 0.14f))
    drawCircle(Color.White, r * 0.58f, Offset(c.x - r * 0.62f, c.y + r * 0.04f))
    drawCircle(Color.White, r * 0.8f, c)
    drawCircle(Color.White, r * 0.56f, Offset(c.x + r * 0.66f, c.y + r * 0.06f))
    drawRect(Color.White, Offset(c.x - r * 0.62f, c.y), Size(r * 1.3f, r * 0.6f))
}

private fun DrawScope.arbusto(c: Offset, r: Float) {
    drawCircle(GramaFunda, r * 0.75f, Offset(c.x - r * 0.6f, c.y))
    drawCircle(GramaFunda, r, Offset(c.x, c.y - r * 0.25f))
    drawCircle(GramaFunda, r * 0.7f, Offset(c.x + r * 0.65f, c.y + r * 0.05f))
    drawCircle(Color(0x3DFFFFFF), r * 0.35f, Offset(c.x - r * 0.25f, c.y - r * 0.55f))
    drawCircle(Color(0xFFFF6B6B), r * 0.1f, Offset(c.x + r * 0.3f, c.y - r * 0.3f))
    drawCircle(Color(0xFFFF6B6B), r * 0.1f, Offset(c.x - r * 0.55f, c.y + r * 0.05f))
}

private fun DrawScope.flor(c: Offset, r: Float, cor: Color) {
    drawLine(GramaFunda, c, Offset(c.x, c.y + r * 2.4f), strokeWidth = r * 0.45f)
    for (k in 0 until 5) {
        val a = k * 2f * PI.toFloat() / 5f
        drawCircle(cor, r * 0.62f, Offset(c.x + cos(a) * r * 0.75f, c.y + sin(a) * r * 0.75f))
    }
    drawCircle(Color(0xFFFFF3B0), r * 0.5f, c)
}

private fun DrawScope.tufo(c: Offset, r: Float) {
    for (k in -1..1) {
        drawLine(GramaFunda, Offset(c.x + k * r * 0.35f, c.y), Offset(c.x + k * r * 0.8f, c.y - r * (1.2f - kotlin.math.abs(k) * 0.3f)), strokeWidth = r * 0.28f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

/** Casinha de longe, igual à do jogo (telhado vermelho). [c] é o meio da base. */
private fun DrawScope.casinhaAoLonge(c: Offset, lado: Float) {
    val parede = Color(0xFFFFF4DC)
    drawRect(parede, Offset(c.x - lado / 2f, c.y - lado * 0.75f), Size(lado, lado * 0.75f))
    drawPath(
        Path().apply {
            moveTo(c.x - lado * 0.65f, c.y - lado * 0.72f)
            lineTo(c.x, c.y - lado * 1.3f)
            lineTo(c.x + lado * 0.65f, c.y - lado * 0.72f)
            close()
        },
        // O mesmo telhado vermelho da casinha do jogo.
        Color(0xFFEF476F),
    )
    drawRect(Color(0xFF9A6A4A), Offset(c.x - lado * 0.12f, c.y - lado * 0.38f), Size(lado * 0.24f, lado * 0.38f))
    drawRect(Color(0xFF8FD3FF), Offset(c.x + lado * 0.2f, c.y - lado * 0.6f), Size(lado * 0.2f, lado * 0.18f))
}

// ------------------------------------------------------------------------------------- visual brinquedo 3D

/** Céu do jogo (maquete 2), o mesmo da tela inicial: azul em cima, clarinho no meio, pêssego e rosa embaixo. */
private val CeuDoJogo = Brush.verticalGradient(
    0f to Color(0xFF7CC4FA),
    0.38f to Color(0xFFB8E2FF),
    0.66f to Color(0xFFFFE6D6),
    1f to Color(0xFFFFD0DC),
)

/**
 * Fundo das telas de jogo no visual 3D: o céu em degradê e a ilha do caminho [trilha] numa camada só (composta uma
 * vez por largura em [ComposicaoDoJogo]), com o centro da ilha entre o balão da pergunta e as respostas
 * ([QuadroDoJogo.daIlha]). Sem a arte da ilha, o cenário desenhado de antes na cor do caminho ([cor]).
 */
@Composable
fun Modifier.cenarioDoJogo(cor: Color, trilha: Trilha): Modifier {
    val contexto = LocalContext.current
    val info = remember(trilha) { ArteDoApp.info(contexto, QuadroDoJogo.idDaIlha(trilha)) }
    if (info == null) return cenarioDoJogo(cor)
    return this.drawWithCache {
        val w = size.width
        val h = size.height
        val quadro = QuadroDoJogo.daIlha(w, h)
        val ilha = if (w >= 1f && h >= 1f) ComposicaoDoJogo.camada(contexto, info, quadro.largura.roundToInt()) else null
        onDrawBehind {
            if (ilha == null) {
                if (w >= 1f && h >= 1f) desenharCenarioDoJogo(cor)
            } else {
                drawRect(CeuDoJogo)
                desenharNoQuadro(ilha, quadro)
            }
        }
    }
}

/** Desenha a camada [imagem] (quadro inteiro da arte do jogo) no [quadro]. */
private fun DrawScope.desenharNoQuadro(imagem: ImageBitmap, quadro: QuadroDaArte) {
    drawImage(
        image = imagem,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(imagem.width, imagem.height),
        dstOffset = IntOffset(quadro.x.roundToInt(), quadro.y.roundToInt()),
        dstSize = IntSize(quadro.largura.roundToInt().coerceAtLeast(1), quadro.altura.roundToInt().coerceAtLeast(1)),
    )
}

/**
 * As camadas grandes do jogo (ilhas e cenas comuns, 800 x 1040 px) prontas para desenhar numa largura, como a
 * casa ([ComposicaoDaCasa]): cada uma é lida sem passar pelo cache da [ArteDoApp], reduzida para a largura pedida
 * (arredondada em passos de 64 px, para a mesma tela não guardar várias quase iguais) e guardada aqui, num cache
 * pequeno (1/16 da memória do app, com lugar para a ilha e duas cenas: a da pergunta que sai e a da que entra).
 */
object ComposicaoDoJogo {
    private data class Chave(val id: String, val largura: Int)

    private class Composta(val bitmap: Bitmap, val imagem: ImageBitmap)

    private val cache = object : LruCache<Chave, Composta>(
        (Runtime.getRuntime().maxMemory() / 1024 / 16).toInt().coerceAtLeast(12 * 1024),
    ) {
        override fun sizeOf(key: Chave, value: Composta): Int = (value.bitmap.byteCount / 1024).coerceAtLeast(1)
    }

    /** A camada [info] com [largura] px (no máximo a da arte), ou nula se não der para ler. */
    fun camada(contexto: Context, info: InfoDaArte, largura: Int): ImageBitmap? {
        if (largura <= 0) return null
        val l = (((largura + 63) / 64) * 64).coerceIn(1, info.largura)
        val chave = Chave(info.id, l)
        cache.get(chave)?.let { return it.imagem }
        val a = (l.toLong() * info.altura / info.largura).toInt().coerceAtLeast(1)
        val lida = ArteDoApp.decodificar(contexto, info, amostraDaArte(info.largura, info.altura, l, a)) ?: return null
        val bmp = if (lida.width == l && lida.height == a) {
            lida
        } else {
            val reduzida = try {
                Bitmap.createScaledBitmap(lida, l, a, true)
            } catch (e: OutOfMemoryError) {
                null
            }
            if (reduzida !== lida) lida.recycle()
            reduzida ?: return null
        }
        bmp.prepareToDraw()
        val composta = Composta(bmp, bmp.asImageBitmap())
        cache.put(chave, composta)
        return composta.imagem
    }

    fun limparCache() = cache.evictAll()
}

/**
 * Uma figura das perguntas: a figura 3D (`fig_<nome>`) quando o [emoji] tem modelo (campo `emojis` do manifesto,
 * [FigurasDaPergunta]), ou o próprio emoji quando ainda não tem ([reserva]; por padrão o emoji do tamanho de
 * antes). A imagem é decorativa (quem fala o nome é o botão ou a peça em volta), como o emoji era.
 */
@Composable
fun FiguraDaPergunta(
    emoji: String,
    tamanho: Dp,
    modifier: Modifier = Modifier,
    reserva: (@Composable () -> Unit)? = null,
) {
    val contexto = LocalContext.current
    val id = remember(emoji) { FigurasDaPergunta.idPara(ArteDoApp.manifesto(contexto), emoji) }
    val doEmoji: @Composable () -> Unit = reserva ?: {
        Text(emoji, fontSize = (tamanho.value * 0.86f).sp, lineHeight = (tamanho.value * 1.0f).sp, maxLines = 1, softWrap = false)
    }
    Box(modifier.size(tamanho), contentAlignment = Alignment.Center) {
        if (id == null) doEmoji() else ImagemDeArte(id, Modifier.size(tamanho), reserva = doEmoji)
    }
}

/**
 * Figuras de contar numa cena 3D (maquete 2: os patinhos na lagoa): a cena comum do `lugar` das figuras (lagoa,
 * mesa, varal, cercado ou tapete) com cada figura numa vaga; figuras da grama e do céu (ou sem modelo) ficam
 * direto na grama da ilha do caminho [trilha], que está atrás ([cenarioDoJogo]). Mais figuras que vagas se
 * espalham em fileiras. Tudo cabe na caixa dada, centrado.
 */
@Composable
fun FigurasNaCena(figuras: List<String>, trilha: Trilha, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val manifesto = remember { ArteDoApp.manifesto(contexto) }
    val n = figuras.size
    val lugarDasFiguras = remember(figuras) { FigurasDaPergunta.lugarDaCena(manifesto, figuras) }
    val infoDaCena = remember(lugarDasFiguras, n) {
        lugarDasFiguras
            ?.takeIf { it in QuadroDoJogo.LUGARES_COM_CENA && !(it == "varal" && n > 5) }
            ?.let { manifesto.imagens[QuadroDoJogo.idDaCena(it)] }
    }
    val lugar = when {
        infoDaCena != null -> lugarDasFiguras
        lugarDasFiguras == "ceu" -> "ceu"
        else -> null
    }
    val vagas = remember(n, lugar, infoDaCena, trilha) {
        val ancoras = infoDaCena?.ancoras ?: manifesto.imagens[QuadroDoJogo.idDaIlha(trilha)]?.ancoras.orEmpty()
        QuadroDoJogo.vagas(n, ancoras, lugar)
    }
    BoxWithConstraints(modifier) {
        // Sem limite (dentro de uma rolagem), um quadrado da largura; sem largura, nada a desenhar.
        val w = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() else 0f
        val h = if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() else w
        val desenho = if (infoDaCena != null && lugar != null) QuadroDoJogo.desenhoDaCena(lugar) else null
        val caixa = QuadroDoJogo.caixaVisivel(vagas, desenho)
        // Com cena, ela não fica mais larga que a área; só figuras na grama podem crescer um pouco mais.
        val quadro = QuadroDoJogo.paraCaber(caixa, w, h, larguraMaxima = if (desenho != null) w * 1.1f else w * 1.3f)
        if (infoDaCena != null) {
            val larguraDaCena = quadro.largura.roundToInt()
            val camada = remember(infoDaCena, larguraDaCena) { ComposicaoDoJogo.camada(contexto, infoDaCena, larguraDaCena) }
            if (camada != null) Spacer(Modifier.fillMaxSize().drawBehind { desenharNoQuadro(camada, quadro) })
        }
        val densidade = LocalDensity.current
        for ((i, v) in vagas.withIndex()) {
            val emoji = figuras[i % n]
            val ladoPx = v.lado * quadro.escala
            val (fx, fy) = remember(emoji, v.pendurada) { pontoDeApoio(manifesto, emoji, v.pendurada) }
            FiguraDaPergunta(
                emoji,
                with(densidade) { ladoPx.toDp() },
                Modifier.offset {
                    IntOffset((quadro.emX(v.x) - fx * ladoPx).roundToInt(), (quadro.emY(v.y) - fy * ladoPx).roundToInt())
                },
            )
        }
    }
}

/** Onde a figura de [emoji] encosta na vaga, como fração da imagem: o pé (apoio) ou, pendurada, o topo. */
private fun pontoDeApoio(manifesto: ManifestoDaArte, emoji: String, pendurada: Boolean): Pair<Float, Float> {
    val info = FigurasDaPergunta.idPara(manifesto, emoji)?.let { manifesto.imagens[it] }
        ?: return if (pendurada) 0.5f to 0.1f else 0.5f to 0.92f
    val ponto = if (pendurada) info.ancoras["topo"] ?: info.apoio else info.apoio
    return info.fracao(ponto)
}
